package sk.henrichg.phoneprofilesplus;

class MobileCellsData {

    int cellId;
    long cellIdLong;
    String name;
    boolean connected;
    boolean _new;
    long lastConnectedTime;
    //String lastRunningEvents;
    //String lastPausedEvents;
    //boolean doNotDetect;

    // constructor is required for GSon !!!
    @SuppressWarnings("unused")
    MobileCellsData() {
    }

    MobileCellsData(int cellId, long celIdLong, String name, boolean connected, boolean _new, long lastConnectedTime/*,
                    String lastRunningEvents, String lastPausedEvents, boolean doNotDetect*/) {
        this.cellId = cellId;
        this.cellIdLong = celIdLong;
        this.name = name;
        this.connected = connected;
        this._new = _new;
        this.lastConnectedTime = lastConnectedTime;
        //this.lastRunningEvents = lastRunningEvents;
        //this.lastPausedEvents = lastPausedEvents;
        //this.doNotDetect = doNotDetect;
    }

}
