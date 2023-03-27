package com.jack.android.gradle.method.trace.runtime.analyzer

import com.jack.android.gradle.method.trace.runtime.MethodTrace
import org.junit.Assert
import org.junit.Test

/**
 * Created on 2023/3/9.
 *
 * @author Jack Chen
 * @email zhenchen@tubi.tv
 */
internal class SimpleMethodAnalyzerTest {
    class Foo {
        companion object {
            @JvmStatic
            fun staticMethod() {
                MethodTrace.onMethodEnter(123, Foo::class.java, null, emptyArray(), "staticMethod")
                println("invoke staticMethod")
                MethodTrace.onMethodExit(
                    123, Foo::class.java, null, emptyArray(), "staticMethod", null, System.currentTimeMillis()
                )
            }

            @JvmStatic
            fun staticMethodWithResult(var1: String, var2: Int, var3: Int): String {
                MethodTrace.onMethodEnter(123, Foo::class.java, null, arrayOf(var1, var2, var3), "staticMethodWithResult")
                println("invoke staticMethodWithResult")
                var result = "Result"
                MethodTrace.onMethodExit(
                    123,
                    Foo::class.java,
                    null,
                    arrayOf(var1, var2, var3),
                    "staticMethodWithResult",
                    result,
                    System.currentTimeMillis()
                )
                return result
            }
        }

        fun testMethod(var1: String) {
            MethodTrace.onMethodEnter(123, Foo::class.java, this, arrayOf(var1), "testMethod")
            println("invoke testMethod:$var1")
            MethodTrace.onMethodExit(
                123, Foo::class.java, this, arrayOf(var1), "staticMethod", null, System.currentTimeMillis()
            )
        }

        fun testMethodWithResult(var1: String): String {
            MethodTrace.onMethodEnter(123, Foo::class.java, this, arrayOf(var1), "testMethodWithResult")
            println("invoke testMethod:$var1")
            var result = "Result"
            MethodTrace.onMethodExit(
                123, Foo::class.java, this, arrayOf(var1), "testMethodWithResult", result, System.currentTimeMillis()
            )
            return result
        }
    }

    class FooMethodAnalyzer : SimpleMethodAnalyzer() {
        @MethodExit("Foo#staticMethod")
        fun fun1(@MethodName methodName: String) {
            Assert.assertEquals("staticMethod", methodName)
        }

        @MethodExit("Foo#staticMethodWithResult")
        fun fun2(
            @MethodName methodName: String,
            @MethodResult result: String,
            @Arg3 var3: Int
        ) {
            Assert.assertEquals("staticMethodWithResult", methodName)
            Assert.assertEquals("Result", result)
            Assert.assertEquals(456, var3)
        }

        @MethodExit("Foo#testMethod")
        fun fun3(
            @ThisRef ref: Foo,
            @MethodResult result: String,
            @Arg1 var1: String
        ) {
            Assert.assertNotNull(ref)
            Assert.assertEquals("Result", result)
            Assert.assertEquals("var2", var1)
        }

        @MethodExit("Foo#testMethodWithResult")
        fun fun4(
            @ThisRef ref: Foo,
            @MethodResult result: String,
            @Arg1 var1: String
        ) {
            Assert.assertNotNull(ref)
            Assert.assertEquals("Result", result)
            Assert.assertEquals("var2", var1)
        }
    }

    @Test
    fun test() {
        MethodTrace.registerMethodAnalyzer(FooMethodAnalyzer())
        val foo = Foo()
        Foo.staticMethod()
        Foo.staticMethodWithResult("123", 123, 456)
        foo.testMethod("var1")
        foo.testMethodWithResult("var2")
    }
}