package connecticus.chat;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static connecticus.chat.R.id.imageView2;
import static connecticus.chat.Services.audio_uri;
import static connecticus.chat.Services.image_uri;
import static connecticus.chat.Services.media;
import static connecticus.chat.Services.video_uri;

public class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {
    private static final String TAG = ChatArrayAdapter.class.getSimpleName();
    private final Context context;
    private long enqueue;
    private DownloadManager dm;
    static String second="";
    private List<ChatMessage> chatMessageList = new ArrayList<>();
    private String strhref;
    private String pdfhref;
    public static ImageView server_image;
    MediaPlayer mp = new MediaPlayer();
    private MediaController mediaController;
    public Activity activity;
     private int flagcount = 0;

    @Override
    public void add(ChatMessage object) {
        chatMessageList.add(object);
        super.add(object);
    }

    public ChatArrayAdapter(Context context1, int textViewResourceId,Activity activity1) {
        super(context1, textViewResourceId);
        this.context = context1;
        this.activity = activity1;
    }

    public int getCount() {

        return this.chatMessageList.size();
    }

    public ChatMessage getItem(int index) {
        return this.chatMessageList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.activity_chat_singlemessage, parent, false);
        }
        Date date = new Date();
        SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a");
        String time = timeFormatter.format(date);
        LinearLayout singleMessageContainer = (LinearLayout) row.findViewById(R.id.singleMessageContainer);
       // LinearLayout mediaLayout = (LinearLayout) row.findViewById(R.id.mediaContainer);
       // mediaLayout.setVisibility(View.GONE);
        ChatMessage chatMessageObj = getItem(position);

        assert chatMessageObj != null;
        ArrayList<String> chatMessageObjFromChatMessage = new ArrayList<String>();
        chatMessageObjFromChatMessage = chatMessageObj.message;
        TextView chatText = (TextView) row.findViewById(R.id.singleMessage);
        //TextView chatText  = new TextView(context);
        //singleMessageContainer.addView(chatText);


        String str = chatMessageObj.message.get(0);
        if(null!=str){
            if(str.contains("href")) {

                String splitstrarr1[] = str.split("=");
                String splitstrarr2[] = splitstrarr1[1].split("'");
                String splitstrarr3[] = splitstrarr2[1].split("'");
                strhref = splitstrarr3[0];
                pdfhref = strhref.replace(" ","%20");

             chatText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getContext(), "Click event", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent();
                        i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(i);

                        dm = (DownloadManager) getContext().getSystemService(getContext().DOWNLOAD_SERVICE);
                        DownloadManager.Request request = new DownloadManager.Request(
                                Uri.parse(pdfhref));
                        enqueue = dm.enqueue(request);
                        Log.e(TAG, "onClick: ");
                        //Intent intent = new Intent(Intent.ACTION_VIEW);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
            });
            }
        }
        String chatMessage = "";
        if(chatMessageObjFromChatMessage.size() == 1){
            chatMessage = chatMessageObjFromChatMessage.get(0);
            if(null!=chatMessage) {
                if (chatMessage.contains("user:") || chatMessage.contains("dept:") || chatMessage.contains("city:")) {
                    String parts[] = chatMessage.split(":");
                    chatMessage = parts[1];
                }
            }
        }
        else{
            chatMessage = chatMessageObjFromChatMessage.get(0);
        }

         String ServerName = "<font color=#c1c1c1 size=20>" + " Nessa Bot" + " </font> " + "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0" + "<font color=#c1c1c1 size=12>" + time + "</font> <br>" + chatMessage;
         String userName = "<font color=#c1c1c1 size=20>" + " Viraj " + "</font>" + "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0" + "<font color=#c1c1c1 size=12>" + time + "</font> <br>" + chatMessage;

        ImageView imgView = (ImageView) row.findViewById(R.id.imageView);
        ImageView imgView2 = (ImageView) row.findViewById(R.id.imageView2);
        //server_image = (ImageView) row.findViewById(R.id.server_image);
        //server_image.setVisibility(View.GONE);
        chatText.setBackgroundResource(chatMessageObj.left ? R.drawable.bubble_a : R.drawable.bubble_b);
        /*imgView2.setImageResource(chatMessageObj.left ? R.drawable.user : R.drawable.bot);*/
        System.out.println("chatMessageObj.left = " + chatMessageObj.left);
        if (chatMessageObj.left) {

            imgView2.setImageResource(R.drawable.user);
            imgView.setVisibility(View.GONE);
            imgView2.setVisibility(View.VISIBLE);
            chatText.setTextSize(15);
            chatText.setText(Html.fromHtml(userName));
        } else {

            imgView.setImageResource(R.drawable.bot);
            imgView2.setVisibility(View.GONE);
            imgView.setVisibility(View.VISIBLE);

                chatText.setText(Html.fromHtml(ServerName));

                if(media.equals("audiofile")){
                    try {
                        String url = audio_uri.replaceAll(" ", "%20");
                        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mp.setDataSource(url);
                        //mediaLayout.setVisibility(View.VISIBLE);
                        mp.prepare();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mp.start();
                }
                if(media.equals("videofile")&& (audio_uri!="")){
                    try {
                        if(flagcount == 0) {
                            flagcount = 1;
                            Toast.makeText(context, "Count=" + flagcount, Toast.LENGTH_SHORT).show();
                            LinearLayout layoutmain = (LinearLayout) parent.getChildAt(position).findViewById(R.id.mainLinearLayout);

                            LinearLayout layoutView = new LinearLayout(context);
                            layoutView.setPadding(0,70,0,0);
                            layoutView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            layoutView.setOrientation(LinearLayout.HORIZONTAL);

                            //String vurl = video_uri.replaceAll(" ", "%20");
                            String vurl = "https://www.hdwplayer.com/videos/300.mp4";
                            VideoView video = new VideoView(context);
                            mediaController = new MediaController(context);
                            //mediaLayout.setVisibility(View.VISIBLE);
                            mediaController.setAnchorView(video);
                            video.setVideoURI(Uri.parse(vurl));
                            video.setLayoutParams(new FrameLayout.LayoutParams(550, 550));
                            mediaController.setMediaPlayer(video);
                            video.setMediaController(mediaController);
                            video.requestFocus();
                            layoutView.addView(video);
                            layoutmain.addView(layoutView);
                            video.start();

                        }
                        else{
                            Toast.makeText(context,"Else" ,Toast.LENGTH_SHORT).show();
                        }

                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                }
            if(media.equals("imagefile")){
                try {
                    //String url = image_uri.replaceAll(" ", "%20");
                    String url = "http://api.androidhive.info/images/sample.jpg";
                    ImageView imageView = new ImageView(context);
                    imageView.setImageURI(Uri.parse(url));
                    //mediaLayout.setVisibility(View.VISIBLE);
                    //mediaLayout.addView(imageView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        singleMessageContainer.setGravity(chatMessageObj.left ? Gravity.LEFT : Gravity.RIGHT);
        return row;
    }

}