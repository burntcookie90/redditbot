package io.dwak.reddit.bot.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OauthInterceptor @Inject constructor(): Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain : Interceptor.Chain) : Response {
    val builder = chain.request().newBuilder()

    builder.header("Authorization", "bearer ${RedditLoginManager.getAuthToken()}")
    return chain.proceed(builder.build())
  }
}
