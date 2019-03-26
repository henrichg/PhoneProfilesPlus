package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class RingtonePreferenceAdapterX extends BaseAdapter {

    final Map<String, String> toneList;
    private final RingtonePreferenceFragmentX preferenceFragment;

    private final LayoutInflater inflater;

    RingtonePreferenceAdapterX(RingtonePreferenceFragmentX preferenceFragment, Context c, Map<String, String> toneList)
    {
        this.preferenceFragment = preferenceFragment;
        this.toneList = toneList;

        inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return toneList.size();
    }

    public Object getItem(int position) {
        List<String> uris = new ArrayList<>(toneList.keySet());
        String uri;
        uri = uris.get(position);
        return uri;
    }

    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView ringtoneLabel;
        RadioButton radioBtn;
        //int position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        String ringtone = (new ArrayList<>(toneList.keySet())).get(position);
        String ringtoneTitle = (new ArrayList<>(toneList.values())).get(position);

        View vi = convertView;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.ringtone_preference_list_item, parent, false);

            holder = new ViewHolder();
            holder.ringtoneLabel = vi.findViewById(R.id.ringtone_pref_dlg_item_label);
            holder.radioBtn = vi.findViewById(R.id.ringtone_pref_dlg_item_radiobtn);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        holder.radioBtn.setTag(ringtone);
        if ((preferenceFragment.preference.ringtoneUri != null) && preferenceFragment.preference.ringtoneUri.equals(ringtone))
            holder.radioBtn.setChecked(true);
        else
            holder.radioBtn.setChecked(false);
        holder.radioBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RadioButton rb = (RadioButton) v;
                preferenceFragment.preference.setRingtone((String)rb.getTag(), false);
                preferenceFragment.preference.playRingtone();
            }
        });

        holder.ringtoneLabel.setText(ringtoneTitle);

        return vi;
    }

}
