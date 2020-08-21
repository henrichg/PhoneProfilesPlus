package sk.henrichg.phoneprofilesplus;

import android.os.HandlerThread;

public class OrientationScannerHandlerThread extends HandlerThread {

    int eventCountSinceGZChanged = 0;
    static final int MAX_COUNT_GZ_CHANGE = 5;
    float[] gravity = new float[3];
    float[] geomagnetic = new float[3];
    float gravityZ = 0;  //gravity acceleration along the z axis

    float maxProximityDistance;
    float maxLightDistance;

    static final int DEVICE_ORIENTATION_UNKNOWN = 0;
    static final int DEVICE_ORIENTATION_RIGHT_SIDE_UP = 3;
    static final int DEVICE_ORIENTATION_LEFT_SIDE_UP = 4;
    static final int DEVICE_ORIENTATION_UP_SIDE_UP = 5;
    static final int DEVICE_ORIENTATION_DOWN_SIDE_UP = 6;
    static final int DEVICE_ORIENTATION_HORIZONTAL = 9;

    static final int DEVICE_ORIENTATION_DISPLAY_UP = 1;
    static final int DEVICE_ORIENTATION_DISPLAY_DOWN = 2;

    static final int DEVICE_ORIENTATION_DEVICE_IS_NEAR = 7;
    static final int DEVICE_ORIENTATION_DEVICE_IS_FAR = 8;

    int tmpSideUp = DEVICE_ORIENTATION_UNKNOWN;
    long tmpSideTimestamp = 0;

    int previousResultDisplayUp = DEVICE_ORIENTATION_UNKNOWN;
    int previousResultSideUp = DEVICE_ORIENTATION_UNKNOWN;
    int previousResultDeviceDistance = DEVICE_ORIENTATION_UNKNOWN;
    int previousResultLight = 0;

    int resultDisplayUp = DEVICE_ORIENTATION_UNKNOWN;
    int resultSideUp = DEVICE_ORIENTATION_UNKNOWN;
    int resultDeviceDistance = DEVICE_ORIENTATION_UNKNOWN;
    int resultLight = 0;

    /*public OrientationScannerHandlerThread(String name) {
        super(name);
    }*/

    public OrientationScannerHandlerThread(String name, int priority) {
        super(name, priority);
    }

}
