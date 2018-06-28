package connecticus.chat;

import android.content.Context;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TableLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RetryPolicy;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static connecticus.chat.ChatBubbleActivity.chatArrayAdapter;
import static connecticus.chat.ChatBubbleActivity.chatEditText;
import static connecticus.chat.ChatBubbleActivity.dialog;
import static connecticus.chat.ChatBubbleActivity.flag;
import static connecticus.chat.ChatBubbleActivity.side;
import static connecticus.chat.ChatBubbleActivity.textToSpeech;

public class Services {

    Context context;
    static String prefix = "";
    RetryPolicy policy = new DefaultRetryPolicy(500000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    public static String image_uri = "";
    public static String audio_uri = "";
    public static String video_uri = "";
    public static String media = "";
    private TableLayout contentView;
    private AbsListView.LayoutParams newTableLayout;
    public ListView lv;
    String JSON_STRING1;


    Services(Context context1) {
        context = context1;
    }

    //CODE FOR BINDING KEYBOARD TEXT TO CHAT POP-UP =====START=====(This method is called every time when user type something )
    public boolean sendChatMessage() {

        String chatmsg = chatEditText.getText().toString();
        ArrayList<String> chatMessage = new ArrayList<String>();
        if (!(prefix.equals(""))) {
            chatmsg = prefix + chatmsg;
        }
        chatMessage.add(chatmsg);
        chatArrayAdapter.add(new ChatMessage(side, chatMessage));
        secondRequest(chatmsg);
        chatEditText.setText("");
        side = !side;
        dialog.show();
        return true;
    }
    //CODE FOR BINDING KEYBOARD TEXT TO CHAT POP-UP =====END=====

    // CODE FOR ALL REQUEST AFTER FIRST CALL
    public void secondRequest(String query) {

        ChatBubbleActivity.firstflag = false;
        //   Toast.makeText(context, "service query=" + query, Toast.LENGTH_SHORT).show();
        HttpsTrustManager.allowAllSSL();
        final String qrerysrt = query;
        //http call
        new secondAsyncTask().execute(qrerysrt);
    }

    //CODE FOR ALL HTTP REQUEST AFTER FIRST CALL FROM MOBILEAPP TO BOT =====START=====
    class secondAsyncTask extends AsyncTask<String, Void, String> {
        String textResponse = null;

        // here actual call is made to the NessaBot
        @Override
        protected String doInBackground(String... qrerysrt) {
            try {
                //establishing connection
                String url = ChatBubbleActivity.basePath;
                URL urlToRequest = new URL(url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) urlToRequest.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // write out form parameters
                String params = "userUtterance=" + qrerysrt[0];
                httpURLConnection.setFixedLengthStreamingMode(params.getBytes().length);
                PrintWriter out = new PrintWriter(httpURLConnection.getOutputStream());
                out.print(params);
                out.close();

                // connect
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();

                while ((JSON_STRING1 = bufferedReader.readLine()) != null) {
                    stringBuilder.append(JSON_STRING1 + "\n");
                }

                // disconnect
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        // here we get the result from the NessaBot in  @String responce
        @Override
        protected void onPostExecute(String response) {

            try {
                JSONObject jsonResponce = (JSONObject) new JSONTokener(response).nextValue();
                JSONObject json2 = jsonResponce.getJSONObject("result");
                //  textResponse = (String) json2.get("reply");
                //   Log.e("reply from server",textResponse);
                ///  textResponse = (String) json2.get("reply") + "<div><a href='" + json2.get("speech") + "'>Click Here To Download Document</a></div>";
                receiveChatMessage(jsonResponce);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dialog.dismiss();
            super.onPostExecute(response);
        }
    }
    //CODE FOR  HTTP REQUEST FROM MOBILEAPP TO BOT =====END=====

    //CODE FOR DISPLAYING RESULT ON CHAT WINDOW =====START=====(eg.show responce result in TABLE format)
    public boolean receiveChatMessage(JSONObject response) {
        String speakResponse;
        String textResponse = null;
        try {
            JSONObject result2 = (JSONObject) response.get("result");
//            if(result2.get("current_entity").equals("getEmpName"))  invoiceList
//                prefix = "user:";
//            if(result2.get("current_entity").equals("getDept"))
//                prefix = "dept:";
//            if(result2.get("current_entity").equals("getReportManager"))
//                prefix = "user:";
//            if(result2.get("current_entity").equals("getWorkingLocation"))
//                prefix = "city:";

            if (response.has("message")) {

                JSONObject message = (JSONObject) response.get("message");
                speakResponse = (String) message.get("chat");
                JSONObject data = (JSONObject) message.get("data");
                JSONObject info = (JSONObject) data.get("info");

                //code for displaying invoiceslist in the form of table
                if (info.has("invoiceList") && !(info.get("invoiceList")).equals("")) {

                    String tableData = "<table border='1px solid black'>";

                    try {
                        JSONArray invoiceDetailsList = (JSONArray) info.get("invoiceList");

                        for (int i = 0; i < invoiceDetailsList.length(); i++) {
                            JSONObject jsonArr_data = invoiceDetailsList.getJSONObject(i);
//                            Log.e("invoiceObj==>", jsonArr_data.toString());

                            String _id = jsonArr_data.getString("_id");
                            String ledgerName = jsonArr_data.getString("ledgerName");
                            String date = jsonArr_data.getString("date");
                            String invoiceNumber = jsonArr_data.getString("invoiceNumber");
                            String totalAmount = jsonArr_data.getString("totalAmount");
                            String status = jsonArr_data.getString("status");
                            String isInvoicePaid = jsonArr_data.getString("isInvoicePaid");

                            tableData = tableData + "<tr><td><p><b>कंपनी का नाम : </b>" + ledgerName + "</p>" +
                                    "<p><b>तारीख : </b>" + date + "</p>" +
                                    "<p><b>इनवॉइस नंबर  : </b>" + invoiceNumber + "</p>" +
                                    // "<p><b>इनवॉइस स्टेटस :</b><a id=" + _id + " data-toggle='tooltip' title='एप्रूव्ड इनवॉइस.' onclick='markActiveLink(this)'>" + " " + status + "</a></p>" +
                                    "<p><b>इनवॉइस स्टेटस  : </b>" + status + "</p>" +
                                    "<p><b>इनवॉइस पेड  : </b>" + isInvoicePaid + "</p>" +
                                    "<p><b>टोटल अमाउंट  : </b>" + totalAmount + ".00" + "</p>" +
                                    "<p><b>------------------------------------------</b></p></td></td></tr>";
                        }
                        textResponse = tableData + "</table><br>";

                        ArrayList<String> responseArray = new ArrayList<String>();
                        responseArray.add(textResponse);

                        chatArrayAdapter.add(new ChatMessage(side, responseArray));
                        side = !side;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if ((info.get("text")).equals("") && info.has("file")) {
                    String file = (String) info.get("file");
                    //  textResponse = file + "<br>" + speakResponse;
                } else if (info.has("file")) {

                    if (!(info.get("file")).equals("")) {
                        String text = (String) info.get("text");
                        String file = (String) info.get("file");
                        //  textResponse = text + "<br>" + file;
                    } else {
                        textResponse = (String) info.get("text");
                    }
                } else {
                    if (!(info.get("text")).equals("")) {
                        // textResponse = (String) info.get("text");
                    } else {
                        textResponse = (String) result2.get("reply");
                        if (!message.get("chat").equals(""))
                            speakResponse = (String) result2.get("reply");
                    }
                }

//                if (info.has("documents") && !(info.get("documents")).equals("")) {
//                    Toast.makeText(context, (info.get("documents")).toString(), Toast.LENGTH_SHORT).show();
//                    if (!(info.get("text")).equals("")) {
//                        textResponse = (String) info.get("text") + "<div><a href='" + info.get("documents") + "'>Click Here To Download Document</a></div>";
//                    } else {
//                        textResponse = "<div><a href='" + info.get("documents") + "'>Click Here To Download Document</a></div>";
//                    }
//                }
//                if (info.has("image") && !(info.get("image")).equals("")) {
//                    media = "imagefile";
//                    // Toast.makeText(context,(info.get("image")).toString(),Toast.LENGTH_SHORT).show();
//                    image_uri = info.get("image").toString();
//                }
//                if (info.has("video") && !(info.get("video")).equals("")) {
//                    media = "videofile";
//                    video_uri = info.get("video").toString();
//                }
//                if (info.has("audio") && !(info.get("audio")).equals("")) {
//                    //media = "audiofile";
//                    media = "videofile";
//                    // Toast.makeText(context,(info.get("audio")).toString(),Toast.LENGTH_SHORT).show();
//                    audio_uri = info.get("audio").toString();
//                }
//
            } else {
                JSONObject result = (JSONObject) response.get("result");
                textResponse = (String) result.get("reply");
                speakResponse = (String) result.get("reply");

                ArrayList<String> responseArray = new ArrayList<String>();
                responseArray.add(textResponse);

                chatArrayAdapter.add(new ChatMessage(side, responseArray));
                side = !side;
            }

//            ArrayList<String> responseArray = new ArrayList<String>();
//            responseArray.add(textResponse);
//
//            chatArrayAdapter.add(new ChatMessage(side, responseArray));
//            side = !side;
            if (flag) {
                textToSpeech.speak(speakResponse, TextToSpeech.QUEUE_FLUSH, null);
                flag = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }
    //CODE FOR DISPLAYING RESULT ON CHAT WINDOW =====END=====

    public void setContentView(TableLayout contentView) {
        this.contentView = contentView;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
