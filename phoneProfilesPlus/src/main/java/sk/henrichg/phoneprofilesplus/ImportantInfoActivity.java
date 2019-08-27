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


public class ImportantInfoActivity extends AppCompatActivity {

    static final String EXTRA_SHOW_QUICK_GUIDE = "extra_important_info_activity_show_quick_guide";
    static final String EXTRA_SCROLL_TO = "extra_important_info_activity_scroll_to";

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, true/*, false*/); // must by called before super.onCreate()
        GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_important_info);

        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            switch (ApplicationPreferences.applicationTheme(getApplicationContext(), true)) {
                case "color":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary));
                    break;
                case "white":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primaryDark19_white));
                    break;
                default:
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary_dark));
                    break;
            }
        }
        */

        /*if (Build.VERSION.SDK_INT >= 21) {
            View toolbarShadow = findViewById(R.id.activity_important_info_toolbar_shadow);
            if (toolbarShadow != null)
                toolbarShadow.setVisibility(View.GONE);
        }*/

        Toolbar toolbar = findViewById(R.id.activity_important_info_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.important_info_activity_title);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(0)*/);
        }

        TabLayout tabLayout = findViewById(R.id.activity_important_info_tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.important_info_important_info_tab));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.important_info_quick_guide_tab));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager2 viewPager = findViewById(R.id.activity_important_info_pager);
        ImportantInfoActivityFragmentStateAdapterX adapter = new ImportantInfoActivityFragmentStateAdapterX(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(adapter);

        Intent intent = getIntent();
        boolean firstInstallation = intent.getBooleanExtra(ImportantInfoNotification.EXTRA_FIRST_INSTALLATION, false);
        int scrollTo = intent.getIntExtra(EXTRA_SCROLL_TO, 0);

        // add Fragments in your ViewPagerFragmentAdapter class
        ImportantInfoHelpFragment importantInfoHelpFragment = new ImportantInfoHelpFragment();
        importantInfoHelpFragment.scrollTo = scrollTo;
        importantInfoHelpFragment.firstInstallation = firstInstallation;
        adapter.addFragment(importantInfoHelpFragment);

        QuickGuideHelpFragment quickGuideHelpFragment = new QuickGuideHelpFragment();
        quickGuideHelpFragment.scrollTo = scrollTo;
        adapter.addFragment(quickGuideHelpFragment);

        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.OnConfigureTabCallback() {
                    @SuppressWarnings("unused")
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        if (position == 1)
                            tab.setText(R.string.important_info_quick_guide_tab);
                        else
                            tab.setText(R.string.important_info_important_info_tab);
                    }
                });
        tabLayoutMediator.attach();

        if (intent.getBooleanExtra(EXTRA_SHOW_QUICK_GUIDE, false)) {
            tabLayout.setScrollPosition(1,0f,true);
            viewPager.setCurrentItem(1);
        }
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
