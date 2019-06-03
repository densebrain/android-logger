package org.densebrain.android.logging

open class Event<T> {
  private var handlers = listOf<(T) -> Unit>()

  infix fun on(handler: (T) -> Unit) {
    synchronized(this) {
      handlers = handlers + handler
    }
  }

  infix fun off(handler: (T) -> Unit) {
    synchronized(this) {
      handlers = handlers.filter { it != handler }
    }
  }

  fun emit(event: T) {
    lateinit var handlers:List<(T) -> Unit>

    synchronized(this) {
      handlers = this.handlers.toList()
    }

    for (handler in handlers) {
      handler(event)
    }
  }
}

