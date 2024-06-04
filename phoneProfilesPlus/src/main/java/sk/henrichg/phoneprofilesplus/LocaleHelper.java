package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.core.os.ConfigurationCompat;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREF_SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";
    private static final String PREF_SELECTED_COUNTRY = "Locale.Helper.Selected.Country";
    private static final String PREF_SELECTED_SCRIPT = "Locale.Helper.Selected.Script";

    private static final String PREF_IS_SET_SYSTEM_LANGUAGE = "Locale.Helper.IsSetSystemLanguage";

    static final String LANG_SYS = "[sys]";
    private static final String LANG_EN = "en";
    private static final String LANG_LATN = "Latn";
    private static final String LANG_SR = "sr";


    public static Context onAttach(Context context) {
        String language;
        String country;
        String script;
        LocaleListCompat systemLocales = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration());
        if (getIsSetSystemLanguage(context)) {
            try {
                //noinspection ConstantConditions
                language = systemLocales.get(0).getLanguage();
                //noinspection ConstantConditions
                country = systemLocales.get(0).getCountry();
                //noinspection ConstantConditions
                script = systemLocales.get(0).getScript();
            } catch (Exception e) {
                language = LANG_EN;
                country = "";
                script = "";
            }
        } else {
            try {
                //noinspection ConstantConditions
                language = getPersistedData(context, PREF_SELECTED_LANGUAGE, systemLocales.get(0).getLanguage());
                //noinspection ConstantConditions
                country = getPersistedData(context, PREF_SELECTED_COUNTRY, systemLocales.get(0).getCountry());
                //noinspection ConstantConditions
                script = getPersistedData(context, PREF_SELECTED_SCRIPT, systemLocales.get(0).getScript());
            } catch (Exception e) {
                language = LANG_EN;
                country = "";
                script = "";
            }
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
        if (lang.equals(LANG_SYS)) {
            LocaleListCompat systemLocales = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration());
            lang = systemLocales.get(0).getLanguage();
        }
        return setLocale(context, lang, false);
    }
    */

    public static String getLanguage(Context context) {
        return getPersistedData(context, PREF_SELECTED_LANGUAGE, Locale.getDefault().getLanguage());
    }

    public static String getCountry(Context context) {
        return getPersistedData(context, PREF_SELECTED_COUNTRY, Locale.getDefault().getCountry());
    }

    public static String getScript(Context context) {
        return getPersistedData(context, PREF_SELECTED_SCRIPT, Locale.getDefault().getScript());
    }

    public static boolean getIsSetSystemLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_IS_SET_SYSTEM_LANGUAGE, true);
    }

    public static void setIsSetSystemLanguage(Context context, boolean value) {
        SharedPreferences preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_IS_SET_SYSTEM_LANGUAGE, value);
        editor.apply();
    }

    public static Context setLocale(Context context,
                                    String language,
                                    String country,
                                    String script,
                                    boolean persist) {

        String languageToStore = language;
        String countryToStore = country;
        String scriptToStore = script;

        if (language.equals(LANG_SYS)) {
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
                languageToStore = LANG_EN;
                countryToStore = "";
                scriptToStore = "";
            }
        }

//        Log.e("LocaleHelper.setLocale", "language="+languageToStore);
//        Log.e("LocaleHelper.setLocale", "country="+countryToStore);
//        Log.e("LocaleHelper.setLocale", "script="+scriptToStore);

        Context localizedContext = updateResources(context, languageToStore, countryToStore, scriptToStore, !persist);

        if ((localizedContext != null) && persist) {
            persist(context, PREF_SELECTED_LANGUAGE, languageToStore);
            persist(context, PREF_SELECTED_COUNTRY, countryToStore);
            persist(context, PREF_SELECTED_SCRIPT, scriptToStore);
        }

        return localizedContext;
    }

    static void setApplicationLocale(Context context) {
        String language;
        String country;
        String script;
        LocaleListCompat systemLocales = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration());
        if (getIsSetSystemLanguage(context)) {
            try {
                //noinspection ConstantConditions
                language = systemLocales.get(0).getLanguage();
                //noinspection ConstantConditions
                country = systemLocales.get(0).getCountry();
                //noinspection ConstantConditions
                script = systemLocales.get(0).getScript();
            } catch (Exception e) {
                language = LANG_EN;
                country = "";
                script = "";
            }
        } else {
            try {
                //noinspection ConstantConditions
                language = getPersistedData(context, PREF_SELECTED_LANGUAGE, systemLocales.get(0).getLanguage());
                //noinspection ConstantConditions
                country = getPersistedData(context, PREF_SELECTED_COUNTRY, systemLocales.get(0).getCountry());
                //noinspection ConstantConditions
                script = getPersistedData(context, PREF_SELECTED_SCRIPT, systemLocales.get(0).getScript());
            } catch (Exception e) {
                language = LANG_EN;
                country = "";
                script = "";
            }
        }

        Locale locale = null;

        if (country.isEmpty() && script.isEmpty())
            locale = new Locale(language);
        if (!country.isEmpty())
            locale = new Locale(language, country);
        if (script.equals(LANG_LATN))
            locale = new Locale.Builder().setLanguage(LANG_SR).setScript(LANG_LATN).build();

//        Log.e("LocaleHelper.setApplicationLocale", "language="+language);
//        Log.e("LocaleHelper.setApplicationLocale", "country="+country);
//        Log.e("LocaleHelper.setApplicationLocale", "script="+script);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        config.setLayoutDirection(locale);
        //noinspection deprecation
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        PPApplication.collator = GlobalUtils.getCollator();
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
        if (script.equals(LANG_LATN))
            locale = new Locale.Builder().setLanguage(LANG_SR).setScript(LANG_LATN).build();

        if (locale != null) {
            Locale.setDefault(locale);

            Configuration configuration = context.getResources().getConfiguration();
            configuration.setLocale(locale);
            configuration.setLayoutDirection(locale);

            PPApplication.collator = GlobalUtils.getCollator();

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