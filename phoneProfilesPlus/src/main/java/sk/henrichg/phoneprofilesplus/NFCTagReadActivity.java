package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import java.util.Calendar;
import java.util.TimeZone;

public class NFCTagReadActivity extends Activity {

    //private String tagName;

    private NFCTagReadWriteManager nfcManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

                Calendar now = Calendar.getInstance();
                int gmtOffset = TimeZone.getDefault().getRawOffset();
                long time = now.getTimeInMillis() + gmtOffset;

                Intent eventsServiceIntent = new Intent(getApplicationContext(), EventsService.class);
                eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_NFC_TAG);
                eventsServiceIntent.putExtra(EventsService.EXTRA_EVENT_NFC_TAG_NAME, tagRead);
                eventsServiceIntent.putExtra(EventsService.EXTRA_EVENT_NFC_DATE, time);
                WakefulIntentService.sendWakefulWork(getApplicationContext(), eventsServiceIntent);

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
