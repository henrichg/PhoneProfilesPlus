package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class ProfileListWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        return(new ProfileListWidgetFactory(this.getBaseContext(),
                intent));
    }

}
