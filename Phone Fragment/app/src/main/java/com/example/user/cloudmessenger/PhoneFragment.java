package com.example.user.cloudmessenger;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Rect;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class PhoneFragment extends Fragment implements View.OnTouchListener,SensorListener {
    Button b_1;
    Button b_2;
    Button b_3;
    Button b_4;
    Button b_5;
    Button b_6;
    Button b_7;
    Button b_8;
    Button b_9;
    Button b_0;
    Button b_h;
    Button b_s;
    ImageButton addcon;

    ImageButton butclr, btnOptions;
    EditText edt;
    ImageButton bcall;
    String texts, lastKey = "";
    LinearLayout keypadLayout;
    private RelativeLayout mFragmentView;
    private SensorManager sensorMgr;
    private long lastUpdate=0;
    private float x,y,z;
    private static final int SHAKE_THRESHOLD = 1000;
    private float last_x,last_y,last_z;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == getActivity().RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                // Handle successful scan
//                Toast.makeText(getActivity(), contents, Toast.LENGTH_LONG).show();
                String[] parts = contents.split("//");
                if (parts[0].equals("hybrid_contact")) {
                    List<String> emails = new ArrayList<String>();
                    List<String> phone = new ArrayList<String>();
                    List<String> name = new ArrayList<String>();
                    for (int i = 1; i < parts.length; i += 2) {
                        String part = parts[i];
                        String data = parts[i + 1];
                        if (part.equals("e")) {
                            emails.add(data);
                        } else if (part.equals("p")) {
                            phone.add(data);
                        } else if (part.equals("u")) {
                            name.add(data);
                        }

                    }
                    ////////////////////////////////////////
                    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

                    ops.add(ContentProviderOperation.newInsert(
                            ContactsContract.RawContacts.CONTENT_URI)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                            .build());

                    if (name.size() > 0) {
                        ops.add(ContentProviderOperation.newInsert(
                                ContactsContract.Data.CONTENT_URI)
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                .withValue(ContactsContract.Data.MIMETYPE,
                                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                .withValue(
                                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                        name.get(0)).build());
                    }
                    for (int i = 0; i < phone.size(); i++) {
                        ops.add(ContentProviderOperation.
                                newInsert(ContactsContract.Data.CONTENT_URI)
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                .withValue(ContactsContract.Data.MIMETYPE,
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                                        phone.get(i))
//                                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
//                                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                                .build());
                    }
                    for (int i = 0; i < emails.size(); i++) {
                        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                .withValue(ContactsContract.Data.MIMETYPE,
                                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.CommonDataKinds.Email.DATA,
                                        emails.get(i))
                                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                                .build());
                    }


                    try {
                        getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY,
                                ops);
                        Toast.makeText(getActivity().getBaseContext(), "Contact Added",
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity().getBaseContext(), "Exception: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                    ////////////////////////////////////////
                }
            } else if (resultCode == getActivity().RESULT_CANCELED) {
                // Handle cancel
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorMgr.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ListView call = (ListView) mFragmentView.findViewById(R.id.call);
        final List<RowItem> rowItems;
        final ArrayList<Date> dateList = new ArrayList<>();
        final ArrayList<String> numberList = new ArrayList<>();
        final ArrayList<String> durList = new ArrayList<>();
        final ArrayList<Integer> iconList = new ArrayList<>();


        ContentResolver cr = getActivity().getContentResolver();
        String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
        Uri callUri = Uri.parse("content://call_log/calls");
        Cursor managedCursor = cr.query(callUri, null, null, null, strOrder);
        if (managedCursor.getCount() > 0) {
            while (managedCursor.moveToNext() && numberList.size()<20) {
                String no = managedCursor.getString(managedCursor.getColumnIndex(CallLog.Calls.NUMBER));
                String s = getContactName(getActivity(), no);
                if (s == null)
                    numberList.add(no);
                else
                    numberList.add(s);
                int code = Integer.parseInt(managedCursor.getString(managedCursor.getColumnIndex(CallLog.Calls.TYPE)));
                switch (code) {
                    case CallLog.Calls.INCOMING_TYPE:
                        iconList.add(R.drawable.incoming);
                        break;
                    case CallLog.Calls.OUTGOING_TYPE:
                        iconList.add(R.drawable.outgoing);
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        iconList.add(R.drawable.misscall);
                        break;
                }
                dateList.add((new Date(Long.valueOf(managedCursor.getString(managedCursor.getColumnIndex(CallLog.Calls.DATE))))));
                durList.add(managedCursor.getString(managedCursor.getColumnIndex(CallLog.Calls.DURATION)));
            }
        }
        managedCursor.close();

        rowItems = new ArrayList<RowItem>();
        for (int i = 0; i < numberList.size(); i++) {
            RowItem item = new RowItem(iconList.get(i), numberList.get(i), dateList.get(i), durList.get(i));
            rowItems.add(item);
        }
        CustomList adapter = new CustomList(getActivity(),
                R.layout.adapter_layout, rowItems);
        call.setAdapter(adapter);

        sensorMgr.registerListener(this,
                SensorManager.SENSOR_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_GAME);
    }

    public static String getContactName(Context context, String ph) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(ph));
        Cursor c = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (c == null) {
            return null;
        }
        String cn = null;
        if (c.moveToFirst()) {
            cn = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        if (c != null && !c.isClosed()) {
            c.close();
            ;
        }
        return cn;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mFragmentView = (RelativeLayout) inflater.inflate(
                R.layout.activity_main, container, false);

        sensorMgr = (SensorManager) getActivity().getSystemService(getActivity().SENSOR_SERVICE);

        edt = (EditText) mFragmentView.findViewById(R.id.edtmainbox);
        edt.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                Context context = getActivity();
                SharedPreferences sharedPref = context.getSharedPreferences(
                        "Contact_backup", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("Contact", edt.getText().toString());
                editor.commit();
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });
        keypadLayout = (LinearLayout) mFragmentView.findViewById(R.id.layoutKeypad);
        keypadLayout.setOnTouchListener(this);
        btnOptions = (ImageButton) mFragmentView.findViewById(R.id.btnOptions);
        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(getActivity().getBaseContext(), btnOptions);
                menu.getMenuInflater().inflate(R.menu.menu_options, menu.getMenu());
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        switch (id) {
                            case R.id.action_showQRCode:
                                Intent i = new Intent(getActivity().getBaseContext(), MyContact.class);
                                startActivity(i);
                                break;
                            case R.id.action_getQRCode:
                                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                                startActivityForResult(intent, 0);
                                break;
                        }
                        return true;
                    }
                });
                menu.show();
            }
        });



        butclr = (ImageButton) mFragmentView.findViewById(R.id.butclr);
        butclr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = edt.getText().toString();
                String newNumber = new String();
                int cursorPosition = edt.getSelectionStart();
                if (cursorPosition == 0 && phoneNumber.length() > 0)
                    edt.setText(phoneNumber.substring(0, phoneNumber.length() - 1));
                if (cursorPosition > 0) {
                    newNumber = phoneNumber.substring(0, cursorPosition - 1) + phoneNumber.substring(cursorPosition, phoneNumber.length());
                    edt.setText(newNumber);
                    edt.setSelection(cursorPosition - 1);
                }
//                Toast.makeText(PhoneFragment.this,"position  "+cursorPosition,Toast.LENGTH_LONG).show();

            }
        });
        butclr.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                edt.setText("");
                return true;
            }
        });


        b_1 = (Button) mFragmentView.findViewById(R.id.butone);

        b_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texts = edt.getText().toString();
                texts = texts + "1";
                edt.setText(texts);
            }
        });


        b_2 = (Button) mFragmentView.findViewById(R.id.buttwo);

        b_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texts = edt.getText().toString();
                texts = texts + "2";
                edt.setText(texts);

            }
        });

        b_3 = (Button) mFragmentView.findViewById(R.id.butthree);

        b_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texts = edt.getText().toString();
                texts = texts + "3";
                edt.setText(texts);

            }
        });

        b_4 = (Button) mFragmentView.findViewById(R.id.butfour);

        b_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texts = edt.getText().toString();
                texts = texts + "4";
                edt.setText(texts);

            }
        });
        b_5 = (Button) mFragmentView.findViewById(R.id.butfive);

        b_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texts = edt.getText().toString();
                texts = texts + "5";
                edt.setText(texts);

            }
        });

        b_6 = (Button) mFragmentView.findViewById(R.id.butsix);

        b_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texts = edt.getText().toString();
                texts = texts + "6";
                edt.setText(texts);

            }
        });
        b_7 = (Button) mFragmentView.findViewById(R.id.butseven);

        b_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texts = edt.getText().toString();
                texts = texts + "7";
                edt.setText(texts);

            }
        });

        b_8 = (Button) mFragmentView.findViewById(R.id.buteight);

        b_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texts = edt.getText().toString();
                texts = texts + "8";
                edt.setText(texts);

            }
        });

        b_9 = (Button) mFragmentView.findViewById(R.id.butnine);

        b_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texts = edt.getText().toString();
                texts = texts + "9";
                edt.setText(texts);

            }
        });


        b_0 = (Button) mFragmentView.findViewById(R.id.butzero);

        b_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texts = edt.getText().toString();
                texts = texts + "0";
                edt.setText(texts);

            }
        });


//        b_1.setOnTouchListener(this);
//        b_2.setOnTouchListener(this);
//        b_3.setOnTouchListener(this);
//        b_4.setOnTouchListener(this);
//        b_5.setOnTouchListener(this);
//        b_6.setOnTouchListener(this);
//        b_7.setOnTouchListener(this);
//        b_8.setOnTouchListener(this);
//        b_9.setOnTouchListener(this);
//        b_0.setOnTouchListener(this);

        b_h = (Button) mFragmentView.findViewById(R.id.buthash);

        b_h.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texts = edt.getText().toString();
                texts = texts + "#";
                edt.setText(texts);

            }
        });
        b_s = (Button) mFragmentView.findViewById(R.id.butstar);

        b_s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texts = edt.getText().toString();
                texts = texts + "*";
                edt.setText(texts);

            }
        });


        b_1.setClickable(false);
        b_2.setClickable(false);
        b_3.setClickable(false);
        b_4.setClickable(false);
        b_5.setClickable(false);
        b_6.setClickable(false);
        b_7.setClickable(false);
        b_8.setClickable(false);
        b_9.setClickable(false);
        b_0.setClickable(false);
        b_s.setClickable(false);
        b_h.setClickable(false);

        addcon = (ImageButton) mFragmentView.findViewById(R.id.butcontactadd);
        addcon.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          String phonenumber = edt.getText().toString();
//
//                                          Intent i = new Intent(Intent.ACTION_INSERT);
//                                          i.putExtra(ContactsContract.CommonDataKinds.Phone.NUMBER, phonenumber);
//                                          i.setType(ContactsContract.Contacts.CONTENT_TYPE);
//                                          startActivity(i);
                                          Intent contactIntent = new Intent(Intent.ACTION_INSERT);
                                          contactIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                                          ArrayList<ContentValues> data = new ArrayList<ContentValues>();

//Filling data with phone numbers

                                              ContentValues row = new ContentValues();
                                              row.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                                              row.put(ContactsContract.CommonDataKinds.Phone
                                                      .NUMBER,phonenumber);
                                              row.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
                                              data.add(row);

                                          contactIntent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);
                                          startActivity(contactIntent);

                                      }
                                  }
        );


        bcall = (ImageButton) mFragmentView.findViewById(R.id.butcall);
        bcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt.getText().toString().length() > 0) {
                    String uri = "tel:" + edt.getText().toString();
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                }
            }
        });
        return mFragmentView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
//        Log.v("TOUCH",String.valueOf( v.getId()));
        int x = (int) event.getX();
        int y = (int) event.getY();
//
//        Log.v("OnTouchDemo", "X==" + String.valueOf(x) + " Y==" + String.valueOf(y));
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_MOVE:
                Button btnValue = getButton(event.getX(), event.getY());
                if(btnValue == null)
                    return false;
                String pressedKey = btnValue.getText().toString();
                if (!lastKey.equals(pressedKey)) {
                    texts = edt.getText().toString();
                    texts = texts + pressedKey;
                    lastKey = pressedKey;
                    edt.setText(texts);
                    Vibrator vb = (Vibrator) mFragmentView.getContext().getSystemService(Context
                            .VIBRATOR_SERVICE);
                    long[] pattern = {0, 100, 200};
                    vb.vibrate(pattern,-1);
                }
//                break;
//        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            lastKey = "";
        }
        return true;
    }

    private Button getButton(float x, float y) {
        Button b;
        b = b_1;
        if (isBound(b, x, y,0)) {
            return b;
        }
        b = b_2;
        if (isBound(b, x, y,0)) {
            return b;
        }
        b = b_3;
        if (isBound(b, x, y,0)) {
            return b;
        }
        b = b_4;
        if (isBound(b, x, y,1)) {
            return b;
        }
        b = b_5;
        if (isBound(b, x, y,1)) {
            return b;
        }
        b = b_6;
        if (isBound(b, x, y,1)) {
            return b;
        }
        b = b_7;
        if (isBound(b, x, y,2)) {
            return b;
        }
        b = b_8;
        if (isBound(b, x, y,2)) {
            return b;
        }
        b = b_9;
        if (isBound(b, x, y,2)) {
            return b;
        }
        b = b_s;
        if (isBound(b, x, y,3)) {
            return b;
        }
        b = b_0;
        if (isBound(b, x, y,3)) {
            return b;
        }
        b = b_h;
        if (isBound(b, x, y,3)) {
            return b;
        }
        Log.v("MOUSE", "No value");
        return null;
    }

    static boolean isBound(Button b,float x,float y,int i) {
        float bx, by, bw, bh;
        bx = b.getX();
        bw = b.getWidth();
        bh = b.getHeight();
        by = i * bh +(i*10);
//        String msg = "btn x:"+String.valueOf(bx)+" ,y:"+String.valueOf(by)+"  |||" +
//                " Mouse x:"+x+" ,y:"+y+" ";
//        Log.v("MOUSE", msg);
        if (bx < x && by < y) {
            if (x < (bx + bw) && y < (by + bh)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onSensorChanged(int sensor, float[] values) {
        if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                x = values[SensorManager.DATA_X];
                y = values[SensorManager.DATA_Y];
                z = values[SensorManager.DATA_Z];

                float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    //if (edt.getText().equals("")) {
                        Context context = getActivity();
                        SharedPreferences sharedPref = context.getSharedPreferences(
                                "Contact_backup", Context.MODE_PRIVATE);
                        edt.setText(sharedPref.getString("Contact",""));
                    //}
                    Log.d("sensor", "shake detected w/ speed: " + speed);
//                    Toast.makeText(getActivity(), "shake detected w/ speed: " + speed,
//                            Toast.LENGTH_SHORT).show();
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(int sensor, int accuracy) {

    }
}
