package sk.henrichg.phoneprofilesplus;

import java.text.DateFormat;
import java.util.Date;

import androidx.annotation.NonNull;

class TwilightState {

    private final boolean mIsNight;
    private final long mYesterdaySunset;
    private final long mTodaySunrise;
    private final long mTodaySunset;
    private final long mTomorrowSunrise;

    TwilightState(boolean isNight,
                  long yesterdaySunset,
                  long todaySunrise, long todaySunset,
                  long tomorrowSunrise) {
        mIsNight = isNight;
        mYesterdaySunset = yesterdaySunset;
        mTodaySunrise = todaySunrise;
        mTodaySunset = todaySunset;
        mTomorrowSunrise = tomorrowSunrise;
    }

    /**
     * Returns true if it is currently night time.
     */
    public boolean isNight() {
        return mIsNight;
    }

    /**
     * Returns the time of yesterday's sunset in the System.currentTimeMillis() timebase,
     * or -1 if the sun never sets.
     */
    public long getYesterdaySunset() {
        return mYesterdaySunset;
    }

    /**
     * Returns the time of today's sunrise in the System.currentTimeMillis() timebase,
     * or -1 if the sun never rises.
     */
    public long getTodaySunrise() {
        return mTodaySunrise;
    }

    /**
     * Returns the time of today's sunset in the System.currentTimeMillis() timebase,
     * or -1 if the sun never sets.
     */
    public long getTodaySunset() {
        return mTodaySunset;
    }

    /**
     * Returns the time of tomorrow's sunrise in the System.currentTimeMillis() timebase,
     * or -1 if the sun never rises.
     */
    public long getTomorrowSunrise() {
        return mTomorrowSunrise;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TwilightState && equals((TwilightState)o);
    }

    public boolean equals(TwilightState other) {
        return other != null
                && mIsNight == other.mIsNight
                && mYesterdaySunset == other.mYesterdaySunset
                && mTodaySunrise == other.mTodaySunrise
                && mTodaySunset == other.mTodaySunset
                && mTomorrowSunrise == other.mTomorrowSunrise;
    }

    @Override
    public int hashCode() {
        return 0; // don't care
    }

    @NonNull
    @Override
    public String toString() {
        DateFormat f = DateFormat.getDateTimeInstance();
        return "{TwilightState: isNight=" + mIsNight
                + ", mYesterdaySunset=" + f.format(new Date(mYesterdaySunset))
                + ", mTodaySunrise=" + f.format(new Date(mTodaySunrise))
                + ", mTodaySunset=" + f.format(new Date(mTodaySunset))
                + ", mTomorrowSunrise=" + f.format(new Date(mTomorrowSunrise))
                + "}";
    }

}
