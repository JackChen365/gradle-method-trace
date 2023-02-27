package com.jack.android.gradle.method.trace.asm.runcatch

import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader

class RunCachingClassAdapterTest {

    @Test
    fun testRunCachingClass() {
        // loading the class
        val file = File("src/test/assets/input/Example.class")
        val classReader = ClassReader(file.inputStream())
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)

        // Wrap the ClassWriter with our custom ClassVisitor
        val mcw = RunCachingClassAdapter(cw)
        classReader.accept(mcw, ClassReader.EXPAND_FRAMES)
        val byteArray: ByteArray = cw.toByteArray()
        DataOutputStream(FileOutputStream("src/test/assets/output/com/jack/test/Example.class")).use {
            it.write(byteArray)
        }
        val outputDir = File("src/test/assets/output")
        val cp: Array<URL> = arrayOf(outputDir.toURI().toURL())
        val classLoader = URLClassLoader(cp)
        val clazz: Class<*> = classLoader.loadClass("com.jack.test.Example")
        val method: Method = clazz.getMethod("staticMethod")
        method.invoke(null)
    }
}