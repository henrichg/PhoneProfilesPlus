package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViewsService;

public class ProfileListWidgetServiceGrid extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        //Log.e("ProfileListWidgetServiceGrid.onGetViewFactory", "ProfileListWidgetServiceGrid");
        return (new ProfileListWidgetFactoryGrid(this.getBaseContext(), intent));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

}
