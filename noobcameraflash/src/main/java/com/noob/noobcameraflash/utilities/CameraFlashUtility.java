package com.noob.noobcameraflash.utilities;

import android.hardware.camera2.CameraAccessException;

/**
 * Created by Abhishek on 23-11-2015.
 */
public interface CameraFlashUtility {
    boolean isFlashOn();
    void turnOnFlash() throws CameraAccessException;
    void turnOffFlash() throws CameraAccessException;
    @SuppressWarnings("unused")
    void setTorchModeCallback(TorchModeCallback torchModeCallback);
    void release();
}
