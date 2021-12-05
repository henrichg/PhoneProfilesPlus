package sk.henrichg.phoneprofilesplus;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

class EventStatusPopupWindow extends GuiInfoPopupWindow {

    @SuppressLint("SetTextI18n")
    EventStatusPopupWindow(final EditorEventListFragment fragment, Event event) {
        super(R.layout.popup_window_event_status, R.string.editor_event_list_item_event_status, fragment.getActivity());

        // Disable default animation
        //setAnimationStyle(0);

        final TextView textView = popupView.findViewById(R.id.event_status_popup_window_text7);
        textView.setText(fragment.getString(R.string.popup_window_events_status_show_info) + " \u21D2");
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
            eventName.setText(fragment.getString(R.string.event_string_0)+": "+event._name);

            final SwitchCompat checkBox = popupView.findViewById(R.id.event_status_popup_window_checkbox);
            checkBox.setChecked(event.getStatus() != Event.ESTATUS_STOP);
            checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                //noinspection ConstantConditions
                if (fragment != null) {
                    if (!fragment.runStopEvent(_event))
                        checkBox.setChecked(false);
                }
            });
        }
    }

}
