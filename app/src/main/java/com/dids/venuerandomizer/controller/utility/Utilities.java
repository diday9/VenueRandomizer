package com.dids.venuerandomizer.controller.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;

import com.dids.venuerandomizer.model.Category;
import com.dids.venuerandomizer.model.Venue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utilities {
    private static final int MAX_BITMAP_DIMENSION = 1024;

    public static int convertDPtoPixel(Context context, int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    public static String getPrimaryCategory(Venue venue) {
        for (Category category : venue.getCategories()) {
            if (category.isPrimary()) {
                return category.getName();
            }
        }
        return null;
    }

    public static String getAddress(Venue venue) {
        StringBuilder address = new StringBuilder();
        if (venue.getCity() != null && !venue.getCity().isEmpty()) {
            address.append(venue.getCity());
        }
        if (venue.getState() != null && !venue.getState().isEmpty()) {
            if (address.toString().isEmpty()) {
                address.append(venue.getState());
            } else {
                address.append(", ");
                address.append(venue.getState());
            }
        }
        return address.toString();
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(String path) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options,
                MAX_BITMAP_DIMENSION, MAX_BITMAP_DIMENSION);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static String getFilesDir(Context context) {
        File file = new File(context.getFilesDir().getAbsolutePath() + "/assets");
        //noinspection ResultOfMethodCallIgnored
        file.mkdirs();
        return file.getAbsolutePath();
    }

    public static Drawable getDrawableFromAsset(Context context, String filename) {
        Drawable drawable;
        try {
            InputStream ims = context.getAssets().open(filename);
            drawable = Drawable.createFromStream(ims, null);
        } catch (IOException ex) {
            drawable = null;
        }
        return drawable;
    }

    public static String getStringFromAsset(Context context, String filename) {
        StringBuilder buffer = new StringBuilder();
        InputStream inputStream;
        try {
            inputStream = context.getAssets().open(filename);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    public static Spanned createSpannedLink(String text, String link) {
        Spanned spanned;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            spanned = Html.fromHtml(String.format("<a href=%s>%s</a>", link, text),
                    Html.FROM_HTML_MODE_LEGACY);
        } else {
            //noinspection deprecation
            spanned = Html.fromHtml(String.format("<a href=%s>%s</a>", link, text));
        }
        return spanned;
    }
}
