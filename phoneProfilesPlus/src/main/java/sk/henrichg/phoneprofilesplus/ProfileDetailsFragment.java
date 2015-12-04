package sk.henrichg.phoneprofilesplus;

import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfileDetailsFragment extends Fragment {

    public long profile_id;
    public int editMode;


    public ProfileDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        setRetainInstance(true);

        profile_id = getArguments().getLong(GlobalData.EXTRA_PROFILE_ID, 0);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView profileIcon;
        TextView profileName;
        ImageView profileIndicator;
        ImageView profileItemEditMenu;
        ImageView profileShowInActivator;

        profileName = (TextView)view.findViewById(R.id.profile_detail_profile_name);
        profileIcon = (ImageView)view.findViewById(R.id.profile_detail_profile_icon);
        profileItemEditMenu = (ImageView)view.findViewById(R.id.profile_detail_edit_menu);
        if (GlobalData.applicationEditorPrefIndicator)
            profileIndicator = (ImageView)view.findViewById(R.id.profile_detail_profile_pref_indicator);
        else
            profileIndicator = null;
        profileShowInActivator = (ImageView)view.findViewById(R.id.profile_detail_show_in_activator);

        DataWrapper dataWrapper = new DataWrapper(getActivity().getApplicationContext(), true, false, 0);

        final Profile profile = dataWrapper.getProfileById(profile_id, false);

        profileName.setTypeface(null, Typeface.BOLD);

        if (profile != null) {

            profileName.setText(profile.getProfileNameWithDuration());

            if (profile.getIsIconResourceID()) {
                if (profile._iconBitmap != null)
                    profileIcon.setImageBitmap(profile._iconBitmap);
                else {
                    //holder.profileIcon.setImageBitmap(null);
                    int res = getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                            getActivity().getPackageName());
                    profileIcon.setImageResource(res); // resource na ikonu
                }
            } else {
                profileIcon.setImageBitmap(profile._iconBitmap);
            }

            if (profile._showInActivator)
                profileShowInActivator.setImageResource(R.drawable.ic_profile_show_in_activator_on);
            else
                profileShowInActivator.setImageResource(R.drawable.ic_profile_show_in_activator_off);

            if (GlobalData.applicationEditorPrefIndicator) {
                //profilePrefIndicatorImageView.setImageBitmap(null);
                //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                if (profileIndicator != null)
                    profileIndicator.setImageBitmap(profile._preferencesIndicator);
            }

            /*
            profileItemEditMenu.setTag(profile);
            final ImageView _profileItemEditMenu = profileItemEditMenu;
            holder.profileItemEditMenu.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    ((EditorProfileListFragment) fragment).showEditMenu(profileItemEditMenu);
                }
            });
            */
        }

    }

}
