package com.owncloud.android.lib.common.utils;

import android.content.Context;
import android.util.Log;

import com.owncloud.android.lib.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;


public class Log_OC {
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

    public static void i(String TAG, String message){
        Log.i(TAG, message);
        appendLog(TAG+" : "+ message);
    }

    public static void d(String TAG, String message){
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message);
            appendLog(TAG + " : " + message);
        }
    }
    public static void d(String TAG, String message, Exception e) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message, e);
            appendLog(TAG + " : " + message + " Exception : " + Arrays.toString(e.getStackTrace()));
        }
    }
    public static void e(String TAG, String message){
        Log.e(TAG, message);
        appendLog(TAG + " : " + message);
    }
    
    public static void e(String TAG, String message, Throwable e) {
        Log.e(TAG, message, e);
        appendLog(TAG+" : " + message +" Exception : " + Arrays.toString(e.getStackTrace()));
    }
    
    public static void v(String TAG, String message){
        if (BuildConfig.DEBUG) {
            Log.v(TAG, message);
            appendLog(TAG + " : " + message);
        }
    }
    
    public static void w(String TAG, String message) {
        Log.w(TAG, message);
        appendLog(TAG+" : "+ message);
    }
    
    public static void wtf(String TAG, String message) {
        Log.wtf(TAG, message);
        appendLog(TAG+" : "+ message);
    }

    /**
     * Start doing logging
     * @param context Context: used for determinated app specific folder
     */
    synchronized public static void startLogging(Context context) {
        mNextcloudDataFolderLog = context.getFilesDir().getAbsolutePath() + File.separator + LOG_FOLDER_NAME;
        mFolder = new File(mNextcloudDataFolderLog);
        mLogFile = new File(mFolder + File.separator + mLogFileNames[0]);

        boolean isFolderCreated = false;

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
            if(mBuf != null) {
                try {
                    mBuf.close();
                } catch(IOException e) {
                    Log.e(TAG, "Initialization finishing failed", e);
                }
            }
        }
    }

    synchronized public static void stopLogging() {
        try {
            if (mBuf != null)
                mBuf.close();
            isEnabled = false;

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
        if(folderLogs.isDirectory()){
            String[] myFiles = folderLogs.list();
            for (int i=0; i<myFiles.length; i++) {
                File myFile = new File(folderLogs, myFiles[i]);
                myFile.delete();
            }
        }
    }
    
    /**
     * Append the info of the device
     */
    private static void appendPhoneInfo() {
        appendLog("Model : " + android.os.Build.MODEL);
        appendLog("Brand : " + android.os.Build.BRAND);
        appendLog("Product : " + android.os.Build.PRODUCT);
        appendLog("Device : " + android.os.Build.DEVICE);
        appendLog("Version-Codename : " + android.os.Build.VERSION.CODENAME);
        appendLog("Version-Release : " + android.os.Build.VERSION.RELEASE);
    }
    
    /**
     * Append to the log file the info passed
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

	        String timeStamp = new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(Calendar.getInstance().getTime());

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
