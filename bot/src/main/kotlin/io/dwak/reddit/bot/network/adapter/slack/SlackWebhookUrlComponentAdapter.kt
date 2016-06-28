package io.dwak.reddit.bot.network.adapter.slack

import com.squareup.moshi.FromJson
import io.dwak.reddit.bot.model.slack.SlackWebhookUrlComponents

@Suppress("unused")
class SlackWebhookUrlComponentAdapter {
  @FromJson fun fromJson(url : String) : SlackWebhookUrlComponents {
    val splits = url.split("services")
    val ids = splits[1].split("/")
    return SlackWebhookUrlComponents(ids[1], ids[2], ids[3])
  }
}
