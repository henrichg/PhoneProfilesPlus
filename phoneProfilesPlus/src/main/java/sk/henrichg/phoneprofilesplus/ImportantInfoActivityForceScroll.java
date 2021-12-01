package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class ImportantInfoActivityForceScroll extends AppCompatActivity {

    static final String EXTRA_SCROLL_TO = "extra_important_info_activity_scroll_to";
    static final String EXTRA_SHOW_FRAGMENT = "extra_important_info_activity_show_fragmenbt";

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, true/*, false*/, false, false); // must by called before super.onCreate()
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_important_info_force_scroll);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        Toolbar toolbar = findViewById(R.id.activity_important_info_force_scroll_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.important_info_activity_title);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(0)*/);
        }

        Intent intent = getIntent();
        boolean showQuickGuide = intent.getBooleanExtra(ImportantInfoActivity.EXTRA_SHOW_QUICK_GUIDE, false);
        int showFragment = intent.getIntExtra(EXTRA_SHOW_FRAGMENT, 0);
        int scrollTo = intent.getIntExtra(EXTRA_SCROLL_TO, 0);

        Fragment fragment = new ImportantInfoActivityForceScrollFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(ImportantInfoActivity.EXTRA_SHOW_QUICK_GUIDE, showQuickGuide);
        arguments.putInt(EXTRA_SHOW_FRAGMENT, showFragment);
        arguments.putInt(EXTRA_SCROLL_TO, scrollTo);
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_important_info_force_scroll_container, fragment, "ImportantInfoActivityForceScrollFragment")
                .commitAllowingStateLoss();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
