package sk.henrichg.phoneprofilesplus;

import android.os.Parcel;
import android.os.Parcelable;

public class PPShortcutForImport implements Parcelable {

    long KEY_S_ID;
    String KEY_S_INTENT;
    String KEY_S_NAME;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.KEY_S_ID);
        dest.writeString(this.KEY_S_INTENT);
        dest.writeString(this.KEY_S_NAME);
    }

    public PPShortcutForImport() {
    }

    protected PPShortcutForImport(Parcel in) {
        this.KEY_S_ID = in.readLong();
        this.KEY_S_INTENT = in.readString();
        this.KEY_S_NAME = in.readString();
    }

    public static final Parcelable.Creator<PPShortcutForImport> CREATOR = new Parcelable.Creator<PPShortcutForImport>() {
        @Override
        public PPShortcutForImport createFromParcel(Parcel source) {
            return new PPShortcutForImport(source);
        }

        @Override
        public PPShortcutForImport[] newArray(int size) {
            return new PPShortcutForImport[size];
        }
    };
}
