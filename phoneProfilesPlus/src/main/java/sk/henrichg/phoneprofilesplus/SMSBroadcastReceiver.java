package sk.henrichg.phoneprofilesplus;

import java.util.Calendar;
import java.util.TimeZone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.SmsMessage;

public class SMSBroadcastReceiver extends WakefulBroadcastReceiver {

	public static final String BROADCAST_RECEIVER_TYPE = "SMS";
	
	//private static ContentObserver smsObserver;
	//private static ContentObserver mmsObserver;
	//private static int mmsCount;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		GlobalData.logE("#### SMSBroadcastReceiver.onReceive","xxx");

		boolean smsMmsReceived = false;
		
	    String origin = "";
	    //String body = "";
		
	    if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
	    {
			GlobalData.logE("SMSBroadcastReceiver.onReceive","SMS received");

			smsMmsReceived = true;
			
			Bundle extras = intent.getExtras();
			Object[] pdus = (Object[]) extras.get("pdus");
			for (Object pdu : pdus)
			{
				SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdu);
			    origin = msg.getOriginatingAddress();
			    //body = msg.getMessageBody();
			}
			
			
	    }
	    /*else
	    if(intent.getAction().equals("android.provider.Telephony.SMS_SENT"))
	    {
			GlobalData.logE("SMSBroadcastReceiver.onReceive","sent");
	    }*/
	    else
	    if (intent.getAction().equals("android.provider.Telephony.WAP_PUSH_RECEIVED") &&
	    	intent.getType().equals("application/vnd.wap.mms-message"))
	    {
			GlobalData.logE("SMSBroadcastReceiver.onReceive","MMS received");

			smsMmsReceived = true;
	    
			Bundle bundle = intent.getExtras();
            if (bundle != null)
            {
                byte[] buffer = bundle.getByteArray("data");
                String incomingNumber = new String(buffer);
                int indx = incomingNumber.indexOf("/TYPE");

                if(indx>0 && (indx-15)>0){
                    int newIndx = indx - 15;
                    incomingNumber = incomingNumber.substring(newIndx, indx);
                    indx = incomingNumber.indexOf("+");
                    if(indx>0){
                    	origin = incomingNumber.substring(indx);
                    }
                }
                /*int transactionId = bundle.getInt("transactionId");
                int pduType = bundle.getInt("pduType");
                byte[] buffer2 = bundle.getByteArray("header");      
                String header = new String(buffer2);*/
            }
	    }

	    if (smsMmsReceived)
	    {
			GlobalData.logE("SMSBroadcastReceiver.onReceive","from="+origin);
			//GlobalData.logE("SMSBroadcastReceiver.onReceive","message="+body);
	
			SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
			Editor editor = preferences.edit();
			//editor.putInt(GlobalData.PREF_EVENT_SMS_EVENT_TYPE, EventPreferencesSMS.SMS_EVENT_INCOMING);
			editor.putString(GlobalData.PREF_EVENT_SMS_PHONE_NUMBER, origin);
	        
			Calendar now = Calendar.getInstance();
	        int gmtOffset = TimeZone.getDefault().getRawOffset();
			long time = now.getTimeInMillis() + gmtOffset;
			editor.putLong(GlobalData.PREF_EVENT_SMS_DATE, time);
	
			editor.commit();
			
			startService(context);
	    }
	}
	
	private static void startService(Context context)
	{
		if (!GlobalData.getApplicationStarted(context))
			// application is not started
			return;

		GlobalData.loadPreferences(context);
		
		if (GlobalData.getGlobalEventsRuning(context))
		{
			GlobalData.logE("@@@ SMSBroadcastReceiver.startService","xxx");

			boolean smsEventsExists = false;
			
			DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
			smsEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_SMS) > 0;
			GlobalData.logE("SMSBroadcastReceiver.onReceive","timeEventsExists="+smsEventsExists);
			dataWrapper.invalidateDataWrapper();

			if (smsEventsExists)
			{
				// start service
				Intent eventsServiceIntent = new Intent(context, EventsService.class);
				eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
				startWakefulService(context, eventsServiceIntent);
			}
		}
	}

/*	
	private static final String CONTENT_SMS = "content://sms";
	// Constant from Android SDK
	private static final int MESSAGE_TYPE_SENT = 2;
	
	
	// Register an observer for listening outgoing sms events.
	// @author khoanguyen
	static public void registerSMSContentObserver(Context context)
	{
		if (smsObserver != null)
			return;

		final Context _context = context;
		
		smsObserver = new ContentObserver(null)
		{
			public void onChange(boolean selfChange)
			{
				GlobalData.logE("SMSBroadcastReceiver.smsObserver.onChange","xxx");

				// read outgoing sms from db
				Cursor cursor = _context.getContentResolver().query(Uri.parse(CONTENT_SMS), null, null, null, null);
				if (cursor.moveToNext())
				{
					String protocol = cursor.getString(cursor.getColumnIndex("protocol"));
					int type = cursor.getInt(cursor.getColumnIndex("type"));
					// Only processing outgoing sms event & only when it
					// is sent successfully (available in SENT box).
					if (protocol != null || type != MESSAGE_TYPE_SENT)
					{
						GlobalData.logE("SMSBroadcastReceiver.smsObserver.onChange","no SMS in SENT box");
						return;
					}
					int dateColumn = cursor.getColumnIndex("date");
					//int bodyColumn = cursor.getColumnIndex("body");
					int addressColumn = cursor.getColumnIndex("address");

					String to = cursor.getString(addressColumn);
					Date date = new Date(cursor.getLong(dateColumn));
					//String message = cursor.getString(bodyColumn);
					
					GlobalData.logE("SMSBroadcastReceiver.smsObserver.onChange","sms sent");
					GlobalData.logE("SMSBroadcastReceiver.smsObserver.onChange","to="+to);
					GlobalData.logE("SMSBroadcastReceiver.smsObserver.onChange","date="+date);
					//GlobalData.logE("SMSBroadcastReceiver.smsObserver.onChange","message="+message);
					
					SharedPreferences preferences = _context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
					Editor editor = preferences.edit();
					editor.putInt(GlobalData.PREF_EVENT_SMS_EVENT_TYPE, EventPreferencesSMS.SMS_EVENT_OUTGOING);
					editor.putString(GlobalData.PREF_EVENT_SMS_PHONE_NUMBER, to);
			        int gmtOffset = TimeZone.getDefault().getRawOffset();
					long time = date.getTime() + gmtOffset;
					editor.putLong(GlobalData.PREF_EVENT_SMS_DATE, time);
					editor.commit();
				}
				cursor.close();
				
				startService(_context);
			}
		};
		
		context.getContentResolver().registerContentObserver(Uri.parse(CONTENT_SMS), true, smsObserver);
	}
		
	public static void unregisterSMSContentObserver(Context context)
	{
		if (smsObserver != null)
			context.getContentResolver().unregisterContentObserver(smsObserver);		
	}

	// not working with with Hangouts :-/
	static public void registerMMSContentObserver(Context context)
	{
		if (mmsObserver != null)
			return;

        Uri uriMMSURI = Uri.parse("content://mms");
        Cursor mmsCur = context.getContentResolver().query(uriMMSURI, null, "msg_box = 4", null, "_id");
        if (mmsCur != null && mmsCur.getCount() > 0) {
           mmsCount = mmsCur.getCount();
        }
		
		final Context _context = context;
		
		mmsObserver = new ContentObserver(null)
		{
			public void onChange(boolean selfChange)
			{
				GlobalData.logE("SMSBroadcastReceiver.mmsObserver.onChange","xxx");

				// read outgoing mms from db
                Uri uriMMSURI = Uri.parse("content://mms/");
                Cursor mmsCur = _context.getContentResolver().query(uriMMSURI, null, "msg_box = 4 or msg_box = 1", null,"_id");

                int currMMSCount = 0;
                if (mmsCur != null && mmsCur.getCount() > 0) {
                   currMMSCount = mmsCur.getCount();
                }

				GlobalData.logE("SMSBroadcastReceiver.mmsObserver.onChange","mmsCount="+mmsCount);
				GlobalData.logE("SMSBroadcastReceiver.mmsObserver.onChange","currMMSCount="+currMMSCount);
                
                if (currMMSCount > mmsCount)
                {
	                if (mmsCur.moveToLast())
	                {
	                	// 132 (RETRIEVE CONF) 130 (NOTIF IND) 128 (SEND REQ)
	                    int type = Integer.parseInt(mmsCur.getString(mmsCur.getColumnIndex("m_type")));

	    				GlobalData.logE("SMSBroadcastReceiver.mmsObserver.onChange","type="+type);
	                    
	                    if (type == 128) {
	                       // Outgoing MMS

          					GlobalData.logE("SMSBroadcastReceiver.mmsObserver.onChange","mms sent");
	                    	
	                    	int id = Integer.parseInt(mmsCur.getString(mmsCur.getColumnIndex("_id")));
	                    	
	                    	// Get Address
	                        Uri uriAddrPart = Uri.parse("content://mms/"+id+"/addr");
	                        Cursor addrCur = _context.getContentResolver().query(uriAddrPart, null, "type=151", null, "_id");
	                        if (addrCur != null)
	                        {
	                        	if (addrCur.moveToLast())
	                        	{   
		                        	do
		                        	{
		                        		int addColIndx = addrCur.getColumnIndex("address");
		                        		String to = addrCur.getString(addColIndx);
		                              
			        					GlobalData.logE("SMSBroadcastReceiver.mmsObserver.onChange","to="+to);
			        					
			        					SharedPreferences preferences = _context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
			        					Editor editor = preferences.edit();
			        					editor.putInt(GlobalData.PREF_EVENT_SMS_EVENT_TYPE, EventPreferencesSMS.SMS_EVENT_OUTGOING);
			        					editor.putString(GlobalData.PREF_EVENT_SMS_PHONE_NUMBER, to);
			        					Calendar now = Calendar.getInstance();
			        			        int gmtOffset = TimeZone.getDefault().getRawOffset();
			        					long time = now.getTimeInMillis() + gmtOffset;
			        					editor.putLong(GlobalData.PREF_EVENT_SMS_DATE, time);
			        					editor.commit();
			        					
			            				startService(_context);
		                            }
		                        	while (addrCur.moveToPrevious());
	                        	}
	                        }	                    	
	                    }                	
	                }
                }
                
                mmsCount = currMMSCount;
                
			}
		};
		
		context.getContentResolver().registerContentObserver(Uri.parse("content://mms-sms"), true, mmsObserver);
	}
		
	public static void unregisterMMSContentObserver(Context context)
	{
		if (mmsObserver != null)
			context.getContentResolver().unregisterContentObserver(mmsObserver);		
	}
*/
}
