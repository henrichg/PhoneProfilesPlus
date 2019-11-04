package sk.henrichg.phoneprofilesplus;

class NFCTag {
    long _id;
    String _uid;
    String _name;

    NFCTag() {}

    NFCTag(long id, String name, String uid)
    {
        this._id = id;
        this._uid = uid;
        this._name = name;
    }

}
