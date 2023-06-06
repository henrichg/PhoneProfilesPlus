package sk.henrichg.phoneprofilesplus;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.skydoves.expandablelayout.ExpandableLayout;


public class ImportantInfoActivity extends AppCompatActivity {

    static final String EXTRA_SHOW_QUICK_GUIDE = "extra_important_info_activity_show_quick_guide";

    ExpandableLayout expandableLayoutSystem;
    ExpandableLayout expandableLayoutProfiles;
    ExpandableLayout expandableLayoutEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, true, false, false, false, false); // must by called before super.onCreate()
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_important_info);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            int packageVersionCode = PPApplicationStatic.getVersionCode(pInfo);
            ImportantInfoNotification.setShowInfoNotificationOnStart(getApplicationContext(), false, packageVersionCode);
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }

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
        ImportantInfoActivityFragmentStateAdapter adapter = new ImportantInfoActivityFragmentStateAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(adapter);
        // this fixes cropped fragment in Quick guide
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 1) {
                    if (expandableLayoutSystem != null) {
                        if (!expandableLayoutSystem.isExpanded()) {
                            if (expandableLayoutProfiles != null)
                                expandableLayoutProfiles.collapse();
                            if (expandableLayoutEvents != null)
                                expandableLayoutEvents.collapse();
                            expandableLayoutSystem.toggleLayout();
                        }
                    }
                }
            }
        });

        Intent intent = getIntent();
        boolean firstInstallation = intent.getBooleanExtra(ImportantInfoNotification.EXTRA_FIRST_INSTALLATION, false);

        // add Fragments in your ViewPagerFragmentAdapter class
        ImportantInfoHelpFragment importantInfoHelpFragment = new ImportantInfoHelpFragment();
        importantInfoHelpFragment.firstInstallation = firstInstallation;
        adapter.addFragment(importantInfoHelpFragment);

        ImportantInfoQuickGuideHelpFragment importantInfoQuickGuideHelpFragment = new ImportantInfoQuickGuideHelpFragment();
        adapter.addFragment(importantInfoQuickGuideHelpFragment);

        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 1)
                        tab.setText(R.string.important_info_quick_guide_tab);
                    else
                        tab.setText(R.string.important_info_important_info_tab);
                });
        tabLayoutMediator.attach();

        if (intent.getBooleanExtra(EXTRA_SHOW_QUICK_GUIDE, false)) {
            tabLayout.setScrollPosition(1,0f,true);
            viewPager.setCurrentItem(1);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
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
