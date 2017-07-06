package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.andraskindler.quickscroll.QuickScroll;

import java.util.List;

class ApplicationEditorDialog
{

    public ApplicationsDialogPreference preference;
    private List<Application> cachedApplicationList;

    MaterialDialog mDialog;

    Application application;

    ApplicationEditorDialog(Context context, ApplicationsDialogPreference preference,
                                        Application application)
    {
        this.preference = preference;
        this.application = application;

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                .title(R.string.applications_editor_dialog_title)
                //.disableDefaultFonts()
                .autoDismiss(false)
                .customView(R.layout.activity_applications_editor_dialog, false);

        mDialog = dialogBuilder.build();

        ListView listView = (ListView)mDialog.getCustomView().findViewById(R.id.applications_editor_dialog_listview);

        if (EditorProfilesActivity.getApplicationsCache() == null)
            EditorProfilesActivity.createApplicationsCache();

        cachedApplicationList = EditorProfilesActivity.getApplicationsCache().getList(false);

        int position = -1;
        if (application != null) {
            int pos = 0;
            if (cachedApplicationList != null) {
                for (Application _application : cachedApplicationList) {
                    if ((application.shortcut == _application.shortcut) &&
                        application.packageName.equals(_application.packageName) &&
                        application.activityName.equals(_application.activityName)) {
                        position = pos;
                        break;
                    }
                    pos++;
                }
            }
        }

        ApplicationEditorDialogAdapter listAdapter = new ApplicationEditorDialogAdapter(this, context, application, position);
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

        final QuickScroll quickscroll = (QuickScroll) mDialog.getCustomView().findViewById(R.id.applications_editor_dialog_quickscroll);
        quickscroll.init(QuickScroll.TYPE_INDICATOR_WITH_HANDLE, listView, listAdapter, QuickScroll.STYLE_HOLO, colorQSScrollbar);
        quickscroll.setHandlebarColor(colorQSHandlebarInactive, colorQSHandlebarActive, colorQSHandlebarStroke);
        quickscroll.setIndicatorColor(colorQSHandlebarActive, colorQSHandlebarActive, Color.WHITE);
        quickscroll.setFixedSize(1);

        if (position > -1) {
            listView.setSelection(position);
            listView.setItemChecked(position, true);
            listView.smoothScrollToPosition(position);
        }

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                doOnItemSelected(position);
            }

        });

    }

    void doOnItemSelected(int position)
    {
        if (cachedApplicationList != null) {
            preference.updateApplication(application, position);
        }
        mDialog.dismiss();
    }

    public void show() {
        mDialog.show();
    }

}
