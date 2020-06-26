package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ApplicationsDialogPreferenceX extends DialogPreference {

    ApplicationsDialogPreferenceFragmentX fragment;

    final Context context;

    private String value = "";
    private String defaultValue;
    private boolean savedInstanceState;

    final List<Application> oldApplicationsList;
    final List<Application> applicationsList;
    final List<PPIntent> intentDBList;

    //PPIntent editedPPIntent = null;

    ApplicationEditorDialogX mEditorDialog;

    private ImageView packageIcon;
    private RelativeLayout packageIcons;
    private ImageView packageIcon1;
    private ImageView packageIcon2;
    private ImageView packageIcon3;
    private ImageView packageIcon4;

    static final int RESULT_APPLICATIONS_EDITOR = 2100;

    public ApplicationsDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        /*
        TypedArray applicationsType = context.obtainStyledAttributes(attrs,
                R.styleable.ApplicationsPreference, 0, 0);

        onlyEdit = applicationsType.getInt(R.styleable.ApplicationsPreference_onlyEdit, 0);

        applicationsType.recycle();
        */

        this.context = context;

        oldApplicationsList = new ArrayList<>();
        applicationsList = new ArrayList<>();
        intentDBList = new ArrayList<>();

        setWidgetLayoutResource(R.layout.widget_applications_preference); // resource na layout custom preference - TextView-ImageView

        if (EditorProfilesActivity.getApplicationsCache() == null)
            EditorProfilesActivity.createApplicationsCache();

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
        // Get the persistent value
        value = getPersistedString((String)defaultValue);
        this.defaultValue = (String)defaultValue;
        getValueAMSDP();
        setSummaryAMSDP();
    }

    void getValueAMSDP()
    {
        //PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP","value="+value);

        applicationsList.clear();

        if (EditorProfilesActivity.getApplicationsCache() != null) {
            List<Application> cachedApplicationList = EditorProfilesActivity.getApplicationsCache().getApplicationList(false);

            String notPassedIntents = "";

            String[] splits = value.split("\\|");
            for (String split : splits) {
                boolean applicationPassed = false;

                if (cachedApplicationList != null) {

                    for (Application application : cachedApplicationList) {
                        application.checked = false;

                        String packageName;
                        String activityName = "";
                        String shortcutIntent;
                        String shortcutId = "";
                        String startApplicationDelay = "0";
                        String[] packageNameActivity = split.split("/"); // package name/activity
                        if (split.length() > 2) {
                            shortcutIntent = packageNameActivity[0].substring(0, 3);

                            switch (shortcutIntent) {
                                case "(i)":
                                    // skip intents
                                    continue;
                                case "(s)":
                                    // shortcut
                                    packageName = packageNameActivity[0].substring(3);
                                    if (packageNameActivity.length == 2) {
                                        // activity exists
                                        String[] shortcutIdDelay = packageNameActivity[1].split("#");
                                        activityName = shortcutIdDelay[0];
                                        if (shortcutIdDelay.length == 3) {
                                            shortcutId = shortcutIdDelay[1];
                                            startApplicationDelay = shortcutIdDelay[2];
                                        } else if (shortcutIdDelay.length == 2)
                                            startApplicationDelay = shortcutIdDelay[1];
                                    } else {
                                        // activity not exists
                                        String[] packageNameShortcutIdDelay = split.split("#");
                                        if (packageNameShortcutIdDelay.length == 3) {
                                            shortcutId = packageNameShortcutIdDelay[1];
                                            startApplicationDelay = packageNameShortcutIdDelay[2];
                                        } else if (packageNameShortcutIdDelay.length == 2) {
                                            startApplicationDelay = packageNameShortcutIdDelay[1];
                                        }
                                    }

                                    boolean typePassed = application.type == Application.TYPE_SHORTCUT;
                                    boolean packagePassed = packageName.equals(application.packageName);
                                    boolean activityPassed = activityName.equals(application.activityName);

                                    applicationPassed = typePassed && packagePassed && activityPassed;
                                    break;
                                default:
                                    // application
                                    if (packageNameActivity.length == 2) {
                                        // activity exists
                                        packageName = packageNameActivity[0];
                                        String[] activityDelay = packageNameActivity[1].split("#");
                                        activityName = activityDelay[0];
                                        if (activityDelay.length >= 2)
                                            startApplicationDelay = activityDelay[1];
                                    } else {
                                        // activity not exists
                                        String[] packageNameDelay = split.split("#");
                                        if (packageNameDelay.length >= 2) {
                                            packageName = packageNameDelay[0];
                                            startApplicationDelay = packageNameDelay[1];
                                        } else {
                                            packageName = split;
                                        }
                                    }

                                    typePassed = application.type == Application.TYPE_APPLICATION;
                                    packagePassed = packageName.equals(application.packageName);
                                    activityPassed = activityName.isEmpty() || activityName.equals(application.activityName);

                                    applicationPassed = typePassed && packagePassed && activityPassed;
                                    break;
                            }

                            if (applicationPassed) {
                                Application _application = new Application();
                                _application.type = application.type;
                                _application.appLabel = application.appLabel;
                                _application.packageName = application.packageName;
                                _application.activityName = application.activityName;

                                try {
                                    _application.startApplicationDelay = Integer.parseInt(startApplicationDelay);
                                } catch (Exception e) {
                                    _application.startApplicationDelay = 0;
                                }

                                if (!shortcutId.isEmpty()) {
                                    try {
                                        _application.shortcutId = Long.parseLong(shortcutId);
                                    } catch (Exception e) {
                                        _application.shortcutId = 0;
                                    }
                                }

                                _application.checked = true;

                                applicationsList.add(_application);

                                /*if (PPApplication.logEnabled()) {
                                    PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP", "packageName=" + packageName);
                                    PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP", "activityName=" + activityName);
                                    PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP", "shortcutIntent=" + shortcutIntent);
                                    PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP", "shortcutId=" + shortcutId);
                                    PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP", "startApplicationDelay=" + startApplicationDelay);
                                    PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP", "checked=" + _application.checked);
                                }*/

                                break;
                            }
                        }
                    }

                    if (applicationPassed)
                        continue;
                }

                boolean intentPassed = false;

                for (PPIntent ppIntent : intentDBList) {
                    String intentId = "";
                    String startApplicationDelay = "0";
                    String shortcutIntent;
                    String[] intentIdDelay = split.split("#");
                    if (split.length() > 2) {
                        shortcutIntent = intentIdDelay[0].substring(0, 3);

                        if ("(i)".equals(shortcutIntent)) {// intent
                            if (intentIdDelay.length >= 2) {
                                intentId = intentIdDelay[0].substring(3);
                                startApplicationDelay = intentIdDelay[1];
                            } else {
                                intentId = split.substring(3);
                            }

                            intentPassed = intentId.equals(String.valueOf(ppIntent._id));
                        }

                        if (intentPassed) {
                            Application _application = new Application();
                            _application.type = Application.TYPE_INTENT;

                            try {
                                _application.intentId = Long.parseLong(intentId);
                            } catch (Exception e) {
                                _application.intentId = 0;
                            }

                            try {
                                _application.startApplicationDelay = Integer.parseInt(startApplicationDelay);
                            } catch (Exception e) {
                                _application.startApplicationDelay = 0;
                            }

                            _application.checked = true;

                            applicationsList.add(_application);

                            /*if (PPApplication.logEnabled()) {
                                PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP", "shortcutIntent=" + shortcutIntent);
                                PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP", "intentId=" + intentId);
                                PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP", "startApplicationDelay=" + startApplicationDelay);
                                PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP", "checked=" + _application.checked);
                            }*/

                            break;
                        }
                    }
                }

                //PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP", "intentPassed=" + intentPassed);

                if (intentPassed)
                    continue;

                //PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP", "split=" + split);
                if (!notPassedIntents.isEmpty())
                    //noinspection StringConcatenationInLoop
                    notPassedIntents = notPassedIntents + "|";
                //noinspection StringConcatenationInLoop
                notPassedIntents = notPassedIntents + split;
            }

            //PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP", "notPassedIntents=" + notPassedIntents);
            if (!notPassedIntents.isEmpty()) {
                // add not passed intents
                splits = notPassedIntents.split("\\|");
                for (String split : splits) {
                    if (split.length() > 2) {
                        Application _application = new Application();
                        _application.type = Application.TYPE_INTENT;
                        _application.intentId = 0;
                        _application.startApplicationDelay = 0;
                        _application.checked = true;

                        applicationsList.add(_application);
                    }
                }
                //PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP", "added not passed intent");
            }
        }
    }

    private void setSummaryAMSDP()
    {
        String prefSummary = context.getString(R.string.applications_multiselect_summary_text_not_selected);
        if (!value.isEmpty() && !value.equals("-")) {
            String[] splits = value.split("\\|");
            prefSummary = context.getString(R.string.applications_multiselect_summary_text_selected) + ": " + splits.length;
            if (splits.length == 1) {
                PackageManager packageManager = context.getPackageManager();
                if (Application.isShortcut(splits[0])) {
                    Intent intent = new Intent();
                    intent.setClassName(Application.getPackageName(splits[0]), Application.getActivityName(splits[0]));
                    ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                    if (info != null) {
                        long shortcutId = Application.getShortcutId(splits[0]);
                        if (shortcutId > 0) {
                            Shortcut shortcut = DatabaseHandler.getInstance(context.getApplicationContext()).getShortcut(shortcutId);
                            if (shortcut != null)
                                prefSummary = shortcut._name;
                        }
                        else
                            prefSummary = info.loadLabel(packageManager).toString();
                    }
                }
                else
                if (Application.isIntent(splits[0])) {
                    long intentId = Application.getIntentId(splits[0]);
                    if (intentId > 0) {
                        for (PPIntent intent : intentDBList) {
                            if (intent._id == intentId) {
                                prefSummary = intent._name;
                                break;
                            }
                        }
                    }
                    else
                        prefSummary = context.getString(R.string.empty_string);
                }
                else {
                    String activityName = Application.getActivityName(splits[0]);
                    if (activityName.isEmpty()) {
                        ApplicationInfo app;
                        try {
                            app = packageManager.getApplicationInfo(splits[0], 0);
                            if (app != null)
                                prefSummary = packageManager.getApplicationLabel(app).toString();
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    else {
                        Intent intent = new Intent();
                        intent.setClassName(Application.getPackageName(splits[0]), activityName);
                        ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                        if (info != null)
                            prefSummary = info.loadLabel(packageManager).toString();
                    }
                }
            }
        }
        setSummary(prefSummary);
    }

    @SuppressWarnings("StringConcatenationInLoop")
    private String getValue() {
        String _value = "";
        if (applicationsList != null)
        {
            for (Application application : applicationsList)
            {
                if (!_value.isEmpty())
                    _value = _value + "|";

                if (application.type == Application.TYPE_SHORTCUT)
                    _value = _value + "(s)";
                if (application.type == Application.TYPE_INTENT)
                    _value = _value + "(i)";

                if (application.type != Application.TYPE_INTENT)
                    _value = _value + application.packageName + "/" + application.activityName;
                else
                    _value = _value + application.intentId;

                if ((application.type == Application.TYPE_SHORTCUT)/* && (application.shortcutId > 0)*/)
                    _value = _value + "#" + application.shortcutId;

                _value = _value + "#" + application.startApplicationDelay;
                //PPApplication.logE("ApplicationsDialogPreference.onPositive","value="+value);
            }
        }
        return _value;
    }

    void persistValue() {
        if (shouldPersist())
        {
            DatabaseHandler.getInstance(context.getApplicationContext()).updatePPIntentUsageCount(oldApplicationsList, applicationsList);
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
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo app;

        String[] splits = value.split("\\|");

        if (!value.isEmpty() && !value.equals("-")) {
            if (splits.length == 1) {
                packageIcon.setVisibility(View.VISIBLE);
                packageIcon1.setImageResource(R.drawable.ic_empty);
                packageIcon2.setImageResource(R.drawable.ic_empty);
                packageIcon3.setImageResource(R.drawable.ic_empty);
                packageIcon4.setImageResource(R.drawable.ic_empty);
                packageIcons.setVisibility(View.GONE);

                if (Application.isShortcut(splits[0])) {
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
                    packageIcon.setImageResource(R.drawable.ic_profile_pref_run_application);
                } else {
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
                    } else {
                        Intent intent = new Intent();
                        intent.setClassName(Application.getPackageName(splits[0]), activityName);
                        ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                        if (info != null)
                            packageIcon.setImageDrawable(info.loadIcon(packageManager));
                        else
                            packageIcon.setImageResource(R.drawable.ic_empty);
                    }
                }
            } else {
                packageIcons.setVisibility(View.VISIBLE);
                packageIcon.setVisibility(View.GONE);
                packageIcon.setImageResource(R.drawable.ic_empty);

                ImageView packIcon = packageIcon1;
                for (int i = 0; i < 4; i++) {
                    //if (i == 0) packIcon = packageIcon1;
                    if (i == 1) packIcon = packageIcon2;
                    if (i == 2) packIcon = packageIcon3;
                    if (i == 3) packIcon = packageIcon4;
                    if (i < splits.length) {
                        if (Application.isShortcut(splits[i])) {
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
                        if (Application.isIntent(splits[0])) {
                            packIcon.setImageResource(R.drawable.ic_profile_pref_run_application);
                        } else {
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
                            } else {
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
                    } else
                        packIcon.setImageResource(R.drawable.ic_empty);
                }
            }
        }
        else {
            packageIcon.setVisibility(View.VISIBLE);
            packageIcons.setVisibility(View.GONE);
            packageIcon.setImageResource(R.drawable.ic_empty);
        }
    }

    void showEditMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        Context _context = view.getContext();
        PopupMenu popup;
        //if (android.os.Build.VERSION.SDK_INT >= 19)
            popup = new PopupMenu(_context, view, Gravity.END);
        //else
        //    popup = new PopupMenu(context, view);

        final Application application = (Application) view.getTag();
        //PPApplication.logE("ApplicationsDialogPreference.showEditMenu", "application="+application);

        new MenuInflater(_context).inflate(R.menu.applications_pref_dlg_item_edit, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.applications_pref_dlg_item_menu_edit:
                        startEditor(application);
                        return true;
                    case R.id.applications_pref_dlg_item_menu_delete:
                        deleteApplication(application);
                        return true;
                    default:
                        return false;
                }
            }
        });

        if (fragment.getActivity() != null)
            if (!fragment.getActivity().isFinishing())
                popup.show();
    }

    void startEditor(Application application) {
        if (fragment.getActivity() != null)
            if (!fragment.getActivity().isFinishing()) {
                mEditorDialog = new ApplicationEditorDialogX(fragment.getActivity(), this, application);
                mEditorDialog.show();
            }
    }

    void updateGUI() {
        if (fragment != null)
            fragment.updateGUI();
    }

    private void deleteApplication(Application application) {
        if (application == null)
            return;

        if (application.shortcutId > 0)
            DatabaseHandler.getInstance(context.getApplicationContext()).deleteShortcut(application.shortcutId);

        applicationsList.remove(application);

        DatabaseHandler.getInstance(context.getApplicationContext()).updatePPIntentUsageCount(oldApplicationsList, applicationsList);

        if (fragment != null)
            fragment.updateGUI();
    }

    void updateApplication(Application application, Application selectedApplication, int startApplicationDelay) {
        if (selectedApplication == null)
            return;

        if (selectedApplication.type == Application.TYPE_INTENT) {
            if (intentDBList != null) {
                Application editedApplication = application;
                if (editedApplication == null) {
                    editedApplication = new Application();
                    applicationsList.add(editedApplication);
                }
                editedApplication.type = selectedApplication.type;
                editedApplication.appLabel = selectedApplication.appLabel;
                editedApplication.intentId = selectedApplication.intentId;

                if (editedApplication.type != Application.TYPE_SHORTCUT)
                    editedApplication.shortcutId = 0;
                if (editedApplication.type != Application.TYPE_INTENT)
                    editedApplication.intentId = 0;
                editedApplication.startApplicationDelay = startApplicationDelay;

                if (fragment != null)
                    fragment.updateGUI();
            }
            return;
        }

        if (EditorProfilesActivity.getApplicationsCache() != null) {
            List<Application> cachedApplicationList = EditorProfilesActivity.getApplicationsCache().getApplicationList(false);
            if (cachedApplicationList != null) {
                int _position = applicationsList.indexOf(application);
                Application editedApplication = application;
                if (editedApplication == null) {
                    editedApplication = new Application();
                    applicationsList.add(editedApplication);
                    _position = applicationsList.size() - 1;
                }
                editedApplication.type = selectedApplication.type;
                editedApplication.appLabel = selectedApplication.appLabel;
                editedApplication.packageName = selectedApplication.packageName;
                editedApplication.activityName = selectedApplication.activityName;

                if (editedApplication.type != Application.TYPE_SHORTCUT)
                    editedApplication.shortcutId = 0;
                if (editedApplication.type != Application.TYPE_INTENT)
                    editedApplication.intentId = 0;
                editedApplication.startApplicationDelay = startApplicationDelay;

                if (fragment != null)
                    fragment.updateGUI();

                if ((editedApplication.type == Application.TYPE_SHORTCUT) &&
                        (editedApplication.packageName != null)) {
                    Intent intent = new Intent(context, LaunchShortcutActivity.class);
                    intent.putExtra(LaunchShortcutActivity.EXTRA_PACKAGE_NAME, editedApplication.packageName);
                    intent.putExtra(LaunchShortcutActivity.EXTRA_ACTIVITY_NAME, editedApplication.activityName);
                    intent.putExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_POSITION, _position);
                    intent.putExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, startApplicationDelay);

                    //ProfilesPrefsFragment.setApplicationsDialogPreference(this);
                    ((Activity) context).startActivityForResult(intent, RESULT_APPLICATIONS_EDITOR);
                }
            }
        }
    }

    void updateShortcut(Intent shortcutIntent, String shortcutName, int position, int startApplicationDelay) {
        Application application = applicationsList.get(position);
        if (application.shortcutId > 0) {
            DatabaseHandler.getInstance(context.getApplicationContext()).deleteShortcut(application.shortcutId);
        }

        if (shortcutIntent != null) {
            /* Storing Intent to SQLite ;-)
            You can simply store the intent in a String way:

            String intentDescription = intent.toUri(0);
            //Save the intent string into your database

            Later you can restore the Intent:

            String intentDescription = cursor.getString(intentIndex);
            Intent intent = Intent.parseUri(intentDescription, 0);
            */
            String intentDescription = shortcutIntent.toUri(0);

            Shortcut shortcut = new Shortcut();
            shortcut._intent = intentDescription;
            shortcut._name = shortcutName;
            DatabaseHandler.getInstance(context.getApplicationContext()).addShortcut(shortcut);
            application.shortcutId = shortcut._id;
            application.startApplicationDelay = startApplicationDelay;
        }

        if (fragment != null)
            fragment.updateGUI();
    }

    void updateIntent(PPIntent ppIntent, Application application, int startApplicationDelay) {
        if (ppIntent != null) {
            //PPApplication.logE("ApplicationsDialogPreference.updateIntent", "ppIntent._id="+ppIntent._id);
            if (ppIntent._id == 0) {
                DatabaseHandler.getInstance(context.getApplicationContext()).addIntent(ppIntent);
                //PPApplication.logE("ApplicationsDialogPreference.updateIntent", "ppIntent._id="+ppIntent._id);
                //intentDBList.add(ppIntent);
            }
            else
                DatabaseHandler.getInstance(context.getApplicationContext()).updateIntent(ppIntent);
            //editedPPIntent = ppIntent;
            if (application != null) {
                // update application
                application.appLabel = ppIntent._name;
                application.intentId = ppIntent._id;
                application.startApplicationDelay = startApplicationDelay;
            }
            else {
                // add new application
                Application _application = new Application();
                _application.type = Application.TYPE_INTENT;
                _application.intentId = ppIntent._id;
                _application.appLabel = ppIntent._name;
                _application.startApplicationDelay = startApplicationDelay;
                applicationsList.add(_application);
            }

            if (fragment != null)
                fragment.refreshListView(true);
        }
        //else
        //    PPApplication.logE("ApplicationsDialogPreference.updateIntent", "ppIntent=null");

        if (fragment != null)
            fragment.updateGUI();
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

        final ApplicationsDialogPreferenceX.SavedState myState = new ApplicationsDialogPreferenceX.SavedState(superState);
        myState.value = getValue();
        myState.defaultValue = defaultValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!state.getClass().equals(ApplicationsDialogPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            getValueAMSDP();
            setSummaryAMSDP();
            return;
        }

        // restore instance state
        ApplicationsDialogPreferenceX.SavedState myState = (ApplicationsDialogPreferenceX.SavedState)state;
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

        @SuppressWarnings("unused")
        public static final Creator<ApplicationsDialogPreferenceX.SavedState> CREATOR =
                new Creator<ApplicationsDialogPreferenceX.SavedState>() {
                    public ApplicationsDialogPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new ApplicationsDialogPreferenceX.SavedState(in);
                    }
                    public ApplicationsDialogPreferenceX.SavedState[] newArray(int size)
                    {
                        return new ApplicationsDialogPreferenceX.SavedState[size];
                    }

                };

    }

}
