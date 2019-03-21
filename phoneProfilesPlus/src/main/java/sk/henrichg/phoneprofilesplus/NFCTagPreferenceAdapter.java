package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

class NFCTagPreferenceAdapter extends BaseAdapter
{
    private final NFCTagPreference preference;
    //private RadioButton selectedRB;
    //int selectedRBIndex = -1;

    private final LayoutInflater inflater;
    //private Context context;

    NFCTagPreferenceAdapter(Context context, NFCTagPreference preference)
    {
        this.preference = preference;

        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
        //this.context = context; 
    }

    public int getCount() {
        return preference.nfcTagList.size();
    }

    public Object getItem(int position) {
        return preference.nfcTagList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    static class ViewHolder {
        TextView tagName;
        //TextView tagUid;
        CheckBox checkBox;
        AppCompatImageButton itemEditMenu;
        //int position;
    }

    public View getView(final int position, View convertView, ViewGroup parent)
    {
        // NFC tag to display
        NFCTag nfcTag = preference.nfcTagList.get(position);
        //System.out.println(String.valueOf(position));

        ViewHolder holder;
        
        View vi = convertView;
        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.nfc_tag_preference_list_item, parent, false);
            holder = new ViewHolder();
            holder.tagName = vi.findViewById(R.id.nfc_tag_pref_dlg_item_tagName);
            //holder.tagUid = vi.findViewById(R.id.nfc_tag_pref_dlg_item_tagUid);
            holder.checkBox = vi.findViewById(R.id.nfc_tag_pref_dlg_item_checkbox);
            holder.itemEditMenu = vi.findViewById(R.id.nfc_tag_pref_dlg_item_edit_menu);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        holder.tagName.setText(nfcTag._name);
        /*if (nfcTag._uid.isEmpty())
            holder.tagUid.setText(R.string.nfc_tag_pref_dlg_tag_uid_empty);
        else
            holder.tagUid.setText(nfcTag._uid);*/

        holder.checkBox.setTag(position);
        holder.checkBox.setChecked(preference.isNfcTagSelected(nfcTag._name));
        holder.checkBox.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                CheckBox chb = (CheckBox) v;

                String tag = preference.nfcTagList.get((Integer)chb.getTag())._name;

                if (chb.isChecked())
                    preference.addNfcTag(tag);
                else
                    preference.removeNfcTag(tag);
            }
        });

        holder.itemEditMenu.setTag(position);
        final ImageView itemEditMenu = holder.itemEditMenu;
        holder.itemEditMenu.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                preference.showEditMenu(itemEditMenu);
            }
        });

        return vi;
    }

}
