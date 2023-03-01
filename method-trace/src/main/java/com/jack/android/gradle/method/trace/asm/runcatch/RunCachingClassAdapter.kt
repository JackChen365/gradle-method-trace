package com.jack.android.gradle.method.trace.asm.runcatch

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class RunCachingClassAdapter(
    classVisitor: ClassVisitor?,
    private val includingAllMethods: Boolean = false,
    private val includingMethods: List<String> = emptyList()
) : ClassVisitor(Opcodes.ASM7, classVisitor) {

    private lateinit var className: String

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.className = name
    }

    override fun visitMethod(
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<String>?
    ): MethodVisitor {
        val mv = super.visitMethod(access, name, desc, signature, exceptions)
        // We are not going to rewrite constructor
        return if (includingAllMethods
            || includingMethods.contains(name)
        ) {
            RunCatchingAdviceAdapter(mv, access, className, name, desc)
        } else {
            mv
        }
    }
}