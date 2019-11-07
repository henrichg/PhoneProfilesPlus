package sk.henrichg.phoneprofilesplus;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

class EventStatusPopupWindow extends GuiInfoPopupWindow {

    @SuppressLint("SetTextI18n")
    EventStatusPopupWindow(final EditorEventListFragment fragment, Event event) {
        super(R.layout.event_status_popup_window, R.string.editor_event_list_item_event_status, fragment.getActivity());

        // Disable default animation
        setAnimationStyle(0);

        final TextView textView = popupView.findViewById(R.id.event_status_popup_window_text7);
        textView.setClickable(true);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fragment.getActivity() != null) {
                    Intent intentLaunch = new Intent(fragment.getActivity(), ImportantInfoActivity.class);
                    intentLaunch.putExtra(ImportantInfoActivity.EXTRA_SHOW_QUICK_GUIDE, 0);
                    intentLaunch.putExtra(ImportantInfoActivity.EXTRA_SCROLL_TO, R.id.activity_info_notification_events);
                    fragment.getActivity().startActivity(intentLaunch);
                }

                dismiss();
            }
        });

        if (event != null) {
            final Event _event = event;

            TextView eventName = popupView.findViewById(R.id.event_status_popup_window_text0);
            eventName.setText(fragment.getString(R.string.event_string_0)+": "+event._name);

            SwitchCompat checkBox = popupView.findViewById(R.id.event_status_popup_window_checkbox);
            checkBox.setChecked(event.getStatus() != Event.ESTATUS_STOP);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    //noinspection ConstantConditions
                    if (fragment != null) {
                        fragment.runStopEvent(_event);
                    }
                }
            });
        }
    }

}
