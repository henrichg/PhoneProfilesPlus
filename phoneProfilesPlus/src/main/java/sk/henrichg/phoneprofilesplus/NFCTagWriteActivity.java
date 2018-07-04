package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.readystatesoftware.systembartint.SystemBarTintManager;

public class NFCTagWriteActivity extends AppCompatActivity {

    private String tagName;

    private NFCTagReadWriteManager nfcManager;

    private TextView writableTextView;

    public static final String EXTRA_TAG_NAME = "tag_name";

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // must by called before super.onCreate() for PreferenceActivity
        GlobalGUIRoutines.setTheme(this, false, false, false); // must by called before super.onCreate()
        GlobalGUIRoutines.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_write_nfc_tag);

        if (/*(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) &&*/ (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            if (ApplicationPreferences.applicationTheme(getApplicationContext()).equals("color"))
                tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary));
            else
            if (ApplicationPreferences.applicationTheme(getApplicationContext()).equals("white"))
                tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary_white));
            else
                tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary_dark));
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.nfc_tag_pref_dlg_writeTagTitle);
            getSupportActionBar().setElevation(GlobalGUIRoutines.dpToPx(1));
        }

        Intent intent = getIntent();
        tagName = intent.getStringExtra(EXTRA_TAG_NAME);

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
                public void onTagRead(String tagRead) {
                    //Toast.makeText(getApplicationContext(), "tag read:"+tagRead, Toast.LENGTH_LONG).show();

                    int[] attrs = {R.attr.navigationDrawerText};
                    TypedArray ta = obtainStyledAttributes(attrs);
                    int color = ta.getResourceId(0, android.R.color.black);
                    writableTextView.setTextColor(getResources().getColor(color));
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
                    Toast.makeText(getApplicationContext(), R.string.write_nfc_tag_writed, Toast.LENGTH_LONG).show();
                    NFCTagWriteActivity.this.finish();
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
                    //Toast.makeText(getApplicationContext(), exception.getType().toString(), Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext().this, R.string.write_nfc_tag_error, Toast.LENGTH_LONG).show();

                    //NFCTagWriteActivity.this.finish();
                }
            });
        }

        Button button = findViewById(R.id.write_nfc_tag_button);
        button.setAllCaps(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        if (nfcManager != null)
            nfcManager.onActivityNewIntent(intent);
        //Log.d("NFCTagWriteActivity.onNewIntent", "xxx");
    }

}
