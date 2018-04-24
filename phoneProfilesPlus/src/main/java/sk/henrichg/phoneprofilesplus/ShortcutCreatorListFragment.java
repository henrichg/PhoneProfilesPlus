package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.pm.ShortcutInfoCompat;
import android.support.v4.content.pm.ShortcutManagerCompat;
import android.support.v4.graphics.drawable.IconCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;

public class ShortcutCreatorListFragment extends Fragment {

    private DataWrapper activityDataWrapper;
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
        setRetainInstance(true);

        activityDataWrapper = new DataWrapper(getActivity().getApplicationContext(), false, 0);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;

        rootView = inflater.inflate(R.layout.shortcut_creator_list, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        doOnViewCreated(view);
    }

    private void doOnViewCreated(View view/*, Bundle savedInstanceState*/)
    {
        listView = view.findViewById(R.id.shortcut_profiles_list);
        textViewNoData = view.findViewById(R.id.shortcut_profiles_list_empty);
        progressBar = view.findViewById(R.id.shortcut_profiles_list_linla_progress);

        listView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                createShortcut(position);

            }

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

        private class ProfileComparator implements Comparator<Profile> {
            public int compare(Profile lhs, Profile rhs) {
                if (GlobalGUIRoutines.collator != null)
                    return GlobalGUIRoutines.collator.compare(lhs._name, rhs._name);
                else
                    return 0;
            }
        }

        private LoadProfileListAsyncTask (ShortcutCreatorListFragment fragment) {
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.dataWrapper = new DataWrapper(fragment.getActivity().getApplicationContext(), false, 0);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            ShortcutCreatorListFragment fragment = this.fragmentWeakRef.get();

            if ((fragment != null) && (fragment.isAdded())) {
                fragment.textViewNoData.setVisibility(View.GONE);
                fragment.progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            this.dataWrapper.fillProfileList(true, ApplicationPreferences.applicationActivatorPrefIndicator(this.dataWrapper.context));
            Collections.sort(this.dataWrapper.profileList, new ProfileComparator());

            // add restart events
            Profile profile = DataWrapper.getNonInitializedProfile(this.dataWrapper.context.getString(R.string.menu_restart_events),
                    this.dataWrapper.context.getResources().getResourceEntryName(R.drawable.ic_action_events_restart_color)+"|1|0|0", 0);
            this.dataWrapper.profileList.add(0, profile);

            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);
            
            ShortcutCreatorListFragment fragment = this.fragmentWeakRef.get(); 
            
            if ((fragment != null) && (fragment.isAdded())) {
                fragment.progressBar.setVisibility(View.GONE);

                // get local profileList
                this.dataWrapper.fillProfileList(true, ApplicationPreferences.applicationActivatorPrefIndicator(this.dataWrapper.context));

                // set copy local profile list into activity profilesDataWrapper
                fragment.activityDataWrapper.copyProfileList(this.dataWrapper);

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
        if (isAsyncTaskPendingOrRunning()) {
            this.asyncTaskContext.get().cancel(true);
        }

        if (listView != null)
            listView.setAdapter(null);
        if (profileListAdapter != null)
            profileListAdapter.release();

        if (activityDataWrapper != null)
            activityDataWrapper.invalidateDataWrapper();
        activityDataWrapper = null;

        super.onDestroy();
    }

    private void createShortcut(int position)
    {
        Profile profile = activityDataWrapper.profileList.get(position);
        boolean isIconResourceID;
        String iconIdentifier;
        Bitmap profileBitmap;
        Bitmap shortcutOverlayBitmap;
        Bitmap profileShortcutBitmap;
        String profileName;
        String longLabel;
        boolean useCustomColor;

        if (profile != null) {
            isIconResourceID = profile.getIsIconResourceID();
            iconIdentifier = profile.getIconIdentifier();
            profileName = profile._name;
            longLabel = getString(R.string.shortcut_activate_profile) + profileName;
            useCustomColor = profile.getUseCustomColorForIcon();
            if (position == 0) {
                profileName = getResources().getString(R.string.menu_restart_events);
                longLabel = profileName;
            }
        } else {
            isIconResourceID = true;
            iconIdentifier = Profile.PROFILE_ICON_DEFAULT;
            profileName = getResources().getString(R.string.profile_name_default);
            longLabel = getString(R.string.shortcut_activate_profile) + profileName;
            useCustomColor = false;
        }
        if (profileName.isEmpty())
            profileName = " ";

        Intent shortcutIntent;
        if (position == 0) {
            // restart events
            shortcutIntent = new Intent(getActivity().getApplicationContext(), ActionForExternalApplicationActivity.class);
            shortcutIntent.setAction(ActionForExternalApplicationActivity.ACTION_RESTART_EVENTS);
        } else {
            shortcutIntent = new Intent(getActivity().getApplicationContext(), BackgroundActivateProfileActivity.class);
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            shortcutIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
            //noinspection ConstantConditions
            shortcutIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
        }

        /*
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, profileName);
        */

        ShortcutInfoCompat.Builder shortcutBuilder = new ShortcutInfoCompat.Builder(getActivity(), "profile_shortcut");
        shortcutBuilder.setIntent(shortcutIntent);
        shortcutBuilder.setShortLabel(profileName);
        shortcutBuilder.setLongLabel(longLabel);

        if (isIconResourceID) {
            //noinspection ConstantConditions
            if (profile._iconBitmap != null)
                profileBitmap = profile._iconBitmap;
            else {
                int iconResource = getResources().getIdentifier(iconIdentifier, "drawable", getActivity().getPackageName());
                profileBitmap = BitmapFactory.decodeResource(getResources(), iconResource);
            }
            shortcutOverlayBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_shortcut_overlay);
        } else {
            Resources resources = getResources();
            int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
            int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
            //Log.d("---- ShortcutCreatorListFragment.generateIconBitmap","resampleBitmapUri");
            profileBitmap = BitmapManipulator.resampleBitmapUri(iconIdentifier, width, height, getActivity().getApplicationContext());
            if (profileBitmap == null) {
                int iconResource = R.drawable.ic_profile_default;
                profileBitmap = BitmapFactory.decodeResource(getResources(), iconResource);
            }
            shortcutOverlayBitmap = BitmapManipulator.resampleResource(resources, R.drawable.ic_shortcut_overlay, width, height);
        }

        if (ApplicationPreferences.applicationWidgetIconColor(activityDataWrapper.context).equals("1")) {
            int monochromeValue = 0xFF;
            String applicationWidgetIconLightness = ApplicationPreferences.applicationWidgetIconLightness(activityDataWrapper.context);
            if (applicationWidgetIconLightness.equals("0")) monochromeValue = 0x00;
            if (applicationWidgetIconLightness.equals("25")) monochromeValue = 0x40;
            if (applicationWidgetIconLightness.equals("50")) monochromeValue = 0x80;
            if (applicationWidgetIconLightness.equals("75")) monochromeValue = 0xC0;
            if (applicationWidgetIconLightness.equals("100")) monochromeValue = 0xFF;

            if (isIconResourceID || useCustomColor) {
                // icon is from resource or colored by custom color
                profileBitmap = BitmapManipulator.monochromeBitmap(profileBitmap, monochromeValue/*, getActivity().getBaseContext()*/);
            } else
                profileBitmap = BitmapManipulator.grayScaleBitmap(profileBitmap);
        }

        profileShortcutBitmap = combineImages(profileBitmap, shortcutOverlayBitmap);
        //intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, profileShortcutBitmap);
        shortcutBuilder.setIcon(IconCompat.createWithBitmap(profileShortcutBitmap));

        //intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        //getActivity().getApplicationContext().sendBroadcast(intent);

        ShortcutInfoCompat shortcutInfo = shortcutBuilder.build();
        Intent intent = ShortcutManagerCompat.createShortcutResultIntent(getActivity(), shortcutInfo);

        getActivity().setResult(Activity.RESULT_OK, intent);

        getActivity().finish();
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
        if (ApplicationPreferences.applicationShortcutEmblem(activityDataWrapper.context))
            canvas.drawBitmap(bitmap2, 0f, 0f, null);

        return combined;
    }

}
