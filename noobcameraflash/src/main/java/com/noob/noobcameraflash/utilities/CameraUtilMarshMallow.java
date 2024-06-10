package com.noob.noobcameraflash.utilities;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * Created by Abhishek on 28-11-2015.
 */
public class CameraUtilMarshMallow extends BaseCameraUtil {
    private CameraManager mCameraManager;
    private CameraManager.TorchCallback mTorchCallback;

    public CameraUtilMarshMallow(Context context) throws CameraAccessException {
        super(context);
        openCamera();
    }

    private void openCamera() throws CameraAccessException {
        if (mCameraManager == null)
            mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);

        // check for all cameras
        boolean flashAvailable = false;
        String[] cameraIds = mCameraManager.getCameraIdList();
        for (String id : cameraIds) {
            flashAvailable = isFlashAvailable(id);
        }

        if (flashAvailable /*isFlashAvailable()*/) {
            mTorchCallback = new CameraManager.TorchCallback() {
                @Override
                public void onTorchModeUnavailable(@NonNull String cameraId) {
                    super.onTorchModeUnavailable(cameraId);
                    //onCameraTorchModeChanged(TorchMode.Unavailable);
                }

                @Override
                public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
                    super.onTorchModeChanged(cameraId, enabled);
                    /*if (enabled)
                        setTorchMode(TorchMode.SwitchedOn);
                    else
                        setTorchMode(TorchMode.SwitchedOff);*/
                }
            };
            mCameraManager.registerTorchCallback(mTorchCallback, null);
        }
    }

    private boolean isFlashAvailable(String cameraId) throws CameraAccessException {
        CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId /*"0"*/);
        return cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
    }

    @Override
    public void turnOnFlash() throws CameraAccessException {
        String[] cameraIds = getCameraManager().getCameraIdList();
        for (String id : cameraIds) {
            // only log exception, because for all cameras must be set torch
            try {
                CameraCharacteristics characteristics = getCameraManager().getCameraCharacteristics(id);
                if (characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                    // added facing check - allowed is only back flash
                    Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if ((facing != null) && (facing == CameraCharacteristics.LENS_FACING_BACK)) {
                        getCameraManager().setTorchMode(id, true);
                        //setTorchMode(TorchMode.SwitchedOn);
                    }
                }
            } catch (Exception e) {
                Log.e("CameraUtilMarshMallow.turnOnFlash", Log.getStackTraceString(e));
            }
        }
    }

    @Override
    public void turnOffFlash() throws CameraAccessException {
        String[] cameraIds = getCameraManager().getCameraIdList();
        for (String id : cameraIds) {
            // only log exception, because for all cameras must be set torch
            try {
                CameraCharacteristics characteristics = getCameraManager().getCameraCharacteristics(id);
                if (characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                    // added facing check - allowed is only back flash
                    Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if ((facing != null) && (facing == CameraCharacteristics.LENS_FACING_BACK)) {
                        getCameraManager().setTorchMode(id, false);
                        //setTorchMode(TorchMode.SwitchedOff);
                    }
                }
            } catch (Exception e) {
                Log.e("CameraUtilMarshMallow.turnOffFlash", Log.getStackTraceString(e));
            }
        }
    }

    @Override
    public void release() {
        if (mCameraManager != null) {
            mCameraManager.unregisterTorchCallback(mTorchCallback);
            mCameraManager = null;
        }
    }

    //region Accessors

    private CameraManager getCameraManager() throws CameraAccessException {
        if (mCameraManager == null) {
            openCamera();
        }
        return mCameraManager;
    }
    //endregion
}
