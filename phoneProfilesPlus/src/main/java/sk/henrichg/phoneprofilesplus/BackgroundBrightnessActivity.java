package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class BackgroundBrightnessActivity extends AppCompatActivity {

    //private DataWrapper dataWrapper;
    //private long profile_id;

    private static final int DELAYED_MESSAGE = 1;
    static final String EXTRA_BRIGHTNESS_VALUE = "brightness_value";

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        //PPApplication.logE("BackgroundBrightnessActivity.onCreate", "xxx");

        Intent intent = getIntent();
        //profile_id = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);

        //dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == DELAYED_MESSAGE) {
                    BackgroundBrightnessActivity.this.finish();
                }
                super.handleMessage(msg);
            }
        };

        float brightnessValue = intent.getFloatExtra(EXTRA_BRIGHTNESS_VALUE, 0);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = brightnessValue;
        getWindow().setAttributes(lp);

        Message message = handler.obtainMessage(DELAYED_MESSAGE);
        //this next line is very important, you need to finish your activity with slight delay
        handler.sendMessageDelayed(message,200);
    }

    /*
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        //dataWrapper.invalidateDataWrapper();
        //dataWrapper = null;
    }
    */

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
