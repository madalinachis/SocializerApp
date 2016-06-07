package licenta.socializerapp.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import licenta.socializerapp.Application;
import licenta.socializerapp.R;
import licenta.socializerapp.adapters.UsersAdapter;
import licenta.socializerapp.dependencies.Injector;
import licenta.socializerapp.model.PeersUser;
import licenta.socializerapp.model.User;
import licenta.socializerapp.network.ErrorHandler;
import licenta.socializerapp.network.UserApis;
import licenta.socializerapp.transferfile.DeviceDetailFragment;
import licenta.socializerapp.transferfile.DeviceListFragment;
import licenta.socializerapp.transferfile.WiFiDirectBroadcastReceiver;
import licenta.socializerapp.utils.GetGpsLocation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        WifiP2pManager.ChannelListener, DeviceListFragment.DeviceActionListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int UPDATE_INTERVAL_IN_SECONDS = 50;
    private static final int FAST_CEILING_IN_SECONDS = 10;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;
    private static final float METERS_PER_FEET = 0.3048f;
    private static final int METERS_PER_KILOMETER = 1000;
    private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;
    private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;
    private static final int MAX_POST_SEARCH_RESULTS = 20;
    private static final int MAX_POST_SEARCH_DISTANCE = 100;

    private SupportMapFragment mapFragment;
    private Circle mapCircle;
    private float radius;
    private float lastRadius;
    private final Map<Integer, Marker> mapMarkers = new HashMap<>();
    private final Map<String, WifiP2pDevice> devicesMap = new HashMap<>();
    private int mostRecentMapUpdate;
    private int selectedPostObjectId;
    private List<WifiP2pDevice> peers = new ArrayList<>();
    private Location lastLocation;
    private Location currentLocation;
    User currentUser;
    UserApis userApis;
    UsersAdapter usersAdapter;
    List<PeersUser> peersUsersList = new ArrayList<>();
    List<User> users;
    GetGpsLocation gpsLocation;
    double latitude;
    double longitude;
    Spinner hobbySpinner;
    String selectedHobby;

    private LocationRequest locationRequest;
    private GoogleApiClient locationClient;

    public static final String TAG = "wifidirectdemo";
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;


    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.init();
        userApis = Injector.getApi(UserApis.class);
        currentUser = getIntent().getParcelableExtra("currentUser");
        radius = Application.getSearchDistance();
        lastRadius = radius;
        setContentView(R.layout.activity_main);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        locationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        hobbySpinner = (Spinner) findViewById(R.id.hobby_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MainActivity.this, R.array.hobbies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hobbySpinner.setAdapter(adapter);
        hobbySpinner.setOnItemSelectedListener(new HobbyItemSelector());

        runCall(userApis.getAllUsers()).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccess()) {
                    users = response.body();
                    // displayUsers(users);
                } else {
                    ErrorHandler.showError(MainActivity.this, response);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                ErrorHandler.showError(MainActivity.this, t);
            }
        });

        Button updateLocationButton = (Button) findViewById(R.id.update_location);
        if (updateLocationButton != null) {
            updateLocationButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    gpsLocation = new GetGpsLocation(MainActivity.this);
                    if (gpsLocation.canGetLocation()) {
                        latitude = gpsLocation.getLatitude();
                        longitude = gpsLocation.getLongitude();
                    } else {
                        gpsLocation.showSettingsAlert();
                    }

                    updateCircle(new LatLng(latitude, longitude));
                    doMapQuery();
                    doListQuery();

                    currentUser.setLatitude(latitude);
                    currentUser.setLongitude(longitude);

                    runCall(userApis.update(currentUser)).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccess()) {
                                Log.d("update", "succes");
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                        }
                    });
                }
            });
        }

        Button transferFileButton = (Button) findViewById(R.id.transfer_button);
        if (transferFileButton != null) {
            transferFileButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!isWifiP2pEnabled) {
                        Toast.makeText(MainActivity.this, R.string.p2p_off_warning, Toast.LENGTH_SHORT).show();
                    }
                    final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
                    fragment.onInitiateDiscovery();
                    manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            Toast.makeText(MainActivity.this, "Discovery Initiated", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int reasonCode) {
                            Toast.makeText(MainActivity.this, "Discovery Failed : " + reasonCode, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        Button searchByHobbyButton = (Button) findViewById(R.id.search_by_hobby_button);
        if (searchByHobbyButton != null) {
            searchByHobbyButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    runCall(userApis.getUsersWithHobby(selectedHobby)).enqueue(new Callback<List<User>>() {
                        @Override
                        public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                            if (response.isSuccess()) {
                                users = response.body();
                                //   displayUsers(users);
                            } else {
                                ErrorHandler.showError(MainActivity.this, response);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<User>> call, Throwable t) {
                            ErrorHandler.showError(MainActivity.this, t);
                        }
                    });
                }
            });
        }

        Button startChatButton = (Button) findViewById(R.id.start_chat);
        if (startChatButton != null) {
            startChatButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, StartChatActivity.class));
                }
            });
        }
    }

    private void displayUsers(List<PeersUser> users) {
        usersAdapter = new UsersAdapter(MainActivity.this, users);
        ListView postsListView = (ListView) findViewById(R.id.posts_listview);
        if (postsListView != null) {
            postsListView.setAdapter(usersAdapter);
            postsListView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    final User user = usersAdapter.getItem(position).getUser();
//                    selectedPostObjectId = user.getUserId();
//                    mapFragment.getMap().animateCamera(
//                            CameraUpdateFactory.newLatLngZoom(new LatLng(user.getLatitude(), user.getLongitude()), 16.0f), new CancelableCallback() {
//                                public void onFinish() {
//                                    Marker marker = mapMarkers.get(user.getUserId());
//                                    if (marker != null) {
//                                        marker.showInfoWindow();
//                                    }
//                                }
//
//                                public void onCancel() {
//                                }
//                            });
//                    Marker marker = mapMarkers.get(user.getUserId());
//                    if (marker != null) {
//                        marker.showInfoWindow();
//                    }
                    WifiP2pDevice device = usersAdapter.getItem(position).getDevice();
                    showDetails(device);
                }
            });
        }
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mapFragment.getMap().setMyLocationEnabled(true);
        mapFragment.getMap().setOnCameraChangeListener(new OnCameraChangeListener() {
            public void onCameraChange(CameraPosition position) {
                doMapQuery();
            }
        });

    }

    private class HobbyItemSelector implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectedHobby = hobbySpinner.getSelectedItem().toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.my_profile:
                Intent myProfileIntent = new Intent(MainActivity.this, MyProfileActivity.class);
                myProfileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(myProfileIntent);
                return true;
            case R.id.settings:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(settingsIntent);
                return true;
            case R.id.log_out:
                Intent logoutIntent = new Intent(MainActivity.this, WelcomeActivity.class);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(logoutIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop() {
        if (locationClient.isConnected()) {
            stopPeriodicUpdates();
        }
        locationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        locationClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, MainActivity.this);
        registerReceiver(receiver, intentFilter);
        radius = Application.getSearchDistance();
        if (lastLocation != null) {
            LatLng myLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

            if (lastRadius != radius) {
                updateZoom(myLatLng);
            }
            updateCircle(myLatLng);
        }
        lastRadius = radius;
        doMapQuery();
        doListQuery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (Application.APPDEBUG) {
                            Log.d(Application.APPTAG, "Connected to Google Play services");
                        }
                        break;
                    default:
                        if (Application.APPDEBUG) {
                            Log.d(Application.APPTAG, "Could not connect to Google Play services");
                        }
                        break;
                }
            default:
                if (Application.APPDEBUG) {
                    Log.d(Application.APPTAG, "Unknown request code received for the activity");
                }
                break;
        }
    }

    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            if (Application.APPDEBUG) {
                Log.d(Application.APPTAG, "Google play services available");
            }
            return true;
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), Application.APPTAG);
            }
            return false;
        }
    }

    public void onConnected(Bundle bundle) {
        if (Application.APPDEBUG) {
            Log.d("Connected to location services", Application.APPTAG);
        }
        currentLocation = getLocation();
        startPeriodicUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(Application.APPTAG, "GoogleApiClient connection has been suspend");
    }

    /*
     * Called by Location Services if the attempt to Location Services fails.
     */
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                if (Application.APPDEBUG) {
                    Log.d(Application.APPTAG, "An error occurred when connecting to location services.", e);
                }
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /*
     * Report location updates to the UI.
     */
    public void onLocationChanged(Location location) {

    }

    /*
     * In response to a request to start updates, send a request to Location Services
     */
    private void startPeriodicUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                locationClient, locationRequest, this);
    }

    /*
     * In response to a request to stop updates, send a request to Location Services
     */
    private void stopPeriodicUpdates() {
        locationClient.disconnect();
    }

    /*
     * Get the current location
     */
    private Location getLocation() {
        if (servicesConnected()) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            return LocationServices.FusedLocationApi.getLastLocation(locationClient);
        } else {
            return null;
        }
    }

    /*
     * Set up a query to update the list view
     */
    private void doListQuery() {
        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
        if (myLoc != null) {
            usersAdapter.notifyDataSetChanged();
        }
    }

    /*
     * Set up the query to update the map view
     */
    private void doMapQuery() {
        final int myUpdateNumber = ++mostRecentMapUpdate;
        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
        if (myLoc == null) {
            cleanUpMarkers(new HashSet<Integer>());
            return;
        }

        if (myUpdateNumber != mostRecentMapUpdate) {
            return;
        }
        Set<Integer> toKeep = new HashSet<>();
        for (User user : users) {
            toKeep.add(user.getUserId());
            Marker oldMarker = mapMarkers.get(user.getUserId());
            MarkerOptions markerOpts = new MarkerOptions().position(new LatLng(user.getLatitude(), user.getLongitude()));
            if (oldMarker != null) {
                if (oldMarker.getSnippet() != null) {
                    continue;
                } else {
                    oldMarker.remove();
                }
            }
            markerOpts = markerOpts.title(user.getUsername()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            Marker marker = mapFragment.getMap().addMarker(markerOpts);
            mapMarkers.put(user.getUserId(), marker);
            if (user.getUserId() == selectedPostObjectId) {
                marker.showInfoWindow();
                selectedPostObjectId = 0;
            }
        }
        cleanUpMarkers(toKeep);
    }

    /*
     * Helper method to clean up old markers
     */
    private void cleanUpMarkers(Set<Integer> markersToKeep) {
        for (int objId : new HashSet<>(mapMarkers.keySet())) {
            if (!markersToKeep.contains(objId)) {
                Marker marker = mapMarkers.get(objId);
                marker.remove();
                mapMarkers.get(objId).remove();
                mapMarkers.remove(objId);
            }
        }
    }

    /*
     * Displays a circle on the map representing the search radius
     */
    private void updateCircle(LatLng myLatLng) {
        if (mapCircle == null) {
            mapCircle = mapFragment.getMap().addCircle(new CircleOptions().center(myLatLng).radius(radius * METERS_PER_FEET));
            int baseColor = Color.DKGRAY;
            mapCircle.setStrokeColor(baseColor);
            mapCircle.setStrokeWidth(2);
            mapCircle.setFillColor(Color.argb(50, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)));
        }
        mapCircle.setCenter(myLatLng);
        mapCircle.setRadius(radius * METERS_PER_FEET); // Convert radius in feet to meters.
    }

    /*
     * Zooms the map to show the area of interest based on the search radius
     */
    private void updateZoom(LatLng myLatLng) {
        LatLngBounds bounds = calculateBoundsWithCenter(myLatLng);
        mapFragment.getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 18));
    }

    /*
     * Helper method to calculate the offset for the bounds used in map zooming
     */
    private double calculateLatLngOffset(LatLng myLatLng, boolean bLatOffset) {
        double latLngOffset = OFFSET_CALCULATION_INIT_DIFF;
        float desiredOffsetInMeters = radius * METERS_PER_FEET;
        float[] distance = new float[1];
        boolean foundMax = false;
        double foundMinDiff = 0;
        do {
            if (bLatOffset) {
                Location.distanceBetween(myLatLng.latitude, myLatLng.longitude, myLatLng.latitude
                        + latLngOffset, myLatLng.longitude, distance);
            } else {
                Location.distanceBetween(myLatLng.latitude, myLatLng.longitude, myLatLng.latitude,
                        myLatLng.longitude + latLngOffset, distance);
            }
            float distanceDiff = distance[0] - desiredOffsetInMeters;
            if (distanceDiff < 0) {
                if (!foundMax) {
                    foundMinDiff = latLngOffset;
                    latLngOffset *= 2;
                } else {
                    double tmp = latLngOffset;
                    latLngOffset += (latLngOffset - foundMinDiff) / 2;
                    foundMinDiff = tmp;
                }
            } else {
                latLngOffset -= (latLngOffset - foundMinDiff) / 2;
                foundMax = true;
            }
        } while (Math.abs(distance[0] - desiredOffsetInMeters) > OFFSET_CALCULATION_ACCURACY);
        return latLngOffset;
    }

    /*
     * Helper method to calculate the bounds for map zooming
     */
    LatLngBounds calculateBoundsWithCenter(LatLng myLatLng) {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        double lngDifference = calculateLatLngOffset(myLatLng, false);
        LatLng east = new LatLng(myLatLng.latitude, myLatLng.longitude + lngDifference);
        builder.include(east);
        LatLng west = new LatLng(myLatLng.latitude, myLatLng.longitude - lngDifference);
        builder.include(west);
        double latDifference = calculateLatLngOffset(myLatLng, true);
        LatLng north = new LatLng(myLatLng.latitude + latDifference, myLatLng.longitude);
        builder.include(north);
        LatLng south = new LatLng(myLatLng.latitude - latDifference, myLatLng.longitude);
        builder.include(south);
        return builder.build();
    }

    /*
     * Show a dialog returned by Google Play services for the connection error code
     */
    private void showErrorDialog(int errorCode) {
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this,
                CONNECTION_FAILURE_RESOLUTION_REQUEST);
        if (errorDialog != null) {
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            errorFragment.setDialog(errorDialog);
            errorFragment.show(getSupportFragmentManager(), Application.APPTAG);
        }
    }

    /*
     * Define a DialogFragment to display the error dialog generated in showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);
        String mac = device.deviceAddress;
        Log.d("MACCCCCCCCCCCC", mac);
    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
            }

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }

        });
    }

    public String macBuilder(String mac) {
        StringBuilder myName = new StringBuilder(mac);
        myName.setCharAt(1, (char) ((int) myName.charAt(1) - 2));
        System.out.println(myName);
        return myName.toString().toUpperCase();
    }

    @Override
    public void getPeersList(WifiP2pDeviceList peerList) {
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        for (WifiP2pDevice device : peers)
            Log.d("NUMAAAAAAAAR", device.deviceAddress);
        peersUsersList.clear();
        for (User user : users) {
            for (WifiP2pDevice device : peers) {
                Log.d("DEVICE ADDRESSSSSSSSSSS", macBuilder(device.deviceAddress));
                Log.d("USER MACCCCCCCC", user.getMac());
                if (user.getMac().equals(macBuilder(device.deviceAddress))) {
                    peersUsersList.add(new PeersUser(user, device));
                    Log.d("egaleeee", user.getUsername());
                }
            }
        }
        displayUsers(peersUsersList);
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this, "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void cancelDisconnect() {

        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
            if (fragment.getDevice() == null || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE || fragment.getDevice().status == WifiP2pDevice.INVITED) {
                manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "Aborting connection",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(MainActivity.this,
                                "Connect abort request failed. Reason Code: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.frag_detail);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }

}
