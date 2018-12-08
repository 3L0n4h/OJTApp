package hci.com.tentativecapstoneui;

import com.google.firebase.database.Exclude;

public class Upload {

        private String mName;
        private String mDesc;
        private String mImageUrl;
        private String mKey;
        private String timestamp = "";

        public Upload() {

        }

        public Upload(String name, String desc, String imageUrl, String timestamp){
            if (name.trim().equals("")) {
                name = "No Name";
            }

            mName = name;
            mDesc = desc;
            mImageUrl = imageUrl;
            this.timestamp = timestamp;

        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            mName = name;
        }

        public String getDesc() {
            return mDesc;
        }

        public void setDesc(String desc) {
            mDesc = desc;
        }

        public String getImageUrl() {
            return mImageUrl;
        }

        public void setImageUrl(String imageUrl) {
            mImageUrl = imageUrl;
        }

        public String getTimestamp(){
            return timestamp;
        }

        @Exclude
        public String getKey() {
            return mKey;
        }

        @Exclude
        public void setKey (String key) {
            mKey = key;
        }

    }
