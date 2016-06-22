package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

public class ApplicationsDialogPreference  extends DialogPreference {

    Context context;

    String value = "";

    public List<Application> applicationsList = null;

    private MaterialDialog mDialog;

    private ListView applicationsListView;
    private LinearLayout linlaProgress;
    private RelativeLayout rellaDialog;

    private ApplicationsPreferenceAdapter listAdapter;

    ImageView packageIcon;
    RelativeLayout packageIcons;
    ImageView packageIcon1;
    ImageView packageIcon2;
    ImageView packageIcon3;
    ImageView packageIcon4;

    private DataWrapper dataWrapper;

    public static final int RESULT_APPLICATIONS_EDITOR = 2100;

    public ApplicationsDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        /*
        TypedArray applicationsType = context.obtainStyledAttributes(attrs,
                R.styleable.ApplicationsPreference, 0, 0);

        onlyEdit = applicationsType.getInt(R.styleable.ApplicationsPreference_onlyEdit, 0);

        applicationsType.recycle();
        */

        this.context = context;
        dataWrapper = new DataWrapper(context, false, false, 0);

        applicationsList = new ArrayList<Application>();

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

    @Override
    protected void showDialog(Bundle state) {

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .autoDismiss(false)
                .content(getDialogMessage());

        mBuilder.positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText());
        mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                if (shouldPersist())
                {
                    // sem narvi stringy aplikacii oddelenych |
                    value = "";
                    if (applicationsList != null)
                    {
                        for (Application application : applicationsList)
                        {
                            if (!value.isEmpty())
                                value = value + "|";
                            if (application.shortcut)
                                value = value + "(s)";
                            value = value + application.packageName + "/" + application.activityName;
                            if (application.shortcut && (application.shortcutId > 0))
                                value = value + "#" + application.shortcutId;
                        }
                    }
                    persistString(value);

                    setIcons();
                    setSummaryAMSDP();
                }
                mDialog.dismiss();
            }
        });
        mBuilder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                mDialog.dismiss();
            }
        });

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_applications_pref_dialog, null);
        onBindDialogView(layout);

        AppCompatImageButton addButton = (AppCompatImageButton)layout.findViewById(R.id.applications_pref_dlg_add);

        applicationsListView = (ListView) layout.findViewById(R.id.applications_pref_dlg_listview);
        linlaProgress = (LinearLayout)layout.findViewById(R.id.applications_pref_dlg_linla_progress);
        rellaDialog = (RelativeLayout) layout.findViewById(R.id.applications_pref_dlg_rella_dialog);

        listAdapter = new ApplicationsPreferenceAdapter(context, this);

        mBuilder.customView(layout, false);

        mBuilder.showListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ApplicationsDialogPreference.this.onShow(dialog);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditor(-1);
            }
        });

        applicationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                startEditor(position);
            }

        });

        mDialog = mBuilder.build();
        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    public void onShow(DialogInterface dialog) {

        new AsyncTask<Void, Integer, Void>() {

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                rellaDialog.setVisibility(View.GONE);
                linlaProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                if (!EditorProfilesActivity.getApplicationsCache().isCached())
                    EditorProfilesActivity.getApplicationsCache().getApplicationsList(context);

                getValueAMSDP();

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                if (!EditorProfilesActivity.getApplicationsCache().isCached())
                    EditorProfilesActivity.getApplicationsCache().clearCache(false);

                applicationsListView.setAdapter(listAdapter);
                rellaDialog.setVisibility(View.VISIBLE);
                linlaProgress.setVisibility(View.GONE);
            }

        }.execute();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        EditorProfilesActivity.getApplicationsCache().cancelCaching();

        if (!EditorProfilesActivity.getApplicationsCache().isCached())
            EditorProfilesActivity.getApplicationsCache().clearCache(false);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            getValueAMSDP();
        }
        else {
            // set state
            // sem narvi default string aplikacii oddeleny |
            value = "";
            persistString("");
        }
        setSummaryAMSDP();
    }

    private void getValueAMSDP()
    {
        // Get the persistent value
        value = getPersistedString(value);
        //Log.d("ApplicationsDialogPreference.getValueAMSDP","value="+value);

        applicationsList.clear();

        List<Application> cachedApplicationList = EditorProfilesActivity.getApplicationsCache().getList(false);
        if (cachedApplicationList != null)
        {
            String[] splits = value.split("\\|");
            for (Application application : cachedApplicationList)
            {
                application.checked = false;
                for (int i = 0; i < splits.length; i++)
                {
                    String packageName;
                    String activityName;
                    String shortcut;
                    String shortcutId = "";
                    String[] splits2 = splits[i].split("/");
                    if (splits[i].length() > 2) {
                        if (splits2.length == 2) {
                            shortcut = splits2[0].substring(0, 3);
                            packageName = splits2[0];
                            String[] splits3 = splits2[1].split("#");
                            activityName = splits3[0];
                            if (splits3.length == 2)
                                shortcutId = splits3[1];
                        }
                        else {
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

                        //Log.d("ApplicationsDialogPreference.getValueAMSDP","shortcut="+shortcut);
                        //Log.d("ApplicationsDialogPreference.getValueAMSDP","packageName="+packageName);
                        //Log.d("ApplicationsDialogPreference.getValueAMSDP","activityName="+activityName);

                        if (!activityName.isEmpty()) {
                            if (shortcutPassed && packagePassed && activityPassed) {
                                application.checked = true;
                                try {
                                    application.shortcutId = Long.parseLong(shortcutId);
                                } catch (Exception e) {
                                    application.shortcutId = 0;
                                }
                            }
                        }
                        else {
                            if (!shortcut.equals("(s)")) {
                                if (packagePassed && (!application.shortcut))
                                    application.checked = true;
                            }
                        }
                    }
                }
                if (application.checked) {
                    Application newInfo = new Application();

                    newInfo.shortcut = application.shortcut;
                    newInfo.appLabel = application.appLabel;
                    newInfo.packageName = application.packageName;
                    newInfo.activityName = application.activityName;
                    newInfo.icon = application.icon;
                    newInfo.shortcutId = application.shortcutId;

                    //Log.d("ApplicationsDialogPreference.getValueAMSDP","app="+newInfo.appLabel);
                    applicationsList.add(newInfo);
                }
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
                if (!ApplicationsCache.isShortcut(splits[0])) {
                    if (ApplicationsCache.getActivityName(splits[0]).isEmpty()) {
                        ApplicationInfo app;
                        try {
                            app = packageManager.getApplicationInfo(splits[0], 0);
                            if (app != null)
                                prefSummary = packageManager.getApplicationLabel(app).toString();
                        } catch (PackageManager.NameNotFoundException e) {
                            //e.printStackTrace();
                        }
                    }
                    else {
                        Intent intent = new Intent();
                        intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                        ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                        if (info != null)
                            prefSummary = info.loadLabel(packageManager).toString();
                    }
                }
                else {
                    Intent intent = new Intent();
                    intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                    ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                    if (info != null) {
                        long shortcutId = ApplicationsCache.getShortcutId(splits[0]);
                        if (shortcutId > 0) {
                            Shortcut shortcut = dataWrapper.getDatabaseHandler().getShortcut(shortcutId);
                            if (shortcut != null)
                                prefSummary = shortcut._name;
                        }
                        else
                            prefSummary = info.loadLabel(packageManager).toString();
                    }
                }
            }
        }
        setSummary(prefSummary);
    }

    private void setIcons() {
        PackageManager packageManager = context.getPackageManager();
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

    public void showEditMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        Context context = view.getContext();
        PopupMenu popup = new PopupMenu(context, view);
        new MenuInflater(context).inflate(R.menu.applications_pref_dlg_item_edit, popup.getMenu());

        final int position = (int)view.getTag();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.applications_pref_dlg_item_menu_edit:
                        startEditor(position);
                        return true;
                    case R.id.applications_pref_dlg_item_menu_delete:
                        deleteApplication(position);
                        return true;
                    default:
                        return false;
                }
            }
        });


        popup.show();
    }

    private void startEditor(int position) {
        Application application = null;
        if (position > -1)
            application = applicationsList.get(position);
        ApplicationEditorDialog dialog = new ApplicationEditorDialog(context, this, application, position);
        dialog.show();
    }

    private void deleteApplication(int position) {
        Application application = applicationsList.get(position);

        if (application.shortcutId > 0)
            dataWrapper.getDatabaseHandler().deleteShortcut(application.shortcutId);

        applicationsList.remove(position);
        listAdapter.notifyDataSetChanged();
    }

    public void updateApplication(Application application, int position, int positionInCache) {
        List<Application> cachedApplicationList = EditorProfilesActivity.getApplicationsCache().getList(false);
        if (cachedApplicationList != null) {
            int _position = position;
            Application cachedApplication = cachedApplicationList.get(positionInCache);
            Application editedApplication = application;
            if (editedApplication == null) {
                Log.d("ApplicationsDialogPreference.updateApplication", "add");
                editedApplication = new Application();
                applicationsList.add(editedApplication);
                _position = applicationsList.size()-1;
            }
            editedApplication.shortcut = cachedApplication.shortcut;
            editedApplication.appLabel = cachedApplication.appLabel;
            editedApplication.packageName = cachedApplication.packageName;
            editedApplication.activityName = cachedApplication.activityName;
            editedApplication.icon = cachedApplication.icon;
            if (!editedApplication.shortcut)
                editedApplication.shortcutId = 0;

            listAdapter.notifyDataSetChanged();

            if (editedApplication.shortcut) {
                Intent intent = new Intent(context, LaunchShortcutActivity.class);
                intent.putExtra(LaunchShortcutActivity.EXTRA_PACKAGE_NAME, editedApplication.packageName);
                intent.putExtra(LaunchShortcutActivity.EXTRA_ACTIVITY_NAME, editedApplication.activityName);
                intent.putExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_POSITION, _position);

                ProfilePreferencesFragment.setApplicationsDialogPreference(this);
                ProfilePreferencesFragment.getPreferencesActivity().startActivityForResult(intent, RESULT_APPLICATIONS_EDITOR);
            }
        }
    }

    public void updateShortcut(Intent shortcutIntent, String shortcutName,
                               Bitmap shortcutIcon, int position) {
        /* Storing Intent to SQLite ;-)
        You can simply store the intent in a String way:

        String intentDescription = intent.toUri(0);
        //Save the intent string into your database

        Later you can restore the Intent:

        String intentDescription = cursor.getString(intentIndex);
        Intent intent = Intent.parseUri(intentDescription, 0);
        */

        String intentDescription = shortcutIntent.toUri(0);

        Application application = applicationsList.get(position);
        Shortcut shortcut = new Shortcut();
        shortcut._intent = intentDescription;
        shortcut._name = shortcutName;
        if (application.shortcutId > 0) {
            dataWrapper.getDatabaseHandler().deleteShortcut(application.shortcutId);
        }
        dataWrapper.getDatabaseHandler().addShortcut(shortcut);
        application.shortcutId = shortcut._id;

        listAdapter.notifyDataSetChanged();
    }
}
