package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.Collections;
import java.util.Iterator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class EditorProfileListAdapter extends RecyclerView.Adapter<EditorProfileListViewHolder>
                                 implements ItemTouchHelperAdapter
{

    private EditorProfileListFragment fragment;
    private DataWrapper activityDataWrapper;
    private final int filterType;

    private final OnStartDragItemListener mDragStartListener;

    //private boolean targetHelpsSequenceStarted;
    static final String PREF_START_TARGET_HELPS = "editor_profile_list_adapter_start_target_helps";
    static final String PREF_START_TARGET_HELPS_ORDER = "editor_profile_list_adapter_start_target_helps_order";
    static final String PREF_START_TARGET_HELPS_SHOW_IN_ACTIVATOR = "editor_profile_list_adapter_start_target_helps_show_in_activator";

    EditorProfileListAdapter(EditorProfileListFragment f, DataWrapper pdw, int filterType,
                              OnStartDragItemListener dragStartListener)
    {
        fragment = f;
        activityDataWrapper = pdw;
        this.filterType = filterType;
        this.mDragStartListener = dragStartListener;
    }

    @NonNull
    @Override
    public EditorProfileListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (filterType == EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR)
        {
            if (ApplicationPreferences.applicationEditorPrefIndicator(activityDataWrapper.context))
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.editor_profile_list_item, parent, false);
            else
                view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.editor_profile_list_item_no_indicator, parent, false);
        }
        else
        if (filterType == EditorProfileListFragment.FILTER_TYPE_ALL)
        {
            if (ApplicationPreferences.applicationEditorPrefIndicator(activityDataWrapper.context))
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.editor_profile_list_item_all_profiles, parent, false);
            else
                view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.editor_profile_list_item_no_indicator_all_profiles, parent, false);
        }
        else
        {
            if (ApplicationPreferences.applicationEditorPrefIndicator(activityDataWrapper.context))
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.editor_profile_list_item_no_order_handler, parent, false);
            else
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.editor_profile_list_item_no_indicator_no_order_handler, parent, false);
        }

        return new EditorProfileListViewHolder(view, fragment, fragment.getActivity(), filterType);
    }

    @Override
    public void onBindViewHolder(@NonNull final EditorProfileListViewHolder holder, int position) {
        Profile profile = getItem(position);
        holder.bindProfile(profile);

        if (filterType == EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR) {
            if (holder.dragHandle != null) {
                holder.dragHandle.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
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
                    }
                });
            }
        }
    }

    public void release()
    {
        fragment = null;
        activityDataWrapper = null;
    }

    @Override
    public int getItemCount() {
        synchronized (activityDataWrapper.profileList) {
            fragment.textViewNoData.setVisibility(
                    ((activityDataWrapper.profileListFilled &&
                      (activityDataWrapper.profileList.size() > 0))
                    ) ? View.GONE : View.VISIBLE);

            if (!activityDataWrapper.profileListFilled)
                return 0;

            if (filterType == EditorProfileListFragment.FILTER_TYPE_ALL)
                return activityDataWrapper.profileList.size();

            int count = 0;
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Profile> it = activityDataWrapper.profileList.iterator(); it.hasNext(); ) {
                Profile profile = it.next();
                switch (filterType) {
                    case EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR:
                        if (profile._showInActivator)
                            ++count;
                        break;
                    case EditorProfileListFragment.FILTER_TYPE_NO_SHOW_IN_ACTIVATOR:
                        if (!profile._showInActivator)
                            ++count;
                        break;
                }
            }
            return count;
        }
    }

    private Profile getItem(int position)
    {
        if (getItemCount() == 0)
            return null;
        else
        {
            synchronized (activityDataWrapper.profileList) {
                if (filterType == EditorProfileListFragment.FILTER_TYPE_ALL)
                    return activityDataWrapper.profileList.get(position);

                Profile _profile = null;

                int pos = -1;
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Profile> it = activityDataWrapper.profileList.iterator(); it.hasNext(); ) {
                    Profile profile = it.next();
                    switch (filterType) {
                        case EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR:
                            if (profile._showInActivator)
                                ++pos;
                            break;
                        case EditorProfileListFragment.FILTER_TYPE_NO_SHOW_IN_ACTIVATOR:
                            if (!profile._showInActivator)
                                ++pos;
                            break;
                    }
                    if (pos == position) {
                        _profile = profile;
                        break;
                    }
                }

                return _profile;
            }
        }
    }

    int getItemPosition(Profile profile)
    {
        if (profile == null)
            return -1;

        synchronized (activityDataWrapper.profileList) {
            if (!activityDataWrapper.profileListFilled)
                return -1;

            int pos = -1;

            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Profile> it = activityDataWrapper.profileList.iterator(); it.hasNext(); ) {
                Profile _profile = it.next();
                switch (filterType) {
                    case EditorProfileListFragment.FILTER_TYPE_ALL:
                        ++pos;
                        break;
                    case EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR:
                        if (profile._showInActivator)
                            ++pos;
                        break;
                    case EditorProfileListFragment.FILTER_TYPE_NO_SHOW_IN_ACTIVATOR:
                        if (!profile._showInActivator)
                            ++pos;
                        break;
                }

                if (_profile._id == profile._id)
                    return pos;
            }
            return -1;
        }
    }

    /*
    public void setList(List<Profile> pl)
    {
        profileList = pl;
        notifyDataSetChanged();
    }
    */

    void addItem(Profile profile/*, boolean refresh*/)
    {
        synchronized (activityDataWrapper.profileList) {
            if (!activityDataWrapper.profileListFilled)
                return;

            //if (refresh)
            //    fragment.listView.getRecycledViewPool().clear();
            activityDataWrapper.profileList.add(profile);
        }
        //if (refresh)
        //    notifyDataSetChanged();
    }

    void deleteItemNoNotify(Profile profile)
    {
        activityDataWrapper.deleteProfile(profile);
    }

    void clearNoNotify()
    {
        activityDataWrapper.deleteAllProfiles();
    }

    /*
    public void clear()
    {
        clearNoNotify();
        notifyDataSetChanged();
    }
    */

    public Profile getActivatedProfile()
    {
        synchronized (activityDataWrapper.profileList) {
            if (!activityDataWrapper.profileListFilled)
                return null;

            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Profile> it = activityDataWrapper.profileList.iterator(); it.hasNext(); ) {
                Profile profile = it.next();
                if (profile._checked) {
                    return profile;
                }
            }
        }

        return null;
    }

    public void activateProfile(Profile profile)
    {
        synchronized (activityDataWrapper.profileList) {
            if (!activityDataWrapper.profileListFilled)
                return;

            fragment.listView.getRecycledViewPool().clear();

            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Profile> it = activityDataWrapper.profileList.iterator(); it.hasNext(); ) {
                Profile _profile = it.next();
                _profile._checked = false;
            }

            // search for profile in profile list
            int position = getItemPosition(profile);
            if (position != -1) {
                // set _checked=true for profile
                Profile _profile = activityDataWrapper.profileList.get(position);
                if (_profile != null)
                    _profile._checked = true;
            }
        }
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged(boolean refreshIcons) {
        if (refreshIcons) {
            synchronized (activityDataWrapper.profileList) {
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Profile> it = activityDataWrapper.profileList.iterator(); it.hasNext(); ) {
                    Profile profile = it.next();
                    activityDataWrapper.refreshProfileIcon(profile, true,
                            ApplicationPreferences.applicationEditorPrefIndicator(activityDataWrapper.context));
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onItemDismiss(int position) {
        synchronized (activityDataWrapper.profileList) {
            activityDataWrapper.profileList.remove(position);
        }
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        synchronized (activityDataWrapper.profileList) {
            if (!activityDataWrapper.profileListFilled)
                return false;

            //Log.d("----- EditorProfileListAdapter.onItemMove", "fromPosition="+fromPosition);
            //Log.d("----- EditorProfileListAdapter.onItemMove", "toPosition="+toPosition);

            if ((fromPosition < 0) || (toPosition < 0))
                return false;

            // convert positions from adapter into profileList
            int plFrom = activityDataWrapper.profileList.indexOf(getItem(fromPosition));
            int plTo = activityDataWrapper.profileList.indexOf(getItem(toPosition));

            if (plFrom < plTo) {
                for (int i = plFrom; i < plTo; i++) {
                    Collections.swap(activityDataWrapper.profileList, i, i + 1);
                }
            } else {
                for (int i = plFrom; i > plTo; i--) {
                    Collections.swap(activityDataWrapper.profileList, i, i - 1);
                }
            }

            DatabaseHandler.getInstance(activityDataWrapper.context).setProfileOrder(activityDataWrapper.profileList);  // set profiles _porder and write it into db
        }

        PPApplication.logE("ActivateProfileHelper.updateGUI", "from EditorProfileListAdapter.onItemMove");
        ActivateProfileHelper.updateGUI(activityDataWrapper.context, false, true);

        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    void showTargetHelps(final Activity activity, final EditorProfileListFragment fragment, final View listItemView) {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        if (fragment.targetHelpsSequenceStarted)
            return;

        ApplicationPreferences.getSharedPreferences(activity);

        boolean startTargetHelps = ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true);
        boolean startTargetHelpsOrder = ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS_ORDER, true);
        boolean startTargetHelpsShowInActivator = ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS_SHOW_IN_ACTIVATOR, true);

        if (startTargetHelps || startTargetHelpsOrder) {

            //Log.d("EditorProfileListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS_ORDER=true");

            String appTheme = ApplicationPreferences.applicationTheme(activity, true);
            int outerCircleColor = R.color.tabTargetHelpOuterCircleColor;
//                if (appTheme.equals("dark"))
//                    outerCircleColor = R.color.tabTargetHelpOuterCircleColor_dark;
            int targetCircleColor = R.color.tabTargetHelpTargetCircleColor;
//                if (appTheme.equals("dark"))
//                    targetCircleColor = R.color.tabTargetHelpTargetCircleColor_dark;
            int textColor = R.color.tabTargetHelpTextColor;
//                if (appTheme.equals("dark"))
//                    textColor = R.color.tabTargetHelpTextColor_dark;
            boolean tintTarget = !appTheme.equals("white");

            if (startTargetHelps) {
                //Log.d("EditorProfileListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                editor.putBoolean(PREF_START_TARGET_HELPS, false);
                editor.apply();

                Rect profileItemTarget = new Rect(0, 0, listItemView.getHeight(), listItemView.getHeight());
                int[] screenLocation = new int[2];
                listItemView.getLocationOnScreen(screenLocation);
                //listItemView.getLocationInWindow(screenLocation);

                final TapTargetSequence sequence = new TapTargetSequence(activity);

                if (filterType == EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR) {
                    View dragHandle = listItemView.findViewById(R.id.profile_list_drag_handle);
                    profileItemTarget.offset(screenLocation[0] + 100 + dragHandle.getWidth(), screenLocation[1]);

                    editor.putBoolean(PREF_START_TARGET_HELPS_ORDER, false);
                    editor.apply();
                    startTargetHelpsOrder = false;
                    startTargetHelpsShowInActivator = false;

                    sequence.targets(
                            TapTarget.forBounds(profileItemTarget, activity.getString(R.string.editor_activity_targetHelps_profilePreferences_title), activity.getString(R.string.editor_activity_targetHelps_profilePreferences_description))
                                    .transparentTarget(true)
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(1),
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_item_edit_menu), activity.getString(R.string.editor_activity_targetHelps_profileMenu_title), activity.getString(R.string.editor_activity_targetHelps_profileMenu_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(2),
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_drag_handle), activity.getString(R.string.editor_activity_targetHelps_profileOrderHandler_title), activity.getString(R.string.editor_activity_targetHelps_profileOrderHandler_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(3)
                    );
                }
                else
                if (filterType == EditorProfileListFragment.FILTER_TYPE_ALL) {
                    profileItemTarget.offset(screenLocation[0] + 100, screenLocation[1]);

                    editor.putBoolean(PREF_START_TARGET_HELPS_SHOW_IN_ACTIVATOR, false);
                    editor.apply();
                    startTargetHelpsShowInActivator = false;

                    sequence.targets(
                            TapTarget.forBounds(profileItemTarget, activity.getString(R.string.editor_activity_targetHelps_profilePreferences_title), activity.getString(R.string.editor_activity_targetHelps_profilePreferences_description))
                                    .transparentTarget(true)
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(1),
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_item_show_in_activator), activity.getString(R.string.editor_activity_targetHelps_showInActivator_title), activity.getString(R.string.editor_activity_targetHelps_showInActivator_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(2),
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_item_edit_menu), activity.getString(R.string.editor_activity_targetHelps_profileMenu_title), activity.getString(R.string.editor_activity_targetHelps_profileMenu_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(3)
                    );
                }
                else {
                    profileItemTarget.offset(screenLocation[0] + 100, screenLocation[1]);

                    sequence.targets(
                            TapTarget.forBounds(profileItemTarget, activity.getString(R.string.editor_activity_targetHelps_profilePreferences_title), activity.getString(R.string.editor_activity_targetHelps_profilePreferences_description))
                                    .transparentTarget(true)
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(1),
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_item_edit_menu), activity.getString(R.string.editor_activity_targetHelps_profileMenu_title), activity.getString(R.string.editor_activity_targetHelps_profileMenu_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(2)
                    );
                }
                sequence.listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        //targetHelpsSequenceStarted = false;
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        //targetHelpsSequenceStarted = false;
                    }
                });
                sequence.continueOnCancel(true)
                        .considerOuterCircleCanceled(true);
                //targetHelpsSequenceStarted = true;
                sequence.start();
            }
            if (startTargetHelpsOrder) {
                //Log.d("EditorProfileListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS=false");
                if (filterType == EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR) {
                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                    editor.putBoolean(PREF_START_TARGET_HELPS_ORDER, false);
                    editor.apply();

                    final TapTargetSequence sequence = new TapTargetSequence(activity);
                    sequence.targets(
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_drag_handle), activity.getString(R.string.editor_activity_targetHelps_profileOrderHandler_title), activity.getString(R.string.editor_activity_targetHelps_profileOrderHandler_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(1)
                    );
                    sequence.listener(new TapTargetSequence.Listener() {
                        // This listener will tell us when interesting(tm) events happen in regards
                        // to the sequence
                        @Override
                        public void onSequenceFinish() {
                            //targetHelpsSequenceStarted = false;
                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                            //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {
                            //targetHelpsSequenceStarted = false;
                        }
                    });
                    sequence.continueOnCancel(true)
                            .considerOuterCircleCanceled(true);
                    //targetHelpsSequenceStarted = true;
                    sequence.start();
                }
            }
            if (startTargetHelpsShowInActivator) {
                //Log.d("EditorProfileListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS=false");
                if (filterType == EditorProfileListFragment.FILTER_TYPE_ALL) {
                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                    editor.putBoolean(PREF_START_TARGET_HELPS_SHOW_IN_ACTIVATOR, false);
                    editor.apply();

                    final TapTargetSequence sequence = new TapTargetSequence(activity);
                    sequence.targets(
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_item_show_in_activator), activity.getString(R.string.editor_activity_targetHelps_showInActivator_title), activity.getString(R.string.editor_activity_targetHelps_showInActivator_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(tintTarget)
                                    .drawShadow(true)
                                    .id(1)
                    );
                    sequence.listener(new TapTargetSequence.Listener() {
                        // This listener will tell us when interesting(tm) events happen in regards
                        // to the sequence
                        @Override
                        public void onSequenceFinish() {
                            //targetHelpsSequenceStarted = false;
                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                            //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {
                            //targetHelpsSequenceStarted = false;
                        }
                    });
                    sequence.continueOnCancel(true)
                            .considerOuterCircleCanceled(true);
                    //targetHelpsSequenceStarted = true;
                    sequence.start();
                }
            }
        }

    }

}
