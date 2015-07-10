package com.lekhavarghese.contactlist.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.lekhavarghese.contactlist.R;
import com.shamanland.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import static android.provider.ContactsContract.CommonDataKinds.Email;


public class MainActivity extends ActionBarActivity {

    Cursor cr = null;
    ArrayList<String> nameList = new ArrayList<>();
    Uri mContactUri;
    Context mContext;
    ListView listView;
    private ArrayList<String> idList = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        listView = (ListView) findViewById(R.id.lsContacts);
        adapter = new ArrayAdapter(MainActivity.this,
                android.R.layout.simple_list_item_1, nameList);
        listView.setAdapter(adapter);

        //ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, nameList);
        // lvContacts.setAdapter(stringArrayAdapter);
        // lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ContactDialogFragment newFragment = new ContactDialogFragment();
                newFragment.setData(idList.get(position),mContext,MainActivity.this);
                newFragment.show(getFragmentManager(), "dialog");

            }
        });
        FloatingActionButton ds = (FloatingActionButton) findViewById(R.id.fabNew);
        ds.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {
                                      Intent i = new Intent(Intent.ACTION_INSERT);
                                      i.setType(ContactsContract.Contacts.CONTENT_TYPE);
                                      startActivity(i);

                                  }
                              }
        );
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void getContact(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                nameList.clear();
                idList.clear();
                ContentResolver cs = getContentResolver();
                try {
//                    cr = cs.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC ");
                    cr = cs.query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
                            ContactsContract.Contacts.DISPLAY_NAME + " ASC ");
                    cr.moveToFirst();
                    if (cr.getCount() > 0) {
                        while (cr.moveToNext()) {

                            String id = cr.getString(cr.getColumnIndex(ContactsContract.Contacts._ID));
                            idList.add(id);
                            nameList.add(cr.getString(cr.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                        }
                    }

                } catch (Exception e) {
                    Log.v("Error Lekha", e.toString());
                    e.printStackTrace();
                } finally {
                    if (cr != null) {
                        cr.close();
                    }
                }
//                Toast.makeText(MainActivity.this, String.valueOf(nameList.size()),
//                        Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getContact();
    }
}
