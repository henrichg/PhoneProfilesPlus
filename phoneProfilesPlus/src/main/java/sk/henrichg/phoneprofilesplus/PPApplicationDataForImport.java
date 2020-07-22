package sk.henrichg.phoneprofilesplus;

import android.os.Parcel;
import android.os.Parcelable;

public class PPApplicationDataForImport implements Parcelable {

    boolean applicationStartOnBoot;
    boolean applicationActivate;
    boolean applicationActivateWithAlert;
    boolean applicationClose;
    boolean applicationLongClickActivation;
    String applicationLanguage;
    String applicationTheme;
    boolean applicationActivatorPrefIndicator;
    boolean applicationEditorPrefIndicator;
    boolean applicationActivatorHeader;
    boolean applicationEditorHeader;
    boolean notificationsToast;
    boolean notificationStatusBar;
    boolean notificationStatusBarPermanent;
    String notificationStatusBarCancel;
    String notificationStatusBarStyle;
    boolean notificationShowInStatusBar;
    String notificationTextColor;
    boolean notificationHideInLockscreen;
    boolean applicationWidgetListPrefIndicator;
    boolean applicationWidgetListHeader;
    String applicationWidgetListBackground;
    String applicationWidgetListLightnessB;
    String applicationWidgetListLightnessT;
    String applicationWidgetIconColor;
    String applicationWidgetIconLightness;
    String applicationWidgetListIconColor;
    String applicationWidgetListIconLightness;
    boolean notificationPrefIndicator;
    String applicationBackgroundProfile;
    boolean applicationActivatorGridLayout;
    boolean applicationWidgetListGridLayout;
    boolean applicationWidgetIconHideProfileName;
    boolean applicationShortcutEmblem;
    String applicationWidgetIconBackground;
    String applicationWidgetIconLightnessB;
    String applicationWidgetIconLightnessT;
    boolean applicationUnlinkRingerNotificationVolumes;
    int applicationForceSetMergeRingNotificationVolumes;
    boolean applicationSamsungEdgeHeader;
    String applicationSamsungEdgeBackground;
    String applicationSamsungEdgeLightnessB;
    String applicationSamsungEdgeLightnessT;
    String applicationSamsungEdgeIconColor;
    String applicationSamsungEdgeIconLightness;
    boolean applicationWidgetListRoundedCorners;
    boolean applicationWidgetIconRoundedCorners;
    boolean applicationWidgetListBackgroundType;
    String applicationWidgetListBackgroundColor;
    boolean applicationWidgetIconBackgroundType;
    String applicationWidgetIconBackgroundColor;
    boolean applicationSamsungEdgeBackgroundType;
    String applicationSamsungEdgeBackgroundColor;
    boolean applicationNeverAskForGrantRoot;
    boolean notificationShowButtonExit;
    boolean applicationWidgetOneRowPrefIndicator;
    String applicationWidgetOneRowBackground;
    String applicationWidgetOneRowLightnessB;
    String applicationWidgetOneRowLightnessT;
    String applicationWidgetOneRowIconColor;
    String applicationWidgetOneRowIconLightness;
    boolean applicationWidgetOneRowRoundedCorners;
    boolean applicationWidgetOneRowBackgroundType;
    String applicationWidgetOneRowBackgroundColor;
    String applicationWidgetListLightnessBorder;
    String applicationWidgetOneRowLightnessBorder;
    String applicationWidgetIconLightnessBorder;
    boolean applicationWidgetListShowBorder;
    boolean applicationWidgetOneRowShowBorder;
    boolean applicationWidgetIconShowBorder;
    boolean applicationWidgetListCustomIconLightness;
    boolean applicationWidgetOneRowCustomIconLightness;
    boolean applicationWidgetIconCustomIconLightness;
    boolean applicationSamsungEdgeCustomIconLightness;
    boolean notificationUseDecoration;
    String notificationLayoutType;
    String notificationBackgroundColor;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.applicationStartOnBoot ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationActivate ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationActivateWithAlert ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationClose ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationLongClickActivation ? (byte) 1 : (byte) 0);
        dest.writeString(this.applicationLanguage);
        dest.writeString(this.applicationTheme);
        dest.writeByte(this.applicationActivatorPrefIndicator ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationEditorPrefIndicator ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationActivatorHeader ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationEditorHeader ? (byte) 1 : (byte) 0);
        dest.writeByte(this.notificationsToast ? (byte) 1 : (byte) 0);
        dest.writeByte(this.notificationStatusBar ? (byte) 1 : (byte) 0);
        dest.writeByte(this.notificationStatusBarPermanent ? (byte) 1 : (byte) 0);
        dest.writeString(this.notificationStatusBarCancel);
        dest.writeString(this.notificationStatusBarStyle);
        dest.writeByte(this.notificationShowInStatusBar ? (byte) 1 : (byte) 0);
        dest.writeString(this.notificationTextColor);
        dest.writeByte(this.notificationHideInLockscreen ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationWidgetListPrefIndicator ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationWidgetListHeader ? (byte) 1 : (byte) 0);
        dest.writeString(this.applicationWidgetListBackground);
        dest.writeString(this.applicationWidgetListLightnessB);
        dest.writeString(this.applicationWidgetListLightnessT);
        dest.writeString(this.applicationWidgetIconColor);
        dest.writeString(this.applicationWidgetIconLightness);
        dest.writeString(this.applicationWidgetListIconColor);
        dest.writeString(this.applicationWidgetListIconLightness);
        dest.writeByte(this.notificationPrefIndicator ? (byte) 1 : (byte) 0);
        dest.writeString(this.applicationBackgroundProfile);
        dest.writeByte(this.applicationActivatorGridLayout ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationWidgetListGridLayout ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationWidgetIconHideProfileName ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationShortcutEmblem ? (byte) 1 : (byte) 0);
        dest.writeString(this.applicationWidgetIconBackground);
        dest.writeString(this.applicationWidgetIconLightnessB);
        dest.writeString(this.applicationWidgetIconLightnessT);
        dest.writeByte(this.applicationUnlinkRingerNotificationVolumes ? (byte) 1 : (byte) 0);
        dest.writeInt(this.applicationForceSetMergeRingNotificationVolumes);
        dest.writeByte(this.applicationSamsungEdgeHeader ? (byte) 1 : (byte) 0);
        dest.writeString(this.applicationSamsungEdgeBackground);
        dest.writeString(this.applicationSamsungEdgeLightnessB);
        dest.writeString(this.applicationSamsungEdgeLightnessT);
        dest.writeString(this.applicationSamsungEdgeIconColor);
        dest.writeString(this.applicationSamsungEdgeIconLightness);
        dest.writeByte(this.applicationWidgetListRoundedCorners ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationWidgetIconRoundedCorners ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationWidgetListBackgroundType ? (byte) 1 : (byte) 0);
        dest.writeString(this.applicationWidgetListBackgroundColor);
        dest.writeByte(this.applicationWidgetIconBackgroundType ? (byte) 1 : (byte) 0);
        dest.writeString(this.applicationWidgetIconBackgroundColor);
        dest.writeByte(this.applicationSamsungEdgeBackgroundType ? (byte) 1 : (byte) 0);
        dest.writeString(this.applicationSamsungEdgeBackgroundColor);
        dest.writeByte(this.applicationNeverAskForGrantRoot ? (byte) 1 : (byte) 0);
        dest.writeByte(this.notificationShowButtonExit ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationWidgetOneRowPrefIndicator ? (byte) 1 : (byte) 0);
        dest.writeString(this.applicationWidgetOneRowBackground);
        dest.writeString(this.applicationWidgetOneRowLightnessB);
        dest.writeString(this.applicationWidgetOneRowLightnessT);
        dest.writeString(this.applicationWidgetOneRowIconColor);
        dest.writeString(this.applicationWidgetOneRowIconLightness);
        dest.writeByte(this.applicationWidgetOneRowRoundedCorners ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationWidgetOneRowBackgroundType ? (byte) 1 : (byte) 0);
        dest.writeString(this.applicationWidgetOneRowBackgroundColor);
        dest.writeString(this.applicationWidgetListLightnessBorder);
        dest.writeString(this.applicationWidgetOneRowLightnessBorder);
        dest.writeString(this.applicationWidgetIconLightnessBorder);
        dest.writeByte(this.applicationWidgetListShowBorder ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationWidgetOneRowShowBorder ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationWidgetIconShowBorder ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationWidgetListCustomIconLightness ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationWidgetOneRowCustomIconLightness ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationWidgetIconCustomIconLightness ? (byte) 1 : (byte) 0);
        dest.writeByte(this.applicationSamsungEdgeCustomIconLightness ? (byte) 1 : (byte) 0);
        dest.writeByte(this.notificationUseDecoration ? (byte) 1 : (byte) 0);
        dest.writeString(this.notificationLayoutType);
        dest.writeString(this.notificationBackgroundColor);
    }

    public PPApplicationDataForImport() {
    }

    protected PPApplicationDataForImport(Parcel in) {
        this.applicationStartOnBoot = in.readByte() != 0;
        this.applicationActivate = in.readByte() != 0;
        this.applicationActivateWithAlert = in.readByte() != 0;
        this.applicationClose = in.readByte() != 0;
        this.applicationLongClickActivation = in.readByte() != 0;
        this.applicationLanguage = in.readString();
        this.applicationTheme = in.readString();
        this.applicationActivatorPrefIndicator = in.readByte() != 0;
        this.applicationEditorPrefIndicator = in.readByte() != 0;
        this.applicationActivatorHeader = in.readByte() != 0;
        this.applicationEditorHeader = in.readByte() != 0;
        this.notificationsToast = in.readByte() != 0;
        this.notificationStatusBar = in.readByte() != 0;
        this.notificationStatusBarPermanent = in.readByte() != 0;
        this.notificationStatusBarCancel = in.readString();
        this.notificationStatusBarStyle = in.readString();
        this.notificationShowInStatusBar = in.readByte() != 0;
        this.notificationTextColor = in.readString();
        this.notificationHideInLockscreen = in.readByte() != 0;
        this.applicationWidgetListPrefIndicator = in.readByte() != 0;
        this.applicationWidgetListHeader = in.readByte() != 0;
        this.applicationWidgetListBackground = in.readString();
        this.applicationWidgetListLightnessB = in.readString();
        this.applicationWidgetListLightnessT = in.readString();
        this.applicationWidgetIconColor = in.readString();
        this.applicationWidgetIconLightness = in.readString();
        this.applicationWidgetListIconColor = in.readString();
        this.applicationWidgetListIconLightness = in.readString();
        this.notificationPrefIndicator = in.readByte() != 0;
        this.applicationBackgroundProfile = in.readString();
        this.applicationActivatorGridLayout = in.readByte() != 0;
        this.applicationWidgetListGridLayout = in.readByte() != 0;
        this.applicationWidgetIconHideProfileName = in.readByte() != 0;
        this.applicationShortcutEmblem = in.readByte() != 0;
        this.applicationWidgetIconBackground = in.readString();
        this.applicationWidgetIconLightnessB = in.readString();
        this.applicationWidgetIconLightnessT = in.readString();
        this.applicationUnlinkRingerNotificationVolumes = in.readByte() != 0;
        this.applicationForceSetMergeRingNotificationVolumes = in.readInt();
        this.applicationSamsungEdgeHeader = in.readByte() != 0;
        this.applicationSamsungEdgeBackground = in.readString();
        this.applicationSamsungEdgeLightnessB = in.readString();
        this.applicationSamsungEdgeLightnessT = in.readString();
        this.applicationSamsungEdgeIconColor = in.readString();
        this.applicationSamsungEdgeIconLightness = in.readString();
        this.applicationWidgetListRoundedCorners = in.readByte() != 0;
        this.applicationWidgetIconRoundedCorners = in.readByte() != 0;
        this.applicationWidgetListBackgroundType = in.readByte() != 0;
        this.applicationWidgetListBackgroundColor = in.readString();
        this.applicationWidgetIconBackgroundType = in.readByte() != 0;
        this.applicationWidgetIconBackgroundColor = in.readString();
        this.applicationSamsungEdgeBackgroundType = in.readByte() != 0;
        this.applicationSamsungEdgeBackgroundColor = in.readString();
        this.applicationNeverAskForGrantRoot = in.readByte() != 0;
        this.notificationShowButtonExit = in.readByte() != 0;
        this.applicationWidgetOneRowPrefIndicator = in.readByte() != 0;
        this.applicationWidgetOneRowBackground = in.readString();
        this.applicationWidgetOneRowLightnessB = in.readString();
        this.applicationWidgetOneRowLightnessT = in.readString();
        this.applicationWidgetOneRowIconColor = in.readString();
        this.applicationWidgetOneRowIconLightness = in.readString();
        this.applicationWidgetOneRowRoundedCorners = in.readByte() != 0;
        this.applicationWidgetOneRowBackgroundType = in.readByte() != 0;
        this.applicationWidgetOneRowBackgroundColor = in.readString();
        this.applicationWidgetListLightnessBorder = in.readString();
        this.applicationWidgetOneRowLightnessBorder = in.readString();
        this.applicationWidgetIconLightnessBorder = in.readString();
        this.applicationWidgetListShowBorder = in.readByte() != 0;
        this.applicationWidgetOneRowShowBorder = in.readByte() != 0;
        this.applicationWidgetIconShowBorder = in.readByte() != 0;
        this.applicationWidgetListCustomIconLightness = in.readByte() != 0;
        this.applicationWidgetOneRowCustomIconLightness = in.readByte() != 0;
        this.applicationWidgetIconCustomIconLightness = in.readByte() != 0;
        this.applicationSamsungEdgeCustomIconLightness = in.readByte() != 0;
        this.notificationUseDecoration = in.readByte() != 0;
        this.notificationLayoutType = in.readString();
        this.notificationBackgroundColor = in.readString();
    }

    public static final Parcelable.Creator<PPApplicationDataForImport> CREATOR = new Parcelable.Creator<PPApplicationDataForImport>() {
        @Override
        public PPApplicationDataForImport createFromParcel(Parcel source) {
            return new PPApplicationDataForImport(source);
        }

        @Override
        public PPApplicationDataForImport[] newArray(int size) {
            return new PPApplicationDataForImport[size];
        }
    };
}
