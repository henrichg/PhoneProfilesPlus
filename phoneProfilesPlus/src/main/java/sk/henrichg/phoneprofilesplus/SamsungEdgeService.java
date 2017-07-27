package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class SamsungEdgeService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return(new SamsungEdgeFactory(this.getBaseContext(), intent));
    }

}
