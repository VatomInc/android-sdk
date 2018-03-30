/**
 *  BlockV AG. Copyright (c) 2018, all rights reserved.
 *
 *  Licensed under the BlockV SDK License (the "License"); you may not use this file or the BlockV SDK except in
 *  compliance with the License accompanying it. Unless required by applicable law or agreed to in writing, the BlockV
 *  SDK distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 *  ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 *
 */
package io.blockv.core.internal.net.rest.api

import io.blockv.core.internal.json.JsonModule
import io.blockv.core.internal.net.rest.Client
import io.blockv.core.internal.net.rest.request.InventoryRequest
import io.blockv.core.internal.net.rest.request.PerformActionRequest
import io.blockv.core.internal.net.rest.request.VatomRequest
import io.blockv.core.internal.net.rest.response.BaseResponse
import io.blockv.core.model.Action
import io.blockv.core.model.DiscoverGroup
import io.blockv.core.model.Group
import org.json.JSONArray
import org.json.JSONObject

class VatomApiImpl(val client: Client,
                   val jsonModule: JsonModule) : VatomApi {

  override fun getUserVatom(request: VatomRequest): BaseResponse<Group?> {
    val response: JSONObject = client.post("v1/user/vatom/get", request.toJson())
    val payload: JSONObject? = response.optJSONObject("payload")


    return BaseResponse(
      response.optString("status"),
      response.optInt("error"),
      response.optString("message"),
      (if (payload != null) jsonModule.groupDeserilizer.deserialize(payload) else null) ?: Group(ArrayList(), ArrayList(), ArrayList())
    )
  }

  override fun discover(request: JSONObject): BaseResponse<DiscoverGroup?> {
    val response: JSONObject = client.post("v1/vatom/discover", request)
    val payload: JSONObject? = response.optJSONObject("payload")

    return BaseResponse(
      response.optString("status"),
      response.optInt("error"),
      response.optString("message"),
      if (payload != null) jsonModule.discoverDeserilizer.deserialize(payload) else null)
  }


  override fun getUserInventory(request: InventoryRequest): BaseResponse<Group?> {
    val response: JSONObject = client.post("/v1/user/vatom/inventory", request.toJson())
    val payload: JSONObject? = response.optJSONObject("payload")

    return BaseResponse(
      response.optString("status"),
      response.optInt("error"),
      response.optString("message"),
      if (payload != null) jsonModule.groupDeserilizer.deserialize(payload) else null)

  }

  override fun getVatomActions(template: String?): BaseResponse<List<Action>> {
    val response: JSONObject = client.get("v1/user/actions/" + template)
    val payload: JSONArray? = response.optJSONArray("payload")
    val list: ArrayList<Action> = ArrayList()
    if (payload != null) {
      var count = 0
      while (count < payload.length()) {
        val action: Action? = jsonModule.actionDeserilizer.deserialize(payload.getJSONObject(count))
        if (action != null) {
          list.add(action)
        }
        count++
      }
    }
    return BaseResponse(
      response.optString("status"),
      response.optInt("error"),
      response.optString("message"),
      list)
  }

  override fun preformAction(request: PerformActionRequest): BaseResponse<JSONObject?> {
    val response: JSONObject = client.post("v1/user/vatom/action/" + request.action, request.toJson())
    val payload: JSONObject? = response.optJSONObject("payload")
    return BaseResponse(
      response.optString("status"),
      response.optInt("error"),
      response.optString("message"),
      payload)
  }
}