package com.example.bookbuffet.api
import com.example.bookbuffet.constant.Constants.Companion.BASE_URL
import com.example.bookbuffet.model.Books
import com.example.bookbuffet.model.RentRequests
import com.example.bookbuffet.model.Users
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

interface ApiInterface {
    @GET("api/Users/Email")
    fun getUser(
        @Query("email") email: String
    ) : Call<Users>

    @GET("api/Users/UserId")
    fun getUserByUserId(
        @Query("userId") userId: String
    ) : Call<Users>

    @Headers("Content-Type:application/json")
    @POST("api/Users")
    fun postUser(
        @Body users: Users
    ) : Call<Users>

    @Headers("Content-Type:application/json")
    @PUT("api/Users/{id}")
    fun putUser(
        @Body users: Users,
        @Path("id") id: Int
    ) : Call<Users>

    @Headers("Content-Type:application/json")
    @PUT("api/Books/{id}")
    fun putBook(
        @Body books: Books,
        @Path("id") id: Int
    ) : Call<Books>

    @Headers("Content-Type:application/json")
    @DELETE("api/Books/{id}")
    fun deleteBook(
        @Path("id") id: Int
    ) : Call<Books>

    @GET("api/Books/{id}")
    fun getBook(
        @Path("id") id: Int,
    ) : Call<Books>

    @GET("api/Books/Search")
    fun getBooksBySearch(
        @Query("title") title: String
    ) : Call<List<Books>>

    @GET("api/Books/User")
    fun getBooksByUserId(
        @Query("userId") userId: String
    ) : Call<List<Books>>

    @GET("api/RentRequests/Requester/Get")
    fun getRentRequestsByRequesterId(
        @Query("userId") userId: String,
        @Query("status") status: String
    ) : Call<List<RentRequests>>

    @GET("api/RentRequests/Receiver")
    fun getRentRequestsByReceiverId(
        @Query("userId") userId: String,
        @Query("status") status: String
    ) : Call<List<RentRequests>>

    @Headers("Content-Type:application/json")
    @POST("api/Books")
    fun postBook(
        @Body books: Books
    ) : Call<Books>

    @Headers("Content-Type:application/json")
    @POST("api/RentRequests")
    fun postRentRequest(
        @Body rentRequests: RentRequests
    ) : Call<RentRequests>

    @Headers("Content-Type:application/json")
    @PUT("api/RentRequests/{id}")
    fun putRentRequest(
        @Body rentRequests: RentRequests,
        @Path("id") id: Int
    ) : Call<RentRequests>

    companion object {
        fun create() : ApiInterface {

            val okHttpClient = OkHttpClient.Builder().build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(okHttpClient)
                .build()
            return retrofit.create(ApiInterface::class.java)
        }
    }
}