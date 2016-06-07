package licenta.socializerapp.network;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Madalina on 5/6/2016.
 */
public class ApiError {
    @SerializedName("ErrorCode")
    private String errorCode;
    @SerializedName("ErrorMessage")
    private String errorMessage;

    private ApiError(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
