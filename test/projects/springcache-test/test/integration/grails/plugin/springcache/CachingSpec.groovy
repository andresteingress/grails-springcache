package grails.plugin.springcache

import grails.plugin.spock.IntegrationSpec
import grails.validation.ValidationException
import net.sf.ehcache.Cache
import net.sf.ehcache.store.MemoryStoreEvictionPolicy
import pirates.*

class CachingSpec extends IntegrationSpec {

	def piracyService
	def springcacheCacheManager

	def setup() {
		Pirate.build(name: "Blackbeard")
		Pirate.build(name: "Calico Jack")
		Pirate.build(name: "Black Bart")
		Ship.build(name: "Queen Anne's Revenge", crew: Pirate.findAllByName("Blackbeard"))
	}

	def cleanup() {
		springcacheCacheManager.removeCache("pirateCache")
		springcacheCacheManager.removeCache("shipCache")

		Ship.list()*.delete()
		Pirate.list()*.delete()
	}

	def "cached results are returned from subsequent method calls"() {
		given: "A cache exists"
		def cache = new Cache("pirateCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache)

		when: "A cachable method is called twice"
		def result1 = piracyService.listPirateNames()
		def result2 = piracyService.listPirateNames()

		then: "The first call primes the cache"
		cache.statistics.objectCount == 1L

		and: "The second call hits the cache"
		cache.statistics.cacheHits == 1L

		and: "The same result is returned by both calls"
		result1 == ["Black Bart", "Blackbeard", "Calico Jack"]
		result2 == result1
	}

	def "cached results are not returned for subsequent method calls with different arguments"() {
		given: "A cache exists"
		def cache = new Cache("pirateCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache)

		when: "A cacheable method is called twice with different arguments"
		def result1 = piracyService.findPirateNames("jack", false)
		def result2 = piracyService.findPirateNames("black", false)

		then: "The cache is not hit"
		cache.statistics.cacheHits == 0L

		and: "The results are cached separately"
		cache.statistics.objectCount == 2L

		and: "The results are correct"
		result1 == ["Calico Jack"]
		result2 == ["Black Bart", "Blackbeard"]
	}

	def "cached results are for subsequent method calls with default arguments"() {
		given: "A cache exists"
		def cache = new Cache("pirateCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache)

		and: "the cache is primed"
		def result1 = piracyService.findPirateNames("black", false)

		when: "A cacheable method is called with default arguments"
		def result2 = piracyService.findPirateNames("black")

		then: "The cache is hit"
		cache.statistics.cacheHits == 1L

		and: "The results are correct"
		result1 == ["Black Bart", "Blackbeard"]
		result2 == result1
	}

	def "caches can be flushed"() {
		given: "A cache exists"
		def cache = new Cache("pirateCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache)

		when: "A cacheable method is called"
		def result1 = piracyService.listPirateNames()

		and: "A flushing method is called"
		piracyService.newPirate("Anne Bonny")

		and: "The cacheable method is called again"
		def result2 = piracyService.listPirateNames()

		then: "The cache is not hit"
		cache.statistics.cacheHits == 0L

		and: "The results from before and after flushing are different"
		result1 == ["Black Bart", "Blackbeard", "Calico Jack"]
		result2 == ["Anne Bonny", "Black Bart", "Blackbeard", "Calico Jack"]
	}

	def "the cache is flushed even if the flushing method fails"() {
		given: "A cache exists"
		def cache = new Cache("pirateCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache)

		and: "The cache is primed"
		piracyService.listPirateNames()

		when: "A flushing method is called with parameters that will cause it to fail"
		piracyService.newPirate("Blackbeard")

		then:
		thrown ValidationException

		and: "The cache is still flushed"
		old(cache.statistics.objectCount) == 1L
		cache.statistics.objectCount == 0L
	}

	def "multiple caches can be flushed by a single method"() {
		given: "Multiple caches exist"
		def cache1 = new Cache("pirateCache", 100, false, true, 0, 0)
		def cache2 = new Cache("shipCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache1)
		springcacheCacheManager.addCache(cache2)

		and: "Both caches are primed"
		piracyService.listPirateNames()
		piracyService.listShipNames()

		when: "A method is called that should flush both caches"
		piracyService.newShip("Royal Fortune", ["Black Bart", "Walter Kennedy"])

		then: "Both caches are flushed"
		old(cache1.statistics.objectCount) == 1L
		cache1.statistics.objectCount == 0L
		old(cache2.statistics.objectCount) == 1L
		cache2.statistics.objectCount == 0L
	}

	def "caches are created on demand if they do not exist"() {
		when: "A cachable method is called when no cache exists"
		piracyService.listPirateNames()

		then: "The cache is created when first used"
		def cache = springcacheCacheManager.getEhcache("pirateCache")
		cache != null
		cache.statistics.objectCount == 1L
	}

	def "caches created on demand have default configuration applied"() {
		when: "A cachable method is called when no cache exists"
		piracyService.listPirateNames()

		then: "The cache created has default properties applied"
		def cache = springcacheCacheManager.getEhcache("pirateCache")
		cache != null
		cache.cacheConfiguration.memoryStoreEvictionPolicy == MemoryStoreEvictionPolicy.LFU
	}

}