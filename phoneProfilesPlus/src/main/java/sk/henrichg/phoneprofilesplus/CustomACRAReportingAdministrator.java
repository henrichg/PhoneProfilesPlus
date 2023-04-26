package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.RemoteServiceException;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.DeadSystemException;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.google.auto.service.AutoService;

import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;
import org.acra.config.ReportingAdministrator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeoutException;

// Custom ACRA ReportingAdministrator
// https://github.com/ACRA/acra/wiki/Custom-Extensions

@SuppressWarnings("unused")
@AutoService(ReportingAdministrator.class)
public class CustomACRAReportingAdministrator implements ReportingAdministrator {

    static final String CRASH_FILENAME = "crash.txt";

    public CustomACRAReportingAdministrator() {
//        Log.e("CustomACRAReportingAdministrator constructor", "xxxx");
    }

    @Override
    public boolean shouldStartCollecting(@NonNull final Context context,
                                         @NonNull CoreConfiguration config,
                                         @NonNull ReportBuilder reportBuilder) {

//        Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "xxxx");

        final Throwable _exception = reportBuilder.getException();
        final Thread _thread = reportBuilder.getUncaughtExceptionThread();

        if (_exception == null)
            return true;

        try {
            //if (PPApplication.lockDeviceActivity != null) {
            if (PPApplication.lockDeviceActivityDisplayed) {
                boolean canWriteSettings;// = true;
                canWriteSettings = Settings.System.canWrite(context);
                if (canWriteSettings) {
                    /*if (PPApplication.deviceIsOppo || PPApplication.deviceIsRealme) {
                        if (PPApplication.screenTimeoutHandler != null) {
                            PPApplication.screenTimeoutHandler.post(() -> {
                                synchronized (PPApplication.rootMutex) {
                                    String command1 = "settings put system " + Settings.System.SCREEN_OFF_TIMEOUT + " " + PPApplication.screenTimeoutBeforeDeviceLock;
                                    //if (PPApplication.isSELinuxEnforcing())
                                    //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                    Command command = new Command(0, false, command1); //, command2);
                                    try {
                                        RootTools.getShell(false, Shell.ShellContext.SYSTEM_APP).add(command);
                                        PPApplication.commandWait(command, "CustomACRAReportingAdministrator.shouldStartCollecting");
                                    } catch (Exception ee) {
                                        // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                        //Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", Log.getStackTraceString(e));
                                        //PPApplicationStatic.recordException(e);
                                    }
                                }
                            });
                        }
                    } else*/
                    //if ((PPApplication.lockDeviceActivity != null) &&
                    //        (PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayed != 0))
                    if (PPApplication.lockDeviceActivityDisplayed &&
                            (PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayed != 0))
                        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayed);
                }
            }
        } catch (Exception ee) {
            //Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", Log.getStackTraceString(ee));
        }

        try {
            if (PPApplication.crashIntoFile) {
                Runnable runnable = () -> {
                    int actualVersionCode = 0;
                    try {
                        PackageInfo pInfo = context.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                        actualVersionCode = PPApplicationStatic.getVersionCode(pInfo);
                    } catch (Exception ignored) {}

                    StackTraceElement[] arr = _exception.getStackTrace();
                    StringBuilder report = new StringBuilder(_exception.toString());

                    report.append("\n\n");

                    report.append("----- App version code: ").append(actualVersionCode).append("\n\n");

                    /*
                    for (StackTraceElement anArr : arr) {
                        report.append("    ").append(anArr.toString()).append("\n");
                    }
                    report.append("-------------------------------\n\n");
                    */

                    report.append("--------- Stack trace ---------\n\n");
                    for (StackTraceElement anArr : arr) {
                        report.append("    ").append(anArr.toString()).append("\n");
                    }
                    report.append("\n");

                    // If the exception was thrown in a background thread inside
                    // AsyncTask, then the actual exception can be found with getCause
                    Throwable cause = _exception.getCause();
                    if (cause != null) {
                        report.append("-------------------------------\n\n");
                        report.append("--------- Cause ---------------\n\n");
                        report.append(cause).append("\n\n");
                        arr = cause.getStackTrace();
                        for (StackTraceElement anArr : arr) {
                            report.append("    ").append(anArr.toString()).append("\n");
                        }
                    }
                    report.append("-------------------------------\n\n");

                    logIntoFile(context, "E", "CustomACRAReportingAdministrator", report.toString());
                };
                PPApplicationStatic.createBasicExecutorPool();
                PPApplication.basicExecutorPool.submit(runnable);
            }
        } catch (Exception ee) {
            //Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", Log.getStackTraceString(ee));
        }

        if (_exception instanceof TimeoutException) {
            if ((_thread != null) && _thread.getName().equals("FinalizerWatchdogDaemon"))
                return false;
        }

        if (_exception instanceof DeadSystemException) {
//            Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "DeadSystemException");
            return false;
        }

        if (_exception.getClass().getSimpleName().equals("CannotDeliverBroadcastException") &&
                (_exception instanceof RemoteServiceException)) {
            // ignore but not exist exception
            // android.app.RemoteServiceException$CannotDeliverBroadcastException: can't deliver broadcast
            // https://stackoverflow.com/questions/72902856/cannotdeliverbroadcastexception-only-on-pixel-devices-running-android-12
//            Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "CannotDeliverBroadcastException");
            return false;
        }

/*
        // this is only for debuging, how is handled ignored exceptions
        if (_exception instanceof java.lang.RuntimeException) {
            if (_exception.getMessage() != null) {
                if (_exception.getMessage().equals("Test Crash")) {
//                    Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "RuntimeException: Test Crash");
                    return false;
                }
                if (_exception.getMessage().equals("Test non-fatal exception")) {
//                    Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "RuntimeException: Test non-fatal exception");
                    return false;
                }
            }
        }
*/

        return true;
    }

    private void logIntoFile(Context context,
                             @SuppressWarnings("SameParameterValue") String type,
                             @SuppressWarnings("SameParameterValue") String tag,
                             String text)
    {
        try {
            /*File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, PPApplication.EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory()))
                exportDir.mkdirs();

            File logFile = new File(sd, PPApplication.EXPORT_PATH + "/" + CRASH_FILENAME);*/

            File path = context.getExternalFilesDir(null);
            File logFile = new File(path, CRASH_FILENAME);

            if (logFile.length() > 1024 * 10000)
                resetLog(context);

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
            //Log.e("CustomACRAReportingAdministrator.logIntoFile", Log.getStackTraceString(ee));
        }
    }

    private void resetLog(Context context)
    {
        /*File sd = Environment.getExternalStorageDirectory();
        File exportDir = new File(sd, PPApplication.EXPORT_PATH);
        if (!(exportDir.exists() && exportDir.isDirectory()))
            exportDir.mkdirs();

        File logFile = new File(sd, PPApplication.EXPORT_PATH + "/" + CRASH_FILENAME);*/

        File path = context.getExternalFilesDir(null);
        File logFile = new File(path, CRASH_FILENAME);

        //noinspection ResultOfMethodCallIgnored
        logFile.delete();
    }

}
