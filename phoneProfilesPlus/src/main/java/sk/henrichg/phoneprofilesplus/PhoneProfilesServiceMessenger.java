package sk.henrichg.phoneprofilesplus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class PhoneProfilesServiceMessenger {

    private Messenger mService = null;
    private int messageValue = 0;
    private boolean mIsBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, messageValue);
                msg.replyTo = null;
                mService.send(msg);
            }
            catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
        }
    };

    public void bingAndSendMessage(Context context, int messageValue) {
        this.messageValue = messageValue;
        if (!mIsBound) {
            mIsBound = true;
            context.bindService(new Intent(context.getApplicationContext(), PhoneProfilesService.class), mConnection, Context.BIND_AUTO_CREATE);
        }
        else {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, this.messageValue);
                    msg.replyTo = null;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
        }
    }

    public void unbind(Context context) {
        return;
        /*if (mIsBound) {
            context.unbindService(mConnection);
            mIsBound = false;
        }*/
    }

}
