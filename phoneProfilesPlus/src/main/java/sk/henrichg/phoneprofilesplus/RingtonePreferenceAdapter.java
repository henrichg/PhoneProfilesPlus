package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class RingtonePreferenceAdapter extends BaseAdapter {

    Map<String, String> toneList;
    private RingtonePreference preference;

    private Context context;

    private LayoutInflater inflater = null;

    RadioButton checkedRadioButton = null;

    RingtonePreferenceAdapter(RingtonePreference preference, Context c, Map<String, String> toneList)
    {
        context = c;

        this.preference = preference;
        this.toneList = toneList;

        inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        int count = toneList.size();
        return count;
    }

    public Object getItem(int position) {
        List<String> uris = new ArrayList(toneList.keySet());
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

        View vi = convertView;
        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.ringtone_preference_list_item, parent, false);

            holder = new ViewHolder();
            holder.ringtoneLabel = (TextView)vi.findViewById(R.id.ringtone_pref_dlg_item_label);
            holder.radioBtn = (RadioButton)vi.findViewById(R.id.ringtone_pref_dlg_item_radiobtn);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        String ringtone = (new ArrayList<String>(toneList.keySet())).get(position);
        String ringtoneTitle = (new ArrayList<String>(toneList.values())).get(position);

        holder.radioBtn.setTag(position);
        holder.radioBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RadioButton rb = (RadioButton) v;
                List<String> uris = new ArrayList(toneList.keySet());
                preference.setRingtone(uris.get((Integer)rb.getTag()), rb);
            }
        });

        PPApplication.logE("RingtonePreferenceAdapter.getView", "ringtone="+ringtone);
        PPApplication.logE("RingtonePreferenceAdapter.getView", "preference.ringtone="+preference.ringtone);

        holder.radioBtn.setChecked(preference.ringtone.equals(ringtone));
        holder.ringtoneLabel.setText(ringtoneTitle);

        if (holder.radioBtn.isChecked() && (checkedRadioButton == null))
            checkedRadioButton = holder.radioBtn;

        return vi;
    }

}
