package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

class OpaquenessLightingPreferenceAdapterX extends BaseAdapter {

    private final String value;
    private final OpaquenessLightingPreferenceFragmentX preferenceFragment;

    private final Context context;

    //private final LayoutInflater inflater;

    OpaquenessLightingPreferenceAdapterX(OpaquenessLightingPreferenceFragmentX preferenceFragment, Context c, String value)
    {
        context = c;

        this.preferenceFragment = preferenceFragment;

        if (value.isEmpty())
            this.value = "0";
        else
            this.value = value;

        //inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        int count;
        if (preferenceFragment.preference.showLighting)
            count = preferenceFragment.preference.lightingValues.length;
        else
            count = preferenceFragment.preference.opaquenessValues.length;
        return count;
    }

    public Object getItem(int position) {
        String value;
        if (preferenceFragment.preference.showLighting)
             value = String.valueOf(preferenceFragment.preference.lightingValues[position]);
        else
            value = String.valueOf(preferenceFragment.preference.opaquenessValues[position]);
        return value;
    }

    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView icon;
        TextView label;
        RadioButton radioBtn;
        //int position;
    }

    @SuppressLint("SetTextI18n")
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        View vi = convertView;

        if (convertView == null)
        {
            vi = LayoutInflater.from(context).inflate(R.layout.opaqueness_lighting_preference_list_item, parent, false);

            holder = new ViewHolder();
            holder.icon = vi.findViewById(R.id.opaqueness_lighting_pref_dlg_item_icon);
            holder.label = vi.findViewById(R.id.opaqueness_lighting_pref_dlg_item_label);
            holder.radioBtn = vi.findViewById(R.id.opaqueness_lighting_pref_dlg_item_radiobtn);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        String value;
        if (preferenceFragment.preference.showLighting)
            value = String.valueOf(preferenceFragment.preference.lightingValues[position]);
        else
            value = String.valueOf(preferenceFragment.preference.opaquenessValues[position]);

        holder.radioBtn.setTag(position);
        holder.radioBtn.setOnClickListener(v -> {
            RadioButton rb = (RadioButton) v;
            preferenceFragment.doOnItemSelected((Integer)rb.getTag());
        });

        holder.radioBtn.setChecked(this.value.equals(value));

        String summary;
        if (preferenceFragment.preference.showLighting)
            summary = context.getString(preferenceFragment.preference.lightingNames[position]);
        else
            summary = context.getString(preferenceFragment.preference.opaquenessNames[position]);
        holder.label.setText(summary);

        int iconResId = R.drawable.ic_empty;
        try {
            if (preferenceFragment.preference.showLighting)
                iconResId = preferenceFragment.preference.lightingIconResIds[position];
            else
                iconResId = preferenceFragment.preference.opaquenessIconResIds[position];
        } catch (Exception ignored) {}
        holder.icon.setImageResource(iconResId);

        return vi;
    }

}
