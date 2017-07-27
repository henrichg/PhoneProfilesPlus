package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.Collections;
import java.util.List;

class EditorProfileListAdapter extends RecyclerView.Adapter<EditorProfileListViewHolder>
                                 implements ItemTouchHelperAdapter
{

    private EditorProfileListFragment fragment;
    private DataWrapper dataWrapper;
    private int filterType;
    List<Profile> profileList;

    private final OnStartDragItemListener mDragStartListener;

    public boolean targetHelpsSequenceStarted;
    static final String PREF_START_TARGET_HELPS = "editor_profile_list_adapter_start_target_helps";
    static final String PREF_START_TARGET_HELPS_ORDER = "editor_profile_list_adapter_start_target_helps_order";

    EditorProfileListAdapter(EditorProfileListFragment f, DataWrapper pdw, int filterType,
                              OnStartDragItemListener dragStartListener)
    {
        fragment = f;
        dataWrapper = pdw;
        profileList = dataWrapper.getProfileList();
        this.filterType = filterType;
        this.mDragStartListener = dragStartListener;
    }

    @Override
    public EditorProfileListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (filterType == EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR)
        {
            if (ApplicationPreferences.applicationEditorPrefIndicator(dataWrapper.context))
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.editor_profile_list_item, parent, false);
            else
                view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.editor_profile_list_item_no_indicator, parent, false);
        }
        else
        {
            if (ApplicationPreferences.applicationEditorPrefIndicator(dataWrapper.context))
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.editor_profile_list_item_no_order_handler, parent, false);
            else
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.editor_profile_list_item_no_indicator_no_order_handler, parent, false);
        }

        return new EditorProfileListViewHolder(view, fragment, fragment.getActivity(), filterType);
    }

    @Override
    public void onBindViewHolder(final EditorProfileListViewHolder holder, int position) {
        Profile profile = getItem(position);
        holder.bindProfile(profile);

        if (filterType == EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR) {
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
        fragment = null;
        profileList = null;
        dataWrapper = null;
    }

    @Override
    public int getItemCount() {
        fragment.textViewNoData.setVisibility(
                ((profileList != null) && (profileList.size() > 0)) ? View.GONE : View.VISIBLE);

        if (profileList == null)
            return 0;

        if (filterType == EditorProfileListFragment.FILTER_TYPE_ALL)
            return profileList.size();

        int count = 0;
        for (Profile profile : profileList)
        {
            switch (filterType)
            {
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

    public Profile getItem(int position)
    {
        if (getItemCount() == 0)
            return null;
        else
        {

            if (filterType == EditorProfileListFragment.FILTER_TYPE_ALL)
                return profileList.get(position);

            Profile _profile = null;

            int pos = -1;
            for (Profile profile : profileList)
            {
                switch (filterType)
                {
                    case EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR:
                        if (profile._showInActivator)
                            ++pos;
                        break;
                    case EditorProfileListFragment.FILTER_TYPE_NO_SHOW_IN_ACTIVATOR:
                        if (!profile._showInActivator)
                            ++pos;
                        break;
                }
                if (pos == position)
                {
                    _profile = profile;
                    break;
                }
            }

            return _profile;
        }
    }

    int getItemPosition(Profile profile)
    {
        if (profile == null)
            return -1;

        if (profileList == null)
            return -1;

        int pos = -1;

        for (int i = 0; i < profileList.size(); i++)
        {
            switch (filterType)
            {
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

            if (profileList.get(i)._id == profile._id)
                return pos;
        }
        return -1;
    }

    public void setList(List<Profile> pl)
    {
        profileList = pl;
        notifyDataSetChanged();
    }

    void addItem(Profile profile, boolean refresh)
    {
        if (profileList == null)
            return;

        profileList.add(profile);
        if (refresh)
            notifyDataSetChanged();
    }

    void deleteItemNoNotify(Profile profile)
    {
        dataWrapper.deleteProfile(profile);
    }

    void clearNoNotify()
    {
        dataWrapper.deleteAllProfiles();
    }

    public void clear()
    {
        clearNoNotify();
        notifyDataSetChanged();
    }

    public Profile getActivatedProfile()
    {
        if (profileList == null)
            return null;

        for (Profile p : profileList)
        {
            if (p._checked)
            {
                return p;
            }
        }

        return null;
    }

    public void activateProfile(Profile profile)
    {
        if (profileList == null)
            return;

        for (Profile p : profileList)
        {
            p._checked = false;
        }

        // teraz musime najst profile v profileList
        int position = getItemPosition(profile);
        if (position != -1)
        {
            // najdenemu objektu nastavime _checked
            Profile _profile = profileList.get(position);
            if (_profile != null)
                _profile._checked = true;
        }
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged(boolean refreshIcons) {
        if (refreshIcons) {
            for (Profile profile : profileList) {
                dataWrapper.refreshProfileIcon(profile, false, 0);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onItemDismiss(int position) {
        profileList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (profileList == null)
            return false;

        //Log.d("----- EditorProfileListAdapter.onItemMove", "fromPosition="+fromPosition);
        //Log.d("----- EditorProfileListAdapter.onItemMove", "toPosition="+toPosition);

        // convert positions from adapter into profileList
        int plFrom = profileList.indexOf(getItem(fromPosition));
        int plTo = profileList.indexOf(getItem(toPosition));

        if (plFrom < plTo) {
            for (int i = plFrom; i < plTo; i++) {
                Collections.swap(profileList, i, i + 1);
            }
        } else {
            for (int i = plFrom; i > plTo; i--) {
                Collections.swap(profileList, i, i - 1);
            }
        }

        fragment.databaseHandler.setProfileOrder(profileList);  // set profiles _porder and write it into db
        fragment.activateProfileHelper.updateWidget(false);

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

        if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true) || ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS_ORDER, true)) {

            //Log.d("EditorProfileListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS_ORDER=true");

            int circleColor = 0xFFFFFF;
            if (ApplicationPreferences.applicationTheme(activity).equals("dark"))
                circleColor = 0x7F7F7F;

            if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true)) {
                //Log.d("EditorProfileListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                editor.putBoolean(PREF_START_TARGET_HELPS, false);
                editor.apply();

                Rect profileItemTarget = new Rect(0, 0, listItemView.getHeight(), listItemView.getHeight());
                int[] screenLocation = new int[2];
                listItemView.getLocationOnScreen(screenLocation);
                profileItemTarget.offset(screenLocation[0] + listItemView.getWidth() / 2 - listItemView.getHeight() / 2, screenLocation[1]);

                final TapTargetSequence sequence = new TapTargetSequence(activity);

                if (filterType == EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR) {
                    editor.putBoolean(PREF_START_TARGET_HELPS_ORDER, false);
                    editor.apply();

                    sequence.targets(
                            TapTarget.forBounds(profileItemTarget, activity.getString(R.string.editor_activity_targetHelps_profilePreferences_title), activity.getString(R.string.editor_activity_targetHelps_profilePreferences_description))
                                    .transparentTarget(true)
                                    .textColorInt(0xFFFFFF)
                                    .drawShadow(true)
                                    .id(1),
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_item_edit_menu), activity.getString(R.string.editor_activity_targetHelps_profileMenu_title), activity.getString(R.string.editor_activity_targetHelps_profileMenu_description))
                                    .targetCircleColorInt(circleColor)
                                    .textColorInt(0xFFFFFF)
                                    .drawShadow(true)
                                    .id(2),
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_drag_handle), activity.getString(R.string.editor_activity_targetHelps_profileOrderHandler_title), activity.getString(R.string.editor_activity_targetHelps_profileOrderHandler_description))
                                    .targetCircleColorInt(circleColor)
                                    .textColorInt(0xFFFFFF)
                                    .drawShadow(true)
                                    .id(3)
                    );
                } else {
                    sequence.targets(
                            TapTarget.forBounds(profileItemTarget, activity.getString(R.string.editor_activity_targetHelps_profilePreferences_title), activity.getString(R.string.editor_activity_targetHelps_profilePreferences_description))
                                    .transparentTarget(true)
                                    .textColorInt(0xFFFFFF)
                                    .drawShadow(true)
                                    .id(1),
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_item_edit_menu), activity.getString(R.string.editor_activity_targetHelps_profileMenu_title), activity.getString(R.string.editor_activity_targetHelps_profileMenu_description))
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
            if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS_ORDER, true)) {
                //Log.d("EditorProfileListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS=false");
                if (filterType == EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR) {
                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                    editor.putBoolean(PREF_START_TARGET_HELPS_ORDER, false);
                    editor.apply();

                    final TapTargetSequence sequence = new TapTargetSequence(activity);
                    sequence.targets(
                            TapTarget.forView(listItemView.findViewById(R.id.profile_list_drag_handle), activity.getString(R.string.editor_activity_targetHelps_profileOrderHandler_title), activity.getString(R.string.editor_activity_targetHelps_profileOrderHandler_description))
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
