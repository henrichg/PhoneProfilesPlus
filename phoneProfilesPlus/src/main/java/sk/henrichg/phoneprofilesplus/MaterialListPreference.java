package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.ListPreference;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.View;

import java.lang.reflect.Field;

public class MaterialListPreference extends ListPreference {

    private Context context;
    private AlertDialog dialog;

    public MaterialListPreference(Context context) {
        super(context);
        init(context/*, null*/);
    }

    public MaterialListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context/*, attrs*/);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context/*, attrs*/);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialListPreference(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context/*, attrs*/);
    }

    private void init(Context context/*, AttributeSet attrs*/) {
        this.context = context;
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void setEntries(CharSequence[] entries) {
        super.setEntries(entries);
        /*if (dialog != null) {
            dialog.setItems(entries);
        }*/
    }

    @Override
    public Dialog getDialog() {
        return dialog;
    }

    @Override
    protected void showDialog(Bundle state) {
        if (getEntries() == null || getEntryValues() == null) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array.");
        }

        int preselect = findIndexOfValue(getValue());
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context)
                        .setTitle(getDialogTitle())
                        .setIcon(getDialogIcon())
                        .setOnDismissListener(this)
                        .setSingleChoiceItems(getEntries(), preselect, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MaterialListPreference.this.onClick(null, DialogInterface.BUTTON_POSITIVE);
                                if (which >= 0 && getEntryValues() != null) {
                                    try {
                                        //noinspection JavaReflectionMemberAccess
                                        Field clickedIndex =
                                                ListPreference.class.getDeclaredField("mClickedDialogEntryIndex");
                                        clickedIndex.setAccessible(true);
                                        clickedIndex.set(MaterialListPreference.this, which);
                                        MaterialListPreference.this.dialog.dismiss();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

        builder.setNegativeButton(getNegativeButtonText(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MaterialListPreference.this.onClick(
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

        dialog = builder.create();
        if (state != null) {
            dialog.onRestoreInstanceState(state);
        }
        //onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
        dialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
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