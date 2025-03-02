package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;

import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.util.List;

class ActivityLogEventsFilterAdapter extends BaseAdapter {

    private final List<Event> eventList;
    private final long eventId;

    private int defaultColor;

    private final ActivityLogEventsFilterDialog dialog;

    private final Context context;

    ActivityLogEventsFilterAdapter(ActivityLogEventsFilterDialog dialog, Context c, long eventId, List<Event> eventList)
    {
        context = c;

        this.dialog = dialog;
        this.eventList = eventList;

        if (eventId == -1)
            this.eventId = 0;
        else
            this.eventId = eventId;

        //LayoutInflater inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        if (eventList == null)
            return 0;

        return eventList.size();
    }

    public Object getItem(int position) {
        Event event;
        if (position == 0)
            event = null;
        else
            event = eventList.get(position-1);
        return event;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        EventListViewHolder holder;

        View vi = convertView;

        boolean applicationNotHideEditorHideEventDetails;
        if (dialog.hideEventDetailsSwitch != null)
            applicationNotHideEditorHideEventDetails = !dialog.hideEventDetailsValue;
        else
            applicationNotHideEditorHideEventDetails = !ApplicationPreferences.applicationEditorHideEventDetails;

        if (convertView == null)
        {
            //LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (applicationNotHideEditorHideEventDetails)
                vi = LayoutInflater.from(context).inflate(R.layout.listitem_event_preference, parent, false);
            else
                vi = LayoutInflater.from(context).inflate(R.layout.listitem_event_preference_no_indicator, parent, false);
            holder = new EventListViewHolder();
            holder.radioButton = vi.findViewById(R.id.event_pref_dlg_item_radio_button);
            holder.eventName = vi.findViewById(R.id.event_pref_dlg_item_event_name);
            holder.profileStartName = vi.findViewById(R.id.event_pref_dlg_item_profile_start_name);
            holder.profileStartIcon = vi.findViewById(R.id.event_pref_dlg_item_profile_start_icon);
            holder.profileEndName = vi.findViewById(R.id.event_pref_dlg_item_profile_end_name);
            holder.profileEndIcon = vi.findViewById(R.id.event_pref_dlg_item_profile_end_icon);
            //holder.profilesRoot = vi.findViewById(R.id.event_pref_dlg_item_profile_profiles_root);
            if (applicationNotHideEditorHideEventDetails) {
                //holder.profilesRoot = vi.findViewById(R.id.event_pref_dlg_item_profile_profiles_root);
                holder.eventPreferencesDescription = vi.findViewById(R.id.event_pref_dlg_item_preferences_description);
                //holder.eventPreferencesDescription.setHorizontallyScrolling(true); // disable auto word wrap :-)
                holder.profileStartIndicator = vi.findViewById(R.id.event_pref_dlg_item_profile_start_pref_indicator);
                holder.profileEndIndicator = vi.findViewById(R.id.event_pref_dlg_item_profile_end_pref_indicator);
            }
            vi.setTag(holder);
            //defaultColor = GlobalGUIRoutines.getThemeSecondaryTextColor(context);
            defaultColor = ContextCompat.getColor(context, R.color.activityNormalTextColor);
        }
        else
        {
            holder = (EventListViewHolder)vi.getTag();
        }


        Event event;
        if (position == 0)
            event = null;
        else
            event = eventList.get(position-1);

        if (event != null) {
            holder.radioButton.setChecked(eventId == event._id);

            String eventName = event._name;
            if (event._ignoreManualActivation) {
                if (event._noPauseByManualActivation)
                    eventName = eventName + StringConstants.CHAR_NEW_LINE + StringConstants.STR_DOUBLE_ARROW_INDICATOR;
                else
                    eventName = eventName + StringConstants.CHAR_NEW_LINE + StringConstants.STR_ARROW_INDICATOR;
            }

            if (!event._startWhenActivatedProfile.isEmpty()) {
                String[] splits = event._startWhenActivatedProfile.split(StringConstants.STR_SPLIT_REGEX);
                Profile profile;
                if (splits.length == 1) {
                    profile = dialog.eventListFragment.activityDataWrapper.getProfileById(Long.parseLong(event._startWhenActivatedProfile), false, false, false);
                    if (profile != null)
                        eventName = eventName + " [#] " + profile._name;
                } else {
                    eventName = eventName + " [#] " + context.getString(R.string.profile_multiselect_summary_text_selected) + " " + splits.length;
                }
            }

            Spannable sbt = new SpannableString(eventName);
            sbt.setSpan(new RelativeSizeSpan(0.8f), event._name.length(), eventName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.eventName.setTextColor(ContextCompat.getColor(context, R.color.activityNormalTextColor));
            holder.eventName.setText(sbt);

            if (applicationNotHideEditorHideEventDetails)
            {
                if (holder.eventPreferencesDescription != null) {
                    holder.eventPreferencesDescription.setVisibility(View.VISIBLE);
                    //String eventPrefDescription = event.getPreferencesDescription(vi.getContext(), false);
                    //holder.eventPreferencesDescription.setText(StringFormatUtils.fromHtml(eventPrefDescription, true, true, false, 0, 0, true));
                    if (event._peferencesDecription != null)
                        holder.eventPreferencesDescription.setText(event._peferencesDecription);

                    /*
                    RelativeLayout.LayoutParams parameter =  (RelativeLayout.LayoutParams) holder.profilesRoot.getLayoutParams();
                    parameter.setMargins(0, -GlobalGUIRoutines.dpToPx(14), 0, 0); // left, top, right, bottom
                    holder.profilesRoot.setLayoutParams(parameter);
                    */
                }
            }

            // profile start
            Profile profile =  dialog.eventListFragment.activityDataWrapper.getProfileById(event._fkProfileStart, true, true, false);
            if (profile != null)
            {
                String profileName = profile._name;
                if (event._manualProfileActivation)
                    profileName = StringConstants.STR_MANUAL_SPACE + profileName;
                if (event._delayStart > 0)
                    profileName = "[" + StringFormatUtils.getDurationString(event._delayStart) + "] " + profileName;
                holder.profileStartName.setText(profileName);
                holder.profileStartName.setTextColor(defaultColor);
                if (profile.getIsIconResourceID())
                {
                    Bitmap bitmap = profile.increaseProfileIconBrightnessForActivity(dialog.getActivity(), profile._iconBitmap);
                    if (bitmap != null)
                        holder.profileStartIcon.setImageBitmap(bitmap);
                    else {
                        if (profile._iconBitmap != null)
                            holder.profileStartIcon.setImageBitmap(profile._iconBitmap);
                        else {
                            //holder.profileStartIcon.setImageBitmap(null);
                            //int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                            //        vi.getContext().PPApplication.PACKAGE_NAME);
                            int res = ProfileStatic.getIconResource(profile.getIconIdentifier());
                            holder.profileStartIcon.setImageResource(res); // icon resource
                        }
                    }
                }
                else
                {
                    //Bitmap bitmap = profile.increaseProfileIconBrightnessForActivity(dialog.activity, profile._iconBitmap);
                    //Bitmap bitmap = profile._iconBitmap;
                    //if (bitmap != null)
                    //    holder.profileStartIcon.setImageBitmap(bitmap);
                    //else
                        holder.profileStartIcon.setImageBitmap(profile._iconBitmap);
                }

                if (ApplicationPreferences.applicationEditorPrefIndicator)
                {
                    //profilePrefIndicatorImageView.setImageBitmap(null);
                    //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                    //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                    if (holder.profileStartIndicator != null) {
                        if (profile._preferencesIndicator != null) {
                            holder.profileStartIndicator.setImageBitmap(profile._preferencesIndicator);
                            holder.profileStartIndicator.setVisibility(View.VISIBLE);
                        }
                        else {
                            //holder.profileStartIndicator.setImageResource(R.drawable.ic_empty);
                            holder.profileStartIndicator.setVisibility(View.GONE);
                        }
                    }
                } else {
                    if (holder.profileStartIndicator != null)
                        holder.profileStartIndicator.setVisibility(View.GONE);
                }
            }
            else
            {
                String profileName = context.getString(R.string.activity_log_filter_events_profile_not_exists);
                holder.profileStartName.setTextColor(ContextCompat.getColor(context, R.color.errorColor));
                holder.profileStartName.setText(profileName);
                holder.profileStartIcon.setImageResource(R.drawable.ic_profile_default);
                //if (ApplicationPreferences.applicationEditorPrefIndicator)
                //{
                    //profilePrefIndicatorImageView.setImageBitmap(null);
                    //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                    //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                    if (holder.profileStartIndicator != null) {
                        //holder.profileStartIndicator.setImageResource(R.drawable.ic_empty);
                        holder.profileStartIndicator.setVisibility(View.GONE);
                    }
                //} else {
                //    if (holder.profileStartIndicator != null) {
                        //holder.profileStartIndicator.setImageResource(R.drawable.ic_empty);
                //        holder.profileStartIndicator.setVisibility(View.GONE);
                //    }
                //}
            }

            // profile end
            /*if (event._manualProfileActivation) {
                holder.profileEndIcon.setVisibility(View.GONE);
                holder.profileEndName.setVisibility(View.GONE);
                if (holder.profileEndIndicator != null)
                    holder.profileEndIndicator.setVisibility(View.GONE);
            } else*/ {
                //holder.profileEndIcon.setVisibility(View.VISIBLE);
                /*
                if (applicationEditorPrefIndicator) {
                    if (event._fkProfileEnd == Profile.PROFILE_NO_ACTIVATE)
                        holder.profileEndIcon.getLayoutParams().height = 1;
                    else
                        holder.profileEndIcon.getLayoutParams().height = GlobalGUIRoutines.dpToPx(30);
                }
                */
                //holder.profileEndName.setVisibility(View.VISIBLE);
                //if (holder.profileEndIndicator != null)
                //    holder.profileEndIndicator.setVisibility(View.VISIBLE);

                profile = dialog.eventListFragment.activityDataWrapper.getProfileById(event._fkProfileEnd, true, true, false);
                if (profile != null) {
                    String profileName;
                    //if (event._atEndHowUndo == 0) {
                        if (event._manualProfileActivationAtEnd)
                            profileName = StringConstants.STR_MANUAL_SPACE + profile._name;
                        else
                            profileName = profile._name;
                        if (event._atEndDo == Event.EATENDDO_UNDONE_PROFILE)
                            profileName = profileName + " + " + vi.getResources().getString(R.string.event_preference_profile_undone);
                        else if (event._atEndDo == Event.EATENDDO_RESTART_EVENTS)
                            profileName = profileName + " + " + vi.getResources().getString(R.string.event_preference_profile_restartEvents);
                    //}
                    //else {
                    //    if (event._atEndDo == Event.EATENDDO_UNDONE_PROFILE)
                    //        profileName = vi.getResources().getString(R.string.event_preference_profile_undone);
                    //    else if (event._atEndDo == Event.EATENDDO_RESTART_EVENTS)
                    //        profileName =  vi.getResources().getString(R.string.event_preference_profile_restartEvents);
                    //}
                    holder.profileEndName.setText(profileName);
                    holder.profileEndName.setTextColor(defaultColor);
                    if (profile.getIsIconResourceID()) {
                        Bitmap bitmap = profile.increaseProfileIconBrightnessForActivity(dialog.getActivity(), profile._iconBitmap);
                        if (bitmap != null)
                            holder.profileEndIcon.setImageBitmap(bitmap);
                        else {
                            if (profile._iconBitmap != null)
                                holder.profileEndIcon.setImageBitmap(profile._iconBitmap);
                            else {
                                //holder.profileEndIcon.setImageBitmap(null);
                                //int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                                //        vi.getContext().PPApplication.PACKAGE_NAME);
                                int res = ProfileStatic.getIconResource(profile.getIconIdentifier());
                                holder.profileEndIcon.setImageResource(res); // icon resource
                            }
                        }
                    } else {
                        //Bitmap bitmap = profile.increaseProfileIconBrightnessForActivity(dialog.activity, profile._iconBitmap);
                        //Bitmap bitmap = profile._iconBitmap;
                        //if (bitmap != null)
                        //    holder.profileEndIcon.setImageBitmap(bitmap);
                        //else
                            holder.profileEndIcon.setImageBitmap(profile._iconBitmap);
                    }

                    if (ApplicationPreferences.applicationEditorPrefIndicator) {
                        //profilePrefIndicatorImageView.setImageBitmap(null);
                        //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                        //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                        if (holder.profileEndIndicator != null) {
                            if (profile._preferencesIndicator != null) {
                                holder.profileEndIndicator.setImageBitmap(profile._preferencesIndicator);
                                holder.profileEndIndicator.setVisibility(View.VISIBLE);
                            }
                            else {
                                //holder.profileEndIndicator.setImageResource(R.drawable.ic_empty);
                                holder.profileEndIndicator.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        if (holder.profileEndIndicator != null)
                            holder.profileEndIndicator.setVisibility(View.GONE);
                    }
                } else {
                    String profileName = context.getString(R.string.activity_log_filter_events_profile_not_exists);
                    //if (event._atEndHowUndo == 0) {
                        if (!profileName.isEmpty()) {
                            if (event._manualProfileActivationAtEnd)
                                profileName = StringConstants.STR_MANUAL_SPACE + profileName;
                            holder.profileEndName.setTextColor(ContextCompat.getColor(context, R.color.errorColor));
                        } else
                            holder.profileEndName.setTextColor(defaultColor);
                    //}
                    //else
                    //    holder.profileEndName.setTextColor(defaultColor);
                    if (profileName.isEmpty()) {
                        if (event._atEndDo == Event.EATENDDO_UNDONE_PROFILE) {
                            if (event._manualProfileActivationAtEnd)
                                profileName = StringConstants.STR_MANUAL_SPACE + vi.getResources().getString(R.string.event_preference_profile_undone);
                            else
                                profileName = vi.getResources().getString(R.string.event_preference_profile_undone);
                        }
                        else if (event._atEndDo == Event.EATENDDO_RESTART_EVENTS)
                            profileName = vi.getResources().getString(R.string.event_preference_profile_restartEvents);
                        else {
                            //if (event._atEndHowUndo == 0) {
                                if (event._fkProfileEnd == Profile.PROFILE_NO_ACTIVATE)
                                    profileName = vi.getResources().getString(R.string.profile_preference_profile_end_no_activate);
                                else
                                    profileName = vi.getResources().getString(R.string.profile_preference_profile_not_set);
                            //}
                        }
                    }
                    else {
                        if (event._manualProfileActivationAtEnd)
                            profileName =  StringConstants.STR_MANUAL_SPACE + profileName;
                        if (event._atEndDo == Event.EATENDDO_UNDONE_PROFILE)
                            profileName = profileName + " + " + vi.getResources().getString(R.string.event_preference_profile_undone);
                        else if (event._atEndDo == Event.EATENDDO_RESTART_EVENTS)
                            profileName = profileName + " + " + vi.getResources().getString(R.string.event_preference_profile_restartEvents);
                    }
                    holder.profileEndName.setText(profileName);
                    holder.profileEndIcon.setImageResource(R.drawable.ic_profile_default);
                    //if (ApplicationPreferences.applicationEditorPrefIndicator) {
                        //profilePrefIndicatorImageView.setImageBitmap(null);
                        //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                        //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                        if (holder.profileEndIndicator != null)
                            //holder.profileEndIndicator.setImageResource(R.drawable.ic_empty);
                            holder.profileEndIndicator.setVisibility(View.GONE);
                    //} else {
                    //    if (holder.profileEndIndicator != null) {
                            //holder.profileStartIndicator.setImageResource(R.drawable.ic_empty);
                    //        holder.profileEndIndicator.setVisibility(View.GONE);
                    //    }
                    //}
                }
            }

        } else {
            if (position == 0)
            {
                holder.radioButton.setChecked((eventId == 0));

                String eventName = context.getString(R.string.activity_log_filter_events_all_events);
                holder.eventName.setTextColor(ContextCompat.getColor(context, R.color.activityNormalTextColor));
                holder.eventName.setText(eventName);

                holder.profileStartName.setVisibility(View.GONE);
                holder.profileStartIcon.setVisibility(View.GONE);
                holder.profileEndName.setVisibility(View.GONE);
                holder.profileEndIcon.setVisibility(View.GONE);
                if (applicationNotHideEditorHideEventDetails) {
                    holder.eventPreferencesDescription.setVisibility(View.GONE);
                    holder.profileStartIndicator.setVisibility(View.GONE);
                    holder.profileEndIndicator.setVisibility(View.GONE);
                }
            }
        }

        holder.radioButton.setTag(position);
        holder.radioButton.setOnClickListener(v -> {
            RadioButton rb = (RadioButton) v;
            rb.setChecked(true);
            final Handler handler = new Handler(context.getMainLooper());
            final WeakReference<ActivityLogEventsFilterDialog> dialogWeakRef = new WeakReference<>(dialog);
            final WeakReference<RadioButton> rbWeakRef = new WeakReference<>(rb);
            handler.postDelayed(() -> {
                ActivityLogEventsFilterDialog dialog1 = dialogWeakRef.get();
                RadioButton rb1 = rbWeakRef.get();
                if ((dialog1 != null) && (rb1 != null))
                    dialog1.doOnItemSelected((Integer) rb1.getTag());
            }, 200);
        });

        return vi;
    }

}
