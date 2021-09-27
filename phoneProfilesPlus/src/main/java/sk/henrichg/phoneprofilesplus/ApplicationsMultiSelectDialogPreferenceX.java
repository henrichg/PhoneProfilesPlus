package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ApplicationsMultiSelectDialogPreferenceX extends DialogPreference
{
    ApplicationsMultiSelectDialogPreferenceFragmentX fragment;

    private final Context _context;

    String value = "";
    private String defaultValue;
    private boolean savedInstanceState;

    private final int removePPApplications;
    private final String systemSettings;

    private ImageView packageIcon;
    private RelativeLayout packageIcons;
    private ImageView packageIcon1;
    private ImageView packageIcon2;
    private ImageView packageIcon3;
    private ImageView packageIcon4;

    final List<Application> applicationList;

    public ApplicationsMultiSelectDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        applicationList = new ArrayList<>();

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.PPApplicationsMultiSelectDialogPreference);

        removePPApplications = typedArray.getInteger(
                R.styleable.PPApplicationsMultiSelectDialogPreference_removePPApplications, 0);
        systemSettings = typedArray.getString(
                R.styleable.PPApplicationsMultiSelectDialogPreference_systemSettings);

        typedArray.recycle();

        setWidgetLayoutResource(R.layout.widget_applications_preference); // resource na layout custom preference - TextView-ImageView

        if (EditorProfilesActivity.getApplicationsCache() == null)
            EditorProfilesActivity.createApplicationsCache();

        //applicationsCache = EditorProfilesActivity.getApplicationsCache();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        packageIcon = (ImageView) holder.findViewById(R.id.applications_pref_icon);
        packageIcons = (RelativeLayout) holder.findViewById(R.id.applications_pref_icons);
        packageIcon1 = (ImageView) holder.findViewById(R.id.applications_pref_icon1);
        packageIcon2 = (ImageView) holder.findViewById(R.id.applications_pref_icon2);
        packageIcon3 = (ImageView) holder.findViewById(R.id.applications_pref_icon3);
        packageIcon4 = (ImageView) holder.findViewById(R.id.applications_pref_icon4);

        setIcons();
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        value = getPersistedString((String) defaultValue);
        this.defaultValue = (String)defaultValue;
        getValueAMSDP();
        setSummaryAMSDP();
    }

    void getValueAMSDP()
    {
        //PPApplication.logE("ApplicationsMultiSelectDialogPreference.getValueAMSDP", "value="+value);
        applicationList.clear();

        // change checked state by value
        if (EditorProfilesActivity.getApplicationsCache() != null) {
            List<Application> cachedApplicationList = EditorProfilesActivity.getApplicationsCache().getApplicationList(true);
            if (cachedApplicationList != null) {
                String[] splits = value.split("\\|");
                for (Application application : cachedApplicationList) {
                    application.checked = false;
                    for (String split : splits) {
                        String packageName;
                        //String shortcut;
                        String[] packageNameActivity = split.split("/");
                        if (split.length() > 2) {
                            if (packageNameActivity.length == 2) {
                                //shortcut = packageNameActivity[0].substring(0, 3);
                                packageName = packageNameActivity[0];
                            } else {
                                //shortcut = value.substring(0, 3);
                                packageName = value;
                            }
                            /*if (shortcut.equals("(s)")) {
                                packageName = packageName.substring(3);
                            }*/
                            if (packageName.equals(application.packageName))
                                application.checked = true;
                        }
                    }
                }
                applicationList.addAll(cachedApplicationList);

                // move checked on top
                int i = 0;
                int ich = 0;
                while (i < applicationList.size()) {
                    Application application = applicationList.get(i);
                    if (removePPApplications == 1) {
                        if (
                                application.packageName.equals(PPApplication.PACKAGE_NAME_PP) ||
                                application.packageName.equals(PPApplication.PACKAGE_NAME) ||
                                application.packageName.equals(PPApplication.PACKAGE_NAME_EXTENDER)
                        ) {
                            applicationList.remove(i);
                            continue;
                        }
                    }
                    if (application.checked) {
                        applicationList.remove(i);
                        applicationList.add(ich, application);
                        ich++;
                    }
                    i++;
                }
            }
        }
    }

    static String getSummaryForPreferenceCategory(String value, String systemSettings, Context _context, boolean forPreference) {
        String prefDataSummary = _context.getString(R.string.applications_multiselect_summary_text_not_selected);
        boolean ok = true;
        if (forPreference) {
            if (systemSettings.equals("notifications") && (!PPNotificationListenerService.isNotificationListenerServiceEnabled(_context, true))) {
                // notification scanner
                ok = false;
                prefDataSummary = _context.getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + _context.getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings);
            } else if (systemSettings.equals("accessibility_2.0")) {
                // PPPExtender
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(_context);
                int requiredVersion = PPApplication.VERSION_CODE_EXTENDER_6_1_2;
                if (extenderVersion == 0) {
                    ok = false;
                    prefDataSummary = _context.getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + _context.getString(R.string.preference_not_allowed_reason_not_extender_installed);
                } else if (extenderVersion < requiredVersion) {
                    ok = false;
                    prefDataSummary = _context.getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + _context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                } else if (!PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(_context, true)) {
                    ok = false;
                    prefDataSummary = _context.getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + _context.getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
                } else if (PPApplication.accessibilityServiceForPPPExtenderConnected == 0) {
                    ok = false;
                    prefDataSummary = _context.getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + _context.getString(R.string.preference_not_allowed_reason_state_of_accessibility_setting_for_extender_is_determined);
                }
            } else if (systemSettings.equals("accessibility_5.0")) {
                // PPPExtender
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(_context);
                int requiredVersion = PPApplication.VERSION_CODE_EXTENDER_6_1_2;
                if (extenderVersion == 0) {
                    ok = false;
                    prefDataSummary = _context.getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + _context.getString(R.string.preference_not_allowed_reason_not_extender_installed);
                } else if (extenderVersion < requiredVersion) {
                    ok = false;
                    prefDataSummary = _context.getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + _context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                } else if (!PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(_context, true)) {
                    ok = false;
                    prefDataSummary = _context.getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + _context.getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
                } else if (PPApplication.accessibilityServiceForPPPExtenderConnected == 0) {
                    ok = false;
                    prefDataSummary = _context.getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + _context.getString(R.string.preference_not_allowed_reason_state_of_accessibility_setting_for_extender_is_determined);
                }
            }
        }
        if (ok) {
            if (!value.isEmpty() && !value.equals("-")) {
                String[] splits = value.split("\\|");
                prefDataSummary = _context.getString(R.string.applications_multiselect_summary_text_selected) + ": " + splits.length;
                if (splits.length == 1) {
                    PackageManager packageManager = _context.getPackageManager();
                    /*if (Application.isShortcut(splits[0])) {
                        Intent intent = new Intent();
                        intent.setClassName(Application.getPackageName(splits[0]), Application.getActivityName(splits[0]));
                        ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                        if (info != null)
                            prefDataSummary = info.loadLabel(packageManager).toString();
                    }
                    else
                    if (Application.isIntent(splits[0])) {
                    } else*/ {
                        String activityName = Application.getActivityName(splits[0]);
                        if (activityName.isEmpty()) {
                            ApplicationInfo app;
                            try {
                                app = packageManager.getApplicationInfo(splits[0], 0);
                                if (app != null)
                                    prefDataSummary = packageManager.getApplicationLabel(app).toString();
                            } catch (PackageManager.NameNotFoundException e) {
                                //PPApplication.recordException(e);
                            }
                            catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        } else {
                            Intent intent = new Intent();
                            intent.setClassName(Application.getPackageName(splits[0]), activityName);
                            ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                            if (info != null)
                                prefDataSummary = info.loadLabel(packageManager).toString();
                        }
                    }
                }
            }
        }
        return prefDataSummary;
    }

    private String getSummaryAMSDP()
    {
        String prefDataSummary = getSummaryForPreferenceCategory(value, systemSettings, _context, true);
        setSummary(prefDataSummary);
        return prefDataSummary;
    }

    void setSummaryAMSDP()
    {
        setSummary(getSummaryAMSDP());
    }

    @SuppressWarnings("StringConcatenationInLoop")
    private String getValue() {
        String _value = "";
        if (applicationList != null)
        {
            for (Application application : applicationList)
            {
                if (application.checked)
                {
                    if (!_value.isEmpty())
                        _value = _value + "|";
                    _value = _value + application.packageName + "/" + application.activityName;
                }
            }
        }
        return _value;
    }

    void persistValue() {
        if (shouldPersist())
        {
            // fill with application strings separated with |
            value = getValue();

            persistString(value);

            setIcons();
            setSummaryAMSDP();
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            value = getPersistedString(defaultValue);
            setIcons();
            setSummaryAMSDP();
        }
        savedInstanceState = false;
    }

    private void setIcons() {
        PackageManager packageManager = _context.getPackageManager();
        ApplicationInfo app;

        String[] splits = value.split("\\|");

        if (splits.length == 1) {
            packageIcon.setVisibility(View.VISIBLE);
            packageIcons.setVisibility(View.GONE);

            /*if (Application.isShortcut(splits[0])) {
                Intent intent = new Intent();
                intent.setClassName(Application.getPackageName(splits[0]), Application.getActivityName(splits[0]));
                ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                if (info != null)
                    packageIcon.setImageDrawable(info.loadIcon(packageManager));
                else
                    packageIcon.setImageResource(R.drawable.ic_empty);
            }
            else
            if (Application.isIntent(splits[0])) {
            }
            else*/ {
                String activityName = Application.getActivityName(splits[0]);
                if (activityName.isEmpty()) {
                    try {
                        app = packageManager.getApplicationInfo(splits[0], 0);
                        if (app != null) {
                            Drawable icon = packageManager.getApplicationIcon(app);
                            //CharSequence name = packageManager.getApplicationLabel(app);
                            packageIcon.setImageDrawable(icon);
                        } else {
                            packageIcon.setImageResource(R.drawable.ic_empty);
                        }
                    } catch (Exception e) {
                        packageIcon.setImageResource(R.drawable.ic_empty);
                    }
                }
                else {
                    Intent intent = new Intent();
                    intent.setClassName(Application.getPackageName(splits[0]), activityName);
                    ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                    if (info != null)
                        packageIcon.setImageDrawable(info.loadIcon(packageManager));
                    else
                        packageIcon.setImageResource(R.drawable.ic_empty);
                }
            }
        }
        else {
            packageIcon.setVisibility(View.GONE);
            packageIcons.setVisibility(View.VISIBLE);
            packageIcon.setImageResource(R.drawable.ic_empty);

            ImageView packIcon = packageIcon1;
            for (int i = 0; i < 4; i++)
            {
                //if (i == 0) packIcon = packageIcon1;
                if (i == 1) packIcon = packageIcon2;
                if (i == 2) packIcon = packageIcon3;
                if (i == 3) packIcon = packageIcon4;
                if (i < splits.length) {

                    /*if (Application.isShortcut(splits[i])) {
                        Intent intent = new Intent();
                        intent.setClassName(Application.getPackageName(splits[i]), Application.getActivityName(splits[i]));
                        ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);

                        if (info != null) {
                            packIcon.setImageDrawable(info.loadIcon(packageManager));
                        } else {
                            packIcon.setImageResource(R.drawable.ic_empty);
                        }
                    }
                    else
                    if (Application.isIntent(splits[i])) {
                    } else*/ {
                        String activityName = Application.getActivityName(splits[i]);
                        if (activityName.isEmpty()) {
                            try {
                                app = packageManager.getApplicationInfo(splits[i], 0);
                                if (app != null) {
                                    Drawable icon = packageManager.getApplicationIcon(app);
                                    //CharSequence name = packageManager.getApplicationLabel(app);
                                    packIcon.setImageDrawable(icon);
                                } else {
                                    packIcon.setImageResource(R.drawable.ic_empty);
                                }
                            } catch (Exception e) {
                                packIcon.setImageResource(R.drawable.ic_empty);
                            }
                        }
                        else {
                            Intent intent = new Intent();
                            intent.setClassName(Application.getPackageName(splits[i]), activityName);
                            ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);

                            if (info != null) {
                                packIcon.setImageDrawable(info.loadIcon(packageManager));
                            } else {
                                packIcon.setImageResource(R.drawable.ic_empty);
                            }
                        }
                    }
                }
                else
                    packIcon.setImageResource(R.drawable.ic_empty);
            }
        }
    }


    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            // save is not needed, is already saved persistent
            return superState;
        }*/

        final ApplicationsMultiSelectDialogPreferenceX.SavedState myState = new ApplicationsMultiSelectDialogPreferenceX.SavedState(superState);
        myState.value = getValue();
        myState.defaultValue = defaultValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!state.getClass().equals(ApplicationsMultiSelectDialogPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            getValueAMSDP();
            setSummaryAMSDP();
            return;
        }

        // restore instance state
        ApplicationsMultiSelectDialogPreferenceX.SavedState myState = (ApplicationsMultiSelectDialogPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        getValueAMSDP();
        setSummaryAMSDP();
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

        public static final Creator<ApplicationsMultiSelectDialogPreferenceX.SavedState> CREATOR =
                new Creator<ApplicationsMultiSelectDialogPreferenceX.SavedState>() {
                    public ApplicationsMultiSelectDialogPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new ApplicationsMultiSelectDialogPreferenceX.SavedState(in);
                    }
                    public ApplicationsMultiSelectDialogPreferenceX.SavedState[] newArray(int size)
                    {
                        return new ApplicationsMultiSelectDialogPreferenceX.SavedState[size];
                    }

                };

    }

}
