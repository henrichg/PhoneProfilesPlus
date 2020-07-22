package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sk.henrichg.phoneprofilesplus.EditorEventListFragment.OnStartEventPreferences;
import sk.henrichg.phoneprofilesplus.EditorProfileListFragment.OnStartProfilePreferences;

//import me.drakeet.support.toast.ToastCompat;

public class EditorProfilesActivity extends AppCompatActivity
                                    implements OnStartProfilePreferences,
                                               OnStartEventPreferences
{

    //private static volatile EditorProfilesActivity instance;
    private boolean activityStarted = false;

    private ImageView eventsRunStopIndicator;

    private static boolean savedInstanceStateChanged;

    private static ApplicationsCache applicationsCache;

    @SuppressWarnings("rawtypes")
    private AsyncTask importAsyncTask = null;
    @SuppressWarnings("rawtypes")
    private AsyncTask importFromPPAsyncTask = null;
    @SuppressWarnings("rawtypes")
    private AsyncTask exportAsyncTask = null;
    static boolean doImport = false;
    private AlertDialog importProgressDialog = null;
    private AlertDialog exportProgressDialog = null;

    static boolean importFromPPStopped = false;

    private static final int DSI_PROFILES_ALL = 0;
    private static final int DSI_PROFILES_SHOW_IN_ACTIVATOR = 1;
    private static final int DSI_PROFILES_NO_SHOW_IN_ACTIVATOR = 2;
    private static final int DSI_EVENTS_START_ORDER = 0;
    private static final int DSI_EVENTS_ALL = 1;
    private static final int DSI_EVENTS_NOT_STOPPED = 2;
    private static final int DSI_EVENTS_RUNNING = 3;
    private static final int DSI_EVENTS_PAUSED = 4;
    private static final int DSI_EVENTS_STOPPED = 5;

    static final String EXTRA_NEW_PROFILE_MODE = "new_profile_mode";
    static final String EXTRA_PREDEFINED_PROFILE_INDEX = "predefined_profile_index";
    static final String EXTRA_NEW_EVENT_MODE = "new_event_mode";
    static final String EXTRA_PREDEFINED_EVENT_INDEX = "predefined_event_index";
    //static final String EXTRA_SELECTED_FILTER = "selected_filter";
    static final String EXTRA_FROM_RED_TEXT_PREFERENCES_NOTIFICATION = "from_red_text_preferences_notification";

    // request code for startActivityForResult with intent BackgroundActivateProfileActivity
    static final int REQUEST_CODE_ACTIVATE_PROFILE = 6220;
    // request code for startActivityForResult with intent ProfilesPrefsActivity
    private static final int REQUEST_CODE_PROFILE_PREFERENCES = 6221;
    // request code for startActivityForResult with intent EventPreferencesActivity
    private static final int REQUEST_CODE_EVENT_PREFERENCES = 6222;
    // request code for startActivityForResult with intent PhoneProfilesActivity
    private static final int REQUEST_CODE_APPLICATION_PREFERENCES = 6229;
    // request code for startActivityForResult with intent "phoneprofiles.intent.action.EXPORTDATA"
    //private static final int REQUEST_CODE_REMOTE_EXPORT = 6250;

    public boolean targetHelpsSequenceStarted;
    public static final String PREF_START_TARGET_HELPS = "editor_profiles_activity_start_target_helps";
    public static final String PREF_START_TARGET_HELPS_DEFAULT_PROFILE = "editor_profile_activity_start_target_helps_default_profile";
    public static final String PREF_START_TARGET_HELPS_FILTER_SPINNER = "editor_profile_activity_start_target_helps_filter_spinner";
    public static final String PREF_START_TARGET_HELPS_RUN_STOP_INDICATOR = "editor_profile_activity_start_target_helps_run_stop_indicator";
    public static final String PREF_START_TARGET_HELPS_BOTTOM_NAVIGATION = "editor_profile_activity_start_target_helps_bottom_navigation";

    private Toolbar editorToolbar;
    //Toolbar bottomToolbar;
    //private DrawerLayout drawerLayout;
    //private PPScrimInsetsFrameLayout drawerRoot;
    //private ListView drawerListView;
    //private ActionBarDrawerToggle drawerToggle;
    //private BottomNavigationView bottomNavigationView;
    private AppCompatSpinner filterSpinner;
    //private AppCompatSpinner orderSpinner;
    //private View headerView;
    //private ImageView drawerHeaderFilterImage;
    //private TextView drawerHeaderFilterTitle;
    //private TextView drawerHeaderFilterSubtitle;
    private BottomNavigationView bottomNavigationView;

    //private String[] drawerItemsTitle;
    //private String[] drawerItemsSubtitle;
    //private Integer[] drawerItemsIcon;

    private int editorSelectedView = 0;
    private int filterProfilesSelectedItem = 0;
    private int filterEventsSelectedItem = 0;

    private boolean startTargetHelps;

    AddProfileDialog addProfileDialog;
    AddEventDialog addEventDialog;

    private final BroadcastReceiver refreshGUIBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            //PPApplication.logE("[BROADCAST CALL] EditorProfilesActivity.refreshGUIBroadcastReceiver", "xxx");
            //boolean refresh = intent.getBooleanExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH, true);
            boolean refreshIcons = intent.getBooleanExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH_ICONS, false);
            long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
            long eventId = intent.getLongExtra(PPApplication.EXTRA_EVENT_ID, 0);
            // not change selection in editor if refresh is outside editor
            EditorProfilesActivity.this.refreshGUI(/*refresh,*//* true,*/  refreshIcons, false, profileId, eventId);
        }
    };

    private final BroadcastReceiver showTargetHelpsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            //PPApplication.logE("[BROADCAST CALL] EditorProfilesActivity.showTargetHelpsBroadcastReceiver", "xxx");
            Fragment fragment = EditorProfilesActivity.this.getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
            if (fragment != null) {
                if (fragment instanceof EditorProfileListFragment)
                    ((EditorProfileListFragment) fragment).showTargetHelps();
                else
                    ((EditorEventListFragment) fragment).showTargetHelps();
            }
        }
    };

    private final BroadcastReceiver finishBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            //PPApplication.logE("[BROADCAST CALL] EditorProfilesActivity.finishBroadcastReceiver", "xxx");
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(PPApplication.ACTION_FINISH_ACTIVITY)) {
                    String what = intent.getStringExtra(PPApplication.EXTRA_WHAT_FINISH);
                    if (what.equals("editor")) {
                        try {
                            EditorProfilesActivity.this.setResult(Activity.RESULT_CANCELED);
                            EditorProfilesActivity.this.finishAffinity();
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                }
            }
        }
    };

    @SuppressLint({"NewApi", "RestrictedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //PPApplication.logE("EditorProfilesActivity.onCreate", "xxx");

        GlobalGUIRoutines.setTheme(this, false, true/*, true*/, false);
        //GlobalGUIRoutines.setLanguage(this);

        savedInstanceStateChanged = (savedInstanceState != null);

        createApplicationsCache();

        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            setContentView(R.layout.activity_editor_list_onepane_19);
        else*/
            setContentView(R.layout.activity_editor_list_onepane);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        boolean doServiceStart = startPPServiceWhenNotStarted();
        if (showNotStartedToast()) {
            finish();
            return;
        }
        else
        if (doServiceStart) {
            finish();
            return;
        }

        activityStarted = true;

        //drawerLayout = findViewById(R.id.editor_list_drawer_layout);

        /*
        if (Build.VERSION.SDK_INT >= 21) {
            drawerLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                        int statusBarHeight = insets.getSystemWindowInsetTop();
                        PPApplication.logE("EditorProfilesActivity.onApplyWindowInsets", "statusBarHeight="+statusBarHeight);
                        Rect rect = insets.getSystemWindowInsets();
                        PPApplication.logE("EditorProfilesActivity.onApplyWindowInsets", "rect.top="+rect.top);
                        rect.top = rect.top + statusBarHeight;
                        return insets.replaceSystemWindowInsets(rect);
                    }
                }
            );
        }
        */

        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        //String appTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), true);

        /*
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            switch (appTheme) {
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

        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //	getWindow().setNavigationBarColor(R.attr.colorPrimary);

        //setWindowContentOverlayCompat();

    /*	// add profile list into list container
        EditorProfileListFragment fragment = new EditorProfileListFragment();
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment").commit(); */

        /*
        drawerRoot = findViewById(R.id.editor_drawer_root);

        // set status bar background for Activity body layout
        switch (appTheme) {
            case "color":
                drawerLayout.setStatusBarBackground(R.color.primaryDark);
                break;
            case "white":
                drawerLayout.setStatusBarBackground(R.color.primaryDark_white);
                break;
            case "dark":
                drawerLayout.setStatusBarBackground(R.color.primaryDark_dark);
                break;
            case "dlight":
                drawerLayout.setStatusBarBackground(R.color.primaryDark_dark);
                break;
        }

        drawerListView = findViewById(R.id.editor_drawer_list);
        //noinspection ConstantConditions
        headerView =  getLayoutInflater().inflate(R.layout.editor_drawer_list_header, drawerListView, false);
        drawerListView.addHeaderView(headerView, null, false);
        drawerHeaderFilterImage = findViewById(R.id.editor_drawer_list_header_icon);
        drawerHeaderFilterTitle = findViewById(R.id.editor_drawer_list_header_title);
        drawerHeaderFilterSubtitle = findViewById(R.id.editor_drawer_list_header_subtitle);

        // set header padding for notches
        //if (Build.VERSION.SDK_INT >= 21) {
            drawerRoot.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                    headerView.setPadding(
                            headerView.getPaddingLeft(),
                            headerView.getPaddingTop() + insets.getSystemWindowInsetTop(),
                            headerView.getPaddingRight(),
                            headerView.getPaddingBottom());
                    insets.consumeSystemWindowInsets();
                    drawerRoot.setOnApplyWindowInsetsListener(null);
                    return insets;
                }
            });
        //}

        //if (Build.VERSION.SDK_INT < 21)
        //    drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // actionbar titles
        drawerItemsTitle = new String[] {
                getResources().getString(R.string.editor_drawer_title_profiles),
                getResources().getString(R.string.editor_drawer_title_profiles),
                getResources().getString(R.string.editor_drawer_title_profiles),
                getResources().getString(R.string.editor_drawer_title_events),
                getResources().getString(R.string.editor_drawer_title_events),
                getResources().getString(R.string.editor_drawer_title_events),
                getResources().getString(R.string.editor_drawer_title_events),
                getResources().getString(R.string.editor_drawer_title_events)
              };

        // drawer item titles
        drawerItemsSubtitle = new String[] {
                getResources().getString(R.string.editor_drawer_list_item_profiles_all),
                getResources().getString(R.string.editor_drawer_list_item_profiles_show_in_activator),
                getResources().getString(R.string.editor_drawer_list_item_profiles_no_show_in_activator),
                getResources().getString(R.string.editor_drawer_list_item_events_start_order),
                getResources().getString(R.string.editor_drawer_list_item_events_all),
                getResources().getString(R.string.editor_drawer_list_item_events_running),
                getResources().getString(R.string.editor_drawer_list_item_events_paused),
                getResources().getString(R.string.editor_drawer_list_item_events_stopped)
              };

        drawerItemsIcon = new Integer[] {
                R.drawable.ic_events_drawer_profile_filter_2,
                R.drawable.ic_events_drawer_profile_filter_0,
                R.drawable.ic_events_drawer_profile_filter_1,
                R.drawable.ic_events_drawer_event_filter_2,
                R.drawable.ic_events_drawer_event_filter_2,
                R.drawable.ic_events_drawer_event_filter_0,
                R.drawable.ic_events_drawer_event_filter_1,
                R.drawable.ic_events_drawer_event_filter_3,
              };


        // Pass string arrays to EditorDrawerListAdapter
        // use action bar themed context
        //drawerAdapter = new EditorDrawerListAdapter(drawerListView, getSupportActionBar().getThemedContext(), drawerItemsTitle, drawerItemsSubtitle, drawerItemsIcon);
        EditorDrawerListAdapter drawerAdapter = new EditorDrawerListAdapter(getBaseContext(), drawerItemsTitle, drawerItemsSubtitle, drawerItemsIcon);
        
        // Set the MenuListAdapter to the ListView
        drawerListView.setAdapter(drawerAdapter);
 
        // Capture listview menu item click
        drawerListView.setOnItemClickListener(new DrawerItemClickListener());
        */

        editorToolbar = findViewById(R.id.editor_toolbar);
        setSupportActionBar(editorToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_editor);
        }

        //bottomToolbar = findViewById(R.id.editor_list_bottom_bar);

        /*
        // Enable ActionBar app icon to behave as action to toggle nav drawer
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        */

        /*
        // is required. This adds hamburger icon in toolbar
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.editor_drawer_open, R.string.editor_drawer_open)
        {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }
 
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
            // this disable animation
            //@Override
            //public void onDrawerSlide(View drawerView, float slideOffset)
            //{
            //      if(drawerView!=null && drawerView == drawerRoot){
            //            super.onDrawerSlide(drawerView, 0);
            //      }else{
            //            super.onDrawerSlide(drawerView, slideOffset);
            //      }
            //}
        };
        drawerLayout.addDrawerListener(drawerToggle);
        */

        bottomNavigationView = findViewById(R.id.editor_list_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_profiles_view:
                                //PPApplication.logE("EditorProfilesActivity.onNavigationItemSelected", "menu_profiles_view");
                                String[] filterItems = new String[] {
                                        getString(R.string.editor_drawer_title_profiles) + " - " + getString(R.string.editor_drawer_list_item_profiles_all),
                                        getString(R.string.editor_drawer_title_profiles) + " - " + getString(R.string.editor_drawer_list_item_profiles_show_in_activator),
                                        getString(R.string.editor_drawer_title_profiles) + " - " + getString(R.string.editor_drawer_list_item_profiles_no_show_in_activator),
                                };
                                GlobalGUIRoutines.HighlightedSpinnerAdapter filterSpinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                                        EditorProfilesActivity.this,
                                        R.layout.highlighted_filter_spinner,
                                        filterItems);
                                filterSpinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
                                filterSpinner.setAdapter(filterSpinnerAdapter);
                                selectFilterItem(0, filterProfilesSelectedItem, false, startTargetHelps);
                                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                                if (fragment instanceof EditorProfileListFragment)
                                    ((EditorProfileListFragment)fragment).showHeaderAndBottomToolbar();
                                break;
                            case R.id.menu_events_view:
                                //PPApplication.logE("EditorProfilesActivity.onNavigationItemSelected", "menu_events_view");
                                filterItems = new String[] {
                                        getString(R.string.editor_drawer_title_events) + " - " + getString(R.string.editor_drawer_list_item_events_start_order),
                                        getString(R.string.editor_drawer_title_events) + " - " + getString(R.string.editor_drawer_list_item_events_all),
                                        getString(R.string.editor_drawer_title_events) + " - " + getString(R.string.editor_drawer_list_item_events_not_stopped),
                                        getString(R.string.editor_drawer_title_events) + " - " + getString(R.string.editor_drawer_list_item_events_running),
                                        getString(R.string.editor_drawer_title_events) + " - " + getString(R.string.editor_drawer_list_item_events_paused),
                                        getString(R.string.editor_drawer_title_events) + " - " + getString(R.string.editor_drawer_list_item_events_stopped)
                                };
                                filterSpinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                                        EditorProfilesActivity.this,
                                        R.layout.highlighted_filter_spinner,
                                        filterItems);
                                filterSpinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
                                filterSpinner.setAdapter(filterSpinnerAdapter);
                                selectFilterItem(1, filterEventsSelectedItem, false, startTargetHelps);
                                fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                                if (fragment instanceof EditorEventListFragment) {
                                    ((EditorEventListFragment)fragment).showHeaderAndBottomToolbar();
                                }
                                break;
                        }
                        return true;
                    }
                });
        // set size of icons
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            final View iconView = menuView.getChildAt(i).findViewById(com.google.android.material.R.id.icon);
            final ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, displayMetrics);
            layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, displayMetrics);
            iconView.setLayoutParams(layoutParams);
        }

        filterSpinner = findViewById(R.id.editor_filter_spinner);
        String[] filterItems = new String[] {
                getString(R.string.editor_drawer_title_profiles) + " - " + getString(R.string.editor_drawer_list_item_profiles_all),
                getString(R.string.editor_drawer_title_profiles) + " - " + getString(R.string.editor_drawer_list_item_profiles_show_in_activator),
                getString(R.string.editor_drawer_title_profiles) + " - " + getString(R.string.editor_drawer_list_item_profiles_no_show_in_activator)
        };
        GlobalGUIRoutines.HighlightedSpinnerAdapter filterSpinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                this,
                R.layout.highlighted_filter_spinner,
                filterItems);
        filterSpinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
        filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
        filterSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(this/*getBaseContext()*/, R.color.highlighted_spinner_all));
/*        switch (appTheme) {
            case "dark":
                filterSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(getBaseContext(), R.color.editorFilterTitleColor_dark));
                //filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_dark);
                break;
            case "white":
                filterSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(getBaseContext(), R.color.editorFilterTitleColor_white));
                //filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_white);
                break;
//            case "dlight":
//                filterSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(getBaseContext(), R.color.editorFilterTitleColor));
//                filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_dlight);
//                break;
            default:
                filterSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(getBaseContext(), R.color.editorFilterTitleColor));
                //filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_white);
                break;
        }*/
        filterSpinner.setAdapter(filterSpinnerAdapter);
        filterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((GlobalGUIRoutines.HighlightedSpinnerAdapter)filterSpinner.getAdapter()).setSelection(position);
                selectFilterItem(editorSelectedView, position, true, true);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        eventsRunStopIndicator = findViewById(R.id.editor_list_run_stop_indicator);
        TooltipCompat.setTooltipText(eventsRunStopIndicator, getString(R.string.editor_activity_targetHelps_trafficLightIcon_title));
        eventsRunStopIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFinishing()) {
                    RunStopIndicatorPopupWindow popup = new RunStopIndicatorPopupWindow(getDataWrapper(), EditorProfilesActivity.this);

                    View contentView = popup.getContentView();
                    contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    int popupWidth = contentView.getMeasuredWidth();
                    //int popupHeight = contentView.getMeasuredHeight();
                    //Log.d("ActivateProfileActivity.eventsRunStopIndicator.onClick","popupWidth="+popupWidth);
                    //Log.d("ActivateProfileActivity.eventsRunStopIndicator.onClick","popupHeight="+popupHeight);

                    int[] runStopIndicatorLocation = new int[2];
                    //eventsRunStopIndicator.getLocationOnScreen(runStopIndicatorLocation);
                    eventsRunStopIndicator.getLocationInWindow(runStopIndicatorLocation);

                    int x = 0;
                    int y = 0;

                    if (runStopIndicatorLocation[0] + eventsRunStopIndicator.getWidth() - popupWidth < 0)
                        x = -(runStopIndicatorLocation[0] + eventsRunStopIndicator.getWidth() - popupWidth);

                    popup.setClippingEnabled(false); // disabled for draw outside activity
                    popup.showOnAnchor(eventsRunStopIndicator, RelativePopupWindow.VerticalPosition.ALIGN_TOP,
                            RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT, x, y, false);
                }
            }
        });
        
        // set drawer item and order
        //if ((savedInstanceState != null) || (ApplicationPreferences.applicationEditorSaveEditorState(getApplicationContext())))
        //{
            //filterSelectedItem = ApplicationPreferences.preferences.getInt(SP_EDITOR_DRAWER_SELECTED_ITEM, 1);
            editorSelectedView = ApplicationPreferences.editorSelectedView;
            filterProfilesSelectedItem = ApplicationPreferences.editorProfilesViewSelectedItem;
            filterEventsSelectedItem = ApplicationPreferences.editorEventsViewSelectedItem;
        //}

        startTargetHelps = false;
        if (editorSelectedView == 0)
            bottomNavigationView.setSelectedItemId(R.id.menu_profiles_view);
        else
            bottomNavigationView.setSelectedItemId(R.id.menu_events_view);
        /*
        if (editorSelectedView == 0)
            selectFilterItem(filterProfilesSelectedItem, false, false, false);
        else
            selectFilterItem(filterEventsSelectedItem, false, false, false);
        */

        /*
        // not working good, all activity is under status bar
        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                int statusBarSize = insets.getSystemWindowInsetTop();
                PPApplication.logE("EditorProfilesActivity.onApplyWindowInsets", "statusBarSize="+statusBarSize);
                return insets;
            }
        });
        */

        getApplicationContext().registerReceiver(finishBroadcastReceiver, new IntentFilter(PPApplication.ACTION_FINISH_ACTIVITY));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        boolean doServiceStart = startPPServiceWhenNotStarted();
        if (showNotStartedToast()) {
            if (!isFinishing())
                finish();
            return;
        }
        else
        if (doServiceStart) {
            if (!isFinishing())
                finish();
            return;
        }

        //PPApplication.logE("EditorProfilesActivity.onStart", "xxx");

        if (activityStarted) {
            Intent intent = new Intent(PPApplication.ACTION_FINISH_ACTIVITY);
            intent.putExtra(PPApplication.EXTRA_WHAT_FINISH, "activator");
            getApplicationContext().sendBroadcast(intent);

            LocalBroadcastManager.getInstance(this).registerReceiver(refreshGUIBroadcastReceiver,
                    new IntentFilter(PPApplication.PACKAGE_NAME + ".RefreshEditorGUIBroadcastReceiver"));
            LocalBroadcastManager.getInstance(this).registerReceiver(showTargetHelpsBroadcastReceiver,
                    new IntentFilter(PPApplication.PACKAGE_NAME + ".ShowEditorTargetHelpsBroadcastReceiver"));

            refreshGUI(/*true,*/ false, true, 0, 0);
        }
        else {
            if (!isFinishing())
                finish();
        }
    }

    private boolean showNotStartedToast() {
        boolean applicationStarted = PPApplication.getApplicationStarted(true);
        boolean fullyStarted = PPApplication.applicationFullyStarted /*&& (!PPApplication.applicationPackageReplaced)*/;
        if (!applicationStarted) {
            String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_not_started);
            PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
            return true;
        }
        if (!fullyStarted) {
            if ((PPApplication.startTimeOfApplicationStart > 0) &&
                    ((Calendar.getInstance().getTimeInMillis() - PPApplication.startTimeOfApplicationStart) > PPApplication.APPLICATION_START_DELAY)) {
                Intent activityIntent = new Intent(this, WorkManagerNotWorkingActivity.class);
                // clear all opened activities
                activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(activityIntent);
            }
            else {
                String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_starting_toast);
                PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
            }
            return true;
        }
        /*//boolean fullyStarted = true;
        if (applicationStarted) {
            //PhoneProfilesService instance = PhoneProfilesService.getInstance();
            //fullyStarted = instance.getApplicationFullyStarted();
            boolean fullyStarted = PPApplication.applicationFullyStarted;
            applicationStarted = fullyStarted && (!PPApplication.applicationPackageReplaced);
        }
        if (!applicationStarted) {
            String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_not_started);
            boolean fullyStarted = PPApplication.applicationFullyStarted;
            if (!fullyStarted)
                text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_starting_toast);
            PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
            return true;
        }*/
        return false;
    }

    private boolean startPPServiceWhenNotStarted() {
        // this is for list widget header
        if (!PPApplication.getApplicationStarted(true)) {
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("EditorProfilesActivity.onStart", "application is not started");
                PPApplication.logE("EditorProfilesActivity.onStart", "service instance=" + PhoneProfilesService.getInstance());
                if (PhoneProfilesService.getInstance() != null)
                    PPApplication.logE("EditorProfilesActivity.onStart", "service hasFirstStart=" + PhoneProfilesService.getInstance().getServiceHasFirstStart());
            }*/
            // start PhoneProfilesService
            //PPApplication.firstStartServiceStarted = false;
            PPApplication.setApplicationStarted(getApplicationContext(), true);
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
            //serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
            serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
            PPApplication.startPPService(this, serviceIntent, true);
            return true;
        } else {
            if ((PhoneProfilesService.getInstance() == null) || (!PhoneProfilesService.getInstance().getServiceHasFirstStart())) {
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("EditorProfilesActivity.onStart", "application is started");
                    PPApplication.logE("EditorProfilesActivity.onStart", "service instance=" + PhoneProfilesService.getInstance());
                    if (PhoneProfilesService.getInstance() != null)
                        PPApplication.logE("EditorProfilesActivity.onStart", "service hasFirstStart=" + PhoneProfilesService.getInstance().getServiceHasFirstStart());
                }*/
                // start PhoneProfilesService
                //PPApplication.firstStartServiceStarted = false;
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
                //serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
                PPApplication.startPPService(this, serviceIntent, true);
                return true;
            }
            //else {
            //    PPApplication.logE("EditorProfilesActivity.onStart", "application and service is started");
            //}
        }

        return false;
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        //PPApplication.logE("EditorProfilesActivity.onStop", "xxx");

        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshGUIBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(showTargetHelpsBroadcastReceiver);

        if ((addProfileDialog != null) && (addProfileDialog.mDialog != null) && addProfileDialog.mDialog.isShowing())
            addProfileDialog.mDialog.dismiss();
        if ((addEventDialog != null) && (addEventDialog.mDialog != null) && addEventDialog.mDialog.isShowing())
            addEventDialog.mDialog.dismiss();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if ((importProgressDialog != null) && importProgressDialog.isShowing()) {
            importProgressDialog.dismiss();
            importProgressDialog = null;
        }
        if ((exportProgressDialog != null) && exportProgressDialog.isShowing()) {
            exportProgressDialog.dismiss();
            exportProgressDialog = null;
        }
        if ((importAsyncTask != null) && !importAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            importAsyncTask.cancel(true);
            doImport = false;
        }
        if ((importFromPPAsyncTask != null) && !importFromPPAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            importFromPPAsyncTask.cancel(true);
            doImport = false;
        }
        if ((exportAsyncTask != null) && !exportAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            exportAsyncTask.cancel(true);
        }

        if (!savedInstanceStateChanged)
        {
            // no destroy caches on orientation change
            if (applicationsCache != null)
                applicationsCache.clearCache(true);
            applicationsCache = null;
        }

        try {
            getApplicationContext().unregisterReceiver(finishBroadcastReceiver);
        } catch (Exception e) {
            //PPApplication.recordException(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        editorToolbar.inflateMenu(R.menu.editor_top_bar);
        return true;
    }

    private static void onNextLayout(final View view, final Runnable runnable) {
        final ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ViewTreeObserver trueObserver;

                if (observer.isAlive()) {
                    trueObserver = observer;
                } else {
                    trueObserver = view.getViewTreeObserver();
                }

                trueObserver.removeOnGlobalLayoutListener(this);

                runnable.run();
            }
        });
    }

    @SuppressLint("AlwaysShowAction")
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);

        MenuItem menuItem;

        //menuItem = menu.findItem(R.id.menu_import_export);
        //menuItem.setTitle(getResources().getString(R.string.menu_import_export) + "  >");

        // change global events run/stop menu item title
        menuItem = menu.findItem(R.id.menu_run_stop_events);
        if (menuItem != null)
        {
            if (Event.getGlobalEventsRunning())
            {
                menuItem.setTitle(R.string.menu_stop_events);
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
            else
            {
                menuItem.setTitle(R.string.menu_run_events);
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
        }

        menuItem = menu.findItem(R.id.menu_restart_events);
        if (menuItem != null)
        {
            menuItem.setVisible(Event.getGlobalEventsRunning());
            menuItem.setEnabled(PPApplication.getApplicationStarted(true));
        }

        menuItem = menu.findItem(R.id.menu_dark_theme);
        if (menuItem != null)
        {
            String appTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), false);
            if (!appTheme.equals("night_mode")) {
                menuItem.setVisible(true);
                if (appTheme.equals("dark"))
                    menuItem.setTitle(R.string.menu_dark_theme_off);
                else
                    menuItem.setTitle(R.string.menu_dark_theme_on);
            }
            else
                menuItem.setVisible(false);
        }

        menuItem = menu.findItem(R.id.menu_email_debug_logs_to_author);
        if (menuItem != null)
        {
            //noinspection ConstantConditions
            menuItem.setVisible(PPApplication.logIntoFile || PPApplication.crashIntoFile);
        }

        menuItem = menu.findItem(R.id.menu_test_crash);
        if (menuItem != null)
        {
            menuItem.setVisible(DebugVersion.enabled);
        }
        menuItem = menu.findItem(R.id.menu_test_nonFatal);
        if (menuItem != null)
        {
            menuItem.setVisible(DebugVersion.enabled);
        }

        menuItem = menu.findItem(R.id.menu_import);
        if (menuItem != null) {
            File sd = Environment.getExternalStorageDirectory();
            File exportedDB = new File(sd, PPApplication.EXPORT_PATH + "/" + DatabaseHandler.EXPORT_DBFILENAME);
            boolean dbExists = exportedDB.exists();
            File exportedAppPreferences = new File(sd, PPApplication.EXPORT_PATH + "/" + GlobalGUIRoutines.EXPORT_APP_PREF_FILENAME);
            boolean appPreferencesExists = exportedAppPreferences.exists();
            menuItem.setEnabled(dbExists && appPreferencesExists);
        }

        menuItem = menu.findItem(R.id.menu_import_from_pp);
        if (menuItem != null) {
            try {
                PackageManager packageManager = getPackageManager();
                PackageInfo pInfo = packageManager.getPackageInfo(PPApplication.PACKAGE_NAME_PP, 0);
                int packageVersionCode = PPApplication.getVersionCode(pInfo);

                /*ActivityManager manager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> processes = manager.getRunningAppProcesses();
                boolean isRunnning = false;
                for (ActivityManager.RunningAppProcessInfo process : processes)
                {
                    if (process.processName.equals(PPApplication.PACKAGE_NAME_PP)) {
                        isRunnning = true;
                        break;
                    }
                }*/
                menuItem.setVisible((packageVersionCode >= 3601) /*&& isRunnning*/);
            } catch (PackageManager.NameNotFoundException e) {
                menuItem.setVisible(false);
            }
        }

        onNextLayout(editorToolbar, new Runnable() {
            @Override
            public void run() {
                showTargetHelps();
            }
        });

        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;

        DataWrapper dataWrapper = getDataWrapper();

        switch (item.getItemId()) {
/*            case android.R.id.home:
//                if (drawerLayout.isDrawerOpen(drawerRoot)) {
//                    drawerLayout.closeDrawer(drawerRoot);
//                } else {
//                    drawerLayout.openDrawer(drawerRoot);
//                }
                return super.onOptionsItemSelected(item);*/
            case R.id.menu_restart_events:
                //getDataWrapper().addActivityLog(DatabaseHandler.ALTYPE_RESTARTEVENTS, null, null, null, 0);

                // ignore manual profile activation
                // and unblock forceRun events
                //PPApplication.logE("$$$ restartEvents","from EditorProfilesActivity.onOptionsItemSelected menu_restart_events");
                if (dataWrapper != null)
                    dataWrapper.restartEventsWithAlert(this);
                return true;
            case R.id.menu_run_stop_events:
                if (dataWrapper != null)
                    dataWrapper.runStopEventsWithAlert(this, null, false);
                return true;
            case R.id.menu_activity_log:
                intent = new Intent(getBaseContext(), ActivityLogActivity.class);
                startActivity(intent);
                return true;
            case R.id.important_info:
                intent = new Intent(getBaseContext(), ImportantInfoActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_settings:
                intent = new Intent(getBaseContext(), PhoneProfilesPrefsActivity.class);
                startActivityForResult(intent, REQUEST_CODE_APPLICATION_PREFERENCES);
                return true;
            case R.id.menu_dark_theme:
                String theme = ApplicationPreferences.applicationTheme(getApplicationContext(), false);
                if (!theme.equals("night_mode")) {
                    SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(getApplicationContext());
                    Editor editor = preferences.edit();
                    if (theme.equals("dark")) {
                        //theme = preferences.getString(ApplicationPreferences.PREF_APPLICATION_NOT_DARK_THEME, "white");
                        //theme = ApplicationPreferences.applicationNightModeOffTheme(getApplicationContext());
                        editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, "white"/*theme*/);
                        editor.apply();
                        ApplicationPreferences.applicationTheme = "white";
                    } else {
                        //editor.putString(ApplicationPreferences.PREF_APPLICATION_NOT_DARK_THEME, theme);
                        editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, "dark");
                        editor.apply();
                        ApplicationPreferences.applicationTheme = "dark";
                    }
                    GlobalGUIRoutines.switchNightMode(getApplicationContext(), false);
                    GlobalGUIRoutines.reloadActivity(this, true);
                }
                return true;
            case R.id.menu_export:
                exportData(false, false);

                return true;
            case R.id.menu_export_and_email:
                exportData(true, false);

                return true;
            case R.id.menu_import:
                importData();

                return true;
            case R.id.menu_email_to_author:
                intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                String[] email = { "henrich.gron@gmail.com" };
                intent.putExtra(Intent.EXTRA_EMAIL, email);
                String packageVersion = "";
                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                    packageVersion = " - v" + pInfo.versionName + " (" + PPApplication.getVersionCode(pInfo) + ")";
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
                intent.putExtra(Intent.EXTRA_SUBJECT, "PhoneProfilesPlus" + packageVersion + " - " + getString(R.string.about_application_support_subject));
                intent.putExtra(Intent.EXTRA_TEXT, AboutApplicationActivity.getEmailBodyText(/*AboutApplicationActivity.EMAIL_BODY_SUPPORT, */this));
                try {
                    startActivity(Intent.createChooser(intent, getString(R.string.email_chooser)));
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }

                return true;
            case R.id.menu_export_and_email_to_author:
                exportData(true, true);

                return true;
            case R.id.menu_email_debug_logs_to_author:
                ArrayList<Uri> uris = new ArrayList<>();

                File sd = getApplicationContext().getExternalFilesDir(null);

                File logFile = new File(sd, PPApplication.LOG_FILENAME);
                if (logFile.exists()) {
                    Uri fileUri = FileProvider.getUriForFile(this, PPApplication.PACKAGE_NAME + ".provider", logFile);
                    uris.add(fileUri);
                }

                File crashFile = new File(sd, TopExceptionHandler.CRASH_FILENAME);
                if (crashFile.exists()) {
                    Uri fileUri = FileProvider.getUriForFile(this, PPApplication.PACKAGE_NAME + ".provider", crashFile);
                    uris.add(fileUri);
                }

                if (uris.size() != 0) {
                    String emailAddress = "henrich.gron@gmail.com";
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", emailAddress, null));

                    packageVersion = "";
                    try {
                        PackageInfo pInfo = getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                        packageVersion = " - v" + pInfo.versionName + " (" + PPApplication.getVersionCode(pInfo) + ")";
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "PhoneProfilesPlus" + packageVersion + " - " + getString(R.string.email_debug_log_files_subject));
                    emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    List<ResolveInfo> resolveInfo = getPackageManager().queryIntentActivities(emailIntent, 0);
                    List<LabeledIntent> intents = new ArrayList<>();
                    for (ResolveInfo info : resolveInfo) {
                        intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                        intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "PhoneProfilesPlus" + packageVersion + " - " + getString(R.string.email_debug_log_files_subject));
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris); //ArrayList<Uri> of attachment Uri's
                        intents.add(new LabeledIntent(intent, info.activityInfo.packageName, info.loadLabel(getPackageManager()), info.icon));
                    }
                    try {
                        Intent chooser = Intent.createChooser(intents.remove(intents.size() - 1), getString(R.string.email_chooser));
                        //noinspection ToArrayCallWithZeroLengthArrayArgument
                        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new LabeledIntent[intents.size()]));
                        startActivity(chooser);
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
                else {
                    // toast notification
                    PPApplication.showToast(getApplicationContext(), getString(R.string.toast_debug_log_files_not_exists),
                                                        Toast.LENGTH_SHORT);
                }

                return true;
            case R.id.menu_about:
                intent = new Intent(getBaseContext(), AboutApplicationActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_exit:
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(R.string.exit_application_alert_title);
                dialogBuilder.setMessage(R.string.exit_application_alert_message);
                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //PPApplication.logE("PPApplication.exitApp", "from EditorProfileActivity.onOptionsItemSelected shutdown=false");

                        //IgnoreBatteryOptimizationNotification.setShowIgnoreBatteryOptimizationNotificationOnStart(getApplicationContext(), true);
                        SharedPreferences settings = ApplicationPreferences.getSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN, false);
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT, false);
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_NEVER_ASK_FOR_GRANT_G1_PERMISSION, false);
                        editor.apply();
                        ApplicationPreferences.applicationEventNeverAskForEnableRun(getApplicationContext());
                        ApplicationPreferences.applicationNeverAskForGrantRoot(getApplicationContext());
                        ApplicationPreferences.applicationNeverAskForGrantG1Permission(getApplicationContext());

                        PPApplication.exitApp(true, getApplicationContext(), EditorProfilesActivity.this.getDataWrapper(),
                                EditorProfilesActivity.this, false/*, true, true*/);
                    }
                });
                dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
                AlertDialog dialog = dialogBuilder.create();

//                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                    @Override
//                    public void onShow(DialogInterface dialog) {
//                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                        if (positive != null) positive.setAllCaps(false);
//                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                        if (negative != null) negative.setAllCaps(false);
//                    }
//                });

                if (!isFinishing())
                    dialog.show();
                return true;
            case R.id.menu_test_crash:
                throw new RuntimeException("Test Crash");
                //return true;
            case R.id.menu_test_nonFatal:
                try {
                    throw new RuntimeException("Test non-fatal exception");
                } catch (Exception e) {
                    // You must relaunch PPP to get this exception in Firebase console:
                    //
                    // Crashlytics processes exceptions on a dedicated background thread, so the performance
                    // impact to your app is minimal. To reduce your users’ network traffic, Crashlytics batches
                    // logged exceptions together and sends them the next time the app launches.
                    //
                    // Crashlytics only stores the most recent 8 exceptions in a given app session. If your app
                    // throws more than 8 exceptions in a session, older exceptions are lost.
                    PPApplication.recordException(e);
                }
                return true;
            case R.id.gui_items_help:
                dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(R.string.gui_items_help_alert_title);
                dialogBuilder.setMessage(R.string.gui_items_help_alert_message);
                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //PPApplication.logE("PPApplication.exitApp", "from EditorProfileActivity.onOptionsItemSelected shutdown=false");
                        ApplicationPreferences.startStopTargetHelps(getApplicationContext(), true);
                        showTargetHelps();
                    }
                });
                dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
                dialog = dialogBuilder.create();

//                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                    @Override
//                    public void onShow(DialogInterface dialog) {
//                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                        if (positive != null) positive.setAllCaps(false);
//                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                        if (negative != null) negative.setAllCaps(false);
//                    }
//                });

                if (!isFinishing())
                    dialog.show();
                return true;
            case R.id.menu_import_from_pp:
                importDataFromPP();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    // fix for bug in LG stock ROM Android <= 4.1
    // https://code.google.com/p/android/issues/detail?id=78154
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String manufacturer = PPApplication.getROMManufacturer();
        if ((keyCode == KeyEvent.KEYCODE_MENU) &&
            (Build.VERSION.SDK_INT <= 16) &&
            (manufacturer != null) && (manufacturer.compareTo("lge") == 0)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        String manufacturer = PPApplication.getROMManufacturer();
        if ((keyCode == KeyEvent.KEYCODE_MENU) &&
            (Build.VERSION.SDK_INT <= 16) &&
            (manufacturer != null) && (manufacturer.compareTo("lge") == 0)) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
    */

    /////

    /*
    // ListView click listener in the navigation drawer
    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // header is position=0
            if (position > 0)
                selectFilterItem(position, true, false, true);
        }
    }
    */

    private void selectFilterItem(int selectedView, int position, boolean fromClickListener, boolean startTargetHelps) {
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("EditorProfilesActivity.selectFilterItem", "editorSelectedView=" + editorSelectedView);
            PPApplication.logE("EditorProfilesActivity.selectFilterItem", "selectedView=" + selectedView);
            PPApplication.logE("EditorProfilesActivity.selectFilterItem", "position=" + position);
        }*/

        boolean viewChanged = false;
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
        if (fragment instanceof EditorProfileListFragment) {
            if (selectedView != 0)
                viewChanged = true;
        } else
        if (fragment instanceof EditorEventListFragment) {
            if (selectedView != 1)
                viewChanged = true;
        }
        else
            viewChanged = true;

        int filterSelectedItem;
        if (selectedView == 0) {
            //PPApplication.logE("EditorProfilesActivity.selectFilterItem", "filterProfilesSelectedItem=" + filterProfilesSelectedItem);
            filterSelectedItem = filterProfilesSelectedItem;
        }
        else {
            //PPApplication.logE("EditorProfilesActivity.selectFilterItem", "filterEventsSelectedItem=" + filterEventsSelectedItem);
            filterSelectedItem = filterEventsSelectedItem;
        }

        if (viewChanged || (position != filterSelectedItem))
        {
            if (viewChanged) {
                // stop running AsyncTask
                if (fragment instanceof EditorProfileListFragment) {
                    if (((EditorProfileListFragment) fragment).isAsyncTaskPendingOrRunning()) {
                        ((EditorProfileListFragment) fragment).stopRunningAsyncTask();
                    }
                } else if (fragment instanceof EditorEventListFragment) {
                    if (((EditorEventListFragment) fragment).isAsyncTaskPendingOrRunning()) {
                        ((EditorEventListFragment) fragment).stopRunningAsyncTask();
                    }
                }
            }

            editorSelectedView = selectedView;
            if (editorSelectedView == 0) {
                filterProfilesSelectedItem = position;
            }
            else {
                filterEventsSelectedItem = position;
            }
            filterSelectedItem = position;
            //PPApplication.logE("EditorProfilesActivity.selectFilterItem", "filterEventsSelectedItem=" + filterEventsSelectedItem);

            // save into shared preferences
            Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
            editor.putInt(ApplicationPreferences.EDITOR_SELECTED_VIEW, editorSelectedView);
            editor.putInt(ApplicationPreferences.EDITOR_PROFILES_VIEW_SELECTED_ITEM, filterProfilesSelectedItem);
            editor.putInt(ApplicationPreferences.EDITOR_EVENTS_VIEW_SELECTED_ITEM, filterEventsSelectedItem);
            editor.apply();
            ApplicationPreferences.editorSelectedView(getApplicationContext());
            ApplicationPreferences.editorProfilesViewSelectedItem(getApplicationContext());
            ApplicationPreferences.editorEventsViewSelectedItem(getApplicationContext());

            Bundle arguments;

            int profilesFilterType;
            int eventsFilterType;
            //int eventsOrderType = getEventsOrderType();

            switch (editorSelectedView) {
                case 0:
                    switch (filterProfilesSelectedItem) {
                        case DSI_PROFILES_ALL:
                            profilesFilterType = EditorProfileListFragment.FILTER_TYPE_ALL;
                            //PPApplication.logE("EditorProfilesActivity.selectFilterItem", "profilesFilterType=FILTER_TYPE_ALL");
                            if (viewChanged) {
                                fragment = new EditorProfileListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorProfileListFragment.FILTER_TYPE_ARGUMENT, profilesFilterType);
                                arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorProfileListFragment displayedFragment = (EditorProfileListFragment)fragment;
                                displayedFragment.changeFragmentFilter(profilesFilterType, startTargetHelps);
                            }
                            break;
                        case DSI_PROFILES_SHOW_IN_ACTIVATOR:
                            profilesFilterType = EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR;
                            //PPApplication.logE("EditorProfilesActivity.selectFilterItem", "profilesFilterType=FILTER_TYPE_SHOW_IN_ACTIVATOR");
                            if (viewChanged) {
                                fragment = new EditorProfileListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorProfileListFragment.FILTER_TYPE_ARGUMENT, profilesFilterType);
                                arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorProfileListFragment displayedFragment = (EditorProfileListFragment)fragment;
                                displayedFragment.changeFragmentFilter(profilesFilterType, startTargetHelps);
                            }
                            break;
                        case DSI_PROFILES_NO_SHOW_IN_ACTIVATOR:
                            profilesFilterType = EditorProfileListFragment.FILTER_TYPE_NO_SHOW_IN_ACTIVATOR;
                            //PPApplication.logE("EditorProfilesActivity.selectFilterItem", "profilesFilterType=FILTER_TYPE_NO_SHOW_IN_ACTIVATOR");
                            if (viewChanged) {
                                fragment = new EditorProfileListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorProfileListFragment.FILTER_TYPE_ARGUMENT, profilesFilterType);
                                arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorProfileListFragment displayedFragment = (EditorProfileListFragment)fragment;
                                displayedFragment.changeFragmentFilter(profilesFilterType, startTargetHelps);
                            }
                            break;
                    }
                    break;
                case 1:
                    switch (filterEventsSelectedItem) {
                        case DSI_EVENTS_START_ORDER:
                            eventsFilterType = EditorEventListFragment.FILTER_TYPE_START_ORDER;
                            //PPApplication.logE("EditorProfilesActivity.selectFilterItem", "eventsFilterType=FILTER_TYPE_START_ORDER");
                            if (viewChanged) {
                                fragment = new EditorEventListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                                arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorEventListFragment displayedFragment = (EditorEventListFragment)fragment;
                                displayedFragment.changeFragmentFilter(eventsFilterType, startTargetHelps);
                            }
                            break;
                        case DSI_EVENTS_ALL:
                            eventsFilterType = EditorEventListFragment.FILTER_TYPE_ALL;
                            //PPApplication.logE("EditorProfilesActivity.selectFilterItem", "eventsFilterType=FILTER_TYPE_ALL");
                            if (viewChanged) {
                                fragment = new EditorEventListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                                arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorEventListFragment displayedFragment = (EditorEventListFragment)fragment;
                                displayedFragment.changeFragmentFilter(eventsFilterType, startTargetHelps);
                            }
                            break;
                        case DSI_EVENTS_NOT_STOPPED:
                            eventsFilterType = EditorEventListFragment.FILTER_TYPE_NOT_STOPPED;
                            //PPApplication.logE("EditorProfilesActivity.selectFilterItem", "eventsFilterType=FILTER_TYPE_NOT_STOPPED");
                            if (viewChanged) {
                                fragment = new EditorEventListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                                arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorEventListFragment displayedFragment = (EditorEventListFragment)fragment;
                                displayedFragment.changeFragmentFilter(eventsFilterType, startTargetHelps);
                            }
                            break;
                        case DSI_EVENTS_RUNNING:
                            eventsFilterType = EditorEventListFragment.FILTER_TYPE_RUNNING;
                            //PPApplication.logE("EditorProfilesActivity.selectFilterItem", "eventsFilterType=FILTER_TYPE_RUNNING");
                            if (viewChanged) {
                                fragment = new EditorEventListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                                arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorEventListFragment displayedFragment = (EditorEventListFragment)fragment;
                                displayedFragment.changeFragmentFilter(eventsFilterType, startTargetHelps);
                            }
                            break;
                        case DSI_EVENTS_PAUSED:
                            eventsFilterType = EditorEventListFragment.FILTER_TYPE_PAUSED;
                            //PPApplication.logE("EditorProfilesActivity.selectFilterItem", "eventsFilterType=FILTER_TYPE_PAUSED");
                            if (viewChanged) {
                                fragment = new EditorEventListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                                arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorEventListFragment displayedFragment = (EditorEventListFragment)fragment;
                                displayedFragment.changeFragmentFilter(eventsFilterType, startTargetHelps);
                            }
                            break;
                        case DSI_EVENTS_STOPPED:
                            eventsFilterType = EditorEventListFragment.FILTER_TYPE_STOPPED;
                            //PPApplication.logE("EditorProfilesActivity.selectFilterItem", "eventsFilterType=FILTER_TYPE_STOPPED");
                            if (viewChanged) {
                                fragment = new EditorEventListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                                arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorEventListFragment displayedFragment = (EditorEventListFragment)fragment;
                                displayedFragment.changeFragmentFilter(eventsFilterType, startTargetHelps);
                            }
                            break;
                    }
                    break;
            }
        }

        /*
        // header is position=0
        drawerListView.setItemChecked(drawerSelectedItem, true);
        // Get the title and icon followed by the position
        //editorToolbar.setSubtitle(drawerItemsTitle[drawerSelectedItem - 1]);
        //setIcon(drawerItemsIcon[drawerSelectedItem-1]);
        drawerHeaderFilterImage.setImageResource(drawerItemsIcon[drawerSelectedItem -1]);
        drawerHeaderFilterTitle.setText(drawerItemsTitle[drawerSelectedItem - 1]);
        */
        if (!fromClickListener)
            filterSpinner.setSelection(filterSelectedItem);

        //bottomToolbar.setVisibility(View.VISIBLE);

        // set filter status bar title
        //setStatusBarTitle();
        
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ACTIVATE_PROFILE)
        {
            Fragment _fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
            if (_fragment instanceof EditorProfileListFragment) {
                EditorProfileListFragment fragment = (EditorProfileListFragment) getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                if (fragment != null)
                    fragment.doOnActivityResult(requestCode, resultCode, data);
            }
        }
        else
        if (requestCode == REQUEST_CODE_PROFILE_PREFERENCES)
        {
            if ((resultCode == RESULT_OK) && (data != null))
            {
                long profile_id = data.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                int newProfileMode = data.getIntExtra(EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
                //int predefinedProfileIndex = data.getIntExtra(EXTRA_PREDEFINED_PROFILE_INDEX, 0);

                if (profile_id > 0)
                {
                    Profile profile = DatabaseHandler.getInstance(getApplicationContext()).getProfile(profile_id, false);
                    if (profile != null) {
                        // generate bitmaps
                        profile.generateIconBitmap(getApplicationContext(), false, 0, false);
                        profile.generatePreferencesIndicator(getApplicationContext(), false, 0);

                        // redraw list fragment , notifications, widgets after finish ProfilesPrefsActivity
                        redrawProfileListFragment(profile, newProfileMode);

                        //Profile mappedProfile = profile; //Profile.getMappedProfile(profile, getApplicationContext());
                        //Permissions.grantProfilePermissions(getApplicationContext(), profile, false, true,
                        //        /*true, false, 0,*/ PPApplication.STARTUP_SOURCE_EDITOR, false, true, false);
                        EditorProfilesActivity.displayPreferencesErrorNotification(profile, null, getApplicationContext());
                    }
                }

                /*Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
                PPApplication.startPPService(this, serviceIntent);*/
                Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
                //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
                PPApplication.runCommand(this, commandIntent);
            }
            else
            if (data != null) {
                boolean restart = data.getBooleanExtra(PhoneProfilesPrefsActivity.EXTRA_RESET_EDITOR, false);
                if (restart) {
                    // refresh activity for special changes
                    GlobalGUIRoutines.reloadActivity(this, true);
                }
            }
        }
        else
        if (requestCode == REQUEST_CODE_EVENT_PREFERENCES)
        {
            if ((resultCode == RESULT_OK) && (data != null))
            {
                // redraw list fragment after finish EventPreferencesActivity
                long event_id = data.getLongExtra(PPApplication.EXTRA_EVENT_ID, 0L);
                int newEventMode = data.getIntExtra(EXTRA_NEW_EVENT_MODE, EditorEventListFragment.EDIT_MODE_UNDEFINED);
                //int predefinedEventIndex = data.getIntExtra(EXTRA_PREDEFINED_EVENT_INDEX, 0);

                if (event_id > 0)
                {
                    Event event = DatabaseHandler.getInstance(getApplicationContext()).getEvent(event_id);

                    // redraw list fragment , notifications, widgets after finish EventPreferencesActivity
                    redrawEventListFragment(event, newEventMode);

                    //Permissions.grantEventPermissions(getApplicationContext(), event, true, false);
                    EditorProfilesActivity.displayPreferencesErrorNotification(null, event, getApplicationContext());
                }

                /*Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
                PPApplication.startPPService(this, serviceIntent);*/
                Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
                //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
                PPApplication.runCommand(this, commandIntent);

                //IgnoreBatteryOptimizationNotification.showNotification(getApplicationContext());
            }
            else
            if (data != null) {
                boolean restart = data.getBooleanExtra(PhoneProfilesPrefsActivity.EXTRA_RESET_EDITOR, false);
                if (restart) {
                    // refresh activity for special changes
                    GlobalGUIRoutines.reloadActivity(this, true);
                }
            }
        }
        else
        if (requestCode == REQUEST_CODE_APPLICATION_PREFERENCES)
        {
            if (resultCode == RESULT_OK)
            {
//                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
//                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
//                serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
//                PPApplication.startPPService(this, serviceIntent);
//                Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
//                //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
//                commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
//                PPApplication.runCommand(this, commandIntent);

//                if (PhoneProfilesService.getInstance() != null) {
//
//                    boolean powerSaveMode = PPApplication.isPowerSaveMode;
//                    if ((PhoneProfilesService.isGeofenceScannerStarted())) {
//                        PhoneProfilesService.getGeofencesScanner().resetLocationUpdates(powerSaveMode, true);
//                    }
//                    PhoneProfilesService.getInstance().resetListeningOrientationSensors(powerSaveMode, true);
//                    if (PhoneProfilesService.isPhoneStateScannerStarted())
//                        PhoneProfilesService.phoneStateScanner.resetListening(powerSaveMode, true);
//
//                }

                boolean restart = data.getBooleanExtra(PhoneProfilesPrefsActivity.EXTRA_RESET_EDITOR, false);
                //PPApplication.logE("EditorProfilesActivity.onActivityResult", "restart="+restart);

                if (restart)
                {
                    // refresh activity for special changes
                    GlobalGUIRoutines.reloadActivity(this, true);
                }
            }
        }
        /*else
        if (requestCode == REQUEST_CODE_REMOTE_EXPORT)
        {
            if (resultCode == RESULT_OK)
            {
                doImportData(GlobalGUIRoutines.REMOTE_EXPORT_PATH);
            }
        }*/
        /*else
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_PROFILE) {
            if (data != null) {
                long profileId = data.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                int startupSource = data.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, 0);
                boolean mergedProfile = data.getBooleanExtra(Permissions.EXTRA_MERGED_PROFILE, false);
                boolean activateProfile = data.getBooleanExtra(Permissions.EXTRA_ACTIVATE_PROFILE, false);

                if (activateProfile && (getDataWrapper() != null)) {
                    Profile profile = getDataWrapper().getProfileById(profileId, false, false, mergedProfile);
                    getDataWrapper().activateProfileFromMainThread(profile, mergedProfile, startupSource, this);
                }
            }
        }*/
        else
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_EXPORT) {
            if (resultCode == RESULT_OK) {
                doExportData(false, false);
            }
        }
        else
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_EXPORT_AND_EMAIL) {
            if (resultCode == RESULT_OK) {
                doExportData(true, false);
            }
        }
        else
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_IMPORT) {
            if ((resultCode == RESULT_OK) && (data != null)) {
                doImportData(data.getStringExtra(Permissions.EXTRA_APPLICATION_DATA_PATH));
            }
        }
        else
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_EXPORT_AND_EMAIL_TO_AUTHOR) {
            if (resultCode == RESULT_OK) {
                doExportData(true, true);
            }
        }
    }

    /*
    @Override
    public void onBackPressed()
    {
        if (drawerLayout.isDrawerOpen(drawerRoot))
            drawerLayout.closeDrawer(drawerRoot);
        else
            super.onBackPressed();
    }
    */

    /*
    @Override
    public void openOptionsMenu() {
        Configuration config = getResources().getConfiguration();
        if ((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) > Configuration.SCREENLAYOUT_SIZE_LARGE) {
            int originalScreenLayout = config.screenLayout;
            config.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
            super.openOptionsMenu();
            config.screenLayout = originalScreenLayout;
        } else {
            super.openOptionsMenu();
        }
    }
    */

    private void importExportErrorDialog(int importExport, int dbResult, int appSettingsResult, int sharedProfileResult)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        String title;
        if (importExport == 1)
            title = getString(R.string.import_profiles_alert_title);
        else
        if (importExport == 3)
            title = getString(R.string.import_profiles_from_pp_alert_title);
        else
            title = getString(R.string.export_profiles_alert_title);
        dialogBuilder.setTitle(title);
        String message;
        if (importExport == 1) {
            message = getString(R.string.import_profiles_alert_error) + ":";
            if (dbResult != DatabaseHandler.IMPORT_OK) {
                if (dbResult == DatabaseHandler.IMPORT_ERROR_NEVER_VERSION)
                    message = message + "\n• " + getString(R.string.import_profiles_alert_error_database_newer_version);
                else
                    message = message + "\n• " + getString(R.string.import_profiles_alert_error_database_bug);
            }
            if (appSettingsResult == 0)
                message = message + "\n• " + getString(R.string.import_profiles_alert_error_appSettings_bug);
            if (sharedProfileResult == 0)
                message = message + "\n• " + getString(R.string.import_profiles_alert_error_sharedProfile_bug);
        }
        else
        if (importExport == 3) {
            message = getString(R.string.import_profiles_from_pp_alert_error) + ":";
            if (dbResult != DatabaseHandler.IMPORT_OK) {
                message = message + "\n• " + getString(R.string.import_profiles_from_pp_alert_error_database_bug);
            }
            if (appSettingsResult == 0)
                message = message + "\n• " + getString(R.string.import_profiles_from_pp_alert_error_appSettings_bug);
            /*if (sharedProfileResult == 0)
                message = message + "\n• " + getString(R.string.import_profiles_from_pp_alert_error_sharedProfile_bug);*/
        }
        else
            message = getString(R.string.export_profiles_alert_error);
        dialogBuilder.setMessage(message);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // refresh activity
                GlobalGUIRoutines.reloadActivity(EditorProfilesActivity.this, true);
            }
        });
        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // refresh activity
                GlobalGUIRoutines.reloadActivity(EditorProfilesActivity.this, true);
            }
        });
        AlertDialog dialog = dialogBuilder.create();

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        if (!isFinishing())
            dialog.show();
    }

    @SuppressLint({"SetWorldReadable", "SetWorldWritable"})
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean importApplicationPreferences(File src, int what) {
        boolean res = true;
        ObjectInputStream input = null;
        try {
            try {
                if (src.exists()) {
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        src.setReadable(true, false);
                    } catch (Exception ee) {
                        PPApplication.recordException(ee);
                    }
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        src.setWritable(true, false);
                    } catch (Exception ee) {
                        PPApplication.recordException(ee);
                    }

                    input = new ObjectInputStream(new FileInputStream(src));
                    Editor prefEdit;
                    if (what == 1)
                        prefEdit = getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE).edit();
                    else
                        prefEdit = getSharedPreferences("profile_preferences_default_profile", Activity.MODE_PRIVATE).edit();
                    prefEdit.clear();
                    //noinspection unchecked
                    Map<String, ?> entries = (Map<String, ?>) input.readObject();
                    for (Entry<String, ?> entry : entries.entrySet()) {
                        Object v = entry.getValue();
                        String key = entry.getKey();

                        if (v instanceof Boolean)
                            prefEdit.putBoolean(key, (Boolean) v);
                        else if (v instanceof Float)
                            prefEdit.putFloat(key, (Float) v);
                        else if (v instanceof Integer)
                            prefEdit.putInt(key, (Integer) v);
                        else if (v instanceof Long)
                            prefEdit.putLong(key, (Long) v);
                        else if (v instanceof String)
                            prefEdit.putString(key, ((String) v));

                        if (what == 1) {
                            if (key.equals(ApplicationPreferences.PREF_APPLICATION_THEME)) {
                                if (v.equals("light") || v.equals("material") || v.equals("color") || v.equals("dlight")) {
                                    String defaultValue = "white";
                                    if (Build.VERSION.SDK_INT >= 28)
                                        defaultValue = "night_mode";
                                    prefEdit.putString(key, defaultValue);
                                }
                            }
                            if (key.equals(ActivateProfileHelper.PREF_MERGED_RING_NOTIFICATION_VOLUMES))
                                ActivateProfileHelper.setMergedRingNotificationVolumes(getApplicationContext(), /*true,*/ prefEdit);
                            if (key.equals(ApplicationPreferences.PREF_APPLICATION_FIRST_START))
                                prefEdit.putBoolean(ApplicationPreferences.PREF_APPLICATION_FIRST_START, false);
                        }

                    /*if (what == 2) {
                        if (key.equals(Profile.PREF_PROFILE_LOCK_DEVICE)) {
                            if (v.equals("3"))
                            prefEdit.putString(Profile.PREF_PROFILE_LOCK_DEVICE, "1");
                        }
                    }*/
                    }
                    prefEdit.apply();
                    if (what == 1) {
                        // save version code
                        try {
                            Context appContext = getApplicationContext();
                            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                            int actualVersionCode = PPApplication.getVersionCode(pInfo);
                            PPApplication.setSavedVersionCode(appContext, actualVersionCode);
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }

                    PPApplication.loadGlobalApplicationData(getApplicationContext());
                    PPApplication.loadApplicationPreferences(getApplicationContext());
                    PPApplication.loadProfileActivationData(getApplicationContext());
                }
                else
                    res = false;
            }/* catch (FileNotFoundException ignored) {
                // no error, this is OK
            }*/ catch (Exception e) {
                //Log.e("EditorProfilesActivity.importApplicationPreferences", Log.getStackTraceString(e));
                PPApplication.recordException(e);
                res = false;
            }
        }finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                PPApplication.recordException(e);
            }

            WifiScanWorker.setScanRequest(getApplicationContext(), false);
            WifiScanWorker.setWaitForResults(getApplicationContext(), false);
            WifiScanWorker.setWifiEnabledForScan(getApplicationContext(), false);

            BluetoothScanWorker.setScanRequest(getApplicationContext(), false);
            BluetoothScanWorker.setLEScanRequest(getApplicationContext(), false);
            BluetoothScanWorker.setWaitForResults(getApplicationContext(), false);
            BluetoothScanWorker.setWaitForLEResults(getApplicationContext(), false);
            BluetoothScanWorker.setBluetoothEnabledForScan(getApplicationContext(), false);
            BluetoothScanWorker.setScanKilled(getApplicationContext(), false);

        }
        return res;
    }

    private void doImportData(String applicationDataPath)
    {
        final EditorProfilesActivity activity = this;
        final String _applicationDataPath = applicationDataPath;

        if (Permissions.checkImport(getApplicationContext())) {

            @SuppressLint("StaticFieldLeak")
            class ImportAsyncTask extends AsyncTask<Void, Integer, Integer> {
                private final DataWrapper _dataWrapper;
                private int dbError = DatabaseHandler.IMPORT_OK;
                private boolean appSettingsError = false;
                private boolean sharedProfileError = false;

                private ImportAsyncTask() {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                    dialogBuilder.setMessage(R.string.import_profiles_alert_title);

                    LayoutInflater inflater = (activity.getLayoutInflater());
                    @SuppressLint("InflateParams")
                    View layout = inflater.inflate(R.layout.dialog_progress_bar, null);
                    dialogBuilder.setView(layout);

                    importProgressDialog = dialogBuilder.create();

//                    importProgressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                        @Override
//                        public void onShow(DialogInterface dialog) {
//                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (positive != null) positive.setAllCaps(false);
//                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                            if (negative != null) negative.setAllCaps(false);
//                        }
//                    });

                    _dataWrapper = getDataWrapper();
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    doImport = true;

                    GlobalGUIRoutines.lockScreenOrientation(activity, false);
                    importProgressDialog.setCancelable(false);
                    importProgressDialog.setCanceledOnTouchOutside(false);
                    if (!activity.isFinishing())
                        importProgressDialog.show();

                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                    if (fragment != null) {
                        if (fragment instanceof EditorProfileListFragment)
                            ((EditorProfileListFragment) fragment).removeAdapter();
                        else
                            ((EditorEventListFragment) fragment).removeAdapter();
                    }
                }

                @SuppressLint({"SetWorldReadable", "SetWorldWritable"})
                @Override
                protected Integer doInBackground(Void... params) {
                    //PPApplication.logE("PPApplication.exitApp", "from EditorProfilesActivity.doImportData shutdown=false");
                    if (_dataWrapper != null) {
                        PPApplication.exitApp(false, _dataWrapper.context, _dataWrapper, null, false/*, false, true*/);

                        File sd = Environment.getExternalStorageDirectory();
                        //File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

                        try {
                            File exportPath = new File(sd, _applicationDataPath);
                            if (exportPath.exists()) {
                                //noinspection ResultOfMethodCallIgnored
                                exportPath.setReadable(true, false);
                            }
                            if (exportPath.exists()) {
                                //noinspection ResultOfMethodCallIgnored
                                exportPath.setWritable(true, false);
                            }
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }

                        // import application preferences must be first,
                        // because in DatabaseHandler.importDB is recompute of volumes in profiles
                        File exportFile = new File(sd, _applicationDataPath + "/" + GlobalGUIRoutines.EXPORT_APP_PREF_FILENAME);
                        appSettingsError = !importApplicationPreferences(exportFile, 1);
                        exportFile = new File(sd, _applicationDataPath + "/" + GlobalGUIRoutines.EXPORT_DEF_PROFILE_PREF_FILENAME);
                        if (exportFile.exists())
                            sharedProfileError = !importApplicationPreferences(exportFile, 2);

                        dbError = DatabaseHandler.getInstance(_dataWrapper.context).importDB(_applicationDataPath);
                        if (dbError == DatabaseHandler.IMPORT_OK) {
                            DatabaseHandler.getInstance(_dataWrapper.context).updateAllEventsStatus(Event.ESTATUS_RUNNING, Event.ESTATUS_PAUSE);
                            DatabaseHandler.getInstance(_dataWrapper.context).updateAllEventsSensorsPassed(EventPreferences.SENSOR_PASSED_WAITING);
                            DatabaseHandler.getInstance(_dataWrapper.context).deactivateProfile();
                            DatabaseHandler.getInstance(_dataWrapper.context).unblockAllEvents();
                            DatabaseHandler.getInstance(_dataWrapper.context).disableNotAllowedPreferences();
                            //this.dataWrapper.invalidateProfileList();
                            //this.dataWrapper.invalidateEventList();
                            //this.dataWrapper.invalidateEventTimelineList();
                            Event.setEventsBlocked(_dataWrapper.context, false);
                            DatabaseHandler.getInstance(_dataWrapper.context).unblockAllEvents();
                            Event.setForceRunEventRunning(_dataWrapper.context, false);
                        }

                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("EditorProfilesActivity.doImportData", "dbError=" + dbError);
                            PPApplication.logE("EditorProfilesActivity.doImportData", "appSettingsError=" + appSettingsError);
                            PPApplication.logE("EditorProfilesActivity.doImportData", "sharedProfileError=" + sharedProfileError);
                        }*/

                        if (!appSettingsError) {
                            /*Editor editor = ApplicationPreferences.preferences.edit();
                            editor.putInt(EDITOR_PROFILES_VIEW_SELECTED_ITEM, 0);
                            editor.putInt(EDITOR_EVENTS_VIEW_SELECTED_ITEM, 0);
                            editor.putInt(EditorEventListFragment.SP_EDITOR_ORDER_SELECTED_ITEM, 0);
                            editor.apply();*/

                            Permissions.setAllShowRequestPermissions(_dataWrapper.context, true);

                            //WifiBluetoothScanner.setShowEnableLocationNotification(_dataWrapper.context, true, WifiBluetoothScanner.SCANNER_TYPE_WIFI);
                            //WifiBluetoothScanner.setShowEnableLocationNotification(_dataWrapper.context, true, WifiBluetoothScanner.SCANNER_TYPE_BLUETOOTH);
                            //PhoneStateScanner.setShowEnableLocationNotification(_dataWrapper.context, true);
                        }

                        if ((dbError == DatabaseHandler.IMPORT_OK) && (!(appSettingsError || sharedProfileError)))
                            return 1;
                        else
                            return 0;
                    }
                    else
                        return 0;
                }

                @Override
                protected void onPostExecute(Integer result) {
                    super.onPostExecute(result);

                    doImport = false;

                    if (!isFinishing()) {
                        if ((importProgressDialog != null) && importProgressDialog.isShowing()) {
                            if (!isDestroyed())
                                importProgressDialog.dismiss();
                            importProgressDialog = null;
                        }
                        GlobalGUIRoutines.unlockScreenOrientation(activity);
                    }

                    if (_dataWrapper != null) {
                        //PPApplication.logE("DataWrapper.updateNotificationAndWidgets", "from EditorProfilesActivity.doImportData");

                        // clear shared preferences for last activated profile
                        Profile profile = DataWrapper.getNonInitializedProfile("", null, 0);
                        Profile.saveProfileToSharedPreferences(profile, _dataWrapper.context, PPApplication.ACTIVATED_PROFILE_PREFS_NAME);
                        PPApplication.setLastActivatedProfile(_dataWrapper.context, 0);

                        //PPApplication.updateNotificationAndWidgets(true, true, _dataWrapper.context);
                        //PPApplication.logE("###### PPApplication.updateGUI", "from=EditorProfilesActivity.doImportData");
                        PPApplication.updateGUI(true/*_dataWrapper.context, true, true*/);

                        PPApplication.setApplicationStarted(_dataWrapper.context, true);
                        Intent serviceIntent = new Intent(_dataWrapper.context, PhoneProfilesService.class);
                        //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                        //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
                        //serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                        serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                        serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
                        PPApplication.startPPService(activity, serviceIntent, true);
                    }

                    if ((_dataWrapper != null) && (dbError == DatabaseHandler.IMPORT_OK) && (!(appSettingsError || sharedProfileError))) {
                        //PPApplication.logE("EditorProfilesActivity.doImportData", "restore is ok");

                        // restart events
                        //if (Event.getGlobalEventsRunning(this.dataWrapper.context)) {
                        //    this.dataWrapper.restartEventsWithDelay(3, false, false, DatabaseHandler.ALTYPE_UNDEFINED);
                        //}

                        PPApplication.addActivityLog(_dataWrapper.context, PPApplication.ALTYPE_DATA_IMPORT, null, null, null, 0, "");

                        // toast notification
                        if (!isFinishing())
                            PPApplication.showToast(_dataWrapper.context.getApplicationContext(),
                                    getResources().getString(R.string.toast_import_ok),
                                    Toast.LENGTH_SHORT);

                        // refresh activity
                        if (!isFinishing())
                            GlobalGUIRoutines.reloadActivity(activity, true);

                        DrawOverAppsPermissionNotification.showNotification(_dataWrapper.context, true);
                        //IgnoreBatteryOptimizationNotification.setShowIgnoreBatteryOptimizationNotificationOnStart(_dataWrapper.context, true);
                        IgnoreBatteryOptimizationNotification.showNotification(_dataWrapper.context, true);
                    } else {
                        //PPApplication.logE("EditorProfilesActivity.doImportData", "error restore");

                        int appSettingsResult = 1;
                        if (appSettingsError) appSettingsResult = 0;
                        int sharedProfileResult = 1;
                        if (sharedProfileError) sharedProfileResult = 0;
                        if (!isFinishing())
                            importExportErrorDialog(1, dbError, appSettingsResult, sharedProfileResult);
                    }
                }

            }

            importAsyncTask = new ImportAsyncTask().execute();
        }
    }

    private void importData()
    {
        AlertDialog.Builder dialogBuilder2 = new AlertDialog.Builder(this);
        dialogBuilder2.setTitle(R.string.import_profiles_alert_title);
        dialogBuilder2.setMessage(R.string.import_profiles_alert_message);

        dialogBuilder2.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (Permissions.grantImportPermissions(getApplicationContext(), EditorProfilesActivity.this/*, PPApplication.EXPORT_PATH*/))
                    doImportData(PPApplication.EXPORT_PATH);
            }
        });
        dialogBuilder2.setNegativeButton(R.string.alert_button_no, null);
        AlertDialog dialog = dialogBuilder2.create();

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        if (!isFinishing())
            dialog.show();
    }

    private void doImportDataFromPP(final boolean importProfiles,
                                    final boolean deleteConfiguredProfiles,
                                    final boolean importApplicationPreferences) {
        final EditorProfilesActivity activity = this;

        @SuppressLint("StaticFieldLeak")
        class ImportAsyncTask extends AsyncTask<Void, Integer, Integer> {
            private final DataWrapper _dataWrapper;
            private int dbError = DatabaseHandler.IMPORT_OK;
            private boolean appSettingsError = false;
            private ImportPPDataBroadcastReceiver importPPDataBroadcastReceiver = null;

            private ImportAsyncTask() {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                dialogBuilder.setMessage(R.string.import_profiles_from_pp_alert_title);

                LayoutInflater inflater = (activity.getLayoutInflater());
                @SuppressLint("InflateParams")
                View layout = inflater.inflate(R.layout.dialog_progress_bar, null);
                dialogBuilder.setView(layout);

                dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // send stop into PP
                                Intent intent = new Intent(PPApplication.ACTION_EXPORT_PP_DATA_STOP);
                                sendBroadcast(intent, PPApplication.EXPORT_PP_DATA_PERMISSION);
                                importFromPPStopped = true;
                            }
                        });

                importProgressDialog = dialogBuilder.create();

//                    importProgressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                        @Override
//                        public void onShow(DialogInterface dialog) {
//                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (positive != null) positive.setAllCaps(false);
//                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                            if (negative != null) negative.setAllCaps(false);
//                        }
//                    });

                _dataWrapper = getDataWrapper();
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                doImport = true;

                // TODO add all actions
                importPPDataBroadcastReceiver = new ImportPPDataBroadcastReceiver();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(PPApplication.ACTION_EXPORT_PP_DATA_START);
                registerReceiver(importPPDataBroadcastReceiver, intentFilter,
                        PPApplication.EXPORT_PP_DATA_PERMISSION, null);


                GlobalGUIRoutines.lockScreenOrientation(activity, false);
                importProgressDialog.setCancelable(false);
                importProgressDialog.setCanceledOnTouchOutside(false);

                if (!activity.isFinishing())
                    importProgressDialog.show();

                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                if (fragment != null) {
                    if (fragment instanceof EditorProfileListFragment)
                        ((EditorProfileListFragment) fragment).removeAdapter();
                    else
                        ((EditorEventListFragment) fragment).removeAdapter();
                }
            }

            @SuppressLint({"SetWorldReadable", "SetWorldWritable"})
            @Override
            protected Integer doInBackground(Void... params) {
                //PPApplication.logE("PPApplication.exitApp", "from EditorProfilesActivity.doImportData shutdown=false");
                if (_dataWrapper != null) {
                    PPApplication.exitApp(false, _dataWrapper.context, _dataWrapper, null, false/*, false, true*/);

                    // send start into PP
                    importFromPPStopped = false;
                    Intent intent = new Intent(PPApplication.ACTION_EXPORT_PP_DATA_START);
                    sendBroadcast(intent, PPApplication.EXPORT_PP_DATA_PERMISSION);

                    // get PP data
                    long start = SystemClock.uptimeMillis();
                    do {
                        if (importFromPPStopped)
                            break;

                        if (importPPDataBroadcastReceiver.importStarted &&
                            importPPDataBroadcastReceiver.importEndeed)
                            break;

                        PPApplication.sleep(500);
                    } while (SystemClock.uptimeMillis() - start < 10 * 1000); // TODO max length 10 seconds? Test it.

                    if (!importFromPPStopped) {
                        if (importPPDataBroadcastReceiver.importStarted &&
                            importPPDataBroadcastReceiver.importEndeed) {
                            // TODO save PP data into PPP

                            // import application preferences must be first,
                            // because in DatabaseHandler.importDB is recompute of volumes in profiles
                            if (importApplicationPreferences) {
                                synchronized (PPApplication.applicationPreferencesMutex) {
                                    ApplicationPreferences.applicationStartOnBoot = importPPDataBroadcastReceiver.applicationData.applicationStartOnBoot;
                                    ApplicationPreferences.applicationActivate = importPPDataBroadcastReceiver.applicationData.applicationActivate;
                                    ApplicationPreferences.applicationActivateWithAlert = importPPDataBroadcastReceiver.applicationData.applicationActivateWithAlert;
                                    ApplicationPreferences.applicationClose = importPPDataBroadcastReceiver.applicationData.applicationClose;
                                    ApplicationPreferences.applicationLongClickActivation = importPPDataBroadcastReceiver.applicationData.applicationLongClickActivation;
                                    ApplicationPreferences.applicationTheme = importPPDataBroadcastReceiver.applicationData.applicationTheme;
                                    ApplicationPreferences.applicationEditorPrefIndicator = importPPDataBroadcastReceiver.applicationData.applicationEditorPrefIndicator;
                                    ApplicationPreferences.notificationsToast = importPPDataBroadcastReceiver.applicationData.notificationsToast;
                                    ApplicationPreferences.notificationStatusBarStyle = importPPDataBroadcastReceiver.applicationData.notificationStatusBarStyle;
                                    ApplicationPreferences.notificationShowInStatusBar = importPPDataBroadcastReceiver.applicationData.notificationShowInStatusBar;
                                    ApplicationPreferences.notificationTextColor = importPPDataBroadcastReceiver.applicationData.notificationTextColor;
                                    ApplicationPreferences.notificationHideInLockScreen = importPPDataBroadcastReceiver.applicationData.notificationHideInLockscreen;
                                    ApplicationPreferences.applicationWidgetListPrefIndicator = importPPDataBroadcastReceiver.applicationData.applicationWidgetListPrefIndicator;
                                    ApplicationPreferences.applicationWidgetListHeader = importPPDataBroadcastReceiver.applicationData.applicationWidgetListHeader;
                                    ApplicationPreferences.applicationWidgetListBackground = importPPDataBroadcastReceiver.applicationData.applicationWidgetListBackground;
                                    ApplicationPreferences.applicationWidgetListLightnessB = importPPDataBroadcastReceiver.applicationData.applicationWidgetListLightnessB;
                                    ApplicationPreferences.applicationWidgetListLightnessT = importPPDataBroadcastReceiver.applicationData.applicationWidgetListLightnessT;
                                    ApplicationPreferences.applicationWidgetIconColor = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconColor;
                                    ApplicationPreferences.applicationWidgetIconLightness = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconLightness;
                                    ApplicationPreferences.applicationWidgetListIconColor = importPPDataBroadcastReceiver.applicationData.applicationWidgetListIconColor;
                                    ApplicationPreferences.applicationWidgetListIconLightness = importPPDataBroadcastReceiver.applicationData.applicationWidgetListIconLightness;
                                    ApplicationPreferences.notificationPrefIndicator = importPPDataBroadcastReceiver.applicationData.notificationPrefIndicator;
                                    ApplicationPreferences.applicationActivatorGridLayout = importPPDataBroadcastReceiver.applicationData.applicationActivatorGridLayout;
                                    ApplicationPreferences.applicationWidgetListGridLayout = importPPDataBroadcastReceiver.applicationData.applicationWidgetListGridLayout;
                                    ApplicationPreferences.applicationWidgetIconHideProfileName = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconHideProfileName;
                                    ApplicationPreferences.applicationShortcutEmblem = importPPDataBroadcastReceiver.applicationData.applicationShortcutEmblem;
                                    ApplicationPreferences.applicationWidgetIconBackground = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconBackground;
                                    ApplicationPreferences.applicationWidgetIconLightnessB = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconLightnessB;
                                    ApplicationPreferences.applicationWidgetIconLightnessT = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconLightnessT;
                                    ApplicationPreferences.applicationUnlinkRingerNotificationVolumes = importPPDataBroadcastReceiver.applicationData.applicationUnlinkRingerNotificationVolumes;
                                    ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes = importPPDataBroadcastReceiver.applicationData.applicationForceSetMergeRingNotificationVolumes;
                                    ApplicationPreferences.applicationSamsungEdgeHeader = importPPDataBroadcastReceiver.applicationData.applicationSamsungEdgeHeader;
                                    ApplicationPreferences.applicationSamsungEdgeBackground = importPPDataBroadcastReceiver.applicationData.applicationSamsungEdgeBackground;
                                    ApplicationPreferences.applicationSamsungEdgeLightnessB = importPPDataBroadcastReceiver.applicationData.applicationSamsungEdgeLightnessB;
                                    ApplicationPreferences.applicationSamsungEdgeLightnessT = importPPDataBroadcastReceiver.applicationData.applicationSamsungEdgeLightnessT;
                                    ApplicationPreferences.applicationSamsungEdgeIconColor = importPPDataBroadcastReceiver.applicationData.applicationSamsungEdgeIconColor;
                                    ApplicationPreferences.applicationSamsungEdgeIconLightness = importPPDataBroadcastReceiver.applicationData.applicationSamsungEdgeIconLightness;
                                    ApplicationPreferences.applicationWidgetListRoundedCorners = importPPDataBroadcastReceiver.applicationData.applicationWidgetListRoundedCorners;
                                    ApplicationPreferences.applicationWidgetIconRoundedCorners = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconRoundedCorners;
                                    ApplicationPreferences.applicationWidgetListBackgroundType = importPPDataBroadcastReceiver.applicationData.applicationWidgetListBackgroundType;
                                    ApplicationPreferences.applicationWidgetListBackgroundColor = importPPDataBroadcastReceiver.applicationData.applicationWidgetListBackgroundColor;
                                    ApplicationPreferences.applicationWidgetIconBackgroundType = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconBackgroundType;
                                    ApplicationPreferences.applicationWidgetIconBackgroundColor = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconBackgroundColor;
                                    ApplicationPreferences.applicationSamsungEdgeBackgroundType = importPPDataBroadcastReceiver.applicationData.applicationSamsungEdgeBackgroundType;
                                    ApplicationPreferences.applicationSamsungEdgeBackgroundColor = importPPDataBroadcastReceiver.applicationData.applicationSamsungEdgeBackgroundColor;
                                    ApplicationPreferences.applicationNeverAskForGrantRoot = importPPDataBroadcastReceiver.applicationData.applicationNeverAskForGrantRoot;
                                    ApplicationPreferences.notificationShowButtonExit = importPPDataBroadcastReceiver.applicationData.notificationShowButtonExit;
                                    ApplicationPreferences.applicationWidgetOneRowPrefIndicator = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowPrefIndicator;
                                    ApplicationPreferences.applicationWidgetOneRowBackground = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowBackground;
                                    ApplicationPreferences.applicationWidgetOneRowLightnessB = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowLightnessB;
                                    ApplicationPreferences.applicationWidgetOneRowLightnessT = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowLightnessT;
                                    ApplicationPreferences.applicationWidgetOneRowIconColor = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowIconColor;
                                    ApplicationPreferences.applicationWidgetOneRowIconLightness = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowIconLightness;
                                    ApplicationPreferences.applicationWidgetOneRowRoundedCorners = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowRoundedCorners;
                                    ApplicationPreferences.applicationWidgetOneRowBackgroundType = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowBackgroundType;
                                    ApplicationPreferences.applicationWidgetOneRowBackgroundColor = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowBackgroundColor;
                                    ApplicationPreferences.applicationWidgetListLightnessBorder = importPPDataBroadcastReceiver.applicationData.applicationWidgetListLightnessBorder;
                                    ApplicationPreferences.applicationWidgetOneRowLightnessBorder = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowLightnessBorder;
                                    ApplicationPreferences.applicationWidgetIconLightnessBorder = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconLightnessBorder;
                                    ApplicationPreferences.applicationWidgetListShowBorder = importPPDataBroadcastReceiver.applicationData.applicationWidgetListShowBorder;
                                    ApplicationPreferences.applicationWidgetOneRowShowBorder = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowShowBorder;
                                    ApplicationPreferences.applicationWidgetIconShowBorder = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconShowBorder;
                                    ApplicationPreferences.applicationWidgetListCustomIconLightness = importPPDataBroadcastReceiver.applicationData.applicationWidgetListCustomIconLightness;
                                    ApplicationPreferences.applicationWidgetOneRowCustomIconLightness = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowCustomIconLightness;
                                    ApplicationPreferences.applicationWidgetIconCustomIconLightness = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconCustomIconLightness;
                                    ApplicationPreferences.applicationSamsungEdgeCustomIconLightness = importPPDataBroadcastReceiver.applicationData.applicationSamsungEdgeCustomIconLightness;
                                    ApplicationPreferences.notificationUseDecoration = importPPDataBroadcastReceiver.applicationData.notificationUseDecoration;
                                    ApplicationPreferences.notificationLayoutType = importPPDataBroadcastReceiver.applicationData.notificationLayoutType;
                                    ApplicationPreferences.notificationBackgroundColor = importPPDataBroadcastReceiver.applicationData.notificationBackgroundColor;
                                    ApplicationPreferences.notificationNotificationStyle = "0"; // custom notification style
                                    ApplicationPreferences.notificationNightMode = false;
                                    ApplicationPreferences.notificationShowProfileIcon = true;
                                    if (ApplicationPreferences.notificationBackgroundColor.equals("2")) {
                                        ApplicationPreferences.notificationBackgroundColor = "0";
                                        ApplicationPreferences.notificationNightMode = true;
                                    }

                                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_START_ON_BOOT, ApplicationPreferences.applicationStartOnBoot);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATE, ApplicationPreferences.applicationActivate);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ALERT, ApplicationPreferences.applicationActivateWithAlert);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_CLOSE, ApplicationPreferences.applicationClose);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_LONG_PRESS_ACTIVATION, ApplicationPreferences.applicationLongClickActivation);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, ApplicationPreferences.applicationTheme);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR, ApplicationPreferences.applicationEditorPrefIndicator);
                                    editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_TOAST, ApplicationPreferences.notificationsToast);
                                    editor.putString(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_STYLE, ApplicationPreferences.notificationStatusBarStyle);
                                    editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, ApplicationPreferences.notificationShowInStatusBar);
                                    editor.putString(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR, ApplicationPreferences.notificationTextColor);
                                    editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN, ApplicationPreferences.notificationHideInLockScreen);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, ApplicationPreferences.applicationWidgetListPrefIndicator);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_HEADER, ApplicationPreferences.applicationWidgetListHeader);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND, ApplicationPreferences.applicationWidgetListBackground);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, ApplicationPreferences.applicationWidgetListLightnessB);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, ApplicationPreferences.applicationWidgetListLightnessT);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR, ApplicationPreferences.applicationWidgetIconColor);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, ApplicationPreferences.applicationWidgetIconLightness);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, ApplicationPreferences.applicationWidgetListIconColor);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, ApplicationPreferences.applicationWidgetListIconLightness);
                                    editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR, ApplicationPreferences.notificationPrefIndicator);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, ApplicationPreferences.applicationActivatorGridLayout);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, ApplicationPreferences.applicationWidgetListGridLayout);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, ApplicationPreferences.applicationWidgetIconHideProfileName);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_EMBLEM, ApplicationPreferences.applicationShortcutEmblem);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND, ApplicationPreferences.applicationWidgetIconBackground);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B, ApplicationPreferences.applicationWidgetIconLightnessB);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, ApplicationPreferences.applicationWidgetIconLightnessT);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES, ApplicationPreferences.applicationUnlinkRingerNotificationVolumes);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES, String.valueOf(ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes));
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_HEADER, ApplicationPreferences.applicationSamsungEdgeHeader);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND, ApplicationPreferences.applicationSamsungEdgeBackground);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B, ApplicationPreferences.applicationSamsungEdgeLightnessB);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T, ApplicationPreferences.applicationSamsungEdgeLightnessT);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR, ApplicationPreferences.applicationSamsungEdgeIconColor);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS, ApplicationPreferences.applicationSamsungEdgeIconLightness);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS, ApplicationPreferences.applicationWidgetListRoundedCorners);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS, ApplicationPreferences.applicationWidgetIconRoundedCorners);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, ApplicationPreferences.applicationWidgetListBackgroundType);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR, ApplicationPreferences.applicationWidgetListBackgroundColor);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, ApplicationPreferences.applicationWidgetIconBackgroundType);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR, ApplicationPreferences.applicationWidgetIconBackgroundColor);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, ApplicationPreferences.applicationSamsungEdgeBackgroundType);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR, ApplicationPreferences.applicationSamsungEdgeBackgroundColor);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT, ApplicationPreferences.applicationNeverAskForGrantRoot);
                                    editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT, ApplicationPreferences.notificationShowButtonExit);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR, ApplicationPreferences.applicationWidgetOneRowPrefIndicator);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND, ApplicationPreferences.applicationWidgetOneRowBackground);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B, ApplicationPreferences.applicationWidgetOneRowLightnessB);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, ApplicationPreferences.applicationWidgetOneRowLightnessT);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR, ApplicationPreferences.applicationWidgetOneRowIconColor);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS, ApplicationPreferences.applicationWidgetOneRowIconLightness);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, ApplicationPreferences.applicationWidgetOneRowRoundedCorners);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, ApplicationPreferences.applicationWidgetOneRowBackgroundType);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR, ApplicationPreferences.applicationWidgetOneRowBackgroundColor);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER, ApplicationPreferences.applicationWidgetListLightnessBorder);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER, ApplicationPreferences.applicationWidgetOneRowLightnessBorder);
                                    editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER, ApplicationPreferences.applicationWidgetIconLightnessBorder);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER, ApplicationPreferences.applicationWidgetListShowBorder);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER, ApplicationPreferences.applicationWidgetOneRowShowBorder);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER, ApplicationPreferences.applicationWidgetIconShowBorder);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.applicationWidgetListCustomIconLightness);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.applicationWidgetOneRowCustomIconLightness);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.applicationWidgetIconCustomIconLightness);
                                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.applicationSamsungEdgeCustomIconLightness);
                                    editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, ApplicationPreferences.notificationUseDecoration);
                                    editor.putString(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE, ApplicationPreferences.notificationLayoutType);
                                    editor.putString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, ApplicationPreferences.notificationBackgroundColor);

                                    editor.apply();
                                }
                            }

                            if (deleteConfiguredProfiles) {
                                DatabaseHandler.getInstance(_dataWrapper.context).deleteAllProfiles();
                            }
                            if (importProfiles) {
                                List<Long> ppShortcutIds = new ArrayList<>();
                                List<Long> importedShortcutIds = new ArrayList<>();
                                List<Long> ppIntentIds = new ArrayList<>();
                                List<Long> importedIntentIds = new ArrayList<>();


                                // first import shortcuts and intents
                                for (PPShortcutForImport shortcutForImport : importPPDataBroadcastReceiver.shortcuts) {
                                    Shortcut shortcut = new Shortcut();
                                    shortcut._intent = shortcutForImport.KEY_S_INTENT;
                                    shortcut._name = shortcutForImport.KEY_S_NAME;

                                    DatabaseHandler.getInstance(_dataWrapper.context).addShortcut(shortcut);
                                    // save shortcut id
                                    ppShortcutIds.add(shortcutForImport.KEY_S_ID);
                                    importedShortcutIds.add(shortcut._id);
                                }
                                for (PPIntentForImport intentForImport : importPPDataBroadcastReceiver.intents) {
                                    PPIntent ppIntent = new PPIntent(
                                            intentForImport.KEY_IN_ID,
                                            intentForImport.KEY_IN_NAME,
                                            intentForImport.KEY_IN_PACKAGE_NAME,
                                            intentForImport.KEY_IN_CLASS_NAME,
                                            intentForImport.KEY_IN_ACTION,
                                            intentForImport.KEY_IN_DATA,
                                            intentForImport.KEY_IN_MIME_TYPE,
                                            intentForImport.KEY_IN_EXTRA_KEY_1,
                                            intentForImport.KEY_IN_EXTRA_VALUE_1,
                                            intentForImport.KEY_IN_EXTRA_TYPE_1,
                                            intentForImport.KEY_IN_EXTRA_KEY_2,
                                            intentForImport.KEY_IN_EXTRA_VALUE_2,
                                            intentForImport.KEY_IN_EXTRA_TYPE_2,
                                            intentForImport.KEY_IN_EXTRA_KEY_3,
                                            intentForImport.KEY_IN_EXTRA_VALUE_3,
                                            intentForImport.KEY_IN_EXTRA_TYPE_3,
                                            intentForImport.KEY_IN_EXTRA_KEY_4,
                                            intentForImport.KEY_IN_EXTRA_VALUE_4,
                                            intentForImport.KEY_IN_EXTRA_TYPE_4,
                                            intentForImport.KEY_IN_EXTRA_KEY_5,
                                            intentForImport.KEY_IN_EXTRA_VALUE_5,
                                            intentForImport.KEY_IN_EXTRA_TYPE_5,
                                            intentForImport.KEY_IN_EXTRA_KEY_6,
                                            intentForImport.KEY_IN_EXTRA_VALUE_6,
                                            intentForImport.KEY_IN_EXTRA_TYPE_6,
                                            intentForImport.KEY_IN_EXTRA_KEY_7,
                                            intentForImport.KEY_IN_EXTRA_VALUE_7,
                                            intentForImport.KEY_IN_EXTRA_TYPE_7,
                                            intentForImport.KEY_IN_EXTRA_KEY_8,
                                            intentForImport.KEY_IN_EXTRA_VALUE_8,
                                            intentForImport.KEY_IN_EXTRA_TYPE_8,
                                            intentForImport.KEY_IN_EXTRA_KEY_9,
                                            intentForImport.KEY_IN_EXTRA_VALUE_9,
                                            intentForImport.KEY_IN_EXTRA_TYPE_9,
                                            intentForImport.KEY_IN_EXTRA_KEY_10,
                                            intentForImport.KEY_IN_EXTRA_VALUE_10,
                                            intentForImport.KEY_IN_EXTRA_TYPE_10,
                                            intentForImport.KEY_IN_CATEGORIES,
                                            intentForImport.KEY_IN_FLAGS,
                                            intentForImport.KEY_IN_USED_COUNT,
                                            intentForImport.KEY_IN_INTENT_TYPE
                                    );

                                    DatabaseHandler.getInstance(_dataWrapper.context).addIntent(ppIntent);
                                    // save intent id
                                    ppIntentIds.add(intentForImport.KEY_IN_ID);
                                    importedIntentIds.add(ppIntent._id);
                                }


                                for (PPProfileForImport profileForImport : importPPDataBroadcastReceiver.profiles) {
                                    Profile profile = new Profile (
                                            profileForImport.KEY_NAME,
                                            profileForImport.KEY_ICON,
                                            profileForImport.KEY_CHECKED,
                                            profileForImport.KEY_PORDER,
                                            profileForImport.KEY_VOLUME_RINGER_MODE,
                                            profileForImport.KEY_VOLUME_RINGTONE,
                                            profileForImport.KEY_VOLUME_NOTIFICATION,
                                            profileForImport.KEY_VOLUME_MEDIA,
                                            profileForImport.KEY_VOLUME_ALARM,
                                            profileForImport.KEY_VOLUME_SYSTEM,
                                            profileForImport.KEY_VOLUME_VOICE,
                                            profileForImport.KEY_SOUND_RINGTONE_CHANGE,
                                            profileForImport.KEY_SOUND_RINGTONE,
                                            profileForImport.KEY_SOUND_NOTIFICATION_CHANGE,
                                            profileForImport.KEY_SOUND_NOTIFICATION,
                                            profileForImport.KEY_SOUND_ALARM_CHANGE,
                                            profileForImport.KEY_SOUND_ALARM,
                                            profileForImport.KEY_DEVICE_AIRPLANE_MODE,
                                            profileForImport.KEY_DEVICE_WIFI,
                                            profileForImport.KEY_DEVICE_BLUETOOTH,
                                            profileForImport.KEY_DEVICE_SCREEN_TIMEOUT,
                                            profileForImport.KEY_DEVICE_BRIGHTNESS,
                                            profileForImport.KEY_DEVICE_WALLPAPER_CHANGE,
                                            profileForImport.KEY_DEVICE_WALLPAPER,
                                            profileForImport.KEY_DEVICE_MOBILE_DATA,
                                            profileForImport.KEY_DEVICE_MOBILE_DATA_PREFS,
                                            profileForImport.KEY_DEVICE_GPS,
                                            profileForImport.KEY_DEVICE_RUN_APPLICATION_CHANGE,
                                            profileForImport.KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME,
                                            profileForImport.KEY_DEVICE_AUTOSYNC,
                                            false,
                                            profileForImport.KEY_DEVICE_AUTOROTATE,
                                            profileForImport.KEY_DEVICE_LOCATION_SERVICE_PREFS,
                                            profileForImport.KEY_VOLUME_SPEAKER_PHONE,
                                            profileForImport.KEY_DEVICE_NFC,
                                            profileForImport.KEY_DURATION,
                                            profileForImport.KEY_AFTER_DURATION_DO,
                                            profileForImport.KEY_VOLUME_ZEN_MODE,
                                            profileForImport.KEY_DEVICE_KEYGUARD,
                                            profileForImport.KEY_VIBRATE_ON_TOUCH,
                                            profileForImport.KEY_DEVICE_WIFI_AP,
                                            profileForImport.KEY_DEVICE_POWER_SAVE_MODE,
                                            profileForImport.KEY_ASK_FOR_DURATION,
                                            profileForImport.KEY_DEVICE_NETWORK_TYPE,
                                            profileForImport.KEY_NOTIFICATION_LED,
                                            profileForImport.KEY_VIBRATE_WHEN_RINGING,
                                            profileForImport.KEY_DEVICE_WALLPAPER_FOR,
                                            profileForImport.KEY_HIDE_STATUS_BAR_ICON,
                                            profileForImport.KEY_LOCK_DEVICE,
                                            profileForImport.KEY_DEVICE_CONNECT_TO_SSID,
                                            0,
                                            0,
                                            profileForImport.KEY_DURATION_NOTIFICATION_SOUND,
                                            profileForImport.KEY_DURATION_NOTIFICATION_VIBRATE,
                                            profileForImport.KEY_DEVICE_WIFI_AP_PREFS,
                                            0,
                                            0,
                                            0,
                                            profileForImport.KEY_HEADS_UP_NOTIFICATIONS,
                                            profileForImport.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE,
                                            profileForImport.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME,
                                            profileForImport.KEY_ACTIVATION_BY_USER_COUNT,
                                            profileForImport.KEY_DEVICE_NETWORK_TYPE_PREFS,
                                            profileForImport.KEY_DEVICE_CLOSE_ALL_APPLICATIONS,
                                            profileForImport.KEY_SCREEN_NIGHT_MODE,
                                            profileForImport.KEY_DTMF_TONE_WHEN_DIALING,
                                            profileForImport.KEY_SOUND_ON_TOUCH,
                                            profileForImport.KEY_VOLUME_DTMF,
                                            profileForImport.KEY_VOLUME_ACCESSIBILITY,
                                            profileForImport.KEY_VOLUME_BLUETOOTH_SCO,
                                            0,
                                            0,
                                            0,
                                            false,
                                            0
                                    );

                                    // replace ids in profile._deviceRunApplicationPackageName
                                    // with new shortcut and intent ids

                                    DatabaseHandler.getInstance(_dataWrapper.context).addProfile(profile, false);
                                    // TODO id of profile from db is returned in profile._id
                                }
                            }
                        }
                    }

                    if (importFromPPStopped)
                        return 2;

                    if ((dbError == DatabaseHandler.IMPORT_OK) && (!(appSettingsError)))
                        return 1;
                    else
                        return 0;
                }
                else
                    return 0;
            }

            @Override
            protected void onPostExecute(Integer result) {
                super.onPostExecute(result);

                doImport = false;

                if (!isFinishing()) {
                    if ((importProgressDialog != null) && importProgressDialog.isShowing()) {
                        if (!isDestroyed())
                            importProgressDialog.dismiss();
                        importProgressDialog = null;
                    }
                    GlobalGUIRoutines.unlockScreenOrientation(activity);
                }

                if (importPPDataBroadcastReceiver != null) {
                    try {
                        unregisterReceiver(importPPDataBroadcastReceiver);
                    } catch (Exception ignored) {
                    }
                }

                if (!importFromPPStopped) {
                    if (_dataWrapper != null) {
                        //PPApplication.logE("DataWrapper.updateNotificationAndWidgets", "from EditorProfilesActivity.doImportData");

                        // clear shared preferences for last activated profile
                        Profile profile = DataWrapper.getNonInitializedProfile("", null, 0);
                        Profile.saveProfileToSharedPreferences(profile, _dataWrapper.context, PPApplication.ACTIVATED_PROFILE_PREFS_NAME);
                        PPApplication.setLastActivatedProfile(_dataWrapper.context, 0);

                        //PPApplication.updateNotificationAndWidgets(true, true, _dataWrapper.context);
                        //PPApplication.logE("###### PPApplication.updateGUI", "from=EditorProfilesActivity.doImportData");
                        PPApplication.updateGUI(true);

                        PPApplication.setApplicationStarted(_dataWrapper.context, true);
                        Intent serviceIntent = new Intent(_dataWrapper.context, PhoneProfilesService.class);
                        //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                        //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
                        //serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                        serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                        serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
                        PPApplication.startPPService(activity, serviceIntent, true);
                    }

                    if ((_dataWrapper != null) && (dbError == DatabaseHandler.IMPORT_OK) && (!appSettingsError)) {
                        //PPApplication.logE("EditorProfilesActivity.doImportData", "restore is ok");

                        // restart events
                        //if (Event.getGlobalEventsRunning(this.dataWrapper.context)) {
                        //    this.dataWrapper.restartEventsWithDelay(3, false, false, DatabaseHandler.ALTYPE_UNDEFINED);
                        //}

                        PPApplication.addActivityLog(_dataWrapper.context, PPApplication.ALTYPE_DATA_IMPORT_FROM_PP, null, null, null, 0, "");

                        // toast notification
                        if (!isFinishing())
                            PPApplication.showToast(_dataWrapper.context.getApplicationContext(),
                                    getResources().getString(R.string.toast_import_from_pp_ok),
                                    Toast.LENGTH_SHORT);

                        // refresh activity
                        if (!isFinishing())
                            GlobalGUIRoutines.reloadActivity(activity, true);

                        DrawOverAppsPermissionNotification.showNotification(_dataWrapper.context, true);
                        //IgnoreBatteryOptimizationNotification.setShowIgnoreBatteryOptimizationNotificationOnStart(_dataWrapper.context, true);
                        IgnoreBatteryOptimizationNotification.showNotification(_dataWrapper.context, true);
                    } else {
                        //PPApplication.logE("EditorProfilesActivity.doImportData", "error restore");

                        int appSettingsResult = 1;
                        if (appSettingsError) appSettingsResult = 0;
                        if (!isFinishing())
                            importExportErrorDialog(1, dbError, appSettingsResult, 1);
                    }
                }
            }
        }

        importFromPPAsyncTask = new ImportAsyncTask().execute();
    }

    private void importDataFromPP() {
        AlertDialog.Builder dialogBuilder2 = new AlertDialog.Builder(this);
        dialogBuilder2.setTitle(R.string.import_profiles_from_pp_alert_title);

        LayoutInflater inflater = (getLayoutInflater());
        @SuppressLint("InflateParams")
        final View layout = inflater.inflate(R.layout.dialog_import_pp_data_alert, null);
        dialogBuilder2.setView(layout);

        dialogBuilder2.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean importProfiles = true;
                boolean deleteConfiguredProfiles = false;
                boolean importApplicationPreferences = false;
                CheckBox checkbox = layout.findViewById(R.id.exportPPDataDataProfiles);
                if (checkbox != null) importProfiles = checkbox.isChecked();
                checkbox = layout.findViewById(R.id.exportPPDataDataDeleteConfiguredProfiles);
                if (checkbox != null) deleteConfiguredProfiles = checkbox.isChecked();
                checkbox = layout.findViewById(R.id.exportPPDataDataApplicationPreferences);
                if (checkbox != null) importApplicationPreferences = checkbox.isChecked();
                doImportDataFromPP(importProfiles, deleteConfiguredProfiles, importApplicationPreferences);
            }
        });
        dialogBuilder2.setNegativeButton(R.string.alert_button_no, null);

        AlertDialog dialog = dialogBuilder2.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                CheckBox checkbox = layout.findViewById(R.id.exportPPDataDataProfiles);
                if (checkbox != null) checkbox.setChecked(true);
                checkbox = layout.findViewById(R.id.exportPPDataDataDeleteConfiguredProfiles);
                if (checkbox != null) checkbox.setChecked(false);
                checkbox = layout.findViewById(R.id.exportPPDataDataApplicationPreferences);
                if (checkbox != null) checkbox.setChecked(false);
            }
        });

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        if (!isFinishing())
            dialog.show();
    }

    @SuppressLint({"ApplySharedPref", "SetWorldReadable", "SetWorldWritable"})
    private boolean exportApplicationPreferences(File dst, boolean runStopEvents/*, int what*/) {
        boolean res = true;
        ObjectOutputStream output = null;
        try {
            try {
                output = new ObjectOutputStream(new FileOutputStream(dst));
                SharedPreferences pref;
                //if (what == 1)
                    pref = getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                //else
                //    pref = getSharedPreferences(PPApplication.SHARED_PROFILE_PREFS_NAME, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                editor.putBoolean(Event.PREF_GLOBAL_EVENTS_RUN_STOP, runStopEvents);

                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    try {
                        editor.putInt("maximumVolume_ring", audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt("maximumVolume_notification", audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt("maximumVolume_music", audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt("maximumVolume_alarm", audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt("maximumVolume_system", audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt("maximumVolume_voiceCall", audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt("maximumVolume_dtmf", audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF));
                    } catch (Exception ignored) {}
                    if (Build.VERSION.SDK_INT >= 26) {
                        try {
                            editor.putInt("maximumVolume_accessibility", audioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY));
                        } catch (Exception ignored) {}
                    }
                    try {
                        editor.putInt("maximumVolume_bluetoothSCO", audioManager.getStreamMaxVolume(ActivateProfileHelper.STREAM_BLUETOOTH_SCO));
                    } catch (Exception ignored) {}
                }

                editor.commit();
                output.writeObject(pref.getAll());

                output.flush();

                try {
                    //noinspection ResultOfMethodCallIgnored
                    dst.setReadable(true, false);
                } catch (Exception ee) {
                    PPApplication.recordException(ee);
                }
                try {
                    //noinspection ResultOfMethodCallIgnored
                    dst.setWritable(true, false);
                } catch (Exception ee) {
                    PPApplication.recordException(ee);
                }

            } catch (FileNotFoundException e) {
                PPApplication.recordException(e);
                // this is OK
            } catch (IOException e) {
                PPApplication.recordException(e);
                res = false;
            }
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException e) {
                PPApplication.recordException(e);
            }
        }
        return res;
    }

    private void exportData(final boolean email, final boolean toAuthor)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.export_profiles_alert_title);
        //File sd = Environment.getExternalStorageDirectory();
        //File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        dialogBuilder.setMessage(getString(R.string.export_profiles_alert_message) + " \"" /*+ "/" + Environment.DIRECTORY_DOCUMENTS*/ + PPApplication.EXPORT_PATH + "\".\n\n" +
                                 getString(R.string.export_profiles_alert_message_note));
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

        dialogBuilder.setPositiveButton(R.string.alert_button_backup, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                if (Permissions.grantExportPermissions(getApplicationContext(), EditorProfilesActivity.this, email, toAuthor))
                    doExportData(email, toAuthor);
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = dialogBuilder.create();

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        if (!isFinishing())
            dialog.show();
    }

    private void doExportData(final boolean email, final boolean toAuthor)
    {
        final EditorProfilesActivity activity = this;

        if (Permissions.checkExport(getApplicationContext())) {

            @SuppressLint("StaticFieldLeak")
            class ExportAsyncTask extends AsyncTask<Void, Integer, Integer> {
                private final DataWrapper dataWrapper;
                private boolean runStopEvents;

                private ExportAsyncTask() {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                    dialogBuilder.setMessage(R.string.export_profiles_alert_title);

                    LayoutInflater inflater = (activity.getLayoutInflater());
                    @SuppressLint("InflateParams")
                    View layout = inflater.inflate(R.layout.dialog_progress_bar, null);
                    dialogBuilder.setView(layout);

                    exportProgressDialog = dialogBuilder.create();

//                    exportProgressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                        @Override
//                        public void onShow(DialogInterface dialog) {
//                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (positive != null) positive.setAllCaps(false);
//                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                            if (negative != null) negative.setAllCaps(false);
//                        }
//                    });

                    this.dataWrapper = getDataWrapper();
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    GlobalGUIRoutines.lockScreenOrientation(activity, false);
                    exportProgressDialog.setCancelable(false);
                    exportProgressDialog.setCanceledOnTouchOutside(false);
                    if (!activity.isFinishing())
                        exportProgressDialog.show();

                    runStopEvents = Event.getGlobalEventsRunning();
                    dataWrapper.globalRunStopEvents(true);
                }

                @SuppressLint({"SetWorldReadable", "SetWorldWritable"})
                @Override
                protected Integer doInBackground(Void... params) {

                    if (this.dataWrapper != null) {
                        File sd = Environment.getExternalStorageDirectory();
                        //File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                        File exportDir = new File(sd, PPApplication.EXPORT_PATH);
                        if (!(exportDir.exists() && exportDir.isDirectory())) {
                            //noinspection ResultOfMethodCallIgnored
                            exportDir.mkdirs();
                            try {
                                //noinspection ResultOfMethodCallIgnored
                                exportDir.setReadable(true, false);
                            } catch (Exception ee) {
                                PPApplication.recordException(ee);
                            }
                            try {
                                //noinspection ResultOfMethodCallIgnored
                                exportDir.setWritable(true, false);
                            } catch (Exception ee) {
                                PPApplication.recordException(ee);
                            }
                        }

                        int ret = DatabaseHandler.getInstance(this.dataWrapper.context).exportDB();
                        if (ret == 1) {
                            File exportFile = new File(sd, PPApplication.EXPORT_PATH + "/" + GlobalGUIRoutines.EXPORT_APP_PREF_FILENAME);
                            if (exportApplicationPreferences(exportFile, runStopEvents/*, 1*/)) {
                            /*exportFile = new File(sd, PPApplication.EXPORT_PATH + "/" + GlobalGUIRoutines.EXPORT_DEF_PROFILE_PREF_FILENAME);
                            if (!exportApplicationPreferences(exportFile, 2))
                                ret = 0;*/
                                ret = 1;
                            } else
                                ret = 0;
                        }

                        Event.setGlobalEventsRunning(this.dataWrapper.context, runStopEvents);

                        return ret;
                    }
                    else
                        return 0;
                }

                @Override
                protected void onPostExecute(Integer result) {
                    super.onPostExecute(result);

                    if (!isFinishing()) {
                        if ((exportProgressDialog != null) && exportProgressDialog.isShowing()) {
                            if (!isDestroyed())
                                exportProgressDialog.dismiss();
                            exportProgressDialog = null;
                        }
                        GlobalGUIRoutines.unlockScreenOrientation(activity);
                    }

                    if ((dataWrapper != null) && (result == 1)) {

                        Context context = this.dataWrapper.context.getApplicationContext();
                        // toast notification
                        if (!isFinishing())
                            PPApplication.showToast(context, getString(R.string.toast_export_ok), Toast.LENGTH_SHORT);

                        dataWrapper.restartEventsWithRescan(false, false, true, false, false, false);

                        if (email) {
                            // email backup

                            ArrayList<Uri> uris = new ArrayList<>();

                            try {
                                File sd = Environment.getExternalStorageDirectory();
                                //File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

                                File exportedDB = new File(sd, PPApplication.EXPORT_PATH + "/" + DatabaseHandler.EXPORT_DBFILENAME);
                                Uri fileUri = FileProvider.getUriForFile(activity, PPApplication.PACKAGE_NAME + ".provider", exportedDB);
                                uris.add(fileUri);

                                File appSettingsFile = new File(sd, PPApplication.EXPORT_PATH + "/" + GlobalGUIRoutines.EXPORT_APP_PREF_FILENAME);
                                fileUri = FileProvider.getUriForFile(activity, PPApplication.PACKAGE_NAME + ".provider", appSettingsFile);
                                uris.add(fileUri);
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }

                            String emailAddress = "";
                            if (toAuthor)
                                emailAddress = "henrich.gron@gmail.com";
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                    "mailto", emailAddress, null));

                            String packageVersion = "";
                            try {
                                PackageInfo pInfo = context.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                                packageVersion = " - v" + pInfo.versionName + " (" + PPApplication.getVersionCode(pInfo) + ")";
                            } catch (Exception e) {
                                //Log.e("EditorProfilesActivity.doExportData", Log.getStackTraceString(e));
                                PPApplication.recordException(e);
                            }
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "PhoneProfilesPlus" + packageVersion + " - " + getString(R.string.menu_export));
                            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            List<ResolveInfo> resolveInfo = getPackageManager().queryIntentActivities(emailIntent, 0);
                            List<LabeledIntent> intents = new ArrayList<>();
                            for (ResolveInfo info : resolveInfo) {
                                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                                intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                                if (!emailAddress.isEmpty())
                                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                                intent.putExtra(Intent.EXTRA_SUBJECT, "PhoneProfilesPlus" + packageVersion + " - " + getString(R.string.menu_export));
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris); //ArrayList<Uri> of attachment Uri's
                                intents.add(new LabeledIntent(intent, info.activityInfo.packageName, info.loadLabel(getPackageManager()), info.icon));
                            }
                            if (intents.size() > 0) {
                                try {
                                    Intent chooser = Intent.createChooser(intents.remove(intents.size() - 1), context.getString(R.string.email_chooser));
                                    //noinspection ToArrayCallWithZeroLengthArrayArgument
                                    chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new LabeledIntent[intents.size()]));
                                    startActivity(chooser);
                                } catch (Exception e) {
                                    //Log.e("EditorProfilesActivity.doExportData", Log.getStackTraceString(e));
                                    PPApplication.recordException(e);
                                }
                            }
                        }

                    } else {
                        if (!isFinishing())
                            importExportErrorDialog(2, 0, 0, 0);
                    }
                }

            }

            exportAsyncTask = new ExportAsyncTask().execute();
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        //drawerToggle.syncState();
    }
 
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        savedInstanceStateChanged = true;
    }

     @Override
     public void setTitle(CharSequence title) {
         if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);
     }

     /*
     public void setIcon(int iconRes) {
         getSupportActionBar().setIcon(iconRes);
     }
     */

     /*
     @SuppressLint("SetTextI18n")
     private void setStatusBarTitle()
     {
        // set filter status bar title
        String text = drawerItemsSubtitle[drawerSelectedItem-1];
        //filterStatusBarTitle.setText(drawerItemsTitle[drawerSelectedItem - 1] + " - " + text);
        drawerHeaderFilterSubtitle.setText(text);
     }
     */

    private void startProfilePreferenceActivity(Profile profile, int editMode, int predefinedProfileIndex) {
        /*
        if (profile != null)
            PPApplication.logE("EditorProfilesActivity.startProfilePreferenceActivity", "profile._name="+profile._name);
        PPApplication.logE("EditorProfilesActivity.startProfilePreferenceActivity", "editMode="+editMode);
        PPApplication.logE("EditorProfilesActivity.startProfilePreferenceActivity", "predefinedProfileIndex="+predefinedProfileIndex);
        */

        Intent intent = new Intent(getBaseContext(), ProfilesPrefsActivity.class);
        if ((profile == null) || (editMode == EditorProfileListFragment.EDIT_MODE_INSERT))
            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, 0L);
        else
            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
        intent.putExtra(EXTRA_NEW_PROFILE_MODE, editMode);
        intent.putExtra(EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
        startActivityForResult(intent, REQUEST_CODE_PROFILE_PREFERENCES);
        //PPApplication.logE("EditorProfilesActivity.startProfilePreferenceActivity", "call of ProfilesPrefsActivity");
    }

    public void onStartProfilePreferences(Profile profile, int editMode, int predefinedProfileIndex/*, boolean startTargetHelps*/) {
        // In single-pane mode, simply start the profile preferences activity
        // for the profile position.
        if (((profile != null) ||
            (editMode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
            (editMode == EditorProfileListFragment.EDIT_MODE_DUPLICATE))
            && (editMode != EditorProfileListFragment.EDIT_MODE_DELETE))
            startProfilePreferenceActivity(profile, editMode, predefinedProfileIndex);
    }

    void redrawProfileListFragment(final Profile profile, int newProfileMode /*int predefinedProfileIndex, boolean startTargetHelps*/) {
        // redraw list fragment, notification a widgets

        Fragment _fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
        if (_fragment instanceof EditorProfileListFragment) {
            final EditorProfileListFragment fragment = (EditorProfileListFragment) getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
            if (fragment != null) {
                // update profile, this rewrite profile in profileList
                fragment.activityDataWrapper.updateProfile(profile);

                boolean newProfile = ((newProfileMode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
                        (newProfileMode == EditorProfileListFragment.EDIT_MODE_DUPLICATE));
                fragment.updateListView(profile, newProfile, false, false/*, 0*/);

                Profile activeProfile = fragment.activityDataWrapper.getActivatedProfile(true,
                        ApplicationPreferences.applicationEditorPrefIndicator);
                fragment.updateHeader(activeProfile);
                //PPApplication.showProfileNotification(/*getApplicationContext()*/true, false);
                //PPApplication.logE("ActivateProfileHelper.updateGUI", "from EditorProfilesActivity.redrawProfileListFragment");
                //PPApplication.logE("###### PPApplication.updateGUI", "from=EditorProfilesActivity.redrawProfileListFragment");
                PPApplication.updateGUI(true/*fragment.activityDataWrapper.context, true, true*/);

                fragment.activityDataWrapper.setDynamicLauncherShortcutsFromMainThread();

                if (filterProfilesSelectedItem != 0) {
                    final EditorProfilesActivity editorActivity = this;
                    Handler handler = new Handler(getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!editorActivity.isFinishing()) {
                                boolean changeFilter = false;
                                switch (filterProfilesSelectedItem) {
                                    case EditorProfilesActivity.DSI_PROFILES_NO_SHOW_IN_ACTIVATOR:
                                        changeFilter = profile._showInActivator;
                                        break;
                                    case EditorProfilesActivity.DSI_PROFILES_SHOW_IN_ACTIVATOR:
                                        changeFilter = !profile._showInActivator;
                                        break;
                                }
                                if (changeFilter) {
                                    fragment.scrollToProfile = profile;
                                    ((GlobalGUIRoutines.HighlightedSpinnerAdapter) editorActivity.filterSpinner.getAdapter()).setSelection(0);
                                    editorActivity.selectFilterItem(0, 0, false, true);
                                }
                                else
                                    fragment.scrollToProfile = null;
                            }
                        }
                    }, 200);
                }
            }
        }
    }

    private void startEventPreferenceActivity(Event event, final int editMode, final int predefinedEventIndex) {
        /*
        if (event != null)
            PPApplication.logE("EditorProfilesActivity.startEventPreferenceActivity", "event._name="+event._name);
        PPApplication.logE("EditorProfilesActivity.startEventPreferenceActivity", "editMode="+editMode);
        PPApplication.logE("EditorProfilesActivity.startEventPreferenceActivity", "predefinedEventIndex="+predefinedEventIndex);
        */

        boolean profileExists = true;
        long startProfileId = 0;
        long endProfileId = -1;
        if ((editMode == EditorEventListFragment.EDIT_MODE_INSERT) && (predefinedEventIndex > 0)) {
            if (getDataWrapper() != null) {
                // search names of start and end profiles
                String[] profileStartNamesArray = getResources().getStringArray(R.array.addEventPredefinedStartProfilesArray);
                String[] profileEndNamesArray = getResources().getStringArray(R.array.addEventPredefinedEndProfilesArray);

                startProfileId = getDataWrapper().getProfileIdByName(profileStartNamesArray[predefinedEventIndex], true);
                if (startProfileId == 0)
                    profileExists = false;

                if (!profileEndNamesArray[predefinedEventIndex].isEmpty()) {
                    endProfileId = getDataWrapper().getProfileIdByName(profileEndNamesArray[predefinedEventIndex], true);
                    if (endProfileId == 0)
                        profileExists = false;
                }
            }
        }

        //PPApplication.logE("EditorProfilesActivity.startEventPreferenceActivity", "profileExists="+profileExists);
        if (profileExists) {
            Intent intent = new Intent(getBaseContext(), EventsPrefsActivity.class);
            if ((event == null) || (editMode == EditorEventListFragment.EDIT_MODE_INSERT))
                intent.putExtra(PPApplication.EXTRA_EVENT_ID, 0L);
            else {
                intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);
                intent.putExtra(PPApplication.EXTRA_EVENT_STATUS, event.getStatus());
            }
            intent.putExtra(EXTRA_NEW_EVENT_MODE, editMode);
            intent.putExtra(EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
            startActivityForResult(intent, REQUEST_CODE_EVENT_PREFERENCES);
            //PPApplication.logE("EditorProfilesActivity.startEventPreferenceActivity", "call of EventsPrefsActivity");
        } else {
            //PPApplication.logE("EditorProfilesActivity.startEventPreferenceActivity", "add new event");

            final long _startProfileId = startProfileId;
            final long _endProfileId = endProfileId;

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(R.string.menu_new_event);

            String startProfileName = "";
            String endProfileName = "";
            if (_startProfileId == 0) {
                // create profile
                int[] profileStartIndex = {0, 0, 0, 2, 4, 0, 5};
                startProfileName = getDataWrapper().getPredefinedProfile(profileStartIndex[predefinedEventIndex], false, getBaseContext())._name;
            }
            if (_endProfileId == 0) {
                // create profile
                int[] profileEndIndex = {0, 0, 0, 0, 0, 0, 6};
                endProfileName = getDataWrapper().getPredefinedProfile(profileEndIndex[predefinedEventIndex], false, getBaseContext())._name;
            }

            String message = "";
            if (!startProfileName.isEmpty())
                message = message + " \"" + startProfileName + "\"";
            if (!endProfileName.isEmpty()) {
                if (!message.isEmpty())
                    message = message + ",";
                message = message + " \"" + endProfileName + "\"";
            }
            message = getString(R.string.new_event_profiles_not_exists_alert_message1) + message + " " +
                    getString(R.string.new_event_profiles_not_exists_alert_message2);

            dialogBuilder.setMessage(message);

            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (_startProfileId == 0) {
                        // create profile
                        int[] profileStartIndex = {0, 0, 0, 2, 4, 0, 5};
                        getDataWrapper().getPredefinedProfile(profileStartIndex[predefinedEventIndex], true, getBaseContext());
                    }
                    if (_endProfileId == 0) {
                        // create profile
                        int[] profileEndIndex = {0, 0, 0, 0, 0, 0, 6};
                        getDataWrapper().getPredefinedProfile(profileEndIndex[predefinedEventIndex], true, getBaseContext());
                    }

                    Intent intent = new Intent(getBaseContext(), EventsPrefsActivity.class);
                    intent.putExtra(PPApplication.EXTRA_EVENT_ID, 0L);
                    intent.putExtra(EXTRA_NEW_EVENT_MODE, editMode);
                    intent.putExtra(EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
                    startActivityForResult(intent, REQUEST_CODE_EVENT_PREFERENCES);
                }
            });
            dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getBaseContext(), EventsPrefsActivity.class);
                    intent.putExtra(PPApplication.EXTRA_EVENT_ID, 0L);
                    intent.putExtra(EXTRA_NEW_EVENT_MODE, editMode);
                    intent.putExtra(EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
                    startActivityForResult(intent, REQUEST_CODE_EVENT_PREFERENCES);
                }
            });
            AlertDialog dialog = dialogBuilder.create();

//            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                @Override
//                public void onShow(DialogInterface dialog) {
//                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                    if (positive != null) positive.setAllCaps(false);
//                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                    if (negative != null) negative.setAllCaps(false);
//                }
//            });

            if (!isFinishing())
                dialog.show();
        }
    }

    public void onStartEventPreferences(Event event, int editMode, int predefinedEventIndex/*, boolean startTargetHelps*/) {
        if (((event != null) ||
            (editMode == EditorEventListFragment.EDIT_MODE_INSERT) ||
            (editMode == EditorEventListFragment.EDIT_MODE_DUPLICATE))
            && (editMode != EditorEventListFragment.EDIT_MODE_DELETE))
            startEventPreferenceActivity(event, editMode, predefinedEventIndex);
    }

    void redrawEventListFragment(final Event event, int newEventMode /*int predefinedEventIndex, boolean startTargetHelps*/) {
        // redraw list fragment, notification and widgets
        Fragment _fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
        if (_fragment instanceof EditorEventListFragment) {
            final EditorEventListFragment fragment = (EditorEventListFragment) getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
            if (fragment != null) {
                // update event, this rewrite event in eventList
                fragment.activityDataWrapper.updateEvent(event);

                boolean newEvent = ((newEventMode == EditorEventListFragment.EDIT_MODE_INSERT) ||
                        (newEventMode == EditorEventListFragment.EDIT_MODE_DUPLICATE));
                fragment.updateListView(event, newEvent, false, false/*, 0*/);

                Profile activeProfile = fragment.activityDataWrapper.getActivatedProfileFromDB(true,
                        ApplicationPreferences.applicationEditorPrefIndicator);
                fragment.updateHeader(activeProfile);

                if (filterEventsSelectedItem != 0) {
                    final EditorProfilesActivity editorActivity = this;
                    Handler handler = new Handler(getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!editorActivity.isFinishing()) {
                                boolean changeFilter = false;
                                switch (filterEventsSelectedItem) {
                                    case EditorProfilesActivity.DSI_EVENTS_NOT_STOPPED:
                                        changeFilter = event.getStatus() == Event.ESTATUS_STOP;
                                        break;
                                    case EditorProfilesActivity.DSI_EVENTS_RUNNING:
                                        changeFilter = event.getStatus() != Event.ESTATUS_RUNNING;
                                        break;
                                    case EditorProfilesActivity.DSI_EVENTS_PAUSED:
                                        changeFilter = event.getStatus() != Event.ESTATUS_PAUSE;
                                        break;
                                    case EditorProfilesActivity.DSI_EVENTS_STOPPED:
                                        changeFilter = event.getStatus() != Event.ESTATUS_STOP;
                                        break;
                                }
                                if (changeFilter) {
                                    fragment.scrollToEvent = event;
                                    ((GlobalGUIRoutines.HighlightedSpinnerAdapter) editorActivity.filterSpinner.getAdapter()).setSelection(0);
                                    editorActivity.selectFilterItem(1, 0, false, true);
                                }
                                else
                                    fragment.scrollToEvent = null;
                            }
                        }
                    }, 200);
                }
            }
        }
    }

    public static ApplicationsCache getApplicationsCache()
    {
        return applicationsCache;
    }

    public static void createApplicationsCache()
    {
        if ((!savedInstanceStateChanged) || (applicationsCache == null))
        {
            if (applicationsCache != null)
                applicationsCache.clearCache(true);
            applicationsCache =  new ApplicationsCache();
        }
    }

    private DataWrapper getDataWrapper()
    {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
        if (fragment != null)
        {
            if (fragment instanceof EditorProfileListFragment)
                return ((EditorProfileListFragment)fragment).activityDataWrapper;
            else
                return ((EditorEventListFragment)fragment).activityDataWrapper;
        }
        else
            return null;
    }

    private void setEventsRunStopIndicator()
    {
        //boolean whiteTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), true).equals("white");
        if (Event.getGlobalEventsRunning())
        {
            if (ApplicationPreferences.prefEventsBlocked) {
                //if (whiteTheme)
                //    eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_manual_activation_white);
                //else
                    eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_manual_activation);
            }
            else {
                //if (whiteTheme)
                //    eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_running_white);
                //else
                    eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_running);
            }
        }
        else {
            //if (whiteTheme)
            //    eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_stopped_white);
            //else
                eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_stopped);
        }
    }

    private void refreshGUI(/*final boolean refresh,*/ final boolean refreshIcons, final boolean setPosition, final long profileId, final long eventId)
    {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
                if (doImport)
                    return;

                setEventsRunStopIndicator();
                invalidateOptionsMenu();

                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                if (fragment != null) {
                    if (fragment instanceof EditorProfileListFragment)
                        ((EditorProfileListFragment) fragment).refreshGUI(/*refresh,*/ refreshIcons, setPosition, profileId);
                    else
                        ((EditorEventListFragment) fragment).refreshGUI(/*refresh,*/ refreshIcons, setPosition, eventId);
                }
//            }
//        });
    }

    /*
    private void setWindowContentOverlayCompat() {
        if (android.os.Build.VERSION.SDK_INT >= 20) {
            // Get the content view
            View contentView = findViewById(android.R.id.content);

            // Make sure it's a valid instance of a FrameLayout
            if (contentView instanceof FrameLayout) {
                TypedValue tv = new TypedValue();

                // Get the windowContentOverlay value of the current theme
                if (getTheme().resolveAttribute(
                        android.R.attr.windowContentOverlay, tv, true)) {

                    // If it's a valid resource, set it as the foreground drawable
                    // for the content view
                    if (tv.resourceId != 0) {
                        ((FrameLayout) contentView).setForeground(
                                getResources().getDrawable(tv.resourceId));
                    }
                }
            }
        }
    }
    */

    private void showTargetHelps() {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        startTargetHelps = true;

        boolean startTargetHelps = ApplicationPreferences.prefEditorActivityStartTargetHelps;
        boolean startTargetHelpsFilterSpinner = ApplicationPreferences.prefEditorActivityStartTargetHelpsFilterSpinner;
        boolean startTargetHelpsRunStopIndicator = ApplicationPreferences.prefEditorActivityStartTargetHelpsRunStopIndicator;
        boolean startTargetHelpsBottomNavigation = ApplicationPreferences.prefEditorActivityStartTargetHelpsBottomNavigation;

        if (startTargetHelps || startTargetHelpsFilterSpinner || startTargetHelpsRunStopIndicator || startTargetHelpsBottomNavigation ||
                ApplicationPreferences.prefEditorActivityStartTargetHelpsDefaultProfile ||
                ApplicationPreferences.prefEditorProfilesFragmentStartTargetHelps ||
                ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelps ||
                ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsOrder ||
                ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsShowInActivator ||
                ApplicationPreferences.prefEditorEventsFragmentStartTargetHelps ||
                ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsOrderSpinner ||
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps ||
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder ||
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsStatus) {

            if (startTargetHelps || startTargetHelpsFilterSpinner || startTargetHelpsRunStopIndicator || startTargetHelpsBottomNavigation) {
                //Log.d("EditorProfilesActivity.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
                editor.putBoolean(PREF_START_TARGET_HELPS, false);
                editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_FILTER_SPINNER, false);
                editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_RUN_STOP_INDICATOR, false);
                editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_BOTTOM_NAVIGATION, false);
                editor.apply();
                ApplicationPreferences.prefEditorActivityStartTargetHelps = false;
                ApplicationPreferences.prefEditorActivityStartTargetHelpsFilterSpinner = false;
                ApplicationPreferences.prefEditorActivityStartTargetHelpsRunStopIndicator = false;
                ApplicationPreferences.prefEditorActivityStartTargetHelpsBottomNavigation = false;

                //TypedValue tv = new TypedValue();
                //getTheme().resolveAttribute(R.attr.colorAccent, tv, true);

                //final Display display = getWindowManager().getDefaultDisplay();

                //String appTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), true);
                int outerCircleColor = R.color.tabTargetHelpOuterCircleColor;
//                if (appTheme.equals("dark"))
//                    outerCircleColor = R.color.tabTargetHelpOuterCircleColor_dark;
                int targetCircleColor = R.color.tabTargetHelpTargetCircleColor;
//                if (appTheme.equals("dark"))
//                    targetCircleColor = R.color.tabTargetHelpTargetCircleColor_dark;
                int textColor = R.color.tabTargetHelpTextColor;
//                if (appTheme.equals("dark"))
//                    textColor = R.color.tabTargetHelpTextColor_dark;

                //int[] screenLocation = new int[2];
                //filterSpinner.getLocationOnScreen(screenLocation);
                //filterSpinner.getLocationInWindow(screenLocation);
                //Rect filterSpinnerTarget = new Rect(0, 0, filterSpinner.getHeight(), filterSpinner.getHeight());
                //filterSpinnerTarget.offset(screenLocation[0] + 100, screenLocation[1]);

                /*
                eventsRunStopIndicator.getLocationOnScreen(screenLocation);
                //eventsRunStopIndicator.getLocationInWindow(screenLocation);
                Rect eventRunStopIndicatorTarget = new Rect(0, 0, eventsRunStopIndicator.getHeight(), eventsRunStopIndicator.getHeight());
                eventRunStopIndicatorTarget.offset(screenLocation[0], screenLocation[1]);
                */

                final TapTargetSequence sequence = new TapTargetSequence(this);
                List<TapTarget> targets = new ArrayList<>();
                if (startTargetHelps) {

                    // do not add it again
                    startTargetHelpsFilterSpinner = false;
                    startTargetHelpsRunStopIndicator = false;
                    startTargetHelpsBottomNavigation = false;

                    if (Event.getGlobalEventsRunning()) {
                        /*targets.add(
                            TapTarget.forToolbarNavigationIcon(editorToolbar, getString(R.string.editor_activity_targetHelps_navigationIcon_title), getString(R.string.editor_activity_targetHelps_navigationIcon_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(true)
                                    .drawShadow(true)
                                    .id(1)
                        );*/
                        targets.add(
                                //TapTarget.forBounds(filterSpinnerTarget, getString(R.string.editor_activity_targetHelps_filterSpinner_title), getString(R.string.editor_activity_targetHelps_filterSpinner_description))
                                TapTarget.forView(filterSpinner, getString(R.string.editor_activity_targetHelps_filterSpinner_title), getString(R.string.editor_activity_targetHelps_filterSpinner_description))
                                        .transparentTarget(true)
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(1)
                        );
                        targets.add(
                                TapTarget.forToolbarOverflow(editorToolbar, getString(R.string.editor_activity_targetHelps_applicationMenu_title), getString(R.string.editor_activity_targetHelps_applicationMenu_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(2)
                        );

                        int id = 3;
                        try {
                            targets.add(
                                    TapTarget.forToolbarMenuItem(editorToolbar, R.id.menu_restart_events, getString(R.string.editor_activity_targetHelps_restartEvents_title), getString(R.string.editor_activity_targetHelps_restartEvents_description))
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .textColor(textColor)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(id)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplication.recordException(e);
                        }
                        try {
                            targets.add(
                                    TapTarget.forToolbarMenuItem(editorToolbar, R.id.menu_activity_log, getString(R.string.editor_activity_targetHelps_activityLog_title), getString(R.string.editor_activity_targetHelps_activityLog_description))
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .textColor(textColor)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(id)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplication.recordException(e);
                        }
                        try {
                            targets.add(
                                    TapTarget.forToolbarMenuItem(editorToolbar, R.id.important_info_menu, getString(R.string.editor_activity_targetHelps_importantInfoButton_title), getString(R.string.editor_activity_targetHelps_importantInfoButton_description))
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .textColor(textColor)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(id)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplication.recordException(e);
                        }

                        targets.add(
                                TapTarget.forView(eventsRunStopIndicator, getString(R.string.editor_activity_targetHelps_trafficLightIcon_title), getString(R.string.editor_activity_targetHelps_trafficLightIcon_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(false)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;

                        targets.add(
                                TapTarget.forView(bottomNavigationView.findViewById(R.id.menu_profiles_view), getString(R.string.editor_activity_targetHelps_bottomNavigationProfiles_title),
                                        getString(R.string.editor_activity_targetHelps_bottomNavigationProfiles_description) + "\n" +
                                        getString(R.string.editor_activity_targetHelps_bottomNavigation_description_2))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                        targets.add(
                                TapTarget.forView(bottomNavigationView.findViewById(R.id.menu_events_view), getString(R.string.editor_activity_targetHelps_bottomNavigationEvents_title),
                                        getString(R.string.editor_activity_targetHelps_bottomNavigationEvents_description) + "\n" +
                                        getString(R.string.editor_activity_targetHelps_bottomNavigation_description_2))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                    } else {
                        /*targets.add(
                                TapTarget.forToolbarNavigationIcon(editorToolbar, getString(R.string.editor_activity_targetHelps_navigationIcon_title), getString(R.string.editor_activity_targetHelps_navigationIcon_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(1)
                        );*/
                        targets.add(
                                //TapTarget.forBounds(filterSpinnerTarget, getString(R.string.editor_activity_targetHelps_filterSpinner_title), getString(R.string.editor_activity_targetHelps_filterSpinner_description))
                                TapTarget.forView(filterSpinner, getString(R.string.editor_activity_targetHelps_filterSpinner_title), getString(R.string.editor_activity_targetHelps_filterSpinner_description))
                                        .transparentTarget(true)
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(1)
                        );
                        targets.add(
                                TapTarget.forToolbarOverflow(editorToolbar, getString(R.string.editor_activity_targetHelps_applicationMenu_title), getString(R.string.editor_activity_targetHelps_applicationMenu_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(2)
                        );

                        int id = 3;
                        try {
                            targets.add(
                                    TapTarget.forToolbarMenuItem(editorToolbar, R.id.menu_run_stop_events, getString(R.string.editor_activity_targetHelps_runStopEvents_title), getString(R.string.editor_activity_targetHelps_runStopEvents_description))
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .textColor(textColor)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(id)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplication.recordException(e);
                        }
                        try {
                            targets.add(
                                    TapTarget.forToolbarMenuItem(editorToolbar, R.id.menu_activity_log, getString(R.string.editor_activity_targetHelps_activityLog_title), getString(R.string.editor_activity_targetHelps_activityLog_description))
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .textColor(textColor)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(id)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplication.recordException(e);
                        }
                        try {
                            targets.add(
                                    TapTarget.forToolbarMenuItem(editorToolbar, R.id.important_info, getString(R.string.editor_activity_targetHelps_importantInfoButton_title), getString(R.string.editor_activity_targetHelps_importantInfoButton_description))
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .textColor(textColor)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(id)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplication.recordException(e);
                        }

                        targets.add(
                                TapTarget.forView(eventsRunStopIndicator, getString(R.string.editor_activity_targetHelps_trafficLightIcon_title), getString(R.string.editor_activity_targetHelps_trafficLightIcon_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(false)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;

                        targets.add(
                                TapTarget.forView(bottomNavigationView.findViewById(R.id.menu_profiles_view), getString(R.string.editor_activity_targetHelps_bottomNavigationProfiles_title),
                                        getString(R.string.editor_activity_targetHelps_bottomNavigationProfiles_description) + "\n" +
                                        getString(R.string.editor_activity_targetHelps_bottomNavigation_description_2))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                        targets.add(
                                TapTarget.forView(bottomNavigationView.findViewById(R.id.menu_events_view), getString(R.string.editor_activity_targetHelps_bottomNavigationEvents_title),
                                        getString(R.string.editor_activity_targetHelps_bottomNavigationEvents_description) + "\n" +
                                        getString(R.string.editor_activity_targetHelps_bottomNavigation_description_2))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;

                    }
                }
                if (startTargetHelpsFilterSpinner) {
                    targets.add(
                            //TapTarget.forBounds(filterSpinnerTarget, getString(R.string.editor_activity_targetHelps_filterSpinner_title), getString(R.string.editor_activity_targetHelps_filterSpinner_description))
                            TapTarget.forView(filterSpinner, getString(R.string.editor_activity_targetHelps_filterSpinner_title), getString(R.string.editor_activity_targetHelps_filterSpinner_description))
                                    .transparentTarget(true)
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(true)
                                    .drawShadow(true)
                                    .id(1)
                    );
                }
                if (startTargetHelpsRunStopIndicator) {
                    targets.add(
                            TapTarget.forView(eventsRunStopIndicator, getString(R.string.editor_activity_targetHelps_trafficLightIcon_title), getString(R.string.editor_activity_targetHelps_trafficLightIcon_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(false)
                                    .drawShadow(true)
                                    .id(1)
                    );
                }
                if (startTargetHelpsBottomNavigation) {
                    targets.add(
                            TapTarget.forView(bottomNavigationView.findViewById(R.id.menu_profiles_view), getString(R.string.editor_activity_targetHelps_bottomNavigationProfiles_title),
                                    getString(R.string.editor_activity_targetHelps_bottomNavigationProfiles_description) + "\n" +
                                    getString(R.string.editor_activity_targetHelps_bottomNavigation_description_2))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(true)
                                    .drawShadow(true)
                                    .id(1)
                    );
                    targets.add(
                            TapTarget.forView(bottomNavigationView.findViewById(R.id.menu_events_view), getString(R.string.editor_activity_targetHelps_bottomNavigationEvents_title),
                                    getString(R.string.editor_activity_targetHelps_bottomNavigationEvents_description) + "\n " +
                                    getString(R.string.editor_activity_targetHelps_bottomNavigation_description_2))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(true)
                                    .drawShadow(true)
                                    .id(2)
                    );
                }

                sequence.targets(targets);

                sequence.listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        targetHelpsSequenceStarted = false;
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                        if (fragment != null) {
                            if (fragment instanceof EditorProfileListFragment)
                                ((EditorProfileListFragment) fragment).showTargetHelps();
                            else
                                ((EditorEventListFragment) fragment).showTargetHelps();
                        }
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        targetHelpsSequenceStarted = false;
                        Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
                        if (editorSelectedView == 0) {
                            editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, false);
                            editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, false);
                            if (filterProfilesSelectedItem == DSI_PROFILES_SHOW_IN_ACTIVATOR)
                                editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
                            if (filterProfilesSelectedItem == DSI_PROFILES_ALL)
                                editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_SHOW_IN_ACTIVATOR, false);
                            ApplicationPreferences.prefEditorProfilesFragmentStartTargetHelps = false;
                            ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelps = false;
                            if (filterProfilesSelectedItem == DSI_PROFILES_SHOW_IN_ACTIVATOR)
                                ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsOrder = false;
                            if (filterProfilesSelectedItem == DSI_PROFILES_ALL)
                                ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsShowInActivator = false;
                        }
                        else {
                            editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS, false);
                            editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, false);
                            if (filterEventsSelectedItem == DSI_EVENTS_START_ORDER)
                                editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
                            editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_STATUS, false);
                            ApplicationPreferences.prefEditorEventsFragmentStartTargetHelps = false;
                            ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps = false;
                            if (filterEventsSelectedItem == DSI_EVENTS_START_ORDER)
                                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder = false;
                            ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsStatus = false;
                        }
                        editor.apply();
                    }
                });
                sequence.continueOnCancel(true)
                        .considerOuterCircleCanceled(true);
                targetHelpsSequenceStarted = true;
                sequence.start();
            }
            else {
                //Log.d("EditorProfilesActivity.showTargetHelps", "PREF_START_TARGET_HELPS=false");
                //final Context context = getApplicationContext();
                final Handler handler = new Handler(getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(PPApplication.PACKAGE_NAME + ".ShowEditorTargetHelpsBroadcastReceiver");
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        /*if (EditorProfilesActivity.getInstance() != null) {
                            Fragment fragment = EditorProfilesActivity.getInstance().getFragmentManager().findFragmentById(R.id.editor_list_container);
                            if (fragment != null) {
                                if (fragment instanceof EditorProfileListFragment)
                                    ((EditorProfileListFragment) fragment).showTargetHelps();
                                else
                                    ((EditorEventListFragment) fragment).showTargetHelps();
                            }
                        }*/
                    }
                }, 500);
            }
        }
    }

    static boolean displayPreferencesErrorNotification(Profile profile, Event event, Context context) {
        if ((profile == null) && (event == null))
            return false;

        if (!PPApplication.getApplicationStarted(true))
            return false;

        if ((profile != null) && (!ProfilesPrefsFragment.isRedTextNotificationRequired(profile, context))) {
            // clear notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            try {
                notificationManager.cancel(
                        PPApplication.DISPLAY_PREFERENCES_PROFILE_ERROR_NOTIFICATION_TAG+"_"+profile._id,
                        PPApplication.PROFILE_ID_NOTIFICATION_ID + (int) profile._id);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }

            return false;
        }
        if ((event != null) && (!EventsPrefsFragment.isRedTextNotificationRequired(event, context))) {
            // clear notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            try {
                notificationManager.cancel(
                        PPApplication.DISPLAY_PREFERENCES_EVENT_ERROR_NOTIFICATION_TAG+"_"+event._id,
                        PPApplication.EVENT_ID_NOTIFICATION_ID + (int) event._id);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }

            return false;
        }

        int notificationID = 0;
        String notificationTag = null;

        String nTitle = "";
        String nText = "";

        Intent intent = null;

        if (profile != null) {
            intent = new Intent(context, ProfilesPrefsActivity.class);
            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
            intent.putExtra(EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_EDIT);
            intent.putExtra(EXTRA_PREDEFINED_PROFILE_INDEX, 0);
        }
        if (event != null) {
            intent = new Intent(context, EventsPrefsActivity.class);
            intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);
            intent.putExtra(PPApplication.EXTRA_EVENT_STATUS, event.getStatus());
            intent.putExtra(EXTRA_NEW_EVENT_MODE, EditorEventListFragment.EDIT_MODE_EDIT);
            intent.putExtra(EXTRA_PREDEFINED_EVENT_INDEX, 0);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (profile != null) {
            nTitle = context.getString(R.string.profile_preferences_red_texts_title);
            nText = context.getString(R.string.profile_preferences_red_texts_text_1) + " " +
                    "\"" + profile._name + "\" " +
                    context.getString(R.string.preferences_red_texts_text_2) + " " +
                    context.getString(R.string.preferences_red_texts_text_click);
            if (android.os.Build.VERSION.SDK_INT < 24) {
                nTitle = context.getString(R.string.ppp_app_name);
                nText = context.getString(R.string.profile_preferences_red_texts_title) + ": " +
                        context.getString(R.string.profile_preferences_red_texts_text_1) + " " +
                        "\"" + profile._name + "\" " +
                        context.getString(R.string.preferences_red_texts_text_2) + " " +
                        context.getString(R.string.preferences_red_texts_text_click);
            }

            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
            notificationID = PPApplication.PROFILE_ID_NOTIFICATION_ID + (int) profile._id;
            notificationTag = PPApplication.DISPLAY_PREFERENCES_PROFILE_ERROR_NOTIFICATION_TAG+"_"+profile._id;
        }

        if (event != null) {
            nTitle = context.getString(R.string.event_preferences_red_texts_title);
            nText = context.getString(R.string.event_preferences_red_texts_text_1) + " " +
                    "\"" + event._name + "\" " +
                    context.getString(R.string.preferences_red_texts_text_2) + " " +
                    context.getString(R.string.preferences_red_texts_text_click);
            if (android.os.Build.VERSION.SDK_INT < 24) {
                nTitle = context.getString(R.string.ppp_app_name);
                nText = context.getString(R.string.event_preferences_red_texts_title) + ": " +
                        context.getString(R.string.event_preferences_red_texts_text_1) + " " +
                        "\"" + event._name + "\" " +
                        context.getString(R.string.preferences_red_texts_text_2) + " " +
                        context.getString(R.string.preferences_red_texts_text_click);
            }

            intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);
            notificationID = PPApplication.EVENT_ID_NOTIFICATION_ID + (int) event._id;
            notificationTag = PPApplication.DISPLAY_PREFERENCES_EVENT_ERROR_NOTIFICATION_TAG+"_"+event._id;
        }

        intent.putExtra(EXTRA_FROM_RED_TEXT_PREFERENCES_NOTIFICATION, true);

        PPApplication.createGrantPermissionNotificationChannel(context);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context, PPApplication.GRANT_PERMISSION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(nTitle) // title for notification
                .setContentText(nText) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));

        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);

        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        mBuilder.setOnlyAlertOnce(true);

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
        try {
            // do not cancel, mBuilder.setOnlyAlertOnce(true); will not be working
            // mNotificationManager.cancel(notificationID);
            mNotificationManager.notify(notificationTag, notificationID, mBuilder.build());
        } catch (Exception e) {
            //Log.e("EditorProfilesActivity.displayNotGrantedPermissionsNotification", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }

        return true;
    }

    static void showDialogAboutRedText(Profile profile, Event event, boolean forShowInActivator, boolean forRunStopEvent, Activity activity) {
        if (activity == null)
            return;

        String nTitle = "";
        String nText = "";

        if (profile != null) {
            nTitle = activity.getString(R.string.profile_preferences_red_texts_title);
            nText = activity.getString(R.string.profile_preferences_red_texts_text_1) + " " +
                    "\"" + profile._name + "\" " +
                    activity.getString(R.string.preferences_red_texts_text_2);
            if (android.os.Build.VERSION.SDK_INT < 24) {
                nTitle = activity.getString(R.string.ppp_app_name);
                nText = activity.getString(R.string.profile_preferences_red_texts_title) + ": " +
                        activity.getString(R.string.profile_preferences_red_texts_text_1) + " " +
                        "\"" + profile._name + "\" " +
                        activity.getString(R.string.preferences_red_texts_text_2);
            }
            if (forShowInActivator)
                nText = nText + " " + activity.getString(R.string.profile_preferences_red_texts_text_3);
            else
                nText = nText + " " + activity.getString(R.string.profile_preferences_red_texts_text_2);
        }

        if (event != null) {
            nTitle = activity.getString(R.string.event_preferences_red_texts_title);
            nText = activity.getString(R.string.event_preferences_red_texts_text_1) + " " +
                    "\"" + event._name + "\" " +
                    activity.getString(R.string.preferences_red_texts_text_2);
            if (android.os.Build.VERSION.SDK_INT < 24) {
                nTitle = activity.getString(R.string.ppp_app_name);
                nText = activity.getString(R.string.event_preferences_red_texts_title) + ": " +
                        activity.getString(R.string.event_preferences_red_texts_text_1) + " " +
                        "\"" + event._name + "\" " +
                        activity.getString(R.string.preferences_red_texts_text_2);
            }
            if (forRunStopEvent)
                nText = nText + " " + activity.getString(R.string.event_preferences_red_texts_text_2);
            else
                nText = nText + " " + activity.getString(R.string.profile_preferences_red_texts_text_2);
        }

        if ((profile != null) || (event != null)) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle(nTitle);
            dialogBuilder.setMessage(nText);
            dialogBuilder.setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = dialogBuilder.create();

//            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                @Override
//                public void onShow(DialogInterface dialog) {
//                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                    if (positive != null) positive.setAllCaps(false);
//                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                    if (negative != null) negative.setAllCaps(false);
//                }
//            });

            if (!activity.isFinishing())
                dialog.show();
        }
    }

}
