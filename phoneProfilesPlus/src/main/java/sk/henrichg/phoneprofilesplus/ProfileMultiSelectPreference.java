package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.Collections;
import java.util.Comparator;

import androidx.appcompat.app.AlertDialog;

public class ProfileMultiSelectPreference extends DialogPreference {

    private String value;

    private final Context prefContext;
    private AlertDialog mDialog;

    private LinearLayout linlaProgress;
    private ListView listView;
    private ProfileMultiSelectPreferenceAdapter profilePreferenceAdapter;

    private ImageView profileIcon;
    private RelativeLayout profileIcons;
    private ImageView profileIcon1;
    private ImageView profileIcon2;
    private ImageView profileIcon3;
    private ImageView profileIcon4;

    private DataWrapper dataWrapper;

    public ProfileMultiSelectPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        value = "";
        prefContext = context;

        dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);

        setWidgetLayoutResource(R.layout.profile_multiselect_preference);
    }

    protected void showDialog(Bundle state) {
        PPApplication.logE("ProfilePreference.showDialog", "xx");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(getDialogTitle());
        dialogBuilder.setIcon(getDialogIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(getNegativeButtonText(), null);
        dialogBuilder.setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
            @SuppressWarnings("StringConcatenationInLoop")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (shouldPersist())
                {
                    // fill with profile id strings separated with |
                    value = "";
                    if (dataWrapper.profileListFilled)
                    {
                        for (Profile profile : dataWrapper.profileList)
                        {
                            if (profile._checked) {
                                if (!value.isEmpty())
                                    value = value + "|";
                                value = value + profile._id;
                            }
                        }
                        PPApplication.logE("ProfileMultiSelectPreference.onPositive","value="+value);
                    }

                    persistString(value);

                    setIcons();
                    setSummaryPMSDP();
                }
            }
        });

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_profile_multiselect_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ProfileMultiSelectPreference.this.onShow(/*dialog*/);
            }
        });

        linlaProgress = layout.findViewById(R.id.profile_multiselect_pref_dlg_linla_progress);

        listView = layout.findViewById(R.id.profile_multiselect_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                Profile profile = (Profile)profilePreferenceAdapter.getItem(position);
                profile._checked = !profile._checked;
                ProfilesViewHolder viewHolder = (ProfilesViewHolder) item.getTag();
                viewHolder.checkBox.setChecked(profile._checked);
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

        profileIcon = view.findViewById(R.id.profile_multiselect_pref_icon);
        profileIcons = view.findViewById(R.id.profile_multiselect_pref_icons);
        profileIcon1 = view.findViewById(R.id.profile_multiselect_pref_icon1);
        profileIcon2 = view.findViewById(R.id.profile_multiselect_pref_icon2);
        profileIcon3 = view.findViewById(R.id.profile_multiselect_pref_icon3);
        profileIcon4 = view.findViewById(R.id.profile_multiselect_pref_icon4);

        setIcons();
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

                getValuePMSDP();

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                listView.setVisibility(View.VISIBLE);
                linlaProgress.setVisibility(View.GONE);

                profilePreferenceAdapter = new ProfileMultiSelectPreferenceAdapter(prefContext, dataWrapper.profileList);
                listView.setAdapter(profilePreferenceAdapter);
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
            getValuePMSDP();
        }
        else {
            // set state
            value = "";
            persistString("");
        }
        setSummaryPMSDP();
    }

    @Override
    protected void onPrepareForRemoval()
    {
        super.onPrepareForRemoval();
        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

    private void getValuePMSDP()
    {
        // Get the persistent value
        value = getPersistedString(value);

        PPApplication.logE("ProfileMultiSelectPreference.getValueAMSDP","value="+value);

        for (Profile profile : dataWrapper.profileList)
            profile._checked = false;

        if (!value.isEmpty()) {
            String[] splits = value.split("\\|");
            for (String split : splits) {
                Profile profile = dataWrapper.getProfileById(Long.parseLong(split), false, false, false);
                if (profile != null)
                    profile._checked = true;
            }
        }
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

    private class AlphabeticallyComparator implements Comparator<Profile> {

        public int compare(Profile lhs, Profile rhs) {
            if (GlobalGUIRoutines.collator != null)
                return GlobalGUIRoutines.collator.compare(lhs._name, rhs._name);
            else
                return 0;
        }
    }
}
