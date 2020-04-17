package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.Collections;
import java.util.Comparator;

import androidx.appcompat.app.AlertDialog;

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
        View layout = inflater.inflate(R.layout.activity_profile_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                if (positive != null) positive.setAllCaps(false);
                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                if (negative != null) negative.setAllCaps(false);

                FastAccessDurationProfileDialog.this.onShow();
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

            /*@Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                //listView.setVisibility(View.GONE);
                //linlaProgress.setVisibility(View.VISIBLE);
            }*/

            @Override
            protected Void doInBackground(Void... params) {
                dataWrapper.fillProfileList(true, ApplicationPreferences.applicationEditorPrefIndicator);
                synchronized (dataWrapper.profileList) {
                    Collections.sort(dataWrapper.profileList, new AlphabeticallyComparator());
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                //listView.setVisibility(View.VISIBLE);
                linlaProgress.setVisibility(View.GONE);

                FastAccessDurationProfileAdapter adapter = new FastAccessDurationProfileAdapter(FastAccessDurationProfileDialog.this, activity, fastAccessDurationDialog.mAfterDoProfile, dataWrapper.profileList);
                listView.setAdapter(adapter);

                int position;
                long iProfileId;
                iProfileId = fastAccessDurationDialog.mAfterDoProfile;
                if (iProfileId == Profile.PROFILE_NO_ACTIVATE)
                    position = 0;
                else
                {
                    boolean found = false;
                    position = 0;
                    for (Profile profile : dataWrapper.profileList)
                    {
                        if (profile._id == iProfileId)
                        {
                            found = true;
                            break;
                        }
                        position++;
                    }
                    if (found)
                    {
                        position++;
                    }
                    else
                        position = 0;
                }
                listView.setSelection(position);
            }

        }.execute();
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

}
