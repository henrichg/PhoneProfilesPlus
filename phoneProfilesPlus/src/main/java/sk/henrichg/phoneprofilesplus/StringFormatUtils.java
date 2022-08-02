package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Vibrator;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.URLSpan;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class StringFormatUtils {

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

    static Spanned fromHtml(String source, boolean forBullets, boolean forNumbers, int numberFrom, int sp) {
        Spanned htmlSpanned;

        //if (Build.VERSION.SDK_INT >= 24) {
        if (forNumbers)
            htmlSpanned = Html.fromHtml(source, Html.FROM_HTML_MODE_COMPACT, null, new GlobalGUIRoutines.LiTagHandler());
        else {
            htmlSpanned = Html.fromHtml(source, Html.FROM_HTML_MODE_COMPACT);
            //htmlSpanned = Html.fromHtml(source, Html.FROM_HTML_MODE_COMPACT, null, new LiTagHandler());
        }
        //} else {
        //    if (forBullets || forNumbers)
        //        htmlSpanned = Html.fromHtml(source, null, new LiTagHandler());
        //    else
        //        htmlSpanned = Html.fromHtml(source);
        //}

        htmlSpanned = removeUnderline(htmlSpanned);

        if (forBullets)
            return addBullets(htmlSpanned);
        else
        if (forNumbers)
            return addNumbers(htmlSpanned, numberFrom, sp);
        else
            return  htmlSpanned;

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
                int end  = spannableBuilder.getSpanEnd(span);
                spannableBuilder.removeSpan(span);
                spannableBuilder.setSpan(new ImprovedBulletSpan(GlobalGUIRoutines.dip(2), GlobalGUIRoutines.dip(8), 0), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
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
                int end  = spannableBuilder.getSpanEnd(span);
                spannableBuilder.removeSpan(span);
                ++listItemCount;
                spannableBuilder.insert(start, listItemCount + ". ");
                spannableBuilder.setSpan(new LeadingMarginSpan.Standard(0, GlobalGUIRoutines.sip(sp)), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableBuilder;
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
            return arrayStrings[index];
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

}
