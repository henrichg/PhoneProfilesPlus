package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ApplicationsMultiSelectDialogPreference extends DialogPreference
{

    private final Context _context;
    private String value = "";

    private AlertDialog mDialog;

    private final int removePPApplications;
    private final String systemSettings;

    // Layout widgets.
    private LinearLayout linlaProgress;
    private RelativeLayout rellaData;

    private ImageView packageIcon;
    private RelativeLayout packageIcons;
    private ImageView packageIcon1;
    private ImageView packageIcon2;
    private ImageView packageIcon3;
    private ImageView packageIcon4;

    private ApplicationsMultiSelectPreferenceAdapter listAdapter;
    //private ItemTouchHelper itemTouchHelper;

    private AsyncTask asyncTask = null;

    final List<Application> applicationList;

    public ApplicationsMultiSelectDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        applicationList = new ArrayList<>();

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.ApplicationsMultiSelectDialogPreference);

        removePPApplications = typedArray.getInteger(
                R.styleable.ApplicationsMultiSelectDialogPreference_removePPApplications, 0);
        systemSettings = typedArray.getString(
                R.styleable.ApplicationsMultiSelectDialogPreference_systemSettings);

        typedArray.recycle();

        setWidgetLayoutResource(R.layout.applications_preference); // resource na layout custom preference - TextView-ImageView

        if (EditorProfilesActivity.getApplicationsCache() == null)
            EditorProfilesActivity.createApplicationsCache();

        //applicationsCache = EditorProfilesActivity.getApplicationsCache();
    }

    //@Override
    protected void onBindView(View view)
    {
        super.onBindView(view);

        packageIcon = view.findViewById(R.id.applications_pref_icon);
        packageIcons = view.findViewById(R.id.applications_pref_icons);
        packageIcon1 = view.findViewById(R.id.applications_pref_icon1);
        packageIcon2 = view.findViewById(R.id.applications_pref_icon2);
        packageIcon3 = view.findViewById(R.id.applications_pref_icon3);
        packageIcon4 = view.findViewById(R.id.applications_pref_icon4);

        setIcons();
    }

    protected void showDialog(Bundle state) {
        PPApplication.logE("ApplicationsMultiSelectDialogPreference.showDialog","xxx");

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
                    // fill with contact strings separated with |
                    value = "";
                    if (applicationList != null)
                    {
                        for (Application application : applicationList)
                        {
                            if (application.checked)
                            {
                                if (!value.isEmpty())
                                    value = value + "|";
                                value = value + application.packageName + "/" + application.activityName;
                            }
                        }
                    }
                    persistString(value);

                    setIcons();
                    setSummaryAMSDP();
                }
            }
        });

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_applications_multiselect_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ApplicationsMultiSelectDialogPreference.this.onShow();
            }
        });

        //noinspection ConstantConditions
        linlaProgress = layout.findViewById(R.id.applications_multiselect_pref_dlg_linla_progress);
        //noinspection ConstantConditions
        rellaData = layout.findViewById(R.id.applications_multiselect_pref_dlg_rella_data);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        //noinspection ConstantConditions
        FastScrollRecyclerView listView = layout.findViewById(R.id.applications_multiselect_pref_dlg_listview);
        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        listAdapter = new ApplicationsMultiSelectPreferenceAdapter(this);
        listView.setAdapter(listAdapter);

        /*
        // added touch helper for drag and drop items
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(listAdapter, false, false);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(listView);
        */

        final Button unselectAllButton = layout.findViewById(R.id.applications_multiselect_pref_dlg_unselect_all);
        //unselectAllButton.setAllCaps(false);
        unselectAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                value="";
                refreshListView(false);
            }
        });

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    private void refreshListView(final boolean notForUnselect) {
        asyncTask = new AsyncTask<Void, Integer, Void>() {

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                if (notForUnselect) {
                    rellaData.setVisibility(View.GONE);
                    linlaProgress.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                if (EditorProfilesActivity.getApplicationsCache() != null)
                    if (!EditorProfilesActivity.getApplicationsCache().cached)
                        EditorProfilesActivity.getApplicationsCache().cacheApplicationsList(_context);

                getValueAMSDP(notForUnselect);

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                if (EditorProfilesActivity.getApplicationsCache() != null)
                    if (!EditorProfilesActivity.getApplicationsCache().cached)
                        EditorProfilesActivity.getApplicationsCache().clearCache(false);

                listAdapter.notifyDataSetChanged();
                if (notForUnselect) {
                    rellaData.setVisibility(View.VISIBLE);
                    linlaProgress.setVisibility(View.GONE);
                }
            }

        }.execute();
    }

    private void onShow(/*DialogInterface dialog*/) {
        refreshListView(true);
    }

    public void onDismiss (DialogInterface dialog)
    {
        super.onDismiss(dialog);

        if ((asyncTask != null) && !asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            asyncTask.cancel(true);
        }

        if (EditorProfilesActivity.getApplicationsCache() != null) {
            EditorProfilesActivity.getApplicationsCache().cancelCaching();
            if (!EditorProfilesActivity.getApplicationsCache().cached)
                EditorProfilesActivity.getApplicationsCache().clearCache(false);
        }
        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            getValueAMSDP(true);
        }
        else {
            // set state
            value = "";
            persistString("");
        }
        setSummaryAMSDP();
    }

    private void getValueAMSDP(boolean notForUnselect)
    {
        if (notForUnselect)
            // Get the persistent value
            value = getPersistedString(value);

        PPApplication.logE("ApplicationsMultiSelectDialogPreference.getValueAMSDP", "value="+value);
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
                        String shortcut;
                        String[] packageNameActivity = split.split("/");
                        if (split.length() > 2) {
                            if (packageNameActivity.length == 2) {
                                shortcut = packageNameActivity[0].substring(0, 3);
                                packageName = packageNameActivity[0];
                            } else {
                                shortcut = value.substring(0, 3);
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
                // move checked on top
                applicationList.addAll(cachedApplicationList);
                int i = 0;
                int ich = 0;
                while (i < applicationList.size()) {
                    Application application = applicationList.get(i);
                    if (removePPApplications == 1) {
                        if (
                                application.packageName.equals("sk.henrichg.phoneprofiles") ||
                                        application.packageName.equals("sk.henrichg.phoneprofilesplus") ||
                                        application.packageName.equals("sk.henrichg.phoneprofilesplusextender")
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

    String getSummaryAMSDP()
    {
        String prefDataSummary = _context.getString(R.string.applications_multiselect_summary_text_not_selected);
        boolean ok = true;
        if (systemSettings.equals("notifications") && (!PPNotificationListenerService.isNotificationListenerServiceEnabled(_context))) {
            ok = false;
            prefDataSummary = _context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                    ": "+_context.getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings);
        }
        else
        if (systemSettings.equals("accessibility_2.0")) {
            int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(_context);
            int requiredVersion = PPApplication.VERSION_CODE_EXTENDER_2_0;
            if (extenderVersion == 0) {
                ok = false;
                prefDataSummary = _context.getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + _context.getString(R.string.preference_not_allowed_reason_not_extender_installed);
            }
            else
            if (extenderVersion < requiredVersion) {
                ok = false;
                prefDataSummary = _context.getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + _context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
            }
            else
            if (!PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(_context)) {
                ok = false;
                prefDataSummary = _context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+_context.getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
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
                            } catch (Exception ignored) {
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
        setSummary(prefDataSummary);
        return prefDataSummary;
    }

    void setSummaryAMSDP()
    {
        setSummary(getSummaryAMSDP());
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

}
