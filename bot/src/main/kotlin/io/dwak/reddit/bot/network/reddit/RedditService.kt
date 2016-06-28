package io.dwak.reddit.bot.network.reddit

import io.dwak.reddit.bot.model.reddit.RedditCommentResponse
import io.dwak.reddit.bot.model.reddit.RedditListing
import retrofit2.http.*
import rx.Observable

interface RedditService {
  @GET("{subreddit}/about/unmoderated")
  fun unmoderated(@Path("subreddit") subreddit : String) : Observable<RedditListing>

  @FormUrlEncoded
  @POST("/api/remove")
  fun removePost(@Field("id") id : String,
                 @Field("spam") spam : Boolean) : Observable<Unit>

  @FormUrlEncoded
  @POST("/api/comment")
  fun postComment(@Field("api_type") apiType : String = "json",
                  @Field("thing_id") thingId : String,
                  @Field("text") text : String) : Observable<RedditCommentResponse>

  @FormUrlEncoded
  @POST("/api/distinguish")
  fun distinguish(@Field("api_type") apiType : String = "json",
                  @Field("id") id : String,
                  @Field("how") how : String = "yes") : Observable<Unit>
}