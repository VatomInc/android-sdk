/*
 *  BlockV AG. Copyright (c) 2018, all rights reserved.
 *
 *  Licensed under the BlockV SDK License (the "License"); you may not use this file or the BlockV SDK except in
 *  compliance with the License accompanying it. Unless required by applicable law or agreed to in writing, the BlockV
 *  SDK distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 *  ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 *
 */
package io.blockv.core.client.manager

import android.net.Uri
import io.blockv.core.internal.repository.Preferences
import io.blockv.core.model.AssetProvider
import io.blockv.core.util.Callable
import java.net.Authenticator

class ResourceManagerImpl(private val preferences: Preferences, private val authenticator: Authenticator) :
  ResourceManager {

  override val assetProviders: List<AssetProvider>?
    get() = preferences.assetProviders

  @Throws(ResourceManager.MissingAssetProviderException::class)
  override fun encodeUrl(url: String): Callable<String> {
    return Callable.single {
      var out = url
      if (assetProviders == null || assetProviders?.size == 0) throw ResourceManager.MissingAssetProviderException()
      for (provider: AssetProvider in assetProviders ?: ArrayList()) {
        if (url.startsWith(provider.uri)) {
          val descriptor: Map<String, String?> = provider.descriptor
          out = encodeParams(url, descriptor)
          break
        }
      }
      out
    }
  }

  private fun encodeParams(url: String, params: Map<String, String?>): String {
    val original = Uri.parse(url)
    val out = Uri.parse(url).buildUpon().clearQuery()
    for (key in params.keys) {
      out.appendQueryParameter(key, params[key])
    }

    for (param in original.queryParameterNames) {
      if (!params.keys.contains(param)) {
        out.appendQueryParameter(param, original.getQueryParameter(param))
      }
    }

    return out.build().toString()
  }


}