package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;

@TargetApi(23)
class ColorNotificationIcon {

    static Icon getFromBitmap(Bitmap bitmap) {
        return Icon.createWithBitmap(bitmap);
    }

}
