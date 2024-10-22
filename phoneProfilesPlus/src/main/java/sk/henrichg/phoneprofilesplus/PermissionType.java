package sk.henrichg.phoneprofilesplus;

import android.os.Parcel;
import android.os.Parcelable;

class PermissionType implements Parcelable {
    final int type;
    final String permission;

    PermissionType (int type, String permission) {
        this.type = type;
        this.permission = permission;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeString(this.permission);
    }

    PermissionType(Parcel in) {
        this.type = in.readInt();
        this.permission = in.readString();
    }

    public static final Parcelable.Creator<PermissionType> CREATOR = new Parcelable.Creator<>() {
        public PermissionType createFromParcel(Parcel source) {
            return new PermissionType(source);
        }

        public PermissionType[] newArray(int size) {
            return new PermissionType[size];
        }
    };
}

