package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class ProfilePreferenceDialog implements PreferenceManager.OnActivityDestroyListener
{

    private ProfilePreference profilePreference;
    int addNoActivateItem;
    int noActivateAsDoNotApply;
    int showDuration;
    private ProfilePreferenceAdapter profilePreferenceAdapter;

    private List<Profile> profileList;

    private MaterialDialog mDialog;

    ProfilePreferenceDialog(Context context, ProfilePreference preference, String profileId)
    {
        profilePreference = preference;

        profileList = ProfilePreference.dataWrapper.getProfileList();
        Collections.sort(profileList, new AlphabeticallyComparator());

        addNoActivateItem = profilePreference.addNoActivateItem;
        noActivateAsDoNotApply = profilePreference.noActivateAsDoNotApply;
        showDuration = profilePreference.showDuration;


        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                .title(R.string.title_activity_profile_preference_dialog)
                //.disableDefaultFonts()
                .autoDismiss(false)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        MaterialDialogsPrefUtil.unregisterOnActivityDestroyListener(profilePreference, ProfilePreferenceDialog.this);
                    }
                })
                .customView(R.layout.activity_profile_pref_dialog, false);

        MaterialDialogsPrefUtil.registerOnActivityDestroyListener(profilePreference, this);

        mDialog = dialogBuilder.build();

        ListView listView = (ListView)mDialog.getCustomView().findViewById(R.id.profile_pref_dlg_listview);

        profilePreferenceAdapter = new ProfilePreferenceAdapter(this, context, profileId, profileList);
        listView.setAdapter(profilePreferenceAdapter);

        int position;
        long iProfileId;
        if (profileId.isEmpty())
            iProfileId = 0;
        else
            iProfileId = Long.valueOf(profileId);
        if ((addNoActivateItem == 1) && (iProfileId == Profile.PROFILE_NO_ACTIVATE))
            position = 0;
        else
        {
            boolean found = false;
            position = 0;
            for (Profile profile : profileList)
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
                if (addNoActivateItem == 1)
                    position++;
            }
            else
                position = 0;
        }
        listView.setSelection(position);
        listView.setItemChecked(position, true);
        listView.smoothScrollToPosition(position);

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                doOnItemSelected(position);
            }

        });

    }

    void doOnItemSelected(int position)
    {
        if (addNoActivateItem == 1)
        {
            long profileId;
            if (position == 0)
                profileId = Profile.PROFILE_NO_ACTIVATE;
            else
                profileId = profilePreferenceAdapter.profileList.get(position-1)._id;
            profilePreference.setProfileId(profileId);
        }
        else
            profilePreference.setProfileId(profilePreferenceAdapter.profileList.get(position)._id);
        mDialog.dismiss();
    }

    @Override
    public void onActivityDestroy() {
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    private class AlphabeticallyComparator implements Comparator<Profile> {

        public int compare(Profile lhs, Profile rhs) {
            return GlobalGUIRoutines.collator.compare(lhs._name, rhs._name);
        }
    }

    public void show() {
        mDialog.show();
    }

}
