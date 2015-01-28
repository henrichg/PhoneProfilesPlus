package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RefreshGUIBroadcastReceiver extends BroadcastReceiver {

	public static final String INTENT_REFRESH_GUI = "sk.henrichg.phoneprofilesplus.REFRESH_GUI";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		GlobalData.logE("### RefreshGUIBroadcastReceiver","xxx");
		
		ActivateProfileActivity activateProfileActivity = ActivateProfileActivity.getInstance();
		if (activateProfileActivity != null)
		{
			GlobalData.logE("RefreshGUIBroadcastReceiver","ActivateProfileActivity");
			activateProfileActivity.refreshGUI();
		}

		EditorProfilesActivity editorProfilesActivity = EditorProfilesActivity.getInstance();
		if (editorProfilesActivity != null)
		{
			GlobalData.logE("RefreshGUIBroadcastReceiver","EditorProfilesActivity");
			editorProfilesActivity.refreshGUI();
		}
		
	}

}
