package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
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
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

public class ApplicationsMultiSelectDialogPreference extends DialogPreference
{

    private Context _context = null;
    private String value = "";

    private MaterialDialog mDialog;

    private final int addShortcuts;
    private final String systemSettings;

    // Layout widgets.
    private LinearLayout linlaProgress;
    private LinearLayout linlaListView;

    private ImageView packageIcon;
    private RelativeLayout packageIcons;
    private ImageView packageIcon1;
    private ImageView packageIcon2;
    private ImageView packageIcon3;
    private ImageView packageIcon4;

    private ApplicationsMultiSelectPreferenceAdapter listAdapter;
    //private ItemTouchHelper itemTouchHelper;

    private AsyncTask asyncTask = null;

    List<Application> applicationList;

    public ApplicationsMultiSelectDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.ApplicationsMultiSelectDialogPreference);

        addShortcuts = typedArray.getInteger(
                R.styleable.ApplicationsMultiSelectDialogPreference_addShortcuts, 0);
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

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .neutralText(R.string.pref_dlg_change_selection_button_unselect_all)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @SuppressWarnings("StringConcatenationInLoop")
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
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
                                        if (application.shortcut)
                                            value = value + "(s)";
                                        value = value + application.packageName + "/" + application.activityName;
                                    }
                                }
                            }
                            persistString(value);

                            setIcons();
                            setSummaryAMSDP();
                            mDialog.dismiss();
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mDialog.dismiss();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        value="";
                        refreshListView(false);
                    }
                })
                .content(getDialogMessage())
                .customView(R.layout.activity_applications_multiselect_pref_dialog, false);

        mBuilder.showListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ApplicationsMultiSelectDialogPreference.this.onShow(/*dialog*/);
            }
        });

        mDialog = mBuilder.build();
        View layout = mDialog.getCustomView();

        //noinspection ConstantConditions
        linlaProgress = layout.findViewById(R.id.applications_multiselect_pref_dlg_linla_progress);
        //noinspection ConstantConditions
        linlaListView = layout.findViewById(R.id.applications_multiselect_pref_dlg_linla_listview);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        //noinspection ConstantConditions
        FastScrollRecyclerView listView = layout.findViewById(R.id.applications_multiselect_pref_dlg_listview);
        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        listAdapter = new ApplicationsMultiSelectPreferenceAdapter(_context, this, addShortcuts);
        listView.setAdapter(listAdapter);

        /*
        // added touch helper for drag and drop items
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(listAdapter, false, false);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(listView);
        */

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
                    linlaListView.setVisibility(View.GONE);
                    linlaProgress.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                if (!EditorProfilesActivity.getApplicationsCache().cached)
                    EditorProfilesActivity.getApplicationsCache().getApplicationsList(_context);

                getValueAMSDP(notForUnselect);

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                if (!EditorProfilesActivity.getApplicationsCache().cached)
                    EditorProfilesActivity.getApplicationsCache().clearCache(false);

                listAdapter.notifyDataSetChanged();
                if (notForUnselect) {
                    linlaListView.setVisibility(View.VISIBLE);
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

        EditorProfilesActivity.getApplicationsCache().cancelCaching();
        if (!EditorProfilesActivity.getApplicationsCache().cached)
            EditorProfilesActivity.getApplicationsCache().clearCache(false);
        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mDialog != null && mDialog.isShowing())
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

        // change checked state by value
        applicationList = EditorProfilesActivity.getApplicationsCache().getList(addShortcuts == 0);
        if (applicationList != null)
        {
            String[] splits = value.split("\\|");
            for (Application application : applicationList)
            {
                application.checked = false;
                for (String split : splits) {
                    String packageName;
                    String activityName;
                    String shortcut;
                    String[] splits2 = split.split("/");
                    if (split.length() > 2) {
                        if (splits2.length == 2) {
                            shortcut = splits2[0].substring(0, 3);
                            packageName = splits2[0];
                            activityName = splits2[1];
                        } else {
                            shortcut = value.substring(0, 3);
                            packageName = value;
                            activityName = "";
                        }
                        if (shortcut.equals("(s)")) {
                            packageName = packageName.substring(3);
                        }
                        boolean shortcutPassed = shortcut.equals("(s)") == application.shortcut;
                        boolean packagePassed = packageName.equals(application.packageName);
                        boolean activityPassed = activityName.equals(application.activityName);

                        if (!activityName.isEmpty()) {
                            if (shortcutPassed && packagePassed && activityPassed)
                                application.checked = true;
                        } else {
                            if (!shortcut.equals("(s)") && (!application.shortcut)) {
                                if (packagePassed)
                                    application.checked = true;
                            }
                        }
                    }
                }
            }
            // move checked on top
            int i = 0;
            int ich = 0;
            while (i < applicationList.size()) {
                Application application = applicationList.get(i);
                if (application.checked) {
                    applicationList.remove(i);
                    applicationList.add(ich, application);
                    ich++;
                }
                i++;
            }
        }
    }

    void setSummaryAMSDP()
    {
        String prefDataSummary = _context.getString(R.string.applications_multiselect_summary_text_not_selected);
        boolean ok = true;
        if (systemSettings.equals("notifications") && (!PPNotificationListenerService.isNotificationListenerServiceEnabled(_context))) {
            ok = false;
            prefDataSummary = _context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                    ": "+_context.getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings);
        }
        else
        if (systemSettings.equals("accessibility")) {
            int exctenderVersion = ForegroundApplicationChangedBroadcastReceiver.isExtenderInstalled(_context);
            if (exctenderVersion == 0) {
                ok = false;
                prefDataSummary = _context.getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + _context.getString(R.string.preference_not_allowed_reason_not_extender_installed);
            }
            else
            if (exctenderVersion < PPApplication.VERSION_CODE_EXTENDER) {
                ok = false;
                prefDataSummary = _context.getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + _context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
            }
            else
            if (!ForegroundApplicationChangedBroadcastReceiver.isAccessibilityServiceEnabled(_context)) {
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
                    if (!ApplicationsCache.isShortcut(splits[0])) {
                        if (ApplicationsCache.getActivityName(splits[0]).isEmpty()) {
                            ApplicationInfo app;
                            try {
                                app = packageManager.getApplicationInfo(splits[0], 0);
                                if (app != null)
                                    prefDataSummary = packageManager.getApplicationLabel(app).toString();
                            } catch (Exception e) {
                                //e.printStackTrace();
                            }
                        } else {
                            Intent intent = new Intent();
                            intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                            ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                            if (info != null)
                                prefDataSummary = info.loadLabel(packageManager).toString();
                        }
                    } else {
                        Intent intent = new Intent();
                        intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                        ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                        if (info != null)
                            prefDataSummary = info.loadLabel(packageManager).toString();
                    }
                }
            }
        }
        setSummary(prefDataSummary);
    }

    private void setIcons() {
        PackageManager packageManager = _context.getPackageManager();
        ApplicationInfo app;

        String[] splits = value.split("\\|");

        if (splits.length == 1) {
            packageIcon.setVisibility(View.VISIBLE);
            packageIcons.setVisibility(View.GONE);

            if (!ApplicationsCache.isShortcut(splits[0])) {
                if (ApplicationsCache.getActivityName(splits[0]).isEmpty()) {
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
                        //e.printStackTrace();
                        packageIcon.setImageResource(R.drawable.ic_empty);
                    }
                }
                else {
                    Intent intent = new Intent();
                    intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                    ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                    if (info != null)
                        packageIcon.setImageDrawable(info.loadIcon(packageManager));
                    else
                        packageIcon.setImageResource(R.drawable.ic_empty);
                }
            }
            else {
                Intent intent = new Intent();
                intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                if (info != null)
                    packageIcon.setImageDrawable(info.loadIcon(packageManager));
                else
                    packageIcon.setImageResource(R.drawable.ic_empty);
            }
        }
        else {
            packageIcon.setVisibility(View.GONE);
            packageIcons.setVisibility(View.VISIBLE);
            packageIcon.setImageResource(R.drawable.ic_empty);

            ImageView packIcon = packageIcon1;
            for (int i = 0; i < 4; i++)
            {
                if (i == 0) packIcon = packageIcon1;
                if (i == 1) packIcon = packageIcon2;
                if (i == 2) packIcon = packageIcon3;
                if (i == 3) packIcon = packageIcon4;
                if (i < splits.length) {

                    if (!ApplicationsCache.isShortcut(splits[i])) {
                        if (ApplicationsCache.getActivityName(splits[i]).isEmpty()) {
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
                                //e.printStackTrace();
                                packIcon.setImageResource(R.drawable.ic_empty);
                            }
                        }
                        else {
                            Intent intent = new Intent();
                            intent.setClassName(ApplicationsCache.getPackageName(splits[i]), ApplicationsCache.getActivityName(splits[i]));
                            ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);

                            if (info != null) {
                                packIcon.setImageDrawable(info.loadIcon(packageManager));
                            } else {
                                packIcon.setImageResource(R.drawable.ic_empty);
                            }
                        }
                    }
                    else {
                        Intent intent = new Intent();
                        intent.setClassName(ApplicationsCache.getPackageName(splits[i]), ApplicationsCache.getActivityName(splits[i]));
                        ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);

                        if (info != null) {
                            packIcon.setImageDrawable(info.loadIcon(packageManager));
                        } else {
                            packIcon.setImageResource(R.drawable.ic_empty);
                        }
                    }
                }
                else
                    packIcon.setImageResource(R.drawable.ic_empty);
            }
        }
    }

}
