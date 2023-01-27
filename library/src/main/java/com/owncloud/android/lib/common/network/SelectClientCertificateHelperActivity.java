/* The MIT license.

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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;

import com.owncloud.android.lib.R;
import com.owncloud.android.lib.common.utils.Log_OC;

import androidx.annotation.Nullable;

public class SelectClientCertificateHelperActivity extends Activity implements KeyChainAliasCallback {

    private static final String TAG = SelectClientCertificateHelperActivity.class.getCanonicalName();

    private static final int REQ_CODE_INSTALL_CERTS = 1;

    private int decisionId;
    private String hostname;
    private int port;

    private Dialog installCertsDialog = null;

    @Override
    public void onResume() {
        super.onResume();
        // Load data from intent
        Intent i = getIntent();
        decisionId = i.getIntExtra(AdvancedX509KeyManager.DECISION_INTENT_ID, AdvancedX509KeyManager.AKMDecision.DECISION_INVALID);
        hostname = i.getStringExtra(AdvancedX509KeyManager.DECISION_INTENT_HOSTNAME);
        port = i.getIntExtra(AdvancedX509KeyManager.DECISION_INTENT_PORT, -1);
        Log_OC.d(TAG, "onResume() with " + i.getExtras() + " decId=" + decisionId + " data=" + i.getData());
        if (installCertsDialog == null) {
            KeyChain.choosePrivateKeyAlias(this, this, null, null, null, -1, null);
        }
    }

    /**
     * Called with the alias of the certificate chosen by the user, or null if no value was chosen.
     *
     * @param alias The alias of the certificate chosen by the user, or null if no value was chosen.
     */
    @Override
    public void alias(@Nullable String alias) {
        // Show a dialog to add a certificate if no certificate was found
        // API Versions < 29 still handle this automatically
        if (alias == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runOnUiThread(() -> {
                installCertsDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.title_no_client_cert)
                        .setMessage(R.string.message_install_client_cert)
                        .setPositiveButton(
                                android.R.string.yes,
                                (dialog, which) -> startActivityForResult(KeyChain.createInstallIntent(), REQ_CODE_INSTALL_CERTS)
                        )
                        .setNegativeButton(android.R.string.no, (dialog, which) -> {
                            dialog.dismiss();
                            sendDecision(AdvancedX509KeyManager.AKMDecision.DECISION_ABORT, null);
                        })
                        .create();
                installCertsDialog.show();
            });
        } else {
            sendDecision(AdvancedX509KeyManager.AKMDecision.DECISION_KEYCHAIN, alias);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_INSTALL_CERTS) {
            installCertsDialog = null;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Stop the user interaction and send result to invoking AdvancedX509KeyManager.
     *
     * @param state type of the result as defined in AKMDecision
     * @param param keychain alias respectively keystore filename
     */
    void sendDecision(int state, String param) {
        Log_OC.d(TAG, "sendDecision(" + state + ", " + param + ", " + hostname + ", " + port + ")");
        AdvancedX509KeyManager.interactResult(decisionId, state, param, hostname, port);
        finish();
    }
}
