package sk.henrichg.phoneprofilesplus;

class NFCTag {
    final long _id;
    final String _uid;
    String _name;

    NFCTag(long id, String name, String uid)
    {
        this._id = id;
        this._uid = uid;
        this._name = name;
    }

}
