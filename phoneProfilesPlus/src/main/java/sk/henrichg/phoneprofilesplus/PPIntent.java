package sk.henrichg.phoneprofilesplus;

import android.os.Parcel;
import android.os.Parcelable;

class PPIntent implements Parcelable {
    long _id;
    String _name;
    String _packageName;
    String _className;
    String _action;
    String _data;
    String _mimeType;
    String _extraKey1;
    String _extraValue1;
    int _extraType1;
    String _extraKey2;
    String _extraValue2;
    int _extraType2;
    String _extraKey3;
    String _extraValue3;
    int _extraType3;
    String _extraKey4;
    String _extraValue4;
    int _extraType4;
    String _extraKey5;
    String _extraValue5;
    int _extraType5;
    String _extraKey6;
    String _extraValue6;
    int _extraType6;
    String _extraKey7;
    String _extraValue7;
    int _extraType7;
    String _extraKey8;
    String _extraValue8;
    int _extraType8;
    String _extraKey9;
    String _extraValue9;
    int _extraType9;
    String _extraKey10;
    String _extraValue10;
    int _extraType10;
    String _categories;
    String _flags;
    int _intentType;

    int _usedCount;
    boolean _doNotDelete;

    PPIntent() {}

    PPIntent(
        long id,
        String name,
        String packageName,
        String className,
        String action,
        String data,
        String mimeType,
        String extraKey1,
        String extraValue1,
        int extraType1,
        String extraKey2,
        String extraValue2,
        int extraType2,
        String extraKey3,
        String extraValue3,
        int extraType3,
        String extraKey4,
        String extraValue4,
        int extraType4,
        String extraKey5,
        String extraValue5,
        int extraType5,
        String extraKey6,
        String extraValue6,
        int extraType6,
        String extraKey7,
        String extraValue7,
        int extraType7,
        String extraKey8,
        String extraValue8,
        int extraType8,
        String extraKey9,
        String extraValue9,
        int extraType9,
        String extraKey10,
        String extraValue10,
        int extraType10,
        String categories,
        String flags,
        int usedCount,
        int intentType,
        boolean doNotDelete
    )
    {
        this._id = id;
        this._name = name;
        this._packageName = packageName;
        this._className = className;
        this._action = action;
        this._data = data;
        this._mimeType = mimeType;
        this._extraKey1 = extraKey1;
        this._extraValue1 = extraValue1;
        this._extraType1 = extraType1;
        this._extraKey2 = extraKey2;
        this._extraValue2 = extraValue2;
        this._extraType2 = extraType2;
        this._extraKey3 = extraKey3;
        this._extraValue3 = extraValue3;
        this._extraType3 = extraType3;
        this._extraKey4 = extraKey4;
        this._extraValue4 = extraValue4;
        this._extraType4 = extraType4;
        this._extraKey5 = extraKey5;
        this._extraValue5 = extraValue5;
        this._extraType5 = extraType5;
        this._extraKey6 = extraKey6;
        this._extraValue6 = extraValue6;
        this._extraType6 = extraType6;
        this._extraKey7 = extraKey7;
        this._extraValue7 = extraValue7;
        this._extraType7 = extraType7;
        this._extraKey8 = extraKey8;
        this._extraValue8 = extraValue8;
        this._extraType8 = extraType8;
        this._extraKey9 = extraKey9;
        this._extraValue9 = extraValue9;
        this._extraType9 = extraType9;
        this._extraKey10 = extraKey10;
        this._extraValue10 = extraValue10;
        this._extraType10 = extraType10;
        this._categories = categories;
        this._flags = flags;
        this._intentType = intentType;

        this._usedCount = usedCount;
        this._doNotDelete = doNotDelete;
    }

    private PPIntent(Parcel in) {
        this._id = in.readLong();
        this._name = in.readString();
        this._packageName = in.readString();
        this._className = in.readString();
        this._action = in.readString();
        this._data = in.readString();
        this._mimeType = in.readString();
        this._extraKey1 = in.readString();
        this._extraValue1 = in.readString();
        this._extraType1 = in.readInt();
        this._extraKey2 = in.readString();
        this._extraValue2 = in.readString();
        this._extraType2 = in.readInt();
        this._extraKey3 = in.readString();
        this._extraValue3 = in.readString();
        this._extraType3 = in.readInt();
        this._extraKey4 = in.readString();
        this._extraValue4 = in.readString();
        this._extraType4 = in.readInt();
        this._extraKey5 = in.readString();
        this._extraValue5 = in.readString();
        this._extraType5 = in.readInt();
        this._extraKey6 = in.readString();
        this._extraValue6 = in.readString();
        this._extraType6 = in.readInt();
        this._extraKey7 = in.readString();
        this._extraValue7 = in.readString();
        this._extraType7 = in.readInt();
        this._extraKey8 = in.readString();
        this._extraValue8 = in.readString();
        this._extraType8 = in.readInt();
        this._extraKey9 = in.readString();
        this._extraValue9 = in.readString();
        this._extraType9 = in.readInt();
        this._extraKey10 = in.readString();
        this._extraValue10 = in.readString();
        this._extraType10 = in.readInt();
        this._categories = in.readString();
        this._flags = in.readString();
        this._intentType = in.readInt();

        this._usedCount = in.readInt();
        this._doNotDelete = in.readInt() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this._id);
        dest.writeString(this._name);
        dest.writeString(this._packageName);
        dest.writeString(this._className);
        dest.writeString(this._action);
        dest.writeString(this._data);
        dest.writeString(this._mimeType);
        dest.writeString(this._extraKey1);
        dest.writeString(this._extraValue1);
        dest.writeInt(this._extraType1);
        dest.writeString(this._extraKey2);
        dest.writeString(this._extraValue2);
        dest.writeInt(this._extraType2);
        dest.writeString(this._extraKey3);
        dest.writeString(this._extraValue3);
        dest.writeInt(this._extraType3);
        dest.writeString(this._extraKey4);
        dest.writeString(this._extraValue4);
        dest.writeInt(this._extraType4);
        dest.writeString(this._extraKey5);
        dest.writeString(this._extraValue5);
        dest.writeInt(this._extraType5);
        dest.writeString(this._extraKey6);
        dest.writeString(this._extraValue6);
        dest.writeInt(this._extraType6);
        dest.writeString(this._extraKey7);
        dest.writeString(this._extraValue7);
        dest.writeInt(this._extraType7);
        dest.writeString(this._extraKey8);
        dest.writeString(this._extraValue8);
        dest.writeInt(this._extraType8);
        dest.writeString(this._extraKey9);
        dest.writeString(this._extraValue9);
        dest.writeInt(this._extraType9);
        dest.writeString(this._extraKey10);
        dest.writeString(this._extraValue10);
        dest.writeInt(this._extraType10);
        dest.writeString(this._categories);
        dest.writeString(this._flags);
        dest.writeInt(this._intentType);

        dest.writeInt(this._usedCount);
        dest.writeInt(this._doNotDelete ? 1 : 0);
    }

    PPIntent duplicate() {
        PPIntent newPPIntent = new PPIntent();
        newPPIntent._name = _name + "_d";
        newPPIntent._packageName = _packageName;
        newPPIntent._className = _className;
        newPPIntent._action = _action;
        newPPIntent._data = _data;
        newPPIntent._mimeType = _mimeType;
        newPPIntent._extraKey1 = _extraKey1;
        newPPIntent._extraValue1 = _extraValue1;
        newPPIntent._extraType1 = _extraType1;
        newPPIntent._extraKey2 = _extraKey2;
        newPPIntent._extraValue2 = _extraValue2;
        newPPIntent._extraType2 = _extraType2;
        newPPIntent._extraKey3 = _extraKey3;
        newPPIntent._extraValue3 = _extraValue3;
        newPPIntent._extraType3 = _extraType3;
        newPPIntent._extraKey4 = _extraKey4;
        newPPIntent._extraValue4 = _extraValue4;
        newPPIntent._extraType4 = _extraType4;
        newPPIntent._extraKey5 = _extraKey5;
        newPPIntent._extraValue5 = _extraValue5;
        newPPIntent._extraType5 = _extraType5;
        newPPIntent._extraKey6 = _extraKey6;
        newPPIntent._extraValue6 = _extraValue6;
        newPPIntent._extraType6 = _extraType6;
        newPPIntent._extraKey7 = _extraKey7;
        newPPIntent._extraValue7 = _extraValue7;
        newPPIntent._extraType7 = _extraType7;
        newPPIntent._extraKey8 = _extraKey8;
        newPPIntent._extraValue8 = _extraValue8;
        newPPIntent._extraType8 = _extraType8;
        newPPIntent._extraKey9 = _extraKey9;
        newPPIntent._extraValue9 = _extraValue9;
        newPPIntent._extraType9 = _extraType9;
        newPPIntent._extraKey10 = _extraKey10;
        newPPIntent._extraValue10 = _extraValue10;
        newPPIntent._extraType10 = _extraType10;
        newPPIntent._categories = _categories;
        newPPIntent._flags = _flags;
        newPPIntent._intentType = _intentType;

        newPPIntent._doNotDelete = _doNotDelete;

        return newPPIntent;
    }


    public static final Parcelable.Creator<PPIntent> CREATOR = new Parcelable.Creator<PPIntent>() {
        public PPIntent createFromParcel(Parcel source) {
            return new PPIntent(source);
        }

        public PPIntent[] newArray(int size) {
            return new PPIntent[size];
        }
    };

}
