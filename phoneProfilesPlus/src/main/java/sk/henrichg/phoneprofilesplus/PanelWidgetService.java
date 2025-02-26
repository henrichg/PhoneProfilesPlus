package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViewsService;

public class PanelWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return(new PanelWidgetFactory(this.getBaseContext(), intent));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

}
