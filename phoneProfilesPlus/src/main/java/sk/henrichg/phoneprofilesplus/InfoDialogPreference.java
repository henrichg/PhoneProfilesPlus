package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;

public class InfoDialogPreference extends DialogPreference {

    private String infoText;

    private Context _context;

    AlertDialog mDialog;

    public InfoDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.InfoDialogPreference);

        infoText = typedArray.getString(R.styleable.InfoDialogPreference_infoText);

        typedArray.recycle();
    }

    @Override
    protected void showDialog(Bundle state) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(_context);
        dialogBuilder.setTitle(getDialogTitle());
        dialogBuilder.setMessage(infoText);
        dialogBuilder.setPositiveButton(android.R.string.ok, null);

        MaterialDialogsPrefUtil.registerOnActivityDestroyListener(this, this);

        mDialog = dialogBuilder.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        MaterialDialogsPrefUtil.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
    }

    void setInfoText(String _infoText) {
        this.infoText = _infoText;
    }

}
