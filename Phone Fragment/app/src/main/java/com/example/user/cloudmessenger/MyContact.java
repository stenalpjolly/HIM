package com.example.user.cloudmessenger;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.List;


public class MyContact extends Activity implements  LoaderManager.LoaderCallbacks<Cursor>{

    Context mContext;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_contact);
        mContext = this;
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arguments) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath( ContactsContract.Profile.CONTENT_URI,ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
                ProfileQuery.PROJECTION,

                //Don't select anything here null will return all available fields

                null,
                null,
                null);
//        return new CursorLoader(this,
//                // Retrieve data rows for the device user's 'profile' contact.
//                Uri.withAppendedPath(
//                        ContactsContract.Profile.CONTENT_URI,
//                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
//                ProfileQuery.PROJECTION,
//
//                // Select only email addresses.
//                ContactsContract.Contacts.Data.MIMETYPE + " = ?",
//                new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE},
//
//                // Show primary email addresses first. Note that there won't be
//                // a primary email address if the user hasn't specified one.
//                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        List<String> phone = new ArrayList<String>();
        List<String> name = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String type = cursor.getString(ProfileQuery.ADDRESS);
            if (type.equals("vnd.android.cursor.item/email_v2")) {
                emails.add(cursor.getString(ProfileQuery.NUMBER));
            }else if (type.equals("vnd.android.cursor.item/phone_v2")) {
                phone.add(cursor.getString(ProfileQuery.NUMBER));
            }else if (type.equals("vnd.android.cursor.item/name")) {
                name.add(cursor.getString(ProfileQuery.NUMBER));
            }

//            TypeName=cursor.getString(ProfileQuery.ADDRESS);//this will give you field name
//            Data=cursor.getString(ProfileQuery.NUMBER);//this will give you field data

            // Potentially filter on ProfileQuery.IS_PRIMARY
            cursor.moveToNext();
        }
        String finalMsg = "hybrid_contact";
        for (int i = 0; i < name.size(); i++) {
            finalMsg += "//u//" + name.get(i);
        }
        for (int i = 0; i < phone.size(); i++) {
            finalMsg += "//p//" + phone.get(i);
        }
        for (int i = 0; i < emails.size(); i++) {
            finalMsg += "//e//" + emails.get(i);
        }
//        Toast.makeText(mContext,finalMsg,Toast.LENGTH_LONG).show();
        //Encode with a QR Code image
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(finalMsg,
                null,
                Contents.Type.TEXT,
                BarcodeFormat.QR_CODE.toString(),
                500);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            ImageView myImage = (ImageView) findViewById(R.id.imageView);
            myImage.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.Contacts.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Email.ADDRESS  ,
                ContactsContract.CommonDataKinds.Email.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Organization.DATA3,

        };

        int ADDRESS = 0;
        int NUMBER = 1;
    }

}
