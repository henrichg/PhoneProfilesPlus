package sk.henrichg.phoneprofilesplus;

class EventTimeline {

    long _id;
    long _fkEvent;
    long _fkProfileEndActivated;
    @SuppressWarnings("unused")
    int _eorder; // is required !!! used is in DatabaseHandler as field KEY_ET_EORDER

    EventTimeline() {}

}
