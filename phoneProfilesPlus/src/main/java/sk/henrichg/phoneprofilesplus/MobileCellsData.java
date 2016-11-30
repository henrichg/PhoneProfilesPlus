package sk.henrichg.phoneprofilesplus;

class MobileCellsData {

    int cellId;
    String name;
    boolean connected;
    boolean _new;
    long lastConnectedTime;

    //MobileCellsData() {
    //}

    MobileCellsData(int cellId, String name, boolean connected, boolean _new, long lastConnectedTime)
    {
        this.cellId = cellId;
        this.name = name;
        this.connected = connected;
        this._new = _new;
        this.lastConnectedTime = lastConnectedTime;
    }

}
