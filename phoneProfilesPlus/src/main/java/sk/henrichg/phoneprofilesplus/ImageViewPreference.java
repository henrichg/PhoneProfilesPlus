package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class ImageViewPreference extends Preference {
	
	private String imageIdentifier;
	private boolean isImageResourceID;
	
	private String imageSource;

	private ImageView imageView;
	private Context prefContext;
	
	CharSequence preferenceTitle;
	
	public static int RESULT_LOAD_IMAGE = 1970;
	
	public ImageViewPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.ImageViewPreference);

		// resource, resource_file, file
		imageSource = typedArray.getString(
			R.styleable.ImageViewPreference_imageSource);
		

		if (imageSource.equals("file"))
		{
			imageIdentifier = "-";
			isImageResourceID = false;
		}
		else
		{
			imageIdentifier = GlobalData.PROFILE_ICON_DEFAULT;
			isImageResourceID = true;
		}
		
		prefContext = context;
		
		preferenceTitle = getTitle();
		
		//Log.d("ImageViewPreference", "title="+preferenceTitle);
		//Log.d("ImageViewPreference", "imageSource="+imageSource);
		
		setWidgetLayoutResource(R.layout.imageview_preference); // resource na layout custom preference - TextView-ImageView
		//setLayoutResource(R.layout.imageview_preference); // resource na layout custom preference - TextView-ImageView
		
		typedArray.recycle();
		
	}
	
	//@Override
	protected void onBindView(View view)
	{
		super.onBindView(view);

		//Log.d("ImageViewPreference.onBindView", "imageIdentifier="+imageIdentifier);
		//Log.d("ImageViewPreference.onBindView", "isImageResourceID="+isImageResourceID);
		
		//imageTitle = (TextView)view.findViewById(R.id.imageview_pref_label);  // resource na image title
		//imageTitle.setText(preferenceTitle);
		
		imageView = (ImageView)view.findViewById(R.id.imageview_pref_imageview); // resource na Textview v custom preference layoute

	    if (imageView != null)
	    {
	    	if (isImageResourceID)
	    	{
	    		// je to resource id
	    		int res = prefContext.getResources().getIdentifier(imageIdentifier, "drawable", prefContext.getPackageName());
	    		//Log.d("ImageViewPreference.onBindView", "resource="+res);
	    		imageView.setImageResource(res); // resource na ikonu
	    	}
	    	else
	    	{
	    		// je to file
	    		//Log.d("ImageViewPreference.onBindView", "file="+imageIdentifier);

        		Resources resources = prefContext.getResources();
        		int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
        		int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
        		Bitmap bitmap = BitmapManipulator.resampleBitmap(imageIdentifier, width, height);
        		
        		//if (bitmap != null)
        			imageView.setImageBitmap(bitmap);
	    	}
	    }
	}
	
	@Override
	protected void onClick()
	{
		// klik na preference

		//Log.d("ImageViewPreference.onClick", "imageResourceID="+imageIdentifier);
		//Log.d("ImageViewPreference.onClick", "imageSource="+imageSource);
		
		if (imageSource.equals("resource_file") || imageSource.equals("resource"))
		{
			final ImageViewPreferenceDialog dialog = new ImageViewPreferenceDialog(prefContext, this, imageSource,
																					imageIdentifier, isImageResourceID);
			dialog.setTitle(R.string.title_activity_image_view_preference_dialog);
			dialog.show();
		}
		else
		{
			// zavolat galeriu na vyzdvihnutie image
			startGallery();
		}
		
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		super.onGetDefaultValue(a, index);
		
		return a.getString(index);  // ikona bude vratena ako retazec
	}
	
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
	{
		if (restoreValue) {
			// restore state
			String value = getPersistedString(imageIdentifier+"|"+((isImageResourceID) ? "1" : "0"));
			String[] splits = value.split("\\|");
			try {
				imageIdentifier = splits[0];
			} catch (Exception e) {
				imageIdentifier = GlobalData.PROFILE_ICON_DEFAULT;
			}
			try {
				isImageResourceID = (splits[1].equals("1")) ? true : false;
			} catch (Exception e) {
				isImageResourceID = true;
			}
		}
		else {
			// set state
			String value = (String) defaultValue;
			String[] splits = value.split("\\|");
			try {
				imageIdentifier = splits[0];
			} catch (Exception e) {
				imageIdentifier = GlobalData.PROFILE_ICON_DEFAULT;
			}
			try {
				isImageResourceID = (splits[1].equals("1")) ? true : false;
			} catch (Exception e) {
				isImageResourceID = true;
			}
			persistString(value);
		}
	}
	
	@Override
	protected Parcelable onSaveInstanceState()
	{
		// ulozime instance state - napriklad kvoli zmene orientacie
		
		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			// netreba ukladat, je ulozene persistentne
			return superState;
		}
		
		// ulozenie istance state
		final SavedState myState = new SavedState(superState);
		myState.imageIdentifierAndType = imageIdentifier+"|"+((isImageResourceID) ? "1" : "0");
		return myState;
		
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state)
	{
		if (!state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}
		
		// restore instance state
		SavedState myState = (SavedState)state;
		super.onRestoreInstanceState(myState.getSuperState());
		String value = (String) myState.imageIdentifierAndType;
		String[] splits = value.split("\\|");
		try {
			imageIdentifier = splits[0];
		} catch (Exception e) {
			imageIdentifier = GlobalData.PROFILE_ICON_DEFAULT;
		}
		try {
			isImageResourceID = (splits[1].equals("1")) ? true : false;
		} catch (Exception e) {
			isImageResourceID = true;
		}
		notifyChanged();
	}
	
	public String getImageIdentifier()
	{
		return imageIdentifier;
	}
	
	public boolean getIsImageResourceID()
	{
		return isImageResourceID;
	}
	
	public void setImageIdentifierAndType(String newImageIdentifier, boolean newIsImageResourceID)
	{
		String newValue = newImageIdentifier+"|"+((newIsImageResourceID) ? "1" : "0");

		if (!callChangeListener(newValue)) {
			// nema sa nova hodnota zapisat
			return;
		}

		String[] splits = newValue.split("\\|");
		try {
			imageIdentifier = splits[0];
		} catch (Exception e) {
			imageIdentifier = GlobalData.PROFILE_ICON_DEFAULT;
		}
		try {
			isImageResourceID = (splits[1].equals("1")) ? true : false;
		} catch (Exception e) {
			isImageResourceID = true;
		}

		// zapis do preferences
		persistString(newValue);
		
		// Data sa zmenili,notifikujeme
		notifyChanged();
		
	}
	
	public void startGallery()
	{
		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		
		// hm, neda sa ziskat aktivita z preference, tak vyuzivam static metodu
		ProfilePreferencesFragment.setChangedImageViewPreference(this);
		ProfilePreferencesFragment.getPreferencesActivity().startActivityForResult(intent, RESULT_LOAD_IMAGE);
		
	}

	
	// SavedState class
	private static class SavedState extends BaseSavedState
	{
		String imageIdentifierAndType;
		
		public SavedState(Parcel source)
		{
			super(source);
			
			// restore image identifier and type
			imageIdentifierAndType = source.readString();
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags)
		{
			super.writeToParcel(dest, flags);
			
			// save image identifier and type
			dest.writeString(imageIdentifierAndType);
		}
		
		public SavedState(Parcelable superState)
		{
			super(superState);
		}
		
		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR =
				new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) 
			{
				return new SavedState(in);
			}
			public SavedState[] newArray(int size)
			{
				return new SavedState[size];
			}
				
		};
	
	}
}
