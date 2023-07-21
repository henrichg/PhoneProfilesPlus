package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

class OrientationScanner implements SensorEventListener {

    @Override
    public void onSensorChanged(final SensorEvent event) {
//        PPApplicationStatic.logE("[IN_LISTENER] OrientationScanner.onSensorChanged", "xxx");
//        PPApplicationStatic.logE("[TEST BATTERY] OrientationScanner.onSensorChanged", "******** ### ******* (1)");

        if (PhoneProfilesService.getInstance() == null)
            return;

        boolean scanningPaused = ApplicationPreferences.applicationEventOrientationScanInTimeMultiply.equals("2") &&
                GlobalUtils.isNowTimeBetweenTimes(
                        ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyFrom,
                        ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyTo);
        if (scanningPaused)
            return;

        final Context appContext = PhoneProfilesService.getInstance().getApplicationContext();

        final int sensorType = event.sensor.getType();

        PPApplicationStatic.startHandlerThreadOrientationScanner();
        OrientationScannerHandlerThread orientationHandler = PPApplication.handlerThreadOrientationScanner;
        if (orientationHandler == null)
            return;

        if (sensorType == Sensor.TYPE_PROXIMITY) {
//            PPApplicationStatic.logE("[TEST BATTERY] OrientationScanner.onSensorChanged", "******** ### ******* TYPE_PROXIMITY");

            //if ((event.values[0] == 0) || (event.values[0] == mMaxProximityDistance)) {
            //if (event.timestamp - tmpDistanceTimestamp >= 250000000L /*1000000000L*/) {
            //    tmpDistanceTimestamp = event.timestamp;
            float mProximity = event.values[0];
            //if (mProximity == 0)
            int tmpDeviceDistance;
            if (mProximity < orientationHandler.maxProximityDistance)
                tmpDeviceDistance = OrientationScannerHandlerThread.DEVICE_ORIENTATION_DEVICE_IS_NEAR;
            else
                tmpDeviceDistance = OrientationScannerHandlerThread.DEVICE_ORIENTATION_DEVICE_IS_FAR;

            if (tmpDeviceDistance != orientationHandler.resultDeviceDistance) {
                orientationHandler.resultDeviceDistance = tmpDeviceDistance;
            }
            //}

            runEventsHandlerForOrientationChange(orientationHandler);
            return;
        }
        boolean runEventsHandler = false;
        if ((sensorType == Sensor.TYPE_ACCELEROMETER) || (sensorType == Sensor.TYPE_MAGNETIC_FIELD)) {
//            PPApplicationStatic.logE("[TEST BATTERY] OrientationScanner.onSensorChanged", "******** ### ******* TYPE_ACCELEROMETER, TYPE_MAGNETIC_FIELD");

            if (PPApplication.magneticFieldSensor != null) {
                if (sensorType == Sensor.TYPE_ACCELEROMETER) {
                    orientationHandler.gravity = exponentialSmoothing(event.values, orientationHandler.gravity, 0.2f);
                }
                if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
                    orientationHandler.geomagnetic = exponentialSmoothing(event.values, orientationHandler.geomagnetic, 0.5f);
                }
                if (event.timestamp - orientationHandler.tmpSideTimestamp >= 250000000L /*1000000000L*/) {
                    orientationHandler.tmpSideTimestamp = event.timestamp;
                    /*if (sensorType == Sensor.TYPE_ACCELEROMETER) {
                        // Isolate the force of gravity with the low-pass filter.
                        mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0];
                        mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1];
                        mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2];
                    }
                    if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
                        mGeomagnetic[0] = event.values[0];
                        mGeomagnetic[1] = event.values[1];
                        mGeomagnetic[2] = event.values[2];
                    }*/
                    if (orientationHandler.gravity != null && orientationHandler.geomagnetic != null) {
                        float[] R = new float[9];
                        float[] I = new float[9];
                        boolean success = SensorManager.getRotationMatrix(R, I, orientationHandler.gravity, orientationHandler.geomagnetic);
                        if (success) {
                            float[] orientation = new float[3];
                            //orientation[0]: azimuth, rotation around the -Z axis, i.e. the opposite direction of Z axis.
                            //orientation[1]: pitch, rotation around the -X axis, i.e the opposite direction of X axis.
                            //orientation[2]: roll, rotation around the Y axis.

                            //noinspection SuspiciousNameCombination
                            SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, I);
                            SensorManager.getOrientation(I, orientation);

                            //float azimuth = (float)Math.toDegrees(orientation[0]);
                            float pitch = (float) Math.toDegrees(orientation[1]);
                            float roll = (float) Math.toDegrees(orientation[2]);

                            int side = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN;
                            if (pitch > -30 && pitch < 30) {
                                if (roll > -60 && roll < 60)
                                    side = OrientationScannerHandlerThread.DEVICE_ORIENTATION_DISPLAY_UP;
                                if (roll > 150 && roll < 180)
                                    side = OrientationScannerHandlerThread.DEVICE_ORIENTATION_DISPLAY_DOWN;
                                if (roll > -180 && roll < -150)
                                    side = OrientationScannerHandlerThread.DEVICE_ORIENTATION_DISPLAY_DOWN;
                                if (roll > 65 && roll < 115)
                                    side = OrientationScannerHandlerThread.DEVICE_ORIENTATION_UP_SIDE_UP;
                                if (roll > -115 && roll < -65)
                                    side = OrientationScannerHandlerThread.DEVICE_ORIENTATION_DOWN_SIDE_UP;
                            }
                            if (pitch > 30 && pitch < 90) {
                                side = OrientationScannerHandlerThread.DEVICE_ORIENTATION_LEFT_SIDE_UP;
                            }
                            if (pitch > -90 && pitch < -30) {
                                side = OrientationScannerHandlerThread.DEVICE_ORIENTATION_RIGHT_SIDE_UP;
                            }

                            if ((orientationHandler.tmpSideUp == OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN) ||
                                    (/*(side != DEVICE_ORIENTATION_UNKNOWN) &&*/ (side != orientationHandler.tmpSideUp))) {
                                orientationHandler.eventCountSinceGZChanged = 0;

                                orientationHandler.tmpSideUp = side;
                            } else {
                                ++orientationHandler.eventCountSinceGZChanged;
                                if (orientationHandler.eventCountSinceGZChanged == OrientationScannerHandlerThread.MAX_COUNT_GZ_CHANGE) {

                                    if (orientationHandler.tmpSideUp != orientationHandler.resultSideUp) {

                                        orientationHandler.resultSideUp = orientationHandler.tmpSideUp;

                                        if ((orientationHandler.resultSideUp == OrientationScannerHandlerThread.DEVICE_ORIENTATION_DISPLAY_UP) ||
                                                (orientationHandler.resultSideUp == OrientationScannerHandlerThread.DEVICE_ORIENTATION_DISPLAY_DOWN))
                                            orientationHandler.resultDisplayUp = orientationHandler.resultSideUp;

                                        //runEventsHandlerForOrientationChange(appContext);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else {
                if (event.timestamp - orientationHandler.tmpSideTimestamp >= 250000000L /*1000000000L*/) {
                    orientationHandler.tmpSideTimestamp = event.timestamp;

                    float gravityZ = event.values[2];
                    if (orientationHandler.gravityZ == 0) {
                        orientationHandler.gravityZ = gravityZ;
                    } else {
                        if ((orientationHandler.gravityZ * gravityZ) < 0) {
                            orientationHandler.eventCountSinceGZChanged++;
                            if (orientationHandler.eventCountSinceGZChanged == OrientationScannerHandlerThread.MAX_COUNT_GZ_CHANGE) {

                                orientationHandler.gravityZ = gravityZ;
                                orientationHandler.eventCountSinceGZChanged = 0;

                                if (gravityZ > 0) {
                                    orientationHandler.resultSideUp = OrientationScannerHandlerThread.DEVICE_ORIENTATION_DISPLAY_UP;
                                } else if (gravityZ < 0) {
                                    orientationHandler.resultSideUp = OrientationScannerHandlerThread.DEVICE_ORIENTATION_DISPLAY_DOWN;
                                }

                                if ((orientationHandler.resultSideUp == OrientationScannerHandlerThread.DEVICE_ORIENTATION_DISPLAY_UP) ||
                                        (orientationHandler.resultSideUp == OrientationScannerHandlerThread.DEVICE_ORIENTATION_DISPLAY_DOWN))
                                    orientationHandler.resultDisplayUp = orientationHandler.resultSideUp;

                                //runEventsHandlerForOrientationChange(appContext);
                            }
                        } else {
                            if (orientationHandler.eventCountSinceGZChanged > 0) {
                                orientationHandler.gravityZ = gravityZ;
                                orientationHandler.eventCountSinceGZChanged = 0;
                            }
                        }
                    }
                }
            }
            runEventsHandler = true;
        }
        if (sensorType == Sensor.TYPE_LIGHT) {
//            PPApplicationStatic.logE("[TEST BATTERY] OrientationScanner.onSensorChanged", "******** ### ******* TYPE_LIGHT");

            //orientationHandler.resultLight = convertLightToSensor(event.values[0], orientationHandler.maxLightDistance);
            orientationHandler.resultLight = Math.round(event.values[0]);

            try {
                // redraw light current value preference
//                PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] OrientationScanner.onSensorChanged", "xxx");
                Intent intent = new Intent(PPApplication.PACKAGE_NAME + ".RefreshEventsPrefsGUIBroadcastReceiver");
                LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent);
            } catch (Exception ignored) {}

            runEventsHandler = true;
        }
        if (runEventsHandler)
            runEventsHandlerForOrientationChange(orientationHandler);

        orientationHandler.previousResultDisplayUp = orientationHandler.resultDisplayUp;
        orientationHandler.previousResultSideUp = orientationHandler.resultSideUp;
        orientationHandler.previousResultDeviceDistance = orientationHandler.resultDeviceDistance;
        orientationHandler.previousResultLight = orientationHandler.resultLight;
    }

    void runEventsHandlerForOrientationChange(OrientationScannerHandlerThread orientationHandler) {
        // start events handler

//        PPApplicationStatic.logE("[TEST BATTERY] OrientationScanner.runEventsHandlerForOrientationChange", "******** ### *******");

        if (
            (orientationHandler.previousResultDisplayUp != orientationHandler.resultDisplayUp) ||
            (orientationHandler.previousResultSideUp != orientationHandler.resultSideUp) ||
            (orientationHandler.previousResultDeviceDistance != orientationHandler.resultDeviceDistance) ||
            (orientationHandler.previousResultLight != orientationHandler.resultLight)
           ) {
            PhoneProfilesService service = PhoneProfilesService.getInstance();
            if (service != null) {
                Context context = service.getApplicationContext();

                if (EventStatic.getGlobalEventsRunning(context)) {

                    Data workData = new Data.Builder()
                            .putInt(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION)
                            .build();

                    String applicationEventOrientationScanInPowerSaveMode = ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode;

                    boolean isPowerSaveMode = GlobalUtils.isPowerSaveMode(context);
                    if (isPowerSaveMode) {
                        if (applicationEventOrientationScanInPowerSaveMode.equals("2"))
                            // start scanning in power save mode is not allowed
                            return;
                    } else {
                        if (ApplicationPreferences.applicationEventOrientationScanInTimeMultiply.equals("2")) {
                            if (GlobalUtils.isNowTimeBetweenTimes(
                                    ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyFrom,
                                    ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyTo)) {
                                // not scan in configured time
                                return;
                            }
                        }
                    }

                    int interval = ApplicationPreferences.applicationEventOrientationScanInterval;
                    if (isPowerSaveMode) {
                        if (applicationEventOrientationScanInPowerSaveMode.equals("1"))
                            interval = 2 * interval;
                    } else {
                        if (ApplicationPreferences.applicationEventOrientationScanInTimeMultiply.equals("1")) {
                            if (GlobalUtils.isNowTimeBetweenTimes(
                                    ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyFrom,
                                    ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyTo)) {
                                interval = 2 * interval;
                            }
                        }
                    }

                    //interval = interval / 2;

                    OneTimeWorkRequest worker =
                            new OneTimeWorkRequest.Builder(MainWorker.class)
                                    .addTag(MainWorker.ORIENTATION_SCANNER_WORK_TAG)
                                    .setInputData(workData)
                                    .setInitialDelay(interval, TimeUnit.SECONDS)
                                    //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                    .build();
                    try {
                        if (PPApplicationStatic.getApplicationStarted(true, true)) {
                            WorkManager workManager = PPApplication.getWorkManagerInstance();
                            if (workManager != null) {

                                //                        //if (PPApplicationStatic.logEnabled()) {
                                //                        ListenableFuture<List<WorkInfo>> statuses;
                                //                        statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_ORIENTATION_SCANNER_WORK_TAG);
                                //                        try {
                                //                            List<WorkInfo> workInfoList = statuses.get();
                                //                        } catch (Exception ignored) {
                                //                        }
                                //                        //}

//                                PPApplicationStatic.logE("[WORKER_CALL] OrientationScanner.runEventsHandlerForOrientationChange", "xxx");
                                //workManager.enqueue(worker);
                                // MUST BE KEEP !!! REPLACE cause to not call worker, because is replaced with delay again !!!
                                workManager.enqueueUniqueWork(MainWorker.ORIENTATION_SCANNER_WORK_TAG, ExistingWorkPolicy.KEEP, worker);
                            }
                        }
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            }
        }
    }

    private float[] exponentialSmoothing(float[] input, float[] output, float alpha) {
        if (output == null)
            return input;
        for (int i=0; i<input.length; i++) {
            output[i] = output[i] + alpha * (input[i] - output[i]);
        }
        return output;
    }

    /*
    private static int convertLightToSensor(float light, float maxLight) {
        if (maxLight > 1.0f)
            return (int)Math.round(light / maxLight * 10000.0);
        else
            return Math.round(light);
    }
    */

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        PPApplicationStatic.logE("[IN_LISTENER] OrientationScanner.onAccuracyChanged", "xxx");
//        PPApplicationStatic.logE("[TEST BATTERY] OrientationScanner.onAccuracyChanged", "******** ### *******");
    }

}
