/*
 * Copyright 2010 Rob Fletcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springcache.web.key

import grails.plugin.springcache.CacheKey
import grails.plugin.springcache.key.CacheKeyBuilder

import grails.plugin.springcache.key.KeyGenerator
import grails.plugin.springcache.web.ContentCacheParameters

abstract class AbstractKeyGenerator implements KeyGenerator<ContentCacheParameters> {

	CacheKey generateKey(ContentCacheParameters context) {
		def builder = new CacheKeyBuilder()
		generateKeyInternal(builder, context)
		builder.toCacheKey()
	}

	protected abstract void generateKeyInternal(CacheKeyBuilder builder, ContentCacheParameters context)

}
