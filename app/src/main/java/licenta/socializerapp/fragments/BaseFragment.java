package licenta.socializerapp.fragments;

import android.support.v4.app.Fragment;
import android.view.View;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;

/**
 * Created by Madalina on 5/6/2016.
 */
public class BaseFragment extends Fragment {

    private Set<Call> runningCalls = Collections.synchronizedSet(new HashSet<Call>());

    public <T> Call<T> runCall(Call<T> call) {
        runningCalls.add(call);
        return call;
    }

    @Override
    public void onStop() {
        super.onStop();
        stopRunningCalls();
    }

    private void stopRunningCalls() {
        for (Call call : runningCalls) {
            call.cancel();
        }
    }

    protected void show(View... views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
        }
    }

    protected void hide(View... views) {
        for (View view : views) {
            view.setVisibility(View.GONE);
        }
    }
}
