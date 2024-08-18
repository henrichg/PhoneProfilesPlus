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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class EditorProfileListAdapter extends RecyclerView.Adapter<EditorProfileListViewHolder>
                                 implements ItemTouchHelperAdapter
{

    private EditorProfileListFragment fragment;
    private DataWrapper activityDataWrapper;
    private final int filterType;

    private final OnStartDragItemListener mDragStartListener;

    //private boolean targetHelpsSequenceStarted;

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
            if (ApplicationPreferences.applicationEditorPrefIndicator)
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_editor_profile, parent, false);
            else
                view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.listitem_editor_profile_no_indicator, parent, false);
        }
        else
        /*if (filterType == EditorProfileListFragment.FILTER_TYPE_ALL)
        {
            if (ApplicationPreferences.applicationEditorPrefIndicator)
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.editor_profile_list_item_all_profiles, parent, false);
            else
                view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.editor_profile_list_item_no_indicator_all_profiles, parent, false);
        }
        else*/
        {
            if (ApplicationPreferences.applicationEditorPrefIndicator)
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_editor_profile_no_order_handler, parent, false);
            else
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_editor_profile_no_indicator_no_order_handler, parent, false);
        }

        return new EditorProfileListViewHolder(view, fragment, fragment.getActivity(), filterType);
    }

    @Override
    public void onBindViewHolder(@NonNull final EditorProfileListViewHolder holder, int position) {
        Profile profile = getItem(position);
        holder.bindProfile(profile);

        if (filterType == EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR) {
            if (holder.dragHandle != null) {
                holder.dragHandle.setOnTouchListener((v, event) -> {
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
//        PPApplicationStatic.logE("[SYNCHRONIZED] EditorProfileListAdapter.getItemCount", "DataWrapper.profileList");
        synchronized (activityDataWrapper.profileList) {
            fragment.viewNoData.setVisibility(
                    ((activityDataWrapper.profileListFilled &&
                      (!activityDataWrapper.profileList.isEmpty()))
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
//            PPApplicationStatic.logE("[SYNCHRONIZED] EditorProfileListAdapter.getItem", "DataWrapper.profileList");
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

//        PPApplicationStatic.logE("[SYNCHRONIZED] EditorProfileListAdapter.getItemPosition", "DataWrapper.profileList");
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
//        PPApplicationStatic.logE("[SYNCHRONIZED] EditorProfileListAdapter.addItem", "DataWrapper.profileList");
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

    /*
    void setFilterType (int filterType) {
        this.filterType = filterType;
    }
    */

    public Profile getActivatedProfile()
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EditorProfileListAdapter.getActivatedProfile", "DataWrapper.profileList");
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

    @SuppressLint("NotifyDataSetChanged")
    public void activateProfile(Profile profile)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EditorProfileListAdapter.activateProfile", "DataWrapper.profileList");
        synchronized (activityDataWrapper.profileList) {
            if (!activityDataWrapper.profileListFilled)
                return;

            fragment.listView.getRecycledViewPool().clear(); // maybe fix for java.lang.IndexOutOfBoundsException: Inconsistency detected.

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
        fragment.listView.getRecycledViewPool().clear();  // maybe fix for java.lang.IndexOutOfBoundsException: Inconsistency detected.
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void notifyDataSetChanged(boolean refreshIcons) {
        if (refreshIcons) {
//            PPApplicationStatic.logE("[SYNCHRONIZED] EditorProfileListAdapter.notifyDataSetChanged", "DataWrapper.profileList");
            synchronized (activityDataWrapper.profileList) {
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Profile> it = activityDataWrapper.profileList.iterator(); it.hasNext(); ) {
                    Profile profile = it.next();
                    activityDataWrapper.refreshProfileIcon(profile, true,
                            ApplicationPreferences.applicationEditorPrefIndicator);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onItemDismiss(int position) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EditorProfileListAdapter.onItemDismiss", "DataWrapper.profileList");
        synchronized (activityDataWrapper.profileList) {
            activityDataWrapper.profileList.remove(position);
        }
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EditorProfileListAdapter.onItemMove", "DataWrapper.profileList");
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

            if ((plFrom != -1) && (plTo != -1)) {
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
        }

        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void clearView() {
//        PPApplicationStatic.logE("[PPP_NOTIFICATION] EditorProfileListAdapter.onItemMove", "call of updateGUI");
        PPApplication.updateGUI(true, false, activityDataWrapper.context);
    }


    void showTargetHelps(final Activity activity, final View listItemView) {
        boolean startTargetHelpsFinished = ApplicationPreferences.prefEditorActivityStartTargetHelpsFinished &&
                ApplicationPreferences.prefEditorProfilesFragmentStartTargetHelpsFinished;
        if (!startTargetHelpsFinished)
            return;


        boolean startTargetHelps = ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelps;
        boolean startTargetHelpsOrder = ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsOrder;
        boolean startTargetHelpsShowInActivator = ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsShowInActivator;

        if (startTargetHelps || startTargetHelpsOrder || startTargetHelpsShowInActivator) {
            //Log.d("EditorProfileListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS_ORDER=true");

            int outerCircleColor = R.color.tabTargetHelpOuterCircleColor;
            int targetCircleColor = R.color.tabTargetHelpTargetCircleColor;
            int titleTextColor = R.color.tabTargetHelpTitleTextColor;
            int descriptionTextColor = R.color.tabTargetHelpDescriptionTextColor;

            final TapTargetSequence sequence = new TapTargetSequence(activity);
            List<TapTarget> targets = new ArrayList<>();

            if (startTargetHelps) {
                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activityDataWrapper.context);
                editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_ADAPTER_START_TARGET_HELPS, false);
                editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_ADAPTER_START_TARGET_HELPS_SHOW_IN_ACTIVATOR, false);
                editor.apply();
                ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelps = false;
                ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsShowInActivator = false;

                startTargetHelpsShowInActivator = false;

                Rect profileItemTarget = new Rect(0, 0, listItemView.getHeight(), listItemView.getHeight());
                int[] screenLocation = new int[2];
                listItemView.getLocationOnScreen(screenLocation);

                if (filterType == EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR) {
                    View dragHandle = listItemView.findViewById(R.id.profile_list_drag_handle);
                    profileItemTarget.offset(screenLocation[0] + 100 + dragHandle.getWidth(), screenLocation[1]);

                    editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_ADAPTER_START_TARGET_HELPS_ORDER, false);
                    editor.apply();
                    ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsOrder = false;

                    // do not add it again
                    startTargetHelpsOrder = false;

                    targets.add(
                            TapTarget.forBounds(profileItemTarget, activity.getString(R.string.editor_activity_targetHelps_profilePreferences_title), activity.getString(R.string.editor_activity_targetHelps_profilePreferences_description))
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
                    targets.add(
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_item_show_in_activator), activity.getString(R.string.editor_activity_targetHelps_showInActivator_title), activity.getString(R.string.editor_activity_targetHelps_showInActivator_description))
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
                                    .id(2)
                    );
                    targets.add(
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_item_edit_menu), activity.getString(R.string.editor_activity_targetHelps_profileMenu_title), activity.getString(R.string.editor_activity_targetHelps_profileMenu_description))
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
                                    .id(3)
                    );
                    targets.add(
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_drag_handle), activity.getString(R.string.editor_activity_targetHelps_profileOrderHandler_title), activity.getString(R.string.editor_activity_targetHelps_profileOrderHandler_description))
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
                                    .id(4)
                    );
                } else if (filterType == EditorProfileListFragment.FILTER_TYPE_ALL) {
                    profileItemTarget.offset(screenLocation[0] + 100, screenLocation[1]);

                    targets.add(
                            TapTarget.forBounds(profileItemTarget, activity.getString(R.string.editor_activity_targetHelps_profilePreferences_title), activity.getString(R.string.editor_activity_targetHelps_profilePreferences_description))
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
                    targets.add(
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_item_show_in_activator), activity.getString(R.string.editor_activity_targetHelps_showInActivator_title), activity.getString(R.string.editor_activity_targetHelps_showInActivator_description))
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
                                    .id(2)
                    );
                    targets.add(
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_item_edit_menu), activity.getString(R.string.editor_activity_targetHelps_profileMenu_title), activity.getString(R.string.editor_activity_targetHelps_profileMenu_description))
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
                                    .id(3)
                    );
                } else {
                    profileItemTarget.offset(screenLocation[0] + 100, screenLocation[1]);

                    targets.add(
                            TapTarget.forBounds(profileItemTarget, activity.getString(R.string.editor_activity_targetHelps_profilePreferences_title), activity.getString(R.string.editor_activity_targetHelps_profilePreferences_description))
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
                    targets.add(
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_item_show_in_activator), activity.getString(R.string.editor_activity_targetHelps_showInActivator_title), activity.getString(R.string.editor_activity_targetHelps_showInActivator_description))
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
                                    .id(2)
                    );
                    targets.add(
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_item_edit_menu), activity.getString(R.string.editor_activity_targetHelps_profileMenu_title), activity.getString(R.string.editor_activity_targetHelps_profileMenu_description))
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
                                    .id(3)
                    );
                }

            }

            if (startTargetHelpsOrder) {
                if (filterType == EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR) {
                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activityDataWrapper.context);
                    editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_ADAPTER_START_TARGET_HELPS_ORDER, false);
                    editor.apply();
                    ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsOrder = false;

                    targets.add(
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_drag_handle), activity.getString(R.string.editor_activity_targetHelps_profileOrderHandler_title), activity.getString(R.string.editor_activity_targetHelps_profileOrderHandler_description))
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
                }
            }

            if (startTargetHelpsShowInActivator) {
                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activityDataWrapper.context);
                editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_ADAPTER_START_TARGET_HELPS_SHOW_IN_ACTIVATOR, false);
                editor.apply();
                ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsShowInActivator = false;

                targets.add(
                        TapTarget.forView(listItemView.findViewById(R.id.profile_list_item_show_in_activator), activity.getString(R.string.editor_activity_targetHelps_showInActivator_title), activity.getString(R.string.editor_activity_targetHelps_showInActivator_description))
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
            }

            sequence.listener(new TapTargetSequence.Listener() {
                // This listener will tell us when interesting(tm) events happen in regards
                // to the sequence
                @Override
                public void onSequenceFinish() {
                    //targetHelpsSequenceStarted = false;

                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activity.getApplicationContext());
                    editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_FRAGMENT_START_TARGET_HELPS_FINISHED, true);
                    editor.apply();
                    ApplicationPreferences.prefEditorProfilesFragmentStartTargetHelpsFinished = true;
                }

                @Override
                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                    //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                }

                @Override
                public void onSequenceCanceled(TapTarget lastTarget) {
                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activity.getApplicationContext());
                    editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_FRAGMENT_START_TARGET_HELPS, false);
                    editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_ADAPTER_START_TARGET_HELPS, false);

                    editor.putBoolean(PPApplication.PREF_EDITOR_PROFILE_LIST_FRAGMENT_START_TARGET_HELPS_FINISHED, true);

                    editor.apply();

                    ApplicationPreferences.prefEditorProfilesFragmentStartTargetHelps = false;
                    ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelps = false;

                    ApplicationPreferences.prefEditorProfilesFragmentStartTargetHelpsFinished = true;
                }
            });
            sequence.continueOnCancel(true)
                    .considerOuterCircleCanceled(true);

            for (TapTarget target : targets) {
                target.setDrawBehindStatusBar(true);
                target.setDrawBehindNavigationBar(true);
            }
            sequence.targets(targets);

            sequence.start();

        }
    }

}
