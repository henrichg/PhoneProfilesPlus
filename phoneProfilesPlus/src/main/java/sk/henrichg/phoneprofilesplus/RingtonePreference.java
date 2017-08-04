package sk.henrichg.phoneprofilesplus;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class RingtonePreference extends Preference {

    private String ringtone;

    String ringtoneType;
    boolean showSilent;
    boolean showDefault;

    private Context prefContext;

    public RingtonePreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RingtonePreference);

        ringtoneType = typedArray.getString(R.styleable.RingtonePreference_ringtoneType);
        showSilent = typedArray.getBoolean(R.styleable.RingtonePreference_showSilent, false);
        showDefault = typedArray.getBoolean(R.styleable.RingtonePreference_showDefault, false);

        if (ringtoneType.equals("ringtone"))
            ringtone = Settings.System.DEFAULT_RINGTONE_URI.toString();
        else
        if (ringtoneType.equals("notification"))
            ringtone = Settings.System.DEFAULT_NOTIFICATION_URI.toString();
        else
        if (ringtoneType.equals("alarm"))
            ringtone = Settings.System.DEFAULT_ALARM_ALERT_URI.toString();

        prefContext = context;

        typedArray.recycle();

    }

    //@Override
    protected void onBindView(View view)
    {
        super.onBindView(view);
        _setSummary(ringtone);
    }

    @Override
    protected void onClick()
    {
        // klik na preference
        final RingtonePreferenceDialog dialog = new RingtonePreferenceDialog(prefContext, this, ringtone);
        dialog.show();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        super.onGetDefaultValue(a, index);

        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue) {
            // restore state
            String value;
            try {
                value = getPersistedString(ringtone);
            } catch  (Exception e) {
                value = ringtone;
            }
            ringtone = value;
        }
        else {
            // set state
            String value = (String) defaultValue;
            ringtone = value;
            persistString(value);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        // ulozime instance state - napriklad kvoli zmene orientacie

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            // netreba ukladat, je ulozene persistentne
            return superState;
        }*/

        // ulozenie istance state
        final SavedState myState = new SavedState(superState);
        myState.ringtone = ringtone;
        myState.ringtoneType = ringtoneType;
        myState.showSilent = showSilent;
        myState.showDefault = showDefault;
        return myState;

    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            _setSummary(ringtone);
            return;
        }

        // restore instance state
        SavedState myState = (SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        ringtone = myState.ringtone;
        ringtoneType = myState.ringtoneType;
        showSilent = myState.showSilent;
        showDefault = myState.showDefault;

        _setSummary(ringtone);
        notifyChanged();
    }

    @Override
    protected void onPrepareForRemoval()
    {
        super.onPrepareForRemoval();
    }

    void setRingtone(String newRingtone)
    {
        if (!callChangeListener(newRingtone)) {
            // nema sa nova hodnota zapisat
            return;
        }

        ringtone = newRingtone;

        // set summary
        _setSummary(ringtone);

        // zapis do preferences
        persistString(ringtone);

        // Data sa zmenili,notifikujeme
        notifyChanged();

    }

    public void _setSummary(String ringtone)
    {
        String ringtoneName = prefContext.getString(R.string.ringtone_preference_not_set);

        if (ringtone.equals(Settings.System.DEFAULT_RINGTONE_URI.toString()))
            ringtoneName = prefContext.getString(R.string.ringtone_preference_dialog_default_ringtone);
        else
        if (ringtone.equals(Settings.System.DEFAULT_NOTIFICATION_URI.toString()))
            ringtoneName = prefContext.getString(R.string.ringtone_preference_dialog_default_notification);
        else
        if (ringtone.equals(Settings.System.DEFAULT_ALARM_ALERT_URI.toString()))
            ringtoneName = prefContext.getString(R.string.ringtone_preference_dialog_default_alarm);
        else
        if (ringtone.isEmpty())
            ringtoneName = prefContext.getString(R.string.ringtone_preference_dialog_silent);
        else {
            try {
                Uri ringtoneUri = Uri.parse(ringtone);

                ContentResolver cr = getContext().getContentResolver();
                String[] projection = {MediaStore.MediaColumns.TITLE};
                //String title;
                Cursor cur = cr.query(ringtoneUri, projection, null, null, null);
                if (cur != null) {
                    if (cur.moveToFirst()) {
                        ringtoneName = cur.getString(0);
                    }
                    cur.close();
                }
            } catch (Exception ignored) {
            }
        }

        setSummary(ringtoneName);
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        private String ringtone;
        private String ringtoneType;
        private boolean showSilent;
        private boolean showDefault;

        SavedState(Parcel source)
        {
            super(source);

            // restore ringtone
            ringtone = source.readString();
            ringtoneType = source.readString();
            showSilent = source.readInt() == 1;
            showDefault = source.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            // save profileId
            dest.writeString(ringtone);
            dest.writeString(ringtoneType);
            dest.writeInt((showSilent) ? 1 : 0);
            dest.writeInt((showDefault) ? 1 : 0);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<SavedState> CREATOR =
                new Creator<RingtonePreference.SavedState>() {
            public RingtonePreference.SavedState createFromParcel(Parcel in)
            {
                return new RingtonePreference.SavedState(in);
            }
            public RingtonePreference.SavedState[] newArray(int size)
            {
                return new RingtonePreference.SavedState[size];
            }

        };

    }
}
