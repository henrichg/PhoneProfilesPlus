package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.List;

public class GrantPermissionActivity extends Activity {

    private long profile_id;
    private List<Permissions.PermissionType> permissions;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalData.loadPreferences(getApplicationContext());

        intent = getIntent();
        profile_id = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
        permissions = intent.getParcelableArrayListExtra(Permissions.EXTRA_PERMISSION_TYPES);

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //if (ActivityCompat.shouldShowRequestPermissionRationale(this, ))

        finish();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }


}
