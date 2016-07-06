package io.dwak.reddit.bot

import io.dwak.reddit.bot.network.RedditLoginManager
import javax.inject.Inject

data class Response(val displayName : String, val message : String)

class CannedResponses @Inject constructor() {
  val responses = hashMapOf<String, Response>()

  init {
    with(responses) {
      put("questions_thread",
          Response("Questions Thread",
                   "Removed because, per sub rules, this doesn't merit its own post. " +
                   "We have a questions thread every day, please use it for questions like this."))
      put("rules",
          Response("Rules",
                   "Removed because posts like this are against the sub rules."))
      put("wiki",
          Response("Wiki",
                   "Removed because relevant information can be found in the /r/androiddev " +
                   "[wiki](https://www.reddit.com/r/androiddev/wiki/index))"))
      put("spam",
          Response("Spam",
                   "Removed as spam"))
    }
  }
}

val Response.messageWithFooter: String
  get() = message + "\n\nNote: this is a bot, please direct all messages to the moderators of " +
          "/r/${RedditLoginManager.redditConfig.subreddit} via " +
          "[message](https://www.reddit.com/message/compose?to=%2Fr%2F${RedditLoginManager.redditConfig.subreddit})"
