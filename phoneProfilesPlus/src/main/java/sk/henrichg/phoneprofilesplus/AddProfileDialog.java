package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

class AddProfileDialog
{
    private final EditorProfileListFragment profileListFragment;

    final MaterialDialog mDialog;
    private final Context context;

    private final LinearLayout linlaProgress;
    private final ListView listView;

    AddProfileDialog(Context context, EditorProfileListFragment profileListFragment)
    {
        this.profileListFragment = profileListFragment;
        this.context = context;

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                .title(R.string.new_profile_predefined_profiles_dialog)
                        //.disableDefaultFonts()
                .negativeText(android.R.string.cancel)
                .autoDismiss(true)
                .customView(R.layout.activity_profile_pref_dialog, false);

        dialogBuilder.showListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AddProfileDialog.this.onShow(/*dialog*/);
            }
        });

        mDialog = dialogBuilder.build();

        /*
        MDButton negative = mDialog.getActionButton(DialogAction.NEGATIVE);
        if (negative != null) negative.setAllCaps(false);
        MDButton  neutral = mDialog.getActionButton(DialogAction.NEUTRAL);
        if (neutral != null) neutral.setAllCaps(false);
        MDButton  positive = mDialog.getActionButton(DialogAction.POSITIVE);
        if (positive != null) positive.setAllCaps(false);
        */

        View layout = mDialog.getCustomView();

        //noinspection ConstantConditions
        linlaProgress = layout.findViewById(R.id.profile_pref_dlg_linla_progress);

        //noinspection ConstantConditions
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

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                listView.setVisibility(View.GONE);
                linlaProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {

                Profile profile;
                profile = DataWrapper.getNonInitializedProfile(
                        context.getResources().getString(R.string.profile_name_default),
                        Profile.PROFILE_ICON_DEFAULT, 0);
                profile.generateIconBitmap(context, false, 0xFF);
                if (ApplicationPreferences.applicationEditorPrefIndicator(context))
                    profile.generatePreferencesIndicator(context, false, 0xFF);
                profileList.add(profile);
                for (int index = 0; index < 6; index++) {
                    profile = profileListFragment.activityDataWrapper.getPredefinedProfile(index, false);
                    profile.generateIconBitmap(context, false, 0xFF);
                    if (ApplicationPreferences.applicationEditorPrefIndicator(context))
                        profile.generatePreferencesIndicator(context, false, 0xFF);
                    profileList.add(profile);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                listView.setVisibility(View.VISIBLE);
                linlaProgress.setVisibility(View.GONE);

                AddProfileAdapter addProfileAdapter = new AddProfileAdapter(context, profileList);
                listView.setAdapter(addProfileAdapter);
            }

        }.execute();
    }

    private void doOnItemSelected(int position)
    {
        profileListFragment.startProfilePreferencesActivity(null, position);
        mDialog.dismiss();
    }

    public void show() {
        mDialog.show();
    }

}
