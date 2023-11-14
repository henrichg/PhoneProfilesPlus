package sk.henrichg.phoneprofilesplus;

import android.content.pm.PackageManager;
import rikka.shizuku.Shizuku;

/**
 * Some convenience functions for handling using Shizuku.
 */
class ShizukuUtils {
    /**
     * Checks if Shizuku is available. If the Shizuku Manager app
     * is either uninstalled OR isn't running, this will return
     * false.
     */
    //static boolean shizukuAvailable = Shizuku.pingBinder() && ((Shizuku.getVersion() >= 11) && (!Shizuku.isPreV11()));
    //static boolean shizukuInstalled = (Shizuku.getVersion() >= 11);

    static boolean shizukuAvailable() {
        return Shizuku.pingBinder() && ((Shizuku.getVersion() >= 11) && (!Shizuku.isPreV11()));
    }

    /**
     * Checks if the current app has permission to use Shizuku.
     */
    static boolean hasShizukuPermission() {
        if (!shizukuAvailable()) {
            return false;
        }

        // !!! required is Shizuku v11+
        return (Shizuku.getVersion() >= 11) &&
                (!Shizuku.isPreV11()) &&
                (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED);
    }

    /** @noinspection deprecation*/
    static void executeCommand(String command) throws InterruptedException {
        // I tried working with a Shizuku user bound process and using hidden APIs, but did not
        //  fully get it to work, so I just use the same commands as I did when using SU.
        Process process = Shizuku.newProcess(
            command.split(" "),
            null,
            null
        );
        process.waitFor();
    }

    /** @noinspection deprecation*/
    static Process executeCommandNoWait(String command) {
        // I tried working with a Shizuku user bound process and using hidden APIs, but did not
        //  fully get it to work, so I just use the same commands as I did when using SU.
        return Shizuku.newProcess(
                command.split(" "),
                null,
                null
        );
    }

}
