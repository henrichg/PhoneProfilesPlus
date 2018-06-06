package sk.henrichg.phoneprofilesplus;


import android.app.Activity;
import android.os.Build;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

class RunStopIndicatorPopupWindow extends GuiInfoPopupWindow {

    RunStopIndicatorPopupWindow(final DataWrapper dataWrapper, Activity activity) {
        super(R.layout.run_stop_indicator_popup_window, activity.getBaseContext());

        // Disable default animation for circular reveal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setAnimationStyle(0);
        }

        final DataWrapper _dataWrapper = dataWrapper;
        final Activity _activity = activity;

        SwitchCompat checkBox = popupView.findViewById(R.id.run_stop_indicator_popup_window_checkbox);
        checkBox.setChecked(Event.getGlobalEventsRunning(activity.getApplicationContext()));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (_dataWrapper != null) {
                    _dataWrapper.runStopEvents();
                    PPApplication.showProfileNotification(dataWrapper.context);
                    if (_activity instanceof EditorProfilesActivity)
                        ((EditorProfilesActivity) _activity).refreshGUI(false, true);
                    else if (_activity instanceof ActivateProfileActivity)
                        ((ActivateProfileActivity) _activity).refreshGUI(false);
                }
                ActivateProfileHelper.updateGUI(_activity, false);
            }
        });
    }

}
