package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Vibrator;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.URLSpan;

import androidx.core.text.HtmlCompat;

import org.xml.sax.XMLReader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

class StringFormatUtils {

    static String formatDateTime(Context context, String timeToFormat) {

        String finalDateTime = "";

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date;
        if (timeToFormat != null) {
            try {
                date = iso8601Format.parse(timeToFormat);
            } catch (ParseException e) {
                date = null;
            }

            if (date != null) {
                long when = date.getTime();
                when += TimeZone.getDefault().getOffset(when);

                /*
                int flags = 0;
                flags |= DateUtils.FORMAT_SHOW_TIME;
                flags |= DateUtils.FORMAT_SHOW_DATE;
                flags |= DateUtils.FORMAT_NUMERIC_DATE;
                flags |= DateUtils.FORMAT_SHOW_YEAR;

                finalDateTime = android.text.format.DateUtils.formatDateTime(context,
                        when, flags);

                finalDateTime = DateFormat.getDateFormat(context).format(when) +
                        " " + DateFormat.getTimeFormat(context).format(when);
                */

                /*
                SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yyyy HH:mm:ss");
                finalDateTime = sdf.format(when);
                */

                finalDateTime = timeDateStringFromTimestamp(context, when);
            }
        }
        return finalDateTime;
    }

    @SuppressLint("SimpleDateFormat")
    static String timeDateStringFromTimestamp(Context applicationContext, long timestamp){
        String timeDate;
        String androidDateTime=android.text.format.DateFormat.getDateFormat(applicationContext).format(new Date(timestamp))+" "+
                android.text.format.DateFormat.getTimeFormat(applicationContext).format(new Date(timestamp));
        String javaDateTime = DateFormat.getDateTimeInstance().format(new Date(timestamp));
        String AmPm="";
        if(!Character.isDigit(androidDateTime.charAt(androidDateTime.length()-1))) {
            if(androidDateTime.contains(new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.AM])){
                AmPm=" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.AM];
            }else{
                AmPm=" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.PM];
            }
            androidDateTime=androidDateTime.replace(AmPm, "");
        }
        if(!Character.isDigit(javaDateTime.charAt(javaDateTime.length()-1))){
            javaDateTime=javaDateTime.replace(" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.AM], "");
            javaDateTime=javaDateTime.replace(" "+new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.PM], "");
        }
        javaDateTime=javaDateTime.substring(javaDateTime.length()-3);
        timeDate=androidDateTime.concat(javaDateTime);
        return timeDate.concat(AmPm);
    }

    static Spanned fromHtml(String source, boolean forBullets, boolean forNumbers, int numberFrom, int sp, boolean trimTrailingWhiteSpaces) {
        Spanned htmlSpanned;

        if (forNumbers)
            htmlSpanned = HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_COMPACT, null, new LiTagHandler());
        else {
            htmlSpanned = HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_COMPACT);
            //htmlSpanned = HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_COMPACT, null, new GlobalGUIRoutines.LiTagHandler());
        }

        htmlSpanned = removeUnderline(htmlSpanned);

        SpannableStringBuilder result;

        if (forBullets)
            result = addBullets(htmlSpanned);
        else if (forNumbers)
            result = addNumbers(htmlSpanned, numberFrom, sp);
        else
            result = new SpannableStringBuilder(htmlSpanned);

        if (trimTrailingWhiteSpaces)
            result = trimTrailingWhitespace(result);

        return result;
    }

    private static class URLSpanline_none extends URLSpan {
        public URLSpanline_none(String url) {
            super(url);
        }
        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }

    private static SpannableStringBuilder removeUnderline(Spanned htmlSpanned) {
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(htmlSpanned);
        URLSpan[] spans = spannableBuilder.getSpans(0, spannableBuilder.length(), URLSpan.class);
        for (URLSpan span: spans) {
            int start = spannableBuilder.getSpanStart(span);
            int end = spannableBuilder.getSpanEnd(span);
            spannableBuilder.removeSpan(span);
            span = new URLSpanline_none(span.getURL());
            spannableBuilder.setSpan(span, start, end, 0);
        }
        return spannableBuilder;
    }

    private static SpannableStringBuilder addBullets(Spanned htmlSpanned) {
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(htmlSpanned);
        BulletSpan[] spans = spannableBuilder.getSpans(0, spannableBuilder.length(), BulletSpan.class);
        if (spans != null) {
            for (BulletSpan span : spans) {
                int start = spannableBuilder.getSpanStart(span);
                int end = spannableBuilder.getSpanEnd(span);
                spannableBuilder.removeSpan(span);
                int radius = GlobalGUIRoutines.dip(2) + 1;
                spannableBuilder.setSpan(new ImprovedBulletSpan(radius, GlobalGUIRoutines.dip(8)/*, 0*/), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableBuilder;
    }

    private static SpannableStringBuilder addNumbers(Spanned htmlSpanned, int numberFrom, int sp) {
        int listItemCount = numberFrom-1;
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(htmlSpanned);
        BulletSpan[] spans = spannableBuilder.getSpans(0, spannableBuilder.length(), BulletSpan.class);
        if (spans != null) {
            for (BulletSpan span : spans) {
                int start = spannableBuilder.getSpanStart(span);
                int end = spannableBuilder.getSpanEnd(span);
                spannableBuilder.removeSpan(span);
                ++listItemCount;
                spannableBuilder.insert(start, listItemCount + ". ");
                spannableBuilder.setSpan(new LeadingMarginSpan.Standard(0, GlobalGUIRoutines.sip(sp)), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableBuilder;
    }

    static SpannableStringBuilder trimTrailingWhitespace(SpannableStringBuilder source) {

        if (source == null)
            return null;

        int i = source.length();

        // loop back to the first non-whitespace character
        //noinspection StatementWithEmptyBody
        while (--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return (SpannableStringBuilder) source.subSequence(0, i + 1);
    }
    @SuppressLint("DefaultLocale")
    static String getDurationString(int duration) {
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @SuppressLint("DefaultLocale")
    static String getTimeString(int time) {
        int hours = time / 60;
        int minutes = (time % 60);
        return String.format("%02d:%02d", hours, minutes);
    }

    @SuppressLint("DefaultLocale")
    static String getEndsAsString(int duration) {
        if(duration == 0) {
            return "--";
        }

        Calendar ends = Calendar.getInstance();
        ends.add(Calendar.SECOND, duration);
        int hours = ends.get(Calendar.HOUR_OF_DAY);
        int minutes = ends.get(Calendar.MINUTE);
        int seconds = ends.get(Calendar.SECOND);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    static String getListPreferenceString(String value, int arrayValuesRes,
                                          int arrayStringsRes, Context context) {
        String[] arrayValues = context.getResources().getStringArray(arrayValuesRes);
        String[] arrayStrings = context.getResources().getStringArray(arrayStringsRes);
        int index = 0;
        for (String arrayValue : arrayValues) {
            if (arrayValue.equals(value))
                break;
            ++index;
        }
        try {
            String str = arrayStrings[index].replace("<", "&#60;");
            str = str.replace(">", "&#62;");
            return str;
        } catch (Exception e) {
            return context.getString(R.string.array_pref_no_change);
        }
    }

    static String getZenModePreferenceString(String value, Context context) {
        String[] arrayValues;
        String[] arrayStrings;

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if ((vibrator != null) && vibrator.hasVibrator()) {
            arrayValues = context.getResources().getStringArray(R.array.zenModeValues);
            arrayStrings = context.getResources().getStringArray(R.array.zenModeArray);
        }
        else {
            arrayValues = context.getResources().getStringArray(R.array.zenModeNotVibratorValues);
            arrayStrings = context.getResources().getStringArray(R.array.zenModeNotVibratorArray);
        }

        String[] arraySummaryStrings = context.getResources().getStringArray(R.array.zenModeSummaryArray);
        int index = 0;
        for (String arrayValue : arrayValues) {
            if (arrayValue.equals(value))
                break;
            ++index;
        }
        try {
            return arrayStrings[index] + " - " + arraySummaryStrings[Integer.parseInt(value) - 1];
        } catch (Exception e) {
            return context.getString(R.string.array_pref_no_change);
        }
    }

    private static class LiTagHandler implements Html.TagHandler {

        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {

            class Bullet {
            }

            if (tag.equals("li") && opening) {
                output.setSpan(new Bullet(), output.length(), output.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            if (tag.equals("li") && !opening) {
                //output.append("\n\n");
                output.append(StringConstants.CHAR_NEW_LINE);
                Bullet[] spans = output.getSpans(0, output.length(), Bullet.class);
                if (spans != null) {
                    Bullet lastMark = spans[spans.length - 1];
                    int start = output.getSpanStart(lastMark);
                    output.removeSpan(lastMark);
                    if (start != output.length()) {
                        output.setSpan(new BulletSpan(), start, output.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }

    }

}
