package sk.henrichg.phoneprofilesplus;

import android.app.Dialog;
import android.content.Context;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
//import android.preference.Preference;
//import android.preference.Preference.OnPreferenceChangeListener;
import android.view.View;


public class ImageViewPreferenceDialog extends Dialog {

	private ImageViewPreference imageViewPreference;
	private String imageSource;
	
	public ImageViewPreferenceDialog(Context context) {
		super(context);
	}
	
	public ImageViewPreferenceDialog(Context context, ImageViewPreference preference, String imgSource, 
										String imageIdentifier, boolean isImageResourceID)
	{
		super(context);
		
		imageViewPreference = preference;
		imageSource = imgSource;

		final Context _context = context;
		
		GridView gridView;
		
		//Log.d("ImageViewPreferenceDialog", "imageSource="+imageSource);
		
		if (imageSource.equals("resource_file"))
		{
			setContentView(R.layout.activity_imageview_resource_file_pref_dialog);
			gridView = (GridView)findViewById(R.id.imageview_resource_file_pref_dlg_gridview);
		}
		else
		{
			setContentView(R.layout.activity_imageview_resource_pref_dialog);
			gridView = (GridView)findViewById(R.id.imageview_resource_pref_dlg_gridview);
		}
		
		gridView.setAdapter(new ImageViewPreferenceAdapter(_context, imageIdentifier, isImageResourceID));
		
		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				imageViewPreference.setImageIdentifierAndType(ImageViewPreferenceAdapter.ThumbsIds[position], true);
				ImageViewPreferenceDialog.this.dismiss();
			}

		});

		if (imageSource.equals("resource_file"))
		{
			Button button = (Button)findViewById(R.id.imageview_resource_file_pref_dlg_button);
			button.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					// zavolat galeriu na vyzdvihnutie image
					imageViewPreference.startGallery();
					ImageViewPreferenceDialog.this.dismiss();
				}

			});
		}
		
/*		imageViewPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
			}	
		}); */
		
			
	}


}
