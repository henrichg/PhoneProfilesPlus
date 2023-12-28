package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;
import java.util.Comparator;

public class ShortcutCreatorListFragment extends Fragment {

    DataWrapper activityDataWrapper;
    private ShortcutCreatorListAdapter profileListAdapter;
    private ListView listView;
    RelativeLayout viewNoData;
    private LinearLayout progressBar;

    private LoadProfileListAsyncTask loadAsyncTask = null;
    private CreateShortcutAsyncTask createShortcutAsyncTask = null;

    public ShortcutCreatorListFragment() {
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

        rootView = inflater.inflate(R.layout.fragment_shortcut_creator_list, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        doOnViewCreated(view);
    }

    private void doOnViewCreated(View view/*, Bundle savedInstanceState*/)
    {
        listView = view.findViewById(R.id.shortcut_profiles_list);
        viewNoData = view.findViewById(R.id.shortcut_profiles_list_empty);
        progressBar = view.findViewById(R.id.shortcut_profiles_list_linla_progress);
        Button cancelButton = view.findViewById(R.id.shortcut_profiles_list_cancel);

        listView.setOnItemClickListener((parent, item, position, id) -> {
            if (getActivity() != null) {
                ShortcutCreatorListViewHolder viewHolder = (ShortcutCreatorListViewHolder) item.getTag();
                if (viewHolder != null)
                    viewHolder.radioButton.setChecked(true);
                final Handler handler = new Handler(getActivity().getMainLooper());
                handler.postDelayed(() -> createShortcut(position), 200);
            }
        });

        cancelButton.setOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().finish();
        });

        if (!activityDataWrapper.profileListFilled)
        {
            loadAsyncTask.execute();
        }
        else
        {
            listView.setAdapter(profileListAdapter);
        }

    }

    private static class LoadProfileListAsyncTask extends AsyncTask<Void, Void, Void> {

        private final WeakReference<ShortcutCreatorListFragment> fragmentWeakRef;
        private final DataWrapper dataWrapper;

        final boolean applicationActivatorPrefIndicator;

        Handler progressBarHandler;
        Runnable progressBarRunnable;

        private static class ProfileComparator implements Comparator<Profile> {
            public int compare(Profile lhs, Profile rhs) {
                if (PPApplication.collator != null)
                    return PPApplication.collator.compare(lhs._name, rhs._name);
                else
                    return 0;
            }
        }

        public LoadProfileListAsyncTask (ShortcutCreatorListFragment fragment) {
            this.fragmentWeakRef = new WeakReference<>(fragment);
            //noinspection ConstantConditions
            this.dataWrapper = new DataWrapper(fragment.getActivity().getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);

            //applicationActivatorPrefIndicator = ApplicationPreferences.applicationActivatorPrefIndicator(this.dataWrapper.context);
            applicationActivatorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            final ShortcutCreatorListFragment fragment = this.fragmentWeakRef.get();

            if ((fragment != null) && (fragment.isAdded())) {
                progressBarHandler = new Handler(this.dataWrapper.context.getMainLooper());
                progressBarRunnable = () -> {
//                        PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ShortcutCreatorListFragment.LoadProfileListAsyncTask");
                    //fragment.textViewNoData.setVisibility(GONE);
                    fragment.progressBar.setVisibility(View.VISIBLE);
                };
                progressBarHandler.postDelayed(progressBarRunnable, 100);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            this.dataWrapper.fillProfileList(true, applicationActivatorPrefIndicator);
            this.dataWrapper.profileList.sort(new ProfileComparator());

            // add restart events
            //Profile profile = DataWrapper.getNonInitializedProfile(this.dataWrapper.context.getString(R.string.menu_restart_events), "ic_profile_restart_events|1|0|0", 0);
            Profile profile = DataWrapperStatic.getNonInitializedProfile(this.dataWrapper.context.getString(R.string.menu_restart_events),
                    StringConstants.PROFILE_ICON_RESTART_EVENTS+"|1|1|"+ApplicationPreferences.applicationRestartEventsIconColor, 0);
            profile.generateIconBitmap(dataWrapper.context, false, 0, false);
            this.dataWrapper.profileList.add(0, profile);

            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);
            
            ShortcutCreatorListFragment fragment = this.fragmentWeakRef.get(); 
            
            if ((fragment != null) && (fragment.isAdded())) {
                if ((fragment.getActivity() != null) && (!fragment.getActivity().isFinishing())) {
                    progressBarHandler.removeCallbacks(progressBarRunnable);
                    fragment.progressBar.setVisibility(View.GONE);

                    // get local profileList
                    //this.dataWrapper.fillProfileList(true, applicationActivatorPrefIndicator);

                    // set copy local profile list into activity profilesDataWrapper
                    fragment.activityDataWrapper.copyProfileList(this.dataWrapper);

//                    PPApplicationStatic.logE("[SYNCHRONIZED] ShortcutCreatorListFragment.LoadProfileListAsyncTask", "DataWrapper.profileList");
                    synchronized (fragment.activityDataWrapper.profileList) {
                        if (fragment.activityDataWrapper.profileList.size() == 0)
                            fragment.viewNoData.setVisibility(View.VISIBLE);
                    }

                    fragment.profileListAdapter = new ShortcutCreatorListAdapter(fragment, fragment.activityDataWrapper);
                    fragment.listView.setAdapter(fragment.profileListAdapter);
                }
            }
        }
    }

    private boolean isAsyncTaskRunning() {
        return (loadAsyncTask != null) &&
                loadAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (isAsyncTaskRunning()) {
            loadAsyncTask.cancel(true);
        }
        loadAsyncTask = null;
        if ((createShortcutAsyncTask != null) &&
                createShortcutAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            createShortcutAsyncTask.cancel(true);
        createShortcutAsyncTask = null;

        if (listView != null)
            listView.setAdapter(null);
        if (profileListAdapter != null)
            profileListAdapter.release();

        if (activityDataWrapper != null)
            activityDataWrapper.invalidateDataWrapper();
    }

    void createShortcut(final int position)
    {
        createShortcutAsyncTask = new CreateShortcutAsyncTask(position, this);
        createShortcutAsyncTask.execute();
    }

    /*
    private Bitmap combineImages(Bitmap bitmap1, Bitmap bitmap2)
    {
        Bitmap combined;

        int width;
        int height;

        width = bitmap2.getWidth();
        height = bitmap2.getHeight();

        combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(combined);
        canvas.drawBitmap(bitmap1, 0f, 0f, null);
        if (ApplicationPreferences.applicationShortcutEmblem)
            canvas.drawBitmap(bitmap2, 0f, 0f, null);

        return combined;
    }
    */

    private static class CreateShortcutAsyncTask extends AsyncTask<Void, Integer, Void> {

        Profile profile;
        boolean isIconResourceID;
        String iconIdentifier;
        Bitmap profileBitmap;
        //Bitmap shortcutOverlayBitmap;
        Bitmap profileShortcutBitmap;
        String profileName;
        String longLabel;
        boolean useCustomColor;
        Intent shortcutIntent;
        ShortcutInfoCompat.Builder shortcutBuilderCompat;

        final int position;
        private final WeakReference<ShortcutCreatorListFragment> fragmentWeakRef;

        public CreateShortcutAsyncTask(final int position,
                                       ShortcutCreatorListFragment fragment) {
            this.position = position;
            this.fragmentWeakRef = new WeakReference<>(fragment);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            ShortcutCreatorListFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {

//                PPApplicationStatic.logE("[SYNCHRONIZED] ShortcutCreatorListFragment.CreateShortcutAsyncTask", "DataWrapper.profileList");
                synchronized (fragment.activityDataWrapper.profileList) {
                    profile = fragment.activityDataWrapper.profileList.get(position);
                }

                if (fragment.getActivity() != null) {
                    Context context = fragment.getActivity().getApplicationContext();

                    if (profile != null) {
                        isIconResourceID = profile.getIsIconResourceID();
                        iconIdentifier = profile.getIconIdentifier();
                        profileName = profile._name;
                        longLabel = fragment.getString(R.string.shortcut_activate_profile) + profileName;
                        useCustomColor = profile.getUseCustomColorForIcon();
                        String id;
                        if (position == 0) {
                            profileName = fragment.getString(R.string.menu_restart_events);
                            longLabel = profileName;
                            id = "restart_events";
                        } else
                            id = "profile_" + profile._id;

                        if (profileName.isEmpty())
                            profileName = " ";

                        if (position == 0) {
                            // restart events
                        /*shortcutIntent = new Intent(context, ActionForExternalApplicationActivity.class);
                        shortcutIntent.setAction(ActionForExternalApplicationActivity.ACTION_RESTART_EVENTS);*/
                            shortcutIntent = new Intent(context, BackgroundActivateProfileActivity.class);
                            shortcutIntent.setAction(Intent.ACTION_MAIN);
                            shortcutIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
                            shortcutIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, Profile.RESTART_EVENTS_PROFILE_ID);
                        } else {
                            shortcutIntent = new Intent(context, BackgroundActivateProfileActivity.class);
                            shortcutIntent.setAction(Intent.ACTION_MAIN);
                            shortcutIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
                            shortcutIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                        }

                        /*
                        Intent intent = new Intent();
                        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, profileName);
                        */

                        shortcutBuilderCompat = new ShortcutInfoCompat.Builder(context, id);
                        shortcutBuilderCompat.setIntent(shortcutIntent);
                        shortcutBuilderCompat.setShortLabel(profileName);
                        shortcutBuilderCompat.setLongLabel(longLabel);
                    }
                }
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            ShortcutCreatorListFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if (fragment.getActivity() != null) {
                    Context context = fragment.getActivity().getApplicationContext();

                    if (profile != null) {
                        //profile.releaseIconBitmap();
                        profile.generateIconBitmap(context, false, 0, false);

                        if (isIconResourceID) {
                            if (profile._iconBitmap != null)
                                profileBitmap = profile._iconBitmap;
                            else {
                                //int iconResource = getResources().getIdentifier(iconIdentifier, "drawable", context.PPApplication.PACKAGE_NAME);
                                int iconResource = ProfileStatic.getIconResource(iconIdentifier);
                                //profileBitmap = BitmapFactory.decodeResource(getResources(), iconResource);
                                profileBitmap = BitmapManipulator.getBitmapFromResource(iconResource, true, context);
                            }
                        } else {
                            int height = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
                            int width = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
                            //Log.d("---- ShortcutCreatorListFragment.generateIconBitmap","resampleBitmapUri");
                            profileBitmap = BitmapManipulator.resampleBitmapUri(iconIdentifier, width, height, true, false, context);
                            if (profileBitmap == null) {
                                int iconResource = R.drawable.ic_profile_default;
                                //profileBitmap = BitmapFactory.decodeResource(getResources(), iconResource);
                                profileBitmap = BitmapManipulator.getBitmapFromResource(iconResource, true, context);
                            }
                        }
                        if (ApplicationPreferences.applicationShortcutIconColor.equals("1")) {
                            if (isIconResourceID || useCustomColor) {
                                // icon is from resource or colored by custom color
                                int monochromeValue = 0xFF;
                                String applicationWidgetIconLightness = ApplicationPreferences.applicationShortcutIconLightness;
                                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0))
                                    monochromeValue = 0x00;
                                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12))
                                    monochromeValue = 0x20;
                                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25))
                                    monochromeValue = 0x40;
                                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37))
                                    monochromeValue = 0x60;
                                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50))
                                    monochromeValue = 0x80;
                                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62))
                                    monochromeValue = 0xA0;
                                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75))
                                    monochromeValue = 0xC0;
                                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87))
                                    monochromeValue = 0xE0;
                                //if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100)) monochromeValue = 0xFF;
                                profileBitmap = BitmapManipulator.monochromeBitmap(profileBitmap, monochromeValue/*, context*/);
                            } else {
                                float monochromeValue = 255f;
                                String applicationWidgetIconLightness = ApplicationPreferences.applicationShortcutIconLightness;
                                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0))
                                    monochromeValue = -255f;
                                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_12))
                                    monochromeValue = -192f;
                                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_25))
                                    monochromeValue = -128f;
                                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_37))
                                    monochromeValue = -64f;
                                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_50))
                                    monochromeValue = 0f;
                                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_62))
                                    monochromeValue = 64f;
                                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_75))
                                    monochromeValue = 128f;
                                if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_87))
                                    monochromeValue = 192f;
                                //if (applicationWidgetIconLightness.equals(GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_100)) monochromeValue = 255f;
                                profileBitmap = BitmapManipulator.grayScaleBitmap(profileBitmap);
                                if (ApplicationPreferences.applicationShortcutCustomIconLightness)
                                    profileBitmap = BitmapManipulator.setBitmapBrightness(profileBitmap, monochromeValue);
                            }
                        }

                        profileShortcutBitmap = profileBitmap;
                        //intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, profileShortcutBitmap);
                        shortcutBuilderCompat.setIcon(IconCompat.createWithBitmap(profileShortcutBitmap));
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            ShortcutCreatorListFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if ((fragment.getActivity() != null) && !fragment.getActivity().isFinishing()) {

                    Context context = fragment.getActivity().getApplicationContext();

                    if (profile != null) {

                        //intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                        //context.sendBroadcast(intent);

                        try {
                            ShortcutInfoCompat shortcutInfo = shortcutBuilderCompat.build();
                            Intent intent = ShortcutManagerCompat.createShortcutResultIntent(context, shortcutInfo);
                            fragment.getActivity().setResult(Activity.RESULT_OK, intent);
                        } catch (Exception e) {
                            // show dialog about this crash
                            // for Microsft laucher it is:
                            // java.lang.IllegalArgumentException ... already exists but disabled
                        }
                    }

                    fragment.getActivity().finish();
                }
            }
        }

    }

}
