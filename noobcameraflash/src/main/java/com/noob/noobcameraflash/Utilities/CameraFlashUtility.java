package com.noob.noobcameraflash.Utilities;

import android.hardware.camera2.CameraAccessException;

/**
 * Created by Abhishek on 23-11-2015.
 */
public interface CameraFlashUtility {
    boolean isFlashOn();
    void turnOnFlash() throws CameraAccessException;
    void turnOffFlash() throws CameraAccessException;
    void setTorchModeCallback(TorchModeCallback torchModeCallback);
    void release();
}
