package io.dwak.reddit.bot.model.slack

import com.squareup.moshi.Json

data class SlackAccessToken(@Json(name = "access_token") val accessToken : String,
                            val scope : String)

