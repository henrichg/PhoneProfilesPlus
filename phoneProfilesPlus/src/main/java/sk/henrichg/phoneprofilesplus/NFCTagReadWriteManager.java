package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcBarcode;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Parcelable;

import java.io.IOException;

import sk.henrichg.phoneprofilesplus.NFCTagWriteException.NFCErrorType;

class NFCTagReadWriteManager {
    private NfcAdapter nfcAdapter;
    private final Activity activity;
    private PendingIntent pendingIntent;

    //boolean uidRead = false;

    boolean tagRead = false;

    boolean tagIsWritable;  // is tag writable?

    private TagReadListener onTagReadListener;
    private TagWriteListener onTagWriteListener;
    private TagWriteErrorListener onTagWriteErrorListener;

    private String writeText = null;

    // list of NFC technologies detected:
    private final String[][] techList = new String[][] {
            new String[] {
                    NfcA.class.getName(),
                    NfcB.class.getName(),
                    NfcF.class.getName(),
                    NfcV.class.getName(),
                    IsoDep.class.getName(),
                    MifareClassic.class.getName(),
                    MifareUltralight.class.getName(),
                    NfcBarcode.class.getName(),
                    Ndef.class.getName(),
                    NdefFormatable.class.getName()
            }
    };

    NFCTagReadWriteManager(Activity activity) {
        this.activity = activity;
    }

    /*
     * Sets the listener to read events
     */
    void setOnTagReadListener(TagReadListener onTagReadListener) {
        this.onTagReadListener = onTagReadListener;
    }

    /*
     * Sets the listener to write events
     */
    void setOnTagWriteListener(TagWriteListener onTagWriteListener) {
        this.onTagWriteListener = onTagWriteListener;
    }

    /*
     * Sets the listener to write error events
     */
    void setOnTagWriteErrorListener(TagWriteErrorListener onTagWriteErrorListener) {
        this.onTagWriteErrorListener = onTagWriteErrorListener;
    }

    /*
     * Indicates that we want to write the given text to the next tag detected
     */
    void writeText(String writeText) {
        this.writeText = writeText;
    }

    /*
     * Stops a writeText operation
     */
    /*public void undoWriteText() {
        this.writeText = null;
    }*/


    /*
     * To be executed on OnCreate of the activity
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    void onActivityCreate() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        pendingIntent = PendingIntent.getActivity(activity, 0,
                new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    /*
     * To be executed on onResume of the activity
     */
    void onActivityResume() {
        if (nfcAdapter != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
            filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
            filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
            nfcAdapter.enableForegroundDispatch(activity, pendingIntent, new IntentFilter[]{filter}, techList);
            //if (writeText == null)
            readTagFromIntent(activity.getIntent());
        }
    }

    /*
     * To be executed on onPause of the activity
     */
    void onActivityPause() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(activity);
        }
    }

    /*
     * To be executed on onNewIntent of activity
     * @param intent x
     */
    void onActivityNewIntent(Intent intent) {
        if (nfcAdapter != null) {
            // activity.setIntent(intent);

            //if (writeText == null)
            readTagFromIntent(intent);
            //else {
            if (writeText != null)/* && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))*/ {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                try {
                    writeTag(/*activity, */tag, writeText);
                    onTagWriteListener.onTagWritten();
                } catch (NFCTagWriteException exception) {
                    onTagWriteErrorListener.onTagWriteError(exception);
                } finally {
                    writeText = null;
                }
            }
        }
    }

    /*
     * Reads a tag for a given intent and notifies listeners
     * @param intent x
     */
    private void readTagFromIntent(Intent intent) {
        if (intent != null){
            String action = intent.getAction();

            /*if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
                uidRead = true;

                String uid = ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID));
                onTagReadListener.onUidRead(uid);
            }*/
            if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
                tagRead = true;

                // get NDEF tag details
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                if (tag != null) {
                    Ndef ndefTag = Ndef.get(tag);
                    //int tagSize = ndefTag.getMaxSize();         // tag size
                    tagIsWritable = ndefTag.isWritable();   // is tag writable?
                    //String tagType = ndefTag.getType();            // tag type

                    Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                    if (rawMessages != null) {
                        NdefRecord[] records = ((NdefMessage) rawMessages[0]).getRecords();
                        String text = ndefRecordToString(records[0]);
                        onTagReadListener.onTagRead(text);
                    }
                }
            }
        }
    }

    /*
    private String ByteArrayToHexString(byte [] inArray) {
        int i, j, in;
        String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        StringBuilder out= new StringBuilder();

        for(j = 0 ; j < inArray.length ; ++j)
        {
            in = (int) inArray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out.append(hex[i]);
            i = in & 0x0f;
            out.append(hex[i]);
        }
        return out.toString();
    }
    */

    private String ndefRecordToString(NdefRecord record) {
        byte[] payload = record.getPayload();
        return new String(payload);
    }

    /*
     * Writes a text to a tag
     * @param context x
     * @param tag x
     * @param data x
     * @throws NFCTagWriteException
     */
    private  void writeTag(/*Context context, */Tag tag, String data) throws NFCTagWriteException {
        // Record with actual data we care about
        //NdefRecord relayRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, null, data.getBytes());

        byte[] textBytes = data.getBytes();
        NdefRecord relayRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                "application/vnd.phoneprofilesplus.events".getBytes(), new byte[] {}, textBytes);

        // Complete NDEF message with both records
        NdefMessage message = new NdefMessage(new NdefRecord[] { relayRecord });

        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            // If the tag is already formatted, just write the message to it
            try {
                ndef.connect();
            } catch (IOException e) {
                throw new NFCTagWriteException(NFCTagWriteException.NFCErrorType.unknownError);
            }
            // Make sure the tag is writable
            if (!ndef.isWritable()) {
                throw new NFCTagWriteException(NFCErrorType.ReadOnly);
            }

            // Check if there's enough space on the tag for the message
            int size = message.toByteArray().length;
            if (ndef.getMaxSize() < size) {
                throw new NFCTagWriteException(NFCErrorType.NoEnoughSpace);
            }

            try {
                // Write the data to the tag
                ndef.writeNdefMessage(message);
            } catch (TagLostException tle) {
                throw new NFCTagWriteException(NFCTagWriteException.NFCErrorType.tagLost, tle);
            } catch (IOException | FormatException ioe) {
                throw new NFCTagWriteException(NFCErrorType.formattingError, ioe);// nfcFormattingErrorTitle
            }
        } else {
            // If the tag is not formatted, format it with the message
            NdefFormatable format = NdefFormatable.get(tag);
            if (format != null) {
                try {
                    format.connect();
                    format.format(message);
                } catch (TagLostException tle) {
                    throw new NFCTagWriteException(NFCErrorType.tagLost, tle);
                } catch (IOException | FormatException ioe) {
                    throw new NFCTagWriteException(NFCErrorType.formattingError, ioe);
                }
            } else {
                throw new NFCTagWriteException(NFCErrorType.noNdefError);
            }
        }

    }

    interface TagReadListener {
        //void onUidRead(String uid);
        void onTagRead(String tagData);
    }

    interface TagWriteListener {
        void onTagWritten();
    }

    interface TagWriteErrorListener {
        void onTagWriteError(NFCTagWriteException exception);
    }
}
