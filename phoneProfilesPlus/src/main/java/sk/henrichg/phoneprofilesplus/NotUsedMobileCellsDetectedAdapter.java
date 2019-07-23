package sk.henrichg.phoneprofilesplus;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

class NotUsedMobileCellsDetectedAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final List<Event> eventList;
    private final NotUsedMobileCellsDetectedActivity activity;

    NotUsedMobileCellsDetectedAdapter(NotUsedMobileCellsDetectedActivity activity, List<Event> eventList) {
        inflater = LayoutInflater.from(activity);
        this.eventList = eventList;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return eventList.size();
    }

    @Override
    public Object getItem(int position) {
        return eventList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView eventNameTextView;
        CheckBox checkBox;
        //int position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NotUsedMobileCellsDetectedAdapter.ViewHolder holder;

        View vi = convertView;
        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.not_used_mobile_cells_events_list_item, parent, false);
            holder = new NotUsedMobileCellsDetectedAdapter.ViewHolder();
            holder.eventNameTextView = vi.findViewById(R.id.not_used_mobile_cells_events_list_item_event_name);
            holder.checkBox = vi.findViewById(R.id.not_used_mobile_cells_events_list_item_checkBox);
            vi.setTag(holder);
        }
        else
        {
            holder = (NotUsedMobileCellsDetectedAdapter.ViewHolder)vi.getTag();
        }

        Event event = eventList.get(position);
        holder.eventNameTextView.setText(event._name);

        holder.checkBox.setTag(event);
        holder.checkBox.setChecked(event.getStatus() == 1);
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                Event event = (Event) cb.getTag();
                event.setStatus(cb.isChecked() ? 1 : 0);

                boolean anyChecked = false;
                for (Event _event : eventList) {
                    if (_event.getStatus() == 1)
                        anyChecked = true;
                }
                activity.mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(
                        !activity.cellNameTextView.getText().toString().isEmpty() && anyChecked);
            }
        });

        return vi;
    }
}
