package sk.henrichg.phoneprofilesplus;

public class MobileCellsData {

    public int cellId;
    public String name;
    public boolean registered;

    public MobileCellsData() {
    }

    public MobileCellsData(int cellId, String name, boolean registered)
    {
        this.cellId = cellId;
        this.name = name;
        this.registered = registered;
    }

}
