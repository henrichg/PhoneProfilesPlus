package sk.henrichg.phoneprofilesplus;

import android.nfc.INfcAdapter;
import android.os.ServiceManager;

/**
 * A shell executable for NTC toggle.
 */
class CmdNfc {

  public static void main(String[] args) {
      //PPApplication.logE("CmdNfc.main", "args="+args);
      if (!(run(Boolean.parseBoolean(args[0])))) {
          System.exit(1);
      }
  }

  static boolean run(boolean newValue) {
      try {
          INfcAdapter adapter = INfcAdapter.Stub.asInterface(ServiceManager.getService("nfc"));
          return newValue ? adapter.enable() : adapter.disable(true);
      } catch (Throwable e) {
          return false;
      }
  }

}
