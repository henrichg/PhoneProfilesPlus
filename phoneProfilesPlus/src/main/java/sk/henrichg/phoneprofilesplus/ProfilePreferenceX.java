package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

public class ProfilePreferenceX extends DialogPreference {

    ProfilePreferenceFragmentX fragment;

    String profileId;

    final int addNoActivateItem;
    final int noActivateAsDoNotApply;
    final int showDuration;

    private final Context prefContext;

    final DataWrapper dataWrapper;

    public ProfilePreferenceX(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        //PPApplication.logE("ProfilePreferenceX.ProfilePreferenceX", "xxx");

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PPProfilePreference);

        addNoActivateItem = typedArray.getInt(R.styleable.PPProfilePreference_addNoActivateItem, 0);
        noActivateAsDoNotApply = typedArray.getInt(R.styleable.PPProfilePreference_noActivateAsDoNotApply, 0);
        showDuration = typedArray.getInt(R.styleable.PPProfilePreference_showDuration, 0);

        profileId = "0";
        prefContext = context;
        //preferenceTitle = getTitle();

        dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);

        setWidgetLayoutResource(R.layout.widget_profile_preference); // resource na layout custom preference - TextView-ImageView

        typedArray.recycle();

        setPositiveButtonText(null);

    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        //preferenceTitleView = view.findViewById(R.id.applications_pref_label);  // resource na title
        //preferenceTitleView.setText(preferenceTitle);

        ImageView profileIcon = (ImageView) holder.findViewById(R.id.profile_pref_icon);

        if (profileIcon != null)
        {
            Profile profile = dataWrapper.getProfileById(Long.parseLong(profileId), true, false, false);
            if (profile != null)
            {
                if (profile.getIsIconResourceID())
                {
                    if (profile._iconBitmap != null)
                        profileIcon.setImageBitmap(profile._iconBitmap);
                    else {
                        //profileIcon.setImageBitmap(null);
                        //int res = prefContext.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                        //        prefContext.PPApplication.PACKAGE_NAME);
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
            {
                //if ((addNoActivateItem == 1) && (Long.parseLong(profileId) == PPApplication.PROFILE_NO_ACTIVATE))
                //    profileIcon.setImageResource(R.drawable.ic_profile_default); // icon resource
                //else
                    profileIcon.setImageResource(R.drawable.ic_empty); // icon resource
            }

            Handler handler = new Handler(prefContext.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=ProfilePreferenceX.onBindViewHolder");
                    setSummary(Long.parseLong(profileId));
                }
            }, 200);
        }
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
        String value;
        try {
            value = getPersistedString((String) defaultValue);
        } catch  (Exception e) {
            value = (String) defaultValue;
        }
        profileId = value;
    }

    /*
    @Override
    protected void onPrepareForRemoval()
    {
        super.onPrepareForRemoval();
        //dataWrapper.invalidateDataWrapper();
        //dataWrapper = null;
    }
    */

    /*
    public String getProfileId()
    {
        return profileId;
    }
    */

    void setProfileId(long newProfileId)
    {
        String newValue = String.valueOf(newProfileId);

        if (!callChangeListener(newValue)) {
            // no save new value
            return;
        }

        profileId = newValue;

        // set summary
        setSummary(Long.parseLong(profileId));

        // save to preferences
        persistString(newValue);

        // and notify
        notifyChanged();

    }

    public void setSummary(long profileId)
    {
        Profile profile = dataWrapper.getProfileById(profileId, false, false, false);
        if (profile != null)
        {
            if (showDuration == 1)
                setSummary(profile.getProfileNameWithDuration("", "", false, false, prefContext.getApplicationContext()));
            else
                setSummary(profile._name);
        }
        else
        {
            if ((addNoActivateItem == 1) && (profileId == Profile.PROFILE_NO_ACTIVATE))
                if (noActivateAsDoNotApply == 1)
                    setSummary(prefContext.getResources().getString(R.string.profile_preference_do_not_apply));
                else
                    setSummary(prefContext.getResources().getString(R.string.profile_preference_profile_end_no_activate));
            else
                setSummary(prefContext.getResources().getString(R.string.profile_preference_profile_not_set));
        }
    }


    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final SavedState myState = new SavedState(superState);
        myState.profileId = profileId;
        /*myState.addNoActivateItem = addNoActivateItem;
        myState.noActivateAsDoNotApply = noActivateAsDoNotApply;
        myState.showDuration = showDuration;*/
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummary(Long.parseLong(profileId));
            return;
        }

        // restore instance state
        SavedState myState = (SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        profileId = myState.profileId;
        /*addNoActivateItem = myState.addNoActivateItem;
        noActivateAsDoNotApply = myState.noActivateAsDoNotApply;
        showDuration = myState.showDuration;*/

        setSummary(Long.parseLong(profileId));
        //notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String profileId;
        /*int addNoActivateItem;
        int noActivateAsDoNotApply;
        int showDuration;*/

        SavedState(Parcel source)
        {
            super(source);

            // restore profileId
            profileId = source.readString();
            /*addNoActivateItem = source.readInt();
            noActivateAsDoNotApply = source.readInt();
            showDuration = source.readInt();*/
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save profileId
            dest.writeString(profileId);
            /*dest.writeInt(addNoActivateItem);
            dest.writeInt(noActivateAsDoNotApply);
            dest.writeInt(showDuration);*/
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<SavedState> CREATOR =
                new Creator<ProfilePreferenceX.SavedState>() {
            public ProfilePreferenceX.SavedState createFromParcel(Parcel in)
            {
                return new ProfilePreferenceX.SavedState(in);
            }
            public ProfilePreferenceX.SavedState[] newArray(int size)
            {
                return new ProfilePreferenceX.SavedState[size];
            }

        };

    }

}
