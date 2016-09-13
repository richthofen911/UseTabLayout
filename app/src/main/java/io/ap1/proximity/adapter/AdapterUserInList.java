package io.ap1.proximity.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import io.ap1.proximity.Constants;
import io.ap1.proximity.AppDataStore;
import io.ap1.proximity.MyBackendlessUser;
import io.ap1.proximity.R;
import io.ap1.proximity.viewholder.ViewHolderUserInList;

/**
 * Created by admin on 17/03/16.
 */
public class AdapterUserInList extends RecyclerView.Adapter<ViewHolderUserInList>{
    private Context context;

    public AdapterUserInList(Context context){
        this.context = context;
    }

    @Override
    public ViewHolderUserInList onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_in_list, viewGroup, false);
        return new ViewHolderUserInList(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderUserInList viewHolder, int position){
        viewHolder.setIsRecyclable(false);
        MyBackendlessUser userTmp = AppDataStore.userList.get(position);
        /*
        String userName = (String) userTmp.getProperty("name");
        String userBio = (String) userTmp.getProperty("bio");
        String userColor = "#" + userTmp.getProperty("color");
        Log.e("adpater color", userColor);
        String userPictureURL = (String) userTmp.getProperty("pictureUrl");
        String pictureUrl = Constants.PROFILE_IMAGE_PATH_ROOT + userPictureURL;
        Log.e("picasso", pictureUrl);
        */
        String userName = userTmp.getName();
        String userBio = userTmp.getBio();
        String userColor = userTmp.getColor();
        String userPictureUrl = Constants.PROFILE_IMAGE_PATH_ROOT + userTmp.getProfileImage();
        viewHolder.tvDetectedUserColor.setBackgroundColor(Color.parseColor(userColor));
        if(userTmp.getUnreadMessageList().size() == 0)
            viewHolder.tvDetectedUserMsgNotify.setVisibility(View.GONE);
        else{
            int newSize = userTmp.getUnreadMessageList().size();
            viewHolder.tvDetectedUserMsgNotify.setVisibility(View.VISIBLE);
            viewHolder.tvDetectedUserMsgNotify.setText(Integer.toString(newSize));
        }

        Picasso.with(context).load(userPictureUrl).into(viewHolder.ivDetectedUserProfileImage);
        viewHolder.tvDetectedUserName.setText(userName);
        viewHolder.tvDetectedUserBio.setText(userBio);

        viewHolder.selfPosition = position;
    }

    @Override
    public int getItemCount() {
        return AppDataStore.userList.size();
    }
}
