package sk.henrichg.phoneprofilesplus;

import android.app.WallpaperInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.wallpaper.WallpaperService;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import java.util.ArrayList;
import java.util.List;

public class LiveWallpapersDialogPreferenceX extends DialogPreference {

    LiveWallpapersDialogPreferenceFragmentX fragment;

    private final Context context;

    String value = "";
    private String defaultValue;
    private boolean savedInstanceState;

    //final int disableSharedProfile;

    List<LiveWallpapersData> liveWallpapersList;

    public LiveWallpapersDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        liveWallpapersList = new ArrayList<>();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        value = getPersistedString((String) defaultValue);
        this.defaultValue = (String)defaultValue;

        setSummaryLWDP();
    }

    private void setSummaryLWDP()
    {
        String prefSummary = context.getString(R.string.live_wallpapers_pref_dlg_summary_text_not_selected);
        if (!value.isEmpty()) {
            // get label for ComponentName in value

            PackageManager packageManager = context.getPackageManager();

            // get ResolveInfo of live wallpapers
            List<ResolveInfo> availableWallpapersList =
                    packageManager.queryIntentServices(
                            new Intent(WallpaperService.SERVICE_INTERFACE),
                            PackageManager.GET_META_DATA);

            for (int i = 0; i < availableWallpapersList.size(); i++) {
                ResolveInfo wallpaperResInfo = availableWallpapersList.get(i);

                WallpaperInfo info;
                try {
                    // WallaperInfo from ResolveInfo
                    info = new WallpaperInfo(context, wallpaperResInfo);
                    if (value.equals(info.getComponent().flattenToString())) {
                        prefSummary = wallpaperResInfo.loadLabel(packageManager).toString();
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        setSummary(prefSummary);
    }

    void persistValue() {
        // to get ComponentName from value, use ComponentName.unflattenFromString(value)

        persistString(value);
        setSummaryLWDP();
    }

    void resetSummary() {
        if (!savedInstanceState) {
            value = getPersistedString(defaultValue);
            setSummaryLWDP();
        }
        savedInstanceState = false;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final LiveWallpapersDialogPreferenceX.SavedState myState = new LiveWallpapersDialogPreferenceX.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!state.getClass().equals(LiveWallpapersDialogPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryLWDP();
            return;
        }

        // restore instance state
        LiveWallpapersDialogPreferenceX.SavedState myState = (LiveWallpapersDialogPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        setSummaryLWDP();
    }

    /*
    void refreshListView() {
        if (fragment != null)
            fragment.refreshListView();
    }
    */

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;
        String defaultValue;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readString();
            defaultValue = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(value);
            dest.writeString(defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<LiveWallpapersDialogPreferenceX.SavedState> CREATOR =
                new Creator<LiveWallpapersDialogPreferenceX.SavedState>() {
                    public LiveWallpapersDialogPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new LiveWallpapersDialogPreferenceX.SavedState(in);
                    }
                    public LiveWallpapersDialogPreferenceX.SavedState[] newArray(int size)
                    {
                        return new LiveWallpapersDialogPreferenceX.SavedState[size];
                    }

                };

    }

}
