package sk.henrichg.phoneprofilesplus;

// Thanks to author of MacroDroid application.
// It is used as source of this implenetation.

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.RequiresApi;

import com.android.dx.Code;
import com.android.dx.DexMaker;
import com.android.dx.Local;
import com.android.dx.MethodId;
import com.android.dx.TypeId;

import java.io.IOException;

public final class WifiTetheringCallbackMaker {

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final MyOnStartTetheringCallbackAbstract myOnStartTetheringCallbackAbstract;

    final Context context;
    Class<?> tetheringCallback;
    DexMaker dexMaker;

    @SuppressWarnings({"rawtypes", "RedundantArrayCreation"})
    @SuppressLint("PrivateApi")
    @RequiresApi(api = 21)
    public WifiTetheringCallbackMaker(Context context,
                                      MyOnStartTetheringCallbackAbstract myOnStartTetheringCallbackAbstract) {
        Class<?> onStartTetheringCallback;
        this.context = context;
        this.myOnStartTetheringCallbackAbstract = myOnStartTetheringCallbackAbstract;
        try {
            onStartTetheringCallback = Class.forName("android.net.ConnectivityManager$OnStartTetheringCallback");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            onStartTetheringCallback = null;
        }
        if (onStartTetheringCallback != null) {
            TypeId<?> typeIdOfOnStartTetheringCallback = TypeId.get(onStartTetheringCallback);
            this.dexMaker = new DexMaker();
            TypeId typeIdOfLTetheringCallback = TypeId.get("LTetheringCallback;");
            this.dexMaker.declare(typeIdOfLTetheringCallback, "TetheringCallback.generated", 1, typeIdOfOnStartTetheringCallback, new TypeId[0]);
            this.dexMaker.declare(typeIdOfLTetheringCallback.getField(TypeId.get(MyOnStartTetheringCallbackAbstract.class), "callback"), 2, (Object) null);
            invokeOnStartTetheringCallback(typeIdOfLTetheringCallback, typeIdOfOnStartTetheringCallback);
            //noinspection TryWithIdenticalCatches
            try {
                this.tetheringCallback = this.dexMaker.generateAndLoad(WifiTetheringCallbackMaker.class.getClassLoader(), this.context.getCodeCacheDir()).loadClass("TetheringCallback");
            } catch (IOException e2) {
                e2.printStackTrace();
            } catch (ClassNotFoundException e3) {
                e3.printStackTrace();
            }
        }
    }

    @SuppressWarnings({"RedundantArrayCreation", "rawtypes", "unchecked"})
    public void invokeOnStartTetheringCallback(TypeId typeIdOfLTetheringCallback, TypeId<?> typeIdOfOnStartTetheringCallback) {
        MethodId<?, Void> constructor = typeIdOfOnStartTetheringCallback.getConstructor(new TypeId[0]);
        Code declare = this.dexMaker.declare(typeIdOfLTetheringCallback.getConstructor(TypeId.INT), 1);
        declare.invokeDirect(constructor,
                null,
                declare.getThis(typeIdOfLTetheringCallback),
                new Local[0]);
        declare.returnVoid();
    }

    /* renamed from: b */
    public Class<?> getTtetheringCallback() {
        return this.tetheringCallback;
    }

}
