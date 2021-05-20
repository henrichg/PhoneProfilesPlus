package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;

class FastAccessDurationProfileDialog
{
    private final FastAccessDurationDialog fastAccessDurationDialog;

    private final DataWrapper dataWrapper;

    private final AlertDialog mDialog;
    private final Activity activity;

    private final LinearLayout linlaProgress;
    private final ListView listView;

    FastAccessDurationProfileDialog(Activity activity, FastAccessDurationDialog fastAccessDurationDialog)
    {
        this.fastAccessDurationDialog = fastAccessDurationDialog;
        this.activity = activity;

        dataWrapper = new DataWrapper(activity.getApplicationContext(), false, 0, false);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.profile_preferences_afterDurationProfile);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = activity.getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.dialog_profile_preference, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(dialog -> {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);

            FastAccessDurationProfileDialog.this.onShow();
        });

        linlaProgress = layout.findViewById(R.id.profile_pref_dlg_linla_progress);

        listView = layout.findViewById(R.id.profile_pref_dlg_listview);

        listView.setOnItemClickListener((parent, v, position, id) -> doOnItemSelected(position));

    }

    @SuppressLint("StaticFieldLeak")
    private void onShow(/*DialogInterface dialog*/) {
        new ShowDialogAsyncTask(fastAccessDurationDialog.mAfterDoProfile, this, activity).execute();
    }

    void doOnItemSelected(int position)
    {
        long profileId = Profile.PROFILE_NO_ACTIVATE;
        if (position > 0) {
            synchronized (dataWrapper.profileList) {
                profileId = dataWrapper.profileList.get(position - 1)._id;
            }
        }
        fastAccessDurationDialog.updateAfterDoProfile(profileId);
        mDialog.dismiss();
    }

    public void show() {
        if (!activity.isFinishing())
            mDialog.show();
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

        private final WeakReference<FastAccessDurationProfileDialog> dialogWeakRef;
        private final WeakReference<Activity> activityWeakReference;
        final long afterDoProfile;

        public ShowDialogAsyncTask(final long afterDoProfile,
                FastAccessDurationProfileDialog dialog,
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
            FastAccessDurationProfileDialog dialog = dialogWeakRef.get();
            if (dialog != null) {
                dialog.dataWrapper.fillProfileList(true, ApplicationPreferences.applicationEditorPrefIndicator);
                synchronized (dialog.dataWrapper.profileList) {
                    //noinspection Java8ListSort
                    Collections.sort(dialog.dataWrapper.profileList, new AlphabeticallyComparator());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            FastAccessDurationProfileDialog dialog = dialogWeakRef.get();
            Activity activity = activityWeakReference.get();
            if ((dialog != null) && (activity != null)) {
                //listView.setVisibility(View.VISIBLE);
                dialog.linlaProgress.setVisibility(View.GONE);

                FastAccessDurationProfileAdapter adapter = new FastAccessDurationProfileAdapter(
                        dialog, activity, afterDoProfile, dialog.dataWrapper.profileList);
                dialog.listView.setAdapter(adapter);

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
            }
        }

    }

}
