package sk.henrichg.phoneprofilesplus;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

class EventStatusPopupWindow extends GuiInfoPopupWindow {

    @SuppressLint("SetTextI18n")
    EventStatusPopupWindow(final EditorEventListFragment fragment, Event event) {
        super(R.layout.popup_window_event_status, R.string.editor_event_list_item_event_status, fragment.getActivity());

        // Disable default animation
        //setAnimationStyle(0);

        final TextView textView = popupView.findViewById(R.id.event_status_popup_window_text7);
        textView.setText(fragment.getString(R.string.popup_window_events_status_show_info) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW);
        textView.setClickable(true);
        textView.setOnClickListener(v -> {
            if (fragment.getActivity() != null) {
                Intent intentLaunch = new Intent(fragment.getActivity(), ImportantInfoActivityForceScroll.class);
                intentLaunch.putExtra(ImportantInfoActivity.EXTRA_SHOW_QUICK_GUIDE, false);
                intentLaunch.putExtra(ImportantInfoActivityForceScroll.EXTRA_SHOW_FRAGMENT, 2);
                intentLaunch.putExtra(ImportantInfoActivityForceScroll.EXTRA_SCROLL_TO, R.id.activity_info_notification_event_states);
                fragment.getActivity().startActivity(intentLaunch);
            }

            dismiss();
        });

        if (event != null) {
            final Event _event = event;

            TextView eventName = popupView.findViewById(R.id.event_status_popup_window_text0);
            eventName.setText(fragment.getString(R.string.event_string_0)+StringConstants.STR_COLON_WITH_SPACE+event._name);

            Context context = fragment.getContext();

            TextView eventSateText = popupView.findViewById(R.id.event_status_popup_window_textState);
            ImageView eventStatusImage = popupView.findViewById(R.id.event_status_popup_window_imageState);
            TextView eventSateDescription = popupView.findViewById(R.id.event_status_popup_window_textStateDescription);

            if (context != null) {
                int _eventStatus = event.getStatus();
                boolean manualProfileActivation = DataWrapperStatic.getIsManualProfileActivation(false, context.getApplicationContext());

                int imageStatusRes = R.drawable.ic_event_status_stop; //GlobalGUIRoutines.getThemeEventStopStatusIndicator(context);
                int colorRes = R.color.event_status_stop;
                int textStatusRes = R.string.popup_event_states_stopped;

                /*if (!Event.getGlobalEventsRunning()) {
                    if (_eventStatus != Event.ESTATUS_STOP)
                        statusRes = R.drawable.ic_event_status_pause_manual_activation;
                }*/
                if (EventStatic.getGlobalEventsRunning(context)) {
                    //else {
                    switch (_eventStatus) {
                        case Event.ESTATUS_RUNNING:
                            if (event._isInDelayEnd) {
                                imageStatusRes = R.drawable.ic_event_status_running_delay;
                                colorRes = R.color.altype_eventDelayStartEnd;
                                textStatusRes = R.string.popup_event_states_running_delay;
                            } else {
                                imageStatusRes = R.drawable.ic_event_status_running;
                                colorRes = R.color.altype_eventStart;
                                textStatusRes = R.string.popup_event_states_running;
                            }
                            break;
                        case Event.ESTATUS_PAUSE:
                            if (/*!Event.getGlobalEventsRunning() ||*/ (manualProfileActivation && !event._ignoreManualActivation)) {
                                imageStatusRes = R.drawable.ic_event_status_pause_manual_activation;
                                colorRes = R.color.altype_eventEnd;
                                textStatusRes = R.string.popup_event_states_paused;
                            } else if (event._isInDelayStart) {
                                imageStatusRes = R.drawable.ic_event_status_pause_delay;
                                colorRes = R.color.altype_eventDelayStartEnd;
                                textStatusRes = R.string.popup_event_states_paused_delay;
                            } else {
                                imageStatusRes = R.drawable.ic_event_status_pause;
                                colorRes = R.color.altype_eventEnd;
                                textStatusRes = R.string.popup_event_states_paused;
                            }
                            break;
                        case Event.ESTATUS_STOP:
                            //if (isRunnable)
                            //noinspection ConstantConditions
                            imageStatusRes = R.drawable.ic_event_status_stop;
                            //noinspection ConstantConditions
                            colorRes = R.color.event_status_stop;
                            //statusRes = GlobalGUIRoutines.getThemeEventStopStatusIndicator(context);
                            //else
                            //    statusRes = R.drawable.ic_event_status_stop_not_runnable;
                            //noinspection ConstantConditions
                            textStatusRes = R.string.popup_event_states_stopped;
                            break;
                    }
                }
                eventStatusImage.setImageResource(imageStatusRes);
                eventStatusImage.setColorFilter(ContextCompat.getColor(context, colorRes));

                eventSateDescription.setText(textStatusRes);
            } else {
                eventSateText.setVisibility(View.GONE);
                eventStatusImage.setVisibility(View.GONE);
                eventSateDescription.setVisibility(View.GONE);
            }


            final SwitchCompat checkBox = popupView.findViewById(R.id.event_status_popup_window_checkbox);
            checkBox.setChecked(event.getStatus() != Event.ESTATUS_STOP);
            checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                //noinspection ConstantConditions
                if (fragment != null) {
                    boolean ok = EventStatic.runStopEvent(fragment.activityDataWrapper, _event, (EditorActivity) fragment.getActivity());
                    if (!ok)
                        checkBox.setChecked(false);
                }
            });
        }
    }

}
