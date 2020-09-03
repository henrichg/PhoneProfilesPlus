package sk.henrichg.phoneprofilesimport;

import android.os.Parcel;
import android.os.Parcelable;

public class PPShortcutForExport implements Parcelable {

    public long KEY_S_ID;
    public String KEY_S_INTENT;
    public String KEY_S_NAME;

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

    @SuppressWarnings("unused")
    public PPShortcutForExport() {
    }

    protected PPShortcutForExport(Parcel in) {
        this.KEY_S_ID = in.readLong();
        this.KEY_S_INTENT = in.readString();
        this.KEY_S_NAME = in.readString();
    }

    public static final Parcelable.Creator<PPShortcutForExport> CREATOR = new Parcelable.Creator<PPShortcutForExport>() {
        @Override
        public PPShortcutForExport createFromParcel(Parcel source) {
            return new PPShortcutForExport(source);
        }

        @Override
        public PPShortcutForExport[] newArray(int size) {
            return new PPShortcutForExport[size];
        }
    };
}
