package com.lekhavarghese.contactlist.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lekhavarghese.contactlist.R;

import java.util.ArrayList;

/**
 * Created by Stenal P Jolly on 17-Apr-15.
 */
public class ContactDialogFragment extends DialogFragment {

    ArrayList<String> phoneNumbers = new ArrayList<>();
    String id;
    private Context mContext;
    private MainActivity activity;

    public ContactDialogFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout v = (LinearLayout)inflater.inflate(R.layout.contactdialog, container,false);
        if (mContext == null) {
            return v;
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup
                .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < phoneNumbers.size(); i++) {

            final TextView txtPhoneNumber = new TextView(mContext);
            txtPhoneNumber.setPadding(20,10,0,10);
            txtPhoneNumber.setTextSize(17f);
            txtPhoneNumber.setLayoutParams(layoutParams);
            txtPhoneNumber.setText(phoneNumbers.get(i));

            txtPhoneNumber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    String uri = "tel:" + txtPhoneNumber.getText().toString() ;
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                }
            });

            v.addView(txtPhoneNumber);
        }

        Button btnEdit = new Button(mContext);
        btnEdit.setLayoutParams(layoutParams);
        Button btnDelete = new Button(mContext);
        btnDelete.setLayoutParams(layoutParams);
        btnEdit.setText("Edit Contact");
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts
                            .CONTENT_URI, Long.parseLong(id));
                    Intent i = new Intent(Intent.ACTION_EDIT);
                    i.setData(contactUri);
                    i.setData(contactUri);
//                    intent.putExtra("finishActivity",true);
//                    intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(i, Integer.parseInt(id));
                }catch(Exception e) {
                    Log.v("Contact Edit", e.toString());
                }
                dismiss();

            }
        });
        btnDelete.setText("Delete Contact");
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList ops = new ArrayList();
                final ContentResolver cr = mContext.getContentResolver();
                ops.add(ContentProviderOperation
                        .newDelete(ContactsContract.RawContacts.CONTENT_URI)
                        .withSelection(
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                        + " = ?",
                                new String[] { id })
                        .build());
                AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                alertDialog.setTitle("Delete This Contact!");
                alertDialog.setMessage("Are you Sure you want to delete this contact?");
                alertDialog.setButton("Delete", new DialogInterface.OnClickListener() {    //
                // DEPRECATED
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            cr.applyBatch(ContactsContract.AUTHORITY, ops);
                            ops.clear();
                            activity.getContact();
                        } catch (OperationApplicationException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();

                        } catch (RemoteException e) {
                            // System.out.println(" length :"+i);
                        }
                        return;
                    } });
                alertDialog.setButton2("Cancel", (DialogInterface.OnClickListener)null)
                ;
                 // DEPRECATED
                try {
                    alertDialog.show();
                }catch(Exception e) {
                    //              Log.e(THIS_FILE, "error while trying to show deletion yes/no dialog");
                }
                dismiss();
            }
        });

        v.addView(btnEdit);
        v.addView(btnDelete);

        return v;
    }

    public void setData(String id,Context context,MainActivity activity) {

        this.activity = activity;
        // Build the Uri to query to table
//        Uri myPhoneUri = Uri.withAppendedPath(
//                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, id);
        mContext = context;
        // Query the table
//        Cursor phoneCursor = mContext.getContentResolver().query(myPhoneUri, null, null, null, null);
        Cursor pCur = mContext.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{id}, null);

        phoneNumbers.clear();
        this.id = id;
        while (pCur.moveToNext()) {
            phoneNumbers.add(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone
                    .NUMBER)));
        }
        pCur.close();

    }
}
