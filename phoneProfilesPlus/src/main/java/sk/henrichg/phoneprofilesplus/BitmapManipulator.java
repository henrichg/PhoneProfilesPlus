package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class BitmapManipulator {

    public static Bitmap resampleBitmap(String bitmapFile, int width, int height, Context context)
    {
        if (bitmapFile == null)
            return null;

        if (!Permissions.checkGallery(context))
            return null;

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(bitmapFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        File f = new File(bitmapFile);
        if (f.exists())
        {
            // first decode with inJustDecodeDpunds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(bitmapFile, options);

            int rotate = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    rotate = 90;
                    break;
            }

            int rotatedWidth, rotatedHeight;
            if (rotate == 90 || rotate == 270) {
                rotatedWidth = height;
                rotatedHeight = width;
            } else {
                rotatedWidth = width;
                rotatedHeight = height;
            }

            // calaculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, rotatedWidth, rotatedHeight);

            // decode bitmap with inSampleSize
            options.inJustDecodeBounds = false;
            Bitmap decodedSampleBitmap = BitmapFactory.decodeFile(bitmapFile, options);
            decodedSampleBitmap = rotateBitmap(decodedSampleBitmap, orientation);

            return decodedSampleBitmap;
        }
        else
            return null;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                Log.d("BitmapManipulator.rotateBitmap","ORIENTATION_NORMAL");
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                Log.d("BitmapManipulator.rotateBitmap","ORIENTATION_FLIP_HORIZONTAL");
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                Log.d("BitmapManipulator.rotateBitmap","ORIENTATION_ROTATE_180");
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                Log.d("BitmapManipulator.rotateBitmap","ORIENTATION_FLIP_VERTICAL");
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                Log.d("BitmapManipulator.rotateBitmap","ORIENTATION_TRANSPOSE");
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                Log.d("BitmapManipulator.rotateBitmap","ORIENTATION_ROTATE_90");
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                Log.d("BitmapManipulator.rotateBitmap","ORIENTATION_TRANSVERSE");
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                Log.d("BitmapManipulator.rotateBitmap","ORIENTATION_ROTATE_270");
                matrix.setRotate(-90);
                break;
            default:
                Log.d("BitmapManipulator.rotateBitmap","default");
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap resampleResource(Resources resources, int bitmapResource, int width, int height)
    {
        // first decode with inJustDecodeDpunds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, bitmapResource, options);
        // calaculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, width, height);
        // decode bitmap with inSampleSize
        options.inJustDecodeBounds = false;
        Bitmap decodedSampleBitmap = BitmapFactory.decodeResource(resources, bitmapResource, options);

        return decodedSampleBitmap;
    }

    public static Bitmap recolorBitmap(Bitmap bitmap, int color, Context context)
    {
        if (bitmap == null)
            return null;

        Bitmap colorBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                                                        bitmap.getHeight(),
                                                        bitmap.getConfig());
                                                        //Config.ARGB_8888);

        Canvas canvas = new Canvas(colorBitmap);
        Paint paint = new Paint();
        Matrix matrix = new Matrix();

        ColorFilter filter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        paint.setColorFilter(filter);
        canvas.drawBitmap(bitmap, matrix, paint);

        return colorBitmap;
    }

    public static Bitmap monochromeBitmap(Bitmap bitmap, int value, Context context) {
        int color = Color.argb(0xFF, value, value, value);
        return recolorBitmap(bitmap, color, context);
    }

    public static Drawable tintDrawableByColor(Drawable drawable, int color) {
        Drawable wrapDrawable = DrawableCompat.wrap(drawable);
        //DrawableCompat.setTintMode(wrapDrawable,  PorterDuff.Mode.DST_ATOP);
        DrawableCompat.setTint(wrapDrawable, color);
        return wrapDrawable;
    }

    public static Drawable tintDrawableByValue(Drawable drawable, int value) {
        int color  = Color.argb(0xFF, value, value, value);
        return tintDrawableByColor(drawable, color);
    }

    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static Bitmap grayscaleBitmap(Bitmap bitmap)
    {
        if (bitmap == null)
            return null;

        Bitmap monochromeBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(),
                bitmap.getConfig());
        Canvas canvas = new Canvas(monochromeBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0.0f);
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        Matrix matrix = new Matrix();
        canvas.drawBitmap(bitmap, matrix, paint);
        //ColorFilter filter = new PorterDuffColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
        //paint.setColorFilter(filter);
        //canvas.drawBitmap(monochromeBitmap, matrix, paint);

        /*
        float[] colorTransform = {
                0, 1f, 0, 0, 0,
                0, 0, 0f, 0, 0,
                0, 0, 0, 0f, 0,
                0, 0, 0, 1f, 0};

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0f); //Remove Colour
        colorMatrix.set(colorTransform); //Apply the Red

        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
        Paint paint = new Paint();
        paint.setColorFilter(colorFilter);

        Canvas canvas = new Canvas(monochromeBitmap);
        Matrix matrix = new Matrix();
        canvas.drawBitmap(bitmap, matrix, paint);
        */

        return monochromeBitmap;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        // raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth)
        {
            // calculate ratios of height and width to requested height an width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // choose the smalest ratio as InSamleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width
            inSampleSize = (heightRatio < widthRatio) ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }



}
