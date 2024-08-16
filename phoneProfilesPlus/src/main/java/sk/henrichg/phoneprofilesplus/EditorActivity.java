package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
//import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.MenuCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import me.ibrahimsn.lib.SmoothBottomBar;
import sk.henrichg.phoneprofilesplus.EditorEventListFragment.OnStartEventPreferences;
import sk.henrichg.phoneprofilesplus.EditorProfileListFragment.OnStartProfilePreferences;

/** @noinspection ExtractMethodRecommender*/
public class EditorActivity extends AppCompatActivity
                                    implements OnStartProfilePreferences,
                                               OnStartEventPreferences,
                                               RefreshGUIActivatorEditorListener,
                                               ShowTargetHelpsActivatorEditorListener,
                                               FinishActivityActivatorEditorListener
{

    //private static volatile EditorActivity instance;
    private boolean activityStarted = false;

    private ImageView eventsRunStopIndicator;

    private static volatile boolean savedInstanceStateChanged;

    private ImportAsyncTask importAsyncTask = null;
    private ExportAsyncTask exportAsyncTask = null;
    private BackupAsyncTask backupAsyncTask = null;
    private RestoreAsyncTask restoreAsyncTask = null;

    private static volatile boolean doImport = false;
    private AlertDialog importProgressDialog = null;
    private AlertDialog exportProgressDialog = null;
    private AlertDialog backupProgressDialog = null;
    private AlertDialog restoreProgressDialog = null;

    private static final int DSI_PROFILES_ALL = 0;
    private static final int DSI_PROFILES_SHOW_IN_ACTIVATOR = 1;
    private static final int DSI_PROFILES_NO_SHOW_IN_ACTIVATOR = 2;
    private static final int DSI_EVENTS_START_ORDER = 0;
    private static final int DSI_EVENTS_ALL = 1;
    private static final int DSI_EVENTS_NOT_STOPPED = 2;
    private static final int DSI_EVENTS_RUNNING = 3;
    private static final int DSI_EVENTS_PAUSED = 4;
    private static final int DSI_EVENTS_STOPPED = 5;

    // request code for startActivityForResult with intent BackgroundActivateProfileActivity
    static final int REQUEST_CODE_ACTIVATE_PROFILE = 6220;
    // request code for startActivityForResult with intent ProfilesPrefsActivity
    private static final int REQUEST_CODE_PROFILE_PREFERENCES = 6221;
    // request code for startActivityForResult with intent EventPrefsActivity
    private static final int REQUEST_CODE_EVENT_PREFERENCES = 6222;
    // request code for startActivityForResult with intent PhoneProfilesActivity
    private static final int REQUEST_CODE_APPLICATION_PREFERENCES = 6229;
    // request code for startActivityForResult with intent "phoneprofiles.intent.action.EXPORTDATA"
    //private static final int REQUEST_CODE_REMOTE_EXPORT = 6250;
    // request code for startActivityForResult with intent ACTION_OPEN_DOCUMENT_TREE
    private static final int REQUEST_CODE_BACKUP_SETTINGS = 6230;
    private static final int REQUEST_CODE_BACKUP_SETTINGS_2 = 6231;
    private static final int REQUEST_CODE_RESTORE_SETTINGS = 6232;
    private static final int REQUEST_CODE_SHARE_SETTINGS = 6233;
    private static final int REQUEST_CODE_RESTORE_SHARED_SETTINGS = 6234;

    private static final String PREF_BACKUP_CREATE_PPP_SUBFOLDER = "backup_create_ppp_subfolder";

    private static final String ACTION_SHOW_EDITOR_TARGET_HELPS_BROADCAST_RECEIVER = PPApplication.PACKAGE_NAME + ".ShowEditorTargetHelpsBroadcastReceiver";

    private static final int IMPORTEXPORT_IMPORT = 1;
    private static final int IMPORTEXPORT_EXPORT = 2;

    private Toolbar editorToolbar;
    //Toolbar bottomToolbar;
    //private DrawerLayout drawerLayout;
    //private PPScrimInsetsFrameLayout drawerRoot;
    //private ListView drawerListView;
    //private ActionBarDrawerToggle drawerToggle;
    //private BottomNavigationView bottomNavigationView;
    AppCompatSpinner filterSpinner;
    //private AppCompatSpinner orderSpinner;
    //private View headerView;
    //private ImageView drawerHeaderFilterImage;
    //private TextView drawerHeaderFilterTitle;
    //private TextView drawerHeaderFilterSubtitle;
    //private BottomNavigationView bottomNavigationView;
    private SmoothBottomBar bottomNavigationView;

    //private String[] drawerItemsTitle;
    //private String[] drawerItemsSubtitle;
    //private Integer[] drawerItemsIcon;

    private boolean filterInitialized;

    private int editorSelectedView = 0;
    private int filterProfilesSelectedItem = 0;
    private int filterEventsSelectedItem = 0;

    //private boolean startTargetHelps;
    //public boolean targetHelpsSequenceStarted;

    int selectedLanguage = 0;
    String defaultLanguage = "";
    String defaultCountry = "";
    String defaultScript = "";
    //final Collator languagesCollator = GlobalUtils.getCollator();

    AddProfileDialog addProfileDialog;
    AddEventDialog addEventDialog;

    static private class RefreshGUIBroadcastReceiver extends BroadcastReceiver {

        private final RefreshGUIActivatorEditorListener listener;

        public RefreshGUIBroadcastReceiver(RefreshGUIActivatorEditorListener listener){
            this.listener = listener;
        }

        @Override
        public void onReceive( Context context, Intent intent ) {
            listener.refreshGUIFromListener(intent);
        }
    }
    private RefreshGUIBroadcastReceiver refreshGUIBroadcastReceiver;

    static private class ShowTargetHelpsBroadcastReceiver extends BroadcastReceiver {
        private final ShowTargetHelpsActivatorEditorListener listener;

        public ShowTargetHelpsBroadcastReceiver(ShowTargetHelpsActivatorEditorListener listener){
            this.listener = listener;
        }

        @Override
        public void onReceive( Context context, Intent intent ) {
            listener.showTargetHelpsFromListener(intent);
        }
    }
    private ShowTargetHelpsBroadcastReceiver showTargetHelpsBroadcastReceiver;

    static private class FinishActivityBroadcastReceiver extends BroadcastReceiver {
        private final FinishActivityActivatorEditorListener listener;

        public FinishActivityBroadcastReceiver(FinishActivityActivatorEditorListener listener){
            this.listener = listener;
        }

        @Override
        public void onReceive( Context context, Intent intent ) {
            listener.finishActivityFromListener(intent);
        }
    }
    private FinishActivityBroadcastReceiver finishBroadcastReceiver;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, true, false, false, false, false);

        super.onCreate(savedInstanceState);
//        Log.e("EditorActivity.onCreate", "xxxx");

        //GlobalGUIRoutines.setLanguage(this);

        savedInstanceStateChanged = (savedInstanceState != null);

        PPApplicationStatic.createApplicationsCache(true);

        setContentView(R.layout.activity_editor);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        boolean doServiceStart = startPPServiceWhenNotStarted();
        if (doServiceStart) {
            finish();
            return;
        }
        else
        if (showNotStartedToast()) {
            finish();
            return;
        }

        activityStarted = true;

        //drawerLayout = findViewById(R.id.editor_list_drawer_layout);

        /*
            drawerLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                        int statusBarHeight = insets.getSystemWindowInsetTop();
                        Rect rect = insets.getSystemWindowInsets();
                        rect.top = rect.top + statusBarHeight;
                        return insets.replaceSystemWindowInsets(rect);
                    }
                }
            );
        */

        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        //String appTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), true);

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
        headerView =  getLayoutInflater().inflate(R.layout.editor_drawer_list_header, drawerListView, false);
        drawerListView.addHeaderView(headerView, null, false);
        drawerHeaderFilterImage = findViewById(R.id.editor_drawer_list_header_icon);
        drawerHeaderFilterTitle = findViewById(R.id.editor_drawer_list_header_title);
        drawerHeaderFilterSubtitle = findViewById(R.id.editor_drawer_list_header_subtitle);

        // set header padding for notches
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

        // actionbar titles
        drawerItemsTitle = new String[] {
                getString(R.string.editor_drawer_title_profiles),
                getString(R.string.editor_drawer_title_profiles),
                getString(R.string.editor_drawer_title_profiles),
                getString(R.string.editor_drawer_title_events),
                getString(R.string.editor_drawer_title_events),
                getString(R.string.editor_drawer_title_events),
                getString(R.string.editor_drawer_title_events),
                getString(R.string.editor_drawer_title_events)
              };

        // drawer item titles
        drawerItemsSubtitle = new String[] {
                getString(R.string.editor_drawer_list_item_profiles_all),
                getString(R.string.editor_drawer_list_item_profiles_show_in_activator),
                getString(R.string.editor_drawer_list_item_profiles_no_show_in_activator),
                getString(R.string.editor_drawer_list_item_events_start_order),
                getString(R.string.editor_drawer_list_item_events_all),
                getString(R.string.editor_drawer_list_item_events_running),
                getString(R.string.editor_drawer_list_item_events_paused),
                getString(R.string.editor_drawer_list_item_events_stopped)
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

        int startupSource = getIntent().getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, 0);

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
        bottomNavigationView.setOnItemSelectedListener(
                (me.ibrahimsn.lib.OnItemSelectedListener) item -> {
                    bottomNavigationView.playSoundEffect(SoundEffectConstants.CLICK);
                    return EditorActivity.this.selectViewItem(item);
                }
        );
        // set size of icons of BottomNavigationView
        /*BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            final View iconView = menuView.getChildAt(i).findViewById(com.google.android.material.R.id.icon);
            final ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, displayMetrics);
            layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, displayMetrics);
            iconView.setLayoutParams(layoutParams);
        }*/

        filterSpinner = findViewById(R.id.editor_filter_spinner);
        String[] filterItems = new String[] {
                /*getString(R.string.editor_drawer_title_profiles) + " - " + */getString(R.string.editor_drawer_list_item_profiles_all),
                /*getString(R.string.editor_drawer_title_profiles) + " - " + */getString(R.string.editor_drawer_list_item_profiles_show_in_activator),
                /*getString(R.string.editor_drawer_title_profiles) + " - " + */getString(R.string.editor_drawer_list_item_profiles_no_show_in_activator)
        };
        HighlightedSpinnerAdapter filterSpinnerAdapter = new HighlightedSpinnerAdapter(
                this,
                R.layout.spinner_highlighted_filter,
                filterItems);
        filterSpinnerAdapter.setDropDownViewResource(R.layout.spinner_highlighted_dropdown);
        filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
        filterSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(this/*getBaseContext()*/, R.color.highlighted_spinner_all_editor));
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
        filterInitialized = false;
        filterSpinner.setAdapter(filterSpinnerAdapter);
        filterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!filterInitialized) {
                    filterInitialized = true;
                    return;
                }
                if (filterSpinner.getAdapter() != null) {
                    //if (filterSpinner.getAdapter().getCount() <= position)
                    //    position = 0;
                    ((HighlightedSpinnerAdapter) filterSpinner.getAdapter()).setSelection(position);
                }

                selectFilterItem(editorSelectedView, position, true/*, true*/);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        eventsRunStopIndicator = findViewById(R.id.editor_list_run_stop_indicator);
        TooltipCompat.setTooltipText(eventsRunStopIndicator, getString(R.string.editor_activity_targetHelps_trafficLightIcon_title));
        eventsRunStopIndicator.setOnClickListener(view -> {
            if (!isFinishing()) {
                RunStopIndicatorPopupWindow popup = new RunStopIndicatorPopupWindow(getDataWrapper(), EditorActivity.this);

                View contentView = popup.getContentView();
                contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int popupWidth = contentView.getMeasuredWidth();
                //int popupHeight = contentView.getMeasuredHeight();
                //Log.d("ActivatorActivity.eventsRunStopIndicator.onClick","popupWidth="+popupWidth);
                //Log.d("ActivatorActivity.eventsRunStopIndicator.onClick","popupHeight="+popupHeight);

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
        //if ((savedInstanceState != null) || (ApplicationPreferences.applicationEditorSaveEditorState(getApplicationContext())))
        //{
            //filterSelectedItem = ApplicationPreferences.preferences.getInt(SP_EDITOR_DRAWER_SELECTED_ITEM, 1);
            //Context appContext = getApplicationContext();

            if (startupSource == PPApplication.STARTUP_SOURCE_EDITOR_SHOW_IN_ACTIVATOR_FILTER) {
                editorSelectedView = 0;
                filterProfilesSelectedItem = DSI_PROFILES_ALL;
            }
            else if (startupSource == PPApplication.STARTUP_SOURCE_EDITOR_SHOW_IN_EDITOR_FILTER) {
                editorSelectedView = 0;
                filterProfilesSelectedItem = DSI_EVENTS_ALL;
            }
            else {
//                ApplicationPreferences.editorSelectedView(appContext);
//                ApplicationPreferences.editorProfilesViewSelectedItem(appContext);
                editorSelectedView = ApplicationPreferences.editorSelectedView;
                filterProfilesSelectedItem = ApplicationPreferences.editorProfilesViewSelectedItem;
            }
//            ApplicationPreferences.editorEventsViewSelectedItem(appContext);
            filterEventsSelectedItem = ApplicationPreferences.editorEventsViewSelectedItem;
        //}

        //startTargetHelps = false;
        /*if (editorSelectedView == 0)
            bottomNavigationView.setSelectedItemId(R.id.menu_profiles_view);
        else
            bottomNavigationView.setSelectedItemId(R.id.menu_events_view);*/
        bottomNavigationView.setItemActiveIndex(editorSelectedView);
        selectViewItem(editorSelectedView);

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
                return insets;
            }
        });
        */

        finishBroadcastReceiver = new FinishActivityBroadcastReceiver(this);
        int receiverFlags = 0;
        if (Build.VERSION.SDK_INT >= 34)
            receiverFlags = RECEIVER_NOT_EXPORTED;
        getApplicationContext().registerReceiver(finishBroadcastReceiver, new IntentFilter(PPApplication.ACTION_FINISH_ACTIVITY), receiverFlags);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onStart()
    {
        super.onStart();
//        Log.e("EditorActivity.onStart", "xxxx");

        boolean doServiceStart = startPPServiceWhenNotStarted();
        if (doServiceStart) {
            if (!isFinishing())
                finish();
            return;
        }
        else
        if (showNotStartedToast()) {
            if (!isFinishing())
                finish();
            return;
        }

        if (activityStarted) {
            // this is for API 33+
            if (!Permissions.grantNotificationsPermission(this)) {
                Intent intent = new Intent(PPApplication.ACTION_FINISH_ACTIVITY);
                intent.putExtra(PPApplication.EXTRA_WHAT_FINISH, StringConstants.EXTRA_ACTIVATOR);
                getApplicationContext().sendBroadcast(intent);

                refreshGUIBroadcastReceiver = new RefreshGUIBroadcastReceiver(this);
                LocalBroadcastManager.getInstance(this).registerReceiver(refreshGUIBroadcastReceiver,
                        new IntentFilter(PPApplication.ACTION_REFRESH_EDITOR_GUI_BROADCAST_RECEIVER));
                showTargetHelpsBroadcastReceiver = new ShowTargetHelpsBroadcastReceiver(this);
                LocalBroadcastManager.getInstance(this).registerReceiver(showTargetHelpsBroadcastReceiver,
                        new IntentFilter(ACTION_SHOW_EDITOR_TARGET_HELPS_BROADCAST_RECEIVER));

                refreshGUI(/*true,*/ false, true, 0, 0);
            }
        }
        else {
            if (!isFinishing())
                finish();
        }
    }

    @SuppressWarnings("SameReturnValue")
    private boolean showNotStartedToast() {
        PPApplicationStatic.setApplicationFullyStarted(getApplicationContext());
//        PPApplicationStatic.logE("[APPLICATION_FULLY_STARTED] EditorActivity.showNotStartedToast", "xxx");
        return false;
/*        boolean applicationStarted = PPApplicationStatic.getApplicationStarted(true);
        boolean fullyStarted = PPApplication.applicationFullyStarted;
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
        return false;*/
    }

    @SuppressWarnings("SameReturnValue")
    private boolean startPPServiceWhenNotStarted() {
        Context appContext = getApplicationContext();
        if (PPApplicationStatic.getApplicationStopping(appContext)) {
            String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_stopping_toast);
            PPApplication.showToast(appContext, text, Toast.LENGTH_SHORT);
            return true;
        }

        boolean serviceStarted = GlobalUtils.isServiceRunning(appContext, PhoneProfilesService.class, false);
        if (!serviceStarted) {
            //AutostartPermissionNotification.showNotification(appContext, true);

            // start PhoneProfilesService
            //PPApplication.firstStartServiceStarted = false;
            PPApplicationStatic.setApplicationStarted(appContext, true);
            Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
            serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
            serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_SHIZUKU_START, false);
//            PPApplicationStatic.logE("[START_PP_SERVICE] EditorActivity.startPPServiceWhenNotStarted", "(1)");
            PPApplicationStatic.startPPService(this, serviceIntent, true);
            //return true;
        } /*else {
            if ((PhoneProfilesService.getInstance() == null) || (!PhoneProfilesService.getInstance().getServiceHasFirstStart())) {
                //return true;
            }
        }*/

        return false;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
//        Log.e("EditorActivity.onResume", "xxxx");

        savedInstanceStateChanged = false;
    }

    private void unregisterReceiversInStop() {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshGUIBroadcastReceiver);
        } catch (Exception ignored) {}
        refreshGUIBroadcastReceiver = null;
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(showTargetHelpsBroadcastReceiver);
        } catch (Exception ignored) {}
        showTargetHelpsBroadcastReceiver = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
//        Log.e("EditorActivity.onStop", "xxxx");

        unregisterReceiversInStop();

        if ((addProfileDialog != null) && (addProfileDialog.mDialog != null) && addProfileDialog.mDialog.isShowing())
            addProfileDialog.mDialog.dismiss();
        if ((addEventDialog != null) && (addEventDialog.mDialog != null) && addEventDialog.mDialog.isShowing())
            addEventDialog.mDialog.dismiss();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
//        Log.e("EditorActivity.onDestroy", "xxxx");

        unregisterReceiversInStop();

        if ((importProgressDialog != null) && importProgressDialog.isShowing()) {
            importProgressDialog.dismiss();
            importProgressDialog = null;
        }
        if ((exportProgressDialog != null) && exportProgressDialog.isShowing()) {
            exportProgressDialog.dismiss();
            exportProgressDialog = null;
        }
        if ((backupProgressDialog != null) && backupProgressDialog.isShowing()) {
            backupProgressDialog.dismiss();
            backupProgressDialog = null;
        }
        if ((restoreProgressDialog != null) && restoreProgressDialog.isShowing()) {
            restoreProgressDialog.dismiss();
            restoreProgressDialog = null;
        }

        if ((importAsyncTask != null) && importAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            importAsyncTask.cancel(true);
        doImport = false;
        importAsyncTask = null;
        if ((exportAsyncTask != null) && exportAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            exportAsyncTask.cancel(true);
        exportAsyncTask = null;
        if ((backupAsyncTask != null) && backupAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            backupAsyncTask.cancel(true);
        backupAsyncTask = null;
        if ((restoreAsyncTask != null) && restoreAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            restoreAsyncTask.cancel(true);
        restoreAsyncTask = null;

        if (!savedInstanceStateChanged) {
            // no destroy caches on orientation change
//            Log.e("EditorActivity.onDestroy", "clear Application cache = "+PPApplicationStatic.getApplicationsCache());

            Runnable runnable = () -> {
                if (PPApplicationStatic.getApplicationsCache() != null) {
                    PPApplicationStatic.getApplicationsCache().cancelCaching();
                    //if (PPApplicationStatic.getApplicationsCache().cached)
                    PPApplicationStatic.getApplicationsCache().clearCache(true);
//                    PPApplicationStatic.logE("[SYNCHRONIZED] EditorActivity.onDestroy", "PPApplication.applicationCacheMutex");
                    synchronized (PPApplication.applicationCacheMutex) {
                        PPApplication.applicationsCache = null;
                    }
                }
            };
            PPApplicationStatic.createBasicExecutorPool();
            PPApplication.basicExecutorPool.submit(runnable);
        }

        try {
            getApplicationContext().unregisterReceiver(finishBroadcastReceiver);
        } catch (Exception e) {
            //PPApplicationStatic.recordException(e);
        }
        finishBroadcastReceiver = null;
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        editorToolbar.inflateMenu(R.menu.editor_top_bar);
        if (DebugVersion.enabled)
            editorToolbar.inflateMenu(R.menu.editor_debug);

        MenuCompat.setGroupDividerEnabled(menu, true);

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

        menuItem = menu.findItem(R.id.menu_settings_submenu);
        if (menuItem != null)
        {
            //Log.e("EditorActivity.onPrepareOptionsMenu", "(1)");
            SubMenu subMenu = menuItem.getSubMenu();
            if (subMenu != null) {
                //Log.e("EditorActivity.onPrepareOptionsMenu", "(2)");
                /*
                SpannableString headerTitle = new SpannableString("▼   " + menuItem.getTitle());
                //headerTitle.setSpan(new RelativeSizeSpan(0.7f), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                headerTitle.setSpan(new AbsoluteSizeSpan(40, false), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                //headerTitle.setSpan(new ScaleXSpan(0.7f), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                //headerTitle.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.altype_error)), 0, headerTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                */
                Drawable triangle = ContextCompat.getDrawable(this, R.drawable.ic_submenu_triangle);
                if (triangle != null) {
                    triangle.setTint(ContextCompat.getColor(this, R.color.activitySecondaryTextColor));
                    SpannableString headerTitle = new SpannableString("    " + menuItem.getTitle());
                    //triangle.setBounds(0, 5, 30, 28);
                    triangle.setBounds(0,
                            GlobalGUIRoutines.sip(1),
                            GlobalGUIRoutines.sip(10.5f),
                            GlobalGUIRoutines.sip(8.5f));
                    headerTitle.setSpan(new ImageSpan(triangle, ImageSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //headerTitle.setSpan(new ImageSpan(this, R.drawable.ic_submenu_triangle, DynamicDrawableSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    subMenu.setHeaderTitle(headerTitle);
                }
            }
        }
        menuItem = menu.findItem(R.id.menu_import_export);
        if (menuItem != null)
        {
            SubMenu subMenu = menuItem.getSubMenu();
            if (subMenu != null) {
                Drawable triangle = ContextCompat.getDrawable(this, R.drawable.ic_submenu_triangle);
                if (triangle != null) {
                    triangle.setTint(ContextCompat.getColor(this, R.color.activitySecondaryTextColor));
                    SpannableString headerTitle = new SpannableString("    " + menuItem.getTitle());
                    triangle.setBounds(0,
                            GlobalGUIRoutines.sip(1),
                            GlobalGUIRoutines.sip(10.5f),
                            GlobalGUIRoutines.sip(8.5f));
                    headerTitle.setSpan(new ImageSpan(triangle, ImageSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //headerTitle.setSpan(new ImageSpan(this, R.drawable.ic_submenu_triangle, DynamicDrawableSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    subMenu.setHeaderTitle(headerTitle);
                }
            }
        }
        menuItem = menu.findItem(R.id.menu_support);
        if (menuItem != null)
        {
            SubMenu subMenu = menuItem.getSubMenu();
            if (subMenu != null) {
                Drawable triangle = ContextCompat.getDrawable(this, R.drawable.ic_submenu_triangle);
                if (triangle != null) {
                    triangle.setTint(ContextCompat.getColor(this, R.color.activitySecondaryTextColor));
                    SpannableString headerTitle = new SpannableString("    " + menuItem.getTitle());
                    triangle.setBounds(0,
                            GlobalGUIRoutines.sip(1),
                            GlobalGUIRoutines.sip(10.5f),
                            GlobalGUIRoutines.sip(8.5f));
                    headerTitle.setSpan(new ImageSpan(triangle, ImageSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //headerTitle.setSpan(new ImageSpan(this, R.drawable.ic_submenu_triangle, DynamicDrawableSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    subMenu.setHeaderTitle(headerTitle);
                }
            }
        }
        menuItem = menu.findItem(R.id.menu_check_github_releases);
        if (menuItem != null)
        {
            SubMenu subMenu = menuItem.getSubMenu();
            if (subMenu != null) {
                Drawable triangle = ContextCompat.getDrawable(this, R.drawable.ic_submenu_triangle);
                if (triangle != null) {
                    triangle.setTint(ContextCompat.getColor(this, R.color.activitySecondaryTextColor));
                    SpannableString headerTitle = new SpannableString("    " + menuItem.getTitle());
                    triangle.setBounds(0,
                            GlobalGUIRoutines.sip(1),
                            GlobalGUIRoutines.sip(10.5f),
                            GlobalGUIRoutines.sip(8.5f));
                    headerTitle.setSpan(new ImageSpan(triangle, ImageSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //headerTitle.setSpan(new ImageSpan(this, R.drawable.ic_submenu_triangle, DynamicDrawableSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    subMenu.setHeaderTitle(headerTitle);
                }
            }
        }
        PackageManager packageManager = getPackageManager();
//        menuItem = menu.findItem(R.id.menu_check_in_galaxy_store);
//        if (menuItem != null) {
//            Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.GALAXY_STORE_PACKAGE_NAME);
//            if (intent != null)
//                menuItem.setTitle(StringConstants.CHAR_ARROW +" " + getString(R.string.menu_check_releases_galaxy_store));
//            else
//                menuItem.setTitle(R.string.menu_check_releases_galaxy_store);
//        }
//        menuItem = menu.findItem(R.id.menu_check_in_appgallery);
//        if (menuItem != null) {
//            Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.HUAWEI_APPGALLERY_PACKAGE_NAME);
//            if (intent != null)
//                menuItem.setTitle(StringConstants.CHAR_ARROW +" " + getString(R.string.menu_check_releases_appgallery));
//            else
//                menuItem.setTitle(R.string.menu_check_releases_appgallery);
//        }
        menuItem = menu.findItem(R.id.menu_check_in_droidify);
        if (menuItem != null) {
            Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.DROIDIFY_PACKAGE_NAME);
            if (intent != null)
                menuItem.setTitle(StringConstants.CHAR_ARROW +" " + getString(R.string.menu_check_releases_droidify));
            else
                menuItem.setTitle(R.string.menu_check_releases_droidify);
        }
        menuItem = menu.findItem(R.id.menu_check_in_neostore);
        if (menuItem != null) {
            Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.NEOSTORE_PACKAGE_NAME);
            if (intent != null)
                menuItem.setTitle(StringConstants.CHAR_ARROW +" " + getString(R.string.menu_check_releases_neostore));
            else
                menuItem.setTitle(R.string.menu_check_releases_neostore);
        }
        menuItem = menu.findItem(R.id.menu_check_in_fdroid);
        if (menuItem != null) {
            Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.FDROID_PACKAGE_NAME);
            if (intent != null)
                menuItem.setTitle(StringConstants.CHAR_ARROW +" " + getString(R.string.menu_check_releases_fdroid));
            else
                menuItem.setTitle(R.string.menu_check_releases_fdroid);
        }
        menuItem = menu.findItem(R.id.menu_check_in_apkpure);
        if (menuItem != null) {
            Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.APKPURE_PACKAGE_NAME);
            if (intent != null)
                menuItem.setTitle(StringConstants.CHAR_ARROW +" " + getString(R.string.menu_check_releases_apkpure));
            else
                menuItem.setTitle(R.string.menu_check_releases_apkpure);
        }

        if (DebugVersion.enabled) {
            menuItem = menu.findItem(R.id.menu_debug);
            if (menuItem != null) {
                SubMenu subMenu = menuItem.getSubMenu();
                if (subMenu != null) {
                    Drawable triangle = ContextCompat.getDrawable(this, R.drawable.ic_submenu_triangle);
                    if (triangle != null) {
                        triangle.setTint(ContextCompat.getColor(this, R.color.activitySecondaryTextColor));
                        SpannableString headerTitle = new SpannableString("    " + menuItem.getTitle());
                        triangle.setBounds(0,
                                GlobalGUIRoutines.sip(1),
                                GlobalGUIRoutines.sip(10.5f),
                                GlobalGUIRoutines.sip(8.5f));
                        headerTitle.setSpan(new ImageSpan(triangle, ImageSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        //headerTitle.setSpan(new ImageSpan(this, R.drawable.ic_submenu_triangle, DynamicDrawableSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        subMenu.setHeaderTitle(headerTitle);
                    }
                }
            }
        }


        //menuItem = menu.findItem(R.id.menu_import_export);
        //menuItem.setTitle(getResources().getString(R.string.menu_import_export) + "  >");

        // change global events run/stop menu item title
        menuItem = menu.findItem(R.id.menu_run_stop_events);
        if (menuItem != null)
        {
            if (EventStatic.getGlobalEventsRunning(this))
            {
                menuItem.setTitle(R.string.menu_stop_events);
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
            else
            {
                menuItem.setTitle(R.string.menu_run_events);
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            }
        }

        menuItem = menu.findItem(R.id.menu_restart_events);
        if (menuItem != null)
        {
            menuItem.setVisible(EventStatic.getGlobalEventsRunning(this));
            menuItem.setEnabled(PPApplicationStatic.getApplicationStarted(true, false));
        }

        /*
        menuItem = menu.findItem(R.id.menu_dark_theme);
        if (menuItem != null)
        {
            String appTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), false);
            if (!appTheme.equals("night_mode")) {
                menuItem.setVisible(true);
                menuItem.setEnabled(true);
                if (appTheme.equals("dark"))
                    menuItem.setTitle(R.string.menu_dark_theme_off);
                else
                    menuItem.setTitle(R.string.menu_dark_theme_on);
            }
            else {
                menuItem.setVisible(false);
                menuItem.setEnabled(false);
            }
        }
        */

        menuItem = menu.findItem(R.id.menu_email_debug_logs_to_author);
        if (menuItem != null)
        {
            //noinspection ConstantConditions
            menuItem.setVisible(PPApplication.logIntoFile || PPApplication.crashIntoFile);
            //noinspection ConstantConditions
            menuItem.setEnabled(PPApplication.logIntoFile || PPApplication.crashIntoFile);
        }

        /*
        menuItem = menu.findItem(R.id.menu_debug);
        if (menuItem != null) {
            menuItem.setVisible(DebugVersion.enabled);
            menuItem.setEnabled(DebugVersion.enabled);
        }
        */

        /*
        boolean activityExists = GlobalGUIRoutines.activityActionExists(Intent.ACTION_OPEN_DOCUMENT_TREE, getApplicationContext());
        menuItem = menu.findItem(R.id.menu_import);
        if (menuItem != null) {
            menuItem.setVisible(activityExists);
            menuItem.setEnabled(activityExists);
        }
        menuItem = menu.findItem(R.id.menu_export);
        if (menuItem != null) {
            menuItem.setVisible(activityExists);
            menuItem.setEnabled(activityExists);
        }
        */

        /*activityExists = GlobalGUIRoutines.activityActionExists(Intent.ACTION_OPEN_DOCUMENT_TREE, getApplicationContext());
        menuItem = menu.findItem(R.id.menu_share_settings);
        if (menuItem != null) {
            menuItem.setVisible(activityExists);
            menuItem.setEnabled(activityExists);
        }*/
        /*activityExists = GlobalGUIRoutines.activityActionExists(Intent.ACTION_OPEN_DOCUMENT, getApplicationContext());
        menuItem = menu.findItem(R.id.menu_restore_shared_settings);
        if (menuItem != null) {
            menuItem.setVisible(activityExists);
            menuItem.setEnabled(activityExists);
        }*/

        //PackageManager packageManager = getPackageManager();
        //Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.GALAXY_STORE_PACKAGE_NAME);
        //boolean galaxyStoreInstalled = (intent != null);
        //menuItem = menu.findItem(R.id.menu_check_in_galaxy_store);
        //if (menuItem != null) {
        //    menuItem.setVisible(PPApplication.deviceIsSamsung && galaxyStoreInstalled);
        //}

        //intent = packageManager.getLaunchIntentForPackage(PPApplication.HUAWEI_APPGALLERY_PACKAGE_NAME);
        //boolean appGalleryInstalled = (intent != null);
        //menuItem = menu.findItem(R.id.menu_check_in_appgallery);
        //if (menuItem != null) {
        //    menuItem.setVisible(PPApplication.deviceIsHuawei && PPApplication.romIsEMUI && appGalleryInstalled);
        //}

        menuItem = menu.findItem(R.id.menu_discord);
        if (menuItem != null)
        {
            SubMenu subMenu = menuItem.getSubMenu();
            if (subMenu != null) {
                Drawable triangle = ContextCompat.getDrawable(this, R.drawable.ic_submenu_triangle);
                if (triangle != null) {
                    triangle.setTint(ContextCompat.getColor(this, R.color.activitySecondaryTextColor));
                    SpannableString headerTitle = new SpannableString("    " + menuItem.getTitle());
                    triangle.setBounds(0,
                            GlobalGUIRoutines.sip(1),
                            GlobalGUIRoutines.sip(10.5f),
                            GlobalGUIRoutines.sip(8.5f));
                    headerTitle.setSpan(new ImageSpan(triangle, ImageSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //headerTitle.setSpan(new ImageSpan(this, R.drawable.ic_submenu_triangle, DynamicDrawableSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    subMenu.setHeaderTitle(headerTitle);
                }
            }
        }

        onNextLayout(editorToolbar, this::showTargetHelps);

        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;

        DataWrapper dataWrapper = getDataWrapper();
        Context appContext = getApplicationContext();

        int itemId = item.getItemId();
/*        if (itemId == android.R.id.home) {
//                if (drawerLayout.isDrawerOpen(drawerRoot)) {
//                    drawerLayout.closeDrawer(drawerRoot);
//                } else {
//                    drawerLayout.openDrawer(drawerRoot);
//                }
                return super.onOptionsItemSelected(item);
          }
 */
        if (itemId == R.id.menu_restart_events) {
            //getDataWrapper().addActivityLog(DatabaseHandler.ALTYPE_RESTARTEVENTS, null, null, null, 0);

            // ignore manual profile activation
            // and unblock forceRun events
            if (dataWrapper != null)
                dataWrapper.restartEventsWithAlert(this);
            return true;
        }
        else
        if (itemId == R.id.menu_run_stop_events) {
            if (dataWrapper != null)
                dataWrapper.runStopEventsWithAlert(this, null, false);
            return true;
        }
        else
        if (itemId == R.id.menu_activity_log) {
            intent = new Intent(getBaseContext(), ActivityLogActivity.class);
            startActivity(intent);
            return true;
        }
        else
        if (itemId == R.id.important_info) {
            intent = new Intent(getBaseContext(), ImportantInfoActivity.class);
            startActivity(intent);
            return true;
        }
        else
        if (itemId == R.id.menu_settings) {
            intent = new Intent(getBaseContext(), PhoneProfilesPrefsActivity.class);
            //noinspection deprecation
            startActivityForResult(intent, REQUEST_CODE_APPLICATION_PREFERENCES);
            return true;
        }
        else
        /*
        if (itemId == R.id.menu_dark_theme) {
            String theme = ApplicationPreferences.applicationTheme(appContext, false);
            if (!theme.equals("night_mode")) {
                SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(appContext);
                Editor editor = preferences.edit();
                if (theme.equals("dark")) {
                    //theme = preferences.getString(ApplicationPreferences.PREF_APPLICATION_NOT_DARK_THEME, "white");
                    //theme = ApplicationPreferences.applicationNightModeOffTheme(appContext);
                    editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, "white");
                    editor.apply();
                    ApplicationPreferences.applicationTheme = "white";
                } else {
                    //editor.putString(ApplicationPreferences.PREF_APPLICATION_NOT_DARK_THEME, theme);
                    editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, "dark");
                    editor.apply();
                    ApplicationPreferences.applicationTheme = "dark";
                }
                GlobalGUIRoutines.switchNightMode(appContext, false);
                GlobalGUIRoutines.reloadActivity(this, true);
            }
            return true;
        }
        else
        */
        if (itemId == R.id.menu_appliction_theme) {
            intent = new Intent(getBaseContext(), PhoneProfilesPrefsActivity.class);
            intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_APPLICATION_INTERFACE_CATEGORY_ROOT);
            //noinspection deprecation
            startActivityForResult(intent, REQUEST_CODE_APPLICATION_PREFERENCES);
            return true;
        }
        else
        if (itemId == R.id.menu_export) {
            exportData(R.string.menu_export, false, false, false);
            return true;
        }
        else
        if (itemId == R.id.menu_import) {
            importData(R.string.menu_import, false);
            return true;
        }

        else
        if (itemId == R.id.menu_share_settings) {
            exportData(R.string.menu_share_settings, false, false, true);
            return true;
        }
        else
        if (itemId == R.id.menu_restore_shared_settings) {
            importData(R.string.menu_restore_shared_settings, true);
            return true;
        }

        else
        if (itemId == R.id.menu_export_and_email) {
            exportData(R.string.menu_export_and_email, true, false, false);
            return true;
        }
        else
        if (itemId == R.id.menu_email_to_author) {
            intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse(StringConstants.INTENT_DATA_MAIL_TO_COLON)); // only email apps should handle this
            String[] email = {StringConstants.AUTHOR_EMAIL};
            intent.putExtra(Intent.EXTRA_EMAIL, email);
            String packageVersion = "";
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                packageVersion = " - v" + pInfo.versionName + " (" + PPApplicationStatic.getVersionCode(pInfo) + ")";
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            intent.putExtra(Intent.EXTRA_SUBJECT, StringConstants.PHONE_PROFILES_PLUS + packageVersion + " - " + getString(R.string.about_application_support_subject));
            intent.putExtra(Intent.EXTRA_TEXT, getEmailBodyText(this));
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.email_chooser)));
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }

            return true;
        }
        else
        if (itemId == R.id.menu_export_and_email_to_author) {
            exportData(R.string.menu_export_and_email_to_author, true, true, false);
            return true;
        }
        else
        if (itemId == R.id.menu_email_debug_logs_to_author) {
            ArrayList<Uri> uris = new ArrayList<>();

            File sd = appContext.getExternalFilesDir(null);

            File logFile = new File(sd, PPApplication.LOG_FILENAME);
            if (logFile.exists()) {
                Uri fileUri = FileProvider.getUriForFile(this, PPApplication.PACKAGE_NAME + ".provider", logFile);
                uris.add(fileUri);
            }

            File crashFile = new File(sd, CustomACRAReportingAdministrator.CRASH_FILENAME);
            if (crashFile.exists()) {
                Uri fileUri = FileProvider.getUriForFile(this, PPApplication.PACKAGE_NAME + ".provider", crashFile);
                uris.add(fileUri);
            }

            if (!uris.isEmpty()) {
                String emailAddress = StringConstants.AUTHOR_EMAIL;
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        StringConstants.INTENT_DATA_MAIL_TO, emailAddress, null));

                String packageVersion = "";
                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                    packageVersion = " - v" + pInfo.versionName + " (" + PPApplicationStatic.getVersionCode(pInfo) + ")";
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, StringConstants.PHONE_PROFILES_PLUS + packageVersion + " - " + getString(R.string.email_debug_log_files_subject));
                emailIntent.putExtra(Intent.EXTRA_TEXT, getEmailBodyText(this));
                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                List<ResolveInfo> resolveInfo = getPackageManager().queryIntentActivities(emailIntent, 0);
                List<LabeledIntent> intents = new ArrayList<>();
                for (ResolveInfo info : resolveInfo) {
                    intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                    intent.putExtra(Intent.EXTRA_SUBJECT, StringConstants.PHONE_PROFILES_PLUS + packageVersion + " - " + getString(R.string.email_debug_log_files_subject));
                    intent.putExtra(Intent.EXTRA_TEXT, getEmailBodyText(this));
                    intent.setType(StringConstants.MINE_TYPE_ALL); // gmail will only match with type set
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris); //ArrayList<Uri> of attachment Uri's
                    intents.add(new LabeledIntent(intent, info.activityInfo.packageName, info.loadLabel(getPackageManager()), info.icon));
                }
                if (!intents.isEmpty()) {
                    try {
                        Intent chooser = Intent.createChooser(new Intent(Intent.ACTION_CHOOSER), getString(R.string.email_chooser));
                        chooser.putExtra(Intent.EXTRA_INTENT, intents.get(0));
                        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new LabeledIntent[0]));
                        startActivity(chooser);
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            } else {
                // toast notification
                PPApplication.showToast(appContext, getString(R.string.toast_debug_log_files_not_exists),
                        Toast.LENGTH_SHORT);
            }

            return true;
        }
        else
        if (itemId == R.id.menu_about) {
            intent = new Intent(getBaseContext(), AboutApplicationActivity.class);
            startActivity(intent);
            return true;
        }
        else
        if (itemId == R.id.menu_exit) {
            PPAlertDialog dialog = new PPAlertDialog(
                    getString(R.string.exit_application_alert_title),
                    getString(R.string.exit_application_alert_message),
                    getString(R.string.alert_button_yes),
                    getString(R.string.alert_button_no),
                    null, null,
                    (dialog1, which) -> PPApplicationStatic.exitApp(true, appContext, getDataWrapper(), EditorActivity.this, false, true, true),
                    null,
                    null,
                    null,
                    null,
                    true, true,
                    false, false,
                    true,
                    this
            );

            if (!isFinishing())
                dialog.show();
            return true;
        }
        else
        if (itemId == R.id.gui_items_help) {
            PPAlertDialog dialog = new PPAlertDialog(
                    getString(R.string.gui_items_help_alert_title),
                    getString(R.string.gui_items_help_alert_message),
                    getString(R.string.alert_button_yes), getString(R.string.alert_button_no), null, null,
                    (dialog1, which) -> {
                        ApplicationPreferences.startStopTargetHelps(appContext, true);
                        GlobalGUIRoutines.reloadActivity(this, true);
                    },
                    null,
                    null,
                    null,
                    null,
                    true, true,
                    false, false,
                    false,
                    this
            );

            if (!isFinishing())
                dialog.show();
            return true;
        }
        else
        if ((itemId == R.id.menu_check_in_github) ||
                (itemId == R.id.menu_check_in_fdroid) ||
                (itemId == R.id.menu_check_in_droidify) ||
                (itemId == R.id.menu_check_in_neostore) ||
                //(itemId == R.id.menu_check_in_appgallery) ||
                (itemId == R.id.menu_check_in_apkpure)) {

            Intent _intent;
            _intent = new Intent(this, CheckPPPReleasesActivity.class);
            _intent.putExtra(CheckPPPReleasesActivity.EXTRA_MENU_ITEM_ID, itemId);
            _intent.putExtra(CheckPPPReleasesActivity.EXTRA_CRITICAL_CHECK, false);
            _intent.putExtra(CheckPPPReleasesActivity.EXTRA_NEW_VERSION_NAME, "");
            _intent.putExtra(CheckPPPReleasesActivity.EXTRA_NEW_VERSION_CODE, 0);
            startActivity(_intent);

            return true;
        }
        else
        if (itemId == R.id.menu_donation) {
            intent = new Intent(getBaseContext(), DonationPayPalActivity.class);
            startActivity(intent);
            return true;
        }
        else
        if (itemId == R.id.menu_choose_language) {
            ChooseLanguageDialog chooseLanguageDialog = new ChooseLanguageDialog(this);
            chooseLanguageDialog.show();
            return true;
        }
        else
        if (itemId == R.id.menu_xda_developers) {
            String url = PPApplication.XDA_DEVELOPERS_PPP_URL;
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.web_browser_chooser)));
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return true;
        }
        else
        if (itemId == R.id.menu_discord_server) {
            String url = PPApplication.DISCORD_SERVER_URL;
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.web_browser_chooser)));
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return true;
        }
        else
        if (itemId == R.id.menu_discord_invitation) {
            String url = PPApplication.DISCORD_INVITATION_URL;
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.web_browser_chooser)));
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return true;
        }
        else
        if (itemId == R.id.menu_twitter) {
            String url = PPApplication.TWITTER_URL;
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.web_browser_chooser)));
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return true;
        }
        else
        if (itemId == R.id.menu_reddit) {
            String url = PPApplication.REDDIT_URL;
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.web_browser_chooser)));
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return true;
        }
        else
        if (itemId == R.id.menu_bluesky) {
            String url = PPApplication.BLUESKY_URL;
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.web_browser_chooser)));
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            return true;
        }
        else
        if (DebugVersion.debugMenuItems(itemId, this))
            return true;
        else
            return super.onOptionsItemSelected(item);
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

    private boolean selectViewItem(int item) {
        //int itemId = item.getItemId();
        //if (itemId == R.id.menu_profiles_view) {
        if (item == 0) {
            editorToolbar.setTitle(getString(R.string.editor_drawer_title_profiles) + " - " + getString(R.string.title_activity_editor));
            //editorToolbar.setSubtitle(R.string.title_activity_editor);
            final Handler handler = new Handler(getMainLooper());
            final WeakReference<EditorActivity> activityWeakRef = new WeakReference<>(this);
            handler.postDelayed(() -> {
                EditorActivity activity = activityWeakRef.get();
                if ((activity == null) || activity.isFinishing() || activity.isDestroyed())
                    return;

//                PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EditorActivity.selectViewItem (0)");
                String[] filterItems = new String[]{
                        /*activity.getString(R.string.editor_drawer_title_profiles) + " - " + */activity.getString(R.string.editor_drawer_list_item_profiles_all),
                        /*activity.getString(R.string.editor_drawer_title_profiles) + " - " + */activity.getString(R.string.editor_drawer_list_item_profiles_show_in_activator),
                        /*activity.getString(R.string.editor_drawer_title_profiles) + " - " + */activity.getString(R.string.editor_drawer_list_item_profiles_no_show_in_activator),
                };
                HighlightedSpinnerAdapter filterSpinnerAdapter = new HighlightedSpinnerAdapter(
                        activity,
                        R.layout.spinner_highlighted_filter,
                        filterItems);
                filterSpinnerAdapter.setDropDownViewResource(R.layout.spinner_highlighted_dropdown);
                activity.filterSpinner.setAdapter(filterSpinnerAdapter);
                activity.selectFilterItem(0, activity.filterProfilesSelectedItem, false/*, startTargetHelps*/);
                Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                if (fragment instanceof EditorProfileListFragment)
                    ((EditorProfileListFragment) fragment).showHeaderAndBottomToolbar();
            }, 200);
            return true;
            //} else if (itemId == R.id.menu_events_view) {
        } else if (item == 1) {
            editorToolbar.setTitle(getString(R.string.editor_drawer_title_events) + " - " + getString(R.string.title_activity_editor));
            //editorToolbar.setSubtitle(R.string.title_activity_editor);
            final Handler handler = new Handler(getMainLooper());
            final WeakReference<EditorActivity> activityWeakRef = new WeakReference<>(this);
            handler.postDelayed(() -> {
                EditorActivity activity = activityWeakRef.get();
                if ((activity == null) || activity.isFinishing() || activity.isDestroyed())
                    return;

//                PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EditorActivity.selectViewItem (1)");
                String[] filterItems = new String[]{
                        /*activity.getString(R.string.editor_drawer_title_events) + " - " + */activity.getString(R.string.editor_drawer_list_item_events_start_order),
                        /*activity.getString(R.string.editor_drawer_title_events) + " - " + */activity.getString(R.string.editor_drawer_list_item_events_all),
                        /*activity.getString(R.string.editor_drawer_title_events) + " - " + */activity.getString(R.string.editor_drawer_list_item_events_not_stopped),
                        /*activity.getString(R.string.editor_drawer_title_events) + " - " + */activity.getString(R.string.editor_drawer_list_item_events_running),
                        /*activity.getString(R.string.editor_drawer_title_events) + " - " + */activity.getString(R.string.editor_drawer_list_item_events_paused),
                        /*activity.getString(R.string.editor_drawer_title_events) + " - " + */activity.getString(R.string.editor_drawer_list_item_events_stopped)
                };
                HighlightedSpinnerAdapter filterSpinnerAdapter = new HighlightedSpinnerAdapter(
                        activity,
                        R.layout.spinner_highlighted_filter,
                        filterItems);
                filterSpinnerAdapter.setDropDownViewResource(R.layout.spinner_highlighted_dropdown);
                activity.filterSpinner.setAdapter(filterSpinnerAdapter);
                activity.selectFilterItem(1, activity.filterEventsSelectedItem, false/*, startTargetHelps*/);
                Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                if (fragment instanceof EditorEventListFragment) {
                    ((EditorEventListFragment) fragment).showHeaderAndBottomToolbar();
                }
            }, 200);
            return true;
        } else
            return false;
    }

    private void selectFilterItem(int selectedView, int position, boolean fromClickListener/*, boolean startTargetHelps*/) {
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
            if ((filterSpinner.getAdapter() == null) || (filterSpinner.getAdapter().getCount() <= filterProfilesSelectedItem))
                filterSelectedItem = 0;
            else
                filterSelectedItem = filterProfilesSelectedItem;
        }
        else {
            if ((filterSpinner.getAdapter() == null) || (filterSpinner.getAdapter().getCount() <= filterEventsSelectedItem))
                filterSelectedItem = 0;
            else
                filterSelectedItem = filterEventsSelectedItem;
        }

        if (viewChanged || (position != filterSelectedItem))
        {
            if (viewChanged) {
                // stop running AsyncTask
                if (fragment instanceof EditorProfileListFragment) {
                    if (((EditorProfileListFragment) fragment).isAsyncTaskRunning()) {
                        //Log.e("EditorActivity.selectFilterItem", "AsyncTask finished - profiles");
                        ((EditorProfileListFragment) fragment).stopRunningAsyncTask();
                    }
                } else if (fragment instanceof EditorEventListFragment) {
                    if (((EditorEventListFragment) fragment).isAsyncTaskRunning()) {
                        //Log.e("EditorActivity.selectFilterItem", "AsyncTask finished - events");
                        ((EditorEventListFragment) fragment).stopRunningAsyncTask();
                    }
                }
            }

            editorSelectedView = selectedView;
            if (editorSelectedView == 0) {
                if ((filterSpinner.getAdapter() == null) || (filterSpinner.getAdapter().getCount() <= position)) {
                    filterProfilesSelectedItem = 0;
                    filterSelectedItem = ApplicationPreferences.EDITOR_PROFILES_VIEW_SELECTED_ITEM_DEFAULT_VALUE;
                }
                else {
                    filterProfilesSelectedItem = position;
                    filterSelectedItem = position;
                }
            }
            else {
                if ((filterSpinner.getAdapter() == null) || (filterSpinner.getAdapter().getCount() <= position)) {
                    filterEventsSelectedItem = 0;
                    filterSelectedItem = ApplicationPreferences.EDITOR_EVENTS_VIEW_SELECTED_ITEM_DEFAULT_VALUE;
                }
                else {
                    filterEventsSelectedItem = position;
                    filterSelectedItem = position;
                }
            }

            // save into shared preferences
            Context appContext = getApplicationContext();
            Editor editor = ApplicationPreferences.getEditor(appContext);
            editor.putInt(ApplicationPreferences.PREF_EDITOR_SELECTED_VIEW, editorSelectedView);
            editor.putInt(ApplicationPreferences.PREF_EDITOR_PROFILES_VIEW_SELECTED_ITEM, filterProfilesSelectedItem);
            editor.putInt(ApplicationPreferences.PREF_EDITOR_EVENTS_VIEW_SELECTED_ITEM, filterEventsSelectedItem);
            editor.apply();
            ApplicationPreferences.editorSelectedView(appContext);
            ApplicationPreferences.editorProfilesViewSelectedItem(appContext);
            ApplicationPreferences.editorEventsViewSelectedItem(appContext);

            Bundle arguments;

            int profilesFilterType;
            int eventsFilterType;
            //int eventsOrderType = getEventsOrderType();

            switch (editorSelectedView) {
                case 0:
                    switch (filterProfilesSelectedItem) {
                        case DSI_PROFILES_ALL:
                            profilesFilterType = EditorProfileListFragment.FILTER_TYPE_ALL;
                            if (viewChanged) {
                                fragment = new EditorProfileListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorProfileListFragment.BUNDLE_FILTER_TYPE, profilesFilterType);
                                //arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorProfileListFragment displayedFragment = (EditorProfileListFragment)fragment;
                                displayedFragment.changeFragmentFilter(profilesFilterType/*, startTargetHelps*/);
                            }
                            break;
                        case DSI_PROFILES_SHOW_IN_ACTIVATOR:
                            profilesFilterType = EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR;
                            if (viewChanged) {
                                fragment = new EditorProfileListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorProfileListFragment.BUNDLE_FILTER_TYPE, profilesFilterType);
                                //arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorProfileListFragment displayedFragment = (EditorProfileListFragment)fragment;
                                displayedFragment.changeFragmentFilter(profilesFilterType/*, startTargetHelps*/);
                            }
                            break;
                        case DSI_PROFILES_NO_SHOW_IN_ACTIVATOR:
                            profilesFilterType = EditorProfileListFragment.FILTER_TYPE_NO_SHOW_IN_ACTIVATOR;
                            if (viewChanged) {
                                fragment = new EditorProfileListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorProfileListFragment.BUNDLE_FILTER_TYPE, profilesFilterType);
                                //arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorProfileListFragment displayedFragment = (EditorProfileListFragment)fragment;
                                displayedFragment.changeFragmentFilter(profilesFilterType/*, startTargetHelps*/);
                            }
                            break;
                    }
                    break;
                case 1:
                    switch (filterEventsSelectedItem) {
                        case DSI_EVENTS_START_ORDER:
                            eventsFilterType = EditorEventListFragment.FILTER_TYPE_START_ORDER;
                            if (viewChanged) {
                                fragment = new EditorEventListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorEventListFragment.BUNDLE_FILTER_TYPE, eventsFilterType);
                                //arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorEventListFragment displayedFragment = (EditorEventListFragment)fragment;
                                displayedFragment.changeFragmentFilter(eventsFilterType/*, startTargetHelps*/);
                            }
                            break;
                        case DSI_EVENTS_ALL:
                            eventsFilterType = EditorEventListFragment.FILTER_TYPE_ALL;
                            if (viewChanged) {
                                fragment = new EditorEventListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorEventListFragment.BUNDLE_FILTER_TYPE, eventsFilterType);
                                //arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorEventListFragment displayedFragment = (EditorEventListFragment)fragment;
                                displayedFragment.changeFragmentFilter(eventsFilterType/*, startTargetHelps*/);
                            }
                            break;
                        case DSI_EVENTS_NOT_STOPPED:
                            eventsFilterType = EditorEventListFragment.FILTER_TYPE_NOT_STOPPED;
                            if (viewChanged) {
                                fragment = new EditorEventListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorEventListFragment.BUNDLE_FILTER_TYPE, eventsFilterType);
                                //arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorEventListFragment displayedFragment = (EditorEventListFragment)fragment;
                                displayedFragment.changeFragmentFilter(eventsFilterType/*, startTargetHelps*/);
                            }
                            break;
                        case DSI_EVENTS_RUNNING:
                            eventsFilterType = EditorEventListFragment.FILTER_TYPE_RUNNING;
                            if (viewChanged) {
                                fragment = new EditorEventListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorEventListFragment.BUNDLE_FILTER_TYPE, eventsFilterType);
                                //arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorEventListFragment displayedFragment = (EditorEventListFragment)fragment;
                                displayedFragment.changeFragmentFilter(eventsFilterType/*, startTargetHelps*/);
                            }
                            break;
                        case DSI_EVENTS_PAUSED:
                            eventsFilterType = EditorEventListFragment.FILTER_TYPE_PAUSED;
                            if (viewChanged) {
                                fragment = new EditorEventListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorEventListFragment.BUNDLE_FILTER_TYPE, eventsFilterType);
                                //arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorEventListFragment displayedFragment = (EditorEventListFragment)fragment;
                                displayedFragment.changeFragmentFilter(eventsFilterType/*, startTargetHelps*/);
                            }
                            break;
                        case DSI_EVENTS_STOPPED:
                            eventsFilterType = EditorEventListFragment.FILTER_TYPE_STOPPED;
                            if (viewChanged) {
                                fragment = new EditorEventListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorEventListFragment.BUNDLE_FILTER_TYPE, eventsFilterType);
                                //arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorEventListFragment displayedFragment = (EditorEventListFragment)fragment;
                                displayedFragment.changeFragmentFilter(eventsFilterType/*, startTargetHelps*/);
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

        Context appContext = getApplicationContext();

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
                int newProfileMode = data.getIntExtra(PPApplication.EXTRA_NEW_PROFILE_MODE, PPApplication.EDIT_MODE_UNDEFINED);
                //int predefinedProfileIndex = data.getIntExtra(EXTRA_PREDEFINED_PROFILE_INDEX, 0);

                if (profile_id > 0)
                {
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(appContext);
                    notificationManager.cancel(
                            PPApplication.DISPLAY_PREFERENCES_PROFILE_ERROR_NOTIFICATION_TAG+"_"+profile_id,
                            PPApplication.PROFILE_ID_NOTIFICATION_ID + (int) profile_id);
                    ActivateProfileHelper.cancelNotificationsForInteractiveParameters(appContext);

                    Profile profile = DatabaseHandler.getInstance(appContext).getProfile(profile_id, false);
                    if (profile != null) {
                        // generate bitmaps
                        profile.generateIconBitmap(appContext, false, 0, false);
                        profile.generatePreferencesIndicator(appContext, false, 0, DataWrapper.IT_FOR_EDITOR, 0f);


                        boolean isShown = false;
                        NotificationManager mNotificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (mNotificationManager != null) {
                            StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
                            for (StatusBarNotification notification : notifications) {
                                String tag = notification.getTag();
                                if ((tag != null) && tag.contains(PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_TAG)) {
                                    if (notification.getId() == PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_ID + (int) profile._id) {
                                        isShown = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (isShown) {
                            // redraw generated notification
                            ActivateProfileHelper.generateNotifiction(appContext, profile);
                        }

                        // redraw list fragment , notifications, widgets after finish ProfilesPrefsActivity
                        redrawProfileListFragment(profile, newProfileMode);

                        //Profile mappedProfile = profile; //Profile.getMappedProfile(profile, appContext);
                        //Permissions.grantProfilePermissions(appContext, profile, false, true,
                        //        /*true, false, 0,*/ PPApplication.STARTUP_SOURCE_EDITOR, false, true, false);
                        DataWrapperStatic.displayPreferencesErrorNotification(profile, null, false, getApplicationContext());
                    }
                }

                /*Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
                PPApplication.startPPService(this, serviceIntent);*/
                Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
                //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
                PPApplicationStatic.runCommand(this, commandIntent);
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
                int newEventMode = data.getIntExtra(PPApplication.EXTRA_NEW_EVENT_MODE, PPApplication.EDIT_MODE_UNDEFINED);
                //int predefinedEventIndex = data.getIntExtra(EXTRA_PREDEFINED_EVENT_INDEX, 0);

                if (event_id > 0)
                {
                    Event event = DatabaseHandler.getInstance(appContext).getEvent(event_id);

                    // redraw list fragment , notifications, widgets after finish EventPreferencesActivity
                    redrawEventListFragment(event, newEventMode);

                    //Permissions.grantEventPermissions(appContext, event, true, false);
                    DataWrapperStatic.displayPreferencesErrorNotification(null, event, false, getApplicationContext());
                }

                /*Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
                PPApplication.startPPService(this, serviceIntent);*/
                Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
                //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
                PPApplicationStatic.runCommand(this, commandIntent);

                //IgnoreBatteryOptimizationNotification.showNotification(appContext);

//                PPApplicationStatic.logE("[MAIN_WORKER_CALL] EditorActivity.onActivityResult", "xxxxxxxxxxxxxxxxxxxx");

                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(MainWorker.DISABLE_NOT_USED_SCANNERS_WORK_TAG)
                                .setInitialDelay(30, TimeUnit.MINUTES)
                                .build();
                try {
                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                    if (workManager != null) {

//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.SCHEDULE_AVOID_RESCHEDULE_RECEIVER_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                        PPApplicationStatic.logE("[WORKER_CALL] EditorActivity.onActivityResult", "xxx");
                        workManager.enqueueUniqueWork(MainWorker.DISABLE_NOT_USED_SCANNERS_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }

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
//                Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
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
//                    if ((PhoneProfilesService.isLocationScannerStarted())) {
//                        PhoneProfilesService.getLocationScanner().resetLocationUpdates(powerSaveMode, true);
//                    }
//                    PhoneProfilesService.getInstance().resetListeningOrientationSensors(powerSaveMode, true);
//                    if (PhoneProfilesService.isMobileCellsScannerStarted())
//                        PhoneProfilesService.mobileCellsScanner.resetListening(powerSaveMode, true);
//
//                }

                boolean restart = data.getBooleanExtra(PhoneProfilesPrefsActivity.EXTRA_RESET_EDITOR, false);

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
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_PROFILE)) {
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
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_EXPORT)) {
            if (resultCode == RESULT_OK) {
                doExportData(false, false, false);
            }
        }
        else
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_EXPORT_AND_EMAIL)) {
            if (resultCode == RESULT_OK) {
                doExportData(true, false, false);
            }
        }
        else
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_IMPORT)) {
            if ((resultCode == RESULT_OK) && (data != null)) {
                boolean ok = false;
                try {
                    Intent intent;
                    if (Build.VERSION.SDK_INT >= 29) {
                        StorageManager sm = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
                        intent = sm.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
                    }
                    else {
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                    //intent.putExtra("android.content.extra.SHOW_ADVANCED",true);
                    //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, PPApplication.backupFolderUri);
                    //noinspection deprecation
                    startActivityForResult(intent, REQUEST_CODE_RESTORE_SETTINGS);
                    ok = true;
                } catch (Exception e) {
                    //PPApplicationStatic.recordException(e);
                }
                if (!ok) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            getString(R.string.menu_import),
                            getString(R.string.directory_tree_activity_not_found_alert),
                            getString(android.R.string.ok),
                            null,
                            null, null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            true,
                            this
                    );

                    if (!isFinishing())
                        dialog.show();
                }
            }
        }
        else
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_SHARED_IMPORT)) {
            if ((resultCode == RESULT_OK) && (data != null)) {
                boolean ok = false;
                try {
                    Intent intent;
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setType("application/zip");
                    //noinspection deprecation
                    startActivityForResult(intent, REQUEST_CODE_RESTORE_SHARED_SETTINGS);
                    ok = true;
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
                if (!ok) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            getString(R.string.menu_restore_shared_settings),
                            getString(R.string.open_document_activity_not_found_alert),
                            getString(android.R.string.ok),
                            null,
                            null, null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            true,
                            this
                    );

                    if (!isFinishing())
                        dialog.show();
                }
            }
        }
        else
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_EXPORT_AND_EMAIL_TO_AUTHOR)) {
            if (resultCode == RESULT_OK) {
                doExportData(true, true, false);
            }
        }
        else
        if ((requestCode == REQUEST_CODE_BACKUP_SETTINGS) || (requestCode == REQUEST_CODE_BACKUP_SETTINGS_2)) {
            if (resultCode == RESULT_OK) {
                // uri of folder
                Uri treeUri = data.getData();
                if (treeUri != null) {
                    appContext.grantUriPermission(PPApplication.PACKAGE_NAME, treeUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION/* | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION*/);
                    // persistent permissions
                    final int takeFlags = //data.getFlags() &
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    appContext.getContentResolver().takePersistableUriPermission(treeUri, takeFlags);

/*                    class BackupAsyncTask extends AsyncTask<Void, Integer, Integer> {
                        DocumentFile pickedDir;
                        final Uri treeUri;
                        final Activity activity;

                        final int requestCode;
                        int ok = 1;

                        private BackupAsyncTask(int requestCode, Uri treeUri, Activity activity) {
                            this.treeUri = treeUri;
                            this.requestCode = requestCode;
                            this.activity = activity;

                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                            dialogBuilder.setMessage(R.string.backup_settings_alert_title);

                            LayoutInflater inflater = (activity.getLayoutInflater());
                            View layout = inflater.inflate(R.layout.dialog_progress_bar, null);
                            dialogBuilder.setView(layout);

                            backupProgressDialog = dialogBuilder.create();

                        }

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();

                            pickedDir = DocumentFile.fromTreeUri(appContext, treeUri);

                            GlobalGUIRoutines.lockScreenOrientation(activity, false);
                            backupProgressDialog.setCancelable(false);
                            backupProgressDialog.setCanceledOnTouchOutside(false);
                            if (!activity.isFinishing())
                                backupProgressDialog.show();
                        }

                        @Override
                        protected Integer doInBackground(Void... params) {
                            if (pickedDir != null) {
                                if (pickedDir.canWrite()) {
                                    if (requestCode == REQUEST_CODE_BACKUP_SETTINGS_2) {
                                        // if directory exists, create new = "PhoneProfilesPlus (x)"
                                        // create subdirectory
                                        pickedDir = pickedDir.createDirectory("PhoneProfilesPlus");
                                        if (pickedDir == null) {
                                            // error for create directory
                                            ok = 0;
                                        }
                                    }
                                }
                                else {
                                    // pickedDir is not writable
                                    ok = 0;
                                }

                                if (ok == 1) {
                                    if (pickedDir.canWrite()) {
                                        File applicationDir = appContext.getExternalFilesDir(null);

                                        ok = copyToBackupDirectory(pickedDir, applicationDir, PPApplication.EXPORT_APP_PREF_FILENAME, getApplicationContext());
                                        if (ok == 1)
                                            ok = copyToBackupDirectory(pickedDir, applicationDir, DatabaseHandler.EXPORT_DBFILENAME, getApplicationContext());
                                    }
                                    else {
                                        // cannot copy backup files, pickedDir is not writable
                                        ok = 0;
                                    }
                                }

                            }
                            else {
                                // pickedDir is null
                                ok = 0;
                            }

                            return ok;
                        }

                        @Override
                        protected void onPostExecute(Integer result) {
                            super.onPostExecute(result);

                            if (!isFinishing()) {
                                if ((backupProgressDialog != null) && backupProgressDialog.isShowing()) {
                                    if (!isDestroyed())
                                        backupProgressDialog.dismiss();
                                    backupProgressDialog = null;
                                }
                                GlobalGUIRoutines.unlockScreenOrientation(activity);
                            }

                            if (result == 0) {
                                if (!activity.isFinishing()) {
                                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                                    dialogBuilder.setTitle(R.string.backup_settings_alert_title);
                                    dialogBuilder.setMessage(R.string.backup_settings_error_on_backup);
                                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                    dialogBuilder.setPositiveButton(android.R.string.ok, null);
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

                                    dialog.show();
                                }
                            }
                            else {
                                PPApplication.showToast(appContext, getString(R.string.backup_settings_ok_backed_up), Toast.LENGTH_SHORT);
                            }
                        }
                    }
 */

                    backupAsyncTask = new BackupAsyncTask(requestCode, treeUri, this);
                    backupAsyncTask.execute();
                }
            }
        }
        else
        if (requestCode == REQUEST_CODE_RESTORE_SETTINGS) {
            if (resultCode == RESULT_OK) {
                // uri of folder
                Uri treeUri = data.getData();
                if (treeUri != null) {
                    appContext.grantUriPermission(PPApplication.PACKAGE_NAME, treeUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION/* | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION*/);
                    // persistent permissions
                    final int takeFlags = //data.getFlags() &
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    appContext.getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
                    restoreAsyncTask = new RestoreAsyncTask(treeUri, false, this);
                    restoreAsyncTask.execute();
                }
            }
        }
        else
        if (requestCode == REQUEST_CODE_SHARE_SETTINGS) {
            if (resultCode == RESULT_OK) {
                PPApplication.showToast(appContext, getString(R.string.share_settings_ok_shared), Toast.LENGTH_SHORT);
            } else {
                if (!isFinishing()) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            getString(R.string.share_settings_alert_title),
                            getString(R.string.share_settings_error_on_share),
                            getString(android.R.string.ok),
                            null,
                            null, null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            true,
                            this
                    );

                    dialog.show();
                }
            }
        }
        else
        if (requestCode == REQUEST_CODE_RESTORE_SHARED_SETTINGS) {
            if (resultCode == RESULT_OK) {
                // uri of folder
                Uri fileUri = data.getData();
                if (fileUri != null) {
                    appContext.grantUriPermission(PPApplication.PACKAGE_NAME, fileUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION/* | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION*/);
                    // persistent permissions
                    final int takeFlags = //data.getFlags() &
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    appContext.getContentResolver().takePersistableUriPermission(fileUri, takeFlags);

                    restoreAsyncTask = new RestoreAsyncTask(fileUri, true, this);
                    restoreAsyncTask.execute();

                }
            }
        }
        else
        if (requestCode == Permissions.NOTIFICATIONS_PERMISSION_REQUEST_CODE)
        {
            ActivityManager.RunningServiceInfo serviceInfo = GlobalUtils.getServiceInfo(appContext, PhoneProfilesService.class);
            if (serviceInfo == null)
                startPPServiceWhenNotStarted();
            else {
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] EditorActivity.onActivityResult", "call of PPAppNotification.drawNotification");
                ImportantInfoNotification.showInfoNotification(appContext);
                ProfileListNotification.drawNotification(true, appContext);
                DrawOverAppsPermissionNotification.showNotification(appContext, true);
                IgnoreBatteryOptimizationNotification.showNotification(appContext, true);
                DNDPermissionNotification.showNotification(appContext, true);
                PPAppNotification.drawNotification(true, appContext);
            }

            //!!!! THIS IS IMPORTANT BECAUSE WITHOUT THIS IS GENERATED CRASH
            //  java.lang.NullPointerException: Attempt to invoke virtual method 'void android.content.BroadcastReceiver.onReceive(android.content.Context, android.content.Intent)'
            //  on a null object reference
            //  at androidx.localbroadcastmanager.content.LocalBroadcastManager.executePendingBroadcasts(LocalBroadcastManager.java:313)
            finish();
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

    private void importExportErrorDialog(int importExport, int dbResult, int appSettingsResult/*, int sharedProfileResult*/)
    {
        //AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        String title;
        if (importExport == IMPORTEXPORT_IMPORT)
            title = getString(R.string.import_profiles_alert_title);
        else
            title = getString(R.string.export_profiles_alert_title);
        //dialogBuilder.setTitle(title);
        String message;
        if (importExport == IMPORTEXPORT_IMPORT) {
            message = getString(R.string.import_profiles_alert_error) + ":";
            if (dbResult != DatabaseHandler.IMPORT_OK) {
                if (dbResult == DatabaseHandler.IMPORT_ERROR_NEVER_VERSION)
                    message = message + StringConstants.CHAR_NEW_LINE+StringConstants.CHAR_BULLET +" " + getString(R.string.import_profiles_alert_error_database_newer_version);
                else
                    message = message + StringConstants.CHAR_NEW_LINE+StringConstants.CHAR_BULLET +" " + getString(R.string.import_profiles_alert_error_database_bug);
            }
            if (appSettingsResult == 0)
                message = message + StringConstants.CHAR_NEW_LINE+StringConstants.CHAR_BULLET +" " + getString(R.string.import_profiles_alert_error_appSettings_bug);
            //if (sharedProfileResult == 0)
            //    message = message + "\n• " + getString(R.string.import_profiles_alert_error_sharedProfile_bug);
        }
        else
            message = getString(R.string.export_profiles_alert_error);

        PPAlertDialog dialog = new PPAlertDialog(title, message,
                getString(android.R.string.ok), null, null, null,
                (dialog1, which) -> {
                    // refresh activity
                    GlobalGUIRoutines.reloadActivity(EditorActivity.this, true);
                },
                null,
                null,
                dialog13 -> {
                    // refresh activity
                    GlobalGUIRoutines.reloadActivity(EditorActivity.this, true);
                },
                null,
                true, true,
                false, false,
                false,
                this
        );

        if (!isFinishing())
            dialog.show();
    }

    /** @noinspection BlockingMethodInNonBlockingContext*/
    @SuppressLint({"SetWorldReadable", "SetWorldWritable"})
    private boolean importApplicationPreferences(File src/*, int what*/) {
        Context appContext = getApplicationContext();
        boolean res = true;
        ObjectInputStream input = null;
        try {
            try {
                if (src.exists()) {
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        src.setReadable(true, false);
                    } catch (Exception ee) {
                        PPApplicationStatic.recordException(ee);
                    }
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        src.setWritable(true, false);
                    } catch (Exception ee) {
                        PPApplicationStatic.recordException(ee);
                    }

                    //noinspection IOStreamConstructor
                    input = new ObjectInputStream(new FileInputStream(src));
                    Editor prefEdit;
                    //if (what == 1) {
                        prefEdit = getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE).edit();
                        prefEdit.clear();
                    //}
                    //else {
                    //    prefEdit = getSharedPreferences("profile_preferences_default_profile", Activity.MODE_PRIVATE).edit();
                    //    prefEdit.clear();
                    //}
                    @SuppressWarnings("unchecked")
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

                        //if (what == 1) {
                            if (key.equals(ApplicationPreferences.PREF_APPLICATION_THEME)) {
                                if (v.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_LIGHT) ||
                                        v.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_MATERIAL) ||
                                        v.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_COLOR) ||
                                        v.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_DLIGHT)) {
                                    String defaultValue = ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_WHITE;
                                    if (Build.VERSION.SDK_INT >= 28)
                                        defaultValue = ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_NIGHT_MODE;
                                    prefEdit.putString(key, defaultValue);
                                }
                            }
                            if (key.equals(ActivateProfileHelper.PREF_MERGED_RING_NOTIFICATION_VOLUMES))
                                ActivateProfileHelper.setMergedRingNotificationVolumes(appContext, /*true,*/ prefEdit);
                            if (key.equals(ApplicationPreferences.PREF_APPLICATION_FIRST_START))
                                prefEdit.putBoolean(ApplicationPreferences.PREF_APPLICATION_FIRST_START, false);
                        //}

                    /*if (what == 2) {
                        if (key.equals(Profile.PREF_PROFILE_LOCK_DEVICE)) {
                            if (v.equals("3"))
                            prefEdit.putString(Profile.PREF_PROFILE_LOCK_DEVICE, "1");
                        }
                    }*/
                    }
                    prefEdit.apply();
                    //if (what == 1) {
                        // save version code
                        try {
                            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                            int actualVersionCode = PPApplicationStatic.getVersionCode(pInfo);
                            PPApplicationStatic.setSavedVersionCode(appContext, actualVersionCode);
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    //}

                    for (int i = 0; i < PPApplication.quickTileProfileId.length; i++)
                        ApplicationPreferences.setQuickTileProfileId(appContext, i, 0);


                    // set application parameters to "Not used" for non-granted Uri premissions
                    ContentResolver contentResolver = appContext.getContentResolver();
                    String tone = ApplicationPreferences.getSharedPreferences(appContext).getString(
                            ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_SOUND,
                            ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_SOUND_DEFAULT_VALUE);
                    if (!tone.isEmpty()) {
                        if (tone.contains(StringConstants.RINGTONE_CONTENT_EXTERNAL)) {
                            boolean isGranted = false;
                            Uri uri = Uri.parse(tone);
                            if (uri != null) {
                                try {
                                    appContext.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
                            }
                            if (!isGranted) {
                                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                                editor.putString(ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_SOUND,
                                        ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_SOUND_DEFAULT_VALUE);
                                editor.apply();
                            }
                        }
                    }
                    tone = ApplicationPreferences.getSharedPreferences(appContext).getString(
                            ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND,
                            ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND_DEFAULT_VALUE);
                    if (!tone.isEmpty()) {
                        if (tone.contains(StringConstants.RINGTONE_CONTENT_EXTERNAL)) {
                            boolean isGranted = false;
                            Uri uri = Uri.parse(tone);
                            if (uri != null) {
                                try {
                                    appContext.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
                            }
                            if (!isGranted) {
                                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                                editor.putString(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND,
                                        ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND_DEFAULT_VALUE);
                                editor.apply();
                            }
                        }
                    }

                    PPApplicationStatic.loadGlobalApplicationData(appContext);
                    PPApplicationStatic.loadApplicationPreferences(appContext);
                    PPApplicationStatic.loadProfileActivationData(appContext);
                }
                else
                    res = false;
            }/* catch (FileNotFoundException ignored) {
                // no error, this is OK
            }*/ catch (Exception e) {
                //Log.e("EditorActivity.importApplicationPreferences", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
                res = false;
            }
        }finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                PPApplicationStatic.recordException(e);
            }

            WifiScanWorker.setScanRequest(appContext, false);
            WifiScanWorker.setWaitForResults(appContext, false);
            WifiScanWorker.setWifiEnabledForScan(appContext, false);

            BluetoothScanWorker.setScanRequest(appContext, false);
            BluetoothScanWorker.setLEScanRequest(appContext, false);
            BluetoothScanWorker.setWaitForResults(appContext, false);
            BluetoothScanWorker.setWaitForLEResults(appContext, false);
            BluetoothScanWorker.setBluetoothEnabledForScan(appContext, false);
            BluetoothScanWorker.setScanKilled(appContext, false);

        }
        return res;
    }

    private void doImportData(/*String applicationDataPath*/)
    {
        //final EditorActivity activity = this;
        //final String _applicationDataPath = applicationDataPath;

        importAsyncTask = new ImportAsyncTask(this);
        importAsyncTask.execute();
    }

    private void importData(final int titleRes, final boolean share) {
        PPAlertDialog dialog = new PPAlertDialog(
                getString(titleRes),
                getString(R.string.import_profiles_alert_message),
                getString(R.string.alert_button_yes),
                getString(R.string.alert_button_no),
                null, null,
                (dialogX, which) -> {
                    if (share) {
                        if (Permissions.grantImportPermissions(true, getApplicationContext(), EditorActivity.this)) {
                            boolean ok = false;
                            try {
                                Intent intent;
                                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                intent.setType("application/zip");
                                //noinspection deprecation
                                startActivityForResult(intent, REQUEST_CODE_RESTORE_SHARED_SETTINGS);
                                ok = true;
                            } catch (Exception e) {
                                PPApplicationStatic.recordException(e);
                            }
                            if (!ok) {
                                PPAlertDialog _dialog = new PPAlertDialog(
                                        getString(R.string.menu_restore_shared_settings),
                                        getString(R.string.open_document_activity_not_found_alert),
                                        getString(android.R.string.ok),
                                        null,
                                        null, null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        true, true,
                                        false, false,
                                        true,
                                        EditorActivity.this
                                );

                                if (!isFinishing())
                                    _dialog.show();
                            }
                        }

                    } else {
                        if (Permissions.grantImportPermissions(false, getApplicationContext(), EditorActivity.this)) {
                            boolean ok = false;
                            try {
                                Intent intent;
                                if (Build.VERSION.SDK_INT >= 29) {
                                    StorageManager sm = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
                                    intent = sm.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
                                } else {
                                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                }
                                // not supported by ACTION_OPEN_DOCUMENT_TREE
                                //intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);

                                //intent.putExtra("android.content.extra.SHOW_ADVANCED",true);
                                //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, PPApplication.backupFolderUri);
                                //noinspection deprecation
                                startActivityForResult(intent, REQUEST_CODE_RESTORE_SETTINGS);
                                ok = true;
                            } catch (Exception e) {
                                //PPApplicationStatic.recordException(e);
                            }
                            if (!ok) {
                                PPAlertDialog _dialog = new PPAlertDialog(
                                        getString(R.string.restore_settings_alert_title),
                                        getString(R.string.directory_tree_activity_not_found_alert),
                                        getString(android.R.string.ok),
                                        null,
                                        null, null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        true, true,
                                        false, false,
                                        true,
                                        EditorActivity.this
                                );

                                if (!isFinishing())
                                    _dialog.show();
                            }
                        }
                    }
                },
                null,
                null,
                null,
                null,
                true, true,
                false, false,
                true,
                this
        );

        if (!isFinishing())
            dialog.show();
    }

    /** @noinspection BlockingMethodInNonBlockingContext*/
    @SuppressLint({"SetWorldReadable", "SetWorldWritable", "ApplySharedPref"})
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
                        editor.putInt(DatabaseHandlerImportExport.PREF_MAXIMUM_VOLUME_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt(DatabaseHandlerImportExport.PREF_MAXIMUM_VOLUME_NOTIFICATION, audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt(DatabaseHandlerImportExport.PREF_MAXIMUM_VOLUME_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt(DatabaseHandlerImportExport.PREF_MAXIMUM_VOLUME_ALARM, audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt(DatabaseHandlerImportExport.PREF_MAXIMUM_VOLUME_SYSTEM, audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt(DatabaseHandlerImportExport.PREF_MAXIMUM_VOLUME_VOICE_CALL, audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt(DatabaseHandlerImportExport.PREF_MAXIMUM_VOLUME_DTMF, audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt(DatabaseHandlerImportExport.PREF_MAXIMUM_VOLUME_ACCESSIBILITY, audioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt(DatabaseHandlerImportExport.PREF_MAXIMUM_VOLUME_BLUETOOTH_SCO, audioManager.getStreamMaxVolume(ActivateProfileHelper.STREAM_BLUETOOTH_SCO));
                    } catch (Exception ignored) {}
                }

                editor.commit();
                output.writeObject(pref.getAll());

                output.flush();

                try {
                    //noinspection ResultOfMethodCallIgnored
                    dst.setReadable(true, false);
                } catch (Exception ee) {
                    PPApplicationStatic.recordException(ee);
                }
                try {
                    //noinspection ResultOfMethodCallIgnored
                    dst.setWritable(true, false);
                } catch (Exception ee) {
                    PPApplicationStatic.recordException(ee);
                }

            } catch (FileNotFoundException e) {
                PPApplicationStatic.recordException(e);
                // this is OK
            } catch (IOException e) {
                PPApplicationStatic.recordException(e);
                res = false;
            }
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException e) {
                PPApplicationStatic.recordException(e);
            }
        }
        return res;
    }

    private void exportData(final int titleRes, final boolean email, final boolean toAuthor, final boolean share)
    {
        String title = getString(titleRes);
        String message;
        if (email)
            message = getString(R.string.export_profiles_alert_message_note);
        else if (share) {
            message = getString(R.string.share_settings_alert_message) + StringConstants.STR_DOUBLE_NEWLINE +
                    getString(R.string.export_profiles_alert_message_note);
        } else
            message = getString(R.string.export_profiles_alert_message) + StringConstants.STR_DOUBLE_NEWLINE +
                    getString(R.string.export_profiles_alert_message_note);

        PPAlertDialog dialog = new PPAlertDialog(title, message,
                getString(R.string.alert_button_backup), getString(android.R.string.cancel), null, null,
                (dialog1, which) -> {
                    if (email || share)
                        doExportData(email, toAuthor, share);
                    else if (Permissions.grantExportPermissions(getApplicationContext(), EditorActivity.this))
                        doExportData(false, false, false);
                },
                null,
                null,
                null,
                null,
                true, true,
                false, false,
                false,
                this
        );

        if (!isFinishing())
            dialog.show();
    }

    private void doExportData(final boolean email, final boolean toAuthor, final boolean share)
    {
        if (email || share || Permissions.checkExport(getApplicationContext())) {

            final EditorActivity activity = this;

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            if (share)
                dialogBuilder.setTitle(R.string.menu_share_settings);
            else
            if (toAuthor)
                dialogBuilder.setTitle(R.string.menu_export_and_email_to_author);
            else
            if (email)
                dialogBuilder.setTitle(R.string.menu_export_and_email);
            else
                dialogBuilder.setTitle(R.string.menu_export);
            dialogBuilder.setCancelable(true);
            //dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            LayoutInflater inflater = getLayoutInflater();
            final View layout = inflater.inflate(R.layout.dialog_delete_secure_data_in_export, null);
            dialogBuilder.setView(layout);

            dialogBuilder.setPositiveButton(R.string.alert_button_backup, (dialog, which) -> {
                CheckBox checkbox = layout.findViewById(R.id.deleteSecureDataInExportDialogGeofences);
                boolean deleteGeofences = checkbox.isChecked();
                checkbox = layout.findViewById(R.id.deleteSecureDataInExportDialogWifiSSIDs);
                boolean deleteWifiSSIDs = checkbox.isChecked();
                checkbox = layout.findViewById(R.id.deleteSecureDataInExportDialogBluetoothNames);
                boolean deleteBluetoothNames = checkbox.isChecked();
                checkbox = layout.findViewById(R.id.deleteSecureDataInExportDialogMobileCells);
                boolean deleteMobileCells = checkbox.isChecked();

                checkbox = layout.findViewById(R.id.deleteSecureDataInExportDialogCall);
                boolean deleteCall = checkbox.isChecked();
                checkbox = layout.findViewById(R.id.deleteSecureDataInExportDialogSMS);
                boolean deleteSMS = checkbox.isChecked();
                checkbox = layout.findViewById(R.id.deleteSecureDataInExportDialogNotification);
                boolean deleteNotification = checkbox.isChecked();
                checkbox = layout.findViewById(R.id.deleteSecureDataInExportDialogSendSMS);
                boolean deletePhoneCalls = checkbox.isChecked();
                checkbox = layout.findViewById(R.id.deleteSecureDataInExportDialogCallScreening);
                boolean deleteCallScreening = checkbox.isChecked();

                exportAsyncTask = new ExportAsyncTask(email, toAuthor, share,
                        deleteGeofences, deleteWifiSSIDs, deleteBluetoothNames, deleteMobileCells,
                        deleteCall, deleteSMS, deleteNotification, deletePhoneCalls, deleteCallScreening,
                        activity);
                exportAsyncTask.execute();
            });
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);

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
    public void onRestoreInstanceState(@NonNull Bundle outState) {
        super.onRestoreInstanceState(outState);
        savedInstanceStateChanged = false;
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
     private void setStatusBarTitle()
     {
        // set filter status bar title
        String text = drawerItemsSubtitle[drawerSelectedItem-1];
        //filterStatusBarTitle.setText(drawerItemsTitle[drawerSelectedItem - 1] + " - " + text);
        drawerHeaderFilterSubtitle.setText(text);
     }
     */

    private void startProfilePreferenceActivity(Profile profile, int editMode, int predefinedProfileIndex) {
        Intent intent = new Intent(getBaseContext(), ProfilesPrefsActivity.class);
        if ((profile == null) || (editMode == PPApplication.EDIT_MODE_INSERT))
            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, 0L);
        else
            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
        intent.putExtra(PPApplication.EXTRA_NEW_PROFILE_MODE, editMode);
        intent.putExtra(PPApplication.EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
        //noinspection deprecation
        startActivityForResult(intent, REQUEST_CODE_PROFILE_PREFERENCES);
    }

    /** @noinspection ClassEscapesDefinedScope*/
    public void onStartProfilePreferences(Profile profile, int editMode, int predefinedProfileIndex/*, boolean startTargetHelps*/) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
        if (fragment instanceof EditorProfileListFragment) {
            ((EditorProfileListFragment) fragment).updateBottomMenu();
        }

        // In single-pane mode, simply start the profile preferences activity
        // for the profile position.
        if (((profile != null) ||
            (editMode == PPApplication.EDIT_MODE_INSERT) ||
            (editMode == PPApplication.EDIT_MODE_DUPLICATE))
            && (editMode != PPApplication.EDIT_MODE_DELETE))
            startProfilePreferenceActivity(profile, editMode, predefinedProfileIndex);
    }

    void redrawProfileListFragment(Profile profile, int newProfileMode /*int predefinedProfileIndex, boolean startTargetHelps*/) {
        // redraw list fragment, notification a widgets

        Fragment _fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
        if (_fragment instanceof EditorProfileListFragment) {
            final EditorProfileListFragment fragment = (EditorProfileListFragment) getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
            if (fragment != null) {
                // update profile, this rewrite profile in profileList
                fragment.activityDataWrapper.updateProfile(profile);

                boolean newProfile = ((newProfileMode == PPApplication.EDIT_MODE_INSERT) ||
                        (newProfileMode == PPApplication.EDIT_MODE_DUPLICATE));
                fragment.updateListView(profile, newProfile, false, false/*, 0*/);

                Profile activeProfile = fragment.activityDataWrapper.getActivatedProfile(true,
                        ApplicationPreferences.applicationEditorPrefIndicator);
                fragment.updateHeader(activeProfile);
//                PPApplicationStatic.logE("[PPP_NOTIFICATION] EditorActivity.redrawProfileListFragment", "call of updateGUI");
                PPApplication.updateGUI(true, false, fragment.activityDataWrapper.context);

                DataWrapperStatic.setDynamicLauncherShortcutsFromMainThread(getApplicationContext(), false);

                if (filterProfilesSelectedItem != 0) {
                    final Handler handler = new Handler(getMainLooper());
                    final WeakReference<EditorActivity> activityWeakRef = new WeakReference<>(this);
                    final WeakReference<Profile> profileWeakRef = new WeakReference<>(profile);
                    handler.postDelayed(() -> {
//                            PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EditorActivity.redrawProfileListFragment");
                        EditorActivity editorActivity = activityWeakRef.get();
                        if ((editorActivity == null) || editorActivity.isFinishing() || editorActivity.isDestroyed())
                            return;

                        Profile _profile = profileWeakRef.get();
                        if (_profile == null)
                            return;

                        boolean changeFilter = false;
                        switch (editorActivity.filterProfilesSelectedItem) {
                            case EditorActivity.DSI_PROFILES_NO_SHOW_IN_ACTIVATOR:
                                changeFilter = _profile._showInActivator;
                                break;
                            case EditorActivity.DSI_PROFILES_SHOW_IN_ACTIVATOR:
                                changeFilter = !_profile._showInActivator;
                                break;
                        }
                        if (changeFilter) {
                            fragment.scrollToProfile = _profile;
                            ((HighlightedSpinnerAdapter) editorActivity.filterSpinner.getAdapter())
                                    .setSelection(ApplicationPreferences.EDITOR_PROFILES_VIEW_SELECTED_ITEM_DEFAULT_VALUE);
                            editorActivity.selectFilterItem(0, ApplicationPreferences.EDITOR_PROFILES_VIEW_SELECTED_ITEM_DEFAULT_VALUE, false/*, true*/);
                        }
                        else
                            fragment.scrollToProfile = null;
                    }, 200);
                }
            }
        }
    }

    private void startEventPreferenceActivity(Event event, final int editMode, final int predefinedEventIndex) {
        boolean profileExists = true;
        long startProfileId = 0;
        long endProfileId = -1;
        if ((editMode == PPApplication.EDIT_MODE_INSERT) && (predefinedEventIndex > 0)) {
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

        if (profileExists) {
            Intent intent = new Intent(getBaseContext(), EventsPrefsActivity.class);
            if ((event == null) || (editMode == PPApplication.EDIT_MODE_INSERT))
                intent.putExtra(PPApplication.EXTRA_EVENT_ID, 0L);
            else {
                intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);
                intent.putExtra(PPApplication.EXTRA_EVENT_STATUS, event.getStatus());
            }
            intent.putExtra(PPApplication.EXTRA_NEW_EVENT_MODE, editMode);
            intent.putExtra(PPApplication.EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
            //noinspection deprecation
            startActivityForResult(intent, REQUEST_CODE_EVENT_PREFERENCES);
        } else {
            final long _startProfileId = startProfileId;
            final long _endProfileId = endProfileId;

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

            PPAlertDialog dialog = new PPAlertDialog(
                    getString(R.string.menu_new_event),
                    message,
                    getString(R.string.alert_button_yes),
                    getString(R.string.alert_button_no),
                    null, null,
                    (dialog1, which) -> {
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
                        intent.putExtra(PPApplication.EXTRA_NEW_EVENT_MODE, editMode);
                        intent.putExtra(PPApplication.EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
                        //noinspection deprecation
                        startActivityForResult(intent, REQUEST_CODE_EVENT_PREFERENCES);
                    },
                    (dialog2, which) -> {
                        Intent intent = new Intent(getBaseContext(), EventsPrefsActivity.class);
                        intent.putExtra(PPApplication.EXTRA_EVENT_ID, 0L);
                        intent.putExtra(PPApplication.EXTRA_NEW_EVENT_MODE, editMode);
                        intent.putExtra(PPApplication.EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
                        //noinspection deprecation
                        startActivityForResult(intent, REQUEST_CODE_EVENT_PREFERENCES);
                    },
                    null,
                    null,
                    null,
                    true, true,
                    false, false,
                    true,
                    this
            );

            if (!isFinishing())
                dialog.show();
        }
    }

    /** @noinspection ClassEscapesDefinedScope*/
    public void onStartEventPreferences(Event event, int editMode, int predefinedEventIndex/*, boolean startTargetHelps*/) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
        if (fragment instanceof EditorEventListFragment) {
            ((EditorEventListFragment) fragment).updateBottomMenu();
        }

        if (((event != null) ||
            (editMode == PPApplication.EDIT_MODE_INSERT) ||
            (editMode == PPApplication.EDIT_MODE_DUPLICATE))
            && (editMode != PPApplication.EDIT_MODE_DELETE))
            startEventPreferenceActivity(event, editMode, predefinedEventIndex);
    }

    void redrawEventListFragment(Event event, int newEventMode /*int predefinedEventIndex, boolean startTargetHelps*/) {
        // redraw list fragment, notification and widgets
        Fragment _fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
        if (_fragment instanceof EditorEventListFragment) {
            final EditorEventListFragment fragment = (EditorEventListFragment) getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
            if (fragment != null) {
                // update event, this rewrite event in eventList
                fragment.activityDataWrapper.updateEvent(event, this);

                boolean newEvent = ((newEventMode == PPApplication.EDIT_MODE_INSERT) ||
                        (newEventMode == PPApplication.EDIT_MODE_DUPLICATE));
                fragment.updateListView(event, newEvent, false, false/*, 0*/);

                Profile activeProfile = fragment.activityDataWrapper.getActivatedProfileFromDB(true,
                        ApplicationPreferences.applicationEditorPrefIndicator);
                fragment.updateHeader(activeProfile);

                if (filterEventsSelectedItem != 0) {
                    final Handler handler = new Handler(getMainLooper());
                    final WeakReference<EditorActivity> activityWeakRef = new WeakReference<>(this);
                    final WeakReference<Event> eventWeakRef = new WeakReference<>(event);
                    handler.postDelayed(() -> {
//                            PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EditorActivity.redrawEventListFragment");
                        EditorActivity editorActivity = activityWeakRef.get();
                        if ((editorActivity == null) || editorActivity.isFinishing() || editorActivity.isDestroyed())
                            return;

                        Event _event = eventWeakRef.get();
                        if (_event == null)
                            return;

                        boolean changeFilter = false;
                        switch (editorActivity.filterEventsSelectedItem) {
                            case EditorActivity.DSI_EVENTS_NOT_STOPPED:
                                changeFilter = _event.getStatus() == Event.ESTATUS_STOP;
                                break;
                            case EditorActivity.DSI_EVENTS_RUNNING:
                                changeFilter = _event.getStatus() != Event.ESTATUS_RUNNING;
                                break;
                            case EditorActivity.DSI_EVENTS_PAUSED:
                                changeFilter = _event.getStatus() != Event.ESTATUS_PAUSE;
                                break;
                            case EditorActivity.DSI_EVENTS_STOPPED:
                                changeFilter = _event.getStatus() != Event.ESTATUS_STOP;
                                break;
                        }
                        if (changeFilter) {
                            fragment.scrollToEvent = _event;
                            ((HighlightedSpinnerAdapter) editorActivity.filterSpinner.getAdapter())
                                    .setSelection(ApplicationPreferences.EDITOR_EVENTS_VIEW_SELECTED_ITEM_DEFAULT_VALUE);
                            editorActivity.selectFilterItem(1, ApplicationPreferences.EDITOR_EVENTS_VIEW_SELECTED_ITEM_DEFAULT_VALUE, false/*, true*/);
                        }
                        else
                            fragment.scrollToEvent = null;
                    }, 200);
                }
            }
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
        if (EventStatic.getGlobalEventsRunning(this))
        {
            //if (ApplicationPreferences.prefEventsBlocked) {
            if (EventStatic.getEventsBlocked(getApplicationContext())) {
                eventsRunStopIndicator.setImageResource(R.drawable.ic_traffic_light_manual_activation);
            }
            else {
                eventsRunStopIndicator.setImageResource(R.drawable.ic_traffic_light_running);
            }
        }
        else {
            eventsRunStopIndicator.setImageResource(R.drawable.ic_traffic_light_stopped);
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

    private void showTargetHelps() {
        //startTargetHelps = true;

        final Context appContext = getApplicationContext();

        boolean startTargetHelps = ApplicationPreferences.prefEditorActivityStartTargetHelps;
        //boolean startTargetHelpsProfilesFilterSpinner = ApplicationPreferences.prefEditorActivityStartTargetHelpsProfilesFilterSpinner;
        //boolean startTargetHelpsEventsFilterSpinner = ApplicationPreferences.prefEditorActivityStartTargetHelpsEventsFilterSpinner;
        boolean startTargetHelpsRunStopIndicator = ApplicationPreferences.prefEditorActivityStartTargetHelpsRunStopIndicator;
        boolean startTargetHelpsBottomNavigation = ApplicationPreferences.prefEditorActivityStartTargetHelpsBottomNavigation;

        if (startTargetHelps || //startTargetHelpsProfilesFilterSpinner || startTargetHelpsEventsFilterSpinner ||
                startTargetHelpsRunStopIndicator || startTargetHelpsBottomNavigation ||
                ApplicationPreferences.prefEditorFragmentStartTargetHelpsDefaultProfile ||
                ApplicationPreferences.prefEditorProfilesFragmentStartTargetHelps ||
                ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelps ||
                ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsOrder ||
                ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsShowInActivator ||
                ApplicationPreferences.prefEditorEventsFragmentStartTargetHelps ||
                ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsOrderSpinner ||
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps ||
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder ||
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsStatus) {

            if (startTargetHelps || //startTargetHelpsProfilesFilterSpinner || startTargetHelpsEventsFilterSpinner ||
                    startTargetHelpsRunStopIndicator || startTargetHelpsBottomNavigation) {
                //Log.d("EditorActivity.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                Editor editor = ApplicationPreferences.getEditor(appContext);
                editor.putBoolean(PPApplication.PREF_EDITOR_ACTIVITY_START_TARGET_HELPS, false);

                //if (editorSelectedView == 0)
                //    editor.putBoolean(EditorActivity.PREF_START_TARGET_HELPS_PROFILES_FILTER_SPINNER, false);
                //else
                //    editor.putBoolean(EditorActivity.PREF_START_TARGET_HELPS_EVENTS_FILTER_SPINNER, false);

                editor.putBoolean(PPApplication.PREF_EDITOR_ACTIVITY_START_TARGET_HELPS_RUN_STOP_INDICATOR, false);
                editor.putBoolean(PPApplication.PREF_EDITOR_ACTIVITY_START_TARGET_HELPS_BOTTOM_NAVIGATION, false);
                editor.apply();
                ApplicationPreferences.prefEditorActivityStartTargetHelps = false;

                //if (editorSelectedView == 0)
                //    ApplicationPreferences.prefEditorActivityStartTargetHelpsProfilesFilterSpinner = false;
                //else
                //    ApplicationPreferences.prefEditorActivityStartTargetHelpsEventsFilterSpinner = false;

                ApplicationPreferences.prefEditorActivityStartTargetHelpsRunStopIndicator = false;
                ApplicationPreferences.prefEditorActivityStartTargetHelpsBottomNavigation = false;

                //TypedValue tv = new TypedValue();
                //getTheme().resolveAttribute(R.attr.colorAccent, tv, true);

                //final Display display = getWindowManager().getDefaultDisplay();

                //String appTheme = ApplicationPreferences.applicationTheme(appContext, true);
                int outerCircleColor = R.color.tabTargetHelpOuterCircleColor;
//                if (appTheme.equals("dark"))
//                    outerCircleColor = R.color.tabTargetHelpOuterCircleColor_dark;
                int targetCircleColor = R.color.tabTargetHelpTargetCircleColor;
//                if (appTheme.equals("dark"))
//                    targetCircleColor = R.color.tabTargetHelpTargetCircleColor_dark;
                int titleTextColor = R.color.tabTargetHelpTitleTextColor;
                int descriptionTextColor = R.color.tabTargetHelpDescriptionTextColor;
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
                    //if (editorSelectedView == 0)
                    //    startTargetHelpsProfilesFilterSpinner = false;
                    //else
                    //    startTargetHelpsEventsFilterSpinner = false;

                    startTargetHelpsRunStopIndicator = false;
                    startTargetHelpsBottomNavigation = false;

                    if (EventStatic.getGlobalEventsRunning(this)) {
                        /*targets.add(
                            TapTarget.forToolbarNavigationIcon(editorToolbar, getString(R.string.editor_activity_targetHelps_navigationIcon_title), getString(R.string.editor_activity_targetHelps_navigationIcon_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(true)
                                    .drawShadow(true)
                                    .id(1)
                        );*/
                        /*if (editorSelectedView == 0)
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
                        else
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
                        */
                        targets.add(
                                TapTarget.forToolbarOverflow(editorToolbar, getString(R.string.editor_activity_targetHelps_applicationMenu_title), getString(R.string.editor_activity_targetHelps_applicationMenu_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .titleTextColor(titleTextColor)
                                        .descriptionTextColor(descriptionTextColor)
                                        .textTypeface(Typeface.DEFAULT_BOLD)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(1)
                        );

                        int id = 2;
                        try {
                            targets.add(
                                    TapTarget.forToolbarMenuItem(editorToolbar, R.id.menu_restart_events, getString(R.string.editor_activity_targetHelps_restartEvents_title), getString(R.string.editor_activity_targetHelps_restartEvents_description))
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .titleTextColor(titleTextColor)
                                            .descriptionTextColor(descriptionTextColor)
                                            .textTypeface(Typeface.DEFAULT_BOLD)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(id)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplicationStatic.recordException(e);
                        }
                        try {
                            targets.add(
                                    TapTarget.forToolbarMenuItem(editorToolbar, R.id.menu_activity_log, getString(R.string.editor_activity_targetHelps_activityLog_title), getString(R.string.editor_activity_targetHelps_activityLog_description))
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .titleTextColor(titleTextColor)
                                            .descriptionTextColor(descriptionTextColor)
                                            .textTypeface(Typeface.DEFAULT_BOLD)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(id)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplicationStatic.recordException(e);
                        }
                        try {
                            targets.add(
                                    TapTarget.forToolbarMenuItem(editorToolbar, R.id.important_info_menu, getString(R.string.editor_activity_targetHelps_importantInfoButton_title), getString(R.string.editor_activity_targetHelps_importantInfoButton_description))
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .titleTextColor(titleTextColor)
                                            .descriptionTextColor(descriptionTextColor)
                                            .textTypeface(Typeface.DEFAULT_BOLD)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(id)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplicationStatic.recordException(e);
                        }

                        targets.add(
                                TapTarget.forView(eventsRunStopIndicator, getString(R.string.editor_activity_targetHelps_trafficLightIcon_title), getString(R.string.editor_activity_targetHelps_trafficLightIcon_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .titleTextColor(titleTextColor)
                                        .descriptionTextColor(descriptionTextColor)
                                        .textTypeface(Typeface.DEFAULT_BOLD)
                                        .tintTarget(false)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
/*
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
 */
                        targets.add(
                                TapTarget.forView(bottomNavigationView, getString(R.string.editor_activity_targetHelps_bottomNavigation_title),
                                        getString(R.string.editor_activity_targetHelps_bottomNavigation_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .titleTextColor(titleTextColor)
                                        .descriptionTextColor(descriptionTextColor)
                                        .textTypeface(Typeface.DEFAULT_BOLD)
                                        .tintTarget(false)
                                        .drawShadow(true)
                                        .transparentTarget(true)
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
                        /*if (editorSelectedView == 0)
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
                        else
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
                            );*/
                        targets.add(
                                TapTarget.forToolbarOverflow(editorToolbar, getString(R.string.editor_activity_targetHelps_applicationMenu_title), getString(R.string.editor_activity_targetHelps_applicationMenu_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .titleTextColor(titleTextColor)
                                        .descriptionTextColor(descriptionTextColor)
                                        .textTypeface(Typeface.DEFAULT_BOLD)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(1)
                        );

                        int id = 2;
                        try {
                            targets.add(
                                    TapTarget.forToolbarMenuItem(editorToolbar, R.id.menu_run_stop_events, getString(R.string.editor_activity_targetHelps_runStopEvents_title), getString(R.string.editor_activity_targetHelps_runStopEvents_description))
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .titleTextColor(titleTextColor)
                                            .descriptionTextColor(descriptionTextColor)
                                            .textTypeface(Typeface.DEFAULT_BOLD)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(id)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplicationStatic.recordException(e);
                        }
                        try {
                            targets.add(
                                    TapTarget.forToolbarMenuItem(editorToolbar, R.id.menu_activity_log, getString(R.string.editor_activity_targetHelps_activityLog_title), getString(R.string.editor_activity_targetHelps_activityLog_description))
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .titleTextColor(titleTextColor)
                                            .descriptionTextColor(descriptionTextColor)
                                            .textTypeface(Typeface.DEFAULT_BOLD)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(id)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplicationStatic.recordException(e);
                        }
                        try {
                            targets.add(
                                    TapTarget.forToolbarMenuItem(editorToolbar, R.id.important_info, getString(R.string.editor_activity_targetHelps_importantInfoButton_title), getString(R.string.editor_activity_targetHelps_importantInfoButton_description))
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .titleTextColor(titleTextColor)
                                            .descriptionTextColor(descriptionTextColor)
                                            .textTypeface(Typeface.DEFAULT_BOLD)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(id)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplicationStatic.recordException(e);
                        }

                        targets.add(
                                TapTarget.forView(eventsRunStopIndicator, getString(R.string.editor_activity_targetHelps_trafficLightIcon_title), getString(R.string.editor_activity_targetHelps_trafficLightIcon_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .titleTextColor(titleTextColor)
                                        .descriptionTextColor(descriptionTextColor)
                                        .textTypeface(Typeface.DEFAULT_BOLD)
                                        .tintTarget(false)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
/*
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
*/
                        targets.add(
                                TapTarget.forView(bottomNavigationView, getString(R.string.editor_activity_targetHelps_bottomNavigation_title),
                                        getString(R.string.editor_activity_targetHelps_bottomNavigation_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .titleTextColor(titleTextColor)
                                        .descriptionTextColor(descriptionTextColor)
                                        .textTypeface(Typeface.DEFAULT_BOLD)
                                        .tintTarget(false)
                                        .drawShadow(true)
                                        .transparentTarget(true)
                                        .id(id)
                        );
                        ++id;

                    }
                }
                /*if (startTargetHelpsProfilesFilterSpinner) {
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
                if (startTargetHelpsEventsFilterSpinner) {
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
                }*/
                if (startTargetHelpsRunStopIndicator) {
                    targets.add(
                            TapTarget.forView(eventsRunStopIndicator, getString(R.string.editor_activity_targetHelps_trafficLightIcon_title), getString(R.string.editor_activity_targetHelps_trafficLightIcon_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .titleTextColor(titleTextColor)
                                    .descriptionTextColor(descriptionTextColor)
                                    .textTypeface(Typeface.DEFAULT_BOLD)
                                    .tintTarget(false)
                                    .drawShadow(true)
                                    .id(1)
                    );
                }
                if (startTargetHelpsBottomNavigation) {
                    /*targets.add(
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
                    */
                    targets.add(
                            TapTarget.forView(bottomNavigationView, getString(R.string.editor_activity_targetHelps_bottomNavigation_title),
                                    getString(R.string.editor_activity_targetHelps_bottomNavigation_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .titleTextColor(titleTextColor)
                                    .descriptionTextColor(descriptionTextColor)
                                    .textTypeface(Typeface.DEFAULT_BOLD)
                                    .tintTarget(false)
                                    .drawShadow(true)
                                    .transparentTarget(true)
                                    .id(1)
                    );
                }

                sequence.targets(targets);

                sequence.listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        //targetHelpsSequenceStarted = false;

                        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                        editor.putBoolean(PPApplication.PREF_EDITOR_ACTIVITY_START_TARGET_HELPS_FINISHED, true);
                        editor.apply();
                        ApplicationPreferences.prefEditorActivityStartTargetHelpsFinished = true;

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
                        //targetHelpsSequenceStarted = false;
                        Editor editor = ApplicationPreferences.getEditor(appContext);
                        if (editorSelectedView == 0) {
                            editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_FRAGMENT_START_TARGET_HELPS, false);
                            editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_ADAPTER_START_TARGET_HELPS, false);
                            if (filterProfilesSelectedItem == DSI_PROFILES_SHOW_IN_ACTIVATOR)
                                editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_ADAPTER_START_TARGET_HELPS_ORDER, false);
                            if (filterProfilesSelectedItem == DSI_PROFILES_ALL)
                                editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_ADAPTER_START_TARGET_HELPS_SHOW_IN_ACTIVATOR, false);

                            editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_FRAGMENT_START_TARGET_HELPS_FINISHED, true);
                            //editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_FINISHED, true);

                            ApplicationPreferences.prefEditorProfilesFragmentStartTargetHelps = false;
                            ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelps = false;
                            if (filterProfilesSelectedItem == DSI_PROFILES_SHOW_IN_ACTIVATOR)
                                ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsOrder = false;
                            if (filterProfilesSelectedItem == DSI_PROFILES_ALL)
                                ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsShowInActivator = false;

                            ApplicationPreferences.prefEditorProfilesFragmentStartTargetHelpsFinished = true;
                            //ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsFinished = true;

                        }
                        else {
                            editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_FRAGMENT_START_TARGET_HELPS, false);
                            editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_ADAPTER_START_TARGET_HELPS, false);
                            if (filterEventsSelectedItem == DSI_EVENTS_START_ORDER)
                                editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_ADAPTER_START_TARGET_HELPS_ORDER, false);
                            editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_ADAPTER_START_TARGET_HELPS_STATUS, false);

                            editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_FRAGMENT_START_TARGET_HELPS_FINISHED, true);
                            //editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_FINISHED, true);

                            ApplicationPreferences.prefEditorEventsFragmentStartTargetHelps = false;
                            ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps = false;
                            if (filterEventsSelectedItem == DSI_EVENTS_START_ORDER)
                                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder = false;
                            ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsStatus = false;

                            ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsFinished = true;
                            //ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsFinished = true;
                        }
                        editor.apply();
                    }
                });
                sequence.continueOnCancel(true)
                        .considerOuterCircleCanceled(true);
                //targetHelpsSequenceStarted = true;

                editor = ApplicationPreferences.getEditor(appContext);
                editor.putBoolean(PPApplication.PREF_EDITOR_ACTIVITY_START_TARGET_HELPS_FINISHED, false);
                editor.apply();
                ApplicationPreferences.prefEditorActivityStartTargetHelpsFinished = false;

                sequence.start();
            }
            else {
                //Log.d("EditorActivity.showTargetHelps", "PREF_START_TARGET_HELPS=false");
                final Handler handler = new Handler(getMainLooper());
                final WeakReference<EditorActivity> activityWeakRef = new WeakReference<>(this);
                handler.postDelayed(() -> {
//                        PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EditorActivity.showTargetHelps");
                    EditorActivity activity = activityWeakRef.get();
                    if ((activity == null) || activity.isFinishing() || activity.isDestroyed())
                        return;

//                    PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] EditorActivity.showTargetHelps", "xxx");
                    Intent intent = new Intent(ACTION_SHOW_EDITOR_TARGET_HELPS_BROADCAST_RECEIVER);
                    LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(intent);
                    /*if (EditorActivity.getInstance() != null) {
                        Fragment fragment = EditorActivity.getInstance().getFragmentManager().findFragmentById(R.id.editor_list_container);
                        if (fragment != null) {
                            if (fragment instanceof EditorProfileListFragment)
                                ((EditorProfileListFragment) fragment).showTargetHelps();
                            else
                                ((EditorEventListFragment) fragment).showTargetHelps();
                        }
                    }*/
                }, 500);
            }
        }
    }

    static String getEmailBodyText(Context context) {
        String body;
        body = context.getString(R.string.important_info_email_body_device) + " " +
                Settings.Global.getString(context.getContentResolver(), Settings.Global.DEVICE_NAME) +
                " (" + Build.MODEL + ")" + StringConstants.STR_NEWLINE_WITH_SPACE;
        body = body + context.getString(R.string.important_info_email_body_android_version) + " " + Build.VERSION.RELEASE + StringConstants.STR_DOUBLE_NEWLINE_WITH_SPACE;
        return body;
    }

    private static class BackupAsyncTask extends AsyncTask<Void, Integer, Integer> {
        DocumentFile pickedDir;
        final Uri treeUri;

        final int requestCode;
        int ok = 1;

        private final WeakReference<EditorActivity> activityWeakRef;

        public BackupAsyncTask(int requestCode, Uri treeUri, EditorActivity activity) {
            this.treeUri = treeUri;
            this.requestCode = requestCode;

            this.activityWeakRef = new WeakReference<>(activity);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle(R.string.backup_settings_alert_title);

            LayoutInflater inflater = (activity.getLayoutInflater());
            View layout = inflater.inflate(R.layout.dialog_progress_bar, null);
            dialogBuilder.setView(layout);

            activity.backupProgressDialog = dialogBuilder.create();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                pickedDir = DocumentFile.fromTreeUri(activity.getApplicationContext(), treeUri);

                GlobalGUIRoutines.lockScreenOrientation(activity, false);
                activity.backupProgressDialog.setCancelable(false);
                activity.backupProgressDialog.setCanceledOnTouchOutside(false);
                if (!activity.isFinishing())
                    activity.backupProgressDialog.show();
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                if (pickedDir != null) {
                    if (pickedDir.canWrite()) {
                        if (requestCode == REQUEST_CODE_BACKUP_SETTINGS_2) {
                            // if directory exists, create new = "PhoneProfilesPlus (x)"
                            // create subdirectory
                            pickedDir = pickedDir.createDirectory(StringConstants.PHONE_PROFILES_PLUS);
                            if (pickedDir == null) {
                                // error for create directory
                                ok = -10;
                            }
                        }
                    } else {
                        // pickedDir is not writable
                        ok = -11;
                    }

                    if (ok == 1) {
                        if (pickedDir.canWrite()) {
                            File applicationDir = activity.getApplicationContext().getExternalFilesDir(null);

                            ok = copyToBackupDirectory(pickedDir, applicationDir, PPApplication.EXPORT_APP_PREF_FILENAME, activity.getApplicationContext());
                            if (ok == 1)
                                ok = copyToBackupDirectory(pickedDir, applicationDir, DatabaseHandler.EXPORT_DBFILENAME, activity.getApplicationContext());
                        } else {
                            // cannot copy backup files, pickedDir is not writable
                            ok = -12;
                        }
                    }

                } else {
                    // pickedDir is null
                    ok = -13;
                }
            } else {
                ok = -14;
            }
            return ok;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                if (!activity.isFinishing()) {
                    if ((activity.backupProgressDialog != null) && activity.backupProgressDialog.isShowing()) {
                        if (!activity.isDestroyed())
                            activity.backupProgressDialog.dismiss();
                        activity.backupProgressDialog = null;
                    }
                    GlobalGUIRoutines.unlockScreenOrientation(activity);
                }

                if (result <= 0) {
                    if (!activity.isFinishing()) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                activity.getString(R.string.backup_settings_alert_title),
                                activity.getString(R.string.backup_settings_error_on_backup) +
                                        " (" + activity.getString(R.string.error_code) + " " + result + ")",
                                activity.getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                activity
                        );

                        dialog.show();
                    }
                } else {
                    PPApplication.showToast(activity.getApplicationContext(), activity.getString(R.string.backup_settings_ok_backed_up), Toast.LENGTH_SHORT);
                }
            }
        }

        /** @noinspection BlockingMethodInNonBlockingContext*/
        private int copyToBackupDirectory(DocumentFile pickedDir, File applicationDir, String fileName, Context context) {
            DocumentFile oldFile = pickedDir.findFile(fileName);
            if (oldFile != null) {
                // delete old file
                if (!oldFile.delete()) {
                    // cannot delete existed file
                    return -1;
                }
            }
            // copy file
            DocumentFile newFile = pickedDir.createFile("application/x-binary", fileName);
            if (newFile != null) {
                try {
                    File exportFile = new File(applicationDir, fileName);
                    FileInputStream inStream = new FileInputStream(exportFile);
                    OutputStream outStream = context.getContentResolver().openOutputStream(newFile.getUri());
                    if (outStream != null) {
                        try {
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = inStream.read(buf)) > 0) {
                                outStream.write(buf, 0, len);
                            }
                        } finally {
                            inStream.close();
                            outStream.close();
                        }
                    }
                    else {
                        // cannot open fileName stream
                        return -2;
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                    return -3;
                }
            }
            else {
                // cannot create fileName
                return -4;
            }
            return 1;
        }

    }

    private static class RestoreAsyncTask extends AsyncTask<Void, Integer, Integer> {
        final boolean share;
        Uri treeUri;
        Uri fileUri;

        DocumentFile pickedDir;
        DocumentFile pickedFile;

        //int _requestCode;
        int ok = 1;

        private final WeakReference<EditorActivity> activityWeakRef;

        public RestoreAsyncTask(Uri treeFileUri, boolean share, EditorActivity activity) {
            this.share = share;
            if (share)
                this.fileUri = treeFileUri;
            else
                this.treeUri = treeFileUri;
            //this._requestCode = requestCode;

            this.activityWeakRef = new WeakReference<>(activity);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            if (share)
                dialogBuilder.setTitle(R.string.restore_shared_settings_alert_title);
            else
                dialogBuilder.setTitle(R.string.restore_settings_alert_title);

            LayoutInflater inflater = (activity.getLayoutInflater());
            View layout = inflater.inflate(R.layout.dialog_progress_bar, null);
            dialogBuilder.setView(layout);

            activity.restoreProgressDialog = dialogBuilder.create();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                if (share) {
                    pickedFile = DocumentFile.fromSingleUri(activity.getApplicationContext(), fileUri);
                } else {
                    pickedDir = DocumentFile.fromTreeUri(activity.getApplicationContext(), treeUri);
                }

                GlobalGUIRoutines.lockScreenOrientation(activity, false);
                activity.restoreProgressDialog.setCancelable(false);
                activity.restoreProgressDialog.setCanceledOnTouchOutside(false);
                if (!activity.isFinishing())
                    activity.restoreProgressDialog.show();
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                if (share) {
                    if (pickedFile != null) {
                        if (pickedFile.canRead()) {
                            File applicationDir = activity.getApplicationContext().getExternalFilesDir(null);

                            // file name in local storage will be PPApplication.SHARED_EXPORT_FILENAME + PPApplication.SHARED_EXPORT_FILEEXTENSION
                            ok = copySharedFile(pickedFile, applicationDir, activity.getApplicationContext());

                            if (ok == 1) {
                                // delete backup files
                                File importFile = new File(applicationDir, PPApplication.EXPORT_APP_PREF_FILENAME);
                                if (importFile.exists()) {
                                    // delete old file
                                    if (!importFile.delete())
                                        ok = -10;
                                }
                                if (ok == 1) {
                                    importFile = new File(applicationDir, DatabaseHandler.EXPORT_DBFILENAME);
                                    if (importFile.exists()) {
                                        // delete old file
                                        if (!importFile.delete())
                                            ok = -11;
                                    }
                                }

                                if (ok == 1) {
                                    // unzip shared file
                                    ZipManager zipManager = new ZipManager();
                                    File zipFile = new File(applicationDir, PPApplication.SHARED_EXPORT_FILENAME + PPApplication.SHARED_EXPORT_FILEEXTENSION);
                                    String destinationDir = applicationDir.getAbsolutePath();
                                    if (!destinationDir.endsWith("/"))
                                        destinationDir = destinationDir + "/";
                                    if (!zipManager.unzip(zipFile.getAbsolutePath(), destinationDir))
                                        ok = -12;
                                }
                            }

                        } else {
                            // pickedDir is not writable
                            ok = -13;
                        }
                    } else {
                        // pickedDir is null
                        ok = -14;
                    }
                } else {
                    if (pickedDir != null) {
                        if (pickedDir.canRead()) {
                            File applicationDir = activity.getApplicationContext().getExternalFilesDir(null);
                            ok = copyFromBackupDirectory(pickedDir, applicationDir, PPApplication.EXPORT_APP_PREF_FILENAME, activity.getApplicationContext());
                            if (ok == 1)
                                ok = copyFromBackupDirectory(pickedDir, applicationDir, DatabaseHandler.EXPORT_DBFILENAME, activity.getApplicationContext());
                        } else {
                            // pickedDir is not readable
                            ok = -10;
                        }
                    } else {
                        // pickedDir is null
                        ok = -11;
                    }
                }
            } else {
                ok = -20;
            }

            return ok;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                if (!activity.isFinishing()) {
                    if ((activity.restoreProgressDialog != null) && activity.restoreProgressDialog.isShowing()) {
                        if (!activity.isDestroyed())
                            activity.restoreProgressDialog.dismiss();
                        activity.restoreProgressDialog = null;
                    }
                    GlobalGUIRoutines.unlockScreenOrientation(activity);
                }

                if (result <= 0) {
                    if (!activity.isFinishing()) {
                        CharSequence title;
                        CharSequence message;
                        if (share) {
                            title = activity.getString(R.string.restore_shared_settings_alert_title);
                            message = activity.getString(R.string.restore_shared_settings_error_on_backup) +
                                    " (" + activity.getString(R.string.error_code) + " " + result + ")";
                        } else {
                            title = activity.getString(R.string.restore_settings_alert_title);
                            message = activity.getString(R.string.restore_settings_error_on_backup) +
                                    " (" + activity.getString(R.string.error_code) + " " + result + ")";
                        }
                        PPAlertDialog dialog = new PPAlertDialog(
                                title,
                                message,
                                activity.getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                activity
                        );

                        dialog.show();
                    }
                } else {
                    if (share)
                        PPApplication.showToast(activity.getApplicationContext(), activity.getString(R.string.restore_shared_settings_ok_backed_up), Toast.LENGTH_SHORT);
                    else
                        PPApplication.showToast(activity.getApplicationContext(), activity.getString(R.string.restore_settings_ok_backed_up), Toast.LENGTH_SHORT);

                    activity.doImportData();
                }
            }
        }

        /** @noinspection BlockingMethodInNonBlockingContext*/
        private int copyFromBackupDirectory(DocumentFile pickedDir, File applicationDir, String fileName, Context context) {
//            Log.e("EditorActivity.copyFromBackupDirectory", "applicationDir="+applicationDir);
//            Log.e("EditorActivity.copyFromBackupDirectory", "fileName="+fileName);

            File importFile = new File(applicationDir, fileName);
            if (importFile.exists()) {
                // delete old file
                if (!importFile.delete()) {
                    // cannot delete existed file
                    return -1;
                }
            }
            // copy file
            DocumentFile inputFile = pickedDir.findFile(fileName);
            if (inputFile != null) {
//                Log.e("EditorActivity.copyFromBackupDirectory", "inputFile="+inputFile.getUri().getPath());
                try {
                    FileOutputStream outStream = new FileOutputStream(importFile);
                    InputStream inStream = context.getContentResolver().openInputStream(inputFile.getUri());
                    if (inStream != null) {
                        try {
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = inStream.read(buf)) > 0) {
                                outStream.write(buf, 0, len);
                            }
                        } finally {
                            inStream.close();
                            outStream.close();
                        }
                    }
                    else {
                        // cannot open fileName stream
//                        Log.e("EditorActivity.copyFromBackupDirectory", "cannot open fileName stream");
                        return -2;
                    }
                } catch (Exception e) {
//                    Log.e("EditorActivity.copyFromBackupDirectory", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                    return -3;
                }
            }
            else {
                // fileName not found
//                Log.e("EditorActivity.copyFromBackupDirectory", "cannot create fileName");
                return -4;
            }
            return 1;
        }

        /** @noinspection BlockingMethodInNonBlockingContext*/
        private int copySharedFile(DocumentFile pickedFile, File applicationDir, Context context) {
            // delete all zip files in local storage
            File sd = context.getApplicationContext().getExternalFilesDir(null);
            File[] oldZipFiles = sd.listFiles();
            if (oldZipFiles != null) {
                for (File f : oldZipFiles) {
                    if (f.getName().startsWith(PPApplication.SHARED_EXPORT_FILENAME)) {
                        if (!f.delete())
                            return -1;
                    }
                }
            }
            // copy file
            //DocumentFile inputFile = pickedFile;
            if (pickedFile != null) {
                try {
                    File importFile = new File(applicationDir, PPApplication.SHARED_EXPORT_FILENAME + PPApplication.SHARED_EXPORT_FILEEXTENSION);
                    FileOutputStream outStream = new FileOutputStream(importFile);
                    InputStream inStream = context.getContentResolver().openInputStream(pickedFile.getUri());
                    if (inStream != null) {
                        try {
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = inStream.read(buf)) > 0) {
                                outStream.write(buf, 0, len);
                            }
                        } finally {
                            inStream.close();
                            outStream.close();
                        }
                    }
                    else {
                        // cannot open fileName stream
                        return -2;
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                    return -3;
                }
            }
            else {
                // cannot create fileName
                return -4;
            }
            return 1;
        }

    }

    private static class ImportAsyncTask extends AsyncTask<Void, Integer, Integer> {
        private final DataWrapper _dataWrapper;
        private int dbError = DatabaseHandler.IMPORT_OK;
        private boolean appSettingsError = false;
        //private boolean sharedProfileError = false;

        private final WeakReference<EditorActivity> activityWeakRef;

        public ImportAsyncTask(EditorActivity activity) {
            this.activityWeakRef = new WeakReference<>(activity);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle(R.string.import_profiles_alert_title);

            LayoutInflater inflater = (activity.getLayoutInflater());
            View layout = inflater.inflate(R.layout.dialog_progress_bar, null);
            dialogBuilder.setView(layout);

            activity.importProgressDialog = dialogBuilder.create();

//                    importProgressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                        @Override
//                        public void onShow(DialogInterface dialog) {
//                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (positive != null) positive.setAllCaps(false);
//                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                            if (negative != null) negative.setAllCaps(false);
//                        }
//                    });

            _dataWrapper = activity.getDataWrapper();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                doImport = true;

                GlobalGUIRoutines.lockScreenOrientation(activity, false);
                activity.importProgressDialog.setCancelable(false);
                activity.importProgressDialog.setCanceledOnTouchOutside(false);
                if (!activity.isFinishing())
                    activity.importProgressDialog.show();

                Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                if (fragment != null) {
                    if (fragment instanceof EditorProfileListFragment)
                        ((EditorProfileListFragment) fragment).removeAdapter();
                    else
                        ((EditorEventListFragment) fragment).removeAdapter();
                }
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                if (_dataWrapper != null) {
                    PPApplicationStatic.exitApp(false, _dataWrapper.context, _dataWrapper, null, false, true, false);

                    //File sd = Environment.getExternalStorageDirectory();
                    //File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                    File sd = activity.getApplicationContext().getExternalFilesDir(null);

                    /*try {
                        File exportPath = new File(sd, _applicationDataPath);
                        if (exportPath.exists()) {
                            exportPath.setReadable(true, false);
                        }
                        if (exportPath.exists()) {
                            exportPath.setWritable(true, false);
                        }
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }*/

                    // import application preferences must be first,
                    // because in DatabaseHandler.importDB is recompute of volumes in profiles
                    //File exportFile = new File(sd, _applicationDataPath + "/" + PPApplication.EXPORT_APP_PREF_FILENAME);
                    File exportFile = new File(sd, PPApplication.EXPORT_APP_PREF_FILENAME);
                    appSettingsError = !activity.importApplicationPreferences(exportFile/*, 1*/);
                    //exportFile = new File(sd, _applicationDataPath + "/" + GlobalGUIRoutines.EXPORT_DEF_PROFILE_PREF_FILENAME);
                    //exportFile = new File(sd, GlobalGUIRoutines.EXPORT_DEF_PROFILE_PREF_FILENAME);
                    //if (exportFile.exists())
                    //    sharedProfileError = !importApplicationPreferences(exportFile, 2);

                    //dbError = DatabaseHandler.getInstance(_dataWrapper.context).importDB(_applicationDataPath);
                    dbError = DatabaseHandler.getInstance(_dataWrapper.context).importDB(/*_applicationDataPath*/);
                    if (dbError == DatabaseHandler.IMPORT_OK) {
                        DatabaseHandler.getInstance(_dataWrapper.context).updateAllEventsStatus(Event.ESTATUS_RUNNING, Event.ESTATUS_PAUSE);
                        DatabaseHandler.getInstance(_dataWrapper.context).updateAllEventsSensorsPassed(EventPreferences.SENSOR_PASSED_WAITING);
                        DatabaseHandler.getInstance(_dataWrapper.context).deactivateProfile();
                        DatabaseHandler.getInstance(_dataWrapper.context).unblockAllEvents();
                        DatabaseHandler.getInstance(_dataWrapper.context).disableNotAllowedPreferences();
                        //this.dataWrapper.invalidateProfileList();
                        //this.dataWrapper.invalidateEventList();
                        //this.dataWrapper.invalidateEventTimelineList();
                        EventStatic.setEventsBlocked(_dataWrapper.context, false);
                        DatabaseHandler.getInstance(_dataWrapper.context).unblockAllEvents();
                        EventStatic.setForceRunEventRunning(_dataWrapper.context, false);
                    }

                    if (!appSettingsError) {
                        /*Editor editor = ApplicationPreferences.preferences.edit();
                        editor.putInt(EDITOR_PROFILES_VIEW_SELECTED_ITEM, 0);
                        editor.putInt(EDITOR_EVENTS_VIEW_SELECTED_ITEM, 0);
                        editor.putInt(EditorEventListFragment.SP_EDITOR_ORDER_SELECTED_ITEM, 0);
                        editor.apply();*/

                        Permissions.setAllShowRequestPermissions(_dataWrapper.context, true);

                        //WifiBluetoothScanner.setShowEnableLocationNotification(_dataWrapper.context, true, WifiBluetoothScanner.SCANNER_TYPE_WIFI);
                        //WifiBluetoothScanner.setShowEnableLocationNotification(_dataWrapper.context, true, WifiBluetoothScanner.SCANNER_TYPE_BLUETOOTH);
                        //MobileCellsScanner.setShowEnableLocationNotification(_dataWrapper.context, true);
                    }

                    if ((dbError == DatabaseHandler.IMPORT_OK) && (!(appSettingsError/* || sharedProfileError*/)))
                        return 1;
                    else
                        return 0;
                } else
                    return 0;
            }
            else
                return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                doImport = false;

                if (!activity.isFinishing()) {
                    if ((activity.importProgressDialog != null) && activity.importProgressDialog.isShowing()) {
                        if (!activity.isDestroyed())
                            activity.importProgressDialog.dismiss();
                        activity.importProgressDialog = null;
                    }
                    GlobalGUIRoutines.unlockScreenOrientation(activity);
                }

                if (_dataWrapper != null) {
                    // clear shared preferences for last activated profile
                    //Profile profile = DataWrapper.getNonInitializedProfile("", null, 0);
                    //Profile.saveProfileToSharedPreferences(profile, _dataWrapper.context);
                    PPApplicationStatic.setLastActivatedProfile(_dataWrapper.context, 0);

//                    PPApplicationStatic.logE("[PPP_NOTIFICATION] EditorActivity.importAsyncTask", "call of updateGUI");
                    PPApplication.updateGUI(true, false, _dataWrapper.context);

                    PPApplicationStatic.setApplicationStarted(_dataWrapper.context, true);
                    Intent serviceIntent = new Intent(_dataWrapper.context, PhoneProfilesService.class);
                    //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                    //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
                    serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                    serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_SHIZUKU_START, false);
//                    PPApplicationStatic.logE("[START_PP_SERVICE] EditorActivity.doImportData", "xxx");
                    PPApplicationStatic.startPPService(activity, serviceIntent, true);
                }

                if ((_dataWrapper != null) && (dbError == DatabaseHandler.IMPORT_OK) && (!(appSettingsError/* || sharedProfileError*/))) {
                    // restart events
                    //if (Event.getGlobalEventsRunning(this.dataWrapper.context)) {
                    //    this.dataWrapper.restartEventsWithDelay(3, false, false, DatabaseHandler.ALTYPE_UNDEFINED);
                    //}

                    PPApplicationStatic.addActivityLog(_dataWrapper.context, PPApplication.ALTYPE_DATA_IMPORT, null, null, "");

                    // toast notification
                    if (!activity.isFinishing())
                        PPApplication.showToast(_dataWrapper.context.getApplicationContext(),
                                activity.getString(R.string.toast_import_ok),
                                Toast.LENGTH_SHORT);

                    // refresh activity
                    if (!activity.isFinishing())
                        GlobalGUIRoutines.reloadActivity(activity, true);

                    DrawOverAppsPermissionNotification.showNotification(_dataWrapper.context, true);
                    IgnoreBatteryOptimizationNotification.showNotification(_dataWrapper.context, true);
                    DNDPermissionNotification.showNotification(_dataWrapper.context, true);

                    PPApplicationStatic.setCustomKey(PPApplication.CRASHLYTICS_LOG_RESTORE_BACKUP_OK, true);
                } else {
                    int appSettingsResult = 1;
                    if (appSettingsError) appSettingsResult = 0;
                    //int sharedProfileResult = 1;
                    //if (sharedProfileError) sharedProfileResult = 0;
                    if (!activity.isFinishing())
                        activity.importExportErrorDialog(IMPORTEXPORT_IMPORT, dbError, appSettingsResult/*, sharedProfileResult*/);

                    PPApplicationStatic.setCustomKey(PPApplication.CRASHLYTICS_LOG_RESTORE_BACKUP_OK, false);
                }
            }
        }
    }

    private static class ExportAsyncTask extends AsyncTask<Void, Integer, Integer> {
        private final DataWrapper dataWrapper;
        private boolean runStopEvents;

        private final WeakReference<EditorActivity> activityWeakRef;
        final boolean email;
        final boolean toAuthor;
        final boolean share;
        final boolean deleteGeofences;
        final boolean deleteWifiSSIDs;
        final boolean deleteBluetoothNames;
        final boolean deleteMobileCells;
        final boolean deleteCall;
        final boolean deleteSMS;
        final boolean deleteNotification;
        final boolean deletePhoneCalls;
        final boolean deleteCallScreening;
        File zipFile = null;

        public ExportAsyncTask(final boolean email, final boolean toAuthor, final boolean share,
                               final boolean deleteGeofences, final boolean deleteWifiSSIDs,
                               final boolean deleteBluetoothNames, final boolean deleteMobileCells,
                               final boolean deleteCall, final boolean deleteSMS,
                               final boolean deleteNotification, final boolean deletePhoneCalls,
                               final boolean deleteCallScreening,
                               EditorActivity activity) {
            this.activityWeakRef = new WeakReference<>(activity);
            this.email = email;
            this.toAuthor = toAuthor;
            this.share = share;
            this.deleteGeofences = deleteGeofences;
            this.deleteWifiSSIDs = deleteWifiSSIDs;
            this.deleteBluetoothNames = deleteBluetoothNames;
            this.deleteMobileCells = deleteMobileCells;
            this.deleteCall = deleteCall;
            this.deleteSMS = deleteSMS;
            this.deleteNotification = deleteNotification;
            this.deletePhoneCalls = deletePhoneCalls;
            this.deleteCallScreening = deleteCallScreening;

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle(R.string.export_profiles_alert_title);

            LayoutInflater inflater = (activity.getLayoutInflater());
            View layout = inflater.inflate(R.layout.dialog_progress_bar, null);
            dialogBuilder.setView(layout);

            activity.exportProgressDialog = dialogBuilder.create();

//                    exportProgressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                        @Override
//                        public void onShow(DialogInterface dialog) {
//                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (positive != null) positive.setAllCaps(false);
//                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                            if (negative != null) negative.setAllCaps(false);
//                        }
//                    });

            this.dataWrapper = activity.getDataWrapper();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                GlobalGUIRoutines.lockScreenOrientation(activity, false);
                activity.exportProgressDialog.setCancelable(false);
                activity.exportProgressDialog.setCanceledOnTouchOutside(false);
                if (!activity.isFinishing())
                    activity.exportProgressDialog.show();

                runStopEvents = EventStatic.getGlobalEventsRunning(dataWrapper.context);
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                if (this.dataWrapper != null) {
//                    PPApplicationStatic.logE("[SYNCHRONIZED] EditorActivity.ExportAsyncTask", "(1) PPApplication.applicationStartedMutex");
                    synchronized (PPApplication.applicationStartedMutex) {
                        PPApplication.exportIsRunning = true;
                    }
                    GlobalUtils.sleep(3000); // wait 3 seconds for end of running things

                    File sd = activity.getApplicationContext().getExternalFilesDir(null);

                    int ret = DatabaseHandler.getInstance(this.dataWrapper.context).exportDB(
                            this.deleteGeofences, this.deleteWifiSSIDs,
                            this.deleteBluetoothNames, this.deleteMobileCells,
                            this.deleteCall, this.deleteSMS, this.deleteNotification,
                            this.deletePhoneCalls, this.deleteCallScreening
                    );
                    if (ret == 1) {
                        //File exportFile = new File(sd, PPApplication.EXPORT_PATH + "/" + PPApplication.EXPORT_APP_PREF_FILENAME);
                        File exportFile = new File(sd, PPApplication.EXPORT_APP_PREF_FILENAME);
                        //noinspection StatementWithEmptyBody
                        if (activity.exportApplicationPreferences(exportFile, runStopEvents/*, 1*/)) {
                            /*exportFile = new File(sd, PPApplication.EXPORT_PATH + "/" + GlobalGUIRoutines.EXPORT_DEF_PROFILE_PREF_FILENAME);
                            if (!exportApplicationPreferences(exportFile, 2))
                                ret = 0;*/
                            //ret = 1;
                        } else
                            ret = 0;
                    }

                    if ((ret == 1) && (this.share)) {
                        String[] filesToZip = new String[2];

                        try {
                            // delete all zip files in local storage
                            sd = activity.getApplicationContext().getExternalFilesDir(null);
                            File[] oldZipFiles = sd.listFiles();
                            if (oldZipFiles != null) {
                                for (File f : oldZipFiles) {
                                    if (f.getName().startsWith(PPApplication.SHARED_EXPORT_FILENAME)) {
                                        //noinspection ResultOfMethodCallIgnored
                                        f.delete();
                                    }
                                }
                            }

                            CharSequence dateTime = android.text.format.DateFormat.format("yyyyMMddHHmmss", new java.util.Date());

                            String fileName = PPApplication.SHARED_EXPORT_FILENAME + "_" + dateTime +
                                                PPApplication.SHARED_EXPORT_FILEEXTENSION;
                            zipFile = new File(sd, fileName);
                            String zipFilePath = zipFile.getAbsolutePath();

                            filesToZip[0] = new File(sd, DatabaseHandler.EXPORT_DBFILENAME).getAbsolutePath();
                            filesToZip[1] = new File(sd, PPApplication.EXPORT_APP_PREF_FILENAME).getAbsolutePath();

                            ZipManager zipManager = new ZipManager();
                            if (!zipManager.zip(filesToZip, zipFilePath))
                                ret = 0;
                        } catch (Exception e) {
                            //PPApplicationStatic.recordException(e);
                            Log.e("EditorActivity.doInBackground", Log.getStackTraceString(e));
                            ret = 0;
                        }

                    }

                    PPApplicationStatic.addActivityLog(this.dataWrapper.context, PPApplication.ALTYPE_DATA_EXPORT, null, null, "");

//                    PPApplicationStatic.logE("[SYNCHRONIZED] EditorActivity.ExportAsyncTask", "(2) PPApplication.applicationStartedMutex");
                    synchronized (PPApplication.applicationStartedMutex) {
                        PPApplication.exportIsRunning = false;
                    }

                    return ret;
                } else
                    return 0;
            } else
                return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                if (!activity.isFinishing()) {
                    if ((activity.exportProgressDialog != null) && activity.exportProgressDialog.isShowing()) {
                        if (!activity.isDestroyed())
                            activity.exportProgressDialog.dismiss();
                        activity.exportProgressDialog = null;
                    }
                    GlobalGUIRoutines.unlockScreenOrientation(activity);
                }

                if (result != 1) {
                    try {
                        // delete both files
                        File sd = activity.getApplicationContext().getExternalFilesDir(null);
                        File importFile = new File(sd, PPApplication.EXPORT_APP_PREF_FILENAME);
                        if (importFile.exists()) {
                            //noinspection ResultOfMethodCallIgnored
                            importFile.delete();
                        }
                        importFile = new File(sd, DatabaseHandler.EXPORT_DBFILENAME);
                        if (importFile.exists()) {
                            //noinspection ResultOfMethodCallIgnored
                            importFile.delete();
                        }
                    } catch (Exception ignored) {}
                }

                if ((dataWrapper != null) && (result == 1)) {

                    Context context = this.dataWrapper.context.getApplicationContext();
                    // toast notification
                    if (!activity.isFinishing())
                        PPApplication.showToast(context, activity.getString(R.string.toast_export_ok), Toast.LENGTH_SHORT);

                    //dataWrapper.restartEventsWithRescan(false, false, true, false, false, false);

                    if (email) {
                        // email backup

                        ArrayList<Uri> uris = new ArrayList<>();

                        try {
                            //File sd = Environment.getExternalStorageDirectory();
                            //File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                            File sd = context.getExternalFilesDir(null);

                            //File exportedDB = new File(sd, PPApplication.EXPORT_PATH + "/" + DatabaseHandler.EXPORT_DBFILENAME);
                            File exportedDB = new File(sd, DatabaseHandler.EXPORT_DBFILENAME);
                            Uri fileUri = FileProvider.getUriForFile(activity, PPApplication.PACKAGE_NAME + ".provider", exportedDB);
                            uris.add(fileUri);

                            //File appSettingsFile = new File(sd, PPApplication.EXPORT_PATH + "/" + PPApplication.EXPORT_APP_PREF_FILENAME);
                            File appSettingsFile = new File(sd, PPApplication.EXPORT_APP_PREF_FILENAME);
                            fileUri = FileProvider.getUriForFile(activity, PPApplication.PACKAGE_NAME + ".provider", appSettingsFile);
                            uris.add(fileUri);
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }

                        String emailAddress = "";
                        if (toAuthor)
                            emailAddress = StringConstants.AUTHOR_EMAIL;
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                StringConstants.INTENT_DATA_MAIL_TO, emailAddress, null));

                        String packageVersion = "";
                        try {
                            PackageInfo pInfo = context.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                            packageVersion = " - v" + pInfo.versionName + " (" + PPApplicationStatic.getVersionCode(pInfo) + ")";
                        } catch (Exception e) {
                            //Log.e("EditorActivity.ExportAsyncTask.onPostExecute", Log.getStackTraceString(e));
                            PPApplicationStatic.recordException(e);
                        }
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, StringConstants.PHONE_PROFILES_PLUS + packageVersion + " - " + activity.getString(R.string.export_data_email_subject));
                        emailIntent.putExtra(Intent.EXTRA_TEXT, getEmailBodyText(activity));
                        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentActivities(emailIntent, 0);
                        List<LabeledIntent> intents = new ArrayList<>();
                        for (ResolveInfo info : resolveInfo) {
                            //Log.e("EditorActivity.ExportAsyncTask.onPostExecute", "packageName="+info.activityInfo.packageName);
                            //Log.e("EditorActivity.ExportAsyncTask.onPostExecute", "name="+info.activityInfo.name);
                            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                            intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                            if (!emailAddress.isEmpty())
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                            intent.putExtra(Intent.EXTRA_SUBJECT, StringConstants.PHONE_PROFILES_PLUS + packageVersion + " - " + activity.getString(R.string.export_data_email_subject));
                            intent.putExtra(Intent.EXTRA_TEXT, getEmailBodyText(activity));
                            intent.setType(StringConstants.MINE_TYPE_ALL); // gmail will only match with type set
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris); //ArrayList<Uri> of attachment Uri's
                            intents.add(new LabeledIntent(intent, info.activityInfo.packageName, info.loadLabel(context.getPackageManager()), info.icon));
                        }
                        //Log.e("EditorActivity.ExportAsyncTask.onPostExecute", "intents.size()="+intents.size());
                        if (!intents.isEmpty()) {
                            try {
                                Intent chooser = Intent.createChooser(new Intent(Intent.ACTION_CHOOSER), context.getString(R.string.email_chooser));
                                chooser.putExtra(Intent.EXTRA_INTENT, intents.get(0));
                                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new LabeledIntent[0]));
                                activity.startActivity(chooser);
                            } catch (Exception e) {
                                //Log.e("EditorActivity.ExportAsyncTask.onPostExecute", Log.getStackTraceString(e));
                                PPApplicationStatic.recordException(e);
                            }
                        }
                    } else
                    if (share) {
                        //File sd = context.getExternalFilesDir(null);
                        //File zipFile = new File(sd, PPApplication.SHARED_EXPORT_LOCALFILENAME);
                        if (zipFile.exists()) {
                            Uri zipFileUri = FileProvider.getUriForFile(activity, PPApplication.PACKAGE_NAME + ".provider", zipFile);

                            ShareCompat.IntentBuilder shareBuilder = new ShareCompat.IntentBuilder(activity);
                            shareBuilder.setType("application/zip")
                                    .setStream(zipFileUri)
                                    .setChooserTitle(R.string.share_settings_choose_bar);

                            Intent intent = shareBuilder.createChooserIntent()
                                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            //activity.startActivity(intent);
                            //noinspection deprecation
                            activity.startActivityForResult(intent, REQUEST_CODE_SHARE_SETTINGS);
                        }
                    } else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                        LayoutInflater inflater = (activity).getLayoutInflater();
                        View layout = inflater.inflate(R.layout.dialog_backup_settings_alert, null);
                        dialogBuilder.setView(layout);
                        dialogBuilder.setTitle(R.string.backup_settings_alert_title);

                        boolean createPPPSubfolder = ApplicationPreferences.getSharedPreferences(context).getBoolean(PREF_BACKUP_CREATE_PPP_SUBFOLDER, true);

                        final TextView rewriteInfo = layout.findViewById(R.id.backup_settings_alert_dialog_rewrite_files_info);
                        rewriteInfo.setEnabled(!createPPPSubfolder);

                        final CheckBox checkBox = layout.findViewById(R.id.backup_settings_alert_dialog_checkBox);
                        checkBox.setChecked(createPPPSubfolder);

                        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> rewriteInfo.setEnabled(!isChecked));
                        dialogBuilder.setPositiveButton(R.string.alert_button_yes, (dialog, which) -> {
                            boolean ok = false;
                            try {

                                boolean _createPPPSubfolder = checkBox.isChecked();
                                Editor editor = ApplicationPreferences.getEditor(context);
                                editor.putBoolean(PREF_BACKUP_CREATE_PPP_SUBFOLDER, _createPPPSubfolder);
                                editor.apply();

                                Intent intent;
                                if (Build.VERSION.SDK_INT >= 29) {
                                    StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                                    intent = sm.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
                                } else {
                                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                }
                                // not supported by ACTION_OPEN_DOCUMENT_TREE
                                //intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);

                                //intent.putExtra("android.content.extra.SHOW_ADVANCED",true);
                                //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, PPApplication.backupFolderUri);
                                if (_createPPPSubfolder)
                                    //noinspection deprecation
                                    activity.startActivityForResult(intent, REQUEST_CODE_BACKUP_SETTINGS_2);
                                else
                                    //noinspection deprecation
                                    activity.startActivityForResult(intent, REQUEST_CODE_BACKUP_SETTINGS);
                                ok = true;
                            } catch (Exception e) {
                                //PPApplicationStatic.recordException(e);
                            }
                            if (!ok) {
                                PPAlertDialog _dialog = new PPAlertDialog(
                                        activity.getString(R.string.backup_settings_alert_title),
                                        activity.getString(R.string.directory_tree_activity_not_found_alert),
                                        activity.getString(android.R.string.ok),
                                        null,
                                        null, null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        true, true,
                                        false, false,
                                        true,
                                        activity
                                );

                                if (!activity.isFinishing())
                                    _dialog.show();
                            }
                        });
                        dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
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

                        if (!activity.isFinishing())
                            dialog.show();

                    }

                } else {
                    if (!activity.isFinishing())
                        activity.importExportErrorDialog(IMPORTEXPORT_EXPORT, 0, 0/*, 0*/);
                }
            }
        }

    }

/*
    private static class Language {
        String language;
        String country;
        String script;
        String name;
    }

    private static class LanguagesComparator implements Comparator<Language> {

        public int compare(Language lhs, Language rhs) {
            return PPApplication.collator.compare(lhs.name, rhs.name);
        }
    }
*/

    @Override
    public void refreshGUIFromListener(Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] EditorActivity.refreshGUIBroadcastReceiver", "xxx");
        //boolean refresh = intent.getBooleanExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH, true);

        if (intent.getBooleanExtra(RefreshActivitiesBroadcastReceiver.EXTRA_RELOAD_ACTIVITY, false))
            GlobalGUIRoutines.reloadActivity(this, true);
        else {
            boolean refreshIcons = intent.getBooleanExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH_ICONS, false);
            long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
            long eventId = intent.getLongExtra(PPApplication.EXTRA_EVENT_ID, 0);
            // not change selection in editor if refresh is outside editor
            refreshGUI(/*refresh,*//* true,*/  refreshIcons, false, profileId, eventId);
        }
    }

    @Override
    public void showTargetHelpsFromListener(Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] EditorActivity.showTargetHelpsBroadcastReceiver", "xxx");
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
        if (fragment != null) {
            if (fragment instanceof EditorProfileListFragment)
                ((EditorProfileListFragment) fragment).showTargetHelps();
            else
                ((EditorEventListFragment) fragment).showTargetHelps();
        }
    }

    @Override
    public void finishActivityFromListener(Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] EditorActivity.finishBroadcastReceiver", "xxx");
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(PPApplication.ACTION_FINISH_ACTIVITY)) {
                String what = intent.getStringExtra(PPApplication.EXTRA_WHAT_FINISH);
                if (what.equals(StringConstants.EXTRA_EDITOR)) {
                    try {
                        setResult(Activity.RESULT_CANCELED);
                        finishAffinity();
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            }
        }
    }

}
