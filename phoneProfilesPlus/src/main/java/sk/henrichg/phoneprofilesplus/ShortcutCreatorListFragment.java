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
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ShortcutCreatorListFragment extends Fragment {

    private DataWrapper dataWrapper;
    private List<Profile> profileList;
    private ShortcutProfileListAdapter profileListAdapter;
    private ListView listView;

    private WeakReference<LoadProfileListAsyncTask> asyncTaskContext;

    public ShortcutCreatorListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        setRetainInstance(true);

        dataWrapper = new DataWrapper(getActivity().getApplicationContext(), true, false, 0);

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
        doOnViewCreated(view, savedInstanceState);
    }

    private void doOnViewCreated(View view, Bundle savedInstanceState)
    {
        listView = view.findViewById(R.id.shortcut_profiles_list);

        listView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                createShortcut(position);

            }

        });

        if (profileList == null)
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
            this.dataWrapper = new DataWrapper(fragment.getActivity().getApplicationContext(), true, false, 0);
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<Profile> profileList = this.dataWrapper.getProfileList();
            Collections.sort(profileList, new ProfileComparator());
            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);
            
            ShortcutCreatorListFragment fragment = this.fragmentWeakRef.get(); 
            
            if ((fragment != null) && (fragment.isAdded())) {

                // get local profileList
                List<Profile> profileList = this.dataWrapper.getProfileList();

                // add restart events
                Profile profile = DataWrapper.getNonInitializedProfile(this.dataWrapper.context.getString(R.string.menu_restart_events),
                                            "ic_action_events_restart_color", 0);
                profileList.add(0, profile);

                // set copy local profile list into activity profilesDataWrapper
                fragment.dataWrapper.setProfileList(profileList, false);
                // set reference of profile list from profilesDataWrapper
                fragment.profileList = fragment.dataWrapper.getProfileList();

                fragment.profileListAdapter = new ShortcutProfileListAdapter(fragment, fragment.profileList);
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

        profileList = null;

        if (dataWrapper != null)
            dataWrapper.invalidateDataWrapper();
        dataWrapper = null;

        super.onDestroy();
    }

    private void createShortcut(int position)
    {
        Profile profile = profileList.get(position);
        boolean isIconResourceID;
        String iconIdentifier;
        Bitmap profileBitmap;
        Bitmap shortcutOverlayBitmap;
        Bitmap profileShortcutBitmap;
        String profileName;
        boolean useCustomColor;

        if (profile != null) {
            isIconResourceID = profile.getIsIconResourceID();
            iconIdentifier = profile.getIconIdentifier();
            profileName = profile._name;
            useCustomColor = profile.getUseCustomColorForIcon();
        } else {
            isIconResourceID = true;
            iconIdentifier = Profile.PROFILE_ICON_DEFAULT;
            profileName = getResources().getString(R.string.profile_name_default);
            useCustomColor = false;
        }

        Intent shortcutIntent;
        if (position == 0) {
            // restart events
            //Log.e("ShortcutCreatorListFragment.createShortcut","restart events");
            shortcutIntent = new Intent(getActivity().getApplicationContext(), ActionForExternalApplicationActivity.class);
            shortcutIntent.setAction(ActionForExternalApplicationActivity.ACTION_RESTART_EVENTS);
        } else {
            //if (profile != null)
            //    Log.e("ShortcutCreatorListFragment.createShortcut","profile="+profile._name);
            //else
            //    Log.e("ShortcutCreatorListFragment.createShortcut","profile=null");
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
        shortcutBuilder.setLongLabel(profileName);

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

        if (ApplicationPreferences.applicationWidgetIconColor(dataWrapper.context).equals("1")) {
            int monochromeValue = 0xFF;
            String applicationWidgetIconLightness = ApplicationPreferences.applicationWidgetIconLightness(dataWrapper.context);
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
        if (ApplicationPreferences.applicationShortcutEmblem(dataWrapper.context))
            canvas.drawBitmap(bitmap2, 0f, 0f, null);

        return combined;
    }

}
