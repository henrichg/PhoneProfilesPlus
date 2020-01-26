package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationScanner implements SensorEventListener {

    @Override
    public void onSensorChanged(SensorEvent event) {
        //PPApplication.logE("OrientationScanner.onSensorChanged", "xxx");

        if (PhoneProfilesService.getInstance() == null)
            return;

        Context appContext = PhoneProfilesService.getInstance().getApplicationContext();

        OrientationScannerHandlerThread handler = PPApplication.handlerThreadOrientationScanner;

        int sensorType = event.sensor.getType();
        //PPApplication.logE("OrientationScanner.onSensorChanged", "sensorType="+sensorType);

        //if (event.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW)
        //    return;

        if (sensorType == Sensor.TYPE_PROXIMITY) {
            //PPApplication.logE("PhoneProfilesService.onSensorChanged", "proximity value=" + event.values[0]);
            //PPApplication.logE("PhoneProfilesService.onSensorChanged", "proximity mMaxProximityDistance=" + mMaxProximityDistance);
            //if ((event.values[0] == 0) || (event.values[0] == mMaxProximityDistance)) {
            //if (event.timestamp - tmpDistanceTimestamp >= 250000000L /*1000000000L*/) {
            //    tmpDistanceTimestamp = event.timestamp;
            float mProximity = event.values[0];
            //if (mProximity == 0)
            int tmpDeviceDistance;
            if (mProximity < handler.mMaxProximityDistance)
                tmpDeviceDistance = OrientationScannerHandlerThread.DEVICE_ORIENTATION_DEVICE_IS_NEAR;
            else
                tmpDeviceDistance = OrientationScannerHandlerThread.DEVICE_ORIENTATION_DEVICE_IS_FAR;

            if (tmpDeviceDistance != handler.mDeviceDistance) {
                //PPApplication.logE("PhoneProfilesService.onSensorChanged", "mProximity="+mProximity);
                handler.mDeviceDistance = tmpDeviceDistance;
                runEventsHandlerForOrientationChange(appContext);
            }
            //}
            return;
        }
        if ((sensorType == Sensor.TYPE_ACCELEROMETER) || (sensorType == Sensor.TYPE_MAGNETIC_FIELD)) {
            if (PPApplication.magneticFieldSensor != null) {
                //PPApplication.logE("PhoneProfilesService.onSensorChanged", "magnetic value="+event.values[0]);

                if (sensorType == Sensor.TYPE_ACCELEROMETER) {
                    handler.mGravity = exponentialSmoothing(event.values, handler.mGravity, 0.2f);
                }
                if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
                    handler.mGeomagnetic = exponentialSmoothing(event.values, handler.mGeomagnetic, 0.5f);
                }
                if (event.timestamp - handler.tmpSideTimestamp >= 250000000L /*1000000000L*/) {
                    handler.tmpSideTimestamp = event.timestamp;
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
                    if (handler.mGravity != null && handler.mGeomagnetic != null) {
                        float[] R = new float[9];
                        float[] I = new float[9];
                        boolean success = SensorManager.getRotationMatrix(R, I, handler.mGravity, handler.mGeomagnetic);
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

                            //PPApplication.logE("PhoneProfilesService.onSensorChanged", "pitch=" + pitch);
                            //PPApplication.logE("PhoneProfilesService.onSensorChanged", "roll=" + roll);

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

                            if ((handler.tmpSideUp == OrientationScannerHandlerThread.DEVICE_ORIENTATION_UNKNOWN) ||
                                    (/*(side != DEVICE_ORIENTATION_UNKNOWN) &&*/ (side != handler.tmpSideUp))) {
                                handler.mEventCountSinceGZChanged = 0;

                                //PPApplication.logE("PhoneProfilesService.onSensorChanged", "azimuth="+azimuth);
                                //PPApplication.logE("PhoneProfilesService.onSensorChanged", "pitch=" + pitch);
                                //PPApplication.logE("PhoneProfilesService.onSensorChanged", "roll=" + roll);

                                handler.tmpSideUp = side;
                            } else {
                                ++handler.mEventCountSinceGZChanged;
                                if (handler.mEventCountSinceGZChanged == OrientationScannerHandlerThread.MAX_COUNT_GZ_CHANGE) {

                                    if (handler.tmpSideUp != handler.mSideUp) {
                                        //PPApplication.logE("PhoneProfilesService.onSensorChanged", "magnetic+accelerometer - send broadcast");

                                        handler.mSideUp = handler.tmpSideUp;

                                        if ((handler.mSideUp == OrientationScannerHandlerThread.DEVICE_ORIENTATION_DISPLAY_UP) ||
                                                (handler.mSideUp == OrientationScannerHandlerThread.DEVICE_ORIENTATION_DISPLAY_DOWN))
                                            handler.mDisplayUp = handler.mSideUp;

                                        /*
                                        if (mDisplayUp == DEVICE_ORIENTATION_DISPLAY_UP)
                                            PPApplication.logE("PhoneProfilesService.onSensorChanged", "now screen is facing up.");
                                        if (mDisplayUp == DEVICE_ORIENTATION_DISPLAY_DOWN)
                                            PPApplication.logE("PhoneProfilesService.onSensorChanged", "now screen is facing down.");

                                        if (mSideUp == DEVICE_ORIENTATION_UP_SIDE_UP)
                                            PPApplication.logE("PhoneProfilesService.onSensorChanged", "now up side is facing up.");
                                        if (mSideUp == DEVICE_ORIENTATION_DOWN_SIDE_UP)
                                            PPApplication.logE("PhoneProfilesService.onSensorChanged", "now down side is facing up.");
                                        if (mSideUp == DEVICE_ORIENTATION_RIGHT_SIDE_UP)
                                            PPApplication.logE("PhoneProfilesService.onSensorChanged", "now right side is facing up.");
                                        if (mSideUp == DEVICE_ORIENTATION_LEFT_SIDE_UP)
                                            PPApplication.logE("PhoneProfilesService.onSensorChanged", "now left side is facing up.");
                                        if (mSideUp == DEVICE_ORIENTATION_UNKNOWN)
                                            PPApplication.logE("PhoneProfilesService.onSensorChanged", "unknown side.");
                                        */

                                        runEventsHandlerForOrientationChange(appContext);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else {
                //PPApplication.logE("PhoneProfilesService.onSensorChanged", "accelerometer value="+event.values[0]);

                if (event.timestamp - handler.tmpSideTimestamp >= 250000000L /*1000000000L*/) {
                    handler.tmpSideTimestamp = event.timestamp;

                    float gravityZ = event.values[2];
                    if (handler.mGravityZ == 0) {
                        handler.mGravityZ = gravityZ;
                    } else {
                        if ((handler.mGravityZ * gravityZ) < 0) {
                            handler.mEventCountSinceGZChanged++;
                            if (handler.mEventCountSinceGZChanged == OrientationScannerHandlerThread.MAX_COUNT_GZ_CHANGE) {
                                //PPApplication.logE("PhoneProfilesService.onSensorChanged", "accelerometer - send broadcast");

                                handler.mGravityZ = gravityZ;
                                handler.mEventCountSinceGZChanged = 0;

                                if (gravityZ > 0) {
                                    //PPApplication.logE("PhoneProfilesService.onSensorChanged", "now screen is facing up.");
                                    handler.mSideUp = OrientationScannerHandlerThread.DEVICE_ORIENTATION_DISPLAY_UP;
                                } else if (gravityZ < 0) {
                                    //PPApplication.logE("PhoneProfilesService.onSensorChanged", "now screen is facing down.");
                                    handler.mSideUp = OrientationScannerHandlerThread.DEVICE_ORIENTATION_DISPLAY_DOWN;
                                }

                                if ((handler.mSideUp == OrientationScannerHandlerThread.DEVICE_ORIENTATION_DISPLAY_UP) ||
                                        (handler.mSideUp == OrientationScannerHandlerThread.DEVICE_ORIENTATION_DISPLAY_DOWN))
                                    handler.mDisplayUp = handler.mSideUp;

                                runEventsHandlerForOrientationChange(appContext);
                            }
                        } else {
                            if (handler.mEventCountSinceGZChanged > 0) {
                                handler.mGravityZ = gravityZ;
                                handler.mEventCountSinceGZChanged = 0;
                            }
                        }
                    }
                }
            }
        }
        if (sensorType == Sensor.TYPE_LIGHT) {
            //PPApplication.logE("PhoneProfilesService.onSensorChanged", "light value="+event.values[0]);
            //PPApplication.logE("PhoneProfilesService.onSensorChanged", "light mMaxLightDistance="+mMaxLightDistance);

            handler.mLight = EventPreferencesOrientation.convertLightToSensor(event.values[0], handler.mMaxLightDistance);
            //PPApplication.logE("PhoneProfilesService.onSensorChanged", "light mLight="+mLight);
            runEventsHandlerForOrientationChange(appContext);
        }

    }

    private void runEventsHandlerForOrientationChange(final Context context) {
        if (Event.getGlobalEventsRunning()) {
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PhoneProfilesService.runEventsHandlerForOrientationChange");

                PPApplication.logE("@@@ PhoneProfilesService.runEventsHandlerForOrientationChange", "-----------");

                if (mDeviceDistance == DEVICE_ORIENTATION_DEVICE_IS_NEAR)
                    PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "now device is NEAR.");
                else if (mDeviceDistance == DEVICE_ORIENTATION_DEVICE_IS_FAR)
                    PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "now device is FAR");
                else if (mDeviceDistance == DEVICE_ORIENTATION_UNKNOWN)
                    PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "unknown distance");

                if (mDisplayUp == DEVICE_ORIENTATION_DISPLAY_UP)
                    PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(D) now screen is facing up.");
                if (mDisplayUp == DEVICE_ORIENTATION_DISPLAY_DOWN)
                    PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(D) now screen is facing down.");
                if (mDisplayUp == DEVICE_ORIENTATION_UNKNOWN)
                    PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(D) unknown display orientation.");

                if (mSideUp == DEVICE_ORIENTATION_DISPLAY_UP)
                    PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(S) now screen is facing up.");
                if (mSideUp == DEVICE_ORIENTATION_DISPLAY_DOWN)
                    PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(S) now screen is facing down.");

                if (mSideUp == mDisplayUp)
                    PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(S) now device is horizontal.");
                if (mSideUp == DEVICE_ORIENTATION_UP_SIDE_UP)
                    PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(S) now up side is facing up.");
                if (mSideUp == DEVICE_ORIENTATION_DOWN_SIDE_UP)
                    PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(S) now down side is facing up.");
                if (mSideUp == DEVICE_ORIENTATION_RIGHT_SIDE_UP)
                    PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(S) now right side is facing up.");
                if (mSideUp == DEVICE_ORIENTATION_LEFT_SIDE_UP)
                    PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(S) now left side is facing up.");
                if (mSideUp == DEVICE_ORIENTATION_UNKNOWN)
                    PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(S) unknown side.");

                PPApplication.logE("PhoneProfilesService.runEventsHandlerForOrientationChange", "(L) light=" + mLight);

                PPApplication.logE("@@@ PhoneProfilesService.runEventsHandlerForOrientationChange", "-----------");
            }*/

            // start events handler
            EventsHandler eventsHandler = new EventsHandler(context);
            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION);
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
