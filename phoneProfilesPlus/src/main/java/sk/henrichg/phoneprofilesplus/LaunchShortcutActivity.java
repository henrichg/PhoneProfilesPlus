package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

public class LaunchShortcutActivity extends Activity {

    public static final String EXTRA_PACKAGE_NAME = "packageName";
    public static final String EXTRA_ACTIVITY_NAME = "activityName";
    public static final String EXTRA_DIALOG_PREFERENCE_POSITION = "dialogPreferencePosition";

    String packageName;
    String activityName;
    int dialogPreferencePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        super.onCreate(savedInstanceState);

        packageName = getIntent().getStringExtra(EXTRA_PACKAGE_NAME);
        activityName = getIntent().getStringExtra(EXTRA_ACTIVITY_NAME);
        dialogPreferencePosition = getIntent().getIntExtra(EXTRA_DIALOG_PREFERENCE_POSITION, -1);

        //Log.d("LaunchShortcutActivity.onCreate","dialogPreferencePosition="+dialogPreferencePosition);

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        ComponentName componentName = new ComponentName(packageName, activityName);
        //if (componentName != null) {
            //intent = new Intent(Intent.ACTION_MAIN);
            Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            intent.setComponent(componentName);
            try {
                startActivityForResult(intent, 100);
            } catch (Exception e) {
                //e.printStackTrace();
                finish();
            }
        //}

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 100) {
            if ((resultCode == RESULT_OK) && (data != null)) {
                Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
                String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
                Bitmap icon = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

                /*
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    System.out.println(e);
                }
                */

                Intent returnIntent = new Intent();
                returnIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
                returnIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
                returnIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
                returnIntent.putExtra(EXTRA_DIALOG_PREFERENCE_POSITION, dialogPreferencePosition);
                setResult(RESULT_OK,returnIntent);
            }
        }

        finish();
    }

}
