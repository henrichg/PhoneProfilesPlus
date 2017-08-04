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
import java.util.List;
import java.util.Map;

class RingtonePreferenceAdapter extends BaseAdapter {

    Map<String, String> toneList;
    private String ringtone;
    private RingtonePreferenceDialog dialog;

    private Context context;

    private LayoutInflater inflater = null;

    RingtonePreferenceAdapter(RingtonePreferenceDialog dialog, Context c, String ringtone, Map<String, String> toneList)
    {
        context = c;

        this.dialog = dialog;
        this.toneList = toneList;

        if (toneList.isEmpty())
            this.ringtone = "";
        else
            this.ringtone = ringtone;

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
                dialog.doOnItemSelected((Integer)rb.getTag());
            }
        });

        holder.radioBtn.setChecked(this.ringtone.equals(ringtone));
        holder.ringtoneLabel.setText(ringtoneTitle);

        return vi;
    }

}
