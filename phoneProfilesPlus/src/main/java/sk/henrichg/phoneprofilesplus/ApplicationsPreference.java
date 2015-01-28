package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class ApplicationsPreference extends Preference {
	
	private String packageName;

	private ImageView packageIcon;

	private Context prefContext;
	
	CharSequence preferenceTitle;
	
	public ApplicationsPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		/*
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.ApplicationsPreference);

		// resource, resource_file, file
		imageSource = typedArray.getString(
			R.styleable.ImageViewPreference_imageSource);
		*/
		

		packageName = "-";
		
		prefContext = context;
		
		preferenceTitle = getTitle();
		
		//Log.d("ApplicationsPreference", "title="+preferenceTitle);
		//Log.d("ApplicationsPreference", "imageSource="+imageSource);
		
		setWidgetLayoutResource(R.layout.applications_preference); // resource na layout custom preference - TextView-ImageView
		
		//pedArray.recycle();
		
	}
	
	//@Override
	protected void onBindView(View view)
	{
		super.onBindView(view);

		//Log.d("ApplicationsPreference.onBindView", "packageName="+packageName);
		
		//preferenceTitleView = (TextView)view.findViewById(R.id.applications_pref_label);  // resource na title
		//preferenceTitleView.setText(preferenceTitle);
		
		packageIcon = (ImageView)view.findViewById(R.id.applications_pref_icon); // resource na ImageView v custom preference layoute

	    if (packageIcon != null)
	    {
			PackageManager packageManager = prefContext.getPackageManager();
			ApplicationInfo app;
			try {
				app = packageManager.getApplicationInfo(packageName, 0);
				if (app != null)
				{
					setSummary(packageManager.getApplicationLabel(app));
					Drawable icon = packageManager.getApplicationIcon(app);
					//CharSequence name = packageManager.getApplicationLabel(app);
					packageIcon.setImageDrawable(icon);
				}
				else
				{
					setSummary("");
					packageIcon.setImageDrawable(null);
				}
			} catch (NameNotFoundException e) {
				//e.printStackTrace();
				setSummary("");
				packageIcon.setImageDrawable(null);
			}
	    }
	}
	
	@Override
	protected void onClick()
	{
		// klik na preference

		//Log.d("ApplicationsPreference.onClick", "packageName="+packageName);

		final ApplicationsPreferenceDialog dialog = new ApplicationsPreferenceDialog(prefContext, this, packageName);
		dialog.setTitle(R.string.title_activity_applications_preference_dialog);
		dialog.show();
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		super.onGetDefaultValue(a, index);
		
		return a.getString(index);  // packageName vratene ako retazec
	}
	
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
	{
		if (restoreValue) {
			// restore state
			String value = getPersistedString(packageName);
			packageName = value;
		}
		else {
			// set state
			String value = (String) defaultValue;
			packageName = value;
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
		myState.packageName = packageName;
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
		String value = (String) myState.packageName;
		packageName = value;
		notifyChanged();
	}
	
	public String getPackageName()
	{
		return packageName;
	}
	
	public void setPackageName(String newPackageName)
	{
		String newValue = newPackageName;

		if (!callChangeListener(newValue)) {
			// nema sa nova hodnota zapisat
			return;
		}

		packageName = newValue;

		// set summary
		PackageManager packageManager = prefContext.getPackageManager();
		ApplicationInfo app;
		try {
			app = packageManager.getApplicationInfo(packageName, 0);
			if (app != null)
				setSummary(packageManager.getApplicationLabel(app));
			else
				setSummary("");
		} catch (NameNotFoundException e) {
			//e.printStackTrace();
			setSummary("");
		}

		// zapis do preferences
		persistString(newValue);
		
		// Data sa zmenili,notifikujeme
		notifyChanged();
		
	}
	
	// SavedState class
	private static class SavedState extends BaseSavedState
	{
		String packageName;
		
		public SavedState(Parcel source)
		{
			super(source);
			
			// restore packageName
			packageName = source.readString();
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags)
		{
			super.writeToParcel(dest, flags);
			
			// save packageName
			dest.writeString(packageName);
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
