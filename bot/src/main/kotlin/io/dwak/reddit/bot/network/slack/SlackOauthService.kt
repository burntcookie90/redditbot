package io.dwak.reddit.bot.network.slack

import io.dwak.reddit.bot.model.slack.SlackOauthResponse
import io.dwak.reddit.bot.network.SlackLoginManager
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SlackOauthService {
  @FormUrlEncoded
  @POST("oauth.access")
  fun getOauth(@Field("client_id") clientId : String = SlackLoginManager.configHelper.slackConfig.slackKey,
               @Field("client_secret") clientSecret : String = SlackLoginManager.configHelper.slackConfig.slackToken,
               @Field("code") code : String)
          : Call<SlackOauthResponse>
}