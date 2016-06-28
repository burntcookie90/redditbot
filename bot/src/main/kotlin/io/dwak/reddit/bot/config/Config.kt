package io.dwak.reddit.bot.config

import com.squareup.moshi.Json
import io.dwak.reddit.bot.model.slack.SlackWebhookUrlComponents

data class RedditConfig(
        val subreddit : String,
        @Json(name = "reddit_username") val redditUsername : String,
        @Json(name = "reddit_pwd") val redditPassword : String,
        @Json(name = "reddit_client_id") val redditClientId : String,
        @Json(name = "reddit_client_secret") val redditClientSecret : String)

data class SlackConfig(
        @Json(name = "slack_key") val slackKey : String,
        @Json(name = "slack_token") val slackToken : String,
        @Json(name = "slack_verification_token") val slackVerificationToken : String,
        @Json(name = "channel") val channel : String? = null,
        @Json(name = "webhook_url") val webHookUrl : SlackWebhookUrlComponents? = null,
        @Json(name = "access_token") val accessToken : String? = null)