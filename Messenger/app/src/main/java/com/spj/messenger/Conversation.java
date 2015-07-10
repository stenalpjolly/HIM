package com.spj.messenger;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.spj.messenger.EmoticonsGridAdapter.KeyClickListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;


public class Conversation extends ActionBarActivity implements KeyClickListener {

    //    private static final int NO_OF_EMOTICONS = 54;
    private static final int NO_OF_EMOTICONS = 185;
    private static final int MIN_DISTANCE = 100;
    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;
    String vUsername, vPhoneNumber, vEmailId, vRegId;
    ImageButton btnSend;
    ImageButton btnCamera;
    EditText txtMessage;
    ListView lvMessages;
    String myRegID, myPhoneNumber;
    Context mContext;
    int MaxIdReceived = 0;
    AsyncTask msgUpdateAsyncTask;
    Boolean flag = true;
    /**
     * Checking keyboard height and keyboard visibility
     */
    int previousHeightDiffrence = 0;
    private float x1, x2;
    private String SQL_MsgSendRequest, SQL_MsgGetRequest;
    private MessageAdapter messageArrayAdapter;
    private ArrayList<Message> messages;
    private View popUpView;
    private LinearLayout emoticonsCover;
    private PopupWindow popupWindow;
    private int keyboardHeight;
    private EditText content;
    private LinearLayout parentLayout;
    private Bitmap[] emoticons;
    private Uri mImageCaptureUri;
    private Bitmap photo;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case PICK_FROM_CAMERA:
//                mImageCaptureUri = data.getData();
                doCrop();

                break;

            case PICK_FROM_FILE:
                mImageCaptureUri = data.getData();

                doCrop();

                break;

            case CROP_FROM_CAMERA:
                Bundle extras = data.getExtras();

                if (extras != null) {
                    photo = extras.getParcelable("data");
                    addNewMessage(photo);
                }

                File f = new File(mImageCaptureUri.getPath());

                if (f.exists()) f.delete();

                break;

        }
    }

    private void doCrop() {

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(mImageCaptureUri, "image/*");

//        intent.putExtra("outputX", 200);
//        intent.putExtra("outputY", 200);
//        intent.putExtra("aspectX", 1);
//        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);

        startActivityForResult(intent, CROP_FROM_CAMERA);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_backup);
        mContext = this;
        SQL_MsgSendRequest = getResources().getString(R.string.SQL_MsgSend_Request);
        SQL_MsgGetRequest = getResources().getString(R.string.SQL_MsgGet_Request);

        getWindow().setBackgroundDrawableResource(R.drawable.conv_bg);

        vUsername = getIntent().getExtras().getString("name");
        vPhoneNumber = getIntent().getExtras().getString("phone_number");
        vRegId = getIntent().getExtras().getString("regId");
        vEmailId = getIntent().getExtras().getString("emailId");

        SharedPreferences sharedPreferences = getSharedPreferences("hybrid_messenger", 0);

        myRegID = sharedPreferences.getString("registration_id", "");
        myPhoneNumber = sharedPreferences.getString("phoneNumber", "");

//        Toast.makeText(mContext, myRegID, Toast.LENGTH_SHORT).show();
//        Toast.makeText(mContext, myPhoneNumber, Toast.LENGTH_SHORT).show();

        //////////////////////////////////
        emoticonsCover = (LinearLayout) findViewById(R.id.footer_for_emoticons);

        popUpView = getLayoutInflater().inflate(R.layout.emoticons_popup, null);

        parentLayout = (LinearLayout) findViewById(R.id.layout_conversation);

        readEmoticons();
        enablePopUpView();
        checkKeyboardHeight(parentLayout);
        enableFooterView();

        //////////////////////////////////
        btnSend = (ImageButton) findViewById(R.id.btnMsgSend);
        btnCamera = (ImageButton) findViewById(R.id.btnMsgCamera);
        txtMessage = (EditText) findViewById(R.id.txtMsgMessage);
        lvMessages = (ListView) findViewById(R.id.lvConversations);
        /////////////////////////////////

        final String[] items = new String[]{"Take from camera", "Select from gallery"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, items);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select Image");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) { //pick from camera
                if (item == 0) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                            "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));

                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

                    try {
                        intent.putExtra("return-data", true);

                        startActivityForResult(intent, PICK_FROM_CAMERA);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                } else { //pick from file
                    Intent intent = new Intent();

                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
                }
            }
        });

        final AlertDialog dialog = builder.create();

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        //////////////////////////////////

        getSupportActionBar().setTitle(vUsername);


        //////////////////////////////////
        lvMessages.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (popupWindow.isShowing())
                    popupWindow.dismiss();
                return false;
            }
        });

        // Defining default height of keyboard which is equal to 230 dip
        final float popUpheight = 260;
        changeKeyboardHeight((int) popUpheight);

        // Showing and Dismissing pop up on clicking emoticons button
        ImageView emoticonsButton = (ImageView) findViewById(R.id.emoticons_button);
        emoticonsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                InputMethodManager keyboard = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                if (!popupWindow.isShowing()) {

                    popupWindow.setHeight(keyboardHeight);

                    keyboard.hideSoftInputFromWindow(txtMessage.getWindowToken(), 0);

//                    if (isKeyBoardVisible) {
//                        emoticonsCover.setVisibility(LinearLayout.GONE);
//
//                    } else {
                    emoticonsCover.setVisibility(LinearLayout.VISIBLE);
//                    }
                    popupWindow.showAtLocation(parentLayout, Gravity.BOTTOM, 0, 0);

                } else {
                    popupWindow.dismiss();
                    keyboard.showSoftInputFromInputMethod(txtMessage.getWindowToken(), 0);
                }

            }
        });
        //////////////////////////////////
        txtMessage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                popupWindow.dismiss();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        float deltaX = x2 - x1;
                        if ((-deltaX) > MIN_DISTANCE)
                            txtMessage.setText("");
                        if (deltaX > MIN_DISTANCE)
                            btnSend.performClick();
                        break;
                }
                return false;
            }
        });

        messages = new ArrayList<>();
        DbHelper dbHelper = new DbHelper(mContext);
        ArrayList<Message> messageArrayList = dbHelper.getAllMessage(emoticons, vPhoneNumber);
        messages.addAll(messageArrayList);
//        Toast.makeText(mContext,"Total Message : "+String.valueOf(messageArrayList.size()),Toast.LENGTH_SHORT).show();

        messageArrayAdapter = new MessageAdapter(this, messages);
        lvMessages.setAdapter(messageArrayAdapter);

        lvMessages.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(mContext, messageArrayAdapter.getItem(position).getvMsgType(),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtMessage.getText().length() > 0)
                    addNewMessage(txtMessage.getText());
                txtMessage.setText("");
            }
        });


        for (int i = 0; i < messages.size(); i++) {
            Message temp = messages.get(i);
            String id = temp.getvMsgId();
            try {
                int tempVal = Integer.parseInt(id);
                if (tempVal > MaxIdReceived) {
                    MaxIdReceived = tempVal;
                }
            } catch (NumberFormatException e) {
                Log.v("Num_Convrn_Error1", e.toString());
            }
        }
        updateAllMsg();


    }

    /**
     * Reading all emoticons in local cache
     */
    private void readEmoticons() {

        emoticons = new Bitmap[NO_OF_EMOTICONS];
        for (short i = 0; i < NO_OF_EMOTICONS; i++) {
            emoticons[i] = getImage((i + 1) + ".png");
        }

    }

    /**
     * Enabling all content in footer i.e. post window
     */
    private void enableFooterView() {

        content = (EditText) findViewById(R.id.txtMsgMessage);
        content.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (popupWindow.isShowing()) {

                    popupWindow.dismiss();

                }

            }
        });

    }

    /**
     * Overriding onKeyDown for dismissing keyboard on key down
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void checkKeyboardHeight(final View parentLayout) {

        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        Rect r = new Rect();
                        parentLayout.getWindowVisibleDisplayFrame(r);

                        int screenHeight = parentLayout.getRootView()
                                .getHeight();
                        int heightDifference = screenHeight - (r.bottom);

                        if (previousHeightDiffrence - heightDifference > 50) {
                            popupWindow.dismiss();
                        }

                        previousHeightDiffrence = heightDifference;
                        if (heightDifference > 100) {

                            changeKeyboardHeight(heightDifference);

                        }

                    }
                });

    }

    /**
     * change height of emoticons keyboard according to height of actual
     * keyboard
     *
     * @param height minimum height by which we can make sure actual keyboard is
     *               open or not
     */
    private void changeKeyboardHeight(int height) {

        if (height > 100) {
            keyboardHeight = height;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight);
            emoticonsCover.setLayoutParams(params);
        }

    }

    /**
     * Defining all components of emoticons keyboard
     */
    private void enablePopUpView() {

        ViewPager pager = (ViewPager) popUpView.findViewById(R.id.emoticons_pager);
        pager.setOffscreenPageLimit(3);

        ArrayList<String> paths = new ArrayList<>();

        for (short i = 1; i <= NO_OF_EMOTICONS; i++) {
            paths.add(i + ".png");
        }

        EmoticonsPagerAdapter adapter = new EmoticonsPagerAdapter(Conversation.this, paths, this);
        pager.setAdapter(adapter);

        // Creating a pop window for emoticons keyboard
        popupWindow = new PopupWindow(popUpView, ViewGroup.LayoutParams.MATCH_PARENT,
                (int) keyboardHeight, false);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                emoticonsCover.setVisibility(LinearLayout.GONE);
            }
        });
    }

    /**
     * For loading smileys from assets
     */
    private Bitmap getImage(String path) {
        AssetManager mngr = getAssets();
        InputStream in = null;
        try {
            in = mngr.open("emoticons/" + path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bitmap temp = BitmapFactory.decodeStream(in, null, null);
        return temp;
    }

    private void addNewMessage(Spanned msg) {
        final String time = Calendar.getInstance().getTime().toString();

        Calendar calender = Calendar.getInstance();
        Integer year = calender.get(Calendar.YEAR);
        Integer monthOfYear = calender.get(Calendar.MONTH) + 1;
        Integer dayOfMonth = calender.get(Calendar.DAY_OF_MONTH);

        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        String currentDateTimeString = sdf.format(d);

        final Message message = new Message(this);

        message.setvMsg(msg);
        message.setvFrom(myPhoneNumber);
        message.setvTo(vPhoneNumber);
        message.setvDate(dayOfMonth.toString() + "/" + monthOfYear.toString() + "/" + year.toString());
        message.setvTime(currentDateTimeString);
        messages.add(message);
        message.setvMsgState("N");

        messageArrayAdapter.notifyDataSetChanged();


        AsyncTask<Void, Void, String> msgSentAsyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... params) {

                String result = "";
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    String requestString = SQL_MsgSendRequest;
//                            "&regId=" + vRegId +
//                            "&message=" + DbHelper.getStringFromSpanned(message.getvMsg()) +
//                            "&msg_type=" + message.getvMsgType() +
//                            "&sender=" + myPhoneNumber +
//                            "&receiver=" + vPhoneNumber +
//                            "&receiver_type=S&time=" + time.replaceAll(" ", "%20") +
//                            "&msg_state=U";
//                    requestString = requestString.replaceAll(" ", "%20");
                    Log.v("SQL_SendMsg_REQUEST", requestString);
                    HttpPost request = new HttpPost(requestString);

                    List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                    postParameters.add(new BasicNameValuePair("regId", vRegId));
//                    postParameters.add(new BasicNameValuePair("message", DbHelper.getStringFromSpanned(message.getvMsg())));
                    postParameters.add(new BasicNameValuePair("message", DbHelper.getStringFromSpanned(message.getvMsg())));
                    postParameters.add(new BasicNameValuePair("msg_type", message.getvMsgType()));
                    postParameters.add(new BasicNameValuePair("sender", myPhoneNumber));
                    postParameters.add(new BasicNameValuePair("receiver", vPhoneNumber));
                    postParameters.add(new BasicNameValuePair("receiver_type", "S"));
                    postParameters.add(new BasicNameValuePair("time", time.replaceAll(" ", "%20")));
                    postParameters.add(new BasicNameValuePair("msg_state", "U"));
                    UrlEncodedFormEntity form = new UrlEncodedFormEntity(postParameters);
                    request.setEntity(form);

                    HttpResponse response = httpClient.execute(request);
                    HttpEntity httpEntity = response.getEntity();
                    result = EntityUtils.toString(httpEntity);
                } catch (IOException e) {
                    result = "Error";
                    e.printStackTrace();
                } finally {
                    return result;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result.equals("") || result.equals("Not send"))
                    ;
                else
                    message.setvMsgState("S");
//                Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
                super.onPostExecute(result);
            }
        };

        msgSentAsyncTask.execute();
        DbHelper dbHelper = new DbHelper(mContext);
        dbHelper.insertIntoMessage(message);
    }

    private void addNewMessage(Bitmap imgMsg) {

        final String time = Calendar.getInstance().getTime().toString();
        Calendar calender = Calendar.getInstance();
        Integer year = calender.get(Calendar.YEAR);
        Integer monthOfYear = calender.get(Calendar.MONTH) + 1;
        Integer dayOfMonth = calender.get(Calendar.DAY_OF_MONTH);

        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        String currentDateTimeString = sdf.format(d);

        final Message message = new Message(this);
        Spanned msg = Html.fromHtml(CommonFuntions.encodeTobase64(imgMsg));
        message.setvMsg(msg);
        message.setvFrom(myPhoneNumber);
        message.setvTo(vPhoneNumber);
        message.setvDate(dayOfMonth.toString() + "/" + monthOfYear.toString() + "/" + year.toString());
        message.setvTime(currentDateTimeString);
        message.setvMsgType("image/base64");
        messages.add(message);
        message.setvMsgState("N");
//        messageArrayAdapter.clear();
        messageArrayAdapter.notifyDataSetChanged();


        AsyncTask<Void, Void, String> msgSentAsyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... params) {

                String result = "";
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    String requestString = SQL_MsgSendRequest;
                    Log.v("SQL_SendMsg_REQUEST", requestString);
                    HttpPost request = new HttpPost(requestString);

                    List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                    postParameters.add(new BasicNameValuePair("regId", vRegId));
//                    postParameters.add(new BasicNameValuePair("message", DbHelper.getStringFromSpanned(message.getvMsg())));
                    postParameters.add(new BasicNameValuePair("message", DbHelper.getStringFromSpanned(message.getvMsg())));
                    postParameters.add(new BasicNameValuePair("msg_type", message.getvMsgType()));
                    postParameters.add(new BasicNameValuePair("sender", myPhoneNumber));
                    postParameters.add(new BasicNameValuePair("receiver", vPhoneNumber));
                    postParameters.add(new BasicNameValuePair("receiver_type", "S"));
                    postParameters.add(new BasicNameValuePair("time", time.replaceAll(" ", "%20")));
                    postParameters.add(new BasicNameValuePair("msg_state", "U"));
                    UrlEncodedFormEntity form = new UrlEncodedFormEntity(postParameters);
                    request.setEntity(form);

                    HttpResponse response = httpClient.execute(request);
                    HttpEntity httpEntity = response.getEntity();
                    result = EntityUtils.toString(httpEntity);
                } catch (IOException e) {
                    result = "Error";
                    e.printStackTrace();
                } finally {
                    return result;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result.equals("") || result.equals("Not send"))
                    ;
                else
                    message.setvMsgState("S");
//                Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
                super.onPostExecute(result);
            }
        };

        msgSentAsyncTask.execute();
        DbHelper dbHelper = new DbHelper(mContext);
        dbHelper.insertIntoMessage(message);
    }

    @Override
    protected void onPause() {
//        Toast.makeText(getBaseContext(), "Pause", Toast.LENGTH_SHORT).show();
        msgUpdateAsyncTask.cancel(false);
        flag = false;
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_msgUpdate) {
            updateAllMsg();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateAllMsg() {
        msgUpdateAsyncTask =
                new AsyncTask<Void, Void, String>() {

                    @Override
                    protected String doInBackground(Void... params) {
                        if (isCancelled())
                            return null;
                        String result = "";
                        try {
                            HttpClient httpClient = new DefaultHttpClient();
                            String requestString = SQL_MsgGetRequest + "&number=" + myPhoneNumber + "&threshold=" + MaxIdReceived;
//                    URI uri = URI.create(requestString);
                            Log.v("SERVER_SQL_GETMSG", requestString);
                            HttpPost request = new HttpPost(requestString);
                            HttpResponse response = httpClient.execute(request);
                            HttpEntity httpEntity = response.getEntity();
                            result = EntityUtils.toString(httpEntity);
                        } catch (IOException e) {
                            result = "Error";
                            e.printStackTrace();
                        } finally {
                            return result;
                        }
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        if (result == null) {
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            JSONArray messsageJsonArray = jsonObject.getJSONArray("messages");
                            for (int i = 0; i < messsageJsonArray.length(); i++) {
                                JSONObject explrObject = messsageJsonArray.getJSONObject(i);
                                String id = explrObject.getString("_id");
                                try {
                                    int tempVal = Integer.parseInt(id);
                                    if (tempVal > MaxIdReceived) {
                                        MaxIdReceived = tempVal;
                                    }
                                } catch (NumberFormatException e) {
                                    Log.v("Num_Convrn_Error2", e.toString());
                                }
                                String msg_type = explrObject.getString("msg_type");
                                String message = explrObject.getString("message");
                                Log.v("RECE_MSG", message);

                                Html.ImageGetter imageGetter = new Html.ImageGetter() {
                                    public Drawable getDrawable(String source) {
                                        StringTokenizer st = new StringTokenizer(source, ".");
                                        Drawable d = new BitmapDrawable(getResources(), emoticons[Integer.parseInt(st.nextToken()) - 1]);
                                        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                                        return d;
                                    }
                                };
                                message = message.trim().replaceAll("%20", " ");
                                message = message.replaceAll("%3C", "<");
                                message = message.replaceAll("%3E", ">");
                                message = message.replaceAll("%22", "\"");
                                Spanned spannedMsg = Html.fromHtml(message, imageGetter, null);
                                String sender = explrObject.getString("sender");
                                String receiver_list = explrObject.getString("receiver_list");
                                String receiver_type = explrObject.getString("receiver_type");
                                String time = explrObject.getString("time");
                                Message tempMessage = new Message(mContext, spannedMsg, msg_type, sender, receiver_list, time, time, true);
                                tempMessage.setvMsgId(id);
                                if (sender.equals(vPhoneNumber)) {
                                    DbHelper dbHelper = new DbHelper(mContext);
                                    dbHelper.insertIntoMessage(tempMessage);
                                    messages.add(tempMessage);
                                    messageArrayAdapter.notifyDataSetChanged();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (!isCancelled() && flag) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    updateAllMsg();
                                }
                            }, 2000);
                        }
                        super.onPostExecute(result);

                    }
                }.execute();
    }

    @Override
    public void keyClickedIndex(String index) {

        Html.ImageGetter imageGetter = new Html.ImageGetter() {
            public Drawable getDrawable(String source) {
                StringTokenizer st = new StringTokenizer(source, ".");
                Drawable d = new BitmapDrawable(getResources(), emoticons[Integer.parseInt(st.nextToken()) - 1]);
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                return d;
            }
        };

        Spanned cs = Html.fromHtml("<img src ='" + index + "'/>", imageGetter, null);

        int cursorPosition = content.getSelectionStart();
        content.getText().insert(cursorPosition, cs);
    }
}
