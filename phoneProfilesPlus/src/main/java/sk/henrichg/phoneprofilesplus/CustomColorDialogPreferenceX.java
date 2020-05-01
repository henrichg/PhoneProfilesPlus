package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

import com.kunzisoft.androidclearchroma.ChromaUtil;

public class CustomColorDialogPreferenceX extends DialogPreference {

    CustomColorDialogPreferenceFragmentX fragment;

    final int chromaColorMode;
    final int chromaIndicatorMode;

    private AppCompatImageView backgroundPreview;
    private AppCompatImageView colorPreview;

    // Custom xml attributes.
    int value;

    private int defaultValue;
    private boolean savedInstanceState;


    public CustomColorDialogPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.ChromaPreference);

        chromaColorMode = typedArray.getInteger(
                R.styleable.ChromaPreference_chromaColorMode, 1);
        chromaIndicatorMode = typedArray.getInteger(
                R.styleable.ChromaPreference_chromaIndicatorMode, 1);

        setWidgetLayoutResource(R.layout.dialog_custom_color_preference); // resource na layout custom preference - TextView-ImageView

        typedArray.recycle();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        backgroundPreview = (AppCompatImageView)holder.findViewById(R.id.dialog_color_chooser_pref_background_preview);
        colorPreview = (AppCompatImageView)holder.findViewById(R.id.dialog_color_chooser_pref_color_preview);

        setColorInWidget();
    }

    private Bitmap getRoundedCroppedBitmap(Bitmap bitmap, int widthLight, int heightLight, float radius) {
        Bitmap output = Bitmap.createBitmap(widthLight, heightLight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paintColor = new Paint();
        paintColor.setFlags(Paint.ANTI_ALIAS_FLAG);

        RectF rectF = new RectF(new Rect(0, 0, widthLight, heightLight));

        canvas.drawRoundRect(rectF, radius, radius, paintColor);

        Paint paintImage = new Paint();
        paintImage.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        //noinspection IntegerDivisionInFloatingPointContext
        canvas.drawBitmap(bitmap, -(bitmap.getWidth() - widthLight)/2 , -(bitmap.getHeight() - heightLight)/2, paintImage);

        return output;
    }

    private void setColorInWidget() {

        int color;
        if (fragment != null)
            color = fragment.chromaColorView.getCurrentColor();
        else
            color = value;

        try {
            if (colorPreview != null) {
                int shapeWidth = getContext().getResources()
                        .getDimensionPixelSize(R.dimen.acch_shape_preference_width);
                @SuppressWarnings("IntegerDivisionInFloatingPointContext")
                float radius = shapeWidth / 2;

                colorPreview.setImageResource(R.drawable.acch_circle);

                // Update color
                colorPreview.getDrawable()
                        .setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));

                // Bitmap to crop for background
                Bitmap draughtboard = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.acch_draughtboard);
                //noinspection SuspiciousNameCombination
                draughtboard = getRoundedCroppedBitmap(draughtboard, shapeWidth, shapeWidth, radius);
                backgroundPreview.setImageBitmap(draughtboard);

                colorPreview.invalidate();
                backgroundPreview.invalidate();
            }
            //setSummary(summaryPreference);
        } catch (Exception e) {
            Log.e("CustomColorDialogPreferenceX.setColorInWidget", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return Color.parseColor(ta.getString(index));
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // Get the persistent value and correct it for the minimum value.
        //PPApplication.logE("CustomColorDialogPreferenceX.onSetInitialValue", "defaultValue="+defaultValue);
        if (defaultValue != null) {
            value = getPersistedInt((int) defaultValue);
            this.defaultValue = (int)defaultValue;
        }
        else {
            value = getPersistedInt(0xFFFFFFFF);
            this.defaultValue = 0xFFFFFFFF;
        }
        //PPApplication.logE("CustomColorDialogPreferenceX.onSetInitialValue", "value="+value);

        setColorInWidget();
        setSummaryCCDP(value);
    }

    void persistValue() {
        if (shouldPersist()) {
            persistInt(value);
            setColorInWidget();
            setSummaryCCDP(value);
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            value = getPersistedInt(defaultValue);
            setSummaryCCDP(value);
        }
        savedInstanceState = false;
    }

    private void setSummaryCCDP(int value)
    {
        setSummary(ChromaUtil.getFormattedColorString(value, false));
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            // save is not needed, is already saved persistent
            return superState;
        }*/

        final CustomColorDialogPreferenceX.SavedState myState = new CustomColorDialogPreferenceX.SavedState(superState);
        if (fragment != null) {
            myState.value = fragment.chromaColorView.getCurrentColor();
            myState.defaultValue = defaultValue;
        }
        else {
            myState.value = value;
            myState.defaultValue = defaultValue;
        }
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            int value = this.value;
            if (fragment != null)
                fragment.chromaColorView.setCurrentColor(value);
            setSummaryCCDP(value);
            return;
        }

        // restore instance state
        CustomColorDialogPreferenceX.SavedState myState = (CustomColorDialogPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        if (fragment != null)
            fragment.chromaColorView.setCurrentColor(value);
        setSummaryCCDP(value);
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        int value;
        int defaultValue;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readInt();
            defaultValue = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeInt(value);
            dest.writeInt(defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<SavedState> CREATOR =
                new Creator<CustomColorDialogPreferenceX.SavedState>() {
                    public CustomColorDialogPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new CustomColorDialogPreferenceX.SavedState(in);
                    }
                    public CustomColorDialogPreferenceX.SavedState[] newArray(int size)
                    {
                        return new CustomColorDialogPreferenceX.SavedState[size];
                    }

                };

    }

}
