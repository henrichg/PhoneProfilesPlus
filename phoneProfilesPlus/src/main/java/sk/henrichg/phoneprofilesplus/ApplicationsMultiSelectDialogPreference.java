package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

public class ApplicationsMultiSelectDialogPreference extends DialogPreference
{

    Context _context = null;
    String value = "";

    // Layout widgets.
    private ListView listView = null;
    private LinearLayout linlaProgress;
    private LinearLayout linlaListView;

    private ApplicationsMultiselectPreferenceAdapter listAdapter;

    public ApplicationsMultiSelectDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        setWidgetLayoutResource(R.layout.applications_preference); // resource na layout custom preference - TextView-ImageView

        if (EditorProfilesActivity.getApplicationsCache() == null)
            EditorProfilesActivity.createApplicationsCache();

    }

    //@Override
    protected void onBindView(View view)
    {
        super.onBindView(view);

        //preferenceTitleView = (TextView)view.findViewById(R.id.applications_pref_label);  // resource na title
        //preferenceTitleView.setText(preferenceTitle);

        ImageView packageIcon = (ImageView)view.findViewById(R.id.applications_pref_icon); // resource na ImageView v custom preference layoute

        if (packageIcon != null)
        {
            PackageManager packageManager = _context.getPackageManager();
            ApplicationInfo app;
            try {

                String[] splits = value.split("\\|");

                if (splits.length > 0) {
                    app = packageManager.getApplicationInfo(splits[0], 0);
                    if (app != null) {
                        Drawable icon = packageManager.getApplicationIcon(app);
                        //CharSequence name = packageManager.getApplicationLabel(app);
                        packageIcon.setImageDrawable(icon);
                    } else {
                        packageIcon.setImageDrawable(null);
                    }
                }
                else {
                    packageIcon.setImageDrawable(null);
                }

            } catch (PackageManager.NameNotFoundException e) {
                //e.printStackTrace();
                packageIcon.setImageDrawable(null);
            }
        }
    }

    protected void showDialog(Bundle state) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .callback(callback)
                .content(getDialogMessage());

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_applications_multiselect_pref_dialog, null);
        onBindDialogView(layout);

        linlaProgress = (LinearLayout)layout.findViewById(R.id.applications_multiselect_pref_dlg_linla_progress);
        linlaListView = (LinearLayout)layout.findViewById(R.id.applications_multiselect_pref_dlg_linla_listview);
        listView = (ListView)layout.findViewById(R.id.applications_multiselect_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                Application application = (Application)listAdapter.getItem(position);
                application.toggleChecked();
                ApplicationViewHolder viewHolder = (ApplicationViewHolder) item.getTag();
                viewHolder.checkBox.setChecked(application.checked);
            }
        });

        listAdapter = new ApplicationsMultiselectPreferenceAdapter(_context);

        mBuilder.customView(layout, false);

        mBuilder.showListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ApplicationsMultiSelectDialogPreference.this.onShow(dialog);
            }
        });

        MaterialDialog mDialog = mBuilder.build();
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
                linlaListView.setVisibility(View.GONE);
                linlaProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                if (!EditorProfilesActivity.getApplicationsCache().isCached())
                    EditorProfilesActivity.getApplicationsCache().getApplicationsList(_context);

                getValueAMSDP();

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                if (!EditorProfilesActivity.getApplicationsCache().isCached())
                    EditorProfilesActivity.getApplicationsCache().clearCache(false);

                listView.setAdapter(listAdapter);
                linlaListView.setVisibility(View.VISIBLE);
                linlaProgress.setVisibility(View.GONE);
            }

        }.execute();
    }

    private final MaterialDialog.ButtonCallback callback = new MaterialDialog.ButtonCallback() {
        @Override
        public void onPositive(MaterialDialog dialog) {
        if (shouldPersist())
        {
            // sem narvi stringy kontatkov oddelenych |
            value = "";
            List<Application> applicationList = EditorProfilesActivity.getApplicationsCache().getList();
            if (applicationList != null)
            {
                for (Application application : applicationList)
                {
                    if (application.checked)
                    {
                        if (!value.isEmpty())
                            value = value + "|";
                        value = value + application.packageName;
                    }
                }
            }
            persistString(value);

            setSummaryAMSDP();
        }
        }
    };


    public void onDismiss (DialogInterface dialog)
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
            // sem narvi default string kontaktov oddeleny |
            value = "";
            persistString("");
        }
        setSummaryAMSDP();
    }

    private void getValueAMSDP()
    {
        // Get the persistent value
        value = getPersistedString(value);

        // change checked state by value
        List<Application> applicationList = EditorProfilesActivity.getApplicationsCache().getList();
        if (applicationList != null)
        {
            String[] splits = value.split("\\|");
            for (Application application : applicationList)
            {
                application.checked = false;
                for (int i = 0; i < splits.length; i++)
                {
                    String packageName = splits[i];
                    if (packageName.equals(application.packageName))
                        application.checked = true;
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

    private void setSummaryAMSDP()
    {
        String prefVolumeDataSummary = _context.getString(R.string.applications_multiselect_summary_text_not_selected);
        if (!value.isEmpty()) {
            String[] splits = value.split("\\|");
            if (splits.length == 1) {
                PackageManager packageManager = _context.getPackageManager();
                ApplicationInfo app;
                try {
                    app = packageManager.getApplicationInfo(splits[0], 0);
                    if (app != null)
                        prefVolumeDataSummary = packageManager.getApplicationLabel(app).toString();
                } catch (PackageManager.NameNotFoundException e) {
                    //e.printStackTrace();
                    prefVolumeDataSummary = _context.getString(R.string.applications_multiselect_summary_text_selected);
                }
            }
            else
                prefVolumeDataSummary = _context.getString(R.string.applications_multiselect_summary_text_selected);
        }
        setSummary(prefVolumeDataSummary);
    }

}
