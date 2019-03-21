package sk.henrichg.phoneprofilesplus;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class LaunchShortcutActivity extends AppCompatActivity {

    public static final String EXTRA_PACKAGE_NAME = "packageName";
    public static final String EXTRA_ACTIVITY_NAME = "activityName";
    public static final String EXTRA_DIALOG_PREFERENCE_POSITION = "dialogPreferencePosition";
    public static final String EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY = "dialogPreferenceStartApplicationDelay";

    private String packageName;
    private String activityName;
    private int dialogPreferencePosition;
    private int startApplicationDelay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        packageName = getIntent().getStringExtra(EXTRA_PACKAGE_NAME);
        activityName = getIntent().getStringExtra(EXTRA_ACTIVITY_NAME);
        dialogPreferencePosition = getIntent().getIntExtra(EXTRA_DIALOG_PREFERENCE_POSITION, -1);
        startApplicationDelay = getIntent().getIntExtra(EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, 0);

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
                finish();
            }
        //}

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 100) {
            if ((resultCode == RESULT_OK) && (data != null)) {
                data.putExtra(EXTRA_DIALOG_PREFERENCE_POSITION, dialogPreferencePosition);
                data.putExtra(EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, startApplicationDelay);
                setResult(RESULT_OK, data);
            }
        }

        finish();
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
