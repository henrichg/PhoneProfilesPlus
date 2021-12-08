package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;

public class ShortcutCreatorListFragment extends Fragment {

    DataWrapper activityDataWrapper;
    private ShortcutCreatorListAdapter profileListAdapter;
    private ListView listView;
    TextView textViewNoData;
    private LinearLayout progressBar;

    private WeakReference<LoadProfileListAsyncTask> asyncTaskContext;

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

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;

        rootView = inflater.inflate(R.layout.shortcut_creator_list, container, false);

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
        textViewNoData = view.findViewById(R.id.shortcut_profiles_list_empty);
        progressBar = view.findViewById(R.id.shortcut_profiles_list_linla_progress);
        Button cancelButton = view.findViewById(R.id.shortcut_profiles_list_cancel);

        listView.setOnItemClickListener((parent, item, position, id) -> {
            if (getActivity() != null) {
                ShortcutCreatorListAdapter.ViewHolder viewHolder = (ShortcutCreatorListAdapter.ViewHolder) item.getTag();
                if (viewHolder != null)
                    viewHolder.radioButton.setChecked(true);
                Handler handler = new Handler(getActivity().getMainLooper());
                handler.postDelayed(() -> createShortcut(position), 200);
            }
        });

        cancelButton.setOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().finish();
        });

        if (!activityDataWrapper.profileListFilled)
        {
            LoadProfileListAsyncTask asyncTask = new LoadProfileListAsyncTask(this);
            this.asyncTaskContext = new WeakReference<>(asyncTask );
            asyncTask.execute();
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
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ShortcutCreatorListFragment.LoadProfileListAsyncTask");
                    //fragment.textViewNoData.setVisibility(GONE);
                    fragment.progressBar.setVisibility(View.VISIBLE);
                };
                progressBarHandler.postDelayed(progressBarRunnable, 100);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            this.dataWrapper.fillProfileList(true, applicationActivatorPrefIndicator);
            //noinspection Java8ListSort
            Collections.sort(this.dataWrapper.profileList, new ProfileComparator());

            // add restart events
            Profile profile = DataWrapper.getNonInitializedProfile(this.dataWrapper.context.getString(R.string.menu_restart_events), "ic_list_item_events_restart_color_filled|1|0|0", 0);
            this.dataWrapper.profileList.add(0, profile);

            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);
            
            ShortcutCreatorListFragment fragment = this.fragmentWeakRef.get(); 
            
            if ((fragment != null) && (fragment.isAdded())) {
                progressBarHandler.removeCallbacks(progressBarRunnable);
                fragment.progressBar.setVisibility(View.GONE);

                // get local profileList
                this.dataWrapper.fillProfileList(true, applicationActivatorPrefIndicator);

                // set copy local profile list into activity profilesDataWrapper
                fragment.activityDataWrapper.copyProfileList(this.dataWrapper);

                synchronized (fragment.activityDataWrapper.profileList) {
                    if (fragment.activityDataWrapper.profileList.size() == 0)
                        fragment.textViewNoData.setVisibility(View.VISIBLE);
                }

                fragment.profileListAdapter = new ShortcutCreatorListAdapter(fragment, fragment.activityDataWrapper);
                fragment.listView.setAdapter(fragment.profileListAdapter);
            }
        }
    }

    private boolean isAsyncTaskPendingOrRunning() {
        return this.asyncTaskContext != null &&
              this.asyncTaskContext.get() != null &&
              !this.asyncTaskContext.get().getStatus().equals(AsyncTask.Status.FINISHED);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (isAsyncTaskPendingOrRunning()) {
            this.asyncTaskContext.get().cancel(true);
        }

        if (listView != null)
            listView.setAdapter(null);
        if (profileListAdapter != null)
            profileListAdapter.release();

        //if (activityDataWrapper != null)
        //    activityDataWrapper.invalidateDataWrapper();
        activityDataWrapper = null;
    }

    @SuppressLint("StaticFieldLeak")
    void createShortcut(final int position)
    {
        new CreateShortcutAsyncTask(position, this).execute();
    }

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

    private static class CreateShortcutAsyncTask extends AsyncTask<Void, Integer, Void> {

        Profile profile;
        boolean isIconResourceID;
        String iconIdentifier;
        Bitmap profileBitmap;
        Bitmap shortcutOverlayBitmap;
        Bitmap profileShortcutBitmap;
        String profileName;
        String longLabel;
        boolean useCustomColor;
        Context context;
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

                //PPApplication.logE("ShortcutCreatorListFragment.createShortcut","position="+position);
                synchronized (fragment.activityDataWrapper.profileList) {
                    profile = fragment.activityDataWrapper.profileList.get(position);
                }

                //noinspection ConstantConditions
                context = fragment.getActivity().getApplicationContext();

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

                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("ShortcutCreatorListFragment.createShortcut", "profile._id=" + profile._id);
                        PPApplication.logE("ShortcutCreatorListFragment.createShortcut", "profileName=" + profileName);
                    }*/

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

        @Override
        protected Void doInBackground(Void... params) {
            ShortcutCreatorListFragment fragment = fragmentWeakRef.get();
            if (fragment != null) {
                if (profile != null) {
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("ShortcutCreatorListFragment.createShortcut", "isIconResourceID=" + isIconResourceID);
                        PPApplication.logE("ShortcutCreatorListFragment.createShortcut", "profile._iconBitmap=" + profile._iconBitmap);
                    }*/

                    if (isIconResourceID) {
                        if (profile._iconBitmap != null)
                            profileBitmap = profile._iconBitmap;
                        else {
                            //int iconResource = getResources().getIdentifier(iconIdentifier, "drawable", context.PPApplication.PACKAGE_NAME);
                            int iconResource = Profile.getIconResource(iconIdentifier);
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
                    if (Build.VERSION.SDK_INT < 26)
                        shortcutOverlayBitmap = BitmapManipulator.getBitmapFromResource(R.drawable.ic_shortcut_overlay, false, context);

                    if (ApplicationPreferences.applicationWidgetIconColor.equals("1")) {
                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("ShortcutCreatorListFragment.createShortcut", "applicationWidgetIconColor=1");
                            PPApplication.logE("ShortcutCreatorListFragment.createShortcut", "useCustomColor=" + useCustomColor);
                        }*/
                        if (isIconResourceID || useCustomColor) {
                            // icon is from resource or colored by custom color
                            int monochromeValue = 0xFF;
                            String applicationWidgetIconLightness = ApplicationPreferences.applicationWidgetIconLightness;
                            if (applicationWidgetIconLightness.equals("0")) monochromeValue = 0x00;
                            if (applicationWidgetIconLightness.equals("12")) monochromeValue = 0x20;
                            if (applicationWidgetIconLightness.equals("25")) monochromeValue = 0x40;
                            if (applicationWidgetIconLightness.equals("37")) monochromeValue = 0x60;
                            if (applicationWidgetIconLightness.equals("50")) monochromeValue = 0x80;
                            if (applicationWidgetIconLightness.equals("62")) monochromeValue = 0xA0;
                            if (applicationWidgetIconLightness.equals("75")) monochromeValue = 0xC0;
                            if (applicationWidgetIconLightness.equals("87")) monochromeValue = 0xE0;
                            //if (applicationWidgetIconLightness.equals("100")) monochromeValue = 0xFF;
                            profileBitmap = BitmapManipulator.monochromeBitmap(profileBitmap, monochromeValue/*, context*/);
                        } else {
                            float monochromeValue = 255f;
                            String applicationWidgetIconLightness = ApplicationPreferences.applicationWidgetIconLightness;
                            if (applicationWidgetIconLightness.equals("0")) monochromeValue = -255f;
                            if (applicationWidgetIconLightness.equals("12")) monochromeValue = -192f;
                            if (applicationWidgetIconLightness.equals("25")) monochromeValue = -128f;
                            if (applicationWidgetIconLightness.equals("37")) monochromeValue = -64f;
                            if (applicationWidgetIconLightness.equals("50")) monochromeValue = 0f;
                            if (applicationWidgetIconLightness.equals("62")) monochromeValue = 64f;
                            if (applicationWidgetIconLightness.equals("75")) monochromeValue = 128f;
                            if (applicationWidgetIconLightness.equals("87")) monochromeValue = 192f;
                            //if (applicationWidgetIconLightness.equals("100")) monochromeValue = 255f;
                            profileBitmap = BitmapManipulator.grayScaleBitmap(profileBitmap);
                            profileBitmap = BitmapManipulator.setBitmapBrightness(profileBitmap, monochromeValue);
                        }
                    }

                    //PPApplication.logE("ShortcutCreatorListFragment.createShortcut", "profileBitmap=" + profileBitmap);

                    if (Build.VERSION.SDK_INT < 26)
                        profileShortcutBitmap = fragment.combineImages(profileBitmap, shortcutOverlayBitmap);
                    else
                        profileShortcutBitmap = profileBitmap;
                    //intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, profileShortcutBitmap);
                    shortcutBuilderCompat.setIcon(IconCompat.createWithBitmap(profileShortcutBitmap));
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
                    if (profile != null) {
                        //PPApplication.logE("ShortcutCreatorListFragment.createShortcut", "create result intent");

                        //intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                        //context.sendBroadcast(intent);

                        ShortcutInfoCompat shortcutInfo = shortcutBuilderCompat.build();
                        Intent intent = ShortcutManagerCompat.createShortcutResultIntent(context, shortcutInfo);
                        fragment.getActivity().setResult(Activity.RESULT_OK, intent);
                    }

                    fragment.getActivity().finish();
                }
            }
        }

    }

}
