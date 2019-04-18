package sk.henrichg.phoneprofilesplus;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

public class EventDetailsFragment extends Fragment {

    public long event_id;
    //public int editMode;
    //public int predefinedEventIndex;

    public EventDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * The fragment's current callback objects
     */
    private OnStartEventPreferencesFromDetail onStartEventPreferencesCallback = sDummyOnStartEventPreferencesCallback;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified.
     */
    // invoked when start profile preference fragment/activity needed
    interface OnStartEventPreferencesFromDetail {
        void onStartEventPreferencesFromDetail(Event event);
    }

    /**
     * A dummy implementation of the Callbacks interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static final OnStartEventPreferencesFromDetail sDummyOnStartEventPreferencesCallback = new OnStartEventPreferencesFromDetail() {
        public void onStartEventPreferencesFromDetail(Event event) {
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        onStartEventPreferencesCallback = (OnStartEventPreferencesFromDetail) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        onStartEventPreferencesCallback = sDummyOnStartEventPreferencesCallback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        setRetainInstance(true);

        //noinspection ConstantConditions
        event_id = getArguments().getLong(PPApplication.EXTRA_EVENT_ID, 0);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView eventName;
        TextView eventPreferencesDescription;
        ImageView eventStatus;
        ImageView profileStartIcon;
        TextView profileStartName;
        ImageView profileStartIndicator;
        ImageView profileEndIcon;
        TextView profileEndName;
        ImageView profileEndIndicator;
        AppCompatImageButton eventItemEdit;

        eventName = view.findViewById(R.id.event_detail_event_name);
        eventStatus = view.findViewById(R.id.event_detail_status);
        eventItemEdit = view.findViewById(R.id.event_detail_edit);
        profileStartName = view.findViewById(R.id.event_detail_profile_start_name);
        profileStartIcon = view.findViewById(R.id.event_detail_profile_start_icon);
        profileEndName = view.findViewById(R.id.event_detail_profile_end_name);
        profileEndIcon = view.findViewById(R.id.event_detail_profile_end_icon);
        //if (PPApplication.applicationEditorPrefIndicator)
        //{
            eventPreferencesDescription  = view.findViewById(R.id.event_detail_preferences_description);
            //holder.eventPreferencesDescription.setHorizontallyScrolling(true); // disable auto word wrap :-)
            profileStartIndicator = view.findViewById(R.id.event_detail_profile_start_pref_indicator);
            profileEndIndicator = view.findViewById(R.id.event_detail_profile_end_pref_indicator);
        //}
        //int defaultColor = eventName.getTextColors().getDefaultColor();

        //noinspection ConstantConditions
        DataWrapper dataWrapper = new DataWrapper(getActivity().getApplicationContext(), false, 0, false);

        final Event event = dataWrapper.getEventById(event_id);

        if (event != null)
        {
            int _eventStatus = event.getStatusFromDB(dataWrapper.context);
            boolean isRunnable = event.isRunnable(dataWrapper.context, true);
            boolean isPermissionGranted = Permissions.checkEventPermissions(dataWrapper.context, event).size() == 0;
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
                    //if (isRunnable)
                        statusRes = GlobalGUIRoutines.getThemeEventStopStatusIndicator(getActivity());
                    //else
                    //    statusRes = R.drawable.ic_event_status_stop_not_runnable;
                    break;
            }
            eventStatus.setImageResource(statusRes);

            if (_eventStatus == Event.ESTATUS_RUNNING) {
                eventName.setTypeface(null, Typeface.BOLD);
                eventName.setTextSize(16);
                //if (event._isInDelayEnd)
                //    eventName.setTextColor(GlobalGUIRoutines.getThemeEventInDelayColor(getActivity()));
                //else
                    eventName.setTextColor(GlobalGUIRoutines.getThemeAccentColor(getActivity()));
            }
            else
            if (!(isRunnable && isPermissionGranted)) {
                if (!isRunnable)
                    eventName.setTypeface(null, Typeface.ITALIC);
                else
                    eventName.setTypeface(null, Typeface.NORMAL);
                eventName.setTextSize(15);
                eventName.setTextColor(Color.RED);
            }
            else
            if (_eventStatus == Event.ESTATUS_STOP) {
                eventName.setTypeface(null, Typeface.ITALIC);
                eventName.setTextSize(15);
                eventName.setTextColor(GlobalGUIRoutines.getThemeEventStopColor(getActivity()));
            }
            else
            if (_eventStatus == Event.ESTATUS_PAUSE) {
                eventName.setTypeface(null, Typeface.NORMAL);
                eventName.setTextSize(15);
                //if (event._isInDelayEnd)
                //    eventName.setTextColor(GlobalGUIRoutines.getThemeEventInDelayColor(getActivity()));
                //else
                //    eventName.setTextColor(GlobalGUIRoutines.getThemeEventPauseColor(getActivity()));
                eventName.setTextColor(GlobalGUIRoutines.getThemeTextColor(getActivity()));
            }
            else {
                eventName.setTypeface(null, Typeface.NORMAL);
                eventName.setTextSize(15);
                eventName.setTextColor(GlobalGUIRoutines.getThemeTextColor(getActivity()));
            }

            String _eventName = event._name;
            String eventPriority = "";
            if (ApplicationPreferences.applicationEventUsePriority(getActivity()))
                eventPriority = "[P:" + (event._priority + Event.EPRIORITY_HIGHEST) + "] ";
            //else
            //    eventPriority = "[P:" + "5" + "] ";
            if (event._forceRun) {
                _eventName = eventPriority + "[\u00BB] " + _eventName;
            } else
                _eventName = eventPriority + _eventName;

            if (!event._startWhenActivatedProfile.isEmpty()) {
                String[] splits = event._startWhenActivatedProfile.split("\\|");
                Profile profile;
                if (splits.length == 1) {
                    profile = dataWrapper.getProfileById(Long.valueOf(event._startWhenActivatedProfile), false, false, false);
                    if (profile != null)
                        _eventName = _eventName + "\n" + "[#] " + profile._name;
                } else {
                    _eventName = _eventName + "\n" + "[#] " + getResources().getString(R.string.profile_multiselect_summary_text_selected) + " " + splits.length;
                }
            }

            //if (!isRunnable)
            //    _eventName = _eventName + "\n\n" + getResources().getString(R.string.event_preferences_error);
            eventName.setText(_eventName);

            //if (PPApplication.applicationEditorPrefIndicator)
            //{
                if (eventPreferencesDescription != null) {
                    String eventPrefDescription = event.getPreferencesDescription(getActivity().getApplicationContext(), false);
                    eventPreferencesDescription.setText(GlobalGUIRoutines.fromHtml(eventPrefDescription));
                }
            //}

            // profile start
            Profile profile =  dataWrapper.getProfileById(event._fkProfileStart, true, true, false);
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
                        //int res = getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                        //        getActivity().getPackageName());
                        int res = Profile.getIconResource(profile.getIconIdentifier());
                        profileStartIcon.setImageResource(res); // icon resource
                    }
                }
                else
                {
                    profileStartIcon.setImageBitmap(profile._iconBitmap);
                }

                //if (PPApplication.applicationEditorPrefIndicator)
                //{
                    //profilePrefIndicatorImageView.setImageBitmap(null);
                    //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                    //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                    if (profileStartIndicator != null) {
                        if (profile._preferencesIndicator != null)
                            profileStartIndicator.setImageBitmap(profile._preferencesIndicator);
                        else
                            profileStartIndicator.setImageResource(R.drawable.ic_empty);
                    }
                //}
            }
            else
            {
                profileStartName.setText(R.string.profile_preference_profile_not_set);
                profileStartIcon.setImageResource(R.drawable.ic_profile_default);
                //if (PPApplication.applicationEditorPrefIndicator)
                //{
                    //profilePrefIndicatorImageView.setImageBitmap(null);
                    //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                    //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                    if (profileStartIndicator != null)
                        profileStartIndicator.setImageResource(R.drawable.ic_empty);
                //}
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

                profile = dataWrapper.getProfileById(event._fkProfileEnd, true, true, false);
                if (profile != null) {
                    String profileName = profile._name;
                    if (event._delayStart > 0)
                        profileName = "[" + GlobalGUIRoutines.getDurationString(event._delayStart) + "] " + profileName;
                    if (event._atEndDo == Event.EATENDDO_UNDONE_PROFILE)
                        profileName = profileName + " + " + getResources().getString(R.string.event_preference_profile_undone);
                    else if (event._atEndDo == Event.EATENDDO_RESTART_EVENTS)
                        profileName = profileName + " + " + getResources().getString(R.string.event_preference_profile_restartEvents);
                    profileEndName.setText(profileName);
                    if (profile.getIsIconResourceID()) {
                        if (profile._iconBitmap != null)
                            profileEndIcon.setImageBitmap(profile._iconBitmap);
                        else {
                            //holder.profileEndIcon.setImageBitmap(null);
                            //int res = getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                            //        getActivity().getPackageName());
                            int res = Profile.getIconResource(profile.getIconIdentifier());
                            profileEndIcon.setImageResource(res); // icon resource
                        }
                    } else {
                        profileEndIcon.setImageBitmap(profile._iconBitmap);
                    }

                    //if (PPApplication.applicationEditorPrefIndicator) {
                        //profilePrefIndicatorImageView.setImageBitmap(null);
                        //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                        //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                        if (profileEndIndicator != null) {
                            if (profile._preferencesIndicator != null)
                                profileEndIndicator.setImageBitmap(profile._preferencesIndicator);
                            else
                                profileEndIndicator.setImageResource(R.drawable.ic_empty);
                        }
                    //}
                } else {
                    String profileName = "";
                    if (event._delayEnd > 0)
                        profileName = "[" + GlobalGUIRoutines.getDurationString(event._delayEnd) + "] " + profileName;
                    if (event._atEndDo == Event.EATENDDO_UNDONE_PROFILE)
                        profileName = profileName + getResources().getString(R.string.event_preference_profile_undone);
                    else if (event._atEndDo == Event.EATENDDO_RESTART_EVENTS)
                        profileName = profileName + getResources().getString(R.string.event_preference_profile_restartEvents);
                    else {
                        if (event._fkProfileEnd == Profile.PROFILE_NO_ACTIVATE)
                            profileName = profileName + getResources().getString(R.string.profile_preference_profile_end_no_activate);
                        else
                            profileName = profileName + getResources().getString(R.string.profile_preference_profile_not_set);
                    }
                    profileEndName.setText(profileName);
                    profileEndIcon.setImageResource(R.drawable.ic_empty);
                    //if (PPApplication.applicationEditorPrefIndicator) {
                        //profilePrefIndicatorImageView.setImageBitmap(null);
                        //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                        //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                        if (profileEndIndicator != null)
                            //holder.profileEndIndicator.setImageResource(R.drawable.ic_empty);
                            profileEndIndicator.setVisibility(View.GONE);
                    //}
                }
            }

            eventItemEdit.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    startEventPreferencesActivity(event);
                }
            });

        }

    }

    private void startEventPreferencesActivity(Event event)
    {
        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) one must start profile preferences
        onStartEventPreferencesCallback.onStartEventPreferencesFromDetail(event);
    }


}
