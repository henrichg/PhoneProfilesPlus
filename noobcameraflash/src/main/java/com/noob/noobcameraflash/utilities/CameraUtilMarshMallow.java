package com.noob.noobcameraflash.utilities;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

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
        if (isFlashAvailable()) {
            mTorchCallback = new CameraManager.TorchCallback() {
                @Override
                public void onTorchModeUnavailable(@NonNull String cameraId) {
                    super.onTorchModeUnavailable(cameraId);
                    onCameraTorchModeChanged(TorchMode.Unavailable);
                }

                @Override
                public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
                    super.onTorchModeChanged(cameraId, enabled);
                    if (enabled)
                        setTorchMode(TorchMode.SwitchedOn);
                    else
                        setTorchMode(TorchMode.SwitchedOff);
                }
            };
            mCameraManager.registerTorchCallback(mTorchCallback, null);
        }
    }

    private boolean isFlashAvailable() throws CameraAccessException {
        CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics("0");
        return cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
    }

    @Override
    public void turnOnFlash() throws CameraAccessException {
        String[] cameraIds = getCameraManager().getCameraIdList();
        for (String id : cameraIds) {
            CameraCharacteristics characteristics = getCameraManager().getCameraCharacteristics(id);
            if (characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                // added facing check - allowed is only back flash
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if ((facing != null) && (facing == CameraCharacteristics.LENS_FACING_BACK)) {
                    getCameraManager().setTorchMode(id, true);
                    setTorchMode(TorchMode.SwitchedOn);
                }
            }
        }
    }

    @Override
    public void turnOffFlash() throws CameraAccessException {
        String[] cameraIds = getCameraManager().getCameraIdList();
        for (String id : cameraIds) {
            CameraCharacteristics characteristics = getCameraManager().getCameraCharacteristics(id);
            if (characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                // added facing check - allowed is only back flash
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if ((facing != null) && (facing == CameraCharacteristics.LENS_FACING_BACK)) {
                    getCameraManager().setTorchMode(id, false);
                    setTorchMode(TorchMode.SwitchedOff);
                }
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
