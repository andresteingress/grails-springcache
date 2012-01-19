package grails.plugin.springcache.web

import grails.plugin.springcache.SpringcacheService
import groovyx.net.http.RESTClient
import net.sf.ehcache.Ehcache
import org.apache.http.HttpStatus
import org.codehaus.groovy.grails.commons.ApplicationHolder
import spock.lang.*

@Issue("http://jira.grails.org/browse/GPSPRINGCACHE-5")
@Stepwise
class ForwardingSpec extends Specification {

	@Shared SpringcacheService springcacheService = ApplicationHolder.application.mainContext.springcacheService
	@Shared Ehcache forwardingControllerCache = ApplicationHolder.application.mainContext.forwardingControllerCache

	private RESTClient http = new RESTClient("http://localhost:8080/")

	@Unroll({"an initial hit on #action primes the cache"})
	def "an initial hit on an action primes the cache"() {
		when:
		def response = http.get(path: "/forwarding/$action")

		then:
		response.status == HttpStatus.SC_OK
		response.data.text ==~ /\d+/

		and:
		cacheMisses == old(cacheMisses) + misses
		cacheHits == old(cacheHits) + hits

		where:
		action                     | hits | misses
		"uncachedForwardsToCached" | 0    | 1
		"cachedForwardsToUncached" | 0    | 1
		"cachedForwardsToCached"   | 1    | 1
	}

	@Unroll({"a subsequent hit on #action primes the cache"})
	def "a subsequent hit on an action hits the cache"() {
		when:
		def response = http.get(path: "/forwarding/$action")

		then:
		response.status == HttpStatus.SC_OK
		response.data.text ==~ /\d+/

		and:
		cacheMisses == old(cacheMisses) + misses
		cacheHits == old(cacheHits) + hits

		where:
		action                     | hits | misses
		"uncachedForwardsToCached" | 1    | 0
		"cachedForwardsToUncached" | 1    | 0
		"cachedForwardsToCached"   | 1    | 0
	}

	private long getCacheMisses() {
		forwardingControllerCache.statistics.cacheMisses
	}

	private long getCacheHits() {
		forwardingControllerCache.statistics.cacheHits
	}

}
