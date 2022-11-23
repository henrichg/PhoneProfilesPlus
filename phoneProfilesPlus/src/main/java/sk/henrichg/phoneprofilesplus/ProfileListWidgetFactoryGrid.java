package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

class ProfileListWidgetFactoryGrid extends ProfileListWidgetFactory {

    ProfileListWidgetFactoryGrid(Context context, @SuppressWarnings("unused") Intent intent) {
        super(context, intent);
        //Log.e("ProfileListWidgetFactoryGrid constuctor", "xxxxx");
    }

    public void onCreate() {
        //Log.e("ProfileListWidgetFactoryGrid.onCreate", "xxxx");
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.e("ProfileListWidgetFactoryGrid.getViewAt", "START");
        applicationWidgetListGridLayout = true;
        applicationWidgetListCompactGrid = false;

        return super.getViewAt(position);
    }

}
