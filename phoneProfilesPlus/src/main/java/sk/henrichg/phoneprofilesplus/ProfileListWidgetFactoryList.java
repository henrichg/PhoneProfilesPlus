package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

class ProfileListWidgetFactoryList extends ProfileListWidgetFactory {

    ProfileListWidgetFactoryList(Context context, Intent intent) {
        super(context, intent);
        //Log.e("ProfileListWidgetFactoryList constuctor", "xxxxx");
    }

    public void onCreate() {
        //Log.e("ProfileListWidgetFactoryList.onCreate", "xxxx");
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.e("ProfileListWidgetFactoryList.getViewAt", "START");
        applicationWidgetListGridLayout = false;
        applicationWidgetListCompactGrid = false;

        return super.getViewAt(position);
    }

}
