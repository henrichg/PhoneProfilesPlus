package com.noob.noobcameraflash.managers;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;

import androidx.annotation.NonNull;

import com.noob.noobcameraflash.utilities.CameraFlashUtility;
import com.noob.noobcameraflash.utilities.CameraUtilMarshMallow;

/**
 * Created by abhi on 23/10/16.
 */

public class NoobCameraManager {
    private CameraFlashUtility mCameraUtil;
    //region singleton
    private static NoobCameraManager mInstance;

    public static NoobCameraManager getInstance() {
        if (mInstance == null) {
            mInstance = new NoobCameraManager();
        }
        return mInstance;
    }

    private NoobCameraManager() {
    }
    //endregion singleton

    public void init(@NonNull Context context) throws CameraAccessException, SecurityException {
        mCameraUtil = new CameraUtilMarshMallow(context);
    }

    @SuppressWarnings("unused")
    public void setCameraUtil(CameraFlashUtility cameraUtil) {
        mCameraUtil = cameraUtil;
    }

    public boolean isFlashOn() {
        return mCameraUtil.isFlashOn();
    }

    public void turnOnFlash() throws CameraAccessException {
        mCameraUtil.turnOnFlash();
    }

    public void turnOffFlash() throws CameraAccessException {
        mCameraUtil.turnOffFlash();
    }

    @SuppressWarnings("unused")
    public void toggleFlash() throws CameraAccessException {
        if (isFlashOn()) {
            turnOffFlash();
        } else {
            turnOnFlash();
        }
    }

    //May or may not release all resources
    @SuppressWarnings("unused")
    public void release() {
        if (mCameraUtil != null)
            mCameraUtil.release();
    }
}
