package sk.henrichg.phoneprofilesplus;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

//import me.drakeet.support.toast.ToastCompat;

public class EditorEventListFragment extends Fragment
                                        implements OnStartDragItemListener {

    public DataWrapper activityDataWrapper;

    private View rootView;
    private RelativeLayout activatedProfileHeader;
    RecyclerView listView;
    private TextView activeProfileName;
    private ImageView activeProfileIcon;
    private Toolbar bottomToolbar;
    TextView textViewNoData;
    private LinearLayout progressBar;
    private AppCompatSpinner orderSpinner;
    private ImageView profilePrefIndicatorImageView;

    private EditorEventListAdapter eventListAdapter;
    private ItemTouchHelper itemTouchHelper;

    private WeakReference<LoadEventListAsyncTask> asyncTaskContext;

    Event scrollToEvent = null;

//    private ValueAnimator hideAnimatorHeader;
//    private ValueAnimator showAnimatorHeader;
//    private int headerHeight;
//    private ValueAnimator hideAnimatorBottomBar;
//    private ValueAnimator showAnimatorBottomBar;
//    private int bottomBarHeight;

    private int orderSelectedItem = 0;

    static final int EDIT_MODE_UNDEFINED = 0;
    static final int EDIT_MODE_INSERT = 1;
    static final int EDIT_MODE_DUPLICATE = 2;
    static final int EDIT_MODE_EDIT = 3;
    static final int EDIT_MODE_DELETE = 4;

    static final String FILTER_TYPE_ARGUMENT = "filter_type";
    //static final String ORDER_TYPE_ARGUMENT = "order_type";
    static final String START_TARGET_HELPS_ARGUMENT = "start_target_helps";

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

    public boolean targetHelpsSequenceStarted;
    public static final String PREF_START_TARGET_HELPS = "editor_event_list_fragment_start_target_helps";
    public static final String PREF_START_TARGET_HELPS_FILTER_SPINNER = "editor_profile_activity_start_target_helps_filter_spinner";
    public static final String PREF_START_TARGET_HELPS_ORDER_SPINNER = "editor_profile_activity_start_target_helps_order_spinner";

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
                getArguments().getInt(FILTER_TYPE_ARGUMENT, EditorEventListFragment.FILTER_TYPE_START_ORDER) :
                    EditorEventListFragment.FILTER_TYPE_START_ORDER;
//        orderType = getArguments() != null ?
//                getArguments().getInt(ORDER_TYPE_ARGUMENT, EditorEventListFragment.ORDER_TYPE_START_ORDER) :
//                    EditorEventListFragment.ORDER_TYPE_START_ORDER;
        orderType = getEventsOrderType();

        //Log.d("EditorEventListFragment.onCreate","filterType="+filterType);
        //Log.d("EditorEventListFragment.onCreate","orderType="+orderType);

        //noinspection ConstantConditions
        activityDataWrapper = new DataWrapper(getActivity().getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0f);

        //getActivity().getIntent();

        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.editor_event_list, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        doOnViewCreated(view, true);

        boolean startTargetHelps = getArguments() != null && getArguments().getBoolean(START_TARGET_HELPS_ARGUMENT, false);
        if (startTargetHelps)
            showTargetHelps();
    }

    @SuppressLint({"AlwaysShowAction", "SetTextI18n"})
    private void doOnViewCreated(View view, boolean fromOnViewCreated)
    {
        profilePrefIndicatorImageView = view.findViewById(R.id.activated_profile_pref_indicator);
        if (!ApplicationPreferences.applicationEditorPrefIndicator)
            profilePrefIndicatorImageView.setVisibility(GONE);


        activeProfileName = view.findViewById(R.id.activated_profile_name);
        activeProfileIcon = view.findViewById(R.id.activated_profile_icon);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        listView = view.findViewById(R.id.editor_events_list);
        //listView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        activatedProfileHeader = view.findViewById(R.id.activated_profile_header);
        bottomToolbar = view.findViewById(R.id.editor_list_bottom_bar);

        //noinspection ConstantConditions
        if (GlobalGUIRoutines.areSystemAnimationsEnabled(getActivity().getApplicationContext())) {
            /*if (activatedProfileHeader != null) {
                Handler handler = new Handler(getActivity().getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() == null)
                            return;

                        headerHeight = activatedProfileHeader.getMeasuredHeight();
                        //Log.e("EditorProfileListFragment.doOnViewCreated", "headerHeight="+headerHeight);
                        hideAnimatorHeader = ValueAnimator.ofInt(headerHeight / 4, 0);
                        hideAnimatorHeader.setDuration(500);
                        hideAnimatorHeader.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                int val = (Integer) valueAnimator.getAnimatedValue();
                                //Log.e("hideAnimator.onAnimationUpdate", "val="+val);
                                ViewGroup.LayoutParams layoutParams = activatedProfileHeader.getLayoutParams();
                                layoutParams.height = val * 4;
                                activatedProfileHeader.setLayoutParams(layoutParams);
                            }
                        });
                        showAnimatorHeader = ValueAnimator.ofInt(0, headerHeight / 4);
                        showAnimatorHeader.setDuration(500);
                        showAnimatorHeader.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                int val = (Integer) valueAnimator.getAnimatedValue();
                                //Log.e("showAnimator.onAnimationUpdate", "val="+val);
                                ViewGroup.LayoutParams layoutParams = activatedProfileHeader.getLayoutParams();
                                layoutParams.height = val * 4;
                                activatedProfileHeader.setLayoutParams(layoutParams);
                            }
                        });

                        bottomBarHeight = bottomToolbar.getMeasuredHeight();
                        //Log.e("EditorProfileListFragment.doOnViewCreated", "headerHeight="+headerHeight);
                        hideAnimatorBottomBar = ValueAnimator.ofInt(bottomBarHeight / 4, 0);
                        hideAnimatorBottomBar.setDuration(500);
                        hideAnimatorBottomBar.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                int val = (Integer) valueAnimator.getAnimatedValue();
                                //Log.e("hideAnimator.onAnimationUpdate", "val="+val);
                                ViewGroup.LayoutParams layoutParams = bottomToolbar.getLayoutParams();
                                layoutParams.height = val * 4;
                                bottomToolbar.setLayoutParams(layoutParams);
                            }
                        });
                        showAnimatorBottomBar = ValueAnimator.ofInt(0, bottomBarHeight / 4);
                        showAnimatorBottomBar.setDuration(500);
                        showAnimatorBottomBar.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                int val = (Integer) valueAnimator.getAnimatedValue();
                                //Log.e("showAnimator.onAnimationUpdate", "val="+val);
                                ViewGroup.LayoutParams layoutParams = bottomToolbar.getLayoutParams();
                                layoutParams.height = val * 4;
                                bottomToolbar.setLayoutParams(layoutParams);
                            }
                        });

                    }
                }, 200);
            }*/

            if (ApplicationPreferences.applicationEditorHideHeaderOrBottomBar) {
                final LayoutTransition layoutTransition = ((ViewGroup) view.findViewById(R.id.layout_events_list_fragment))
                        .getLayoutTransition();
                layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
                //layoutTransition.setDuration(500);

                listView.addOnScrollListener(new HidingRecyclerViewScrollListener() {
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
                            //final int firstVisibleItem = ((LinearLayoutManager) listView.getLayoutManager()).findFirstVisibleItemPosition();
                            //if (firstVisibleItem != 0)
                            activatedProfileHeader.setVisibility(View.GONE);

                            bottomToolbar.setVisibility(VISIBLE);
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
                            //final int firstVisibleItem = ((LinearLayoutManager) listView.getLayoutManager()).findFirstVisibleItemPosition();
                            //if (firstVisibleItem == 0)
                            activatedProfileHeader.setVisibility(VISIBLE);

                            bottomToolbar.setVisibility(View.GONE);
                        }
                    }
                });
            }
            else
                showHeaderAndBottomToolbar();
        }

        textViewNoData = view.findViewById(R.id.editor_events_list_empty);
        progressBar = view.findViewById(R.id.editor_events_list_linla_progress);

        /*
        View footerView =  getActivity().getLayoutInflater()..inflate(R.layout.editor_list_footer, null, false);
        listView.addFooterView(footerView, null, false);
        */

        final EditorEventListFragment fragment = this;

        Menu menu = bottomToolbar.getMenu();
        if (menu != null) menu.clear();
        bottomToolbar.inflateMenu(R.menu.editor_events_bottom_bar);

//        menu = bottomToolbar.getMenu();
//        if (menu != null) {
//            MenuItem item = menu.findItem(R.id.menu_default_profile);
//
//            if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER)
//                item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
//            else
//                item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
//        }

        bottomToolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_add_event) {
                if (eventListAdapter != null) {
                    if (!getActivity().isFinishing()) {
                        ((EditorProfilesActivity) getActivity()).addEventDialog = new AddEventDialog(getActivity(), fragment);
                        ((EditorProfilesActivity) getActivity()).addEventDialog.show();
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
                Intent intent = new Intent(getActivity(), PhoneProfilesPrefsActivity.class);
                intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "profileActivationCategoryRoot");
                startActivity(intent);
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

        LinearLayout bottomBarOrderRoot = view.findViewById(R.id.editor_list_bottom_bar_order_root);
        if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER)
            bottomBarOrderRoot.setVisibility(View.INVISIBLE); // MUST BE INVISIBLE, required for showTargetHelps().
        else
            bottomBarOrderRoot.setVisibility(VISIBLE);

        orderSpinner = view.findViewById(R.id.editor_list_bottom_bar_order);

//        if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER)
//            orderSpinner.setVisibility(View.INVISIBLE); // MUST BE INVISIBLE, required for shoTargetHelps().
//        else
//            orderSpinner.setVisibility(VISIBLE);

        TextView orderLabel = view.findViewById(R.id.editor_list_bottom_bar_order_title);
        orderLabel.setText(getString(R.string.editor_drawer_title_events_order) + ":");

        String[] orderItems = new String[] {
                /*getString(R.string.editor_drawer_title_events_order) + ": " +*/ getString(R.string.editor_drawer_order_start_order),
                /*getString(R.string.editor_drawer_title_events_order) + ": " +*/ getString(R.string.editor_drawer_order_event_name),
                /*getString(R.string.editor_drawer_title_events_order) + ": " +*/ getString(R.string.editor_drawer_order_start_profile_name),
                /*getString(R.string.editor_drawer_title_events_order) + ": " +*/ getString(R.string.editor_drawer_order_end_profile_name),
                /*getString(R.string.editor_drawer_title_events_order) + ": " +*/ getString(R.string.editor_drawer_order_priority)
        };

        GlobalGUIRoutines.HighlightedSpinnerAdapter orderSpinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                getActivity(),
                R.layout.highlighted_order_spinner,
                orderItems);
        orderSpinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
        orderSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
        orderSpinner.setBackgroundTintList(ContextCompat.getColorStateList(getActivity()/*.getBaseContext()*/, R.color.highlighted_spinner_all));
        orderSpinner.setAdapter(orderSpinnerAdapter);
        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((GlobalGUIRoutines.HighlightedSpinnerAdapter)orderSpinner.getAdapter()).setSelection(position);
                if (position != orderSelectedItem)
                    changeEventOrder(position, false);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        orderLabel.setOnClickListener(v -> orderSpinner.performClick());

        //PPApplication.logE("EditorEventListFragment.doOnViewCreated", "orderSelectedItem="+orderSelectedItem);
        // first must be set eventsOrderType
        changeEventOrder(orderSelectedItem, fromOnViewCreated);
    }

    void changeFragmentFilter(int eventsFilterType, boolean startTargetHelps) {
        filterType = eventsFilterType;

        doOnViewCreated(rootView, false);

        if (startTargetHelps)
            showTargetHelps();
    }

    private static class LoadEventListAsyncTask extends AsyncTask<Void, Void, Void> {

        private final WeakReference<EditorEventListFragment> fragmentWeakRef;
        private final DataWrapper _dataWrapper;
        private final int _filterType;
        private final int _orderType;
        boolean defaultEventsGenerated = false;

        final boolean applicationEditorPrefIndicator;

        Handler progressBarHandler;
        Runnable progressBarRunnable;

        public LoadEventListAsyncTask (EditorEventListFragment fragment, int filterType, int orderType) {
            fragmentWeakRef = new WeakReference<>(fragment);
            _filterType = filterType;
            _orderType = orderType;
            //noinspection ConstantConditions
            _dataWrapper = new DataWrapper(fragment.getActivity().getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0f);

            applicationEditorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            final EditorEventListFragment fragment = this.fragmentWeakRef.get();

            if ((fragment != null) && (fragment.isAdded())) {
                progressBarHandler = new Handler(_dataWrapper.context.getMainLooper());
                progressBarRunnable = () -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EditorEventListFragment.LoadEventListAsyncTask (1)");
                    //fragment.textViewNoData.setVisibility(GONE);
                    fragment.progressBar.setVisibility(VISIBLE);
                };
                progressBarHandler.postDelayed(progressBarRunnable, 100);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            _dataWrapper.fillProfileList(true, applicationEditorPrefIndicator);
            _dataWrapper.fillEventList();
            //Log.e("EditorEventListFragment.LoadEventListAsyncTask","_dataWrapper.eventList.size()="+_dataWrapper.eventList.size());

            if ((_dataWrapper.eventList.size() == 0) && PPApplication.restoreFinished)
            {
                if (ApplicationPreferences.getSharedPreferences(_dataWrapper.context).getBoolean(ApplicationPreferences.PREF_EDITOR_EVENTS_FIRST_START, true)) {
                    // no events in DB, generate default events
                    // PPApplication.restoreFinished = Google auto-backup finished
                    final EditorEventListFragment fragment = this.fragmentWeakRef.get();
                    if ((fragment != null) && (fragment.getActivity() != null)) {
                        _dataWrapper.generatePredefinedEventList(fragment.getActivity());
                        defaultEventsGenerated = true;
                    }
                }
            }

            SharedPreferences sharedPreferences = ApplicationPreferences.getSharedPreferences(_dataWrapper.context);
            if (sharedPreferences != null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(ApplicationPreferences.PREF_EDITOR_EVENTS_FIRST_START, false);
                editor.apply();
            }

            _dataWrapper.getEventTimelineList(true);
            if (_filterType == FILTER_TYPE_START_ORDER)
                EditorEventListFragment.sortList(_dataWrapper.eventList, ORDER_TYPE_START_ORDER, _dataWrapper);
            else
                EditorEventListFragment.sortList(_dataWrapper.eventList, _orderType, _dataWrapper);

            return null;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);
            
            EditorEventListFragment fragment = fragmentWeakRef.get();
            
            if ((fragment != null) && (fragment.isAdded())) {
                progressBarHandler.removeCallbacks(progressBarRunnable);
                fragment.progressBar.setVisibility(View.GONE);

                // get local profileList
                _dataWrapper.fillProfileList(true, applicationEditorPrefIndicator);
                // set local profile list into activity dataWrapper
                fragment.activityDataWrapper.copyProfileList(_dataWrapper);

                // get local eventList
                _dataWrapper.fillEventList();
                // set local event list into activity dataWrapper
                fragment.activityDataWrapper.copyEventList(_dataWrapper);

                synchronized (fragment.activityDataWrapper.eventList) {
                    if (fragment.activityDataWrapper.eventList.size() == 0)
                        fragment.textViewNoData.setVisibility(VISIBLE);
                }

                // get local eventTimelineList
                _dataWrapper.getEventTimelineList(true);
                // set copy local event timeline list into activity dataWrapper
                fragment.activityDataWrapper.copyEventTimelineList(_dataWrapper);

                fragment.eventListAdapter = new EditorEventListAdapter(fragment, fragment.activityDataWrapper, _filterType, fragment);

                // added touch helper for drag and drop items
                ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(fragment.eventListAdapter, false, false);
                fragment.itemTouchHelper = new ItemTouchHelper(callback);
                fragment.itemTouchHelper.attachToRecyclerView(fragment.listView);

                fragment.listView.setAdapter(fragment.eventListAdapter);

                Profile profile = _dataWrapper.getActivatedProfileFromDB(true, applicationEditorPrefIndicator);
                fragment.updateHeader(profile);
                fragment.eventListAdapter.notifyDataSetChanged(false);

                if (defaultEventsGenerated)
                {
                    if ((fragment.getActivity() != null ) && (!fragment.getActivity().isFinishing()))
                        PPApplication.showToast(_dataWrapper.context.getApplicationContext(),
                                fragment.getString(R.string.toast_predefined_events_generated),
                                Toast.LENGTH_SHORT);
                }
            }
        }
    }

    boolean isAsyncTaskPendingOrRunning() {
        try {
            return this.asyncTaskContext != null &&
                    this.asyncTaskContext.get() != null &&
                    !this.asyncTaskContext.get().getStatus().equals(AsyncTask.Status.FINISHED);
        } catch (Exception e) {
            return false;
        }
    }

    void stopRunningAsyncTask() {
        this.asyncTaskContext.get().cancel(true);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (isAsyncTaskPendingOrRunning()) {
            stopRunningAsyncTask();
        }

        if (listView != null)
            listView.setAdapter(null);
        if (eventListAdapter != null)
            eventListAdapter.release();

        //if (activityDataWrapper != null)
        //    activityDataWrapper.invalidateDataWrapper();
        //activityDataWrapper = null;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    void startEventPreferencesActivity(Event event, int predefinedEventIndex)
    {
        /*
        PPApplication.logE("EditorEventListFragment.startEventPreferencesActivity", "event="+event);
        if (event != null)
            PPApplication.logE("EditorEventListFragment.startEventPreferencesActivity", "event._name="+event._name);
        PPApplication.logE("EditorEventListFragment.startEventPreferencesActivity", "predefinedEventIndex="+predefinedEventIndex);
        */

        int editMode;

        if (event != null)
        {
            // edit event
            int eventPos = eventListAdapter.getItemPosition(event);
            //PPApplication.logE("EditorEventListFragment.startEventPreferencesActivity", "eventPos="+eventPos);
            /*int last = listView.getLastVisiblePosition();
            int first = listView.getFirstVisiblePosition();
            if ((eventPos <= first) || (eventPos >= last)) {
                listView.setSelection(eventPos);
            }*/
            RecyclerView.LayoutManager lm = listView.getLayoutManager();
            if (lm != null)
                lm.scrollToPosition(eventPos);

            boolean startTargetHelps = getArguments() != null && getArguments().getBoolean(START_TARGET_HELPS_ARGUMENT, false);
            if (startTargetHelps)
                showAdapterTargetHelps();

            editMode = EDIT_MODE_EDIT;
        }
        else
        {
            // add new event
            editMode = EDIT_MODE_INSERT;

        }

        //PPApplication.logE("EditorEventListFragment.startEventPreferencesActivity", "editMode="+editMode);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) one must start profile preferences
        onStartEventPreferencesCallback.onStartEventPreferences(event, editMode, predefinedEventIndex);
    }

    boolean runStopEvent(final Event event) {
        if (Event.getGlobalEventsRunning()) {
            // events are not globally stopped

            activityDataWrapper.getEventTimelineList(true);
            if (event.getStatusFromDB(activityDataWrapper.context) == Event.ESTATUS_STOP) {
                if (!EventsPrefsFragment.isRedTextNotificationRequired(event, activityDataWrapper.context)) {
                    // pause event
                    //IgnoreBatteryOptimizationNotification.showNotification(activityDataWrapper.context);

                    final DataWrapper dataWrapper = activityDataWrapper;
                    PPApplication.startHandlerThread();
                    final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
                    //__handler.post(new RunStopEventRunnable(activityDataWrapper, event) {
                    __handler.post(() -> {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EditorEventListFragment.runStopEvent.1");

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
//                                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                                PPApplication.recordException(e);
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        //}
                    });

                }
                else {
                    EditorProfilesActivity.showDialogAboutRedText(null, event, false, false, true, getActivity());
                    return false;
                }
            } else {
                // stop event

                final DataWrapper dataWrapper = activityDataWrapper;
                PPApplication.startHandlerThread();
                final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
                //__handler.post(new RunStopEventRunnable(activityDataWrapper, event) {
                __handler.post(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EditorEventListFragment.runStopEvent.2");

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
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplication.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    //}
                });

            }

            // redraw event list
            //updateListView(event, false, false, true, 0);
            if (getActivity() != null)
                ((EditorProfilesActivity) getActivity()).redrawEventListFragment(event, EDIT_MODE_EDIT);

            // restart events
            //PPApplication.logE("$$$ restartEvents", "from EditorEventListFragment.runStopEvent");
            //activityDataWrapper.restartEvents(false, true, true, true, true);
//            PPApplication.logE("[APP_START] EditorEventListFragment.runStopEvent", "(1)");
            activityDataWrapper.restartEventsWithRescan(true, false, true, false, true, false);

            /*Intent serviceIntent = new Intent(activityDataWrapper.context, PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
            PPApplication.startPPService(activityDataWrapper.context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
            PPApplication.runCommand(activityDataWrapper.context, commandIntent);
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
                ((EditorProfilesActivity) getActivity()).redrawEventListFragment(event, EDIT_MODE_EDIT);

            // restart events
            //PPApplication.logE("$$$ restartEvents", "from EditorEventListFragment.runStopEvent");
            //activityDataWrapper.restartEvents(false, true, true, true, true);
//            PPApplication.logE("[APP_START] EditorEventListFragment.runStopEvent", "(2)");
            activityDataWrapper.restartEventsWithRescan(true, false, true, false, true, false);

            /*Intent serviceIntent = new Intent(activityDataWrapper.context, PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
            PPApplication.startPPService(activityDataWrapper.context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
            PPApplication.runCommand(activityDataWrapper.context, commandIntent);
        }
        return true;
    }

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
        editMode = EDIT_MODE_DUPLICATE;

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

        PPApplication.addActivityLog(activityDataWrapper.context, PPApplication.ALTYPE_EVENT_DELETED, event._name, null, null, 0, "");

        listView.getRecycledViewPool().clear();

        synchronized (activityDataWrapper.eventList) {
            // remove notifications about event parameters errors
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activityDataWrapper.context);
            try {
                notificationManager.cancel(
                        PPApplication.DISPLAY_PREFERENCES_EVENT_ERROR_NOTIFICATION_TAG+"_"+event._id,
                        PPApplication.EVENT_ID_NOTIFICATION_ID + (int) event._id);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }

        eventListAdapter.deleteItemNoNotify(event);
        DatabaseHandler.getInstance(activityDataWrapper.context).deleteEvent(event);

        // restart events
        //PPApplication.logE("$$$ restartEvents", "from EditorEventListFragment.deleteEvent");
        //activityDataWrapper.restartEvents(false, true, true, true, true);
//        PPApplication.logE("[APP_START] EditorEventListFragment.deleteEvent", "xxx");
        activityDataWrapper.restartEventsWithRescan(true, false, true, false, true, false);

        eventListAdapter.notifyDataSetChanged();

        /*Intent serviceIntent = new Intent(getActivity().getApplicationContext(), PhoneProfilesService.class);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
        PPApplication.startPPService(getActivity(), serviceIntent);*/
        Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
        //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
        PPApplication.runCommand(getActivity(), commandIntent);

        onStartEventPreferencesCallback.onStartEventPreferences(null, EDIT_MODE_DELETE, 0);
    }

    public void showEditMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        Context _context = view.getContext();
        PopupMenu popup;
        //if (android.os.Build.VERSION.SDK_INT >= 19)
            popup = new PopupMenu(_context, view, Gravity.END);
        //else
        //    popup = new PopupMenu(context, view);
        Menu menu = popup.getMenu();
        //noinspection ConstantConditions
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
    }

    private void deleteEventWithAlert(Event event)
    {
        final Event _event = event;
        //noinspection ConstantConditions
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(getString(R.string.event_string_0) + ": " + event._name);
        dialogBuilder.setMessage(getString(R.string.delete_event_alert_message));
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setPositiveButton(R.string.alert_button_yes, (dialog, which) -> deleteEvent(_event));
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

        if ((getActivity() != null) && (!getActivity().isFinishing()))
            dialog.show();
    }

    private void deleteAllEvents()
    {
        if (eventListAdapter != null) {
            //noinspection ConstantConditions
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            dialogBuilder.setTitle(getString(R.string.alert_title_delete_all_events));
            dialogBuilder.setMessage(getString(R.string.alert_message_delete_all_events));
            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            dialogBuilder.setPositiveButton(R.string.alert_button_yes, (dialog, which) -> {
                PPApplication.addActivityLog(activityDataWrapper.context, PPApplication.ALTYPE_ALL_EVENTS_DELETED, null, null, null, 0, "");

                listView.getRecycledViewPool().clear();

                activityDataWrapper.stopAllEventsFromMainThread(true, true);

                synchronized (activityDataWrapper.eventList) {
                    // remove notifications about event parameters errors
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activityDataWrapper.context);
                    //noinspection ForLoopReplaceableByForEach
                    for (Iterator<Event> it = activityDataWrapper.eventList.iterator(); it.hasNext(); ) {
                        Event event = it.next();
                        try {
                            notificationManager.cancel(
                                    PPApplication.DISPLAY_PREFERENCES_EVENT_ERROR_NOTIFICATION_TAG+"_"+event._id,
                                    PPApplication.EVENT_ID_NOTIFICATION_ID + (int) event._id);
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                }

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
                    PPApplication.runCommand(getActivity(), commandIntent);
                }

                onStartEventPreferencesCallback.onStartEventPreferences(null, EDIT_MODE_DELETE, 0);
            });
            dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
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

        //Log.e("***** EditorEventListFragment.updateHeader", "profile="+profile);

        String oldDisplayedText = (String)activatedProfileHeader.getTag();

        if (profile == null)
        {
            activatedProfileHeader.setTag(getString(R.string.profiles_header_profile_name_no_activated));

            activeProfileName.setText(getString(R.string.profiles_header_profile_name_no_activated));
            activeProfileIcon.setImageResource(R.drawable.ic_profile_default);
        }
        else
        {
            Spannable profileName = DataWrapper.getProfileNameWithManualIndicator(profile, true, "", true, false, false, activityDataWrapper);
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
                if (profile._iconBitmap != null)
                    activeProfileIcon.setImageBitmap(profile._iconBitmap);
                else {
                    int res = Profile.getIconResource(profile.getIconIdentifier());
                    activeProfileIcon.setImageResource(res); // icon resource
                }
            }
            else
            {
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

        new UpdateHeaderAsyncTask(this).execute();
    }

    @SuppressLint("NotifyDataSetChanged")
    void updateListView(Event event, boolean newEvent, boolean refreshIcons, boolean setPosition/*, long loadEventId*/)
    {
        /*if (listView != null)
            listView.cancelDrag();*/

        //if (eventListAdapter != null)
        if (listView != null)
            listView.getRecycledViewPool().clear();

        if (eventListAdapter != null) {
            if ((newEvent) && (event != null))
                // add event into listview
                eventListAdapter.addItem(event);
        }

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

            boolean startTargetHelps = getArguments() != null && getArguments().getBoolean(START_TARGET_HELPS_ARGUMENT, false);
            if (startTargetHelps)
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
        if (isAsyncTaskPendingOrRunning()) {
            this.asyncTaskContext.get().cancel(true);
        }

        this.orderType = orderType;

        if (fromOnViewCreated) {
            synchronized (activityDataWrapper.eventList) {
                if (!activityDataWrapper.eventListFilled) {
                    LoadEventListAsyncTask asyncTask = new LoadEventListAsyncTask(this, filterType, orderType);
                    this.asyncTaskContext = new WeakReference<>(asyncTask);
                    asyncTask.execute();
                } else {
                    if (eventListAdapter != null) {
                        sortList(activityDataWrapper.eventList, orderType, activityDataWrapper);
                        listView.setAdapter(eventListAdapter);
                        synchronized (activityDataWrapper.profileList) {
                            Profile profile = activityDataWrapper.getActivatedProfileFromDB(true,
                                    ApplicationPreferences.applicationEditorPrefIndicator);
                            updateHeader(profile);
                        }
                        listView.getRecycledViewPool().clear();
                    }
                    else {
                        if (filterType == FILTER_TYPE_START_ORDER)
                            EditorEventListFragment.sortList(activityDataWrapper.eventList, ORDER_TYPE_START_ORDER, activityDataWrapper);
                        else
                            EditorEventListFragment.sortList(activityDataWrapper.eventList, orderType, activityDataWrapper);
                        synchronized (activityDataWrapper.profileList) {
                            Profile profile = activityDataWrapper.getActivatedProfileFromDB(true,
                                    ApplicationPreferences.applicationEditorPrefIndicator);
                            updateHeader(profile);
                        }

                        listView.getRecycledViewPool().clear();

                        eventListAdapter = new EditorEventListAdapter(this, activityDataWrapper, filterType, this);

                        // added touch helper for drag and drop items
                        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(eventListAdapter, false, false);
                        itemTouchHelper = new ItemTouchHelper(callback);
                        itemTouchHelper.attachToRecyclerView(listView);

                        listView.setAdapter(eventListAdapter);
                    }
                    eventListAdapter.notifyDataSetChanged();
                }
            }
        }
        else {
            //PPApplication.logE("[OPT] EditorEventListFragment.changeListOrder", "xxx");
            synchronized (activityDataWrapper.eventList) {
                if (filterType == FILTER_TYPE_START_ORDER)
                    EditorEventListFragment.sortList(activityDataWrapper.eventList, ORDER_TYPE_START_ORDER, activityDataWrapper);
                else
                    EditorEventListFragment.sortList(activityDataWrapper.eventList, orderType, activityDataWrapper);
                synchronized (activityDataWrapper.profileList) {
                    Profile profile = activityDataWrapper.getActivatedProfileFromDB(true,
                            ApplicationPreferences.applicationEditorPrefIndicator);
                    updateHeader(profile);
                }
                eventListAdapter = new EditorEventListAdapter(this, activityDataWrapper, filterType, this);

                // added touch helper for drag and drop items
                ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(eventListAdapter, false, false);
                itemTouchHelper = new ItemTouchHelper(callback);
                itemTouchHelper.attachToRecyclerView(listView);

                listView.setAdapter(eventListAdapter);

                int eventPos = ListView.INVALID_POSITION;
                if (scrollToEvent != null) {
                    eventPos = eventListAdapter.getItemPosition(scrollToEvent);
                    scrollToEvent = null;
                }

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

    private static void sortList(List<Event> eventList, int orderType, DataWrapper _dataWrapper)
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
                //noinspection Java8ListSort
                Collections.sort(eventList, new EventNameComparator());
                break;
            case ORDER_TYPE_START_ORDER:
                //noinspection Java8ListSort
                Collections.sort(eventList, new StartOrderComparator());
                break;
            case ORDER_TYPE_START_PROFILE_NAME:
                //noinspection Java8ListSort
                Collections.sort(eventList, new StartProfileNameComparator());
                break;
            case ORDER_TYPE_END_PROFILE_NAME:
                //noinspection Java8ListSort
                Collections.sort(eventList, new EndProfileNameComparator());
                break;
            case ORDER_TYPE_PRIORITY:
                if (ApplicationPreferences.applicationEventUsePriority)
                    //noinspection Java8ListSort
                    Collections.sort(eventList, new PriorityComparator());
                else
                    //noinspection Java8ListSort
                    Collections.sort(eventList, new StartOrderComparator());
                break;
        }
    }

    void refreshGUI(/*final boolean refresh,*/ final boolean refreshIcons, final boolean setPosition, final long eventId)
    {
        if (activityDataWrapper == null)
            return;

        //PPApplication.logE("EditorEventListFragment.refreshGUI", "refresh="+refresh);

        EditorEventListFragment.RefreshGUIAsyncTask asyncTask =
                new EditorEventListFragment.RefreshGUIAsyncTask(
                        refreshIcons, setPosition, eventId, this, activityDataWrapper);
        asyncTask.execute();

/*        new AsyncTask<Void, Integer, Void>() {

            Profile profileFromDB;
            Profile profileFromDataWrapper;
            boolean _refreshIcons;

            boolean doNotRefresh = false;

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                _refreshIcons = refreshIcons;
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    profileFromDB = DatabaseHandler.getInstance(activityDataWrapper.context).getActivatedProfile();
                    activityDataWrapper.getEventTimelineList(true);

                    if (profileFromDB != null) {
                        profileFromDataWrapper = activityDataWrapper.getProfileById(profileFromDB._id, true,
                                ApplicationPreferences.applicationEditorPrefIndicator, false);
                    }

//                    String pName;
//                    if (profileFromDB != null) {
//                        pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profileFromDB, true, "", true, false, false, activityDataWrapper);
//                    } else
//                        pName = activityDataWrapper.context.getString(R.string.profiles_header_profile_name_no_activated);
//                    //PPApplication.logE("EditorEventListFragment.refreshGUI", "pName="+pName);
//
//                    if (!refresh) {
//                        String pNameHeader = PPApplication.prefActivityProfileName3;
//                        //PPApplication.logE("EditorEventListFragment.refreshGUI", "pNameHeader="+pNameHeader);
//
//                        if ((!pNameHeader.isEmpty()) && pName.equals(pNameHeader)) {
//                            //PPApplication.logE("EditorEventListFragment.refreshGUI", "activated profile NOT changed");
//                            doNotRefresh = true;
//                            return null;
//                        }
//                    }
//
//                    PPApplication.setActivityProfileName(activityDataWrapper.context, 2, pName);
//                    PPApplication.setActivityProfileName(activityDataWrapper.context, 3, pName);

                    synchronized (activityDataWrapper.eventList) {
                        if (!activityDataWrapper.eventListFilled) {
                            doNotRefresh = true;
                            return null;
                        }

                        //noinspection ForLoopReplaceableByForEach
                        for (Iterator<Event> it = activityDataWrapper.eventList.iterator(); it.hasNext(); ) {
                            Event event = it.next();
                            int status = DatabaseHandler.getInstance(activityDataWrapper.context).getEventStatus(event);
                            event.setStatus(status);
                            event._isInDelayStart = DatabaseHandler.getInstance(activityDataWrapper.context).getEventInDelayStart(event);
                            event._isInDelayEnd = DatabaseHandler.getInstance(activityDataWrapper.context).getEventInDelayEnd(event);
                            DatabaseHandler.getInstance(activityDataWrapper.context).setEventCalendarTimes(event);
                            DatabaseHandler.getInstance(activityDataWrapper.context).getSMSStartTime(event);
                            //DatabaseHandler.getInstance(activityDataWrapper.context).getNotificationStartTime(event);
                            DatabaseHandler.getInstance(activityDataWrapper.context).getNFCStartTime(event);
                            DatabaseHandler.getInstance(activityDataWrapper.context).getCallStartTime(event);
                            DatabaseHandler.getInstance(activityDataWrapper.context).getAlarmClockStartTime(event);
                            DatabaseHandler.getInstance(activityDataWrapper.context).getDeviceBootStartTime(event);
                        }
                    }

                    if (eventId != 0) {
                        Event eventFromDB = DatabaseHandler.getInstance(activityDataWrapper.context).getEvent(eventId);
                        activityDataWrapper.updateEvent(eventFromDB);
                        _refreshIcons = true;
                    }
                } catch (Exception e) {
                    if ((activityDataWrapper != null) && (activityDataWrapper.context != null))
                        PPApplication.recordException(e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);
                if ((getActivity() != null) && (!getActivity().isFinishing())) {
                    if (!doNotRefresh) {
                        if (profileFromDB != null) {
                            //PPApplication.logE("EditorEventListFragment.refreshGUI", "profile activated");
                            if (profileFromDataWrapper != null)
                                profileFromDataWrapper._checked = true;
                            updateHeader(profileFromDataWrapper);
                        } else {
                            //PPApplication.logE("EditorEventListFragment.refreshGUI", "profile not activated");
                            updateHeader(null);
                        }
                        updateListView(null, false, _refreshIcons, setPosition);
                    }
                }
            }

        }.execute();*/

        /*Profile profileFromDB = DatabaseHandler.getInstance(activityDataWrapper.context).getActivatedProfile();
        activityDataWrapper.getEventTimelineList(true);

        String pName;
        if (profileFromDB != null)
            pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profileFromDB, true, "", true, false, false, activityDataWrapper);
        else
            pName = getResources().getString(R.string.profiles_header_profile_name_no_activated);
        //PPApplication.logE("EditorEventListFragment.refreshGUI", "pName="+pName);

        if (!refresh) {
            String pNameHeader = PPApplication.prefActivityProfileName3;
            //PPApplication.logE("EditorEventListFragment.refreshGUI", "pNameHeader="+pNameHeader);

            if ((!pNameHeader.isEmpty()) && pName.equals(pNameHeader)) {
                //PPApplication.logE("EditorEventListFragment.refreshGUI", "activated profile NOT changed");
                return;
            }
        }

        PPApplication.setActivityProfileName(activityDataWrapper.context, 2, pName);
        PPApplication.setActivityProfileName(activityDataWrapper.context, 3, pName);

        synchronized (activityDataWrapper.eventList) {
            if (!activityDataWrapper.eventListFilled)
                return;

            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = activityDataWrapper.eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                int status = DatabaseHandler.getInstance(activityDataWrapper.context).getEventStatus(event);
                event.setStatus(status);
                event._isInDelayStart = DatabaseHandler.getInstance(activityDataWrapper.context).getEventInDelayStart(event);
                event._isInDelayEnd = DatabaseHandler.getInstance(activityDataWrapper.context).getEventInDelayEnd(event);
                DatabaseHandler.getInstance(activityDataWrapper.context).setEventCalendarTimes(event);
                DatabaseHandler.getInstance(activityDataWrapper.context).getSMSStartTime(event);
                //DatabaseHandler.getInstance(activityDataWrapper.context).getNotificationStartTime(event);
                DatabaseHandler.getInstance(activityDataWrapper.context).getNFCStartTime(event);
                DatabaseHandler.getInstance(activityDataWrapper.context).getCallStartTime(event);
                DatabaseHandler.getInstance(activityDataWrapper.context).getAlarmClockStartTime(event);
                DatabaseHandler.getInstance(activityDataWrapper.context).getDeviceBootStartTime(event);
            }
        }

        if (profileFromDB != null) {
            //PPApplication.logE("EditorEventListFragment.refreshGUI", "profile activated");
            Profile profileFromDataWrapper = activityDataWrapper.getProfileById(profileFromDB._id, true,
                    ApplicationPreferences.applicationEditorPrefIndicator, false);
            if (profileFromDataWrapper != null)
                profileFromDataWrapper._checked = true;
            updateHeader(profileFromDataWrapper);
        } else {
            //PPApplication.logE("EditorEventListFragment.refreshGUI", "profile not activated");
            updateHeader(null);
        }
        updateListView(null, false, refreshIcons, setPosition, eventId);*/
    }

    void removeAdapter() {
        if (listView != null)
            listView.setAdapter(null);
    }

    void showTargetHelps() {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        if (getActivity() == null)
            return;

        if (((EditorProfilesActivity)getActivity()).targetHelpsSequenceStarted)
            return;

        boolean startTargetHelps = ApplicationPreferences.prefEditorEventsFragmentStartTargetHelps;
        boolean startTargetHelpsFilterSpinner = ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsFilterSpinner;
        boolean startTargetHelpsDefaultProfile = ApplicationPreferences.prefEditorActivityStartTargetHelpsDefaultProfile;
        boolean startTargetHelpsOrderSpinner = ApplicationPreferences. prefEditorEventsFragmentStartTargetHelpsOrderSpinner;

        if (startTargetHelps || startTargetHelpsFilterSpinner || startTargetHelpsDefaultProfile || startTargetHelpsOrderSpinner ||
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps ||
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder ||
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsStatus) {

            if (startTargetHelps || startTargetHelpsFilterSpinner || startTargetHelpsDefaultProfile || startTargetHelpsOrderSpinner) {

                //Log.d("EditorEventListFragment.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activityDataWrapper.context);
                editor.putBoolean(PREF_START_TARGET_HELPS, false);
                editor.putBoolean(PREF_START_TARGET_HELPS_FILTER_SPINNER, false);
                editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_DEFAULT_PROFILE, false);
                if (filterType != FILTER_TYPE_START_ORDER)
                    editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS_ORDER_SPINNER, false);
                editor.apply();
                ApplicationPreferences.prefEditorEventsFragmentStartTargetHelps = false;
                ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsFilterSpinner = false;
                ApplicationPreferences.prefEditorActivityStartTargetHelpsDefaultProfile = false;
                if (filterType != FILTER_TYPE_START_ORDER)
                    ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsOrderSpinner = false;

                //String appTheme = ApplicationPreferences.applicationTheme(getActivity(), true);
                int outerCircleColor = R.color.tabTargetHelpOuterCircleColor;
//                if (appTheme.equals("dark"))
//                    outerCircleColor = R.color.tabTargetHelpOuterCircleColor_dark;
                int targetCircleColor = R.color.tabTargetHelpTargetCircleColor;
//                if (appTheme.equals("dark"))
//                    targetCircleColor = R.color.tabTargetHelpTargetCircleColor_dark;
                int textColor = R.color.tabTargetHelpTextColor;
//                if (appTheme.equals("dark"))
//                    textColor = R.color.tabTargetHelpTextColor_dark;
                //boolean tintTarget = !appTheme.equals("white");

                //int[] screenLocation = new int[2];
                //orderSpinner.getLocationOnScreen(screenLocation);
                //orderSpinner.getLocationInWindow(screenLocation);
                //Rect orderSpinnerTarget = new Rect(0, 0, orderSpinner.getHeight(), orderSpinner.getHeight());
                //Log.e("+++++++++++ EditorEventListFragment.showTargetHelps", "orderSpinner.getHeight()="+orderSpinner.getHeight());
                //orderSpinnerTarget.offset(screenLocation[0] + 100, screenLocation[1]);
                //Log.e("+++++++++++ EditorEventListFragment.showTargetHelps", "orderSpinnerTarget="+orderSpinnerTarget);

                final TapTargetSequence sequence = new TapTargetSequence(getActivity());
                List<TapTarget> targets = new ArrayList<>();
                int id = 1;
                if (startTargetHelps) {
                    try {
                        targets.add(
                                TapTarget.forView(((EditorProfilesActivity)getActivity()).filterSpinner, getString(R.string.editor_activity_targetHelps_eventsFilterSpinner_title), getString(R.string.editor_activity_targetHelps_eventsFilterSpinner_description))
                                        .transparentTarget(true)
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
                                TapTarget.forToolbarOverflow(bottomToolbar, getString(R.string.editor_activity_targetHelps_eventsBottomMenu_title), getString(R.string.editor_activity_targetHelps_eventsBottomMenu_description))
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
                                TapTarget.forToolbarMenuItem(bottomToolbar, R.id.menu_add_event, getString(R.string.editor_activity_targetHelps_newEventButton_title), getString(R.string.editor_activity_targetHelps_newEventButton_description))
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
                                TapTarget.forToolbarMenuItem(bottomToolbar, R.id.menu_delete_all_events, getString(R.string.editor_activity_targetHelps_deleteAllEventsButton_title), getString(R.string.editor_activity_targetHelps_deleteAllEventsButton_description))
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
                }
                if (startTargetHelpsDefaultProfile) {
                    try {
                        targets.add(
                                TapTarget.forToolbarMenuItem(bottomToolbar, R.id.menu_default_profile, getString(R.string.editor_activity_targetHelps_backgroundProfileButton_title), getString(R.string.editor_activity_targetHelps_backgroundProfileButton_description))
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
                                            .textColor(textColor)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(1)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplication.recordException(e);
                        }
                    }
                }

                sequence.targets(targets)
                        .listener(new TapTargetSequence.Listener() {
                            // This listener will tell us when interesting(tm) events happen in regards
                            // to the sequence
                            @Override
                            public void onSequenceFinish() {
                                targetHelpsSequenceStarted = false;
                                showAdapterTargetHelps();
                            }

                            @Override
                            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                                //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                            }

                            @Override
                            public void onSequenceCanceled(TapTarget lastTarget) {
                                targetHelpsSequenceStarted = false;
                                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activityDataWrapper.context);
                                editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, false);
                                if (filterType == FILTER_TYPE_START_ORDER)
                                    editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
                                editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_STATUS, false);
                                editor.apply();
                                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps = false;
                                if (filterType != FILTER_TYPE_START_ORDER)
                                    ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder = false;
                                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsStatus = false;
                            }
                        });
                sequence.continueOnCancel(true)
                        .considerOuterCircleCanceled(true);
                targetHelpsSequenceStarted = true;
                sequence.start();
            }
            else {
                //Log.d("EditorEventListFragment.showTargetHelps", "PREF_START_TARGET_HELPS=false");
                final Handler handler = new Handler(getActivity().getMainLooper());
                handler.postDelayed(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EditorEventListFragment.showTargetHelps");
                    //noinspection Convert2MethodRef
                    showAdapterTargetHelps();
                }, 500);
            }
        }
    }

    private void showAdapterTargetHelps() {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        if (getActivity() == null)
            return;

        View itemView;
        if (listView.getChildCount() > 1)
            itemView = listView.getChildAt(1);
        else
            itemView = listView.getChildAt(0);
        if ((eventListAdapter != null) && (itemView != null))
            eventListAdapter.showTargetHelps(getActivity(), this, itemView);
        else {
            targetHelpsSequenceStarted = false;
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(getActivity().getApplicationContext());
            editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, false);
            if (filterType == FILTER_TYPE_START_ORDER)
                editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
            editor.apply();
            ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps = false;
            if (filterType == FILTER_TYPE_START_ORDER)
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder = false;
        }
    }

    private void changeEventOrder(int position, boolean fromOnViewCreated) {
        orderSelectedItem = position;

        if (filterType != EditorEventListFragment.FILTER_TYPE_START_ORDER) {
            // save into shared preferences
            if (getActivity() != null) {
                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(getActivity().getApplicationContext());
                editor.putInt(ApplicationPreferences.EDITOR_ORDER_SELECTED_ITEM, orderSelectedItem);
                editor.apply();
                ApplicationPreferences.editorOrderSelectedItem(getActivity().getApplicationContext());
            }
        }

        int _eventsOrderType = getEventsOrderType();
        //setStatusBarTitle();

        /*if (PPApplication.logEnabled()) {
            //PPApplication.logE("EditorProfilesActivity.changeEventOrder", "filterSelectedItem="+filterSelectedItem);
            PPApplication.logE("EditorProfilesActivity.changeEventOrder", "orderSelectedItem=" + orderSelectedItem);
            PPApplication.logE("EditorProfilesActivity.changeEventOrder", "_eventsOrderType=" + _eventsOrderType);
        }*/

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

    @SuppressWarnings("RedundantArrayCreation")
    void showIgnoreManualActivationMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        Context _context = view.getContext();
        //Context context = new ContextThemeWrapper(getActivity().getBaseContext(), R.style.PopupMenu_editorItem_dayNight);
        PopupMenu popup;
        //if (android.os.Build.VERSION.SDK_INT >= 19)
        popup = new PopupMenu(_context, view, Gravity.END);
        //else
        //    popup = new PopupMenu(context, view);
        //noinspection ConstantConditions
        getActivity().getMenuInflater().inflate(R.menu.event_list_item_ignore_manual_activation, popup.getMenu());
        /*Menu menu = popup.getMenu();
        MenuItem menuItem = menu.findItem(R.id.event_list_item_ignore_manual_activation_title);
        menuItem.setTitle("[»] " + context.getString(R.string.event_preferences_ForceRun));*/

        // show icons
        try {
            @SuppressLint("DiscouragedPrivateApi")
            Field field = popup.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            Object menuPopupHelper = field.get(popup);
            @SuppressLint("PrivateApi")
            Class<?> cls = Class.forName("com.android.internal.view.menu.MenuPopupHelper");
            Method method = cls.getDeclaredMethod("setForceShowIcon", new Class[]{boolean.class});
            method.setAccessible(true);
            method.invoke(menuPopupHelper, new Object[]{true});
        } catch (Exception e) {
            //PPApplication.recordException(e);
        }

        final Event event = (Event)view.getTag();

        Menu menu = popup.getMenu();
        Drawable drawable;
        if (event._ignoreManualActivation && event._noPauseByManualActivation)
            drawable = menu.findItem(R.id.event_list_item_ignore_manual_activation_no_pause).getIcon();
        else
        if (event._ignoreManualActivation)
            drawable = menu.findItem(R.id.event_list_item_ignore_manual_activation).getIcon();
        else
            drawable = menu.findItem(R.id.event_list_item_not_ignore_manual_activation).getIcon();
        if(drawable != null) {
            drawable.mutate();
            drawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.accent), PorterDuff.Mode.SRC_ATOP);
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
                    //eventListAdapter.notifyDataSetChanged();
                    EventsPrefsActivity.saveUpdateOfPreferences(event, activityDataWrapper, event.getStatus());
                    ((EditorProfilesActivity) getActivity()).redrawEventListFragment(event, EDIT_MODE_EDIT);

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
                    //eventListAdapter.notifyDataSetChanged();
                    EventsPrefsActivity.saveUpdateOfPreferences(event, activityDataWrapper, event.getStatus());
                    ((EditorProfilesActivity) getActivity()).redrawEventListFragment(event, EDIT_MODE_EDIT);

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
                    //eventListAdapter.notifyDataSetChanged();
                    EventsPrefsActivity.saveUpdateOfPreferences(event, activityDataWrapper, event.getStatus());
                    ((EditorProfilesActivity) getActivity()).redrawEventListFragment(event, EDIT_MODE_EDIT);

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
    }

    /*
    void updateEventForceRun(final Event event) {
        //noinspection ConstantConditions
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(getString(R.string.event_string_0) + ": " + event._name);
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        int noPause = event._forceRun ? 1 : 0;
        dialogBuilder.setSingleChoiceItems(R.array.ignoreManualActivationArray, noPause, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                event._forceRun = which == 1;
                DatabaseHandler.getInstance(activityDataWrapper.context).updateEventForceRun(event);
                eventListAdapter.notifyDataSetChanged();

                EventsPrefsActivity.saveUpdateOfPreferences(event, activityDataWrapper, event.getStatus());

                dialog.dismiss();
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
        if (!getActivity().isFinishing())
            dialog.show();
    }
    */

    void showHeaderAndBottomToolbar() {
        if (activatedProfileHeader != null)
            activatedProfileHeader.setVisibility(VISIBLE);
        if (bottomToolbar != null)
            bottomToolbar.setVisibility(VISIBLE);
    }

    private static class RefreshGUIAsyncTask extends AsyncTask<Void, Integer, Void> {

        Profile profileFromDB;
        Profile profileFromDataWrapper;

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
                    profileFromDB = DatabaseHandler.getInstance(dataWrapper.context).getActivatedProfile();
                    dataWrapper.getEventTimelineList(true);

                    if (profileFromDB != null) {
                        profileFromDataWrapper = dataWrapper.getProfileById(profileFromDB._id, true,
                                ApplicationPreferences.applicationEditorPrefIndicator, false);
                    }

                    /*
                    String pName;
                    if (profileFromDB != null) {
                        pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profileFromDB, true, "", true, false, false, activityDataWrapper);
                    } else
                        pName = activityDataWrapper.context.getString(R.string.profiles_header_profile_name_no_activated);
                    //PPApplication.logE("EditorEventListFragment.refreshGUI", "pName="+pName);

                    if (!refresh) {
                        String pNameHeader = PPApplication.prefActivityProfileName3;
                        //PPApplication.logE("EditorEventListFragment.refreshGUI", "pNameHeader="+pNameHeader);

                        if ((!pNameHeader.isEmpty()) && pName.equals(pNameHeader)) {
                            //PPApplication.logE("EditorEventListFragment.refreshGUI", "activated profile NOT changed");
                            doNotRefresh = true;
                            return null;
                        }
                    }

                    PPApplication.setActivityProfileName(activityDataWrapper.context, 2, pName);
                    PPApplication.setActivityProfileName(activityDataWrapper.context, 3, pName);
                    */

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
                            DatabaseHandler.getInstance(dataWrapper.context).setEventCalendarTimes(event);
                            DatabaseHandler.getInstance(dataWrapper.context).getSMSStartTime(event);
                            //DatabaseHandler.getInstance(activityDataWrapper.context).getNotificationStartTime(event);
                            DatabaseHandler.getInstance(dataWrapper.context).getNFCStartTime(event);
                            DatabaseHandler.getInstance(dataWrapper.context).getCallStartTime(event);
                            DatabaseHandler.getInstance(dataWrapper.context).getAlarmClockStartTime(event);
                            DatabaseHandler.getInstance(dataWrapper.context).getDeviceBootStartTime(event);
                            DatabaseHandler.getInstance(dataWrapper.context).getPeriodicCounter(event);
                            DatabaseHandler.getInstance(dataWrapper.context).getPeriodicStartTime(event);
                        }
                    }

                    if (eventId != 0) {
                        Event eventFromDB = DatabaseHandler.getInstance(dataWrapper.context).getEvent(eventId);
                        dataWrapper.updateEvent(eventFromDB);
                        refreshIcons = true;
                    }
                } catch (Exception e) {
                    if ((dataWrapper != null) && (dataWrapper.context != null))
                        PPApplication.recordException(e);
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
                        if (profileFromDB != null) {
                            //PPApplication.logE("EditorEventListFragment.refreshGUI", "profile activated");
                            if (profileFromDataWrapper != null)
                                profileFromDataWrapper._checked = true;
                            fragment.updateHeader(profileFromDataWrapper);
                        } else {
                            //PPApplication.logE("EditorEventListFragment.refreshGUI", "profile not activated");
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
                    _dataWrapper = new DataWrapper(fragment.getActivity().getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0f);
                    _dataWrapper.copyEventList(fragment.activityDataWrapper);

                    for (Event event : _dataWrapper.eventList) {
                        if (EventsPrefsFragment.isRedTextNotificationRequired(event, _dataWrapper.context))
                            redTextVisible = true;
                    }
                }
            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            EditorEventListFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if ((fragment.getActivity() != null) && (!fragment.getActivity().isFinishing())) {
                    try {
                        //if (activatedProfileHeader.isVisibleToUser()) {
                        TextView redText = fragment.activatedProfileHeader.findViewById(R.id.activated_profile_red_text);
                        if (redTextVisible)
                            redText.setVisibility(View.VISIBLE);
                        else
                            redText.setVisibility(GONE);
                        //}
                    } catch (Exception e) {
                        PPApplication.recordException(e);
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

}
