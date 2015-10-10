package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

public class BluetoothNamePreferenceAdapter extends BaseAdapter 
{
    BluetoothNamePreference preference;

    private LayoutInflater inflater;
    //private Context context;

    public BluetoothNamePreferenceAdapter(Context context, BluetoothNamePreference preference) 
    {
        this.preference = preference;

        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
        //this.context = context; 
    }

    public int getCount() {
        return preference.bluetoothList.size();
    }

    public Object getItem(int position) {
        return preference.bluetoothList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    static class ViewHolder {
          TextView bluetoothName;
          RadioButton radioBtn;
          int position;
        }

    public View getView(final int position, View convertView, ViewGroup parent)
    {
        // BluetoothDevice to display
        BluetoothDeviceData bluetoothDevice = preference.bluetoothList.get(position);
        //System.out.println(String.valueOf(position));

        ViewHolder holder;
        
        View vi = convertView;
        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.bluetooth_name_preference_list_item, parent, false);
            holder = new ViewHolder();
            holder.bluetoothName = (TextView)vi.findViewById(R.id.bluetooth_name_pref_dlg_item_label);
            holder.radioBtn = (RadioButton)vi.findViewById(R.id.bluetooth_name_pref_dlg_item_radiobtn);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        holder.bluetoothName.setText(bluetoothDevice.getName());

        holder.radioBtn.setTag(position);
        holder.radioBtn.setChecked(bluetoothDevice.getName().equalsIgnoreCase(preference.getBluetoothName()));
        /*if(preference.selectedRB != null && holder.radioBtn != preference.selectedRB){
            preference.selectedRB = holder.radioBtn;
        }*/
        if ((preference.selectedRB == null) && (holder.radioBtn.isChecked()))
            preference.selectedRB = holder.radioBtn;
        holder.radioBtn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                RadioButton rb = (RadioButton) v;

                if((position != preference.selectedPosition) && (preference.selectedRB != null)){
                    preference.selectedRB.setChecked(false);
                }

                preference.selectedPosition = position;
                preference.selectedRB = rb;

                int index = (Integer)rb.getTag();
                String bluetoothName = preference.bluetoothList.get(index).getName();
                preference.setBluetoothName(bluetoothName);
            }
        });


        return vi;
    }

}
