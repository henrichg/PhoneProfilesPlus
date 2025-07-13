package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.core.content.ContextCompat;

/** @noinspection ExtractMethodRecommender*/
class ProfilePreferencesIndicator {

    final int[] drawables = new int[70];
    final boolean[] disabled = new boolean[70];
    final String[] strings = new String[70];
    int countDrawables = 0;

    final String[] preferences = new String[70];
    final int[] countItems = new int[70];
    int countPreferences = 0;

    //static final int DISABLED_ALPHA_DYNAMIC_LIGHT = 255;
    //static final int DISABLED_ALPHA_DYNAMIC_DARK = 150;
    static final int DISABLED_ALPHA_MONOCHROME = 128;

    private Bitmap createIndicatorBitmap(/*Context context,*/ int countDrawables)
    {
        // bitmap to get size
        //Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_profile_pref_volume_on);

        //Bitmap bmp = BitmapManipulator.getBitmapFromResource(R.drawable.ic_profile_pref_volume_on, false, context);

        //int width  = bmp.getWidth() * countDrawables;
        //int height  = bmp.getHeight();

        //final BitmapFactory.Options opt = new BitmapFactory.Options();
        //opt.inJustDecodeBounds = true;
        //BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_profile_pref_volume_on, opt);

        //int width = opt.outWidth * countDrawables;
        //int height = opt.outHeight;

        int iconSize = GlobalGUIRoutines.dpToPx(24);
        int width = iconSize * countDrawables;

        return Bitmap.createBitmap(width, iconSize, Bitmap.Config.ARGB_8888);
    }

    private void addIndicator(int preferenceBitmapResourceID, int index,
                              boolean monochrome, boolean disabled,
                              int indicatorsType, float indicatorsLightnessValue,
                              Context context, Canvas canvas)
    {
        Bitmap preferenceBitmap = BitmapFactory.decodeResource(context.getResources(), preferenceBitmapResourceID);

        if (indicatorsType == DataWrapper.IT_FOR_EDITOR) {
            Paint paint = new Paint();

            // must be used check for theme, because context is application context, not activity context
            String applicationTheme = ApplicationPreferences.applicationTheme(context, true);
            if (applicationTheme.equals(ApplicationPreferences.PREF_APPLICATION_THEME_VALUE_DARK)) {
                if (disabled)
                    paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_dark), PorterDuff.Mode.SRC_ATOP));
                else
                    paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_dark), PorterDuff.Mode.SRC_ATOP));
            } else {
                if (disabled)
                    paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_light), PorterDuff.Mode.SRC_ATOP));
                else
                    paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_light), PorterDuff.Mode.SRC_ATOP));
            }

            // draw one indicator icon
            Bitmap bitmapResult = Bitmap.createBitmap(preferenceBitmap.getWidth(), preferenceBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas _canvas = new Canvas(bitmapResult);
            _canvas.drawBitmap(preferenceBitmap, 0, 0, paint);

            // add icon into profile preferences indicator
            //noinspection ConstantValue
            if (bitmapResult != null)
                canvas.drawBitmap(bitmapResult, preferenceBitmap.getWidth() * index, 0, null);

            //noinspection ConstantValue
            if (bitmapResult != null) {
                if (!bitmapResult.isRecycled()) {
                    try {
                        bitmapResult.recycle();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        else
        if ((indicatorsType == DataWrapper.IT_FOR_NOTIFICATION) ||
            (indicatorsType == DataWrapper.IT_FOR_NOTIFICATION_NATIVE_BACKGROUND) ||
                (indicatorsType == DataWrapper.IT_FOR_NOTIFICATION_DARK_BACKGROUND) ||
                (indicatorsType == DataWrapper.IT_FOR_NOTIFICATION_LIGHT_BACKGROUND)) {
            Paint paint = new Paint();

            boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext());
            //(context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
            //                    == Configuration.UI_MODE_NIGHT_YES;

            //boolean setAlpha = true;

            if ((Build.VERSION.SDK_INT >= 31) &&
                    (indicatorsType == DataWrapper.IT_FOR_NOTIFICATION_NATIVE_BACKGROUND)) {
                int dynamicColor = GlobalGUIRoutines.getDynamicColor(R.attr.colorPrimary, context);
                if ((dynamicColor != 0) && (!disabled) && (!monochrome)) {
                    dynamicColor = saturateColor(dynamicColor, !nightModeOn);
                    paint.setColorFilter(new PorterDuffColorFilter(dynamicColor, PorterDuff.Mode.SRC_ATOP));
                } else {
                    if (!monochrome) {
                        if (nightModeOn) {
                            if (disabled)
                                paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabledDynamic_dark), PorterDuff.Mode.SRC_ATOP));
                            else
                                paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_dark), PorterDuff.Mode.SRC_ATOP));
                        } else {
                            if (disabled)
                                paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabledDynamic_light), PorterDuff.Mode.SRC_ATOP));
                            else
                                paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_light), PorterDuff.Mode.SRC_ATOP));
                        }
                    }
                }
            } else {
                switch (indicatorsType) {
                    case DataWrapper.IT_FOR_NOTIFICATION_NATIVE_BACKGROUND:
                        if (nightModeOn) {
                            if (disabled)
                                paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_dark), PorterDuff.Mode.SRC_ATOP));
                            else
                                paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_dark), PorterDuff.Mode.SRC_ATOP));
                        } else {
                            if (disabled)
                                paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_light), PorterDuff.Mode.SRC_ATOP));
                            else
                                paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_light), PorterDuff.Mode.SRC_ATOP));
                        }
                        //setAlpha = false;
                        break;
                    case DataWrapper.IT_FOR_NOTIFICATION_DARK_BACKGROUND:
                        if (disabled)
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_dark), PorterDuff.Mode.SRC_ATOP));
                        else
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_dark), PorterDuff.Mode.SRC_ATOP));
                        //nightModeOn = true;
                        //setAlpha = false;
                        break;
                    case DataWrapper.IT_FOR_NOTIFICATION_LIGHT_BACKGROUND:
                        //noinspection DuplicateBranchesInSwitch
                        if (disabled)
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_light), PorterDuff.Mode.SRC_ATOP));
                        else
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_light), PorterDuff.Mode.SRC_ATOP));
                        //nightModeOn = false;
                        //setAlpha = false;
                        break;
                    default:
                        //nightModeOn = false;
                        if (disabled)
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_light), PorterDuff.Mode.SRC_ATOP));
                        else
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_light), PorterDuff.Mode.SRC_ATOP));
                }
            }

            if (disabled) {
                /*if (!monochrome) {
                    if (setAlpha) {
                        if (nightModeOn)
                            paint.setAlpha(DISABLED_ALPHA_DYNAMIC_DARK);
                        else
                            paint.setAlpha(DISABLED_ALPHA_DYNAMIC_LIGHT);
                    }
                } else
                    paint.setAlpha(DISABLED_ALPHA_MONOCHROME);*/
                if (monochrome)
                    paint.setAlpha(DISABLED_ALPHA_MONOCHROME);
            }

            // draw one indicator icon
            Bitmap bitmapResult = Bitmap.createBitmap(preferenceBitmap.getWidth(), preferenceBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas _canvas = new Canvas(bitmapResult);
            _canvas.drawBitmap(preferenceBitmap, 0, 0, paint);

            // change brightness of indicator
            bitmapResult = BitmapManipulator.setBitmapBrightness(bitmapResult, indicatorsLightnessValue);

            // add icon into profile preferences indicator
            if (bitmapResult != null)
                canvas.drawBitmap(bitmapResult, preferenceBitmap.getWidth() * index, 0, null);

            if (bitmapResult != null) {
                if (!bitmapResult.isRecycled()) {
                    try {
                        bitmapResult.recycle();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        else
        if (indicatorsType == DataWrapper.IT_FOR_NOTIFICATION_DYNAMIC_COLORS) {
            // this is only for API 31+

            Paint paint = new Paint();

            boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext());
            //(context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
            //                    == Configuration.UI_MODE_NIGHT_YES;

            int dynamicColor = GlobalGUIRoutines.getDynamicColor(R.attr.colorPrimary, context);
            if ((dynamicColor != 0) && (!disabled) && (!monochrome)) {
                dynamicColor = saturateColor(dynamicColor, !nightModeOn);
                paint.setColorFilter(new PorterDuffColorFilter(dynamicColor, PorterDuff.Mode.SRC_ATOP));
            } else {
                if (!monochrome) {
                    if (nightModeOn) {
                        if (disabled)
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabledDynamic_dark), PorterDuff.Mode.SRC_ATOP));
                        else
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_dark), PorterDuff.Mode.SRC_ATOP));
                    } else {
                        if (disabled)
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabledDynamic_light), PorterDuff.Mode.SRC_ATOP));
                        else
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_light), PorterDuff.Mode.SRC_ATOP));
                    }
                }
            }

            if (disabled) {
                /*if (!monochrome) {
                    if (nightModeOn)
                        paint.setAlpha(DISABLED_ALPHA_DYNAMIC_DARK);
                    else
                        paint.setAlpha(DISABLED_ALPHA_DYNAMIC_LIGHT);
                } else
                    paint.setAlpha(DISABLED_ALPHA_MONOCHROME);*/
                if (monochrome)
                    paint.setAlpha(DISABLED_ALPHA_MONOCHROME);
            }

            // draw one indicator icon
            Bitmap bitmapResult = Bitmap.createBitmap(preferenceBitmap.getWidth(), preferenceBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas _canvas = new Canvas(bitmapResult);
            _canvas.drawBitmap(preferenceBitmap, 0, 0, paint);

            // change brightness of indicator
            bitmapResult = BitmapManipulator.setBitmapBrightness(bitmapResult, indicatorsLightnessValue);

            // add icon into profile preferences indicator
            if (bitmapResult != null)
                canvas.drawBitmap(bitmapResult, preferenceBitmap.getWidth() * index, 0, null);

            if (bitmapResult != null) {
                if (!bitmapResult.isRecycled()) {
                    try {
                        bitmapResult.recycle();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        else
        if ((indicatorsType == DataWrapper.IT_FOR_WIDGET) ||
            (indicatorsType == DataWrapper.IT_FOR_WIDGET_NATIVE_BACKGROUND) ||
            (indicatorsType == DataWrapper.IT_FOR_WIDGET_DARK_BACKGROUND) ||
            (indicatorsType == DataWrapper.IT_FOR_WIDGET_LIGHT_BACKGROUND)) {
            Paint paint = new Paint();

            boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext());
            //(context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
            //                == Configuration.UI_MODE_NIGHT_YES;

            //boolean setAlpha = true;

            if (!monochrome) {
                switch (indicatorsType) {
                    case DataWrapper.IT_FOR_WIDGET_NATIVE_BACKGROUND:
                        if (nightModeOn) {
                            if (disabled)
                                paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_dark), PorterDuff.Mode.SRC_ATOP));
                            else
                                paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_dark), PorterDuff.Mode.SRC_ATOP));
                        } else {
                            if (disabled)
                                paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_light), PorterDuff.Mode.SRC_ATOP));
                            else
                                paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_light), PorterDuff.Mode.SRC_ATOP));
                        }
                        //setAlpha = false;
                        break;
                    case DataWrapper.IT_FOR_WIDGET_DARK_BACKGROUND:
                        if (disabled)
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_dark), PorterDuff.Mode.SRC_ATOP));
                        else
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_dark), PorterDuff.Mode.SRC_ATOP));
                        //nightModeOn = true;
                        //setAlpha = false;
                        break;
                    case DataWrapper.IT_FOR_WIDGET_LIGHT_BACKGROUND:
                        //noinspection DuplicateBranchesInSwitch
                        if (disabled)
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_light), PorterDuff.Mode.SRC_ATOP));
                        else
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_light), PorterDuff.Mode.SRC_ATOP));
                        //nightModeOn = false;
                        //setAlpha = false;
                        break;
                    default:
                        if (disabled)
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_light), PorterDuff.Mode.SRC_ATOP));
                        else
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_light), PorterDuff.Mode.SRC_ATOP));
                        //nightModeOn = false;
                }
            }

            if (disabled) {
                /*if (!monochrome) {
                    if (setAlpha) {
                        if (nightModeOn)
                            paint.setAlpha(DISABLED_ALPHA_DYNAMIC_DARK);
                        else
                            paint.setAlpha(DISABLED_ALPHA_DYNAMIC_LIGHT);
                    }
                } else
                    paint.setAlpha(DISABLED_ALPHA_MONOCHROME);*/
                if (monochrome)
                    paint.setAlpha(DISABLED_ALPHA_MONOCHROME);
            }

            // draw one indicator icon
            Bitmap bitmapResult = Bitmap.createBitmap(preferenceBitmap.getWidth(), preferenceBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas _canvas = new Canvas(bitmapResult);
            _canvas.drawBitmap(preferenceBitmap, 0, 0, paint);

            // change brightness of indicator
            bitmapResult = BitmapManipulator.setBitmapBrightness(bitmapResult, indicatorsLightnessValue);

            // add icon into profile preferences indicator
            if (bitmapResult != null)
                canvas.drawBitmap(bitmapResult, preferenceBitmap.getWidth() * index, 0, null);

            if (bitmapResult != null) {
                if (!bitmapResult.isRecycled()) {
                    try {
                        bitmapResult.recycle();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        else
        if (indicatorsType == DataWrapper.IT_FOR_WIDGET_DYNAMIC_COLORS) {
            // this is only for API 31+

            Paint paint = new Paint();

            boolean nightModeOn = GlobalGUIRoutines.isNightModeEnabled(context.getApplicationContext());
            //(context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
            //                    == Configuration.UI_MODE_NIGHT_YES;

            if (!monochrome) {
                int dynamicColor = GlobalGUIRoutines.getDynamicColor(R.attr.colorPrimary, context);
                if ((dynamicColor != 0) && (!disabled)/* && (!monochrome)*/) {
                    dynamicColor = saturateColor(dynamicColor, !nightModeOn);
                    paint.setColorFilter(new PorterDuffColorFilter(dynamicColor, PorterDuff.Mode.SRC_ATOP));
                } else {
                    if (nightModeOn) {
                        if (disabled)
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabledDynamic_dark), PorterDuff.Mode.SRC_ATOP));
                        else
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_dark), PorterDuff.Mode.SRC_ATOP));
                    } else {
                        if (disabled)
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabledDynamic_light), PorterDuff.Mode.SRC_ATOP));
                        else
                            paint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_light), PorterDuff.Mode.SRC_ATOP));
                    }
                }
            }

            if (disabled) {
                /*if (!monochrome) {
                    if (nightModeOn)
                        paint.setAlpha(DISABLED_ALPHA_DYNAMIC_DARK);
                    else
                        paint.setAlpha(DISABLED_ALPHA_DYNAMIC_LIGHT);
                } else
                    paint.setAlpha(DISABLED_ALPHA_MONOCHROME);*/
                if (monochrome)
                    paint.setAlpha(DISABLED_ALPHA_MONOCHROME);
            }

            // draw one indicator icon
            Bitmap bitmapResult = Bitmap.createBitmap(preferenceBitmap.getWidth(), preferenceBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas _canvas = new Canvas(bitmapResult);
            _canvas.drawBitmap(preferenceBitmap, 0, 0, paint);

            // change brightness of indicator
            bitmapResult = BitmapManipulator.setBitmapBrightness(bitmapResult, indicatorsLightnessValue);

            // add icon into profile preferences indicator
            if (bitmapResult != null)
                canvas.drawBitmap(bitmapResult, preferenceBitmap.getWidth() * index, 0, null);

            if (bitmapResult != null) {
                if (!bitmapResult.isRecycled()) {
                    try {
                        bitmapResult.recycle();
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        if (!preferenceBitmap.isRecycled()) {
            try {
                preferenceBitmap.recycle();
            } catch (Exception ignored) {
            }
        }

    }

    void fillArrays(Profile profile, boolean fillStrings, //boolean monochrome,
                    boolean fillPreferences, /*int indicatorsType,*/ Context appContext) {
        countDrawables = 0;
        countPreferences = 0;
        if (profile != null)
        {
            SharedPreferences sharedPreferences = appContext.getSharedPreferences(PPApplication.TMP_SHARED_PREFS_PROFILE_PREFERENCES_INDICATOR, Context.MODE_PRIVATE);
            profile.saveProfileToSharedPreferences(sharedPreferences);

            if (profile._volumeRingerMode != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean vibrateWhenRingingAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED;
                    boolean vibrateNotificationsAllowed = false;
                    if ((Build.VERSION.SDK_INT >= 28) && (Build.VERSION.SDK_INT < 33)) {
                        vibrateNotificationsAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED;
                    }
                    boolean addVibrateIndicator = false;
                    if (vibrateWhenRingingAllowed && ((profile._vibrateWhenRinging == 1) || (profile._vibrateWhenRinging == 3)))
                        addVibrateIndicator = true;
                    if ((Build.VERSION.SDK_INT >= 28) && (Build.VERSION.SDK_INT < 33)) {
                        if (vibrateNotificationsAllowed && ((profile._vibrateNotifications == 1) || (profile._vibrateNotifications == 3)))
                            addVibrateIndicator = true;
                    }

                    if (profile._volumeRingerMode == 5) {
                        // zen mode
                        if (profile._volumeZenMode == 1) {
                            if (fillPreferences) {
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSoundMode) + StringConstants.STR_COLON_WITH_SPACE +
                                        appContext.getString(R.string.array_pref_soundModeArray_ZenModeM) + " = " +
                                        appContext.getString(R.string.array_pref_zenModeArray_off);
                                if (addVibrateIndicator)
                                    preferences[countPreferences] = preferences[countPreferences] + ", " +
                                            appContext.getString(R.string.profile_preferences_vibrateWhenRingingOrNotifications);
                            }
                            if (fillStrings) {
                                strings[countDrawables++] = "dnd:off";
                                if (addVibrateIndicator)
                                    strings[countDrawables++] = "vibr";
                            }
                            else {
                                disabled[countDrawables] = false;
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_zen_mode;
                                if (addVibrateIndicator) {
                                    disabled[countDrawables] = false;
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration;
                                }
                            }
                            if (fillPreferences) {
                                if (addVibrateIndicator)
                                    countItems[countPreferences++] = 2;
                                else
                                    countItems[countPreferences++] = 1;
                            }
                        }
                        if (profile._volumeZenMode == 2) {
                            if (fillPreferences) {
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSoundMode) + StringConstants.STR_COLON_WITH_SPACE +
                                        appContext.getString(R.string.array_pref_soundModeArray_ZenModeM) + " = " +
                                        appContext.getString(R.string.array_pref_zenModeArray_priority);
                                if (addVibrateIndicator)
                                    preferences[countPreferences] = preferences[countPreferences] + ", " +
                                            appContext.getString(R.string.profile_preferences_vibrateWhenRingingOrNotifications);
                            }
                            if (fillStrings) {
                                strings[countDrawables++] = "dnd:pri";
                                if (addVibrateIndicator)
                                    strings[countDrawables++] = "vibr";
                            }
                            else {
                                disabled[countDrawables] = false;
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_zenmode_priority;
                                if (addVibrateIndicator) {
                                    disabled[countDrawables] = false;
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration;
                                }
                            }
                            if (fillPreferences) {
                                if (addVibrateIndicator)
                                    countItems[countPreferences++] = 2;
                                else
                                    countItems[countPreferences++] = 1;
                            }
                        }
                        if (profile._volumeZenMode == 3) {
                            if (fillPreferences)
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSoundMode) + StringConstants.STR_COLON_WITH_SPACE +
                                        appContext.getString(R.string.array_pref_soundModeArray_ZenModeM) + " = " +
                                        appContext.getString(R.string.array_pref_zenModeArray_totalSilence);
                            if (fillStrings)
                                strings[countDrawables++] = "dnd:sln";
                            else {
                                disabled[countDrawables] = false;
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_zenmode_none;
                            }
                            if (fillPreferences)
                                countItems[countPreferences++] = 1;
                        }
                        if (profile._volumeZenMode == 4) {
                            if (fillPreferences)
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSoundMode) + StringConstants.STR_COLON_WITH_SPACE +
                                        appContext.getString(R.string.array_pref_soundModeArray_ZenModeM) + " = " +
                                        appContext.getString(R.string.array_pref_zenModeArray_offButVibration);
                            if (fillStrings) {
                                strings[countDrawables++] = "dnd:off";
                                strings[countDrawables++] = "vib";
                            }
                            else {
                                disabled[countDrawables] = false;
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_zen_mode;
                                disabled[countDrawables] = false;
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration;
                            }
                            if (fillPreferences)
                                countItems[countPreferences++] = 2;
                        }
                        if (profile._volumeZenMode == 5) {
                            if (fillPreferences)
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSoundMode) + StringConstants.STR_COLON_WITH_SPACE +
                                        appContext.getString(R.string.array_pref_soundModeArray_ZenModeM) + " = " +
                                        appContext.getString(R.string.array_pref_zenModeArray_priorityWithVibration);
                            if (fillStrings) {
                                strings[countDrawables++] = "dnd:pri";
                                strings[countDrawables++] = "vib";
                            }
                            else {
                                disabled[countDrawables] = false;
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_zenmode_priority;
                                disabled[countDrawables] = false;
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration;
                            }
                            if (fillPreferences)
                                countItems[countPreferences++] = 2;
                        }
                        if (profile._volumeZenMode == 6) {
                            if (fillPreferences)
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSoundMode) + StringConstants.STR_COLON_WITH_SPACE +
                                        appContext.getString(R.string.array_pref_soundModeArray_ZenModeM) + " = " +
                                        appContext.getString(R.string.array_pref_zenModeArray_alarms);
                            if (fillStrings)
                                strings[countDrawables++] = "dnd:ala";
                            else {
                                disabled[countDrawables] = false;
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_zenmode_alarms;
                            }
                            if (fillPreferences)
                                countItems[countPreferences++] = 1;
                        }
                    } else {
                        // sound mode sound
                        if ((profile._volumeRingerMode == 1) || (profile._volumeRingerMode == 2)) {
                            if (fillPreferences) {
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSoundMode) + StringConstants.STR_COLON_WITH_SPACE +
                                        appContext.getString(R.string.array_pref_soundModeArray_sound);
                                if (addVibrateIndicator)
                                    preferences[countPreferences] = preferences[countPreferences] + ", " +
                                            appContext.getString(R.string.profile_preferences_vibrateWhenRingingOrNotifications);
                            }
                            if (fillStrings) {
                                strings[countDrawables++] = "sond";
                                if (addVibrateIndicator)
                                    strings[countDrawables++] = "vibr";
                            }
                            else {
                                disabled[countDrawables] = false;
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_volume_on;
                                if (addVibrateIndicator) {
                                    disabled[countDrawables] = false;
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration;
                                }
                            }
                            if (fillPreferences) {
                                if (addVibrateIndicator)
                                    countItems[countPreferences++] = 2;
                                else
                                    countItems[countPreferences++] = 1;
                            }
                        }
                        // sound mode vibrate
                        if ((profile._volumeRingerMode == 2) || (profile._volumeRingerMode == 3)) {
                            if (fillPreferences)
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSoundMode) + StringConstants.STR_COLON_WITH_SPACE +
                                        appContext.getString(R.string.array_pref_soundModeArray_vibration);
                            if (fillStrings)
                                strings[countDrawables++] = "vibr";
                            else {
                                disabled[countDrawables] = false;
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration;
                            }
                            if (fillPreferences)
                                countItems[countPreferences++] = 1;
                        }
                        // sound mode alarms only
                        if (profile._volumeRingerMode == 4) {
                            if (fillPreferences) {
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSoundMode) + StringConstants.STR_COLON_WITH_SPACE +
                                        appContext.getString(R.string.array_pref_soundModeArray_silentM);
                                if (addVibrateIndicator)
                                    preferences[countPreferences] = preferences[countPreferences] + ", " +
                                            appContext.getString(R.string.profile_preferences_vibrateWhenRingingOrNotifications);
                            }
                            if (fillStrings) {
                                strings[countDrawables++] = "alrm";
                                if (addVibrateIndicator)
                                    strings[countDrawables++] = "vibr";
                            }
                            else {
                                disabled[countDrawables] = false;
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_zenmode_alarms;
                                if (addVibrateIndicator) {
                                    disabled[countDrawables] = false;
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration;
                                }
                            }
                            if (fillPreferences) {
                                if (addVibrateIndicator)
                                    countItems[countPreferences++] = 2;
                                else
                                    countItems[countPreferences++] = 1;
                            }
                        }
                    }
                }
            }
            // volume level
            if (profile._volumeMuteSound &&
                    (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                if (fillPreferences)
                    preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeMuteSound) + StringConstants.STR_COLON_WITH_SPACE +
                            appContext.getString(R.string.array_pref_hardwareModeArray_off);
                if (fillStrings)
                    strings[countDrawables++] = "volm";
                else {
                    disabled[countDrawables] = false;
                    drawables[countDrawables++] = R.drawable.ic_profile_pref_volume_mute;
                }
                if (fillPreferences)
                    countItems[countPreferences++] = 1;

                if (profile.getVolumeAlarmChange() ||
                        profile.getVolumeVoiceChange() ||
                        profile.getVolumeAccessibilityChange() ||
                        profile.getVolumeBluetoothSCOChange()) {
                    if ((ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_ALARM, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_VOICE, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumePartial);
                        if (fillStrings)
                            strings[countDrawables++] = "volp";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_volume_level_partial;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            else {
                if (profile.getVolumeAlarmChange() ||
                        profile.getVolumeMediaChange() ||
                        profile.getVolumeNotificationChange() ||
                        profile.getVolumeRingtoneChange() ||
                        profile.getVolumeSystemChange() ||
                        profile.getVolumeVoiceChange() ||
                        profile.getVolumeDTMFChange() ||
                        profile.getVolumeAccessibilityChange() ||
                        profile.getVolumeBluetoothSCOChange()) {
                    if ((ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_ALARM, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_MEDIA, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_RINGTONE, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_SYSTEM, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_VOICE, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_DTMF, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeAll);
                        if (fillStrings)
                            strings[countDrawables++] = "volu";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_volume_level;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // speaker phone
            if (profile._volumeSpeakerPhone != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (profile._volumeSpeakerPhone == 1) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSpeakerPhone) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "spe:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_speakerphone;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._volumeSpeakerPhone == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSpeakerPhone) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "spe:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_speakerphone;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }

            // vibration intensity
            if (profile.getVibrationIntensityRingingChange() ||
                    profile.getVibrationIntensityNotificationsChange() ||
                    profile.getVibrationIntensityTouchInteractionChange()) {
                if ((ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                        (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                        (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_vibrationIntensityAll);
                    if (fillStrings)
                        strings[countDrawables++] = "vibi";
                    else {
                        disabled[countDrawables] = false;
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration_intensity;
                    }
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }

            // sound
            if ((profile._soundRingtoneChange == 1) ||
                    (profile._soundNotificationChange == 1) ||
                    (profile._soundAlarmChange == 1)) {
                if ((ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                        (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                        (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_soundsChange);
                    if (fillStrings)
                        strings[countDrawables++] = "sond";
                    else {
                        disabled[countDrawables] = false;
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_sound;
                    }
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }

            if (((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                            (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                            (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                            (PPApplication.deviceIsOnePlus))) {
                final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        // sound for sim 1
                        if ((profile._soundRingtoneChangeSIM1 == 1) ||
                                (profile._soundNotificationChangeSIM1 == 1)) {
                            if ((ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                    (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                                if (fillPreferences)
                                    preferences[countPreferences] = appContext.getString(R.string.profile_preferences_soundsChangeSIM1);
                                if (fillStrings)
                                    strings[countDrawables++] = "snd1";
                                else {
                                    disabled[countDrawables] = false;
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_sound_sim1;
                                }
                                if (fillPreferences)
                                    countItems[countPreferences++] = 1;
                            }
                        }
                        // sound for sim 2
                        if ((profile._soundRingtoneChangeSIM2 == 1) ||
                                (profile._soundNotificationChangeSIM2 == 1)) {
                            if ((ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                    (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                                if (fillPreferences)
                                    preferences[countPreferences] = appContext.getString(R.string.profile_preferences_soundsChangeSIM2);
                                if (fillStrings)
                                    strings[countDrawables++] = "snd2";
                                else {
                                    disabled[countDrawables] = false;
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_sound_sim2;
                                }
                                if (fillPreferences)
                                    countItems[countPreferences++] = 1;
                            }
                        }
                    }
                }
                if (profile._soundSameRingtoneForBothSIMCards == 1) {
                    if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_soundSameRingtoneForBothSIMCards);
                        if (fillStrings)
                            strings[countDrawables++] = "srbs";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_sound;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }

            // sound on touch
            if (profile._soundOnTouch != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_ON_TOUCH, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._soundOnTouch == 1) || (profile._soundOnTouch == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_soundOnTouch) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "sto:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_sound_on_touch;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._soundOnTouch == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_soundOnTouch) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "sto:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_sound_on_touch;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // vibration on touch
            if (profile._vibrationOnTouch != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._vibrationOnTouch == 1) || (profile._vibrationOnTouch == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_vibrationOnTouch) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "vto:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration_on_touch;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._vibrationOnTouch == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_vibrationOnTouch) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "vto:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration_on_touch;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // dtmf tone when dialing
            if (profile._dtmfToneWhenDialing != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._dtmfToneWhenDialing == 1) || (profile._dtmfToneWhenDialing == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_dtmfToneWhenDialing) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "dtd:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_dtmf_tone_when_dialing;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._dtmfToneWhenDialing == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_dtmfToneWhenDialing) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "dtd:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_dtmf_tone_when_dialing;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // airplane mode
            if (profile._deviceAirplaneMode != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._deviceAirplaneMode == 1) || (profile._deviceAirplaneMode == 3) ||
                            (profile._deviceAirplaneMode == 4)  || (profile._deviceAirplaneMode == 6)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceAirplaneMode) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "arm:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_airplane_mode;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if ((profile._deviceAirplaneMode == 2) || (profile._deviceAirplaneMode == 5)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceAirplaneMode) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "arm:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_airplane_mode;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // auto-sync
            if (profile._deviceAutoSync != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_AUTOSYNC, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._deviceAutoSync == 1) || (profile._deviceAutoSync == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceAutosync) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "asy:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_autosync;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._deviceAutoSync == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceAutosync) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "asy:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_autosync;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // on/off sim
            if (Build.VERSION.SDK_INT >= 29) {
                final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        if (profile._deviceOnOffSIM1 != 0) {
                            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                                if ((profile._deviceOnOffSIM1 == 1) || (profile._deviceOnOffSIM1 == 3)) {
                                    if (fillPreferences)
                                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceOnOff_SIM1) + StringConstants.STR_COLON_WITH_SPACE +
                                                appContext.getString(R.string.array_pref_hardwareModeArray_on);
                                    if (fillStrings)
                                        strings[countDrawables++] = "so1:1";
                                    else {
                                        disabled[countDrawables] = false;
                                        drawables[countDrawables++] = R.drawable.ic_profile_pref_onoff_sim1;
                                    }
                                    if (fillPreferences)
                                        countItems[countPreferences++] = 1;
                                }
                                if (profile._deviceOnOffSIM1 == 2) {
                                    if (fillPreferences)
                                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceOnOff_SIM1) + StringConstants.STR_COLON_WITH_SPACE +
                                                appContext.getString(R.string.array_pref_hardwareModeArray_off);
                                    if (fillStrings)
                                        strings[countDrawables++] = "so1:0";
                                    else {
                                        disabled[countDrawables] = true;
                                        drawables[countDrawables++] = R.drawable.ic_profile_pref_onoff_sim1;
                                    }
                                    if (fillPreferences)
                                        countItems[countPreferences++] = 1;
                                }
                            }
                        }
                        if (profile._deviceOnOffSIM2 != 0) {
                            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                                if ((profile._deviceOnOffSIM2 == 1) || (profile._deviceOnOffSIM2 == 3)) {
                                    if (fillPreferences)
                                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceOnOff_SIM2) + StringConstants.STR_COLON_WITH_SPACE +
                                                appContext.getString(R.string.array_pref_hardwareModeArray_on);
                                    if (fillStrings)
                                        strings[countDrawables++] = "so2:1";
                                    else {
                                        disabled[countDrawables] = false;
                                        drawables[countDrawables++] = R.drawable.ic_profile_pref_onoff_sim2;
                                    }
                                    if (fillPreferences)
                                        countItems[countPreferences++] = 1;
                                }
                                if (profile._deviceOnOffSIM2 == 2) {
                                    if (fillPreferences)
                                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceOnOff_SIM2) + StringConstants.STR_COLON_WITH_SPACE +
                                                appContext.getString(R.string.array_pref_hardwareModeArray_off);
                                    if (fillStrings)
                                        strings[countDrawables++] = "so2:0";
                                    else {
                                        disabled[countDrawables] = true;
                                        drawables[countDrawables++] = R.drawable.ic_profile_pref_onoff_sim2;
                                    }
                                    if (fillPreferences)
                                        countItems[countPreferences++] = 1;
                                }
                            }
                        }
                    }
                }
            }

            // default sim card
                if (!profile._deviceDefaultSIMCards.equals("0|0|0")) {
                    if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                        if (telephonyManager != null) {
                            int phoneCount = telephonyManager.getPhoneCount();
                            if (phoneCount > 1) {
                                if (fillPreferences)
                                    preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceDefaultSIM);
                                if (fillStrings)
                                    strings[countDrawables++] = "dsim";
                                else {
                                    disabled[countDrawables] = false;
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_defaultsimcards;
                                }
                                if (fillPreferences)
                                    countItems[countPreferences++] = 1;
                            }
                        }
                    }
                }

            // mobile data
            if (profile._deviceMobileData != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._deviceMobileData == 1) || (profile._deviceMobileData == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceMobileData_21) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "mda:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._deviceMobileData == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceMobileData_21) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "mda:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
                /*
                final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        if (profile._deviceMobileDataSIM1 != 0) {
                            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1, null, sharedPreferences, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                                if ((profile._deviceMobileDataSIM1 == 1) || (profile._deviceMobileDataSIM1 == 3)) {
                                    if (fillPreferences)
                                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceMobileData_21_SIM1) + ": " +
                                                appContext.getString(R.string.array_pref_hardwareModeArray_on);
                                    if (fillStrings)
                                        strings[countDrawables++] = "md1:1";
                                    else {
                                        disabled[countDrawables] = false;
                                        drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata_sim1;
                                    }
                                    if (fillPreferences)
                                        countItems[countPreferences++] = 1;
                                }
                                if (profile._deviceMobileDataSIM1 == 2) {
                                    if (fillPreferences)
                                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceMobileData_21_SIM1) + ": " +
                                                appContext.getString(R.string.array_pref_hardwareModeArray_off);
                                    if (fillStrings)
                                        strings[countDrawables++] = "md1:0";
                                    else {
                                        disabled[countDrawables] = true;
                                        drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata_sim1;
                                    }
                                    if (fillPreferences)
                                        countItems[countPreferences++] = 1;
                                }
                            }
                        }
                        if (profile._deviceMobileDataSIM2 != 0) {
                            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2, null, sharedPreferences, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                                if ((profile._deviceMobileDataSIM2 == 1) || (profile._deviceMobileDataSIM2 == 3)) {
                                    if (fillPreferences)
                                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceMobileData_21_SIM2) + ": " +
                                                appContext.getString(R.string.array_pref_hardwareModeArray_on);
                                    if (fillStrings)
                                        strings[countDrawables++] = "md2:1";
                                    else {
                                        disabled[countDrawables] = false;
                                        drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata_sim2;
                                    }
                                    if (fillPreferences)
                                        countItems[countPreferences++] = 1;
                                }
                                if (profile._deviceMobileDataSIM2 == 2) {
                                    if (fillPreferences)
                                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceMobileData_21_SIM2) + ": " +
                                                appContext.getString(R.string.array_pref_hardwareModeArray_off);
                                    if (fillStrings)
                                        strings[countDrawables++] = "md2:0";
                                    else {
                                        disabled[countDrawables] = true;
                                        drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata_sim2;
                                    }
                                    if (fillPreferences)
                                        countItems[countPreferences++] = 1;
                                }
                            }
                        }
                    }
                }
                */

            // mobile data preferences
            if (profile._deviceMobileDataPrefs == 1) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceMobileDataPrefs);
                    if (fillStrings)
                        strings[countDrawables++] = "mdpr";
                    else {
                        disabled[countDrawables] = false;
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata_pref;
                    }
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // wifi
            if (profile._deviceWiFi != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._deviceWiFi == 1) ||
                            (profile._deviceWiFi == 3) ||
                            (profile._deviceWiFi == 4) ||
                            (profile._deviceWiFi == 5) ||
                            (profile._deviceWiFi == 6) ||
                            (profile._deviceWiFi == 8)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceWiFi) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "wif:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if ((profile._deviceWiFi == 2) ||
                        (profile._deviceWiFi == 7)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceWiFi) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "wif:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            //if (Build.VERSION.SDK_INT < 30) {
                // wifi AP
                if (profile._deviceWiFiAP != 0) {
                    if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI_AP, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        if ((profile._deviceWiFiAP == 1) || (profile._deviceWiFiAP == 3) || (profile._deviceWiFiAP == 4) || (profile._deviceWiFiAP == 5)) {
                            if (fillPreferences)
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceWiFiAP) + StringConstants.STR_COLON_WITH_SPACE +
                                        appContext.getString(R.string.array_pref_hardwareModeArray_on);
                            if (fillStrings)
                                strings[countDrawables++] = "wap:1";
                            else {
                                disabled[countDrawables] = false;
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi_ap;
                            }
                            if (fillPreferences)
                                countItems[countPreferences++] = 1;
                        }
                        if (profile._deviceWiFiAP == 2) {
                            if (fillPreferences)
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceWiFiAP) + StringConstants.STR_COLON_WITH_SPACE +
                                        appContext.getString(R.string.array_pref_hardwareModeArray_off);
                            if (fillStrings)
                                strings[countDrawables++] = "wap:0";
                            else {
                                disabled[countDrawables] = true;
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi_ap;
                            }
                            if (fillPreferences)
                                countItems[countPreferences++] = 1;
                        }
                    }
                }
            //}
            // wifi AP preferences
            if (profile._deviceWiFiAPPrefs == 1) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceWiFiAPPrefs);
                    if (fillStrings)
                        strings[countDrawables++] = "wapr";
                    else {
                        disabled[countDrawables] = false;
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi_ap_pref;
                    }
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // connect to ssid
            if (!profile._deviceConnectToSSID.equals(StringConstants.CONNECTTOSSID_JUSTANY)) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceConnectToSSID);
                    if (fillStrings)
                        strings[countDrawables++] = "ssid";
                    else {
                        disabled[countDrawables] = false;
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_connect_to_ssid;
                    }
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // bluetooth
            if (profile._deviceBluetooth != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._deviceBluetooth == 1) || (profile._deviceBluetooth == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceBluetooth) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "blt:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_bluetooth;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._deviceBluetooth == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceBluetooth) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "blt:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_bluetooth;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // location mode
            if (profile._deviceLocationMode != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_LOCATION_MODE, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (profile._deviceLocationMode > 1) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceLocationMode) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "lom:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_location_mode_on;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._deviceLocationMode == 1) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceLocationMode) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "lom:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_location_mode_on;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // gps
            if (profile._deviceGPS != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_GPS, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._deviceGPS == 1) || (profile._deviceGPS == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceGPS) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "gps:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_gps_on;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._deviceGPS == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceGPS) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "gps:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_gps_on;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // location settings preferences
            if (profile._deviceLocationServicePrefs == 1) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceLocationServicePrefs);
                    if (fillStrings)
                        strings[countDrawables++] = "lopr";
                    else {
                        disabled[countDrawables] = false;
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_locationsettings_pref;
                    }
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // nfc
            if (profile._deviceNFC != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NFC, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._deviceNFC == 1) || (profile._deviceNFC == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceNFC) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "nfc:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_nfc;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._deviceNFC == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceNFC) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "nfc:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_nfc;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // network type
            if (profile._deviceNetworkType != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceNetworkType);
                    if (fillStrings)
                        strings[countDrawables++] = "ntyp";
                    else {
                        disabled[countDrawables] = false;
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_network_type;
                    }
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
                final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        if (profile._deviceNetworkTypeSIM1 != 0) {
                            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                                if (fillPreferences)
                                    preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceNetworkTypeSIM1);
                                if (fillStrings)
                                    strings[countDrawables++] = "ntp1";
                                else {
                                    disabled[countDrawables] = false;
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_network_type_sim1;
                                }
                                if (fillPreferences)
                                    countItems[countPreferences++] = 1;
                            }
                        }
                        if (profile._deviceNetworkTypeSIM2 != 0) {
                            if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                                if (fillPreferences)
                                    preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceNetworkTypeSIM2);
                                if (fillStrings)
                                    strings[countDrawables++] = "ntp2";
                                else {
                                    disabled[countDrawables] = false;
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_network_type_sim2;
                                }
                                if (fillPreferences)
                                    countItems[countPreferences++] = 1;
                            }
                        }
                    }
                }
            // network type prefs
            if (profile._deviceNetworkTypePrefs != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceNetworkTypePrefs);
                    if (fillStrings)
                        strings[countDrawables++] = "ntpr";
                    else {
                        disabled[countDrawables] = false;
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_network_type_pref;
                    }
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // VPN
            if (!profile._deviceVPN.startsWith("0")) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_VPN, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    String[] splits = profile._deviceVPN.split(StringConstants.STR_SPLIT_REGEX);
                    boolean enableVPN;
                    try {
                        enableVPN = splits[1].equals("0");
                    }
                    catch (Exception e) {
                        enableVPN = false;
                    }
                    //noinspection IfStatementWithIdenticalBranches
                    if (enableVPN) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceVPN)  + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "vpn:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_vpn;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    } else {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceVPN) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "vpn:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_vpn;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // VPN Settings preferences
            if (profile._deviceVPNSettingsPrefs == 1) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_VPN_SETTINGS_PREFS, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceVPNSettingsPrefs);
                    if (fillStrings)
                        strings[countDrawables++] = "vpns";
                    else {
                        disabled[countDrawables] = false;
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_vpn_settings_pref;
                    }
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }

            // screen timeout
            if (profile._deviceScreenTimeout != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceScreenTimeout);
                    if (fillStrings)
                        strings[countDrawables++] = "sctm";
                    else {
                        disabled[countDrawables] = false;
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_timeout;
                    }
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // brightness/auto-brightness
            if (profile.getDeviceBrightnessChange()) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (profile.getDeviceBrightnessAutomatic()) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceBrightness) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.profile_preferences_deviceBrightness_automatic);
                        if (fillStrings)
                            strings[countDrawables++] = "bri:a";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_autobrightness;
                        }
                    }
                    else {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceBrightness) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.profile_preferences_deviceBrightness_manual);
                        if (fillStrings)
                            strings[countDrawables++] = "bri:m";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_brightness;
                        }
                    }
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // auto-rotate
            if (profile._deviceAutoRotate != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_AUTOROTATE, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                    if (profile._deviceAutoRotate == 6) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceAutoRotation) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "art:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_autorotate;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    else {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceAutoRotation) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "art:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_autorotate;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
            }
            // screen on permanent
            if (profile._screenOnPermanent != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._screenOnPermanent == 1) || (profile._screenOnPermanent == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceScreenOnPermanent) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "son:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_on_permanent;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._screenOnPermanent == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceScreenOnPermanent) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "son:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_on_permanent;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // screen on permanent
            if (profile._screenOnOff != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SCREEN_ON_OFF, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._screenOnOff == 1) || (profile._screenOnOff == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceScreenOnOff) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "sof:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_on_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._screenOnOff == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceScreenOnOff) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "sof:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_on_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // wallpaper
            if (profile._deviceWallpaperChange != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, profile,null,  true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceWallpaperChange);
                    if (fillStrings)
                        strings[countDrawables++] = "walp";
                    else {
                        disabled[countDrawables] = false;
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_wallpaper;
                    }
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // lock screen
            if (profile._deviceKeyguard != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_KEYGUARD, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._deviceKeyguard == 1) || (profile._deviceKeyguard == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceKeyguard) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "kgu:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_lockscreen;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._deviceKeyguard == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceKeyguard) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "kgu:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_lockscreen;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // lock device
            if (profile._lockDevice != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_LOCK_DEVICE, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (profile._lockDevice == 3) {
                        boolean enabled;
                        enabled = PPExtenderBroadcastReceiver.isEnabled(appContext, PPApplication.VERSION_CODE_EXTENDER_REQUIRED, false, false
                                /*, "ProfilePreferencesIndicator.fillArrays (profile._lockDevice)"*/);
                        if (enabled) {
                            if (fillPreferences)
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_lockDevice);
                            if (fillStrings)
                                strings[countDrawables++] = "lock";
                            else {
                                disabled[countDrawables] = false;
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_lock;
                            }
                            if (fillPreferences)
                                countItems[countPreferences++] = 1;
                        }
                    } else {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_lockDevice);
                        if (fillStrings)
                            strings[countDrawables++] = "lock";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_lock;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // notification led
            if (profile._notificationLed != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_NOTIFICATION_LED, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._notificationLed == 1) || (profile._notificationLed == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_notificationLed) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "nld:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_notification_led;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._notificationLed == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_notificationLed) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "nld:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_notification_led;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // heads-up notifications
            if (profile._headsUpNotifications != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._headsUpNotifications == 1) || (profile._headsUpNotifications == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_headsUpNotifications) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "hup:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_heads_up_notifications;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._headsUpNotifications == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_headsUpNotifications) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "hup:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_heads_up_notifications;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // always on display
            if (profile._alwaysOnDisplay != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._alwaysOnDisplay == 1) || (profile._alwaysOnDisplay == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_alwaysOnDisplay) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "aod:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_always_on_display;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._alwaysOnDisplay == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_alwaysOnDisplay) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "aod:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_always_on_display;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // screen dark mode
            if (profile._screenDarkMode != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SCREEN_DARK_MODE, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (profile._screenDarkMode == 1) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_screenDarkMode) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "dkm:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_dark_mode;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._screenDarkMode == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_screenDarkMode) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "dkm:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_dark_mode;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // screen night light
            if (profile._screenNightLight != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (profile._screenNightLight == 1) {
                        if (fillPreferences)
                            preferences[countPreferences] = ProfileStatic.getNightLightStringString(appContext) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "nli:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_night_light;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._screenNightLight == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = ProfileStatic.getNightLightStringString(appContext) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "nli:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_night_light;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // screen night light prefereces
            if (profile._screenNightLightPrefs == 1) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT_PREFS, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = ProfileStatic.getNightLightPrefsStringString(appContext);
                    if (fillStrings)
                        strings[countDrawables++] = "nlis";
                    else {
                        disabled[countDrawables] = false;
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_night_light_pref;
                    }
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }

            // power save mode
            if (profile._devicePowerSaveMode != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._devicePowerSaveMode == 1) || (profile._devicePowerSaveMode == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_devicePowerSaveMode) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "psm:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_power_save_mode;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._devicePowerSaveMode == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_devicePowerSaveMode) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "psm:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_power_save_mode;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // run application
            if (profile._deviceRunApplicationChange == 1) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceRunApplicationsShortcutsChange);
                    if (fillStrings)
                        strings[countDrawables++] = "ruap";
                    else {
                        disabled[countDrawables] = false;
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_run_application;
                    }
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // close app applications
            if (profile._deviceCloseAllApplications != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceCloseAllApplications);
                    if (fillStrings)
                        strings[countDrawables++] = "caap";
                    else {
                        disabled[countDrawables] = false;
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_close_all_applications;
                    }
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // force stop application
            if (profile._deviceForceStopApplicationChange >= 1) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (profile._deviceForceStopApplicationChange == 1) {
                        boolean enabled;
                        enabled = PPExtenderBroadcastReceiver.isEnabled(appContext, PPApplication.VERSION_CODE_EXTENDER_REQUIRED, false, false
                                /*, "ProfilePreferencesIndicator.fillArrays (profile._deviceForceStopApplicationChange)"*/);
                        if (enabled) {
                            if (fillPreferences)
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceForceStopApplicationsChange);
                            if (fillStrings)
                                strings[countDrawables++] = "fcst";
                            else {
                                disabled[countDrawables] = false;
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_force_stop_application;
                            }
                            if (fillPreferences)
                                countItems[countPreferences++] = 1;
                        }
                    } else {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceForceStopApplicationsChange);
                        if (fillStrings)
                            strings[countDrawables++] = "fcst";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_force_stop_application;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // generate notification
            if (profile.getGenerateNotificationGenerate()) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_GENERATE_NOTIFICATION, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_generateNotification);
                    if (fillStrings)
                        strings[countDrawables++] = "gent";
                    else {
                        disabled[countDrawables] = false;
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_generate_notification;
                    }
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // clear notifications
            if (profile._clearNotificationEnabled) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_clearNotificationEnabled);
                    if (fillStrings)
                        strings[countDrawables++] = "clnt";
                    else {
                        disabled[countDrawables] = false;
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_clear_notifications;
                    }
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // camera flash
            if (profile._cameraFlash != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_CAMERA_FLASH, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._cameraFlash == 1) || (profile._cameraFlash == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_cameraFlash) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "fla:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_camera_flash;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._cameraFlash == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_cameraFlash) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "fla:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_camera_flash;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // send sms
            if (
                    (
                            /*(profile._phoneCallsContactListType == EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) ||*/
                            ((profile._sendSMSContacts != null) && (!profile._sendSMSContacts.isEmpty())) ||
                                    ((profile._sendSMSContactGroups != null) && (!profile._sendSMSContactGroups.isEmpty()))
                    ) &&
                            profile._sendSMSSendSMS) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SEND_SMS_SEND_SMS, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_category_send_sms);
                    if (fillStrings)
                        strings[countDrawables++] = "ssms";
                    else {
                        disabled[countDrawables] = false;
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_send_sms;
                    }
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // play music
            if (profile._playMusic != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_PLAY_MUSIC, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (profile._playMusic > 0) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_playMusic);
                        if (fillStrings)
                            strings[countDrawables++] = "plmu";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_play_music;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }

            // enable wifi scanning
            if (profile._applicationEnableWifiScanning != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_ENABLE_WIFI_SCANNING, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (profile._applicationEnableWifiScanning == 1) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationEnableWifiScanning) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_disabled);
                        if (fillStrings)
                            strings[countDrawables++] = "wfs:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_wifi_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if ((profile._applicationEnableWifiScanning == 2) || (profile._applicationEnableWifiScanning == 3)){
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationEnableWifiScanning) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_enabled);
                        if (fillStrings)
                            strings[countDrawables++] = "wfs:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_wifi_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // enable bluetooth scanning
            if (profile._applicationEnableBluetoothScanning != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_ENABLE_BLUETOOTH_SCANNING, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (profile._applicationEnableBluetoothScanning == 1) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationEnableBluetoothScanning) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_disabled);
                        if (fillStrings)
                            strings[countDrawables++] = "bls:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_bluetooth_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if ((profile._applicationEnableBluetoothScanning == 2) || (profile._applicationEnableBluetoothScanning == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationEnableBluetoothScanning) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_enabled);
                        if (fillStrings)
                            strings[countDrawables++] = "bls:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_bluetooth_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // enable location scanning
            if (profile._applicationEnableLocationScanning != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_ENABLE_LOCATION_SCANNING, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (profile._applicationEnableLocationScanning == 1) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationEnableLocationScanning) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_disabled);
                        if (fillStrings)
                            strings[countDrawables++] = "los:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_location_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if ((profile._applicationEnableLocationScanning == 2) || (profile._applicationEnableLocationScanning == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationEnableLocationScanning) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_enabled);
                        if (fillStrings)
                            strings[countDrawables++] = "los:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_location_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // enable mobile cell scanning
            if (profile._applicationEnableMobileCellScanning != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_ENABLE_MOBILE_CELL_SCANNING, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (profile._applicationEnableMobileCellScanning == 1) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationEnableMobileCellScanning) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_disabled);
                        if (fillStrings)
                            strings[countDrawables++] = "mcs:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_mobile_cell_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if ((profile._applicationEnableMobileCellScanning == 2)  || (profile._applicationEnableMobileCellScanning == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationEnableMobileCellScanning) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_enabled);
                        if (fillStrings)
                            strings[countDrawables++] = "mcs:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_mobile_cell_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // enable orientation scanning
            if (profile._applicationEnableOrientationScanning != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_ENABLE_ORIENTATION_SCANNING, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (profile._applicationEnableOrientationScanning == 1) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationEnableOrientationScanning) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_disabled);
                        if (fillStrings)
                            strings[countDrawables++] = "ors:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_orientation_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if ((profile._applicationEnableOrientationScanning == 2) || (profile._applicationEnableOrientationScanning == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationEnableOrientationScanning) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_enabled);
                        if (fillStrings)
                            strings[countDrawables++] = "ors:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_orientation_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // enable notification scanning
            if (profile._applicationEnableNotificationScanning != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_ENABLE_NOTIFICATION_SCANNING, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (profile._applicationEnableNotificationScanning == 1) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationEnableNotificationScanning) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_disabled);
                        if (fillStrings)
                            strings[countDrawables++] = "nos:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_notification_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if ((profile._applicationEnableNotificationScanning == 2) || (profile._applicationEnableNotificationScanning == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationEnableNotificationScanning) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_enabled);
                        if (fillStrings)
                            strings[countDrawables++] = "nos:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_notification_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // enable periodic scanning
            if (profile._applicationEnablePeriodicScanning != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_ENABLE_PERIODIC_SCANNING, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (profile._applicationEnablePeriodicScanning == 1) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationEnablePeriodicScanning) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_disabled);
                        if (fillStrings)
                            strings[countDrawables++] = "pes:1";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_periodic_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if ((profile._applicationEnablePeriodicScanning == 2) || (profile._applicationEnablePeriodicScanning == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationEnablePeriodicScanning) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_enabled);
                        if (fillStrings)
                            strings[countDrawables++] = "pes:0";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_periodic_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // disable global events run
            if (profile._applicationDisableGloabalEventsRun != 0) {
                if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN, null, sharedPreferences, true, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._applicationDisableGloabalEventsRun == 1) || (profile._applicationDisableGloabalEventsRun == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationEnableGlobalEventsRun) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_applicationDisableGlobalEventsRun_disabled);
                        if (fillStrings)
                            strings[countDrawables++] = "ern:0";
                        else {
                            disabled[countDrawables] = true;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_events_run_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._applicationDisableGloabalEventsRun == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationEnableGlobalEventsRun) + StringConstants.STR_COLON_WITH_SPACE +
                                    appContext.getString(R.string.array_pref_applicationDisableGlobalEventsRun_enabled);
                        if (fillStrings)
                            strings[countDrawables++] = "ern:1";
                        else {
                            disabled[countDrawables] = false;
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_events_run_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
        }
        else
            countDrawables = -1;
    }

    Bitmap paint(Profile profile, boolean monochrome, int indicatorsType, float indicatorsLightnessValue, Context context)
    {
        Context appContext = context.getApplicationContext();

        //Profile profile = _profile; //Profile.getMappedProfile(_profile, context);

        fillArrays(profile, false, /*monochrome,*/ false, /*indicatorsType,*/ context);

        Bitmap indicatorBitmap;
        if (countDrawables >= 0)
        {
            if (countDrawables > 0)
            {
                try {
                    indicatorBitmap = createIndicatorBitmap(/*appContext,*/ countDrawables);
                    Canvas canvas = new Canvas(indicatorBitmap);
                    for (int i = 0; i < countDrawables; i++)
                        addIndicator(drawables[i], i, monochrome, disabled[i], indicatorsType, indicatorsLightnessValue, appContext, canvas);
                } catch (Exception e) {
                    indicatorBitmap = null;
                }
            }
            else
                indicatorBitmap = createIndicatorBitmap(/*appContext,*/ 1);
        }
        else
            indicatorBitmap = null;

        return indicatorBitmap;

    }

    /*
    private String addIntoIndicator(String indicator, String preference)
    {
        String ind = indicator;
        //if (maxLineLength > 0) {
        //    if (ind.length() > maxLength) {
        //        ind = ind + '\n';
        //        maxLength += maxLineLength;
        //   }
        //    else
        //        if (!ind.isEmpty()) ind = ind + "-";
        //}
        //else
            if (!ind.isEmpty()) ind = ind + StringConstants.STR_BULLET;

        ind = ind + preference;
        return ind;
    }
    */

    String getString(Profile profile, Context context) {
        // profile preferences indicator

        Context appContext = context.getApplicationContext();

        fillArrays(profile, true, /*false,*/ false, /*0,*/ appContext);

        StringBuilder indicators = new StringBuilder();
        if (countDrawables > 0) {
            //maxLength = maxLineLength;
            for (int i = 0; i < countDrawables; i++) {
                if (indicators.length() > 0) indicators.append(StringConstants.STR_BULLET);
                indicators.append(strings[i]);
            }
        }
        return indicators.toString();

        /*
        String indicator1 = "";
        if (countDrawables > 0) {
            //maxLength = maxLineLength;
            for (int i = 0; i < countDrawables; i++)
                indicator1 = addIntoIndicator(indicator1, strings[i]);
        }

        return indicator1;
        */
    }

    private int saturateColor(int color, boolean forLightTheme) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        //Log.e("ProfilePreferencesIndicator.saturateColor", "hsv[1]="+hsv[1]);
        if (hsv[1] < 0.45f)
            hsv[1] = 0.45f;  // saturation component
        if (forLightTheme)
            hsv[2] = 0.55f; // value component
        return Color.HSVToColor(hsv);
    }

}
