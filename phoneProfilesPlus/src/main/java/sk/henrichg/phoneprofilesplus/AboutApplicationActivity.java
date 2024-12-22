package sk.henrichg.phoneprofilesplus;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.appcompat.widget.Toolbar;

public class AboutApplicationActivity extends AppCompatActivity {

    //static final int EMAIL_BODY_SUPPORT = 1;
    //static final int EMAIL_BODY_TRANSLATIONS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EditorActivity.itemDragPerformed = false;

        GlobalGUIRoutines.setTheme(this, false, true, false, false, false, false); // must by called before super.onCreate()
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about_application);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        Toolbar toolbar = findViewById(R.id.about_application_application_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.about_application_title);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        /*
        ImageView appIcon = findViewById(R.id.about_application_application_icon);
        try {
            Drawable drawable = getPackageManager().getApplicationIcon(PPApplication.PACKAGE_NAME);
            if (drawable instanceof AdaptiveIconDrawable) {
                drawable = ((AdaptiveIconDrawable) drawable).getForeground();
            }
            appIcon.setImageDrawable(drawable);
        } catch (Exception e) {
            Log.e("AboutApplicationActivity.onCreate", Log.getStackTraceString(e));
        }
        */

        TextView text = findViewById(R.id.about_application_application_version);
        String message;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            message = getString(R.string.about_application_version) + " " + pInfo.versionName + " (" + PPApplicationStatic.getVersionCode(pInfo) + ")";
            if (DebugVersion.enabled)
                message = message + " - debug";
        } catch (Exception e) {
            message = "";
        }
        //message = message + getString(R.string.about_application_package_type_github);
        //noinspection DataFlowIssue
        text.setText(message);

        text = findViewById(R.id.about_application_author);
        CharSequence str1 = getString(R.string.about_application_author);
        CharSequence str2 = str1 + " Henrich Gron";
        Spannable sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //noinspection DataFlowIssue
        text.setText(sbt);

        /*
        emailMe(findViewById(R.id.about_application_support),
                getString(R.string.about_application_support),
                getString(R.string.about_application_support2),
                getString(R.string.about_application_support_subject),
                getEmailBodyText(this),
                this);
        */

        text = findViewById(R.id.about_application_translations);
        str1 = getString(R.string.about_application_translations);
        str2 = str1 + StringConstants.CHAR_NEW_LINE + PPApplication.CROWDIN_URL + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
        sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                //ds.bgColor = Color.GRAY;
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = PPApplication.CROWDIN_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        //noinspection DataFlowIssue
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
        str2 = str1 + StringConstants.CHAR_NEW_LINE + PPApplication.PRIVACY_POLICY_URL + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
        sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        /*ClickableSpan*/ clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = PPApplication.PRIVACY_POLICY_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        //noinspection DataFlowIssue
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        text = findViewById(R.id.about_application_releases);
        str1 = getString(R.string.about_application_releases);
        str2 = str1 + StringConstants.CHAR_NEW_LINE + PPApplication.GITHUB_PPP_RELEASES_URL + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
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
                String url = PPApplication.GITHUB_PPP_RELEASES_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        //noinspection DataFlowIssue
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        text = findViewById(R.id.about_application_source_code);
        str1 = getString(R.string.about_application_source_code);
        str2 = str1 + StringConstants.CHAR_NEW_LINE + PPApplication.GITHUB_PPP_URL + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
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
                String url = PPApplication.GITHUB_PPP_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        //noinspection DataFlowIssue
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        text = findViewById(R.id.about_application_extender_source_code);
        str1 = getString(R.string.about_application_extender_source_code);
        str2 = str1 + StringConstants.CHAR_NEW_LINE + PPApplication.GITHUB_PPPE_URL + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
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
                String url = PPApplication.GITHUB_PPPE_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        //noinspection DataFlowIssue
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        text = findViewById(R.id.about_application_pppps_source_code);
        str1 = getString(R.string.about_application_pppps_source_code);
        str2 = str1 + StringConstants.CHAR_NEW_LINE + PPApplication.GITHUB_PPPPS_URL + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
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
                String url = PPApplication.GITHUB_PPPPS_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        //noinspection DataFlowIssue
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        /*
        text = findViewById(R.id.about_application_xda_developers_community);
        str1 = getString(R.string.about_application_xda_developers_community);
        str2 = str1 + StringConstants.CHAR_NEW_LINE + PPApplication.XDA_DEVELOPERS_PPP_URL + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
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
                String url = PPApplication.XDA_DEVELOPERS_PPP_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        */

        /*
        text = findViewById(R.id.about_application_google_plus_community);
        str1 = getString(R.string.about_application_google_plus_community);
        str2 = str1 + "\nhttps://plus.google.com/communities/100282006628784777672" + " »»";
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

        //text = findViewById(R.id.about_application_rate_application);
        //if (PPApplication.gitHubRelease) {
        //    text.setVisibility(View.GONE);
        //}
        //else {
/*            str1 = getString(R.string.about_application_rate_in_googlePlay) + ".";
            sbt = new SpannableString(str1);
            clickableSpan = new ClickableSpan() {
                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(ds.linkColor);    // you can use custom color
                    ds.setUnderlineText(false);    // this remove the underline
                }

                @Override
                public void onClick(@NonNull View textView) {
                    Uri uri = Uri.parse("market://details?id=" + PPApplication.PACKAGE_NAME);
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    // To count with Play market back stack, After pressing back button,
                    // to taken back to our application, we need to add following flags to intent.
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    try {
                        startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        try {
                            Intent i = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/apps/details?id=" + PPApplication.PACKAGE_NAME));
                            startActivity(Intent.createChooser(i, getString(R.string.google_play_chooser)));
                        } catch (Exception ee) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                }
            };
            sbt.setSpan(clickableSpan, 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //sbt.setSpan(new UnderlineSpan(), 0, str1.length(), 0);
            text.setText(sbt);
            text.setMovementMethod(LinkMovementMethod.getInstance());
*/
        //}
        /*else {
            final TextView reviewText = findViewById(R.id.about_application_rate_application);
            text.setVisibility(View.GONE);

            final ReviewManager manager = ReviewManagerFactory.create(this);
            //final FakeReviewManager manager = new FakeReviewManager(this);
            final Activity activity = this;
            manager.requestReviewFlow().addOnCompleteListener(new OnCompleteListener<ReviewInfo>() {
                ReviewInfo reviewInfo;

                @Override
                public void onComplete(@NonNull Task<ReviewInfo> task) {
                    if (task.isSuccessful()) {
                        reviewInfo = task.getResult();
                        CharSequence str1 = getString(R.string.about_application_rate_in_googlePlay) + ".";
                        SpannableString sbt = new SpannableString(str1);
                        ClickableSpan clickableSpan = new ClickableSpan() {
                            @Override
                            public void updateDrawState(@NonNull TextPaint ds) {
                                ds.setColor(ds.linkColor);    // you can use custom color
                                ds.setUnderlineText(false);    // this remove the underline
                            }

                            @Override
                            public void onClick(@NonNull View textView) {
                                Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
                                flow.addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                    }
                                });
                                flow.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {
                                    }
                                });
                            }
                        };
                        sbt.setSpan(clickableSpan, 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        //sbt.setSpan(new UnderlineSpan(), 0, str1.length(), 0);
                        reviewText.setText(sbt);
                        reviewText.setMovementMethod(LinkMovementMethod.getInstance());
                        reviewText.setVisibility(View.VISIBLE);
                    } else {
                        reviewText.setVisibility(View.GONE);
                    }
                }
            });
            manager.requestReviewFlow().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    reviewText.setVisibility(View.GONE);
                }
            });
        }
        */

        Button donateButton = findViewById(R.id.about_application_donate_button);
        //noinspection DataFlowIssue
        donateButton.setOnClickListener(view -> {
            Intent intent;
            intent = new Intent(getBaseContext(), DonationPayPalActivity.class);
            startActivity(intent);
        });

        /*
        Button closeButton = findViewById(R.id.about_application_close);
        //noinspection DataFlowIssue
        closeButton.setOnClickListener(view -> finish());
        */

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
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

}
