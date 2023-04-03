package com.jack.android.gradle.method.trace.asm.methodtrace

import extension.trace.METHOD_ENTER_DESC
import extension.trace.METHOD_EXIT_DESC
import extension.trace.METHOD_ON_ENTER
import extension.trace.METHOD_ON_EXIT
import extension.trace.METHOD_TRACE_OWNER
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter

internal class MethodTraceMethodAdapterVisitor constructor(
    mv: MethodVisitor?,
    access: Int,
    private val className: String,
    private val methodName: String,
    desc: String?
) : AdviceAdapter(ASM7, mv, access, methodName, desc) {
    private val isStaticMethod: Boolean
    private var startTimeIdentifier = 0
    private var idIdentifier = 0

    init {
        isStaticMethod = access and ACC_STATIC != 0
    }

    override fun onMethodEnter() {
        // declare startTime
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
        startTimeIdentifier = newLocal(Type.LONG_TYPE)
        mv.visitVarInsn(LSTORE, startTimeIdentifier)
        // declare identifier
        idIdentifier = newLocal(Type.getType(Int::class.java))
        mv.visitLdcInsn(methodName.hashCode())
        mv.visitVarInsn(ISTORE, idIdentifier)
        // Identifier
        mv.visitVarInsn(ILOAD, idIdentifier)
        // Class
        mv.visitLdcInsn(Type.getType("L$className;"))
        // This ref
        if (isStaticMethod) {
            mv.visitInsn(ACONST_NULL)
        } else {
            loadThis()
        }
        // Arg array
        loadArgArray()
        // Method name
        mv.visitLdcInsn(methodName)
        // Invoke
        mv.visitMethodInsn(INVOKESTATIC, METHOD_TRACE_OWNER, METHOD_ON_ENTER, METHOD_ENTER_DESC, false)
    }

    override fun onMethodExit(opcode: Int) {
        // Result value
        val resultIdentifier = newLocal(Type.getType(String::class.java))
        when (opcode) {
            RETURN -> {
                visitInsn(ACONST_NULL)
            }
            ARETURN, ATHROW -> {
                dup()
            }
            else -> {
                if (opcode == LRETURN || opcode == DRETURN) {
                    dup2()
                } else {
                    dup()
                }
                box(Type.getReturnType(methodDesc))
            }
        }
        mv.visitVarInsn(ASTORE, resultIdentifier)
        // Load variables for the method.
        // identifier
        mv.visitVarInsn(ILOAD, idIdentifier)
        // Class
        mv.visitLdcInsn(Type.getType("L$className;"));
        // This ref
        if (isStaticMethod) {
            mv.visitInsn(ACONST_NULL)
        } else {
            loadThis()
        }
        // Arg array
        loadArgArray()
        // Method name
        mv.visitLdcInsn(methodName)
        mv.visitVarInsn(ALOAD, resultIdentifier)
        // Start time
        mv.visitVarInsn(LLOAD, startTimeIdentifier)
        // Invoke
        mv.visitMethodInsn(INVOKESTATIC, METHOD_TRACE_OWNER, METHOD_ON_EXIT, METHOD_EXIT_DESC, false)
    }
}