package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialog;

class ApplicationEditorDialog
{

    private final ApplicationsDialogPreference preference;
    private final ApplicationEditorDialogAdapter listAdapter;
    private final Activity activity;

    final AlertDialog mDialog;
    private final TextView mDelayValue;
    private final TimeDurationPickerDialog mDelayValueDialog;
    private final ImageView mSelectedAppIcon;
    private final TextView mSelectedAppName;
    private final AppCompatImageButton addButton;


    private final List<Application> cachedApplicationList;
    final List<Application> applicationList;

    private final Application editedApplication;

    private Application selectedApplication;
    private int startApplicationDelay = 0;

    private final String[] filterValues;

    int selectedPosition = -1;
    int selectedFilter = 0;

    private FastScrollRecyclerView listView;

    static final int RESULT_INTENT_EDITOR = 3100;
    static final String EXTRA_APPLICATION = "application";
    static final String EXTRA_PP_INTENT = "ppIntent";

    ApplicationEditorDialog(Activity activity, ApplicationsDialogPreference preference,
                            final Application application)
    {
        this.preference = preference;
        this.activity = activity;

        this.editedApplication = application;
        this.selectedApplication = application;
        if (editedApplication != null)
            startApplicationDelay = editedApplication.startApplicationDelay;

        applicationList = new ArrayList<>();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.applications_editor_dialog_title);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if (cachedApplicationList != null) {
                    ApplicationEditorDialog.this.preference.updateApplication(editedApplication, selectedApplication, startApplicationDelay);
                //}
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = activity.getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_applications_editor_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDelayValue = layout.findViewById(R.id.applications_editor_dialog_startApplicationDelay);
        mDelayValue.setText(GlobalGUIRoutines.getDurationString(startApplicationDelay));

        mSelectedAppIcon = layout.findViewById(R.id.applications_editor_dialog_selectedIcon);
        mSelectedAppName = layout.findViewById(R.id.applications_editor_dialog_selectedAppName);

        LinearLayout delayValueRoot = layout.findViewById(R.id.applications_editor_dialog_startApplicationDelay_root);
        mDelayValueDialog = new TimeDurationPickerDialog(activity, new TimeDurationPickerDialog.OnDurationSetListener() {
            @Override
            public void onDurationSet(TimeDurationPicker view, long duration) {
                int iValue = (int) duration / 1000;

                if (iValue < 0)
                    iValue = 0;
                if (iValue > 86400)
                    iValue = 86400;

                mDelayValue.setText(GlobalGUIRoutines.getDurationString(iValue));

                startApplicationDelay = iValue;
            }
        }, startApplicationDelay * 1000, TimeDurationPicker.HH_MM_SS);
        delayValueRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDelayValueDialog.setDuration(startApplicationDelay * 1000);
                    if (!ApplicationEditorDialog.this.activity.isFinishing())
                            mDelayValueDialog.show();
                }
            }
        );

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (selectedPosition == -1) {
                    Button positive = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (positive != null)
                        positive.setEnabled(false);
                }
            }
        });

        Spinner filterSpinner = layout.findViewById(R.id.applications_editor_dialog_filter_spinner);
        switch (ApplicationPreferences.applicationTheme(activity, true)) {
            case "dark":
                filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_dark);
                break;
            case "white":
                filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_white);
                break;
            case "dlight":
                filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_dlight);
                break;
            default:
                filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_color);
                break;
        }
        filterValues= activity.getResources().getStringArray(R.array.applicationsEditorDialogFilterValues);

        if (editedApplication != null) {
            switch (editedApplication.type) {
                case Application.TYPE_APPLICATION:
                    selectedFilter = 0;
                    break;
                case Application.TYPE_SHORTCUT:
                    selectedFilter = 1;
                    break;
                case Application.TYPE_INTENT:
                    selectedFilter = 2;
                    break;
            }
        }

        filterSpinner.setSelection(Arrays.asList(filterValues).indexOf(String.valueOf(selectedFilter)));
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFilter = Integer.valueOf(filterValues[position]);
                if (selectedFilter == 2)
                    addButton.setVisibility(View.VISIBLE);
                else
                    addButton.setVisibility(View.GONE);

                fillApplicationList();
                listView.getRecycledViewPool().clear();
                listView.setAdapter(null);
                listView.setAdapter(listAdapter);
                listAdapter.notifyDataSetChanged();
                //listView.setAdapter(listAdapter);

                RecyclerView.LayoutManager lm = listView.getLayoutManager();
                if (lm != null) {
                    if (selectedPosition > -1)
                        lm.scrollToPosition(selectedPosition);
                    else
                        lm.scrollToPosition(0);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        addButton  = layout.findViewById(R.id.applications_editor_dialog_addIntent);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startEditor(null);
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        listView = layout.findViewById(R.id.applications_editor_dialog_listview);
        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        if (EditorProfilesActivity.getApplicationsCache() == null)
            EditorProfilesActivity.createApplicationsCache();

        cachedApplicationList = EditorProfilesActivity.getApplicationsCache().getApplicationList(false);

        fillApplicationList();
        updateSelectedAppViews();

        listAdapter = new ApplicationEditorDialogAdapter(this);
        listView.setAdapter(listAdapter);

        if (selectedPosition > -1) {
            RecyclerView.LayoutManager lm = listView.getLayoutManager();
            if (lm != null)
                lm.scrollToPosition(selectedPosition);
        }
    }

    private void fillApplicationList() {
        applicationList.clear();
        selectedPosition = -1;
        int pos = 0;

        PPApplication.logE("ApplicationEditorDialog.fillApplicationList", "selectedFilter="+selectedFilter);

        if (selectedFilter == 2) {
            if (preference.intentDBList != null) {
                for (PPIntent ppIntent : preference.intentDBList) {
                    Application _application = new Application();
                    _application.type = Application.TYPE_INTENT;
                    _application.intentId = ppIntent._id;
                    _application.appLabel = ppIntent._name;
                    if (selectedApplication != null) {
                        if (selectedApplication.type == Application.TYPE_INTENT) {
                            if (selectedApplication.intentId == _application.intentId)
                                selectedPosition = pos;
                        }
                    }
                    PPApplication.logE("ApplicationEditorDialog.fillApplicationList", "_application.intentId="+_application.intentId);
                    PPApplication.logE("ApplicationEditorDialog.fillApplicationList", "_application.appLabel="+_application.appLabel);
                    applicationList.add(_application);
                    pos++;
                }
            }
            return;
        }

        if (cachedApplicationList != null) {
            for (Application _application : cachedApplicationList) {
                boolean add = false;
                if ((selectedFilter == 0) && (_application.type == Application.TYPE_APPLICATION))
                    add = true;
                if ((selectedFilter == 1) && (_application.type == Application.TYPE_SHORTCUT))
                    add = true;
                if (add) {
                    if (selectedApplication != null) {
                        switch (selectedApplication.type) {
                            case Application.TYPE_APPLICATION:
                                if (selectedApplication.packageName.equals(_application.packageName))
                                    selectedPosition = pos;
                                break;
                            case Application.TYPE_SHORTCUT:
                                if (selectedApplication.packageName.equals(_application.packageName) &&
                                        selectedApplication.activityName.equals(_application.activityName))
                                    selectedPosition = pos;
                                break;
                            case Application.TYPE_INTENT:
                                if (selectedApplication.intentId == _application.intentId)
                                    selectedPosition = pos;
                                break;
                        }
                    }
                    applicationList.add(_application);
                    pos++;
                }
            }
        }
    }

    private Application getSelectedApplication() {
        if (selectedFilter == 2) {
            if (preference.intentDBList != null) {
                int pos = 0;
                for (PPIntent ppIntent : preference.intentDBList) {
                    if (pos == selectedPosition) {
                        Application _application = new Application();
                        _application.type = Application.TYPE_INTENT;
                        _application.intentId = ppIntent._id;
                        _application.appLabel = ppIntent._name;

                        return  _application;
                    }
                    pos++;
                }
            }
        }

        if (cachedApplicationList != null) {
            // search filtered application in cachedApplicationList
            int pos = 0;
            for (Application _application : cachedApplicationList) {
                boolean search = false;
                if ((selectedFilter == 0) && (_application.type == Application.TYPE_APPLICATION))
                    search = true;
                if ((selectedFilter == 1) && (_application.type == Application.TYPE_SHORTCUT))
                    search = true;
                if (search) {
                    if (pos == selectedPosition) {
                        return  _application;
                    }
                    pos++;
                }
            }
        }
        return null;
    }

    private void updateSelectedAppViews() {
        Bitmap applicationIcon = null;
        if (selectedPosition != -1) {
            selectedApplication = getSelectedApplication();
            if (selectedApplication != null) {
                applicationIcon = EditorProfilesActivity.getApplicationsCache().getApplicationIcon(selectedApplication, false);
            }
        }
        if (selectedApplication != null) {
            if (applicationIcon != null) {
                mSelectedAppIcon.setImageBitmap(applicationIcon);
                mSelectedAppIcon.setVisibility(View.VISIBLE);
            }
            else
                mSelectedAppIcon.setVisibility(View.GONE);
            String appName = "";
            switch (selectedApplication.type) {
                case Application.TYPE_APPLICATION:
                    appName = "(A) ";
                    break;
                case Application.TYPE_SHORTCUT:
                    appName = "(S) ";
                    break;
                case Application.TYPE_INTENT:
                    appName = "(I) ";
                    break;
            }
            appName = appName + selectedApplication.appLabel;
            mSelectedAppName.setText(appName);
        }
        else {
            mSelectedAppIcon.setVisibility(View.GONE);
            mSelectedAppName.setText(R.string.applications_editor_dialog_not_selected);
        }
    }

    void doOnItemSelected(int position)
    {
        if (position != -1) {
            Button positive = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positive.setEnabled(true);


            Application application = applicationList.get(position);

            if ((application != null) && (application.type == Application.TYPE_INTENT)) {
                if (selectedPosition != -1) {
                    PPIntent ppIntent = preference.intentDBList.get(selectedPosition);
                    if (ppIntent != null)
                        --ppIntent._usedCount;
                }
                PPIntent ppIntent = preference.intentDBList.get(position);
                if (ppIntent != null)
                    ++ppIntent._usedCount;
            }

            selectedPosition = position;

            listAdapter.notifyDataSetChanged();

            updateSelectedAppViews();
        }
    }

    void showEditMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        Context context = view.getContext();
        PopupMenu popup;
        //if (android.os.Build.VERSION.SDK_INT >= 19)
        popup = new PopupMenu(context, view, Gravity.END);
        //else
        //    popup = new PopupMenu(context, view);

        int position = (int) view.getTag();
        final Application application = applicationList.get(position);

        boolean canDelete = true;
        if ((application != null) && (application.type == Application.TYPE_INTENT)) {
            for (PPIntent ppIntent : preference.intentDBList) {
                if (ppIntent._id == application.intentId) {
                    canDelete = ppIntent._usedCount == 0;
                    break;
                }
            }
        }
        if (canDelete)
            new MenuInflater(context).inflate(R.menu.applications_intent_editor_dlg_item_edit, popup.getMenu());
        else
            new MenuInflater(context).inflate(R.menu.applications_intent_editor_dlg_item_edit_no_delete, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.applications_intent_editor_dlg_item_menu_edit:
                        startEditor(application);
                        return true;
                    case R.id.applications_intent_editor_dlg_item_menu_duplicate:
                        Application newApplication = duplicateIntent(application);
                        startEditor(newApplication);
                        return true;
                    case R.id.applications_intent_editor_dlg_item_menu_delete:
                        deleteIntent(application);
                        return true;
                    default:
                        return false;
                }
            }
        });

        popup.show();
    }

    private void startEditor(Application application) {
        Intent intent = new Intent(activity, ApplicationEditorIntentActivity.class);
        intent.putExtra(EXTRA_APPLICATION, application);
        PPIntent ppIntent;
        if ((application != null) && application.intentId > 0)
            ppIntent = DatabaseHandler.getInstance(preference.context.getApplicationContext()).getIntent(application.intentId);
        else
            ppIntent = new PPIntent();
        intent.putExtra(EXTRA_PP_INTENT, ppIntent);
        intent.putExtra(ApplicationEditorIntentActivity.EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, startApplicationDelay);

        activity.startActivityForResult(intent, RESULT_INTENT_EDITOR);
    }

    void updateAfterEdit() {
        fillApplicationList();
        listView.getRecycledViewPool().clear();
        listAdapter.notifyDataSetChanged();
    }

    private Application duplicateIntent(Application originalApplication) {
        if (originalApplication == null)
            return null;

        Application newApplication = null;
        PPIntent newPPIntent = null;

        if (preference.intentDBList != null) {
            for (PPIntent ppIntent : preference.intentDBList) {
                if (ppIntent._id == originalApplication.intentId) {
                    newPPIntent = ppIntent.duplicate();
                    break;
                }
            }
        }

        if (newPPIntent != null) {
            DatabaseHandler.getInstance(preference.context.getApplicationContext()).addIntent(newPPIntent);

            preference.intentDBList.add(newPPIntent);

            fillApplicationList();

            for (Application application : applicationList) {
                if (application.intentId == newPPIntent._id) {
                    newApplication = application;
                    break;
                }
            }

            listView.getRecycledViewPool().clear();
            listAdapter.notifyDataSetChanged();
        }

        return newApplication;
    }

    private void deleteIntent(Application application) {
        if (application == null)
            return;

        DatabaseHandler.getInstance(preference.context.getApplicationContext()).deleteIntent(application.intentId);

        if (preference.intentDBList != null) {
            for (PPIntent ppIntent : preference.intentDBList) {
                if (ppIntent._id == application.intentId) {
                    preference.intentDBList.remove(ppIntent);
                    break;
                }
            }
        }

        applicationList.remove(application);

        int position = applicationList.indexOf(application);

        if (position == selectedPosition) {
            Button positive = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positive.setEnabled(false);
            selectedPosition = -1;
            updateSelectedAppViews();
            if (editedApplication != null)
                editedApplication.intentId = 0;
        }

        listView.getRecycledViewPool().clear();
        listAdapter.notifyDataSetChanged();

        ApplicationEditorDialog.this.preference.updateGUI();
    }

    public void show() {
        if (!activity.isFinishing())
            mDialog.show();
    }

}
