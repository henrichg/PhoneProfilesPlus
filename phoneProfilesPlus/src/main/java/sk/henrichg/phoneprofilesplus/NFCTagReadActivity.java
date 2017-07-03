package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class NFCTagReadActivity extends Activity {

    //private String tagName;

    NFCTagReadWriteManager nfcManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        super.onCreate(savedInstanceState);

        //Intent intent = getIntent();
        //tagName = intent.getStringExtra(EXTRA_TAG_NAME);

        //Log.d("NFCTagReadActivity.onCreate", "tagName="+tagName);

        nfcManager = new NFCTagReadWriteManager(this);
        nfcManager.onActivityCreate();

        nfcManager.setOnTagReadListener(new NFCTagReadWriteManager.TagReadListener() {
            @Override
            public void onTagRead(String tagRead) {
                Toast.makeText(NFCTagReadActivity.this, "("+getString(R.string.app_name)+") "+getString(R.string.read_nfc_tag_readed)+": "+tagRead, Toast.LENGTH_LONG).show();

                /*Intent intent = new Intent(getApplicationContext(), NFCBroadcastReceiver.class);
                intent.putExtra(EventsService.EXTRA_EVENT_NFC_TAG_NAME, tagRead);
                sendBroadcast(intent);*/
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(PPApplication.nfcBroadcastReceiver, new IntentFilter("NFCBroadcastReceiver"));
                Intent intent = new Intent("NFCBroadcastReceiver");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

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
                //Toast.makeText(NFCTagReadActivity.this, exception.getType().toString(), Toast.LENGTH_LONG).show();
                Toast.makeText(NFCTagReadActivity.this, "("+getString(R.string.app_name)+") "+getString(R.string.read_nfc_tag_error), Toast.LENGTH_LONG).show();
                NFCTagReadActivity.this.finish();
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

    @Override
    protected void onStart()
    {
        super.onStart();
        //Log.d("NFCTagReadActivity.onStart", "xxx");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //Log.d("NFCTagReadActivity.onDestroy", "xxx");
    }

}
