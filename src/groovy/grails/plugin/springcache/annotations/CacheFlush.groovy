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
package grails.plugin.springcache.annotations;

import java.lang.annotation.*

/**
 * Annotation used to mark methods whose return values should be cached.
 *
 * Based on org.springmodules.cache.annotations.CacheFlush (see https://springmodules.dev.java.net/)
 *
 * @author Alex Ruiz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD, ElementType.TYPE, ElementType.FIELD])
@Inherited
@Documented
public @interface CacheFlush {
	String[] value() default []
	String cacheResolver() default "defaultCacheResolver"
}