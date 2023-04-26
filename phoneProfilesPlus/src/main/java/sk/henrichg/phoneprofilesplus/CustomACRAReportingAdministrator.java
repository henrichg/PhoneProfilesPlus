package sk.henrichg.phoneprofilesplus;

import android.app.RemoteServiceException;
import android.content.Context;
import android.os.DeadSystemException;

import androidx.annotation.NonNull;

import com.google.auto.service.AutoService;

import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;
import org.acra.config.ReportingAdministrator;

import java.util.concurrent.TimeoutException;

// Custom ACRA ReportingAdministrator
// https://github.com/ACRA/acra/wiki/Custom-Extensions

@SuppressWarnings("unused")
@AutoService(ReportingAdministrator.class)
public class CustomACRAReportingAdministrator implements ReportingAdministrator {

    public CustomACRAReportingAdministrator() {
//        Log.e("CustomACRAReportingAdministrator constructor", "xxxx");
    }

    @Override
    public boolean shouldStartCollecting(@NonNull Context context,
                                         @NonNull CoreConfiguration config,
                                         @NonNull ReportBuilder reportBuilder) {

//        Log.e("CustomACRAReportingAdministrator.shouldStartCollecting", "xxxx");

        Throwable _exception = reportBuilder.getException();
        Thread _thread = reportBuilder.getUncaughtExceptionThread();

        if (_exception == null)
            return true;

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
}
