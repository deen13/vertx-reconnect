package io.github.deen13.reconnect

import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.mqtt.connectAwait
import io.vertx.kotlin.mqtt.subscribeAwait
import io.vertx.mqtt.MqttClient
import io.vertx.mqtt.MqttClientOptions
import kotlinx.coroutines.delay
import java.lang.Exception

class MainVerticle : CoroutineVerticle() {

  private val mqttClientConfig by lazy { config.getJsonObject("mqtt").getJsonObject("client") }
  private val mqttServerConfig by lazy { config.getJsonObject("mqtt").getJsonObject("server") }
  private val client by lazy { MqttClient.create(vertx, MqttClientOptions(mqttClientConfig)) }

  override suspend fun start() {
    connect()

    client.subscribeAwait("test", 1)

    client.publishHandler {
      println("Received message on topic ${it.topicName()}")
    }
  }

  private suspend fun connect() {
    try {
      client.connectAwait(mqttServerConfig.getInteger("port"), mqttServerConfig.getString("hostname"))
    } catch (e: Exception) {
      println("Connection attempt failed. Waiting 5 seconds until the next reconnect.")
      delay(5_000)
      println("Reconnecting...")

      connect()
    }
  }
}
