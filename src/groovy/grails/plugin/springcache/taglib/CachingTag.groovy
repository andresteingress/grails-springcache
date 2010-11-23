/*
 * Copyright 2010 Luke Daley
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
package grails.plugin.springcache.taglib

import grails.plugin.springcache.CacheKey
import grails.plugin.springcache.annotations.Cacheable
import grails.plugin.springcache.key.CacheKeyBuilder
import org.slf4j.LoggerFactory
import org.codehaus.groovy.grails.web.pages.*

/**
 * Wraps a closure "tag implementation" with caching logic.
 *
 * This is only suitable for use with tag implementations, so is not a general
 * purpose solution for caching closures.
 *
 * @see CachingTagLibDecorator
 */
class CachingTag extends Closure {

	private final log = LoggerFactory.getLogger(CachingTag.class)

	private final String namespace
	private final String tagName
	private final Closure cached
	private final Cacheable annotation
	private final springcacheService

	private final cacheName

	CachingTag(String namespace, String tagName, Closure cached, Cacheable annotation, springcacheService) {
		super(null, null)

		this.namespace = namespace
		this.tagName = tagName
		this.cached = cached
		this.annotation = annotation
		this.springcacheService = springcacheService
		this.cacheName = annotation.cache() ?: annotation.value()
	}

	def doCall(Object[] args) {
		def params = args[0]
		def cacheKey = toCacheKey(params)

		def resultAndBuffer = springcacheService.doWithCache(cacheName, cacheKey) {
			def outputStack = GroovyPageOutputStack.currentStack()

			// Install an out that we can control
			def writer = new FastStringWriter()
			outputStack.push(writer, true)

			// Invoke the tag
			def result = cached.call(* args)

			// Remove our out
			outputStack.pop()

			// Cache the buffer we wrote to and the result
			new ResultAndBuffer(result: result, buffer: writer.buffer)
		}

		GroovyPageOutputStack.currentWriter() << resultAndBuffer.buffer

		resultAndBuffer.result
	}

	private CacheKey toCacheKey(Map params) {
		def builder = new CacheKeyBuilder()
		builder << namespace
		builder << tagName
		params.sort { it.key }.each { entry ->
			builder << entry
		}
		builder.toCacheKey()
	}

	Closure asWritable() {
		caches.asWritable()
	}

	Closure curry(Object[] arguments) {
		new CachingTag(cached.curry(* arguments))
	}

	Object getDelegate() {
		cached.getDelegate()
	}

	int getDirective() {
		cached.getDirective()
	}

	int getMaximumNumberOfParameters() {
		cached.getMaximumNumberOfParameters()
	}

	Object getOwner() {
		cached.getOwner()
	}

	Class[] getParameterTypes() {
		cached.getParameterTypes()
	}

	Object getProperty(String property) {
			getMetaClass().getAttribute(this, property) ?: this.@cached.getProperty(property)
	}

	int getResolveStrategy() {
		cached.getResolveStrategy()
	}

	Object getThisObject() {
		cached.getThisObject()
	}

	boolean isCase(Object candidate) {
		cached.isCase(candidate)
	}

	Closure ncurry(int n, Object[] arguments) {
		cached.ncurry(n, * arguments)
	}

	Closure rcurry(Object[] arguments) {
		cached.rcurry(* arguments)
	}

	void setDelegate(Object delegate) {
		cached.setDelegate(delegate)
	}

	void setDirective(int directive) {
		cached.setDirective(directive)
	}

	void setProperty(String property, Object newValue) {
		cached.setProperty(property, newValue)
	}

	void setResolveStrategy(int resolveStrategy) {
		cached.setResolveStrategy(resolveStrategy)
	}

}

class ResultAndBuffer {
	def result
	def buffer
}