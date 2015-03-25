package sk.henrichg.phoneprofilesplus;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.provider.CalendarContract;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

public class ContactGroupsMultiSelectDialogPreference extends DialogPreference
{

	Context _context = null;
	String value = "";

	// Layout widgets.
	private ListView listView = null;
	private LinearLayout linlaProgress;

	private ContactGroupsMultiselectPreferenceAdapter listAdapter;

	public ContactGroupsMultiSelectDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		_context = context;

		if (EditorProfilesActivity.getContactGroupsCache() == null)
			EditorProfilesActivity.createContactGroupsCache();
		
	}

    protected void showDialog(Bundle state) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .callback(callback)
                .content(getDialogMessage());

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_contact_groups_multiselect_pref_dialog, null);
        onBindDialogView(layout);

        linlaProgress = (LinearLayout)layout.findViewById(R.id.contact_groups_multiselect_pref_dlg_linla_progress);
        listView = (ListView)layout.findViewById(R.id.contact_groups_multiselect_pref_dlg_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id)
            {
                ContactGroup contactGroup = (ContactGroup)listAdapter.getItem(position);
                contactGroup.toggleChecked();
                ContactGroupViewHolder viewHolder = (ContactGroupViewHolder) item.getTag();
                viewHolder.checkBox.setChecked(contactGroup.checked);
            }
        });

        listAdapter = new ContactGroupsMultiselectPreferenceAdapter(_context);

        mBuilder.customView(layout, false);

        mBuilder.showListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ContactGroupsMultiSelectDialogPreference.this.onShow(dialog);
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
                linlaProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                if (!EditorProfilesActivity.getContactGroupsCache().isCached())
                    EditorProfilesActivity.getContactGroupsCache().getContactGroupList(_context);

                getValueCMSDP();

                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);

                if (!EditorProfilesActivity.getContactGroupsCache().isCached())
                    EditorProfilesActivity.getContactGroupsCache().clearCache(false);

                listView.setAdapter(listAdapter);
                linlaProgress.setVisibility(View.GONE);
            }

        }.execute();
    }

    private final MaterialDialog.ButtonCallback callback = new MaterialDialog.ButtonCallback() {
        @Override
        public void onPositive(MaterialDialog dialog) {
            if (shouldPersist())
            {
                // sem narvi stringy skupin kontatkov oddelenych |
                value = "";
                List<ContactGroup> contactGroupList = EditorProfilesActivity.getContactGroupsCache().getList();
                if (contactGroupList != null)
                {
                    for (ContactGroup contactGroup : contactGroupList)
                    {
                        if (contactGroup.checked)
                        {
                            if (!value.isEmpty())
                                value = value + "|";
                            value = value + contactGroup.groupId;
                        }
                    }
                }
                persistString(value);

                setSummaryCMSDP();
            }
        }
    };

	public void onDismiss (DialogInterface dialog)
	{
		EditorProfilesActivity.getContactGroupsCache().cancelCaching();
		
		if (!EditorProfilesActivity.getContactGroupsCache().isCached())
			EditorProfilesActivity.getContactGroupsCache().clearCache(false);
	}
	
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
	{
		if (restoreValue) {
			// restore state
			getValueCMSDP();
		}
		else {
			// set state
			// sem narvi default string skupin kontaktov oddeleny |
			value = "";
			persistString("");
		}
		setSummaryCMSDP();
	}
	
	private void getValueCMSDP()
	{
		// Get the persistent value
		value = getPersistedString(value);
		
		// change checked state by value
		List<ContactGroup> contactGroupList = EditorProfilesActivity.getContactGroupsCache().getList();
		if (contactGroupList != null)
		{
			String[] splits = value.split("\\|");
			for (ContactGroup contactGroup : contactGroupList)
			{
				contactGroup.checked = false;
				for (int i = 0; i < splits.length; i++)
				{
					try {
						long groupId = Long.parseLong(splits[i]);
						if (contactGroup.groupId == groupId)
							contactGroup.checked = true;
					} catch (Exception e) {
					}
				}
			}
            // move checked on top
            int i = 0;
            int ich = 0;
            while (i < contactGroupList.size()) {
                ContactGroup contactGroup = contactGroupList.get(i);
                if (contactGroup.checked) {
                    contactGroupList.remove(i);
                    contactGroupList.add(ich, contactGroup);
                    ich++;
                }
                i++;
            }
		}
	}
	
	private void setSummaryCMSDP()
	{
		String prefVolumeDataSummary = _context.getString(R.string.contacts_multiselect_summary_text_not_selected);
		if (!value.isEmpty())
			prefVolumeDataSummary = _context.getString(R.string.contacts_multiselect_summary_text_selected);
		setSummary(prefVolumeDataSummary);
	}
	
}
