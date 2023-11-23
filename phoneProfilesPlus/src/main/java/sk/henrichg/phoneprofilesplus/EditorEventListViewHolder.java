package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

class EditorEventListViewHolder extends RecyclerView.ViewHolder
                                    implements View.OnClickListener

{

    final DragHandle dragHandle;
    //RelativeLayout listItemRoot;
    private final TextView eventName;
    private TextView eventPreferencesDescription;
    private final ImageView eventStatus;
    private final ImageView profileStartIcon;
    private final TextView profileStartName;
    private ImageView profileStartIndicator;
    private final ImageView profileEndIcon;
    private final TextView profileEndName;
    private ImageView profileEndIndicator;
    private final AppCompatImageButton eventItemEditMenu;
    private final AppCompatImageButton ignoreManualActivationButton;

    private Event event;
    final EditorEventListFragment editorFragment;

    private final Context context;
    private final int filterType;

    EditorEventListViewHolder(View itemView, EditorEventListFragment editorFragment, Context context, int filterType) {
        super(itemView);

        this.context = context;
        this.editorFragment = editorFragment;
        this.filterType = filterType;

        if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER)
            dragHandle = itemView.findViewById(R.id.event_list_drag_handle);
        else
            dragHandle = null;

        eventName = itemView.findViewById(R.id.event_list_item_event_name);
        eventStatus = itemView.findViewById(R.id.event_list_item_status);
        eventItemEditMenu = itemView.findViewById(R.id.event_list_item_edit_menu);
        profileStartName = itemView.findViewById(R.id.event_list_item_profile_start_name);
        profileStartIcon = itemView.findViewById(R.id.event_list_item_profile_start_icon);
        profileEndName = itemView.findViewById(R.id.event_list_item_profile_end_name);
        profileEndIcon = itemView.findViewById(R.id.event_list_item_profile_end_icon);
        ignoreManualActivationButton = itemView.findViewById(R.id.event_list_item_ignore_manual_activation);
        if (ApplicationPreferences.applicationEditorPrefIndicator)
        {
            eventPreferencesDescription  = itemView.findViewById(R.id.event_list_item_preferences_description);
            //eventPreferencesDescription.setHorizontallyScrolling(true); // disable auto word wrap :-)
            profileStartIndicator = itemView.findViewById(R.id.event_list_item_profile_start_pref_indicator);
            profileEndIndicator = itemView.findViewById(R.id.event_list_item_profile_end_pref_indicator);
        }

        // don't delete this - it is workaround for set this LinearLayout non-clickable
        LinearLayout buttonsLayout = itemView.findViewById(R.id.event_list_item_buttons_root);
        buttonsLayout.setOnClickListener(v -> {});

        itemView.setOnClickListener(this);
    }

    void bindEvent(Event event) {

        this.event = event;

        if (event != null)
        {
            int _eventStatus = event.getStatusFromDB(context);

            //boolean isRunnable = event.isRunnable(context, true);
            //boolean isPermissionGranted = Permissions.checkEventPermissions(context, event).size() == 0;
            //boolean isAccessibilityServiceEnabled = (event.isAccessibilityServiceEnabled(context, true) == 1);

            //DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);
            boolean manualProfileActivation = DataWrapperStatic.getIsManualProfileActivation(false, context.getApplicationContext());
            //dataWrapper.invalidateDataWrapper();

            int statusRes = R.drawable.ic_event_status_stop; //GlobalGUIRoutines.getThemeEventStopStatusIndicator(context);
            int colorRes = R.color.event_status_stop;
            /*if (!Event.getGlobalEventsRunning()) {
                if (_eventStatus != Event.ESTATUS_STOP)
                    statusRes = R.drawable.ic_event_status_pause_manual_activation;
            }*/
            if (EventStatic.getGlobalEventsRunning(context)) {
            //else {
                switch (_eventStatus) {
                    case Event.ESTATUS_RUNNING:
                        if (event._isInDelayEnd) {
                            statusRes = R.drawable.ic_event_status_running_delay;
                            colorRes = R.color.altype_eventDelayStartEnd;
                        }
                        else {
                            statusRes = R.drawable.ic_event_status_running;
                            colorRes = R.color.altype_eventStart;
                        }
                        break;
                    case Event.ESTATUS_PAUSE:
                        if (/*!Event.getGlobalEventsRunning() ||*/ (manualProfileActivation && !event._ignoreManualActivation)) {
                            statusRes = R.drawable.ic_event_status_pause_manual_activation;
                            colorRes = R.color.altype_eventEnd;
                        }
                        else if (event._isInDelayStart) {
                            statusRes = R.drawable.ic_event_status_pause_delay;
                            colorRes = R.color.altype_eventDelayStartEnd;
                        }
                        else {
                            statusRes = R.drawable.ic_event_status_pause;
                            colorRes = R.color.altype_eventEnd;
                        }
                        break;
                    case Event.ESTATUS_STOP:
                        //if (isRunnable)
                        //noinspection ConstantConditions
                        statusRes = R.drawable.ic_event_status_stop;
                        //noinspection ConstantConditions
                        colorRes = R.color.event_status_stop;
                        //statusRes = GlobalGUIRoutines.getThemeEventStopStatusIndicator(context);
                        //else
                        //    statusRes = R.drawable.ic_event_status_stop_not_runnable;
                        break;
                }
            }
            eventStatus.setImageResource(statusRes);
            eventStatus.setColorFilter(ContextCompat.getColor(context, colorRes));


            //TypedArray themeArray = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColorSecondary});
            //ColorStateList textColorSecondary = themeArray.getColorStateList(0);

            if (EventStatic.isRedTextNotificationRequired(event, false, context)) {
                //if (!isRunnable)
                eventName.setTypeface(null, Typeface.BOLD_ITALIC/*ITALIC*/);
                //else
                //    eventName.setTypeface(null, Typeface.NORMAL);
                //eventName.setTextSize(15);
                eventName.setTextColor(ContextCompat.getColor(context, R.color.error_color));
            }
            else
            if (!EventStatic.getGlobalEventsRunning(context)/* || (manualProfileActivation && !event._ignoreManualActivation)*/) {
                eventName.setTypeface(null, Typeface.BOLD_ITALIC/*ITALIC*/);
                //eventName.setTextSize(15);
                //eventName.setTextColor(GlobalGUIRoutines.getThemeNormalTextColor(editorFragment.getActivity()));
                //noinspection ConstantConditions
                eventName.setTextColor(ContextCompat.getColor(editorFragment.getActivity(), R.color.activityNormalTextColor));
                //eventName.setTextColor(textColorSecondary);
            }
            else
            if (_eventStatus == Event.ESTATUS_STOP) {
                eventName.setTypeface(null, Typeface.BOLD_ITALIC/*ITALIC*/);
                //eventName.setTextSize(15);
                //eventName.setTextColor(GlobalGUIRoutines.getThemeEventStopColor(context));
                //noinspection ConstantConditions
                eventName.setTextColor(ContextCompat.getColor(editorFragment.getActivity(), R.color.eventStopTextColor));
                //eventName.setTextColor(ContextCompat.getColor(context, R.color.eventStopTextColor));
            }
            else
            if (_eventStatus == Event.ESTATUS_PAUSE) {
                eventName.setTypeface(null, Typeface.BOLD/*NORMAL*/);
                //eventName.setTextSize(15);
                //if (event._isInDelayEnd)
                //    eventName.setTextColor(GlobalGUIRoutines.getThemeEventInDelayColor(editorFragment.getActivity()));
                //else
                //    eventName.setTextColor(GlobalGUIRoutines.getThemeEventPauseColor(editorFragment.getActivity()));
                //eventName.setTextColor(GlobalGUIRoutines.getThemeNormalTextColor(editorFragment.getActivity()));
                //noinspection ConstantConditions
                eventName.setTextColor(ContextCompat.getColor(editorFragment.getActivity(), R.color.activityNormalTextColor));
                //eventName.setTextColor(textColorSecondary);
            }
            else
            if (_eventStatus == Event.ESTATUS_RUNNING) {
                eventName.setTypeface(null, Typeface.BOLD);
                //eventName.setTextSize(15);
                //if (event._isInDelayEnd)
                //    eventName.setTextColor(GlobalGUIRoutines.getThemeEventInDelayColor(editorFragment.getActivity()));
                //else
                //eventName.setTextColor(GlobalGUIRoutines.getThemeAccentColor(editorFragment.getActivity()));
                //noinspection ConstantConditions
                eventName.setTextColor(ContextCompat.getColor(editorFragment.getActivity(), R.color.accent_color));
            }
            else {
                eventName.setTypeface(null, Typeface.BOLD/*NORMAL*/);
                //eventName.setTextSize(15);
                //eventName.setTextColor(GlobalGUIRoutines.getThemeNormalTextColor(editorFragment.getActivity()));
                //noinspection ConstantConditions
                eventName.setTextColor(ContextCompat.getColor(editorFragment.getActivity(), R.color.activityNormalTextColor));
                //eventName.setTextColor(textColorSecondary);
            }

            boolean applicationEditorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator;

            String _eventName;
            String eventStartOrder = "[O:" + event._startOrder + "] ";
            if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER)
                eventStartOrder = "";
            /*String eventPriority = "";
            if (ApplicationPreferences.applicationEventUsePriority) {
                if (event._priority == Event.EPRIORITY_DO_NOT_USE)
                    eventPriority = "[P:--]";
                else
                    eventPriority = "[P:" + (event._priority + Event.EPRIORITY_HIGHEST) + "] ";
            }*/
            boolean addedLF = false;
            if (eventStartOrder.isEmpty()/* && eventPriority.isEmpty()*/) {
                if (event._ignoreManualActivation) {
                    addedLF = true;
                    if (event._noPauseByManualActivation)
                        _eventName = event._name + StringConstants.CHAR_NEW_LINE + StringConstants.STR_DOUBLE_ARROW_INDICATOR;
                    else
                        _eventName = event._name + StringConstants.CHAR_NEW_LINE + StringConstants.STR_ARROW_INDICATOR;
                } else
                    _eventName = event._name;
            }
            else {
                addedLF = true;
                if (event._ignoreManualActivation) {
                    if (event._noPauseByManualActivation)
                        _eventName = event._name + StringConstants.CHAR_NEW_LINE + eventStartOrder /*+ eventPriority*/ + "["+StringConstants.STR_DOUBLE_ARROW+"]";
                    else
                        _eventName = event._name + StringConstants.CHAR_NEW_LINE + eventStartOrder /*+ eventPriority*/ + "["+StringConstants.CHAR_ARROW +"]";
                } else
                    _eventName = event._name + StringConstants.CHAR_NEW_LINE + eventStartOrder /*+ eventPriority*/;
            }

            if (!event._startWhenActivatedProfile.isEmpty()) {
                String[] splits = event._startWhenActivatedProfile.split(StringConstants.STR_SPLIT_REGEX);
                Profile profile;
                if (splits.length == 1) {
                    profile = editorFragment.activityDataWrapper.getProfileById(Long.parseLong(event._startWhenActivatedProfile), false, false, false);
                    if (profile != null) {
                        if (addedLF)
                            _eventName = _eventName + " ";
                        else
                            _eventName = _eventName + StringConstants.CHAR_NEW_LINE;
                        _eventName = _eventName + "[#:" + profile._name + "]";
                    }
                } else {
                    if (addedLF)
                        _eventName = _eventName + " ";
                    else
                        _eventName = _eventName + StringConstants.CHAR_NEW_LINE;
                    _eventName = _eventName + "[#:" + context.getString(R.string.profile_multiselect_summary_text_selected) + " " + splits.length + "]";
                }
            }

            Spannable sbt = new SpannableString(_eventName);
            sbt.setSpan(new RelativeSizeSpan(0.8f), event._name.length(), _eventName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            eventName.setText(sbt);

            if (applicationEditorPrefIndicator)
            {
                if (eventPreferencesDescription != null) {
                    //String eventPrefDescription = event.getPreferencesDescription(context, true);
                    //eventPreferencesDescription.setText(StringFormatUtils.fromHtml(eventPrefDescription, true, true, false, 0, 0, true));
                    if (event._peferencesDecription != null)
                        eventPreferencesDescription.setText(event._peferencesDecription);
                }
            }

            // profile start
            /*
            if (applicationEditorPrefIndicator) {
                if (event._fkProfileStart == Profile.PROFILE_NO_ACTIVATE)
                    profileStartIcon.getLayoutParams().height = 1;
                else
                    profileStartIcon.getLayoutParams().height = GlobalGUIRoutines.dpToPx(30);
            }
            */
            Profile profile =  editorFragment.activityDataWrapper.getProfileById(event._fkProfileStart, true,
                    applicationEditorPrefIndicator, false);
            if (profile != null)
            {
                String profileName = profile._name;
                if (event._manualProfileActivation)
                    profileName = StringConstants.STR_MANUAL_SPACE + profileName;
                if (event._delayStart > 0)
                    profileName = "[" + StringFormatUtils.getDurationString(event._delayStart) + "] " + profileName;
                profileStartName.setText(profileName);
                if (profile.getIsIconResourceID())
                {
                    Bitmap bitmap = profile.increaseProfileIconBrightnessForActivity(editorFragment.getActivity(), profile._iconBitmap);
                    if (bitmap != null)
                        profileStartIcon.setImageBitmap(bitmap);
                    else {
                        if (profile._iconBitmap != null)
                            profileStartIcon.setImageBitmap(profile._iconBitmap);
                        else {
                            //holder.profileStartIcon.setImageBitmap(null);
                            //int res = context.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                            //        context.PPApplication.PACKAGE_NAME);
                            int res = ProfileStatic.getIconResource(profile.getIconIdentifier());
                            profileStartIcon.setImageResource(res); // icon resource
                        }
                    }
                }
                else
                {
                    Bitmap bitmap = profile.increaseProfileIconBrightnessForActivity(editorFragment.getActivity(), profile._iconBitmap);
                    if (bitmap != null)
                        profileStartIcon.setImageBitmap(bitmap);
                    else
                        profileStartIcon.setImageBitmap(profile._iconBitmap);
                }

                if (applicationEditorPrefIndicator)
                {
                    //profilePrefIndicatorImageView.setImageBitmap(null);
                    //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                    //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                    if (profileStartIndicator != null) {
                        profileStartIndicator.setVisibility(View.VISIBLE);
                        if (profile._preferencesIndicator != null) {
                            profileStartIndicator.setImageBitmap(profile._preferencesIndicator);
                        } else
                            profileStartIndicator.setImageResource(R.drawable.ic_empty);
                    }
                }
            }
            else
            {
                //if (event._fkProfileStart == Profile.PROFILE_NO_ACTIVATE) {
                    profileStartName.setText(R.string.profile_preference_profile_end_no_activate);
                    profileStartIcon.setImageResource(R.drawable.ic_empty);
                    if (applicationEditorPrefIndicator) {
                        //profilePrefIndicatorImageView.setImageBitmap(null);
                        //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                        //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                        if (profileStartIndicator != null)
                            //profileStartIndicator.setImageResource(R.drawable.ic_empty);
                            profileStartIndicator.setVisibility(View.GONE);
                    }
                //}
                /*else {
                    profileStartName.setText(R.string.profile_preference_profile_not_set);
                    profileStartIcon.setImageResource(R.drawable.ic_profile_default);
                    if (applicationEditorPrefIndicator)
                    {
                        //profilePrefIndicatorImageView.setImageBitmap(null);
                        //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                        //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                        if (profileStartIndicator != null)
                            profileStartIndicator.setImageResource(R.drawable.ic_empty);
                    }
                }*/
            }

            // profile end
            /*if (event._manualProfileActivation) {
                profileEndIcon.setVisibility(View.GONE);
                profileEndName.setVisibility(View.GONE);
                if (profileEndIndicator != null)
                    profileEndIndicator.setVisibility(View.GONE);
            }
            else*/ {
                //profileEndIcon.setVisibility(View.VISIBLE);
                /*
                if (applicationEditorPrefIndicator) {
                    if (event._fkProfileEnd == Profile.PROFILE_NO_ACTIVATE)
                        profileEndIcon.getLayoutParams().height = 1;
                    else
                        profileEndIcon.getLayoutParams().height = GlobalGUIRoutines.dpToPx(30);
                }
                */
                //profileEndName.setVisibility(View.VISIBLE);
                //if (profileEndIndicator != null)
                //    profileEndIndicator.setVisibility(View.VISIBLE);

                profile = editorFragment.activityDataWrapper.getProfileById(event._fkProfileEnd, true,
                        applicationEditorPrefIndicator, false);
                if (profile != null) {
                    String profileName;
                    //if (event._atEndHowUndo == 0) {
                        if (event._manualProfileActivationAtEnd)
                            profileName = StringConstants.STR_MANUAL_SPACE +profile._name;
                        else
                            profileName = profile._name;
                        if (event._delayEnd > 0)
                            profileName = "[" + StringFormatUtils.getDurationString(event._delayEnd) + "] " + profileName;
                        if (event._atEndDo == Event.EATENDDO_UNDONE_PROFILE)
                            profileName = profileName + " + " + context.getString(R.string.event_preference_profile_undone);
                        else if (event._atEndDo == Event.EATENDDO_RESTART_EVENTS)
                            profileName = profileName + " + " + context.getString(R.string.event_preference_profile_restartEvents);
                    //}
                    //else {
                    //    if (event._delayEnd > 0)
                    //        profileName = "[" + GlobalGUIRoutines.getDurationString(event._delayEnd) + "]";
                    //    if (event._atEndDo == Event.EATENDDO_UNDONE_PROFILE)
                    //        profileName = profileName + " + " + context.getString(R.string.event_preference_profile_undone);
                    //    else if (event._atEndDo == Event.EATENDDO_RESTART_EVENTS)
                    //        profileName = profileName + " + " + context.getString(R.string.event_preference_profile_restartEvents);
                    //}
                    profileEndName.setText(profileName);
                    if (profile.getIsIconResourceID()) {
                        Bitmap bitmap = profile.increaseProfileIconBrightnessForActivity(editorFragment.getActivity(), profile._iconBitmap);
                        if (bitmap != null)
                            profileEndIcon.setImageBitmap(bitmap);
                        else {
                            if (profile._iconBitmap != null)
                                profileEndIcon.setImageBitmap(profile._iconBitmap);
                            else {
                                //holder.profileEndIcon.setImageBitmap(null);
                                //int res = context.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                                //        context.PPApplication.PACKAGE_NAME);
                                int res = ProfileStatic.getIconResource(profile.getIconIdentifier());
                                profileEndIcon.setImageResource(res); // icon resource
                            }
                        }
                    } else {
                        Bitmap bitmap = profile.increaseProfileIconBrightnessForActivity(editorFragment.getActivity(), profile._iconBitmap);
                        if (bitmap != null)
                            profileEndIcon.setImageBitmap(bitmap);
                        else
                            profileEndIcon.setImageBitmap(profile._iconBitmap);
                    }

                    if (applicationEditorPrefIndicator) {
                        //profilePrefIndicatorImageView.setImageBitmap(null);
                        //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                        //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                        if (profileEndIndicator != null) {
                            profileEndIndicator.setVisibility(View.VISIBLE);
                            if (profile._preferencesIndicator != null)
                                profileEndIndicator.setImageBitmap(profile._preferencesIndicator);
                            else
                                profileEndIndicator.setImageResource(R.drawable.ic_empty);
                        }
                    }
                } else {
                    String profileName = "";
                    if (event._delayEnd > 0)
                        profileName = "[" + StringFormatUtils.getDurationString(event._delayEnd) + "] ";
                    if (event._atEndDo == Event.EATENDDO_UNDONE_PROFILE) {
                        if (event._manualProfileActivationAtEnd)
                            profileName = StringConstants.STR_MANUAL_SPACE;
                        profileName = profileName + context.getString(R.string.event_preference_profile_undone);
                    }
                    else if (event._atEndDo == Event.EATENDDO_RESTART_EVENTS)
                        profileName = profileName + context.getString(R.string.event_preference_profile_restartEvents);
                    else {
                        //if (event._atEndHowUndo == 0) {
                            //if (event._fkProfileEnd == Profile.PROFILE_NO_ACTIVATE)
                                profileName = profileName + context.getString(R.string.profile_preference_profile_end_no_activate);
                            //else
                            //    profileName = profileName + context.getString(R.string.profile_preference_profile_not_set);
                        //}
                    }
                    profileEndName.setText(profileName);
                    profileEndIcon.setImageResource(R.drawable.ic_empty);
                    if (applicationEditorPrefIndicator) {
                        //profilePrefIndicatorImageView.setImageBitmap(null);
                        //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                        //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                        if (profileEndIndicator != null)
                            //profileEndIndicator.setImageResource(R.drawable.ic_empty);
                            profileEndIndicator.setVisibility(View.GONE);
                    }
                }
            }

            TooltipCompat.setTooltipText(eventItemEditMenu, context.getString(R.string.tooltip_options_menu));
            eventItemEditMenu.setTag(event);
            eventItemEditMenu.setOnClickListener(v -> editorFragment.showEditMenu(eventItemEditMenu));

            TooltipCompat.setTooltipText(eventStatus, context.getString(R.string.editor_event_list_item_event_status));
            final Event _event = this.event;
            eventStatus.setOnClickListener(view -> {
                if (editorFragment.getActivity() == null)
                    return;

                if (!editorFragment.getActivity().isFinishing()) {
                    EventStatusPopupWindow popup = new EventStatusPopupWindow(editorFragment, _event);

                    ViewGroup activityView = editorFragment.getActivity().findViewById(android.R.id.content);

                    int x = GlobalGUIRoutines.dpToPx(10);
                    int y = 0;

                    popup.showOnAnchor(activityView, RelativePopupWindow.VerticalPosition.CENTER,
                            RelativePopupWindow.HorizontalPosition.ALIGN_LEFT, x, y, true);
                }
            });

            if (event._ignoreManualActivation) {
                if (event._noPauseByManualActivation)
                    ignoreManualActivationButton.setImageResource(R.drawable.ic_ignore_manual_activation_no_pause);
                else
                    ignoreManualActivationButton.setImageResource(R.drawable.ic_ignore_manual_activation);
            }
            else
                ignoreManualActivationButton.setImageResource(R.drawable.ic_not_show_in_activator);
            TooltipCompat.setTooltipText(ignoreManualActivationButton, context.getString(R.string.event_preferences_ForceRun));
            ignoreManualActivationButton.setTag(event);
            ignoreManualActivationButton.setOnClickListener(v -> {
                editorFragment.showIgnoreManualActivationMenu(ignoreManualActivationButton);
                /*final Event event = (Event)v.getTag();
                if (event != null) {
                    editorFragment.updateEventForceRun(event);
                }*/
            });
        }
    }

    @Override
    public void onClick(View v) {
        editorFragment.startEventPreferencesActivity(event, 0);
    }

}
