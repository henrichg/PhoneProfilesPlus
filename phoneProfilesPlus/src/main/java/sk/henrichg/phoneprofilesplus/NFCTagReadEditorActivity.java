package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
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

public class NFCTagReadEditorActivity extends AppCompatActivity {

    long tagDbId;
    String tagName;

    private NFCTagReadWriteManager nfcManager;

    private TextView descriptionTextView;

    public static final String EXTRA_TAG_DB_ID = "tag_db_id";
    public static final String EXTRA_TAG_UID = "tag_uid";
    public static final String EXTRA_TAG_NAME = "tag_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, false, false); // must by called before super.onCreate()
        GlobalGUIRoutines.setLanguage(getBaseContext());
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_read_nfc_tag_editor);

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
                tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primaryDark19_white));
            else
                tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary_dark));
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.nfc_tag_pref_dlg_read_tag_editor_title);
            getSupportActionBar().setElevation(GlobalGUIRoutines.dpToPx(1));
        }

        Intent intent = getIntent();
        tagDbId = intent.getLongExtra(EXTRA_TAG_DB_ID, 0);
        tagName = intent.getStringExtra(EXTRA_TAG_NAME);

        descriptionTextView = findViewById(R.id.read_nfc_tag_editor_description);

        if ((tagName == null) || tagName.isEmpty()) {
            nfcManager = null;

            descriptionTextView.setText(R.string.nfc_tag_pref_dlg_writeToNfcTag_emptyTagName);
        }
        else {

            nfcManager = new NFCTagReadWriteManager(this);
            nfcManager.onActivityCreate();

            nfcManager.setOnTagReadListener(new NFCTagReadWriteManager.TagReadListener() {
                @Override
                public void onUidRead(String uid) {
                    Toast.makeText(getApplicationContext(), "("+getString(R.string.app_name)+") "+getString(R.string.read_nfc_tag_read)+": "+uid, Toast.LENGTH_LONG).show();

                    if (nfcManager.uidRead) {
                        descriptionTextView.setText(uid);
                    }

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(EXTRA_TAG_NAME, tagName);
                    returnIntent.putExtra(EXTRA_TAG_UID, uid);
                    returnIntent.putExtra(EXTRA_TAG_DB_ID, tagDbId);
                    NFCTagReadEditorActivity.this.setResult(Activity.RESULT_OK, returnIntent);
                    NFCTagReadEditorActivity.this.finish();
                }

                @Override
                public void onTagRead(String tagData) {
                    /*Toast.makeText(getApplicationContext(), "("+getString(R.string.app_name)+") "+getString(R.string.read_nfc_tag_read)+": "+tagRead, Toast.LENGTH_LONG).show();

                    NFCTagReadEditorActivity.this.finish();*/
                }
            });

            /*nfcManager.setOnTagWriteListener(new NFCTagReadWriteManager.TagWriteListener() {
                @Override
                public void onTagWritten() {
                    Toast.makeText(getApplicationContext(), "tag write finished", Toast.LENGTH_LONG).show();
                }
            });

            nfcManager.setOnTagWriteErrorListener(new NFCTagReadWriteManager.TagWriteErrorListener() {
                @Override
                public void onTagWriteError(NFCTagWriteException exception) {
                    //Toast.makeText(getApplicationContext(), exception.getType().toString(), Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), "("+getString(R.string.app_name)+") "+getString(R.string.read_nfc_tag_error), Toast.LENGTH_LONG).show();
                    NFCTagReadEditorActivity.this.finish();
                }
            });*/

            descriptionTextView.setText(R.string.empty_string);

        }

        Button button = findViewById(R.id.read_nfc_tag_editor_button);
        //button.setAllCaps(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NFCTagReadEditorActivity.this.setResult(Activity.RESULT_CANCELED);
                NFCTagReadEditorActivity.this.finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcManager.onActivityResume();
        //Log.d("NFCTagReadActivity.onResume", "xxx");
    }

    @Override
    protected void onPause() {
        nfcManager.onActivityPause();
        super.onPause();
        //Log.d("NFCTagReadActivity.onPause", "xxx");
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        nfcManager.onActivityNewIntent(intent);
        //Log.d("NFCTagReadActivity.onNewIntent", "xxx");
    }

}
