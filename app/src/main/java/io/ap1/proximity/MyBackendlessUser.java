package io.ap1.proximity;

import com.backendless.BackendlessUser;

import net.callofdroidy.apas.Message;

import java.util.ArrayList;

/**
 * Created by admin on 18/03/16.
 */
public class MyBackendlessUser {
    private BackendlessUser backendlessUser;
    private ArrayList<Message> unreadMessageList;

    public MyBackendlessUser(BackendlessUser backendlessUser){
        this.backendlessUser = backendlessUser;
        unreadMessageList = new ArrayList<>();
    }

    public String getName(){
        return (String) backendlessUser.getProperty("name");
    }

    public String getBio(){
        return (String) backendlessUser.getProperty("bio");
    }

    public String getColor(){
        return "#" + backendlessUser.getProperty("color");
    }

    public String getProfileImage(){
        return (String) backendlessUser.getProperty("profileImage");
    }

    public String getUserObjectId(){
        return backendlessUser.getObjectId();
    }

    public void addToMessageList(Message newMessage){
        unreadMessageList.add(newMessage);

    }

    public void clearMessageList(){
        unreadMessageList.clear();
    }

    public ArrayList<Message> getUnreadMessageList(){
        return unreadMessageList;
    }
}
