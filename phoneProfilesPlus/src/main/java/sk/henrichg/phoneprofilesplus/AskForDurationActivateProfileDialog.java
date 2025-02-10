package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.lang.ref.WeakReference;
import java.util.Comparator;

public class AskForDurationActivateProfileDialog extends DialogFragment
{
    private AskForDurationDialog askForDurationDialog;

    private DataWrapper dataWrapper;

    private AlertDialog mDialog;
    private AppCompatActivity activity;

    private LinearLayout linlaProgress;
    private ListView listView;
    private RelativeLayout emptyList;

    private ShowDialogAsyncTask showDialogAsyncTask = null;

    public AskForDurationActivateProfileDialog() {
    }

    public AskForDurationActivateProfileDialog(AppCompatActivity activity, AskForDurationDialog askForDurationDialog)
    {
        this.askForDurationDialog = askForDurationDialog;
        this.activity = activity;
        dataWrapper = new DataWrapper(activity.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            GlobalGUIRoutines.lockScreenOrientation(activity);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                    activity.getString(R.string.profile_preferences_afterDurationProfile), null);
            //dialogBuilder.setTitle(R.string.profile_preferences_afterDurationProfile);
            dialogBuilder.setCancelable(true);
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_profile_preference, null);
            dialogBuilder.setView(layout);

            mDialog = dialogBuilder.create();

            mDialog.setOnShowListener(dialog -> {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);

                doShow();
            });

            linlaProgress = layout.findViewById(R.id.profile_pref_dlg_linla_progress);

            listView = layout.findViewById(R.id.profile_pref_dlg_listview);
            emptyList = layout.findViewById(R.id.profile_pref_dlg_empty);

            //noinspection DataFlowIssue
            listView.setOnItemClickListener((parent, v, position, id) -> doOnItemSelected(position));
        }
        return mDialog;
    }

    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if ((showDialogAsyncTask != null) &&
                showDialogAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            showDialogAsyncTask.cancel(true);
        }
        showDialogAsyncTask = null;
        dataWrapper.invalidateDataWrapper();

        if (activity != null)
            GlobalGUIRoutines.unlockScreenOrientation(activity);
    }

    private void doShow() {
        showDialogAsyncTask = new ShowDialogAsyncTask(askForDurationDialog.mAfterDoProfile, this, activity);
        showDialogAsyncTask.execute();
    }

    void doOnItemSelected(int position)
    {
        long profileId = Profile.PROFILE_NO_ACTIVATE;
        if (position > 0) {
//            PPApplicationStatic.logE("[SYNCHRONIZED] AskForDurationActivateProfileDialog.doOnItemSelected", "DataWrapper.profileList");
            synchronized (dataWrapper.profileList) {
                profileId = dataWrapper.profileList.get(position - 1)._id;
            }
        }
        askForDurationDialog.updateAfterDoProfile(profileId);
        dismiss();
    }

    void showDialog() {
        if ((activity != null) && (!activity.isFinishing()))
            show(activity.getSupportFragmentManager(), "ASK_FOR_DURATION_ACTIVATE_PROFILE_DIALOG");
    }

    private static class AlphabeticallyComparator implements Comparator<Profile> {

        public int compare(Profile lhs, Profile rhs) {
            if (PPApplication.collator != null)
                return PPApplication.collator.compare(lhs._name, rhs._name);
            else
                return 0;
        }
    }

    private static class ShowDialogAsyncTask extends AsyncTask<Void, Integer, Void> {

        private final WeakReference<AskForDurationActivateProfileDialog> dialogWeakRef;
        private final WeakReference<Activity> activityWeakReference;
        final long afterDoProfile;

        public ShowDialogAsyncTask(final long afterDoProfile,
                AskForDurationActivateProfileDialog dialog,
                Activity activity) {
            this.dialogWeakRef = new WeakReference<>(dialog);
            this.activityWeakReference = new WeakReference<>(activity);
            this.afterDoProfile = afterDoProfile;
        }

        /*@Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            //listView.setVisibility(View.GONE);
            //linlaProgress.setVisibility(View.VISIBLE);
        }*/

        @Override
        protected Void doInBackground(Void... params) {
            AskForDurationActivateProfileDialog dialog = dialogWeakRef.get();
            if (dialog != null) {
                dialog.dataWrapper.fillProfileList(true, ApplicationPreferences.applicationEditorPrefIndicator);
//                PPApplicationStatic.logE("[SYNCHRONIZED] AskForDurationActivateProfileDialog.ShowDialogAsyncTask", "DataWrapper.profileList");
                synchronized (dialog.dataWrapper.profileList) {
                    dialog.dataWrapper.profileList.sort(new AlphabeticallyComparator());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            AskForDurationActivateProfileDialog dialog = dialogWeakRef.get();
            Activity activity = activityWeakReference.get();
            if ((dialog != null) && (activity != null)) {
                dialog.linlaProgress.setVisibility(View.GONE);

                final Handler handler = new Handler(activity.getMainLooper());
                handler.post(() -> {
                    dialog.listView.setVisibility(View.VISIBLE);
                    if (dialog.dataWrapper.profileList.isEmpty()) {
                        dialog.listView.setVisibility(View.GONE);
                        dialog.emptyList.setVisibility(View.VISIBLE);
                    } else {
                        dialog.emptyList.setVisibility(View.GONE);
                        dialog.listView.setVisibility(View.VISIBLE);
                    }

                    AskForDurationActivateProfileAdapter adapter = new AskForDurationActivateProfileAdapter(
                            dialog, activity, afterDoProfile, dialog.dataWrapper.profileList);
                    dialog.listView.setAdapter(adapter);

                    //noinspection ExtractMethodRecommender
                    int position;
                    long iProfileId;
                    iProfileId = afterDoProfile;
                    if (iProfileId == Profile.PROFILE_NO_ACTIVATE)
                        position = 0;
                    else {
                        boolean found = false;
                        position = 0;
                        for (Profile profile : dialog.dataWrapper.profileList) {
                            if (profile._id == iProfileId) {
                                found = true;
                                break;
                            }
                            position++;
                        }
                        if (found) {
                            position++;
                        } else
                            position = 0;
                    }
                    dialog.listView.setSelection(position);
                });
            }
        }

    }

}
