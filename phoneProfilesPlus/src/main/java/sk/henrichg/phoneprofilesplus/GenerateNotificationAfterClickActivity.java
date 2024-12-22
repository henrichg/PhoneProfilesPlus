package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class GenerateNotificationAfterClickActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EditorActivity.itemDragPerformed = false;

        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

//        PPApplicationStatic.logE("[BACKGROUND_ACTIVITY] GenerateNotificationAfterClickActivity.onCreate", "xxx");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // set theme and language for dialog alert ;-)
        GlobalGUIRoutines.setTheme(this, true, false, false, false, false, false);
        //GlobalGUIRoutines.setLanguage(this);

        GenerateNotificationAfterClickDialog dialog = new GenerateNotificationAfterClickDialog(this);
        dialog.show();

        if (!isFinishing())
            dialog.show();
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
