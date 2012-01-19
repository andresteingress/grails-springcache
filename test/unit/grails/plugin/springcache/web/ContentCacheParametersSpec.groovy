/*
 * Copyright 2010 Grails Plugin Collective
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springcache.web

import grails.plugin.spock.UnitSpec
import grails.util.GrailsNameUtils
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import spock.lang.Unroll
import org.codehaus.groovy.grails.commons.*

class ContentCacheParametersSpec extends UnitSpec {

	def setup() {
		// set up the controllers as artefacts
		def application = Mock(GrailsApplication)
		[TestController].each { controllerClass ->
			def name = GrailsNameUtils.getLogicalPropertyName(controllerClass.name, "Controller")
			def artefact = new DefaultGrailsControllerClass(controllerClass)
			application.getArtefactByLogicalPropertyName("Controller", name) >> artefact
		}
		ApplicationHolder.application = application
	}

	@Unroll({"controller is #expectedController and action is #expectedAction when controllerName is '#controllerName' and actionName is '#actionName'"})
	def "controller and action are identified based on the request context"() {
		given:
		def webRequest = Mock(GrailsWebRequest)
		webRequest.controllerName >> controllerName
		webRequest.actionName >> actionName

		when:
		def cacheParameters = new ContentCacheParameters(webRequest)

		then:
		cacheParameters.controller?.clazz == expectedController
		cacheParameters.action?.name == expectedActionName

		where:
		controllerName | actionName | expectedController | expectedActionName
		null           | null       | null               | null
		"test"         | "index"    | TestController     | "index"
		"test"         | "list"     | TestController     | "list"
		"test"         | null       | TestController     | "index"
		"test"         | "blah"     | TestController     | null
	}
}

class TestController {

	def index = {}
	def list = {}

}
