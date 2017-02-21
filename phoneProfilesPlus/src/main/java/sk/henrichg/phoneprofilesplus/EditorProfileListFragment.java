package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.mobeta.android.dslv.DragSortListView;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EditorProfileListFragment extends Fragment {

    public DataWrapper dataWrapper;
    private ActivateProfileHelper activateProfileHelper;
    private List<Profile> profileList;
    private EditorProfileListAdapter profileListAdapter;
    private DragSortListView listView;
    private TextView activeProfileName;
    private ImageView activeProfileIcon;
    private Toolbar bottomToolbar;
    private DatabaseHandler databaseHandler;

    private WeakReference<LoadProfileListAsyncTask> asyncTaskContext;

    public static final int EDIT_MODE_UNDEFINED = 0;
    public static final int EDIT_MODE_INSERT = 1;
    public static final int EDIT_MODE_DUPLICATE = 2;
    public static final int EDIT_MODE_EDIT = 3;
    public static final int EDIT_MODE_DELETE = 4;

    public static final String FILTER_TYPE_ARGUMENT = "filter_type";
    public static final String START_TARGET_HELPS_ARGUMENT = "start_target_helps";

    public static final int FILTER_TYPE_ALL = 0;
    public static final int FILTER_TYPE_SHOW_IN_ACTIVATOR = 1;
    public static final int FILTER_TYPE_NO_SHOW_IN_ACTIVATOR = 2;

    public boolean targetHelpsSequenceStarted;
    public static final String PREF_START_TARGET_HELPS = "editor_profile_list_fragment_start_target_helps";

    private int filterType = FILTER_TYPE_ALL;

    /**
     * The fragment's current callback objects
     */
    private OnStartProfilePreferences onStartProfilePreferencesCallback = sDummyOnStartProfilePreferencesCallback;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified.
     */
    // invoked when start profile preference fragment/activity needed
    public interface OnStartProfilePreferences {
        void onStartProfilePreferences(Profile profile, int editMode, int predefinedProfileIndex, boolean startTargetHelps);
    }

    /**
     * A dummy implementation of the Callbacks interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static OnStartProfilePreferences sDummyOnStartProfilePreferencesCallback = new OnStartProfilePreferences() {
        public void onStartProfilePreferences(Profile profile, int editMode, int predefinedProfileIndex, boolean startTargetHelps) {
        }
    };

    public EditorProfileListFragment() {
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof OnStartProfilePreferences)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }
        onStartProfilePreferencesCallback = (OnStartProfilePreferences) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        onStartProfilePreferencesCallback = sDummyOnStartProfilePreferencesCallback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        setRetainInstance(true);

        filterType = getArguments() != null ? 
                getArguments().getInt(FILTER_TYPE_ARGUMENT, EditorProfileListFragment.FILTER_TYPE_ALL) :
                    EditorProfileListFragment.FILTER_TYPE_ALL;

        dataWrapper = new DataWrapper(getActivity().getApplicationContext(), true, false, 0);

        databaseHandler = dataWrapper.getDatabaseHandler();

        activateProfileHelper = dataWrapper.getActivateProfileHelper();
        activateProfileHelper.initialize(dataWrapper, getActivity().getApplicationContext());

        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;

        if (PPApplication.applicationEditorPrefIndicator && PPApplication.applicationEditorHeader)
            rootView = inflater.inflate(R.layout.editor_profile_list, container, false);
        else
        if (PPApplication.applicationEditorHeader)
            rootView = inflater.inflate(R.layout.editor_profile_list_no_indicator, container, false);
        else
            rootView = inflater.inflate(R.layout.editor_profile_list_no_header, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        doOnViewCreated(view, savedInstanceState);

        boolean startTargetHelps = getArguments() != null && getArguments().getBoolean(START_TARGET_HELPS_ARGUMENT, false);
        if (startTargetHelps)
            showTargetHelps();
    }

    //@Override
    //public void onActivityCreated(Bundle savedInstanceState)
    @SuppressLint("InflateParams")
    public void doOnViewCreated(View view, Bundle savedInstanceState)
    {
        //super.onActivityCreated(savedInstanceState);

        // az tu mame layout, tak mozeme ziskat view-y
    /*	activeProfileName = (TextView)getActivity().findViewById(R.id.activated_profile_name);
        activeProfileIcon = (ImageView)getActivity().findViewById(R.id.activated_profile_icon);
        listView = (DragSortListView)getActivity().findViewById(R.id.editor_profiles_list);
        listView.setEmptyView(getActivity().findViewById(R.id.editor_profiles_list_empty));
    */
        activeProfileName = (TextView)view.findViewById(R.id.activated_profile_name);
        activeProfileIcon = (ImageView)view.findViewById(R.id.activated_profile_icon);
        listView = (DragSortListView)view.findViewById(R.id.editor_profiles_list);
        listView.setEmptyView(view.findViewById(R.id.editor_profiles_list_empty));

        /*
        View footerView =  ((LayoutInflater)getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.editor_list_footer, null, false);
        listView.addFooterView(footerView, null, false);
        */

        final Activity activity = getActivity();
        final EditorProfileListFragment fragment = this;

        bottomToolbar = (Toolbar)getActivity().findViewById(R.id.editor_list_bottom_bar);
        Menu menu = bottomToolbar.getMenu();
        if (menu != null) menu.clear();
        bottomToolbar.inflateMenu(R.menu.editor_profiles_bottom_bar);
        bottomToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_add_profile:
                        AddProfileDialog dialog = new AddProfileDialog(activity, fragment);
                        dialog.show();
                        return true;
                    case R.id.menu_delete_all_profiles:
                        deleteAllProfiles();
                        return true;
                    /*case R.id.menu_default_profile:
                        // start preferences activity for default profile
                        Intent intent = new Intent(getActivity().getBaseContext(), ProfilePreferencesActivity.class);
                        intent.putExtra(PPApplication.EXTRA_PROFILE_ID, PPApplication.DEFAULT_PROFILE_ID);
                        intent.putExtra(PPApplication.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_EDIT);
                        intent.putExtra(PPApplication.EXTRA_PREDEFINED_PROFILE_INDEX, 0);
                        getActivity().startActivityForResult(intent, PPApplication.REQUEST_CODE_PROFILE_PREFERENCES);
                        return true;*/
                    case R.id.important_info:
                        Intent intent = new Intent(getActivity().getBaseContext(), ImportantInfoActivity.class);
                        getActivity().startActivity(intent);
                        return true;
                }
                return false;
            }
        });

        LinearLayout orderLayout = (LinearLayout)getActivity().findViewById(R.id.editor_list_bottom_bar_order_root);
        orderLayout.setVisibility(View.GONE);
        
        listView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startProfilePreferencesActivity((Profile)profileListAdapter.getItem(position), 0);
            }

        });

        listView.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                activateProfile((Profile) profileListAdapter.getItem(position)/*, true*/);
                return true;
            }

        });

        listView.setDropListener(new DragSortListView.DropListener() {
            public void drop(int from, int to) {
                profileListAdapter.changeItemOrder(from, to); // swap profiles
                databaseHandler.setProfileOrder(profileList);  // set profiles _porder and write it into db
                activateProfileHelper.updateWidget();
            }
        });

        if (profileList == null)
        {
            LoadProfileListAsyncTask asyncTask = new LoadProfileListAsyncTask(this, filterType);
            this.asyncTaskContext = new WeakReference<>(asyncTask );
            asyncTask.execute();
        }
        else
        {
            listView.setAdapter(profileListAdapter);
        
            // pre profil, ktory je prave aktivny, treba aktualizovat aktivitu
            Profile profile;
            profile = dataWrapper.getActivatedProfile();
            updateHeader(profile);
            setProfileSelection(profile, false);
        }

    }

    private static class LoadProfileListAsyncTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<EditorProfileListFragment> fragmentWeakRef;
        private DataWrapper dataWrapper;
        private int filterType;
        boolean defaultProfilesGenerated = false;
        boolean defaultEventsGenerated = false;

        private LoadProfileListAsyncTask (EditorProfileListFragment fragment, int filterType) {
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.filterType = filterType;
            this.dataWrapper = new DataWrapper(fragment.getActivity().getApplicationContext(), true, false, 0);
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<Profile> profileList = dataWrapper.getProfileList();
            if (profileList.size() == 0)
            {
                // no profiles in DB, generate default profiles and events

                profileList = dataWrapper.getPredefinedProfileList();
                defaultProfilesGenerated = true;

                dataWrapper.generatePredefinedEventList();
                defaultEventsGenerated = true;
            }
            // sort list
            if (filterType != EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR)
                EditorProfileListFragment.sortAlphabetically(profileList);
            else
                EditorProfileListFragment.sortByPOrder(profileList);

            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);
            
            EditorProfileListFragment fragment = this.fragmentWeakRef.get(); 
            
            if ((fragment != null) && (fragment.isAdded())) {

                // get local profileList
                List<Profile> profileList = dataWrapper.getProfileList();
                // set local profile list into activity dataWrapper
                fragment.dataWrapper.setProfileList(profileList, false);
                // set reference of profile list from dataWrapper
                fragment.profileList = fragment.dataWrapper.getProfileList();

                fragment.profileListAdapter = new EditorProfileListAdapter(fragment, fragment.dataWrapper, fragment.filterType);
                fragment.listView.setAdapter(fragment.profileListAdapter);

                // pre profil, ktory je prave aktivny, treba aktualizovat aktivitu
                Profile profile;
                profile = fragment.dataWrapper.getActivatedProfile();
                fragment.updateHeader(profile);
                fragment.setProfileSelection(profile, false);

                if (defaultProfilesGenerated)
                {
                    fragment.activateProfileHelper.updateWidget();
                    Toast msg = Toast.makeText(fragment.getActivity(),
                            fragment.getResources().getString(R.string.toast_default_profiles_generated),
                            Toast.LENGTH_SHORT);
                    msg.show();
                }
                if (defaultEventsGenerated)
                {
                    Toast msg = Toast.makeText(fragment.getActivity(),
                            fragment.getResources().getString(R.string.toast_default_events_generated),
                            Toast.LENGTH_SHORT);
                    msg.show();
                }
            
            }
        }
    }

    private boolean isAsyncTaskPendingOrRunning() {
        return this.asyncTaskContext != null &&
              this.asyncTaskContext.get() != null &&
              !this.asyncTaskContext.get().getStatus().equals(AsyncTask.Status.FINISHED);
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onDestroy()
    {
        if (!isAsyncTaskPendingOrRunning())
        {
            if (listView != null)
                listView.setAdapter(null);
            if (profileListAdapter != null)
                profileListAdapter.release();

            activateProfileHelper = null;
            profileList = null;
            databaseHandler = null;

            if (dataWrapper != null)
                dataWrapper.invalidateDataWrapper();
            dataWrapper = null;
        }

        super.onDestroy();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.fragment_editor_profile_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        /*case R.id.menu_new_profile:
            startProfilePreferencesActivity(null);

            return true;*/
        /*case R.id.menu_delete_all_profiles:
            deleteAllProfiles();

            return true;*/
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public void startProfilePreferencesActivity(Profile profile, int predefinedProfileIndex)
    {
        int editMode;

        if (profile != null)
        {
            // editacia profilu
            int profilePos = profileListAdapter.getItemPosition(profile);
            listView.setItemChecked(profilePos, true);
            int last = listView.getLastVisiblePosition();
            int first = listView.getFirstVisiblePosition();
            if ((profilePos <= first) || (profilePos >= last)) {
                listView.setSelection(profilePos);
                //listView.smoothScrollToPosition(profilePos);
            }

            boolean startTargetHelps = getArguments() != null && getArguments().getBoolean(START_TARGET_HELPS_ARGUMENT, false);
            if (startTargetHelps)
                showAdapterTargetHelps();

            editMode = EDIT_MODE_EDIT;
        }
        else
        {
            // pridanie noveho profilu
            editMode = EDIT_MODE_INSERT;
        }

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) one must start profile preferences
        onStartProfilePreferencesCallback.onStartProfilePreferences(profile, editMode, predefinedProfileIndex, true);
    }

    public void duplicateProfile(Profile origProfile)
    {
        int editMode;

        // zduplikovanie profilu
        editMode = EDIT_MODE_DUPLICATE;

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) one must start profile preferences
        onStartProfilePreferencesCallback.onStartProfilePreferences(origProfile, editMode, 0, true);

    }

    public void deleteProfile(Profile profile)
    {
        //final Profile _profile = profile;
        //final Activity activity = getActivity();

        if (dataWrapper.getProfileById(profile._id, false) == null)
            // profile not exists
            return;

        /*
        class DeleteAsyncTask extends AsyncTask<Void, Integer, Integer>
        {
            private MaterialDialog dialog;

            DeleteAsyncTask()
            {
                 this.dialog = new MaterialDialog.Builder(activity)
                                 .content(getResources().getString(R.string.delete_profile_progress_title) + "...")
                                 .disableDefaultFonts()
                                 .progress(true, 0)
                                 .build();
            }

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();

                try {
                    this.dialog.show();
                } catch (Exception e) {
                }
            }

            @Override
            protected Integer doInBackground(Void... params) {

                dataWrapper.stopEventsForProfile(_profile, true);
                dataWrapper.unlinkEventsFromProfile(_profile);
                profileListAdapter.deleteItemNoNotify(_profile);
                databaseHandler.unlinkEventsFromProfile(_profile);
                databaseHandler.deleteProfile(_profile);

                return 1;
            }

            @Override
            protected void onPostExecute(Integer result)
            {
                super.onPostExecute(result);

                try {
                    if (dialog.isShowing())
                        dialog.dismiss();
                } catch (Exception e) {
                }

                if (result == 1)
                {
                    if (!profileListAdapter.released)
                    {
                        profileListAdapter.notifyDataSetChanged();
                        // v pripade, ze sa odmaze aktivovany profil, nastavime, ze nic nie je aktivovane
                        //Profile profile = databaseHandler.getActivatedProfile();
                        Profile profile = profileListAdapter.getActivatedProfile();
                        updateHeader(profile);
                        activateProfileHelper.showNotification(profile, "");
                        activateProfileHelper.updateWidget();

                        onStartProfilePreferencesCallback.onStartProfilePreferences(null, EDIT_MODE_DELETE, filterType);
                    }
                }
            }

        }

        new DeleteAsyncTask().execute();
        */

        Profile activatedProfile = dataWrapper.getActivatedProfile();
        if ((activatedProfile != null) && (activatedProfile._id == profile._id)) {
            // remove alarm for profile duration
            ProfileDurationAlarmBroadcastReceiver.removeAlarm(getActivity().getApplicationContext());
            PPApplication.setActivatedProfileForDuration(getActivity().getApplicationContext(), 0);
        }

        dataWrapper.stopEventsForProfile(profile, true);
        dataWrapper.unlinkEventsFromProfile(profile);
        profileListAdapter.deleteItemNoNotify(profile);
        databaseHandler.unlinkEventsFromProfile(profile);
        databaseHandler.deleteProfile(profile);

        profileListAdapter.notifyDataSetChanged();
        // v pripade, ze sa odmaze aktivovany profil, nastavime, ze nic nie je aktivovane
        //Profile profile = databaseHandler.getActivatedProfile();
        Profile _profile = profileListAdapter.getActivatedProfile();
        updateHeader(_profile);
        activateProfileHelper.showNotification(_profile);
        activateProfileHelper.updateWidget();

        onStartProfilePreferencesCallback.onStartProfilePreferences(null, EDIT_MODE_DELETE, 0, true);

    }

    public void showEditMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        Context context = view.getContext();
        PopupMenu popup = new PopupMenu(context, view);
        getActivity().getMenuInflater().inflate(R.menu.profile_list_item_edit, popup.getMenu());

        final Profile profile = (Profile)view.getTag();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                case R.id.profile_list_item_menu_activate:
                    activateProfile(profile/*, true*/);
                    return true;
                case R.id.profile_list_item_menu_duplicate:
                    duplicateProfile(profile);
                    return true;
                case R.id.profile_list_item_menu_delete:
                    deleteProfileWithAlert(profile);
                    return true;
                default:
                    return false;
                }
            }
            });


        popup.show();
    }

    public void deleteProfileWithAlert(Profile profile)
    {
        final Profile _profile = profile;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(getResources().getString(R.string.profile_string_0) + ": " + profile._name);
        dialogBuilder.setMessage(R.string.delete_profile_alert_message);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

        dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                deleteProfile(_profile);
            }
        });
        dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
        dialogBuilder.show();
    }

    private void deleteAllProfiles()
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.alert_title_delete_all_profiles);
        dialogBuilder.setMessage(R.string.alert_message_delete_all_profiles);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

        //final Activity activity = getActivity();

        dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                /*
                class DeleteAsyncTask extends AsyncTask<Void, Integer, Integer>
                {
                    private MaterialDialog dialog;

                    DeleteAsyncTask()
                    {
                         this.dialog = new MaterialDialog.Builder(activity)
                                         .content(getResources().getString(R.string.delete_profiles_progress_title) + "...")
                                         .disableDefaultFonts()
                                         .progress(true, 0)
                                         .build();
                    }

                    @Override
                    protected void onPreExecute()
                    {
                        super.onPreExecute();

                        this.dialog.show();
                    }

                    @Override
                    protected Integer doInBackground(Void... params) {

                        dataWrapper.stopAllEvents(true);
                        dataWrapper.unlinkAllEvents();
                        profileListAdapter.clearNoNotify();
                        databaseHandler.deleteAllProfiles();
                        databaseHandler.unlinkAllEvents();

                        return 1;
                    }

                    @Override
                    protected void onPostExecute(Integer result)
                    {
                        super.onPostExecute(result);

                        if (dialog.isShowing())
                            dialog.dismiss();

                        if (result == 1)
                        {
                            profileListAdapter.notifyDataSetChanged();
                            // v pripade, ze sa odmaze aktivovany profil, nastavime, ze nic nie je aktivovane
                            //Profile profile = databaseHandler.getActivatedProfile();
                            //Profile profile = profileListAdapter.getActivatedProfile();
                            updateHeader(null);
                            activateProfileHelper.removeNotification();
                            activateProfileHelper.updateWidget();

                            onStartProfilePreferencesCallback.onStartProfilePreferences(null, EDIT_MODE_DELETE, filterType);
                        }
                    }

                }

                new DeleteAsyncTask().execute();
                */

                // remove alarm for profile duration
                ProfileDurationAlarmBroadcastReceiver.removeAlarm(getActivity().getApplicationContext());
                PPApplication.setActivatedProfileForDuration(getActivity().getApplicationContext(), 0);

                dataWrapper.stopAllEvents(true, false);
                dataWrapper.unlinkAllEvents();
                profileListAdapter.clearNoNotify();
                databaseHandler.deleteAllProfiles();
                databaseHandler.unlinkAllEvents();

                profileListAdapter.notifyDataSetChanged();
                // v pripade, ze sa odmaze aktivovany profil, nastavime, ze nic nie je aktivovane
                //Profile profile = databaseHandler.getActivatedProfile();
                //Profile profile = profileListAdapter.getActivatedProfile();
                updateHeader(null);
                activateProfileHelper.removeNotification();
                activateProfileHelper.updateWidget();

                onStartProfilePreferencesCallback.onStartProfilePreferences(null, EDIT_MODE_DELETE, 0, true);

            }
        });
        dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
        dialogBuilder.show();
    }

    public void updateHeader(Profile profile)
    {
        if (!PPApplication.applicationEditorHeader)
            return;

        if ((activeProfileName == null) || (activeProfileIcon == null))
            return;

        if (profile == null)
        {
            activeProfileName.setText(getResources().getString(R.string.profiles_header_profile_name_no_activated));
            activeProfileIcon.setImageResource(R.drawable.ic_profile_default);
        }
        else
        {
            activeProfileName.setText(dataWrapper.getProfileNameWithManualIndicator(profile, true, true, false));
            if (profile.getIsIconResourceID())
            {
                if (profile._iconBitmap != null)
                    activeProfileIcon.setImageBitmap(profile._iconBitmap);
                else {
                    int res = getResources().getIdentifier(profile.getIconIdentifier(), "drawable", getActivity().getPackageName());
                    activeProfileIcon.setImageResource(res); // resource na ikonu
                }
            }
            else
            {
                //Resources resources = getResources();
                //int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
                //int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
                //Bitmap bitmap = BitmapResampler.resample(profile.getIconIdentifier(), width, height);
                //activeProfileIcon.setImageBitmap(bitmap);
                activeProfileIcon.setImageBitmap(profile._iconBitmap);
            }
        }

        if (PPApplication.applicationEditorPrefIndicator)
        {
            ImageView profilePrefIndicatorImageView = (ImageView)getActivity().findViewById(R.id.activated_profile_pref_indicator);
            if (profilePrefIndicatorImageView != null)
            {
                //profilePrefIndicatorImageView.setImageBitmap(ProfilePreferencesIndicator.paint(profile, getActivity().getBaseContext()));
                if (profile == null)
                    profilePrefIndicatorImageView.setImageResource(R.drawable.ic_empty);
                else
                    profilePrefIndicatorImageView.setImageBitmap(profile._preferencesIndicator);
            }
        }
    }

    public void doOnActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PPApplication.REQUEST_CODE_ACTIVATE_PROFILE)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                long profile_id = data.getLongExtra(PPApplication.EXTRA_PROFILE_ID, -1);
                Profile profile = dataWrapper.getProfileById(profile_id, false);

                if (profileListAdapter != null)
                    profileListAdapter.activateProfile(profile);
                updateHeader(profile);
             }
             //if (resultCode == Activity.RESULT_CANCELED)
             //{
                 //Write your code if there's no result
             //}
        }
    }

    public void activateProfile(Profile profile/*, boolean interactive*/)
    {
        dataWrapper.activateProfile(profile._id, PPApplication.STARTUP_SOURCE_EDITOR, getActivity()/*, ""*/);
    }

    private void setProfileSelection(Profile profile, boolean refreshIcons) {
        if (profileListAdapter != null)
        {
            int profilePos;

            if (profile != null)
                profilePos = profileListAdapter.getItemPosition(profile);
            else
                profilePos = listView.getCheckedItemPosition();

            profileListAdapter.notifyDataSetChanged(refreshIcons);

            if (profilePos != ListView.INVALID_POSITION)
            {
                // set profile visible in list
                listView.setItemChecked(profilePos, true);
                int last = listView.getLastVisiblePosition();
                int first = listView.getFirstVisiblePosition();
                if ((profilePos <= first) || (profilePos >= last)) {
                    listView.setSelection(profilePos);
                    //listView.smoothScrollToPosition(profilePos);
                }
            }

            boolean startTargetHelps = getArguments() != null && getArguments().getBoolean(START_TARGET_HELPS_ARGUMENT, false);
            if (startTargetHelps)
                showAdapterTargetHelps();
        }
    }

    public void updateListView(Profile profile, boolean newProfile, boolean refreshIcons, boolean setPosition)
    {
        if (profileListAdapter != null)
        {
            if ((newProfile) && (profile != null))
                // add profile into listview
                profileListAdapter.addItem(profile, false);
        }

        if (profileList != null)
        {
            // sort list
            if (filterType != EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR)
                sortAlphabetically(profileList);
            else
                sortByPOrder(profileList);
        }

        if (setPosition || newProfile)
            setProfileSelection(profile, refreshIcons);
    }

    /*
    public int getFilterType()
    {
        return filterType;
    }
    */

    public static void sortAlphabetically(List<Profile> profileList)
    {
        class AlphabeticallyComparator implements Comparator<Profile> {
            public int compare(Profile lhs, Profile rhs) {

                return GlobalGUIRoutines.collator.compare(lhs._name, rhs._name);
            }
        }
        Collections.sort(profileList, new AlphabeticallyComparator());
    }

    public static void sortByPOrder(List<Profile> profileList)
    {
        class ByPOrderComparator implements Comparator<Profile> {
            public int compare(Profile lhs, Profile rhs) {
                int res = 0;
                if ((lhs != null) && (rhs != null))
                    res =  lhs._porder - rhs._porder;
                return res;
            }
        }
        Collections.sort(profileList, new ByPOrderComparator());
    }

    public void refreshGUI(boolean refreshIcons, boolean setPosition)
    {
        if ((dataWrapper == null) || (profileListAdapter == null))
            return;

        PPApplication.logE("EditorProfileListFragment.refreshGUI","refresh");

        Profile profileFromAdapter = profileListAdapter.getActivatedProfile();
        if (profileFromAdapter != null)
            profileFromAdapter._checked = false;

        Profile profileFromDB = dataWrapper.getDatabaseHandler().getActivatedProfile();
        if (profileFromDB != null)
        {
            PPApplication.logE("EditorProfileListFragment.refreshGUI","profile activated");
            Profile profileFromDataWrapper = dataWrapper.getProfileById(profileFromDB._id, false);
            if (profileFromDataWrapper != null)
                profileFromDataWrapper._checked = true;
            updateHeader(profileFromDataWrapper);
            updateListView(profileFromDataWrapper, false, refreshIcons, setPosition);
        }
        else
        {
            PPApplication.logE("EditorProfileListFragment.refreshGUI","profile not activated");
            updateHeader(null);
            updateListView(null, false, refreshIcons, setPosition);
        }

    }

    public void removeAdapter() {
        if (listView != null)
            listView.setAdapter(null);
    }

    void showTargetHelps() {
        if (((EditorProfilesActivity)getActivity()).targetHelpsSequenceStarted)
            return;

        final SharedPreferences preferences = getActivity().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);

        if (preferences.getBoolean(PREF_START_TARGET_HELPS, true) ||
                preferences.getBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, true) ||
                preferences.getBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, true)) {

            Log.d("EditorProfileListFragment.showTargetHelps", "PREF_START_TARGET_HELPS_ORDER=true");

            if (preferences.getBoolean(PREF_START_TARGET_HELPS, true)) {

                Log.d("EditorProfileListFragment.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PREF_START_TARGET_HELPS, false);
                editor.commit();

                int circleColor = 0xFFFFFF;
                if (PPApplication.applicationTheme.equals("dark"))
                    circleColor = 0x7F7F7F;

                final TapTargetSequence sequence = new TapTargetSequence(getActivity())
                        .targets(
                                TapTarget.forToolbarMenuItem(bottomToolbar, R.id.menu_add_profile, getString(R.string.editor_activity_targetHelps_newProfileButton_title), getString(R.string.editor_activity_targetHelps_newProfileButton_description))
                                        .targetCircleColorInt(circleColor)
                                        .textColorInt(0xFFFFFF)
                                        .drawShadow(true)
                                        .id(1),
                                TapTarget.forToolbarMenuItem(bottomToolbar, R.id.menu_delete_all_profiles, getString(R.string.editor_activity_targetHelps_deleteAllProfilesButton_title), getString(R.string.editor_activity_targetHelps_deleteAllProfilesButton_description))
                                        .targetCircleColorInt(circleColor)
                                        .textColorInt(0xFFFFFF)
                                        .drawShadow(true)
                                        .id(2),
                                TapTarget.forToolbarMenuItem(bottomToolbar, R.id.important_info, getString(R.string.editor_activity_targetHelps_importantInfoButton_title), getString(R.string.editor_activity_targetHelps_importantInfoButton_description))
                                        .targetCircleColorInt(circleColor)
                                        .textColorInt(0xFFFFFF)
                                        .drawShadow(true)
                                        .id(3)
                        )
                        .listener(new TapTargetSequence.Listener() {
                            // This listener will tell us when interesting(tm) events happen in regards
                            // to the sequence
                            @Override
                            public void onSequenceFinish() {
                                targetHelpsSequenceStarted = false;
                                showAdapterTargetHelps();
                            }

                            @Override
                            public void onSequenceStep(TapTarget lastTarget) {
                                //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                            }

                            @Override
                            public void onSequenceCanceled(TapTarget lastTarget) {
                                targetHelpsSequenceStarted = false;
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, false);
                                if (filterType == FILTER_TYPE_SHOW_IN_ACTIVATOR)
                                    editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
                                editor.commit();
                            }
                        });
                targetHelpsSequenceStarted = true;
                sequence.start();
            }
            else {
                Log.d("EditorProfileListFragment.showTargetHelps", "PREF_START_TARGET_HELPS=false");
                final Handler handler = new Handler();
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
        View itemView;
        if (listView.getChildCount() > 1)
            itemView = listView.getChildAt(1);
        else
            itemView = listView.getChildAt(0);
        Log.d("EditorProfileListFragment.showAdapterTargetHelps", "profileListAdapter="+profileListAdapter);
        Log.d("EditorProfileListFragment.showAdapterTargetHelps", "itemView="+itemView);
        if ((profileListAdapter != null) && (itemView != null))
            profileListAdapter.showTargetHelps(getActivity(), this, itemView);
        else {
            targetHelpsSequenceStarted = false;
            final SharedPreferences preferences = getActivity().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(PREF_START_TARGET_HELPS, false);
            editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, false);
            editor.commit();
        }
    }

}
