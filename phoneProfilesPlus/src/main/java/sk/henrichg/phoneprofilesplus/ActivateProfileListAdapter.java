package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.List;

class ActivateProfileListAdapter extends BaseAdapter
{

    private List<Profile> profileList;
    private ActivateProfileListFragment fragment;
    private DataWrapper dataWrapper;

    //public boolean targetHelpsSequenceStarted;
    static final String PREF_START_TARGET_HELPS = "activate_profile_list_adapter_start_target_helps";

    ActivateProfileListAdapter(ActivateProfileListFragment f, List<Profile> pl, DataWrapper dataWrapper)
    {
        fragment = f;
        profileList = pl;
        this.dataWrapper = dataWrapper;
    }

    public void release()
    {
        fragment = null;
        profileList = null;
    }

    public int getCount()
    {
        int count = 0;
        for (Profile profile : profileList)
        {
            if (profile._showInActivator)
                ++count;
        }
        return count;
    }

    public Object getItem(int position)
    {
        if (getCount() == 0)
            return null;
        else
        {
            Profile _profile = null;

            int pos = -1;
            for (Profile profile : profileList)
            {
                if (profile._showInActivator)
                    ++pos;

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
            ++pos;
            if (profileList.get(i)._id == profile._id)
                return pos;
        }
        return -1;
    }

    public Profile getActivatedProfile()
    {
        for (Profile p : profileList)
        {
            if (p._checked)
            {
                return p;
            }
        }

        return null;
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
          ViewGroup listItemRoot;
          ImageView profileIcon;
          TextView profileName;
          ImageView profileIndicator;
          int position;
        }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        View vi = convertView;
        if (convertView == null)
        {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
            if (!ApplicationPreferences.applicationActivatorGridLayout(fragment.getActivity()))
            {
                if (ApplicationPreferences.applicationActivatorPrefIndicator(fragment.getActivity()))
                    vi = inflater.inflate(R.layout.activate_profile_list_item, parent, false);
                else
                    vi = inflater.inflate(R.layout.activate_profile_list_item_no_indicator, parent, false);
                holder.listItemRoot = (RelativeLayout)vi.findViewById(R.id.act_prof_list_item_root);
                holder.profileName = (TextView) vi.findViewById(R.id.act_prof_list_item_profile_name);
                holder.profileIcon = (ImageView)vi.findViewById(R.id.act_prof_list_item_profile_icon);
                if (ApplicationPreferences.applicationActivatorPrefIndicator(fragment.getActivity()))
                    holder.profileIndicator = (ImageView)vi.findViewById(R.id.act_prof_list_profile_pref_indicator);
            }
            else
            {
                vi = inflater.inflate(R.layout.activate_profile_grid_item, parent, false);
                holder.listItemRoot = (LinearLayout)vi.findViewById(R.id.act_prof_list_item_root);
                holder.profileName = (TextView) vi.findViewById(R.id.act_prof_list_item_profile_name);
                holder.profileIcon = (ImageView)vi.findViewById(R.id.act_prof_list_item_profile_icon);
            }
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        final Profile profile = (Profile)getItem(position);

        if ((ApplicationPreferences.applicationActivatorGridLayout(fragment.getActivity())) &&
                (profile._porder == ActivateProfileListFragment.PORDER_FOR_IGNORED_PROFILE)) {
            holder.profileName.setText(R.string.empty_string);
            holder.profileIcon.setImageResource(R.drawable.ic_empty);
        }
        else {
            if (profile._checked && (!ApplicationPreferences.applicationActivatorHeader(fragment.getActivity()))) {
                holder.profileName.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
                if (ApplicationPreferences.applicationActivatorGridLayout(fragment.getActivity()))
                    holder.profileName.setTextSize(14);
                else
                    holder.profileName.setTextSize(16);
                holder.profileName.setTextColor(GlobalGUIRoutines.getThemeAccentColor(fragment.getActivity()));
            } else {
                holder.profileName.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
                if (ApplicationPreferences.applicationActivatorGridLayout(fragment.getActivity()))
                    holder.profileName.setTextSize(13);
                else
                    holder.profileName.setTextSize(15);
                holder.profileName.setTextColor(GlobalGUIRoutines.getThemeTextColor(fragment.getActivity()));
            }

            String profileName = dataWrapper.getProfileNameWithManualIndicator(profile,
                    (!ApplicationPreferences.applicationActivatorGridLayout(fragment.getActivity())) &&
                            profile._checked &&
                            (!ApplicationPreferences.applicationActivatorHeader(fragment.getActivity())), true,
                    ApplicationPreferences.applicationActivatorGridLayout(fragment.getActivity()));
            holder.profileName.setText(profileName);


            if (profile.getIsIconResourceID()) {
                if (profile._iconBitmap != null)
                    holder.profileIcon.setImageBitmap(profile._iconBitmap);
                else {
                    //holder.profileIcon.setImageBitmap(null);
                    int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                            vi.getContext().getPackageName());
                    holder.profileIcon.setImageResource(res); // resource na ikonu
                }
            } else {
                holder.profileIcon.setImageBitmap(profile._iconBitmap);
            }

            if ((ApplicationPreferences.applicationActivatorPrefIndicator(fragment.getActivity())) && (!ApplicationPreferences.applicationActivatorGridLayout(fragment.getActivity()))) {
                //profilePrefIndicatorImageView.setImageBitmap(null);
                //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                holder.profileIndicator.setImageBitmap(profile._preferencesIndicator);
            }
        }

        return vi;
    }

    void showTargetHelps(final Activity activity, final ActivateProfileListFragment fragment, final View listItemView) {
        if (fragment.targetHelpsSequenceStarted)
            return;

        ApplicationPreferences.getSharedPreferences(activity);

        if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true)) {

            //Log.d("ActivateProfileListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS=true");

            SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
            editor.putBoolean(PREF_START_TARGET_HELPS, false);
            editor.apply();

            Rect profileItemTarget = new Rect(0, 0, listItemView.getHeight(), listItemView.getHeight());
            int[] screenLocation = new int[2];
            listItemView.getLocationOnScreen(screenLocation);
            profileItemTarget.offset(screenLocation[0] + listItemView.getWidth() / 2 - listItemView.getHeight() / 2, screenLocation[1]);

            final TapTargetSequence sequence = new TapTargetSequence(ActivatorTargetHelpsActivity.activity);

            sequence.targets(
                    TapTarget.forBounds(profileItemTarget, activity.getString(R.string.activator_activity_targetHelps_activateProfile_title), activity.getString(R.string.activator_activity_targetHelps_activateProfile_description))
                            .transparentTarget(true)
                            .textColorInt(0xFFFFFF)
                            .drawShadow(true)
                            .id(1)
            );
            sequence.listener(new TapTargetSequence.Listener() {
                // This listener will tell us when interesting(tm) events happen in regards
                // to the sequence
                @Override
                public void onSequenceFinish() {
                    //targetHelpsSequenceStarted = false;
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (ActivatorTargetHelpsActivity.activity != null) {
                                //Log.d("ActivateProfileListAdapter.showTargetHelps", "finish activity");
                                ActivatorTargetHelpsActivity.activity.finish();
                                ActivatorTargetHelpsActivity.activity = null;
                            }
                        }
                    }, 500);
                }

                @Override
                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                    //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                }

                @Override
                public void onSequenceCanceled(TapTarget lastTarget) {
                    //targetHelpsSequenceStarted = false;
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (ActivatorTargetHelpsActivity.activity != null) {
                                //Log.d("ActivateProfileListAdapter.showTargetHelps", "finish activity");
                                ActivatorTargetHelpsActivity.activity.finish();
                                ActivatorTargetHelpsActivity.activity = null;
                            }
                        }
                    }, 500);
                }
            });
            //targetHelpsSequenceStarted = true;
            sequence.start();
        }
        else {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (ActivatorTargetHelpsActivity.activity != null) {
                        //Log.d("ActivateProfileListAdapter.showTargetHelps", "finish activity");
                        ActivatorTargetHelpsActivity.activity.finish();
                        ActivatorTargetHelpsActivity.activity = null;
                    }
                }
            }, 500);
        }
    }

}
