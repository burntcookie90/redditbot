package io.dwak.reddit.bot.network.slack

import io.dwak.reddit.bot.network.SlackLoginManager
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path
import rx.Observable

interface SlackService {
  @FormUrlEncoded
  @POST("/services/{id1}/{id2}/{id3}")
  fun postToWebHook(@Path("id1") id1 : String = SlackLoginManager.slackConfig.webHookUrl!!.id1,
                    @Path("id2") id2 : String = SlackLoginManager.slackConfig.webHookUrl!!.id2,
                    @Path("id3") id3 : String = SlackLoginManager.slackConfig.webHookUrl!!.id3,
                    @Field("payload") payload : String)
          : Observable<String>

  @FormUrlEncoded
  @POST("/actions/{id1}/{id2}/{id3}")
  fun respondToMessage(@Path("id1") id1 : String,
                       @Path("id2") id2 : String,
                       @Path("id3") id3 : String,
                       @Field("payload") payload : String)
  : Observable<Unit>
}