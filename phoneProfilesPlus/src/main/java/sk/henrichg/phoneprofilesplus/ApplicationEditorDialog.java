package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

public class ApplicationEditorDialog
{

    public ApplicationsDialogPreference preference;
    private ApplicationEditorDialogAdapter listAdapter;
    private List<Application> cachedApplicationList;

    private Context _context;

    private MaterialDialog mDialog;
    private ListView listView;

    Application application;
    int dialogPrefPosition;

    public ApplicationEditorDialog(Context context, ApplicationsDialogPreference preference,
                                        Application application, int dialogPrefPosition)
    {
        this.preference = preference;
        this.application = application;
        this.dialogPrefPosition = dialogPrefPosition;

        _context = context;

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                .title(R.string.applications_editor_dialog_title)
                //.disableDefaultFonts()
                .autoDismiss(false)
                .customView(R.layout.activity_applications_editor_dialog, false);

        mDialog = dialogBuilder.build();

        listView = (ListView)mDialog.getCustomView().findViewById(R.id.applications_editor_dialog_listview);

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

        listAdapter = new ApplicationEditorDialogAdapter(this, _context, application, position);
        listView.setAdapter(listAdapter);

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

    public void doOnItemSelected(int position)
    {
        if (cachedApplicationList != null) {
            preference.updateApplication(application, dialogPrefPosition, position);
        }
        mDialog.dismiss();
    }

    public void show() {
        mDialog.show();
    }

}
