package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

class AddEventAdapter extends BaseAdapter {

    private final List<Event> eventList;
    private final String[] profileNamesArray;
    private final int[] profileIconsArray;
    private int defaultColor;

    private final AddEventDialog dialog;

    private final Context context;

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
        RadioButton radioButton;
        TextView eventName;
        TextView eventPreferencesDescription;
        ImageView profileStartIcon;
        TextView profileStartName;
        ImageView profileStartIndicator;
        ImageView profileEndIcon;
        TextView profileEndName;
        ImageView profileEndIndicator;
        //int position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        View vi = convertView;

        boolean applicationEditorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator(context);

        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (applicationEditorPrefIndicator)
                //noinspection ConstantConditions
                vi = inflater.inflate(R.layout.add_event_list_item, parent, false);
            else
                //noinspection ConstantConditions
                vi = inflater.inflate(R.layout.add_event_list_item_no_indicator, parent, false);
            holder = new ViewHolder();
            holder.radioButton = vi.findViewById(R.id.event_pref_dlg_item_radio_button);
            holder.eventName = vi.findViewById(R.id.event_pref_dlg_item_event_name);
            holder.profileStartName = vi.findViewById(R.id.event_pref_dlg_item_profile_start_name);
            holder.profileStartIcon = vi.findViewById(R.id.event_pref_dlg_item_profile_start_icon);
            holder.profileEndName = vi.findViewById(R.id.event_pref_dlg_item_profile_end_name);
            holder.profileEndIcon = vi.findViewById(R.id.event_pref_dlg_item_profile_end_icon);
            if (applicationEditorPrefIndicator)
            {
                holder.eventPreferencesDescription  = vi.findViewById(R.id.event_pref_dlg_item_preferences_description);
                //holder.eventPreferencesDescription.setHorizontallyScrolling(true); // disable auto word wrap :-)
                holder.profileStartIndicator = vi.findViewById(R.id.event_pref_dlg_item_profile_start_pref_indicator);
                holder.profileEndIndicator = vi.findViewById(R.id.event_pref_dlg_item_profile_end_pref_indicator);
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
            if (ApplicationPreferences.applicationEventUsePriority(context))
                eventPriority = "[P:" + (event._priority + Event.EPRIORITY_HIGHEST) + "] ";
            //else
            //    eventPriority = "[P:" + "5" + "] ";
            if (event._forceRun) {
                eventName = eventName + "\n" + eventPriority + "[\u00BB]";
            } else
                eventName = eventName + "\n" + eventPriority;

            if (!event._startWhenActivatedProfile.isEmpty()) {
                String[] splits = event._startWhenActivatedProfile.split("\\|");
                Profile profile;
                if (splits.length == 1) {
                    profile = dialog.eventListFragment.activityDataWrapper.getProfileById(Long.valueOf(event._startWhenActivatedProfile), false, false, false);
                    if (profile != null)
                        eventName = eventName + " " + "[#] " + profile._name;
                } else {
                    eventName = eventName + " " + "[#] " + context.getString(R.string.profile_multiselect_summary_text_selected) + " " + splits.length;
                }
            }

            Spannable sbt = new SpannableString(eventName);
            if (position == 0)
                sbt.setSpan(new RelativeSizeSpan(0.8f), context.getString(R.string.new_empty_event).length(), eventName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            else
                sbt.setSpan(new RelativeSizeSpan(0.8f), event._name.length(), eventName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.eventName.setText(sbt);
            //holder.eventName.setText(eventName);

            if (applicationEditorPrefIndicator)
            {
                if (holder.eventPreferencesDescription != null) {
                    if (position == 0)
                        //holder.eventPreferencesDescription.setVisibility(View.GONE);
                        holder.eventPreferencesDescription.setText(R.string.empty_string);
                    else {
                        String eventPrefDescription = event.getPreferencesDescription(vi.getContext(), false);
                        holder.eventPreferencesDescription.setText(GlobalGUIRoutines.fromHtml(eventPrefDescription, true, false, 0, 0));
                        //holder.eventPreferencesDescription.setVisibility(View.VISIBLE);
                    }
                }
            }

            // profile start
            Profile profile =  dialog.eventListFragment.activityDataWrapper.getProfileById(event._fkProfileStart, true, true, false);
            if (profile != null)
            {
                String profileName = profile._name;
                if (event._manualProfileActivation)
                    profileName = "[M] " + profileName;
                if (event._delayStart > 0)
                    profileName = "[" + GlobalGUIRoutines.getDurationString(event._delayStart) + "] " + profileName;
                holder.profileStartName.setText(profileName);
                holder.profileStartName.setTextColor(defaultColor);
                if (profile.getIsIconResourceID())
                {
                    if (profile._iconBitmap != null)
                        holder.profileStartIcon.setImageBitmap(profile._iconBitmap);
                    else {
                        //holder.profileStartIcon.setImageBitmap(null);
                        //int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                        //        vi.getContext().getPackageName());
                        int res = Profile.getIconResource(profile.getIconIdentifier());
                        holder.profileStartIcon.setImageResource(res); // icon resource
                    }
                }
                else
                {
                    holder.profileStartIcon.setImageBitmap(profile._iconBitmap);
                }

                if (applicationEditorPrefIndicator)
                {
                    //profilePrefIndicatorImageView.setImageBitmap(null);
                    //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                    //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                    if (holder.profileStartIndicator != null) {
                        if (profile._preferencesIndicator != null)
                            holder.profileStartIndicator.setImageBitmap(profile._preferencesIndicator);
                        else
                            holder.profileStartIndicator.setImageResource(R.drawable.ic_empty);
                    }
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
                if (applicationEditorPrefIndicator)
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

                profile = dialog.eventListFragment.activityDataWrapper.getProfileById(event._fkProfileEnd, true, true, false);
                if (profile != null) {
                    String profileName = profile._name;
                    if (event._atEndDo == Event.EATENDDO_UNDONE_PROFILE)
                        profileName = profileName + " + " + vi.getResources().getString(R.string.event_preference_profile_undone);
                    else if (event._atEndDo == Event.EATENDDO_RESTART_EVENTS)
                        profileName = profileName + " + " + vi.getResources().getString(R.string.event_preference_profile_restartEvents);
                    holder.profileEndName.setText(profileName);
                    holder.profileEndName.setTextColor(defaultColor);
                    if (profile.getIsIconResourceID()) {
                        if (profile._iconBitmap != null)
                            holder.profileEndIcon.setImageBitmap(profile._iconBitmap);
                        else {
                            //holder.profileEndIcon.setImageBitmap(null);
                            //int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                            //        vi.getContext().getPackageName());
                            int res = Profile.getIconResource(profile.getIconIdentifier());
                            holder.profileEndIcon.setImageResource(res); // icon resource
                        }
                    } else {
                        holder.profileEndIcon.setImageBitmap(profile._iconBitmap);
                    }

                    if (applicationEditorPrefIndicator) {
                        //profilePrefIndicatorImageView.setImageBitmap(null);
                        //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                        //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                        if (holder.profileEndIndicator != null) {
                            if (profile._preferencesIndicator != null)
                                holder.profileEndIndicator.setImageBitmap(profile._preferencesIndicator);
                            else
                                holder.profileEndIndicator.setImageResource(R.drawable.ic_empty);
                        }
                    }
                } else {
                    String profileName;
                    if (event._atEndDo == Event.EATENDDO_UNDONE_PROFILE)
                        profileName = vi.getResources().getString(R.string.event_preference_profile_undone);
                    else if (event._atEndDo == Event.EATENDDO_RESTART_EVENTS)
                        profileName = vi.getResources().getString(R.string.event_preference_profile_restartEvents);
                    else {
                        if (event._fkProfileEnd == Profile.PROFILE_NO_ACTIVATE)
                            profileName = vi.getResources().getString(R.string.profile_preference_profile_end_no_activate);
                        else
                            profileName = vi.getResources().getString(R.string.profile_preference_profile_not_set);
                    }
                    holder.profileEndName.setText(profileName);
                    holder.profileEndIcon.setImageResource(R.drawable.ic_empty);
                    if (applicationEditorPrefIndicator) {
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

        holder.radioButton.setTag(position);
        holder.radioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RadioButton rb = (RadioButton) v;
                dialog.doOnItemSelected((Integer)rb.getTag());
            }
        });

        return vi;
    }

}
