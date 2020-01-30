package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

public class ProfileMultiSelectPreferenceX extends DialogPreference {

    ProfileMultiSelectPreferenceFragmentX fragment;

    String value;
    private String defaultValue;
    private boolean savedInstanceState;

    private final Context prefContext;

    private ImageView profileIcon;
    private RelativeLayout profileIcons;
    private ImageView profileIcon1;
    private ImageView profileIcon2;
    private ImageView profileIcon3;
    private ImageView profileIcon4;

    final DataWrapper dataWrapper;

    public ProfileMultiSelectPreferenceX(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        value = "";
        prefContext = context;

        dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);

        setWidgetLayoutResource(R.layout.profile_multiselect_preference);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        profileIcon = (ImageView)holder.findViewById(R.id.profile_multiselect_pref_icon);
        profileIcons = (RelativeLayout)holder.findViewById(R.id.profile_multiselect_pref_icons);
        profileIcon1 = (ImageView)holder.findViewById(R.id.profile_multiselect_pref_icon1);
        profileIcon2 = (ImageView)holder.findViewById(R.id.profile_multiselect_pref_icon2);
        profileIcon3 = (ImageView)holder.findViewById(R.id.profile_multiselect_pref_icon3);
        profileIcon4 = (ImageView)holder.findViewById(R.id.profile_multiselect_pref_icon4);

        setIcons();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        super.onGetDefaultValue(a, index);

        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        value = getPersistedString((String)defaultValue);
        this.defaultValue = (String)defaultValue;
        setSummaryPMSDP();
    }

    private void setSummaryPMSDP()
    {
        String prefSummary = prefContext.getString(R.string.profile_multiselect_summary_text_not_selected);
        if (!value.isEmpty() && !value.equals("-")) {
            String[] splits = value.split("\\|");
            prefSummary = prefContext.getString(R.string.profile_multiselect_summary_text_selected) + ": " + splits.length;
            if (splits.length == 1) {
                Profile profile = dataWrapper.getProfileById(Long.parseLong(value), false, false, false);
                if (profile != null)
                {
                    prefSummary = profile._name;
                }
            }
        }
        setSummary(prefSummary);
    }

    private void setIcons() {
        if (!value.isEmpty() && !value.equals("-")) {
            String[] splits = value.split("\\|");
            if (splits.length == 1) {
                profileIcon.setVisibility(View.VISIBLE);
                profileIcon1.setImageResource(R.drawable.ic_empty);
                profileIcon2.setImageResource(R.drawable.ic_empty);
                profileIcon3.setImageResource(R.drawable.ic_empty);
                profileIcon4.setImageResource(R.drawable.ic_empty);
                profileIcons.setVisibility(View.GONE);

                Profile profile = dataWrapper.getProfileById(Long.parseLong(value), true, false, false);
                if (profile != null)
                {
                    if (profile.getIsIconResourceID())
                    {
                        if (profile._iconBitmap != null)
                            profileIcon.setImageBitmap(profile._iconBitmap);
                        else {
                            //profileIcon.setImageBitmap(null);
                            //int res = prefContext.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                            //        prefContext.getPackageName());
                            int res = Profile.getIconResource(profile.getIconIdentifier());
                            profileIcon.setImageResource(res); // icon resource
                        }
                    }
                    else
                    {
                        profileIcon.setImageBitmap(profile._iconBitmap);
                    }
                }
                else
                    profileIcon.setImageResource(R.drawable.ic_empty); // icon resource
            } else {
                profileIcons.setVisibility(View.VISIBLE);
                profileIcon.setVisibility(View.GONE);
                profileIcon.setImageResource(R.drawable.ic_empty);

                ImageView profIcon = profileIcon1;
                for (int i = 0; i < 4; i++) {
                    //if (i == 0) profIcon = profileIcon1;
                    if (i == 1) profIcon = profileIcon2;
                    if (i == 2) profIcon = profileIcon3;
                    if (i == 3) profIcon = profileIcon4;
                    if (i < splits.length) {
                        Profile profile = dataWrapper.getProfileById(Long.parseLong(splits[i]), true, false, false);
                        if (profile != null)
                        {
                            if (profile.getIsIconResourceID())
                            {
                                if (profile._iconBitmap != null)
                                    profIcon.setImageBitmap(profile._iconBitmap);
                                else {
                                    //profileIcon.setImageBitmap(null);
                                    //int res = prefContext.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                                    //        prefContext.getPackageName());
                                    int res = Profile.getIconResource(profile.getIconIdentifier());
                                    profIcon.setImageResource(res); // icon resource
                                }
                            }
                            else
                            {
                                profIcon.setImageBitmap(profile._iconBitmap);
                            }
                        }
                        else
                            profIcon.setImageResource(R.drawable.ic_empty); // icon resource

                    } else
                        profIcon.setImageResource(R.drawable.ic_empty);
                }
            }
        }
        else {
            profileIcon.setVisibility(View.VISIBLE);
            profileIcons.setVisibility(View.GONE);
            profileIcon.setImageResource(R.drawable.ic_empty);
        }
    }

    @SuppressWarnings("StringConcatenationInLoop")
    private void setValue() {
        // fill with profile id strings separated with |
        value = "";
        if (dataWrapper.profileListFilled)
        {
            /*HG*/
            synchronized (dataWrapper.profileList) {
                for (Profile profile : dataWrapper.profileList) {
                    if (profile._checked) {
                        if (!value.isEmpty())
                            value = value + "|";
                        value = value + profile._id;
                    }
                }
            }
            //PPApplication.logE("ProfileMultiSelectPreferenceX.onPositive","value="+value);
        }
    }

    void persistValue() {
        if (shouldPersist())
        {
            setValue();
            persistString(value);

            setIcons();
            setSummaryPMSDP();
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            value = getPersistedString(defaultValue);
            setSummaryPMSDP();
        }
        savedInstanceState = false;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        setValue();
        final ProfileMultiSelectPreferenceX.SavedState myState = new ProfileMultiSelectPreferenceX.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if (!state.getClass().equals(ProfileMultiSelectPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryPMSDP();
            return;
        }

        // restore instance state
        ProfileMultiSelectPreferenceX.SavedState myState = (ProfileMultiSelectPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;
        setSummaryPMSDP();
        //notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;
        String defaultValue;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readString();
            defaultValue = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(value);
            dest.writeString(defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<ProfileMultiSelectPreferenceX.SavedState> CREATOR =
                new Creator<ProfileMultiSelectPreferenceX.SavedState>() {
                    public ProfileMultiSelectPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new ProfileMultiSelectPreferenceX.SavedState(in);
                    }
                    public ProfileMultiSelectPreferenceX.SavedState[] newArray(int size)
                    {
                        return new ProfileMultiSelectPreferenceX.SavedState[size];
                    }

                };

    }

}
