package sk.henrichg.phoneprofilesplus;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;

import static android.view.View.GONE;

public class ActivateProfileListFragment extends Fragment {

    DataWrapper activityDataWrapper;
    private ActivateProfileListAdapter profileListAdapter = null;
    private RelativeLayout activatedProfileHeader;
    private ListView listView = null;
    private GridView gridView = null;
    private TextView activeProfileName;
    private ImageView activeProfileIcon;
    TextView textViewNoData;
    private LinearLayout progressBar;
    //FrameLayout gridViewDivider = null;

    private WeakReference<LoadProfileListAsyncTask> asyncTaskContext;

    //private ValueAnimator hideAnimator;
    //private ValueAnimator showAnimator;
    //private int headerHeight;

    //private  static final String START_TARGET_HELPS_ARGUMENT = "start_target_helps";

    //public boolean targetHelpsSequenceStarted;
    public static final String PREF_START_TARGET_HELPS = "activate_profile_list_fragment_start_target_helps";
    public static final String PREF_START_TARGET_HELPS_FINISHED = "activate_profile_list_fragment_start_target_helps_finished";

    static final int PORDER_FOR_EMPTY_SPACE = 1000000;

    public ActivateProfileListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        //noinspection deprecation
        setRetainInstance(true);

        //noinspection ConstantConditions
        activityDataWrapper = new DataWrapper(getActivity().getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;

        boolean applicationActivatorGridLayout = ApplicationPreferences.applicationActivatorGridLayout;
        //boolean applicationActivatorPrefIndicator = ApplicationPreferences.applicationActivatorPrefIndicator;
        boolean applicationActivatorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator;
        //boolean applicationActivatorHeader = ApplicationPreferences.applicationActivatorHeader(activityDataWrapper.context);

        if (!applicationActivatorGridLayout)
        {
            if (applicationActivatorPrefIndicator/* && applicationActivatorHeader*/)
                rootView = inflater.inflate(R.layout.activate_profile_list, container, false);
            else
            //if (applicationActivatorHeader)
                rootView = inflater.inflate(R.layout.activate_profile_list_no_indicator, container, false);
            //else
            //    rootView = inflater.inflate(R.layout.activate_profile_list_no_header, container, false);
        }
        else
        {
            if (applicationActivatorPrefIndicator/* && applicationActivatorHeader*/)
                rootView = inflater.inflate(R.layout.activate_profile_grid, container, false);
            else
            //if (applicationActivatorHeader)
                rootView = inflater.inflate(R.layout.activate_profile_grid_no_indicator, container, false);
            //else
            //    rootView = inflater.inflate(R.layout.activate_profile_grid_no_header, container, false);
        }

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        doOnViewCreated(view/*, savedInstanceState*/);

        //boolean startTargetHelps = getArguments() != null && getArguments().getBoolean(START_TARGET_HELPS_ARGUMENT, false);
        //if (startTargetHelps)
            showTargetHelps();
    }

    private void doOnViewCreated(View view/*, Bundle savedInstanceState*/)
    {
        boolean applicationActivatorGridLayout = ApplicationPreferences.applicationActivatorGridLayout;

        activeProfileName = view.findViewById(R.id.act_prof_activated_profile_name);
        activeProfileIcon = view.findViewById(R.id.act_prof_activated_profile_icon);
        if (!applicationActivatorGridLayout)
            listView = view.findViewById(R.id.act_prof_profiles_list);
        else {
            gridView = view.findViewById(R.id.act_prof_profiles_grid);
            try {
                gridView.setNumColumns(Integer.parseInt(ApplicationPreferences.applicationActivatorNumColums));
            } catch (Exception e) {
                gridView.setNumColumns(3);
            }
            //gridViewDivider = view.findViewById(R.id.act_prof_profiles_grid_divider);
        }
        textViewNoData = view.findViewById(R.id.act_prof_list_empty);
        progressBar = view.findViewById(R.id.act_prof_list_linla_progress);

        AbsListView absListView;
        if (!applicationActivatorGridLayout)
            absListView = listView;
        else
            absListView = gridView;

        //absListView.setLongClickable(false);

        absListView.setOnItemClickListener((parent, view1, position, id) -> {

            if (!ApplicationPreferences.applicationLongClickActivation)
                activateProfile((Profile) profileListAdapter.getItem(position));
        });

        absListView.setOnItemLongClickListener((parent, view12, position, id) -> {

            if (ApplicationPreferences.applicationLongClickActivation)
                activateProfile((Profile)profileListAdapter.getItem(position));

            return false;
        });

        //absListView.setRemoveListener(onRemove);

        activatedProfileHeader = view.findViewById(R.id.act_prof_header);
        if (activatedProfileHeader != null) {
            /*@SuppressWarnings("ConstantConditions")
            Handler handler = new Handler(getActivity().getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null)
                        return;

                    headerHeight = activatedProfileHeader.getMeasuredHeight();
                    Log.e("ActivateProfileListFragment.doOnViewCreated", "headerHeight="+headerHeight);
                    hideAnimator = ValueAnimator.ofInt(headerHeight / 4, 0);
                    hideAnimator.setDuration(500);
                    hideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            int val = (Integer) valueAnimator.getAnimatedValue();
                            //Log.e("hideAnimator.onAnimationUpdate", "val="+val);
                            ViewGroup.LayoutParams layoutParams = activatedProfileHeader.getLayoutParams();
                            layoutParams.height = val * 4;
                            activatedProfileHeader.setLayoutParams(layoutParams);
                        }
                    });
                    showAnimator = ValueAnimator.ofInt(0, headerHeight / 4);
                    showAnimator.setDuration(500);
                    showAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            int val = (Integer) valueAnimator.getAnimatedValue();
                            //Log.e("showAnimator.onAnimationUpdate", "val="+val);
                            ViewGroup.LayoutParams layoutParams = activatedProfileHeader.getLayoutParams();
                            layoutParams.height = val * 4;
                            activatedProfileHeader.setLayoutParams(layoutParams);
                        }
                    });

                }
            }, 200);*/

            final LayoutTransition layoutTransition = ((ViewGroup) view.findViewById(R.id.layout_activator_list_fragment))
                    .getLayoutTransition();
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);

            absListView.setOnScrollListener(new HidingAbsListViewScrollListener() {
                @Override
                public void onHide() {
                    /*if ((activatedProfileHeader.getMeasuredHeight() >= headerHeight - 4) &&
                        (activatedProfileHeader.getMeasuredHeight() <= headerHeight + 4))
                        hideAnimator.start();*/
                    if (!layoutTransition.isRunning())
                        activatedProfileHeader.setVisibility(GONE);
                }
                @Override
                public void onShow() {
                    /*if (activatedProfileHeader.getMeasuredHeight() == 0)
                        showAnimator.start();*/
                    if (!layoutTransition.isRunning())
                        activatedProfileHeader.setVisibility(View.VISIBLE);
                }
            });
        }

        if (!activityDataWrapper.profileListFilled)
        {
            LoadProfileListAsyncTask asyncTask = new LoadProfileListAsyncTask(this);
            this.asyncTaskContext = new WeakReference<>(asyncTask );
            asyncTask.execute();
        }
        else
        {
            absListView.setAdapter(profileListAdapter);
            doOnStart();
        }

    }

    private static class LoadProfileListAsyncTask extends AsyncTask<Void, Void, Void> {

        private final WeakReference<ActivateProfileListFragment> fragmentWeakRef;
        private final DataWrapper dataWrapper;

        private final boolean applicationActivatorPrefIndicator;
        //private final boolean applicationActivatorHeader;
        private final boolean applicationActivatorGridLayout;
        private final GridView gridView;

        //private boolean someErrorProfiles = false;

        Handler progressBarHandler;
        Runnable progressBarRunnable;

        private static class ProfileComparator implements Comparator<Profile> {
            public int compare(Profile lhs, Profile rhs) {
                int res = 0;
                if ((lhs != null) && (rhs != null))
                    res = lhs._porder - rhs._porder;
                return res;
            }
        }

        private LoadProfileListAsyncTask (ActivateProfileListFragment fragment) {
            this.fragmentWeakRef = new WeakReference<>(fragment);
            //noinspection ConstantConditions
            this.dataWrapper = new DataWrapper(fragment.getActivity().getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);

            //applicationActivatorPrefIndicator = ApplicationPreferences.applicationActivatorPrefIndicator;
            applicationActivatorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator;
            //applicationActivatorHeader = ApplicationPreferences.applicationActivatorHeader(this.dataWrapper.context);
            applicationActivatorGridLayout = ApplicationPreferences.applicationActivatorGridLayout;
            gridView = fragment.gridView;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            final ActivateProfileListFragment fragment = this.fragmentWeakRef.get();

            if ((fragment != null) && (fragment.isAdded())) {
                progressBarHandler = new Handler(this.dataWrapper.context.getMainLooper());
                progressBarRunnable = () -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivateProfileListFragment.LoadProfileListAsyncTask (1)");

                    //fragment.textViewNoData.setVisibility(View.GONE);
                    fragment.progressBar.setVisibility(View.VISIBLE);
                };
                progressBarHandler.postDelayed(progressBarRunnable, 100);
                //fragment.progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            this.dataWrapper.fillProfileList(true, applicationActivatorPrefIndicator);

//            for (Profile profile : this.dataWrapper.profileList) {
//                if (ProfilesPrefsFragment.isRedTextNotificationRequired(profile,this.dataWrapper.context)) {
//                    someErrorProfiles = true;
//                    break;
//                }
//            }

//            if (!someErrorProfiles) {
            if (ApplicationPreferences.applicationActivatorAddRestartEventsIntoProfileList) {
                if (Event.getGlobalEventsRunning()) {
                    Profile restartEvents = DataWrapper.getNonInitializedProfile(dataWrapper.context.getString(R.string.menu_restart_events), "ic_list_item_events_restart_color_filled|1|0|0", 0);
                    restartEvents._showInActivator = true;
                    restartEvents._id = Profile.RESTART_EVENTS_PROFILE_ID;
                    dataWrapper.profileList.add(0, restartEvents);
                }
            }

            if (applicationActivatorGridLayout) {
                int count = 0;
                for (Profile profile : this.dataWrapper.profileList) {
                    if (profile._showInActivator)
                        ++count;
                }

                int numColumns = gridView.getNumColumns();

                int modulo = count % numColumns;
                if (modulo > 0) {
                    for (int i = 0; i < numColumns - modulo; i++) {
                        Profile profile = DataWrapper.getNonInitializedProfile(
                                dataWrapper.context.getString(R.string.profile_name_default),
                                Profile.PROFILE_ICON_DEFAULT, PORDER_FOR_EMPTY_SPACE);
                        profile._showInActivator = true;
                        this.dataWrapper.profileList.add(profile);
                    }
                }
            }

            //noinspection Java8ListSort
            Collections.sort(this.dataWrapper.profileList, new ProfileComparator());

            dataWrapper.getEventTimelineList(true);

            //}
            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);
            
            final ActivateProfileListFragment fragment = this.fragmentWeakRef.get();
            
            if ((fragment != null) && (fragment.isAdded())) {
                progressBarHandler.removeCallbacks(progressBarRunnable);
                fragment.progressBar.setVisibility(View.GONE);

                // get local profileList
                this.dataWrapper.fillProfileList(true, applicationActivatorPrefIndicator);
                // set copy local profile list into activity profilesDataWrapper
                fragment.activityDataWrapper.copyProfileList(this.dataWrapper);

                // get local eventTimelineList
                this.dataWrapper.fillEventTimelineList();
                // set copy local event timeline list into activity profilesDataWrapper
                fragment.activityDataWrapper.copyEventTimelineList(this.dataWrapper);

                synchronized (fragment.activityDataWrapper.profileList) {
                    if (fragment.activityDataWrapper.profileList.size() == 0) {
                        fragment.textViewNoData.setVisibility(View.VISIBLE);

                        // no profile in list, start Editor

                        //noinspection ConstantConditions
                        Intent intent = new Intent(fragment.getActivity().getBaseContext(), EditorProfilesActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_ACTIVATOR_START);
                        fragment.getActivity().startActivity(intent);

                        try {
                            fragment.getActivity().finish();
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }

                        return;
                    }
//                    else {
//                        if (someErrorProfiles) {
//                            // some profiles has errors
//
//                            //noinspection ConstantConditions
//                            Intent intent = new Intent(fragment.getActivity().getBaseContext(), EditorProfilesActivity.class);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_EDITOR_SHOW_IN_ACTIVATOR_FILTER);
//                            //noinspection ConstantConditions
//                            fragment.getActivity().startActivity(intent);
//
//                            try {
//                                fragment.getActivity().finish();
//                            } catch (Exception e) {
//                                PPApplication.recordException(e);
//                            }
//
//                            return;
//                        }
//                    }
                }

                fragment.profileListAdapter = new ActivateProfileListAdapter(fragment, /*fragment.profileList, */fragment.activityDataWrapper);

                AbsListView absListView;
                if (!applicationActivatorGridLayout)
                    absListView = fragment.listView;
                else
                    absListView = fragment.gridView;
                absListView.setAdapter(fragment.profileListAdapter);

                fragment.doOnStart();

                //noinspection ConstantConditions
                final Handler handler = new Handler(fragment.getActivity().getMainLooper());
                handler.postDelayed(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivateProfileListFragment.LoadProfileListAsyncTask (2)");

                    if (fragment.getActivity() != null) {
                        if (!fragment.getActivity().isFinishing())
                            ((ActivateProfileActivity) fragment.getActivity()).startTargetHelpsActivity();
                    }
                }, 500);

            }
        }
    }

    private boolean isAsyncTaskPendingOrRunning() {
        try {
            return this.asyncTaskContext != null &&
                    this.asyncTaskContext.get() != null &&
                    !this.asyncTaskContext.get().getStatus().equals(AsyncTask.Status.FINISHED);
        } catch (Exception e) {
            return false;
        }
    }

    private void doOnStart()
    {
        //long nanoTimeStart = PPApplication.startMeasuringRunTime();

        //Profile profile = activityDataWrapper.getActivatedProfile(true, ApplicationPreferences.applicationActivatorPrefIndicator);
        Profile profile = activityDataWrapper.getActivatedProfile(true, ApplicationPreferences.applicationEditorPrefIndicator);

        updateHeader(profile);
        if (profileListAdapter != null)
            profileListAdapter.notifyDataSetChanged(false);
        //setProfileSelection(profile, false);

        //PPApplication.getMeasuredRunTime(nanoTimeStart, "ActivateProfileActivity.onStart");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (isAsyncTaskPendingOrRunning()) {
            this.asyncTaskContext.get().cancel(true);
        }

        AbsListView absListView;
        if (!ApplicationPreferences.applicationActivatorGridLayout)
            absListView = listView;
        else
            absListView = gridView;
        if (absListView != null)
            absListView.setAdapter(null);
        if (profileListAdapter != null)
            profileListAdapter.release();

        //if (activityDataWrapper != null)
        //    activityDataWrapper.invalidateDataWrapper();
        //activityDataWrapper = null;
    }

    private void updateHeader(Profile profile)
    {
        //if (!ApplicationPreferences.applicationActivatorHeader(activityDataWrapper.context))
        //    return;

        if (activeProfileName == null)
            // Activator opened from recent app list and setting for show header is changed
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
            activatedProfileHeader.setTag(DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, false, activityDataWrapper));

            activeProfileName.setText(DataWrapper.getProfileNameWithManualIndicator(profile, true, "", true, false, false, activityDataWrapper));
            if (profile.getIsIconResourceID())
            {
                if (profile._iconBitmap != null)
                    activeProfileIcon.setImageBitmap(profile._iconBitmap);
                else {
                    //int res = getResources().getIdentifier(profile.getIconIdentifier(), "drawable", getActivity().PPApplication.PACKAGE_NAME);
                    int res = Profile.getIconResource(profile.getIconIdentifier());
                    activeProfileIcon.setImageResource(res); // icon resource
                }
            }
            else
            {
                activeProfileIcon.setImageBitmap(profile._iconBitmap);
            }
        }

        //if (ApplicationPreferences.applicationActivatorPrefIndicator)
        if (ApplicationPreferences.applicationEditorPrefIndicator)
        {
            //noinspection ConstantConditions
            ImageView profilePrefIndicatorImageView = getActivity().findViewById(R.id.act_prof_activated_profile_pref_indicator);
            if (profilePrefIndicatorImageView != null)
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
        }

        String newDisplayedText = (String)activatedProfileHeader.getTag();
        if (!newDisplayedText.equals(oldDisplayedText))
            activatedProfileHeader.setVisibility(View.VISIBLE);
    }

    private void activateProfile(Profile profile)
    {
        if ((activityDataWrapper == null) || (profile == null))
            return;

        if (profile._porder != PORDER_FOR_EMPTY_SPACE) {
            if (profile._id == Profile.RESTART_EVENTS_PROFILE_ID) {
                activityDataWrapper.restartEventsWithAlert(getActivity());
            }
            else
            if (!ProfilesPrefsFragment.isRedTextNotificationRequired(profile, activityDataWrapper.context)) {
                PPApplication.showToastForProfileActivation = true;
                activityDataWrapper.activateProfile(profile._id, PPApplication.STARTUP_SOURCE_ACTIVATOR, getActivity(), false);
            }
            else
                EditorProfilesActivity.showDialogAboutRedText(profile, null, true, true, false, false, getActivity());
        }
    }

    void refreshGUI(/*final boolean refresh,*/ final boolean refreshIcons)
    {
        if ((activityDataWrapper == null) || (profileListAdapter == null))
            return;

        ActivateProfileListFragment.RefreshGUIAsyncTask asyncTask = new ActivateProfileListFragment.RefreshGUIAsyncTask(refreshIcons, this, activityDataWrapper);
        asyncTask.execute();

/*
        new AsyncTask<Void, Integer, Void>() {

            Profile profileFromDB;
            Profile profileFromDataWrapper;

            //boolean doNotRefresh = false;

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
//
//                    if (!refresh) {
//                        String pNameHeader = PPApplication.prefActivityProfileName1;
//                        //if (PPApplication.logEnabled()) {
//                        //    PPApplication.logE("ActivateProfileListFragment.refreshGUI", "pNameHeader=" + pNameHeader);
//                        //    PPApplication.logE("ActivateProfileListFragment.refreshGUI", "pName=" + pName);
//                        //}
//
//                        if ((!pNameHeader.isEmpty()) && pName.equals(pNameHeader)) {
//                            //PPApplication.logE("ActivateProfileListFragment.refreshGUI", "activated profile NOT changed");
//                            doNotRefresh = true;
//                            return null;
//                        }
//                    }
//
//                    PPApplication.setActivityProfileName(activityDataWrapper.context, 1, pName);

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
                    //if (!doNotRefresh) {
                        ((ActivateProfileActivity) getActivity()).setEventsRunStopIndicator();

                        Profile profileFromAdapter = profileListAdapter.getActivatedProfile();
                        if (profileFromAdapter != null)
                            profileFromAdapter._checked = false;

                        if (profileFromDB != null) {
                            if (profileFromDataWrapper != null)
                                profileFromDataWrapper._checked = true;
                            updateHeader(profileFromDataWrapper);
                            //setProfileSelection(profileFromDataWrapper, refreshIcons);
                        } else {
                            updateHeader(null);
                            //setProfileSelection(null, refreshIcons);
                        }

                        profileListAdapter.notifyDataSetChanged(refreshIcons);
                    //}
                }
            }

        }.execute();
 */
    }

    void showTargetHelps() {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        if (getActivity() == null)
            return;

        //if (((ActivateProfileActivity)getActivity()).targetHelpsSequenceStarted)
        //    return;

        boolean startTargetHelpsFinished = ApplicationPreferences.prefActivatorActivityStartTargetHelpsFinished;
        if (!startTargetHelpsFinished)
            return;


        boolean showTargetHelps = ApplicationPreferences.prefActivatorFragmentStartTargetHelps;

        if (showTargetHelps ||
                ApplicationPreferences.prefActivatorAdapterStartTargetHelps) {

            //Log.d("ActivateProfileListFragment.showTargetHelps", "PREF_START_TARGET_HELPS_ORDER=true");

            if (showTargetHelps) {

                //Log.d("ActivateProfileListFragment.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activityDataWrapper.context);
                editor.putBoolean(PREF_START_TARGET_HELPS, false);
                editor.putBoolean(PREF_START_TARGET_HELPS_FINISHED, true);
                editor.apply();
                ApplicationPreferences.prefActivatorFragmentStartTargetHelps = false;
                ApplicationPreferences.prefActivatorFragmentStartTargetHelpsFinished = true;

                showAdapterTargetHelps();
            }
            else {
                //Log.d("ActivateProfileListFragment.showTargetHelps", "PREF_START_TARGET_HELPS=false");
                final Handler handler = new Handler(getActivity().getMainLooper());
                handler.postDelayed(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivateProfileListFragment.showTargetHelps (1)");
                    //noinspection Convert2MethodRef
                    showAdapterTargetHelps();
                }, 500);
            }
        }
        else {
            final Handler handler = new Handler(getActivity().getMainLooper());
            handler.postDelayed(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivateProfileListFragment.showTargetHelps (2)");

                if (ActivatorTargetHelpsActivity.activity != null) {
                    //Log.d("ActivateProfileListFragment.showTargetHelps", "finish activity");
                    try {
                        ActivatorTargetHelpsActivity.activity.finish();
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                    ActivatorTargetHelpsActivity.activity = null;
                    //ActivatorTargetHelpsActivity.activatorActivity = null;
                }
            }, 500);
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
        if (!ApplicationPreferences.applicationActivatorGridLayout) {
            if (listView.getChildCount() > 1)
                itemView = listView.getChildAt(1);
            else
                itemView = listView.getChildAt(0);
        }
        else {
            if (gridView.getChildCount() > 1)
                itemView = gridView.getChildAt(1);
            else
                itemView = gridView.getChildAt(0);
        }
        //Log.d("ActivateProfileListFragment.showAdapterTargetHelps", "profileListAdapter="+profileListAdapter);
        //Log.d("ActivateProfileListFragment.showAdapterTargetHelps", "itemView="+itemView);
        if ((profileListAdapter != null) && (itemView != null))
            profileListAdapter.showTargetHelps(getActivity(), /*this,*/ itemView);
        else {
            final Handler handler = new Handler(getActivity().getMainLooper());
            handler.postDelayed(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivateProfileListFragment.showAdapterTargetHelps");

                if (ActivatorTargetHelpsActivity.activity != null) {
                    //Log.d("ActivateProfileListFragment.showAdapterTargetHelps", "finish activity");
                    try {
                        ActivatorTargetHelpsActivity.activity.finish();
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                    ActivatorTargetHelpsActivity.activity = null;
                    //ActivatorTargetHelpsActivity.activatorActivity = null;
                }
            }, 500);
        }
    }

    private static class RefreshGUIAsyncTask extends AsyncTask<Void, Integer, Void> {

        Profile profileFromDB;
        Profile profileFromDataWrapper;

        //boolean doNotRefresh = false;

        private final WeakReference<ActivateProfileListFragment> fragmentWeakRef;
        final DataWrapper dataWrapper;
        private final boolean refreshIcons;

        public RefreshGUIAsyncTask(final boolean refreshIcons,
                                   final ActivateProfileListFragment fragment,
                                   final DataWrapper dataWrapper) {
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.dataWrapper = dataWrapper.copyDataWrapper();
            this.refreshIcons = refreshIcons;
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

                    if (!refresh) {
                        String pNameHeader = PPApplication.prefActivityProfileName1;
                        //if (PPApplication.logEnabled()) {
                        //    PPApplication.logE("ActivateProfileListFragment.refreshGUI", "pNameHeader=" + pNameHeader);
                        //    PPApplication.logE("ActivateProfileListFragment.refreshGUI", "pName=" + pName);
                        //}

                        if ((!pNameHeader.isEmpty()) && pName.equals(pNameHeader)) {
                            //PPApplication.logE("ActivateProfileListFragment.refreshGUI", "activated profile NOT changed");
                            doNotRefresh = true;
                            return null;
                        }
                    }

                    PPApplication.setActivityProfileName(activityDataWrapper.context, 1, pName);
                    */
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
            ActivateProfileListFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if ((fragment.getActivity() != null) && (!fragment.getActivity().isFinishing())) {
                    //if (!doNotRefresh) {
                    ((ActivateProfileActivity) fragment.getActivity()).setEventsRunStopIndicator();

                    Profile profileFromAdapter = fragment.profileListAdapter.getActivatedProfile();
                    if (profileFromAdapter != null)
                        profileFromAdapter._checked = false;

                    if (profileFromDB != null) {
                        if (profileFromDataWrapper != null)
                            profileFromDataWrapper._checked = true;
                        fragment.updateHeader(profileFromDataWrapper);
                        //setProfileSelection(profileFromDataWrapper, refreshIcons);
                    } else {
                        fragment.updateHeader(null);
                        //setProfileSelection(null, refreshIcons);
                    }

                    fragment.profileListAdapter.notifyDataSetChanged(refreshIcons);
                    //}
                }
            }
        }

    }

}
