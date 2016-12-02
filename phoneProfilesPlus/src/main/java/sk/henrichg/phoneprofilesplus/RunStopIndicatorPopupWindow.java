package sk.henrichg.phoneprofilesplus;


import android.animation.Animator;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.labo.kaji.relativepopupwindow.RelativePopupWindow;

class RunStopIndicatorPopupWindow extends GuiInfoPopupWindow {

    RunStopIndicatorPopupWindow(DataWrapper dataWrapper, Activity activity) {
        super(R.layout.run_stop_indicator_popup_window, activity.getBaseContext());

        // Disable default animation for circular reveal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setAnimationStyle(0);
        }

        final DataWrapper _dataWrapper = dataWrapper;
        final Activity _activity = activity;

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.run_stop_indicator_popup_window_checkbox);
        checkBox.setChecked(GlobalData.getGlobalEventsRuning(activity.getApplicationContext()));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (_dataWrapper != null) {
                    _dataWrapper.runStopEvents();
                    if (_activity instanceof EditorProfilesActivity) {
                        _activity.invalidateOptionsMenu();
                        ((EditorProfilesActivity) _activity).refreshGUI(false, true);
                    } else if (_activity instanceof ActivateProfileActivity) {
                        ((ActivateProfileActivity) _activity).refreshGUI(false);
                    }
                }
            }
        });
    }

}
