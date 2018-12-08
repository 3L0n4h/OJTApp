package hci.com.tentativecapstoneui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Post {

    private String postTitle;
    private String postDescription;
    private String imageUrl;

    private String postUid;

    private String postNameFile;
    private String postDate;
    private String username;
    private String mKey;
    private String profilePic;
    private String fileLink;


    public Post(String postTitle, String postDescription, String imageUrl, String postUid, String postDate, String username, String postNameFile, String fileLink) {
        this.postTitle = postTitle;
        this.postDescription = postDescription;
        this.imageUrl = imageUrl;
        this.postUid = postUid;
        this.postDate = postDate;
        this.username = username;
        this.postNameFile = postNameFile;
        this.fileLink = fileLink;
    }

    public Post() {
    }

    public String getPostUid() {
        return postUid;
    }

    public void setPostUid(String postUid) {
        this.postUid = postUid;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String username) {
        this.username = username;
    }
    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }
    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public String getPostDescription() {
        return postDescription;
    }
    public String getmKey() {
        return mKey;
    }

    public void setmKey(String mKey) {
        this.mKey = mKey;
    }

    public String getPostNameFile() {
        return postNameFile;
    }

    public void setPostNameFile(String postNameFile) {
        this.postNameFile = postNameFile;
    }


    public String getFileLink() {
        return fileLink;
    }

    public void setFileLink(String fileLink) {
        this.fileLink = fileLink;
    }

}