package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.Collections;
import java.util.List;

class EditorEventListAdapter extends RecyclerView.Adapter<EditorEventListViewHolder>
                                 implements ItemTouchHelperAdapter
{

    private EditorEventListFragment fragment;
    private DataWrapper dataWrapper;
    private int filterType;
    private List<Event> eventList;
    boolean released = false;
    //private int defaultColor;

    private final OnStartDragItemListener mDragStartListener;

    public boolean targetHelpsSequenceStarted;
    static final String PREF_START_TARGET_HELPS = "editor_event_list_adapter_start_target_helps";
    static final String PREF_START_TARGET_HELPS_ORDER = "editor_event_list_adapter_start_target_helps_order";

    EditorEventListAdapter(EditorEventListFragment f, DataWrapper pdw, int filterType,
                           OnStartDragItemListener dragStartListener)
    {
        fragment = f;
        dataWrapper = pdw;
        eventList = dataWrapper.getEventList();
        this.filterType = filterType;
        this.mDragStartListener = dragStartListener;
    }

    @Override
    public EditorEventListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER) {
            if (ApplicationPreferences.applicationEditorPrefIndicator(fragment.getActivity()))
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.editor_event_list_item_with_order, parent, false);
            else
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.editor_event_list_item_no_indicator_with_order, parent, false);
        }
        else {
            if (ApplicationPreferences.applicationEditorPrefIndicator(fragment.getActivity()))
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.editor_event_list_item, parent, false);
            else
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.editor_event_list_item_no_indicator, parent, false);
        }

        return new EditorEventListViewHolder(view, fragment, fragment.getActivity(), filterType);
    }

    @Override
    public void onBindViewHolder(final EditorEventListViewHolder holder, int position) {
        Event event = getItem(position);
        holder.bindEvent(event);

        if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER) {
            holder.dragHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        mDragStartListener.onStartDrag(holder);
                    }
                    return false;
                }
            });
        }
    }

    public void release()
    {
        released = true;

        fragment = null;
        eventList = null;
        dataWrapper = null;
    }

    @Override
    public int getItemCount() {
        fragment.textViewNoData.setVisibility(
                ((eventList != null) && (eventList.size() > 0)) ? View.GONE : View.VISIBLE);

        if (eventList == null)
            return 0;

        if ((filterType == EditorEventListFragment.FILTER_TYPE_ALL) ||
                (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER))
            return eventList.size();

        int count = 0;
        for (Event event : eventList)
        {
            switch (filterType)
            {
                case EditorEventListFragment.FILTER_TYPE_RUNNING:
                    if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_RUNNING)
                        ++count;
                    break;
                case EditorEventListFragment.FILTER_TYPE_PAUSED:
                    if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_PAUSE)
                        ++count;
                    break;
                case EditorEventListFragment.FILTER_TYPE_STOPPED:
                    if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_STOP)
                        ++count;
                    break;
            }
        }
        return count;
    }

    public Event getItem(int position)
    {
        if (getItemCount() == 0)
            return null;
        else
        {
            if ((filterType == EditorEventListFragment.FILTER_TYPE_ALL) ||
                    (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER))
                return eventList.get(position);

            Event _event = null;

            int pos = -1;
            for (Event event : eventList)
            {
                switch (filterType)
                {
                    case EditorEventListFragment.FILTER_TYPE_RUNNING:
                        if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_RUNNING)
                            ++pos;
                        break;
                    case EditorEventListFragment.FILTER_TYPE_PAUSED:
                        if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_PAUSE)
                            ++pos;
                        break;
                    case EditorEventListFragment.FILTER_TYPE_STOPPED:
                        if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_STOP)
                            ++pos;
                        break;
                }
                if (pos == position)
                {
                    _event = event;
                    break;
                }
            }

            return _event;
        }
    }

    int getItemPosition(Event event)
    {
        if (eventList == null)
            return -1;

        if (event == null)
            return -1;

        int pos = -1;

        for (int i = 0; i < eventList.size(); i++)
        {
            switch (filterType)
            {
                case EditorEventListFragment.FILTER_TYPE_ALL:
                case EditorEventListFragment.FILTER_TYPE_START_ORDER:
                    ++pos;
                    break;
                case EditorEventListFragment.FILTER_TYPE_RUNNING:
                    if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_RUNNING)
                        ++pos;
                    break;
                case EditorEventListFragment.FILTER_TYPE_PAUSED:
                    if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_PAUSE)
                        ++pos;
                    break;
                case EditorEventListFragment.FILTER_TYPE_STOPPED:
                    if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_STOP)
                        ++pos;
                    break;
            }

            if (eventList.get(i)._id == event._id)
                return pos;
        }
        return -1;
    }

    public void setList(List<Event> el)
    {
        eventList = el;
        notifyDataSetChanged();
    }

    void addItem(Event event, boolean refresh)
    {
        if (eventList == null)
            return;

        eventList.add(event);
        if (refresh)
            notifyDataSetChanged();
    }

    void deleteItemNoNotify(Event event)
    {
        if (eventList == null)
            return;

        eventList.remove(event);
    }

    public void clear()
    {
        if (eventList == null)
            return;

        eventList.clear();
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged(boolean refreshIcons) {
        if (refreshIcons) {
            for (Event event : eventList) {
                Profile profile = dataWrapper.getProfileById(event._fkProfileStart, false);
                dataWrapper.refreshProfileIcon(profile, false, 0);
                profile = dataWrapper.getProfileById(event._fkProfileEnd, false);
                dataWrapper.refreshProfileIcon(profile, false, 0);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onItemDismiss(int position) {
        eventList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (eventList == null)
            return false;

        //Log.d("----- EditorEventListAdapter.onItemMove", "fromPosition="+fromPosition);
        //Log.d("----- EditorEventListAdapter.onItemMove", "toPosition="+toPosition);

        // convert positions from adapter into profileList
        int plFrom = eventList.indexOf(getItem(fromPosition));
        int plTo = eventList.indexOf(getItem(toPosition));

        if (plFrom < plTo) {
            for (int i = plFrom; i < plTo; i++) {
                Collections.swap(eventList, i, i + 1);
            }
        } else {
            for (int i = plFrom; i > plTo; i--) {
                Collections.swap(eventList, i, i - 1);
            }
        }

        fragment.databaseHandler.setEventStartOrder(eventList);  // set events _startOrder and write it into db

        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    void showTargetHelps(Activity activity, EditorEventListFragment fragment, View listItemView) {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        if (fragment.targetHelpsSequenceStarted)
            return;

        ApplicationPreferences.getSharedPreferences(activity);

        if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true) || ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS_ORDER, true)) {

            //Log.d("EditorEventListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS_ORDER=true");

            int circleColor = 0xFFFFFF;
            if (ApplicationPreferences.applicationTheme(activity).equals("dark"))
                circleColor = 0x7F7F7F;

            if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true)) {
                //Log.d("EditorEventListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                editor.putBoolean(PREF_START_TARGET_HELPS, false);
                editor.apply();

                Rect eventItemTarget = new Rect(0, 0, listItemView.getHeight(), listItemView.getHeight());
                int[] screenLocation = new int[2];
                listItemView.getLocationOnScreen(screenLocation);
                eventItemTarget.offset(screenLocation[0] + listItemView.getWidth() / 2 - listItemView.getHeight() / 2, screenLocation[1]);

                final TapTargetSequence sequence = new TapTargetSequence(activity);

                if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER) {
                    editor.putBoolean(PREF_START_TARGET_HELPS_ORDER, false);
                    editor.apply();

                    sequence.targets(
                            TapTarget.forBounds(eventItemTarget, activity.getString(R.string.editor_activity_targetHelps_eventPreferences_title), activity.getString(R.string.editor_activity_targetHelps_eventPreferences_description))
                                    .transparentTarget(true)
                                    .textColorInt(0xFFFFFF)
                                    .drawShadow(true)
                                    .id(1),
                            TapTarget.forView(listItemView.findViewById(R.id.event_list_item_edit_menu), activity.getString(R.string.editor_activity_targetHelps_eventMenu_title), activity.getString(R.string.editor_activity_targetHelps_eventMenu_description))
                                    .targetCircleColorInt(circleColor)
                                    .textColorInt(0xFFFFFF)
                                    .drawShadow(true)
                                    .id(2),
                            TapTarget.forView(listItemView.findViewById(R.id.event_list_drag_handle), activity.getString(R.string.editor_activity_targetHelps_eventOrderHandler_title), activity.getString(R.string.editor_activity_targetHelps_eventOrderHandler_description))
                                    .targetCircleColorInt(circleColor)
                                    .textColorInt(0xFFFFFF)
                                    .drawShadow(true)
                                    .id(3)
                    );
                } else {
                    sequence.targets(
                            TapTarget.forBounds(eventItemTarget, activity.getString(R.string.editor_activity_targetHelps_eventPreferences_title), activity.getString(R.string.editor_activity_targetHelps_eventPreferences_description))
                                    .transparentTarget(true)
                                    .textColorInt(0xFFFFFF)
                                    .drawShadow(true)
                                    .id(1),
                            TapTarget.forView(listItemView.findViewById(R.id.event_list_item_edit_menu), activity.getString(R.string.editor_activity_targetHelps_eventMenu_title), activity.getString(R.string.editor_activity_targetHelps_eventMenu_description))
                                    .targetCircleColorInt(circleColor)
                                    .textColorInt(0xFFFFFF)
                                    .drawShadow(true)
                                    .id(2)
                    );
                }
                sequence.listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        targetHelpsSequenceStarted = false;
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        targetHelpsSequenceStarted = false;
                    }
                });
                sequence.continueOnCancel(true)
                        .considerOuterCircleCanceled(true);
                targetHelpsSequenceStarted = true;
                sequence.start();
            }
            else
            if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS_ORDER, true)) {
                //Log.d("EditorEventListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS=false");
                if (filterType == EditorEventListFragment.FILTER_TYPE_START_ORDER) {
                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                    editor.putBoolean(PREF_START_TARGET_HELPS_ORDER, false);
                    editor.apply();

                    final TapTargetSequence sequence = new TapTargetSequence(activity);
                    sequence.targets(
                            TapTarget.forView(listItemView.findViewById(R.id.event_list_drag_handle), activity.getString(R.string.editor_activity_targetHelps_eventOrderHandler_title), activity.getString(R.string.editor_activity_targetHelps_eventOrderHandler_description))
                                    .targetCircleColorInt(circleColor)
                                    .textColorInt(0xFFFFFF)
                                    .drawShadow(true)
                                    .id(1)
                    );
                    sequence.listener(new TapTargetSequence.Listener() {
                        // This listener will tell us when interesting(tm) events happen in regards
                        // to the sequence
                        @Override
                        public void onSequenceFinish() {
                            targetHelpsSequenceStarted = false;
                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                            //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {
                            targetHelpsSequenceStarted = false;
                        }
                    });
                    sequence.continueOnCancel(true)
                            .considerOuterCircleCanceled(true);
                    targetHelpsSequenceStarted = true;
                    sequence.start();
                }
            }
        }
    }

}
