package com.jack.android.gradle.method.trace.asm.methodtrace

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader

internal class MethodTraceClassVisitorTest {

    @Test
    fun testMethodTrace() {
        // loading the class
        val file = File("src/test/assets/input/Example.class")
        val classReader = ClassReader(file.inputStream())
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)

        // Wrap the ClassWriter with our custom ClassVisitor
        val mcw = MethodTraceClassVisitor(cw)
        classReader.accept(mcw, ClassReader.EXPAND_FRAMES)
        val byteArray: ByteArray = cw.toByteArray()
        DataOutputStream(FileOutputStream("src/test/assets/output/com/jack/test/Example.class")).use {
            it.write(byteArray)
        }
        val f = File("src/test/assets/output/")
        val cp: Array<URL> = arrayOf(f.toURI().toURL())
        val classLoader = URLClassLoader(cp)
        val clazz: Class<*> = classLoader.loadClass("com.jack.test.Example")
        val method: Method = clazz.getMethod("staticMethod")
        method.invoke(null)
        val instance = clazz.getConstructor().newInstance()
        val refMethod: Method = clazz.getDeclaredMethod(
            "refMethod",
            String::class.java,
            String::class.java,
            String::class.java
        )
        refMethod.invoke(instance, "Var1", "Var2", "Var3")
        val refMethodWithResult: Method = clazz.getDeclaredMethod(
            "refMethodWithResult", String::class.java, String::class.java,
            String::class.java
        )
        refMethodWithResult.invoke(instance, "Var1", "Var2", "Var3")
    }
}