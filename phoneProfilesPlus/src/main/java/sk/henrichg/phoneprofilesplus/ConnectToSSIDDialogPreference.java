package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.appcompat.app.AlertDialog;

public class ConnectToSSIDDialogPreference extends DialogPreference {

    private final Context context;

    String value = "";
    private final int disableSharedProfile;

    List<WifiSSIDData> ssidList;

    private AlertDialog mDialog;

    private ListView listView;
    private LinearLayout linlaProgress;

    private ConnectToSSIDPreferenceAdapter listAdapter;

    private AsyncTask asyncTask = null;

    public ConnectToSSIDDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.ConnectToSSIDDialogPreference);

        disableSharedProfile = typedArray.getInteger(
                R.styleable.ConnectToSSIDDialogPreference_cDisableSharedProfile, 0);

        this.context = context;
        ssidList = new ArrayList<>();

        //setWidgetLayoutResource(R.layout.applications_preference); // resource na layout custom preference - TextView-ImageView

        typedArray.recycle();
    }

    @Override
    protected void showDialog(Bundle state) {
        getValueCTSDP();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(getDialogTitle());
        dialogBuilder.setIcon(getDialogIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(getNegativeButtonText(), null);
        dialogBuilder.setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
            @SuppressWarnings("StringConcatenationInLoop")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (callChangeListener(value))
                {
                    persistString(value);
                    setSummaryCTSDP();
                }
            }
        });

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_connect_to_ssid_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ConnectToSSIDDialogPreference.this.onShow(/*dialog*/);
            }
        });

        //noinspection ConstantConditions
        listView = layout.findViewById(R.id.connect_to_ssid_pref_dlg_listview);
        //noinspection ConstantConditions
        linlaProgress = layout.findViewById(R.id.connect_to_ssid_pref_dlg_linla_progress);

        listAdapter = new ConnectToSSIDPreferenceAdapter(context, this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                value = ssidList.get(position).ssid;
                listAdapter.notifyDataSetChanged();
            }
        });

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        if (!((Activity)context).isFinishing())
            mDialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    private void onShow(/*DialogInterface dialog*/) {

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
                            _SSIDList.add(new WifiSSIDData(wifiConfiguration.ssid/*.replace("\"", "")*/, wifiConfiguration.bssid, false, true, false));
                    }
                }

                Collections.sort(_SSIDList, new ConnectToSSIDDialogPreference.SortList());

                if (disableSharedProfile == 0)
                    _SSIDList.add(0, new WifiSSIDData(Profile.CONNECTTOSSID_SHAREDPROFILE, "", false, false, false));
                _SSIDList.add(0, new WifiSSIDData(Profile.CONNECTTOSSID_JUSTANY, "", false, false, false));

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

        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if ((mDialog != null) && mDialog.isShowing())
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
        if (!value.isEmpty() && value.equals(Profile.CONNECTTOSSID_SHAREDPROFILE))
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
