package com.noob.noobcameraflash.utilities;

import android.content.Context;

/**
 * Created by Abhishek on 08-12-2016.
 */

public abstract class BaseCameraUtil implements CameraFlashUtility {
    private final Context mContext;

    //private boolean torchModeOn = false;
    private TorchMode mTorchMode = TorchMode.None;
    private TorchModeCallback mTorchModeCallback;

    //region Constructors
    BaseCameraUtil(Context context) {
        mContext = context;
    }
    //endregion

    //region Overrides
    @Override
    public boolean isFlashOn() {
        return mTorchMode == TorchMode.SwitchedOn;
    }

    @SuppressWarnings("unused")
    @Override
    public void setTorchModeCallback(TorchModeCallback torchModeCallback) {
        mTorchModeCallback = torchModeCallback;
    }
    //endregion

    //protected methods
    void onCameraTorchModeChanged(@SuppressWarnings("SameParameterValue") TorchMode torchMode) {
        if (mTorchModeCallback != null) {
            mTorchModeCallback.onTorchModeChanged(torchMode);
        }
    }

    //endregion

    //region Accessors

    Context getContext() {
        return mContext;
    }

    @SuppressWarnings("unused")
    public TorchMode getTorchMode() {
        return mTorchMode;
    }

    void setTorchMode(TorchMode torchMode) {
        mTorchMode = torchMode;
    }
    //endregion
}
