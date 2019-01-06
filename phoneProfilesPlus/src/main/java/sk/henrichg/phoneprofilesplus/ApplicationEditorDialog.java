package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

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

    List<Application> cachedApplicationList;
    private final Application mApplication;

    int selectedPosition;
    private int startApplicationDelay = 0;

    ApplicationEditorDialog(Activity activity, ApplicationsDialogPreference preference,
                            final Application application)
    {
        this.preference = preference;
        this.mApplication = application;
        if (mApplication != null)
            startApplicationDelay = mApplication.startApplicationDelay;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.applications_editor_dialog_title);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (cachedApplicationList != null) {
                    ApplicationEditorDialog.this.preference.updateApplication(mApplication, selectedPosition, startApplicationDelay);
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

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        //noinspection ConstantConditions
        FastScrollRecyclerView listView = layout.findViewById(R.id.applications_editor_dialog_listview);
        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        if (EditorProfilesActivity.getApplicationsCache() == null)
            EditorProfilesActivity.createApplicationsCache();

        cachedApplicationList = EditorProfilesActivity.getApplicationsCache().getList(false);

        selectedPosition = -1;
        if (mApplication != null) {
            int pos = 0;
            if (cachedApplicationList != null) {
                for (Application _application : cachedApplicationList) {
                    if ((mApplication.shortcut == _application.shortcut) &&
                        mApplication.packageName.equals(_application.packageName) &&
                        mApplication.activityName.equals(_application.activityName)) {
                        selectedPosition = pos;
                        break;
                    }
                    pos++;
                }
            }
        }

        listAdapter = new ApplicationEditorDialogAdapter(this, activity);
        listView.setAdapter(listAdapter);

        if (selectedPosition > -1) {
            RecyclerView.LayoutManager lm = listView.getLayoutManager();
            if (lm != null)
                lm.scrollToPosition(selectedPosition);
        }
    }

    void doOnItemSelected(int position)
    {
        if (position != -1) {
            Button positive = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positive.setEnabled(true);

            selectedPosition = position;
            listAdapter.notifyDataSetChanged();
        }
    }

    public void show() {
        mDialog.show();
    }

}
