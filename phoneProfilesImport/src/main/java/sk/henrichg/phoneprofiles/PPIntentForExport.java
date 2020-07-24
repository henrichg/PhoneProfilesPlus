package sk.henrichg.phoneprofiles;

import android.os.Parcel;
import android.os.Parcelable;

public class PPIntentForExport implements Parcelable {

    public long KEY_IN_ID;
    public String KEY_IN_PACKAGE_NAME;
    public String KEY_IN_CLASS_NAME;
    public String KEY_IN_ACTION;
    public String KEY_IN_DATA;
    public String KEY_IN_MIME_TYPE;
    public String KEY_IN_EXTRA_KEY_1;
    public String KEY_IN_EXTRA_VALUE_1;
    public int KEY_IN_EXTRA_TYPE_1;
    public String KEY_IN_EXTRA_KEY_2;
    public String KEY_IN_EXTRA_VALUE_2;
    public int KEY_IN_EXTRA_TYPE_2;
    public String KEY_IN_EXTRA_KEY_3;
    public String KEY_IN_EXTRA_VALUE_3;
    public int KEY_IN_EXTRA_TYPE_3;
    public String KEY_IN_EXTRA_KEY_4;
    public String KEY_IN_EXTRA_VALUE_4;
    public int KEY_IN_EXTRA_TYPE_4;
    public String KEY_IN_EXTRA_KEY_5;
    public String KEY_IN_EXTRA_VALUE_5;
    public int KEY_IN_EXTRA_TYPE_5;
    public String KEY_IN_EXTRA_KEY_6;
    public String KEY_IN_EXTRA_VALUE_6;
    public int KEY_IN_EXTRA_TYPE_6;
    public String KEY_IN_EXTRA_KEY_7;
    public String KEY_IN_EXTRA_VALUE_7;
    public int KEY_IN_EXTRA_TYPE_7;
    public String KEY_IN_EXTRA_KEY_8;
    public String KEY_IN_EXTRA_VALUE_8;
    public int KEY_IN_EXTRA_TYPE_8;
    public String KEY_IN_EXTRA_KEY_9;
    public String KEY_IN_EXTRA_VALUE_9;
    public int KEY_IN_EXTRA_TYPE_9;
    public String KEY_IN_EXTRA_KEY_10;
    public String KEY_IN_EXTRA_VALUE_10;
    public int KEY_IN_EXTRA_TYPE_10;
    public String KEY_IN_CATEGORIES;
    public String KEY_IN_FLAGS;
    public String KEY_IN_NAME;
    public int KEY_IN_USED_COUNT;
    public int KEY_IN_INTENT_TYPE;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.KEY_IN_ID);
        dest.writeString(this.KEY_IN_PACKAGE_NAME);
        dest.writeString(this.KEY_IN_CLASS_NAME);
        dest.writeString(this.KEY_IN_ACTION);
        dest.writeString(this.KEY_IN_DATA);
        dest.writeString(this.KEY_IN_MIME_TYPE);
        dest.writeString(this.KEY_IN_EXTRA_KEY_1);
        dest.writeString(this.KEY_IN_EXTRA_VALUE_1);
        dest.writeInt(this.KEY_IN_EXTRA_TYPE_1);
        dest.writeString(this.KEY_IN_EXTRA_KEY_2);
        dest.writeString(this.KEY_IN_EXTRA_VALUE_2);
        dest.writeInt(this.KEY_IN_EXTRA_TYPE_2);
        dest.writeString(this.KEY_IN_EXTRA_KEY_3);
        dest.writeString(this.KEY_IN_EXTRA_VALUE_3);
        dest.writeInt(this.KEY_IN_EXTRA_TYPE_3);
        dest.writeString(this.KEY_IN_EXTRA_KEY_4);
        dest.writeString(this.KEY_IN_EXTRA_VALUE_4);
        dest.writeInt(this.KEY_IN_EXTRA_TYPE_4);
        dest.writeString(this.KEY_IN_EXTRA_KEY_5);
        dest.writeString(this.KEY_IN_EXTRA_VALUE_5);
        dest.writeInt(this.KEY_IN_EXTRA_TYPE_5);
        dest.writeString(this.KEY_IN_EXTRA_KEY_6);
        dest.writeString(this.KEY_IN_EXTRA_VALUE_6);
        dest.writeInt(this.KEY_IN_EXTRA_TYPE_6);
        dest.writeString(this.KEY_IN_EXTRA_KEY_7);
        dest.writeString(this.KEY_IN_EXTRA_VALUE_7);
        dest.writeInt(this.KEY_IN_EXTRA_TYPE_7);
        dest.writeString(this.KEY_IN_EXTRA_KEY_8);
        dest.writeString(this.KEY_IN_EXTRA_VALUE_8);
        dest.writeInt(this.KEY_IN_EXTRA_TYPE_8);
        dest.writeString(this.KEY_IN_EXTRA_KEY_9);
        dest.writeString(this.KEY_IN_EXTRA_VALUE_9);
        dest.writeInt(this.KEY_IN_EXTRA_TYPE_9);
        dest.writeString(this.KEY_IN_EXTRA_KEY_10);
        dest.writeString(this.KEY_IN_EXTRA_VALUE_10);
        dest.writeInt(this.KEY_IN_EXTRA_TYPE_10);
        dest.writeString(this.KEY_IN_CATEGORIES);
        dest.writeString(this.KEY_IN_FLAGS);
        dest.writeString(this.KEY_IN_NAME);
        dest.writeInt(this.KEY_IN_USED_COUNT);
        dest.writeInt(this.KEY_IN_INTENT_TYPE);
    }

    public PPIntentForExport() {
    }

    protected PPIntentForExport(Parcel in) {
        this.KEY_IN_ID = in.readLong();
        this.KEY_IN_PACKAGE_NAME = in.readString();
        this.KEY_IN_CLASS_NAME = in.readString();
        this.KEY_IN_ACTION = in.readString();
        this.KEY_IN_DATA = in.readString();
        this.KEY_IN_MIME_TYPE = in.readString();
        this.KEY_IN_EXTRA_KEY_1 = in.readString();
        this.KEY_IN_EXTRA_VALUE_1 = in.readString();
        this.KEY_IN_EXTRA_TYPE_1 = in.readInt();
        this.KEY_IN_EXTRA_KEY_2 = in.readString();
        this.KEY_IN_EXTRA_VALUE_2 = in.readString();
        this.KEY_IN_EXTRA_TYPE_2 = in.readInt();
        this.KEY_IN_EXTRA_KEY_3 = in.readString();
        this.KEY_IN_EXTRA_VALUE_3 = in.readString();
        this.KEY_IN_EXTRA_TYPE_3 = in.readInt();
        this.KEY_IN_EXTRA_KEY_4 = in.readString();
        this.KEY_IN_EXTRA_VALUE_4 = in.readString();
        this.KEY_IN_EXTRA_TYPE_4 = in.readInt();
        this.KEY_IN_EXTRA_KEY_5 = in.readString();
        this.KEY_IN_EXTRA_VALUE_5 = in.readString();
        this.KEY_IN_EXTRA_TYPE_5 = in.readInt();
        this.KEY_IN_EXTRA_KEY_6 = in.readString();
        this.KEY_IN_EXTRA_VALUE_6 = in.readString();
        this.KEY_IN_EXTRA_TYPE_6 = in.readInt();
        this.KEY_IN_EXTRA_KEY_7 = in.readString();
        this.KEY_IN_EXTRA_VALUE_7 = in.readString();
        this.KEY_IN_EXTRA_TYPE_7 = in.readInt();
        this.KEY_IN_EXTRA_KEY_8 = in.readString();
        this.KEY_IN_EXTRA_VALUE_8 = in.readString();
        this.KEY_IN_EXTRA_TYPE_8 = in.readInt();
        this.KEY_IN_EXTRA_KEY_9 = in.readString();
        this.KEY_IN_EXTRA_VALUE_9 = in.readString();
        this.KEY_IN_EXTRA_TYPE_9 = in.readInt();
        this.KEY_IN_EXTRA_KEY_10 = in.readString();
        this.KEY_IN_EXTRA_VALUE_10 = in.readString();
        this.KEY_IN_EXTRA_TYPE_10 = in.readInt();
        this.KEY_IN_CATEGORIES = in.readString();
        this.KEY_IN_FLAGS = in.readString();
        this.KEY_IN_NAME = in.readString();
        this.KEY_IN_USED_COUNT = in.readInt();
        this.KEY_IN_INTENT_TYPE = in.readInt();
    }

    public static final Parcelable.Creator<PPIntentForExport> CREATOR = new Parcelable.Creator<PPIntentForExport>() {
        @Override
        public PPIntentForExport createFromParcel(Parcel source) {
            return new PPIntentForExport(source);
        }

        @Override
        public PPIntentForExport[] newArray(int size) {
            return new PPIntentForExport[size];
        }
    };
}
