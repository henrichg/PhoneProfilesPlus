package sk.henrichg.phoneprofilesplus;

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

public class ContactsMultiSelectDialogPreference extends DialogPreference
{

	Context _context = null;
	String value = "";
	
	// Layout widgets.
	private ListView listView = null;
	private LinearLayout linlaProgress;

	private ContactsMultiselectPreferenceAdapter listAdapter;
	
	public ContactsMultiSelectDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		_context = context;

		if (EditorProfilesActivity.getContactsCache() == null)
			EditorProfilesActivity.createContactsCache();
		
	}

	protected View onCreateDialogView() {
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());

		View view = layoutInflater.inflate(
			R.layout.activity_contacts_multiselect_pref_dialog, null);
		
		linlaProgress = (LinearLayout)view.findViewById(R.id.contacts_multiselect_pref_dlg_linla_progress);
		listView = (ListView)view.findViewById(R.id.contacts_multiselect_pref_dlg_listview);
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item, int position, long id) 
            {
                Contact contact = (Contact)listAdapter.getItem(position);
                contact.toggleChecked();
                ContactViewHolder viewHolder = (ContactViewHolder) item.getTag();
                viewHolder.checkBox.setChecked(contact.checked);
            }
        });
		
	    listAdapter = new ContactsMultiselectPreferenceAdapter(_context);
		
		new AsyncTask<Void, Integer, Void>() {

			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
				linlaProgress.setVisibility(View.VISIBLE);
			}
			
			@Override
			protected Void doInBackground(Void... params) {
				if (!EditorProfilesActivity.getContactsCache().isCached())
					EditorProfilesActivity.getContactsCache().getContactList(_context);

				getValueCMSDP();
				
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result)
			{
				super.onPostExecute(result);
				
				if (!EditorProfilesActivity.getContactsCache().isCached())
					EditorProfilesActivity.getContactsCache().clearCache(false);
				
			    listView.setAdapter(listAdapter);
				linlaProgress.setVisibility(View.GONE);
			}
			
		}.execute();
		
		return view;
	}

	public void onClick(DialogInterface dialog, int which) {
		// if the positive button is clicked, we persist the value.
		if (EditorProfilesActivity.getContactsCache().isCached())
		{
			if (which == DialogInterface.BUTTON_POSITIVE) {
				if (shouldPersist()) 
				{
					// sem narvi stringy kontatkov oddelenych |
					value = "";
					List<Contact> contactList = EditorProfilesActivity.getContactsCache().getList();
					if (contactList != null)
					{
						for (Contact contact : contactList)
						{
							if (contact.checked)
							{
								if (!value.isEmpty())
									value = value + "|";
								value = value + contact.contactId + "#" + contact.phoneId;
							}
						}
					}
					persistString(value);
					
					setSummaryCMSDP();
				}
			}
		}
		super.onClick(dialog, which);
	}

	public void onDismiss (DialogInterface dialog)
	{
		EditorProfilesActivity.getContactsCache().cancelCaching();
		
		if (!EditorProfilesActivity.getContactsCache().isCached())
			EditorProfilesActivity.getContactsCache().clearCache(false);
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
			// sem narvi default string kontaktov oddeleny |
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
		List<Contact> contactList = EditorProfilesActivity.getContactsCache().getList();
		if (contactList != null)
		{
			String[] splits = value.split("\\|");
			for (Contact contact : contactList)
			{
				contact.checked = false;
				for (int i = 0; i < splits.length; i++)
				{
					try {
						String [] splits2 = splits[i].split("#");
						long contactId = Long.parseLong(splits2[0]);
						long phoneId = Long.parseLong(splits2[1]);
						if ((contact.contactId == contactId) && (contact.phoneId == phoneId))
							contact.checked = true;
					} catch (Exception e) {
					}
				}
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
