package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

public class AddProfileDialog
{
    private AddProfileAdapter addProfileAdapter;

    public List<Profile> profileList;

    private Context _context;
    private EditorProfileListFragment profileListFragment;

    private MaterialDialog mDialog;
    private ListView listView;

    public AddProfileDialog(Context context, EditorProfileListFragment profileListFragment)
    {
        _context = context;
        this.profileListFragment = profileListFragment;

        profileList = new ArrayList<Profile>();

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
            profile = profileListFragment.dataWrapper.getDefaultProfile(index, false);
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

        listView = (ListView)mDialog.getCustomView().findViewById(R.id.profile_pref_dlg_listview);

        addProfileAdapter = new AddProfileAdapter(this, _context, profileList);
        listView.setAdapter(addProfileAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                doOnItemSelected(position);
            }

        });

    }

    public void doOnItemSelected(int position)
    {
        profileListFragment.startProfilePreferencesActivity(null, position);
        mDialog.dismiss();
    }

    public void show() {
        mDialog.show();
    }

}
