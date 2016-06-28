package io.dwak.reddit.bot.network

import io.dwak.reddit.bot.config.ConfigHelper
import io.dwak.reddit.bot.config.SlackConfig
import io.dwak.reddit.bot.dagger.OG
import io.dwak.reddit.bot.network.slack.SlackOauthService
import javax.inject.Inject

object SlackLoginManager {
  @field:Inject lateinit var loginService : SlackOauthService
  @field:Inject lateinit var configHelper : ConfigHelper
  var slackConfig : SlackConfig private set

  init {
    OG.injectStatics()
    slackConfig = configHelper.slackConfig
  }

  fun login(code : String) {
    val call = loginService.getOauth(code = code)
    val response = call.execute().body()
    slackConfig = slackConfig.copy(accessToken = response.accessToken,
                                   channel = response.incomingWebHook.channel,
                                   webHookUrl = response.incomingWebHook.url)
  }
}

