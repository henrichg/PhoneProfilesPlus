package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

class AddEventAdapter extends BaseAdapter {

    private List<Event> eventList;
    private String[] profileNamesArray;
    private int[] profileIconsArray;
    private int defaultColor;

    AddEventDialog dialog;

    private Context context;

    AddEventAdapter(AddEventDialog dialog, Context c, List<Event> eventList)
    {
        context = c;

        this.dialog = dialog;
        this.eventList = eventList;

        //LayoutInflater inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        profileNamesArray = c.getResources().getStringArray(R.array.addEventPredefinedStartProfilesArray);
        TypedArray profileIconsTypedArray = c.getResources().obtainTypedArray(R.array.addEventPredefinedStartProfileIconsArray);
        profileIconsArray = new int[profileIconsTypedArray.length()];
        for (int i = 0; i < profileIconsTypedArray.length(); i++) {
            profileIconsArray[i] = profileIconsTypedArray.getResourceId(i, -1);
        }
        profileIconsTypedArray.recycle();
    }

    public int getCount() {
        return eventList.size();
    }

    public Object getItem(int position) {
        Event event;
        event = eventList.get(position);
        return event;
    }

    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView eventName;
        TextView eventPreferencesDescription;
        ImageView profileStartIcon;
        TextView profileStartName;
        ImageView profileStartIndicator;
        ImageView profileEndIcon;
        TextView profileEndName;
        ImageView profileEndIndicator;
        int position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        View vi = convertView;
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (PPApplication.applicationEditorPrefIndicator)
                vi = inflater.inflate(R.layout.event_preference_list_item, parent, false);
            else
                vi = inflater.inflate(R.layout.event_preference_list_item_no_indicator, parent, false);
            holder = new ViewHolder();
            holder.eventName = (TextView)vi.findViewById(R.id.event_pref_dlg_item_event_name);
            holder.profileStartName = (TextView)vi.findViewById(R.id.event_pref_dlg_item_profile_start_name);
            holder.profileStartIcon = (ImageView)vi.findViewById(R.id.event_pref_dlg_item_profile_start_icon);
            holder.profileEndName = (TextView)vi.findViewById(R.id.event_pref_dlg_item_profile_end_name);
            holder.profileEndIcon = (ImageView)vi.findViewById(R.id.event_pref_dlg_item_profile_end_icon);
            if (PPApplication.applicationEditorPrefIndicator)
            {
                holder.eventPreferencesDescription  = (TextView)vi.findViewById(R.id.event_pref_dlg_item_preferences_description);
                //holder.eventPreferencesDescription.setHorizontallyScrolling(true); // disable auto word wrap :-)
                holder.profileStartIndicator = (ImageView)vi.findViewById(R.id.event_pref_dlg_item_profile_start_pref_indicator);
                holder.profileEndIndicator = (ImageView)vi.findViewById(R.id.event_pref_dlg_item_profile_end_pref_indicator);
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
            String eventName = event._name;
            if (position == 0)
                eventName = context.getString(R.string.new_empty_event);
            String eventPriority = "";
            if (PPApplication.applicationEventUsePriority)
                eventPriority = "[P:" + (event._priority + Event.EPRIORITY_HIGHEST) + "] ";
            //else
            //    eventPriority = "[P:" + "5" + "] ";
            if (event._forceRun) {
                eventName = eventPriority + "[\u00BB] " + eventName;
            } else
                eventName = eventPriority + eventName;
            if (event._fkProfileStartWhenActivated > 0) {
                Profile profile =  dialog.eventListFragment.dataWrapper.getProfileById(event._fkProfileStartWhenActivated, false);
                if (profile != null)
                    eventName = eventName + "\n" + "[#] " + profile._name;
            }
            //if (!isRunnable)
            //    eventName = eventName + "\n\n" + vi.getResources().getString(R.string.event_preferences_error);
            holder.eventName.setText(eventName);

            if (PPApplication.applicationEditorPrefIndicator)
            {
                if (holder.eventPreferencesDescription != null) {
                    String eventPrefDescription = event.getPreferencesDescription(vi.getContext());
                    holder.eventPreferencesDescription.setText(GlobalGUIRoutines.fromHtml(eventPrefDescription));
                }
            }

            // profile start
            Profile profile =  dialog.eventListFragment.dataWrapper.getProfileById(event._fkProfileStart, false);
            if (profile != null)
            {
                String profileName = profile._name;
                if (event._manualProfileActivation)
                    profileName = "[M] " + profileName;
                if (event._delayStart > 0)
                    profileName = "[" + event._delayStart + "] " + profileName;
                holder.profileStartName.setText(profileName);
                holder.profileStartName.setTextColor(defaultColor);
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

                if (PPApplication.applicationEditorPrefIndicator)
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
                String profileName = profileNamesArray[position];
                if (position > 0) {
                    profileName = "(*) " + profileName;
                    holder.profileStartName.setTextColor(Color.RED);
                }
                else
                    holder.profileStartName.setTextColor(defaultColor);
                holder.profileStartName.setText(profileName);
                holder.profileStartIcon.setImageResource(profileIconsArray[position]);
                if (PPApplication.applicationEditorPrefIndicator)
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
            } else {
                holder.profileEndIcon.setVisibility(View.VISIBLE);
                holder.profileEndName.setVisibility(View.VISIBLE);
                if (holder.profileEndIndicator != null)
                    holder.profileEndIndicator.setVisibility(View.VISIBLE);

                profile = dialog.eventListFragment.dataWrapper.getProfileById(event._fkProfileEnd, false);
                if (profile != null) {
                    String profileName = profile._name;
                    if (event._atEndDo == Event.EATENDDO_UNDONE_PROFILE)
                        profileName = profileName + " + " + vi.getResources().getString(R.string.event_prefernce_profile_undone);
                    else if (event._atEndDo == Event.EATENDDO_RESTART_EVENTS)
                        profileName = profileName + " + " + vi.getResources().getString(R.string.event_preference_profile_restartEvents);
                    holder.profileEndName.setText(profileName);
                    holder.profileEndName.setTextColor(defaultColor);
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

                    if (PPApplication.applicationEditorPrefIndicator) {
                        //profilePrefIndicatorImageView.setImageBitmap(null);
                        //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                        //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                        if (holder.profileEndIndicator != null)
                            holder.profileEndIndicator.setImageBitmap(profile._preferencesIndicator);
                    }
                } else {
                    String profileName;
                    if (event._atEndDo == Event.EATENDDO_UNDONE_PROFILE)
                        profileName = vi.getResources().getString(R.string.event_prefernce_profile_undone);
                    else if (event._atEndDo == Event.EATENDDO_RESTART_EVENTS)
                        profileName = vi.getResources().getString(R.string.event_preference_profile_restartEvents);
                    else {
                        if (event._fkProfileEnd == PPApplication.PROFILE_NO_ACTIVATE)
                            profileName = vi.getResources().getString(R.string.profile_preference_profile_end_no_activate);
                        else
                            profileName = vi.getResources().getString(R.string.profile_preference_profile_not_set);
                    }
                    holder.profileEndName.setText(profileName);
                    holder.profileEndIcon.setImageResource(R.drawable.ic_empty);
                    if (PPApplication.applicationEditorPrefIndicator) {
                        //profilePrefIndicatorImageView.setImageBitmap(null);
                        //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                        //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                        if (holder.profileEndIndicator != null)
                            //holder.profileEndIndicator.setImageResource(R.drawable.ic_empty);
                            holder.profileEndIndicator.setVisibility(View.GONE);
                    }
                }
            }

        }

        return vi;
    }

}
