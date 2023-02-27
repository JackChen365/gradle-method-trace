package com.jack.android.gradle.method.trace

private val cachingRegex = mutableMapOf<String, Regex>()
private val listCaching = mutableMapOf<List<String>, List<Regex>>()
val List<String>.caching: List<Regex>
    get() {
        var cachingRegexList = listCaching[this]
        if (null == cachingRegexList) {
            cachingRegexList = map { it.substringBefore("#") }
                .toSet()
                .map { value ->
                    var regex = cachingRegex[value]
                    if (null == regex) {
                        regex = value.toRegex()
                        cachingRegex[value] = regex
                    }
                    regex
                }
            listCaching[this] = cachingRegexList
        }
        return cachingRegexList
    }
