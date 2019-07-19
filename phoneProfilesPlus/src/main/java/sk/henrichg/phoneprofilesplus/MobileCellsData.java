package sk.henrichg.phoneprofilesplus;

class MobileCellsData {

    final int cellId;
    String name;
    boolean connected;
    final boolean _new;
    final long lastConnectedTime;
    final String lastRunningEvents;

    //MobileCellsData() {
    //}

    MobileCellsData(int cellId, String name, boolean connected, boolean _new, long lastConnectedTime, String lastRunningEvents)
    {
        this.cellId = cellId;
        this.name = name;
        this.connected = connected;
        this._new = _new;
        this.lastConnectedTime = lastConnectedTime;
        this.lastRunningEvents = lastRunningEvents;
    }

}
