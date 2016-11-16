package sk.henrichg.phoneprofilesplus;

class MobileCellsData {

    int cellId;
    String name;
    boolean connected;
    boolean _new;

    //MobileCellsData() {
    //}

    MobileCellsData(int cellId, String name, boolean connected, boolean _new)
    {
        this.cellId = cellId;
        this.name = name;
        this.connected = connected;
        this._new = _new;
    }

}
