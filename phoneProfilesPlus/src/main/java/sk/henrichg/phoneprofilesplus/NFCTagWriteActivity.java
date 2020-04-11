package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//import com.crashlytics.android.Crashlytics;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

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

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, false/*, false*/, false); // must by called before super.onCreate()
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_write_nfc_tag);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name)));

        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            switch (ApplicationPreferences.applicationTheme(getApplicationContext(), true)) {
                case "color":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary));
                    break;
                case "white":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primaryDark19_white));
                    break;
                default:
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary_dark));
                    break;
            }
        }
        */

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

            nfcManager.setOnTagReadListener(new NFCTagReadWriteManager.TagReadListener() {

                @Override
                public void onTagRead(String tagData) {
                    //ToastCompat.makeText(getApplicationContext(), "tag read:"+tagData, Toast.LENGTH_LONG).show();

                    int[] attrs = {R.attr.activityWhiteTextColor};
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
                }
            });

            nfcManager.setOnTagWriteListener(new NFCTagReadWriteManager.TagWriteListener() {
                @Override
                public void onTagWritten() {
                    PPApplication.showToast(getApplicationContext(), getString(R.string.write_nfc_tag_written), Toast.LENGTH_LONG);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(EXTRA_TAG_NAME, tagName);
                    returnIntent.putExtra(EXTRA_TAG_DB_ID, tagDbId);
                    NFCTagWriteActivity.this.setResult(Activity.RESULT_OK, returnIntent);
                    try {
                        NFCTagWriteActivity.this.finish();
                    } catch (Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        //Crashlytics.logException(e);
                    }
                }
            });
            nfcManager.setOnTagWriteErrorListener(new NFCTagReadWriteManager.TagWriteErrorListener() {
                @Override
                public void onTagWriteError(NFCTagWriteException exception) {
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
                        //NFCTagWriteActivity.this.finish();
                    //} catch (Exception ignored) {};
                }
            });
        }

        Button button = findViewById(R.id.write_nfc_tag_button);
        //button.setAllCaps(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NFCTagWriteActivity.this.setResult(Activity.RESULT_CANCELED);
                NFCTagWriteActivity.this.finish();
            }
        });

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
        GlobalGUIRoutines.lockScreenOrientation(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GlobalGUIRoutines.unlockScreenOrientation(this);
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        if (nfcManager != null)
            nfcManager.onActivityNewIntent(intent);
        //Log.d("NFCTagWriteActivity.onNewIntent", "xxx");
    }

}
