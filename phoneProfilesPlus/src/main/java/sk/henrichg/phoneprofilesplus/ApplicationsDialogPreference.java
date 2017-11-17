package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

public class ApplicationsDialogPreference  extends DialogPreference
                                            implements OnStartDragItemListener {

    private final Context context;

    private String value = "";

    List<Application> applicationsList = null;

    private MaterialDialog mDialog;
    private ApplicationEditorDialog mEditorDialog;

    private RecyclerView applicationsListView;
    private ItemTouchHelper itemTouchHelper;

    private LinearLayout linlaProgress;
    private RelativeLayout rellaDialog;

    private ApplicationsDialogPreferenceAdapter listAdapter;

    private ImageView packageIcon;
    private RelativeLayout packageIcons;
    private ImageView packageIcon1;
    private ImageView packageIcon2;
    private ImageView packageIcon3;
    private ImageView packageIcon4;

    private final DataWrapper dataWrapper;

    private AsyncTask asyncTask = null;

    static final int RESULT_APPLICATIONS_EDITOR = 2100;

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

        applicationsList = new ArrayList<>();

        setWidgetLayoutResource(R.layout.applications_preference); // resource na layout custom preference - TextView-ImageView

        if (EditorProfilesActivity.getApplicationsCache() == null)
            EditorProfilesActivity.createApplicationsCache();

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

    @Override
    protected void showDialog(Bundle state) {

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .autoDismiss(false)
                .content(getDialogMessage())
                .customView(R.layout.activity_applications_pref_dialog, false);

        mBuilder.positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText());
        mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @SuppressWarnings("StringConcatenationInLoop")
            @Override
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                if (shouldPersist())
                {
                    // fill with application strings separated with |
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
                            value = value + "#" + application.startApplicationDelay;
                            PPApplication.logE("ApplicationsDialogPreference.onPositive","value="+value);
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
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                mDialog.dismiss();
            }
        });

        mBuilder.showListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ApplicationsDialogPreference.this.onShow(/*dialog*/);
            }
        });

        mDialog = mBuilder.build();
        View layout = mDialog.getCustomView();

        AppCompatImageButton addButton = layout.findViewById(R.id.applications_pref_dlg_add);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        applicationsListView = layout.findViewById(R.id.applications_pref_dlg_listview);
        //applicationsListView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        applicationsListView.setLayoutManager(layoutManager);
        applicationsListView.setHasFixedSize(true);

        linlaProgress = layout.findViewById(R.id.applications_pref_dlg_linla_progress);
        rellaDialog = layout.findViewById(R.id.applications_pref_dlg_rella_dialog);

        listAdapter = new ApplicationsDialogPreferenceAdapter(context, this, this);

        // added touch helper for drag and drop items
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(listAdapter, false, false);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(applicationsListView);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditor(null);
            }
        });

        MaterialDialogsPrefUtil.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    private void onShow(/*DialogInterface dialog*/) {

        asyncTask = new AsyncTask<Void, Integer, Void>() {

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
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);

        if ((asyncTask != null) && !asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            asyncTask.cancel(true);
        }

        EditorProfilesActivity.getApplicationsCache().cancelCaching();
        if (!EditorProfilesActivity.getApplicationsCache().isCached())
            EditorProfilesActivity.getApplicationsCache().clearCache(false);
        MaterialDialogsPrefUtil.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mEditorDialog != null && mEditorDialog.mDialog != null && mEditorDialog.mDialog.isShowing())
            mEditorDialog.mDialog.dismiss();
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
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
            value = "";
            persistString("");
        }
        setSummaryAMSDP();
    }

    private void getValueAMSDP()
    {
        // Get the persistent value
        value = getPersistedString(value);

        PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP","value="+value);

        applicationsList.clear();

        List<Application> cachedApplicationList = EditorProfilesActivity.getApplicationsCache().getList(false);
        if (cachedApplicationList != null)
        {
            String[] splits = value.split("\\|");
            for (String split : splits) {
                Application _application = null;
                for (Application application : cachedApplicationList) {
                    application.checked = false;

                    String packageName;
                    String activityName;
                    String shortcut;
                    String shortcutId = "";
                    String startApplicationDelay = "0";
                    String[] splits2 = split.split("/"); // activity
                    if (split.length() > 2) {
                        if (splits2.length == 2) {
                            // activity exists
                            shortcut = splits2[0].substring(0, 3);
                            packageName = splits2[0];
                            String[] splits4 = splits2[1].split("#"); // shortcut id, startApplicationDelay
                            activityName = splits4[0];
                            if (shortcut.equals("(s)")) {
                                if (splits4.length >= 2)
                                    shortcutId = splits4[1];
                                if (splits4.length >= 3)
                                    startApplicationDelay = splits4[2];
                            }
                            else {
                                if (splits4.length >= 2)
                                    startApplicationDelay = splits4[1];
                            }
                        } else {
                            // activity not exists
                            shortcut = split.substring(0, 3);
                            String[] splits4 = split.split("#"); // startApplicationDelay
                            if (splits4.length >= 2) {
                                packageName = splits4[0];
                                startApplicationDelay = splits4[1];
                            }
                            else {
                                packageName = split;
                            }
                            activityName = "";
                        }
                        if (shortcut.equals("(s)"))
                            packageName = packageName.substring(3);
                        else
                            shortcut = "";

                        boolean shortcutPassed = shortcut.equals("(s)") == application.shortcut;
                        boolean packagePassed = packageName.equals(application.packageName);
                        boolean activityPassed = activityName.equals(application.activityName);

                        if (!activityName.isEmpty()) {
                            if (shortcutPassed && packagePassed && activityPassed) {
                                application.checked = true;
                                try {
                                    application.shortcutId = Long.parseLong(shortcutId);
                                } catch (Exception e) {
                                    application.shortcutId = 0;
                                }
                            }
                        } else {
                            if (!shortcut.equals("(s)")) {
                                if (packagePassed && (!application.shortcut))
                                    application.checked = true;
                            }
                        }
                        _application = application;
                        try {
                            _application.startApplicationDelay = Integer.parseInt(startApplicationDelay);
                        } catch (Exception e) {
                            _application.startApplicationDelay = 0;
                        }

                        PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP","packageName="+packageName);
                        PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP","activityName="+activityName);
                        PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP","shortcut="+shortcut);
                        PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP","shortcutId="+shortcutId);
                        PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP","startApplicationDelay="+startApplicationDelay);
                        PPApplication.logE("ApplicationsDialogPreference.getValueAMSDP","checked="+_application.checked);

                        if (_application.checked)
                            break;
                    }
                }
                if ((_application != null) && _application.checked) {
                    Application newInfo = new Application();

                    newInfo.shortcut = _application.shortcut;
                    newInfo.appLabel = _application.appLabel;
                    newInfo.packageName = _application.packageName;
                    newInfo.activityName = _application.activityName;
                    newInfo.shortcutId = _application.shortcutId;
                    newInfo.startApplicationDelay = _application.startApplicationDelay;

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
                        } catch (Exception e) {
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

        if (!value.isEmpty() && !value.equals("-")) {
            if (splits.length == 1) {
                packageIcon.setVisibility(View.VISIBLE);
                packageIcon1.setImageResource(R.drawable.ic_empty);
                packageIcon2.setImageResource(R.drawable.ic_empty);
                packageIcon3.setImageResource(R.drawable.ic_empty);
                packageIcon4.setImageResource(R.drawable.ic_empty);
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
                    } else {
                        Intent intent = new Intent();
                        intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                        ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                        if (info != null)
                            packageIcon.setImageDrawable(info.loadIcon(packageManager));
                        else
                            packageIcon.setImageResource(R.drawable.ic_empty);
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                    ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                    if (info != null)
                        packageIcon.setImageDrawable(info.loadIcon(packageManager));
                    else
                        packageIcon.setImageResource(R.drawable.ic_empty);
                }
            } else {
                packageIcons.setVisibility(View.VISIBLE);
                packageIcon.setVisibility(View.GONE);
                packageIcon.setImageResource(R.drawable.ic_empty);

                ImageView packIcon = packageIcon1;
                for (int i = 0; i < 4; i++) {
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
                            } else {
                                Intent intent = new Intent();
                                intent.setClassName(ApplicationsCache.getPackageName(splits[i]), ApplicationsCache.getActivityName(splits[i]));
                                ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);

                                if (info != null) {
                                    packIcon.setImageDrawable(info.loadIcon(packageManager));
                                } else {
                                    packIcon.setImageResource(R.drawable.ic_empty);
                                }
                            }
                        } else {
                            Intent intent = new Intent();
                            intent.setClassName(ApplicationsCache.getPackageName(splits[i]), ApplicationsCache.getActivityName(splits[i]));
                            ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);

                            if (info != null) {
                                packIcon.setImageDrawable(info.loadIcon(packageManager));
                            } else {
                                packIcon.setImageResource(R.drawable.ic_empty);
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
        Context context = view.getContext();
        PopupMenu popup;
        if (android.os.Build.VERSION.SDK_INT >= 19)
            popup = new PopupMenu(context, view, Gravity.END);
        else
            popup = new PopupMenu(context, view);
        new MenuInflater(context).inflate(R.menu.applications_pref_dlg_item_edit, popup.getMenu());

        final Application application = (Application) view.getTag();

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

        popup.show();
    }

    void startEditor(Application application) {
        mEditorDialog = new ApplicationEditorDialog(context, this, application);
        mEditorDialog.show();
    }

    private void deleteApplication(Application application) {

        if (application.shortcutId > 0)
            dataWrapper.getDatabaseHandler().deleteShortcut(application.shortcutId);

        applicationsList.remove(application);
        applicationsListView.getRecycledViewPool().clear();
        listAdapter.notifyDataSetChanged();
    }

    void updateApplication(Application application, int positionInEditor, int startApplicationDelay) {
        List<Application> cachedApplicationList = EditorProfilesActivity.getApplicationsCache().getList(false);
        if (cachedApplicationList != null) {
            int _position = applicationsList.indexOf(application);
            Application cachedApplication = cachedApplicationList.get(positionInEditor);
            Application editedApplication = application;
            if (editedApplication == null) {
                editedApplication = new Application();
                applicationsList.add(editedApplication);
                _position = applicationsList.size()-1;
            }
            editedApplication.shortcut = cachedApplication.shortcut;
            editedApplication.appLabel = cachedApplication.appLabel;
            editedApplication.packageName = cachedApplication.packageName;
            editedApplication.activityName = cachedApplication.activityName;
            if (!editedApplication.shortcut)
                editedApplication.shortcutId = 0;
            editedApplication.startApplicationDelay = startApplicationDelay;

            applicationsListView.getRecycledViewPool().clear();
            listAdapter.notifyDataSetChanged();

            if (editedApplication.shortcut &&
                (editedApplication.packageName != null)) {
                Intent intent = new Intent(context, LaunchShortcutActivity.class);
                intent.putExtra(LaunchShortcutActivity.EXTRA_PACKAGE_NAME, editedApplication.packageName);
                intent.putExtra(LaunchShortcutActivity.EXTRA_ACTIVITY_NAME, editedApplication.activityName);
                intent.putExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_POSITION, _position);
                intent.putExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, startApplicationDelay);

                ProfilePreferencesFragment.setApplicationsDialogPreference(this);
                ((Activity)context).startActivityForResult(intent, RESULT_APPLICATIONS_EDITOR);
            }
        }
    }

    void updateShortcut(Intent shortcutIntent, String shortcutName, int position, int startApplicationDelay) {
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
        application.startApplicationDelay = startApplicationDelay;

        applicationsListView.getRecycledViewPool().clear();
        listAdapter.notifyDataSetChanged();
    }
}
