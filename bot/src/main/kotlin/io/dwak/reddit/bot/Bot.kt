package io.dwak.reddit.bot

import com.spotify.apollo.RequestContext
import com.squareup.moshi.Moshi
import io.dwak.reddit.bot.model.reddit.T3Data
import io.dwak.reddit.bot.model.reddit.isSelfPost
import io.dwak.reddit.bot.model.reddit.isSuspiciousPost
import io.dwak.reddit.bot.model.slack.*
import io.dwak.reddit.bot.network.RedditLoginManager
import io.dwak.reddit.bot.network.SlackLoginManager
import io.dwak.reddit.bot.network.reddit.RedditService
import io.dwak.reddit.bot.network.slack.SlackService
import rx.Observable
import rx.Subscription
import java.net.URLDecoder
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class Bot @Inject constructor(private val lazyRedditService : dagger.Lazy<RedditService>,
                              private val lazySlackService : dagger.Lazy<SlackService>,
                              private val moshi : Moshi,
                              private val cannedResponses : CannedResponses) {
  companion object {
    val ACTION_FLAIR = "flair"
    val ACTION_REMOVE = "remove"
    val POST_WINDOW = 10L
    val CACHE_SIZE = 10
    val ACTION_SELECT_FLAIR = "select-flair"
  }

  private val redditService by lazy { lazyRedditService.get() }
  private val slackService by lazy { lazySlackService.get() }
  private val postedIds : LinkedHashMap<String, T3Data>

  private var lastCheckedTime : ZonedDateTime
  private var redditPollSubscription : Subscription? = null


  init {
    lastCheckedTime = ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(POST_WINDOW)

    postedIds = object : LinkedHashMap<String, T3Data>(CACHE_SIZE) {
      override fun removeEldestEntry(eldest : MutableMap.MutableEntry<String, T3Data>?) : Boolean {
        return size >= CACHE_SIZE
      }
    }
  }

  fun login() = RedditLoginManager.login()

  private fun beginPollingForPosts() {
    redditPollSubscription?.let {
      it.unsubscribe()
      redditPollSubscription = null
    }
    redditPollSubscription = Observable.interval(0, POST_WINDOW, TimeUnit.MINUTES)
            .flatMap {
              redditService.unmoderated(RedditLoginManager.redditConfig.subreddit)
            }
            .map { it.data }
            .flatMap { Observable.from(it.children) }
            .map { it.data }
            .filter {
              val createdUtc = ZonedDateTime.of(LocalDateTime.ofEpochSecond(it.created_utc, 0, ZoneOffset.UTC),
                                                ZoneOffset.UTC)
              createdUtc.isAfter(lastCheckedTime)
            }
            .filter { !postedIds.containsKey(it.id) }
            .doOnNext { postedIds.put(it.id, it) }
            .map {
              var postBody : String? = null
              if (it.isSelfPost()) {
                postBody = it.selftext
              }

              var title = "*Title*: ${it.title}"
              if (it.isSuspiciousPost()) {
                title = "*SUSPICIOUS POST*\n $title"
              }
              WebHookPayload(title,
                             listOf(
                                     WebHookPayloadAttachment(
                                             "Author: <https://www.reddit.com/u/${it.author}|${it.author}>" +
                                             "\n<https://www.reddit.com${it.permalink}|Post Link>" +
                                             "\nID: ${it.id}" +
                                             "\nPost Body: ${postBody ?: "Link Post"}",
                                             "can't remove",
                                             it.id,
                                             "default",
                                             listOf(WebHookPayloadAction(ACTION_REMOVE,
                                                                         "Remove",
                                                                         "button",
                                                                         ACTION_REMOVE),
                                                    WebHookPayloadAction(ACTION_FLAIR,
                                                                         "Flair",
                                                                         "button",
                                                                         ACTION_FLAIR)
                                             )
                                     )
                             )
              )
            }
            .map { moshi.adapter(WebHookPayload::class.java).toJson(it) }
            .flatMap { slackService.postToWebHook(payload = it) }
            .subscribe {
              lastCheckedTime = ZonedDateTime.now(ZoneOffset.UTC)
            }
  }

  fun slackLogin() : (RequestContext) -> CompletableFuture<String> {
    return {
      completableFuture(it, { rc, cf ->
        val code = rc.request().parameter("code").orElse("")
        if (code.isEmpty()) {
          cf.complete("Error logging in!")
        }
        SlackLoginManager.login(code)
        beginPollingForPosts()
        cf.complete("You've authorized with RedditBot, it's going to post into ${SlackLoginManager.slackConfig.channel}")
      })
    }
  }

  fun buttonPressed() : (RequestContext) -> CompletableFuture<String> {
    return {
      completableFuture(it, { rc, cf ->
        val responsePayloadObservable = Observable.just(it.request().payload().get().utf8())
                .map { it.substring(8) } //Slack prepends payload with `payload=` thanks Slack.
                .map { URLDecoder.decode(it, "UTF-8") }
                .map {
                  moshi.adapter(SlackMessagePayload::class.java)
                          .lenient()
                          .fromJson(it)
                }
                .filter { it.token == SlackLoginManager.slackConfig.slackVerificationToken }
                .share()

        responsePayloadObservable.subscribe { println(it) }
        handleRemove(responsePayloadObservable)
        handleCannedResponse(responsePayloadObservable)
        handleFlair(responsePayloadObservable)
        handleApplyFlair(responsePayloadObservable)

        cf.complete("")
      })
    }
  }

  private fun handleCannedResponse(responsePayloadObservable : Observable<SlackMessagePayload>) {
    responsePayloadObservable
            .filter { payload : SlackMessagePayload ->
              cannedResponses.responses.containsKey(payload.actions[0].value)
            }
            .take(1)
            .map {
              Pair(cannedResponses.responses[it.actions[0].value]!!, it)
            }
            .flatMap {
              responseSlackMessagePayloadPair : Pair<Response, SlackMessagePayload> ->
              val fullName = "t3_${responseSlackMessagePayloadPair.second.callbackId}"
              val isSpam = responseSlackMessagePayloadPair.first.displayName == "Spam"
              val removePostObservable = redditService.removePost(fullName, isSpam)
              if (isSpam) {
                removePostObservable.map {
                  responseSlackMessagePayloadPair
                }
              }
              else {
                removePostObservable
                        .flatMap {
                          redditService.postComment(thingId = fullName,
                                                    text = responseSlackMessagePayloadPair.first.messageWithFooter)
                        }
                        .flatMap {
                          redditService.distinguish(id = it.json.data.things[0].data.name)
                        }
                        .map {
                          responseSlackMessagePayloadPair
                        }
              }
            }
            .map {
              val originalMessage = it.second.originalMessage
              val newMessage = originalMessage.copy(attachments = listOf(
                      WebHookPayloadAttachment(text = "${originalMessage.attachments[0].text}" +
                                                      "\nRemoved by ${it.second.user.name} for ${it.first.displayName}!",
                                               fallback = "Removed!",
                                               callback_id = it.second.callbackId,
                                               actions = emptyList())))
              Pair(it.second.responseUrl, newMessage)
            }
            .map(payloadToJson())
            .map(getWebHookUrlComponents())
            .flatMap(respondToSlackMessage())
            .subscribe { println("Done!") }
  }

  private fun handleRemove(responsePayloadObservable : Observable<SlackMessagePayload>) {
    responsePayloadObservable.filter { it.actions[0].value == ACTION_REMOVE }
            .take(1)
            .map {
              val originalMessage = it.originalMessage
              val originalAttachment = originalMessage.attachments[0]
              val newActionsList = arrayListOf<WebHookPayloadAction>()
              cannedResponses.responses.forEach {
                newActionsList.add(WebHookPayloadAction(it.value.displayName, it.value.displayName, value = it.key))
              }
              val newMessage = originalMessage.copy(attachments =
                                                    listOf(originalAttachment.copy(actions = newActionsList)))
              Pair(it.responseUrl, newMessage)
            }
            .map(payloadToJson())
            .map(getWebHookUrlComponents())
            .flatMap(respondToSlackMessage())
            .subscribe {
              println("Posted to slack!")
            }
  }

  private fun handleFlair(responsePayloadObservable : Observable<SlackMessagePayload>) {
    responsePayloadObservable.filter { it.actions[0].value == ACTION_FLAIR }
            .take(1)
            .flatMap { slackPayload ->
              redditService.flairSelector(RedditLoginManager.redditConfig.subreddit,
                                          "t3_${slackPayload.callbackId}")
                      .map { Pair(slackPayload, it) }
            }
            .map {
              val slackPayload = it.first
              val flairResponse = it.second
              val originalMessage = slackPayload.originalMessage
              val originalAttachment = originalMessage.attachments[0]
              val newActionsList = arrayListOf<WebHookPayloadAction>()
              flairResponse.choices
                      .forEach {
                        newActionsList.add(WebHookPayloadAction(name = it.flairTemplateId,
                                                                text = it.flairText,
                                                                value = ACTION_SELECT_FLAIR))
                      }
              val newMessage = originalMessage.copy(attachments =
                                                    listOf(originalAttachment.copy(actions = newActionsList)))
              Pair(slackPayload.responseUrl, newMessage)
            }
            .map(payloadToJson())
            .map(getWebHookUrlComponents())
            .flatMap(respondToSlackMessage())
            .subscribe { println("Posted flairs to slack!") }
  }

  private fun handleApplyFlair(responsePayloadObservable : Observable<SlackMessagePayload>) {
    responsePayloadObservable.filter { it.actions[0].value == ACTION_SELECT_FLAIR }
            .take(1)
            .flatMap { slackPayload ->
              redditService.selectFlair(subreddit = RedditLoginManager.redditConfig.subreddit,
                                        flairTemplateId = slackPayload.actions[0].name,
                                        fullname = "t3_${slackPayload.callbackId}",
                                        username = RedditLoginManager.redditConfig.redditUsername)
                      .map { slackPayload }
            }
            .map {
              val originalMessage = it.originalMessage
              val newMessage = originalMessage.copy(attachments = listOf(
                      WebHookPayloadAttachment(text = "${originalMessage.attachments[0].text}" +
                                                      "\nFlaired by ${it.user.name}!",
                                               fallback = "Flaired!",
                                               callback_id = it.callbackId,
                                               actions = emptyList())))
              Pair(it.responseUrl, newMessage)
            }
            .map(payloadToJson())
            .map(getWebHookUrlComponents())
            .flatMap(respondToSlackMessage())
            .subscribe { println("Flaired!!") }
  }

  private fun respondToSlackMessage() : (Pair<SlackWebhookUrlComponents, String>) -> Observable<Unit> {
    return {
      slackService.respondToMessage(it.first.id1,
                                    it.first.id2,
                                    it.first.id3,
                                    it.second)
    }
  }

  private fun getWebHookUrlComponents() : (Pair<String, String>) -> Pair<SlackWebhookUrlComponents, String> {
    return {
      val splits = it.first.split("actions")
      val ids = splits[1].split("/")
      Pair(SlackWebhookUrlComponents(ids[1], ids[2], ids[3]), it.second)
    }
  }

  private fun payloadToJson() : (Pair<String, WebHookPayload>) -> Pair<String, String> = { Pair(it.first, moshi.adapter(WebHookPayload::class.java).toJson(it.second)) }

  fun checkPosts() : (RequestContext) -> CompletableFuture<String> {
    return {
      completableFuture(it, { rc, cf ->
        beginPollingForPosts()
        cf.complete("k")
      })
    }
  }
}

fun completableFuture(rc : RequestContext,
                      f : (RequestContext, CompletableFuture<String>) -> Unit)
        : CompletableFuture<String> {
  val stage = CompletableFuture<String>()
  f(rc, stage)
  return stage
}