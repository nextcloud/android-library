package com.owncloud.android.lib.common.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;


public class Log_OC {

    private static Adapter impl = new LegacyImpl();

    /**
     * This adapter allows external library users to hook up
     * application-specific logging framework, redirecting calls to legacy
     * logging functions to new or custom implementation.
     */
    public interface Adapter {
        void i(String tag, String message);
        void d(String tag, String message);
        void d(String tag, String message, Exception e);
        void e(String tag, String message);
        void e(String tag, String message, Throwable t);
        void v(String tag, String message);
        void w(String tag, String message);
        void wtf(String tag, String message);
    }

    /**
     * This is legacy logger implementation extracted to allow
     * the code to compile and run without hiccup.
     *
     * Generally don't use it.
     */
    static class LegacyImpl implements Adapter {

        @Override
        public void i(final String tag, final String message) {
            if (tag != null && message != null) {
                Log.i(tag, message);
                appendLog(tag + ": " + message);
            }
        }

        @Override
        public void d(final String tag, final String message) {
            if (tag != null && message != null) {
                Log.d(tag, message);
                appendLog(tag + ": " + message);
            }
        }

        @Override
        public void d(final String tag, final String message, Exception e) {
            if (tag != null && message != null && e != null) {
                Log.d(tag, message, e);
                StackTraceElement[] trace = e.getStackTrace();
                appendLog(tag + ": " + message + " Exception: " + Arrays.toString(trace));
            }
        }

        @Override
        public void e(final String tag, final String message) {
            if (tag != null && message != null) {
                Log.e(tag, message);
                appendLog(tag + ": " + message);
            }
        }

        @Override
        public void e(final String tag, final String message, final Throwable t) {
            if (tag != null && message != null && t != null) {
                Log.e(tag, message, t);
                StackTraceElement[] trace = t.getStackTrace();
                appendLog(tag + ": " + message + " Exception: " + Arrays.toString(trace));
            }
        }

        @Override
        public void v(final String tag, final String message) {
            if (tag != null && message != null) {
                Log.v(tag, message);
                appendLog(tag + ": " + message);
            }
        }

        @Override
        public void w(final String tag, final String message) {
            if (tag != null && message != null) {
                Log.w(tag, message);
                appendLog(tag + ": " + message);
            }
        }

        @Override
        public void wtf(final String tag, final String message) {
            if (tag != null && message != null) {
                Log.wtf(tag, message);
                appendLog(tag + ": " + message);
            }
        }
    }

    private static final String SIMPLE_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
    private static final String LOG_FOLDER_NAME = "log";
    private static final long MAX_FILE_SIZE = 1000000; // 1MB

    private static final String TAG = Log_OC.class.getSimpleName();

    private static String mNextcloudDataFolderLog;

    private static File mLogFile;
    private static File mFolder;
    private static BufferedWriter mBuf;

    private static String[] mLogFileNames = {"currentLog.txt", "olderLog.txt"};

    private static boolean isMaxFileSizeReached = false;
    private static boolean isEnabled = false;

    public static String getLogPath() {
        return mNextcloudDataFolderLog;
    }

    /**
     * Plug your own logger implementation. Call this as early as possible.
     *
     * @param adapter Custom logger implementation.
     */
    public static void setLoggerImplementation(Adapter adapter) {
        impl = adapter;
    }

    public static void i(String TAG, String message) {
        impl.i(TAG, message);
    }

    public static void i(Object object, String message) {
        i(object.getClass().getSimpleName(), message);
    }

    public static void d(String TAG, String message) {
        impl.d(TAG, message);
    }

    public static void d(Object object, String message) {
        d(object.getClass().getSimpleName(), message);
    }

    public static void d(String TAG, String message, Exception e) {
        impl.d(TAG, message, e);
    }

    public static void d(Object object, String message, Exception e) {
        d(object.getClass().getSimpleName(), message, e);
    }

    public static void e(String TAG, String message) {
        impl.e(TAG, message);
    }

    public static void e(Object object, String message) {
        e(object.getClass().getSimpleName(), message);
    }

    public static void e(String TAG, String message, Throwable e) {
        impl.e(TAG, message, e);
    }

    public static void e(Object object, String message, Throwable e) {
        e(object.getClass().getSimpleName(), message, e);
    }

    public static void v(String TAG, String message) {
        impl.v(TAG, message);
    }

    public static void v(Object object, String message) {
        v(object.getClass().getSimpleName(), message);
    }

    public static void w(String TAG, String message) {
        impl.w(TAG, message);
    }

    public static void w(Object object, String message) {
        w(object.getClass().getSimpleName(), message);
    }

    public static void wtf(String TAG, String message) {
        impl.wtf(TAG, message);
    }

    public static void wtf(Object object, String message) {
        wtf(object.getClass().getSimpleName(), message);
    }

    /**
     * Start logging
     *
     * @param context Context: used for determinated app specific folder
     */
    synchronized public static void startLogging(Context context) {
        mNextcloudDataFolderLog = context.getFilesDir().getAbsolutePath() + File.separator + LOG_FOLDER_NAME;
        mFolder = new File(mNextcloudDataFolderLog);
        mLogFile = new File(mFolder + File.separator + mLogFileNames[0]);

        boolean isFolderCreated;

        if (!mFolder.exists()) {
            isFolderCreated = mFolder.mkdirs();
            Log.d(TAG, "Log folder created at: " + mNextcloudDataFolderLog);
        } else {
            isFolderCreated = true;
        }

        try {
            // Create the current log file if does not exist
            isEnabled = mLogFile.createNewFile();
            mBuf = new BufferedWriter(new FileWriter(mLogFile, true));

            if (isFolderCreated && isEnabled) {
                appendPhoneInfo();
            }

        } catch (IOException e) {
            Log.e(TAG, "Log initialization failed", e);
        } finally {
            if (mBuf != null) {
                try {
                    mBuf.close();
                } catch (IOException e) {
                    Log.e(TAG, "Initialization finishing failed", e);
                }
            }
        }
    }

    synchronized public static void stopLogging() {
        try {
            if (mBuf != null) {
                mBuf.close();
            }

            mLogFile = null;
            mFolder = null;
            mBuf = null;
            isMaxFileSizeReached = false;
            isEnabled = false;
        } catch (IOException e) {
            // Because we are stopping logging, we only log to Android console.
            Log.e(TAG, "Closing log file failed: ", e);
        } catch (Exception e) {
            // This catch should never fire because we do null check on mBuf.
            // But just for the sake of stability let's log this odd situation.
            // Because we are stopping logging, we only log to Android console.
            Log.e(TAG, "Stopping logging failed: ", e);
        }
    }

    /**
     * Delete history logging
     */
    public static void deleteHistoryLogging() {
        File folderLogs = new File(mFolder + File.separator);
        if (folderLogs.isDirectory()) {
            String[] files = folderLogs.list();
            for (String file : files) {
                new File(folderLogs, file).delete();
            }
        }
    }

    /**
     * Append the info of the device
     */
    private static void appendPhoneInfo() {
        i("Device", "Model: " + android.os.Build.MODEL);
        i("Device", "Brand: " + android.os.Build.BRAND);
        i("Device", "Product: " + android.os.Build.PRODUCT);
        i("Device", "Device: " + android.os.Build.DEVICE);
        i("Device", "Version-Codename: " + android.os.Build.VERSION.CODENAME);
        i("Device", "Version-Release: " + android.os.Build.VERSION.RELEASE);
    }

    /**
     * Append to the log file the info passed
     *
     * @param text : text for adding to the log file
     */
    synchronized private static void appendLog(String text) {

        if (isEnabled) {
            if (isMaxFileSizeReached) {
                // Move current log file info to another file (old logs)
                File olderFile = new File(mFolder + File.separator + mLogFileNames[1]);
                if (mLogFile.exists()) {
                    mLogFile.renameTo(olderFile);
                }

                // Construct a new file for current log info
                mLogFile = new File(mFolder + File.separator + mLogFileNames[0]);
                isMaxFileSizeReached = false;
            }

            String timeStamp = new SimpleDateFormat(SIMPLE_DATE_FORMAT, Locale.getDefault())
                    .format(Calendar.getInstance().getTime());

            try {
                mBuf = new BufferedWriter(new FileWriter(mLogFile, true));
                mBuf.newLine();
                mBuf.write(timeStamp);
                mBuf.newLine();
                mBuf.write(text);
                mBuf.newLine();
            } catch (IOException e) {
                Log.e(TAG, "Writing to logfile failed", e);
            } finally {
                try {
                    mBuf.close();
                } catch (IOException e) {
                    Log.e(TAG, "Cleaning after logging failed", e);
                }
            }

            // Check if current log file size is bigger than the max file size defined
            if (mLogFile.length() > MAX_FILE_SIZE) {
                isMaxFileSizeReached = true;
            }
        }
    }

    public static String[] getLogFileNames() {
        return mLogFileNames;
    }
}
