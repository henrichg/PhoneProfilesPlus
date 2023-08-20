package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MobileCellsPreferenceFragment extends PreferenceDialogFragmentCompat
        implements MobileCellsPreferenceFragmentRefreshListViewListener {

    private Context prefContext;
    private MobileCellsPreference preference;
    int phoneCount = 1;

    private SingleSelectListDialog mRenameDialog;
    private SingleSelectListDialog mSelectorDialog;
    private SingleSelectListDialog mSortDialog;

    private TextView cellFilter;
    private TextView cellName;
    private TextView connectedCellSIM1;
    private TextView connectedCellSIM2;
    private TextView connectedCellDefault;
    private MobileCellsPreferenceAdapter listAdapter;
    private MobileCellNamesDialog mMobileCellsFilterDialog;
    private MobileCellNamesDialog mMobileCellNamesDialog;
    private AppCompatImageButton addCellButtonSIM1;
    private AppCompatImageButton addCellButtonSIM2;
    private AppCompatImageButton addCellButtonDefault;
    private AppCompatImageButton editButton;
    private RelativeLayout locationSystemSettingsRelLa;
    private TextView locationEnabledStatusTextView;
    private AppCompatImageButton locationSystemSettingsButton;
    private Button rescanButton;

    private RefreshListViewAsyncTask rescanAsyncTask = null;

    private RefreshListViewBroadcastReceiver refreshListViewBroadcastReceiver = null;
    private DeleteCellNamesFromEventsAsyncTask deleteCellNamesFromEventsAsyncTask = null;
    private RenameCellNamesFromEventsAsyncTask renameCellNamesFromEventsAsyncTask = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (MobileCellsPreference) getPreference();
        preference.fragment = this;

        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            phoneCount = telephonyManager.getPhoneCount();
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_mobile_cells_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        refreshListViewBroadcastReceiver = new RefreshListViewBroadcastReceiver(this);
        LocalBroadcastManager.getInstance(prefContext).registerReceiver(refreshListViewBroadcastReceiver,
                new IntentFilter(MobileCellsPreference.ACTION_MOBILE_CELLS_PREF_REFRESH_LISTVIEW_BROADCAST_RECEIVER));

        PPApplication.mobileCellsForceStart = true;
        PPApplicationStatic.forceStartMobileCellsScanner(prefContext);

        cellFilter = view.findViewById(R.id.mobile_cells_pref_dlg_cells_filter_name);
        if ((preference.cellFilter == null) || preference.cellFilter.isEmpty()) {
            //if (preference.value.isEmpty())
            //    cellFilter.setText(R.string.mobile_cell_names_dialog_item_show_all);
            //else
                cellFilter.setText(R.string.mobile_cell_names_dialog_item_show_new);
        }
        else
            cellFilter.setText(preference.cellFilter);
        cellFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshListView(false, Integer.MAX_VALUE);
            }
        });

        cellName = view.findViewById(R.id.mobile_cells_pref_dlg_cells_name);
        connectedCellSIM1 = view.findViewById(R.id.mobile_cells_pref_dlg_connectedCell_sim1);
        connectedCellSIM2 = view.findViewById(R.id.mobile_cells_pref_dlg_connectedCell_sim2);
        connectedCellDefault = view.findViewById(R.id.mobile_cells_pref_dlg_connectedCell_simDefault);

        ListView cellsListView = view.findViewById(R.id.mobile_cells_pref_dlg_listview);
        listAdapter = new MobileCellsPreferenceAdapter(prefContext, preference);
        cellsListView.setAdapter(listAdapter);

        //refreshListView(false);

        cellsListView.setOnItemClickListener((parent, item, position, id) -> {
            int cellId = preference.filteredCellsList.get(position).cellId;
            MobileCellsPreferenceViewHolder viewHolder =
                    (MobileCellsPreferenceViewHolder) item.getTag();
            viewHolder.checkBox.setChecked(!preference.isCellSelected(cellId));
            if (viewHolder.checkBox.isChecked())
                preference.addCellId(cellId);
            else
                preference.removeCellId(cellId);
            preference.refreshListView(false, Integer.MAX_VALUE);
        });

        mMobileCellsFilterDialog = new MobileCellNamesDialog((Activity)prefContext, preference, true);
        cellFilter.setOnClickListener(view1 -> {
            if (getActivity() != null)
                if (!getActivity().isFinishing())
                    mMobileCellsFilterDialog.show();
        });

        mMobileCellNamesDialog = new MobileCellNamesDialog((Activity) prefContext, preference, false);
        cellName.setOnClickListener(view12 -> {
            if (getActivity() != null)
                if (!getActivity().isFinishing())
                    mMobileCellNamesDialog.show();
        });

        editButton = view.findViewById(R.id.mobile_cells_pref_dlg_rename);
        TooltipCompat.setTooltipText(editButton, getString(R.string.mobile_cells_pref_dlg_rename_cell_button_tooltip));
        editButton.setOnClickListener(v -> {
            if (getActivity() != null)
                if (!getActivity().isFinishing()) {
                    if (!cellName.getText().toString().isEmpty()) {
                        mRenameDialog = new SingleSelectListDialog(
                                false,
                                getString(R.string.mobile_cells_pref_dlg_cell_rename_title),
                                null,
                                R.array.mobileCellsRenameArray,
                                SingleSelectListDialog.NOT_USE_RADIO_BUTTONS,
                                (dialog, which) -> {
                                    String oldCellNames = "";
                                    final DatabaseHandler db = DatabaseHandler.getInstance(prefContext);
                                    switch (which) {
                                        case 0:
                                        case 1:
                                            oldCellNames = db.renameMobileCellsList(preference.filteredCellsList, cellName.getText().toString(), which == 0, preference.value);
                                            break;
                                        case 2:
                                            oldCellNames = db.renameMobileCellsList(preference.filteredCellsList, cellName.getText().toString(), false, null);
                                            break;
                                    }
                                    refreshListView(false, Integer.MAX_VALUE);
                                    renameCellNamesFromEventsAsyncTask = new RenameCellNamesFromEventsAsyncTask(oldCellNames, cellName.getText().toString(), prefContext);
                                    renameCellNamesFromEventsAsyncTask.execute();
                                    //dialog.dismiss();
                                },
                                null,
                                false,
                                (Activity) prefContext);

                        mRenameDialog.show();
                    }
                }
        });
        AppCompatImageButton changeSelectionIcon = view.findViewById(R.id.mobile_cells_pref_dlg_changeSelection);
        TooltipCompat.setTooltipText(changeSelectionIcon, getString(R.string.mobile_cells_pref_dlg_select_button_tooltip));
        changeSelectionIcon.setOnClickListener(view13 -> {
            if (getActivity() != null)
                if (!getActivity().isFinishing()) {
                    mSelectorDialog = new SingleSelectListDialog(
                            false,
                            getString(R.string.pref_dlg_change_selection_title),
                            null,
                            R.array.mobileCellsChangeSelectionArray,
                            SingleSelectListDialog.NOT_USE_RADIO_BUTTONS,
                            (dialog, which) -> {
                                switch (which) {
                                    case 0:
                                        preference.value = "";
                                        break;
                                    case 1:
                                        for (MobileCellsData cell : preference.filteredCellsList) {
                                            if (cell.name.equals(cellName.getText().toString()))
                                                preference.addCellId(cell.cellId);
                                        }
                                        break;
                                    case 2:
                                        preference.value = "";
                                        for (MobileCellsData cell : preference.filteredCellsList) {
                                            preference.addCellId(cell.cellId);
                                        }
                                        break;
                                    default:
                                }
                                refreshListView(false, Integer.MAX_VALUE);
                                //dialog.dismiss();
                            },
                            null,
                            false,
                            (Activity) prefContext);

                    mSelectorDialog.show();
                }
        });
        final AppCompatImageButton sortIcon = view.findViewById(R.id.mobile_cells_pref_dlg_sort);
        TooltipCompat.setTooltipText(sortIcon, getString(R.string.mobile_cells_pref_dlg_button_tooltip));
        sortIcon.setOnClickListener(v -> {
            if (getActivity() != null)
                if (!getActivity().isFinishing()) {
                    mSortDialog = new SingleSelectListDialog(
                            false,
                            getString(R.string.mobile_cells_pref_dlg_cell_sort_title),
                            null,
                            R.array.mobileCellsSortArray,
                            preference.sortCellsBy,
                            (dialog, which) -> {
                                preference.sortCellsBy = which;
                                refreshListView(false, Integer.MAX_VALUE);
                            },
                            null,
                            false,
                            getActivity());
                    mSortDialog.show();
                }
        });

        final AppCompatImageButton helpIcon = view.findViewById(R.id.mobile_cells_pref_dlg_helpIcon);
        TooltipCompat.setTooltipText(helpIcon, getString(R.string.help_button_tooltip));
        helpIcon.setOnClickListener(v -> DialogHelpPopupWindow.showPopup(helpIcon, R.string.menu_help, (Activity)prefContext, /*getDialog(),*/ R.string.mobile_cells_pref_dlg_help, false));

        rescanButton = view.findViewById(R.id.mobile_cells_pref_dlg_rescanButton);
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
                        refreshListView(true, Integer.MAX_VALUE);
                });
            }
            else
                rescanButton.setEnabled(false);

        }
        else
            rescanButton.setEnabled(false);

        if (/*(Build.VERSION.SDK_INT >= 26) &&*/ (phoneCount > 1)) {
            addCellButtonSIM1 = view.findViewById(R.id.mobile_cells_pref_dlg_addCellButton_sim1);
            TooltipCompat.setTooltipText(addCellButtonSIM1, getString(R.string.mobile_cells_pref_dlg_add_button_tooltip));
            addCellButtonSIM1.setOnClickListener(v -> {
                if (preference.registeredCellDataSIM1 != null) {
                    preference.addCellId(preference.registeredCellDataSIM1.cellId);
                    refreshListView(false, preference.registeredCellDataSIM1.cellId);
                }
            });
            addCellButtonSIM2 = view.findViewById(R.id.mobile_cells_pref_dlg_addCellButton_sim2);
            TooltipCompat.setTooltipText(addCellButtonSIM2, getString(R.string.mobile_cells_pref_dlg_add_button_tooltip));
            addCellButtonSIM2.setOnClickListener(v -> {
                if (preference.registeredCellDataSIM2 != null) {
                    preference.addCellId(preference.registeredCellDataSIM2.cellId);
                    refreshListView(false, preference.registeredCellDataSIM2.cellId);
                }
            });
        } else {
            addCellButtonDefault = view.findViewById(R.id.mobile_cells_pref_dlg_addCellButton_simDefault);
            TooltipCompat.setTooltipText(addCellButtonDefault, getString(R.string.mobile_cells_pref_dlg_add_button_tooltip));
            addCellButtonDefault.setOnClickListener(v -> {
                if (preference.registeredCellDataDefault != null) {
                    preference.addCellId(preference.registeredCellDataDefault.cellId);
                    refreshListView(false, preference.registeredCellDataDefault.cellId);
                }
            });
        }

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

        RelativeLayout connectedCellRelLa;
        if (/*(Build.VERSION.SDK_INT >= 26) &&*/ (phoneCount > 1)) {
            if (!sim1Exists) {
                connectedCellRelLa = view.findViewById(R.id.mobile_cells_pref_dlg_reLa1_sim1);
                connectedCellRelLa.setVisibility(View.GONE);
            }
            if (!sim2Exists) {
                connectedCellRelLa = view.findViewById(R.id.mobile_cells_pref_dlg_reLa1_sim2);
                connectedCellRelLa.setVisibility(View.GONE);
            }

            connectedCellRelLa = view.findViewById(R.id.mobile_cells_pref_dlg_reLa1_simDefault);
            connectedCellRelLa.setVisibility(View.GONE);
        }
        else {
            connectedCellRelLa = view.findViewById(R.id.mobile_cells_pref_dlg_reLa1_sim1);
            if (connectedCellRelLa != null)
                connectedCellRelLa.setVisibility(View.GONE);

            connectedCellRelLa = view.findViewById(R.id.mobile_cells_pref_dlg_reLa1_sim2);
            if (connectedCellRelLa != null)
                connectedCellRelLa.setVisibility(View.GONE);
        }

        locationSystemSettingsRelLa = view.findViewById(R.id.mobile_cells_pref_dlg_locationSystemSettingsRelLa);
        locationEnabledStatusTextView = view.findViewById(R.id.mobile_cells_pref_dlg_locationEnableStatus);
        locationSystemSettingsButton = view.findViewById(R.id.mobile_cells_pref_dlg_locationSystemSettingsButton);
        TooltipCompat.setTooltipText(locationSystemSettingsButton, getString(R.string.location_settings_button_tooltip));

        setLocationEnableStatus();

        refreshListView(false, Integer.MAX_VALUE);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        /*if (positiveResult) {
            preference.persistValue();
        } else {
            preference.resetSummary();
        }*/

        if ((mRenameDialog != null) && mRenameDialog.mDialog.isShowing())
            mRenameDialog.mDialog.dismiss();
        if ((mSelectorDialog != null) && mSelectorDialog.mDialog.isShowing())
            mSelectorDialog.mDialog.dismiss();
        if ((mSortDialog != null) && mSortDialog.mDialog.isShowing())
            mSortDialog.mDialog.dismiss();

        if ((rescanAsyncTask != null) && rescanAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            rescanAsyncTask.cancel(true);
        rescanAsyncTask = null;
        if ((deleteCellNamesFromEventsAsyncTask != null) && deleteCellNamesFromEventsAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            deleteCellNamesFromEventsAsyncTask.cancel(true);
        deleteCellNamesFromEventsAsyncTask = null;
        if ((renameCellNamesFromEventsAsyncTask != null) && renameCellNamesFromEventsAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            renameCellNamesFromEventsAsyncTask.cancel(true);
        renameCellNamesFromEventsAsyncTask = null;

        if (refreshListViewBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(prefContext).unregisterReceiver(refreshListViewBroadcastReceiver);
            refreshListViewBroadcastReceiver = null;
        }

        PPApplication.mobileCellsForceStart = false;
        PPApplicationStatic.restartMobileCellsScanner(prefContext);

        OneTimeWorkRequest worker =
                new OneTimeWorkRequest.Builder(MainWorker.class)
                        .addTag(MainWorker.SET_MOBILE_CELLS_AS_OLD_WORK_TAG)
                        .build();
        try {
            WorkManager workManager = PPApplication.getWorkManagerInstance();
            if (workManager != null) {

//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.SCHEDULE_AVOID_RESCHEDULE_RECEIVER_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                        PPApplicationStatic.logE("[WORKER_CALL] EditorActivity.onActivityResult", "xxx");
                workManager.enqueueUniqueWork(MainWorker.SET_MOBILE_CELLS_AS_OLD_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }

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

    void refreshListView(final boolean forRescan, final int renameCellId)
    {
        rescanAsyncTask = new RefreshListViewAsyncTask(forRescan, renameCellId, preference, this, prefContext);
        rescanAsyncTask.execute();
    }

    private static class SortByNameList implements Comparator<MobileCellsData> {

        public int compare(MobileCellsData lhs, MobileCellsData rhs) {
            if (PPApplication.collator != null) {
                String _lhs = "";
                if (lhs._new)
                    _lhs = _lhs + "0000";
                else
                    _lhs = _lhs + "0001";
                if (lhs.name.isEmpty())
                    _lhs = _lhs + "0002";
                else
                    _lhs = _lhs + "0003" + lhs.name;
                _lhs = _lhs + "-" + lhs.cellId;

                String _rhs = "";
                if (rhs._new)
                    _rhs = _rhs + "0000";
                else
                    _rhs = _rhs + "0001";
                if (rhs.name.isEmpty())
                    _rhs = _rhs + "0002";
                else
                    _rhs = _rhs + "0003" + rhs.name;
                _rhs = _rhs + "-" + rhs.cellId;
                return PPApplication.collator.compare(_lhs, _rhs);
            }
            else
                return 0;
        }

    }

    private static class SortByConnectionList implements Comparator<MobileCellsData> {

        public int compare(MobileCellsData lhs, MobileCellsData rhs) {
            if (PPApplication.collator != null) {
                String _lhs;// = "";
                /*if (lhs._new)
                    _lhs = _lhs + "0000";
                else
                    _lhs = _lhs + "0001";
                if (lhs.name.isEmpty())
                    _lhs = _lhs + "0002";
                else
                    _lhs = _lhs + "0003" + lhs.name;
                _lhs = _lhs + "-" + lhs.cellId;*/
                _lhs = String.valueOf(lhs.lastConnectedTime);

                String _rhs;// = "";
                /*if (rhs._new)
                    _rhs = _rhs + "0000";
                else
                    _rhs = _rhs + "0001";
                if (rhs.name.isEmpty())
                    _rhs = _rhs + "0002";
                else
                    _rhs = _rhs + "0003" + rhs.name;
                _rhs = _rhs + "-" + rhs.cellId;*/
                _rhs = String.valueOf(rhs.lastConnectedTime);
                return PPApplication.collator.compare(_rhs, _lhs);
            } else
                return 0;
        }

    }

    void showEditMenu(View view) {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        final Context _context = view.getContext();
        PopupMenu popup;
        //if (android.os.Build.VERSION.SDK_INT >= 19)
        popup = new PopupMenu(_context, view, Gravity.END);
        //else
        //    popup = new PopupMenu(context, view);
        new MenuInflater(_context).inflate(R.menu.mobile_cells_pref_item_edit, popup.getMenu());

        final int cellId = (int)view.getTag();

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.mobile_cells_pref_item_menu_delete) {
                if (getActivity() != null) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            getString(R.string.profile_context_item_delete),
                            getString(R.string.delete_mobile_cell_alert_message) + StringConstants.STR_DOUBLE_NEWLINE +
                            getString(R.string.delete_mobile_cell_alert_message_warning),
                            getString(R.string.alert_button_yes),
                            getString(R.string.alert_button_no),
                            null, null,
                            (dialog1, which) -> {
                                DatabaseHandler db = DatabaseHandler.getInstance(_context);
                                db.deleteMobileCell(cellId);
                                preference.removeCellId(cellId);
                                refreshListView(false, Integer.MAX_VALUE);
                                deleteCellNamesFromEventsAsyncTask = new DeleteCellNamesFromEventsAsyncTask(String.valueOf(cellId), prefContext);
                                deleteCellNamesFromEventsAsyncTask.execute();
                            },
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            true,
                            getActivity()
                    );

                    if ((getActivity() != null) && (!getActivity().isFinishing()))
                        dialog.show();
                }
                return true;
            }
            else
            if (itemId == R.id.mobile_cells_pref_item_menu_delete_all_selected) {
                // this delete all selected cells in actual filter
                if (getActivity() != null) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            getString(R.string.profile_context_item_delete),
                            getString(R.string.delete_selected_mobile_cells_alert_message) + StringConstants.STR_DOUBLE_NEWLINE +
                                    getString(R.string.delete_selected_mobile_cells_alert_message_warning),
                            getString(R.string.alert_button_yes),
                            getString(R.string.alert_button_no),
                            null, null,
                            (dialog1, which) -> {
                                String[] splits = preference.value.split(StringConstants.STR_SPLIT_REGEX);
                                StringBuilder deletedCellIds = new StringBuilder();
                                DatabaseHandler db = DatabaseHandler.getInstance(_context);
                                for (MobileCellsData cell : preference.filteredCellsList) {
                                    for (String valueCell : splits) {
                                        if (valueCell.equals(Integer.toString(cell.cellId))) {
                                            db.deleteMobileCell(cell.cellId);
                                            preference.removeCellId(cell.cellId);
                                            if (deletedCellIds.length() > 0)
                                                deletedCellIds.append("|");
                                            deletedCellIds.append(cell.cellId);
                                        }
                                    }
                                }
                                refreshListView(false, Integer.MAX_VALUE);
                                deleteCellNamesFromEventsAsyncTask = new DeleteCellNamesFromEventsAsyncTask(deletedCellIds.toString(), prefContext);
                                deleteCellNamesFromEventsAsyncTask.execute();
                            },
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            true,
                            getActivity()
                    );

                    if ((getActivity() != null) && (!getActivity().isFinishing()))
                        dialog.show();
                }
                return true;
            } else if (itemId == R.id.mobile_cells_pref_item_menu_delete_all_unselected) {
                // this delete all unselected cells in actual filter
                if (getActivity() != null) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            getString(R.string.profile_context_item_delete),
                            getString(R.string.delete_unselected_mobile_cells_alert_message) + StringConstants.STR_DOUBLE_NEWLINE +
                                    getString(R.string.delete_unselected_mobile_cells_alert_message_warning),
                            getString(R.string.alert_button_yes),
                            getString(R.string.alert_button_no),
                            null, null,
                            (dialog1, which) -> {
                                String[] splits = preference.value.split(StringConstants.STR_SPLIT_REGEX);
                                StringBuilder deletedCellIds = new StringBuilder();
                                DatabaseHandler db = DatabaseHandler.getInstance(_context);
                                for (MobileCellsData cell : preference.filteredCellsList) {
                                    boolean isSelected = false;
                                    for (String valueCell : splits) {
                                        if (valueCell.equals(Integer.toString(cell.cellId))) {
                                            isSelected = true;
                                            break;
                                        }
                                    }
                                    if (!isSelected) {
                                        db.deleteMobileCell(cell.cellId);
                                        preference.removeCellId(cell.cellId);
                                        if (deletedCellIds.length() > 0)
                                            deletedCellIds.append("|");
                                        deletedCellIds.append(cell.cellId);
                                    }
                                }
                                refreshListView(false, Integer.MAX_VALUE);
                                deleteCellNamesFromEventsAsyncTask = new DeleteCellNamesFromEventsAsyncTask(deletedCellIds.toString(), prefContext);
                                deleteCellNamesFromEventsAsyncTask.execute();
                            },
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            true,
                            getActivity()
                    );

                    if ((getActivity() != null) && (!getActivity().isFinishing()))
                        dialog.show();
                }
                return true;
            } else {
                return false;
            }
        });

        if (getActivity() != null)
            if (!getActivity().isFinishing())
                popup.show();
    }

    private static class RefreshListViewBroadcastReceiver extends BroadcastReceiver {

        private final MobileCellsPreferenceFragmentRefreshListViewListener listener;

        public RefreshListViewBroadcastReceiver(
                MobileCellsPreferenceFragmentRefreshListViewListener listener) {
            this.listener = listener;
        }

        @Override
        public void onReceive( Context context, Intent intent ) {
            listener.refreshListViewFromListener();
        }

    }

    @Override
    public void refreshListViewFromListener() {
//            PPApplicationStatic.logE("[IN_BROADCAST] MobileCellsPreferenceFragment.RefreshListViewBroadcastReceiver", "xxx");
        //if (preference != null)
        //    preference.refreshListView(false, Integer.MAX_VALUE);
        refreshListView(false, Integer.MAX_VALUE);
    }

    void setCellNameText(String text) {
        cellName.setText(text);

        GlobalGUIRoutines.setImageButtonEnabled(
                !text.isEmpty(), editButton, prefContext);
    }

    String getCellNameText() {
        return  cellName.getText().toString();
    }

    void setCellFilterText(String text) {
        cellFilter.setText(text);
    }

    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {

        final boolean forRescan;
        final int renameCellId;

        private final WeakReference<MobileCellsPreference> preferenceWeakRef;
        private final WeakReference<MobileCellsPreferenceFragment> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        String _cellName;
        List<MobileCellsData> _cellsList = null;
        List<MobileCellsData> _filteredCellsList = null;
        String _cellFilterValue;
        String _value;
        int _sortCellsBy;

        MobileCellsData _registeredCellDataSIM1;
        boolean _registeredCellInTableSIM1;
        boolean _registeredCellInValueSIM1;
        MobileCellsData _registeredCellDataSIM2;
        boolean _registeredCellInTableSIM2;
        boolean _registeredCellInValueSIM2;
        MobileCellsData _registeredCellDataDefault;
        boolean _registeredCellInTableDefault;
        boolean _registeredCellInValueDefault;

        boolean sim1Exists;
        boolean sim2Exists;

        public RefreshListViewAsyncTask(final boolean forRescan, final int renameCellId,
                                        MobileCellsPreference preference,
                                        MobileCellsPreferenceFragment fragment,
                                        Context prefContext) {
            this.forRescan = forRescan;
            this.renameCellId = renameCellId;
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            MobileCellsPreferenceFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {

                if (fragment.getActivity() != null) {
                    Context appContext = fragment.getActivity().getApplicationContext();
                    HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(appContext);
                    sim1Exists = hasSIMCardData.hasSIM1;
                    sim2Exists = hasSIMCardData.hasSIM2;
                } else {
                    sim1Exists = false;
                    sim2Exists = false;
                }

                _cellName = fragment.cellName.getText().toString();
                _cellsList = new ArrayList<>();
                _filteredCellsList = new ArrayList<>();
                _cellFilterValue = fragment.cellFilter.getText().toString();
                _value = fragment.preference.value;
                _sortCellsBy = fragment.preference.sortCellsBy;

                if (/*(Build.VERSION.SDK_INT >= 26) &&*/ (fragment.phoneCount > 1)) {
                    if (sim1Exists) {
                        if (fragment.preference.registeredCellDataSIM1 != null) {
                            _registeredCellDataSIM1 = new MobileCellsData(fragment.preference.registeredCellDataSIM1.cellId,
                                    fragment.preference.registeredCellDataSIM1.name,
                                    fragment.preference.registeredCellDataSIM1.connected,
                                    fragment.preference.registeredCellDataSIM1._new,
                                    fragment.preference.registeredCellDataSIM1.lastConnectedTime//,
                                    //fragment.preference.registeredCellDataSIM1.lastRunningEvents,
                                    //fragment.preference.registeredCellDataSIM1.lastPausedEvents,
                                    //fragment.preference.registeredCellDataSIM1.doNotDetect
                            );
                        }
                        _registeredCellInTableSIM1 = fragment.preference.registeredCellInTableSIM1;
                        _registeredCellInValueSIM1 = fragment.preference.registeredCellInValueSIM1;
                    }

                    if (sim2Exists) {
                        if (fragment.preference.registeredCellDataSIM2 != null) {
                            _registeredCellDataSIM2 = new MobileCellsData(fragment.preference.registeredCellDataSIM2.cellId,
                                    fragment.preference.registeredCellDataSIM2.name,
                                    fragment.preference.registeredCellDataSIM2.connected,
                                    fragment.preference.registeredCellDataSIM2._new,
                                    fragment.preference.registeredCellDataSIM2.lastConnectedTime//,
                                    //fragment.preference.registeredCellDataSIM2.lastRunningEvents,
                                    //fragment.preference.registeredCellDataSIM2.lastPausedEvents,
                                    //fragment.preference.registeredCellDataSIM2.doNotDetect
                            );
                        }
                        _registeredCellInTableSIM2 = fragment.preference.registeredCellInTableSIM2;
                        _registeredCellInValueSIM2 = fragment.preference.registeredCellInValueSIM2;
                    }
                } else {
                    if (fragment.preference.registeredCellDataDefault != null) {
                        _registeredCellDataDefault = new MobileCellsData(fragment.preference.registeredCellDataDefault.cellId,
                                fragment.preference.registeredCellDataDefault.name,
                                fragment.preference.registeredCellDataDefault.connected,
                                fragment.preference.registeredCellDataDefault._new,
                                fragment.preference.registeredCellDataDefault.lastConnectedTime//,
                                //fragment.preference.registeredCellDataDefault.lastRunningEvents,
                                //fragment.preference.registeredCellDataDefault.lastPausedEvents,
                                //fragment.preference.registeredCellDataDefault.doNotDetect
                        );
                    }
                    _registeredCellInTableDefault = fragment.preference.registeredCellInTableDefault;
                    _registeredCellInValueDefault = fragment.preference.registeredCellInValueDefault;
                }


                //dataRelativeLayout.setVisibility(View.GONE);
                //progressLinearLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            synchronized (PPApplication.mobileCellsScannerMutex) {
                MobileCellsPreferenceFragment fragment = fragmentWeakRef.get();
                MobileCellsPreference preference = preferenceWeakRef.get();
                Context prefContext = prefContextWeakRef.get();
                if ((fragment != null) && (preference != null) && (prefContext != null)) {

                    if (forRescan) {
                        if ((PhoneProfilesService.getInstance() != null) && (PPApplication.mobileCellsScanner != null)) {
                            PPApplication.mobileCellsScanner.registerCell();

                            //PPApplication.sleep(200);
                        }
                    }

                    // add all from table
                    DatabaseHandler db = DatabaseHandler.getInstance(prefContext.getApplicationContext());
                    db.addMobileCellsToList(_cellsList, 0);

                    _registeredCellDataSIM1 = null;
                    _registeredCellInTableSIM1 = false;
                    _registeredCellInValueSIM1 = false;
                    _registeredCellDataSIM2 = null;
                    _registeredCellInTableSIM2 = false;
                    _registeredCellInValueSIM2 = false;
                    _registeredCellDataDefault = null;
                    _registeredCellInTableDefault = false;
                    _registeredCellInValueDefault = false;

                    if ((PhoneProfilesService.getInstance() != null) && (PPApplication.mobileCellsScanner != null)) {
                        // add registered cell

                        //MobileCellsScanner scanner = PhoneProfilesService.getInstance().getMobileCellsScanner();

                        if (/*(Build.VERSION.SDK_INT >= 26) &&*/ (fragment.phoneCount > 1)) {
                            if (sim1Exists) {
                                if (PPApplication.mobileCellsScanner != null) {
                                    int registeredCell = PPApplication.mobileCellsScanner.getRegisteredCell(1);
                                    long lastConnectedTime = PPApplication.mobileCellsScanner.getLastConnectedTime(1);
                                    for (MobileCellsData cell : _cellsList) {
                                        if (cell.cellId == registeredCell) {
                                            cell.connected = true;
                                            _registeredCellDataSIM1 = cell;
                                            _registeredCellInTableSIM1 = true;
                                            break;
                                        }
                                    }
                                    if (!_registeredCellInTableSIM1 && MobileCellsScanner.isValidCellId(registeredCell) &&
                                            (!_cellName.isEmpty())) {
                                        //synchronized (PPApplication.mobileCellsScannerMutex) {
                                        _registeredCellDataSIM1 = new MobileCellsData(registeredCell,
                                                _cellName, true, true,
                                                lastConnectedTime//,
                                                //MobileCellsScanner.lastRunningEventsNotOutside,
                                                //MobileCellsScanner.lastPausedEventsOutside,
                                                //false
                                        );
                                        _cellsList.add(_registeredCellDataSIM1);
                                        //}
                                    }
                                }
                            }
                            if (sim2Exists) {
                                if (PPApplication.mobileCellsScanner != null) {
                                    int registeredCell = PPApplication.mobileCellsScanner.getRegisteredCell(2);
                                    long lastConnectedTime = PPApplication.mobileCellsScanner.getLastConnectedTime(2);
                                    for (MobileCellsData cell : _cellsList) {
                                        if (cell.cellId == registeredCell) {
                                            cell.connected = true;
                                            _registeredCellDataSIM2 = cell;
                                            _registeredCellInTableSIM2 = true;
                                            break;
                                        }
                                    }
                                    if (!_registeredCellInTableSIM2 && MobileCellsScanner.isValidCellId(registeredCell) &&
                                            (!_cellName.isEmpty())) {
                                        //synchronized (PPApplication.mobileCellsScannerMutex) {
                                        _registeredCellDataSIM2 = new MobileCellsData(registeredCell,
                                                _cellName, true, true,
                                                lastConnectedTime//,
                                                //MobileCellsScanner.lastRunningEventsNotOutside,
                                                //MobileCellsScanner.lastPausedEventsOutside,
                                                //false
                                        );
                                        _cellsList.add(_registeredCellDataSIM2);
                                        //}
                                    }
                                }
                            }
                        } else {
                            if (PPApplication.mobileCellsScanner != null) {
                                int registeredCell = PPApplication.mobileCellsScanner.getRegisteredCell(0);
                                long lastConnectedTime = PPApplication.mobileCellsScanner.getLastConnectedTime(0);
                                for (MobileCellsData cell : _cellsList) {
                                    if (cell.cellId == registeredCell) {
                                        cell.connected = true;
                                        _registeredCellDataDefault = cell;
                                        _registeredCellInTableDefault = true;
                                        break;
                                    }
                                }
                                if (!_registeredCellInTableDefault && MobileCellsScanner.isValidCellId(registeredCell) &&
                                        (!_cellName.isEmpty())) {
                                    //synchronized (PPApplication.mobileCellsScannerMutex) {
                                    _registeredCellDataDefault = new MobileCellsData(registeredCell,
                                            _cellName, true, true,
                                            lastConnectedTime//,
                                            //MobileCellsScanner.lastRunningEventsNotOutside,
                                            //MobileCellsScanner.lastPausedEventsOutside,
                                            //false
                                    );
                                    _cellsList.add(_registeredCellDataDefault);
                                    //}
                                }
                            }
                        }

                    }

                    // add all from value
                    String[] splits = _value.split(StringConstants.STR_SPLIT_REGEX);
                    for (String cell : splits) {
                        if (cell.isEmpty())
                            continue;

                        if (!_cellName.isEmpty()) {
                            boolean found = false;
                            for (MobileCellsData mCell : _cellsList) {
                                if (cell.equals(Integer.toString(mCell.cellId))) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                try {
                                    int iCell = Integer.parseInt(cell);
                                    _cellsList.add(new MobileCellsData(iCell, _cellName,
                                            false, false, 0/*,
                                        "", "", false*/
                                    ));
                                } catch (Exception e) {
                                    //PPApplicationStatic.recordException(e);
                                }
                            }
                        }

                        if (/*(Build.VERSION.SDK_INT >= 26) &&*/ (fragment.phoneCount > 1)) {
                            if (sim1Exists) {
                                if (_registeredCellDataSIM1 != null) {
                                    if (Integer.parseInt(cell) == _registeredCellDataSIM1.cellId)
                                        _registeredCellInValueSIM1 = true;
                                }
                            }
                            if (sim2Exists) {
                                if (_registeredCellDataSIM2 != null) {
                                    if (Integer.parseInt(cell) == _registeredCellDataSIM2.cellId)
                                        _registeredCellInValueSIM2 = true;
                                }
                            }
                        } else {
                            if (_registeredCellDataDefault != null) {
                                if (Integer.parseInt(cell) == _registeredCellDataDefault.cellId)
                                    _registeredCellInValueDefault = true;
                            }
                        }
                    }

                    // save all from value + registeredCell to table
                    db.saveMobileCellsList(_cellsList, true, false);

                    // rename cell added by "plus" icon
                    if (MobileCellsScanner.isValidCellId(renameCellId) && (!_cellName.isEmpty())) {
                        String val = String.valueOf(renameCellId);
                        db.renameMobileCellsList(_cellsList, _cellName, false, val);
                    }

                    if (_sortCellsBy == 0)
                        _cellsList.sort(new SortByNameList());
                    else
                        _cellsList.sort(new SortByConnectionList());

                    _filteredCellsList.clear();
                    for (MobileCellsData cellData : _cellsList) {
                        if (_cellFilterValue.equals(prefContext.getString(R.string.mobile_cell_names_dialog_item_show_selected))) {
                            // add only cell in _value = selected for edited event
                            splits = _value.split(StringConstants.STR_SPLIT_REGEX);
                            for (String cell : splits) {
                                if (cell.equals(Integer.toString(cellData.cellId))) {
                                    _filteredCellsList.add(cellData);
                                    break;
                                }
                            }
                        } else if (_cellFilterValue.equals(prefContext.getString(R.string.mobile_cell_names_dialog_item_show_without_name))) {
                            // add all with empty cell name
                            if (cellData.name.isEmpty())
                                _filteredCellsList.add(cellData);
                        } else if (_cellFilterValue.equals(prefContext.getString(R.string.mobile_cell_names_dialog_item_show_new))) {
                            // add all with _new=true
                            if (cellData._new)
                                _filteredCellsList.add(cellData);
                        } else if (_cellFilterValue.equals(prefContext.getString(R.string.mobile_cell_names_dialog_item_show_all))) {
                            // add all cells
                            _filteredCellsList.add(cellData);
                        } else {
                            // add only cells with filtered cell name
                            if (_cellFilterValue.equals(cellData.name))
                                _filteredCellsList.add(cellData);
                        }
                    }
                }

                return null;
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            MobileCellsPreferenceFragment fragment = fragmentWeakRef.get();
            MobileCellsPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {

                //preference.cellsList = new ArrayList<>(_cellsList);
                preference.filteredCellsList = new ArrayList<>(_filteredCellsList);
                fragment.listAdapter.notifyDataSetChanged();

                if (fragment.cellName.getText().toString().isEmpty()) {
                    boolean found = false;
                    for (MobileCellsData cell : preference.filteredCellsList) {
                        if (preference.isCellSelected(cell.cellId) && (!cell.name.isEmpty())) {
                            // cell name = first selected filtered cell name. (???)
                            fragment.cellName.setText(cell.name);
                            found = true;
                        }
                    }
                    if (!found) {
                        // cell name = event name
                        SharedPreferences sharedPreferences = preference.getSharedPreferences();
                        if (sharedPreferences != null)
                            fragment.cellName.setText(sharedPreferences.getString(Event.PREF_EVENT_NAME, ""));
                    }
                }

                if (/*(Build.VERSION.SDK_INT >= 26) &&*/ (fragment.phoneCount > 1)) {
                    if (sim1Exists) {
                        if (_registeredCellDataSIM1 != null) {
                            preference.registeredCellDataSIM1 = new MobileCellsData(
                                    _registeredCellDataSIM1.cellId,
                                    _registeredCellDataSIM1.name,
                                    _registeredCellDataSIM1.connected,
                                    _registeredCellDataSIM1._new,
                                    _registeredCellDataSIM1.lastConnectedTime//,
                                    //_registeredCellDataSIM1.lastRunningEvents,
                                    //_registeredCellDataSIM1.lastPausedEvents,
                                    //_registeredCellDataSIM1.doNotDetect
                            );
                        }
                        preference.registeredCellInTableSIM1 = _registeredCellInTableSIM1;
                        preference.registeredCellInValueSIM1 = _registeredCellInValueSIM1;
                    }
                    if (sim2Exists) {
                        if (_registeredCellDataSIM2 != null) {
                            preference.registeredCellDataSIM2 = new MobileCellsData(
                                    _registeredCellDataSIM2.cellId,
                                    _registeredCellDataSIM2.name,
                                    _registeredCellDataSIM2.connected,
                                    _registeredCellDataSIM2._new,
                                    _registeredCellDataSIM2.lastConnectedTime//,
                                    //_registeredCellDataSIM2.lastRunningEvents,
                                    //_registeredCellDataSIM2.lastPausedEvents,
                                    //_registeredCellDataSIM2.doNotDetect
                            );
                        }
                        preference.registeredCellInTableSIM2 = _registeredCellInTableSIM2;
                        preference.registeredCellInValueSIM2 = _registeredCellInValueSIM2;
                    }
                } else {
                    if (_registeredCellDataDefault != null) {
                        preference.registeredCellDataDefault = new MobileCellsData(
                                _registeredCellDataDefault.cellId,
                                _registeredCellDataDefault.name,
                                _registeredCellDataDefault.connected,
                                _registeredCellDataDefault._new,
                                _registeredCellDataDefault.lastConnectedTime//,
                                //_registeredCellDataDefault.lastRunningEvents,
                                //_registeredCellDataDefault.lastPausedEvents,
                                //_registeredCellDataDefault.doNotDetect
                        );
                    }
                    preference.registeredCellInTableDefault = _registeredCellInTableDefault;
                    preference.registeredCellInValueDefault = _registeredCellInValueDefault;
                }

                if (/*(Build.VERSION.SDK_INT >= 26) &&*/ (fragment.phoneCount > 1)) {
                    if (sim1Exists) {
                        String connectedCellName = prefContext.getString(R.string.mobile_cells_pref_dlg_connected_cell_sim1) + " ";
                        if (preference.registeredCellDataSIM1 != null) {
                            if (!preference.registeredCellDataSIM1.name.isEmpty())
                                connectedCellName = connectedCellName + preference.registeredCellDataSIM1.name + ", ";
                            String cellFlags = "";
                            if (preference.registeredCellDataSIM1._new)
                                cellFlags = cellFlags + "N";
                            //if (registeredCellData.connected)
                            //    cellFlags = cellFlags + "C";
                            if (!cellFlags.isEmpty())
                                connectedCellName = connectedCellName + "(" + cellFlags + ") ";
                            connectedCellName = connectedCellName + preference.registeredCellDataSIM1.cellId;
                        }
                        fragment.connectedCellSIM1.setText(connectedCellName);
                        GlobalGUIRoutines.setImageButtonEnabled(
                                (preference.registeredCellDataSIM1 != null) &&
                                        (!preference.registeredCellDataSIM1.name.isEmpty()) &&
                                        !(preference.registeredCellInTableSIM1 &&
                                                preference.registeredCellInValueSIM1),
                                fragment.addCellButtonSIM1, prefContext);
                    }
                    if (sim2Exists) {
                        String connectedCellName = prefContext.getString(R.string.mobile_cells_pref_dlg_connected_cell_sim2) + " ";
                        if (preference.registeredCellDataSIM2 != null) {
                            if (!preference.registeredCellDataSIM2.name.isEmpty())
                                connectedCellName = connectedCellName + preference.registeredCellDataSIM2.name + ", ";
                            String cellFlags = "";
                            if (preference.registeredCellDataSIM2._new)
                                cellFlags = cellFlags + "N";
                            //if (registeredCellData.connected)
                            //    cellFlags = cellFlags + "C";
                            if (!cellFlags.isEmpty())
                                connectedCellName = connectedCellName + "(" + cellFlags + ") ";
                            connectedCellName = connectedCellName + preference.registeredCellDataSIM2.cellId;
                        }
                        fragment.connectedCellSIM2.setText(connectedCellName);
                        GlobalGUIRoutines.setImageButtonEnabled(
                                (preference.registeredCellDataSIM2 != null) &&
                                        (!preference.registeredCellDataSIM2.name.isEmpty()) &&
                                        !(preference.registeredCellInTableSIM2 &&
                                                preference.registeredCellInValueSIM2),
                                fragment.addCellButtonSIM2, prefContext);
                    }
                } else {
                    String connectedCellName = prefContext.getString(R.string.mobile_cells_pref_dlg_connected_cell) + " ";
                    if (preference.registeredCellDataDefault != null) {
                        if (!preference.registeredCellDataDefault.name.isEmpty())
                            connectedCellName = connectedCellName + preference.registeredCellDataDefault.name + ", ";
                        String cellFlags = "";
                        if (preference.registeredCellDataDefault._new)
                            cellFlags = cellFlags + "N";
                        //if (registeredCellData.connected)
                        //    cellFlags = cellFlags + "C";
                        if (!cellFlags.isEmpty())
                            connectedCellName = connectedCellName + "(" + cellFlags + ") ";
                        connectedCellName = connectedCellName + preference.registeredCellDataDefault.cellId;
                    }
                    fragment.connectedCellDefault.setText(connectedCellName);
                    GlobalGUIRoutines.setImageButtonEnabled(
                            (preference.registeredCellDataDefault != null) &&
                                    (!preference.registeredCellDataDefault.name.isEmpty()) &&
                                    !(preference.registeredCellInTableDefault &&
                                            preference.registeredCellInValueDefault),
                            fragment.addCellButtonDefault, prefContext);
                }
            }
        }

    }

    private static class DeleteCellNamesFromEventsAsyncTask extends AsyncTask<Void, Integer, Void> {

        private final WeakReference<Context> prefContextWeakRef;
        private final String deletedCellIds;

        public DeleteCellNamesFromEventsAsyncTask(String _deletedCellIds, Context prefContext) {
            this.prefContextWeakRef = new WeakReference<>(prefContext);
            deletedCellIds = _deletedCellIds;
        }

        /*
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        */

        @Override
        protected Void doInBackground(Void... voids) {
            Context prefContext = prefContextWeakRef.get();
            if (prefContext != null) {
                DatabaseHandler db = DatabaseHandler.getInstance(prefContext.getApplicationContext());

                //get list of events with mobile cells sensor
                List<MobileCellsSensorEvent> mobileCellsEventList = new ArrayList<>();
                db.loadMobileCellsSensorEvents(mobileCellsEventList);

                String[] splits = deletedCellIds.split(StringConstants.STR_SPLIT_REGEX);
                for (String cellid : splits) {
                    // cellid = deleted cell
                    // get MobileCellsData from cellid
                    List<MobileCellsData> _cellsList = new ArrayList<>();
                    db.addMobileCellsToList(_cellsList, Integer.parseInt(cellid));


                    for (MobileCellsSensorEvent sensorEvent : mobileCellsEventList) {
                        //sensorEvent = event with mobile cells sensor

                        // delete cellid (_cellList.get(0)) by name from sensorEvent.cellNames
                        String[] splits2 = sensorEvent.cellNames.split(StringConstants.STR_SPLIT_REGEX);
                        sensorEvent.cellNames = "";
                        StringBuilder _value = new StringBuilder();
                        for (String cellName : splits2) {
                            // cellName sensorEvent.cellNames
                            if (!cellName.isEmpty()) {
                                if (!cellName.equals(_cellsList.get(0).name)) {
                                    // cellName is not deleted cellName (cellid), add it
                                    if (_value.length() > 0)
                                        _value.append("|");
                                    _value.append(cellName);
                                }
                            }
                        }
                        sensorEvent.cellNames = _value.toString();

                        // update event with new sensorEvent.cellNames
                        db.updateMobileCellsCells(sensorEvent.eventId, sensorEvent.cellNames);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // restart mobile cells scanner
            Context prefContext = prefContextWeakRef.get();
            if (prefContext != null)
                PPApplicationStatic.restartMobileCellsScanner(prefContext.getApplicationContext());
        }

    }

    private static class RenameCellNamesFromEventsAsyncTask extends AsyncTask<Void, Integer, Void> {

        private final WeakReference<Context> prefContextWeakRef;
        private final String oldCellNames;
        private final String newCellName;

        public RenameCellNamesFromEventsAsyncTask(String _oldCellNames, String _newCellName, Context prefContext) {
            this.prefContextWeakRef = new WeakReference<>(prefContext);
            oldCellNames = _oldCellNames;
            newCellName = _newCellName;
//            Log.e("MobileCellsPreferenceFragment.RenameCellNamesFromEventsAsyncTask", "oldCellNames="+oldCellNames);
//            Log.e("MobileCellsPreferenceFragment.RenameCellNamesFromEventsAsyncTask", "newCellName="+newCellName);
        }

        /*
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        */

        @Override
        protected Void doInBackground(Void... voids) {
            Context prefContext = prefContextWeakRef.get();
            if (prefContext != null) {
                DatabaseHandler db = DatabaseHandler.getInstance(prefContext.getApplicationContext());

                //get list of events with mobile cells sensor
                List<MobileCellsSensorEvent> mobileCellsEventList = new ArrayList<>();
                db.loadMobileCellsSensorEvents(mobileCellsEventList);

                for (MobileCellsSensorEvent sensorEvent : mobileCellsEventList) {
                    //sensorEvent = event with mobile cells sensor

//                    Log.e("MobileCellsPreferenceFragment.RenameCellNamesFromEventsAsyncTask", "------------------------------");
//                    Log.e("MobileCellsPreferenceFragment.RenameCellNamesFromEventsAsyncTask", "old cellNames for sensor=" + sensorEvent.cellNames);

                    StringBuilder _value = new StringBuilder();

                    String[] splits2 = sensorEvent.cellNames.split(StringConstants.STR_SPLIT_REGEX);
                    for (String cellNameFromSensor : splits2) {
//                        Log.e("MobileCellsPreferenceFragment.RenameCellNamesFromEventsAsyncTask", "cellNameFromSensor=" + cellNameFromSensor);

                        boolean renamed = false;

                        if (!cellNameFromSensor.isEmpty()) {
                            String[] splits = oldCellNames.split(StringConstants.STR_SPLIT_REGEX);
                            for (String oldCellName : splits) {
//                                Log.e("MobileCellsPreferenceFragment.RenameCellNamesFromEventsAsyncTask", "oldCellName=" + oldCellName);

                                if (cellNameFromSensor.equals(oldCellName)) {
                                    // renamed cellName is in sensor
                                    renamed = true;
                                    break;
                                }
                            }
                        }

                        if (_value.length() > 0)
                            _value.append("|");

                        if (renamed)
                            // renamed cellName is in sensor
                            _value.append(newCellName);
                        else
                            _value.append(cellNameFromSensor);
                    }

                    sensorEvent.cellNames = _value.toString();
//                    Log.e("MobileCellsPreferenceFragment.RenameCellNamesFromEventsAsyncTask", "new cellNames for sensor="+sensorEvent.cellNames);

                    // update event with new sensorEvent.cellNames
                    db.updateMobileCellsCells(sensorEvent.eventId, sensorEvent.cellNames);
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // restart mobile cells scanner
            Context prefContext = prefContextWeakRef.get();
            if (prefContext != null)
                PPApplicationStatic.restartMobileCellsScanner(prefContext.getApplicationContext());
        }

    }

}
