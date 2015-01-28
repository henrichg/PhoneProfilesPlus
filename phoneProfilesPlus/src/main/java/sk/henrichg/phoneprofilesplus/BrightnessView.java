package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.ViewGroup;

public class BrightnessView extends ViewGroup {

	Context context;
	
	public BrightnessView(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		//WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		//windowManager.removeViewImmediate(this);
		
	}

}
