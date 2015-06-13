package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;

public class ProfileIconPreference extends DialogPreference {

	private String imageIdentifier;
	private boolean isImageResourceID;

	private String imageSource;

	private MaterialDialog mDialog;

	private ImageView imageView;
    ProfileIconPreferenceAdapter adapter;
	private Context prefContext;

	CharSequence preferenceTitle;

	public static int RESULT_LOAD_IMAGE = 1971;

	public ProfileIconPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);

        /*
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.ProfileIconPreference);

		// resource, resource_file, file
		imageSource = typedArray.getString(
			R.styleable.ProfileIconPreference_iconSource);
		*/


	    imageIdentifier = GlobalData.PROFILE_ICON_DEFAULT;
		isImageResourceID = true;

		prefContext = context;

		preferenceTitle = getTitle();

		setWidgetLayoutResource(R.layout.profileicon_preference); // resource na layout custom preference - TextView-ImageView

		//typedArray.recycle();

	}

	//@Override
	protected void onBindView(View view)
	{
		super.onBindView(view);

		imageView = (ImageView)view.findViewById(R.id.profileicon_pref_imageview); // resource na Textview v custom preference layoute

	    if (imageView != null)
	    {
	    	if (isImageResourceID)
	    	{
	    		// je to resource id
	    		int res = prefContext.getResources().getIdentifier(imageIdentifier, "drawable", prefContext.getPackageName());
	    		imageView.setImageResource(res); // resource na ikonu
	    	}
	    	else
	    	{
	    		// je to file
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
    protected void showDialog(Bundle state) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                        //.disableDefaultFonts()
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .neutralText(R.string.imageview_resource_file_pref_dialog_gallery_btn)
                .callback(callback)
                .autoDismiss(false)
                .content(getDialogMessage());

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_profileicon_pref_dialog, null);
        onBindDialogView(layout);

        GridView gridView = (GridView)layout.findViewById(R.id.profileicon_pref_dlg_gridview);
        adapter = new ProfileIconPreferenceAdapter(prefContext, imageIdentifier, isImageResourceID);
        gridView.setAdapter(adapter);
        gridView.setSelection(adapter.getImageResourcePosition(imageIdentifier));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                setImageIdentifierAndType(ImageViewPreferenceAdapter.ThumbsIds[position], true, false);
                adapter.imageIdentifierAndTypeChanged(imageIdentifier, isImageResourceID);
            }
        });

        mBuilder.customView(layout, false);

        mDialog = mBuilder.build();
        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    private final MaterialDialog.ButtonCallback callback = new MaterialDialog.ButtonCallback() {
        @Override
        public void onPositive(MaterialDialog dialog) {
            if (shouldPersist()) {
                setImageIdentifierAndType("", true, true);
            }
            mDialog.dismiss();
        }
        @Override
        public void onNegative(MaterialDialog dialog) {
            mDialog.dismiss();
        }
        @Override
        public void onNeutral(MaterialDialog dialog) {
            // zavolat galeriu na vyzdvihnutie image
            startGallery();
            dialog.dismiss();
        }
    };

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
				isImageResourceID = splits[1].equals("1");
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
				isImageResourceID = splits[1].equals("1");
			} catch (Exception e) {
				isImageResourceID = true;
			}
			persistString(value);
		}
    }

    /*
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
			isImageResourceID = splits[1].equals("1");
		} catch (Exception e) {
			isImageResourceID = true;
		}
		notifyChanged();
	}
    */

	public String getImageIdentifier()
	{
		return imageIdentifier;
	}

	public boolean getIsImageResourceID()
	{
		return isImageResourceID;
	}

	public void setImageIdentifierAndType(String newImageIdentifier, boolean newIsImageResourceID, boolean saveToPreference)
	{
		String newValue = newImageIdentifier+"|"+((newIsImageResourceID) ? "1" : "0");

        if (!saveToPreference) {
            String[] splits = newValue.split("\\|");
            try {
                imageIdentifier = splits[0];
            } catch (Exception e) {
                imageIdentifier = GlobalData.PROFILE_ICON_DEFAULT;
            }
            try {
                isImageResourceID = splits[1].equals("1");
            } catch (Exception e) {
                isImageResourceID = true;
            }
        }

        if (saveToPreference) {
            if (!newIsImageResourceID) {
                imageIdentifier = newImageIdentifier;
                isImageResourceID = false;
            }
            newValue = imageIdentifier+"|"+((isImageResourceID) ? "1" : "0");
            if (callChangeListener(newValue)) {
                persistString(newValue);
                // Data sa zmenili,notifikujeme
                notifyChanged();
            }
        }

	}

	public void startGallery()
	{
        //Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        // hm, neda sa ziskat aktivita z preference, tak vyuzivam static metodu
        ProfilePreferencesFragment.setChangedProfileIconPreference(this);
        ProfilePreferencesFragment.getPreferencesActivity().startActivityForResult(intent, RESULT_LOAD_IMAGE);
	}

    /*
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
		public static final Creator<SavedState> CREATOR =
				new Creator<SavedState>() {
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
	*/

//---------------------------------------------------------------------------------------------

    // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

}
