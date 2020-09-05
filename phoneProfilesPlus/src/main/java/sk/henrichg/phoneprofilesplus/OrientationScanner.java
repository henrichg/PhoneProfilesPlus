package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

class OrientationScanner implements SensorEventListener {

    @Override
    public void onSensorChanged(final SensorEvent event) {
        //PPApplication.logE("[LISTENER CALL] OrientationScanner.onSensorChanged", "xxx");

        //PPApplication.logE("OrientationScanner.onSensorChanged", "current thread="+Thread.currentThread());

        if (PhoneProfilesService.getInstance() == null)
            return;

        //final Context appContext = PhoneProfilesService.getInstance().getApplicationContext();

        final int sensorType = event.sensor.getType();
        //PPApplication.logE("OrientationScanner.onSensorChanged", "sensorType="+sensorType);

        // handler is used in PhoneProfilesService.startListeningOrientationSensors()
        OrientationScannerHandlerThread orientationHandler = PPApplication.handlerThreadOrientationScanner;

        if (sensorType == Sensor.TYPE_PROXIMITY) {
            //PPApplication.logE("OrientationScanner.onSensorChanged", "proximity value=" + event.values[0]);
            //PPApplication.logE("OrientationScanner.onSensorChanged", "proximity mMaxProximityDistance=" + mMaxProximityDistance);
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
                //PPApplication.logE("OrientationScanner.onSensorChanged", "mProximity="+mProximity);
                orientationHandler.resultDeviceDistance = tmpDeviceDistance;
            }
            //}

            runEventsHandlerForOrientationChange(orientationHandler);
            return;
        }
        boolean runEventsHandler = false;
        if ((sensorType == Sensor.TYPE_ACCELEROMETER) || (sensorType == Sensor.TYPE_MAGNETIC_FIELD)) {
            if (PPApplication.magneticFieldSensor != null) {
                //PPApplication.logE("OrientationScanner.onSensorChanged", "magnetic value="+event.values[0]);

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

                            //PPApplication.logE("OrientationScanner.onSensorChanged", "pitch=" + pitch);
                            //PPApplication.logE("OrientationScanner.onSensorChanged", "roll=" + roll);

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

                                //PPApplication.logE("OrientationScanner.onSensorChanged", "azimuth="+azimuth);
                                //PPApplication.logE("OrientationScanner.onSensorChanged", "pitch=" + pitch);
                                //PPApplication.logE("OrientationScanner.onSensorChanged", "roll=" + roll);

                                orientationHandler.tmpSideUp = side;
                            } else {
                                ++orientationHandler.eventCountSinceGZChanged;
                                if (orientationHandler.eventCountSinceGZChanged == OrientationScannerHandlerThread.MAX_COUNT_GZ_CHANGE) {

                                    if (orientationHandler.tmpSideUp != orientationHandler.resultSideUp) {
                                        //PPApplication.logE("OrientationScanner.onSensorChanged", "magnetic+accelerometer - send broadcast");

                                        orientationHandler.resultSideUp = orientationHandler.tmpSideUp;

                                        if ((orientationHandler.resultSideUp == OrientationScannerHandlerThread.DEVICE_ORIENTATION_DISPLAY_UP) ||
                                                (orientationHandler.resultSideUp == OrientationScannerHandlerThread.DEVICE_ORIENTATION_DISPLAY_DOWN))
                                            orientationHandler.resultDisplayUp = orientationHandler.resultSideUp;

                                        /*
                                        if (mDisplayUp == DEVICE_ORIENTATION_DISPLAY_UP)
                                            PPApplication.logE("OrientationScanner.onSensorChanged", "now screen is facing up.");
                                        if (mDisplayUp == DEVICE_ORIENTATION_DISPLAY_DOWN)
                                            PPApplication.logE("OrientationScanner.onSensorChanged", "now screen is facing down.");

                                        if (mSideUp == DEVICE_ORIENTATION_UP_SIDE_UP)
                                            PPApplication.logE("OrientationScanner.onSensorChanged", "now up side is facing up.");
                                        if (mSideUp == DEVICE_ORIENTATION_DOWN_SIDE_UP)
                                            PPApplication.logE("OrientationScanner.onSensorChanged", "now down side is facing up.");
                                        if (mSideUp == DEVICE_ORIENTATION_RIGHT_SIDE_UP)
                                            PPApplication.logE("OrientationScanner.onSensorChanged", "now right side is facing up.");
                                        if (mSideUp == DEVICE_ORIENTATION_LEFT_SIDE_UP)
                                            PPApplication.logE("OrientationScanner.onSensorChanged", "now left side is facing up.");
                                        if (mSideUp == DEVICE_ORIENTATION_UNKNOWN)
                                            PPApplication.logE("OrientationScanner.onSensorChanged", "unknown side.");
                                        */

                                        //runEventsHandlerForOrientationChange(appContext);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else {
                //PPApplication.logE("OrientationScanner.onSensorChanged", "accelerometer value="+event.values[0]);

                if (event.timestamp - orientationHandler.tmpSideTimestamp >= 250000000L /*1000000000L*/) {
                    orientationHandler.tmpSideTimestamp = event.timestamp;

                    float gravityZ = event.values[2];
                    if (orientationHandler.gravityZ == 0) {
                        orientationHandler.gravityZ = gravityZ;
                    } else {
                        if ((orientationHandler.gravityZ * gravityZ) < 0) {
                            orientationHandler.eventCountSinceGZChanged++;
                            if (orientationHandler.eventCountSinceGZChanged == OrientationScannerHandlerThread.MAX_COUNT_GZ_CHANGE) {
                                //PPApplication.logE("OrientationScanner.onSensorChanged", "accelerometer - send broadcast");

                                orientationHandler.gravityZ = gravityZ;
                                orientationHandler.eventCountSinceGZChanged = 0;

                                if (gravityZ > 0) {
                                    //PPApplication.logE("OrientationScanner.onSensorChanged", "now screen is facing up.");
                                    orientationHandler.resultSideUp = OrientationScannerHandlerThread.DEVICE_ORIENTATION_DISPLAY_UP;
                                } else if (gravityZ < 0) {
                                    //PPApplication.logE("OrientationScanner.onSensorChanged", "now screen is facing down.");
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
            //PPApplication.logE("OrientationScanner.onSensorChanged", "light value="+event.values[0]);
            //PPApplication.logE("OrientationScanner.onSensorChanged", "light mMaxLightDistance="+mMaxLightDistance);

            orientationHandler.resultLight = convertLightToSensor(event.values[0], orientationHandler.maxLightDistance);
            //PPApplication.logE("OrientationScanner.onSensorChanged", "light mLight="+mLight);
            runEventsHandler = true;
        }
        if (runEventsHandler)
            runEventsHandlerForOrientationChange(orientationHandler);

        orientationHandler.previousResultDisplayUp = orientationHandler.resultDisplayUp;
        orientationHandler.previousResultSideUp = orientationHandler.resultSideUp;
        orientationHandler.previousResultDeviceDistance = orientationHandler.resultDeviceDistance;
        orientationHandler.previousResultLight = orientationHandler.resultLight;
    }

    private void runEventsHandlerForOrientationChange(OrientationScannerHandlerThread orientationHandler) {
        // start events handler

        if (
            (orientationHandler.previousResultDisplayUp != orientationHandler.resultDisplayUp) ||
            (orientationHandler.previousResultSideUp != orientationHandler.resultSideUp) ||
            (orientationHandler.previousResultDeviceDistance != orientationHandler.resultDeviceDistance) ||
            (orientationHandler.previousResultLight != orientationHandler.resultLight)
           )
        {
            /*
            PPApplication.logE("OrientationScanner.onSensorChanged", "orientationHandler.previousResultDisplayUp="+orientationHandler.previousResultDisplayUp);
            PPApplication.logE("OrientationScanner.onSensorChanged", "orientationHandler.previousResultSideUp="+orientationHandler.previousResultSideUp);
            PPApplication.logE("OrientationScanner.onSensorChanged", "orientationHandler.previousResultDeviceDistance="+orientationHandler.previousResultDeviceDistance);
            PPApplication.logE("OrientationScanner.onSensorChanged", "orientationHandler.previousResultLight="+orientationHandler.previousResultLight);

            PPApplication.logE("OrientationScanner.onSensorChanged", "orientationHandler.resultDisplayUp="+orientationHandler.resultDisplayUp);
            PPApplication.logE("OrientationScanner.onSensorChanged", "orientationHandler.resultSideUp="+orientationHandler.resultSideUp);
            PPApplication.logE("OrientationScanner.onSensorChanged", "orientationHandler.resultDeviceDistance="+orientationHandler.resultDeviceDistance);
            PPApplication.logE("OrientationScanner.onSensorChanged", "orientationHandler.resultLight="+orientationHandler.resultLight);
            */

            //PPApplication.logE("OrientationScanner.onSensorChanged", "values chanhged");

            if (Event.getGlobalEventsRunning())
            {
                PhoneProfilesService service = PhoneProfilesService.getInstance();
                if (service != null) {
                    Context context = service.getApplicationContext();

                    Data workData = new Data.Builder()
                            .putString(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION)
                            .build();

                    String applicationEventOrientationScanInPowerSaveMode = ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode;

                    boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
                    if (isPowerSaveMode && applicationEventOrientationScanInPowerSaveMode.equals("2"))
                        // start scanning in power save mode is not allowed
                        return;

                    int interval = ApplicationPreferences.applicationEventOrientationScanInterval;
                    if (isPowerSaveMode && applicationEventOrientationScanInPowerSaveMode.equals("1"))
                        interval *= 2;

                    interval = interval / 2;

                    OneTimeWorkRequest worker =
                            new OneTimeWorkRequest.Builder(MainWorker.class)
                                    .addTag(MainWorker.HANDLE_EVENTS_ORIENTATION_SCANNER_WORK_TAG)
                                    .setInputData(workData)
                                    .setInitialDelay(interval, TimeUnit.SECONDS)
                                    //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                    .build();
                    try {
                        if (PPApplication.getApplicationStarted(true)) {
                            WorkManager workManager = PPApplication.getWorkManagerInstance();
                            if (workManager != null) {

        //                        //if (PPApplication.logEnabled()) {
        //                        ListenableFuture<List<WorkInfo>> statuses;
        //                        statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_ORIENTATION_SCANNER_WORK_TAG);
        //                        try {
        //                            List<WorkInfo> workInfoList = statuses.get();
        //                            PPApplication.logE("[TEST BATTERY] OrientationScanner.runEventsHandlerForOrientationChange", "for=" + MainWorker.HANDLE_EVENTS_ORIENTATION_SCANNER_WORK_TAG + " workInfoList.size()=" + workInfoList.size());
        //                        } catch (Exception ignored) {
        //                        }
        //                        //}

                                //workManager.enqueue(worker);
                                // MUST BE KEEP !!! REPLACE cause to not call worker, because is replaced with delat again !!!
                                workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_ORIENTATION_SCANNER_WORK_TAG, ExistingWorkPolicy.KEEP, worker);
                            }
                        }
                    } catch (Exception e) {
                        PPApplication.recordException(e);
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

    private static int convertLightToSensor(float light, float maxLight) {
        return (int)Math.round(light / maxLight * 10000.0);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        PPApplication.logE("[LISTENER CALL] OrientationScanner.onAccuracyChanged", "xxx");
    }

}
