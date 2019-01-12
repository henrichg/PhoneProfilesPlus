package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialog;

class ApplicationEditorDialog
{

    private final ApplicationsDialogPreference preference;
    private final ApplicationEditorDialogAdapter listAdapter;

    final AlertDialog mDialog;
    private final TextView mDelayValue;
    private final TimeDurationPickerDialog mDelayValueDialog;
    private final ImageView mSelectedAppIcon;
    private final TextView mSelectedAppName;

    List<Application> cachedApplicationList;
    final List<Application> applicationList;

    private final Application editedApplication;

    private Application selectedApplication;
    private int startApplicationDelay = 0;

    private final String[] filterValues;

    int selectedPosition = -1;
    private int selectedFilter = 0;

    FastScrollRecyclerView listView;

    ApplicationEditorDialog(Activity activity, ApplicationsDialogPreference preference,
                            final Application application)
    {
        this.preference = preference;

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
                if (cachedApplicationList != null) {
                    ApplicationEditorDialog.this.preference.updateApplication(editedApplication, selectedApplication, startApplicationDelay);
                }
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = activity.getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_applications_editor_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        //noinspection ConstantConditions
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
        switch (ApplicationPreferences.applicationTheme(activity)) {
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
            if (!editedApplication.shortcut)
                selectedFilter = 0;
            if (editedApplication.shortcut)
                selectedFilter = 1;
        }

        filterSpinner.setSelection(Arrays.asList(filterValues).indexOf(String.valueOf(selectedFilter)));
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFilter = Integer.valueOf(filterValues[position]);
                fillApplicationList();
                listAdapter.notifyDataSetChanged();

                if (selectedPosition > -1) {
                    RecyclerView.LayoutManager lm = listView.getLayoutManager();
                    if (lm != null)
                        lm.scrollToPosition(selectedPosition);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        //noinspection ConstantConditions
        listView = layout.findViewById(R.id.applications_editor_dialog_listview);
        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        if (EditorProfilesActivity.getApplicationsCache() == null)
            EditorProfilesActivity.createApplicationsCache();

        cachedApplicationList = EditorProfilesActivity.getApplicationsCache().getList(false);

        fillApplicationList();
        updateSelectedAppViews();

        listAdapter = new ApplicationEditorDialogAdapter(this, activity);
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
        if (cachedApplicationList != null) {
            for (Application _application : cachedApplicationList) {
                boolean add = false;
                if ((selectedFilter == 0) && (!_application.shortcut))
                    add = true;
                if ((selectedFilter == 1) && (_application.shortcut))
                    add = true;
                if (add) {
                    if (selectedApplication != null) {
                        if ((selectedApplication.shortcut == _application.shortcut) &&
                                selectedApplication.packageName.equals(_application.packageName) &&
                                selectedApplication.activityName.equals(_application.activityName)) {
                            selectedPosition = pos;
                        }
                    }
                    applicationList.add(_application);
                    pos++;
                }
            }
        }
    }

    private Application getSelectedApplication() {
        if (EditorProfilesActivity.getApplicationsCache() != null) {
            List<Application> cachedApplicationList = EditorProfilesActivity.getApplicationsCache().getList(false);
            if (cachedApplicationList != null) {
                // search filtered application in cachedApplicationList
                int pos = 0;
                for (Application _application : cachedApplicationList) {
                    boolean search = false;
                    if ((selectedFilter == 0) && (!_application.shortcut))
                        search = true;
                    if ((selectedFilter == 1) && (_application.shortcut))
                        search = true;
                    if (search) {
                        if (pos == selectedPosition) {
                            return  _application;
                        }
                        pos++;
                    }
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
            if (applicationIcon != null)
                mSelectedAppIcon.setImageBitmap(applicationIcon);
            else
                mSelectedAppIcon.setImageResource(R.drawable.ic_empty);
            String appName;
            if (selectedApplication.shortcut)
                appName = "(S) ";
            else
                appName = "(A) ";
            appName = appName + selectedApplication.appLabel;
            mSelectedAppName.setText(appName);
        }
        else {
            mSelectedAppIcon.setImageResource(R.drawable.ic_empty);
            mSelectedAppName.setText(R.string.empty_string);
        }
    }

    void doOnItemSelected(int position)
    {
        if (position != -1) {
            Button positive = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positive.setEnabled(true);

            selectedPosition = position;
            listAdapter.notifyDataSetChanged();

            updateSelectedAppViews();
        }
    }

    public void show() {
        mDialog.show();
    }

}
