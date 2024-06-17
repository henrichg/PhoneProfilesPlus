package sk.henrichg.phoneprofilesplus;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DownloadCompletedBroadcastReceiver extends BroadcastReceiver {

    static long downloadReferencePPP = 0;
    static long downloadReferencePPPE = 0;
    static long downloadReferencePPPPS = 0;
    static long downloadReferenceInstallWithOptions = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        /*
        Log.e("DownloadCompletedBroadcastReceiver.onReceive", "downloadReferencePPP="+downloadReferencePPP);
        Log.e("DownloadCompletedBroadcastReceiver.onReceive", "downloadReferencePPPE="+downloadReferencePPPE);
        Log.e("DownloadCompletedBroadcastReceiver.onReceive", "downloadReferencePPPPS="+downloadReferencePPPPS);
        Log.e("DownloadCompletedBroadcastReceiver.onReceive", "downloadReferenceInstallWithOptions="+downloadReferenceInstallWithOptions);

        long completedDownloadReference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        Log.e("DownloadCompletedBroadcastReceiver.onReceive", "completedDownloadReference="+completedDownloadReference);

        try {
            Uri apkFile = null;

            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            if (completedDownloadReference == downloadReferencePPPPS) {
                apkFile = downloadManager.getUriForDownloadedFile(completedDownloadReference);
                Log.e("DownloadCompletedBroadcastReceiver.onReceive", "apkFile="+apkFile);
            }

            if (apkFile != null) //noinspection ExtractMethodRecommender
            {
                Intent intentDownloads = new Intent(Intent.ACTION_VIEW, apkFile);
                //intentDownloads.setDataAndType(apkFile, downloadManager.getMimeTypeForDownloadedFile(completedDownloadReference));
                //intentDownloads.setData(apkFile);
                //intentDownloads.putExtra(Intent.EXTRA_STREAM, apkFile);
                intentDownloads.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intentDownloads.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentDownloads);
            }
        } catch (Exception e) {
            Log.e("DownloadCompletedBroadcastReceiver.onReceive", Log.getStackTraceString(e));
        }
        */

        try {
            Intent intentDownloads = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            intentDownloads.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentDownloads);
        } catch (Exception e) {
            Log.e("DownloadCompletedBroadcastReceiver.onReceive", Log.getStackTraceString(e));
        }
    }

}
