package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class NFCTagReadActivity extends Activity {

    //private String tagName;

    NFCTagReadWriteManager nfcManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Intent intent = getIntent();
        //tagName = intent.getStringExtra(EXTRA_TAG_NAME);

        //Log.d("NFCTagWriteActivity.onCreate", "tagName="+tagName);

        nfcManager = new NFCTagReadWriteManager(this);
        nfcManager.onActivityCreate();

        nfcManager.setOnTagReadListener(new NFCTagReadWriteManager.TagReadListener() {
            @Override
            public void onTagRead(String tagRead) {
                Toast.makeText(NFCTagReadActivity.this, "tag read:"+tagRead, Toast.LENGTH_LONG).show();
                NFCTagReadActivity.this.finish();
            }
        });

        /*nfcManager.setOnTagWriteListener(new NFCTagReadWriteManager.TagWriteListener() {
            @Override
            public void onTagWritten() {
                Toast.makeText(NFCTagReadActivity.this, "tag writen", Toast.LENGTH_LONG).show();
                NFCTagReadActivity.this.finish();
            }
        });*/

        nfcManager.setOnTagWriteErrorListener(new NFCTagReadWriteManager.TagWriteErrorListener() {
            @Override
            public void onTagWriteError(NFCTagWriteException exception) {
                Toast.makeText(NFCTagReadActivity.this, exception.getType().toString(), Toast.LENGTH_LONG).show();
                NFCTagReadActivity.this.finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcManager.onActivityResume();
        Log.d("NFCTagWriteActivity.onResume", "xxx");
    }

    @Override
    protected void onPause() {
        nfcManager.onActivityPause();
        super.onPause();
        Log.d("NFCTagWriteActivity.onPause", "xxx");
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        nfcManager.onActivityNewIntent(intent);
        Log.d("NFCTagWriteActivity.onNewIntent", "xxx");
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Log.d("NFCTagWriteActivity.onStart", "xxx");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d("NFCTagWriteActivity.onDestroy", "xxx");
    }

}
