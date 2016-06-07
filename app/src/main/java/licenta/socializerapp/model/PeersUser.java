package licenta.socializerapp.model;

import android.net.wifi.p2p.WifiP2pDevice;

import java.util.List;

/**
 * Created by Madalina on 5/14/2016.
 */
public class PeersUser {

    public User user;
    public WifiP2pDevice device;

    public PeersUser(User user, WifiP2pDevice device) {
        this.user = user;
        this.device = device;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public WifiP2pDevice getDevice() {
        return device;
    }

    public void setDevice(WifiP2pDevice device) {
        this.device = device;
    }
}
