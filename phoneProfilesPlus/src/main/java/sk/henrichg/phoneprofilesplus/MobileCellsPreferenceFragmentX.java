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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MobileCellsPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private MobileCellsPreferenceX preference;
    int phoneCount = 1;

    private AlertDialog mRenameDialog;
    private AlertDialog mSelectorDialog;
    private AlertDialog mSortDialog;

    private TextView cellFilter;
    private TextView cellName;
    private TextView connectedCellSIM1;
    private TextView connectedCellSIM2;
    private TextView connectedCellDefault;
    private MobileCellsPreferenceAdapterX listAdapter;
    private MobileCellNamesDialogX mMobileCellsFilterDialog;
    private MobileCellNamesDialogX mMobileCellNamesDialog;
    private AppCompatImageButton addCellButtonSIM1;
    private AppCompatImageButton addCellButtonSIM2;
    private AppCompatImageButton addCellButtonDefault;
    private RelativeLayout locationSystemSettingsRelLa;
    private TextView locationEnabledStatusTextView;
    private AppCompatImageButton locationSystemSettingsButton;
    private Button rescanButton;

    private RefreshListViewAsyncTask rescanAsyncTask;

    private RefreshListViewBroadcastReceiver refreshListViewBroadcastReceiver;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (MobileCellsPreferenceX) getPreference();
        preference.fragment = this;

        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            phoneCount = telephonyManager.getPhoneCount();
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_mobile_cells_preference, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        //Log.e("MobileCellsPreferenceFragmentX.onBindDialogView", "xxx");

        refreshListViewBroadcastReceiver = new MobileCellsPreferenceFragmentX.RefreshListViewBroadcastReceiver(/*preference*/);
        LocalBroadcastManager.getInstance(prefContext).registerReceiver(refreshListViewBroadcastReceiver,
                new IntentFilter(PPApplication.PACKAGE_NAME + ".MobileCellsPreference_refreshListView"));

        PPApplication.forceStartMobileCellsScanner(prefContext);
        MobileCellsPreferenceX.forceStart = true;

        cellFilter = view.findViewById(R.id.mobile_cells_pref_dlg_cells_filter_name);
        if ((preference.cellFilter == null) || preference.cellFilter.isEmpty()) {
            if (preference.value.isEmpty())
                cellFilter.setText(R.string.mobile_cell_names_dialog_item_show_all);
            else
                cellFilter.setText(R.string.mobile_cell_names_dialog_item_show_selected);
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
        listAdapter = new MobileCellsPreferenceAdapterX(prefContext, preference);
        cellsListView.setAdapter(listAdapter);

        //refreshListView(false);

        /*
        cellsListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                cellName.setText(cellsList.get(position).name);
            }

        });
        */

        mMobileCellsFilterDialog = new MobileCellNamesDialogX((Activity)prefContext, preference, true);
        cellFilter.setOnClickListener(view1 -> {
            if (getActivity() != null)
                if (!getActivity().isFinishing())
                    mMobileCellsFilterDialog.show();
        });

        mMobileCellNamesDialog = new MobileCellNamesDialogX((Activity)prefContext, preference, false);
        cellName.setOnClickListener(view12 -> {
            if (getActivity() != null)
                if (!getActivity().isFinishing())
                    mMobileCellNamesDialog.show();
        });

        final AppCompatImageButton editIcon = view.findViewById(R.id.mobile_cells_pref_dlg_rename);
        TooltipCompat.setTooltipText(editIcon, getString(R.string.mobile_cells_pref_dlg_rename_cell_button_tooltip));
        editIcon.setOnClickListener(v -> {
            if (getActivity() != null)
                if (!getActivity().isFinishing()) {
                    mRenameDialog = new AlertDialog.Builder(prefContext)
                            .setTitle(R.string.mobile_cells_pref_dlg_cell_rename_title)
                            .setCancelable(true)
                            .setNegativeButton(android.R.string.cancel, null)
                            //.setSingleChoiceItems(R.array.mobileCellsRenameArray, 0, new DialogInterface.OnClickListener() {
                            .setItems(R.array.mobileCellsRenameArray, (dialog, which) -> {
                                final DatabaseHandler db = DatabaseHandler.getInstance(prefContext);
                                switch (which) {
                                    case 0:
                                    case 1:
                                        db.renameMobileCellsList(preference.filteredCellsList, cellName.getText().toString(), which == 0, preference.value);
                                        break;
                                    case 2:
                                        db.renameMobileCellsList(preference.filteredCellsList, cellName.getText().toString(), false, null);
                                        break;
                                }
                                refreshListView(false, Integer.MAX_VALUE);
                                //dialog.dismiss();
                            })
                            .create();

//                    mRenameDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                        @Override
//                        public void onShow(DialogInterface dialog) {
//                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (positive != null) positive.setAllCaps(false);
//                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                            if (negative != null) negative.setAllCaps(false);
//                        }
//                    });

                      mRenameDialog.show();
                }
        });
        AppCompatImageButton changeSelectionIcon = view.findViewById(R.id.mobile_cells_pref_dlg_changeSelection);
        TooltipCompat.setTooltipText(changeSelectionIcon, getString(R.string.mobile_cells_pref_dlg_select_button_tooltip));
        changeSelectionIcon.setOnClickListener(view13 -> {
            if (getActivity() != null)
                if (!getActivity().isFinishing()) {
                    mSelectorDialog = new AlertDialog.Builder(prefContext)
                            .setTitle(R.string.pref_dlg_change_selection_title)
                            .setCancelable(true)
                            .setNegativeButton(android.R.string.cancel, null)
                            //.setSingleChoiceItems(R.array.mobileCellsChangeSelectionArray, 0, new DialogInterface.OnClickListener() {
                            .setItems(R.array.mobileCellsChangeSelectionArray, (dialog, which) -> {
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
                            })
                            .create();

//                    mSelectorDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                        @Override
//                        public void onShow(DialogInterface dialog) {
//                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (positive != null) positive.setAllCaps(false);
//                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                            if (negative != null) negative.setAllCaps(false);
//                        }
//                    });

                    mSelectorDialog.show();
                }
        });
        final AppCompatImageButton sortIcon = view.findViewById(R.id.mobile_cells_pref_dlg_sort);
        TooltipCompat.setTooltipText(sortIcon, getString(R.string.mobile_cells_pref_dlg_button_tooltip));
        sortIcon.setOnClickListener(v -> {
            if (getActivity() != null)
                if (!getActivity().isFinishing()) {
                    mSortDialog = new AlertDialog.Builder(prefContext)
                            .setTitle(R.string.mobile_cells_pref_dlg_cell_sort_title)
                            .setCancelable(true)
                            .setNegativeButton(android.R.string.cancel, null)
                            .setSingleChoiceItems(R.array.mobileCellsSortArray, preference.sortCellsBy, (dialog, which) -> {
                                preference.sortCellsBy = which;
                                //PPApplication.logE("MobileCellsPreferenceFragmentX.sortIcon.onClickListener", "sortCellsBy="+preference.sortCellsBy);
                                refreshListView(false, Integer.MAX_VALUE);
                                dialog.dismiss();
                            })
                            .create();

//                    mSortDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                        @Override
//                        public void onShow(DialogInterface dialog) {
//                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (positive != null) positive.setAllCaps(false);
//                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                            if (negative != null) negative.setAllCaps(false);
//                        }
//                    });

                    mSortDialog.show();
                }
        });

        final AppCompatImageButton helpIcon = view.findViewById(R.id.mobile_cells_pref_dlg_helpIcon);
        TooltipCompat.setTooltipText(helpIcon, getString(R.string.help_button_tooltip));
        helpIcon.setOnClickListener(v -> DialogHelpPopupWindowX.showPopup(helpIcon, R.string.menu_help, (Activity)prefContext, /*getDialog(),*/ R.string.mobile_cells_pref_dlg_help, false));

        rescanButton = view.findViewById(R.id.mobile_cells_pref_dlg_rescanButton);
        if (PPApplication.HAS_FEATURE_TELEPHONY) {
            TelephonyManager telephonyManager = (TelephonyManager) prefContext.getSystemService(Context.TELEPHONY_SERVICE);
            boolean simIsReady = false;
            if (telephonyManager != null) {
                if (Build.VERSION.SDK_INT < 26) {
                    if (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY)
                        // sim card is ready
                        simIsReady = true;
                } else {
                    if (Permissions.checkPhone(prefContext.getApplicationContext())) {
                        SubscriptionManager mSubscriptionManager = (SubscriptionManager) prefContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                        //PPApplication.logE("MobileCellsPreferenceFragmentX.onBindDialogView", "mSubscriptionManager=" + mSubscriptionManager);
                        //SubscriptionManager.from(context);
                        if (mSubscriptionManager != null) {
                            List<SubscriptionInfo> subscriptionList = null;
                            try {
                                // Loop through the subscription list i.e. SIM list.
                                subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                            } catch (SecurityException e) {
                                PPApplication.recordException(e);
                                //PPApplication.logE("MobileCellsPreferenceFragmentX.onBindDialogView", Log.getStackTraceString(e));
                            }
                            //PPApplication.logE("MobileCellsPreferenceFragmentX.onBindDialogView", "subscriptionList=" + subscriptionList);
                            if (subscriptionList != null) {
                                //PPApplication.logE("MobileCellsPreferenceFragmentX.onBindDialogView", "subscriptionList.size()=" + subscriptionList.size());
                                for (int i = 0; i < subscriptionList.size();/*mSubscriptionManager.getActiveSubscriptionInfoCountMax();*/ i++) {
                                    // Get the active subscription ID for a given SIM card.
                                    SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                                    //PPApplication.logE("MobileCellsPreferenceFragmentX.onBindDialogView", "subscriptionInfo=" + subscriptionInfo);
                                    if (subscriptionInfo != null) {
                                        int slotIndex = subscriptionInfo.getSimSlotIndex();
                                        //PPApplication.logE("MobileCellsPreferenceFragmentX.onBindDialogView", "slotIndex=" + slotIndex);
                                        //PPApplication.logE("MobileCellsPreferenceFragmentX.onBindDialogView", "telephonyManager.getSimState(slotIndex)=" + telephonyManager.getSimState(slotIndex));
                                        if (telephonyManager.getSimState(slotIndex) == TelephonyManager.SIM_STATE_READY) {
                                            // sim card is ready
                                            //PPApplication.logE("MobileCellsPreferenceFragmentX.onBindDialogView", "sim card is ready");
                                            simIsReady = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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

        if ((Build.VERSION.SDK_INT >= 26) && (phoneCount > 1)) {
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
        synchronized (PPApplication.simCardsMutext) {
            sim1Exists = PPApplication.simCardsMutext.simCardsDetected;
            sim2Exists = sim1Exists;
            sim1Exists = sim1Exists && PPApplication.simCardsMutext.sim1Exists;
            sim2Exists = sim2Exists && PPApplication.simCardsMutext.sim2Exists;
        }

        RelativeLayout connectedCellRelLa;
        //noinspection IfStatementWithIdenticalBranches
        if ((Build.VERSION.SDK_INT >= 26) && (phoneCount > 1)) {
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
        //Log.e("MobileCellsPreferenceFragmentX.onDialogClosed", "xxx");

        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        if ((mRenameDialog != null) && mRenameDialog.isShowing())
            mRenameDialog.dismiss();
        if ((mSelectorDialog != null) && mSelectorDialog.isShowing())
            mSelectorDialog.dismiss();
        if ((mSortDialog != null) && mSortDialog.isShowing())
            mSortDialog.dismiss();

        if ((rescanAsyncTask != null) && (!rescanAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)))
            rescanAsyncTask.cancel(true);

        if (refreshListViewBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(prefContext).unregisterReceiver(refreshListViewBroadcastReceiver);
            refreshListViewBroadcastReceiver = null;
        }

        MobileCellsPreferenceX.forceStart = false;
        PPApplication.restartMobileCellsScanner(prefContext);

        preference.fragment = null;
    }

    void setLocationEnableStatus() {
        if (Build.VERSION.SDK_INT >= 28) {
            String statusText;
            if (!PhoneProfilesService.isLocationEnabled(prefContext)) {
                /*if (Build.VERSION.SDK_INT < 28)
                    statusText = prefContext.getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
                            prefContext.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary);
                else*/
                    statusText = prefContext.getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ":\n" +
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
                                getActivity().startActivityForResult(intent, EventsPrefsFragment.RESULT_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
                                ok = true;
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }
                        if (!ok) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(prefContext);
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();

//                                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                    @Override
//                                    public void onShow(DialogInterface dialog) {
//                                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                        if (positive != null) positive.setAllCaps(false);
//                                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                        if (negative != null) negative.setAllCaps(false);
//                                    }
//                                });

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

    @SuppressLint("StaticFieldLeak")
    public void refreshListView(final boolean forRescan, final int renameCellId)
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
            }
            else
                return 0;
        }

    }

    public void showEditMenu(View view)
    {
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
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder.setTitle(getString(R.string.profile_context_item_delete));
                    dialogBuilder.setMessage(getString(R.string.delete_mobile_cell_alert_message));
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(R.string.alert_button_yes, (dialog, which) -> {
                        DatabaseHandler db = DatabaseHandler.getInstance(_context);
                        db.deleteMobileCell(cellId);
                        preference.removeCellId(cellId);
                        refreshListView(false, Integer.MAX_VALUE);
                    });
                    dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
                    AlertDialog dialog = dialogBuilder.create();

                    //        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    //            @Override
                    //            public void onShow(DialogInterface dialog) {
                    //                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    //                if (positive != null) positive.setAllCaps(false);
                    //                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                    //                if (negative != null) negative.setAllCaps(false);
                    //            }
                    //        });

                    if ((getActivity() != null) && (!getActivity().isFinishing()))
                        dialog.show();
                }
                return true;
            }
            if (itemId == R.id.mobile_cells_pref_item_menu_delete_all_selected) {
                if (getActivity() != null) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder.setTitle(getString(R.string.profile_context_item_delete));
                    dialogBuilder.setMessage(getString(R.string.delete_selected_mobile_cells_alert_message));
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(R.string.alert_button_yes, (dialog, which) -> {
                        DatabaseHandler db = DatabaseHandler.getInstance(_context);
                        for (MobileCellsData cell : preference.filteredCellsList) {
                            String[] splits = preference.value.split("\\|");
                            for (String valueCell : splits) {
                                if (valueCell.equals(Integer.toString(cell.cellId))) {
                                    db.deleteMobileCell(cell.cellId);
                                    preference.removeCellId(cell.cellId);
                                }
                            }
                        }
                        refreshListView(false, Integer.MAX_VALUE);
                    });
                    dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
                    AlertDialog dialog = dialogBuilder.create();

                    //        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    //            @Override
                    //            public void onShow(DialogInterface dialog) {
                    //                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    //                if (positive != null) positive.setAllCaps(false);
                    //                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                    //                if (negative != null) negative.setAllCaps(false);
                    //            }
                    //        });

                    if ((getActivity() != null) && (!getActivity().isFinishing()))
                        dialog.show();
                }
                return true;
            }
            else {
                return false;
            }
        });

        if (getActivity() != null)
            if (!getActivity().isFinishing())
                popup.show();
    }

    public class RefreshListViewBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent) {
//            PPApplication.logE("[IN_BROADCAST] MobileCellsPreferenceFragmentX.RefreshListViewBroadcastReceiver", "xxx");
            //if (preference != null)
            //    preference.refreshListView(false, Integer.MAX_VALUE);
            refreshListView(false, Integer.MAX_VALUE);
        }
    }

    void setCellNameText(String text) {
        cellName.setText(text);
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

        private final WeakReference<MobileCellsPreferenceX> preferenceWeakRef;
        private final WeakReference<MobileCellsPreferenceFragmentX> fragmentWeakRef;
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
                                        MobileCellsPreferenceX preference,
                                        MobileCellsPreferenceFragmentX fragment,
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

            MobileCellsPreferenceFragmentX fragment = fragmentWeakRef.get();
            if (fragment != null) {

                synchronized (PPApplication.simCardsMutext) {
                    sim1Exists = PPApplication.simCardsMutext.simCardsDetected;
                    sim2Exists = sim1Exists;
                    sim1Exists = sim1Exists && PPApplication.simCardsMutext.sim1Exists;
                    sim2Exists = sim2Exists && PPApplication.simCardsMutext.sim2Exists;
                }

                _cellName = fragment.cellName.getText().toString();
                _cellsList = new ArrayList<>();
                _filteredCellsList = new ArrayList<>();
                _cellFilterValue = fragment.cellFilter.getText().toString();
                _value = fragment.preference.value;
                _sortCellsBy = fragment.preference.sortCellsBy;

                if ((Build.VERSION.SDK_INT >= 26) && (fragment.phoneCount > 1)) {
                    if (sim1Exists) {
                        if (fragment.preference.registeredCellDataSIM1 != null) {
                            _registeredCellDataSIM1 = new MobileCellsData(fragment.preference.registeredCellDataSIM1.cellId,
                                    fragment.preference.registeredCellDataSIM1.name,
                                    fragment.preference.registeredCellDataSIM1.connected,
                                    fragment.preference.registeredCellDataSIM1._new,
                                    fragment.preference.registeredCellDataSIM1.lastConnectedTime,
                                    fragment.preference.registeredCellDataSIM1.lastRunningEvents,
                                    fragment.preference.registeredCellDataSIM1.lastPausedEvents,
                                    fragment.preference.registeredCellDataSIM1.doNotDetect);
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
                                    fragment.preference.registeredCellDataSIM2.lastConnectedTime,
                                    fragment.preference.registeredCellDataSIM2.lastRunningEvents,
                                    fragment.preference.registeredCellDataSIM2.lastPausedEvents,
                                    fragment.preference.registeredCellDataSIM2.doNotDetect);
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
                                fragment.preference.registeredCellDataDefault.lastConnectedTime,
                                fragment.preference.registeredCellDataDefault.lastRunningEvents,
                                fragment.preference.registeredCellDataDefault.lastPausedEvents,
                                fragment.preference.registeredCellDataDefault.doNotDetect);
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
                MobileCellsPreferenceFragmentX fragment = fragmentWeakRef.get();
                MobileCellsPreferenceX preference = preferenceWeakRef.get();
                Context prefContext = prefContextWeakRef.get();
                if ((fragment != null) && (preference != null) && (prefContext != null)) {

//                    Log.e("MobileCellsPreferenceFragmentX.refreshListView", "in doInBackground");
//                    Log.e("MobileCellsPreferenceFragmentX.refreshListView", "PPApplication.mobileCellsScanner="+PPApplication.mobileCellsScanner);

                    if (forRescan) {
//                        PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", " **** forRescan");
                        if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isMobileCellsScannerStarted()) {
//                            PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", " **** registerCell");
//                            Log.e("MobileCellsPreferenceFragmentX.refreshListView", "scanner started");
                            PhoneProfilesService.getInstance().getMobileCellsScanner().registerCell();

                            //PPApplication.sleep(200);
                        }
                    }

//                    if (MobileCellsScanner.isValidCellId(MobileCellsScanner.registeredCell))
//                        PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", " **** registeredCell="+MobileCellsScanner.registeredCell);
//                    else
//                        PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "**** registeredCell=NOT valid");

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

                    if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isMobileCellsScannerStarted()) {
                        // add registered cell
                        //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "search registered cell from scanner");

//                        Log.e("MobileCellsPreferenceFragmentX.refreshListView", "scanner started");

                        MobileCellsScanner scanner = PhoneProfilesService.getInstance().getMobileCellsScanner();

                        if ((Build.VERSION.SDK_INT >= 26) && (fragment.phoneCount > 1)) {
                            if (sim1Exists) {
                                int registeredCell = scanner.getRegisteredCell(1);
//                                Log.e("MobileCellsPreferenceFragmentX.refreshListView", "registeredCell from sim 1 ="+registeredCell);
                                long lastConnectedTime = scanner.getLastConnectedTime(1);
                                for (MobileCellsData cell : _cellsList) {
                                    if (cell.cellId == registeredCell) {
                                        cell.connected = true;
                                        _registeredCellDataSIM1 = cell;
                                        _registeredCellInTableSIM1 = true;
                                        //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add registered cell from scanner - found");
                                        break;
                                    }
                                }
                                if (!_registeredCellInTableSIM1 && MobileCellsScanner.isValidCellId(registeredCell)) {
                                    //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add registered cell from scanner - not found - add it to list");
                                    synchronized (PPApplication.mobileCellsScannerMutex) {
                                        _registeredCellDataSIM1 = new MobileCellsData(registeredCell,
                                                _cellName, true, true,
                                                lastConnectedTime,
                                                MobileCellsScanner.lastRunningEventsNotOutside,
                                                MobileCellsScanner.lastPausedEventsOutside,
                                                false);
                                        _cellsList.add(_registeredCellDataSIM1);
                                    }
                                }
                            }
                            if (sim2Exists) {
                                int registeredCell = scanner.getRegisteredCell(2);
//                                Log.e("MobileCellsPreferenceFragmentX.refreshListView", "registeredCell from sim 2 ="+registeredCell);
                                long lastConnectedTime = scanner.getLastConnectedTime(2);
                                for (MobileCellsData cell : _cellsList) {
                                    if (cell.cellId == registeredCell) {
                                        cell.connected = true;
                                        _registeredCellDataSIM2 = cell;
                                        _registeredCellInTableSIM2 = true;
                                        //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add registered cell from scanner - found");
                                        break;
                                    }
                                }
                                if (!_registeredCellInTableSIM2 && MobileCellsScanner.isValidCellId(registeredCell)) {
                                    //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add registered cell from scanner - not found - add it to list");
                                    synchronized (PPApplication.mobileCellsScannerMutex) {
                                        _registeredCellDataSIM2 = new MobileCellsData(registeredCell,
                                                _cellName, true, true,
                                                lastConnectedTime,
                                                MobileCellsScanner.lastRunningEventsNotOutside,
                                                MobileCellsScanner.lastPausedEventsOutside,
                                                false);
                                        _cellsList.add(_registeredCellDataSIM2);
                                    }
                                }
                            }
                        } else {
                            int registeredCell = scanner.getRegisteredCell(0);
//                            Log.e("MobileCellsPreferenceFragmentX.refreshListView", "registeredCell from default="+registeredCell);
                            long lastConnectedTime = scanner.getLastConnectedTime(0);
                            for (MobileCellsData cell : _cellsList) {
                                if (cell.cellId == registeredCell) {
                                    cell.connected = true;
                                    _registeredCellDataDefault = cell;
                                    _registeredCellInTableDefault = true;
                                    //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add registered cell from scanner - found");
                                    break;
                                }
                            }
                            if (!_registeredCellInTableDefault && MobileCellsScanner.isValidCellId(registeredCell)) {
                                //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add registered cell from scanner - not found - add it to list");
                                synchronized (PPApplication.mobileCellsScannerMutex) {
                                    _registeredCellDataDefault = new MobileCellsData(registeredCell,
                                            _cellName, true, true,
                                            lastConnectedTime,
                                            MobileCellsScanner.lastRunningEventsNotOutside,
                                            MobileCellsScanner.lastPausedEventsOutside,
                                            false);
                                    _cellsList.add(_registeredCellDataDefault);
                                }
                            }
                        }

                        /*if (!_registeredCellInTable) {
                            PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add registered cell from scanner - NOT added into list");
                        }
                        else {
                            PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add registered cell from scanner - registeredCellData.cellId="+_registeredCellData.cellId);
                        }*/
                    }

                    // add all from value
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "search cells from preference value");
                        PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "_value=" + _value);
                    }*/
                    String[] splits = _value.split("\\|");
                    for (String cell : splits) {
                        if (cell.isEmpty())
                            continue;

                        boolean found = false;
                        for (MobileCellsData mCell : _cellsList) {
                            if (cell.equals(Integer.toString(mCell.cellId))) {
                                found = true;
                                //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells from preference value - found");
                                break;
                            }
                        }
                        if (!found) {
                            try {
                                int iCell = Integer.parseInt(cell);
                                _cellsList.add(new MobileCellsData(iCell, _cellName,
                                        false, false, 0,
                                        "", "", false));
                                //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells from preference value - not found - add it to list");
                            } catch (Exception e) {
                                //PPApplication.recordException(e);
                            }
                        }

                        if ((Build.VERSION.SDK_INT >= 26) && (fragment.phoneCount > 1)) {
                            if (sim1Exists) {
                                if (_registeredCellDataSIM1 != null) {
                                    //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells from preference value - registeredCellData.cellId="+registeredCellData.cellId);
                                    //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells from preference value - cell="+cell);
                                    if (Integer.parseInt(cell) == _registeredCellDataSIM1.cellId)
                                        _registeredCellInValueSIM1 = true;
                                }
                            }
                            if (sim2Exists) {
                                if (_registeredCellDataSIM2 != null) {
                                    //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells from preference value - registeredCellData.cellId="+registeredCellData.cellId);
                                    //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells from preference value - cell="+cell);
                                    if (Integer.parseInt(cell) == _registeredCellDataSIM2.cellId)
                                        _registeredCellInValueSIM2 = true;
                                }
                            }
                        } else {
                            if (_registeredCellDataDefault != null) {
                                //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells from preference value - registeredCellData.cellId="+registeredCellData.cellId);
                                //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells from preference value - cell="+cell);
                                if (Integer.parseInt(cell) == _registeredCellDataDefault.cellId)
                                    _registeredCellInValueDefault = true;
                            }
                        }
                    }
                    /*if (PPApplication.logEnabled()) {
                        if (!_registeredCellInValue) {
                            PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells from preference value - registered cell is NOT in value");
                        } else {
                            PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells from preference value - registered cell is in value");
                        }
                    }*/

                    // save all from value + registeredCell to table
                    db.saveMobileCellsList(_cellsList, true, false);

                    // rename cell added by "plus" icon
                    if (MobileCellsScanner.isValidCellId(renameCellId)) {
                        String val = String.valueOf(renameCellId);
                        db.renameMobileCellsList(_cellsList, _cellName, false, val);
                    }

                    if (_sortCellsBy == 0)
                        //noinspection Java8ListSort
                        Collections.sort(_cellsList, new SortByNameList());
                    else
                        //noinspection Java8ListSort
                        Collections.sort(_cellsList, new SortByConnectionList());


                    //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells into filtered list");
                    _filteredCellsList.clear();
                    splits = _value.split("\\|");
                    for (MobileCellsData cellData : _cellsList) {
                        if (_cellFilterValue.equals(prefContext.getString(R.string.mobile_cell_names_dialog_item_show_selected))) {
                            for (String cell : splits) {
                                if (cell.equals(Integer.toString(cellData.cellId))) {
                                    //PPApplication.logE("MobileCellsPreferenceFragmentX.refreshListView", "add cells into filtered list - added selected cellId="+cellData.cellId);
                                    _filteredCellsList.add(cellData);
                                    break;
                                }
                            }
                        } else if (_cellFilterValue.equals(prefContext.getString(R.string.mobile_cell_names_dialog_item_show_without_name))) {
                            if (cellData.name.isEmpty())
                                _filteredCellsList.add(cellData);
                        } else if (_cellFilterValue.equals(prefContext.getString(R.string.mobile_cell_names_dialog_item_show_new))) {
                            if (cellData._new)
                                _filteredCellsList.add(cellData);
                        } else if (_cellFilterValue.equals(prefContext.getString(R.string.mobile_cell_names_dialog_item_show_all))) {
                            _filteredCellsList.add(cellData);
                        } else {
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

            MobileCellsPreferenceFragmentX fragment = fragmentWeakRef.get();
            MobileCellsPreferenceX preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {

                preference.cellsList = new ArrayList<>(_cellsList);
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
                        fragment.cellName.setText(sharedPreferences.getString(Event.PREF_EVENT_NAME, ""));
                    }
                }

                if ((Build.VERSION.SDK_INT >= 26) && (fragment.phoneCount > 1)) {
                    if (sim1Exists) {
                        if (_registeredCellDataSIM1 != null) {
                            preference.registeredCellDataSIM1 = new MobileCellsData(_registeredCellDataSIM1.cellId,
                                    _registeredCellDataSIM1.name,
                                    _registeredCellDataSIM1.connected,
                                    _registeredCellDataSIM1._new,
                                    _registeredCellDataSIM1.lastConnectedTime,
                                    _registeredCellDataSIM1.lastRunningEvents,
                                    _registeredCellDataSIM1.lastPausedEvents,
                                    _registeredCellDataSIM1.doNotDetect);
                        }
                        preference.registeredCellInTableSIM1 = _registeredCellInTableSIM1;
                        preference.registeredCellInValueSIM1 = _registeredCellInValueSIM1;
                    }
                    if (sim2Exists) {
                        if (_registeredCellDataSIM2 != null) {
                            preference.registeredCellDataSIM2 = new MobileCellsData(_registeredCellDataSIM2.cellId,
                                    _registeredCellDataSIM2.name,
                                    _registeredCellDataSIM2.connected,
                                    _registeredCellDataSIM2._new,
                                    _registeredCellDataSIM2.lastConnectedTime,
                                    _registeredCellDataSIM2.lastRunningEvents,
                                    _registeredCellDataSIM2.lastPausedEvents,
                                    _registeredCellDataSIM2.doNotDetect);
                        }
                        preference.registeredCellInTableSIM2 = _registeredCellInTableSIM2;
                        preference.registeredCellInValueSIM2 = _registeredCellInValueSIM2;
                    }
                } else {
                    if (_registeredCellDataDefault != null) {
                        preference.registeredCellDataDefault = new MobileCellsData(_registeredCellDataDefault.cellId,
                                _registeredCellDataDefault.name,
                                _registeredCellDataDefault.connected,
                                _registeredCellDataDefault._new,
                                _registeredCellDataDefault.lastConnectedTime,
                                _registeredCellDataDefault.lastRunningEvents,
                                _registeredCellDataDefault.lastPausedEvents,
                                _registeredCellDataDefault.doNotDetect);
                    }
                    preference.registeredCellInTableDefault = _registeredCellInTableDefault;
                    preference.registeredCellInValueDefault = _registeredCellInValueDefault;
                }

                if ((Build.VERSION.SDK_INT >= 26) && (fragment.phoneCount > 1)) {
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
                        GlobalGUIRoutines.setImageButtonEnabled((preference.registeredCellDataSIM1 != null) &&
                                        !(preference.registeredCellInTableSIM1 && preference.registeredCellInValueSIM1),
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
                        GlobalGUIRoutines.setImageButtonEnabled((preference.registeredCellDataSIM2 != null) &&
                                        !(preference.registeredCellInTableSIM2 && preference.registeredCellInValueSIM2),
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
                    GlobalGUIRoutines.setImageButtonEnabled((preference.registeredCellDataDefault != null) &&
                                    !(preference.registeredCellInTableDefault && preference.registeredCellInValueDefault),
                            fragment.addCellButtonDefault, prefContext);
                }
            }
        }

    }

}
