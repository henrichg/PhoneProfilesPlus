package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.core.os.ConfigurationCompat;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

public class LocaleHelper {

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";
    private static final String SELECTED_COUNTRY = "Locale.Helper.Selected.Country";
    private static final String SELECTED_SCRIPT = "Locale.Helper.Selected.Script";

    public static Context onAttach(Context context) {
        LocaleListCompat systemLocales = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration());
        String language;
        String country;
        String script;
        try {
            //noinspection ConstantConditions
            language = getPersistedData(context, SELECTED_LANGUAGE, systemLocales.get(0).getLanguage());
            //noinspection ConstantConditions
            country = getPersistedData(context, SELECTED_COUNTRY, systemLocales.get(0).getCountry());
            //noinspection ConstantConditions
            script = getPersistedData(context, SELECTED_SCRIPT, systemLocales.get(0).getScript());
        } catch (Exception e) {
            language = "en";
            country = "";
            script = "";
        }
        /*if (language.equals("[sys]")) {
            language = systemLocales.get(0).getLanguage();
            country = systemLocales.get(0).getCountry();
            script = systemLocales.get(0).getScript();
        }*/
        return setLocale(context, language, country, script, false);
    }

    /*
    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        if (lang.equals("[sys]")) {
            LocaleListCompat systemLocales = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration());
            lang = systemLocales.get(0).getLanguage();
        }
        return setLocale(context, lang, false);
    }
    */

    public static String getLanguage(Context context) {
        return getPersistedData(context, SELECTED_LANGUAGE, Locale.getDefault().getLanguage());
    }

    public static String getCountry(Context context) {
        return getPersistedData(context, SELECTED_COUNTRY, Locale.getDefault().getCountry());
    }

    public static String getScript(Context context) {
        return getPersistedData(context, SELECTED_SCRIPT, Locale.getDefault().getScript());
    }

    public static Context setLocale(Context context,
                                    String language,
                                    String country,
                                    String script,
                                    boolean persist) {

        String languageToStore = language;
        String countryToStore = country;
        String scriptToStore = script;

        if (language.equals("[sys]")) {
            LocaleListCompat systemLocales = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration());
            try {
                //noinspection ConstantConditions
                languageToStore = systemLocales.get(0).getLanguage();
                //noinspection ConstantConditions
                countryToStore = systemLocales.get(0).getCountry();
                //noinspection ConstantConditions
                scriptToStore = systemLocales.get(0).getScript();
            } catch (Exception e) {
//                Log.e("LocaleHelper.setLocale", Log.getStackTraceString(e));
                languageToStore = "en";
                countryToStore = "";
                scriptToStore = "";
            }
        }

//        Log.e("LocaleHelper.setLocale", "language="+languageToStore);
//        Log.e("LocaleHelper.setLocale", "country="+countryToStore);
//        Log.e("LocaleHelper.setLocale", "script="+scriptToStore);

        Context localizedContext = updateResources(context, languageToStore, countryToStore, scriptToStore, !persist);

        if ((localizedContext != null) && persist) {
            persist(context, SELECTED_LANGUAGE, languageToStore);
            persist(context, SELECTED_COUNTRY, countryToStore);
            persist(context, SELECTED_SCRIPT, scriptToStore);
        }

        return localizedContext;
    }

    static void setApplicationLocale(Context context) {
//        String language;
//        String country;
//        String script;

        //LocaleListCompat systemLocales = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration());
        Locale locale = context.getResources().getConfiguration().getLocales().get(0);
//        try {
//            //noinspection ConstantConditions
//            language = locale.getLanguage();
//            //noinspection ConstantConditions
//            country = locale.getCountry();
//            //noinspection ConstantConditions
//            script = locale.getScript();
//        } catch (Exception e) {
//            Log.e("LocaleHelper.setApplicationLocale", Log.getStackTraceString(e));
//            language = "en";
//            country = "";
//            script = "";
//        }

//        Log.e("LocaleHelper.setApplicationLocale", "language="+language);
//        Log.e("LocaleHelper.setApplicationLocale", "country="+country);
//        Log.e("LocaleHelper.setApplicationLocale", "script="+script);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        config.setLayoutDirection(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        //GlobalUtils.getCollator();
    }

    private static String getPersistedData(Context context, String data, String defaultValue) {
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(data, defaultValue);
    }

    private static void persist(Context context, String data, String value) {
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(data, value);
        editor.apply();
    }

    //@TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context,
                                           String language,
                                           String country,
                                           String script,
                                           boolean forAttach) {
        //Locale locale = new Locale(language);
        Locale locale = null;

        if (country.isEmpty() && script.isEmpty())
            locale = new Locale(language);
        if (!country.isEmpty())
            locale = new Locale(language, country);
        if (script.equals("Latn"))
            locale = new Locale.Builder().setLanguage("sr").setScript("Latn").build();

        if (locale != null) {
            Locale.setDefault(locale);

            Configuration configuration = context.getResources().getConfiguration();
            configuration.setLocale(locale);
            configuration.setLayoutDirection(locale);

            GlobalUtils.getCollator();

            if (forAttach) {
                // !!! this must be, without this not working detection of night mode
                configuration.uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED;
                //??? configuration.uiMode ^= (~Configuration.UI_MODE_NIGHT_MASK) & Configuration.UI_MODE_NIGHT_UNDEFINED;
            }

            return context.createConfigurationContext(configuration);
        } else
            return null;
    }

}