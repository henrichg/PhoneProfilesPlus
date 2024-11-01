package sk.henrichg.phoneprofilesplus;

//import static org.acra.util.IOUtils.writeStringToFile;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.google.auto.service.AutoService;

import org.acra.attachment.AcraContentProvider;
import org.acra.config.Configuration;
import org.acra.config.CoreConfiguration;
import org.acra.config.MailSenderConfiguration;
import org.acra.data.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.acra.sender.ReportSenderFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Custom ACRA e-mail ReportSender used in Pixel devices with Android 13
// https://github.com/ACRA/acra/tree/master/examples/acra-basic-java-example
// https://github.com/ACRA/acra/blob/master/acra-mail/src/main/java/org/acra/sender/EmailIntentSender.kt

@SuppressWarnings("unused")
public class CustomACRAEmailSender implements ReportSender {

    final CoreConfiguration coreConfiguration;

    CustomACRAEmailSender(@NotNull CoreConfiguration coreConfiguration) {
//        Log.e("CustomACRAEmailSender constructor", "***** ###### *****");
        this.coreConfiguration = coreConfiguration;
    }

    @Override
    public boolean requiresForeground() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public void send(@NotNull Context context, @NotNull CrashReportData errorContent)
            throws ReportSenderException {
//        Log.e("CustomACRAEmailSender.send", "Report Sent!");

        try {
            List<Configuration> plugins = coreConfiguration.getPluginConfigurations();
//        Log.e("CustomACRAEmailSender.send", "plugins.size=" + plugins.size());
            for (Configuration plugin : plugins) {
                if (plugin instanceof MailSenderConfiguration) {
//                Log.e("CustomACRAEmailSender.send", "MailSenderConfiguration");

                    MailSenderConfiguration mailConfig = (MailSenderConfiguration) plugin;

                    if (Build.VERSION.SDK_INT >= 35) {
                        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
                        emailIntent.setType("message/rfc822"); // only email apps should handle this
                        //emailIntent.setData(Uri.parse(StringConstants.INTENT_DATA_MAIL_TO_COLON));

                        String reportText;
                        try {
                            reportText = coreConfiguration.getReportFormat().toFormattedString(errorContent, coreConfiguration.getReportContent(), StringConstants.CHAR_NEW_LINE, "\n\t", false);
                        } catch (Exception e) {
                            throw new ReportSenderException("Failed to convert Report to text", e);
                        }

                        ArrayList<Uri> attachments = new ArrayList<>();
                        Uri report = createAttachmentFromString(context, mailConfig.getReportFileName(), reportText);
                        if (report != null) {
                            /*try {
                                ContentResolver contentResolver = context.getContentResolver();
                                context.grantUriPermission(PPApplication.PACKAGE_NAME, report, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                contentResolver.takePersistableUriPermission(report, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception e) {
                                // java.lang.SecurityException: UID 10157 does not have permission to
                                // content://com.android.externalstorage.documents/document/93ED-1CEC%3AMirek%2Fmobil%2F.obr%C3%A1zek%2Fblack.jpg
                                // [user 0]; you could obtain access using ACTION_OPEN_DOCUMENT or related APIs
                                //Log.e("ActivateProfileHelper.setTones (1)", Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }*/
                            attachments.add(report);
                        }

                        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments);
                        String emailAddress = mailConfig.getMailTo();
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});

                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, mailConfig.getSubject());
                        emailIntent.putExtra(Intent.EXTRA_TEXT, mailConfig.getBody());
                        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        try {
                            Intent chooser = Intent.createChooser(emailIntent, context.getString(R.string.email_chooser));
                            //chooser.putExtra(Intent.EXTRA_INTENT, intents.get(0));
                            //chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new LabeledIntent[0]));
                            context.startActivity(chooser);
                        } catch (Exception e) {
                            PPApplicationStatic.logException("CustomACRAEmailSender.send", Log.getStackTraceString(e));
                        }
                    } else {
                        String emailAddress = mailConfig.getMailTo();
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                StringConstants.INTENT_DATA_MAIL_TO, emailAddress, null));

                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, mailConfig.getSubject());
                        emailIntent.putExtra(Intent.EXTRA_TEXT, mailConfig.getBody());
                        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        ArrayList<Uri> attachments = new ArrayList<>();

                        String reportText;
                        try {
                            reportText = coreConfiguration.getReportFormat().toFormattedString(errorContent, coreConfiguration.getReportContent(), StringConstants.CHAR_NEW_LINE, "\n\t", false);
                        } catch (Exception e) {
                            throw new ReportSenderException("Failed to convert Report to text", e);
                        }

                        Uri report = createAttachmentFromString(context, mailConfig.getReportFileName(), reportText);
                        if (report != null) {
                            /*try {
                                ContentResolver contentResolver = context.getContentResolver();
                                context.grantUriPermission(PPApplication.PACKAGE_NAME, report, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                contentResolver.takePersistableUriPermission(report, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception e) {
                                // java.lang.SecurityException: UID 10157 does not have permission to
                                // content://com.android.externalstorage.documents/document/93ED-1CEC%3AMirek%2Fmobil%2F.obr%C3%A1zek%2Fblack.jpg
                                // [user 0]; you could obtain access using ACTION_OPEN_DOCUMENT or related APIs
                                //Log.e("ActivateProfileHelper.setTones (1)", Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }*/
                            attachments.add(report);
                        }

                        List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentActivities(emailIntent, 0);
//                        Log.e("CustomACRAEmailSender.send", "resolveInfo.size()=" + resolveInfo.size());
                        List<LabeledIntent> intents = new ArrayList<>();
                        for (ResolveInfo info : resolveInfo) {
                            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                            intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                            //if (!emailAddress.isEmpty())
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                            intent.putExtra(Intent.EXTRA_SUBJECT, mailConfig.getSubject());
                            intent.putExtra(Intent.EXTRA_TEXT, mailConfig.getBody());
                            intent.setType(StringConstants.MINE_TYPE_ALL); // gmail will only match with type set
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments); //ArrayList<Uri> of attachment Uri's
                            intents.add(new LabeledIntent(intent, info.activityInfo.packageName, info.loadLabel(context.getPackageManager()), info.icon));
                        }
//                        Log.e("CustomACRAEmailSender.send", "intents.size()="+intents.size());
                        if (!intents.isEmpty()) {
                            try {
//                                for (Intent _intent : intents) {
//                                    Log.e("CustomACRAEmailSender.send", "intents.size()=" + _intent.getAction());
//                                }
                                Intent chooser = Intent.createChooser(new Intent(Intent.ACTION_CHOOSER), context.getString(R.string.email_chooser));
                                chooser.putExtra(Intent.EXTRA_INTENT, intents.get(0));
                                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new LabeledIntent[0]));
                                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(chooser);
//                                Log.e("CustomACRAEmailSender.send", "mail sent");
                            } catch (Exception e) {
                                PPApplicationStatic.logException("CustomACRAEmailSender.send", Log.getStackTraceString(e));
                            }
                        }
                    }

                    break;
                }
            }
        } catch (Exception ee) {
            PPApplicationStatic.logException("CustomACRAEmailSender.send", Log.getStackTraceString(ee));
        }
    }

    protected Uri createAttachmentFromString(Context context,
                                             @SuppressWarnings("SameParameterValue") String name,
                                             String content) {
        File cache = new File(context.getCacheDir(), name);
        try {
            org.acra.util.IOUtils.writeStringToFile(cache, content);
            return AcraContentProvider.getUriForFile(context, cache);
        } catch (IOException ignored) {}
        return null;
    }

    @AutoService(ReportSenderFactory.class)
    public static class CustomACRAEmailSenderFactory implements ReportSenderFactory {
        @NotNull
        @Override
        public ReportSender create(@NotNull Context context, @NotNull CoreConfiguration coreConfiguration) {
//            Log.e("CustomACRAEmailSenderFactory.create", "#### ***** ####");
            return new CustomACRAEmailSender(coreConfiguration);
        }
    }

}
