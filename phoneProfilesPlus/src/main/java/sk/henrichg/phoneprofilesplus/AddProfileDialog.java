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
import java.util.ArrayList;
import java.util.List;

class AddProfileDialog
{
    private final EditorProfileListFragment profileListFragment;

    final AlertDialog mDialog;
    private final Activity activity;

    private final LinearLayout linlaProgress;
    private final ListView listView;

    AddProfileDialog(Activity activity, EditorProfileListFragment profileListFragment)
    {
        this.profileListFragment = profileListFragment;
        this.activity = activity;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.new_profile_predefined_profiles_dialog);
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

            AddProfileDialog.this.onShow();
        });

        linlaProgress = layout.findViewById(R.id.profile_pref_dlg_linla_progress);

        listView = layout.findViewById(R.id.profile_pref_dlg_listview);

        listView.setOnItemClickListener((parent, v, position, id) -> doOnItemSelected(position));

    }

    @SuppressLint("StaticFieldLeak")
    private void onShow(/*DialogInterface dialog*/) {
        AddProfileDialog.GetProfilesAsyncTask asyncTask = new AddProfileDialog.GetProfilesAsyncTask(this, activity, profileListFragment.activityDataWrapper);
        asyncTask.execute();

/*        new AsyncTask<Void, Integer, Void>() {

            final List<Profile> profileList = new ArrayList<>();

            //@Override
            //protected void onPreExecute()
            //{
            //    super.onPreExecute();
                //listView.setVisibility(View.GONE);
                //linlaProgress.setVisibility(View.VISIBLE);
            //}

            @Override
            protected Void doInBackground(Void... params) {
                boolean applicationEditorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator;
                Profile profile;
                profile = DataWrapper.getNonInitializedProfile(
                        activity.getResources().getString(R.string.profile_name_default),
                        Profile.PROFILE_ICON_DEFAULT, 0);
                profile.generateIconBitmap(activity.getApplicationContext(), false, 0xFF, false);
                if (applicationEditorPrefIndicator)
                    profile.generatePreferencesIndicator(activity.getApplicationContext(), false, 0xFF);
                profileList.add(profile);
                for (int index = 0; index < 7; index++) {
                    profile = profileListFragment.activityDataWrapper.getPredefinedProfile(index, false, activity);
                    profile.generateIconBitmap(activity.getApplicationContext(), false, 0xFF, false);
                    if (applicationEditorPrefIndicator)
                        profile.generatePreferencesIndicator(activity.getApplicationContext(), false, 0xFF);
                    profileList.add(profile);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                //listView.setVisibility(View.VISIBLE);
                linlaProgress.setVisibility(View.GONE);

                AddProfileAdapter addProfileAdapter = new AddProfileAdapter(AddProfileDialog.this, activity, profileList);
                listView.setAdapter(addProfileAdapter);
            }

        }.execute();*/
    }

    void doOnItemSelected(int position)
    {
        profileListFragment.startProfilePreferencesActivity(null, position);
        mDialog.dismiss();
    }

    public void show() {
        if (!activity.isFinishing())
            mDialog.show();
    }

    private static class GetProfilesAsyncTask extends AsyncTask<Void, Integer, Void> {

        final List<Profile> profileList = new ArrayList<>();

        private final WeakReference<AddProfileDialog> dialogWeakRef;
        private final WeakReference<Activity> activityWeakRef;
        final DataWrapper dataWrapper;

        public GetProfilesAsyncTask(final AddProfileDialog dialog,
                                  final Activity activity,
                                  final DataWrapper dataWrapper) {
            this.dialogWeakRef = new WeakReference<>(dialog);
            this.activityWeakRef = new WeakReference<>(activity);
            this.dataWrapper = dataWrapper.copyDataWrapper();
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
            Activity activity = activityWeakRef.get();
            if (activity != null) {
                boolean applicationEditorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator;
                Profile profile;
                profile = DataWrapper.getNonInitializedProfile(
                        activity.getResources().getString(R.string.profile_name_default),
                        Profile.PROFILE_ICON_DEFAULT, 0);
                profile.generateIconBitmap(activity.getApplicationContext(), false, 0xFF, false);
                if (applicationEditorPrefIndicator)
                    profile.generatePreferencesIndicator(activity.getApplicationContext(), false, 0xFF, DataWrapper.IT_FOR_EDITOR);
                profileList.add(profile);
                for (int index = 0; index < 7; index++) {
                    profile = dataWrapper.getPredefinedProfile(index, false, activity);
                    profile.generateIconBitmap(activity.getApplicationContext(), false, 0xFF, false);
                    if (applicationEditorPrefIndicator)
                        profile.generatePreferencesIndicator(activity.getApplicationContext(), false, 0xFF, DataWrapper.IT_FOR_EDITOR);
                    profileList.add(profile);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            AddProfileDialog dialog = dialogWeakRef.get();
            Activity activity = activityWeakRef.get();
            if ((dialog != null) && (activity != null)) {
                //listView.setVisibility(View.VISIBLE);
                dialog.linlaProgress.setVisibility(View.GONE);

                AddProfileAdapter addProfileAdapter = new AddProfileAdapter(dialog, activity, profileList);
                dialog.listView.setAdapter(addProfileAdapter);
            }
        }

    }

}
