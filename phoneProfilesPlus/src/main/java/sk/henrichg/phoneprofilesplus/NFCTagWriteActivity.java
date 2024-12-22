package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class NFCTagWriteActivity extends AppCompatActivity {

    private String tagName;
    private String tagData;
    private long tagDbId;

    private NFCTagReadWriteManager nfcManager;

    private TextView touchTextView;
    private TextView writableTextView;
    private TextView tagDataTextView;
    private Button addReadedNameButton;
    private Button writeNameToTagAndAddNameButton;

    static final String EXTRA_TAG_NAME = "tag_name";
    static final String EXTRA_TAG_DB_ID = "tag_db_id";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EditorActivity.itemDragPerformed = false;

        GlobalGUIRoutines.setTheme(this, false, true, false, false, false, false); // must by called before super.onCreate()
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nfc_write_tag);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        Toolbar toolbar = findViewById(R.id.write_nfc_tag_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.nfc_tag_pref_dlg_writeTagTitle);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        Intent intent = getIntent();
        tagName = intent.getStringExtra(EXTRA_TAG_NAME);
        tagDbId = intent.getLongExtra(EXTRA_TAG_DB_ID, 0);

        touchTextView = findViewById(R.id.write_nfc_tag_touch);
        //noinspection DataFlowIssue
        touchTextView.setText(R.string.nfc_tag_pref_dlg_readNfcTag_touch);

        tagDataTextView = findViewById(R.id.write_nfc_tag_data);
        writableTextView = findViewById(R.id.write_nfc_tag_writable);
        addReadedNameButton = findViewById(R.id.write_nfc_tag_addReadedTagName);
        //noinspection DataFlowIssue
        addReadedNameButton.setEnabled(false);
        writeNameToTagAndAddNameButton = findViewById(R.id.write_nfc_tag_writeNameToTagAndAddName);
        //noinspection DataFlowIssue
        writeNameToTagAndAddNameButton.setEnabled(false);

        TextView tagNameTextView = findViewById(R.id.write_nfc_tag_name);
        //noinspection DataFlowIssue
        tagNameTextView.setText(getString(R.string.nfc_tag_pref_dlg_writeToNfcTag_tagName) + " " + tagName);
        tagDataTextView.setText(getString(R.string.nfc_tag_pref_dlg_writeToNfcTag_tagData) + " " +
                                getString(R.string.nfc_tag_pref_dlg_writeToNfcTag_tagData_noData));

        if (tagDbId != 0) {
            TextView addUpdateTagTextView = findViewById(R.id.write_nfc_tag_addUpdateName);
            //noinspection DataFlowIssue
            addUpdateTagTextView.setText(R.string.nfc_tag_pref_dlg_writeToNfcTag_changeNameButtons);
        }

        if ((tagName == null) || tagName.isEmpty()) {
            nfcManager = null;

            writableTextView.setText(R.string.nfc_tag_pref_dlg_writeToNfcTag_emptyTagName);
        }
        else {
            //Log.d("NFCTagWriteActivity.onCreate", "tagName="+tagName);

            nfcManager = new NFCTagReadWriteManager(this);
            nfcManager.onActivityCreate();

            writableTextView.setText("");

            nfcManager.setOnTagReadListener(_tagData -> {
//                    PPApplicationStatic.logE("[IN_LISTENER] NFCTagWriteActivity.onTagRead", "xxx");
//                Log.e("NFCTagWriteActivity.OnTagRead", "xxxxx");

                tagData = _tagData;
                if ((tagData == null) || tagData.isEmpty()) {
                    tagDataTextView.setText(getString(R.string.nfc_tag_pref_dlg_writeToNfcTag_tagData) + " " +
                            getString(R.string.nfc_tag_pref_dlg_writeToNfcTag_tagData_noData));
                    addReadedNameButton.setEnabled(false);
                }
                else {
                    tagDataTextView.setText(getString(R.string.nfc_tag_pref_dlg_writeToNfcTag_tagData) + " " +
                            tagData);
                    addReadedNameButton.setEnabled(true);
                }

                //ToastCompat.makeText(getApplicationContext(), "tag read:"+tagData, Toast.LENGTH_LONG).show();

                //int[] attrs = {R.attr.activityNormalTextColor};
                //TypedArray ta = obtainStyledAttributes(attrs);
                //int color = ta.getResourceId(0, android.R.color.black);
                //writableTextView.setTextColor(ContextCompat.getColor(getBaseContext(), color));
                //ta.recycle();
                writableTextView.setTextColor(ContextCompat.getColor(this, R.color.activityNormalTextColor));

                if (nfcManager.tagRead) {
                    if (nfcManager.tagIsWritable) {
                        writableTextView.setText(R.string.nfc_tag_pref_dlg_writeToNfcTag_writable);
                        touchTextView.setText(getString(R.string.nfc_tag_pref_dlg_writeToNfcTag_touch1) + " \"" +
                                getString(R.string.nfc_tag_pref_dlg_writeToNfcTag_writeNameToTagAndAddNameButton) + "\". " +
                                getString(R.string.nfc_tag_pref_dlg_writeToNfcTag_touch2));
                        writeNameToTagAndAddNameButton.setEnabled(nfcManager.intentForWrite != null);
                    }
                    else {
                        writableTextView.setTextColor(ContextCompat.getColor(this, R.color.errorColor));
                        writableTextView.setText(R.string.nfc_tag_pref_dlg_writeToNfcTag_not_writable);
                        touchTextView.setText(R.string.nfc_tag_pref_dlg_readNfcTag_touch);
                        writeNameToTagAndAddNameButton.setEnabled(false);
                    }
                }
                //Log.d("NFCTagWriteActivity.onTagRead", "xxx");
            });

            nfcManager.setOnTagWriteListener(() -> {
//                    PPApplicationStatic.logE("[IN_LISTENER] NFCTagWriteActivity.onTagWritten", "xxx");

                PPApplication.showToast(getApplicationContext(), getString(R.string.write_nfc_tag_written), Toast.LENGTH_LONG);
                Intent returnIntent = new Intent();
                returnIntent.putExtra(EXTRA_TAG_NAME, tagName);
                returnIntent.putExtra(EXTRA_TAG_DB_ID, tagDbId);
                nfcManager.activity.setResult(Activity.RESULT_OK, returnIntent);
                try {
                    nfcManager.activity.finish();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            });
            nfcManager.setOnTagWriteErrorListener(exception -> {
//                    PPApplicationStatic.logE("[IN_LISTENER] NFCTagWriteActivity.onTagWriteError", "xxx");
                //noinspection Convert2MethodRef
                displayError(exception);
            });
        }

        addReadedNameButton.setOnClickListener(v -> {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(EXTRA_TAG_NAME, tagData);
            returnIntent.putExtra(EXTRA_TAG_DB_ID, tagDbId);
            nfcManager.activity.setResult(Activity.RESULT_OK, returnIntent);
            try {
                nfcManager.activity.finish();
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        });
        writeNameToTagAndAddNameButton.setOnClickListener(v -> {
            if ((nfcManager != null) && (nfcManager.intentForWrite != null)) {
                nfcManager.writeTag();
            } else
                displayError(null);
        });


        Button button = findViewById(R.id.write_nfc_tag_button);
        //noinspection DataFlowIssue
        button.setOnClickListener(view -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
    }

    private void displayError(NFCTagWriteException exception) {
        String text = getString(R.string.write_nfc_tag_error);
        if (exception != null)
            text = text + StringConstants.STR_COLON_WITH_SPACE + exception.getType().toString();
        if (nfcManager.tagRead) {
            if (nfcManager.tagIsWritable)
                text = text + " (" + getString(R.string.nfc_tag_pref_dlg_writeToNfcTag_writable) + ")";
            else {
                text = text + " (" + getString(R.string.nfc_tag_pref_dlg_writeToNfcTag_not_writable) + ")";
            }
        }
        writableTextView.setTextColor(ContextCompat.getColor(this, R.color.errorColor));
        writableTextView.setText(text);
        //ToastCompat.makeText(getApplicationContext(), exception.getType().toString(), Toast.LENGTH_LONG).show();
        //ToastCompat.makeText(getApplicationContext().this, R.string.write_nfc_tag_error, Toast.LENGTH_LONG).show();
        //try {
        //nfcManager.activity.finish();
        //} catch (Exception ignored) {};
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
//        Log.e("NFCTagWriteActivity.onResume", "xxx");
    }

    @Override
    protected void onPause() {
        if (nfcManager != null)
            nfcManager.onActivityPause();
        super.onPause();
//        Log.e("NFCTagWriteActivity.onPause", "xxx");
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

    /** @noinspection NullableProblems*/
    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);

        if (nfcManager != null) {
            nfcManager.onActivityNewIntent(intent);
            if (nfcManager.intentForWrite != null)
                writeNameToTagAndAddNameButton.setEnabled(true);
        }

//        Log.e("NFCTagWriteActivity.onNewIntent", "xxx");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

}
