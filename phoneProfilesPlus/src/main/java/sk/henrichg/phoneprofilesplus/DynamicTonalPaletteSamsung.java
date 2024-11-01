// do not used because some dynamic notification, widgets has its own laypouts and in it
// are colors configured = keep material componets lib to 1.10.0
//
// This class is not tested !!!
package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;

/** @noinspection unused*/
class DynamicTonalPaletteSamsung {

    /** @noinspection InnerClassMayBeStatic*/
    private class TonalPalette {
        // The neutral tonal range from the generated dynamic color palette.
        int neutral100;
        int neutral99;
        int neutral95;
        int neutral90;
        int neutral80;
        int neutral70;
        int neutral60;
        int neutral50;
        int neutral40;
        int neutral30;
        int neutral20;
        int neutral10;
        int neutral0;

        // The neutral variant tonal range from the generated dynamic color palette.
        int neutralVariant100;
        int neutralVariant99;
        int neutralVariant98;
        int neutralVariant96;
        int neutralVariant94;
        int neutralVariant92;
        int neutralVariant95;
        int neutralVariant90;
        int neutralVariant80;
        int neutralVariant87;
        int neutralVariant70;
        int neutralVariant60;
        int neutralVariant50;
        int neutralVariant40;
        int neutralVariant30;
        int neutralVariant24;
        int neutralVariant22;
        int neutralVariant20;
        int neutralVariant17;
        int neutralVariant12;
        int neutralVariant6;
        int neutralVariant4;
        int neutralVariant10;
        int neutralVariant0;

        // The primary tonal range from the generated dynamic color palette.
        int primary100;
        int primary99;
        int primary95;
        int primary90;
        int primary80;
        int primary70;
        int primary60;
        int primary50;
        int primary40;
        int primary30;
        int primary20;
        int primary10;
        int primary0;

        // The secondary tonal range from the generated dynamic color palette.
        int secondary100;
        int secondary99;
        int secondary95;
        int secondary90;
        int secondary80;
        int secondary70;
        int secondary60;
        int secondary50;
        int secondary40;
        int secondary30;
        int secondary20;
        int secondary10;
        int secondary0;

        // The tertiary tonal range from the generated dynamic color palette.
        int tertiary100;
        int tertiary99;
        int tertiary95;
        int tertiary90;
        int tertiary80;
        int tertiary70;
        int tertiary60;
        int tertiary50;
        int tertiary40;
        int tertiary30;
        int tertiary20;
        int tertiary10;
        int tertiary0;

        //boolean isDarkTheme = false;
        //boolean isSystemTheme = true;
    }

    /** @noinspection InnerClassMayBeStatic*/
    class ColorScheme {

        int primary;
        int onPrimary;
        int primaryContainer;
        int onPrimaryContainer;
        int inversePrimary;
        int secondary;
        int onSecondary;
        int secondaryContainer;
        int onSecondaryContainer;
        int tertiary;
        int onTertiary;
        int tertiaryContainer;
        int onTertiaryContainer;
        int background;
        int onBackground;
        int surface;
        int onSurface;
        int surfaceVariant;
        int onSurfaceVariant;
        int inverseSurface;
        int inverseOnSurface;
        int outline;
        int outlineVariant;
        int scrim;
        int surfaceBright;
        int surfaceDim;
        int surfaceContainer;
        int surfaceContainerHigh;
        int surfaceContainerHighest;
        int surfaceContainerLow;
        int surfaceContainerLowest;
        int surfaceTint;
    }

    /* Dynamic colors in Material. */
    @RequiresApi(api = Build.VERSION_CODES.S)
    TonalPalette dynamicTonalPalette(Context context) {
        TonalPalette palette = new TonalPalette();

        // The neutral tonal range from the generated dynamic color palette.
        palette.neutral100 = context.getColor(android.R.color.system_neutral1_0);
        palette.neutral99 = context.getColor(android.R.color.system_neutral1_10);
        palette.neutral95 = context.getColor(android.R.color.system_neutral1_50);
        palette.neutral90 = context.getColor(android.R.color.system_neutral1_100);
        palette.neutral80 = context.getColor(android.R.color.system_neutral1_200);
        palette.neutral70 = context.getColor(android.R.color.system_neutral1_300);
        palette.neutral60 = context.getColor(android.R.color.system_neutral1_400);
        palette.neutral50 = context.getColor(android.R.color.system_neutral1_500);
        palette.neutral40 = context.getColor(android.R.color.system_neutral1_600);
        palette.neutral30 = context.getColor(android.R.color.system_neutral1_700);
        palette.neutral20 = context.getColor(android.R.color.system_neutral1_800);
        palette.neutral10 = context.getColor(android.R.color.system_neutral1_900);
        palette.neutral0 = context.getColor(android.R.color.system_neutral1_1000);

        // The neutral variant tonal range, sometimes called "neutral 2",  from the
        // generated dynamic color palette.
        palette.neutralVariant100 = context.getColor(android.R.color.system_neutral2_0);
        palette.neutralVariant99 = context.getColor(android.R.color.system_neutral2_10);

        palette.neutralVariant98 = setLuminance(
                context.getColor(android.R.color.system_neutral2_600), 98f);
        palette.neutralVariant96 = setLuminance(
                context.getColor(android.R.color.system_neutral2_600),96f);
        palette.neutralVariant95 = context.getColor(android.R.color.system_neutral2_50);
        palette.neutralVariant94 = setLuminance(
                context.getColor(android.R.color.system_neutral2_600),94f);
        palette.neutralVariant92 = setLuminance(
                context.getColor(android.R.color.system_neutral2_600), 92f);
        palette.neutralVariant90 = context.getColor(android.R.color.system_neutral2_100);
        palette.neutralVariant80 = context.getColor(android.R.color.system_neutral2_200);
        palette.neutralVariant87 = setLuminance(
                context.getColor(android.R.color.system_neutral2_600),87f);
        palette.neutralVariant70 = context.getColor(android.R.color.system_neutral2_300);
        palette.neutralVariant60 = context.getColor(android.R.color.system_neutral2_400);
        palette.neutralVariant50 = context.getColor(android.R.color.system_neutral2_500);
        palette.neutralVariant40 = context.getColor(android.R.color.system_neutral2_600);
        palette.neutralVariant30 = context.getColor(android.R.color.system_neutral2_700);
        palette.neutralVariant24 = setLuminance(
                context.getColor(android.R.color.system_neutral2_600),24f);
        palette.neutralVariant22 = setLuminance(
                context.getColor(android.R.color.system_neutral2_600),22f);
        palette.neutralVariant17 = setLuminance(
                context.getColor(android.R.color.system_neutral2_600),17f);
        palette.neutralVariant12 = setLuminance(
                context.getColor(android.R.color.system_neutral2_600),12f);
        palette.neutralVariant6 = setLuminance(
                context.getColor(android.R.color.system_neutral2_600),6f);
        palette.neutralVariant4 = setLuminance(
                context.getColor(android.R.color.system_neutral2_600),4f);
        palette.neutralVariant20 = context.getColor(android.R.color.system_neutral2_800);
        palette.neutralVariant10 = context.getColor(android.R.color.system_neutral2_900);
        palette.neutralVariant0 = context.getColor(android.R.color.system_neutral2_1000);

        // The primary tonal range from the generated dynamic color palette.
        palette.primary100 = context.getColor(android.R.color.system_accent1_0);
        palette.primary99 = context.getColor(android.R.color.system_accent1_10);
        palette.primary95 = context.getColor(android.R.color.system_accent1_50);
        palette.primary90 = context.getColor(android.R.color.system_accent1_100);
        palette.primary80 = context.getColor(android.R.color.system_accent1_200);
        palette.primary70 = context.getColor(android.R.color.system_accent1_300);
        palette.primary60 = context.getColor(android.R.color.system_accent1_400);
        palette.primary50 = context.getColor(android.R.color.system_accent1_500);
        palette.primary40 = context.getColor(android.R.color.system_accent1_600);
        palette.primary30 = context.getColor(android.R.color.system_accent1_700);
        palette.primary20 = context.getColor(android.R.color.system_accent1_800);
        palette.primary10 = context.getColor(android.R.color.system_accent1_900);
        palette.primary0 = context.getColor(android.R.color.system_accent1_1000);

        // The secondary tonal range from the generated dynamic color palette.
        palette.secondary100 = context.getColor(android.R.color.system_accent2_0);
        palette.secondary99 = context.getColor(android.R.color.system_accent2_10);
        palette.secondary95 = context.getColor(android.R.color.system_accent2_50);
        palette.secondary90 = context.getColor(android.R.color.system_accent2_100);
        palette.secondary80 = context.getColor(android.R.color.system_accent2_200);
        palette.secondary70 = context.getColor(android.R.color.system_accent2_300);
        palette.secondary60 = context.getColor(android.R.color.system_accent2_400);
        palette.secondary50 = context.getColor(android.R.color.system_accent2_500);
        palette.secondary40 = context.getColor(android.R.color.system_accent2_600);
        palette.secondary30 = context.getColor(android.R.color.system_accent2_700);
        palette.secondary20 = context.getColor(android.R.color.system_accent2_800);
        palette.secondary10 = context.getColor(android.R.color.system_accent2_900);
        palette.secondary0 = context.getColor(android.R.color.system_accent2_1000);

        // The tertiary tonal range from the generated dynamic color palette.
        palette.tertiary100 = context.getColor(android.R.color.system_accent3_0);
        palette.tertiary99 = context.getColor(android.R.color.system_accent3_10);
        palette.tertiary95 = context.getColor(android.R.color.system_accent3_50);
        palette.tertiary90 = context.getColor(android.R.color.system_accent3_100);
        palette.tertiary80 = context.getColor(android.R.color.system_accent3_200);
        palette.tertiary70 = context.getColor(android.R.color.system_accent3_300);
        palette.tertiary60 = context.getColor(android.R.color.system_accent3_400);
        palette.tertiary50 = context.getColor(android.R.color.system_accent3_500);
        palette.tertiary40 = context.getColor(android.R.color.system_accent3_600);
        palette.tertiary30 = context.getColor(android.R.color.system_accent3_700);
        palette.tertiary20 = context.getColor(android.R.color.system_accent3_800);
        palette.tertiary10 = context.getColor(android.R.color.system_accent3_900);
        palette.tertiary0 = context.getColor(android.R.color.system_accent3_1000);

        return palette;
    }

    /**
     * Creates a light dynamic color scheme.
     *
     * Use this function to create a color scheme based off the system wallpaper. If the developer
     * changes the wallpaper this color scheme will change accordingly. This dynamic scheme is a
     * light theme variant.
     *
     * @param context The context required to get system resource data.
     * @noinspection JavadocBlankLines
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    ColorScheme dynamicLightColorSchemeSamsung(Context context) {
        if (PPApplication.deviceIsSamsung && (!PPApplication.romIsGalaxy611)) {
            TonalPalette tonalPalette = dynamicTonalPalette(context);

            ColorScheme colorSchene = new ColorScheme();

            colorSchene.primary = tonalPalette.primary40;
            colorSchene.onPrimary = tonalPalette.primary100;
            colorSchene.primaryContainer = tonalPalette.primary90;
            colorSchene.onPrimaryContainer = tonalPalette.primary10;
            colorSchene.inversePrimary = tonalPalette.primary80;
            colorSchene.secondary = tonalPalette.secondary40;
            colorSchene.onSecondary = tonalPalette.secondary100;
            colorSchene.secondaryContainer = tonalPalette.secondary90;
            colorSchene.onSecondaryContainer = tonalPalette.secondary10;
            colorSchene.tertiary = tonalPalette.tertiary40;
            colorSchene.onTertiary = tonalPalette.tertiary100;
            colorSchene.tertiaryContainer = tonalPalette.tertiary90;
            colorSchene.onTertiaryContainer = tonalPalette.tertiary10;
            colorSchene.background = tonalPalette.neutralVariant98;
            colorSchene.onBackground = tonalPalette.neutralVariant10;
            colorSchene.surface = tonalPalette.neutralVariant98;
            colorSchene.onSurface = tonalPalette.neutralVariant10;
            colorSchene.surfaceVariant = tonalPalette.neutralVariant90;
            colorSchene.onSurfaceVariant = tonalPalette.neutralVariant30;
            colorSchene.inverseSurface = tonalPalette.neutralVariant20;
            colorSchene.inverseOnSurface = tonalPalette.neutralVariant95;
            colorSchene.outline = tonalPalette.neutralVariant50;
            colorSchene.outlineVariant = tonalPalette.neutralVariant80;
            colorSchene.scrim = tonalPalette.neutralVariant0;
            colorSchene.surfaceBright = tonalPalette.neutralVariant98;
            colorSchene.surfaceDim = tonalPalette.neutralVariant87;
            colorSchene.surfaceContainer = tonalPalette.neutralVariant94;
            colorSchene.surfaceContainerHigh = tonalPalette.neutralVariant92;
            colorSchene.surfaceContainerHighest = tonalPalette.neutralVariant90;
            colorSchene.surfaceContainerLow = tonalPalette.neutralVariant96;
            colorSchene.surfaceContainerLowest = tonalPalette.neutralVariant100;
            colorSchene.surfaceTint = tonalPalette.primary40;

            return colorSchene;
        }
        else
            return null;
    }

    /**
     * Creates a dark dynamic color scheme.
     *
     * Use this function to create a color scheme based off the system wallpaper. If the developer
     * changes the wallpaper this color scheme will change accordingly. This dynamic scheme is a dark
     * theme variant.
     *
     * @param context The context required to get system resource data.
     * @noinspection JavadocBlankLines
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    ColorScheme dynamicDarkColorSchemeSamsung(Context context) {
        if (PPApplication.deviceIsSamsung && (!PPApplication.romIsGalaxy611)) {
            TonalPalette tonalPalette = dynamicTonalPalette(context);

            ColorScheme colorSchene = new ColorScheme();

            colorSchene.primary = tonalPalette.primary80;
            colorSchene.onPrimary = tonalPalette.primary20;
            colorSchene.primaryContainer = tonalPalette.primary30;
            colorSchene.onPrimaryContainer = tonalPalette.primary90;
            colorSchene.inversePrimary = tonalPalette.primary40;
            colorSchene.secondary = tonalPalette.secondary80;
            colorSchene.onSecondary = tonalPalette.secondary20;
            colorSchene.secondaryContainer = tonalPalette.secondary30;
            colorSchene.onSecondaryContainer = tonalPalette.secondary90;
            colorSchene.tertiary = tonalPalette.tertiary80;
            colorSchene.onTertiary = tonalPalette.tertiary20;
            colorSchene.tertiaryContainer = tonalPalette.tertiary30;
            colorSchene.onTertiaryContainer = tonalPalette.tertiary90;
            colorSchene.background = tonalPalette.neutralVariant6;
            colorSchene.onBackground = tonalPalette.neutralVariant90;
            colorSchene.surface = tonalPalette.neutralVariant6;
            colorSchene.onSurface = tonalPalette.neutralVariant90;
            colorSchene.surfaceVariant = tonalPalette.neutralVariant30;
            colorSchene.onSurfaceVariant = tonalPalette.neutralVariant80;
            colorSchene.inverseSurface = tonalPalette.neutralVariant90;
            colorSchene.inverseOnSurface = tonalPalette.neutralVariant20;
            colorSchene.outline = tonalPalette.neutralVariant60;
            colorSchene.outlineVariant = tonalPalette.neutralVariant30;
            colorSchene.scrim = tonalPalette.neutralVariant0;
            colorSchene.surfaceBright = tonalPalette.neutralVariant24;
            colorSchene.surfaceDim = tonalPalette.neutralVariant6;
            colorSchene.surfaceContainer = tonalPalette.neutralVariant12;
            colorSchene.surfaceContainerHigh = tonalPalette.neutralVariant17;
            colorSchene.surfaceContainerHighest = tonalPalette.neutralVariant22;
            colorSchene.surfaceContainerLow = tonalPalette.neutralVariant10;
            colorSchene.surfaceContainerLowest = tonalPalette.neutralVariant4;
            colorSchene.surfaceTint = tonalPalette.primary80;

            return colorSchene;
        } else
            return null;
    }


    int setLuminance(int color, float luminance) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        //int alpha = Color.alpha(intColor);
        float[] hsl = GlobalGUIRoutines.rgbToHsl(red, green, blue);
        hsl[2] = hsl[2] / 100f * luminance;
        int[] rgb = GlobalGUIRoutines.hslToRgb(hsl[0], hsl[1], hsl[2]);
        Color newColor = Color.valueOf(rgb[0], rgb[1], rgb[2], 1);
        return newColor.toArgb();
    }

}
