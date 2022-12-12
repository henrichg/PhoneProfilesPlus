package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViewsService;

public class ProfileListWidgetServiceCompactGrid extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        //Log.e("ProfileListWidgetServiceCompactGrid.onGetViewFactory", "ProfileListWidgetServiceCompactGrid");
        return (new ProfileListWidgetFactoryCompactGrid(this.getBaseContext(), intent));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

}
