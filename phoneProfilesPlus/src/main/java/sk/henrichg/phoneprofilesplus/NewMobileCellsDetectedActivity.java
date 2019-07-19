package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;


public class NewMobileCellsDetectedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, false/*, false*/); // must by called before super.onCreate()
        GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        /*
        setContentView(R.layout.activity_new_mobile_cells_detected);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_new_mobile_cells_detected);
            getSupportActionBar().setElevation(GlobalGUIRoutines.dpToPx(1));
        }
        */

    }

}
