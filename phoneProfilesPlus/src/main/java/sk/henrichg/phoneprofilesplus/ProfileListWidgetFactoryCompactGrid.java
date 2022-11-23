package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

class ProfileListWidgetFactoryCompactGrid extends ProfileListWidgetFactory {

    ProfileListWidgetFactoryCompactGrid(Context context, @SuppressWarnings("unused") Intent intent) {
        super(context, intent);
        //Log.e("ProfileListWidgetFactoryCompactGrid constuctor", "xxxxx");
    }

    public void onCreate() {
        //Log.e("ProfileListWidgetFactoryCompactGrid.onCreate", "xxxx");
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.e("ProfileListWidgetFactoryCompactGrid.getViewAt", "START");
        applicationWidgetListGridLayout = true;
        applicationWidgetListCompactGrid = true;

        return super.getViewAt(position);
    }

}
