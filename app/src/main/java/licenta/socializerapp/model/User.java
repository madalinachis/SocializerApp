package licenta.socializerapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Madalina on 5/6/2016.
 */
public class User implements Parcelable {

    @SerializedName("userId")
    private int userId;
    @SerializedName("hobby")
    private String hobby;
    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;
    @SerializedName("mac")
    private String mac;
    @SerializedName("name")
    private String name;
    @SerializedName("username")
    private String username;
    @SerializedName("password")
    private String password;

    private User() {

    }

    public static User create() {
        return new User();
    }

    public User userId(int userId) {
        this.userId = userId;
        return this;
    }

    public User hobby(String hobby) {
        this.hobby = hobby;
        return this;
    }

    public User latitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public User longitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public User mac(String mac) {
        this.mac = mac;
        return this;
    }

    public User name(String name) {
        this.name = name;
        return this;
    }

    public User username(String username) {
        this.username = username;
        return this;
    }

    public User password(String password) {
        this.password = password;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.userId);
        dest.writeString(this.hobby);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeString(this.mac);
        dest.writeString(this.name);
        dest.writeString(this.username);
        dest.writeString(this.password);
    }

    protected User(Parcel in) {
        this.userId = in.readInt();
        this.hobby = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.mac = in.readString();
        this.name = in.readString();
        this.username = in.readString();
        this.password = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public double getLatitude() {
        return latitude;
    }

    public String getHobby() {
        return hobby;
    }

    public int getUserId() {
        return userId;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getMac() {
        return mac;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
