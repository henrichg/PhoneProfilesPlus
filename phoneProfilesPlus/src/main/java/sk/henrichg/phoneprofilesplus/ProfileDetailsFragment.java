package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfileDetailsFragment extends Fragment {

    public long profile_id;
    public int editMode;
    public int predefinedProfileIndex;


    public ProfileDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * The fragment's current callback objects
     */
    private OnStartProfilePreferencesFromDetail onStartProfilePreferencesCallback = sDummyOnStartProfilePreferencesCallback;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified.
     */
    // invoked when start profile preference fragment/activity needed
    interface OnStartProfilePreferencesFromDetail {
        void onStartProfilePreferencesFromDetail(Profile profile);
    }

    /**
     * A dummy implementation of the Callbacks interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static OnStartProfilePreferencesFromDetail sDummyOnStartProfilePreferencesCallback = new OnStartProfilePreferencesFromDetail() {
        public void onStartProfilePreferencesFromDetail(Profile profile) {
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof OnStartProfilePreferencesFromDetail)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }
        onStartProfilePreferencesCallback = (OnStartProfilePreferencesFromDetail) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        onStartProfilePreferencesCallback = sDummyOnStartProfilePreferencesCallback;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        setRetainInstance(true);

        profile_id = getArguments().getLong(PPApplication.EXTRA_PROFILE_ID, 0);

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
        ImageView profileItemEdit;

        profileName = view.findViewById(R.id.profile_detail_profile_name);
        profileIcon = view.findViewById(R.id.profile_detail_profile_icon);
        profileItemEdit = view.findViewById(R.id.profile_detail_edit);
        //if (PPApplication.applicationEditorPrefIndicator)
            profileIndicator = view.findViewById(R.id.profile_detail_profile_pref_indicator);
        //else
        //    profileIndicator = null;

        DataWrapper dataWrapper = new DataWrapper(getActivity().getApplicationContext(), true, false, 0);

        final Profile profile = dataWrapper.getProfileById(profile_id, false);

        profileName.setTypeface(null, Typeface.BOLD);

        if (profile != null) {

            String sProfileName = profile.getProfileNameWithDuration(false, dataWrapper.context);
            if (profile._showInActivator)
                sProfileName = "[A] " + sProfileName;

            profileName.setText(sProfileName);

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

            //if (PPApplication.applicationEditorPrefIndicator) {
                //profilePrefIndicatorImageView.setImageBitmap(null);
                //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                if (profileIndicator != null)
                    profileIndicator.setImageBitmap(profile._preferencesIndicator);
                else
                    profileIndicator.setImageResource(R.drawable.ic_empty);
            //}

            profileItemEdit.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    startProfilePreferencesActivity(profile);
                }
            });
        }

    }

    private void startProfilePreferencesActivity(Profile profile)
    {
        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) one must start profile preferences
        onStartProfilePreferencesCallback.onStartProfilePreferencesFromDetail(profile);
    }

}
