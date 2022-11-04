package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
//import me.drakeet.support.toast.ToastCompat;

public class NFCTagWriteActivity extends AppCompatActivity {

    private String tagName;
    private long tagDbId;

    private NFCTagReadWriteManager nfcManager;

    private TextView writableTextView;

    public static final String EXTRA_TAG_NAME = "tag_name";
    public static final String EXTRA_TAG_DB_ID = "tag_db_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, false/*, false*/, false, false, false, false); // must by called before super.onCreate()
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_write_nfc_tag);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.nfc_tag_pref_dlg_writeTagTitle);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        Intent intent = getIntent();
        tagName = intent.getStringExtra(EXTRA_TAG_NAME);
        tagDbId = intent.getLongExtra(EXTRA_TAG_DB_ID, 0);

        if ((tagName == null) || tagName.isEmpty()) {
            nfcManager = null;

            writableTextView = findViewById(R.id.write_nfc_tag_writable);
            writableTextView.setText(R.string.nfc_tag_pref_dlg_writeToNfcTag_emptyTagName);
        }
        else {

            //Log.d("NFCTagWriteActivity.onCreate", "tagName="+tagName);

            nfcManager = new NFCTagReadWriteManager(this);
            nfcManager.onActivityCreate();

            writableTextView = findViewById(R.id.write_nfc_tag_writable);
            writableTextView.setText(R.string.empty_string);

            nfcManager.setOnTagReadListener(tagData -> {
//                    PPApplication.logE("[IN_LISTENER] NFCTagWriteActivity.onTagRead", "xxx");

                //ToastCompat.makeText(getApplicationContext(), "tag read:"+tagData, Toast.LENGTH_LONG).show();

                int[] attrs = {R.attr.activityWhiteTextColor};
                @SuppressLint("ResourceType")
                TypedArray ta = obtainStyledAttributes(attrs);
                int color = ta.getResourceId(0, android.R.color.black);
                writableTextView.setTextColor(ContextCompat.getColor(getBaseContext(), color));
                ta.recycle();

                if (nfcManager.tagRead) {
                    if (nfcManager.tagIsWritable)
                        writableTextView.setText(R.string.nfc_tag_pref_dlg_writeToNfcTag_writable);
                    else {
                        writableTextView.setTextColor(Color.RED);
                        writableTextView.setText(R.string.nfc_tag_pref_dlg_writeToNfcTag_not_writable);
                    }
                }
                //Log.d("NFCTagWriteActivity.onTagRead", "xxx");
            });

            nfcManager.setOnTagWriteListener(() -> {
//                    PPApplication.logE("[IN_LISTENER] NFCTagWriteActivity.onTagWritten", "xxx");

                PPApplication.showToast(getApplicationContext(), getString(R.string.write_nfc_tag_written), Toast.LENGTH_LONG);
                Intent returnIntent = new Intent();
                returnIntent.putExtra(EXTRA_TAG_NAME, tagName);
                returnIntent.putExtra(EXTRA_TAG_DB_ID, tagDbId);
                nfcManager.activity.setResult(Activity.RESULT_OK, returnIntent);
                try {
                    nfcManager.activity.finish();
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            });
            nfcManager.setOnTagWriteErrorListener(exception -> {
//                    PPApplication.logE("[IN_LISTENER] NFCTagWriteActivity.onTagWriteError", "xxx");

                String text = getString(R.string.write_nfc_tag_error);
                text = text + ": " + exception.getType().toString();
                if (nfcManager.tagRead) {
                    if (nfcManager.tagIsWritable)
                        text = text + " (" + getString(R.string.nfc_tag_pref_dlg_writeToNfcTag_writable) + ")";
                    else {
                        text = text + " (" + getString(R.string.nfc_tag_pref_dlg_writeToNfcTag_not_writable) + ")";
                    }
                }
                writableTextView.setTextColor(Color.RED);
                writableTextView.setText(text);
                //ToastCompat.makeText(getApplicationContext(), exception.getType().toString(), Toast.LENGTH_LONG).show();
                //ToastCompat.makeText(getApplicationContext().this, R.string.write_nfc_tag_error, Toast.LENGTH_LONG).show();
                //try {
                    //nfcManager.activity.finish();
                //} catch (Exception ignored) {};
            });
        }

        Button button = findViewById(R.id.write_nfc_tag_button);
        button.setOnClickListener(view -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcManager != null) {
            nfcManager.onActivityResume();
            if ((tagName != null) && (!tagName.isEmpty()))
                nfcManager.writeText(tagName);
        }
        //Log.d("NFCTagWriteActivity.onResume", "xxx");
    }

    @Override
    protected void onPause() {
        if (nfcManager != null)
            nfcManager.onActivityPause();
        super.onPause();
        //Log.d("NFCTagWriteActivity.onPause", "xxx");
    }

    @Override
    protected void onStart() {
        super.onStart();
        GlobalGUIRoutines.lockScreenOrientation(this, true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GlobalGUIRoutines.unlockScreenOrientation(this);
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);

//        PPApplication.logE("[IN_LISTENER] NFCTagWriteActivity.onNewIntent", "xxx");
        if (nfcManager != null)
            nfcManager.onActivityNewIntent(intent);
        //Log.d("NFCTagWriteActivity.onNewIntent", "xxx");
    }

}
