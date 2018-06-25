package io.blockv.rx.client.manager

import io.blockv.core.internal.json.JsonModule
import io.blockv.core.internal.net.websocket.WebsocketImpl
import io.blockv.core.internal.net.websocket.WebsocketListener
import io.blockv.core.model.ActivityEvent
import io.blockv.core.model.InventoryEvent
import io.blockv.core.model.StateEvent
import io.blockv.core.model.WebSocketEvent
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.io.IOException

class EventManagerImpl(val webSocket: WebsocketImpl,
                       val jsonModule: JsonModule) : EventManager {

  @Volatile
  private var eventFlowable: Flowable<WebSocketEvent<JSONObject>>? = null

  companion object {
    val NULL_STATE_EVENT = WebSocketEvent<StateEvent>("", "", null)
    val NULL_INVENTORY_EVENT = WebSocketEvent<InventoryEvent>("", "", null)
    val NULL_ACTIVITY_EVENT = WebSocketEvent<ActivityEvent>("", "", null)
  }

  @Synchronized
  override fun getEvents(): Flowable<WebSocketEvent<JSONObject>> {
    if (eventFlowable == null) {
      eventFlowable = connect().share()
    }
    return eventFlowable!!
  }

  override fun getVatomStateEvents(): Flowable<WebSocketEvent<StateEvent>> {
    return getEvents()
      .observeOn(Schedulers.computation())
      .filter { event -> event.type == WebSocketEvent.MessageType.STATE_UPDATE }
      .map { event ->
        var stateEvent = NULL_STATE_EVENT
        if (event.payload != null) {
          val payload = jsonModule.stateEventDeserializer.deserialize(event.payload!!)
          if (payload != null) {
            stateEvent = WebSocketEvent(
              event.messageType,
              event.userId,
              payload)
          }
        }
        stateEvent
      }
      .filter { event -> event !== NULL_STATE_EVENT }
      .observeOn(AndroidSchedulers.mainThread())
  }

  override fun getInventoryEvents(): Flowable<WebSocketEvent<InventoryEvent>> {
    return getEvents()
      .observeOn(Schedulers.computation())
      .filter { event -> event.type == WebSocketEvent.MessageType.INVENTORY }
      .map { event ->
        var stateEvent = NULL_INVENTORY_EVENT
        if (event.payload != null) {
          val payload = jsonModule.inventoryEventDeserializer.deserialize(event.payload!!)
          if (payload != null) {
            stateEvent = WebSocketEvent(
              event.messageType,
              event.userId,
              payload)
          }
        }
        stateEvent
      }
      .filter { event -> event !== NULL_INVENTORY_EVENT }
      .observeOn(AndroidSchedulers.mainThread())
  }

  override fun getActivityEvents(): Flowable<WebSocketEvent<ActivityEvent>> {
    return getEvents()
      .observeOn(Schedulers.computation())
      .filter { event -> event.type == WebSocketEvent.MessageType.ACTIVITY }
      .map { event ->
        var stateEvent = NULL_ACTIVITY_EVENT
        if (event.payload != null) {
          val payload = jsonModule.activityEventDeserializer.deserialize(event.payload!!)
          if (payload != null) {
            stateEvent = WebSocketEvent(
              event.messageType,
              event.userId,
              payload)
          }
        }
        stateEvent
      }
      .filter { event -> event !== NULL_ACTIVITY_EVENT }
      .observeOn(AndroidSchedulers.mainThread())
  }

  private fun connect(): Flowable<WebSocketEvent<JSONObject>> {

    return Flowable.create<WebSocketEvent<JSONObject>>({ subscriber ->
      val listener: WebsocketListener = object : WebsocketListener {
        override fun onEvent(event: WebSocketEvent<JSONObject>) {
          if (!subscriber.isCancelled) {
            subscriber.onNext(event)
          }
        }

        override fun onError(throwable: Throwable) {
          if (!subscriber.isCancelled) {
            subscriber.onError(throwable)
            subscriber.onComplete()
          }
        }

        override fun onDisconnect() {
          if (!subscriber.isCancelled) {
            subscriber.onError(IOException("WebSocket Disconnected"))
            subscriber.onComplete()
          }
        }

      }
      subscriber.setCancellable({ webSocket.disconnect() })
      webSocket.connect(listener)
    }, BackpressureStrategy.BUFFER)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
  }

}