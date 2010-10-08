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

import grails.plugin.springcache.key.CacheKeyBuilder
import grails.plugin.springcache.web.FilterContext

/**
 * A key generator that simply uses controller name, action name and request parameters
 * as the basis of the key.
 */
class DefaultKeyGenerator extends AbstractKeyGenerator {

	protected void generateKeyInternal(CacheKeyBuilder builder, FilterContext context) {
		builder << context.controllerName
		builder << context.actionName
		context.params?.sort { it.key }?.each { entry ->
			if (!(entry.key in ["controller", "action"])) {
				builder << entry
			}
		}
	}
	
}
