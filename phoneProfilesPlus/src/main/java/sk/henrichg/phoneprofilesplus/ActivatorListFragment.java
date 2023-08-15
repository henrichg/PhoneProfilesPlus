package sk.henrichg.phoneprofilesplus;

import static android.view.View.GONE;

import android.animation.LayoutTransition;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;
import java.util.Comparator;

public class ActivatorListFragment extends Fragment {

    DataWrapper activityDataWrapper;
    private ActivatorListAdapter profileListAdapter = null;
    private LinearLayout activatedProfileHeader;
    private ListView listView = null;
    private GridView gridView = null;
    private TextView activeProfileName;
    private ImageView activeProfileIcon;
    TextView textViewNoData;
    private LinearLayout progressBar;
    //FrameLayout gridViewDivider = null;

    private LoadProfileListAsyncTask loadAsyncTask = null;

    //private ValueAnimator hideAnimator;
    //private ValueAnimator showAnimator;
    //private int headerHeight;

    //private  static final String START_TARGET_HELPS_ARGUMENT = "start_target_helps";

    //public boolean targetHelpsSequenceStarted;
    public static final String PREF_START_TARGET_HELPS = "activate_profile_list_fragment_start_target_helps";
    public static final String PREF_START_TARGET_HELPS_FINISHED = "activate_profile_list_fragment_start_target_helps_finished";

    static final int PORDER_FOR_EMPTY_SPACE = 1000000;

    public ActivatorListFragment() {
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

        loadAsyncTask = new LoadProfileListAsyncTask(this);
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
                rootView = inflater.inflate(R.layout.fragment_activator_list, container, false);
            else
            //if (applicationActivatorHeader)
                rootView = inflater.inflate(R.layout.fragment_activator_list_no_indicator, container, false);
            //else
            //    rootView = inflater.inflate(R.layout.activate_profile_list_no_header, container, false);
        }
        else
        {
            if (applicationActivatorPrefIndicator/* && applicationActivatorHeader*/)
                rootView = inflater.inflate(R.layout.fragment_activator_grid, container, false);
            else
            //if (applicationActivatorHeader)
                rootView = inflater.inflate(R.layout.fragment_activator_grid_no_indicator, container, false);
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
            //Log.e("ActivatorListFragment.onViewCreated", "showTargetHelps");
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
            /* Handler handler = new Handler(getActivity().getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null)
                        return;

                    headerHeight = activatedProfileHeader.getMeasuredHeight();
                    hideAnimator = ValueAnimator.ofInt(headerHeight / 4, 0);
                    hideAnimator.setDuration(500);
                    hideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            int val = (Integer) valueAnimator.getAnimatedValue();
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
            if (!isAsyncTaskRunning())
                loadAsyncTask.execute();
        }
        else
        {
            absListView.setAdapter(profileListAdapter);
            doOnStart();
        }

    }

    private static class LoadProfileListAsyncTask extends AsyncTask<Void, Void, Void> {

        private final WeakReference<ActivatorListFragment> fragmentWeakRef;
        private final DataWrapper dataWrapper;

        private final boolean applicationActivatorPrefIndicator;
        //private final boolean applicationActivatorHeader;
        private final boolean applicationActivatorGridLayout;
        //private final GridView gridView;

        //private boolean someErrorProfiles = false;

        private boolean globalEventsRunning;

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

        private LoadProfileListAsyncTask (ActivatorListFragment fragment) {
            this.fragmentWeakRef = new WeakReference<>(fragment);
            //noinspection ConstantConditions
            this.dataWrapper = new DataWrapper(fragment.getActivity().getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);

            //applicationActivatorPrefIndicator = ApplicationPreferences.applicationActivatorPrefIndicator;
            applicationActivatorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator;
            //applicationActivatorHeader = ApplicationPreferences.applicationActivatorHeader(this.dataWrapper.context);
            applicationActivatorGridLayout = ApplicationPreferences.applicationActivatorGridLayout;
            //gridView = fragment.gridView;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            globalEventsRunning = EventStatic.getGlobalEventsRunning(dataWrapper.context);

            final ActivatorListFragment fragment = this.fragmentWeakRef.get();

            if ((fragment != null) && (fragment.isAdded())) {
                progressBarHandler = new Handler(this.dataWrapper.context.getMainLooper());
                progressBarRunnable = () -> {
//                        PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivatorListFragment.LoadProfileListAsyncTask (1)");

                    //fragment.textViewNoData.setVisibility(View.GONE);
                    fragment.progressBar.setVisibility(View.VISIBLE);
                };
                progressBarHandler.postDelayed(progressBarRunnable, 100);
                //fragment.progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            //this.dataWrapper.fillProfileList(true, applicationActivatorPrefIndicator);

            this.dataWrapper.fillProfileList(false, false);
            for (Profile profile : this.dataWrapper.profileList) {
                if (profile._showInActivator) {
                    this.dataWrapper.generateProfileIcon(profile, true, applicationActivatorPrefIndicator);
                }
            }

//            if (!someErrorProfiles) {
            if (ApplicationPreferences.applicationActivatorAddRestartEventsIntoProfileList) {
                if (globalEventsRunning) {
                    //Profile restartEvents = DataWrapper.getNonInitializedProfile(dataWrapper.context.getString(R.string.menu_restart_events), "ic_profile_restart_events|1|0|0", 0);
                    Profile restartEvents = DataWrapperStatic.getNonInitializedProfile(dataWrapper.context.getString(R.string.menu_restart_events),
                            StringConstants.PROFILE_ICON_RESTART_EVENTS+"|1|1|"+ApplicationPreferences.applicationRestartEventsIconColor, 0);
                    restartEvents._showInActivator = true;
                    restartEvents._id = Profile.RESTART_EVENTS_PROFILE_ID;
                    dataWrapper.generateProfileIcon(restartEvents, true, false);
                    dataWrapper.profileList.add(0, restartEvents);
                }
            }

            if (applicationActivatorGridLayout) {
                final ActivatorListFragment fragment = this.fragmentWeakRef.get();
                if ((fragment != null) && (fragment.isAdded())) {

                    int count = 0;
                    for (Profile profile : this.dataWrapper.profileList) {
                        if (profile._showInActivator)
                            ++count;
                    }

                    int numColumns = fragment.gridView.getNumColumns();

                    int modulo = count % numColumns;
                    if (modulo > 0) {
                        int size = numColumns - modulo;
                        for (int i = 0; i < size; i++) {
                            Profile profile = DataWrapperStatic.getNonInitializedProfile(
                                    dataWrapper.context.getString(R.string.profile_name_default),
                                    StringConstants.PROFILE_ICON_DEFAULT, PORDER_FOR_EMPTY_SPACE);
                            profile._showInActivator = true;
                            this.dataWrapper.profileList.add(profile);
                        }
                    }
                }
            }

            this.dataWrapper.profileList.sort(new ProfileComparator());

            dataWrapper.getEventTimelineList(true);

            //}
            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);
            
            final ActivatorListFragment fragment = this.fragmentWeakRef.get();

            if ((fragment != null) && (fragment.isAdded())) {
                if ((fragment.getActivity() != null) && (!fragment.getActivity().isFinishing())) {

                    progressBarHandler.removeCallbacks(progressBarRunnable);
                    fragment.progressBar.setVisibility(View.GONE);

                    // get local profileList
                    //this.dataWrapper.fillProfileList(true, applicationActivatorPrefIndicator);
                    // set copy local profile list into activity profilesDataWrapper
                    fragment.activityDataWrapper.copyProfileList(this.dataWrapper);

                    // get local eventTimelineList
                    //this.dataWrapper.fillEventTimelineList();
                    // set copy local event timeline list into activity profilesDataWrapper
                    fragment.activityDataWrapper.copyEventTimelineList(this.dataWrapper);

                    this.dataWrapper.clearProfileList();
                    this.dataWrapper.clearEventTimelineList();

                    synchronized (fragment.activityDataWrapper.profileList) {
                        if (fragment.activityDataWrapper.profileList.size() == 0) {
                            fragment.textViewNoData.setVisibility(View.VISIBLE);

                            // no profile in list, start Editor

//                            Log.e("ActivatorListFragment.LoadProfileListAsyncTask", "start Editor");
                            /*Intent intent = new Intent(fragment.getActivity().getBaseContext(), EditorActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_ACTIVATOR_START);
                            fragment.getActivity().startActivity(intent);*/

                            if (Build.VERSION.SDK_INT < 33) {
                                // Activator must be displayed for grant notification permission
                                if (((ActivatorActivity) fragment.getActivity()).firstStartOfPPP) {
                                    try {
                                        fragment.getActivity().finish();
                                    } catch (Exception e) {
                                        PPApplicationStatic.recordException(e);
                                    }
                                    return;
                                }
                            }
                        }
//                    else {
//                        if (someErrorProfiles) {
//                            // some profiles has errors
//
//                            Intent intent = new Intent(fragment.getActivity().getBaseContext(), EditorActivity.class);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_EDITOR_SHOW_IN_ACTIVATOR_FILTER);
//                            fragment.getActivity().startActivity(intent);
//
//                            try {
//                                fragment.getActivity().finish();
//                            } catch (Exception e) {
//                                PPApplicationStatic.recordException(e);
//                            }
//
//                            return;
//                        }
//                    }
                    }

                    fragment.profileListAdapter = new ActivatorListAdapter(fragment, /*fragment.profileList, */fragment.activityDataWrapper);

                    AbsListView absListView;
                    if (!applicationActivatorGridLayout)
                        absListView = fragment.listView;
                    else
                        absListView = fragment.gridView;
                    absListView.setAdapter(fragment.profileListAdapter);

                    fragment.doOnStart();

                    final Handler handler = new Handler(fragment.getActivity().getMainLooper());
                    handler.postDelayed(() -> {
//                        PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivatorListFragment.LoadProfileListAsyncTask (2)");

                        if (fragment.getActivity() != null) {
                            if (!fragment.getActivity().isFinishing())
                                ((ActivatorActivity) fragment.getActivity()).startTargetHelpsActivity();
                        }
                    }, 500);

                }

            }
        }
    }

    private boolean isAsyncTaskRunning() {
        try {
            //Log.e("ActivatorListFragment.isAsyncTaskRunning", "loadAsyncTask="+loadAsyncTask);
            //Log.e("ActivatorListFragment.isAsyncTaskRunning", "loadAsyncTask.getStatus()="+loadAsyncTask.getStatus());

            return (loadAsyncTask != null) &&
                    loadAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING);
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

        //PPApplication.getMeasuredRunTime(nanoTimeStart, "ActivatorActivity.onStart");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //Log.e("ActivatorListFragment.onDestroy", "xxx");

        if (isAsyncTaskRunning()) {
            //Log.e("ActivatorListFragment.onDestroy", "AsyncTask not finished");
            loadAsyncTask.cancel(true);
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

        if (activityDataWrapper != null)
            activityDataWrapper.invalidateDataWrapper();
        activityDataWrapper = null;
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
            activatedProfileHeader.setTag(DataWrapperStatic.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, false, activityDataWrapper));

            activeProfileName.setText(DataWrapperStatic.getProfileNameWithManualIndicator(profile, true, "", true, false, false, activityDataWrapper));
            if (profile.getIsIconResourceID())
            {
                Bitmap bitmap = profile.increaseProfileIconBrightnessForActivity(getActivity(), profile._iconBitmap);
                if (bitmap != null)
                    activeProfileIcon.setImageBitmap(bitmap);
                else {
                    if (profile._iconBitmap != null)
                        activeProfileIcon.setImageBitmap(profile._iconBitmap);
                    else {
                        //int res = getResources().getIdentifier(profile.getIconIdentifier(), "drawable", getActivity().PPApplication.PACKAGE_NAME);
                        int res = ProfileStatic.getIconResource(profile.getIconIdentifier());
                        activeProfileIcon.setImageResource(res); // icon resource
                    }
                }
            }
            else
            {
                Bitmap bitmap = profile.increaseProfileIconBrightnessForActivity(getActivity(), profile._iconBitmap);
                if (bitmap != null)
                    activeProfileIcon.setImageBitmap(bitmap);
                else
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
            if (!ProfileStatic.isRedTextNotificationRequired(profile, false, activityDataWrapper.context)) {
                PPApplication.showToastForProfileActivation = true;
                activityDataWrapper.activateProfile(profile._id, PPApplication.STARTUP_SOURCE_ACTIVATOR, getActivity(), false);
            }
            else
                GlobalGUIRoutines.showDialogAboutRedText(profile, null, true, true, false, false, getActivity());
        }
    }

    void refreshGUI(/*final boolean refresh,*/ final boolean refreshIcons)
    {
        if ((activityDataWrapper == null) || (profileListAdapter == null))
            return;

        ActivatorListFragment.RefreshGUIAsyncTask asyncTask = new ActivatorListFragment.RefreshGUIAsyncTask(refreshIcons, this, activityDataWrapper);
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
//
//                        if ((!pNameHeader.isEmpty()) && pName.equals(pNameHeader)) {
//                            doNotRefresh = true;
//                            return null;
//                        }
//                    }
//
//                    PPApplication.setActivityProfileName(activityDataWrapper.context, 1, pName);

                } catch (Exception e) {
                    if ((activityDataWrapper != null) && (activityDataWrapper.context != null))
                        PPApplicationStatic.recordException(e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);
                if ((getActivity() != null) && (!getActivity().isFinishing())) {
                    //if (!doNotRefresh) {
                        ((ActivatorActivity) getActivity()).setEventsRunStopIndicator();

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

        //if (((ActivatorActivity)getActivity()).targetHelpsSequenceStarted)
        //    return;

        //Log.e("ActivatorListFragment.showTargetHelps", "(0)");

        boolean startTargetHelpsFinished = ApplicationPreferences.prefActivatorActivityStartTargetHelpsFinished;
        if (!startTargetHelpsFinished)
            return;

        //Log.e("ActivatorListFragment.showTargetHelps", "(1)");

        boolean showTargetHelps = ApplicationPreferences.prefActivatorFragmentStartTargetHelps;

        if (showTargetHelps ||
                ApplicationPreferences.prefActivatorAdapterStartTargetHelps) {

            //Log.e("ActivatorListFragment.showTargetHelps", "(2)");

            if (showTargetHelps) {
                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activityDataWrapper.context);
                editor.putBoolean(PREF_START_TARGET_HELPS, false);
                editor.putBoolean(PREF_START_TARGET_HELPS_FINISHED, true);
                editor.apply();
                ApplicationPreferences.prefActivatorFragmentStartTargetHelps = false;
                ApplicationPreferences.prefActivatorFragmentStartTargetHelpsFinished = true;

                //Log.e("ActivatorListFragment.showTargetHelps", "start showAdapterTargetHelps (1)");
                showAdapterTargetHelps();
            }
            else {
                final Handler handler = new Handler(getActivity().getMainLooper());
                handler.postDelayed(() -> {
//                        PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivatorListFragment.showTargetHelps (1)");
                    //Log.e("ActivatorListFragment.showTargetHelps", "start showAdapterTargetHelps (2)");
                    //noinspection Convert2MethodRef
                    showAdapterTargetHelps();
                }, 500);
            }
        }
        else {
            final Handler handler = new Handler(getActivity().getMainLooper());
            handler.postDelayed(() -> {
//                    PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivatorListFragment.showTargetHelps (2)");

                //Log.e("ActivatorListFragment.showTargetHelps", "(3)");

                if (ActivatorTargetHelpsActivity.activity != null) {
                    try {
                        ActivatorTargetHelpsActivity.activity.finish();
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                    ActivatorTargetHelpsActivity.activity = null;
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
        //Log.d("ActivatorListFragment.showAdapterTargetHelps", "profileListAdapter="+profileListAdapter);
        //Log.d("ActivatorListFragment.showAdapterTargetHelps", "itemView="+itemView);
        if ((profileListAdapter != null) && (itemView != null))
            profileListAdapter.showTargetHelps(getActivity(), /*this,*/ itemView);
        else {
            final Handler handler = new Handler(getActivity().getMainLooper());
            handler.postDelayed(() -> {
//                    PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivatorListFragment.showAdapterTargetHelps");

                if (ActivatorTargetHelpsActivity.activity != null) {
                    //Log.d("ActivatorListFragment.showAdapterTargetHelps", "finish activity");
                    try {
                        ActivatorTargetHelpsActivity.activity.finish();
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
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

        private final WeakReference<ActivatorListFragment> fragmentWeakRef;
        final DataWrapper dataWrapper;
        private final boolean refreshIcons;

        public RefreshGUIAsyncTask(final boolean refreshIcons,
                                   final ActivatorListFragment fragment,
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

                        if ((!pNameHeader.isEmpty()) && pName.equals(pNameHeader)) {
                            doNotRefresh = true;
                            return null;
                        }
                    }

                    PPApplication.setActivityProfileName(activityDataWrapper.context, 1, pName);
                    */
                } catch (Exception e) {
                    if ((dataWrapper != null) && (dataWrapper.context != null))
                        PPApplicationStatic.recordException(e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            ActivatorListFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if ((fragment.getActivity() != null) && (!fragment.getActivity().isFinishing())) {
                    //if (!doNotRefresh) {
                    ((ActivatorActivity) fragment.getActivity()).setEventsRunStopIndicator();

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
