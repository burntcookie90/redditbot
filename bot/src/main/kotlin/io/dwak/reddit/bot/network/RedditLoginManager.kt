package io.dwak.reddit.bot.network

import io.dwak.reddit.bot.config.ConfigHelper
import io.dwak.reddit.bot.config.RedditConfig
import io.dwak.reddit.bot.dagger.OG
import io.dwak.reddit.bot.model.reddit.AuthData
import io.dwak.reddit.bot.network.reddit.RedditLoginService
import java.util.*
import javax.inject.Inject

object RedditLoginManager {
  @field:Inject lateinit var loginService : RedditLoginService
  @field:Inject lateinit var configHelper : ConfigHelper
  val redditConfig : RedditConfig
  private val basicAuth : String
  private var authData : AuthData? = null
  private var lastLoginTime : Long = System.currentTimeMillis()

  init {
    OG.injectStatics()
    redditConfig = configHelper.redditConfig
    basicAuth = "Basic ${Base64.getEncoder()
            .encodeToString(("${redditConfig.redditClientId}:${redditConfig.redditClientSecret}")
                                    .toByteArray())}"
  }

  fun login() {
    val call = loginService.getAccessToken(authorization = basicAuth,
                                           username = redditConfig.redditUsername,
                                           password = redditConfig.redditPassword)
    authData = call.execute().body()
    lastLoginTime = System.currentTimeMillis()
  }

  fun refreshToken() {
    val call = loginService.getAccessToken(authorization = basicAuth,
                                           username = redditConfig.redditUsername,
                                           password = redditConfig.redditPassword)
    authData = call.execute().body()
    lastLoginTime = System.currentTimeMillis()
  }

  fun getAuthToken() : String {
    if (hasValidToken()) {
      return authData!!.accessToken
    }
    else {
      refreshToken()
      return getAuthToken()
    }
  }

  fun hasValidToken() = authData != null
                        && (System.currentTimeMillis() - lastLoginTime) <= authData!!.expiresIn
}