package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.labo.kaji.relativepopupwindow.RelativePopupWindow;

class EditorEventListViewHolder extends RecyclerView.ViewHolder
                                    implements View.OnClickListener

{

    ImageView dragHandle;
    //RelativeLayout listItemRoot;
    private TextView eventName;
    private TextView eventPreferencesDescription;
    private ImageView eventStatus;
    private ImageView profileStartIcon;
    private TextView profileStartName;
    private ImageView profileStartIndicator;
    private ImageView profileEndIcon;
    private TextView profileEndName;
    private ImageView profileEndIndicator;
    private ImageView eventItemEditMenu;

    private Event event;
    private EditorEventListFragment editorFragment;

    private Context context;
    private int filterType;

    EditorEventListViewHolder(View itemView, EditorEventListFragment editorFragment, Context context, int filterType) {
        super(itemView);

        this.context = context;
        this.editorFragment = editorFragment;
        this.filterType = filterType;

        if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER)
            dragHandle = (ImageView) itemView.findViewById(R.id.event_list_drag_handle);
        else
            dragHandle = null;

        //listItemRoot = (RelativeLayout)vi.findViewById(R.id.event_list_item_root);
        eventName = (TextView) itemView.findViewById(R.id.event_list_item_event_name);
        eventStatus = (ImageView)itemView.findViewById(R.id.event_list_item_status);
        eventItemEditMenu = (ImageView)itemView.findViewById(R.id.event_list_item_edit_menu);
        profileStartName = (TextView)itemView.findViewById(R.id.event_list_item_profile_start_name);
        profileStartIcon = (ImageView)itemView.findViewById(R.id.event_list_item_profile_start_icon);
        profileEndName = (TextView)itemView.findViewById(R.id.event_list_item_profile_end_name);
        profileEndIcon = (ImageView)itemView.findViewById(R.id.event_list_item_profile_end_icon);
        if (ApplicationPreferences.applicationEditorPrefIndicator(context))
        {
            eventPreferencesDescription  = (TextView)itemView.findViewById(R.id.event_list_item_preferences_description);
            //eventPreferencesDescription.setHorizontallyScrolling(true); // disable auto word wrap :-)
            profileStartIndicator = (ImageView)itemView.findViewById(R.id.event_list_item_profile_start_pref_indicator);
            profileEndIndicator = (ImageView)itemView.findViewById(R.id.event_list_item_profile_end_pref_indicator);
        }

        itemView.setOnClickListener(this);
    }

    void bindEvent(Event event) {

        this.event = event;

        if (event != null)
        {
            int _eventStatus = event.getStatusFromDB(editorFragment.dataWrapper);

            boolean isRunnable = event.isRunnable(context);
            int statusRes = R.drawable.ic_event_status_stop_not_runnable;
            switch (_eventStatus)
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
            eventStatus.setImageResource(statusRes);

            if (_eventStatus == Event.ESTATUS_RUNNING) {
                eventName.setTypeface(null, Typeface.BOLD);
                eventName.setTextSize(16);
                eventName.setTextColor(GlobalGUIRoutines.getThemeAccentColor(editorFragment.getActivity()));
            }
            else
            if (!isRunnable) {
                eventName.setTypeface(null, Typeface.NORMAL);
                eventName.setTextSize(15);
                eventName.setTextColor(Color.RED);
            }
            else {
                eventName.setTypeface(null, Typeface.NORMAL);
                eventName.setTextSize(15);
                eventName.setTextColor(GlobalGUIRoutines.getThemeTextColor(editorFragment.getActivity()));
            }

            String _eventName = event._name;
            String eventStartOrder = "[O:" + event._startOrder + "] ";
            if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER)
                eventStartOrder = "";
            String eventPriority = "";
            if (ApplicationPreferences.applicationEventUsePriority(context))
                eventPriority = "[P:" + (event._priority + Event.EPRIORITY_HIGHEST) + "] ";
            //else
            //    eventPriority = "[P:" + "5" + "] ";
            if (event._forceRun) {
                _eventName = eventStartOrder + eventPriority + "[\u00BB] " + _eventName;
            } else
                _eventName = eventStartOrder + eventPriority + _eventName;
            if (event._fkProfileStartWhenActivated > 0) {
                Profile profile =  editorFragment.dataWrapper.getProfileById(event._fkProfileStartWhenActivated, false);
                if (profile != null)
                    _eventName = _eventName + "\n" + "[#] " + profile._name;
            }
            if (!isRunnable)
                _eventName = _eventName + "\n\n" + context.getResources().getString(R.string.event_preferences_error);
            eventName.setText(_eventName);

            if (ApplicationPreferences.applicationEditorPrefIndicator(context))
            {
                if (eventPreferencesDescription != null) {
                    String eventPrefDescription = event.getPreferencesDescription(context);
                    eventPreferencesDescription.setText(GlobalGUIRoutines.fromHtml(eventPrefDescription));
                }
            }

            // profile start
            Profile profile =  editorFragment.dataWrapper.getProfileById(event._fkProfileStart, false);
            if (profile != null)
            {
                String profileName = profile._name;
                if (event._manualProfileActivation)
                    profileName = "[M] " + profileName;
                if (event._delayStart > 0)
                    profileName = "[" + GlobalGUIRoutines.getDurationString(event._delayStart) + "] " + profileName;
                profileStartName.setText(profileName);
                if (profile.getIsIconResourceID())
                {
                    if (profile._iconBitmap != null)
                        profileStartIcon.setImageBitmap(profile._iconBitmap);
                    else {
                        //holder.profileStartIcon.setImageBitmap(null);
                        int res = context.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                                context.getPackageName());
                        profileStartIcon.setImageResource(res); // resource na ikonu
                    }
                }
                else
                {
                    profileStartIcon.setImageBitmap(profile._iconBitmap);
                }

                if (ApplicationPreferences.applicationEditorPrefIndicator(context))
                {
                    //profilePrefIndicatorImageView.setImageBitmap(null);
                    //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                    //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                    if (profileStartIndicator != null)
                        profileStartIndicator.setImageBitmap(profile._preferencesIndicator);
                }
            }
            else
            {
                profileStartName.setText(R.string.profile_preference_profile_not_set);
                profileStartIcon.setImageResource(R.drawable.ic_profile_default);
                if (ApplicationPreferences.applicationEditorPrefIndicator(context))
                {
                    //profilePrefIndicatorImageView.setImageBitmap(null);
                    //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                    //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                    if (profileStartIndicator != null)
                        profileStartIndicator.setImageResource(R.drawable.ic_empty);
                }
            }

            // profile end
            if (event._manualProfileActivation) {
                profileEndIcon.setVisibility(View.GONE);
                profileEndName.setVisibility(View.GONE);
                if (profileEndIndicator != null)
                    profileEndIndicator.setVisibility(View.GONE);
            }
            else {
                profileEndIcon.setVisibility(View.VISIBLE);
                profileEndName.setVisibility(View.VISIBLE);
                if (profileEndIndicator != null)
                    profileEndIndicator.setVisibility(View.VISIBLE);

                profile = editorFragment.dataWrapper.getProfileById(event._fkProfileEnd, false);
                if (profile != null) {
                    String profileName = profile._name;
                    if (event._delayEnd > 0)
                        profileName = "[" + GlobalGUIRoutines.getDurationString(event._delayEnd) + "] " + profileName;
                    if (event._atEndDo == Event.EATENDDO_UNDONE_PROFILE)
                        profileName = profileName + " + " + context.getResources().getString(R.string.event_prefernce_profile_undone);
                    else if (event._atEndDo == Event.EATENDDO_RESTART_EVENTS)
                        profileName = profileName + " + " + context.getResources().getString(R.string.event_preference_profile_restartEvents);
                    profileEndName.setText(profileName);
                    if (profile.getIsIconResourceID()) {
                        if (profile._iconBitmap != null)
                            profileEndIcon.setImageBitmap(profile._iconBitmap);
                        else {
                            //holder.profileEndIcon.setImageBitmap(null);
                            int res = context.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                                    context.getPackageName());
                            profileEndIcon.setImageResource(res); // resource na ikonu
                        }
                    } else {
                        profileEndIcon.setImageBitmap(profile._iconBitmap);
                    }

                    if (ApplicationPreferences.applicationEditorPrefIndicator(context)) {
                        //profilePrefIndicatorImageView.setImageBitmap(null);
                        //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                        //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                        if (profileEndIndicator != null)
                            profileEndIndicator.setImageBitmap(profile._preferencesIndicator);
                    }
                } else {
                    String profileName = "";
                    if (event._delayEnd > 0)
                        profileName = "[" + GlobalGUIRoutines.getDurationString(event._delayEnd) + "] ";
                    if (event._atEndDo == Event.EATENDDO_UNDONE_PROFILE)
                        profileName = profileName + context.getResources().getString(R.string.event_prefernce_profile_undone);
                    else if (event._atEndDo == Event.EATENDDO_RESTART_EVENTS)
                        profileName = profileName + context.getResources().getString(R.string.event_preference_profile_restartEvents);
                    else {
                        if (event._fkProfileEnd == Profile.PROFILE_NO_ACTIVATE)
                            profileName = profileName + context.getResources().getString(R.string.profile_preference_profile_end_no_activate);
                        else
                            profileName = profileName + context.getResources().getString(R.string.profile_preference_profile_not_set);
                    }
                    profileEndName.setText(profileName);
                    profileEndIcon.setImageResource(R.drawable.ic_empty);
                    if (ApplicationPreferences.applicationEditorPrefIndicator(context)) {
                        //profilePrefIndicatorImageView.setImageBitmap(null);
                        //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                        //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                        if (profileEndIndicator != null)
                            //profileEndIndicator.setImageResource(R.drawable.ic_empty);
                            profileEndIndicator.setVisibility(View.GONE);
                    }
                }
            }

            eventItemEditMenu.setTag(event);
            final ImageView _eventItemEditMenu = eventItemEditMenu;
            eventItemEditMenu.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    editorFragment.showEditMenu(_eventItemEditMenu);
                }
            });

            final ImageView _eventStatusView = eventStatus;
            final Event _event = this.event;
            eventStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventStatusPopupWindow popup = new EventStatusPopupWindow(editorFragment, _event);

                    View contentView = popup.getContentView();
                    contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    int measuredW = contentView.getMeasuredWidth();
                    int measuredH = contentView.getMeasuredHeight();
                    //Log.d("EditorEventListAdapter.eventsRunStopIndicator.onClick","measuredW="+measuredW);
                    //Log.d("EditorEventListAdapter.eventsRunStopIndicator.onClick","measuredH="+measuredH);

                    Point screenSize = GlobalGUIRoutines.getRealScreenSize(editorFragment.getActivity());

                    int[] location = new int[2];
                    _eventStatusView.getLocationOnScreen(location);
                    int x = 0;
                    int y = 0;

                    int statusBarHeight = (int) (24 * editorFragment.getResources().getDisplayMetrics().density + 0.5f);

                    if ((location[0] + measuredW) > screenSize.x)
                        x = -(location[0]
                                - (screenSize.x - measuredW));

                    if ((location[1] + _eventStatusView.getHeight() + measuredH) > screenSize.y)
                        y = -(location[1] - _eventStatusView.getHeight()
                                - (screenSize.y - measuredH)
                                + GlobalGUIRoutines.getNavigationBarSize(editorFragment.getActivity()).y
                                + statusBarHeight);

                    popup.setClippingEnabled(false);
                    popup.showOnAnchor(_eventStatusView, RelativePopupWindow.VerticalPosition.ALIGN_TOP,
                            RelativePopupWindow.HorizontalPosition.ALIGN_LEFT, x, y);
                }
            });

        }

    }

    @Override
    public void onClick(View v) {
        editorFragment.startEventPreferencesActivity(event, 0);
    }

}
