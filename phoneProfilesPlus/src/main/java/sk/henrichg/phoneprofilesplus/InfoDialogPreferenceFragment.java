package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

public class InfoDialogPreferenceFragment extends PreferenceDialogFragmentCompat
        implements PPLinkMovementMethod.OnPPLinkMovementMethodListener {
    private InfoDialogPreference preference;
    private Context context;

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
            infoTextView.setText(StringFormatUtils.fromHtml(preference.infoText, true,  false, 0, 0, true));
            infoTextView.setClickable(true);
            infoTextView.setMovementMethod(new PPLinkMovementMethod(this, context));
        } else
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
                        true, true,
                        false, false,
                        true,
                        false,
                        activity
                );

                dialog2.show();
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
    }

    @Override
    public void onLongClick(String text) {

    }

}
