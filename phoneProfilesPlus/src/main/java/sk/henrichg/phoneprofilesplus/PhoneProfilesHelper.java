package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.stericson.rootshell.execution.Command;
import com.stericson.rootshell.execution.Shell;
import com.stericson.roottools.RootTools;

import java.util.List;

class PhoneProfilesHelper {

    static int PPHelperVersion = -1;

    //static final int PPHELPER_CURRENT_VERSION = 59;

    private static boolean errorNoRoot = false;
    //private static boolean nowPPHelperUninstalled = false;

    /*
    static boolean isPPHelperInstalled(Context context, int minVersion)
    {
        PPHelperVersion = -1;

        if (nowPPHelperUninstalled)
            return false;

        // get package version
        PackageInfo pinfo = null;
        try {
            pinfo = context.getPackageManager().getPackageInfo("sk.henrichg.phoneprofileshelper", 0);
            GlobalData.logE("PhoneProfilesHelper.isPPHelperInstalled", "found");
            PPHelperVersion = pinfo.versionCode;
        } catch (NameNotFoundException e) {
            GlobalData.logE("PhoneProfilesHelper.isPPHelperInstalled", "not found");
            //e.printStackTrace();
        }
        return PPHelperVersion >= minVersion;
    }
    */

    static private boolean doUninstallPPHelper(Activity activity)
    {
        boolean OK;
        errorNoRoot = false;

        /*if (!GlobalData.isRooted(false))
        {
            Log.e("PhoneProfilesHelper.doUninstallPPHelper", "Device is not rooted");
            errorNoRoot = true;
            return false;
        }*/

        if (!GlobalData.isRooted()/*GlobalData.isRootGranted()*/)
        {
            GlobalData.logE("PhoneProfilesHelper.doUninstallPPHelper", "Grant root failed");
            errorNoRoot = true;
            return false;
        }

        String destinationFile = "PhoneProfilesHelper.apk";
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2)
            destinationFile = "/system/priv-app/"+destinationFile;
        else
            destinationFile = "/system/app/"+destinationFile;

        if (GlobalData.isSELinuxEnforcing())
            Shell.defaultContext = Shell.ShellContext.RECOVERY;
        OK = RootTools.remount("/system", "RW");
        boolean remountOK = OK;
        if (!OK)
            GlobalData.logE("PhoneProfilesHelper.doUninstallPPHelper", "remount RW ERROR");
        if (OK)
            //OK = RootTools.deleteFileOrDirectory(destinationFile, true);
            OK = deleteFile_su(destinationFile);
        if (!OK)
            GlobalData.logE("PhoneProfilesHelper.doUninstallPPHelper", "delete file ERROR");
        if (remountOK)
            //OK =
            RootTools.remount("/system", "RO");
        //if (!OK)
        //    Log.e("PhoneProfilesHelper.doUninstallPPHelper", "remount RO ERROR");
        if (GlobalData.isSELinuxEnforcing())
            Shell.defaultContext = Shell.ShellContext.NORMAL;

        /*
        String command1 = "mount -o remount,rw /system";		//mounts the system partition to be writeable
        String command2 = "rm "+destinationFile;				//removes the old systemapp
        String command3 = "mount -o remount,ro /system";		//mounts the system partition to be read-only again
        //if (GlobalData.isSELinuxEnforcing())
        //{
        //	command1 = GlobalData.getSELinuxEnforceCommand(command1, Shell.ShellContext.RECOVERY);
        //	command2 = GlobalData.getSELinuxEnforceCommand(command2, Shell.ShellContext.RECOVERY);
        //	command3 = GlobalData.getSELinuxEnforceCommand(command3, Shell.ShellContext.RECOVERY);
        //}
        Command command = new Command(0, false, command1, command2, command3);
        try {
            RootTools.closeAllShells();
            RootTools.getShell(true, Shell.ShellContext.RECOVERY).add(command);
            OK = commandWait(command);
            if (OK)
                Log.e("PhoneProfilesHelper.doUninstallPPHelper", "PhoneProfilesHelper uninstalled");
            else
                Log.e("PhoneProfilesHelper.doUninstallPPHelper", "PhoneProfilesHelper uninstallation failed!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PhoneProfilesHelper.doUninstallPPHelper", "PhoneProfilesHelper uninstallation failed!");
            OK = false;
        }
        */

        if (OK)
        {
            //nowPPHelperUninstalled = true;

            Context context = activity.getApplicationContext();
            // update profiles for hardware changes
            DatabaseHandler databaseHandler = DatabaseHandler.getInstance(context);
            databaseHandler.disableNotAllowedPreferences(context);
            // refresh GUI
            Intent refreshIntent = new Intent();
            refreshIntent.setAction(RefreshGUIBroadcastReceiver.INTENT_REFRESH_GUI);
            context.sendBroadcast(refreshIntent);
        }

        return OK;
    }

    static void uninstallPPHelper(Activity activity)
    {
        final Activity _activity = activity;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(activity.getResources().getString(R.string.phoneprofilehepler_uninstall_title));
        dialogBuilder.setMessage(activity.getResources().getString(R.string.phoneprofilehepler_uninstall_message));
        dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                class UninstallAsyncTask extends AsyncTask<Void, Integer, Boolean>
                {
                    private MaterialDialog dialog;

                    private UninstallAsyncTask()
                    {
                        this.dialog = new MaterialDialog.Builder(_activity)
                                .content(R.string.phoneprofilehepler_uninstall_title)
                                //.disableDefaultFonts()
                                .progress(true, 0)
                                .build();
                    }

                    @Override
                    protected void onPreExecute()
                    {
                        super.onPreExecute();

                        lockScreenOrientation();
                        this.dialog.setCancelable(false);
                        this.dialog.setCanceledOnTouchOutside(false);
                        this.dialog.show();
                    }

                    @Override
                    protected Boolean doInBackground(Void... params) {
                        return doUninstallPPHelper(_activity);
                    }

                    @Override
                    protected void onPostExecute(Boolean result)
                    {
                        super.onPostExecute(result);

                        if (dialog.isShowing())
                            dialog.dismiss();
                        unlockScreenOrientation();

                        if (result)
                        {
                            restartAndroid(_activity, /*2,*/ false);
                        }
                        else
                            installUnInstallPPhelperErrorDialog(_activity, /*2,*/ false);
                    }

                    private void lockScreenOrientation() {
                        int currentOrientation = _activity.getResources().getConfiguration().orientation;
                        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                            _activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                        } else {
                            _activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                        }
                    }

                    private void unlockScreenOrientation() {
                        _activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                    }

                }

                new UninstallAsyncTask().execute();
            }
        });
        dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
        dialogBuilder.show();
    }

    static private void restartAndroid(Activity activity, /*int installUninstall,*/ boolean finishActivity)
    {
        final Activity _activity = activity;
        final boolean _finishActivity = finishActivity;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.phoneprofilehepler_reboot_title_uninstall);
        dialogBuilder.setMessage(R.string.phoneprofilehepler_reboot_message_uninstall);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

        dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // restart device
                RootTools.restartAndroid();
            }
        });
        dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (_finishActivity)
                    _activity.finish();
            }
        });
        dialogBuilder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (_finishActivity)
                    _activity.finish();
            }
        });

        dialogBuilder.show();
    }

    static private boolean commandWait(Command cmd) throws Exception {
        boolean OK;

        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 6400; //7 tries, 12750 msec
        //50+100+200+400+800+1600+3200+6400

        OK = true;
        
        while (!cmd.isFinished() && waitTill<=waitTillLimit) {
            synchronized (cmd) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(waitTill);
                        waitTill *= waitTillMultiplier;
                    }
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                    OK = false;
                }
            }
        }
        if (!cmd.isFinished()){
            Log.e("PhoneProfilesHelper.commandWaid", "Could not finish root command in " + (waitTill/waitTillMultiplier));
            OK = false;
        }
        
        return OK;
    }

    static private void installUnInstallPPhelperErrorDialog(Activity activity, /*int installUninstall,*/ boolean finishActivity)
    {
        final Activity _activity = activity;
        final boolean _finishActivity = finishActivity;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        int resString;
        resString = R.string.phoneprofilehepler_uninstall_title;
        dialogBuilder.setTitle(resString);
        if (!errorNoRoot)
        {
            resString = R.string.phoneprofilehepler_uninstall_error;
        }
        else
        {
            resString = R.string.phoneprofilehepler_uninstall_error_no_root;
        }
        dialogBuilder.setMessage(resString);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

        dialogBuilder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (_finishActivity)
                    _activity.finish();
            }
        });
        dialogBuilder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (_finishActivity)
                    _activity.finish();
            }
        });

        dialogBuilder.show();
    }

    private static boolean deleteFile_su(String file) {
        boolean OK;

        List<String> settingsPaths = RootTools.findBinary("rm");
        if (settingsPaths.size() > 0) {
            String command1 = "rm " + file;
            //if (GlobalData.isSELinuxEnforcing())
            //	command1 = GlobalData.getSELinuxEnforceCommad(command1);
            Command command = new Command(0, false, command1);
            try {
                //RootTools.closeAllShells();
                RootTools.getShell(true, Shell.ShellContext.RECOVERY).add(command);
                OK = commandWait(command);
                OK = OK && command.getExitCode() == 0;
            } catch (Exception e) {
                //e.printStackTrace();
                OK = false;
            }
        }
        else {
            OK = RootTools.deleteFileOrDirectory(file, false);
        }
        return OK;
    }
}
