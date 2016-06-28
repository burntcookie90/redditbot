package io.dwak.reddit.bot.config

import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import okio.Buffer
import java.io.File
import javax.inject.Inject

class ConfigHelper @Inject constructor(private val moshi : Moshi) {
  val redditConfig : RedditConfig
  var slackConfig : SlackConfig

  init {
    val redditConfigResource = javaClass.classLoader.getResource("reddit-config.json")
    val slackConfigResource = javaClass.classLoader.getResource("slack-config.json")
    var redditConfigFile : File? = null
    var slackConfigFile : File? = null
    if (redditConfigResource != null && slackConfigResource != null) {
      redditConfigFile = File(redditConfigResource.file)
      slackConfigFile = File(slackConfigResource.file)
    }
    if (redditConfigFile != null && redditConfigFile.exists()
        && slackConfigFile != null && slackConfigFile.exists()) {
      val redditBuffer = Buffer().readFrom(redditConfigFile.inputStream())
      redditConfig = moshi.adapter(RedditConfig::class.java).fromJson(JsonReader.of(redditBuffer))

      val slackBuffer = Buffer().readFrom(slackConfigFile.inputStream())
      slackConfig = moshi.adapter(SlackConfig::class.java).fromJson(JsonReader.of(slackBuffer))
    }
    else if (System.getenv("subreddit") != null) {
      val subreddit = System.getenv("subreddit")
      val reddit_username = System.getenv("reddit_username")
      val reddit_pwd = System.getenv("reddit_pwd")
      val reddit_client_id = System.getenv("reddit_client_id")
      val reddit_client_secret = System.getenv("reddit_client_secret")
      val slack_key = System.getenv("slack_key")
      val slack_token = System.getenv("slack_token")
      val slack_verification_token = System.getenv("slack_verification_token")
      redditConfig = RedditConfig(subreddit,
                                  reddit_username,
                                  reddit_pwd,
                                  reddit_client_id,
                                  reddit_client_secret)
      slackConfig = SlackConfig(slack_key,
                                slack_token,
                                slack_verification_token)
    }
    else {
      throw IllegalStateException("No config defined: " +
                                  "Please create /resources/reddit-config.json or set the expected environment variables.")
    }
  }
}
