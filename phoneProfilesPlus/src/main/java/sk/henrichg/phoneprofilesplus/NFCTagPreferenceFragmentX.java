package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
import androidx.appcompat.widget.TooltipCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

@SuppressWarnings("WeakerAccess")
public class NFCTagPreferenceFragmentX extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private NFCTagPreferenceX preference;

    private AlertDialog mSelectorDialog;
    //private LinearLayout progressLinearLayout;
    //private RelativeLayout dataRelativeLayout;
    private ListView nfcTagListView;
    private EditText nfcTagName;
    private AppCompatImageButton addIcon;
    private NFCTagPreferenceAdapterX listAdapter;

    private AsyncTask<Void, Integer, Void> rescanAsyncTask;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(Context context)
    {
        prefContext = context;
        preference = (NFCTagPreferenceX) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.activity_nfc_tag_pref_dialog, null, false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        //progressLinearLayout = layout.findViewById(R.id.nfc_tag_pref_dlg_linla_progress);
        //dataRelativeLayout = layout.findViewById(R.id.nfc_tag_pref_dlg_rella_data);

        addIcon = view.findViewById(R.id.nfc_tag_pref_dlg_addIcon);
        TooltipCompat.setTooltipText(addIcon, getString(R.string.nfc_tag_pref_dlg_add_button_tooltip));
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

                preference.writeToNFCTag(0, tagName);
            }
        });

        nfcTagName = view.findViewById(R.id.nfc_tag_pref_dlg_bt_name);
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
                        addIcon, R.drawable.ic_button_add, prefContext.getApplicationContext());
            }
        });

        GlobalGUIRoutines.setImageButtonEnabled(!nfcTagName.getText().toString().isEmpty(),
                addIcon, R.drawable.ic_button_add, prefContext.getApplicationContext());

        nfcTagListView = view.findViewById(R.id.nfc_tag_pref_dlg_listview);
        listAdapter = new NFCTagPreferenceAdapterX(prefContext, preference);
        nfcTagListView.setAdapter(listAdapter);

        nfcTagListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //NFCTagPreferenceAdapter.ViewHolder viewHolder =
                //        (NFCTagPreferenceAdapter.ViewHolder) v.getTag();
                String nfcTag = preference.nfcTagList.get(position)._name;
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
            helpIcon.setImageResource(R.drawable.ic_button_help);
            helpText.setVisibility(View.GONE);
        }
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplicationPreferences.getSharedPreferences(context);
                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                int visibility = helpText.getVisibility();
                if (visibility == View.VISIBLE) {
                    helpIcon.setImageResource(R.drawable.ic_button_help);
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
        final ImageView helpIcon = view.findViewById(R.id.nfc_tag_pref_dlg_helpIcon);
        TooltipCompat.setTooltipText(helpIcon, getString(R.string.help_button_tooltip));
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelpPopupWindowX.showPopup(helpIcon, R.string.menu_help, (Activity)prefContext, getDialog(), R.string.nfc_tag_pref_dlg_help);
            }
        });


        ImageView changeSelectionIcon = view.findViewById(R.id.nfc_tag_pref_dlg_changeSelection);
        TooltipCompat.setTooltipText(changeSelectionIcon, getString(R.string.nfc_tag_pref_dlg_select_button_tooltip));
        changeSelectionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!((Activity)prefContext).isFinishing()) {
                    mSelectorDialog = new AlertDialog.Builder(prefContext)
                            .setTitle(R.string.pref_dlg_change_selection_title)
                            .setCancelable(true)
                            .setNegativeButton(android.R.string.cancel, null)
                            //.setSingleChoiceItems(R.array.bluetoothNameDChangeSelectionArray, 0, new DialogInterface.OnClickListener() {
                            .setItems(R.array.nfcTagsChangeSelectionArray, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            preference.value = "";
                                            break;
                                        case 1:
                                            for (NFCTag nfcTag : preference.nfcTagList) {
                                                if (nfcTag._name.equals(nfcTagName.getText().toString()))
                                                    preference.addNfcTag(nfcTag._name);
                                            }
                                            break;
                                        default:
                                    }
                                    refreshListView("");
                                    //dialog.dismiss();
                                }
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
            }
        });

        refreshListView("");
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        if ((mSelectorDialog != null) && mSelectorDialog.isShowing())
            mSelectorDialog.dismiss();

        if ((rescanAsyncTask != null) && (!rescanAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)))
            rescanAsyncTask.cancel(true);

        preference.fragment = null;
    }

    @SuppressLint("StaticFieldLeak")
    void refreshListView(/*boolean forRescan, */final String scrollToTag)
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
                List<NFCTag> tagsFromDb = DatabaseHandler.getInstance(prefContext).getAllNFCTags();
                for (NFCTag tag : tagsFromDb)
                    _nfcTagList.add(new NFCTag(tag._id, tag._name, tag._uid));

                // add all from value
                boolean found;
                String[] splits = preference.value.split("\\|");
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

                // move checked on top
                int i = 0;
                int ich = 0;
                while (i < _nfcTagList.size()) {
                    NFCTag nfcTag = _nfcTagList.get(i);
                    if (preference.isNfcTagSelected(nfcTag._name)) {
                        _nfcTagList.remove(i);
                        _nfcTagList.add(ich, nfcTag);
                        ich++;
                    }
                    i++;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                preference.nfcTagList = new ArrayList<>(_nfcTagList);
                listAdapter.notifyDataSetChanged();

                /*
                if (_forRescan) {
                    progressLinearLayout.setVisibility(View.GONE);
                    dataRelativeLayout.setVisibility(View.VISIBLE);
                }
                */

                if (!scrollToTag.isEmpty()) {
                    for (int position = 0; position < preference.nfcTagList.size() - 1; position++) {
                        if (preference.nfcTagList.get(position)._name.equals(scrollToTag)) {
                            nfcTagListView.setSelection(position);
                            break;
                        }
                    }
                }
            }

        };

        rescanAsyncTask.execute();
    }

    private static class SortList implements Comparator<NFCTag> {

        public int compare(NFCTag lhs, NFCTag rhs) {
            if (PPApplication.collator != null)
                return PPApplication.collator.compare(lhs._name, rhs._name);
            else
                return 0;
        }

    }

    void showEditMenu(View view)
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
        final NFCTag tagInItem = preference.nfcTagList.get(tagPos);
        //PPApplication.logE("NFCTagPreference.showEditMenu", "tagInItem._name="+tagInItem._name);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nfc_tag_pref_dlg_item_menu_writeToNfcTag:
                        preference.writeToNFCTag(tagInItem._id, tagInItem._name);
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
                            //PPApplication.logE("NFCTagPreference.showEditMenu.change", "tagInItem._name="+tagInItem._name);
                            String[] splits = preference.value.split("\\|");
                            preference.value = "";
                            boolean found = false;
                            // add all tags without item tag
                            for (String tag : splits) {
                                if (!tag.isEmpty()) {
                                    if (!tag.equals(tagInItem._name)) {
                                        if (!preference.value.isEmpty())
                                            //noinspection StringConcatenationInLoop
                                            preference.value = preference.value + "|";
                                        //noinspection StringConcatenationInLoop
                                        preference.value = preference.value + tag;
                                        //PPApplication.logE("NFCTagPreference.showEditMenu.change", "value="+preference.value);
                                    } else
                                        found = true;
                                }
                            }
                            //PPApplication.logE("NFCTagPreference.showEditMenu.change", "found="+found);
                            if (found) {
                                // add item tag with new name
                                if (!preference.value.isEmpty())
                                    preference.value = preference.value + "|";
                                preference.value = preference.value + nfcTagName.getText().toString();
                            }
                            //PPApplication.logE("NFCTagPreference.showEditMenu.change", "value=" + preference.value);
                            tagInItem._name = nfcTagName.getText().toString();
                            DatabaseHandler.getInstance(prefContext.getApplicationContext()).updateNFCTag(tagInItem);
                            refreshListView("");
                        }
                        return true;
                    case R.id.nfc_tag_pref_dlg_item_menu_delete:
                        preference.removeNfcTag(tagInItem._name);
                        DatabaseHandler.getInstance(prefContext.getApplicationContext()).deleteNFCTag(tagInItem);
                        refreshListView("");
                        return true;
                    default:
                        return false;
                }
            }
        });


        popup.show();
    }

}
