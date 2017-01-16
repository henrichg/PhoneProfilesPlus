package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NFCTagPreference extends DialogPreference {

    private String value;
    List<NFCTagData> nfcTagList = null;

    Context context;

    private MaterialDialog mDialog;
    private LinearLayout progressLinearLayout;
    private RelativeLayout dataRelativeLayout;
    private EditText nfcTagName;
    private ImageView addIcon;
    private NFCTagPreferenceAdapter listAdapter;

    private AsyncTask<Void, Integer, Void> rescanAsyncTask;

    public NFCTagPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.context = context;

        nfcTagList = new ArrayList<>();
    }

    @Override
    protected void showDialog(Bundle state) {
        value = getPersistedString(value);

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                //.neutralText(R.string.wifi_ssid_pref_dlg_rescan_button)
                .autoDismiss(false)
                .content(getDialogMessage())
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (shouldPersist()) {
                            if (callChangeListener(value))
                            {
                                persistString(value);
                            }
                        }
                        mDialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        mDialog.dismiss();
                    }
                })
                /*.onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        if (Permissions.grantWifiScanDialogPermissions(context, NFCTagPreference.this))
                            refreshListView(true);
                    }
                })*/;

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_nfc_tag_pref_dialog, null);
        onBindDialogView(layout);

        progressLinearLayout = (LinearLayout) layout.findViewById(R.id.nfc_tag_pref_dlg_linla_progress);
        dataRelativeLayout = (RelativeLayout) layout.findViewById(R.id.nfc_tag_pref_dlg_rella_data);

        addIcon = (ImageView)layout.findViewById(R.id.nfc_tag_pref_dlg_addIcon);
        addIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = nfcTagName.getText().toString();
                addNfcTag(tag);
                DatabaseHandler.getInstance(context).addNFCTag(tag);
                refreshListView(false);
            }
        });

        nfcTagName = (EditText) layout.findViewById(R.id.nfc_tag_pref_dlg_bt_name);
        nfcTagName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                addIcon.setEnabled(!nfcTagName.getText().toString().isEmpty());
            }
        });

        addIcon.setEnabled(!nfcTagName.getText().toString().isEmpty());

        ListView nfcTagListView = (ListView) layout.findViewById(R.id.nfc_tag_pref_dlg_listview);
        listAdapter = new NFCTagPreferenceAdapter(context, this);
        nfcTagListView.setAdapter(listAdapter);

        refreshListView(false);

        nfcTagListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //NFCTagPreferenceAdapter.ViewHolder viewHolder =
                //        (NFCTagPreferenceAdapter.ViewHolder) v.getTag();
                String nfcTag = nfcTagList.get(position).name;
                nfcTagName.setText(nfcTag);
            }

        });

        mBuilder.customView(layout, false);

        final TextView helpText = (TextView)layout.findViewById(R.id.nfc_tag_pref_dlg_helpText);
        /*String helpString = context.getString(R.string.pref_dlg_info_about_wildcards_1) + " " +
                            context.getString(R.string.pref_dlg_info_about_wildcards_2) + " " +
                            context.getString(R.string.wifi_ssid_pref_dlg_info_about_wildcards) + " " +
                            context.getString(R.string.pref_dlg_info_about_wildcards_3);
        helpText.setText(helpString);*/

        ImageView helpIcon = (ImageView)layout.findViewById(R.id.nfc_tag_pref_dlg_helpIcon);
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = helpText.getVisibility();
                if (visibility == View.VISIBLE)
                    visibility = View.GONE;
                else
                    visibility = View.VISIBLE;
                helpText.setVisibility(visibility);
            }
        });
        ImageView changeSelectionIcon = (ImageView)layout.findViewById(R.id.nfc_tag_pref_dlg_changeSelection);
        changeSelectionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(context)
                        .title(R.string.pref_dlg_change_selection_title)
                        .items(R.array.nfcTagsChangeSelectionArray)
                        .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                switch (which) {
                                    case 0:
                                        value = "";
                                        break;
                                    case 1:
                                        for (NFCTagData nfcTag : nfcTagList) {
                                            if (nfcTag.name.equals(nfcTagName.getText().toString()))
                                                addNfcTag(nfcTag.name);
                                        }
                                        break;
                                    default:
                                }
                                refreshListView(false);
                                return true;
                            }
                        })
                        .positiveText(R.string.pref_dlg_change_selection_button)
                        .show();
            }
        });

        mDialog = mBuilder.build();
        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        if ((rescanAsyncTask != null) && (!rescanAsyncTask.isCancelled()))
            rescanAsyncTask.cancel(true);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if(restoreValue)
        {
            value = getPersistedString(value);
        }
        else
        {
            value = (String)defaultValue;
            persistString(value);
        }
    }

    /*public String getNfcTags()
    {
        return value;
    }*/

    void addNfcTag(String tag) {
        String[] splits = value.split("\\|");
        boolean found = false;
        for (String _tag : splits) {
            if (_tag.equals(tag))
                found = true;
        }
        if (!found) {
            if (!value.isEmpty())
                value = value + "|";
            value = value + tag;
        }
        //Log.d("WifiSSIDPreference.addSSID","value="+value);
    }

    void removeNfcTag(String tag) {
        String[] splits = value.split("\\|");
        value = "";
        for (String _tag : splits) {
            if (!_tag.isEmpty()) {
                if (!_tag.equals(tag)) {
                    if (!value.isEmpty())
                        value = value + "|";
                    value = value + _tag;
                }
            }
        }
        //Log.d("WifiSSIDPreference.removeSSID","value="+value);
    }

    boolean isNfcTagSelected(String tag) {
        String[] splits = value.split("\\|");
        for (String _tag : splits) {
            if (_tag.equals(tag))
                return true;
        }
        return false;
    }

    public void refreshListView(boolean forRescan)
    {
        final boolean _forRescan = forRescan;

        rescanAsyncTask = new AsyncTask<Void, Integer, Void>() {

            List<NFCTagData> _nfcTagList = null;

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();

                _nfcTagList = new ArrayList<>();

                if (_forRescan) {
                    dataRelativeLayout.setVisibility(View.GONE);
                    progressLinearLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {

                //if (_forRescan)
                //{
                //}

                // add all from db
                List<NFCTag> tags = DatabaseHandler.getInstance(context).getAllNFCTags();
                for (NFCTag tag : tags)
                    _nfcTagList.add(new NFCTagData(tag._name));

                // add all from value
                boolean found;
                String[] splits = value.split("\\|");
                for (String _tag : splits) {
                    if (!_tag.isEmpty()) {
                        found = false;
                        for (NFCTagData tag : _nfcTagList) {
                            if (_tag.equals(tag.name)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            _nfcTagList.add(new NFCTagData(_tag));
                        }
                    }
                }

                Collections.sort(_nfcTagList, new SortList());

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                nfcTagList = new ArrayList<>(_nfcTagList);
                listAdapter.notifyDataSetChanged();

                if (_forRescan) {
                    progressLinearLayout.setVisibility(View.GONE);
                    dataRelativeLayout.setVisibility(View.VISIBLE);
                }

                /*
                for (int position = 0; position < SSIDList.size() - 1; position++) {
                    if (SSIDList.get(position).ssid.equals(value)) {
                        SSIDListView.setSelection(position);
                        SSIDListView.setItemChecked(position, true);
                        SSIDListView.smoothScrollToPosition(position);
                        break;
                    }
                }
                */
            }

        };

        rescanAsyncTask.execute();
    }

    private class SortList implements Comparator<NFCTagData> {

        public int compare(NFCTagData lhs, NFCTagData rhs) {
            return GlobalGUIRoutines.collator.compare(lhs.name, rhs.name);
        }

    }

    public void showEditMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        final Context context = view.getContext();
        PopupMenu popup = new PopupMenu(context, view);
        new MenuInflater(context).inflate(R.menu.nfc_tag_pref_dlg_item_edit, popup.getMenu());

        int tagPos = (int)view.getTag();
        final String tag = nfcTagList.get(tagPos).name;

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nfc_tag_pref_dlg_item_menu_writeToNfcTag:
                        writeToNFCTag(tag);
                        return true;
                    case R.id.nfc_tag_pref_dlg_item_menu_change:
                        if (!nfcTagName.getText().toString().isEmpty()) {
                            String[] splits = value.split("\\|");
                            value = "";
                            boolean found = false;
                            for (String _tag : splits) {
                                if (!_tag.isEmpty()) {
                                    if (!_tag.equals(tag)) {
                                        if (!value.isEmpty())
                                            value = value + "|";
                                        value = value + _tag;
                                    } else
                                        found = true;
                                }
                            }
                            if (found) {
                                if (!value.isEmpty())
                                    value = value + "|";
                                value = value + nfcTagName.getText().toString();
                                DatabaseHandler.getInstance(context).updateNFCTag(tag, nfcTagName.getText().toString());
                            }
                            refreshListView(false);
                        }
                        return true;
                    case R.id.nfc_tag_pref_dlg_item_menu_delete:
                        removeNfcTag(tag);
                        DatabaseHandler.getInstance(context).deleteNFCTag(tag);
                        refreshListView(false);
                        return true;
                    default:
                        return false;
                }
            }
        });


        popup.show();
    }

    private void writeToNFCTag(String tag) {

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if (!nfcAdapter.isEnabled()) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            dialogBuilder.setTitle(R.string.nfc_tag_pref_dlg_menu_writeToNfcTag);
            dialogBuilder.setMessage(R.string.nfc_tag_pref_dlg_writeToNfcTag_nfcNotEnabled);
            dialogBuilder.setPositiveButton(android.R.string.ok, null);
            dialogBuilder.show();
            return;
        }

        Intent nfcTagIntent = new Intent(context.getApplicationContext(), NFCTagWriteActivity.class);
        nfcTagIntent.putExtra(NFCTagWriteActivity.EXTRA_TAG_NAME, tag);
        context.startActivity(nfcTagIntent);
    }

}