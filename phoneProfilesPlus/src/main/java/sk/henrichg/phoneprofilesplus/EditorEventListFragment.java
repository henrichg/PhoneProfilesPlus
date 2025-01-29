package sk.henrichg.phoneprofilesplus;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/** @noinspection ExtractMethodRecommender*/
public class EditorEventListFragment extends Fragment
                                        implements OnStartDragItemListener {

    DataWrapper activityDataWrapper;
    EditorActivity activity;

    private View rootView;
    LinearLayout activatedProfileHeader;
    RecyclerView listView;
    private TextView activeProfileName;
    private ImageView activeProfileIcon;
    Toolbar bottomToolbar;
    Toolbar editorSubToolbar;
    RelativeLayout viewNoData;
    private LinearLayout progressBar;
    private AppCompatSpinner orderSpinner;
    private ImageView profilePrefIndicatorImageView;

    private EditorEventListAdapter eventListAdapter;
    private ItemTouchHelper itemTouchHelper;

    private LoadEventListAsyncTask loadAsyncTask = null;
    private RefreshGUIAsyncTask refreshGUIAsyncTask = null;
    private UpdateHeaderAsyncTask updateHeaderAsyncTask = null;

    Event scrollToEvent = null;

//    private ValueAnimator hideAnimatorHeader;
//    private ValueAnimator showAnimatorHeader;
//    private int headerHeight;
//    private ValueAnimator hideAnimatorBottomBar;
//    private ValueAnimator showAnimatorBottomBar;
//    private int bottomBarHeight;

    private int orderSelectedItem = 0;

    static final String BUNDLE_FILTER_TYPE = "filter_type";
    //static final String BUNDLE_ORDER_TYPE_ = "order_type";
    //static final String BUNDLE_START_TARGET_HELPS = "start_target_helps";

    static final int FILTER_TYPE_ALL = 0;
    static final int FILTER_TYPE_RUNNING = 1;
    static final int FILTER_TYPE_PAUSED = 2;
    static final int FILTER_TYPE_STOPPED = 3;
    static final int FILTER_TYPE_START_ORDER = 4;
    static final int FILTER_TYPE_NOT_STOPPED = 5;

    private static final int ORDER_TYPE_START_ORDER = 0;
    private static final int ORDER_TYPE_EVENT_NAME = 1;
    private static final int ORDER_TYPE_START_PROFILE_NAME = 2;
    private static final int ORDER_TYPE_PRIORITY = 3;
    private static final int ORDER_TYPE_END_PROFILE_NAME = 4;

    //boolean targetHelpsSequenceStarted;

    private int filterType = FILTER_TYPE_ALL;
    private int orderType = ORDER_TYPE_EVENT_NAME;

    /**
     * The fragment's current callback objects
     */
    private OnStartEventPreferences onStartEventPreferencesCallback = sDummyOnStartEventPreferencesCallback;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified.
     */
    // invoked when start profile preference fragment/activity needed
    interface OnStartEventPreferences {
        void onStartEventPreferences(Event event, int editMode, int predefinedEventIndex/*, boolean startTargetHelps*/);
    }

    /*, boolean startTargetHelps*/
    /**
     * A dummy implementation of the Callbacks interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static final OnStartEventPreferences sDummyOnStartEventPreferencesCallback = (event, editMode, predefinedEventIndex) -> {
    };

    public EditorEventListFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        onStartEventPreferencesCallback = (OnStartEventPreferences) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        onStartEventPreferencesCallback = sDummyOnStartEventPreferencesCallback;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        //noinspection deprecation
        setRetainInstance(true);

        filterType = getArguments() != null ? 
                getArguments().getInt(BUNDLE_FILTER_TYPE, EditorEventListFragment.FILTER_TYPE_START_ORDER) :
                    EditorEventListFragment.FILTER_TYPE_START_ORDER;
//        orderType = getArguments() != null ?
//                getArguments().getInt(ORDER_TYPE_ARGUMENT, EditorEventListFragment.ORDER_TYPE_START_ORDER) :
//                    EditorEventListFragment.ORDER_TYPE_START_ORDER;
        orderType = getEventsOrderType();

        //Log.d("EditorEventListFragment.onCreate","filterType="+filterType);
        //Log.d("EditorEventListFragment.onCreate","orderType="+orderType);

        //noinspection ConstantConditions
        activityDataWrapper = new DataWrapper(getActivity().getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
        //loadAsyncTask = new LoadEventListAsyncTask(this, filterType, orderType);

        activity = (EditorActivity) getActivity();

        //getActivity().getIntent();

        //noinspection deprecation
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_editor_event_list, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        doOnViewCreated(view, true);

        //boolean startTargetHelps = getArguments() != null && getArguments().getBoolean(START_TARGET_HELPS_ARGUMENT, false);
        //if (startTargetHelps)
            showTargetHelps();
    }

    @SuppressLint("SetTextI18n")
    private void doOnViewCreated(View view, boolean fromOnViewCreated)
    {
        profilePrefIndicatorImageView = view.findViewById(R.id.editor_events_activated_profile_pref_indicator);
        if (!ApplicationPreferences.applicationEditorPrefIndicator)
            //noinspection DataFlowIssue
            profilePrefIndicatorImageView.setVisibility(GONE);

        activeProfileName = view.findViewById(R.id.editor_events_activated_profile_name);
        activeProfileIcon = view.findViewById(R.id.editor_events_activated_profile_icon);

        PPLinearLayoutManager layoutManager = new PPLinearLayoutManager(getActivity());
        listView = view.findViewById(R.id.editor_events_list);
        //listView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        //noinspection DataFlowIssue
        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        activatedProfileHeader = view.findViewById(R.id.editor_events_activated_profile_header);
        bottomToolbar = view.findViewById(R.id.editor_events_list_bottom_bar);
        //noinspection DataFlowIssue
        editorSubToolbar = getActivity().findViewById(R.id.editor_subToolbar);

        //noinspection ConstantConditions
        if (GlobalGUIRoutines.areSystemAnimationsEnabled(getActivity().getApplicationContext())) {
            if (ApplicationPreferences.applicationEditorHideHeaderOrBottomBar ||
                    getResources().getBoolean(R.bool.forceHideHeaderOrBottomBar)) {
                ViewGroup eventListFragmnet = view.findViewById(R.id.layout_events_list_fragment);
                //noinspection DataFlowIssue
                final LayoutTransition layoutTransition =  eventListFragmnet.getLayoutTransition();
                layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
                //layoutTransition.setDuration(500);

                listView.addOnScrollListener(new EditorAutoHideShowHeaderBottomBarScrollListener(/*2*/) {
                    @Override
                    public void onHide() {
                        //if ((activatedProfileHeader.getMeasuredHeight() >= headerHeight - 4) &&
                        //    (activatedProfileHeader.getMeasuredHeight() <= headerHeight + 4))
        //                if (!hideAnimatorHeader.isRunning()) {
        //                    hideAnimatorHeader.start();
        //                }
        //                if (!showAnimatorBottomBar.isRunning()) {
        //                    showAnimatorBottomBar.start();
        //                }

                        if (!layoutTransition.isRunning()) {
                            activatedProfileHeader.setVisibility(GONE);
                            editorSubToolbar.setVisibility(GONE);
                            //noinspection DataFlowIssue
                            final Handler handler = new Handler(getActivity().getMainLooper());
                            final WeakReference<Toolbar> bottomToolbarWeakRef = new WeakReference<>(bottomToolbar);
                            handler.postDelayed(() -> {
                            //handler.post(() -> {
//                                PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EditorProfileListFragment.HidingRecyclerViewScrollListener.onHide");
                                Toolbar bottomToolbar = bottomToolbarWeakRef.get();
                                bottomToolbar.setVisibility(GONE);
                            }, 100);
                            //});
                        }
                    }

                    @Override
                    public void onShow() {
                        //if (activatedProfileHeader.getMeasuredHeight() == 0)
        //                if (!showAnimatorHeader.isRunning()) {
        //                    showAnimatorHeader.start();
        //                }
        //                if (!hideAnimatorBottomBar.isRunning()) {
        //                    hideAnimatorBottomBar.start();
        //                }

                        if (!layoutTransition.isRunning()) {
                            //noinspection DataFlowIssue
                            final Handler handler = new Handler(getActivity().getMainLooper());
                            final WeakReference<Toolbar> bottomToolbarWeakRef = new WeakReference<>(bottomToolbar);
                            handler.postDelayed(() -> {
                            //handler.post(() -> {
//                                PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EditorProfileListFragment.HidingRecyclerViewScrollListener.onShow");
                                Toolbar bottomToolbar = bottomToolbarWeakRef.get();
                                bottomToolbar.setVisibility(VISIBLE);
                            }, 100);
                            //});
                            activatedProfileHeader.setVisibility(VISIBLE);
                            editorSubToolbar.setVisibility(VISIBLE);
                        }
                    }
                });
            }
            else
                showHeaderAndBottomToolbar();
        }

        viewNoData = view.findViewById(R.id.editor_events_list_empty);
        progressBar = view.findViewById(R.id.editor_events_list_linla_progress);

        /*
        View footerView =  getActivity().getLayoutInflater()..inflate(R.layout.editor_list_footer, null, false);
        listView.addFooterView(footerView, null, false);
        */

        Menu menu = bottomToolbar.getMenu();
        if (menu != null) menu.clear();
        bottomToolbar.inflateMenu(R.menu.editor_events_bottom_bar);
        bottomToolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_add_event) {
                if (eventListAdapter != null) {
                    if (!activity.isFinishing()) {
                        activity.addEventDialog = new AddEventDialog(activity, this);
                        activity.addEventDialog.show();
                    }
                }
                return true;
            }
            else
            if (itemId == R.id.menu_delete_all_events) {
                deleteAllEvents();
                return true;
            }
            else
            if (itemId == R.id.menu_default_profile) {
                Intent intent = new Intent(activity, PhoneProfilesPrefsActivity.class);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_PROFILE_ACTIVATION_CATEGORY_ROOT);
                startActivity(intent);
                return true;
            }
            else
            if (itemId == R.id.menu_generate_predefined_events) {
                //final Activity activity = getActivity();
                //final EditorEventListFragment fragment = this;
                final Handler progressBarHandler = new Handler(activity.getMainLooper());
                final WeakReference<EditorEventListFragment> fragmentWeakRef = new WeakReference<>(this);
                final Runnable progressBarRunnable = () -> {
                    EditorEventListFragment fragment = fragmentWeakRef.get();
                    if (fragment != null) {
                        fragment.loadAsyncTask = new LoadEventListAsyncTask(fragment, fragment.filterType, fragment.orderType, true);
                        fragment.loadAsyncTask.execute();
                    }
                };
                progressBarHandler.post(progressBarRunnable);
                return true;
            }
            else
                return false;
        });

        /*
        LinearLayout orderLayout = view.findViewById(R.id.editor_list_bottom_bar_order_root);
        if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER)
            orderLayout.setVisibility(View.GONE);
        else
            orderLayout.setVisibility(VISIBLE);
         */

        orderSelectedItem = ApplicationPreferences.editorOrderSelectedItem;

        /*
        LinearLayout bottomBarOrderRoot = view.findViewById(R.id.editor_events_list_bottom_bar_order_root);
        if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER)
            //noinspection DataFlowIssue
            bottomBarOrderRoot.setVisibility(View.INVISIBLE); // MUST BE INVISIBLE, required for showTargetHelps().
        else
            //noinspection DataFlowIssue
            bottomBarOrderRoot.setVisibility(VISIBLE);
        */

        TextView orderLabel = view.findViewById(R.id.editor_events_list_bottom_bar_order_title);
        orderSpinner = view.findViewById(R.id.editor_events_list_bottom_bar_order);
        SwitchCompat hideEventDetaildSwitch = view.findViewById(R.id.editor_events_list_hide_event_details);
        if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER) {
            //noinspection DataFlowIssue
            orderLabel.setVisibility(GONE);
            orderSpinner.setVisibility(GONE);
            //noinspection DataFlowIssue
            hideEventDetaildSwitch.setVisibility(VISIBLE);
            hideEventDetaildSwitch.setChecked(ApplicationPreferences.applicationEditorHideEventDetailsForStartOrder);
            hideEventDetaildSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences preferences = activityDataWrapper.context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_HIDE_EVENT_DETAILS_FOR_START_ORDER, isChecked);
                editor.apply();
                ApplicationPreferences.applicationEditorHideEventDetailsForStartOrder = isChecked;
                // must be called reloadActivity(), because must by invocked fragment layout with/without event details
                GlobalGUIRoutines.reloadActivity(activity, false);
            });
        } else {
            //noinspection DataFlowIssue
            orderLabel.setVisibility(VISIBLE);
            orderSpinner.setVisibility(VISIBLE);
            //noinspection DataFlowIssue
            hideEventDetaildSwitch.setVisibility(GONE);
        }

//        if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER)
//            orderSpinner.setVisibility(View.INVISIBLE); // MUST BE INVISIBLE, required for shoTargetHelps().
//        else
//            orderSpinner.setVisibility(VISIBLE);

        //TextView orderLabel = view.findViewById(R.id.editor_events_list_bottom_bar_order_title);
        //noinspection DataFlowIssue
        orderLabel.setText(getString(R.string.editor_drawer_title_events_order) + ":");

        String[] orderItems = new String[] {
                getString(R.string.editor_drawer_order_start_order),
                getString(R.string.editor_drawer_order_event_name),
                getString(R.string.editor_drawer_order_start_profile_name),
                getString(R.string.editor_drawer_order_end_profile_name),
                getString(R.string.editor_drawer_order_priority)
        };

        PPSpinnerAdapter orderSpinnerAdapter = new PPSpinnerAdapter(
                activity,
                R.layout.ppp_spinner_order,
                orderItems);
        orderSpinnerAdapter.setDropDownViewResource(R.layout.ppp_spinner_dropdown);
        orderSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
        //orderSpinner.setBackgroundTintList(ContextCompat.getColorStateList(activity/*.getBaseContext()*/, R.color.highlighted_spinner_all_editor));
        orderSpinner.setAdapter(orderSpinnerAdapter);
        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (orderSpinner.getAdapter() != null) {
                    //if (orderSpinner.getAdapter().getCount() <= position)
                    //    position = 0;
                    ((PPSpinnerAdapter) orderSpinner.getAdapter()).setSelection(position);
                }
                if (position != orderSelectedItem)
                    changeEventOrder(position, false);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        orderLabel.setOnClickListener(v -> orderSpinner.performClick());

        // first must be set eventsOrderType
        changeEventOrder(orderSelectedItem, fromOnViewCreated);

        updateBottomMenu();
    }

    void changeFragmentFilter(int eventsFilterType/*, boolean startTargetHelps*/) {
        filterType = eventsFilterType;

        doOnViewCreated(rootView, false);

        //if (startTargetHelps)
            showTargetHelps();
    }

    private static class LoadEventListAsyncTask extends AsyncTask<Void, Void, Void> {

        final WeakReference<EditorEventListFragment> fragmentWeakRef;
        DataWrapper _dataWrapper;
        final int _filterType;
        final int _orderType;
        final boolean _generatePredefinedProfiles;
        boolean defaultEventsGenerated = false;

        final boolean applicationEditorNotHideEventDetails;

        //Handler progressBarHandler;
        //Runnable progressBarRunnable;

        public LoadEventListAsyncTask (EditorEventListFragment fragment,
                                       int filterType,
                                       int orderType,
                                       boolean generatePredefinedProfiles) {
            fragmentWeakRef = new WeakReference<>(fragment);
            _filterType = filterType;
            _orderType = orderType;
            _generatePredefinedProfiles = generatePredefinedProfiles;
            //noinspection ConstantConditions
            _dataWrapper = new DataWrapper(fragment.getActivity().getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);

            applicationEditorNotHideEventDetails = !ApplicationPreferences.applicationEditorHideEventDetails;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            final EditorEventListFragment fragment = this.fragmentWeakRef.get();

            if ((fragment != null) /*&& (fragment.isAdded())*/) {
                 fragment.progressBar.setVisibility(VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            _dataWrapper.fillProfileList(true, applicationEditorNotHideEventDetails);
            _dataWrapper.fillEventList();
            _dataWrapper.fillEventTimelineList();

            final EditorEventListFragment fragment = this.fragmentWeakRef.get();

            if (_generatePredefinedProfiles) {
                if ((_dataWrapper.eventList.isEmpty())) {
                    // no events in DB, generate default events
                    // PPApplication.restoreFinished = Google auto-backup finished
                    if ((fragment != null) && (fragment.getActivity() != null)) {
                        _dataWrapper.generatePredefinedEventList(fragment.getActivity());
                        defaultEventsGenerated = true;
                    }
                }
            }

            _dataWrapper.getEventTimelineList(true);

            if ((fragment != null) && fragment.getActivity() != null) {
                for (Event event : _dataWrapper.eventList)
                    event._peferencesDecription = StringFormatUtils.fromHtml(
                            event.getPreferencesDescription(fragment.getActivity(), _dataWrapper, true),
                            true,  false, 0, 0, true);
            }

            if ((fragment != null) && (fragment.getActivity() != null)) {
                if (_filterType == FILTER_TYPE_START_ORDER)
                    fragment.sortList(_dataWrapper.eventList, ORDER_TYPE_START_ORDER, _dataWrapper);
                else
                    fragment.sortList(_dataWrapper.eventList, _orderType, _dataWrapper);
            }

            return null;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);

            EditorEventListFragment fragment = fragmentWeakRef.get();
            
            if ((fragment != null) && (fragment.isAdded())) {
                if ((fragment.getActivity() != null) && (!fragment.getActivity().isFinishing())) {
                    //progressBarHandler.removeCallbacks(progressBarRunnable);
                    fragment.progressBar.setVisibility(View.GONE);

                    fragment.listView.getRecycledViewPool().clear(); // maybe fix for java.lang.IndexOutOfBoundsException: Inconsistency detected.

                    // get local profileList
                    //_dataWrapper.fillProfileList(true, applicationEditorPrefIndicator);
                    // set local profile list into activity dataWrapper
                    fragment.activityDataWrapper.copyProfileList(_dataWrapper);

                    // get local eventList
                    //_dataWrapper.fillEventList();
                    // set local event list into activity dataWrapper
                    fragment.activityDataWrapper.copyEventList(_dataWrapper);

//                    PPApplicationStatic.logE("[SYNCHRONIZED] EditorEventListFragment.LoadEventListAsyncTask", "DataWrapper.eventList");
                    synchronized (fragment.activityDataWrapper.eventList) {
                        if (fragment.activityDataWrapper.eventList.isEmpty())
                            fragment.viewNoData.setVisibility(VISIBLE);
                    }
                    fragment.updateBottomMenu();

                    // get local eventTimelineList
                    _dataWrapper.getEventTimelineList(true);
                    // set copy local event timeline list into activity dataWrapper
                    fragment.activityDataWrapper.copyEventTimelineList(_dataWrapper);

                    _dataWrapper.clearProfileList();
                    _dataWrapper.clearEventList();
                    _dataWrapper.clearEventTimelineList();
                    _dataWrapper = null;

                    fragment.eventListAdapter = new EditorEventListAdapter(fragment, fragment.activityDataWrapper, _filterType, fragment);

                    // added touch helper for drag and drop items
                    //if (fragment.itemTouchHelper == null) {
                        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(fragment.eventListAdapter, false, false);
                        fragment.itemTouchHelper = new ItemTouchHelper(callback);
                        fragment.itemTouchHelper.attachToRecyclerView(fragment.listView);
                    //}

                    fragment.listView.setAdapter(fragment.eventListAdapter);

                    Profile profile = fragment.activityDataWrapper.getActivatedProfileFromDB(true,
                            ApplicationPreferences.applicationEditorPrefIndicator);
                    fragment.updateHeader(profile);

                    fragment.listView.getRecycledViewPool().clear(); // maybe fix for java.lang.IndexOutOfBoundsException: Inconsistency detected.
                    fragment.eventListAdapter.notifyDataSetChanged(false);

                    if (defaultEventsGenerated) {
                        if ((fragment.getActivity() != null) && (!fragment.getActivity().isFinishing()))
                            PPApplication.showToast(fragment.activityDataWrapper.context.getApplicationContext(),
                                    fragment.getString(R.string.toast_predefined_events_generated),
                                    Toast.LENGTH_SHORT);
                    }
                }
            }
        }
    }

    boolean isAsyncTaskRunning() {
        try {
            //Log.e("EditorEventListFragment.isAsyncTaskRunning", "loadAsyncTask="+loadAsyncTask);
            //Log.e("EditorEventListFragment.isAsyncTaskRunning", "loadAsyncTask.getStatus()="+loadAsyncTask.getStatus());

            return (loadAsyncTask != null) &&
                    loadAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING);
        } catch (Exception e) {
            return false;
        }
    }

    void stopRunningAsyncTask() {
        if (loadAsyncTask != null) {
            loadAsyncTask.cancel(true);
            loadAsyncTask = null;
        }
        if ((refreshGUIAsyncTask != null) &&
                refreshGUIAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            refreshGUIAsyncTask.cancel(true);
        refreshGUIAsyncTask = null;
        if ((updateHeaderAsyncTask != null) &&
                updateHeaderAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            updateHeaderAsyncTask.cancel(true);
        updateHeaderAsyncTask = null;

        if (activityDataWrapper != null) {
            activityDataWrapper.invalidateDataWrapper();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (isAsyncTaskRunning()) {
            //Log.e("EditorEventListFragment.onDestroy", "AsyncTask not finished");
            stopRunningAsyncTask();
        }

        if (itemTouchHelper != null)
            itemTouchHelper.attachToRecyclerView(null);
        itemTouchHelper = null;
        if (listView != null)
            listView.setAdapter(null);
        if (eventListAdapter != null)
            eventListAdapter.release();

        if (activityDataWrapper != null)
            activityDataWrapper.invalidateDataWrapper();
        activityDataWrapper = null;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    void startEventPreferencesActivity(Event event, int predefinedEventIndex)
    {
        int editMode;

        if (event != null)
        {
            // edit event
            int eventPos = eventListAdapter.getItemPosition(event);
            /*int last = listView.getLastVisiblePosition();
            int first = listView.getFirstVisiblePosition();
            if ((eventPos <= first) || (eventPos >= last)) {
                listView.setSelection(eventPos);
            }*/
            RecyclerView.LayoutManager lm = listView.getLayoutManager();
            if (lm != null)
                lm.scrollToPosition(eventPos);

            //boolean startTargetHelps = getArguments() != null && getArguments().getBoolean(START_TARGET_HELPS_ARGUMENT, false);
            //if (startTargetHelps)
            showAdapterTargetHelps();

            editMode = PPApplication.EDIT_MODE_EDIT;
        }
        else
        {
            // add new event
            editMode = PPApplication.EDIT_MODE_INSERT;

        }

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) one must start profile preferences
        onStartEventPreferencesCallback.onStartEventPreferences(event, editMode, predefinedEventIndex);
    }

/*
    boolean runStopEvent(final Event event) {
        if (EventStatic.getGlobalEventsRunning(activityDataWrapper.context)) {
            // events are not globally stopped

            activityDataWrapper.getEventTimelineList(true);
            if (event.getStatusFromDB(activityDataWrapper.context) == Event.ESTATUS_STOP) {
                if (!EventsPrefsFragment.isRedTextNotificationRequired(event, false, activityDataWrapper.context)) {
                    // pause event
                    //IgnoreBatteryOptimizationNotification.showNotification(activityDataWrapper.context);

                    final DataWrapper dataWrapper = activityDataWrapper;
                    Runnable runnable = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=EditorEventListFragment.runStopEvent.1");

                        //DataWrapper dataWrapper = dataWrapperWeakRef.get();
                        //Event event = eventWeakRef.get();

                        //if ((dataWrapper != null) && (event != null)) {
                            PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":EditorEventListFragment_runStopEvent_1");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                synchronized (PPApplication.eventsHandlerMutex) {
                                    event.pauseEvent(dataWrapper, false, false,
                                            false, true, null, false, false, true);
                                }

                            } catch (Exception e) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                                PPApplicationStatic.recordException(e);
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        //}
                    };
                    PPApplicationStatic.createBasicExecutorPool();
                    PPApplication.basicExecutorPool.submit(runnable);

                }
                else {
                    GlobalGUIRoutines.showDialogAboutRedText(null, event, false, false, false, true, getActivity());
                    return false;
                }
            } else {
                // stop event

                final DataWrapper dataWrapper = activityDataWrapper;
                Runnable runnable = () -> {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=EditorEventListFragment.runStopEvent.2");

                    //DataWrapper dataWrapper = dataWrapperWeakRef.get();
                    //Event event = eventWeakRef.get();

                    //if ((dataWrapper != null) && (event != null)) {
                        PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":EditorEventListFragment_runStopEvent_2");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            synchronized (PPApplication.eventsHandlerMutex) {
                                event.stopEvent(dataWrapper, false, false,
                                        true, true, true); // activate return profile
                            }

                        } catch (Exception e) {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplicationStatic.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    //}
                };
                PPApplicationStatic.createBasicExecutorPool();
                PPApplication.basicExecutorPool.submit(runnable);

            }

            // redraw event list
            //updateListView(event, false, false, true, 0);
            if (getActivity() != null)
                ((EditorActivity) getActivity()).redrawEventListFragment(event, EDIT_MODE_EDIT);

            // restart events
            //activityDataWrapper.restartEvents(false, true, true, true, true);
            activityDataWrapper.restartEventsWithRescan(true, false, true, false, true, false);

            //Intent serviceIntent = new Intent(activityDataWrapper.context, PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
            //PPApplication.startPPService(activityDataWrapper.context, serviceIntent);
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
            PPApplicationStatic.runCommand(activityDataWrapper.context, commandIntent);

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

//                    PPApplicationStatic.logE("[WORKER_CALL] EditorEventListFragment.runStopEvent", "xxx");
                    workManager.enqueueUniqueWork(MainWorker.DISABLE_NOT_USED_SCANNERS_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                }
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }

        } else {
            if (event.getStatusFromDB(activityDataWrapper.context) == Event.ESTATUS_STOP) {
                // pause event
                event.setStatus(Event.ESTATUS_PAUSE);
            } else {
                // stop event
                event.setStatus(Event.ESTATUS_STOP);
            }

            // update event in DB
            DatabaseHandler.getInstance(activityDataWrapper.context).updateEvent(event);

            // redraw event list
            //updateListView(event, false, false, true, 0);
            if (getActivity() != null)
                ((EditorActivity) getActivity()).redrawEventListFragment(event, EDIT_MODE_EDIT);

            // restart events
            //activityDataWrapper.restartEvents(false, true, true, true, true);
            activityDataWrapper.restartEventsWithRescan(true, false, true, false, true, false);

            //Intent serviceIntent = new Intent(activityDataWrapper.context, PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
            //PPApplication.startPPService(activityDataWrapper.context, serviceIntent);
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
            PPApplicationStatic.runCommand(activityDataWrapper.context, commandIntent);

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

//                    PPApplicationStatic.logE("[WORKER_CALL] EditorEventListFragment.runStopEvent", "xxx");
                    workManager.enqueueUniqueWork(MainWorker.DISABLE_NOT_USED_SCANNERS_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                }
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        }
        return true;
    }
*/

    private void duplicateEvent(Event origEvent)
    {
        /*
        Event newEvent = new Event(
                   origEvent._name+"_d",
                   origEvent._type,
                   origEvent._fkProfile,
                   origEvent._status
                    );
        newEvent.copyEventPreferences(origEvent);

        // add event into db and set id and order
        databaseHandler.addEvent(newEvent);
        // add event into listview
        eventListAdapter.addItem(newEvent, false);

        updateListView(newEvent, false);

        startEventPreferencesActivity(newEvent);
        */

        int editMode;

        // duplicate event
        editMode = PPApplication.EDIT_MODE_DUPLICATE;

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) one must start profile preferences
        onStartEventPreferencesCallback.onStartEventPreferences(origEvent, editMode, 0);

    }

    @SuppressLint("NotifyDataSetChanged")
    private void deleteEvent(final Event event)
    {
        if (activityDataWrapper.getEventById(event._id) == null)
            // event not exists
            return;

        PPApplicationStatic.addActivityLog(activityDataWrapper.context, PPApplication.ALTYPE_EVENT_DELETED, event._name, null, "");

        listView.getRecycledViewPool().clear();  // maybe fix for java.lang.IndexOutOfBoundsException: Inconsistency detected.

//        PPApplicationStatic.logE("[SYNCHRONIZED] EditorEventListFragment.deleteEvent", "DataWrapper.eventList");
        synchronized (activityDataWrapper.eventList) {
            // remove notifications about event parameters errors
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activityDataWrapper.context);
            try {
                notificationManager.cancel(
                        PPApplication.DISPLAY_PREFERENCES_EVENT_ERROR_NOTIFICATION_TAG+"_"+event._id,
                        PPApplication.EVENT_ID_NOTIFICATION_ID + (int) event._id);
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        }

        eventListAdapter.deleteItemNoNotify(event);
        DatabaseHandler.getInstance(activityDataWrapper.context).deleteEvent(event);

        // restart events
        //activityDataWrapper.restartEvents(false, true, true, true, true);
        activityDataWrapper.restartEventsWithRescan(true, false, true, true, true, false);

        listView.getRecycledViewPool().clear();  // maybe fix for java.lang.IndexOutOfBoundsException: Inconsistency detected.
        eventListAdapter.notifyDataSetChanged();

        /*Intent serviceIntent = new Intent(getActivity().getApplicationContext(), PhoneProfilesService.class);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
        PPApplication.startPPService(getActivity(), serviceIntent);*/
        Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
        //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
        commandIntent.putExtra(PhoneProfilesService.EXTRA_DISABLE_NOT_USED_SCANNERS, true);
        PPApplicationStatic.runCommand(getActivity(), commandIntent);

        onStartEventPreferencesCallback.onStartEventPreferences(null, PPApplication.EDIT_MODE_DELETE, 0);
    }

    void showEditMenu(View view)
    {
        // because of refresh list is popup menu moved up
        // for this reason is used SingleSelectListDialog

        final Event event = (Event)view.getTag();

        int itemsRes;
        if (event.getStatusFromDB(activityDataWrapper.context) == Event.ESTATUS_STOP)
            itemsRes = R.array.eventListItemEditEnableRunArray;
        else
            itemsRes = R.array.eventListItemEditStopArray;

        SingleSelectListDialog dialog = new SingleSelectListDialog(
                true,
                getString(R.string.event_string_0) + StringConstants.STR_COLON_WITH_SPACE + event._name,
                getString(R.string.tooltip_options_menu),
                itemsRes,
                SingleSelectListDialog.NOT_USE_RADIO_BUTTONS,
                (dialog1, which) -> {
                    switch (which) {
                        case 0:
                            //runStopEvent(event);
                            EventStatic.runStopEvent(activityDataWrapper, event, (EditorActivity) getActivity());
                            break;
                        case 1:
                            duplicateEvent(event);
                            break;
                        case 2:
                            deleteEventWithAlert(event);
                            break;
                        default:
                    }
                },
                null,
                false,
                getActivity());
        dialog.show();


/*
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        Context _context = view.getContext();
        PopupMenu popup;
        popup = new PopupMenu(_context, view, Gravity.END);
        Menu menu = popup.getMenu();
        getActivity().getMenuInflater().inflate(R.menu.event_list_item_edit, menu);

        final Event event = (Event)view.getTag();

        MenuItem menuItem = menu.findItem(R.id.event_list_item_menu_run_stop);
        //if (PPApplication.getGlobalEventsRunning(dataWrapper.context))
        //{
            //menuItem.setVisible(true);

            if (event.getStatusFromDB(activityDataWrapper.context) == Event.ESTATUS_STOP)
            {
                menuItem.setTitle(R.string.event_list_item_menu_run);
            }
            else
            {
                menuItem.setTitle(R.string.event_list_item_menu_stop);
            }
        //}
        //else
        //	menuItem.setVisible(false);

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.event_list_item_menu_run_stop) {
                runStopEvent(event);
                return true;
            }
            else
            if (itemId == R.id.event_list_item_menu_duplicate) {
                duplicateEvent(event);
                return true;
            }
            else
            if (itemId == R.id.event_list_item_menu_delete) {
                deleteEventWithAlert(event);
                return true;
            }
            else
                return false;
        });


        if ((getActivity() != null) && (!getActivity().isFinishing()))
            popup.show();
 */
    }

    private void deleteEventWithAlert(Event event)
    {
        final Event _event = event;

        PPAlertDialog dialog = new PPAlertDialog(
                getString(R.string.event_string_0) + StringConstants.STR_COLON_WITH_SPACE + event._name,
                getString(R.string.delete_event_alert_message),
                getString(R.string.alert_button_yes),
                getString(R.string.alert_button_no),
                null, null,
                (dialog1, which) -> deleteEvent(_event),
                null,
                null,
                null,
                null,
                true, true,
                false, false,
                true,
                false,
                getActivity()
        );

        if ((getActivity() != null) && (!getActivity().isFinishing()))
            dialog.show();
    }

    private void deleteAllEvents()
    {
        if (eventListAdapter != null) {
            PPAlertDialog dialog = new PPAlertDialog(
                    getString(R.string.alert_title_delete_all_events),
                    getString(R.string.alert_message_delete_all_events),
                    getString(R.string.alert_button_yes),
                    getString(R.string.alert_button_no),
                    null, null,
                    (dialog1, which) -> {
                        PPApplicationStatic.addActivityLog(activityDataWrapper.context, PPApplication.ALTYPE_ALL_EVENTS_DELETED, null, null, "");

                        listView.getRecycledViewPool().clear();  // maybe fix for java.lang.IndexOutOfBoundsException: Inconsistency detected.

                        // this delete events from db
                        activityDataWrapper.stopAllEventsFromMainThread(true, true);

//                        PPApplicationStatic.logE("[SYNCHRONIZED] EditorEventListFragment.deleteAllEvents", "DataWrapper.eventList");
                        synchronized (activityDataWrapper.eventList) {
                            // remove notifications about event parameters errors
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activityDataWrapper.context);
                            //noinspection ForLoopReplaceableByForEach
                            for (Iterator<Event> it = activityDataWrapper.eventList.iterator(); it.hasNext(); ) {
                                Event event = it.next();
                                try {
                                    notificationManager.cancel(
                                            PPApplication.DISPLAY_PREFERENCES_EVENT_ERROR_NOTIFICATION_TAG + "_" + event._id,
                                            PPApplication.EVENT_ID_NOTIFICATION_ID + (int) event._id);
                                } catch (Exception e) {
                                    PPApplicationStatic.recordException(e);
                                }
                            }
                        }

                        listView.getRecycledViewPool().clear();  // maybe fix for java.lang.IndexOutOfBoundsException: Inconsistency detected.
                        eventListAdapter.clear();
                        // this is in eventListAdapter.clear()
                        //eventListAdapter.notifyDataSetChanged();

                        if (getActivity() != null) {
                            /*Intent serviceIntent = new Intent(getActivity().getApplicationContext(), PhoneProfilesService.class);
                            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                            serviceIntent.putExtra(PhoneProfilesService.EXTRA_UNREGISTER_RECEIVERS_AND_WORKERS, true);
                            PPApplication.startPPService(getActivity(), serviceIntent);*/
                            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
                            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                            commandIntent.putExtra(PhoneProfilesService.EXTRA_UNREGISTER_RECEIVERS_AND_WORKERS, true);
                            commandIntent.putExtra(PhoneProfilesService.EXTRA_DISABLE_NOT_USED_SCANNERS, true);
                            PPApplicationStatic.runCommand(getActivity(), commandIntent);
                        }

                        onStartEventPreferencesCallback.onStartEventPreferences(null, PPApplication.EDIT_MODE_DELETE, 0);
                    },
                    null,
                    null,
                    null,
                    null,
                    true, true,
                    false, false,
                    true,
                    false,
                    getActivity()
            );

            if ((getActivity() != null) && (!getActivity().isFinishing()))
                dialog.show();
        }
    }

    void updateHeader(Profile profile)
    {
        //if (!ApplicationPreferences.applicationEditorHeader(activityDataWrapper.context))
        //    return;

        if ((activeProfileName == null) || (activeProfileIcon == null))
            return;

        String oldDisplayedText = (String)activatedProfileHeader.getTag();

        if (profile == null)
        {
            activatedProfileHeader.setTag(getString(R.string.profiles_header_profile_name_no_activated));

            activeProfileName.setText(getString(R.string.profiles_header_profile_name_no_activated));
            activeProfileIcon.setImageResource(R.drawable.ic_profile_default);
        }
        else
        {
            Spannable profileName = DataWrapperStatic.getProfileNameWithManualIndicator(profile, true, "", true, false, false, false, activityDataWrapper);
            Spannable sbt = new SpannableString(profileName);
            Object[] spansToRemove = sbt.getSpans(0, profileName.length(), Object.class);
            for (Object span : spansToRemove) {
                if (span instanceof CharacterStyle)
                    sbt.removeSpan(span);
            }
            activatedProfileHeader.setTag(sbt.toString());

            activeProfileName.setText(profileName);
            if (profile.getIsIconResourceID())
            {
                Bitmap bitmap = profile.increaseProfileIconBrightnessForActivity(getActivity(), profile._iconBitmap);
                if (bitmap != null)
                    activeProfileIcon.setImageBitmap(bitmap);
                else {
                    if (profile._iconBitmap != null)
                        activeProfileIcon.setImageBitmap(profile._iconBitmap);
                    else {
                        int res = ProfileStatic.getIconResource(profile.getIconIdentifier());
                        activeProfileIcon.setImageResource(res); // icon resource
                    }
                }
            }
            else
            {
                //Bitmap bitmap = profile.increaseProfileIconBrightnessForActivity(getActivity(), profile._iconBitmap);
                //Bitmap bitmap = profile._iconBitmap;
                //if (bitmap != null)
                //    activeProfileIcon.setImageBitmap(bitmap);
                //else
                    activeProfileIcon.setImageBitmap(profile._iconBitmap);
            }
        }

        if (ApplicationPreferences.applicationEditorPrefIndicator)
        {
            if (profile == null)
                //profilePrefIndicatorImageView.setImageResource(R.drawable.ic_empty);
                profilePrefIndicatorImageView.setVisibility(GONE);
            else {
                if (profile._preferencesIndicator != null)
                    profilePrefIndicatorImageView.setImageBitmap(profile._preferencesIndicator);
                else
                    profilePrefIndicatorImageView.setImageResource(R.drawable.ic_empty);
            }
        }

        String newDisplayedText = (String)activatedProfileHeader.getTag();
        if (!newDisplayedText.equals(oldDisplayedText))
            activatedProfileHeader.setVisibility(VISIBLE);

        updateHeaderAsyncTask = new UpdateHeaderAsyncTask(this);
        updateHeaderAsyncTask.execute();
    }

    @SuppressLint("NotifyDataSetChanged")
    void updateListView(Event event, boolean newEvent, boolean refreshIcons, boolean setPosition/*, long loadEventId*/)
    {
        /*if (listView != null)
            listView.cancelDrag();*/

        //if (eventListAdapter != null)
        if (listView != null)
            listView.getRecycledViewPool().clear();  // maybe fix for java.lang.IndexOutOfBoundsException: Inconsistency detected.

        if (eventListAdapter != null) {
            if ((newEvent) && (event != null))
                // add event into listview
                eventListAdapter.addItem(event);
        }

//        PPApplicationStatic.logE("[SYNCHRONIZED] EditorEventListFragment.updateListView", "DataWrapper.eventList");
        synchronized (activityDataWrapper.eventList) {
            //if (activityDataWrapper.eventList != null) {
                // sort list
                sortList(activityDataWrapper.eventList, orderType, activityDataWrapper);
            //}
        }

        if (eventListAdapter != null) {
            int eventPos = ListView.INVALID_POSITION;

            if (event != null)
                eventPos = eventListAdapter.getItemPosition(event);
            //else
            //    eventPos = listView.getCheckedItemPosition();

            /*if (loadEventId != 0) {
                if (getActivity() != null) {
                    Event eventFromDB = DatabaseHandler.getInstance(getActivity().getApplicationContext()).getEvent(loadEventId);
                    activityDataWrapper.updateEvent(eventFromDB);
                    refreshIcons = true;
                }
            }*/
            if (listView != null)
                listView.getRecycledViewPool().clear();  // maybe fix for java.lang.IndexOutOfBoundsException: Inconsistency detected.
            eventListAdapter.notifyDataSetChanged(refreshIcons);

            if (setPosition || newEvent) {
                if (eventPos != ListView.INVALID_POSITION) {
                    if (listView != null) {
                        // set event visible in list
                        //int last = listView.getLastVisiblePosition();
                        //int first = listView.getFirstVisiblePosition();
                        //if ((eventPos <= first) || (eventPos >= last)) {
                        //    listView.setSelection(eventPos);
                        //}
                        RecyclerView.LayoutManager lm = listView.getLayoutManager();
                        if (lm != null)
                            lm.scrollToPosition(eventPos);
                    }
                }
            }

            //boolean startTargetHelps = getArguments() != null && getArguments().getBoolean(START_TARGET_HELPS_ARGUMENT, false);
            //if (startTargetHelps)
                showAdapterTargetHelps();
        }
    }

    /*
    public int getFilterType()
    {
        return filterType;
    }
    */

    @SuppressLint("NotifyDataSetChanged")
    private void changeListOrder(int orderType, boolean fromOnViewCreated)
    {
        if (isAsyncTaskRunning()) {
            //Log.e("EditorEventListFragment.changeListOrder", "AsyncTask running");
            stopRunningAsyncTask();
        }

        this.orderType = orderType;

        final Activity activity = getActivity();

        if (fromOnViewCreated) {
//            PPApplicationStatic.logE("[SYNCHRONIZED] EditorEventListFragment.changeListOrder", "(1) DataWrapper.eventList");
            synchronized (activityDataWrapper.eventList) {
                if (!activityDataWrapper.eventListFilled) {
//                    Log.e("EditorEventListFragment.changeListOrder", "eventList not filled");
                    // start new AsyncTask, because old may be cancelled
                    if (activity != null) {
                        final Handler progressBarHandler = new Handler(activity.getMainLooper());
                        final WeakReference<EditorEventListFragment> fragmentWeakRef = new WeakReference<>(this);
                        final Runnable progressBarRunnable = () -> {
                            EditorEventListFragment fragment = fragmentWeakRef.get();
                            if (fragment != null) {
                                fragment.loadAsyncTask = new LoadEventListAsyncTask(fragment, fragment.filterType, fragment.orderType, false);
                                fragment.loadAsyncTask.execute();
                            }
                        };
                        progressBarHandler.post(progressBarRunnable);
                    } else {
                        loadAsyncTask = new LoadEventListAsyncTask(this, filterType, orderType, false);
                        loadAsyncTask.execute();
                    }
                } else {
                    listView.getRecycledViewPool().clear();  // maybe fix for java.lang.IndexOutOfBoundsException: Inconsistency detected.
                    if (eventListAdapter != null) {
                        sortList(activityDataWrapper.eventList, orderType, activityDataWrapper);

                        // added touch helper for drag and drop items
                        //if (itemTouchHelper == null) {
                            ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(eventListAdapter, false, false);
                            itemTouchHelper = new ItemTouchHelper(callback);
                            itemTouchHelper.attachToRecyclerView(listView);
                        //}

                        listView.setAdapter(eventListAdapter);
//                        PPApplicationStatic.logE("[SYNCHRONIZED] EditorEventListFragment.changeListOrder", "(2) DataWrapper.profileList");
                        synchronized (activityDataWrapper.profileList) {
                            Profile profile = activityDataWrapper.getActivatedProfileFromDB(true,
                                    ApplicationPreferences.applicationEditorPrefIndicator);
                            updateHeader(profile);
                        }
                    }
                    else {
                        if (filterType == FILTER_TYPE_START_ORDER)
                            sortList(activityDataWrapper.eventList, ORDER_TYPE_START_ORDER, activityDataWrapper);
                        else
                            sortList(activityDataWrapper.eventList, orderType, activityDataWrapper);
//                        PPApplicationStatic.logE("[SYNCHRONIZED] EditorEventListFragment.changeListOrder", "(3) DataWrapper.profileList");
                        synchronized (activityDataWrapper.profileList) {
                            Profile profile = activityDataWrapper.getActivatedProfileFromDB(true,
                                    ApplicationPreferences.applicationEditorPrefIndicator);
                            updateHeader(profile);
                        }

                        eventListAdapter = new EditorEventListAdapter(this, activityDataWrapper, filterType, this);

                        // added touch helper for drag and drop items
                        //if (itemTouchHelper == null) {
                            ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(eventListAdapter, false, false);
                            itemTouchHelper = new ItemTouchHelper(callback);
                            itemTouchHelper.attachToRecyclerView(listView);
                        //}

                        listView.setAdapter(eventListAdapter);
                    }
                    listView.getRecycledViewPool().clear();  // maybe fix for java.lang.IndexOutOfBoundsException: Inconsistency detected.
                    eventListAdapter.notifyDataSetChanged();
                }
            }
        }
        else {
//            PPApplicationStatic.logE("[SYNCHRONIZED] EditorEventListFragment.changeListOrder", "(4) DataWrapper.eventList");
            synchronized (activityDataWrapper.eventList) {
                if (!activityDataWrapper.eventListFilled) {
//                    Log.e("EditorEventListFragment.changeListOrder", "eventList not filled");
                    // start new AsyncTask, because old may be cancelled
                    if (activity != null) {
                        final Handler progressBarHandler = new Handler(activity.getMainLooper());
                        final WeakReference<EditorEventListFragment> fragmentWeakRef = new WeakReference<>(this);
                        final Runnable progressBarRunnable = () -> {
                            EditorEventListFragment fragment = fragmentWeakRef.get();
                            if (fragment != null) {
                                fragment.loadAsyncTask = new LoadEventListAsyncTask(fragment, fragment.filterType, fragment.orderType, false);
                                fragment.loadAsyncTask.execute();
                            }
                        };
                        progressBarHandler.post(progressBarRunnable);
                    } else {
                        loadAsyncTask = new LoadEventListAsyncTask(this, filterType, orderType, false);
                        loadAsyncTask.execute();
                    }
                } else {
                    listView.getRecycledViewPool().clear();  // maybe fix for java.lang.IndexOutOfBoundsException: Inconsistency detected.

                    if (filterType == FILTER_TYPE_START_ORDER)
                        sortList(activityDataWrapper.eventList, ORDER_TYPE_START_ORDER, activityDataWrapper);
                    else
                        sortList(activityDataWrapper.eventList, orderType, activityDataWrapper);
//                    PPApplicationStatic.logE("[SYNCHRONIZED] EditorEventListFragment.changeListOrder", "(5) DataWrapper.profileList");
                    synchronized (activityDataWrapper.profileList) {
                        Profile profile = activityDataWrapper.getActivatedProfileFromDB(true,
                                ApplicationPreferences.applicationEditorPrefIndicator);
                        updateHeader(profile);
                    }
                    eventListAdapter = new EditorEventListAdapter(this, activityDataWrapper, filterType, this);

                    // added touch helper for drag and drop items
                    //if (itemTouchHelper == null) {
                        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(eventListAdapter, false, false);
                        itemTouchHelper = new ItemTouchHelper(callback);
                        itemTouchHelper.attachToRecyclerView(listView);
                    //}

                    listView.setAdapter(eventListAdapter);

                    int eventPos = ListView.INVALID_POSITION;
                    if (scrollToEvent != null) {
                        eventPos = eventListAdapter.getItemPosition(scrollToEvent);
                        scrollToEvent = null;
                    }

                    listView.getRecycledViewPool().clear();  // maybe fix for java.lang.IndexOutOfBoundsException: Inconsistency detected.
                    eventListAdapter.notifyDataSetChanged();

                    if (eventPos != ListView.INVALID_POSITION) {
                        if (listView != null) {
                            // set event visible in list
                            //int last = listView.getLastVisiblePosition();
                            //int first = listView.getFirstVisiblePosition();
                            //if ((eventPos <= first) || (eventPos >= last)) {
                            //    listView.setSelection(eventPos);
                            //}
                            RecyclerView.LayoutManager lm = listView.getLayoutManager();
                            if (lm != null)
                                lm.scrollToPosition(eventPos);
                        }
                    }
                }
            }
        }
    }

    private void sortList(List<Event> eventList, int orderType, DataWrapper _dataWrapper)
    {
        final DataWrapper dataWrapper = _dataWrapper;

        class EventNameComparator implements Comparator<Event> {
            public int compare(Event lhs, Event rhs) {
                if (PPApplication.collator != null)
                    return PPApplication.collator.compare(lhs._name, rhs._name);
                else
                    return 0;
            }
        }

        class StartOrderComparator implements Comparator<Event> {
            public int compare(Event lhs, Event rhs) {
                return lhs._startOrder - rhs._startOrder;
            }
        }

        class StartProfileNameComparator implements Comparator<Event> {
            public int compare(Event lhs, Event rhs) {
                if (PPApplication.collator != null) {
                    String nameLhs = "";
                    if (lhs._fkProfileStart != Profile.PROFILE_NO_ACTIVATE) {
                        Profile profileLhs = dataWrapper.getProfileById(lhs._fkProfileStart, false, false, false);
                        if (profileLhs != null) nameLhs = profileLhs._name;
                    }
                    String nameRhs = "";
                    if (rhs._fkProfileStart != Profile.PROFILE_NO_ACTIVATE) {
                        Profile profileRhs = dataWrapper.getProfileById(rhs._fkProfileStart, false, false, false);
                        if (profileRhs != null) nameRhs = profileRhs._name;
                    }
                    return PPApplication.collator.compare(nameLhs, nameRhs);
                }
                else
                    return 0;
            }
        }

        class EndProfileNameComparator implements Comparator<Event> {
            public int compare(Event lhs, Event rhs) {
                if (PPApplication.collator != null) {
                    String nameLhs = "";
                    if (lhs._fkProfileEnd != Profile.PROFILE_NO_ACTIVATE) {
                        Profile profileLhs = dataWrapper.getProfileById(lhs._fkProfileEnd, false, false, false);
                        if (profileLhs != null) nameLhs = profileLhs._name;
                    }
                    String nameRhs = "";
                    if (rhs._fkProfileEnd != Profile.PROFILE_NO_ACTIVATE) {
                        Profile profileRhs = dataWrapper.getProfileById(rhs._fkProfileEnd, false, false, false);
                        if (profileRhs != null) nameRhs = profileRhs._name;
                    }
                    return PPApplication.collator.compare(nameLhs, nameRhs);
                }
                else
                    return 0;
            }
        }

        class PriorityComparator implements Comparator<Event> {
            public int compare(Event lhs, Event rhs) {
                //int res =  lhs._priority - rhs._priority;
                return rhs._priority - lhs._priority;
            }
        }

        switch (orderType)
        {
            case ORDER_TYPE_EVENT_NAME:
                eventList.sort(new EventNameComparator());
                break;
            case ORDER_TYPE_START_ORDER:
                eventList.sort(new StartOrderComparator());
                break;
            case ORDER_TYPE_START_PROFILE_NAME:
                eventList.sort(new StartProfileNameComparator());
                break;
            case ORDER_TYPE_END_PROFILE_NAME:
                eventList.sort(new EndProfileNameComparator());
                break;
            case ORDER_TYPE_PRIORITY:
                if (ApplicationPreferences.applicationEventUsePriority)
                    eventList.sort(new PriorityComparator());
                else
                    eventList.sort(new StartOrderComparator());
                break;
        }
    }

    void refreshGUI(final boolean refreshIcons, final boolean setPosition, final long eventId)
    {
        if (activityDataWrapper == null)
            return;

        refreshGUIAsyncTask =
                new RefreshGUIAsyncTask(
                        refreshIcons, setPosition, eventId, this, activityDataWrapper);
        refreshGUIAsyncTask.execute();
    }

    void removeAdapter() {
        if (itemTouchHelper != null)
            itemTouchHelper.attachToRecyclerView(null);
        if (listView != null)
            listView.setAdapter(null);
    }

    void showTargetHelps() {
        if (getActivity() == null)
            return;

        boolean startTargetHelpsFinished = ApplicationPreferences.prefEditorActivityStartTargetHelpsFinished;
        if (!startTargetHelpsFinished)
            return;

        boolean startTargetHelps = ApplicationPreferences.prefEditorEventsFragmentStartTargetHelps;
        boolean startTargetHelpsFilterSpinner = ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsFilterSpinner;
        boolean startTargetHelpsDefaultProfile = ApplicationPreferences.prefEditorFragmentStartTargetHelpsDefaultProfile;
        boolean startTargetHelpsOrderSpinner = ApplicationPreferences. prefEditorEventsFragmentStartTargetHelpsOrderSpinner;

        if (startTargetHelps || startTargetHelpsFilterSpinner || startTargetHelpsDefaultProfile || startTargetHelpsOrderSpinner ||
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps ||
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder ||
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsStatus) {

            if (startTargetHelps || startTargetHelpsFilterSpinner || startTargetHelpsDefaultProfile || startTargetHelpsOrderSpinner) {

                //Log.d("EditorEventListFragment.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activityDataWrapper.context);
                editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_FRAGMENT_START_TARGET_HELPS, false);
                editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_FRAGMENT_START_TARGET_HELPS_FILTER_SPINNER, false);
                editor.putBoolean(PPApplication.PREF_EDITOR_ACTIVITY_START_TARGET_HELPS_DEFAULT_PROFILE, false);
                if (filterType != FILTER_TYPE_START_ORDER)
                    editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_FRAGMENT_START_TARGET_HELPS_ORDER_SPINNER, false);
                editor.apply();
                ApplicationPreferences.prefEditorEventsFragmentStartTargetHelps = false;
                ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsFilterSpinner = false;
                ApplicationPreferences.prefEditorFragmentStartTargetHelpsDefaultProfile = false;
                if (filterType != FILTER_TYPE_START_ORDER)
                    ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsOrderSpinner = false;

                int outerCircleColor = R.color.tabTargetHelpOuterCircleColor;
                int targetCircleColor = R.color.tabTargetHelpTargetCircleColor;
                int titleTextColor = R.color.tabTargetHelpTitleTextColor;
                int descriptionTextColor = R.color.tabTargetHelpDescriptionTextColor;

                final TapTargetSequence sequence = new TapTargetSequence(getActivity());
                List<TapTarget> targets = new ArrayList<>();
                int id = 1;
                if (startTargetHelps) {
                    try {
                        targets.add(
                                TapTarget.forView(((EditorActivity)getActivity()).filterSpinner, getString(R.string.editor_activity_targetHelps_eventsFilterSpinner_title), getString(R.string.editor_activity_targetHelps_eventsFilterSpinner_description))
                                        .transparentTarget(true)
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .titleTextColor(titleTextColor)
                                        .descriptionTextColor(descriptionTextColor)
                                        .descriptionTextAlpha(PPApplication.descriptionTapTargetAlpha)
                                        .dimColor(R.color.tabTargetHelpDimColor)
                                        .titleTextSize(PPApplication.titleTapTargetSize)
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
                                TapTarget.forToolbarOverflow(bottomToolbar, getString(R.string.editor_activity_targetHelps_eventsBottomMenu_title), getString(R.string.editor_activity_targetHelps_eventsBottomMenu_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .titleTextColor(titleTextColor)
                                        .descriptionTextColor(descriptionTextColor)
                                        .descriptionTextAlpha(PPApplication.descriptionTapTargetAlpha)
                                        .dimColor(R.color.tabTargetHelpDimColor)
                                        .titleTextSize(PPApplication.titleTapTargetSize)
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
                                TapTarget.forToolbarMenuItem(bottomToolbar, R.id.menu_add_event, getString(R.string.editor_activity_targetHelps_newEventButton_title), getString(R.string.editor_activity_targetHelps_newEventButton_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .titleTextColor(titleTextColor)
                                        .descriptionTextColor(descriptionTextColor)
                                        .descriptionTextAlpha(PPApplication.descriptionTapTargetAlpha)
                                        .dimColor(R.color.tabTargetHelpDimColor)
                                        .titleTextSize(PPApplication.titleTapTargetSize)
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
                                TapTarget.forToolbarMenuItem(bottomToolbar, R.id.menu_delete_all_events, getString(R.string.editor_activity_targetHelps_deleteAllEventsButton_title), getString(R.string.editor_activity_targetHelps_deleteAllEventsButton_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .titleTextColor(titleTextColor)
                                        .descriptionTextColor(descriptionTextColor)
                                        .descriptionTextAlpha(PPApplication.descriptionTapTargetAlpha)
                                        .dimColor(R.color.tabTargetHelpDimColor)
                                        .titleTextSize(PPApplication.titleTapTargetSize)
                                        .textTypeface(Typeface.DEFAULT_BOLD)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                    } catch (Exception e) {
                        //PPApplicationStatic.recordException(e);
                    }
                }
                if (startTargetHelpsDefaultProfile) {
                    try {
                        targets.add(
                                TapTarget.forToolbarMenuItem(bottomToolbar, R.id.menu_default_profile, getString(R.string.editor_activity_targetHelps_backgroundProfileButton_title), getString(R.string.editor_activity_targetHelps_backgroundProfileButton_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .titleTextColor(titleTextColor)
                                        .descriptionTextColor(descriptionTextColor)
                                        .descriptionTextAlpha(PPApplication.descriptionTapTargetAlpha)
                                        .dimColor(R.color.tabTargetHelpDimColor)
                                        .titleTextSize(PPApplication.titleTapTargetSize)
                                        .textTypeface(Typeface.DEFAULT_BOLD)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                    } catch (Exception e) {
                        //PPApplicationStatic.recordException(e);
                    }
                }
                if (startTargetHelpsOrderSpinner) {
                    if (filterType != FILTER_TYPE_START_ORDER) {
                        try {
                            targets.add(
                                    //TapTarget.forBounds(orderSpinnerTarget, getString(R.string.editor_activity_targetHelps_orderSpinner_title), getString(R.string.editor_activity_targetHelps_orderSpinner_description))
                                    TapTarget.forView(orderSpinner, getString(R.string.editor_activity_targetHelps_orderSpinner_title), getString(R.string.editor_activity_targetHelps_orderSpinner_description))
                                            .transparentTarget(true)
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .titleTextColor(titleTextColor)
                                            .descriptionTextColor(descriptionTextColor)
                                            .descriptionTextAlpha(PPApplication.descriptionTapTargetAlpha)
                                            .dimColor(R.color.tabTargetHelpDimColor)
                                            .titleTextSize(PPApplication.titleTapTargetSize)
                                            .textTypeface(Typeface.DEFAULT_BOLD)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(1)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplicationStatic.recordException(e);
                        }
                    }
                }

                for (TapTarget target : targets) {
                    target.setDrawBehindStatusBar(true);
                    target.setDrawBehindNavigationBar(true);
                }

                sequence.targets(targets)
                        .listener(new TapTargetSequence.Listener() {
                            // This listener will tell us when interesting(tm) events happen in regards
                            // to the sequence
                            @Override
                            public void onSequenceFinish() {
                                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activityDataWrapper.context);
                                editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_FRAGMENT_START_TARGET_HELPS_FINISHED, true);
                                editor.apply();
                                ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsFinished = true;

                                showAdapterTargetHelps();
                            }

                            @Override
                            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                                //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                            }

                            @Override
                            public void onSequenceCanceled(TapTarget lastTarget) {
                                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activityDataWrapper.context);
                                editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_ADAPTER_START_TARGET_HELPS, false);
                                if (filterType == FILTER_TYPE_START_ORDER)
                                    editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_ADAPTER_START_TARGET_HELPS_ORDER, false);
                                editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_ADAPTER_START_TARGET_HELPS_STATUS, false);

                                editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_FRAGMENT_START_TARGET_HELPS_FINISHED, true);

                                editor.apply();

                                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps = false;
                                if (filterType != FILTER_TYPE_START_ORDER)
                                    ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder = false;
                                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsStatus = false;

                                ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsFinished = true;
                            }
                        });
                sequence.continueOnCancel(true)
                        .considerOuterCircleCanceled(true);

                editor = ApplicationPreferences.getEditor(activityDataWrapper.context);
                editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_FRAGMENT_START_TARGET_HELPS_FINISHED, false);
                editor.apply();
                ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsFinished = false;

                sequence.start();
            }
            else {
                //Log.d("EditorEventListFragment.showTargetHelps", "PREF_START_TARGET_HELPS=false");
                final Handler handler = new Handler(getActivity().getMainLooper());
                final WeakReference<EditorEventListFragment> fragmentWeakRef = new WeakReference<>(this);
                handler.postDelayed(() -> {
//                        PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EditorEventListFragment.showTargetHelps");
                    EditorEventListFragment fragment = fragmentWeakRef.get();
                    if (fragment != null) {
                        Activity activity = fragment.getActivity();
                        if ((activity == null) || activity.isFinishing() || activity.isDestroyed())
                            return;

                        //noinspection Convert2MethodRef
                        fragment.showAdapterTargetHelps();
                    }
                }, 500);
            }
        }
    }

    private void showAdapterTargetHelps() {
        if (getActivity() == null)
            return;

        View itemView;
        if (listView.getChildCount() > 1)
            itemView = listView.getChildAt(1);
        else
            itemView = listView.getChildAt(0);
        if ((eventListAdapter != null) && (itemView != null))
            eventListAdapter.showTargetHelps(getActivity(), /*this,*/ itemView);
        else {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(getActivity().getApplicationContext());
            editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_ADAPTER_START_TARGET_HELPS, false);
            if (filterType == FILTER_TYPE_START_ORDER)
                editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_ADAPTER_START_TARGET_HELPS_ORDER, false);
            editor.apply();
            ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps = false;
            if (filterType == FILTER_TYPE_START_ORDER)
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder = false;
        }
    }

    private void changeEventOrder(int position, boolean fromOnViewCreated) {
        if ((orderSpinner.getAdapter() == null) || (orderSpinner.getAdapter().getCount() <= position)) {
            orderSelectedItem = 0;
        }
        else
            orderSelectedItem = position;

        if (filterType != EditorEventListFragment.FILTER_TYPE_START_ORDER) {
            // save into shared preferences
            if (getActivity() != null) {
                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(getActivity().getApplicationContext());
                editor.putInt(ApplicationPreferences.PREF_EDITOR_ORDER_SELECTED_ITEM, orderSelectedItem);
                editor.apply();
                ApplicationPreferences.editorOrderSelectedItem(getActivity().getApplicationContext());
            }
        }

        int _eventsOrderType = getEventsOrderType();
        //setStatusBarTitle();

        changeListOrder(_eventsOrderType, fromOnViewCreated);

        orderSpinner.setSelection(orderSelectedItem);

        /*
        // Close drawer
        if (ApplicationPreferences.applicationEditorAutoCloseDrawer(getApplicationContext()) && (!orientationChange))
            drawerLayout.closeDrawer(drawerRoot);
        */
    }

    private int getEventsOrderType() {
        int _eventsOrderType;
        if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER) {
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
                    _eventsOrderType = EditorEventListFragment.ORDER_TYPE_START_PROFILE_NAME;
                    break;
                case 3:
                    _eventsOrderType = EditorEventListFragment.ORDER_TYPE_END_PROFILE_NAME;
                    break;
                case 4:
                    _eventsOrderType = EditorEventListFragment.ORDER_TYPE_PRIORITY;
                    break;
            }
        }
        return _eventsOrderType;
    }

    void showIgnoreManualActivationMenu(View view)
    {
        // because of refresh list is popup menu moved up
        // for this reason is used SingleSelectListDialog

        if (getActivity() == null)
            return;

        final Event event = (Event)view.getTag();

        int value;
        if (event._ignoreManualActivation && event._noPauseByManualActivation)
            value = 2;
        else if (event._ignoreManualActivation)
            value = 1;
        else
            value = 0;

        SingleSelectListDialog dialog = new SingleSelectListDialog(
                true,
                getString(R.string.event_string_0) + StringConstants.STR_COLON_WITH_SPACE + event._name,
                getString(R.string.event_preferences_ForceRun),
                R.array.eventListItemIgnoreManualActivationArray,
                value,
                (dialog1, which) -> {
                    switch (which) {
                        case 0:
                            event._ignoreManualActivation = false;
                            DatabaseHandler.getInstance(activityDataWrapper.context).updateEventForceRun(event);
                            EventsPrefsActivity.saveUpdateOfPreferences(event, activityDataWrapper, event.getStatus());
                            ((EditorActivity) getActivity()).redrawEventListFragment(event, PPApplication.EDIT_MODE_EDIT);

                            PPApplication.showToast(activityDataWrapper.context.getApplicationContext(),
                                    getString(R.string.ignore_manual_activation_not_ignore_toast),
                                    Toast.LENGTH_LONG);
                            break;
                        case 1:
                            event._ignoreManualActivation = true;
                            event._noPauseByManualActivation = false;
                            DatabaseHandler.getInstance(activityDataWrapper.context).updateEventForceRun(event);
                            EventsPrefsActivity.saveUpdateOfPreferences(event, activityDataWrapper, event.getStatus());
                            ((EditorActivity) getActivity()).redrawEventListFragment(event, PPApplication.EDIT_MODE_EDIT);

                            PPApplication.showToast(activityDataWrapper.context.getApplicationContext(),
                                    getString(R.string.ignore_manual_activation_ignore_toast),
                                    Toast.LENGTH_LONG);
                            break;
                        case 2:
                            event._ignoreManualActivation = true;
                            event._noPauseByManualActivation = true;
                            DatabaseHandler.getInstance(activityDataWrapper.context).updateEventForceRun(event);
                            EventsPrefsActivity.saveUpdateOfPreferences(event, activityDataWrapper, event.getStatus());
                            ((EditorActivity) getActivity()).redrawEventListFragment(event, PPApplication.EDIT_MODE_EDIT);

                            PPApplication.showToast(activityDataWrapper.context.getApplicationContext(),
                                    getString(R.string.ignore_manual_activation_ignore_no_pause_toast),
                                    Toast.LENGTH_LONG);
                            break;
                        default:
                    }
                },
                null,
                false,
                getActivity());
        dialog.show();

/*
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        Context _context = view.getContext();
        //Context context = new ContextThemeWrapper(getActivity().getBaseContext(), R.style.PopupMenu_editorItem_dayNight);
        PopupMenu popup;
        popup = new PopupMenu(_context, view, Gravity.END);
        getActivity().getMenuInflater().inflate(R.menu.event_list_item_ignore_manual_activation, popup.getMenu());

        // show icons
        try {
            Field field = popup.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            Object menuPopupHelper = field.get(popup);
            Class<?> cls = Class.forName("com.android.internal.view.menu.MenuPopupHelper");
            Method method = cls.getDeclaredMethod("setForceShowIcon", new Class[]{boolean.class});
            method.setAccessible(true);
            method.invoke(menuPopupHelper, new Object[]{true});
        } catch (Exception e) {
            //PPApplicationStatic.recordException(e);
        }

        final Event event = (Event)view.getTag();

        Menu menu = popup.getMenu();
        Drawable drawable;
        {
            drawable = menu.findItem(R.id.event_list_item_ignore_manual_activation_no_pause).getIcon();
            if (drawable != null) {
                drawable.mutate();
                if (event._ignoreManualActivation && event._noPauseByManualActivation)
                    drawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.accent_color), PorterDuff.Mode.SRC_ATOP);
                else
                    drawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.notSelectedIconColor), PorterDuff.Mode.SRC_ATOP);
            }
            drawable = menu.findItem(R.id.event_list_item_ignore_manual_activation).getIcon();
            if (drawable != null) {
                drawable.mutate();
                if (event._ignoreManualActivation && (!event._noPauseByManualActivation))
                    drawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.accent_color), PorterDuff.Mode.SRC_ATOP);
                else
                    drawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.notSelectedIconColor), PorterDuff.Mode.SRC_ATOP);
            }
            drawable = menu.findItem(R.id.event_list_item_not_ignore_manual_activation).getIcon();
            if (drawable != null) {
                drawable.mutate();
                if ((!event._ignoreManualActivation))
                    drawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.accent_color), PorterDuff.Mode.SRC_ATOP);
                else
                    drawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.notSelectedIconColor), PorterDuff.Mode.SRC_ATOP);
            }
        }

        popup.setOnMenuItemClickListener(item -> {
            if (getActivity() != null) {
                int itemId = item.getItemId();
                if (itemId == R.id.event_list_item_ignore_manual_activation_title) {
                    PPApplication.showToast(activityDataWrapper.context.getApplicationContext(),
                            getString(R.string.popupmenu_title_click_below_toast),
                            Toast.LENGTH_SHORT);
                    return true;
                }
                else
                if (itemId == R.id.event_list_item_not_ignore_manual_activation) {
                    event._ignoreManualActivation = false;
                    DatabaseHandler.getInstance(activityDataWrapper.context).updateEventForceRun(event);
                    EventsPrefsActivity.saveUpdateOfPreferences(event, activityDataWrapper, event.getStatus());
                    ((EditorActivity) getActivity()).redrawEventListFragment(event, EDIT_MODE_EDIT);

                    PPApplication.showToast(activityDataWrapper.context.getApplicationContext(),
                            getString(R.string.ignore_manual_activation_not_ignore_toast),
                            Toast.LENGTH_LONG);

                    return true;
                }
                else
                if (itemId == R.id.event_list_item_ignore_manual_activation) {
                    event._ignoreManualActivation = true;
                    event._noPauseByManualActivation = false;
                    DatabaseHandler.getInstance(activityDataWrapper.context).updateEventForceRun(event);
                    EventsPrefsActivity.saveUpdateOfPreferences(event, activityDataWrapper, event.getStatus());
                    ((EditorActivity) getActivity()).redrawEventListFragment(event, EDIT_MODE_EDIT);

                    PPApplication.showToast(activityDataWrapper.context.getApplicationContext(),
                            getString(R.string.ignore_manual_activation_ignore_toast),
                            Toast.LENGTH_LONG);

                    return true;
                }
                else
                if (itemId == R.id.event_list_item_ignore_manual_activation_no_pause) {
                    event._ignoreManualActivation = true;
                    event._noPauseByManualActivation = true;
                    DatabaseHandler.getInstance(activityDataWrapper.context).updateEventForceRun(event);
                    EventsPrefsActivity.saveUpdateOfPreferences(event, activityDataWrapper, event.getStatus());
                    ((EditorActivity) getActivity()).redrawEventListFragment(event, EDIT_MODE_EDIT);

                    PPApplication.showToast(activityDataWrapper.context.getApplicationContext(),
                            getString(R.string.ignore_manual_activation_ignore_no_pause_toast),
                            Toast.LENGTH_LONG);

                    return true;
                }
                else {
                    return false;
                }
            }
            return true;
        });

        if ((getActivity() != null) && (!getActivity().isFinishing()))
            popup.show();
*/
    }

    void showHeaderAndBottomToolbar() {
        if (activatedProfileHeader != null)
            activatedProfileHeader.setVisibility(VISIBLE);
        if (bottomToolbar != null)
            bottomToolbar.setVisibility(VISIBLE);
    }

    private static class RefreshGUIAsyncTask extends AsyncTask<Void, Integer, Void> {

        long activatedProfileId;
        Profile profileFromDataWrapperForHeader;

        boolean doNotRefresh = false;

        private final WeakReference<EditorEventListFragment> fragmentWeakRef;
        final DataWrapper dataWrapper;
        private boolean refreshIcons;
        private final boolean setPosition;
        private final long eventId;

        public RefreshGUIAsyncTask(final boolean refreshIcons,
                                   final boolean setPosition,
                                   final long eventId,
                                   final EditorEventListFragment fragment,
                                   final DataWrapper dataWrapper) {
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.dataWrapper = dataWrapper.copyDataWrapper();
            this.refreshIcons = refreshIcons;
            this.setPosition = setPosition;
            this.eventId = eventId;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (fragmentWeakRef.get() != null) {
                try {
                    activatedProfileId = DatabaseHandler.getInstance(dataWrapper.context).getActivatedProfileId();
                    dataWrapper.getEventTimelineList(true);

                    // must be refreshed timelinelist for fragment.activityDataWrapper
                    EditorEventListFragment fragment = fragmentWeakRef.get();
                    if ((fragment != null) && (fragment.activityDataWrapper != null)) {
                        fragment.activityDataWrapper.getEventTimelineList(true);
                    }

                    if (activatedProfileId != -1) {
                        profileFromDataWrapperForHeader = dataWrapper.getProfileByIdFromDB(activatedProfileId, true,
                                ApplicationPreferences.applicationEditorPrefIndicator, false);
                    }

                    /*
                    String pName;
                    if (profileFromDB != null) {
                        pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profileFromDB, true, "", true, false, false, activityDataWrapper);
                    } else
                        pName = activityDataWrapper.context.getString(R.string.profiles_header_profile_name_no_activated);

                    if (!refresh) {
                        String pNameHeader = PPApplication.prefActivityProfileName3;

                        if ((!pNameHeader.isEmpty()) && pName.equals(pNameHeader)) {
                            doNotRefresh = true;
                            return null;
                        }
                    }

                    PPApplication.setActivityProfileName(activityDataWrapper.context, 2, pName);
                    PPApplication.setActivityProfileName(activityDataWrapper.context, 3, pName);
                    */

//                    PPApplicationStatic.logE("[SYNCHRONIZED] EditorEventListFragment.RefreshGUIAsyncTask", "(1) DataWrapper.eventList");
                    synchronized (dataWrapper.eventList) {
                        if (!dataWrapper.eventListFilled) {
                            doNotRefresh = true;
                            return null;
                        }

                        //noinspection ForLoopReplaceableByForEach
                        for (Iterator<Event> it = dataWrapper.eventList.iterator(); it.hasNext(); ) {
                            Event event = it.next();
                            int status = DatabaseHandler.getInstance(dataWrapper.context).getEventStatus(event);
                            event.setStatus(status);
                            event._isInDelayStart = DatabaseHandler.getInstance(dataWrapper.context).getEventInDelayStart(event);
                            event._isInDelayEnd = DatabaseHandler.getInstance(dataWrapper.context).getEventInDelayEnd(event);

                            if ((fragment != null) && (fragment.getActivity() != null))
                                event._peferencesDecription = StringFormatUtils.fromHtml(
                                        event.getPreferencesDescription(fragment.getActivity(), dataWrapper, true),
                                        true,  false, 0, 0, true);

                            DatabaseHandler.getInstance(dataWrapper.context).setEventCalendarTimes(event);
                            DatabaseHandler.getInstance(dataWrapper.context).getSMSStartTime(event);
                            //DatabaseHandler.getInstance(activityDataWrapper.context).getNotificationStartTime(event);
                            DatabaseHandler.getInstance(dataWrapper.context).getNFCStartTime(event);
                            DatabaseHandler.getInstance(dataWrapper.context).getCallRunAfterCallEndTime(event);
                            DatabaseHandler.getInstance(dataWrapper.context).getAlarmClockStartTime(event);
                            DatabaseHandler.getInstance(dataWrapper.context).getDeviceBootStartTime(event);
                            DatabaseHandler.getInstance(dataWrapper.context).getPeriodicStartTime(event);
                            DatabaseHandler.getInstance(dataWrapper.context).getApplicationStartTime(event);
                            DatabaseHandler.getInstance(dataWrapper.context).getCallScreeningStartTime(event);
                        }
                    }

                    if (eventId != 0) {
                        Event eventFromDB = DatabaseHandler.getInstance(dataWrapper.context).getEvent(eventId);
                        Activity activity = null;
                        if (fragment != null)
                            activity = fragment.getActivity();
                        dataWrapper.updateEvent(eventFromDB, activity);
                        refreshIcons = true;
                    }
                } catch (Exception e) {
                    if ((dataWrapper != null) && (dataWrapper.context != null))
                        PPApplicationStatic.recordException(e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            EditorEventListFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if ((fragment.getActivity() != null) && (!fragment.getActivity().isFinishing())) {
                    if (!doNotRefresh) {
                        if (activatedProfileId != -1) {
                            if (profileFromDataWrapperForHeader != null)
                                profileFromDataWrapperForHeader._checked = true;
                            fragment.updateHeader(profileFromDataWrapperForHeader);
                        } else {
                            fragment.updateHeader(null);
                        }
                        fragment.updateListView(null, false, refreshIcons, setPosition/*, eventId*/);
                    }
                }
            }
        }

    }

    private static class UpdateHeaderAsyncTask extends AsyncTask<Void, Integer, Void> {

        boolean redTextVisible = false;
        DataWrapper _dataWrapper;

        private final WeakReference<EditorEventListFragment> fragmentWeakRef;

        public UpdateHeaderAsyncTask(final EditorEventListFragment fragment) {
            this.fragmentWeakRef = new WeakReference<>(fragment);
        }

        @Override
        protected Void doInBackground(Void... params) {
            EditorEventListFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if (fragment.getActivity() != null) {
                    _dataWrapper = new DataWrapper(fragment.getActivity().getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
                    _dataWrapper.copyEventList(fragment.activityDataWrapper);

                    for (Event event : _dataWrapper.eventList) {
                        if (EventStatic.isRedTextNotificationRequired(event, false, _dataWrapper.context))
                            redTextVisible = true;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            EditorEventListFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if ((fragment.getActivity() != null) && (!fragment.getActivity().isFinishing())) {
                    try {
                        //if (activatedProfileHeader.isVisibleToUser()) {
                        TextView redText = fragment.activatedProfileHeader.findViewById(R.id.editor_events_activated_profile_red_text);
                        if (redTextVisible)
                            //noinspection DataFlowIssue
                            redText.setVisibility(View.VISIBLE);
                        else
                            //noinspection DataFlowIssue
                            redText.setVisibility(GONE);
                        //}
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            }
        }

    }

/*    private static abstract class RunStopEventRunnable implements Runnable {

        final WeakReference<DataWrapper> dataWrapperWeakRef;
        final WeakReference<Event> eventWeakRef;

        RunStopEventRunnable(DataWrapper dataWrapper,
                                       Event event) {
            this.dataWrapperWeakRef = new WeakReference<>(dataWrapper);
            this.eventWeakRef = new WeakReference<>(event);
        }

    }*/


    void updateBottomMenu() {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EditorEventListFragment.updateBottomMenu", "(1) DataWrapper.eventList");
        synchronized (activityDataWrapper.eventList) {
            Menu menu = bottomToolbar.getMenu();
            if (menu != null) {
                MenuItem item = menu.findItem(R.id.menu_generate_predefined_events);
                item.setVisible(activityDataWrapper.eventList.isEmpty());
                item = menu.findItem(R.id.menu_delete_all_events);
                item.setVisible(!activityDataWrapper.eventList.isEmpty());
            }
        }
    }

}
