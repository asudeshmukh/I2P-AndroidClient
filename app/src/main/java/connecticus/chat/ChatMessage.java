package connecticus.chat;

import android.widget.ImageView;
import android.widget.VideoView;

import java.util.ArrayList;

public class ChatMessage {

	public boolean left;
	public ArrayList<String> message;

	public ChatMessage(boolean left, ArrayList<String> message) {
		super();
		this.left = left;
        this.message = message;
    }
}