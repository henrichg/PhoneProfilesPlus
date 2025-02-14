package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class GrantDrawOverAppsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.countScreenOrientationLocks = 0;

        EditorActivity.itemDragPerformed = false;

        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

//        PPApplicationStatic.logE("[BACKGROUND_ACTIVITY] GrantDrawOverAppsActivity.onCreate", "xxx");
        //Log.e("GrantDrawOverAppsActivity.onCreate", "xxx");

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart()
    {
        super.onStart();

        final AppCompatActivity activity = this;

        GlobalGUIRoutines.lockScreenOrientation(this/*, false*/);

        // set theme and language for dialog alert ;-)
        GlobalGUIRoutines.setTheme(this, true, false, false, false, false, false);
        //GlobalGUIRoutines.setLanguage(this);

        GrantDrawOverAppsDialog dialog = new GrantDrawOverAppsDialog(activity);
        if (!isFinishing())
            dialog.showDialog();
    }

    @Override
    protected void onStop() {
        super.onStop();
        GlobalGUIRoutines.unlockScreenOrientation(this);
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /*
    private void doShow() {
        new ShowActivityAsyncTask(this).execute();
    }
    */

}
