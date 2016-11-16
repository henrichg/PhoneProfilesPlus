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
    public List<Profile> profileList;

    private EditorProfileListFragment profileListFragment;

    private MaterialDialog mDialog;

    AddProfileDialog(Context context, EditorProfileListFragment profileListFragment)
    {
        this.profileListFragment = profileListFragment;

        profileList = new ArrayList<>();

        boolean monochrome = false;
        int monochromeValue = 0xFF;

        Profile profile;
        profile = DataWrapper.getNoinitializedProfile(
                                        context.getResources().getString(R.string.profile_name_default),
                                        GlobalData.PROFILE_ICON_DEFAULT, 0);
        profile.generateIconBitmap(context, monochrome, monochromeValue);
        profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
        profileList.add(profile);
        for (int index = 0; index < 6; index++) {
            profile = profileListFragment.dataWrapper.getPredefinedProfile(index, false);
            profile.generateIconBitmap(context, monochrome, monochromeValue);
            profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
            profileList.add(profile);
        }

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                .title(R.string.new_profile_predefined_profiles_dialog)
                        //.disableDefaultFonts()
                .autoDismiss(false)
                .customView(R.layout.activity_profile_pref_dialog, false);

        mDialog = dialogBuilder.build();

        ListView listView = (ListView)mDialog.getCustomView().findViewById(R.id.profile_pref_dlg_listview);

        AddProfileAdapter addProfileAdapter = new AddProfileAdapter(this, context, profileList);
        listView.setAdapter(addProfileAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                doOnItemSelected(position);
            }

        });

    }

    void doOnItemSelected(int position)
    {
        profileListFragment.startProfilePreferencesActivity(null, position);
        mDialog.dismiss();
    }

    public void show() {
        mDialog.show();
    }

}
