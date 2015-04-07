package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.afollestad.materialdialogs.MaterialDialog;

public class ImageViewPreferenceDialog  {

    private ImageViewPreference imageViewPreference;
    private String imageSource;
    MaterialDialog dialog;

    public ImageViewPreferenceDialog(Context context, ImageViewPreference preference, String imgSource,
                                     String imageIdentifier, boolean isImageResourceID)
    {
        this.imageViewPreference = preference;
        this.imageSource = imgSource;

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                .title(R.string.title_activity_image_view_preference_dialog)
                .disableDefaultFonts()
                .autoDismiss(false)
                .customView(R.layout.activity_imageview_resource_pref_dialog, false);

        if (imageSource.equals("resource_file"))
        {
            dialogBuilder.positiveText(R.string.imageview_resource_file_pref_dialog_gallery_btn);
            dialogBuilder.callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    // zavolat galeriu na vyzdvihnutie image
                    imageViewPreference.startGallery();
                    dialog.dismiss();
                }
            });
        }

        dialog = dialogBuilder.build();

        GridView gridView = (GridView)dialog.getCustomView().findViewById(R.id.imageview_resource_pref_dlg_gridview);
        gridView.setAdapter(new ImageViewPreferenceAdapter(context, imageIdentifier, isImageResourceID));

        gridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                imageViewPreference.setImageIdentifierAndType(ImageViewPreferenceAdapter.ThumbsIds[position], true);
                dialog.dismiss();
            }

        });

    }

    public void show() {
        dialog.show();
    }
}
