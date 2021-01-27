package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.stericson.rootshell.execution.Command;
import com.stericson.rootshell.execution.Shell;
import com.stericson.roottools.RootTools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeoutException;

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
                if (canWriteSettings) {
                    if (PPApplication.deviceIsOppo || PPApplication.deviceIsRealme) {
                        if (PPApplication.screenTimeoutHandler != null) {
                            PPApplication.screenTimeoutHandler.post(() -> {
                                synchronized (PPApplication.rootMutex) {
                                    PPApplication.logE("TopExceptionHandler.uncaughtException", "" + PPApplication.screenTimeoutBeforeDeviceLock);
                                    String command1 = "settings put system " + Settings.System.SCREEN_OFF_TIMEOUT + " " + PPApplication.screenTimeoutBeforeDeviceLock;
                                    //if (PPApplication.isSELinuxEnforcing())
                                    //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                    Command command = new Command(0, false, command1); //, command2);
                                    try {
                                        RootTools.getShell(false, Shell.ShellContext.SYSTEM_APP).add(command);
                                        PPApplication.commandWait(command, "TopExceptionHandler.uncaughtException");
                                    } catch (Exception ee) {
                                        // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                        //Log.e("ActivateProfileHelper.setScreenTimeout", Log.getStackTraceString(e));
                                        //PPApplication.recordException(e);
                                    }
                                }
                            });
                        }
                    } else
                        Settings.System.putInt(applicationContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PPApplication.screenTimeoutBeforeDeviceLock);
                }
            }
        } catch (Exception ee) {
            //Log.e("TopExceptionHandler.uncaughtException", Log.getStackTraceString(ee));
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
            //Log.e("TopExceptionHandler.uncaughtException", Log.getStackTraceString(ee));
        }

        if (defaultUEH != null) {
            boolean ignore = false;
            if (t.getName().equals("FinalizerWatchdogDaemon") && (e instanceof TimeoutException)) {
                // ignore these exceptions
                // java.util.concurrent.TimeoutException: com.android.internal.os.BinderInternal$GcWatcher.finalize() timed out after 10 seconds
                // https://stackoverflow.com/a/55999687/2863059
                ignore = true;
            }
            /*if (Build.VERSION.SDK_INT >= 24) {
                if (e instanceof DeadSystemException) {
                    // ignore these exceptions
                    // these are from dead of system for example:
                    // java.lang.RuntimeException: Unable to create service
                    // androidx.work.impl.background.systemjob.SystemJobService:
                    // java.lang.RuntimeException: android.os.DeadSystemException
                    ignore = true;
                }
            }*/

            if (!ignore) {
                //Delegates to Android's error handling
                defaultUEH.uncaughtException(t, e);
            }
        }
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
            //Log.e("TopExceptionHandler.logIntoFile", Log.getStackTraceString(ee));
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
