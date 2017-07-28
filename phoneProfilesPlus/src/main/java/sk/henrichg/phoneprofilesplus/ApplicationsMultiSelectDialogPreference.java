package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.andraskindler.quickscroll.QuickScroll;

import java.util.List;

public class ApplicationsMultiSelectDialogPreference extends DialogPreference
{

    private Context _context = null;
    private String value = "";

    private MaterialDialog mDialog;

    private int addShortcuts;
    private String systemSettings;

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

    }

    //@Override
    protected void onBindView(View view)
    {
        super.onBindView(view);

        packageIcon = (ImageView)view.findViewById(R.id.applications_pref_icon);
        packageIcons = (RelativeLayout)view.findViewById(R.id.applications_pref_icons);
        packageIcon1 = (ImageView)view.findViewById(R.id.applications_pref_icon1);
        packageIcon2 = (ImageView)view.findViewById(R.id.applications_pref_icon2);
        packageIcon3 = (ImageView)view.findViewById(R.id.applications_pref_icon3);
        packageIcon4 = (ImageView)view.findViewById(R.id.applications_pref_icon4);

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
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (shouldPersist())
                        {
                            // sem narvi stringy kontatkov oddelenych |
                            value = "";
                            List<Application> applicationList = EditorProfilesActivity.getApplicationsCache().getList(addShortcuts == 0);
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
                ApplicationsMultiSelectDialogPreference.this.onShow(dialog);
            }
        });

        mDialog = mBuilder.build();
        View layout = mDialog.getCustomView();

        linlaProgress = (LinearLayout)layout.findViewById(R.id.applications_multiselect_pref_dlg_linla_progress);
        linlaListView = (LinearLayout)layout.findViewById(R.id.applications_multiselect_pref_dlg_linla_listview);
        ListView listView = (ListView)layout.findViewById(R.id.applications_multiselect_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                Application application = (Application)listAdapter.getItem(position);
                application.toggleChecked();
                ApplicationViewHolder viewHolder = (ApplicationViewHolder) item.getTag();
                viewHolder.checkBox.setChecked(application.checked);
            }
        });

        listAdapter = new ApplicationsMultiSelectPreferenceAdapter(_context, addShortcuts);
        listView.setAdapter(listAdapter);

        TypedValue tv = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorQSScrollbar, tv, true);
        int colorQSScrollbar = tv.data;
        getContext().getTheme().resolveAttribute(R.attr.colorQSHandlebarInactive, tv, true);
        int colorQSHandlebarInactive = tv.data;
        getContext().getTheme().resolveAttribute(R.attr.colorQSHandlebarActive, tv, true);
        int colorQSHandlebarActive = tv.data;
        getContext().getTheme().resolveAttribute(R.attr.colorQSHandlebarStroke, tv, true);
        int colorQSHandlebarStroke = tv.data;

        final QuickScroll quickscroll = (QuickScroll)layout.findViewById(R.id.applications_pref_dlg_quickscroll);
        quickscroll.init(QuickScroll.TYPE_INDICATOR_WITH_HANDLE, listView, listAdapter, QuickScroll.STYLE_HOLO, colorQSScrollbar);
        quickscroll.setHandlebarColor(colorQSHandlebarInactive, colorQSHandlebarActive, colorQSHandlebarStroke);
        quickscroll.setIndicatorColor(colorQSHandlebarActive, colorQSHandlebarActive, Color.WHITE);
        quickscroll.setFixedSize(1);

        MaterialDialogsPrefUtil.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    private void refreshListView(final boolean notForUnselect) {
        new AsyncTask<Void, Integer, Void>() {

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
                if (!EditorProfilesActivity.getApplicationsCache().isCached())
                    EditorProfilesActivity.getApplicationsCache().getApplicationsList(_context);

                getValueAMSDP(notForUnselect);

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                if (!EditorProfilesActivity.getApplicationsCache().isCached())
                    EditorProfilesActivity.getApplicationsCache().clearCache(false);

                listAdapter.notifyDataSetChanged();
                if (notForUnselect) {
                    linlaListView.setVisibility(View.VISIBLE);
                    linlaProgress.setVisibility(View.GONE);
                }
            }

        }.execute();
    }

    private void onShow(DialogInterface dialog) {
        refreshListView(true);
    }

    public void onDismiss (DialogInterface dialog)
    {
        super.onDismiss(dialog);
        EditorProfilesActivity.getApplicationsCache().cancelCaching();
        if (!EditorProfilesActivity.getApplicationsCache().isCached())
            EditorProfilesActivity.getApplicationsCache().clearCache(false);
        MaterialDialogsPrefUtil.unregisterOnActivityDestroyListener(this, this);
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
            // sem narvi default string kontaktov oddeleny |
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
        List<Application> applicationList = EditorProfilesActivity.getApplicationsCache().getList(addShortcuts == 0);
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
        if (systemSettings.equals("notifications") && (!PPNotificationListenerService.isNotificationListenerServiceEnabled(_context))) {
            prefDataSummary = _context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                    ": "+_context.getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings);
        }
        else
        if (systemSettings.equals("accessibility") && (!ForegroundApplicationChangedService.isEnabled(_context))) {
            prefDataSummary = _context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                    ": "+_context.getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings);
        }
        else
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
                        } catch (PackageManager.NameNotFoundException e) {
                            //e.printStackTrace();
                        }
                    }
                    else {
                        Intent intent = new Intent();
                        intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                        ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                        if (info != null)
                            prefDataSummary = info.loadLabel(packageManager).toString();
                    }
                }
                else {
                    Intent intent = new Intent();
                    intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                    ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                    if (info != null)
                        prefDataSummary = info.loadLabel(packageManager).toString();
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
                    } catch (PackageManager.NameNotFoundException e) {
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
                            } catch (PackageManager.NameNotFoundException e) {
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
