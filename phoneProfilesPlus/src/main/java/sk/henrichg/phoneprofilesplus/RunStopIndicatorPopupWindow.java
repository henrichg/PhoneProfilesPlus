package sk.henrichg.phoneprofilesplus;


import android.app.Activity;
import android.widget.CompoundButton;

import androidx.appcompat.widget.SwitchCompat;

class RunStopIndicatorPopupWindow extends GuiInfoPopupWindow {

    RunStopIndicatorPopupWindow(final DataWrapper dataWrapper, final Activity activity) {
        super(R.layout.run_stop_indicator_popup_window, activity);

        // Disable default animation
        setAnimationStyle(0);

        final SwitchCompat checkBox = popupView.findViewById(R.id.run_stop_indicator_popup_window_checkbox);
        checkBox.setChecked(Event.getGlobalEventsRunning(activity.getApplicationContext()));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (dataWrapper != null)
                    dataWrapper.runStopEventsWithAlert(activity, checkBox, isChecked);
            }
        });
    }

}
