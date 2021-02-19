package sk.henrichg.phoneprofilesplus;

class Geofence {

    long _id;
    double _latitude;
    double _longitude;
    float _radius;
    String _name;
    int _transition;

    static final int GEOFENCE_TRANSITION_ENTER = 1;
    static final int GEOFENCE_TRANSITION_EXIT = 2;

    Geofence() {}

}
