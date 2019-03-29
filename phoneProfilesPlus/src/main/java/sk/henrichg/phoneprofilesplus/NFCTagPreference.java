package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;

public class NFCTagPreference extends DialogPreference {

    private String value;
    List<NFCTag> nfcTagList;

    private final Context context;

    private AlertDialog mDialog;
    private AlertDialog mSelectorDialog;
    //private LinearLayout progressLinearLayout;
    //private RelativeLayout dataRelativeLayout;
    private ListView nfcTagListView;
    private EditText nfcTagName;
    private AppCompatImageButton addIcon;
    private NFCTagPreferenceAdapter listAdapter;

    private AsyncTask<Void, Integer, Void> rescanAsyncTask;

    //static final int RESULT_NFC_TAG_READ_EDITOR = 3500;
    static final int RESULT_NFC_TAG_WRITE = 3501;

    //private static final String PREF_SHOW_HELP = "nfc_tag_pref_show_help";

    public NFCTagPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.context = context;

        nfcTagList = new ArrayList<>();
    }

    @Override
    protected void showDialog(Bundle state) {
        value = getPersistedString(value);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(getDialogTitle());
        dialogBuilder.setIcon(getDialogIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(getNegativeButtonText(), null);
        dialogBuilder.setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
            @SuppressWarnings("StringConcatenationInLoop")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (shouldPersist()) {
                    if (callChangeListener(value))
                    {
                        persistString(value);
                    }
                }
            }
        });

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_nfc_tag_pref_dialog, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        //progressLinearLayout = layout.findViewById(R.id.nfc_tag_pref_dlg_linla_progress);
        //dataRelativeLayout = layout.findViewById(R.id.nfc_tag_pref_dlg_rella_data);

        //noinspection ConstantConditions
        addIcon = layout.findViewById(R.id.nfc_tag_pref_dlg_addIcon);
        addIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tagName = nfcTagName.getText().toString();

                /*addNfcTag(tagName);
                NFCTag tag = new NFCTag(0, tagName, "");
                DatabaseHandler.getInstance(context).addNFCTag(tag);
                refreshListView(tagName);*/

                /*Intent nfcTagIntent = new Intent(context.getApplicationContext(), NFCTagReadEditorActivity.class);
                nfcTagIntent.putExtra(NFCTagReadEditorActivity.EXTRA_TAG_NAME, tag);
                ((Activity)context).startActivityForResult(nfcTagIntent, RESULT_NFC_TAG_READ_EDITOR);*/

                writeToNFCTag(0, tagName);
            }
        });

        //noinspection ConstantConditions
        nfcTagName = layout.findViewById(R.id.nfc_tag_pref_dlg_bt_name);
        nfcTagName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                GlobalGUIRoutines.setImageButtonEnabled(!nfcTagName.getText().toString().isEmpty(),
                        addIcon, R.drawable.ic_button_add, context.getApplicationContext());
            }
        });

        GlobalGUIRoutines.setImageButtonEnabled(!nfcTagName.getText().toString().isEmpty(),
                addIcon, R.drawable.ic_button_add, context.getApplicationContext());

        nfcTagListView = layout.findViewById(R.id.nfc_tag_pref_dlg_listview);
        listAdapter = new NFCTagPreferenceAdapter(context, this);
        nfcTagListView.setAdapter(listAdapter);

        refreshListView("");

        nfcTagListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //NFCTagPreferenceAdapter.ViewHolder viewHolder =
                //        (NFCTagPreferenceAdapter.ViewHolder) v.getTag();
                String nfcTag = nfcTagList.get(position)._name;
                nfcTagName.setText(nfcTag);
            }

        });

        /*
        final TextView helpText = layout.findViewById(R.id.nfc_tag_pref_dlg_helpText);
        final ImageView helpIcon = layout.findViewById(R.id.nfc_tag_pref_dlg_helpIcon);
        ApplicationPreferences.getSharedPreferences(context);
        if (ApplicationPreferences.preferences.getBoolean(PREF_SHOW_HELP, true)) {
            helpIcon.setImageResource(R.drawable.ic_action_profileicon_help_closed);
            helpText.setVisibility(View.VISIBLE);
        }
        else {
            helpIcon.setImageResource(R.drawable.ic_button_profileicon_help);
            helpText.setVisibility(View.GONE);
        }
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplicationPreferences.getSharedPreferences(context);
                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                int visibility = helpText.getVisibility();
                if (visibility == View.VISIBLE) {
                    helpIcon.setImageResource(R.drawable.ic_button_profileicon_help);
                    visibility = View.GONE;
                    editor.putBoolean(PREF_SHOW_HELP, false);
                }
                else {
                    helpIcon.setImageResource(R.drawable.ic_action_profileicon_help_closed);
                    visibility = View.VISIBLE;
                    editor.putBoolean(PREF_SHOW_HELP, true);
                }
                helpText.setVisibility(visibility);
                editor.apply();
            }
        });
        */
        final ImageView helpIcon = layout.findViewById(R.id.nfc_tag_pref_dlg_helpIcon);
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelpPopupWindow.showPopup(helpIcon, (Activity)context, mDialog, R.string.nfc_tag_pref_dlg_help);
            }
        });


        ImageView changeSelectionIcon = layout.findViewById(R.id.nfc_tag_pref_dlg_changeSelection);
        changeSelectionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectorDialog = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.pref_dlg_change_selection_title)
                        .setCancelable(true)
                        .setNegativeButton(getNegativeButtonText(), null)
                        //.setSingleChoiceItems(R.array.bluetoothNameDChangeSelectionArray, 0, new DialogInterface.OnClickListener() {
                        .setItems(R.array.nfcTagsChangeSelectionArray, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        value = "";
                                        break;
                                    case 1:
                                        for (NFCTag nfcTag : nfcTagList) {
                                            if (nfcTag._name.equals(nfcTagName.getText().toString()))
                                                addNfcTag(nfcTag._name);
                                        }
                                        break;
                                    default:
                                }
                                refreshListView("");
                                //dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        if (!((Activity)context).isFinishing())
            mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        if ((rescanAsyncTask != null) && (!rescanAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)))
            rescanAsyncTask.cancel(true);
        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if ((mSelectorDialog != null) && mSelectorDialog.isShowing())
            mSelectorDialog.dismiss();
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

    void addNfcTag(String tagName) {
        String[] splits = value.split("\\|");
        boolean found = false;
        for (String tag : splits) {
            if (tag.equals(tagName))
                found = true;
        }
        if (!found) {
            if (!value.isEmpty())
                value = value + "|";
            value = value + tagName;
        }
    }

    @SuppressWarnings("StringConcatenationInLoop")
    void removeNfcTag(String tagName) {
        String[] splits = value.split("\\|");
        value = "";
        for (String tag : splits) {
            if (!tag.isEmpty()) {
                if (!tag.equals(tagName)) {
                    if (!value.isEmpty())
                        value = value + "|";
                    value = value + tag;
                }
            }
        }
    }

    boolean isNfcTagSelected(String tagName) {
        String[] splits = value.split("\\|");
        for (String tag : splits) {
            if (tag.equals(tagName))
                return true;
        }
        return false;
    }

    @SuppressLint("StaticFieldLeak")
    private void refreshListView(/*boolean forRescan, */final String scrollToTag)
    {
        //final boolean _forRescan = forRescan;

        rescanAsyncTask = new AsyncTask<Void, Integer, Void>() {

            List<NFCTag> _nfcTagList = null;

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();

                _nfcTagList = new ArrayList<>();

                /*
                if (_forRescan) {
                    dataRelativeLayout.setVisibility(View.GONE);
                    progressLinearLayout.setVisibility(View.VISIBLE);
                }
                */
            }

            @Override
            protected Void doInBackground(Void... params) {

                //if (_forRescan)
                //{
                //}

                // add all from db
                List<NFCTag> tagsFromDb = DatabaseHandler.getInstance(context).getAllNFCTags();
                for (NFCTag tag : tagsFromDb)
                    _nfcTagList.add(new NFCTag(tag._id, tag._name, tag._uid));

                // add all from value
                boolean found;
                String[] splits = value.split("\\|");
                for (String tag : splits) {
                    if (!tag.isEmpty()) {
                        found = false;
                        for (NFCTag tagData : _nfcTagList) {
                            if (tag.equals(tagData._name)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            for (NFCTag tagFromDb : tagsFromDb) {
                                if (tagFromDb._name.equals(tag))
                                    _nfcTagList.add(new NFCTag(tagFromDb._id, tag, tagFromDb._uid));
                            }
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

                /*
                if (_forRescan) {
                    progressLinearLayout.setVisibility(View.GONE);
                    dataRelativeLayout.setVisibility(View.VISIBLE);
                }
                */

                if (!scrollToTag.isEmpty()) {
                    for (int position = 0; position < nfcTagList.size() - 1; position++) {
                        if (nfcTagList.get(position)._name.equals(scrollToTag)) {
                            nfcTagListView.setSelection(position);
                            break;
                        }
                    }
                }
            }

        };

        rescanAsyncTask.execute();
    }

    private class SortList implements Comparator<NFCTag> {

        public int compare(NFCTag lhs, NFCTag rhs) {
            if (GlobalGUIRoutines.collator != null)
                return GlobalGUIRoutines.collator.compare(lhs._name, rhs._name);
            else
                return 0;
        }

    }

    public void showEditMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        final Context viewContext = view.getContext();
        PopupMenu popup;
        //if (android.os.Build.VERSION.SDK_INT >= 19)
            popup = new PopupMenu(viewContext, view, Gravity.END);
        //else
        //    popup = new PopupMenu(context, view);
        new MenuInflater(viewContext).inflate(R.menu.nfc_tag_pref_dlg_item_edit, popup.getMenu());

        int tagPos = (int)view.getTag();
        final NFCTag tagInItem = nfcTagList.get(tagPos);
        PPApplication.logE("NFCTagPreference.showEditMenu", "tagInItem._name="+tagInItem._name);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nfc_tag_pref_dlg_item_menu_writeToNfcTag:
                        writeToNFCTag(tagInItem._id, tagInItem._name);
                        return true;
                    /*case R.id.nfc_tag_pref_item_menu_readNfcUid:
                        Log.e("NFCTagPreference.showEditMenu.readNfcUid", "tagInItem._name="+tagInItem._name);
                        Log.e("NFCTagPreference.showEditMenu.readNfcUid", "tagInItem._id="+tagInItem._id);
                        Intent nfcTagIntent = new Intent(context, NFCTagReadEditorActivity.class);
                        nfcTagIntent.putExtra(NFCTagReadEditorActivity.EXTRA_TAG_NAME, tagInItem._name);
                        nfcTagIntent.putExtra(NFCTagReadEditorActivity.EXTRA_TAG_DB_ID, tagInItem._id);
                        ((Activity)context).startActivityForResult(nfcTagIntent, RESULT_NFC_TAG_READ_EDITOR);
                        return true;*/
                    case R.id.nfc_tag_pref_dlg_item_menu_change:
                        if (!nfcTagName.getText().toString().isEmpty()) {
                            PPApplication.logE("NFCTagPreference.showEditMenu.change", "tagInItem._name="+tagInItem._name);
                            String[] splits = value.split("\\|");
                            value = "";
                            boolean found = false;
                            // add all tags without item tag
                            for (String tag : splits) {
                                if (!tag.isEmpty()) {
                                    if (!tag.equals(tagInItem._name)) {
                                        if (!value.isEmpty())
                                            //noinspection StringConcatenationInLoop
                                            value = value + "|";
                                        //noinspection StringConcatenationInLoop
                                        value = value + tag;
                                        PPApplication.logE("NFCTagPreference.showEditMenu.change", "value="+value);
                                    } else
                                        found = true;
                                }
                            }
                            PPApplication.logE("NFCTagPreference.showEditMenu.change", "found="+found);
                            if (found) {
                                // add item tag with new name
                                if (!value.isEmpty())
                                    value = value + "|";
                                value = value + nfcTagName.getText().toString();
                            }
                            PPApplication.logE("NFCTagPreference.showEditMenu.change", "value=" + value);
                            tagInItem._name = nfcTagName.getText().toString();
                            DatabaseHandler.getInstance(context.getApplicationContext()).updateNFCTag(tagInItem);
                            refreshListView("");
                        }
                        return true;
                    case R.id.nfc_tag_pref_dlg_item_menu_delete:
                        removeNfcTag(tagInItem._name);
                        DatabaseHandler.getInstance(context.getApplicationContext()).deleteNFCTag(tagInItem);
                        refreshListView("");
                        return true;
                    default:
                        return false;
                }
            }
        });


        popup.show();
    }

    private void writeToNFCTag(long id, String tag) {

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if (!nfcAdapter.isEnabled()) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            dialogBuilder.setTitle(R.string.nfc_tag_pref_dlg_menu_writeToNfcTag);
            dialogBuilder.setMessage(R.string.nfc_tag_pref_dlg_writeToNfcTag_nfcNotEnabled);
            dialogBuilder.setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = dialogBuilder.create();
            //dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            //    @Override
            //    public void onShow(DialogInterface dialog) {
            //        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
            //        if (positive != null) positive.setAllCaps(false);
            //        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
            //        if (negative != null) negative.setAllCaps(false);
            //    }
            //});
            if (!((Activity)context).isFinishing())
                dialog.show();
            return;
        }

        Intent nfcTagIntent = new Intent(context.getApplicationContext(), NFCTagWriteActivity.class);
        nfcTagIntent.putExtra(NFCTagWriteActivity.EXTRA_TAG_NAME, tag);
        nfcTagIntent.putExtra(NFCTagWriteActivity.EXTRA_TAG_DB_ID, id);
        ((Activity)context).startActivityForResult(nfcTagIntent, RESULT_NFC_TAG_WRITE);
    }

    void setNFCTagFromEditor(String tagName,
                             @SuppressWarnings("SameParameterValue") String tagUid,
                             long tagDbId) {
        addNfcTag(tagName);
        NFCTag tag = new NFCTag(tagDbId, tagName, tagUid);
        if (tagDbId == 0)
            DatabaseHandler.getInstance(context).addNFCTag(tag);
        else
            DatabaseHandler.getInstance(context).updateNFCTag(tag);
        refreshListView(tagName);
    }
}