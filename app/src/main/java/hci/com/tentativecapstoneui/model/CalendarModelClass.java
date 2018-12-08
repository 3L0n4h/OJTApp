package hci.com.tentativecapstoneui.model;

public class CalendarModelClass {
    String what;
    String where;
    String date;
    String startTime;
    String endTime;
    String heldBy;
    private String userName;
    private String postDate;
    private String mKey;

    private String userId;

    CalendarModelClass(){

    }

    public CalendarModelClass(String what, String where, String date, String startTime, String endTime, String heldBy) {
        this.what = what;
        this.where = where;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.heldBy = heldBy;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public String getmKey() {
        return mKey;
    }

    public void setmKey(String mKey) {
        this.mKey = mKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getHeldBy() {
        return heldBy;
    }

    public void setHeldBy(String heldBy) {
        this.heldBy = heldBy;
    }
}
