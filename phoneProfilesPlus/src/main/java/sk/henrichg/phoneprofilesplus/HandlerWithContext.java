package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;

class HandlerWithContext extends Handler {

    // A weak reference to the enclosing context
    private WeakReference<Context> mContext;

    HandlerWithContext (Looper looper, Context context) {
        super(looper);
        mContext = new WeakReference<Context>(context);
    }

    public Context getContext() {
        return mContext.get();
    }

}
