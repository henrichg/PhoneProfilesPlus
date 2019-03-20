package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Rect;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.labo.kaji.relativepopupwindow.RelativePopupWindow;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.drakeet.support.toast.ToastCompat;
import sk.henrichg.phoneprofilesplus.EditorEventListFragment.OnStartEventPreferences;
import sk.henrichg.phoneprofilesplus.EditorProfileListFragment.OnStartProfilePreferences;
import sk.henrichg.phoneprofilesplus.EventDetailsFragment.OnStartEventPreferencesFromDetail;
import sk.henrichg.phoneprofilesplus.ProfileDetailsFragment.OnStartProfilePreferencesFromDetail;

public class EditorProfilesActivity extends AppCompatActivity
                                    implements OnStartProfilePreferences,
                                               OnStartEventPreferences,
                                               OnStartProfilePreferencesFromDetail,
                                               OnStartEventPreferencesFromDetail
{

    //private static volatile EditorProfilesActivity instance;

    private ImageView eventsRunStopIndicator;

    private static boolean savedInstanceStateChanged;

    private static ApplicationsCache applicationsCache;
    private static ContactsCache contactsCache;
    private static ContactGroupsCache contactGroupsCache;

    private AsyncTask importAsyncTask = null;
    private AsyncTask exportAsyncTask = null;
    static boolean doImport = false;
    private AlertDialog importProgressDialog = null;
    private AlertDialog exportProgressDialog = null;

    private static final String SP_DATA_DETAILS_DATA_TYPE = "data_detail_data_type";
    private static final String SP_DATA_DETAILS_DATA_ID = "data_detail_data_id";
    private static final String SP_DATA_DETAILS_EDIT_MODE = "data_detail_edit_mode";
    private static final String SP_DATA_DETAILS_PREDEFINED_PROFILE_INDEX = "data_detail_predefined_profile_index";
    private static final String SP_DATA_DETAILS_PREDEFINED_EVENT_INDEX = "data_detail_predefined_event_index";

    private static final String SP_EDITOR_DRAWER_SELECTED_ITEM = "editor_drawer_selected_item";
    private static final String SP_EDITOR_ORDER_SELECTED_ITEM = "editor_order_selected_item";

    private static final int DSI_PROFILES_ALL = 1;
    private static final int DSI_PROFILES_SHOW_IN_ACTIVATOR = 2;
    private static final int DSI_PROFILES_NO_SHOW_IN_ACTIVATOR = 3;
    private static final int DSI_EVENTS_START_ORDER = 4;
    private static final int DSI_EVENTS_ALL = 5;
    private static final int DSI_EVENTS_RUNNING = 6;
    private static final int DSI_EVENTS_PAUSED = 7;
    private static final int DSI_EVENTS_STOPPED = 8;

    static final String EXTRA_NEW_PROFILE_MODE = "new_profile_mode";
    static final String EXTRA_PREDEFINED_PROFILE_INDEX = "predefined_profile_index";
    static final String EXTRA_NEW_EVENT_MODE = "new_event_mode";
    static final String EXTRA_PREDEFINED_EVENT_INDEX = "predefined_event_index";

    // request code for startActivityForResult with intent BackgroundActivateProfileActivity
    static final int REQUEST_CODE_ACTIVATE_PROFILE = 6220;
    // request code for startActivityForResult with intent ProfilePreferencesActivity
    static final int REQUEST_CODE_PROFILE_PREFERENCES = 6221;
    // request code for startActivityForResult with intent EventPreferencesActivity
    private static final int REQUEST_CODE_EVENT_PREFERENCES = 6222;
    // request code for startActivityForResult with intent PhoneProfilesActivity
    private static final int REQUEST_CODE_APPLICATION_PREFERENCES = 6229;
    // request code for startActivityForResult with intent "phoneprofiles.intent.action.EXPORTDATA"
    private static final int REQUEST_CODE_REMOTE_EXPORT = 6250;

    public boolean targetHelpsSequenceStarted;
    public static final String PREF_START_TARGET_HELPS = "editor_profiles_activity_start_target_helps";
    public static final String PREF_START_TARGET_HELPS_DEFAULT_PROFILE = "editor_profile_activity_start_target_helps_default_profile";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private static boolean mTwoPane;

    private Toolbar editorToolbar;
    private DrawerLayout drawerLayout;
    private PPScrimInsetsFrameLayout drawerRoot;
    private ListView drawerListView;
    private ActionBarDrawerToggle drawerToggle;
    private TextView filterStatusBarTitle;
    private AppCompatSpinner orderSpinner;
    private View headerView;
    private ImageView drawerHeaderFilterImage;
    private TextView drawerHeaderFilterTitle;
    private TextView drawerHeaderFilterSubtitle;

    private String[] drawerItemsTitle;
    private String[] drawerItemsSubtitle;
    private Integer[] drawerItemsIcon;

    private int drawerSelectedItem = 1;
    private int orderSelectedItem = 0;
    //private int eventsOrderType = EditorEventListFragment.ORDER_TYPE_START_ORDER;

    private static final int COUNT_DRAWER_PROFILE_ITEMS = 3;

    AddProfileDialog addProfileDialog;
    AddEventDialog addEventDialog;

    private final BroadcastReceiver refreshGUIBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            boolean refresh = intent.getBooleanExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH, true);
            boolean refreshIcons = intent.getBooleanExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH_ICONS, false);
            long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
            long eventId = intent.getLongExtra(PPApplication.EXTRA_EVENT_ID, 0);
            // not change selection in editor if refresh is outside editor
            EditorProfilesActivity.this.refreshGUI(refresh, refreshIcons, false, profileId, eventId);
        }
    };

    private final BroadcastReceiver showTargetHelpsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
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
            EditorProfilesActivity.this.finish();
        }
    };

    @SuppressLint({"NewApi", "RestrictedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalGUIRoutines.setTheme(this, false, true, true);
        GlobalGUIRoutines.setLanguage(getBaseContext());

        /*synchronized (EditorProfilesActivity.class) {
            PPApplication.logE("$$$$$ EditorProfilesActivity.onCreate", "instance set");
            instance = this;
        }*/

        savedInstanceStateChanged = (savedInstanceState != null);

        createApplicationsCache();
        createContactsCache();
        createContactGroupsCache();

        if (/*(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) &&*/ (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP))
            setContentView(R.layout.activity_editor_list_onepane_19);
        else
            setContentView(R.layout.activity_editor_list_onepane);

        drawerLayout = findViewById(R.id.editor_list_drawer_layout);

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

        String appTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), true);

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

        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //	getWindow().setNavigationBarColor(R.attr.colorPrimary);

        //setWindowContentOverlayCompat();

    /*	// add profile list into list container
        EditorProfileListFragment fragment = new EditorProfileListFragment();
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment").commit(); */


        if (findViewById(R.id.editor_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            if (savedInstanceState == null) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_detail_container);
                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .remove(fragment).commit();
                }
            }
            else
            {
                // for 7 inch tablets layout changed:
                //   - portrait - one pane
                //   - landscape - two pane
                // onRestartProfilePreferences is called, when user save/not save profile
                // preference changes (Back button, or Cancel in ActionMode)
                // In this method, editMode and profile_id is saved into shared preferences
                // And when orientation changed into landscape mode, profile preferences fragment
                // must by recreated due profile preference changes
                ApplicationPreferences.getSharedPreferences(this);
                int dataType = ApplicationPreferences.preferences.getInt(SP_DATA_DETAILS_DATA_TYPE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
                if (dataType == 1) {
                    long profile_id = ApplicationPreferences.preferences.getLong(SP_DATA_DETAILS_DATA_ID, 0);
                    int editMode = ApplicationPreferences.preferences.getInt(SP_DATA_DETAILS_EDIT_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
                    int predefinedProfileIndex = ApplicationPreferences.preferences.getInt(SP_DATA_DETAILS_PREDEFINED_PROFILE_INDEX, 0);
                    Bundle arguments = new Bundle();
                    arguments.putLong(PPApplication.EXTRA_PROFILE_ID, profile_id);
                    arguments.putInt(EXTRA_NEW_PROFILE_MODE, editMode);
                    arguments.putInt(EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
                    arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, false);
                    ProfileDetailsFragment fragment = new ProfileDetailsFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.editor_detail_container, fragment, "ProfileDetailsFragment").commit();
                }
                else if (dataType == 2) {
                    long event_id = ApplicationPreferences.preferences.getLong(SP_DATA_DETAILS_DATA_ID, 0);
                    int editMode = ApplicationPreferences.preferences.getInt(SP_DATA_DETAILS_EDIT_MODE, EditorEventListFragment.EDIT_MODE_UNDEFINED);
                    int predefinedEventIndex = ApplicationPreferences.preferences.getInt(SP_DATA_DETAILS_PREDEFINED_EVENT_INDEX, 0);
                    Bundle arguments = new Bundle();
                    arguments.putLong(PPApplication.EXTRA_EVENT_ID, event_id);
                    //arguments.putInt(PPApplication.EXTRA_EVENT_STATUS, event.getStatus());
                    arguments.putInt(EXTRA_NEW_EVENT_MODE, editMode);
                    arguments.putInt(EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
                    arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, false);
                    EventDetailsFragment fragment = new EventDetailsFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.editor_detail_container, fragment, "EventDetailsFragment").commit();
                }
                else {
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_detail_container);
                    if (fragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .remove(fragment).commit();
                    }
                }
            }
        }
        else
        {
            mTwoPane = false;
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag("ProfileDetailsFragment");
            if (fragment != null)
                fragmentManager.beginTransaction()
                .remove(fragment).commit();
            fragment = fragmentManager.findFragmentByTag("EventDetailsFragment");
            if (fragment != null)
                fragmentManager.beginTransaction()
                .remove(fragment).commit();
            fragmentManager.executePendingTransactions();
        }

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
        headerView =  ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
                            inflate(R.layout.editor_drawer_list_header, drawerListView, false);
        drawerListView.addHeaderView(headerView, null, false);
        drawerHeaderFilterImage = findViewById(R.id.editor_drawer_list_header_icon);
        drawerHeaderFilterTitle = findViewById(R.id.editor_drawer_list_header_title);
        drawerHeaderFilterSubtitle = findViewById(R.id.editor_drawer_list_header_subtitle);

        if (Build.VERSION.SDK_INT >= 21) {
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
        }

        if (Build.VERSION.SDK_INT < 21)
            drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

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
        EditorDrawerListAdapter drawerAdapter = new EditorDrawerListAdapter(/*drawerListView, */getBaseContext(), drawerItemsTitle, drawerItemsSubtitle, drawerItemsIcon);
        
        // Set the MenuListAdapter to the ListView
        drawerListView.setAdapter(drawerAdapter);
 
        // Capture listview menu item click
        drawerListView.setOnItemClickListener(new DrawerItemClickListener());


        editorToolbar = findViewById(R.id.editor_toolbar);
        setSupportActionBar(editorToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_editor);
        }

        // Enable ActionBar app icon to behave as action to toggle nav drawer
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.editor_drawer_open, R.string.editor_drawer_open)
        {
            /*
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }
 
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
            */
            
            // this disable animation 
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) 
            {
                  if(drawerView!=null && drawerView == drawerRoot){
                        super.onDrawerSlide(drawerView, 0);
                  }else{
                        super.onDrawerSlide(drawerView, slideOffset);
                  }
            }            
        };
        drawerLayout.addDrawerListener(drawerToggle);
        
        filterStatusBarTitle = findViewById(R.id.editor_filter_title);
        filterStatusBarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(drawerRoot);
            }
        });
       
        orderSpinner = findViewById(R.id.editor_list_bottom_bar_order);
        ArrayAdapter<CharSequence> orderSpinnerAdapter = ArrayAdapter.createFromResource(
                                    //getSupportActionBar().getThemedContext(),
                                    getBaseContext(),
                                    R.array.drawerOrderEvents,
                                    //android.R.layout.simple_spinner_item);
                                    R.layout.editor_drawer_spinner);
        //orderSpinnerAdapter.setDropDownViewResource(android.support.v7.appcompat.R.layout.support_simple_spinner_dropdown_item);
        orderSpinnerAdapter.setDropDownViewResource(R.layout.editor_drawer_spinner_dropdown);
        switch (appTheme) {
            case "dark":
                orderSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(getBaseContext(), R.color.editorFilterTitleColor_dark));
                orderSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_dark);
                break;
            case "white":
                orderSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(getBaseContext(), R.color.editorFilterTitleColor_white));
                orderSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_white);
                break;
            case "dlight":
                orderSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(getBaseContext(), R.color.editorFilterTitleColor));
                orderSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_dlight);
                break;
            default:
                orderSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(getBaseContext(), R.color.editorFilterTitleColor));
                orderSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_color);
                break;
        }
        orderSpinner.setAdapter(orderSpinnerAdapter);
        orderSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != orderSelectedItem)
                    changeEventOrder(position, false);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        TextView orderLabel = findViewById(R.id.editor_list_bottom_bar_order_title);
        orderLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderSpinner.performClick();
            }
        });

        eventsRunStopIndicator = findViewById(R.id.editor_list_run_stop_indicator);
        eventsRunStopIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });
        
        // set drawer item and order
        if ((savedInstanceState != null) || (ApplicationPreferences.applicationEditorSaveEditorState(getApplicationContext())))
        {
            ApplicationPreferences.getSharedPreferences(this);
            drawerSelectedItem = ApplicationPreferences.preferences.getInt(SP_EDITOR_DRAWER_SELECTED_ITEM, 1);
            orderSelectedItem = ApplicationPreferences.preferences.getInt(SP_EDITOR_ORDER_SELECTED_ITEM, 0);
        }

        PPApplication.logE("EditorProfilesActivity.onCreate", "orderSelectedItem="+orderSelectedItem);
        // first must be set eventsOrderType
        changeEventOrder(orderSelectedItem, savedInstanceState != null);
        selectDrawerItem(drawerSelectedItem, false, savedInstanceState != null, false);

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


        LocalBroadcastManager.getInstance(this).registerReceiver(refreshGUIBroadcastReceiver,
                new IntentFilter("RefreshEditorGUIBroadcastReceiver"));
        LocalBroadcastManager.getInstance(this).registerReceiver(showTargetHelpsBroadcastReceiver,
                new IntentFilter("ShowEditorTargetHelpsBroadcastReceiver"));

        refreshGUI(true, false, true, 0, 0);

        LocalBroadcastManager.getInstance(this).registerReceiver(finishBroadcastReceiver,
                new IntentFilter("FinishEditorBroadcastReceiver"));
    }

    /*public static EditorProfilesActivity getInstance()
    {
        return instance;
    }*/

    @Override
    protected void onStart()
    {
        super.onStart();

        // this is for list widget header
        if (!PPApplication.getApplicationStarted(getApplicationContext(), true))
        {
            PPApplication.logE("EditorProfilesActivity.onStart", "application is not started");
            PPApplication.logE("EditorProfilesActivity.onStart", "service instance="+PhoneProfilesService.getInstance());
            if (PhoneProfilesService.getInstance() != null)
                PPApplication.logE("EditorProfilesActivity.onStart", "service hasFirstStart="+PhoneProfilesService.getInstance().getServiceHasFirstStart());
            // start PhoneProfilesService
            //PPApplication.firstStartServiceStarted = false;
            PPApplication.setApplicationStarted(getApplicationContext(), true);
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_INITIALIZE_START, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
            PPApplication.startPPService(this, serviceIntent);
        }
        else
        {
            if ((PhoneProfilesService.getInstance() == null) || (!PhoneProfilesService.getInstance().getServiceHasFirstStart())) {
                PPApplication.logE("EditorProfilesActivity.onStart", "application is started");
                PPApplication.logE("EditorProfilesActivity.onStart", "service instance="+PhoneProfilesService.getInstance());
                if (PhoneProfilesService.getInstance() != null)
                    PPApplication.logE("EditorProfilesActivity.onStart", "service hasFirstStart="+PhoneProfilesService.getInstance().getServiceHasFirstStart());
                // start PhoneProfilesService
                //PPApplication.firstStartServiceStarted = false;
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_INITIALIZE_START, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
                PPApplication.startPPService(this, serviceIntent);
            }
            else {
                PPApplication.logE("EditorProfilesActivity.onStart", "application and service is started");
            }
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if ((addProfileDialog != null) && (addProfileDialog.mDialog != null) && addProfileDialog.mDialog.isShowing())
            addProfileDialog.mDialog.dismiss();
        if ((addEventDialog != null) && (addEventDialog.mDialog != null) && addEventDialog.mDialog.isShowing())
            addEventDialog.mDialog.dismiss();

        /*synchronized (EditorProfilesActivity.class) {
            PPApplication.logE("$$$$$ EditorProfilesActivity.onStop", "instance clear");
            instance = null;
        }*/
    }

    /*
    @Override
    protected void onResume()
    {
        //Debug.stopMethodTracing();
        super.onResume();

        if (EditorProfilesActivity.getInstance() == null)
        {
            synchronized (EditorProfilesActivity.class) {
                PPApplication.logE("$$$$$ EditorProfilesActivity.onResume", "instance set");
                instance = this;
            }
            refreshGUI(false, false);
        }
    }
    */

    @Override
    protected void onDestroy()
    {
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
        if ((exportAsyncTask != null) && !exportAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            exportAsyncTask.cancel(true);
        }

        if (!savedInstanceStateChanged)
        {
            // no destroy caches on orientation change
            if (applicationsCache != null)
                applicationsCache.clearCache(true);
            applicationsCache = null;
            if (contactsCache != null)
                contactsCache.clearCache(true);
            contactsCache = null;
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshGUIBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(showTargetHelpsBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(finishBroadcastReceiver);

        super.onDestroy();
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
            if (Event.getGlobalEventsRunning(getApplicationContext()))
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
            menuItem.setVisible(Event.getGlobalEventsRunning(getApplicationContext()));
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
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(drawerRoot)) {
                    drawerLayout.closeDrawer(drawerRoot);
                } else {
                    drawerLayout.openDrawer(drawerRoot);
                }
                return super.onOptionsItemSelected(item);
            case R.id.menu_restart_events:
                //noinspection ConstantConditions
                //getDataWrapper().addActivityLog(DatabaseHandler.ALTYPE_RESTARTEVENTS, null, null, null, 0);

                // ignore manual profile activation
                // and unblock forceRun events
                PPApplication.logE("$$$ restartEvents","from EditorProfilesActivity.onOptionsItemSelected menu_restart_events");
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
                intent = new Intent(getBaseContext(), PhoneProfilesPreferencesActivity.class);

                startActivityForResult(intent, REQUEST_CODE_APPLICATION_PREFERENCES);

                return true;
            case R.id.menu_dark_theme:
                String theme = ApplicationPreferences.applicationTheme(getApplicationContext(), false);
                if (!theme.equals("night_mode")) {
                    if (theme.equals("dark")) {
                        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(getApplicationContext());
                        theme = preferences.getString(ApplicationPreferences.PREF_APPLICATION_NOT_DARK_THEME, "color");
                        Editor editor = preferences.edit();
                        editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, theme);
                        editor.apply();
                    } else {
                        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(getApplicationContext());
                        Editor editor = preferences.edit();
                        editor.putString(ApplicationPreferences.PREF_APPLICATION_NOT_DARK_THEME, theme);
                        editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, "dark");
                        editor.apply();
                    }
                    GlobalGUIRoutines.reloadActivity(this, true);
                }
                return true;
            case R.id.menu_export:
                exportData();

                return true;
            case R.id.menu_import:
                importData();

                return true;
            /*case R.id.menu_help:
                try {
                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/henrichg/PhoneProfilesPlus/wiki"));
                    startActivity(myIntent);
                } catch (ActivityNotFoundException e) {
                    ToastCompat.makeText(getApplicationContext(), "No application can handle this request."
                        + " Please install a web browser",  Toast.LENGTH_LONG).show();
                }
                return true;*/
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
                        PPApplication.exitApp(true, getApplicationContext(), EditorProfilesActivity.this.getDataWrapper(),
                                EditorProfilesActivity.this, false/*, true, true*/);
                    }
                });
                dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
                AlertDialog dialog = dialogBuilder.create();
                /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        if (positive != null) positive.setAllCaps(false);
                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        if (negative != null) negative.setAllCaps(false);
                    }
                });*/
                if (!isFinishing())
                    dialog.show();
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

    // ListView click listener in the navigation drawer
    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            // header is position=0
            if (position > 0)
                selectDrawerItem(position, true, false, true);
        }
    }
 
    private void selectDrawerItem(int position, boolean removePreferences, boolean orientationChange, boolean startTargetHelps) {
        PPApplication.logE("EditorProfilesActivity.selectDrawerItem", "position="+position);
        PPApplication.logE("EditorProfilesActivity.selectDrawerItem", "drawerSelectedItem="+drawerSelectedItem);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
        if (position == 0) position = 1;
        if ((position != drawerSelectedItem) || (fragment == null))
        {
            // stop running AsyncTask
            if (fragment instanceof EditorProfileListFragment) {
                if (((EditorProfileListFragment)fragment).isAsyncTaskPendingOrRunning()) {
                    ((EditorProfileListFragment)fragment).stopRunningAsyncTask();
                }
            }
            else
            if (fragment instanceof EditorEventListFragment) {
                if (((EditorEventListFragment)fragment).isAsyncTaskPendingOrRunning()) {
                    ((EditorEventListFragment)fragment).stopRunningAsyncTask();
                }
            }

            drawerSelectedItem = position;
            PPApplication.logE("EditorProfilesActivity.selectDrawerItem", "drawerSelectedItem="+drawerSelectedItem);

            // save into shared preferences
            ApplicationPreferences.getSharedPreferences(this);
            Editor editor = ApplicationPreferences.preferences.edit();
            editor.putInt(SP_EDITOR_DRAWER_SELECTED_ITEM, drawerSelectedItem);
            editor.apply();

            Bundle arguments;

            int profilesFilterType;
            int eventsFilterType;
            int eventsOrderType = getEventsOrderType();

            switch (drawerSelectedItem) {
            case DSI_PROFILES_ALL:
                profilesFilterType = EditorProfileListFragment.FILTER_TYPE_ALL;
                PPApplication.logE("EditorProfilesActivity.selectDrawerItem", "profilesFilterType=FILTER_TYPE_ALL");
                fragment = new EditorProfileListFragment();
                arguments = new Bundle();
                arguments.putInt(EditorProfileListFragment.FILTER_TYPE_ARGUMENT, profilesFilterType);
                arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment")
                    .commitAllowingStateLoss();
                if (removePreferences)
                    redrawProfilePreferences(null, EditorProfileListFragment.EDIT_MODE_EDIT, 0, startTargetHelps);
                break;
            case DSI_PROFILES_SHOW_IN_ACTIVATOR:
                profilesFilterType = EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR;
                PPApplication.logE("EditorProfilesActivity.selectDrawerItem", "profilesFilterType=FILTER_TYPE_SHOW_IN_ACTIVATOR");
                fragment = new EditorProfileListFragment();
                arguments = new Bundle();
                arguments.putInt(EditorProfileListFragment.FILTER_TYPE_ARGUMENT, profilesFilterType);
                arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment")
                    .commitAllowingStateLoss();
                if (removePreferences)
                    redrawProfilePreferences(null, EditorProfileListFragment.EDIT_MODE_EDIT, 0, startTargetHelps);
                break;
            case DSI_PROFILES_NO_SHOW_IN_ACTIVATOR:
                profilesFilterType = EditorProfileListFragment.FILTER_TYPE_NO_SHOW_IN_ACTIVATOR;
                PPApplication.logE("EditorProfilesActivity.selectDrawerItem", "profilesFilterType=FILTER_TYPE_NO_SHOW_IN_ACTIVATOR");
                fragment = new EditorProfileListFragment();
                arguments = new Bundle();
                arguments.putInt(EditorProfileListFragment.FILTER_TYPE_ARGUMENT, profilesFilterType);
                arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment")
                    .commitAllowingStateLoss();
                if (removePreferences)
                    redrawProfilePreferences(null, EditorProfileListFragment.EDIT_MODE_EDIT, 0, startTargetHelps);
                break;
            case DSI_EVENTS_START_ORDER:
                eventsFilterType = EditorEventListFragment.FILTER_TYPE_START_ORDER;
                PPApplication.logE("EditorProfilesActivity.selectDrawerItem", "eventsFilterType=FILTER_TYPE_START_ORDER");
                fragment = new EditorEventListFragment();
                arguments = new Bundle();
                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                arguments.putInt(EditorEventListFragment.ORDER_TYPE_ARGUMENT, EditorEventListFragment.ORDER_TYPE_START_ORDER);
                PPApplication.logE("EditorProfilesActivity.selectDrawerItem", "eventsOrderType=ORDER_TYPE_START_ORDER");
                arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                        .commitAllowingStateLoss();
                if (removePreferences)
                    redrawEventPreferences(null, EditorEventListFragment.EDIT_MODE_EDIT, 0, startTargetHelps);
                break;
            case DSI_EVENTS_ALL:
                eventsFilterType = EditorEventListFragment.FILTER_TYPE_ALL;
                PPApplication.logE("EditorProfilesActivity.selectDrawerItem", "eventsFilterType=FILTER_TYPE_ALL");
                fragment = new EditorEventListFragment();
                arguments = new Bundle();
                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                arguments.putInt(EditorEventListFragment.ORDER_TYPE_ARGUMENT, eventsOrderType);
                PPApplication.logE("EditorProfilesActivity.selectDrawerItem", "eventsOrderType="+eventsOrderType);
                arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                        .commitAllowingStateLoss();
                if (removePreferences)
                    redrawEventPreferences(null, EditorEventListFragment.EDIT_MODE_EDIT, 0, startTargetHelps);
                break;
            case DSI_EVENTS_RUNNING:
                eventsFilterType = EditorEventListFragment.FILTER_TYPE_RUNNING;
                PPApplication.logE("EditorProfilesActivity.selectDrawerItem", "eventsFilterType=FILTER_TYPE_RUNNING");
                fragment = new EditorEventListFragment();
                arguments = new Bundle();
                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                arguments.putInt(EditorEventListFragment.ORDER_TYPE_ARGUMENT, eventsOrderType);
                PPApplication.logE("EditorProfilesActivity.selectDrawerItem", "eventsOrderType="+eventsOrderType);
                arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                        .commitAllowingStateLoss();
                if (removePreferences)
                    redrawEventPreferences(null, EditorEventListFragment.EDIT_MODE_EDIT, 0, startTargetHelps);
                break;
            case DSI_EVENTS_PAUSED:
                eventsFilterType = EditorEventListFragment.FILTER_TYPE_PAUSED;
                PPApplication.logE("EditorProfilesActivity.selectDrawerItem", "eventsFilterType=FILTER_TYPE_PAUSED");
                fragment = new EditorEventListFragment();
                arguments = new Bundle();
                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                arguments.putInt(EditorEventListFragment.ORDER_TYPE_ARGUMENT, eventsOrderType);
                PPApplication.logE("EditorProfilesActivity.selectDrawerItem", "eventsOrderType="+eventsOrderType);
                arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                        .commitAllowingStateLoss();
                if (removePreferences)
                    redrawEventPreferences(null, EditorEventListFragment.EDIT_MODE_EDIT, 0, startTargetHelps);
                break;
            case DSI_EVENTS_STOPPED:
                eventsFilterType = EditorEventListFragment.FILTER_TYPE_STOPPED;
                PPApplication.logE("EditorProfilesActivity.selectDrawerItem", "eventsFilterType=FILTER_TYPE_STOPPED");
                fragment = new EditorEventListFragment();
                arguments = new Bundle();
                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                arguments.putInt(EditorEventListFragment.ORDER_TYPE_ARGUMENT, eventsOrderType);
                PPApplication.logE("EditorProfilesActivity.selectDrawerItem", "eventsOrderType="+eventsOrderType);
                arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                        .commitAllowingStateLoss();
                if (removePreferences)
                    redrawEventPreferences(null, EditorEventListFragment.EDIT_MODE_EDIT, 0, startTargetHelps);
                break;
            }
        }

        // header is position=0
        drawerListView.setItemChecked(drawerSelectedItem, true);
        // Get the title and icon followed by the position
        //editorToolbar.setSubtitle(drawerItemsTitle[drawerSelectedItem - 1]);
        //setIcon(drawerItemsIcon[drawerSelectedItem-1]);
        drawerHeaderFilterImage.setImageResource(drawerItemsIcon[drawerSelectedItem -1]);
        drawerHeaderFilterTitle.setText(drawerItemsTitle[drawerSelectedItem - 1]);

        // set filter status bar title
        setStatusBarTitle();
        
        
        // Close drawer
        if (ApplicationPreferences.applicationEditorAutoCloseDrawer(getApplicationContext()) && (!orientationChange))
            drawerLayout.closeDrawer(drawerRoot);
    }
    
    private void changeEventOrder(int position, boolean orientationChange) {
        orderSelectedItem = position;

        if (drawerSelectedItem != DSI_EVENTS_START_ORDER) {
            // save into shared preferences
            ApplicationPreferences.getSharedPreferences(this);
            Editor editor = ApplicationPreferences.preferences.edit();
            editor.putInt(SP_EDITOR_ORDER_SELECTED_ITEM, orderSelectedItem);
            editor.apply();
        }

        int _eventsOrderType = getEventsOrderType();
        setStatusBarTitle();

        PPApplication.logE("EditorProfilesActivity.changeEventOrder", "drawerSelectedItem="+drawerSelectedItem);
        PPApplication.logE("EditorProfilesActivity.changeEventOrder", "orderSelectedItem="+orderSelectedItem);
        PPApplication.logE("EditorProfilesActivity.changeEventOrder", "_eventsOrderType="+_eventsOrderType);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
        if ((fragment instanceof EditorEventListFragment))
        {
            ((EditorEventListFragment)fragment).changeListOrder(_eventsOrderType);
        }

        //if (drawerSelectedItem != DSI_EVENTS_START_ORDER)
            orderSpinner.setSelection(orderSelectedItem);

        // Close drawer
        if (ApplicationPreferences.applicationEditorAutoCloseDrawer(getApplicationContext()) && (!orientationChange))
            drawerLayout.closeDrawer(drawerRoot);

    }

    private int getEventsOrderType() {
        int _eventsOrderType;
        if (drawerSelectedItem == DSI_EVENTS_START_ORDER) {
            _eventsOrderType = EditorEventListFragment.ORDER_TYPE_START_ORDER;
        } else {
            _eventsOrderType = EditorEventListFragment.ORDER_TYPE_START_ORDER;
            switch (orderSelectedItem) {
                /*case 0:
                    _eventsOrderType = EditorEventListFragment.ORDER_TYPE_START_ORDER;
                    break;*/
                case 1:
                    _eventsOrderType = EditorEventListFragment.ORDER_TYPE_EVENT_NAME;
                    break;
                case 2:
                    _eventsOrderType = EditorEventListFragment.ORDER_TYPE_PROFILE_NAME;
                    break;
                case 3:
                    _eventsOrderType = EditorEventListFragment.ORDER_TYPE_PRIORITY;
                    break;
            }
        }
        return _eventsOrderType;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE_ACTIVATE_PROFILE)
        {
            EditorProfileListFragment fragment = (EditorProfileListFragment)getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
            if (fragment != null)
                fragment.doOnActivityResult(requestCode, resultCode, data);
        }
        else
        if (requestCode == REQUEST_CODE_PROFILE_PREFERENCES)
        {
            if ((resultCode == RESULT_OK) && (data != null))
            {
                long profile_id = data.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                int newProfileMode = data.getIntExtra(EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
                int predefinedProfileIndex = data.getIntExtra(EXTRA_PREDEFINED_PROFILE_INDEX, 0);

                if (profile_id > 0)
                {
                    //noinspection ConstantConditions
                    Profile profile = DatabaseHandler.getInstance(getApplicationContext()).getProfile(profile_id, false);
                    if (profile != null) {
                        // generate bitmaps
                        profile.generateIconBitmap(getBaseContext(), false, 0, false);
                        profile.generatePreferencesIndicator(getBaseContext(), false, 0);

                        // redraw list fragment , notifications, widgets after finish ProfilePreferencesActivity
                        redrawProfileListFragment(profile, newProfileMode, predefinedProfileIndex);

                        Profile mappedProfile = Profile.getMappedProfile(profile, getApplicationContext());
                        Permissions.grantProfilePermissions(getApplicationContext(), mappedProfile, false, false,
                                /*true, false, 0,*/ PPApplication.STARTUP_SOURCE_EDITOR, false, true, false);
                    }
                }
                else
                if (profile_id == Profile.SHARED_PROFILE_ID)
                {
                    // refresh activity for changes of shared profile
                    GlobalGUIRoutines.reloadActivity(this, false);

                    Profile sharedProfile = Profile.getProfileFromSharedPreferences(getApplicationContext(), PPApplication.SHARED_PROFILE_PREFS_NAME);
                    Permissions.grantProfilePermissions(getApplicationContext(), sharedProfile, false, false,
                            /*true, false, 0,*/ PPApplication.STARTUP_SOURCE_EDITOR, false, true, false);
                }

                /*Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_JOBS, true);
                PPApplication.startPPService(this, serviceIntent);*/
                Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
                //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_JOBS, true);
                PPApplication.runCommand(this, commandIntent);
            }
            else
            if (data != null) {
                boolean restart = data.getBooleanExtra(PhoneProfilesPreferencesActivity.EXTRA_RESET_EDITOR, false);
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
                int predefinedEventIndex = data.getIntExtra(EXTRA_PREDEFINED_EVENT_INDEX, 0);

                if (event_id > 0)
                {
                    //noinspection ConstantConditions
                    Event event = DatabaseHandler.getInstance(getApplicationContext()).getEvent(event_id);

                    // redraw list fragment , notifications, widgets after finish EventPreferencesActivity
                    redrawEventListFragment(event, newEventMode, predefinedEventIndex);

                    Permissions.grantEventPermissions(getApplicationContext(), event, false, false);
                }

                /*Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_JOBS, true);
                PPApplication.startPPService(this, serviceIntent);*/
                Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
                //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_JOBS, true);
                PPApplication.runCommand(this, commandIntent);
            }
            else
            if (data != null) {
                boolean restart = data.getBooleanExtra(PhoneProfilesPreferencesActivity.EXTRA_RESET_EDITOR, false);
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
                /*Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_JOBS, true);
                PPApplication.startPPService(this, serviceIntent);*/
                Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
                //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_JOBS, true);
                PPApplication.runCommand(this, commandIntent);

                //if (PhoneProfilesService.getInstance() != null) {
                    /*
                    boolean powerSaveMode = PPApplication.isPowerSaveMode;
                    if ((PhoneProfilesService.isGeofenceScannerStarted())) {
                        PhoneProfilesService.getGeofencesScanner().resetLocationUpdates(powerSaveMode, true);
                    }
                    PhoneProfilesService.getInstance().resetListeningOrientationSensors(powerSaveMode, true);
                    if (PhoneProfilesService.isPhoneStateScannerStarted())
                        PhoneProfilesService.phoneStateScanner.resetListening(powerSaveMode, true);
                    */
                //}

                boolean restart = data.getBooleanExtra(PhoneProfilesPreferencesActivity.EXTRA_RESET_EDITOR, false);

                if (restart)
                {
                    // refresh activity for special changes
                    GlobalGUIRoutines.reloadActivity(this, true);
                }
            }
        }
        else
        if (requestCode == REQUEST_CODE_REMOTE_EXPORT)
        {
            if (resultCode == RESULT_OK)
            {
                doImportData(GlobalGUIRoutines.REMOTE_EXPORT_PATH);
            }
        }
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
                doExportData();
            }
        }
        else
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_IMPORT) {
            if ((resultCode == RESULT_OK) && (data != null)) {
                doImportData(data.getStringExtra(Permissions.EXTRA_APPLICATION_DATA_PATH));
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        if (drawerLayout.isDrawerOpen(drawerRoot))
            drawerLayout.closeDrawer(drawerRoot);
        else
            super.onBackPressed();
    }

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
        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                if (positive != null) positive.setAllCaps(false);
                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                if (negative != null) negative.setAllCaps(false);
            }
        });*/
        if (!isFinishing())
            dialog.show();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean importApplicationPreferences(File src, int what) {
        boolean res = true;
        ObjectInputStream input = null;
        try {
            try {
                input = new ObjectInputStream(new FileInputStream(src));
                Editor prefEdit;
                if (what == 1)
                    prefEdit = getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE).edit();
                else
                    prefEdit = getSharedPreferences(PPApplication.SHARED_PROFILE_PREFS_NAME, Activity.MODE_PRIVATE).edit();
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

                    if (what == 1)
                    {
                        if (key.equals(ApplicationPreferences.PREF_APPLICATION_THEME))
                        {
                            if (v.equals("light"))
                                prefEdit.putString(key, "white");
                            if (v.equals("material"))
                                prefEdit.putString(key, "color");
                        }
                        if (key.equals(ActivateProfileHelper.PREF_MERGED_RING_NOTIFICATION_VOLUMES))
                            ActivateProfileHelper.setMergedRingNotificationVolumes(getApplicationContext(), true, prefEdit);
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
                    PPApplication.setSavedVersionCode(getApplicationContext(), 0);
                }
            }/* catch (FileNotFoundException ignored) {
                // no error, this is OK
            }*/ catch (Exception e) {
                Log.e("EditorProfilesActivity.importApplicationPreferences", Log.getStackTraceString(e));
                res = false;
            }
        }finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ignored) {
            }

            WifiScanJob.setScanRequest(getApplicationContext(), false);
            WifiScanJob.setWaitForResults(getApplicationContext(), false);
            WifiScanJob.setWifiEnabledForScan(getApplicationContext(), false);

            BluetoothScanJob.setScanRequest(getApplicationContext(), false);
            BluetoothScanJob.setLEScanRequest(getApplicationContext(), false);
            BluetoothScanJob.setWaitForResults(getApplicationContext(), false);
            BluetoothScanJob.setWaitForLEResults(getApplicationContext(), false);
            BluetoothScanJob.setBluetoothEnabledForScan(getApplicationContext(), false);

        }
        return res;
    }

    private void doImportData(String applicationDataPath)
    {
        final EditorProfilesActivity activity = this;
        final String _applicationDataPath = applicationDataPath;

        if (Permissions.grantImportPermissions(activity.getApplicationContext(), activity, applicationDataPath)) {

            @SuppressLint("StaticFieldLeak")
            class ImportAsyncTask extends AsyncTask<Void, Integer, Integer> {
                private DataWrapper dataWrapper;
                private int dbError = DatabaseHandler.IMPORT_OK;
                private boolean appSettingsError = false;
                private boolean sharedProfileError = false;

                private ImportAsyncTask() {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                    dialogBuilder.setMessage(R.string.import_profiles_alert_title);

                    LayoutInflater inflater = (activity.getLayoutInflater());
                    @SuppressLint("InflateParams")
                    View layout = inflater.inflate(R.layout.activity_progress_bar_dialog, null);
                    dialogBuilder.setView(layout);

                    importProgressDialog = dialogBuilder.create();

                    this.dataWrapper = getDataWrapper();
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    doImport = true;

                    GlobalGUIRoutines.lockScreenOrientation(activity);
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

                @Override
                protected Integer doInBackground(Void... params) {
                    PPApplication.exitApp(false, dataWrapper.context, dataWrapper, null, false/*, false, true*/);

                    dbError = DatabaseHandler.getInstance(this.dataWrapper.context).importDB(_applicationDataPath);
                    if (dbError == DatabaseHandler.IMPORT_OK) {
                        DatabaseHandler.getInstance(this.dataWrapper.context).updateAllEventsStatus(Event.ESTATUS_RUNNING, Event.ESTATUS_PAUSE);
                        DatabaseHandler.getInstance(this.dataWrapper.context).updateAllEventsSensorsPassed(EventPreferences.SENSOR_PASSED_WAITING);
                        DatabaseHandler.getInstance(this.dataWrapper.context).deactivateProfile();
                        DatabaseHandler.getInstance(this.dataWrapper.context).unblockAllEvents();
                        DatabaseHandler.getInstance(this.dataWrapper.context).disableNotAllowedPreferences();
                        this.dataWrapper.invalidateProfileList();
                        this.dataWrapper.invalidateEventList();
                        Event.setEventsBlocked(getApplicationContext(), false);
                        DatabaseHandler.getInstance(this.dataWrapper.context).unblockAllEvents();
                        Event.setForceRunEventRunning(getApplicationContext(), false);
                    }

                    File sd = Environment.getExternalStorageDirectory();
                    File exportFile = new File(sd, _applicationDataPath + "/" + GlobalGUIRoutines.EXPORT_APP_PREF_FILENAME);
                    appSettingsError = !importApplicationPreferences(exportFile, 1);
                    exportFile = new File(sd, _applicationDataPath + "/" + GlobalGUIRoutines.EXPORT_DEF_PROFILE_PREF_FILENAME);
                    sharedProfileError = !importApplicationPreferences(exportFile, 2);

                    PPApplication.logE("EditorProfilesActivity.doImportData", "dbError="+dbError);
                    PPApplication.logE("EditorProfilesActivity.doImportData", "appSettingsError="+appSettingsError);
                    PPApplication.logE("EditorProfilesActivity.doImportData", "sharedProfileError="+sharedProfileError);

                    if (!appSettingsError) {
                        ApplicationPreferences.getSharedPreferences(dataWrapper.context);
                        Editor editor = ApplicationPreferences.preferences.edit();
                        editor.putInt(SP_EDITOR_DRAWER_SELECTED_ITEM, 1);
                        editor.putInt(SP_EDITOR_ORDER_SELECTED_ITEM, 0);
                        editor.apply();

                        Permissions.setAllShowRequestPermissions(getApplicationContext(), true);

                        //WifiBluetoothScanner.setShowEnableLocationNotification(getApplicationContext(), true, WifiBluetoothScanner.SCANNER_TYPE_WIFI);
                        //WifiBluetoothScanner.setShowEnableLocationNotification(getApplicationContext(), true, WifiBluetoothScanner.SCANNER_TYPE_BLUETOOTH);
                        //PhoneStateScanner.setShowEnableLocationNotification(getApplicationContext(), true);
                    }

                    if ((dbError == DatabaseHandler.IMPORT_OK) && (!(appSettingsError || sharedProfileError)))
                        return 1;
                    else
                        return 0;
                }

                @Override
                protected void onPostExecute(Integer result) {
                    super.onPostExecute(result);

                    doImport = false;

                    if ((importProgressDialog != null) && importProgressDialog.isShowing()) {
                        importProgressDialog.dismiss();
                        importProgressDialog = null;
                    }
                    if (!isFinishing())
                        GlobalGUIRoutines.unlockScreenOrientation(activity);

                    PPApplication.logE("DataWrapper.updateNotificationAndWidgets", "from EditorProfilesActivity.doImportData");
                    this.dataWrapper.updateNotificationAndWidgets(true);

                    PPApplication.setApplicationStarted(this.dataWrapper.context, true);
                    Intent serviceIntent = new Intent(this.dataWrapper.context, PhoneProfilesService.class);
                    //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_INITIALIZE_START, true);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
                    PPApplication.startPPService(activity, serviceIntent);

                    if ((dbError == DatabaseHandler.IMPORT_OK) && (!(appSettingsError || sharedProfileError))) {
                        PPApplication.logE("EditorProfilesActivity.doImportData", "restore is ok");

                        // restart events
                        //if (Event.getGlobalEventsRunning(this.dataWrapper.context)) {
                        //    this.dataWrapper.restartEventsWithDelay(3, false, false, DatabaseHandler.ALTYPE_UNDEFINED);
                        //}

                        this.dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_DATAIMPORT, null, null, null, 0);

                        // toast notification
                        Toast msg = ToastCompat.makeText(this.dataWrapper.context.getApplicationContext(),
                                getResources().getString(R.string.toast_import_ok),
                                Toast.LENGTH_SHORT);
                        msg.show();

                        // refresh activity
                        if (!isFinishing())
                            GlobalGUIRoutines.reloadActivity(activity, true);
                    } else {
                        PPApplication.logE("EditorProfilesActivity.doImportData", "error restore");

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

    private void importDataAlert(/*boolean remoteExport*/)
    {
        //final boolean _remoteExport = remoteExport;

        AlertDialog.Builder dialogBuilder2 = new AlertDialog.Builder(this);
        /*if (remoteExport)
        {
            dialogBuilder2.setTitle(R.string.import_profiles_from_phoneprofiles_alert_title2);
            dialogBuilder2.setMessage(R.string.import_profiles_alert_message);
            //dialogBuilder2.setIcon(android.R.drawable.ic_dialog_alert);
        }
        else
        {*/
            dialogBuilder2.setTitle(R.string.import_profiles_alert_title);
            dialogBuilder2.setMessage(R.string.import_profiles_alert_message);
            //dialogBuilder2.setIcon(android.R.drawable.ic_dialog_alert);
        //}

        dialogBuilder2.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                /*if (_remoteExport)
                {
                    // start RemoteExportDataActivity
                    Intent intent = new Intent("phoneprofiles.intent.action.EXPORTDATA");

                    final PackageManager packageManager = getPackageManager();
                    List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    if (list.size() > 0)
                        startActivityForResult(intent, REQUEST_CODE_REMOTE_EXPORT);
                    else
                        importExportErrorDialog(1);
                }
                else*/
                    doImportData(PPApplication.EXPORT_PATH);
            }
        });
        dialogBuilder2.setNegativeButton(R.string.alert_button_no, null);
        AlertDialog dialog = dialogBuilder2.create();
        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                if (positive != null) positive.setAllCaps(false);
                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                if (negative != null) negative.setAllCaps(false);
            }
        });*/
        if (!isFinishing())
            dialog.show();
    }

    private void importData()
    {
        /*// test whether the PhoneProfile is installed
        PackageManager packageManager = getApplicationContext().getPackageManager();
        Intent phoneProfiles = packageManager.getLaunchIntentForPackage("sk.henrichg.phoneprofiles");
        if (phoneProfiles != null)
        {
            // PhoneProfiles is installed

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(R.string.import_profiles_from_phoneprofiles_alert_title);
            dialogBuilder.setMessage(R.string.import_profiles_from_phoneprofiles_alert_message);
            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

            dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    importDataAlert(true);
                }
            });
            dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    importDataAlert(false);
                }
            });
            AlertDialog dialog = dialogBuilder.create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    if (positive != null) positive.setAllCaps(false);
                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                    if (negative != null) negative.setAllCaps(false);
                }
            });
            dialog.show();
        }
        else*/
            importDataAlert();
    }

    @SuppressLint("ApplySharedPref")
    private boolean exportApplicationPreferences(File dst, int what) {
        boolean res = true;
        ObjectOutputStream output = null;
        try {
            try {
                output = new ObjectOutputStream(new FileOutputStream(dst));
                SharedPreferences pref;
                if (what == 1)
                    pref = getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                else
                    pref = getSharedPreferences(PPApplication.SHARED_PROFILE_PREFS_NAME, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.commit();
                output.writeObject(pref.getAll());
            } catch (FileNotFoundException ignored) {
                // this is OK
            } catch (IOException e) {
                res = false;
            }
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException ignored) {
            }
        }
        return res;
    }

    private void exportData()
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.export_profiles_alert_title);
        dialogBuilder.setMessage(getString(R.string.export_profiles_alert_message) + " \"" + PPApplication.EXPORT_PATH + "\".\n\n" +
                                 getString(R.string.export_profiles_alert_message_note));
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

        dialogBuilder.setPositiveButton(R.string.alert_button_backup, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                doExportData();
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = dialogBuilder.create();
        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                if (positive != null) positive.setAllCaps(false);
                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                if (negative != null) negative.setAllCaps(false);
            }
        });*/
        if (!isFinishing())
            dialog.show();
    }

    private void doExportData()
    {
        final EditorProfilesActivity activity = this;

        if (Permissions.grantExportPermissions(activity.getApplicationContext(), activity)) {

            @SuppressLint("StaticFieldLeak")
            class ExportAsyncTask extends AsyncTask<Void, Integer, Integer> {
                private DataWrapper dataWrapper;

                private ExportAsyncTask() {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                    dialogBuilder.setMessage(R.string.export_profiles_alert_title);

                    LayoutInflater inflater = (activity.getLayoutInflater());
                    @SuppressLint("InflateParams")
                    View layout = inflater.inflate(R.layout.activity_progress_bar_dialog, null);
                    dialogBuilder.setView(layout);

                    exportProgressDialog = dialogBuilder.create();

                    this.dataWrapper = getDataWrapper();
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    GlobalGUIRoutines.lockScreenOrientation(activity);
                    exportProgressDialog.setCancelable(false);
                    exportProgressDialog.setCanceledOnTouchOutside(false);
                    if (!activity.isFinishing())
                        exportProgressDialog.show();
                }

                @Override
                protected Integer doInBackground(Void... params) {

                    int ret = DatabaseHandler.getInstance(this.dataWrapper.context).exportDB();
                    if (ret == 1) {
                        File sd = Environment.getExternalStorageDirectory();
                        File exportFile = new File(sd, PPApplication.EXPORT_PATH + "/" + GlobalGUIRoutines.EXPORT_APP_PREF_FILENAME);
                        if (exportApplicationPreferences(exportFile, 1)) {
                            exportFile = new File(sd, PPApplication.EXPORT_PATH + "/" + GlobalGUIRoutines.EXPORT_DEF_PROFILE_PREF_FILENAME);
                            if (!exportApplicationPreferences(exportFile, 2))
                                ret = 0;
                        }
                        else
                            ret = 0;
                    }

                    return ret;
                }

                @Override
                protected void onPostExecute(Integer result) {
                    super.onPostExecute(result);

                    if ((exportProgressDialog != null) && exportProgressDialog.isShowing()) {
                        exportProgressDialog.dismiss();
                        exportProgressDialog = null;
                    }
                    if (!isFinishing())
                        GlobalGUIRoutines.unlockScreenOrientation(activity);

                    if (result == 1) {

                        // toast notification
                        Toast msg = ToastCompat.makeText(this.dataWrapper.context.getApplicationContext(),
                                getResources().getString(R.string.toast_export_ok),
                                Toast.LENGTH_SHORT);
                        msg.show();

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
        drawerToggle.syncState();
    }
 
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        savedInstanceStateChanged = true;

        if (mTwoPane) {
            ApplicationPreferences.getSharedPreferences(this);

            if (drawerSelectedItem <= COUNT_DRAWER_PROFILE_ITEMS)
            {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment fragment = fragmentManager.findFragmentByTag("ProfileDetailsFragment");
                if (fragment != null)
                {
                    Editor editor = ApplicationPreferences.preferences.edit();
                    editor.putLong(SP_DATA_DETAILS_DATA_ID, ((ProfileDetailsFragment) fragment).profile_id);
                    //editor.putInt(SP_DATA_DETAILS_EDIT_MODE, ((ProfileDetailsFragment) fragment).editMode);
                    //editor.putInt(SP_DATA_DETAILS_PREDEFINED_PROFILE_INDEX, ((ProfileDetailsFragment) fragment).predefinedProfileIndex);
                    editor.apply();
                }
            }
            else
            {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment fragment = fragmentManager.findFragmentByTag("EventDetailsFragment");
                if (fragment != null)
                {
                    Editor editor = ApplicationPreferences.preferences.edit();
                    editor.putLong(SP_DATA_DETAILS_DATA_ID, ((EventDetailsFragment) fragment).event_id);
                    //editor.putInt(SP_DATA_DETAILS_EDIT_MODE, ((EventDetailsFragment) fragment).editMode);
                    //editor.putInt(SP_DATA_DETAILS_PREDEFINED_EVENT_INDEX, ((EventDetailsFragment) fragment).predefinedEventIndex);
                    editor.apply();
                }
            }
        }
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

     @SuppressLint("SetTextI18n")
     private void setStatusBarTitle()
     {
        // set filter status bar title
        /*String text = "";
        if (drawerSelectedItem <= COUNT_DRAWER_PROFILE_ITEMS)
        {
            text = drawerItemsSubtitle[drawerSelectedItem-1];
        }
        else
        {
            String[] orderItems = getResources().getStringArray(R.array.drawerOrderEvents);
            text = drawerItemsSubtitle[drawerSelectedItem-1] +
                    "; " +
                    orderItems[orderSelectedItem];
        }*/
        String text = drawerItemsSubtitle[drawerSelectedItem-1];
        filterStatusBarTitle.setText(drawerItemsTitle[drawerSelectedItem - 1] + " - " + text);
        drawerHeaderFilterSubtitle.setText(text);
     }

    private void startProfilePreferenceActivity(Profile profile, int editMode, int predefinedProfileIndex) {
        Intent intent = new Intent(getBaseContext(), ProfilePreferencesActivity.class);
        if (editMode == EditorProfileListFragment.EDIT_MODE_INSERT)
            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, 0L);
        else
            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
        intent.putExtra(EXTRA_NEW_PROFILE_MODE, editMode);
        intent.putExtra(EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
        startActivityForResult(intent, REQUEST_CODE_PROFILE_PREFERENCES);
    }

    public void onStartProfilePreferences(Profile profile, int editMode, int predefinedProfileIndex/*, boolean startTargetHelps*/) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.

            if ((editMode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
                (editMode == EditorProfileListFragment.EDIT_MODE_DUPLICATE)) {
                startProfilePreferenceActivity(profile, editMode, predefinedProfileIndex);
            }
            else
            if (profile != null)
            {
                Bundle arguments = new Bundle();
                arguments.putLong(PPApplication.EXTRA_PROFILE_ID, profile._id);
                arguments.putInt(EXTRA_NEW_PROFILE_MODE, editMode);
                arguments.putInt(EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
                arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, true/*startTargetHelps*/);
                ProfileDetailsFragment fragment = new ProfileDetailsFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.editor_detail_container, fragment, "ProfileDetailsFragment").commit();
            }
            else
            {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_detail_container);
                if (fragment != null)
                {
                    getSupportFragmentManager().beginTransaction()
                        .remove(fragment).commit();
                }
            }

        } else {
            // In single-pane mode, simply start the profile preferences activity
            // for the profile position.
            if (((profile != null) ||
                (editMode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
                (editMode == EditorProfileListFragment.EDIT_MODE_DUPLICATE))
                && (editMode != EditorProfileListFragment.EDIT_MODE_DELETE))
                startProfilePreferenceActivity(profile, editMode, predefinedProfileIndex);
        }
    }

    @Override
    public void onStartProfilePreferencesFromDetail(Profile profile) {
        startProfilePreferenceActivity(profile, EditorProfileListFragment.EDIT_MODE_EDIT, 0);
    }

    private void redrawProfilePreferences(Profile profile, int newProfileMode, int predefinedProfileIndex, boolean startTargetHelps) {
        if (mTwoPane) {
            if (profile != null)
            {
                // restart profile preferences fragment
                Bundle arguments = new Bundle();
                arguments.putLong(PPApplication.EXTRA_PROFILE_ID, profile._id);
                arguments.putInt(EXTRA_NEW_PROFILE_MODE, newProfileMode);
                arguments.putInt(EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
                arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                ProfileDetailsFragment fragment = new ProfileDetailsFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.editor_detail_container, fragment, "ProfileDetailsFragment").commit();
            }
            else
            {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_detail_container);
                if (fragment != null)
                {
                    getSupportFragmentManager().beginTransaction()
                        .remove(fragment).commit();
                }
            }
        }
    }

    private void redrawProfileListFragment(Profile profile, int newProfileMode, int predefinedProfileIndex/*, boolean startTargetHelps*/) {
        // redraw list fragment, notification a widgets

        final EditorProfileListFragment fragment = (EditorProfileListFragment) getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
        if (fragment != null) {
            // update profile, this rewrite profile in profileList
            fragment.activityDataWrapper.updateProfile(profile);

            boolean newProfile = ((newProfileMode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
                    (newProfileMode == EditorProfileListFragment.EDIT_MODE_DUPLICATE));
            fragment.updateListView(profile, newProfile, false, false, 0);

            Profile activeProfile = fragment.activityDataWrapper.getActivatedProfile(true,
                        ApplicationPreferences.applicationEditorPrefIndicator(fragment.activityDataWrapper.context));
            fragment.updateHeader(activeProfile);
            PPApplication.showProfileNotification(/*getApplicationContext()*/true);
            PPApplication.logE("ActivateProfileHelper.updateGUI", "from EditorProfilesActivity.redrawProfileListFragment");
            ActivateProfileHelper.updateGUI(fragment.activityDataWrapper.context, true, true);

            fragment.activityDataWrapper.setDynamicLauncherShortcutsFromMainThread();
        }
        redrawProfilePreferences(profile, newProfileMode, predefinedProfileIndex, true/*startTargetHelps*/);
    }

    private void startEventPreferenceActivity(Event event, int editMode, int predefinedEventIndex) {
        Intent intent = new Intent(getBaseContext(), EventPreferencesActivity.class);
        if (editMode == EditorEventListFragment.EDIT_MODE_INSERT)
            intent.putExtra(PPApplication.EXTRA_EVENT_ID, 0L);
        else {
            intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);
            intent.putExtra(PPApplication.EXTRA_EVENT_STATUS, event.getStatus());
        }
        intent.putExtra(EXTRA_NEW_EVENT_MODE, editMode);
        intent.putExtra(EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
        startActivityForResult(intent, REQUEST_CODE_EVENT_PREFERENCES);
    }

    public void onStartEventPreferences(Event event, int editMode, int predefinedEventIndex/*, boolean startTargetHelps*/) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.

            if ((editMode == EditorEventListFragment.EDIT_MODE_INSERT) ||
                (editMode == EditorEventListFragment.EDIT_MODE_DUPLICATE)) {
                startEventPreferenceActivity(event, editMode, predefinedEventIndex);
            }
            else
            if (event != null)
            {
                Bundle arguments = new Bundle();
                arguments.putLong(PPApplication.EXTRA_EVENT_ID, event._id);
                arguments.putInt(EXTRA_NEW_EVENT_MODE, editMode);
                arguments.putInt(EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
                arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, true/*startTargetHelps*/);
                EventDetailsFragment fragment = new EventDetailsFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.editor_detail_container, fragment, "EventDetailsFragment").commit();
            }
            else
            {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_detail_container);
                if (fragment != null)
                {
                    getSupportFragmentManager().beginTransaction()
                        .remove(fragment).commit();
                }
            }

        } else {
            // In single-pane mode, simply start the profile preferences activity
            // for the event id.
            if (((event != null) ||
                (editMode == EditorEventListFragment.EDIT_MODE_INSERT) ||
                (editMode == EditorEventListFragment.EDIT_MODE_DUPLICATE))
                && (editMode != EditorEventListFragment.EDIT_MODE_DELETE))
                startEventPreferenceActivity(event, editMode, predefinedEventIndex);
        }
    }

    @Override
    public void onStartEventPreferencesFromDetail(Event event) {
        startEventPreferenceActivity(event, EditorEventListFragment.EDIT_MODE_EDIT, 0);
    }

    private void redrawEventListFragment(Event event, int newEventMode, int predefinedEventIndex/*, boolean startTargetHelps*/) {
        // redraw list fragment, notification and widgets
        EditorEventListFragment fragment = (EditorEventListFragment) getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
        if (fragment != null) {
            // update event, this rewrite event in eventList
            fragment.activityDataWrapper.updateEvent(event);

            boolean newEvent = ((newEventMode == EditorEventListFragment.EDIT_MODE_INSERT) ||
                    (newEventMode == EditorEventListFragment.EDIT_MODE_DUPLICATE));
            fragment.updateListView(event, newEvent, false, false, 0);

            Profile activeProfile = fragment.activityDataWrapper.getActivatedProfileFromDB(true,
                    ApplicationPreferences.applicationEditorPrefIndicator(fragment.activityDataWrapper.context));
            fragment.updateHeader(activeProfile);
        }
        redrawEventPreferences(event, newEventMode, predefinedEventIndex, true/*startTargetHelps*/);
    }

    private void redrawEventPreferences(Event event, int newEventMode, int predefinedEventIndex, boolean startTargetHelps) {
        if (mTwoPane) {
            if (event != null)
            {
                // restart event preferences fragment
                Bundle arguments = new Bundle();
                arguments.putLong(PPApplication.EXTRA_EVENT_ID, event._id);
                arguments.putInt(EXTRA_NEW_EVENT_MODE, newEventMode);
                arguments.putInt(EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
                arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                EventDetailsFragment fragment = new EventDetailsFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.editor_detail_container, fragment, "EventDetailsFragment").commit();
            }
            else
            {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_detail_container);
                if (fragment != null)
                {
                    getSupportFragmentManager().beginTransaction()
                        .remove(fragment).commit();
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

    public static void createContactsCache()
    {
        if ((!savedInstanceStateChanged) || (contactsCache == null))
        {
            if (contactsCache != null)
                contactsCache.clearCache(true);
            contactsCache =  new ContactsCache();
        }
    }

    public static ContactsCache getContactsCache()
    {
        return contactsCache;
    }

    public static void createContactGroupsCache()
    {
        if ((!savedInstanceStateChanged) || (contactGroupsCache == null))
        {
            if (contactGroupsCache != null)
                contactGroupsCache.clearCache(true);
            contactGroupsCache =  new ContactGroupsCache();
        }
    }

    public static ContactGroupsCache getContactGroupsCache()
    {
        return contactGroupsCache;
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
        boolean whiteTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), true).equals("white");
        if (Event.getGlobalEventsRunning(getApplicationContext()))
        {
            if (Event.getEventsBlocked(getApplicationContext())) {
                if (whiteTheme)
                    eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_manual_activation_white);
                else
                    eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_manual_activation);
            }
            else {
                if (whiteTheme)
                    eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_running_white);
                else
                    eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_running);
            }
        }
        else {
            if (whiteTheme)
                eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_stoppped_white);
            else
                eventsRunStopIndicator.setImageResource(R.drawable.ic_run_events_indicator_stopped);
        }
    }

    public void refreshGUI(final boolean refresh, final boolean refreshIcons, final boolean setPosition, final long profileId, final long eventId)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (doImport)
                    return;

                setEventsRunStopIndicator();
                invalidateOptionsMenu();

                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                if (fragment != null) {
                    if (fragment instanceof EditorProfileListFragment)
                        ((EditorProfileListFragment) fragment).refreshGUI(refresh, refreshIcons, setPosition, profileId);
                    else
                        ((EditorEventListFragment) fragment).refreshGUI(refresh, refreshIcons, setPosition, eventId);
                }
            }
        });
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

        ApplicationPreferences.getSharedPreferences(this);

        boolean startTargetHelps = ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true);

        if (startTargetHelps ||
                ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS_DEFAULT_PROFILE, true) ||
                ApplicationPreferences.preferences.getBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, true) ||
                ApplicationPreferences.preferences.getBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, true) ||
                ApplicationPreferences.preferences.getBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, true) ||
                ApplicationPreferences.preferences.getBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS, true) ||
                ApplicationPreferences.preferences.getBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, true) ||
                ApplicationPreferences.preferences.getBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, true)) {

            //Log.d("EditorProfilesActivity.showTargetHelps", "PREF_START_TARGET_HELPS_ORDER=true");

            if (startTargetHelps) {
                //Log.d("EditorProfilesActivity.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                Editor editor = ApplicationPreferences.preferences.edit();
                editor.putBoolean(PREF_START_TARGET_HELPS, false);
                editor.apply();

                //TypedValue tv = new TypedValue();
                //getTheme().resolveAttribute(R.attr.colorAccent, tv, true);

                //final Display display = getWindowManager().getDefaultDisplay();

                String appTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), true);
                int circleColor = R.color.tabTargetHelpCircleColor;
                if (appTheme.equals("dark"))
                    circleColor = R.color.tabTargetHelpCircleColor_dark;
                int textColor = R.color.tabTargetHelpTextColor;
                if (appTheme.equals("white"))
                    textColor = R.color.tabTargetHelpTextColor_white;
                boolean tintTarget = !appTheme.equals("white");

                final TapTargetSequence sequence = new TapTargetSequence(this);
                if (Event.getGlobalEventsRunning(getApplicationContext())) {
                    List<TapTarget> targets = new ArrayList<>();
                    targets.add(
                        TapTarget.forToolbarNavigationIcon(editorToolbar, getString(R.string.editor_activity_targetHelps_navigationIcon_title), getString(R.string.editor_activity_targetHelps_navigationIcon_description))
                                .targetCircleColor(circleColor)
                                .textColor(textColor)
                                .tintTarget(tintTarget)
                                .drawShadow(true)
                                .id(1)
                    );
                    targets.add(
                        TapTarget.forToolbarOverflow(editorToolbar, getString(R.string.editor_activity_targetHelps_applicationMenu_title), getString(R.string.editor_activity_targetHelps_applicationMenu_description))
                                .targetCircleColor(circleColor)
                                .textColor(textColor)
                                .tintTarget(tintTarget)
                                .drawShadow(true)
                                .id(2)
                    );

                    int id = 3;
                    try {
                        targets.add(
                                TapTarget.forToolbarMenuItem(editorToolbar, R.id.menu_restart_events, getString(R.string.editor_activity_targetHelps_restartEvents_title), getString(R.string.editor_activity_targetHelps_restartEvents_description))
                                        .targetCircleColor(circleColor)
                                        .textColor(textColor)
                                        .tintTarget(tintTarget)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                    } catch (Exception ignored) {} // not in action bar?
                    try {
                        targets.add(
                            TapTarget.forToolbarMenuItem(editorToolbar, R.id.menu_activity_log, getString(R.string.editor_activity_targetHelps_activityLog_title), getString(R.string.editor_activity_targetHelps_activityLog_description))
                                    .targetCircleColor(circleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(id)
                        );
                        ++id;
                    } catch (Exception ignored) {} // not in action bar?
                    try {
                        targets.add(
                            TapTarget.forToolbarMenuItem(editorToolbar, R.id.important_info, getString(R.string.editor_activity_targetHelps_importantInfoButton_title), getString(R.string.editor_activity_targetHelps_importantInfoButton_description))
                                    .targetCircleColor(circleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(id)
                        );
                        ++id;
                    } catch (Exception ignored) {} // not in action bar?

                    sequence.targets(targets);
                }
                else {
                    List<TapTarget> targets = new ArrayList<>();
                    targets.add(
                            TapTarget.forToolbarNavigationIcon(editorToolbar, getString(R.string.editor_activity_targetHelps_navigationIcon_title), getString(R.string.editor_activity_targetHelps_navigationIcon_description))
                                    .targetCircleColor(circleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(1)
                    );
                    targets.add(
                            TapTarget.forToolbarOverflow(editorToolbar, getString(R.string.editor_activity_targetHelps_applicationMenu_title), getString(R.string.editor_activity_targetHelps_applicationMenu_description))
                                    .targetCircleColor(circleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(2)
                    );

                    int id = 3;
                    try {
                        targets.add(
                                TapTarget.forToolbarMenuItem(editorToolbar, R.id.menu_activity_log, getString(R.string.editor_activity_targetHelps_activityLog_title), getString(R.string.editor_activity_targetHelps_activityLog_description))
                                        .targetCircleColor(circleColor)
                                        .textColor(textColor)
                                        .tintTarget(tintTarget)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                    } catch (Exception ignored) {} // not in action bar?
                    try {
                        targets.add(
                                TapTarget.forToolbarMenuItem(editorToolbar, R.id.important_info, getString(R.string.editor_activity_targetHelps_importantInfoButton_title), getString(R.string.editor_activity_targetHelps_importantInfoButton_description))
                                        .targetCircleColor(circleColor)
                                        .textColor(textColor)
                                        .tintTarget(tintTarget)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                    } catch (Exception ignored) {} // not in action bar?

                    sequence.targets(targets);
                }

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
                        Editor editor = ApplicationPreferences.preferences.edit();
                        if (drawerSelectedItem <= COUNT_DRAWER_PROFILE_ITEMS) {
                            editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, false);
                            editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, false);
                            if (drawerSelectedItem == DSI_PROFILES_SHOW_IN_ACTIVATOR)
                                editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
                        }
                        else {
                            editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS, false);
                            editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, false);
                            if (drawerSelectedItem == DSI_EVENTS_START_ORDER)
                                editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
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
                        Intent intent = new Intent("ShowEditorTargetHelpsBroadcastReceiver");
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

}
