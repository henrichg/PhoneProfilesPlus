package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

public class InfoDialogPreferenceFragment extends PreferenceDialogFragmentCompat {

    private InfoDialogPreference preference;
    private Context context;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
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

        String _infoText = preference.infoText;
        String[] tagType = new String[2];
        String[] importantInfoTagDataString = new String[2];
        int[] importantInfoTagBeginIndex = new int[2];
        int[] importantInfoTagEndIndex = new int[2];

        for (int tagIndex = 0; tagIndex < 2; tagIndex++) {
//            Log.e("InfoDialogPreferenceFragment", "(1) _infoText="+_infoText);

            String beginTag = "<II" + tagIndex + " [";
//            Log.e("InfoDialogPreferenceFragment", "(1) beginTag="+beginTag);

            int _importantInfoTagBeginIndex = _infoText.indexOf(beginTag);
            int _importantInfoTagEndIndex = _infoText.indexOf("]>");
//            Log.e("InfoDialogPreferenceFragment", "(1) importantInfoTagBeginIndex="+importantInfoTagBeginIndex);
//            Log.e("InfoDialogPreferenceFragment", "(1) importantInfoTagEndIndex="+importantInfoTagEndIndex);

            if ((_importantInfoTagBeginIndex != -1) && (_importantInfoTagEndIndex != -1)) {
                String _importantInfoTagDataString = _infoText.substring(_importantInfoTagBeginIndex + beginTag.length(), _importantInfoTagEndIndex);
//                Log.e("InfoDialogPreferenceFragment", "importantInfoTagDataString="+importantInfoTagDataString);

                beginTag = "<II" + tagIndex + " [" + _importantInfoTagDataString + "]>";
                String endTag = "<II" + tagIndex + "/>";
//                Log.e("InfoDialogPreferenceFragment", "(2) beginTag="+beginTag);
//                Log.e("InfoDialogPreferenceFragment", "(2) endTag="+endTag);

                _importantInfoTagBeginIndex = _infoText.indexOf(beginTag);
                _importantInfoTagEndIndex = _infoText.indexOf(endTag);
//                Log.e("InfoDialogPreferenceFragment", "(2) importantInfoTagBeginIndex="+importantInfoTagBeginIndex);
//                Log.e("InfoDialogPreferenceFragment", "(2) importantInfoTagEndIndex="+importantInfoTagEndIndex);

                if ((_importantInfoTagBeginIndex != -1) && (_importantInfoTagEndIndex != -1)) {
                    _infoText = _infoText.replace(beginTag, "");
                    _infoText = _infoText.replace(endTag, "");

//                    Log.e("InfoDialogPreferenceFragment", "(2) _infoText="+_infoText);

                    tagType[tagIndex] = beginTag.substring(1, 3);
                    importantInfoTagDataString[tagIndex] = _importantInfoTagDataString;
                    importantInfoTagBeginIndex[tagIndex] = _importantInfoTagBeginIndex;
                    importantInfoTagEndIndex[tagIndex] = _importantInfoTagEndIndex -  beginTag.length();
                }
            } else {
                if (preference.isHtml) {
                    infoTextView.setText(StringFormatUtils.fromHtml(preference.infoText, true, false, 0, 0));
                    infoTextView.setClickable(true);
                    infoTextView.setMovementMethod(LinkMovementMethod.getInstance());
                } else
                    infoTextView.setText(preference.infoText);

                return;
            }
        }

        Spannable sbt = new SpannableString(_infoText);
        for (int tagIndex = 0; tagIndex < 2; tagIndex++) {
            if (tagType[tagIndex] != null) {
                ClickableSpan clickableSpan = new InfoDialogClickableSpan(tagType[tagIndex], importantInfoTagDataString[tagIndex]);
                sbt.setSpan(clickableSpan,
                        importantInfoTagBeginIndex[tagIndex], importantInfoTagEndIndex[tagIndex],
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        infoTextView.setText(sbt);
        infoTextView.setClickable(true);
        infoTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        preference.fragment = null;
    }

    private class InfoDialogClickableSpan extends ClickableSpan {
        String tagType;
        String importantInfoTagDataString;

        InfoDialogClickableSpan(String tagType, String importantInfoTagDataString) {
            super();
            this.tagType = tagType;
            this.importantInfoTagDataString = importantInfoTagDataString;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(ds.linkColor);    // you can use custom color
            ds.setUnderlineText(false);    // this remove the underline
        }

        @Override
        public void onClick(View widget) {

            String[] splits = importantInfoTagDataString.split(",");
            int page = Integer.parseInt(splits[0]);

            int fragment = Integer.parseInt(splits[1]);
            // 0 = System
            // 1 = Profiles
            // 2 = Events

            int resource = Integer.parseInt(splits[2]);

            if (tagType.equals("II")) {
                Intent intentLaunch = new Intent(context, ImportantInfoActivityForceScroll.class);
                intentLaunch.putExtra(ImportantInfoActivity.EXTRA_SHOW_QUICK_GUIDE, page == 1);
                intentLaunch.putExtra(ImportantInfoActivityForceScroll.EXTRA_SHOW_FRAGMENT, fragment);
                intentLaunch.putExtra(ImportantInfoActivityForceScroll.EXTRA_SCROLL_TO, resource);
                startActivity(intentLaunch);
            }

            if (getDialog() != null)
                getDialog().cancel();
        }
    }

}
