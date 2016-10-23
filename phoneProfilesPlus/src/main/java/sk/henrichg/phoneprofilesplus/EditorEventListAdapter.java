package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class EditorEventListAdapter extends BaseAdapter
{

    private EditorEventListFragment fragment;
    private DataWrapper dataWrapper;
    private int filterType;
    public List<Event> eventList;
    public boolean released = false;
    private int defaultColor;

    public EditorEventListAdapter(EditorEventListFragment f, DataWrapper pdw, int filterType)
    {
        fragment = f;
        dataWrapper = pdw;
        eventList = dataWrapper.getEventList();
        this.filterType = filterType;
    }

    public void release()
    {
        released = true;

        fragment = null;
        eventList = null;
        dataWrapper = null;
    }

    public int getCount()
    {
        if (eventList == null)
            return 0;

        if ((filterType == EditorEventListFragment.FILTER_TYPE_ALL) ||
                (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER))
            return eventList.size();

        int count = 0;
        for (Event event : eventList)
        {
            switch (filterType)
            {
                case EditorEventListFragment.FILTER_TYPE_RUNNING:
                    if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_RUNNING)
                        ++count;
                    break;
                case EditorEventListFragment.FILTER_TYPE_PAUSED:
                    if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_PAUSE)
                        ++count;
                    break;
                case EditorEventListFragment.FILTER_TYPE_STOPPED:
                    if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_STOP)
                        ++count;
                    break;
            }
        }
        return count;
    }

    public Object getItem(int position)
    {
        if (getCount() == 0)
            return null;
        else
        {

            if ((filterType == EditorEventListFragment.FILTER_TYPE_ALL) ||
                    (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER))
                return eventList.get(position);

            Event _event = null;

            int pos = -1;
            for (Event event : eventList)
            {
                switch (filterType)
                {
                    case EditorEventListFragment.FILTER_TYPE_RUNNING:
                        if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_RUNNING)
                            ++pos;
                        break;
                    case EditorEventListFragment.FILTER_TYPE_PAUSED:
                        if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_PAUSE)
                            ++pos;
                        break;
                    case EditorEventListFragment.FILTER_TYPE_STOPPED:
                        if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_STOP)
                            ++pos;
                        break;
                }
                if (pos == position)
                {
                    _event = event;
                    break;
                }
            }

            return _event;
        }
    }

    public long getItemId(int position)
    {
        return position;
    }

    public int getItemId(Event event)
    {
        if (eventList == null)
            return -1;

        for (int i = 0; i < eventList.size(); i++)
        {
            if (eventList.get(i)._id == event._id)
                return i;
        }
        return -1;
    }

    public int getItemPosition(Event event)
    {
        if (eventList == null)
            return -1;

        if (event == null)
            return -1;

        int pos = -1;

        for (int i = 0; i < eventList.size(); i++)
        {
            switch (filterType)
            {
                case EditorEventListFragment.FILTER_TYPE_ALL:
                case EditorEventListFragment.FILTER_TYPE_START_ORDER:
                    ++pos;
                    break;
                case EditorEventListFragment.FILTER_TYPE_RUNNING:
                    if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_RUNNING)
                        ++pos;
                    break;
                case EditorEventListFragment.FILTER_TYPE_PAUSED:
                    if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_PAUSE)
                        ++pos;
                    break;
                case EditorEventListFragment.FILTER_TYPE_STOPPED:
                    if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_STOP)
                        ++pos;
                    break;
            }

            if (eventList.get(i)._id == event._id)
                return pos;
        }
        return -1;
    }

    public void setList(List<Event> el)
    {
        eventList = el;
        notifyDataSetChanged();
    }

    public void addItem(Event event, boolean refresh)
    {
        if (eventList == null)
            return;

        eventList.add(event);
        if (refresh)
            notifyDataSetChanged();
    }

    public void deleteItemNoNotify(Event event)
    {
        if (eventList == null)
            return;

        eventList.remove(event);
    }

    public void deleteItem(Event event)
    {
        deleteItemNoNotify(event);
        notifyDataSetChanged();
    }

    public void clear()
    {
        if (eventList == null)
            return;

        eventList.clear();
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged(boolean refreshIcons) {
        if (refreshIcons) {
            for (Event event : eventList) {
                Profile profile = dataWrapper.getProfileById(event._fkProfileStart, false);
                dataWrapper.refreshProfileIcon(profile, false, 0);
                profile = dataWrapper.getProfileById(event._fkProfileEnd, false);
                dataWrapper.refreshProfileIcon(profile, false, 0);
            }
        }
        notifyDataSetChanged();
    }

    public void changeItemOrder(int from, int to)
    {
        if (eventList == null)
            return;

        // convert positions from adapter into eventList
        int plFrom = eventList.indexOf(getItem(from));
        int plTo = eventList.indexOf(getItem(to));

        Event event = eventList.get(plFrom);
        eventList.remove(plFrom);
        eventList.add(plTo, event);
        notifyDataSetChanged();
    }

    static class ViewHolder {
          RelativeLayout listItemRoot;
          TextView eventName;
          TextView eventPreferencesDescription;
          ImageView eventStatus;
          ImageView profileStartIcon;
          TextView profileStartName;
          ImageView profileStartIndicator;
          ImageView profileEndIcon;
          TextView profileEndName;
          ImageView profileEndIndicator;
          ImageView eventItemEditMenu;
          int position;
        }

    @SuppressLint("SimpleDateFormat")
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        View vi = convertView;
        if (convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
            if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER) {
                if (GlobalData.applicationEditorPrefIndicator)
                    vi = inflater.inflate(R.layout.editor_event_list_item_with_order, parent, false);
                else
                    vi = inflater.inflate(R.layout.editor_event_list_item_no_indicator_with_order, parent, false);
            }
            else {
                if (GlobalData.applicationEditorPrefIndicator)
                    vi = inflater.inflate(R.layout.editor_event_list_item, parent, false);
                else
                    vi = inflater.inflate(R.layout.editor_event_list_item_no_indicator, parent, false);
            }
            holder = new ViewHolder();
            holder.listItemRoot = (RelativeLayout)vi.findViewById(R.id.event_list_item_root);
            holder.eventName = (TextView)vi.findViewById(R.id.event_list_item_event_name);
            holder.eventStatus = (ImageView)vi.findViewById(R.id.event_list_item_status);
            holder.eventItemEditMenu = (ImageView)vi.findViewById(R.id.event_list_item_edit_menu);
            holder.profileStartName = (TextView)vi.findViewById(R.id.event_list_item_profile_start_name);
            holder.profileStartIcon = (ImageView)vi.findViewById(R.id.event_list_item_profile_start_icon);
            holder.profileEndName = (TextView)vi.findViewById(R.id.event_list_item_profile_end_name);
            holder.profileEndIcon = (ImageView)vi.findViewById(R.id.event_list_item_profile_end_icon);
            if (GlobalData.applicationEditorPrefIndicator)
            {
                holder.eventPreferencesDescription  = (TextView)vi.findViewById(R.id.event_list_item_preferences_description);
                //holder.eventPreferencesDescription.setHorizontallyScrolling(true); // disable auto word wrap :-)
                holder.profileStartIndicator = (ImageView)vi.findViewById(R.id.event_list_item_profile_start_pref_indicator);
                holder.profileEndIndicator = (ImageView)vi.findViewById(R.id.event_list_item_profile_end_pref_indicator);
            }
            vi.setTag(holder);
            defaultColor = holder.eventName.getTextColors().getDefaultColor();
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }
        

        final Event event = (Event)getItem(position);
        if (event != null)
        {
            int eventStatus = event.getStatusFromDB(dataWrapper);

            if (eventStatus == Event.ESTATUS_RUNNING)
            {
                if (GlobalData.applicationTheme.equals("material"))
                    holder.listItemRoot.setBackgroundResource(R.drawable.header_card_dlight);
                else
                if (GlobalData.applicationTheme.equals("dark"))
                    holder.listItemRoot.setBackgroundResource(R.drawable.header_card_dark);
                else
                if (GlobalData.applicationTheme.equals("dlight"))
                    holder.listItemRoot.setBackgroundResource(R.drawable.header_card_dlight);
            }
            else
            {
                if (GlobalData.applicationTheme.equals("material"))
                    holder.listItemRoot.setBackgroundResource(R.drawable.card);
                else
                if (GlobalData.applicationTheme.equals("dark"))
                    holder.listItemRoot.setBackgroundResource(R.drawable.card_dark);
                else
                if (GlobalData.applicationTheme.equals("dlight"))
                    holder.listItemRoot.setBackgroundResource(R.drawable.card);
            }

            boolean isRunnable = event.isRunnable(dataWrapper.context);
            int statusRes = R.drawable.ic_event_status_stop_not_runnable;
            switch (eventStatus)
            {
                case Event.ESTATUS_RUNNING:
                    if (event._isInDelayEnd)
                        statusRes = R.drawable.ic_event_status_running_delay;
                    else
                        statusRes = R.drawable.ic_event_status_running;
                    break;
                case Event.ESTATUS_PAUSE:
                    if (event._isInDelayStart)
                        statusRes = R.drawable.ic_event_status_pause_delay;
                    else
                        statusRes = R.drawable.ic_event_status_pause;
                    break;
                case Event.ESTATUS_STOP:
                    if (isRunnable)
                        statusRes = R.drawable.ic_event_status_stop;
                    else
                        statusRes = R.drawable.ic_event_status_stop_not_runnable;
                    break;
            }
            holder.eventStatus.setImageResource(statusRes);

            if (eventStatus == Event.ESTATUS_RUNNING) {
                holder.eventName.setTypeface(null, Typeface.BOLD);
                holder.eventName.setTextColor(defaultColor);
            }
            else
            if (!isRunnable) {
                holder.eventName.setTypeface(null, Typeface.NORMAL);
                holder.eventName.setTextColor(Color.RED);
            }
            else {
                holder.eventName.setTypeface(null, Typeface.NORMAL);
                holder.eventName.setTextColor(defaultColor);
            }

            String eventName = event._name;
            String eventStartOrder = "[O:" + event._startOrder + "] ";
            if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER)
                eventStartOrder = "";
            String eventPriority = "";
            if (GlobalData.applicationEventUsePriority)
                eventPriority = "[P:" + (event._priority + Event.EPRIORITY_HIGHEST) + "] ";
            //else
            //    eventPriority = "[P:" + "5" + "] ";
            if (event._forceRun) {
                eventName = eventStartOrder + eventPriority + "[\u00BB] " + eventName;
            } else
                eventName = eventStartOrder + eventPriority + eventName;
            if (event._fkProfileStartWhenActivated > 0) {
                Profile profile =  dataWrapper.getProfileById(event._fkProfileStartWhenActivated, false);
                if (profile != null)
                    eventName = eventName + "\n" + "[#] " + profile._name;
            }
            if (!isRunnable)
                eventName = eventName + "\n\n" + vi.getResources().getString(R.string.event_preferences_error);
            holder.eventName.setText(eventName);

            if (GlobalData.applicationEditorPrefIndicator)
            {
                if (holder.eventPreferencesDescription != null) {
                    String eventPrefDescription = event.getPreferencesDescription(vi.getContext());
                    holder.eventPreferencesDescription.setText(Html.fromHtml(eventPrefDescription));
                }
            }

            // profile start
            Profile profile =  dataWrapper.getProfileById(event._fkProfileStart, false);
            if (profile != null)
            {
                String profileName = profile._name;
                if (event._manualProfileActivation)
                    profileName = "[M] " + profileName;
                if (event._delayStart > 0)
                    profileName = "[" + event._delayStart + "] " + profileName;
                holder.profileStartName.setText(profileName);
                if (profile.getIsIconResourceID())
                {
                    if (profile._iconBitmap != null)
                        holder.profileStartIcon.setImageBitmap(profile._iconBitmap);
                    else {
                        //holder.profileStartIcon.setImageBitmap(null);
                        int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                                vi.getContext().getPackageName());
                        holder.profileStartIcon.setImageResource(res); // resource na ikonu
                    }
                }
                else
                {
                    holder.profileStartIcon.setImageBitmap(profile._iconBitmap);
                }

                if (GlobalData.applicationEditorPrefIndicator)
                {
                    //profilePrefIndicatorImageView.setImageBitmap(null);
                    //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                    //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                    if (holder.profileStartIndicator != null)
                        holder.profileStartIndicator.setImageBitmap(profile._preferencesIndicator);
                }
            }
            else
            {
                holder.profileStartName.setText(R.string.profile_preference_profile_not_set);
                holder.profileStartIcon.setImageResource(R.drawable.ic_profile_default);
                if (GlobalData.applicationEditorPrefIndicator)
                {
                    //profilePrefIndicatorImageView.setImageBitmap(null);
                    //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                    //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                    if (holder.profileStartIndicator != null)
                        holder.profileStartIndicator.setImageResource(R.drawable.ic_empty);
                }
            }

            // profile end
            if (event._manualProfileActivation) {
                holder.profileEndIcon.setVisibility(View.GONE);
                holder.profileEndName.setVisibility(View.GONE);
                if (holder.profileEndIndicator != null)
                    holder.profileEndIndicator.setVisibility(View.GONE);
            }
            else {
                holder.profileEndIcon.setVisibility(View.VISIBLE);
                holder.profileEndName.setVisibility(View.VISIBLE);
                if (holder.profileEndIndicator != null)
                    holder.profileEndIndicator.setVisibility(View.VISIBLE);

                profile = dataWrapper.getProfileById(event._fkProfileEnd, false);
                if (profile != null) {
                    String profileName = profile._name;
                    if (event._delayEnd > 0)
                        profileName = "[" + event._delayEnd + "] " + profileName;
                    if (event._atEndDo == Event.EATENDDO_UNDONE_PROFILE)
                        profileName = profileName + " + " + vi.getResources().getString(R.string.event_prefernce_profile_undone);
                    else if (event._atEndDo == Event.EATENDDO_RESTART_EVENTS)
                        profileName = profileName + " + " + vi.getResources().getString(R.string.event_preference_profile_restartEvents);
                    holder.profileEndName.setText(profileName);
                    if (profile.getIsIconResourceID()) {
                        if (profile._iconBitmap != null)
                            holder.profileEndIcon.setImageBitmap(profile._iconBitmap);
                        else {
                            //holder.profileEndIcon.setImageBitmap(null);
                            int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                                    vi.getContext().getPackageName());
                            holder.profileEndIcon.setImageResource(res); // resource na ikonu
                        }
                    } else {
                        holder.profileEndIcon.setImageBitmap(profile._iconBitmap);
                    }

                    if (GlobalData.applicationEditorPrefIndicator) {
                        //profilePrefIndicatorImageView.setImageBitmap(null);
                        //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                        //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                        if (holder.profileEndIndicator != null)
                            holder.profileEndIndicator.setImageBitmap(profile._preferencesIndicator);
                    }
                } else {
                    String profileName = "";
                    if (event._delayEnd > 0)
                        profileName = "[" + event._delayEnd + "] ";
                    if (event._atEndDo == Event.EATENDDO_UNDONE_PROFILE)
                        profileName = profileName + vi.getResources().getString(R.string.event_prefernce_profile_undone);
                    else if (event._atEndDo == Event.EATENDDO_RESTART_EVENTS)
                        profileName = profileName + vi.getResources().getString(R.string.event_preference_profile_restartEvents);
                    else {
                        if (event._fkProfileEnd == GlobalData.PROFILE_NO_ACTIVATE)
                            profileName = profileName + vi.getResources().getString(R.string.profile_preference_profile_end_no_activate);
                        else
                            profileName = profileName + vi.getResources().getString(R.string.profile_preference_profile_not_set);
                    }
                    holder.profileEndName.setText(profileName);
                    holder.profileEndIcon.setImageResource(R.drawable.ic_empty);
                    if (GlobalData.applicationEditorPrefIndicator) {
                        //profilePrefIndicatorImageView.setImageBitmap(null);
                        //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                        //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                        if (holder.profileEndIndicator != null)
                            //holder.profileEndIndicator.setImageResource(R.drawable.ic_empty);
                            holder.profileEndIndicator.setVisibility(View.GONE);
                    }
                }
            }

            holder.eventItemEditMenu.setTag(event);
            final ImageView eventItemEditMenu = holder.eventItemEditMenu;
            holder.eventItemEditMenu.setOnClickListener(new OnClickListener() {

                    public void onClick(View v) {
                        ((EditorEventListFragment)fragment).showEditMenu(eventItemEditMenu);
                    }
                });

        }
        
        return vi;
    }

}
