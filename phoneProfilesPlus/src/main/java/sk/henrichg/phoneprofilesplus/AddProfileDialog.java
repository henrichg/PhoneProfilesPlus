package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;

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
        View layout = inflater.inflate(R.layout.activity_profile_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                AddProfileDialog.this.onShow();
            }
        });

        linlaProgress = layout.findViewById(R.id.profile_pref_dlg_linla_progress);

        listView = layout.findViewById(R.id.profile_pref_dlg_listview);

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                doOnItemSelected(position);
            }

        });

    }

    @SuppressLint("StaticFieldLeak")
    private void onShow(/*DialogInterface dialog*/) {
        new AsyncTask<Void, Integer, Void>() {

            final List<Profile> profileList = new ArrayList<>();

            /*@Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                //listView.setVisibility(View.GONE);
                //linlaProgress.setVisibility(View.VISIBLE);
            }*/

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

        }.execute();
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

}
