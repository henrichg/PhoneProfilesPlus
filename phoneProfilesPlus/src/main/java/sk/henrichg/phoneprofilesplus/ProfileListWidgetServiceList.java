package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViewsService;

public class ProfileListWidgetServiceList extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        //Log.e("ProfileListWidgetServiceList.onGetViewFactory", "ProfileListWidgetServiceList");
        return (new ProfileListWidgetFactoryList(this.getBaseContext(), intent));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

}
