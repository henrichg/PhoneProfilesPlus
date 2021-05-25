package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager.LayoutParams;

import androidx.appcompat.app.AppCompatActivity;

public class TileChooserActivity extends AppCompatActivity {

    int tileId = 0;

    static final String EXTRA_TILE_ID = "tile_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        PPApplication.logE("[BACKGROUND_ACTIVITY] TileChooserActivity.onCreate", "xxx");

        GlobalGUIRoutines.setTheme(this, true, false/*, false*/, false);
        //GlobalGUIRoutines.setLanguage(this);

    // set window dimensions ----------------------------------------------------------

        getWindow().setFlags(LayoutParams.FLAG_DIM_BEHIND, LayoutParams.FLAG_DIM_BEHIND);
        LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        getWindow().setAttributes(params);

        // display dimensions
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float popupWidth = displaymetrics.widthPixels;
        float popupMaxHeight = displaymetrics.heightPixels;
        //Display display = getWindowManager().getDefaultDisplay();
        //float popupWidth = display.getWidth();
        //float popupMaxHeight = display.getHeight();
        float popupHeight = 0;
        float actionBarHeight = 0;

        // action bar height
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(androidx.appcompat.R.attr.actionBarSize, tv, true))
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());

        // set max. dimensions for display orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            //popupWidth = Math.round(popupWidth / 100f * 50f);
            //popupMaxHeight = Math.round(popupMaxHeight / 100f * 90f);
            popupWidth = popupWidth / 100f * 50f;
        }
        else
        {
            //popupWidth = Math.round(popupWidth / 100f * 70f);
            //popupMaxHeight = Math.round(popupMaxHeight / 100f * 90f);
            popupWidth = popupWidth / 100f * 80f;
        }
        popupMaxHeight = popupMaxHeight / 100f * 90f;

        // add action bar height
        popupHeight = popupHeight + actionBarHeight;

        final float scale = getResources().getDisplayMetrics().density;

        // add list items height
        int profileCount = DatabaseHandler.getInstance(getApplicationContext()).getProfilesCount(/*false*/);
        ++profileCount; // for restart events
        if (profileCount > 0) {
            popupHeight = popupHeight + (60f * scale * profileCount); // item
            popupHeight = popupHeight + (1f * scale * (profileCount - 1)); // divider
        }
        else
            popupHeight = popupHeight + 60f * scale; // for empty TextView

        popupHeight = popupHeight + (20f * scale); // listview padding

        if (popupHeight > popupMaxHeight)
            popupHeight = popupMaxHeight;

        // set popup window dimensions
        getWindow().setLayout((int) (popupWidth + 0.5f), (int) (popupHeight + 0.5f));

    //-----------------------------------------------------------------------------------

        setContentView(R.layout.activity_tile_chooser);

        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_tile_chooser);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        Intent intent = getIntent();
        tileId = intent.getIntExtra(EXTRA_TILE_ID, 0);

//        Log.e("TileChooserActivity.onCreate", "tileId="+tileId);

    }

}
