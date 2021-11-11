package io.blockv.common.model

import java.util.*

class OauthData(
  val user: User,
  val flow: Flow
) : Model {

  override fun toString(): String {
    return "OauthData{" +
      " user='" + user + '\'' +
      ", flow='" + flow + '\'' +
      "}"
  }

  enum class Flow(val value: String) {

    LOGIN("login"),
    REGISTER("register"),
    OTHER("other");

    companion object {
      fun from(flow: String): Flow {
          return when (flow.lowercase(Locale.getDefault())) {
              "login" -> LOGIN
              "register" -> REGISTER
              else -> OTHER
          }
      }
    }
  }
}