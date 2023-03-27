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
        val parameterValues = mutableListOf<Any?>()
        method.parameters.forEach { parameter ->
            parameter.annotations.forEach { annotation ->
                when (annotation) {
                    is ThisClass -> parameterValues.add(parameters[PARAMETER_CLAZZ])
                    is ThisRef -> parameterValues.add(parameters[PARAMETER_REF])
                    is MethodIdentifier -> parameterValues.add(parameters[PARAMETER_IDENTIFIER])
                    is MethodName -> parameterValues.add(parameters[PARAMETER_METHOD_NAME])
                    is MethodTime -> parameterValues.add(parameters[PARAMETER_START_TIME])
                    is MethodResult -> parameterValues.add(parameters[PARAMETER_RESULT])
                    is ArgException -> parameterValues.add(parameters[PARAMETER_EXCEPTION])
                    is Arg1 -> parameterValues.add(methodArguments?.get(0))
                    is Arg2 -> parameterValues.add(methodArguments?.get(1))
                    is Arg3 -> parameterValues.add(methodArguments?.get(2))
                    is Arg4 -> parameterValues.add(methodArguments?.get(3))
                    is Arg5 -> parameterValues.add(methodArguments?.get(4))
                    is Arg6 -> parameterValues.add(methodArguments?.get(5))
                    is Arg7 -> parameterValues.add(methodArguments?.get(6))
                    is Arg8 -> parameterValues.add(methodArguments?.get(7))
                    is Arg9 -> parameterValues.add(methodArguments?.get(8))
                    is Arg10 -> parameterValues.add(methodArguments?.get(9))
                    is Arg11 -> parameterValues.add(methodArguments?.get(10))
                    is Arg12 -> parameterValues.add(methodArguments?.get(11))
                    is Arg13 -> parameterValues.add(methodArguments?.get(12))
                    is Arg14 -> parameterValues.add(methodArguments?.get(13))
                    is Arg15 -> parameterValues.add(methodArguments?.get(14))
                    is Arg16 -> parameterValues.add(methodArguments?.get(15))
                    is Arg17 -> parameterValues.add(methodArguments?.get(16))
                    is Arg18 -> parameterValues.add(methodArguments?.get(17))
                    is Arg19 -> parameterValues.add(methodArguments?.get(18))
                    is Arg20 -> parameterValues.add(methodArguments?.get(19))
                    is Arg21 -> parameterValues.add(methodArguments?.get(20))
                    else -> error("Can not handle the annotation:$annotation.")
                }
            }
        }
        return parameterValues.toTypedArray()
    }
}
