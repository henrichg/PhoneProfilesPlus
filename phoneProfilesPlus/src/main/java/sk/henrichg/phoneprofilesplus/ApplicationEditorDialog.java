package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialog;

class ApplicationEditorDialog
{

    private final ApplicationsDialogPreference preference;
    private final ApplicationEditorDialogAdapter listAdapter;

    final MaterialDialog mDialog;
    private final TextView mDelayValue;
    private final TimeDurationPickerDialog mDelayValueDialog;

    List<Application> cachedApplicationList;
    private final Application mApplication;

    int selectedPosition;
    private int startApplicationDelay = 0;

    ApplicationEditorDialog(Context context, ApplicationsDialogPreference preference,
                            final Application application)
    {
        this.preference = preference;
        this.mApplication = application;
        if (mApplication != null)
            startApplicationDelay = mApplication.startApplicationDelay;

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                .title(R.string.applications_editor_dialog_title)
                //.disableDefaultFonts()
                .customView(R.layout.activity_applications_editor_dialog, false)
                .dividerColor(0)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (cachedApplicationList != null) {
                            ApplicationEditorDialog.this.preference.updateApplication(mApplication, selectedPosition, startApplicationDelay);
                        }
                    }
                });

        mDialog = dialogBuilder.build();

        /*
        MDButton negative = mDialog.getActionButton(DialogAction.NEGATIVE);
        if (negative != null) negative.setAllCaps(false);
        MDButton  neutral = mDialog.getActionButton(DialogAction.NEUTRAL);
        if (neutral != null) neutral.setAllCaps(false);
        MDButton  positive = mDialog.getActionButton(DialogAction.POSITIVE);
        if (positive != null) positive.setAllCaps(false);
        */

        View layout = mDialog.getCustomView();

        //noinspection ConstantConditions
        mDelayValue = layout.findViewById(R.id.applications_editor_dialog_startApplicationDelay);
        mDelayValue.setText(GlobalGUIRoutines.getDurationString(startApplicationDelay));

        LinearLayout delayValueRoot = layout.findViewById(R.id.applications_editor_dialog_startApplicationDelay_root);
        mDelayValueDialog = new TimeDurationPickerDialog(context, new TimeDurationPickerDialog.OnDurationSetListener() {
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

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
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

        if (selectedPosition == -1) {
            MDButton  positive = mDialog.getActionButton(DialogAction.POSITIVE);
            if (positive != null)
                positive.setEnabled(false);
        }

        listAdapter = new ApplicationEditorDialogAdapter(this, context);
        listView.setAdapter(listAdapter);

        if (selectedPosition > -1) {
            listView.getLayoutManager().scrollToPosition(selectedPosition);
        }
    }

    void doOnItemSelected(int position)
    {
        View positive = mDialog.getActionButton(DialogAction.POSITIVE);
        positive.setEnabled(true);

        selectedPosition = position;
        listAdapter.notifyDataSetChanged();
    }

    public void show() {
        mDialog.show();
    }

}
