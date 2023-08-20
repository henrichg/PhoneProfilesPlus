package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MobileCellNamesPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private MobileCellNamesPreference preference;
    int phoneCount = 1;

    private MobileCellNamesPreferenceAdapter listAdapter;

    private RelativeLayout locationSystemSettingsRelLa;
    private TextView locationEnabledStatusTextView;
    private AppCompatImageButton locationSystemSettingsButton;
    private TextView connectedCellSIM1;
    private TextView connectedCellSIM2;
    private TextView connectedCellDefault;
    private Button rescanButton;

    private RefreshListViewAsyncTask rescanAsyncTask = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (MobileCellNamesPreference) getPreference();
        preference.fragment = this;

        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            phoneCount = telephonyManager.getPhoneCount();
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_mobile_cell_names_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        PPApplication.mobileCellsForceStart = true;
        PPApplicationStatic.forceStartMobileCellsScanner(prefContext);

        ListView cellsListView = view.findViewById(R.id.mobile_cell_names_pref_dlg_listview);
        listAdapter = new MobileCellNamesPreferenceAdapter(prefContext, preference);
        cellsListView.setAdapter(listAdapter);

        //refreshListView(false);

        cellsListView.setOnItemClickListener((parent, item, position, id) -> {
            String cellName = preference.cellNamesList.get(position);
            MobileCellNamesPreferenceViewHolder viewHolder =
                    (MobileCellNamesPreferenceViewHolder) item.getTag();
            viewHolder.checkBox.setChecked(!preference.isCellSelected(cellName));
            if (viewHolder.checkBox.isChecked())
                preference.addCellName(cellName);
            else
                preference.removeCellName(cellName);
            preference.refreshListView(/*false*/);
        });

        locationSystemSettingsRelLa = view.findViewById(R.id.mobile_cell_names_pref_dlg_locationSystemSettingsRelLa);
        locationEnabledStatusTextView = view.findViewById(R.id.mobile_cell_names_pref_dlg_locationEnableStatus);
        locationSystemSettingsButton = view.findViewById(R.id.mobile_cell_names_pref_dlg_locationSystemSettingsButton);
        TooltipCompat.setTooltipText(locationSystemSettingsButton, getString(R.string.location_settings_button_tooltip));

        connectedCellSIM1 = view.findViewById(R.id.mobile_cell_names_pref_dlg_connectedCell_sim1);
        connectedCellSIM2 = view.findViewById(R.id.mobile_cell_names_pref_dlg_connectedCell_sim2);
        connectedCellDefault = view.findViewById(R.id.mobile_cell_names_pref_dlg_connectedCell_simDefault);

        boolean sim1Exists;
        boolean sim2Exists;
        if (getActivity() != null) {
            Context appContext = getActivity().getApplicationContext();
            HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(appContext);
            sim1Exists = hasSIMCardData.hasSIM1;
            sim2Exists = hasSIMCardData.hasSIM2;
        } else {
            sim1Exists = false;
            sim2Exists = false;
        }

        if (/*(Build.VERSION.SDK_INT >= 26) &&*/ (phoneCount > 1)) {
            if (!sim1Exists)
                connectedCellSIM1.setVisibility(View.GONE);
            if (!sim2Exists)
                connectedCellSIM2.setVisibility(View.GONE);

            connectedCellDefault.setVisibility(View.GONE);
        }
        else {
            connectedCellSIM1.setVisibility(View.GONE);
            connectedCellSIM2.setVisibility(View.GONE);
        }

        rescanButton = view.findViewById(R.id.mobile_cell_names_pref_dlg_rescanButton);
        if (PPApplication.HAS_FEATURE_TELEPHONY) {
            TelephonyManager telephonyManager = (TelephonyManager) prefContext.getSystemService(Context.TELEPHONY_SERVICE);
            boolean simIsReady = false;
            if (telephonyManager != null) {
                /*if (Build.VERSION.SDK_INT < 26) {
                    if (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY)
                        // sim card is ready
                        simIsReady = true;
                } else {*/
                if (Permissions.checkPhone(prefContext.getApplicationContext())) {
                    SubscriptionManager mSubscriptionManager = (SubscriptionManager) prefContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                    //SubscriptionManager.from(context);
                    if (mSubscriptionManager != null) {
                        List<SubscriptionInfo> subscriptionList = null;
                        try {
                            // Loop through the subscription list i.e. SIM list.
                            subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                        } catch (SecurityException e) {
                            PPApplicationStatic.recordException(e);
                            //Log.e("MobileCellsPreferenceFragment.onBindDialogView", Log.getStackTraceString(e));
                        }
                        if (subscriptionList != null) {
                            int size = subscriptionList.size();/*mSubscriptionManager.getActiveSubscriptionInfoCountMax();*/
                            for (int i = 0; i < size; i++) {
                                // Get the active subscription ID for a given SIM card.
                                SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                                if (subscriptionInfo != null) {
                                    int slotIndex = subscriptionInfo.getSimSlotIndex();
                                    if (telephonyManager.getSimState(slotIndex) == TelephonyManager.SIM_STATE_READY) {
                                        // sim card is ready
                                        simIsReady = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                //}
            }
            if (simIsReady) {
                rescanButton.setOnClickListener(v -> {
                    if (Permissions.grantMobileCellsDialogPermissions(prefContext))
                        refreshListView(/*true*/);
                });
            }
            else
                rescanButton.setEnabled(false);

        }
        else
            rescanButton.setEnabled(false);

        setLocationEnableStatus();

        refreshListView(/*false*/);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        } else {
            preference.resetSummary();
        }


        if ((rescanAsyncTask != null) && rescanAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            rescanAsyncTask.cancel(true);
        rescanAsyncTask = null;

        PPApplication.mobileCellsForceStart = false;
        PPApplicationStatic.restartMobileCellsScanner(prefContext);

        preference.fragment = null;
    }

    void setLocationEnableStatus() {
        if (Build.VERSION.SDK_INT >= 28) {
            String statusText;
            if (!GlobalUtils.isLocationEnabled(prefContext)) {
                /*if (Build.VERSION.SDK_INT < 28)
                    statusText = prefContext.getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                            prefContext.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary);
                else*/
                statusText = prefContext.getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + StringConstants.STR_NEWLINE_WITH_COLON +
                        "* " + prefContext.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *";

                locationEnabledStatusTextView.setText(statusText);

                locationSystemSettingsButton.setOnClickListener(v -> {
                    if (getActivity() != null) {
                        boolean ok = false;
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, prefContext.getApplicationContext())) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                //noinspection deprecation
                                getActivity().startActivityForResult(intent, PhoneProfilesPrefsFragment.RESULT_WIFI_BLUETOOTH_MOBILE_CELLS_LOCATION_SETTINGS);
                                ok = true;
                            } catch (Exception e) {
                                PPApplicationStatic.recordException(e);
                            }
                        }
                        if (!ok) {
                            PPAlertDialog dialog = new PPAlertDialog(
                                    getString(R.string.location_settings_button_tooltip),
                                    getString(R.string.setting_screen_not_found_alert),
                                    getString(android.R.string.ok),
                                    null,
                                    null, null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    true, true,
                                    false, false,
                                    true,
                                    getActivity()
                            );

                            if (getActivity() != null)
                                if (!getActivity().isFinishing())
                                    dialog.show();
                        }
                    }
                });

                locationSystemSettingsRelLa.setVisibility(View.VISIBLE);
                //locationEnabledStatusTextView.setVisibility(View.VISIBLE);
                //locationSystemSettingsButton.setVisibility(View.VISIBLE);
                rescanButton.setVisibility(View.GONE);
            } else {
                locationSystemSettingsRelLa.setVisibility(View.GONE);
                //locationEnabledStatusTextView.setVisibility(View.GONE);
                //locationSystemSettingsButton.setVisibility(View.GONE);
                rescanButton.setVisibility(View.VISIBLE);
            }
        }
        else {
            locationSystemSettingsRelLa.setVisibility(View.GONE);
            //locationEnabledStatusTextView.setVisibility(View.GONE);
            //locationSystemSettingsButton.setVisibility(View.GONE);
            rescanButton.setVisibility(View.VISIBLE);
        }
    }

    void refreshListView(/*final boolean forRescan*/)
    {
        rescanAsyncTask = new RefreshListViewAsyncTask(/*forRescan,*/ preference, this, prefContext);
        rescanAsyncTask.execute();
    }

    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {

        //final boolean forRescan;

        private final WeakReference<MobileCellNamesPreference> preferenceWeakRef;
        private final WeakReference<MobileCellNamesPreferenceFragment> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        List<String> _cellsList = null;
        //String _value;

        boolean sim1Exists;
        boolean sim2Exists;
        int registeredCellDefault;
        String registeredCellNameDefault;
        int registeredCellSIM1;
        String registeredCellNameSIM1;
        int registeredCellSIM2;
        String registeredCellNameSIM2;

        public RefreshListViewAsyncTask(/*final boolean forRescan,*/
                                        MobileCellNamesPreference preference,
                                        MobileCellNamesPreferenceFragment fragment,
                                        Context prefContext) {
            //this.forRescan = forRescan;
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            MobileCellNamesPreferenceFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {

                _cellsList = new ArrayList<>();
                //_value = fragment.preference.value;

                if (fragment.getActivity() != null) {
                    Context appContext = fragment.getActivity().getApplicationContext();
                    HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(appContext);
                    sim1Exists = hasSIMCardData.hasSIM1;
                    sim2Exists = hasSIMCardData.hasSIM2;
                } else {
                    sim1Exists = false;
                    sim2Exists = false;
                }

                //dataRelativeLayout.setVisibility(View.GONE);
                //progressLinearLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            synchronized (PPApplication.mobileCellsScannerMutex) {
                MobileCellNamesPreferenceFragment fragment = fragmentWeakRef.get();
                MobileCellNamesPreference preference = preferenceWeakRef.get();
                Context prefContext = prefContextWeakRef.get();
                if ((fragment != null) && (preference != null) && (prefContext != null)) {

                    /*
                    if (forRescan) {
                        if ((PhoneProfilesService.getInstance() != null) && (PPApplication.mobileCellsScanner != null)) {
                            PPApplication.mobileCellsScanner.registerCell();

                            //PPApplication.sleep(200);
                        }
                    }
                    */

                    // add all from table
                    DatabaseHandler db = DatabaseHandler.getInstance(prefContext.getApplicationContext());
                    db.addMobileCellNamesToList(_cellsList);

                    if ((PhoneProfilesService.getInstance() != null) && (PPApplication.mobileCellsScanner != null)) {
                        // add registered cell

                        //MobileCellsScanner scanner = PhoneProfilesService.getInstance().getMobileCellsScanner();

                        if (/*(Build.VERSION.SDK_INT >= 26) &&*/ (fragment.phoneCount > 1)) {
                            if (sim1Exists) {
                                if (PPApplication.mobileCellsScanner != null) {
                                    registeredCellSIM1 = PPApplication.mobileCellsScanner.getRegisteredCell(1);
                                    List<MobileCellsData> _cellsList = new ArrayList<>();
                                    db.addMobileCellsToList(_cellsList, registeredCellSIM1);
                                    registeredCellNameSIM1 = _cellsList.get(0).name;
                                }
                            }
                            if (sim2Exists) {
                                if (PPApplication.mobileCellsScanner != null) {
                                    registeredCellSIM2 = PPApplication.mobileCellsScanner.getRegisteredCell(2);
                                    List<MobileCellsData> _cellsList = new ArrayList<>();
                                    db.addMobileCellsToList(_cellsList, registeredCellSIM2);
                                    registeredCellNameSIM2 = _cellsList.get(0).name;
                                }
                            }
                        } else {
                            if (PPApplication.mobileCellsScanner != null) {
                                registeredCellDefault = PPApplication.mobileCellsScanner.getRegisteredCell(0);
                                List<MobileCellsData> _cellsList = new ArrayList<>();
                                db.addMobileCellsToList(_cellsList, registeredCellDefault);
                                registeredCellNameDefault = _cellsList.get(0).name;
                            }
                        }
                    }

                }

                return null;
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            MobileCellNamesPreferenceFragment fragment = fragmentWeakRef.get();
            MobileCellNamesPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {

                preference.cellNamesList = new ArrayList<>(_cellsList);
                fragment.listAdapter.notifyDataSetChanged();

                if (/*(Build.VERSION.SDK_INT >= 26) &&*/ (fragment.phoneCount > 1)) {
                    if (sim1Exists) {
                        String connectedCellName = prefContext.getString(R.string.mobile_cells_pref_dlg_connected_cell_sim1) + " ";
                        if (registeredCellSIM1 != Integer.MAX_VALUE) {
                            if ((registeredCellNameSIM1 != null) && (!registeredCellNameSIM1.isEmpty()))
                                connectedCellName = connectedCellName + registeredCellNameSIM1 + ", ";
                            connectedCellName = connectedCellName + registeredCellSIM1;
                        }
                        fragment.connectedCellSIM1.setText(connectedCellName);
                    }
                    if (sim2Exists) {
                        String connectedCellName = prefContext.getString(R.string.mobile_cells_pref_dlg_connected_cell_sim2) + " ";
                        if (registeredCellSIM2 != Integer.MAX_VALUE) {
                            if ((registeredCellNameSIM2 != null) && (!registeredCellNameSIM2.isEmpty()))
                                connectedCellName = connectedCellName + registeredCellNameSIM2 + ", ";
                            connectedCellName = connectedCellName + registeredCellSIM2;
                        }
                        fragment.connectedCellSIM2.setText(connectedCellName);
                    }
                } else {
                    String connectedCellName = prefContext.getString(R.string.mobile_cells_pref_dlg_connected_cell) + " ";
                    if (registeredCellDefault != Integer.MAX_VALUE) {
                        if ((registeredCellNameDefault != null) && (!registeredCellNameDefault.isEmpty()))
                            connectedCellName = connectedCellName + registeredCellNameDefault + ", ";
                        connectedCellName = connectedCellName + registeredCellDefault;
                    }
                    fragment.connectedCellDefault.setText(connectedCellName);
                }

            }
        }

    }

}
