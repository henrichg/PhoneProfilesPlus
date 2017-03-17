package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.List;

class EditorProfileListAdapter extends BaseAdapter
{

    private EditorProfileListFragment fragment;
    private DataWrapper dataWrapper;
    private int filterType;
    List<Profile> profileList;

    public boolean targetHelpsSequenceStarted;
    static final String PREF_START_TARGET_HELPS = "editor_profile_list_adapter_start_target_helps";
    static final String PREF_START_TARGET_HELPS_ORDER = "editor_profile_list_adapter_start_target_helps_order";

    EditorProfileListAdapter(EditorProfileListFragment f, DataWrapper pdw, int filterType)
    {
        fragment = f;
        dataWrapper = pdw;
        profileList = dataWrapper.getProfileList();
        this.filterType = filterType;
    }

    public void release()
    {
        fragment = null;
        profileList = null;
        dataWrapper = null;
    }

    public int getCount()
    {
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

    public Object getItem(int position)
    {
        if (getCount() == 0)
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

    public long getItemId(int position)
    {
        return position;
    }

    public int getItemId(Profile profile)
    {
        if (profileList == null)
            return -1;

        for (int i = 0; i < profileList.size(); i++)
        {
            if (profileList.get(i)._id == profile._id)
                return i;
        }
        return -1;
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

    /*
    public void deleteItem(Profile profile)
    {
        deleteItemNoNotify(profile);
        notifyDataSetChanged();
    }
    */

    void clearNoNotify()
    {
        dataWrapper.deleteAllProfiles();
    }

    public void clear()
    {
        clearNoNotify();
        notifyDataSetChanged();
    }

    void changeItemOrder(int from, int to)
    {
        if (profileList == null)
            return;

        // convert positions from adapter into profileList
        int plFrom = profileList.indexOf(getItem(from));
        int plTo = profileList.indexOf(getItem(to));

        Profile profile = profileList.get(plFrom);
        profileList.remove(plFrom);
        profileList.add(plTo, profile);
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
        int position = getItemId(profile);
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

    static class ViewHolder {
          RelativeLayout listItemRoot;
          ImageView profileIcon;
          TextView profileName;
          ImageView profileIndicator;
          ImageView profileItemEditMenu;
          int position;
        }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        View vi = convertView;
        if (convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
            if (filterType == EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR)
            {
                if (ApplicationPreferences.applicationEditorPrefIndicator(dataWrapper.context))
                    vi = inflater.inflate(R.layout.editor_profile_list_item, parent, false);
                else
                    vi = inflater.inflate(R.layout.editor_profile_list_item_no_indicator, parent, false);
            }
            else
            {
                if (ApplicationPreferences.applicationEditorPrefIndicator(dataWrapper.context))
                    vi = inflater.inflate(R.layout.editor_profile_list_item_no_order_handler, parent, false);
                else
                    vi = inflater.inflate(R.layout.editor_profile_list_item_no_indicator_no_order_handler, parent, false);
            }
            holder = new ViewHolder();
            holder.listItemRoot = (RelativeLayout)vi.findViewById(R.id.profile_list_item_root);
            holder.profileName = (TextView)vi.findViewById(R.id.profile_list_item_profile_name);
            holder.profileIcon = (ImageView)vi.findViewById(R.id.profile_list_item_profile_icon);
            holder.profileItemEditMenu = (ImageView)vi.findViewById(R.id.profile_list_item_edit_menu);
            if (ApplicationPreferences.applicationEditorPrefIndicator(dataWrapper.context))
                holder.profileIndicator = (ImageView)vi.findViewById(R.id.profile_list_profile_pref_indicator);
            vi.setTag(holder);        
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }
        
        final Profile profile = (Profile)getItem(position);
        if (profile != null)
        {

            /*
            switch (filterType)
            {
                case DatabaseHandler.FILTER_TYPE_PROFILES_ALL:
                    vi.setVisibility(View.VISIBLE);
                    break;
                case DatabaseHandler.FILTER_TYPE_PROFILES_SHOW_IN_ACTIVATOR:
                    if (!profile._showInActivator)
                        vi.setVisibility(View.GONE);
                    else
                        vi.setVisibility(View.VISIBLE);
                    break;
                case DatabaseHandler.FILTER_TYPE_PROFILES_NO_SHOW_IN_ACTIVATOR:
                    if (profile._showInActivator)
                        vi.setVisibility(View.GONE);
                    else
                        vi.setVisibility(View.VISIBLE);
                    break;
            }
            */

            if (profile._checked && (!ApplicationPreferences.applicationEditorHeader(dataWrapper.context)))
            {
                holder.profileName.setTypeface(null, Typeface.BOLD);
                holder.profileName.setTextSize(16);
                holder.profileName.setTextColor(GlobalGUIRoutines.getThemeAccentColor(fragment.getActivity()));
            }
            else
            {
                holder.profileName.setTypeface(null, Typeface.NORMAL);
                holder.profileName.setTextSize(15);
                holder.profileName.setTextColor(GlobalGUIRoutines.getThemeTextColor(fragment.getActivity()));
            }

            String profileName = dataWrapper.getProfileNameWithManualIndicator(profile,
                    profile._checked &&
                    (!ApplicationPreferences.applicationEditorHeader(dataWrapper.context)), true, false);

            if (profile._showInActivator)
                profileName = "[A] " + profileName;

            holder.profileName.setText(profileName);

            if (profile.getIsIconResourceID())
            {
                if (profile._iconBitmap != null)
                    holder.profileIcon.setImageBitmap(profile._iconBitmap);
                else {
                    //holder.profileIcon.setImageBitmap(null);
                    int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                            vi.getContext().getPackageName());
                    holder.profileIcon.setImageResource(res); // resource na ikonu
                }
            }
            else
            {
                holder.profileIcon.setImageBitmap(profile._iconBitmap);
            }

            if (ApplicationPreferences.applicationEditorPrefIndicator(dataWrapper.context))
            {
                //profilePrefIndicatorImageView.setImageBitmap(null);
                //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                if (holder.profileIndicator != null)
                    holder.profileIndicator.setImageBitmap(profile._preferencesIndicator);
            }

            holder.profileItemEditMenu.setTag(profile);
            final ImageView profileItemEditMenu = holder.profileItemEditMenu;
            holder.profileItemEditMenu.setOnClickListener(new OnClickListener() {

                    public void onClick(View v) {
                        fragment.showEditMenu(profileItemEditMenu);
                    }
                });

        }
      
        return vi;
    }

    void showTargetHelps(final Activity activity, final EditorProfileListFragment fragment, final View listItemView) {
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
                    public void onSequenceStep(TapTarget lastTarget) {
                        //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        targetHelpsSequenceStarted = false;
                    }
                });
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
                        public void onSequenceStep(TapTarget lastTarget) {
                            //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {
                            targetHelpsSequenceStarted = false;
                        }
                    });
                    targetHelpsSequenceStarted = true;
                    sequence.start();
                }
            }
        }

    }

}
