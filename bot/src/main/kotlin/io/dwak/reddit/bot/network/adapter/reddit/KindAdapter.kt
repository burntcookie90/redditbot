package io.dwak.reddit.bot.network.adapter.reddit

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.dwak.reddit.bot.model.reddit.Kind

@Suppress("unused")
class KindAdapter {
  @ToJson fun toJson(kind : Kind) = kind.kind
  @FromJson fun fromJson(kind : String) = Kind.valueOf(kind.toUpperCase())
}