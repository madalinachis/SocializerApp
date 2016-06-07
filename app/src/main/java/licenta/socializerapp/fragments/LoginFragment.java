package licenta.socializerapp.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import licenta.socializerapp.R;
import licenta.socializerapp.activities.LoginActivity;
import licenta.socializerapp.activities.MainActivity;
import licenta.socializerapp.dependencies.Injector;
import licenta.socializerapp.model.User;
import licenta.socializerapp.network.ErrorHandler;
import licenta.socializerapp.network.UserApis;
import licenta.socializerapp.utils.GetGpsLocation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Madalina on 5/7/2016.
 */
public class LoginFragment extends BaseFragment {

    @Bind(R.id.username)
    EditText usernameEditText;
    @Bind(R.id.password)
    EditText passwordEditText;
    UserApis userApis;
    User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstaceState) {
        super.onCreate(savedInstaceState);
        Injector.init();
        userApis = Injector.getApi(UserApis.class);
    }

    @OnClick(R.id.action_button)
    public void doLogin() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        login(username, password);
    }

    private void login(String username, String password) {

        runCall(userApis.login(username, password)).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccess()) {
                    user = response.body();
                    if (user != null) {
                        updateUser();
                    } else {
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                    }
                } else {
                    ErrorHandler.showError(getActivity(), response);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                ErrorHandler.showError(getActivity(), t);
            }
        });

    }

    private void updateUser() {

        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();

        GetGpsLocation gpsLocation;
        double latitude;
        double longitude;

        gpsLocation = new GetGpsLocation(getActivity());
        latitude = gpsLocation.getLatitude();
        longitude = gpsLocation.getLongitude();

        user.setMac(macAddress);
        user.setLatitude(latitude);
        user.setLongitude(longitude);

        runCall(userApis.update(user)).enqueue(new Callback<Void>() {
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

        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("currentUser", user);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
