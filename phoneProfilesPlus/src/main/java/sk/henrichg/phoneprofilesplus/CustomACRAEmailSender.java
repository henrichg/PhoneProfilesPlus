package sk.henrichg.phoneprofilesplus;

import static org.acra.util.IOUtils.writeStringToFile;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
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

/*
ACRA custom report sender:

This working, method "create" is called:

@AutoService(ReportSenderFactory.class)
public static class CustomACRAEmailSenderFactory implements ReportSenderFactory {
    @NotNull
    @Override
    public ReportSender create(@NotNull Context context, @NotNull CoreConfiguration coreConfiguration) {
        Log.e("CustomACRAEmailSenderFactory.create", "#### ***** ####");
        return new CustomACRAEmailSender(coreConfiguration);
    }
}


But this not working, in logcat is not log called at start of method "send":

@Override
public void send(@NotNull Context context, @NotNull CrashReportData errorContent)
        throws ReportSenderException {
    Log.e("CustomACRAEmailSender.send", "Report Sent!");

    ...
}

*/


@SuppressWarnings("unused")
public class CustomACRAEmailSender implements ReportSender {

    final CoreConfiguration coreConfiguration;

    CustomACRAEmailSender(@NotNull CoreConfiguration coreConfiguration) {
//        Log.e("CustomACRAEmailSender constructor", "***** ###### *****");
        this.coreConfiguration = coreConfiguration;
    }

    @Override
    public void send(@NotNull Context context, @NotNull CrashReportData errorContent)
            throws ReportSenderException {
//        Log.e("CustomACRAEmailSender.send", "Report Sent!");

        List<Configuration> plugins = coreConfiguration.getPluginConfigurations();
//        Log.e("CustomACRAEmailSender.send", "plugins.size=" + plugins.size());
        for (Configuration plugin : plugins) {
            if (plugin instanceof MailSenderConfiguration) {
//                Log.e("CustomACRAEmailSender.send", "MailSenderConfiguration");

                MailSenderConfiguration mailConfig = (MailSenderConfiguration) plugin;

                String emailAddress =  mailConfig.getMailTo();
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", emailAddress, null));

                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "(new sender) " + mailConfig.getSubject());
                emailIntent.putExtra(Intent.EXTRA_TEXT, mailConfig.getBody());
                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                ArrayList<Uri> attachments = new ArrayList<>();

                String reportText;
                try {
                    reportText = coreConfiguration.getReportFormat().toFormattedString(errorContent, coreConfiguration.getReportContent(), "\n", "\n\t", false);
                } catch (Exception e) {
                    throw new ReportSenderException("Failed to convert Report to text", e);
                }

                Uri report = createAttachmentFromString(context, mailConfig.getReportFileName(), reportText);
                if (report != null) {
                    attachments.add(report);
                }

                List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentActivities(emailIntent, 0);
                List<LabeledIntent> intents = new ArrayList<>();
                for (ResolveInfo info : resolveInfo) {
                    Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                    if (!emailAddress.isEmpty())
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "(new sender) " + mailConfig.getSubject());
                    intent.putExtra(Intent.EXTRA_TEXT, mailConfig.getBody());
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments); //ArrayList<Uri> of attachment Uri's
                    intents.add(new LabeledIntent(intent, info.activityInfo.packageName, info.loadLabel(context.getPackageManager()), info.icon));
                }
                //Log.e("EditorActivity.ExportAsyncTask.onPostExecute", "intents.size()="+intents.size());
                if (intents.size() > 0) {
                    try {
                        Intent chooser = Intent.createChooser(intents.get(0), context.getString(R.string.email_chooser));
                        //noinspection ToArrayCallWithZeroLengthArrayArgument
                        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new LabeledIntent[intents.size()]));
                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(chooser);
                    } catch (Exception e) {
                        Log.e("CustomACRAEmailSender.send", Log.getStackTraceString(e));
                    }
                }

                break;
            }
        }
    }

    protected Uri createAttachmentFromString(Context context, String name, String content) {
        File cache = new File(context.getCacheDir(), name);
        try {
            writeStringToFile(cache, content);
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
