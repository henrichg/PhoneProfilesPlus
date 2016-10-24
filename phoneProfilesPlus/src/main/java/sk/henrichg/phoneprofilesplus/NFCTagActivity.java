package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class NFCTagActivity extends Activity {

    private String tagName;

    NFCTagReadWriteManager nfcManager;

    public static final String EXTRA_TAG_NAME = "tag_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        tagName = intent.getStringExtra(EXTRA_TAG_NAME);

        nfcManager = new NFCTagReadWriteManager(this);
        nfcManager.onActivityCreate();

        nfcManager.setOnTagReadListener(new NFCTagReadWriteManager.TagReadListener() {
            @Override
            public void onTagRead(String tagRead) {
                Toast.makeText(NFCTagActivity.this, "tag read:"+tagRead, Toast.LENGTH_LONG).show();
                NFCTagActivity.this.finish();
            }
        });

        nfcManager.setOnTagWriteListener(new NFCTagReadWriteManager.TagWriteListener() {
            @Override
            public void onTagWritten() {
                Toast.makeText(NFCTagActivity.this, "tag writen", Toast.LENGTH_LONG).show();
                NFCTagActivity.this.finish();
            }
        });
        nfcManager.setOnTagWriteErrorListener(new NFCTagReadWriteManager.TagWriteErrorListener() {
            @Override
            public void onTagWriteError(NFCTagWriteException exception) {
                Toast.makeText(NFCTagActivity.this, exception.getType().toString(), Toast.LENGTH_LONG).show();
                NFCTagActivity.this.finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcManager.onActivityResume();
    }

    @Override
    protected void onPause() {
        nfcManager.onActivityPause();
        super.onPause();
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        nfcManager.onActivityNewIntent(intent);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (!tagName.isEmpty())
            nfcManager.writeText(tagName);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

}
