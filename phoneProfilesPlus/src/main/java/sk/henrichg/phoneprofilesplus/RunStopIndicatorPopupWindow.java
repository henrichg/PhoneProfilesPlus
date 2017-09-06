package sk.henrichg.phoneprofilesplus;


import android.app.Activity;
import android.os.Build;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

class RunStopIndicatorPopupWindow extends GuiInfoPopupWindow {

    RunStopIndicatorPopupWindow(DataWrapper dataWrapper, Activity activity) {
        super(R.layout.run_stop_indicator_popup_window, activity.getBaseContext());

        // Disable default animation for circular reveal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setAnimationStyle(0);
        }

        final DataWrapper _dataWrapper = dataWrapper;
        final Activity _activity = activity;

        SwitchCompat checkBox = (SwitchCompat) view.findViewById(R.id.run_stop_indicator_popup_window_checkbox);
        checkBox.setChecked(Event.getGlobalEventsRunning(activity.getApplicationContext()));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (_dataWrapper != null) {
                    _dataWrapper.runStopEvents();
                    if (_activity instanceof EditorProfilesActivity) {
                        _activity.invalidateOptionsMenu();
                        ((EditorProfilesActivity) _activity).refreshGUI(false, true);
                    } else if (_activity instanceof ActivateProfileActivity) {
                        _activity.invalidateOptionsMenu();
                        ((ActivateProfileActivity) _activity).refreshGUI(false);
                    }
                }
            }
        });
    }

}
