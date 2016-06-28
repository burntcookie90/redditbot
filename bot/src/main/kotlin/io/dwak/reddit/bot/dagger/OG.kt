package io.dwak.reddit.bot.dagger

import dagger.ObjectGraph
import kotlin.properties.Delegates

object OG {
  var objectGraph : ObjectGraph by Delegates.notNull<ObjectGraph>()

  fun initialize(objectGraph : ObjectGraph) {
    this.objectGraph = objectGraph
  }

  fun injectStatics() = objectGraph.injectStatics()

  fun <T> inject(target : T) = objectGraph.inject(target)

  fun <T> get(clazz : Class<T>) = objectGraph.get(clazz)
}