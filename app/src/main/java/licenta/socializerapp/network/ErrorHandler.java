package licenta.socializerapp.network;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.GsonBuilder;

import java.io.IOException;

import licenta.socializerapp.network.ApiError;
import retrofit2.Response;

/**
 * Created by Madalina on 5/6/2016.
 */
public class ErrorHandler {
    public static void showError(Context context, Response response) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        String errorMessage;
        try {
            String errorString = response.errorBody().string();
            errorMessage = gsonBuilder.create().fromJson(errorString, ApiError.class).getErrorMessage();
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage = "Unknown api error";
        }
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
    }

    public static void showError(Context context, Throwable t) {
        String errorString = t.getMessage();
        Toast.makeText(context, errorString, Toast.LENGTH_LONG).show();
    }
}
