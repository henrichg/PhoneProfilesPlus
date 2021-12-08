package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class QuickGuideHelpFragment extends Fragment {

    //int scrollTo = 0;

    public QuickGuideHelpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.important_info_fragment_quick_guide, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Activity activity = getActivity();
        if (activity == null)
            return;

        TextView textView = view.findViewById(R.id.activity_info_quick_guide_sensors_texts);
        String text = "<ul>";
        text = text + "<li>" + getString(R.string.important_info_quick_guide_sensors_2) + "</li>";
        text = text + "<li>" + getString(R.string.important_info_quick_guide_sensors_3) + "</li>";
        text = text + "<li>" + getString(R.string.important_info_quick_guide_sensors_4) + "</li>";
        text = text + "<li>" + getString(R.string.important_info_quick_guide_sensors_5) + "</li>";
        text = text + "<li>" + getString(R.string.important_info_quick_guide_sensors_6) + "</li>";
        text = text + "<li>" + getString(R.string.important_info_quick_guide_sensors_7) + "</li>";
        text = text + "<li>" + getString(R.string.important_info_quick_guide_sensors_8) + "</li>";
        text = text + "<li>" + getString(R.string.important_info_quick_guide_sensors_9) + "</li>";
        text = text + "</ul>";
        textView.setText(GlobalGUIRoutines.fromHtml(text, true, false, 0, 0));

        textView = view.findViewById(R.id.activity_info_quick_guide_text_2);
        text = "<ol>";
        text = text + "<li>" + getString(R.string.important_info_quick_guide_2) + "</li>";
        text = text + "<li>" + getString(R.string.important_info_quick_guide_3) + "</li>";
        text = text + "</ol>";
        textView.setText(GlobalGUIRoutines.fromHtml(text, false, true, 1, 17));

        AboutApplicationActivity.emailMe(view.findViewById(R.id.activity_info_notification_contact),
                getString(R.string.important_info_contact),
                "", getString(R.string.about_application_support_subject),
                AboutApplicationActivity.getEmailBodyText(/*AboutApplicationActivity.EMAIL_BODY_SUPPORT, */activity),
                /*true,*/ activity);

        TextView translationTextView = view.findViewById(R.id.activity_info_translations);
        String str1 = getString(R.string.about_application_translations);
        String str2 = str1 + " " + PPApplication.CROWDIN_URL + " \u21D2";
        Spannable spannable = new SpannableString(str2);
        //spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
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
                    PPApplication.recordException(e);
                }
            }
        };
        spannable.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        translationTextView.setText(spannable);
        translationTextView.setMovementMethod(LinkMovementMethod.getInstance());
        /*AboutApplicationActivity.emailMe((TextView) view.findViewById(R.id.activity_info_translations),
                getString(R.string.important_info_translations),
                getString(R.string.about_application_translations2),
                getString(R.string.about_application_translations_subject),
                AboutApplicationActivity.getEmailBodyText(AboutApplicationActivity.EMAIL_BODY_TRANSLATIONS, activity),
                true, activity);*/

        /*if ((scrollTo != 0) && (savedInstanceState == null)) {
            final ScrollView scrollView = view.findViewById(R.id.fragment_important_info_scroll_view);
            final View viewToScroll = view.findViewById(scrollTo);
            if ((scrollView != null) && (viewToScroll != null)) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=QuickGuideHelpFragment.onViewCreated");
                    scrollView.scrollTo(0, viewToScroll.getTop());
                }, 200);
            }
        }*/
    }

}
