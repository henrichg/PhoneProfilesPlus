package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceDialogFragmentCompat;

public class InfoDialogPreferenceFragment extends PreferenceDialogFragmentCompat
        implements PPLinkMovementMethod.OnPPLinkMovementMethodListener {
    private InfoDialogPreference preference;
    private Context context;

    @Override
    protected void onPrepareDialogBuilder(@NonNull AlertDialog.Builder builder) {
        GlobalGUIRoutines.setCustomDialogTitle(preference.getContext(), builder, false,
                preference.getDialogTitle(), null);
    }

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context) {
        preference = (InfoDialogPreference) getPreference();
        preference.fragment = this;
        this.context = context;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_info_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        final TextView infoTextView = view.findViewById(R.id.info_pref_dialog_info_text);

        if (preference.isHtml) {
            //noinspection DataFlowIssue
            infoTextView.setText(StringFormatUtils.fromHtml(preference.infoText, true,  false, 0, 0, true));
            infoTextView.setClickable(true);
            infoTextView.setMovementMethod(new PPLinkMovementMethod(this, context));
        } else
            //noinspection DataFlowIssue
            infoTextView.setText(preference.infoText);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        preference.fragment = null;
    }

    @Override
    public void onLinkClicked(final String linkUrl, PPLinkMovementMethod.LinkType linkTypeUrl,
                              final String linkText, PPLinkMovementMethod.LinkType linkTypeText) {
        onLinkClickedListener(linkUrl, linkTypeUrl, linkText, linkTypeText,
                preference.getTitle(), context, getActivity());
    }

    /** @noinspection unused*/
    static void onLinkClickedListener(final String linkUrl, PPLinkMovementMethod.LinkType linkTypeUrl,
                                      final String linkText, PPLinkMovementMethod.LinkType linkTypeText,
                                      CharSequence dialodTitle, Context context, Activity activity) {
        boolean showImportantInfoProfiles = linkUrl.startsWith(InfoDialogPreference.ACTIVITY_IMPORTANT_INFO_PROFILES);
        boolean showPPPAppInfoScreen = linkUrl.startsWith(InfoDialogPreference.PPP_APP_INFO_SCREEN);
        boolean showDroidifyInstallationSite = linkUrl.startsWith(InfoDialogPreference.DROIDIFY_INSTALLATION_SITE);
        boolean grantRoot = linkUrl.equals(InfoDialogPreference.GRANT_ROOT);
        boolean showOpenVPNConectInstallationSite = linkUrl.startsWith(InfoDialogPreference.VPN_OPENVPN_CONNECT);
        boolean showOpenVPNForAndroidInstallationSite = linkUrl.startsWith(InfoDialogPreference.VPN_OPENVPN_FOR_ANDROID);
        boolean showWireguardInstallationSite = linkUrl.startsWith(InfoDialogPreference.VPN_WIREGUARD);
        boolean showDeltaInstallationSite = linkUrl.startsWith(InfoDialogPreference.DELTA_APP);

        int iiFragment;// = -1;
        // 0 = System
        // 1 = Profiles
        // 2 = Events

        boolean iiQuickGuide = false;

        if (showImportantInfoProfiles) {
            iiFragment = 1;

            int scrollToStart = linkUrl.indexOf("__");
            int scrollTo = Integer.parseInt(linkUrl.substring(scrollToStart + 2));

            Intent intentLaunch = new Intent(context, ImportantInfoActivityForceScroll.class);
            intentLaunch.putExtra(ImportantInfoActivity.EXTRA_SHOW_QUICK_GUIDE, iiQuickGuide);
            intentLaunch.putExtra(ImportantInfoActivityForceScroll.EXTRA_SHOW_FRAGMENT, iiFragment);
            intentLaunch.putExtra(ImportantInfoActivityForceScroll.EXTRA_SCROLL_TO, scrollTo);
            context.startActivity(intentLaunch);
        }
        if (showPPPAppInfoScreen) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            //intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse(PPApplication.INTENT_DATA_PACKAGE +PPApplication.PACKAGE_NAME));
            if (GlobalGUIRoutines.activityIntentExists(intent, context)) {
                //noinspection deprecation
                context.startActivity(intent);
            } else {
                PPAlertDialog dialog2 = new PPAlertDialog(
                        dialodTitle,
                        context.getString(R.string.setting_screen_not_found_alert),
                        context.getString(android.R.string.ok),
                        null,
                        null, null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true, true,
                        false, false,
                        true,
                        false,
                        (AppCompatActivity) activity
                );

                dialog2.showDialog();
            }
        }
        if (showDroidifyInstallationSite) {
            String url = PPApplication.DROIDIFY_APPLICATION_URL;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                context.startActivity(Intent.createChooser(i, context.getString(R.string.web_browser_chooser)));
            } catch (Exception ignored) {}
        }
        if (grantRoot) {
            // force check root
            boolean rooted;
//            PPApplicationStatic.logE("[SYNCHRONIZED] InfoDialogPreferenceFragment.onLinkClicked", "PPApplication.rootMutex");
            synchronized (PPApplication.rootMutex) {
                PPApplication.rootMutex.rootChecked = false;
                rooted = RootUtils._isRooted();
            }
            if (rooted) {
                Permissions.grantRootX(null, activity);
            }
        }
        if (showOpenVPNConectInstallationSite) {
            String url = PPApplication.OPENVPN_CONNECT_APPLICATION_URL;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                context.startActivity(Intent.createChooser(i, context.getString(R.string.web_browser_chooser)));
            } catch (Exception ignored) {}
        }
        if (showOpenVPNForAndroidInstallationSite) {
            String url = PPApplication.OPENVPN_FOR_ANDROID_APPLICATION_URL;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                context.startActivity(Intent.createChooser(i, context.getString(R.string.web_browser_chooser)));
            } catch (Exception ignored) {}
        }
        if (showWireguardInstallationSite) {
            String url = PPApplication.WIREGUARD_APPLICATION_URL;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                context.startActivity(Intent.createChooser(i, context.getString(R.string.web_browser_chooser)));
            } catch (Exception ignored) {}
        }
        if (showDeltaInstallationSite) {
            PackageManager packageManager = activity.getPackageManager();
            Intent _intent = packageManager.getLaunchIntentForPackage(PPApplication.FDROID_PACKAGE_NAME);
            boolean fdroidInstalled = (_intent != null);
            _intent = packageManager.getLaunchIntentForPackage(PPApplication.DROIDIFY_PACKAGE_NAME);
            boolean droidifyInstalled = (_intent != null);
            _intent = packageManager.getLaunchIntentForPackage(PPApplication.NEOSTORE_PACKAGE_NAME);
            boolean neostoreInstalled = (_intent != null);

            if (fdroidInstalled || droidifyInstalled || neostoreInstalled) {
                if (droidifyInstalled) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=dev.shadoe.delta"));
                    intent.setPackage(PPApplication.DROIDIFY_PACKAGE_NAME);
                    try {
                        activity.startActivity(intent);
                    } catch (Exception ignored) {}
                } else if (neostoreInstalled) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=dev.shadoe.delta"));
                    intent.setPackage(PPApplication.NEOSTORE_PACKAGE_NAME);
                    try {
                        activity.startActivity(intent);
                    } catch (Exception ignored) {}
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=dev.shadoe.delta"));
                    intent.setPackage(PPApplication.FDROID_PACKAGE_NAME);
                    try {
                        activity.startActivity(intent);
                    } catch (Exception ignored) {}
                }
            } else {
                String url = PPApplication.DELTA_APPLICATION_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    context.startActivity(Intent.createChooser(i, context.getString(R.string.web_browser_chooser)));
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public void onLongClick(String text) {

    }

}
