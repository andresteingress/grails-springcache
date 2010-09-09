/*
 * Copyright 2009 Rob Fletcher
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
package grails.plugin.springcache.aop

import grails.plugin.springcache.SpringcacheService
import grails.plugin.springcache.annotations.CacheFlush
import spock.lang.*

class FlushingAspectSpec extends Specification {

	def aspect = new FlushingAspect()

	def "all specified caches are flushed"() {
		given:
		aspect.springcacheService = Mock(SpringcacheService)
		
		and:
		def annotation = [value: {-> ["cache1", "cache2"] as String[] }] as CacheFlush

		when:
		aspect.flushCaches(annotation)
		
		then:
		1 * aspect.springcacheService.flush(["cache1", "cache2"])
	}
}
