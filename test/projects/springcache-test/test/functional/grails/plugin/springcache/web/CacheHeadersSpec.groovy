package grails.plugin.springcache.web

import grails.plugin.springcache.SpringcacheService
import groovyx.net.http.RESTClient
import org.apache.http.protocol.HttpDateGenerator
import org.codehaus.groovy.grails.commons.ApplicationHolder
import static java.util.concurrent.TimeUnit.HOURS
import static javax.servlet.http.HttpServletResponse.*
import musicstore.*
import net.sf.ehcache.*
import static org.codehaus.groovy.grails.web.servlet.HttpHeaders.*
import spock.lang.*

@Issue("http://jira.codehaus.org/browse/GRAILSPLUGINS-2616")
class CacheHeadersSpec extends Specification {

	@Shared SpringcacheService springcacheService = ApplicationHolder.application.mainContext.springcacheService
	@Shared Ehcache albumControllerCache = ApplicationHolder.application.mainContext.albumControllerCache

	@Shared private Album album
	private RESTClient http = new RESTClient()

	def setupSpec() {
		album = Album.withNewSession {
			Album.build(artist: new Artist(name: "Les Savy Fav"), name: "Root For Ruin", year: "2010")
		}
	}

	def cleanupSpec() {
		Album.withNewSession {
			Album.list()*.delete()
			Artist.list()*.delete()
		}
	}

	def cleanup() {
		springcacheService.flushAll()
		springcacheService.clearStatistics()
	}

	def "cache control headers from the original response are served with a cached response"() {
		given: "the cache is primed by an previous request"
		def response1 = http.get(uri: "http://localhost:8080/album/show/$album.id")

		when: "the same action is invoked again"
		def response2 = http.get(uri: "http://localhost:8080/album/show/$album.id")

		then: "the content is served from the cache"
		response2.status == SC_OK
		albumControllerCache.statistics.cacheHits == 1L

		and: "cache control headers are served with the cached response"
		response1.headers[LAST_MODIFIED].value == response2.headers[LAST_MODIFIED].value
		response1.headers[ETAG].value == response2.headers[ETAG].value
	}

	@Unroll({"a 304 is served rather than a cached response if the client sends #headers"})
	def "a 304 is served rather than a cached response if the client has cached the response"() {
		given: "the cache is primed by an previous request"
		http.get uri: "http://localhost:8080/album/show/$album.id"

		when: "the same action is invoked again with a matching header"
		def response = http."$method"(uri: "http://localhost:8080/album/show/$album.id", headers: headers)

		then: "the server responds with a 304"
		response.status == SC_NOT_MODIFIED

		and: "no content is served"
		response.data == null

		where:
		method | headers
		"get"  | [(IF_MODIFIED_SINCE): currentDate]
		"get"  | [(IF_NONE_MATCH): "$album.id:$album.version"]
		"head" | [(IF_MODIFIED_SINCE): currentDate]
		"head" | [(IF_NONE_MATCH): "$album.id:$album.version"]
	}

	@Unroll({"the cached response is served if the client sends #headers"})
	def "the cached response is served if the client's cached version does not match"() {
		given: "the cache is primed by an previous request"
		def response1 = http.get(uri: "http://localhost:8080/album/show/$album.id")

		when: "the same action is invoked again with a non-matching header"
		def response2 = http.get(uri: "http://localhost:8080/album/show/$album.id", headers: headers)

		then: "the server responds with a 200"
		response2.status == SC_OK

		and: "the cached content is served"
		response1.data == response2.data

		where:
		headers << [[(IF_MODIFIED_SINCE): "Tue, 15 Nov 1994 12:45:26 GMT"], [(IF_NONE_MATCH): "x:x"]]
	}

	def "a cached response's time-to-live is set according to the max-age header if there is one"() {
		when: "a response is cached"
		http.get uri: "http://localhost:8080/album/show/$album.id"

		then: "a cache entry is created"
		albumControllerCache.statistics.objectCount == 1

		and: "the cache entry's ttl is the same as the response's expires header"
		cacheElement.timeToLive == HOURS.toSeconds(1)
	}

	def "a page is not cached if the response contains a no-cache directive in the cache-control header"() {
		when: "an action that sets cache:false is hit"
		http.get uri: "http://localhost:8080/album/random"

		then: "no cache entry is created"
		albumControllerCache.statistics.objectCount == 0
	}

	private Element getCacheElement() {
		def key = albumControllerCache.getKeys().head()
		albumControllerCache.get(key)
	}

	private String getCurrentDate() {
		new HttpDateGenerator().currentDate
	}

}
