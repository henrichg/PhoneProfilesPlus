package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ConnectToSSIDDialogPreference extends DialogPreference {

    private final Context context;

    String value = "";
    private int disableDefaultProfile = 0;

    List<WifiSSIDData> ssidList = null;

    private MaterialDialog mDialog;

    private ListView listView;
    private LinearLayout linlaProgress;

    private ConnectToSSIDPreferenceAdapter listAdapter;

    private AsyncTask asyncTask = null;

    public ConnectToSSIDDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.ConnectToSSIDDialogPreference);

        disableDefaultProfile = typedArray.getInteger(
                R.styleable.ConnectToSSIDDialogPreference_ctsdpDisableDefaultProfile, 0);

        this.context = context;
        ssidList = new ArrayList<>();

        //setWidgetLayoutResource(R.layout.applications_preference); // resource na layout custom preference - TextView-ImageView

        typedArray.recycle();
    }

    @Override
    protected void showDialog(Bundle state) {

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .autoDismiss(false)
                .content(getDialogMessage())
                .customView(R.layout.activity_connect_to_ssid_pref_dialog, false);

        mBuilder.positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText());
        mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                if (callChangeListener(value))
                {
                    persistString(value);
                    setSummaryCTSDP();
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
                ConnectToSSIDDialogPreference.this.onShow(dialog);
            }
        });

        mDialog = mBuilder.build();
        View layout = mDialog.getCustomView();

        listView = layout.findViewById(R.id.connect_to_ssid_pref_dlg_listview);
        linlaProgress = layout.findViewById(R.id.connect_to_ssid_pref_dlg_linla_progress);

        listAdapter = new ConnectToSSIDPreferenceAdapter(context, this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                value = ssidList.get(position).ssid;
                listAdapter.notifyDataSetChanged();
            }
        });

        MaterialDialogsPrefUtil.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    private void onShow(DialogInterface dialog) {

        asyncTask = new AsyncTask<Void, Integer, Void>() {

            List<WifiSSIDData> _SSIDList = null;

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();

                //Log.d("ConnectToSSIDDialogPreference.onShow","onPreExecute");

                _SSIDList = new ArrayList<>();

                listView.setVisibility(View.GONE);
                linlaProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {

                WifiScanJob.fillWifiConfigurationList(context);
                List<WifiSSIDData> wifiConfigurationList = WifiScanJob.getWifiConfigurationList(context);
                if (wifiConfigurationList != null)
                {
                    for (WifiSSIDData wifiConfiguration : wifiConfigurationList)
                    {
                        //if ((wifiConfiguration.ssid != null) && (wifiConfiguration.bssid != null)) {
                        // bssid is null from configuration list
                        if (wifiConfiguration.ssid != null)
                            _SSIDList.add(new WifiSSIDData(wifiConfiguration.ssid/*.replace("\"", "")*/, wifiConfiguration.bssid, false));
                    }
                }

                Collections.sort(_SSIDList, new ConnectToSSIDDialogPreference.SortList());

                if (disableDefaultProfile == 0)
                    _SSIDList.add(0, new WifiSSIDData(Profile.CONNECTTOSSID_DEFAULTPROFILE, "", false));
                _SSIDList.add(0, new WifiSSIDData(Profile.CONNECTTOSSID_JUSTANY, "", false));

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                //Log.d("ConnectToSSIDDialogPreference.onShow","onPostExecute");

                ssidList = new ArrayList<>(_SSIDList);
                listView.setAdapter(listAdapter);

                linlaProgress.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            }

        }.execute();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if ((asyncTask != null) && !asyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            asyncTask.cancel(true);
        }

        MaterialDialogsPrefUtil.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            getValueCTSDP();
        }
        else {
            // set state
            value = Profile.CONNECTTOSSID_JUSTANY;
            persistString(value);
        }
        setSummaryCTSDP();
    }

    private void getValueCTSDP()
    {
        // Get the persistent value
        value = getPersistedString(value);
        //Log.d("ApplicationsDialogPreference.getValueAMSDP","value="+value);
    }

    private void setSummaryCTSDP()
    {
        String prefSummary = context.getString(R.string.connect_to_ssid_pref_dlg_summary_text_just_any);
        if (!value.isEmpty() && value.equals(Profile.CONNECTTOSSID_DEFAULTPROFILE))
            prefSummary = context.getString(R.string.array_pref_default_profile);
        else
        if (!value.isEmpty() && !value.equals(Profile.CONNECTTOSSID_JUSTANY))
            prefSummary = value;
        setSummary(prefSummary);
    }

    private class SortList implements Comparator<WifiSSIDData> {

        public int compare(WifiSSIDData lhs, WifiSSIDData rhs) {
            if (GlobalGUIRoutines.collator != null)
                return GlobalGUIRoutines.collator.compare(lhs.ssid, rhs.ssid);
            else
                return 0;
        }

    }

}
