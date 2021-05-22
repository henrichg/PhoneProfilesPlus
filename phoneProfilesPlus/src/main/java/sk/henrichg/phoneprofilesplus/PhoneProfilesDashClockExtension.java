package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import java.lang.ref.WeakReference;

public class PhoneProfilesDashClockExtension extends DashClockExtension {

    private DataWrapper dataWrapper;
    private static PhoneProfilesDashClockExtension instance;

    public PhoneProfilesDashClockExtension()
    {
        //instance = this;
    }

    public static PhoneProfilesDashClockExtension getInstance()
    {
        return instance;
    }

    @Override
    protected void onInitialize(boolean isReconnect) {
        super.onInitialize(isReconnect);

//        PPApplication.logE("[IN_LISTENER] DashClockExtension.onInitialize", "xxx");

        instance = this;

        //GlobalGUIRoutines.setLanguage(this);

        if (dataWrapper == null)
            dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);

        setUpdateWhenScreenOn(true);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

//        PPApplication.logE("[IN_LISTENER] DashClockExtension.onDestroy", "xxx");

        instance = null;
        /*if (dataWrapper != null)
            dataWrapper.invalidateDataWrapper();
        dataWrapper = null;*/
    }

    @Override
    protected void onUpdateData(int reason) {
//        PPApplication.logE("[IN_LISTENER] DashClockExtension.onUpdateData", "xxx");

        if (instance == null)
            return;

        if (dataWrapper == null)
            return;

        //final PhoneProfilesDashClockExtension _instance = instance;
        //final DataWrapper _dataWrapper = dataWrapper;

        PPApplication.startHandlerThreadWidget();
        final Handler __handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
        __handler.post(new PPHandlerThreadRunnable(dataWrapper.context, dataWrapper) {
            @Override
            public void run() {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThreadWidget", "START run - from=PhoneProfilesDashClockExtension.onUpdateData");

                Context appContext= appContextWeakRef.get();
                DataWrapper dataWrapper = dataWrapperWeakRef.get();

                if ((appContext != null) && (dataWrapper != null) && (instance != null)) {

                    try {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesDashClockExtension.onUpdateData", "do it");

                        //profile = Profile.getMappedProfile(
                        //                            _dataWrapper.getActivatedProfile(true, false), this);
                        Profile profile = dataWrapper.getActivatedProfile(true, false);

                        boolean isIconResourceID;
                        String iconIdentifier;
                        String profileName;
                        if (profile != null) {
                            isIconResourceID = profile.getIsIconResourceID();
                            iconIdentifier = profile.getIconIdentifier();
                            profileName = DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", false, false, false, dataWrapper);
                        } else {
                            isIconResourceID = true;
                            iconIdentifier = Profile.PROFILE_ICON_DEFAULT;
                            profileName = PhoneProfilesDashClockExtension.this.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                        }
                        int iconResource;
                        if (isIconResourceID)
                            //iconResource = getResources().getIdentifier(iconIdentifier, "drawable", PPApplication.PACKAGE_NAME);
                            iconResource = Profile.getIconResource(iconIdentifier);
                        else
                            //iconResource = getResources().getIdentifier(Profile.PROFILE_ICON_DEFAULT, "drawable", PPApplication.PACKAGE_NAME);
                            iconResource = Profile.getIconResource(Profile.PROFILE_ICON_DEFAULT);

                        /////////////////////////////////////////////////////////////

                        if (instance != null) {
                            // intent
                            Intent intent = new Intent(instance, LauncherActivity.class);
                            // clear all opened activities
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);

                            String status = "";
                            //if (ApplicationPreferences.prefEventsBlocked) {
                            if (Event.getEventsBlocked(appContext)) {
                                if (Event.getForceRunEventRunning(appContext)) {
                        /*if (android.os.Build.VERSION.SDK_INT >= 16)
                            status = "\u23E9";
                        else*/
                                    status = "[»]";
                                } else {
                        /*if (android.os.Build.VERSION.SDK_INT >= 16)
                            status = "\uD83D\uDC46";
                        else */
                                    status = "[M]";
                                }
                            }

                            ProfilePreferencesIndicator indicators = new ProfilePreferencesIndicator();

                            // Publish the extension data update.
                            PhoneProfilesDashClockExtension.this.publishUpdate(new ExtensionData()
                                    .visible(true)
                                    .icon(iconResource)
                                    .status(status)
                                    .expandedTitle(profileName)
                                    .expandedBody(indicators.getString(profile, 25, instance))
                                    .contentDescription("PhoneProfilesPlus - " + profileName)
                                    .clickIntent(intent));
                        }
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
            }
        });
    }

    public void updateExtension()
    {
        onUpdateData(UPDATE_REASON_CONTENT_CHANGED);
    }

    private static abstract class PPHandlerThreadRunnable implements Runnable {

        public final WeakReference<Context> appContextWeakRef;
        public final WeakReference<DataWrapper> dataWrapperWeakRef;

        public PPHandlerThreadRunnable(Context appContext,
                                       DataWrapper dataWrapper) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.dataWrapperWeakRef = new WeakReference<>(dataWrapper);
        }

    }

}
