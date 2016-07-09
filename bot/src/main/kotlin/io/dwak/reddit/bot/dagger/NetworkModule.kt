package io.dwak.reddit.bot.dagger

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import io.dwak.reddit.bot.Bot
import io.dwak.reddit.bot.config.ConfigHelper
import io.dwak.reddit.bot.network.RedditLoginManager
import io.dwak.reddit.bot.network.RedditOauthInterceptor
import io.dwak.reddit.bot.network.SlackLoginManager
import io.dwak.reddit.bot.network.adapter.reddit.KindAdapter
import io.dwak.reddit.bot.network.adapter.slack.SlackWebhookUrlComponentAdapter
import io.dwak.reddit.bot.network.reddit.RedditLoginService
import io.dwak.reddit.bot.network.reddit.RedditService
import io.dwak.reddit.bot.network.slack.SlackOauthService
import io.dwak.reddit.bot.network.slack.SlackService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import rx.schedulers.Schedulers
import java.util.*
import javax.inject.Named
import javax.inject.Singleton


@Suppress("unused")
@Module(library = true,
        injects = arrayOf(Bot::class, ConfigHelper::class, SlackService::class, Moshi::class),
        staticInjections = arrayOf(RedditLoginManager::class, SlackLoginManager::class))
class NetworkModule {
  companion object {
    const val REDDIT_SERVICE_BASE_URL = "redditServiceBaseUrl"
    const val REDDIT_SERVICE = "redditService"
    const val REDDIT_OKHTTP_BUILDER = "redditOkHttpBuilder"
    const val REDDIT_OKHTTP = "redditOkhttp"

    const val REDDIT_LOGIN_SERVICE_BASE_URL = "redditLoginServiceBaseUrl"
    const val REDDIT_LOGIN_SERVICE = "redditLoginService"
    const val REDDIT_LOGIN_OKHTTP_BUILDER = "redditLoginOkHttpBuilder"
    const val REDDIT_LOGIN_OKHTTP = "redditLoginOkHttp"

    const val SLACK_SERVICE_BASE_URL = "slackServiceBaseUrl"
    const val SLACK_SERVICE = "slackService"
    const val SLACK_OKHTTP_BUILDER = "slackOkHttpBuilder"
    const val SLACK_OKHTTP = "slackOkHttp"

    const val SLACK_OAUTH_SERVICE_BASE_URL = "slackOauthServiceBaseUrl"
    const val SLACK_OAUTH_SERVICE = "slackOauthService"
    const val SLACK_OAUTH_OKHTTP_BUILDER = "slackOauthOkHttpBuilder"
    const val SLACK_OAUTH_OKHTTP = "slackOauthOkHttp"
  }

  @Provides
  @Named(REDDIT_SERVICE_BASE_URL)
  fun redditServiceBaseUrl() = "https://oauth.reddit.com/r/"

  @Provides
  @Named(REDDIT_LOGIN_SERVICE_BASE_URL)
  fun loginServiceBaseUrl() = "https://www.reddit.com/"

  @Provides
  @Named(SLACK_SERVICE_BASE_URL)
  fun slackServiceBaseUrl() = "https://hooks.slack.com/"

  @Provides
  @Named(SLACK_OAUTH_SERVICE_BASE_URL)
  fun slackOauthServiceBaseUrl() = "https://slack.com/api/"

  @Provides
  @Singleton
  fun moshi() : Moshi = Moshi.Builder()
          .add(KindAdapter())
          .add(SlackWebhookUrlComponentAdapter())
          .build()

  @Provides
  @Singleton
  fun moshiFactory(moshi : Moshi) : Converter.Factory
          = MoshiConverterFactory.create(moshi).asLenient()

  @Provides
  fun interceptors() : ArrayList<Interceptor> {
    val interceptors = arrayListOf<okhttp3.Interceptor>()
    val loggingInterceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { println(it) })
    loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    interceptors.add(loggingInterceptor)

    return interceptors
  }

  @Provides
  @Named(REDDIT_LOGIN_OKHTTP)
  fun loginOkHttp(@Named(REDDIT_LOGIN_OKHTTP_BUILDER) okHttpBuilder : OkHttpClient.Builder,
                  interceptors : ArrayList<Interceptor>)
          : OkHttpClient {
    okHttpBuilder.interceptors().addAll(interceptors)
    return okHttpBuilder.build()
  }

  @Provides
  @Named(REDDIT_OKHTTP)
  fun redditOkHttp(@Named(REDDIT_OKHTTP_BUILDER) okHttpBuilder : OkHttpClient.Builder,
                   interceptors : ArrayList<Interceptor>)
          : OkHttpClient {
    okHttpBuilder.interceptors().addAll(interceptors)
    return okHttpBuilder.build()
  }

  @Provides
  @Named(SLACK_OKHTTP)
  fun slackOkHttp(@Named(SLACK_OKHTTP_BUILDER) okHttpBuilder : OkHttpClient.Builder,
                   interceptors : ArrayList<Interceptor>)
          : OkHttpClient {
    okHttpBuilder.interceptors().addAll(interceptors)
    return okHttpBuilder.build()
  }

  @Provides
  @Named(SLACK_OAUTH_OKHTTP)
  fun slackOauthOkHttp(@Named(SLACK_OAUTH_OKHTTP_BUILDER) okHttpBuilder : OkHttpClient.Builder,
                  interceptors : ArrayList<Interceptor>)
          : OkHttpClient {
    okHttpBuilder.interceptors().addAll(interceptors)
    return okHttpBuilder.build()
  }

  @Provides
  @Named(REDDIT_LOGIN_OKHTTP_BUILDER)
  fun loginOkHttpBuilder() : OkHttpClient.Builder {
    return OkHttpClient.Builder()
  }

  @Provides
  @Named(SLACK_OKHTTP_BUILDER)
  fun slackOkHttpBuilder() : OkHttpClient.Builder {
    return OkHttpClient.Builder()
  }

  @Provides
  @Named(SLACK_OAUTH_OKHTTP_BUILDER)
  fun slackOauthOkHttpBuilder() : OkHttpClient.Builder {
    return OkHttpClient.Builder()
  }

  @Provides
  @Named(REDDIT_OKHTTP_BUILDER)
  fun redditOkHttpBuilder(redditOauthInterceptor : RedditOauthInterceptor) : OkHttpClient.Builder {
    val okHttpBuilder = OkHttpClient.Builder()
    okHttpBuilder.addInterceptor(redditOauthInterceptor)
    return okHttpBuilder
  }

  @Provides
  fun adapterFactory() : CallAdapter.Factory = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io())

  @Provides
  @Singleton
  @Named(REDDIT_SERVICE)
  fun redditServiceRetrofit(converterFactory : Converter.Factory,
                            callAdapterFactory : CallAdapter.Factory,
                            @Named(REDDIT_OKHTTP) client : OkHttpClient,
                            @Named(REDDIT_SERVICE_BASE_URL) baseUrl : String) : Retrofit
          = Retrofit.Builder()
          .addConverterFactory(converterFactory)
          .addCallAdapterFactory(callAdapterFactory)
          .client(client)
          .baseUrl(baseUrl)
          .build()

  @Provides
  @Singleton
  @Named(REDDIT_LOGIN_SERVICE)
  fun loginServiceRetrofit(converterFactory : Converter.Factory,
                           callAdapterFactory : CallAdapter.Factory,
                           @Named(REDDIT_LOGIN_OKHTTP) client : OkHttpClient,
                           @Named(REDDIT_LOGIN_SERVICE_BASE_URL) baseUrl : String) : Retrofit
          = Retrofit.Builder()
          .addConverterFactory(converterFactory)
          .addCallAdapterFactory(callAdapterFactory)
          .client(client)
          .baseUrl(baseUrl)
          .build()

  @Provides
  @Singleton
  @Named(SLACK_SERVICE)
  fun slackServiceRetrofit(converterFactory : Converter.Factory,
                           callAdapterFactory : CallAdapter.Factory,
                           @Named(SLACK_OKHTTP) client : OkHttpClient,
                           @Named(SLACK_SERVICE_BASE_URL) baseUrl : String) : Retrofit
          = Retrofit.Builder()
          .addConverterFactory(converterFactory)
          .addCallAdapterFactory(callAdapterFactory)
          .client(client)
          .baseUrl(baseUrl)
          .build()

  @Provides
  @Singleton
  @Named(SLACK_OAUTH_SERVICE)
  fun slackOauthServiceRetrofit(converterFactory : Converter.Factory,
                           callAdapterFactory : CallAdapter.Factory,
                           @Named(SLACK_OAUTH_OKHTTP) client : OkHttpClient,
                           @Named(SLACK_OAUTH_SERVICE_BASE_URL) baseUrl : String) : Retrofit
          = Retrofit.Builder()
          .addConverterFactory(converterFactory)
          .addCallAdapterFactory(callAdapterFactory)
          .client(client)
          .baseUrl(baseUrl)
          .build()

  @Provides
  @Singleton
  fun redditService(@Named(REDDIT_SERVICE) retrofit : Retrofit) = retrofit.create(RedditService::class.java)

  @Provides
  @Singleton
  fun loginService(@Named(REDDIT_LOGIN_SERVICE) retrofit : Retrofit) = retrofit.create(RedditLoginService::class.java)

  @Provides
  @Singleton
  fun slackServices(@Named(SLACK_SERVICE) retrofit : Retrofit) = retrofit.create(SlackService::class.java)

  @Provides
  @Singleton
  fun slackOauthServices(@Named(SLACK_OAUTH_SERVICE) retrofit : Retrofit) = retrofit.create(SlackOauthService::class.java)
}
