package sk.henrichg.phoneprofilesplus;

public class MobileCellsData {

    public int cellId;
    public String name;
    public boolean connected;
    public boolean saved;

    public MobileCellsData() {
    }

    public MobileCellsData(int cellId, String name, boolean connected, boolean saved)
    {
        this.cellId = cellId;
        this.name = name;
        this.connected = connected;
        this.saved = saved;
    }

}
