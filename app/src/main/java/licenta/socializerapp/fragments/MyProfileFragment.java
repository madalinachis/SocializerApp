package licenta.socializerapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import licenta.socializerapp.R;
import licenta.socializerapp.dependencies.Injector;
import licenta.socializerapp.network.UserApis;

/**
 * Created by Madalina on 5/12/2016.
 */
public class MyProfileFragment extends BaseFragment {

    UserApis userApis;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_myprofile, container, false);
        ButterKnife.bind(this, view);
        Injector.init();
        userApis = Injector.getApi(UserApis.class);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
