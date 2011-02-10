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
package grails.plugin.springcache.web

import org.apache.commons.lang.time.StopWatch
import org.slf4j.LoggerFactory

class Timer {

	private final log = LoggerFactory.getLogger(getClass())
	private final uri
	private final stopWatch = new StopWatch()

	Timer(String uri) {
		this.uri = uri
	}
	
	void start() {
		if (log.isInfoEnabled()) {
			stopWatch.start()
		}
	}

	void stop(boolean cached) {
		if (log.isInfoEnabled()) {
			stopWatch.stop()
			log.info "${cached ? 'Cached' : 'Uncached'} request for $uri took $stopWatch"
		}
	}

}
