package io.dwak.reddit.bot.model.slack

import com.squareup.moshi.Json

data class SlackMessagePayload(val actions : List<SlackMessagePayloadAction>,
                               @Json(name = "callback_id") val callbackId : String,
                               val token : String,
                               @Json(name = "original_message") val originalMessage : WebHookPayload,
                               @Json(name = "response_url") val responseUrl : String)

data class SlackMessagePayloadAction(val name : String,
                                     val value  : String)

