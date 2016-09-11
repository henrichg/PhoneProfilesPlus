package sk.henrichg.phoneprofilesplus;

public class MobileCellsData {

    public int cellId;
    public String name;
    public boolean connected;
    public boolean _new;

    public MobileCellsData() {
    }

    public MobileCellsData(int cellId, String name, boolean connected, boolean _new)
    {
        this.cellId = cellId;
        this.name = name;
        this.connected = connected;
        this._new = _new;
    }

}
