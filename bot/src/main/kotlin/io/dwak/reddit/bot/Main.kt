package io.dwak.reddit.bot

import com.spotify.apollo.Environment
import com.spotify.apollo.httpservice.HttpService
import com.spotify.apollo.route.Route
import dagger.ObjectGraph
import io.dwak.reddit.bot.dagger.NetworkModule
import io.dwak.reddit.bot.dagger.OG
import kotlin.properties.Delegates

val SERVICE_NAME = "reddit-bot"

var bot : Bot by Delegates.notNull()
fun main(args : Array<String>) {
  OG.initialize(ObjectGraph.create(NetworkModule()))
  bot = OG.get(Bot::class.java)
  bot.login()

  HttpService.boot(::init, SERVICE_NAME, args)
}

fun init(env : Environment) {
  env.routingEngine().registerAutoRoute(Route.async("GET", "/check-posts", bot.checkPosts()))
  env.routingEngine().registerAutoRoute(Route.async("GET", "/initiate-bot", bot.slackLogin()))
  env.routingEngine().registerAutoRoute(Route.async("POST", "/slackbutton", bot.beginRemovePost()))
}


