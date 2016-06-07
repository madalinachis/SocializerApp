package licenta.socializerapp.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import licenta.socializerapp.R;
import licenta.socializerapp.activities.LoginActivity;
import licenta.socializerapp.dependencies.Injector;
import licenta.socializerapp.model.User;
import licenta.socializerapp.network.ErrorHandler;
import licenta.socializerapp.network.UserApis;
import licenta.socializerapp.utils.GetGpsLocation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Madalina on 5/6/2016.
 */
public class SignUpFragment extends BaseFragment {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText passwordAgainEditText;
    private EditText nameEditText;
    private EditText hobbyEditText;
    UserApis userApis;
    GetGpsLocation gpsLocation;
    double latitude;
    double longitude;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);
        ButterKnife.bind(this, view);
        Injector.init();
        userApis = Injector.getApi(UserApis.class);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        usernameEditText = (EditText) view.findViewById(R.id.username_edit_text);
        nameEditText = (EditText) view.findViewById(R.id.name_edit_text);
        hobbyEditText = (EditText) view.findViewById(R.id.hobby_edit_text);
        passwordEditText = (EditText) view.findViewById(R.id.password_edit_text);
        passwordAgainEditText = (EditText) view.findViewById(R.id.password_again_edit_text);
        Button mActionButton = (Button) view.findViewById(R.id.action_button);
        mActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                signup();
            }
        });
    }

    private void signup() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String passwordAgain = passwordAgainEditText.getText().toString().trim();

        boolean validationError = false;
        StringBuilder validationErrorMessage = new StringBuilder(getString(R.string.error_intro));
        if (username.length() == 0) {
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_blank_username));
        }
        if (password.length() == 0) {
            if (validationError) {
                validationErrorMessage.append(getString(R.string.error_join));
            }
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_blank_password));
        }
        if (!password.equals(passwordAgain)) {
            if (validationError) {
                validationErrorMessage.append(getString(R.string.error_join));
            }
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_mismatched_passwords));
        }
        validationErrorMessage.append(getString(R.string.error_end));

        if (validationError) {
            Toast.makeText(getActivity(), validationErrorMessage.toString(), Toast.LENGTH_LONG).show();
            return;
        }


        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();

        gpsLocation = new GetGpsLocation(getActivity());
        if (gpsLocation.canGetLocation()) {
            latitude = gpsLocation.getLatitude();
            longitude = gpsLocation.getLongitude();
        } else {
            gpsLocation.showSettingsAlert();
        }

        User user = User.create()
                .hobby(hobbyEditText.getText().toString())
                .latitude(latitude)
                .longitude(longitude)
                .mac(macAddress)
                .name(nameEditText.getText().toString())
                .username(username)
                .password(password);

        runCall(userApis.register(user)).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccess()) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
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
}
