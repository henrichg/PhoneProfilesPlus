package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class AboutApplicationActivity extends AppCompatActivity {

    static final int EMAIL_BODY_SUPPORT = 1;
    static final int EMAIL_BODY_TRANSLATIONS = 2;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, false/*, false*/); // must by called before super.onCreate()
        GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about_application);

        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            switch (ApplicationPreferences.applicationTheme(getApplicationContext(), true)) {
                case "color":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary));
                    break;
                case "white":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primaryDark19_white));
                    break;
                default:
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary_dark));
                    break;
            }
        }
        */

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.about_application_title);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        /*
        ImageView appIcon = findViewById(R.id.about_application_application_icon);
        try {
            Drawable drawable = getPackageManager().getApplicationIcon(getPackageName());
            Log.e("AboutApplicationActivity.onCreate", "drawable.class="+drawable.getClass().getName());
            if (Build.VERSION.SDK_INT >= 26) {
                if (drawable instanceof AdaptiveIconDrawable) {
                    Log.e("AboutApplicationActivity.onCreate", "AdaptiveIconDrawable");
                    drawable = ((AdaptiveIconDrawable) drawable).getForeground();
                }
            }
            appIcon.setImageDrawable(drawable);
        } catch (Exception e) {
            Log.e("AboutApplicationActivity.onCreate", Log.getStackTraceString(e));
        }
        */

        TextView text = findViewById(R.id.about_application_application_version);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            text.setText(getString(R.string.about_application_version) + " " + pInfo.versionName + " (" + PPApplication.getVersionCode(pInfo) + ")");
        } catch (Exception e) {
            text.setText("");
        }

        text = findViewById(R.id.about_application_author);
        CharSequence str1 = getString(R.string.about_application_author);
        CharSequence str2 = str1 + " Henrich Gron";
        Spannable sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setText(sbt);

        emailMe((TextView) findViewById(R.id.about_application_support),
                getString(R.string.about_application_support),
                getString(R.string.about_application_support2),
                getString(R.string.about_application_support_subject),
                getEmailBodyText(EMAIL_BODY_SUPPORT, this),
                false,this);

        text = findViewById(R.id.about_application_translations);
        str1 = getString(R.string.about_application_translations);
        str2 = str1 + " https://crowdin.com/project/phoneprofilesplus";
        sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = "https://crowdin.com/project/phoneprofilesplus";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception ignored) {}
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        /*emailMe((TextView) findViewById(R.id.about_application_translations),
                getString(R.string.about_application_translations),
                getString(R.string.about_application_translations2),
                getString(R.string.about_application_translations_subject),
                getEmailBodyText(EMAIL_BODY_TRANSLATIONS, this),
                false,this);*/

        text = findViewById(R.id.about_application_privacy_policy);
        str1 = getString(R.string.about_application_privacy_policy);
        str2 = str1 + " https://sites.google.com/site/phoneprofilesplus/home/privacy-policy";
        sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        /*ClickableSpan*/ clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = "https://sites.google.com/site/phoneprofilesplus/home/privacy-policy";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception ignored) {}
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        text = findViewById(R.id.about_application_releases);
        str1 = getString(R.string.about_application_releases);
        str2 = str1 + " https://github.com/henrichg/PhoneProfilesPlus/releases";
        sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = "https://github.com/henrichg/PhoneProfilesPlus/releases";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception ignored) {}
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        text = findViewById(R.id.about_application_source_code);
        str1 = getString(R.string.about_application_source_code);
        str2 = str1 + " https://github.com/henrichg/PhoneProfilesPlus";
        sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = "https://github.com/henrichg/PhoneProfilesPlus";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception ignored) {}
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        text = findViewById(R.id.about_application_extender_source_code);
        str1 = getString(R.string.about_application_extender_source_code);
        str2 = str1 + " https://github.com/henrichg/PhoneProfilesPlusExtender";
        sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = "https://github.com/henrichg/PhoneProfilesPlusExtender";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception ignored) {}
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        text = findViewById(R.id.about_application_xda_developers_community);
        str1 = getString(R.string.about_application_xda_developers_community);
        str2 = str1 + " https://forum.xda-developers.com/android/apps-games/phone-profile-plus-t3799429";
        sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = "https://forum.xda-developers.com/android/apps-games/phone-profile-plus-t3799429";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception ignored) {}
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        /*
        text = findViewById(R.id.about_application_google_plus_community);
        str1 = getString(R.string.about_application_google_plus_community);
        str2 = str1 + " https://plus.google.com/communities/100282006628784777672";
        sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = "https://plus.google.com/communities/100282006628784777672";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception ignored) {}
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        */

        text = findViewById(R.id.about_application_rate_application);
        str1 = getString(R.string.about_application_rate_in_googlePlay) + ".";
        sbt = new SpannableString(str1);
        clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market back stack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                //if (android.os.Build.VERSION.SDK_INT >= 21)
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                /*else
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);*/
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    try {
                        Intent i = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName()));
                        startActivity(Intent.createChooser(i, getString(R.string.google_play_chooser)));
                    } catch (Exception ignored) {}
                }
            }
        };
        sbt.setSpan(clickableSpan, 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), 0, str1.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        Button donateButton = findViewById(R.id.about_application_donate_button);
        //donateButton.setAllCaps(false);
        donateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), DonationActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    static void emailMe(final TextView textView, final String text, final String linkText, final String subjectText,
                        final String bodyText, final boolean boldLink, final Context context) {
        String strNoLink = text + " " + linkText;
        String str2 = strNoLink + " henrich.gron@gmail.com";
        Spannable sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, strNoLink.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                String[] email = { "henrich.gron@gmail.com" };
                intent.putExtra(Intent.EXTRA_EMAIL, email);
                String packageVersion = "";
                try {
                    PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                    packageVersion = " - v" + pInfo.versionName + " (" + PPApplication.getVersionCode(pInfo) + ")";
                } catch (Exception ignored) {
                }
                if (subjectText.isEmpty())
                    intent.putExtra(Intent.EXTRA_SUBJECT, "PhoneProfilesPlus" + packageVersion);
                else
                    intent.putExtra(Intent.EXTRA_SUBJECT, "PhoneProfilesPlus" + packageVersion + " - " + subjectText);
                if (!bodyText.isEmpty())
                    intent.putExtra(Intent.EXTRA_TEXT, bodyText);
                try {
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.email_chooser)));
                } catch (Exception ignored) {}
            }
        };
        sbt.setSpan(clickableSpan, strNoLink.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (boldLink)
            sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), strNoLink.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //else
        //    sbt.setSpan(new UnderlineSpan(), strNoLink.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(sbt);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    static String getEmailBodyText(int bodyType, Context context) {
        String body = "";
        switch (bodyType) {
            case EMAIL_BODY_SUPPORT:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                    body = context.getString(R.string.important_info_email_body_device) + " " +
                            Settings.Global.getString(context.getContentResolver(), Settings.Global.DEVICE_NAME) +
                            " (" + Build.MODEL + ")" + " \n";
                else {
                    String manufacturer = Build.MANUFACTURER;
                    String model = Build.MODEL;
                    if (model.startsWith(manufacturer))
                        body = context.getString(R.string.important_info_email_body_device) + " " + model + " \n";
                    else
                        body = context.getString(R.string.important_info_email_body_device) + " " + manufacturer + " " + model + " \n";
                }
                body = body + context.getString(R.string.important_info_email_body_android_version) + " " + Build.VERSION.RELEASE + " \n\n";
                body = body + context.getString(R.string.important_info_email_body_problems) + " \n";
                body = body + context.getString(R.string.important_info_email_body_questions) + " \n";
                body = body + context.getString(R.string.important_info_email_body_suggestions) + " \n\n";
                break;
            case EMAIL_BODY_TRANSLATIONS:
                //body = context.getString(R.string.important_info_email_body_translation_language_from) + " \n";
                body = /*body +*/ context.getString(R.string.important_info_email_body_translation_language_to) + " \n\n";
                break;
        }
        return body;
    }

}
