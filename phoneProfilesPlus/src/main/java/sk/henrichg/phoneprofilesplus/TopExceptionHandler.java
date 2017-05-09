package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class TopExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler defaultUEH;
    //private int actualVersionCode;

    //private static final String CRASH_FILENAME = "crash.txt";

    public TopExceptionHandler(Context applicationContext/*, int actualVersionCode*/) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        //this.actualVersionCode = actualVersionCode;

        Fabric.with(applicationContext, new Crashlytics());
    }

    public void uncaughtException(Thread t, Throwable e)
    {
        /*
        StackTraceElement[] arr = e.getStackTrace();
        String report = e.toString()+"\n\n";

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
        if(cause != null) {
            report += cause.toString() + "\n\n";
            arr = cause.getStackTrace();
            for (StackTraceElement anArr : arr) {
                report += "    " + anArr.toString() + "\n";
            }
        }
        report += "-------------------------------\n\n";

        logIntoFile("E","TopExceptionHandler", report);
        */

        if (defaultUEH != null)
            //Delegates to Android's error handling
            defaultUEH.uncaughtException(t, e);
        else
            //Prevents the service/app from freezing
            System.exit(2);
    }

    /*
    @SuppressLint("SimpleDateFormat")
    private void logIntoFile(String type, String tag, String text)
    {
        try
        {
            File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, PPApplication.EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory()))
                //noinspection ResultOfMethodCallIgnored
                exportDir.mkdirs();

            File logFile = new File(sd, PPApplication.EXPORT_PATH + "/" + CRASH_FILENAME);

            if (logFile.length() > 1024 * 10000)
                resetLog();

            if (!logFile.exists())
            {
                //noinspection ResultOfMethodCallIgnored
                logFile.createNewFile();
            }

            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            String log = "";
            SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
            String time = sdf.format(Calendar.getInstance().getTimeInMillis());
            log = log + time + "--" + type + "-----" + tag + "------" + text;
            buf.append(log);
            buf.newLine();
            buf.flush();
            buf.close();
        }
        catch (IOException ignored) {
        }
    }

    private void resetLog()
    {
        File sd = Environment.getExternalStorageDirectory();
        File exportDir = new File(sd, PPApplication.EXPORT_PATH);
        if (!(exportDir.exists() && exportDir.isDirectory()))
            //noinspection ResultOfMethodCallIgnored
            exportDir.mkdirs();

        File logFile = new File(sd, PPApplication.EXPORT_PATH + "/" + CRASH_FILENAME);
        //noinspection ResultOfMethodCallIgnored
        logFile.delete();
    }
    */

}
