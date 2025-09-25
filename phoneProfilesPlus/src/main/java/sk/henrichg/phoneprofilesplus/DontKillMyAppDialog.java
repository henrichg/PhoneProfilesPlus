package sk.henrichg.phoneprofilesplus;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import dev.doubledot.doki.views.DokiContentView;

public class DontKillMyAppDialog extends DialogFragment
{
    private AlertDialog mDialog;
    private AppCompatActivity activity;

    public DontKillMyAppDialog() {
    }

    public DontKillMyAppDialog(AppCompatActivity activity)
    {
        this.activity = activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (AppCompatActivity) getActivity();
        if (this.activity != null) {
            //GlobalGUIRoutines.lockScreenOrientation(activity);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                    activity.getString(R.string.phone_profiles_pref_applicationDoNotKillMyApp_dialogTitle), null);
            //dialogBuilder.setTitle(R.string.phone_profiles_pref_applicationDoNotKillMyApp_dialogTitle);
            dialogBuilder.setPositiveButton(android.R.string.ok, null);

            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_do_not_kill_my_app, null);
            dialogBuilder.setView(layout);

            DokiContentView doki = layout.findViewById(R.id.do_not_kill_my_app_dialog_dokiContentView);
            if (doki != null) {
                doki.setButtonsVisibility(false);
                doki.loadContent(Build.MANUFACTURER.toLowerCase().replace(" ", "-"));
            }

            mDialog = dialogBuilder.create();

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        }
        return mDialog;
    }

    /*
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (activity != null)
            GlobalGUIRoutines.unlockScreenOrientation(activity);
    }
    */

    void showDialog() {
        if ((activity != null) && (!activity.isFinishing())) {
            //mDialog.show();
            FragmentManager manager = activity.getSupportFragmentManager();
            if (!manager.isDestroyed())
                show(manager, "DONT_KILL_MY_APP_DIALOG");
        }
    }

}
