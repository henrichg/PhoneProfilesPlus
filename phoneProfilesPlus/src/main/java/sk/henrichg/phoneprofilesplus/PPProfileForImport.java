package sk.henrichg.phoneprofilesplus;

import android.os.Parcel;
import android.os.Parcelable;

public class PPProfileForImport implements Parcelable {

    long KEY_ID;
    String KEY_NAME;
    String KEY_ICON;
    boolean KEY_CHECKED;
    int KEY_PORDER;
    int KEY_VOLUME_RINGER_MODE;
    String KEY_VOLUME_RINGTONE;
    String KEY_VOLUME_NOTIFICATION;
    String KEY_VOLUME_MEDIA;
    String KEY_VOLUME_ALARM;
    String KEY_VOLUME_SYSTEM;
    String KEY_VOLUME_VOICE;
    int KEY_SOUND_RINGTONE_CHANGE;
    String KEY_SOUND_RINGTONE;
    int KEY_SOUND_NOTIFICATION_CHANGE;
    String KEY_SOUND_NOTIFICATION;
    int KEY_SOUND_ALARM_CHANGE;
    String KEY_SOUND_ALARM;
    int KEY_DEVICE_AIRPLANE_MODE;
    int KEY_DEVICE_WIFI;
    int KEY_DEVICE_BLUETOOTH;
    int KEY_DEVICE_SCREEN_TIMEOUT;
    String KEY_DEVICE_BRIGHTNESS;
    int KEY_DEVICE_WALLPAPER_CHANGE;
    String KEY_DEVICE_WALLPAPER;
    int KEY_DEVICE_MOBILE_DATA;
    int KEY_DEVICE_MOBILE_DATA_PREFS;
    int KEY_DEVICE_GPS;
    int KEY_DEVICE_RUN_APPLICATION_CHANGE;
    String KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME;
    int KEY_DEVICE_AUTOSYNC;
    int KEY_DEVICE_AUTOROTATE;
    int KEY_DEVICE_LOCATION_SERVICE_PREFS;
    int KEY_VOLUME_SPEAKER_PHONE;
    int KEY_DEVICE_NFC;
    int KEY_DURATION;
    int KEY_AFTER_DURATION_DO;
    int KEY_VOLUME_ZEN_MODE;
    int KEY_DEVICE_KEYGUARD;
    int KEY_VIBRATE_ON_TOUCH;
    int KEY_DEVICE_WIFI_AP;
    int KEY_DEVICE_POWER_SAVE_MODE;
    boolean KEY_ASK_FOR_DURATION;
    int KEY_DEVICE_NETWORK_TYPE;
    int KEY_NOTIFICATION_LED;
    int KEY_VIBRATE_WHEN_RINGING;
    int KEY_DEVICE_WALLPAPER_FOR;
    boolean KEY_HIDE_STATUS_BAR_ICON;
    int KEY_LOCK_DEVICE;
    String KEY_DEVICE_CONNECT_TO_SSID;
    String KEY_DURATION_NOTIFICATION_SOUND;
    boolean KEY_DURATION_NOTIFICATION_VIBRATE;
    int KEY_DEVICE_WIFI_AP_PREFS;
    int KEY_HEADS_UP_NOTIFICATIONS;
    int KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE;
    String KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME;
    long KEY_ACTIVATION_BY_USER_COUNT;
    int KEY_DEVICE_NETWORK_TYPE_PREFS;
    int KEY_DEVICE_CLOSE_ALL_APPLICATIONS;
    int KEY_SCREEN_NIGHT_MODE;
    int KEY_DTMF_TONE_WHEN_DIALING;
    int KEY_SOUND_ON_TOUCH;
    String KEY_VOLUME_DTMF;
    String KEY_VOLUME_ACCESSIBILITY;
    String KEY_VOLUME_BLUETOOTH_SCO;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.KEY_ID);
        dest.writeString(this.KEY_NAME);
        dest.writeString(this.KEY_ICON);
        dest.writeBoolean(this.KEY_CHECKED);
        dest.writeInt(this.KEY_PORDER);
        dest.writeInt(this.KEY_VOLUME_RINGER_MODE);
        dest.writeString(this.KEY_VOLUME_RINGTONE);
        dest.writeString(this.KEY_VOLUME_NOTIFICATION);
        dest.writeString(this.KEY_VOLUME_MEDIA);
        dest.writeString(this.KEY_VOLUME_ALARM);
        dest.writeString(this.KEY_VOLUME_SYSTEM);
        dest.writeString(this.KEY_VOLUME_VOICE);
        dest.writeInt(this.KEY_SOUND_RINGTONE_CHANGE);
        dest.writeString(this.KEY_SOUND_RINGTONE);
        dest.writeInt(this.KEY_SOUND_NOTIFICATION_CHANGE);
        dest.writeString(this.KEY_SOUND_NOTIFICATION);
        dest.writeInt(this.KEY_SOUND_ALARM_CHANGE);
        dest.writeString(this.KEY_SOUND_ALARM);
        dest.writeInt(this.KEY_DEVICE_AIRPLANE_MODE);
        dest.writeInt(this.KEY_DEVICE_WIFI);
        dest.writeInt(this.KEY_DEVICE_BLUETOOTH);
        dest.writeInt(this.KEY_DEVICE_SCREEN_TIMEOUT);
        dest.writeString(this.KEY_DEVICE_BRIGHTNESS);
        dest.writeInt(this.KEY_DEVICE_WALLPAPER_CHANGE);
        dest.writeString(this.KEY_DEVICE_WALLPAPER);
        dest.writeInt(this.KEY_DEVICE_MOBILE_DATA);
        dest.writeInt(this.KEY_DEVICE_MOBILE_DATA_PREFS);
        dest.writeInt(this.KEY_DEVICE_GPS);
        dest.writeInt(this.KEY_DEVICE_RUN_APPLICATION_CHANGE);
        dest.writeString(this.KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
        dest.writeInt(this.KEY_DEVICE_AUTOSYNC);
        dest.writeInt(this.KEY_DEVICE_AUTOROTATE);
        dest.writeInt(this.KEY_DEVICE_LOCATION_SERVICE_PREFS);
        dest.writeInt(this.KEY_VOLUME_SPEAKER_PHONE);
        dest.writeInt(this.KEY_DEVICE_NFC);
        dest.writeInt(this.KEY_DURATION);
        dest.writeInt(this.KEY_AFTER_DURATION_DO);
        dest.writeInt(this.KEY_VOLUME_ZEN_MODE);
        dest.writeInt(this.KEY_DEVICE_KEYGUARD);
        dest.writeInt(this.KEY_VIBRATE_ON_TOUCH);
        dest.writeInt(this.KEY_DEVICE_WIFI_AP);
        dest.writeInt(this.KEY_DEVICE_POWER_SAVE_MODE);
        dest.writeBoolean(this.KEY_ASK_FOR_DURATION);
        dest.writeInt(this.KEY_DEVICE_NETWORK_TYPE);
        dest.writeInt(this.KEY_NOTIFICATION_LED);
        dest.writeInt(this.KEY_VIBRATE_WHEN_RINGING);
        dest.writeInt(this.KEY_DEVICE_WALLPAPER_FOR);
        dest.writeBoolean(this.KEY_HIDE_STATUS_BAR_ICON);
        dest.writeInt(this.KEY_LOCK_DEVICE);
        dest.writeString(this.KEY_DEVICE_CONNECT_TO_SSID);
        dest.writeString(this.KEY_DURATION_NOTIFICATION_SOUND);
        dest.writeBoolean(this.KEY_DURATION_NOTIFICATION_VIBRATE);
        dest.writeInt(this.KEY_DEVICE_WIFI_AP_PREFS);
        dest.writeInt(this.KEY_HEADS_UP_NOTIFICATIONS);
        dest.writeInt(this.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
        dest.writeString(this.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME);
        dest.writeLong(this.KEY_ACTIVATION_BY_USER_COUNT);
        dest.writeInt(this.KEY_DEVICE_NETWORK_TYPE_PREFS);
        dest.writeInt(this.KEY_DEVICE_CLOSE_ALL_APPLICATIONS);
        dest.writeInt(this.KEY_SCREEN_NIGHT_MODE);
        dest.writeInt(this.KEY_DTMF_TONE_WHEN_DIALING);
        dest.writeInt(this.KEY_SOUND_ON_TOUCH);
        dest.writeString(this.KEY_VOLUME_DTMF);
        dest.writeString(this.KEY_VOLUME_ACCESSIBILITY);
        dest.writeString(this.KEY_VOLUME_BLUETOOTH_SCO);
    }

    public PPProfileForImport() {
    }

    protected PPProfileForImport(Parcel in) {
        this.KEY_ID = in.readLong();
        this.KEY_NAME = in.readString();
        this.KEY_ICON = in.readString();
        this.KEY_CHECKED = in.readBoolean();
        this.KEY_PORDER = in.readInt();
        this.KEY_VOLUME_RINGER_MODE = in.readInt();
        this.KEY_VOLUME_RINGTONE = in.readString();
        this.KEY_VOLUME_NOTIFICATION = in.readString();
        this.KEY_VOLUME_MEDIA = in.readString();
        this.KEY_VOLUME_ALARM = in.readString();
        this.KEY_VOLUME_SYSTEM = in.readString();
        this.KEY_VOLUME_VOICE = in.readString();
        this.KEY_SOUND_RINGTONE_CHANGE = in.readInt();
        this.KEY_SOUND_RINGTONE = in.readString();
        this.KEY_SOUND_NOTIFICATION_CHANGE = in.readInt();
        this.KEY_SOUND_NOTIFICATION = in.readString();
        this.KEY_SOUND_ALARM_CHANGE = in.readInt();
        this.KEY_SOUND_ALARM = in.readString();
        this.KEY_DEVICE_AIRPLANE_MODE = in.readInt();
        this.KEY_DEVICE_WIFI = in.readInt();
        this.KEY_DEVICE_BLUETOOTH = in.readInt();
        this.KEY_DEVICE_SCREEN_TIMEOUT = in.readInt();
        this.KEY_DEVICE_BRIGHTNESS = in.readString();
        this.KEY_DEVICE_WALLPAPER_CHANGE = in.readInt();
        this.KEY_DEVICE_WALLPAPER = in.readString();
        this.KEY_DEVICE_MOBILE_DATA = in.readInt();
        this.KEY_DEVICE_MOBILE_DATA_PREFS = in.readInt();
        this.KEY_DEVICE_GPS = in.readInt();
        this.KEY_DEVICE_RUN_APPLICATION_CHANGE = in.readInt();
        this.KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME = in.readString();
        this.KEY_DEVICE_AUTOSYNC = in.readInt();
        this.KEY_DEVICE_AUTOROTATE = in.readInt();
        this.KEY_DEVICE_LOCATION_SERVICE_PREFS = in.readInt();
        this.KEY_VOLUME_SPEAKER_PHONE = in.readInt();
        this.KEY_DEVICE_NFC = in.readInt();
        this.KEY_DURATION = in.readInt();
        this.KEY_AFTER_DURATION_DO = in.readInt();
        this.KEY_VOLUME_ZEN_MODE = in.readInt();
        this.KEY_DEVICE_KEYGUARD = in.readInt();
        this.KEY_VIBRATE_ON_TOUCH = in.readInt();
        this.KEY_DEVICE_WIFI_AP = in.readInt();
        this.KEY_DEVICE_POWER_SAVE_MODE = in.readInt();
        this.KEY_ASK_FOR_DURATION = in.readBoolean();
        this.KEY_DEVICE_NETWORK_TYPE = in.readInt();
        this.KEY_NOTIFICATION_LED = in.readInt();
        this.KEY_VIBRATE_WHEN_RINGING = in.readInt();
        this.KEY_DEVICE_WALLPAPER_FOR = in.readInt();
        this.KEY_HIDE_STATUS_BAR_ICON = in.readBoolean();
        this.KEY_LOCK_DEVICE = in.readInt();
        this.KEY_DEVICE_CONNECT_TO_SSID = in.readString();
        this.KEY_DURATION_NOTIFICATION_SOUND = in.readString();
        this.KEY_DURATION_NOTIFICATION_VIBRATE = in.readBoolean();
        this.KEY_DEVICE_WIFI_AP_PREFS = in.readInt();
        this.KEY_HEADS_UP_NOTIFICATIONS = in.readInt();
        this.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE = in.readInt();
        this.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME = in.readString();
        this.KEY_ACTIVATION_BY_USER_COUNT = in.readLong();
        this.KEY_DEVICE_NETWORK_TYPE_PREFS = in.readInt();
        this.KEY_DEVICE_CLOSE_ALL_APPLICATIONS = in.readInt();
        this.KEY_SCREEN_NIGHT_MODE = in.readInt();
        this.KEY_DTMF_TONE_WHEN_DIALING = in.readInt();
        this.KEY_SOUND_ON_TOUCH = in.readInt();
        this.KEY_VOLUME_DTMF = in.readString();
        this.KEY_VOLUME_ACCESSIBILITY = in.readString();
        this.KEY_VOLUME_BLUETOOTH_SCO = in.readString();
    }

    public static final Parcelable.Creator<PPProfileForImport> CREATOR = new Parcelable.Creator<PPProfileForImport>() {
        @Override
        public PPProfileForImport createFromParcel(Parcel source) {
            return new PPProfileForImport(source);
        }

        @Override
        public PPProfileForImport[] newArray(int size) {
            return new PPProfileForImport[size];
        }
    };
}
