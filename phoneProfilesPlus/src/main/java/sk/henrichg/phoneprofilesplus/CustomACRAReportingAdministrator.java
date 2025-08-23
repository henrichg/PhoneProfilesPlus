package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
// is from reginer android.jar
import android.app.RemoteServiceException;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
// is from reginer android.jar
import android.os.DeadSystemException;
import android.os.DeadSystemRuntimeException;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.auto.service.AutoService;

import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;
import org.acra.config.ReportingAdministrator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeoutException;

// Custom ACRA ReportingAdministrator
// https://github.com/ACRA/acra/wiki/Custom-Extensions

/** @noinspection ExtractMethodRecommender*/
@AutoService(ReportingAdministrator.class)
public class CustomACRAReportingAdministrator implements ReportingAdministrator {

    static final String CRASH_FILENAME = "crash.txt";

    /** @noinspection unused*/
    public CustomACRAReportingAdministrator() {
//        Log.e("CustomACRAReportingAdministrator constructor", "xxxx");
    }

    @Override
    public boolean shouldStartCollecting(@NonNull final Context context,
                                         @NonNull CoreConfiguration config,
                                         @NonNull ReportBuilder reportBuilder) {

//        Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "xxxx");

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
                    if (PPApplication.lockDeviceActivityOnlyScreenOff &&
                            (PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayedForScreenOff != 0)) {
                        Settings.System.putInt(context.getApplicationContext().getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayedForScreenOff);
                    }
                    else
                    if ((!PPApplication.lockDeviceActivityOnlyScreenOff) &&
                            (PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayedForDeviceLock != 0)) {
                        Settings.System.putInt(context.getApplicationContext().getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayedForDeviceLock);
                    }
                }
            }
        } catch (Exception ee) {
            //Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", Log.getStackTraceString(ee));
        }

        Throwable __exception = reportBuilder.getException();

        if (__exception == null)
            return true;

//        Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "(2)");

        //noinspection UnreachableCode
        try {
            if (PPApplication.crashIntoFile) {
                final Context appContext = context.getApplicationContext();
                final WeakReference<Throwable> exceptionWeakRef = new WeakReference<>(__exception);
                Runnable runnable = () -> {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_CustomACRAReportingAdministrator_shouldStartCollecting);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        Throwable _exception = exceptionWeakRef.get();
                        if (_exception != null) {
                            int actualVersionCode = 0;
                            try {
                                PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                                actualVersionCode = PPApplicationStatic.getVersionCode(pInfo);
                            } catch (Exception ignored) {
                            }

                            StackTraceElement[] arr = _exception.getStackTrace();
                            StringBuilder report = new StringBuilder(_exception.toString());

                            report.append(StringConstants.STR_DOUBLE_NEWLINE);

                            report.append("----- App version code: ").append(actualVersionCode).append(StringConstants.STR_DOUBLE_NEWLINE);

                            /*
                            for (StackTraceElement anArr : arr) {
                                report.append("    ").append(anArr.toString()).append("\n");
                            }
                            report.append("-------------------------------").append(StringConstants.STR_DOUBLE_NEWLINE);
                            */

                            report.append("--------- Stack trace ---------").append(StringConstants.STR_DOUBLE_NEWLINE);
                            for (StackTraceElement anArr : arr) {
                                report.append("    ").append(anArr.toString()).append(StringConstants.CHAR_NEW_LINE);
                            }
                            report.append(StringConstants.CHAR_NEW_LINE);

                            // If the exception was thrown in a background thread inside
                            // AsyncTask, then the actual exception can be found with getCause
                            Throwable cause = _exception.getCause();
                            if (cause != null) {
                                report.append("-------------------------------").append(StringConstants.STR_DOUBLE_NEWLINE);
                                report.append("--------- Cause ---------------").append(StringConstants.STR_DOUBLE_NEWLINE);
                                report.append(cause).append(StringConstants.STR_DOUBLE_NEWLINE);
                                arr = cause.getStackTrace();
                                for (StackTraceElement anArr : arr) {
                                    report.append("    ").append(anArr.toString()).append(StringConstants.CHAR_NEW_LINE);
                                }
                            }
                            report.append("-------------------------------").append(StringConstants.STR_DOUBLE_NEWLINE);

                            logIntoFile(appContext, "E", "CustomACRAReportingAdministrator", report.toString());
                        }

                    } catch (Exception e) {
                        PPApplicationStatic.logE("[WAKELOCK_EXCEPTION] CustomACRAReportingAdministrator.shouldStartCollecting", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                };
                PPApplicationStatic.createBasicExecutorPool();
                PPApplication.basicExecutorPool.submit(runnable);
            }
        } catch (Exception ee) {
            Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", Log.getStackTraceString(ee));
        }

//        Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "(3)");

        Thread thread = reportBuilder.getUncaughtExceptionThread();
        if (!isRecordedException(__exception, thread))
            return false;

        return true;
    }

    static boolean isRecordedException(Throwable _exception,
                                       Thread _thread) {
        try {

            if (_exception instanceof TimeoutException) {
                if ((_thread != null) && _thread.getName().equals("FinalizerWatchdogDaemon"))
                    return false;
            }

//            Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "(4)");

            if (_exception.getClass().getSimpleName().equals("CannotDeliverBroadcastException") &&
                    (_exception instanceof RemoteServiceException)) {
                // ignore but not exist exception
                // android.app.RemoteServiceException$CannotDeliverBroadcastException: can't deliver broadcast
                // https://stackoverflow.com/questions/72902856/cannotdeliverbroadcastexception-only-on-pixel-devices-running-android-12
//            Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "CannotDeliverBroadcastException");
                return false;
            }

//            Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "(5)");

            if (_exception instanceof DeadSystemException) {
//                Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "DeadSystemException");
                // Exit app. This restarts PPP
                System.exit(2);
                return false;
            }

//            Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "(6)");

            if (Build.VERSION.SDK_INT >= 33) {
                if (_exception instanceof DeadSystemRuntimeException) {
//                Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "DeadSystemRuntimeException");
                    // Exit app. This restarts PPP
                    System.exit(2);
                    return false;
                }
            }

            if (_exception instanceof RuntimeException) {
                String stackTrace = Log.getStackTraceString(_exception);
                if (stackTrace.contains("android.os.DeadSystemException")) {
                    // Exit app. This restarts PPP
                    System.exit(2);
                    return false;
                }
                if (stackTrace.contains("android.os.DeadSystemRuntimeException")) {
                    // Exit app. This restarts PPP
                    System.exit(2);
                    return false;
                }
            }

/*
            // this is only for debuging, how is handled ignored exceptions
            if (_exception instanceof java.lang.RuntimeException) {
                if (_exception.getMessage() != null) {
                    if (_exception.getMessage().equals("Test Crash")) {
    //                    Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "RuntimeException: Test Crash");
                        // Exit app. This restarts PPP
                        System.exit(2);
                        return false;
                    }
                    if (_exception.getMessage().equals("Test non-fatal exception")) {
    //                    Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "RuntimeException: Test non-fatal exception");
                        return false;
                    }
                }
            }
 */

        } catch (Exception ee) {
            //Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", Log.getStackTraceString(ee));
        }

        return true;
    }

    /** @noinspection BlockingMethodInNonBlockingContext*/
    static private void logIntoFile(Context context,
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

    static private void resetLog(Context context)
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
