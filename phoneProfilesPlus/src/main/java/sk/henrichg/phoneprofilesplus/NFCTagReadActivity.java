package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.Calendar;

public class NFCTagReadActivity extends AppCompatActivity {

    private NFCTagReadWriteManager nfcManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        nfcManager = new NFCTagReadWriteManager(this);
        nfcManager.onActivityCreate();

        nfcManager.setOnTagReadListener(new NFCTagReadWriteManager.TagReadListener() {
            @Override
            public void onTagRead(String tagRead) {
                Toast.makeText(NFCTagReadActivity.this, "("+getString(R.string.app_name)+") "+getString(R.string.read_nfc_tag_readed)+": "+tagRead, Toast.LENGTH_LONG).show();

                final String _tagRead = tagRead;

                Calendar now = Calendar.getInstance();
                int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                final long _time = now.getTimeInMillis() + gmtOffset;

                //EventsHandlerJob.startForNFCTagSensor(getApplicationContext(), tagRead, time);
                final Context appContext = getApplicationContext();
                final Handler handler = new Handler(appContext.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.setEventNFCParameters(_tagRead, _time);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_NFC_TAG, false);
                    }
                });

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
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
