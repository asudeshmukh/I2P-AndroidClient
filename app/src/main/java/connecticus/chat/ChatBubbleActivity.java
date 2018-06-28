package connecticus.chat;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.speech.RecognizerIntent;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChatBubbleActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {

    private static final String TAG = ChatBubbleActivity.class.getSimpleName();
    public static ChatArrayAdapter chatArrayAdapter;
    String JSON_STRING;
    public ListView listView;
    public static boolean firstflag = false;
    public static EditText chatEditText;
    private ImageButton buttonSend, speak;
    public static TextToSpeech textToSpeech;
    ArrayList<String> results;
    Intent intent;
    public static boolean side = false;
    private String path = "http://192.168.0.9:9009/nessa/bot/";
    static String basePath = "";
    RequestQueue requestQueue;
    public static ProgressDialog dialog;
    public static boolean flag = false;
    private GoogleApiClient client;
    public static ArrayList<String> lableStringList;
    public static MenuInflater inflater;
    public static Menu optionMenu;
    public ListView listView2;

    RetryPolicy policy = new DefaultRetryPolicy(500000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //String string =Settings.Secure.getString( getContentResolver(),Settings.Secure.ANDROID_ID);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.avatar4);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Please Wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
        dialog.show();

        setContentView(R.layout.activity_chat);
        speak = (ImageButton) findViewById(R.id.bSpeak);
        buttonSend = (ImageButton) findViewById(R.id.buttonSend);

        ArrayList<HashMap<String, String>> incidentsList = new ArrayList<>();
        listView2 = (ListView) findViewById(R.id.list1);

        textToSpeech = new TextToSpeech(ChatBubbleActivity.this, ChatBubbleActivity.this);
        lableStringList = new ArrayList<String>();
        listView = (ListView) findViewById(R.id.listView1);

        chatArrayAdapter = new ChatArrayAdapter(ChatBubbleActivity.this, R.layout.activity_chat_singlemessage, this);
        listView.setAdapter(chatArrayAdapter);
        chatEditText = (EditText) findViewById(R.id.chatText);

        chatEditText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    Services services = new Services(getApplicationContext());
                    return services.sendChatMessage();
                }
                return false;
            }
        });

        chatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    buttonSend.setVisibility(View.GONE);
                    speak.setVisibility(View.VISIBLE);
                } else {
                    buttonSend.setVisibility(View.VISIBLE);
                    speak.setVisibility(View.GONE);
                }
            }
        });

        buttonSend.setVisibility(View.GONE);
        speak.setVisibility(View.VISIBLE);

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
                dialog.dismiss();
            }
        });

        //http call
        new firstAsyncTask().execute(path);

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    //CODE FOR SEND BUTTON =====START=====
    public void sendButton(View v) {
        Services services = new Services(getApplicationContext());
        services.sendChatMessage();
        buttonSend.setVisibility(View.GONE);
        speak.setVisibility(View.VISIBLE);
    }
    //CODE FOR SEND BUTTON =====START=====


    //CODE FOR SPEAKER BUTTON =====START=====
    public void speakerButton(View v) {
        flag = true;
        // This are the intents needed to start the Voice recognizer
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");
        startActivityForResult(i, 1010);
    }
    //CODE FOR SPEAKER BUTTON =====END=====

    @Override
    public void onDestroy() {
        textToSpeech.shutdown();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        inflater = getMenuInflater();
        optionMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Create email id")) {
            chatEditText.setText("create email");
        } else {
            chatEditText.setText(item.getTitle());
        }
        Services services = new Services(getApplicationContext());
        services.sendChatMessage();
        return true;
    }

    @Override
    public void onInit(int Text2SpeechCurrentStatus) {
        if (Text2SpeechCurrentStatus == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.US);
        }
    }

    //CODE FOR ONLY FIRST HTTP REQUEST FROM MOBILEAPP TO BOT =====START=====
    class firstAsyncTask extends AsyncTask<String, Void, String> {

        // here actual call is made to the NessaBot
        @Override
        protected String doInBackground(String... voids) {
            try {
                //establishing connection
                URL urlToRequest = new URL(path);
                HttpURLConnection httpURLConnection = (HttpURLConnection) urlToRequest.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // write out form parameters
                String postParameters = "user=adb51de4-d9f0-4a99-b676-ce05734277a2";
                httpURLConnection.setFixedLengthStreamingMode(postParameters.getBytes().length);
                PrintWriter out = new PrintWriter(httpURLConnection.getOutputStream());
                out.print(postParameters);
                out.close();

                // connect
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();

                while ((JSON_STRING = bufferedReader.readLine()) != null) {
                    stringBuilder.append(JSON_STRING + "\n");
                }

                // disconnect
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        // here we get the result from the NessaBot in  @String responce
        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject json = (JSONObject) new JSONTokener(response).nextValue();
                String sessionId = (String) json.get("sessionId");
                basePath = path + sessionId;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            secondOptionsRequest("options", response);
            dialog.dismiss();
            super.onPostExecute(response);
        }
    }
    //CODE FOR ONLY FIRST HTTP REQUEST FROM MOBILEAPP TO BOT =====END=====

    @Override
    protected void onStart() {
        super.onStart();
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "ChatBubble Page",
                Uri.parse("http://host/path"),
                Uri.parse("android-app://connecticus.chat/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    private boolean convertToTextMessage() {

        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");
        return true;

    }

//receiveChatMessage()

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        chatEditText.setText(results.get(0));
        Services services = new Services(getApplicationContext());
        services.sendChatMessage();
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onStop() {
        super.onStop();

        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "ChatBubble Page",
                Uri.parse("http://host/path"),
                Uri.parse("android-app://connecticus.chat/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    //secondRequest()
    public void secondOptionsRequest(String query, final String firstResponse) {

        ChatBubbleActivity.firstflag = true;
        requestQueue = Volley.newRequestQueue(this);
        HttpsTrustManager.allowAllSSL();

        String url = basePath + "/getDDF";
        final String qrerysrt = query;

        Map<String, String> params = new HashMap();
        params.put("userUtterance", query);
        //system.out.println("query:"+query);

        JSONObject parameters = new JSONObject(params);
        try {
            JsonResponseRequest jsonResponseRequest = new JsonResponseRequest(Request.Method.POST, url,
                    new Response.Listener<JSONObject>() {
                        public void onResponse(JSONObject response) {
                            try {

                                JSONArray jsonArray = response.getJSONArray("tasks");
//                                Log.e(TAG, "onResponse: " + jsonArray);

                                JSONObject jsonResponce = (JSONObject) new JSONTokener(firstResponse).nextValue();
                                JSONObject result = jsonResponce.getJSONObject("result");
                                String reply = (String) result.get("reply");
                                //    Log.e("reply from server",reply);
                                //  lableStringList.add(firstResponse);
                                lableStringList.add(reply);
                                // Toast.makeText(getApplicationContext(), firstResponse, Toast.LENGTH_SHORT).show();
                                ArrayList<String> lableList = new ArrayList<String>();
                                for (int i = 1; i < jsonArray.length(); i++) {
                                    JSONObject message = (JSONObject) jsonArray.get(i);

                                    //    Log.e(TAG, new String(message.getString("reply").getBytes("ISO-8859-1"), "UTF-8"));

                                    if (!(message.getString("label").equals(""))) {
                                        if (!(message.getString("label").equals("options"))) {
                                            String lable = message.getString("label");
                                            lableList.add(lable);
                                        }
                                    }
                                }
                                chatArrayAdapter.add(new ChatMessage(side, lableStringList));
                                side = !side;

                                if (!lableList.isEmpty()) {
                                    for (int i = 1; i <= lableList.size(); i++) {
                                        optionMenu.add(i, i, i, lableList.get(i));
                                    }
                                }
                                inflater.inflate(R.menu.main_menu, optionMenu);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d("volley", "Error: " + error.getMessage());
                    error.printStackTrace();
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/x-www-form-urlencoded; charset=UTF-8";
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("userUtterance", qrerysrt);
                    return params;
                }
            };

            jsonResponseRequest.setRetryPolicy(policy);
            requestQueue.add(jsonResponseRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}