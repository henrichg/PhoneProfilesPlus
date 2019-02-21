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
import android.preference.EditTextPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MaterialEditTextPreference extends EditTextPreference {

    private AlertDialog dialog;
    private EditText editText;

    private final Context context;

    @SuppressWarnings("unused")
    public MaterialEditTextPreference(Context context) {
        super(context);
        this.context = context;
        init(context, null);
    }

    @SuppressWarnings("unused")
    public MaterialEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context, attrs);
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context, attrs);
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialEditTextPreference(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        editText = new AppCompatEditText(context, attrs);
        // Give it an ID so it can be saved/restored
        editText.setId(android.R.id.edit);
        editText.setEnabled(true);
    }

    @Override
    protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
        ((ViewGroup) dialogView)
                .addView(
                        editText,
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onBindDialogView(View view) {
        EditText editText = this.editText;
        editText.setText(getText());
        // Initialize cursor to end of text
        if (editText.getText().length() > 0) {
            editText.setSelection(editText.length());
        }
        ViewParent oldParent = editText.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(editText);
            }
            onAddEditTextToDialogView(view, editText);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String value = editText.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }

    @Override
    public EditText getEditText() {
        return editText;
    }

    @Override
    public Dialog getDialog() {
        return dialog;
    }

    @Override
    protected void showDialog(Bundle state) {
        AlertDialog.Builder mBuilder =
                new AlertDialog.Builder(getContext())
                        .setTitle(getDialogTitle())
                        .setIcon(getDialogIcon());

        mBuilder.setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MaterialEditTextPreference.this.onClick(
                        dialog, DialogInterface.BUTTON_POSITIVE);
            }
        });
        mBuilder.setNegativeButton(getNegativeButtonText(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MaterialEditTextPreference.this.onClick(
                        dialog, DialogInterface.BUTTON_NEGATIVE);
            }
        });

        mBuilder.setOnDismissListener(this);

        @SuppressLint("InflateParams")
        View layout = LayoutInflater.from(getContext()).inflate(R.layout.material_edit_text_preference_dialog, null);
        onBindDialogView(layout);

        TextView message = layout.findViewById(android.R.id.message);
        if (getDialogMessage() != null && getDialogMessage().toString().length() > 0) {
            message.setVisibility(View.VISIBLE);
            message.setText(getDialogMessage());
        } else {
            message.setVisibility(View.GONE);
        }
        mBuilder.setView(layout);

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        dialog = mBuilder.create();
        if (state != null)
            dialog.onRestoreInstanceState(state);

        requestInputMethod(dialog);

        if (!((Activity)context).isFinishing())
            dialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    /** Copied from DialogPreference.java */
    private void requestInputMethod(Dialog dialog) {
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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