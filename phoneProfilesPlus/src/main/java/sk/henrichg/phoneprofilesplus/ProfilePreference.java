package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.Collections;
import java.util.Comparator;

public class ProfilePreference extends DialogPreference {

    private String profileId;
    int addNoActivateItem;
    int noActivateAsDoNotApply;
    int showDuration;

    private final Context prefContext;
    private AlertDialog mDialog;

    private LinearLayout linlaProgress;
    private ListView listView;
    private ProfilePreferenceAdapter profilePreferenceAdapter;

    private DataWrapper dataWrapper;

    public ProfilePreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProfilePreference);

        addNoActivateItem = typedArray.getInt(R.styleable.ProfilePreference_addNoActivateItem, 0);
        noActivateAsDoNotApply = typedArray.getInt(R.styleable.ProfilePreference_noActivateAsDoNotApply, 0);
        showDuration = typedArray.getInt(R.styleable.ProfilePreference_showDuration, 0);

        profileId = "0";
        prefContext = context;
        //preferenceTitle = getTitle();

        dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);

        setWidgetLayoutResource(R.layout.profile_preference); // resource na layout custom preference - TextView-ImageView

        typedArray.recycle();

    }

    protected void showDialog(Bundle state) {
        PPApplication.logE("ProfilePreference.showDialog", "xx");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(getDialogTitle());
        dialogBuilder.setIcon(getDialogIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(getNegativeButtonText(), null);

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_profile_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ProfilePreference.this.onShow(/*dialog*/);
            }
        });

        //noinspection ConstantConditions
        linlaProgress = layout.findViewById(R.id.profile_pref_dlg_linla_progress);

        //noinspection ConstantConditions
        listView = layout.findViewById(R.id.profile_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                doOnItemSelected(position);
            }
        });


        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        if (!((Activity)prefContext).isFinishing())
            mDialog.show();
    }

    //@Override
    protected void onBindView(View view)
    {
        super.onBindView(view);

        //preferenceTitleView = view.findViewById(R.id.applications_pref_label);  // resource na title
        //preferenceTitleView.setText(preferenceTitle);

        ImageView profileIcon = view.findViewById(R.id.profile_pref_icon);

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
            {
                //if ((addNoActivateItem == 1) && (Long.parseLong(profileId) == PPApplication.PROFILE_NO_ACTIVATE))
                //    profileIcon.setImageResource(R.drawable.ic_profile_default); // icon resource
                //else
                    profileIcon.setImageResource(R.drawable.ic_empty); // icon resource
            }
            setSummary(Long.parseLong(profileId));
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void onShow(/*DialogInterface dialog*/) {
        new AsyncTask<Void, Integer, Void>() {

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                listView.setVisibility(View.GONE);
                linlaProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {

                dataWrapper.fillProfileList(true, ApplicationPreferences.applicationEditorPrefIndicator(dataWrapper.context));
                Collections.sort(dataWrapper.profileList, new AlphabeticallyComparator());

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                listView.setVisibility(View.VISIBLE);
                linlaProgress.setVisibility(View.GONE);

                profilePreferenceAdapter = new ProfilePreferenceAdapter(ProfilePreference.this, prefContext, profileId, dataWrapper.profileList);
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
                        if (addNoActivateItem == 1)
                            position++;
                    }
                    else
                        position = 0;
                }
                listView.setSelection(position);
            }

        }.execute();
    }

    public void onDismiss (DialogInterface dialog)
    {
        super.onDismiss(dialog);
        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();
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
            setProfileId(profileId);
        }
        else
            setProfileId(profilePreferenceAdapter.profileList.get(position)._id);
        mDialog.dismiss();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        super.onGetDefaultValue(a, index);

        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            String value;
            try {
                value = getPersistedString(profileId);
            } catch  (Exception e) {
                value = profileId;
            }
            profileId = value;
        }
        else {
            // set state
            String value = (String) defaultValue;
            profileId = value;
            persistString(value);
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
        myState.addNoActivateItem = addNoActivateItem;
        myState.noActivateAsDoNotApply = noActivateAsDoNotApply;
        myState.showDuration = showDuration;
        return myState;

    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (dataWrapper == null)
            dataWrapper = new DataWrapper(prefContext, false, 0, false);

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
        addNoActivateItem = myState.addNoActivateItem;
        noActivateAsDoNotApply = myState.noActivateAsDoNotApply;
        showDuration = myState.showDuration;

        setSummary(Long.parseLong(profileId));
        notifyChanged();
    }

    @Override
    protected void onPrepareForRemoval()
    {
        super.onPrepareForRemoval();
        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

    /*
    public String getProfileId()
    {
        return profileId;
    }
    */

    private void setProfileId(long newProfileId)
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
                setSummary(profile.getProfileNameWithDuration("", "", false, prefContext));
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

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String profileId;
        int addNoActivateItem;
        int noActivateAsDoNotApply;
        int showDuration;

        SavedState(Parcel source)
        {
            super(source);

            // restore profileId
            profileId = source.readString();
            addNoActivateItem = source.readInt();
            noActivateAsDoNotApply = source.readInt();
            showDuration = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save profileId
            dest.writeString(profileId);
            dest.writeInt(addNoActivateItem);
            dest.writeInt(noActivateAsDoNotApply);
            dest.writeInt(showDuration);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in)
            {
                return new SavedState(in);
            }
            public SavedState[] newArray(int size)
            {
                return new SavedState[size];
            }

        };

    }

    private class AlphabeticallyComparator implements Comparator<Profile> {

        public int compare(Profile lhs, Profile rhs) {
            if (GlobalGUIRoutines.collator != null)
                return GlobalGUIRoutines.collator.compare(lhs._name, rhs._name);
            else
                return 0;
        }
    }
}
