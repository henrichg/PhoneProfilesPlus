package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.MultiSelectListPreference;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashSet;
import java.util.Set;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MaterialMultiSelectListPreference extends MultiSelectListPreference {

    private Context context;
    private AlertDialog mDialog;

    private boolean[] indices;

    @SuppressWarnings("unused")
    public MaterialMultiSelectListPreference(Context context) {
        super(context);
        init(context/*, null*/);
    }

    @SuppressWarnings("unused")
    public MaterialMultiSelectListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context/*, attrs*/);
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialMultiSelectListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context/*, attrs*/);
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialMultiSelectListPreference(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context/*, attrs*/);
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void setEntries(CharSequence[] entries) {
        super.setEntries(entries);
        /*if (mDialog != null) {
            mDialog.setItems(entries);
        }*/
    }

    private void init(Context context/*, AttributeSet attrs*/) {
        this.context = context;
    }

    @Override
    public Dialog getDialog() {
        return mDialog;
    }

    @Override
    protected void showDialog(Bundle state) {
        indices = new boolean[getEntries().length];
        //Log.e("MaterialMultiSelectListPreference.showDialog", "getValues().size="+getValues().size());
        for (String s : getValues()) {
            //Log.e("MaterialMultiSelectListPreference.showDialog", "getValues().s="+s);
            int index = findIndexOfValue(s);
            if (index != -1)
                indices[index] = true;
        }

        AlertDialog.Builder builder =
                new AlertDialog.Builder(context)
                        .setTitle(getDialogTitle())
                        .setIcon(getDialogIcon())
                        .setOnDismissListener(this)
                        .setMultiChoiceItems(getEntries(), indices, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                indices[which] = isChecked;
                            }
                        });

        builder.setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Set<String> values = new HashSet<>();
                for (int i = 0; i < indices.length; i++) {
                    if (indices[i])
                        values.add(getEntryValues()[i].toString());
                }
                if (callChangeListener(values)) {
                    setValues(values);
                }
                MaterialMultiSelectListPreference.this.onClick(
                        dialog, DialogInterface.BUTTON_POSITIVE);
            }
        });
        builder.setNegativeButton(getNegativeButtonText(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MaterialMultiSelectListPreference.this.onClick(
                        dialog, DialogInterface.BUTTON_NEGATIVE);
            }
        });

        final View contentView = onCreateDialogView();
        if (contentView != null) {
            onBindDialogView(contentView);
            builder.setView(contentView);
        } else {
            builder.setMessage(getDialogMessage());
        }

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        mDialog = builder.create();
        if (state != null) {
            mDialog.onRestoreInstanceState(state);
        }
        if (!((Activity)context).isFinishing())
            mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        Dialog dialog = getDialog();
        if (dialog == null || !dialog.isShowing()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.isDialogShowing = true;
        myState.dialogBundle = dialog.onSaveInstanceState();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        if (myState.isDialogShowing) {
            showDialog(myState.dialogBundle);
        }
    }

    // From DialogPreference
    private static class SavedState extends BaseSavedState {

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
        boolean isDialogShowing;
        Bundle dialogBundle;

        @SuppressLint("ParcelClassLoader")
        SavedState(Parcel source) {
            super(source);
            isDialogShowing = source.readInt() == 1;
            dialogBundle = source.readBundle();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(isDialogShowing ? 1 : 0);
            dest.writeBundle(dialogBundle);
        }
    }
}