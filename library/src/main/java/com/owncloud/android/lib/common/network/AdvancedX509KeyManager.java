/* The MIT license.

Copyright (c) 2016-2022 Stephan Ritscher <no3pam@gmail.com>
Copyright (c) 2023 Elv1zz <elv1zz.git@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package com.owncloud.android.lib.common.network;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.security.KeyChain;
import android.security.KeyChainException;
import android.util.SparseArray;
import android.webkit.ClientCertRequest;

import com.owncloud.android.lib.R;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.URIException;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.net.ssl.X509KeyManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import okhttp3.HttpUrl;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.owncloud.android.lib.common.network.AdvancedX509KeyManager.AKMAlias.Type.KEYCHAIN;

/**
 * AdvancedX509KeyManager is an implementation of X509KeyManager that handles key management,
 * as well as user interaction to select an TLS client certificate, and also persist the selection.
 *
 * AdvancedX509KeyManager is based on
 * <a href="https://github.com/stephanritscher/InteractiveKeyManager">InteractiveKeyManager</a>
 * created by Stephan Ritscher.
 *
 * It was stripped down to reduce it to the most relevant parts and to directly include it
 * in nextcloud's android-library. (Removed features were file-based key stores and toast messages.)
 *
 * @author Elv1zz, elv1zz.git@gmail.com
 */
public class AdvancedX509KeyManager implements X509KeyManager, Application.ActivityLifecycleCallbacks {
   private final static String TAG = AdvancedX509KeyManager.class.getCanonicalName();
   private static final String NOTIFICATION_CHANNEL_ID = TAG + ".notifications";

   private final static String DECISION_INTENT = TAG + ".DECISION";
   final static String DECISION_INTENT_ID = DECISION_INTENT + ".decisionId";
   final static String DECISION_INTENT_PORT = DECISION_INTENT + ".port";
   final static String DECISION_INTENT_HOSTNAME = DECISION_INTENT + ".hostname";

   private final static String KEYCHAIN_ALIASES = "KeyChainAliases";

   private SharedPreferences sharedPreferences;

   final private Context context;
   private Activity foregroundAct;

   private final static int NOTIFICATION_ID = 23120;

   private static int decisionId = 0;
   final private static SparseArray<AKMDecision> openDecisions = new SparseArray<>();

   /**
    * Initialize AdvancedX509KeyManager
    * @param context application context (instance of Activity, Application, or Service)
    */
   public AdvancedX509KeyManager(@NonNull Context context) {
      this.context = context.getApplicationContext();
      init();
   }

   /**
    * Perform initialization of global variables (except context) and load settings
    */
   private void init() {
      // Determine application from context
      Application app;
      if (context instanceof Application) {
         app = (Application) context;
      } else if (context instanceof Service) {
         app = ((Service) context).getApplication();
      } else if (context instanceof Activity) {
         app = ((Activity) context).getApplication();
      } else {
         throw new ClassCastException("AdvancedX509KeyManager context must be either Activity, Application, or Service!");
      }
      // Initialize settings
      Log_OC.d(TAG, "init(): Loading SharedPreferences named " + context.getPackageName() + "." + "AdvancedX509KeyManager");
      sharedPreferences = context.getSharedPreferences(context.getPackageName() + "." + "AdvancedX509KeyManager",
              Context.MODE_PRIVATE);
      Log_OC.d(TAG, "init(): keychain aliases = " + Arrays.toString(
              sharedPreferences.getStringSet(KEYCHAIN_ALIASES, new HashSet<>()).toArray()));

      // Register callbacks to determine current activity
      app.registerActivityLifecycleCallbacks(this);
   }

   /**
    * Add KeyChain alias for use for connections to hostname:port
    * @param keyChainAlias alias returned from KeyChain.choosePrivateKeyAlias
    * @param hostname hostname for which the alias shall be used; null for any
    * @param port port for which the alias shall be used (only if hostname is not null); null for any
    * @return alias to be used in KEYCHAIN_ALIASES
    */
   public @NonNull String addKeyChain(@NonNull String keyChainAlias, String hostname,
                                                          Integer port) {
      String alias = new AKMAlias(KEYCHAIN, keyChainAlias, hostname, port).toString();
      Set<String> aliases = new HashSet<>(sharedPreferences.getStringSet(KEYCHAIN_ALIASES, new HashSet<>()));
      aliases.add(alias);
      SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.putStringSet(KEYCHAIN_ALIASES, aliases);
      if (editor.commit()) {
         Log_OC.d(TAG, "addKeyChain(keyChainAlias=" + keyChainAlias + ", hostname=" + hostname + ", port=" +
                 port + "): keychain aliases = " + Arrays.toString(aliases.toArray()));
      } else {
         Log_OC.e(TAG, "addKeyChain(keyChainAlias=" + keyChainAlias + ", hostname=" + hostname + ", port=" +
                 port + "): Could not save preferences");
      }
      return alias;
   }

   /**
    * Remove all KeyChain and keystore aliases
    */
   @SuppressWarnings("unused")
   public void removeAllKeys() {
      try {
         removeKeyChain(new AKMAlias(KEYCHAIN, null, null, null));
      } catch (IllegalArgumentException e) {
         Log_OC.e(TAG, "removeAllKeys()", e);
      }
   }

   /**
    * Remove KeyChain aliases for connections to given host URL
    *
    * @param url  URL for which the alias shall be removed.
    */
   public void removeKeys(String url) {
      try {
         removeKeys(new URL(url));
      } catch(MalformedURLException e) {
         Log_OC.e(TAG, "Tried to remove keys for malformed URL " + url, e);
      }
   }

   /**
    * Remove KeyChain aliases for connections to given host URL
    *
    * @param url  URL for which the alias shall be removed.
    */
   public void removeKeys(HttpUrl url) {
      removeKeys(url.url());
   }

   /**
    * Remove KeyChain aliases for connections to given host URI
    *
    * @param uri  URI for which the alias shall be removed.
    */
   public void removeKeys(org.apache.commons.httpclient.URI uri) {
      try {
         removeKeys(uri.getURI());
      } catch (URIException e) {
         Log_OC.e(TAG, "Tried to remove keys for a malformed URI", e);
      }
   }

   /**
    * Remove KeyChain aliases for connections to given host Uri
    *
    * @param uri  Uri for which the alias shall be removed.
    */
   public void removeKeys(Uri uri) {
      removeKeys(uri.toString());
   }

   /**
    * Remove KeyChain aliases for connections to given host URI
    *
    * @param uri  URI for which the alias shall be removed.
    */
   public void removeKeys(URI uri) {
      try {
         removeKeys(uri.toURL());
      } catch (MalformedURLException e) {
         Log_OC.e(TAG, "Tried to remove keys for a malformed URL", e);
      }
   }

   /**
    * Remove KeyChain aliases for connections to given host URL
    *
    * @param url  URL for which the alias shall be removed.
    */
    public void removeKeys(URL url) {
      int port = url.getPort() != -1 ? url.getPort() : url.getDefaultPort();
      removeKeys(url.getHost(), port);
   }

   /**
    * Remove KeyChain aliases for connections to hostname:port
    *
    * @param hostname hostname for which the alias shall be used; null for any
    * @param port port for which the alias shall be used (only if hostname is not null); null for any
    */
   @SuppressWarnings("unused")
   private void removeKeys(String hostname, Integer port) {
      try {
         removeKeyChain(new AKMAlias(KEYCHAIN, null, hostname, port));
      } catch (IllegalArgumentException e) {
         Log_OC.e(TAG, "removeKeys(hostname=" + hostname + ", port=" + port + ")", e);
      }
   }

   /**
    * Remove KeyChain aliases from KEYCHAIN_ALIASES based on filter and depending on causing
    * exception
    * @param filter AKMAlias object used as filter
    * @param e exception on retrieving certificate/key
    */
   private void removeKeyChain(AKMAlias filter, KeyChainException e) throws IllegalArgumentException {
      if (Objects.requireNonNull(e.getMessage()).contains("keystore is LOCKED")) {
            /* This exception occurs after the start before the password is entered on an
            encrypted device. Don't remove alias in this case. */
         return;
      }
      removeKeyChain(filter);
   }

   /**
    * Remove KeyChain aliases from KEYCHAIN_ALIASES based on filter
    * @param filter AKMAlias object used as filter
    */
   private void removeKeyChain(AKMAlias filter) throws IllegalArgumentException {
      Set<String> aliases = new HashSet<>();
      for (String alias : sharedPreferences.getStringSet(KEYCHAIN_ALIASES, new HashSet<>())) {
         AKMAlias akmAlias = new AKMAlias(alias);
         if (!akmAlias.matches(filter)) {
            aliases.add(alias);
         }
      }
      SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.putStringSet(KEYCHAIN_ALIASES, aliases);
      if (editor.commit()) {
         Log_OC.d(TAG, "removeKeyChain(filter=" + filter + "): keychain aliases = " +
                 Arrays.toString(aliases.toArray()));
      } else {
         Log_OC.e(TAG, "removeKeyChain(filter=" + filter + "): Could not save preferences");
      }
   }

   /**
    * Get all KeyChain aliases matching the filter
    * @param aliases collection of objects whose string representation is as returned from AKMAlias.toString()
    * @param filter AKMAlias object used as filter
    * @return all aliases from KEYCHAIN_ALIASES which satisfy alias.matches(filter)
    */
   private static <T> Collection<String> filterAliases(Collection<T> aliases, AKMAlias filter) {
      Collection<String> filtered = new LinkedList<>();
      for (Object alias : aliases) {
         if (new AKMAlias(alias.toString()).matches(filter)) {
            filtered.add(((String) alias));
         }
      }
      return filtered;
   }

   /**
    * Get keychain aliases for use for connections to hostname:port
    * @param keyTypes accepted keyTypes; null for any
    * @param issuers  issuers; null for any
    * @param hostname hostname of connection; null for any
    * @param port port of connection; null for any
    * @return array of aliases
    */
   private @NonNull String[] getAliases(Set<KeyType> keyTypes, Principal[] issuers, String hostname, Integer port) {
      // Check keychain aliases
      AKMAlias filter = new AKMAlias(KEYCHAIN, null, hostname, port);
      List<String> validAliases = new LinkedList<>(filterAliases(sharedPreferences.getStringSet(KEYCHAIN_ALIASES, new HashSet<>()), filter));

      Log_OC.d(TAG, "getAliases(keyTypes=" + (keyTypes != null ? Arrays.toString(keyTypes.toArray()) : null)
              + ", issuers=" + Arrays.toString(issuers)
              + ", hostname=" + hostname
              + ", port=" + port
              + ") = " + Arrays.toString(validAliases.toArray()));
      return validAliases.toArray(new String[0]);
   }

   /**
    * Choose an alias for a connection, prompting for interaction if no stored alias is found
    * @param keyTypes accepted keyTypes; null for any
    * @param issuers accepted issuers; null for any
    * @param socket connection socket
    * @return keychain alias to use for this connection
    */
   private String chooseAlias(String[] keyTypes, Principal[] issuers, @NonNull Socket socket) {
      // Determine connection parameters
      String hostname = socket.getInetAddress().getHostName();
      int port = socket.getPort();
      return chooseAlias(keyTypes, issuers, hostname, port);
   }

   /**
    * Choose an alias for a connection, prompting for interaction if no stored alias is found
    * @param keyTypes accepted keyTypes; null for any
    * @param issuers accepted issuers; null for any
    * @param hostname hostname of connection
    * @param port port of connection
    * @return keychain alias to use for this connection
    */
   private String chooseAlias(String[] keyTypes, Principal[] issuers, @NonNull String hostname, int port) {
      // Select certificate for one connection at a time. This is important if multiple connections to the same host
      // are started in a short time and avoids prompting the user with multiple dialogs for the same host.
      synchronized (AdvancedX509KeyManager.class) {
         // Get stored aliases for connection
         String[] validAliases = getAliases(KeyType.parse(Arrays.asList(keyTypes)), issuers, hostname, port);
         if (validAliases.length > 0) {
            Log_OC.d(TAG, "chooseAlias(keyTypes=" + Arrays.toString(keyTypes) + ", issuers=" + Arrays.toString(issuers)
                    + ", hostname=" + hostname + ", port=" + port + ") = " + validAliases[0]);
            // Return first alias found
            return validAliases[0];
         } else {
            Log_OC.d(TAG, "chooseAlias(keyTypes=" + Arrays.toString(keyTypes) + ", issuers=" + Arrays.toString(issuers)
                    + ", hostname=" + hostname + ", port=" + port + "): no matching alias found, prompting user...");
            AKMDecision decision = interactClientCert(hostname, port);
            String alias;
            switch (decision.state) {
               case AKMDecision.DECISION_KEYCHAIN: // Add keychain alias for connection
                  alias = addKeyChain(decision.param, decision.hostname, decision.port);
                  Log_OC.d(TAG, "chooseAlias(keyTypes=" + Arrays.toString(keyTypes) + ", issuers=" +
                          Arrays.toString(issuers) + ", hostname=" + hostname + ", port=" + port + "): Use alias " +
                          alias);
                  return alias;
               case AKMDecision.DECISION_ABORT:
                  Log_OC.w(TAG, "chooseAlias(keyTypes=" + Arrays.toString(keyTypes) + ", issuers=" +
                          Arrays.toString(issuers) + ", hostname=" + hostname + ", port=" + port + ") - no alias selected");
                  return null;
               default:
                  throw new IllegalArgumentException("Unknown decision state " + decision.state);
            }
         }
      }
   }

   @Override
   public String chooseClientAlias(String[] keyTypes, Principal[] issuers, @NonNull Socket socket) {
      Log_OC.d(TAG, "chooseClientAlias(keyTypes=" + Arrays.toString(keyTypes) + ", issuers=" + Arrays.toString(issuers) + ")");
      try {
         return chooseAlias(keyTypes, issuers, socket);
      } catch (Throwable t) {
         Log_OC.e(TAG, "chooseClientAlias", t);
         return null;
      }
   }

   @Override
   public String chooseServerAlias(String keyType, Principal[] issuers, @NonNull Socket socket) {
      Log_OC.d(TAG, "chooseServerAlias(keyType=" + keyType + ", issuers=" + Arrays.toString(issuers) + ")");
      return chooseAlias(new String[]{keyType}, issuers, socket);
   }

   @Override
   public String[] getClientAliases(String keyType, Principal[] issuers) {
      Log_OC.d(TAG, "getClientAliases(keyType=" + keyType + ", issuers=" + Arrays.toString(issuers) + ")");
      return getAliases(KeyType.parse(Collections.singletonList(keyType)), issuers, null, null);
   }

   @Override
   public String[] getServerAliases(String keyType, Principal[] issuers) {
      Log_OC.d(TAG, "getServerAliases(keyType=" + keyType + ", issuers=" + Arrays.toString(issuers) + ")");
      return getAliases(KeyType.parse(Collections.singletonList(keyType)), issuers, null, null);
   }

   @Override
   public X509Certificate[] getCertificateChain(@NonNull String alias) {
      Log_OC.d(TAG, "getCertificateChain(alias=" + alias + ")");
      AKMAlias akmAlias = new AKMAlias(alias);
      if (akmAlias.getType() == KEYCHAIN) {
         try {
            X509Certificate[] certificateChain = KeyChain.getCertificateChain(context, akmAlias.getAlias());
            if (certificateChain == null) {
               throw new KeyChainException("could not retrieve certificate chain for alias " + akmAlias.getAlias());
            }
            return certificateChain;
         } catch (KeyChainException e) {
            Log_OC.e(TAG, "getCertificateChain(alias=" + alias + ") - keychain alias=" + akmAlias.getAlias(), e);
            removeKeyChain(akmAlias, e);
            return null;
         } catch (InterruptedException e) {
            Log_OC.d(TAG, "getCertificateChain(alias=" + alias + ")", e);
            Thread.currentThread().interrupt();
            return null;
         }
      } else {
         throw new IllegalArgumentException("Invalid alias");
      }
   }

   @Override
   public PrivateKey getPrivateKey(@NonNull String alias) {
      Log_OC.d(TAG, "getPrivateKey(alias=" + alias + ")");
      AKMAlias akmAlias = new AKMAlias(alias);
      if (akmAlias.getType() == KEYCHAIN) {
         try {
            PrivateKey key = KeyChain.getPrivateKey(context, akmAlias.getAlias());
            if (key == null) {
               throw new KeyChainException("could not retrieve private key for alias " + akmAlias.getAlias());
            }
            return key;
         } catch (KeyChainException e) {
            Log_OC.e(TAG, "getPrivateKey(alias=" + alias + ")", e);
            removeKeyChain(akmAlias, e);
            return null;
         } catch (InterruptedException e) {
            Log_OC.d(TAG, "getPrivateKey(alias=" + alias + ")", e);
            Thread.currentThread().interrupt();
            return null;
         }
      } else {
         throw new IllegalArgumentException("Invalid alias");
      }
   }

   @SuppressWarnings("unused")
   @RequiresApi(21)
   public void handleWebViewClientCertRequest(@NonNull final ClientCertRequest request) {
      Log_OC.d(TAG, "handleWebViewClientCertRequest(keyTypes=" + Arrays.toString(request.getKeyTypes()) +
              ", issuers=" + Arrays.toString(request.getPrincipals()) + ", hostname=" + request.getHost() +
              ", port=" + request.getPort() + ")");
      new Thread() {
         @Override
         public void run() {
            String alias = chooseAlias(
                    request.getKeyTypes(),
                    request.getPrincipals(),
                    request.getHost(),
                    request.getPort()
            );
            if (alias != null) {
               PrivateKey key = getPrivateKey(alias);
               X509Certificate[] chain = getCertificateChain(alias);
               if (key != null && chain != null) {
                  Log_OC.d(TAG, "handleWebViewClientCertRequest: proceed, alias = " + alias);
                  request.proceed(key, chain);
                  return;
               }
            }
            Log_OC.d(TAG, "handleWebViewClientCertRequest: ignore, alias = " + alias);
            request.ignore();
         }
      }.start();
   }

   @SuppressWarnings("unused")
   public void handshakeFailed(Socket socket) throws IOException {
      InputStream is = socket.getInputStream();
      int len = is.available();
      byte[] buffer = new byte[len];
      is.mark(len + 1);
      len = is.read(buffer, 0, len);
      is.reset();
      Log_OC.e(TAG, "handshakeFailed: " + new String(buffer, 0, len, Charset.defaultCharset()));
   }

   /**
    * Generate a unique identifier for a decision and remember it in openDecisions
    *
    * @param decision decision to remember
    * @return unique decision identifier
    */
   private static int createDecisionId(@NonNull AKMDecision decision) {
      int id;
      synchronized (openDecisions) {
         id = decisionId;
         openDecisions.put(id, decision);
         decisionId += 1;
      }
      return id;
   }

   private void startActivityNotification(@NonNull Intent intent, int decisionId, @NonNull String message) {
      int flags = 0;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
         flags |= PendingIntent.FLAG_IMMUTABLE;
      }
      final PendingIntent call = PendingIntent.getActivity(context, 0, intent, flags);
      NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         NotificationChannel channel = new NotificationChannel(
                 NOTIFICATION_CHANNEL_ID,
                 context.getString(R.string.notification_channel_name),
                 NotificationManager.IMPORTANCE_DEFAULT);
         notificationManager.createNotificationChannel(channel);
      }
      final Notification notification = new NotificationCompat
              .Builder(context, NOTIFICATION_CHANNEL_ID)
              .setContentTitle(context.getString(R.string.notification_title_select_client_cert))
              .setContentText(message)
              .setTicker(message)
              .setSmallIcon(android.R.drawable.ic_lock_lock)
              .setWhen(System.currentTimeMillis())
              .setContentIntent(call)
              .setAutoCancel(true)
              .build();

      if (ActivityCompat.checkSelfPermission(context, POST_NOTIFICATIONS) == PERMISSION_GRANTED) {
         notificationManager.notify(NOTIFICATION_ID + decisionId, notification);
      } else {
         Log_OC.w(TAG, "Cannot send notification due to missing permission.");
      }
   }

   /**
    * Display an Android system dialog where the user can select a client certificate for the
    * connection.
    * @param hostname hostname of connection
    * @param port port of connection
    * @return decision object with result of user interaction
    */
   private @NonNull AKMDecision interactClientCert(@NonNull final String hostname, final int port) {
      Log_OC.d(TAG, "interactClientCert(hostname=" + hostname + ", port=" + port + ")");

      final AKMDecision decision = new AKMDecision();
      final int id = createDecisionId(decision);

      Intent ni = new Intent(context, SelectClientCertificateHelperActivity.class);
      ni.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      ni.setData(Uri.parse(SelectClientCertificateHelperActivity.class.getName() + "/" + id));
      ni.putExtra(DECISION_INTENT_ID, id);
      ni.putExtra(DECISION_INTENT_HOSTNAME, hostname);
      ni.putExtra(DECISION_INTENT_PORT, port);

      // we try to directly start the activity and fall back to making a notification
      // e.g. when the app is in the background and we cannot just start a new activity
      try {
         getTopMostContext().startActivity(ni);
      } catch (Exception e) {
         Log_OC.d(TAG, "interactClientCert: startActivity(SelectClientCertificateHelperActivity)", e);
         startActivityNotification(ni, id, context.getString(R.string.notification_message_select_client_cert, hostname, port));
      }

      // wait for user decision
      try {
         synchronized (decision) { // Lint warns that decision is local, but in fact it is persisted in openDecisions
            decision.wait();
         }
      } catch (InterruptedException e) {
         Log_OC.d(TAG, "interactClientCert: InterruptedException", e);
         Thread.currentThread().interrupt();
      }

      return decision;
   }

   /**
    * Callback for SelectKeyStoreActivity to set the decision result.
    * @param decisionId decision identifier
    * @param state type of the result as defined in IKMDecision
    * @param param keychain alias respectively keystore filename
    * @param hostname hostname of connection
    * @param port port of connection
    */
   static void interactResult(int decisionId, int state, String param, String hostname, Integer port) {
      AKMDecision decision;
      Log_OC.d(TAG, "interactResult(decisionId=" + decisionId + ", state=" + state + ", param=" + param +
              ", hostname=" + hostname + ", port=" + port);
      // Get decision object
      synchronized (openDecisions) {
         decision = openDecisions.get(decisionId);
         openDecisions.remove(decisionId);
      }
      if (decision == null) {
         Log_OC.e(TAG, "interactResult: aborting due to stale decision reference!");
         return;
      }
      // Fill in result
      synchronized (decision) {  // Lint warns that decision is local, but in fact it is persisted in openDecisions
         decision.state = state;
         decision.param = param;
         decision.hostname = hostname;
         decision.port = port;
         decision.notify();
      }
   }

   /**
    * Empty implementation of {@link Application.ActivityLifecycleCallbacks#onActivityCreated}.
    *
    * This method gets called when a new {@link Activity} gets created. However, we are only interested in resumed and
    * paused Activities to determine the current foreground Activity.
    *
    * @param activity The newly created Activity.
    * @param savedInstanceState This value can be null.
    */
   @Override
   public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

   /**
    * Empty implementation of {@link Application.ActivityLifecycleCallbacks#onActivityStarted}.
    *
    * This method gets called when an {@link Activity} gets started. However, we are only interested in resumed and
    * paused Activities to determine the current foreground Activity.
    *
    * @param activity The started Activity.
    */
   @Override
   public void onActivityStarted(Activity activity) {}

   private int resumes = 0;

   /**
    * Remember the current foreground Activity.
    *
    * This method gets called when an {@link Activity} gets resumed. We use this to remember the Activity
    * currently being in the foreground.
    *
    * @param activity The resumed Activity
    */
   @Override
   public void onActivityResumed(Activity activity) {
      ++resumes;
      foregroundAct = activity;
   }

   /**
    * Keep track of paused Activities.
    *
    * This method gets called when an {@link Activity} gets paused. We use this to keep track of paused Activities
    * to find the point when there are no more Activities in the "resumed" state. Then we forget the last foreground
    * Activity.
    *
    * @param activity The resumed Activity
    */
   @Override
   public void onActivityPaused(Activity activity) {
      --resumes;
      // it might happen that the previously active Activity enters "paused" state
      // AFTER the new Activity entered "resumed" state. in that case we would forget
      // our Activity by mistake. therefore we try to keep track of resumes and pauses.
      if (resumes == 0) {
         foregroundAct = null;
      }
   }

   /**
    * Empty implementation of {@link Application.ActivityLifecycleCallbacks#onActivityStopped}.
    *
    * This method gets called when an {@link Activity} gets stopped. However, we are only interested in resumed and
    * paused Activities to determine the current foreground Activity.
    *
    * @param activity The stopped Activity.
    */
   @Override
   public void onActivityStopped(Activity activity) {}

   /**
    * Empty implementation of {@link Application.ActivityLifecycleCallbacks#onActivitySaveInstanceState}.
    *
    * This method gets called when an {@link Activity} saves its state. However, we are only interested in resumed and
    * paused Activities to determine the current foreground Activity.
    *
    * @param activity The Activity saving its state.
    */
   @Override
   public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

   /**
    * Empty implementation of {@link Application.ActivityLifecycleCallbacks#onActivityDestroyed(Activity)}.
    *
    * This method gets called when an {@link Activity} gets destroyed. However, we are only interested in resumed and
    * paused Activities to determine the current foreground Activity.
    *
    * @param activity The destroyed Activity.
    */
   @Override
   public void onActivityDestroyed(Activity activity) {}

   /**
    * Returns the top-most entry of the activity stack.
    *
    * @return the context of the currently bound UI or the master context if none is bound
    */
   private Context getTopMostContext() {
      return (foregroundAct != null) ? foregroundAct : context;
   }

   static class AKMDecision {
      public final static int DECISION_INVALID = 0;
      public final static int DECISION_ABORT = 1;
      public final static int DECISION_KEYCHAIN = 2;

      public int state = DECISION_INVALID;
      public String param;
      public String hostname;
      public Integer port;
   }

   static class AKMAlias {
      private final static String TAG = AKMAlias.class.getCanonicalName();

      enum Type {
         KEYCHAIN("KC_"),
         KEYSTORE("KS_");

         private final String prefix;

         Type(String prefix) {
            this.prefix = prefix;
         }

         public String getPrefix() {
            return prefix;
         }

         public static Type parse(String prefix) throws IllegalArgumentException {
            for (Type type : Type.values()) {
               if (type.getPrefix().equals(prefix)) {
                  return type;
               }
            }
            throw new IllegalArgumentException("unknown prefix");
         }
      }

      private final Type type;
      private final String alias;
      private final String hostname;
      private final Integer port;

      /**
       * Constructor of AKMAlias
       *
       * @param type type of alias (KEYCHAIN or KEYSTORE)
       * @param alias alias returned from KeyChain.choosePrivateKeyAlias respectively PrivateKey.hashCode
       * @param hostname hostname for which the alias shall be used; null for any
       * @param port port for which the alias shall be used (only if hostname is not null); null for any
       */
      public AKMAlias(Type type, String alias, String hostname, Integer port) {
         this.type = type;
         this.alias = alias;
         this.hostname = hostname;
         this.port = port;
      }

      /**
       * Constructor of AKMAlias
       *
       * @param alias value returned from AKMAlias.toString()
       */
      public AKMAlias(String alias) throws IllegalArgumentException {
         String[] aliasFields = alias.split(":");
         if (aliasFields.length > 3 || aliasFields[0].length() < 4) {
            throw new IllegalArgumentException("alias was not returned by AKMAlias.toString(): " + alias);
         }
         this.type = Type.parse(aliasFields[0].substring(0, 3));
         this.alias = aliasFields[0].substring(3);
         this.hostname = aliasFields.length > 1 ? aliasFields[1] : null;
         this.port = aliasFields.length > 2 ? Integer.valueOf(aliasFields[2]) : null;
      }

      public Type getType() {
         return type;
      }

      public String getAlias() {
         return alias;
      }

      @SuppressWarnings("unused")
      public String getHostname() {
         return hostname;
      }

      @SuppressWarnings("unused")
      public Integer getPort() {
         return port;
      }

      @NonNull
      @Override
      public String toString() {
         StringBuilder constructedAlias = new StringBuilder();
         constructedAlias.append(type.getPrefix());
         constructedAlias.append(alias);
         if (hostname != null) {
            constructedAlias.append(":");
            constructedAlias.append(hostname);
            if (port != null) {
               constructedAlias.append(":");
               constructedAlias.append(port);
            }
         }
         return constructedAlias.toString();
      }

      @Override
      public boolean equals(Object object) {
         if (!(object instanceof AKMAlias)) {
            return false;
         }
         AKMAlias other = (AKMAlias) object;
         return Objects.equals(type, other.type) &&
                 Objects.equals(alias, other.alias) &&
                 Objects.equals(hostname, other.hostname) &&
                 Objects.equals(port, other.port);
      }

      /**
       * @param filter AKMAlias object used as filter
       * @return true if each non-null field of filter equals the same field of this instance; false otherwise
       * Exception: both hostname fields are resolved to an ip address before comparing if possible.
       */
      public boolean matches(@NonNull AKMAlias filter) {
         if (filter.type != null && !filter.type.equals(type)) {
            Log_OC.d(TAG, "matches: alias " + this + " does not match type " + filter.type);
            return false;
         }
         if (filter.alias != null && !filter.alias.equals(alias)) {
            Log_OC.d(TAG, "matches: alias " + this + " does not match original alias " + filter.alias);
            return false;
         }
         if (hostname != null && filter.hostname != null && !filter.hostname.equals(hostname)) {
            // Resolve hostname fields to ip addresses
            InetAddress address = null, filterAddress = null;
            try {
               address = InetAddress.getByName(hostname);
            } catch (UnknownHostException e) {
               Log_OC.w(TAG, "matches: error resolving " + hostname);
            }
            try {
               filterAddress = InetAddress.getByName(filter.hostname);
            } catch (UnknownHostException e) {
               Log_OC.w(TAG, "matches: error resolving " + filter.hostname);
            }
            // If resolution succeeded, compare addresses, otherwise host names
            if ((address == null || !address.equals(filterAddress))) {
               Log_OC.d(TAG, "matches: alias " + this + " (address=" + address + ") does not match hostname " +
                       filter.hostname + " (address=" + filterAddress + ")");
               return false;
            }
         }
         if (port != null && filter.port != null && !filter.port.equals(port)) {
            Log_OC.d(TAG, "matches: alias " + this + " does not match port " + filter.port);
            return false;
         }
         return true;
      }
   }

   private enum KeyType {
      RSA("RSA"),
      EC("EC", "ECDSA");

      private final Set<String> names;

      KeyType(String... names) {
         this.names = new HashSet<>(Arrays.asList(names));
      }

      public Set<String> getNames() {
         return names;
      }

      public static KeyType parse(String keyType) {
         for (KeyType type : KeyType.values()) {
            if (type.getNames().contains(keyType)) {
               return type;
            }
         }
         throw new IllegalArgumentException("unknown prefix");
      }

      public static Set<KeyType> parse(Iterable<String> keyTypes) {
         Set<KeyType> keyTypeSet = new HashSet<>();
         if (keyTypes != null) {
            for (String keyType : keyTypes) {
               keyTypeSet.add(parse(keyType));
            }
         }
         return keyTypeSet;
      }
   }
}
