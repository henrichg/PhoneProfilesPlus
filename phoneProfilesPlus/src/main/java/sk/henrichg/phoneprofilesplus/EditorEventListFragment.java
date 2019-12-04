package sk.henrichg.phoneprofilesplus;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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
    private static final int EDIT_MODE_EDIT = 3;
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
    private static final int ORDER_TYPE_PROFILE_NAME = 2;
    private static final int ORDER_TYPE_PRIORITY = 3;

    public boolean targetHelpsSequenceStarted;
    public static final String PREF_START_TARGET_HELPS = "editor_event_list_fragment_start_target_helps";
    public static final String PREF_START_TARGET_HELPS_ORDER_SPINNER = "editor_profile_activity_start_target_helps_order_spinner";

    private int filterType = FILTER_TYPE_ALL;
    private int orderType = ORDER_TYPE_EVENT_NAME;

    private static final String SP_EDITOR_ORDER_SELECTED_ITEM = "editor_order_selected_item";

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

    /**
     * A dummy implementation of the Callbacks interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static final OnStartEventPreferences sDummyOnStartEventPreferencesCallback = new OnStartEventPreferences() {
        public void onStartEventPreferences(Event event, int editMode, int predefinedEventIndex/*, boolean startTargetHelps*/) {
        }
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
        activityDataWrapper = new DataWrapper(getActivity().getApplicationContext(), false, 0, false);

        getActivity().getIntent();

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

    @SuppressWarnings("ConstantConditions")
    private void doOnViewCreated(View view, boolean fromOnViewCreated)
    {
        profilePrefIndicatorImageView = view.findViewById(R.id.activated_profile_pref_indicator);
        if (!ApplicationPreferences.applicationEditorPrefIndicator(activityDataWrapper.context))
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

        textViewNoData = view.findViewById(R.id.editor_events_list_empty);
        progressBar = view.findViewById(R.id.editor_events_list_linla_progress);

        /*
        View footerView =  ((LayoutInflater)getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.editor_list_footer, null, false);
        listView.addFooterView(footerView, null, false);
        */

        final EditorEventListFragment fragment = this;

        Menu menu = bottomToolbar.getMenu();
        if (menu != null) menu.clear();
        bottomToolbar.inflateMenu(R.menu.editor_events_bottom_bar);
        bottomToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_add_event:
                        if (eventListAdapter != null) {
                            if (!getActivity().isFinishing()) {
                                ((EditorProfilesActivity) getActivity()).addEventDialog = new AddEventDialog(getActivity(), fragment);
                                ((EditorProfilesActivity) getActivity()).addEventDialog.show();
                            }
                        }
                        return true;
                    case R.id.menu_delete_all_events:
                        deleteAllEvents();
                        return true;
                    case R.id.menu_default_profile:
                        Intent intent = new Intent(getActivity(), PhoneProfilesPrefsActivity.class);
                        intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "profileActivationCategoryRoot");
                        startActivity(intent);
                        return true;
                }
                return false;
            }
        });

        /*
        LinearLayout orderLayout = view.findViewById(R.id.editor_list_bottom_bar_order_root);
        if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER)
            orderLayout.setVisibility(View.GONE);
        else
            orderLayout.setVisibility(VISIBLE);
         */

        ApplicationPreferences.getSharedPreferences(getActivity());
        orderSelectedItem = ApplicationPreferences.preferences.getInt(SP_EDITOR_ORDER_SELECTED_ITEM, 0);

        orderSpinner = view.findViewById(R.id.editor_list_bottom_bar_order);
        if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER)
            orderSpinner.setVisibility(View.INVISIBLE); // MUST BE INVISIBLE, required for shoTargetHelps().
        else
            orderSpinner.setVisibility(VISIBLE);

        String[] orderItems = new String[] {
                getString(R.string.editor_drawer_title_events_order) + ": " + getString(R.string.editor_drawer_order_start_order),
                getString(R.string.editor_drawer_title_events_order) + ": " + getString(R.string.editor_drawer_order_event_name),
                getString(R.string.editor_drawer_title_events_order) + ": " + getString(R.string.editor_drawer_order_profile_name),
                getString(R.string.editor_drawer_title_events_order) + ": " + getString(R.string.editor_drawer_order_priority)
        };

        GlobalGUIRoutines.HighlightedSpinnerAdapter orderSpinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                getActivity(),
                R.layout.highlighted_order_spinner,
                orderItems);
        orderSpinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
        orderSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
        orderSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(getActivity()/*.getBaseContext()*/, R.color.highlighted_spinner_all));
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
        /*TextView orderLabel = view.findViewById(R.id.editor_list_bottom_bar_order_title);
        orderLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderSpinner.performClick();
            }
        });*/

        PPApplication.logE("EditorEventListFragment.doOnViewCreated", "orderSelectedItem="+orderSelectedItem);
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

        final boolean applicationEditorPrefIndicator;

        Handler progressBarHandler;
        Runnable progressBarRunnable;

        private LoadEventListAsyncTask (EditorEventListFragment fragment, int filterType, int orderType) {
            fragmentWeakRef = new WeakReference<>(fragment);
            _filterType = filterType;
            _orderType = orderType;
            //noinspection ConstantConditions
            _dataWrapper = new DataWrapper(fragment.getActivity().getApplicationContext(), false, 0, false);

            applicationEditorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator(_dataWrapper.context);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            final EditorEventListFragment fragment = this.fragmentWeakRef.get();

            if ((fragment != null) && (fragment.isAdded())) {
                progressBarHandler = new Handler(_dataWrapper.context.getMainLooper());
                progressBarRunnable = new Runnable() {
                    @Override
                    public void run() {
                        //fragment.textViewNoData.setVisibility(GONE);
                        fragment.progressBar.setVisibility(VISIBLE);
                    }
                };
                progressBarHandler.postDelayed(progressBarRunnable, 100);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            _dataWrapper.fillProfileList(true, applicationEditorPrefIndicator);
            _dataWrapper.fillEventList();
            _dataWrapper.getEventTimelineList(true);
            //Log.d("EditorEventListFragment.LoadEventListAsyncTask","filterType="+filterType);
            if (_filterType == FILTER_TYPE_START_ORDER)
                EditorEventListFragment.sortList(_dataWrapper.eventList, ORDER_TYPE_START_ORDER, _dataWrapper);
            else
                EditorEventListFragment.sortList(_dataWrapper.eventList, _orderType, _dataWrapper);

            return null;
        }

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

                if (fragment.activityDataWrapper.eventList.size() == 0)
                    fragment.textViewNoData.setVisibility(VISIBLE);

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

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) one must start profile preferences
        onStartEventPreferencesCallback.onStartEventPreferences(event, editMode, predefinedEventIndex);
    }

    void runStopEvent(final Event event)
    {
        if (Event.getGlobalEventsRunning(activityDataWrapper.context)) {
            // events are not globally stopped

            List<EventTimeline> eventTimelineList = activityDataWrapper.getEventTimelineList(true);
            if (event.getStatusFromDB(activityDataWrapper.context) == Event.ESTATUS_STOP) {
                // pause event
                IgnoreBatteryOptimizationNotification.showNotification(activityDataWrapper.context.getApplicationContext());
                // not needed to use handlerThread, profile is not activated (activateReturnProfile=false)
                event.pauseEvent(activityDataWrapper, eventTimelineList, false, false,
                        false, /*false,*/ null, false, false);
            } else {
                // stop event
                // not needed to use handlerThread, profile is not activated (activateReturnProfile=false)
                event.stopEvent(activityDataWrapper, eventTimelineList, false, false,
                        true/*, false*/); // activate return profile
            }

            // redraw event list
            updateListView(event, false, false, true, 0);

            // restart events
            PPApplication.logE("$$$ restartEvents","from EditorEventListFragment.runStopEvent");
            //activityDataWrapper.restartEvents(false, true, true, true, true);
            activityDataWrapper.restartEventsWithRescan(/*true, */false, true, true, false);

            /*Intent serviceIntent = new Intent(activityDataWrapper.context, PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
            PPApplication.startPPService(activityDataWrapper.context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
            PPApplication.runCommand(activityDataWrapper.context, commandIntent);
        }
        else
        {
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
            updateListView(event, false, false, true, 0);
        }
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

    private void deleteEvent(final Event event)
    {
        if (activityDataWrapper.getEventById(event._id) == null)
            // event not exists
            return;

        activityDataWrapper.addActivityLog(DataWrapper.ALTYPE_EVENT_DELETED, event._name, null, null, 0);

        listView.getRecycledViewPool().clear();

        eventListAdapter.deleteItemNoNotify(event);
        DatabaseHandler.getInstance(activityDataWrapper.context).deleteEvent(event);

        // restart events
        PPApplication.logE("$$$ restartEvents", "from EditorEventListFragment.deleteEvent");
        //activityDataWrapper.restartEvents(false, true, true, true, true);
        activityDataWrapper.restartEventsWithRescan(/*true, */false, true, true, false);

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
        Context context = view.getContext();
        PopupMenu popup;
        //if (android.os.Build.VERSION.SDK_INT >= 19)
            popup = new PopupMenu(context, view, Gravity.END);
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

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                case R.id.event_list_item_menu_run_stop:
                    runStopEvent(event);
                    return true;
                case R.id.event_list_item_menu_duplicate:
                    duplicateEvent(event);
                    return true;
                case R.id.event_list_item_menu_delete:
                    deleteEventWithAlert(event);
                    return true;
                default:
                    return false;
                }
            }
            });


        popup.show();
    }

    private void deleteEventWithAlert(Event event)
    {
        final Event _event = event;
        //noinspection ConstantConditions
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(getResources().getString(R.string.event_string_0) + ": " + event._name);
        dialogBuilder.setMessage(getResources().getString(R.string.delete_event_alert_message));
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                deleteEvent(_event);
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
        if (!getActivity().isFinishing())
            dialog.show();
    }

    private void deleteAllEvents()
    {
        if (eventListAdapter != null) {
            //noinspection ConstantConditions
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            dialogBuilder.setTitle(getResources().getString(R.string.alert_title_delete_all_events));
            dialogBuilder.setMessage(getResources().getString(R.string.alert_message_delete_all_events));
            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    activityDataWrapper.addActivityLog(DataWrapper.ALTYPE_ALL_EVENTS_DELETED, null, null, null, 0);

                    listView.getRecycledViewPool().clear();

                    activityDataWrapper.stopAllEventsFromMainThread(true, true);

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
            if (!getActivity().isFinishing())
                dialog.show();
        }
    }

    void updateHeader(Profile profile)
    {
        //if (!ApplicationPreferences.applicationEditorHeader(activityDataWrapper.context))
        //    return;

        //Log.e("***** EditorEventListFragment.updateHeader", "xxx");

        if ((activeProfileName == null) || (activeProfileIcon == null))
            return;

        //Log.e("***** EditorEventListFragment.updateHeader", "profile="+profile);

        String oldDisplayedText = (String)activatedProfileHeader.getTag();

        if (profile == null)
        {
            activatedProfileHeader.setTag(getString(R.string.profiles_header_profile_name_no_activated));

            activeProfileName.setText(getResources().getString(R.string.profiles_header_profile_name_no_activated));
            activeProfileIcon.setImageResource(R.drawable.ic_profile_default);
        }
        else
        {
            activatedProfileHeader.setTag(DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, activityDataWrapper, true, activityDataWrapper.context));

            activeProfileName.setText(DataWrapper.getProfileNameWithManualIndicator(profile, true, "", true, false, activityDataWrapper, true, activityDataWrapper.context));
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

        if (ApplicationPreferences.applicationEditorPrefIndicator(activityDataWrapper.context))
        {
            if (profile == null)
                profilePrefIndicatorImageView.setImageResource(R.drawable.ic_empty);
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
    }

    void updateListView(Event event, boolean newEvent, boolean refreshIcons, boolean setPosition, long loadEventId)
    {
        /*if (listView != null)
            listView.cancelDrag();*/

        if (eventListAdapter != null)
            listView.getRecycledViewPool().clear();

        if (eventListAdapter != null) {
            if ((newEvent) && (event != null))
                // add event into listview
                eventListAdapter.addItem(event);
        }

        synchronized (activityDataWrapper.eventList) {
            if (activityDataWrapper.eventList != null) {
                // sort list
                sortList(activityDataWrapper.eventList, orderType, activityDataWrapper);
            }
        }

        if (eventListAdapter != null) {
            int eventPos = ListView.INVALID_POSITION;

            if (event != null)
                eventPos = eventListAdapter.getItemPosition(event);
            //else
            //    eventPos = listView.getCheckedItemPosition();

            if (loadEventId != 0) {
                if (getActivity() != null) {
                    Event eventFromDB = DatabaseHandler.getInstance(getActivity().getApplicationContext()).getEvent(loadEventId);
                    activityDataWrapper.updateEvent(eventFromDB);
                    refreshIcons = true;
                }
            }
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
                                    ApplicationPreferences.applicationEditorPrefIndicator(activityDataWrapper.context));
                            updateHeader(profile);
                        }
                        listView.getRecycledViewPool().clear();
                        eventListAdapter.notifyDataSetChanged();
                    }
                    else {
                        if (filterType == FILTER_TYPE_START_ORDER)
                            EditorEventListFragment.sortList(activityDataWrapper.eventList, ORDER_TYPE_START_ORDER, activityDataWrapper);
                        else
                            EditorEventListFragment.sortList(activityDataWrapper.eventList, orderType, activityDataWrapper);
                        synchronized (activityDataWrapper.profileList) {
                            Profile profile = activityDataWrapper.getActivatedProfileFromDB(true,
                                    ApplicationPreferences.applicationEditorPrefIndicator(activityDataWrapper.context));
                            updateHeader(profile);
                        }

                        listView.getRecycledViewPool().clear();

                        eventListAdapter = new EditorEventListAdapter(this, activityDataWrapper, filterType, this);

                        // added touch helper for drag and drop items
                        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(eventListAdapter, false, false);
                        itemTouchHelper = new ItemTouchHelper(callback);
                        itemTouchHelper.attachToRecyclerView(listView);

                        listView.setAdapter(eventListAdapter);

                        eventListAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
        else {
            PPApplication.logE("[OPT] EditorEventListFragment.changeListOrder", "xxx");
            synchronized (activityDataWrapper.eventList) {
                if (filterType == FILTER_TYPE_START_ORDER)
                    EditorEventListFragment.sortList(activityDataWrapper.eventList, ORDER_TYPE_START_ORDER, activityDataWrapper);
                else
                    EditorEventListFragment.sortList(activityDataWrapper.eventList, orderType, activityDataWrapper);
                synchronized (activityDataWrapper.profileList) {
                    Profile profile = activityDataWrapper.getActivatedProfileFromDB(true,
                            ApplicationPreferences.applicationEditorPrefIndicator(activityDataWrapper.context));
                    updateHeader(profile);
                }
                eventListAdapter = new EditorEventListAdapter(this, activityDataWrapper, filterType, this);

                // added touch helper for drag and drop items
                ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(eventListAdapter, false, false);
                itemTouchHelper = new ItemTouchHelper(callback);
                itemTouchHelper.attachToRecyclerView(listView);

                listView.setAdapter(eventListAdapter);

                eventListAdapter.notifyDataSetChanged();
            }
        }
    }

    private static void sortList(List<Event> eventList, int orderType, DataWrapper _dataWrapper)
    {
        final DataWrapper dataWrapper = _dataWrapper;

        class EventNameComparator implements Comparator<Event> {
            public int compare(Event lhs, Event rhs) {
                if (GlobalGUIRoutines.collator != null)
                    return GlobalGUIRoutines.collator.compare(lhs._name, rhs._name);
                else
                    return 0;
            }
        }

        class StartOrderComparator implements Comparator<Event> {
            public int compare(Event lhs, Event rhs) {
                return lhs._startOrder - rhs._startOrder;
            }
        }

        class ProfileNameComparator implements Comparator<Event> {
            public int compare(Event lhs, Event rhs) {
                if (GlobalGUIRoutines.collator != null) {
                    Profile profileLhs = dataWrapper.getProfileById(lhs._fkProfileStart, false, false,false);
                    Profile profileRhs = dataWrapper.getProfileById(rhs._fkProfileStart, false, false, false);
                    String nameLhs = "";
                    if (profileLhs != null) nameLhs = profileLhs._name;
                    String nameRhs = "";
                    if (profileRhs != null) nameRhs = profileRhs._name;
                    return GlobalGUIRoutines.collator.compare(nameLhs, nameRhs);
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
                Collections.sort(eventList, new EventNameComparator());
                break;
            case ORDER_TYPE_START_ORDER:
                Collections.sort(eventList, new StartOrderComparator());
                break;
            case ORDER_TYPE_PROFILE_NAME:
                Collections.sort(eventList, new ProfileNameComparator());
                break;
            case ORDER_TYPE_PRIORITY:
                if (ApplicationPreferences.applicationEventUsePriority(_dataWrapper.context))
                    Collections.sort(eventList, new PriorityComparator());
                else
                    Collections.sort(eventList, new StartOrderComparator());
                break;
        }
    }

    void refreshGUI(boolean refresh, boolean refreshIcons, boolean setPosition, long eventId)
    {
        if (activityDataWrapper == null)
            return;

        PPApplication.logE("EditorEventListFragment.refreshGUI", "refresh="+refresh);

        Profile profileFromDB = DatabaseHandler.getInstance(activityDataWrapper.context).getActivatedProfile();
        activityDataWrapper.getEventTimelineList(true);

        String pName;
        if (profileFromDB != null)
            pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profileFromDB, true, "", true, false, activityDataWrapper, true, activityDataWrapper.context);
        else
            pName = getResources().getString(R.string.profiles_header_profile_name_no_activated);
        PPApplication.logE("EditorEventListFragment.refreshGUI", "pName="+pName);

        if (!refresh) {
            String pNameHeader = PPApplication.getActivityProfileName(activityDataWrapper.context, 3);
            PPApplication.logE("EditorEventListFragment.refreshGUI", "pNameHeader="+pNameHeader);

            if ((!pNameHeader.isEmpty()) && pName.equals(pNameHeader)) {
                PPApplication.logE("EditorEventListFragment.refreshGUI", "activated profile NOT changed");
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
            }
        }

        if (profileFromDB != null) {
            PPApplication.logE("EditorEventListFragment.refreshGUI", "profile activated");
            Profile profileFromDataWrapper = activityDataWrapper.getProfileById(profileFromDB._id, true,
                    ApplicationPreferences.applicationEditorPrefIndicator(activityDataWrapper.context), false);
            if (profileFromDataWrapper != null)
                profileFromDataWrapper._checked = true;
            updateHeader(profileFromDataWrapper);
        } else {
            PPApplication.logE("EditorEventListFragment.refreshGUI", "profile not activated");
            updateHeader(null);
        }
        updateListView(null, false, refreshIcons, setPosition, eventId);
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

        ApplicationPreferences.getSharedPreferences(getActivity());

        boolean showTargetHelps = ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true);
        boolean showTargetHelpsDefaultProfile = ApplicationPreferences.preferences.getBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_DEFAULT_PROFILE, true);
        boolean showTargetHelpsOrderSpinner = ApplicationPreferences.preferences.getBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS_ORDER_SPINNER, true);
        if (showTargetHelps || showTargetHelpsDefaultProfile || showTargetHelpsOrderSpinner ||
                ApplicationPreferences.preferences.getBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, true) ||
                ApplicationPreferences.preferences.getBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, true) ||
                ApplicationPreferences.preferences.getBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_STATUS, true)) {

            //Log.d("EditorEventListFragment.showTargetHelps", "PREF_START_TARGET_HELPS_ORDER=true");

            if (showTargetHelps || showTargetHelpsDefaultProfile || showTargetHelpsOrderSpinner) {

                //Log.d("EditorEventListFragment.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                editor.putBoolean(PREF_START_TARGET_HELPS, false);
                editor.putBoolean(EditorProfilesActivity.PREF_START_TARGET_HELPS_DEFAULT_PROFILE, false);
                if (filterType != FILTER_TYPE_START_ORDER)
                    editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS_ORDER_SPINNER, false);
                editor.apply();

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
                if (showTargetHelps) {
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
                    } catch (Exception ignored) {
                    } // not in action bar?
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
                    } catch (Exception ignored) {
                    } // not in action bar?
                }
                if (showTargetHelpsDefaultProfile) {
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
                    } catch (Exception ignored) {
                    } // not in action bar?
                }
                if (showTargetHelpsOrderSpinner) {
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
                        } catch (Exception ignored) {
                        } // not in action bar?
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
                                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                                editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, false);
                                if (filterType == FILTER_TYPE_START_ORDER)
                                    editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
                                editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_STATUS, false);
                                editor.apply();
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
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showAdapterTargetHelps();
                    }
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
            ApplicationPreferences.getSharedPreferences(getActivity());
            SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
            editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, false);
            if (filterType == FILTER_TYPE_START_ORDER)
                editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
            editor.apply();
        }
    }

    private void changeEventOrder(int position, boolean fromOnViewCreated) {
        orderSelectedItem = position;

        if (filterType != EditorEventListFragment.FILTER_TYPE_START_ORDER) {
            // save into shared preferences
            ApplicationPreferences.getSharedPreferences(getActivity());
            SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
            editor.putInt(SP_EDITOR_ORDER_SELECTED_ITEM, orderSelectedItem);
            editor.apply();
        }

        int _eventsOrderType = getEventsOrderType();
        //setStatusBarTitle();

        //PPApplication.logE("EditorProfilesActivity.changeEventOrder", "filterSelectedItem="+filterSelectedItem);
        PPApplication.logE("EditorProfilesActivity.changeEventOrder", "orderSelectedItem="+orderSelectedItem);
        PPApplication.logE("EditorProfilesActivity.changeEventOrder", "_eventsOrderType="+_eventsOrderType);

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
                    _eventsOrderType = EditorEventListFragment.ORDER_TYPE_PROFILE_NAME;
                    break;
                case 3:
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
        Context context = view.getContext();
        PopupMenu popup;
        //if (android.os.Build.VERSION.SDK_INT >= 19)
        popup = new PopupMenu(context, view, Gravity.END);
        //else
        //    popup = new PopupMenu(context, view);
        //noinspection ConstantConditions
        getActivity().getMenuInflater().inflate(R.menu.event_list_item_ignore_manual_activation, popup.getMenu());

        // show icons
        try {
            Field field = popup.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            Object menuPopupHelper = field.get(popup);
            @SuppressLint("PrivateApi")
            Class<?> cls = Class.forName("com.android.internal.view.menu.MenuPopupHelper");
            Method method = cls.getDeclaredMethod("setForceShowIcon", new Class[]{boolean.class});
            method.setAccessible(true);
            method.invoke(menuPopupHelper, new Object[]{true});
        } catch (Exception e) {
            e.printStackTrace();
        }

        final Event event = (Event)view.getTag();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.event_list_item_not_ignore_manual_activation:
                        event._forceRun = false;
                        DatabaseHandler.getInstance(activityDataWrapper.context).updateEventForceRun(event);
                        eventListAdapter.notifyDataSetChanged();
                        EventsPrefsActivity.saveUpdateOfPreferences(event, activityDataWrapper, event.getStatus());
                        return true;
                    case R.id.event_list_item_ignore_manual_activation:
                        event._forceRun = true;
                        DatabaseHandler.getInstance(activityDataWrapper.context).updateEventForceRun(event);
                        eventListAdapter.notifyDataSetChanged();
                        EventsPrefsActivity.saveUpdateOfPreferences(event, activityDataWrapper, event.getStatus());
                        return true;
                    default:
                        return false;
                }
            }
        });


        popup.show();
    }

    /*
    void updateEventForceRun(final Event event) {
        //noinspection ConstantConditions
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(getResources().getString(R.string.event_string_0) + ": " + event._name);
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
        if (!getActivity().isFinishing())
            dialog.show();
    }
    */

}
