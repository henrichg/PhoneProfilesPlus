package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import java.util.Calendar;

public class NFCTagReadActivity extends Activity {

    private NFCTagReadWriteManager nfcManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcManager = new NFCTagReadWriteManager(this);
        nfcManager.onActivityCreate();

        nfcManager.setOnTagReadListener(new NFCTagReadWriteManager.TagReadListener() {
            @Override
            public void onTagRead(String tagRead) {
                Toast.makeText(NFCTagReadActivity.this, "("+getString(R.string.app_name)+") "+getString(R.string.read_nfc_tag_readed)+": "+tagRead, Toast.LENGTH_LONG).show();

                Calendar now = Calendar.getInstance();
                int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                long time = now.getTimeInMillis() + gmtOffset;

                EventsHandlerJob.startForNFCTagSensor(tagRead, time);

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

}
