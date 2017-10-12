package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.andraskindler.quickscroll.QuickScroll;

import java.util.List;

import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialog;

class ApplicationEditorDialog
{

    private final ApplicationsDialogPreference preference;
    private List<Application> cachedApplicationList;
    private final ApplicationEditorDialogAdapter listAdapter;

    final MaterialDialog mDialog;
    private final TextView mDelayValue;
    private final TimeDurationPickerDialog mDelayValueDialog;

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
        View layout = mDialog.getCustomView();

        mDelayValue = layout.findViewById(R.id.applications_editor_dialog_startApplicationDelay);
        mDelayValue.setText(GlobalGUIRoutines.getDurationString(startApplicationDelay));

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

        mDelayValue.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 mDelayValueDialog.setDuration(startApplicationDelay * 1000);
                 mDelayValueDialog.show();
             }
        });

        TextView mValueSpinnerChar = layout.findViewById(R.id.applications_editor_dialog_startApplicationDelay_spinnerChar);
        mValueSpinnerChar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDelayValueDialog.setDuration(startApplicationDelay * 1000);
                mDelayValueDialog.show();
            }
        });

        ListView listView = layout.findViewById(R.id.applications_editor_dialog_listview);

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
            View positive = mDialog.getActionButton(DialogAction.POSITIVE);
            positive.setEnabled(false);
        }

        listAdapter = new ApplicationEditorDialogAdapter(this, context);
        listView.setAdapter(listAdapter);

        TypedValue tv = new TypedValue();
        preference.getContext().getTheme().resolveAttribute(R.attr.colorQSScrollbar, tv, true);
        int colorQSScrollbar = tv.data;
        preference.getContext().getTheme().resolveAttribute(R.attr.colorQSHandlebarInactive, tv, true);
        int colorQSHandlebarInactive = tv.data;
        preference.getContext().getTheme().resolveAttribute(R.attr.colorQSHandlebarActive, tv, true);
        int colorQSHandlebarActive = tv.data;
        preference.getContext().getTheme().resolveAttribute(R.attr.colorQSHandlebarStroke, tv, true);
        int colorQSHandlebarStroke = tv.data;

        final QuickScroll quickscroll = layout.findViewById(R.id.applications_editor_dialog_quickscroll);
        quickscroll.init(QuickScroll.TYPE_INDICATOR_WITH_HANDLE, listView, listAdapter, QuickScroll.STYLE_HOLO, colorQSScrollbar);
        quickscroll.setHandlebarColor(colorQSHandlebarInactive, colorQSHandlebarActive, colorQSHandlebarStroke);
        quickscroll.setIndicatorColor(colorQSHandlebarActive, colorQSHandlebarActive, Color.WHITE);
        quickscroll.setFixedSize(1);

        if (selectedPosition > -1) {
            listView.setSelection(selectedPosition);
        }

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
                ApplicationEditorViewHolder viewHolder = (ApplicationEditorViewHolder) item.getTag();
                doOnItemSelected(position);
                viewHolder.radioBtn.setChecked(true);
            }

        });

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
