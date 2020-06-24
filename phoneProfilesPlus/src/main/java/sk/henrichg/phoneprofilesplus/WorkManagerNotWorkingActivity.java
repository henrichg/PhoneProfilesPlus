package sk.henrichg.phoneprofilesplus;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class WorkManagerNotWorkingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        //PPApplication.logE("ExitApplicationActivity.onCreate", "xxx");
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // set theme and language for dialog alert ;-)
        GlobalGUIRoutines.setTheme(this, true, false/*, false*/, false);
        //GlobalGUIRoutines.setLanguage(this);

        //PPApplication.logE("ExitApplicationActivity.onStart", "xxx");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.background_jobs_not_working_alert_title);
        dialogBuilder.setMessage(R.string.background_jobs_not_working_alert_message);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                WorkManagerNotWorkingActivity.this.finish();
            }
        });
        /*dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                WorkManagerNotWorkingActivity.this.finish();
            }
        });*/
        AlertDialog dialog = dialogBuilder.create();

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

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
