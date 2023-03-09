package com.jack.android.gradle.method.trace.runtime.analyzer

import java.lang.reflect.Method

open class SimpleMethodAnalyzer : MethodAnalyzer {
    companion object {
        private val NULL_INSTANCE = Object()
        private const val PARAMETER_IDENTIFIER = "identifier"
        private const val PARAMETER_CLAZZ = "clazz"
        private const val PARAMETER_REF = "ref"
        private const val PARAMETER_METHOD_NAME = "methodName"
        private const val PARAMETER_RESULT = "result"
        private const val PARAMETER_START_TIME = "startTime"
        private const val PARAMETER_EXCEPTION = "exception"
    }

    private val methodEnterCaching = mutableMapOf<Method, String>()
    private val methodExitCaching = mutableMapOf<Method, String>()
    private val methodExceptionCaching = mutableMapOf<Method, String>()

    private val Array<Method>.methodEnterMap: Map<Method, String>
        get() {
            if (methodEnterCaching.isEmpty()) {
                forEach { method ->
                    var methodEnterDesc = methodEnterCaching[method]
                    if (null == methodEnterDesc) {
                        val methodEnter = method.getAnnotation(MethodEnter::class.java)
                        if (null != methodEnter) {
                            methodEnterDesc = methodEnter.value
                            methodEnterCaching[method] = methodEnterDesc
                        }
                    }
                }
            }
            return methodEnterCaching
        }
    private val Array<Method>.methodExitMap: Map<Method, String>
        get() {
            forEach { method ->
                var methodExitDesc = methodExitCaching[method]
                if (null == methodExitDesc) {
                    val methodExit = method.getAnnotation(MethodExit::class.java)
                    if (null != methodExit) {
                        methodExitDesc = methodExit.value
                        methodExitCaching[method] = methodExitDesc
                    }
                }
            }
            return methodExitCaching
        }
    private val Array<Method>.methodExceptionMap: Map<Method, String>
        get() {
            forEach { method ->
                var methodExceptionDesc = methodEnterCaching[method]
                if (null == methodExceptionDesc) {
                    val methodException = method.getAnnotation(MethodException::class.java)
                    if (null != methodException) {
                        methodExceptionDesc = methodException.value
                        methodExceptionCaching[method] = methodExceptionDesc
                    }
                }
            }
            return methodExceptionCaching
        }

    override fun onMethodEnter(
        identifier: Int,
        clazz: Class<*>,
        ref: Any?,
        arguments: Array<Any?>?,
        methodName: String?
    ) {
        visitMethodEnter(
            identifier = identifier,
            clazz = clazz,
            ref = ref,
            arguments = arguments,
            methodName = methodName
        )
    }

    override fun onMethodExit(
        identifier: Int,
        clazz: Class<*>,
        ref: Any?,
        arguments: Array<Any?>?,
        methodName: String?,
        result: Any?,
        startTime: Long
    ) {
        visitMethodExit(
            identifier = identifier,
            clazz = clazz,
            ref = ref,
            arguments = arguments,
            methodName = methodName,
            methodResult = result,
            startTime = startTime
        )
    }

    override fun onException(
        clazz: Class<*>,
        ref: Any?,
        methodName: String?,
        exception: Exception
    ) {
        visitException(
            clazz = clazz,
            ref = ref,
            methodName = methodName,
            exception = exception
        )
    }

    private fun visitMethodEnter(
        identifier: Int,
        clazz: Class<*>,
        ref: Any?,
        arguments: Array<Any?>?,
        methodName: String?
    ) {
        val result = this.javaClass.declaredMethods.methodEnterMap.entries.find { (_, methodDesc) ->
            val className = methodDesc.substringBefore("#")
            val name = methodDesc.substringAfter("#")
            clazz.name.endsWith(className) && methodName == name
        }
        if (null != result) {
            val method = result.key
            val parameters = findMatchParameters(
                method = method,
                methodArguments = arguments,
                parameters = mapOf(
                    PARAMETER_IDENTIFIER to identifier,
                    PARAMETER_CLAZZ to clazz,
                    PARAMETER_REF to (ref ?: NULL_INSTANCE),
                    PARAMETER_METHOD_NAME to methodName
                )
            )
            method.invoke(this, *parameters)
        }
    }

    private fun visitMethodExit(
        identifier: Int,
        clazz: Class<*>,
        ref: Any?,
        arguments: Array<Any?>?,
        methodName: String?,
        methodResult: Any?,
        startTime: Long
    ) {
        val result = this.javaClass.declaredMethods.methodExitMap.entries.find { (_, methodDesc) ->
            val className = methodDesc.substringBefore("#")
            val name = methodDesc.substringAfter("#")
            clazz.name.endsWith(className) && methodName == name
        }
        if (null != result) {
            val method = result.key
            val parameters = findMatchParameters(
                method = method,
                methodArguments = arguments,
                parameters = mapOf(
                    PARAMETER_IDENTIFIER to identifier,
                    PARAMETER_CLAZZ to clazz,
                    PARAMETER_REF to (ref ?: NULL_INSTANCE),
                    PARAMETER_METHOD_NAME to methodName,
                    PARAMETER_RESULT to (methodResult ?: NULL_INSTANCE),
                    PARAMETER_START_TIME to startTime
                )
            )
            if (!method.isAccessible) {
                method.isAccessible = true
            }
            method.invoke(this, *parameters)
        }
    }

    private fun visitException(
        clazz: Class<*>,
        ref: Any?,
        methodName: String?,
        exception: Exception
    ) {
        val result = this.javaClass.declaredMethods.methodExceptionMap.entries.find { (_, methodDesc) ->
            val className = methodDesc.substringBefore("#")
            val name = methodDesc.substringAfter("#")
            clazz.name.endsWith(className) && methodName == name
        }
        if (null != result) {
            val method = result.key
            val parameters = findMatchParameters(
                method = method,
                methodArguments = emptyArray(),
                parameters = mapOf(
                    PARAMETER_CLAZZ to clazz,
                    PARAMETER_REF to ref,
                    PARAMETER_METHOD_NAME to methodName,
                    PARAMETER_EXCEPTION to exception,
                )
            )
            if (!method.isAccessible) {
                method.isAccessible = true
            }
            method.invoke(this, *parameters)
        }
    }

    private fun findMatchParameters(
        method: Method,
        methodArguments: Array<Any?>?,
        parameters: Map<String, Any?>
    ): Array<Any?> {
        var argumentIndex = 0
        val parameterValues = mutableListOf<Any?>()
        method.parameters.forEach { parameter ->
            val value = parameters[parameter.name]
            if (value == NULL_INSTANCE) {
                parameterValues.add(null)
            } else if (null != value) {
                parameterValues.add(value)
            } else {
                parameterValues.add(methodArguments?.get(argumentIndex++))
            }
        }
        return parameterValues.toTypedArray()
    }
}
