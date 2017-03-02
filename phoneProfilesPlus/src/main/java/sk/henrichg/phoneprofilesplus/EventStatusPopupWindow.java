package sk.henrichg.phoneprofilesplus;


import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;
import android.widget.TextView;

class EventStatusPopupWindow extends GuiInfoPopupWindow {

    EventStatusPopupWindow(EditorEventListFragment fragment, Event event) {
        super(R.layout.event_status_popup_window, fragment.getActivity().getBaseContext());

        if (event != null) {
            final EditorEventListFragment _fragment = fragment;
            final Event _event = event;

            TextView eventName = (TextView) view.findViewById(R.id.event_status_popup_window_text0);
            eventName.setText(fragment.getString(R.string.event_string_0)+": "+event._name);

            SwitchCompat checkBox = (SwitchCompat) view.findViewById(R.id.event_status_popup_window_checkbox);
            checkBox.setChecked(event.getStatus() != Event.ESTATUS_STOP);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (_fragment != null) {
                        _fragment.runStopEvent(_event);
                    }
                }
            });
        }
    }

}
