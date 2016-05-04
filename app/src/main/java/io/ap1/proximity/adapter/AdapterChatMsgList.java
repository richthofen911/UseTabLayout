package io.ap1.proximity.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import net.callofdroidy.apas.Message;

import java.util.ArrayList;

import io.ap1.proximity.AppDataStore;
import io.ap1.proximity.Constants;
import io.ap1.proximity.R;
import io.ap1.proximity.viewholder.ViewHolderChatMessage;

/**
 * Created by admin on 18/03/16.
 */
public class AdapterChatMsgList extends RecyclerView.Adapter<ViewHolderChatMessage>{
    private static final String TAG = "AdapterChatMsgList";

    private Context context;
    private ArrayList<Message> chatHistory;
    private String myObjectId;
    private String otherObjectId;// the person you are talking to
    private String myProfileImageUrl;
    private String otherProfileImageUrl;
    private LinearLayoutManager linearLayoutManager;

    public AdapterChatMsgList(Context context, ArrayList<Message> messages, String myObjectId, String otherObjectId, String myProfileImageName, String otherProfileImageName){
        this.context = context;
        chatHistory = messages;
        this.myObjectId = myObjectId;
        this.otherObjectId = otherObjectId;
        myProfileImageUrl = Constants.PROFILE_IMAGE_PATH_ROOT + myProfileImageName;
        otherProfileImageUrl = Constants.PROFILE_IMAGE_PATH_ROOT + otherProfileImageName;
     }

    @Override
    public ViewHolderChatMessage onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_in_list, viewGroup, false);
        return new ViewHolderChatMessage(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderChatMessage viewHolder, final int position){
        viewHolder.setIsRecyclable(false);
        Message historyMsg = chatHistory.get(position);

        Log.e("myUrl", myProfileImageUrl);
        Log.e("otherUrl", otherProfileImageUrl);

        // the other one's message aligns left, mine align right and has background color
        if(historyMsg.getHeaders().get("source").equals(otherObjectId)){
            viewHolder.tvSelfPadding.setVisibility(View.GONE);
            Picasso.with(context).load(otherProfileImageUrl).into(viewHolder.ivChatUserProfileImage);
        }else{
            Picasso.with(context).load(myProfileImageUrl).into(viewHolder.ivChatUserProfileImage);
            viewHolder.tvChatMsgContent.setBackground(context.getResources().getDrawable(R.drawable.textview_round_corner));
        }

        String userName = historyMsg.getHeaders().get("name");
        Log.e(TAG, "timestamp recv: " + historyMsg.getHeaders().get("timestamp"));
        String timestamp = AppDataStore.myDateFormat.format(Long.parseLong(historyMsg.getHeaders().get("timestamp")));
        String content = historyMsg.getBody();

        viewHolder.tvChatUserName.setText(userName);
        viewHolder.tvChatMsgTimestamp.setText(timestamp);
        viewHolder.tvChatMsgContent.setText(content);

        viewHolder.selfPosition = position;
    }

    @Override
    public int getItemCount() {
        return chatHistory.size();
    }

    public ArrayList<Message> getChatHistory(){
        return chatHistory;
    }
}
