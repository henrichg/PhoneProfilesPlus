package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

class AddProfileDialog
{
    private final EditorProfileListFragment profileListFragment;

    final MaterialDialog mDialog;

    AddProfileDialog(Context context, EditorProfileListFragment profileListFragment)
    {
        this.profileListFragment = profileListFragment;

        List<Profile> profileList = new ArrayList<>();

        Profile profile;
        profile = DataWrapper.getNonInitializedProfile(
                                        context.getResources().getString(R.string.profile_name_default),
                                        Profile.PROFILE_ICON_DEFAULT, 0);
        profile.generateIconBitmap(context, false, 0xFF);
        profile.generatePreferencesIndicator(context, false, 0xFF);
        profileList.add(profile);
        for (int index = 0; index < 6; index++) {
            profile = profileListFragment.dataWrapper.getPredefinedProfile(index, false);
            profile.generateIconBitmap(context, false, 0xFF);
            profile.generatePreferencesIndicator(context, false, 0xFF);
            profileList.add(profile);
        }

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                .title(R.string.new_profile_predefined_profiles_dialog)
                        //.disableDefaultFonts()
                .negativeText(android.R.string.cancel)
                .autoDismiss(true)
                .customView(R.layout.activity_profile_pref_dialog, false);

        mDialog = dialogBuilder.build();

        ListView listView = mDialog.getCustomView().findViewById(R.id.profile_pref_dlg_listview);

        AddProfileAdapter addProfileAdapter = new AddProfileAdapter(context, profileList);
        listView.setAdapter(addProfileAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                doOnItemSelected(position);
            }

        });

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
