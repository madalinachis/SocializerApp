package licenta.socializerapp.network;

import java.util.List;

import licenta.socializerapp.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by Madalina on 5/6/2016.
 */
public interface UserApis {

    @POST("/androidApp-Api/rest/app/createUser")
    Call<User> register(@Body User user);

    @GET("/androidApp-Api/rest/app/getUser/{username}/{password}")
    Call<User> login(@Path("username") String username, @Path("password") String password);

    @GET("/androidApp-Api/rest/app/getAllUsers")
    Call<List<User>> getAllUsers();

    @POST("/androidApp-Api/rest/app/updateUser")
    Call<Void> update(@Body User user);

    @GET("/androidApp-Api/rest/hobbyService/getUsersWithHobby/{hobby}")
    Call<List<User>> getUsersWithHobby(@Path("hobby") String hobby);
}
