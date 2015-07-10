package com.spj.messenger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by Stenal P Jolly on 11-Apr-15.
 */
public class CommonFuntions {

    public static String encodeTobase64(Bitmap image) {
        if (image == null)
            return "";
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.e("LOOK", imageEncoded);
        return imageEncoded;
    }

    public static Bitmap decodeBase64(String input) {
        try {
            if (input == null)
                return null;
            if (input.length() > 10) {
                byte[] decodedByte = Base64.decode(input, 0);
                return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
            }
        } catch (IllegalArgumentException e) {
            Log.v("decodeBase64", e.toString());
        }
        return null;
    }

    public static void saveToFile(Spanned msg) {

        Bitmap bitmap = decodeBase64(Html.toHtml(msg));
        try{
            String path = Environment.getExternalStorageDirectory().toString()+ "/hybrid " +
                    "images/"+"img_" + String.valueOf(System.currentTimeMillis())+".png";
            File file = new File(path);
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();}
        catch (Exception e) {
            e.printStackTrace();
            Log.i("File Save Error", e.toString());
        }
    }
}
