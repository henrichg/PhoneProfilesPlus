package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.Collections;
import java.util.Iterator;

class EditorEventListAdapter extends RecyclerView.Adapter<EditorEventListViewHolder>
                                 implements ItemTouchHelperAdapter
{

    private EditorEventListFragment fragment;
    private DataWrapper activityDataWrapper;
    private final int filterType;
    //boolean released = false;
    //private int defaultColor;

    private final OnStartDragItemListener mDragStartListener;

    //private boolean targetHelpsSequenceStarted;

    EditorEventListAdapter(EditorEventListFragment f, DataWrapper pdw, int filterType,
                           OnStartDragItemListener dragStartListener)
    {
        fragment = f;
        activityDataWrapper = pdw;
        this.filterType = filterType;
        this.mDragStartListener = dragStartListener;
    }

    @NonNull
    @Override
    public EditorEventListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER) {
            if (ApplicationPreferences.applicationEditorPrefIndicator)
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_editor_event_with_order, parent, false);
            else
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_editor_event_no_indicator_with_order, parent, false);
        }
        else {
            if (ApplicationPreferences.applicationEditorPrefIndicator)
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_editor_event, parent, false);
            else
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_editor_event_no_indicator, parent, false);
        }

        return new EditorEventListViewHolder(view, fragment, fragment.getActivity(), filterType);
    }

    @Override
    public void onBindViewHolder(@NonNull final EditorEventListViewHolder holder, int position) {
        Event event = getItem(position);
        holder.bindEvent(event);

        if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER) {
            if (holder.dragHandle != null) {
                holder.dragHandle.setOnTouchListener((v, event1) -> {
                    switch (event1.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mDragStartListener.onStartDrag(holder);
                            break;
                        case MotionEvent.ACTION_UP:
                            v.performClick();
                            break;
                        default:
                            break;
                    }
                /*if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }*/
                    return false;
                });
            }
        }
    }

    public void release()
    {
        //released = true;

        fragment = null;
        activityDataWrapper = null;
    }

    @Override
    public int getItemCount() {
        synchronized (activityDataWrapper.eventList) {
            fragment.textViewNoData.setVisibility(
                    ((activityDataWrapper.eventListFilled) &&
                     (activityDataWrapper.eventList.size() > 0))
                    ? View.GONE : View.VISIBLE);

            if (!activityDataWrapper.eventListFilled)
                return 0;

            if ((filterType == EditorEventListFragment.FILTER_TYPE_ALL) ||
                    (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER))
                return activityDataWrapper.eventList.size();

            int count = 0;
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = activityDataWrapper.eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                switch (filterType) {
                    case EditorEventListFragment.FILTER_TYPE_NOT_STOPPED:
                        if (event.getStatus() != Event.ESTATUS_STOP)
                            ++count;
                        break;
                    case EditorEventListFragment.FILTER_TYPE_RUNNING:
                        if (event.getStatus() == Event.ESTATUS_RUNNING)
                            ++count;
                        break;
                    case EditorEventListFragment.FILTER_TYPE_PAUSED:
                        if (event.getStatus() == Event.ESTATUS_PAUSE)
                            ++count;
                        break;
                    case EditorEventListFragment.FILTER_TYPE_STOPPED:
                        if (event.getStatus() == Event.ESTATUS_STOP)
                            ++count;
                        break;
                }
            }
            return count;
        }
    }

    private Event getItem(int position)
    {
        if (getItemCount() == 0)
            return null;
        else
        {
            synchronized (activityDataWrapper.eventList) {
                if ((filterType == EditorEventListFragment.FILTER_TYPE_ALL) ||
                        (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER))
                    return activityDataWrapper.eventList.get(position);

                Event _event = null;

                int pos = -1;
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Event> it = activityDataWrapper.eventList.iterator(); it.hasNext(); ) {
                    Event event = it.next();
                    switch (filterType) {
                        case EditorEventListFragment.FILTER_TYPE_NOT_STOPPED:
                            if (event.getStatus() != Event.ESTATUS_STOP)
                                ++pos;
                            break;
                        case EditorEventListFragment.FILTER_TYPE_RUNNING:
                            if (event.getStatus() == Event.ESTATUS_RUNNING)
                                ++pos;
                            break;
                        case EditorEventListFragment.FILTER_TYPE_PAUSED:
                            if (event.getStatus() == Event.ESTATUS_PAUSE)
                                ++pos;
                            break;
                        case EditorEventListFragment.FILTER_TYPE_STOPPED:
                            if (event.getStatus() == Event.ESTATUS_STOP)
                                ++pos;
                            break;
                    }
                    if (pos == position) {
                        _event = event;
                        break;
                    }
                }

                return _event;
            }
        }
    }

    int getItemPosition(Event event)
    {
        synchronized (activityDataWrapper.eventList) {
            if (!activityDataWrapper.eventListFilled)
                return -1;

            if (event == null)
                return -1;

            int pos = -1;

            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = activityDataWrapper.eventList.iterator(); it.hasNext(); ) {
                Event _event = it.next();
                switch (filterType) {
                    case EditorEventListFragment.FILTER_TYPE_ALL:
                    case EditorEventListFragment.FILTER_TYPE_START_ORDER:
                        ++pos;
                        break;
                    case EditorEventListFragment.FILTER_TYPE_NOT_STOPPED:
                        if (event.getStatus() != Event.ESTATUS_STOP)
                            ++pos;
                        break;
                    case EditorEventListFragment.FILTER_TYPE_RUNNING:
                        if (event.getStatus() == Event.ESTATUS_RUNNING)
                            ++pos;
                        break;
                    case EditorEventListFragment.FILTER_TYPE_PAUSED:
                        if (event.getStatus() == Event.ESTATUS_PAUSE)
                            ++pos;
                        break;
                    case EditorEventListFragment.FILTER_TYPE_STOPPED:
                        if (event.getStatus() == Event.ESTATUS_STOP)
                            ++pos;
                        break;
                }

                if (_event._id == event._id)
                    return pos;
            }
            return -1;
        }
    }

    /*
    public void setList(List<Event> el)
    {
        eventList = el;
        notifyDataSetChanged();
    }
    */

    void addItem(Event event/*, boolean refresh*/)
    {
        synchronized (activityDataWrapper.eventList) {
            if (!activityDataWrapper.eventListFilled)
                return;

            //if (refresh)
            //    fragment.listView.getRecycledViewPool().clear();
            activityDataWrapper.eventList.add(event);
        }
        //if (refresh)
        //    notifyDataSetChanged();
    }

    void deleteItemNoNotify(Event event)
    {
        synchronized (activityDataWrapper.eventList) {
            if (!activityDataWrapper.eventListFilled)
                return;

            activityDataWrapper.eventList.remove(event);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear()
    {
        synchronized (activityDataWrapper.eventList) {
            if (!activityDataWrapper.eventListFilled)
                return;

            fragment.listView.getRecycledViewPool().clear(); // maybe fix for java.lang.IndexOutOfBoundsException: Inconsistency detected.
            activityDataWrapper.eventList.clear();
        }
        notifyDataSetChanged();
    }

    /*
    void setFilterType (int filterType) {
        this.filterType = filterType;
    }
    */

    @SuppressLint("NotifyDataSetChanged")
    public void notifyDataSetChanged(boolean refreshIcons) {
        if (refreshIcons) {
            synchronized (activityDataWrapper.eventList) {
                boolean applicationEditorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator;
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Event> it = activityDataWrapper.eventList.iterator(); it.hasNext(); ) {
                    Event event = it.next();
                    Profile profile;
                    if (event._fkProfileStart != Profile.PROFILE_NO_ACTIVATE) {
                        profile = activityDataWrapper.getProfileById(event._fkProfileStart, true,
                                applicationEditorPrefIndicator, false);
                        activityDataWrapper.refreshProfileIcon(profile, true,
                                applicationEditorPrefIndicator);
                    }
                    if (event._fkProfileEnd != Profile.PROFILE_NO_ACTIVATE) {
                        profile = activityDataWrapper.getProfileById(event._fkProfileEnd, true,
                                applicationEditorPrefIndicator, false);
                        activityDataWrapper.refreshProfileIcon(profile, true,
                                applicationEditorPrefIndicator);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onItemDismiss(int position) {
        synchronized (activityDataWrapper.eventList) {
            activityDataWrapper.eventList.remove(position);
        }
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        synchronized (activityDataWrapper.eventList) {
            if (!activityDataWrapper.eventListFilled)
                return false;

            //Log.d("----- EditorEventListAdapter.onItemMove", "fromPosition="+fromPosition);
            //Log.d("----- EditorEventListAdapter.onItemMove", "toPosition="+toPosition);

            if ((fromPosition < 0) || (toPosition < 0))
                return false;

            // convert positions from adapter into profileList
            int plFrom = activityDataWrapper.eventList.indexOf(getItem(fromPosition));
            int plTo = activityDataWrapper.eventList.indexOf(getItem(toPosition));

            if ((plFrom != -1) && (plTo != -1)) {
                if (plFrom < plTo) {
                    for (int i = plFrom; i < plTo; i++) {
                        Collections.swap(activityDataWrapper.eventList, i, i + 1);
                    }
                } else {
                    for (int i = plFrom; i > plTo; i--) {
                        Collections.swap(activityDataWrapper.eventList, i, i - 1);
                    }
                }

                DatabaseHandler.getInstance(activityDataWrapper.context).setEventStartOrder(activityDataWrapper.eventList);  // set events _startOrder and write it into db
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void clearView() {
        activityDataWrapper.restartEventsWithDelay(true, true, false, PPApplication.ALTYPE_EVENT_PREFERENCES_CHANGED);
    }

    void showTargetHelps(Activity activity, /*EditorEventListFragment fragment,*/ View listItemView) {
        //if (fragment.targetHelpsSequenceStarted)
        //    return;

        boolean startTargetHelpsFinished = ApplicationPreferences.prefEditorActivityStartTargetHelpsFinished &&
                ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsFinished;
        if (!startTargetHelpsFinished)
            return;

        boolean startTargetHelps = ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps;
        boolean startTargetHelpsOrder = ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder;
        boolean startTargetHelpsStatus = ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsStatus;

        if (startTargetHelps || startTargetHelpsOrder || startTargetHelpsStatus) {

            //String appTheme = ApplicationPreferences.applicationTheme(activity, true);
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
            //boolean tintTarget = !appTheme.equals("white");

            //Log.d("EditorEventListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS=true");

            Rect eventItemTarget = new Rect(0, 0, listItemView.getHeight(), listItemView.getHeight());
            int[] screenLocation = new int[2];
            listItemView.getLocationOnScreen(screenLocation);
            //listItemView.getLocationInWindow(screenLocation);

            final TapTargetSequence sequence = new TapTargetSequence(activity);

            if (startTargetHelps) {

                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activityDataWrapper.context);
                editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_ADAPTER_START_TARGET_HELPS, false);
                editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_ADAPTER_START_TARGET_HELPS_STATUS, false);
                editor.apply();
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps = false;
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsStatus = false;

                // do not add it again
                startTargetHelpsStatus = false;

                if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER) {
                    View view = listItemView.findViewById(R.id.event_list_drag_handle);
                    eventItemTarget.offset(screenLocation[0] + 80 + view.getWidth(), screenLocation[1]);

                    editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_ADAPTER_START_TARGET_HELPS_ORDER, false);
                    editor.apply();
                    ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder = false;

                    // do not add it again
                    startTargetHelpsOrder = false;

                    sequence.targets(
                            TapTarget.forBounds(eventItemTarget, activity.getString(R.string.editor_activity_targetHelps_eventPreferences_title), activity.getString(R.string.editor_activity_targetHelps_eventPreferences_description))
                                    .transparentTarget(true)
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .titleTextColor(titleTextColor)
                                    .descriptionTextColor(descriptionTextColor)
                                    .textTypeface(Typeface.DEFAULT_BOLD)
                                    .tintTarget(true)
                                    .drawShadow(true)
                                    .id(1),
                            TapTarget.forView(listItemView.findViewById(R.id.event_list_item_edit_menu), activity.getString(R.string.editor_activity_targetHelps_eventMenu_title), activity.getString(R.string.editor_activity_targetHelps_eventMenu_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .titleTextColor(titleTextColor)
                                    .descriptionTextColor(descriptionTextColor)
                                    .textTypeface(Typeface.DEFAULT_BOLD)
                                    .tintTarget(true)
                                    .drawShadow(true)
                                    .id(2),
                            TapTarget.forView(listItemView.findViewById(R.id.event_list_item_ignore_manual_activation), activity.getString(R.string.editor_activity_targetHelps_ignoreManualActivation_title), activity.getString(R.string.editor_activity_targetHelps_ignoreManualActivation_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .titleTextColor(titleTextColor)
                                    .descriptionTextColor(descriptionTextColor)
                                    .textTypeface(Typeface.DEFAULT_BOLD)
                                    .tintTarget(true)
                                    .drawShadow(true)
                                    .id(3),
                            TapTarget.forView(listItemView.findViewById(R.id.event_list_drag_handle), activity.getString(R.string.editor_activity_targetHelps_eventOrderHandler_title), activity.getString(R.string.editor_activity_targetHelps_eventOrderHandler_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .titleTextColor(titleTextColor)
                                    .descriptionTextColor(descriptionTextColor)
                                    .textTypeface(Typeface.DEFAULT_BOLD)
                                    .tintTarget(true)
                                    .drawShadow(true)
                                    .id(4),
                            TapTarget.forView(listItemView.findViewById(R.id.event_list_item_status), activity.getString(R.string.editor_activity_targetHelps_eventStatusIcon_title), activity.getString(R.string.editor_activity_targetHelps_eventStatusIcon_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .titleTextColor(titleTextColor)
                                    .descriptionTextColor(descriptionTextColor)
                                    .textTypeface(Typeface.DEFAULT_BOLD)
                                    .tintTarget(false)
                                    .drawShadow(true)
                                    .id(5)
                    );
                } else {
                    eventItemTarget.offset(screenLocation[0] + 80, screenLocation[1]);

                    sequence.targets(
                            TapTarget.forBounds(eventItemTarget, activity.getString(R.string.editor_activity_targetHelps_eventPreferences_title), activity.getString(R.string.editor_activity_targetHelps_eventPreferences_description))
                                    .transparentTarget(true)
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .titleTextColor(titleTextColor)
                                    .descriptionTextColor(descriptionTextColor)
                                    .textTypeface(Typeface.DEFAULT_BOLD)
                                    .tintTarget(true)
                                    .drawShadow(true)
                                    .id(1),
                            TapTarget.forView(listItemView.findViewById(R.id.event_list_item_edit_menu), activity.getString(R.string.editor_activity_targetHelps_eventMenu_title), activity.getString(R.string.editor_activity_targetHelps_eventMenu_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .titleTextColor(titleTextColor)
                                    .descriptionTextColor(descriptionTextColor)
                                    .textTypeface(Typeface.DEFAULT_BOLD)
                                    .tintTarget(true)
                                    .drawShadow(true)
                                    .id(2),
                            TapTarget.forView(listItemView.findViewById(R.id.event_list_item_ignore_manual_activation), activity.getString(R.string.editor_activity_targetHelps_ignoreManualActivation_title), activity.getString(R.string.editor_activity_targetHelps_ignoreManualActivation_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .titleTextColor(titleTextColor)
                                    .descriptionTextColor(descriptionTextColor)
                                    .textTypeface(Typeface.DEFAULT_BOLD)
                                    .tintTarget(true)
                                    .drawShadow(true)
                                    .id(3),
                            TapTarget.forView(listItemView.findViewById(R.id.event_list_item_status), activity.getString(R.string.editor_activity_targetHelps_eventStatusIcon_title), activity.getString(R.string.editor_activity_targetHelps_eventStatusIcon_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .titleTextColor(titleTextColor)
                                    .descriptionTextColor(descriptionTextColor)
                                    .textTypeface(Typeface.DEFAULT_BOLD)
                                    .tintTarget(false)
                                    .drawShadow(true)
                                    .id(4)
                    );
                }
            }

            if (startTargetHelpsOrder) {
                if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER) {
                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activityDataWrapper.context);
                    editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_ADAPTER_START_TARGET_HELPS_ORDER, false);
                    editor.apply();
                    ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder = false;

                    sequence.targets(
                            TapTarget.forView(listItemView.findViewById(R.id.event_list_drag_handle), activity.getString(R.string.editor_activity_targetHelps_eventOrderHandler_title), activity.getString(R.string.editor_activity_targetHelps_eventOrderHandler_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .titleTextColor(titleTextColor)
                                    .descriptionTextColor(descriptionTextColor)
                                    .textTypeface(Typeface.DEFAULT_BOLD)
                                    .tintTarget(true)
                                    .drawShadow(true)
                                    .id(1)
                    );
                }
            }

            if (startTargetHelpsStatus) {
                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activityDataWrapper.context);
                editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_ADAPTER_START_TARGET_HELPS_STATUS, false);
                editor.apply();
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsStatus= false;

                sequence.targets(
                        TapTarget.forView(listItemView.findViewById(R.id.event_list_item_status), activity.getString(R.string.editor_activity_targetHelps_eventStatusIcon_title), activity.getString(R.string.editor_activity_targetHelps_eventStatusIcon_description))
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

            sequence.listener(new TapTargetSequence.Listener() {
                // This listener will tell us when interesting(tm) events happen in regards
                // to the sequence
                @Override
                public void onSequenceFinish() {
                    //targetHelpsSequenceStarted = false;

                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activity.getApplicationContext());
                    editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_FRAGMENT_START_TARGET_HELPS_FINISHED, true);
                    //editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_FINISHED, true);
                    editor.apply();
                    ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsFinished = true;
                    //ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsFinished = true;

                }

                @Override
                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                    //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                }

                @Override
                public void onSequenceCanceled(TapTarget lastTarget) {
                    //targetHelpsSequenceStarted = false;

                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activity.getApplicationContext());
                    editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_FRAGMENT_START_TARGET_HELPS, false);
                    editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_ADAPTER_START_TARGET_HELPS, false);

                    editor.putBoolean(PPApplication.PREF_EDITOR_EVENT_LIST_FRAGMENT_START_TARGET_HELPS_FINISHED, true);
                    //editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_FINISHED, true);

                    editor.apply();

                    ApplicationPreferences.prefEditorEventsFragmentStartTargetHelps = false;
                    ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps = false;

                    ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsFinished = true;
                    //ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsFinished = true;

                }
            });
            sequence.continueOnCancel(true)
                    .considerOuterCircleCanceled(true);
            //targetHelpsSequenceStarted = true;

            //SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activity.getApplicationContext());
            //editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_FINISHED, false);
            //editor.apply();
            //ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsFinished = false;

            sequence.start();
        }
    }

}
