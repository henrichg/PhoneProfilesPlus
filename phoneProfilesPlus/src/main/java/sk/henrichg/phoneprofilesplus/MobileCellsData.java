package sk.henrichg.phoneprofilesplus;

public class MobileCellsData {

    public int cellId;
    public String name;
    public boolean connected;
    public boolean saved;
    public boolean _new;

    public MobileCellsData() {
    }

    public MobileCellsData(int cellId, String name, boolean connected, boolean saved, boolean _new)
    {
        this.cellId = cellId;
        this.name = name;
        this.connected = connected;
        this.saved = saved;
        this._new = _new;
    }

}
