package hci.com.tentativecapstoneui.model;

import java.util.HashMap;
import java.util.List;

public class User {

    private String username;
    private String email;
    private String imageUrl;
    private String userId;

    public User(){

    }

    public User(String name, String email){
        this.username = name;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
