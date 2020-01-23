package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.provider.Settings;

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
    public void uncaughtException(Thread t, Throwable e)
    {
        try {
            if (PhoneProfilesService.getInstance() != null) {
                if (PhoneProfilesService.getInstance().lockDeviceActivity != null) {
                    boolean canWriteSettings = true;
                    if (android.os.Build.VERSION.SDK_INT >= 23)
                        canWriteSettings = Settings.System.canWrite(applicationContext);
                    if (canWriteSettings)
                        Settings.System.putInt(applicationContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PhoneProfilesService.getInstance().screenTimeoutBeforeDeviceLock);
                }
            }
        } catch (Exception ignored) {}

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
        if (PPApplication.crashIntoFile) {
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
            } catch (IOException ignored) {
            }
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
