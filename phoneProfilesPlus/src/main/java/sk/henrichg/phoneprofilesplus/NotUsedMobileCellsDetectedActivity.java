package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class NotUsedMobileCellsDetectedActivity extends AppCompatActivity {

    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        PPApplication.logE("NotUsedMobileCellsDetectedActivity.onCreate", "xxx");
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // set theme and language for dialog alert ;-)
        // not working on Android 2.3.x
        GlobalGUIRoutines.setTheme(this, true, false/*, false*/);
        GlobalGUIRoutines.setLanguage(this);

        PPApplication.logE("NotUsedMobileCellsDetectedActivity.onStart", "xxx");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.not_used_mobile_cells_detected_title);
        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                NotUsedMobileCellsDetectedActivity.this.finish();
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                NotUsedMobileCellsDetectedActivity.this.finish();
            }
        });

        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.activity_not_used_mobile_cells_detected, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                NotUsedMobileCellsDetectedActivity.this.onShow();
            }
        });

        if (!isFinishing())
            mDialog.show();
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void onShow() {
    }

}
