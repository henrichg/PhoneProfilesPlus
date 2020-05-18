package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

class TopExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Thread.UncaughtExceptionHandler defaultUEH;
    private final Context applicationContext;
    private final int actualVersionCode;

    static final String CRASH_FILENAME = "crash.txt";

    TopExceptionHandler(Context applicationContext, int actualVersionCode) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.applicationContext = applicationContext;
        this.actualVersionCode = actualVersionCode;
    }

    @SuppressWarnings("StringConcatenationInLoop")
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e)
    {
        try {
            if (PPApplication.lockDeviceActivity != null) {
                boolean canWriteSettings;// = true;
                canWriteSettings = Settings.System.canWrite(applicationContext);
                if (canWriteSettings)
                    Settings.System.putInt(applicationContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PPApplication.screenTimeoutBeforeDeviceLock);
            }
        } catch (Exception ee) {
            Log.e("TopExceptionHandler.uncaughtException", Log.getStackTraceString(ee));
        }

        try {
            if (PPApplication.crashIntoFile) {
                StackTraceElement[] arr = e.getStackTrace();
                String report = e.toString() + "\n\n";

                report += "----- App version code: " + actualVersionCode + "\n\n";

                for (StackTraceElement anArr : arr) {
                    report += "    " + anArr.toString() + "\n";
                }
                report += "-------------------------------\n\n";

                report += "--------- Stack trace ---------\n\n";
                for (StackTraceElement anArr : arr) {
                    report += "    " + anArr.toString() + "\n";
                }
                report += "-------------------------------\n\n";

                // If the exception was thrown in a background thread inside
                // AsyncTask, then the actual exception can be found with getCause
                report += "--------- Cause ---------------\n\n";
                Throwable cause = e.getCause();
                if (cause != null) {
                    report += cause.toString() + "\n\n";
                    arr = cause.getStackTrace();
                    for (StackTraceElement anArr : arr) {
                        report += "    " + anArr.toString() + "\n";
                    }
                }
                report += "-------------------------------\n\n";

                logIntoFile("E", "TopExceptionHandler", report);
            }
        } catch (Exception ee) {
            Log.e("TopExceptionHandler.uncaughtException", Log.getStackTraceString(ee));
        }

        if (defaultUEH != null)
            //Delegates to Android's error handling
            defaultUEH.uncaughtException(t, e);
        else
            //Prevents the service/app from freezing
            System.exit(2);
    }

    @SuppressWarnings("SameParameterValue")
    private void logIntoFile(String type, String tag, String text)
    {
        try {
            /*File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, PPApplication.EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory()))
                //noinspection ResultOfMethodCallIgnored
                exportDir.mkdirs();

            File logFile = new File(sd, PPApplication.EXPORT_PATH + "/" + CRASH_FILENAME);*/

            File path = applicationContext.getExternalFilesDir(null);
            File logFile = new File(path, CRASH_FILENAME);

            if (logFile.length() > 1024 * 10000)
                resetLog();

            if (!logFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                logFile.createNewFile();
            }

            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
            String time = sdf.format(Calendar.getInstance().getTimeInMillis());
            String log = time + "--" + type + "-----" + tag + "------" + text;
            buf.append(log);
            buf.newLine();
            buf.flush();
            buf.close();
        } catch (IOException ee) {
            Log.e("TopExceptionHandler.logIntoFile", Log.getStackTraceString(ee));
        }
    }

    private void resetLog()
    {
        /*File sd = Environment.getExternalStorageDirectory();
        File exportDir = new File(sd, PPApplication.EXPORT_PATH);
        if (!(exportDir.exists() && exportDir.isDirectory()))
            //noinspection ResultOfMethodCallIgnored
            exportDir.mkdirs();

        File logFile = new File(sd, PPApplication.EXPORT_PATH + "/" + CRASH_FILENAME);*/

        File path = applicationContext.getExternalFilesDir(null);
        File logFile = new File(path, CRASH_FILENAME);

        //noinspection ResultOfMethodCallIgnored
        logFile.delete();
    }

}
