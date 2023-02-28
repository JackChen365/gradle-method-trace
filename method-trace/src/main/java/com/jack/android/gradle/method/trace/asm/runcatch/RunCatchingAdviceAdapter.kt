package com.jack.android.gradle.method.trace.asm.runcatch

import com.jack.android.gradle.method.trace.METHOD_ON_EXCEPTION
import com.jack.android.gradle.method.trace.METHOD_ON_EXCEPTION_DESC
import com.jack.android.gradle.method.trace.METHOD_TRACE_OWNER
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter

class RunCatchingAdviceAdapter(
    methodVisitor: MethodVisitor?,
    access: Int,
    className: String,
    name: String?,
    desc: String?
) : AdviceAdapter(ASM7, methodVisitor, access, name, desc) {
    private var isStaticMethod = false
    private var className: String
    private var methodName: String? = null
    private val startLabel = Label()
    private val endLabel = Label()
    private val handlerLabel = Label()

    init {
        this.methodName = name
        this.className = className
        this.isStaticMethod = access and Opcodes.ACC_STATIC !== 0
    }

    override fun visitCode() {
        super.visitLabel(startLabel)
        super.visitCode()
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        // (2) endLabel
        super.visitLabel(endLabel)
        // (3) handlerLabel
        super.visitLabel(handlerLabel)
        val localIndex = getLocalIndex()
        super.visitVarInsn(ASTORE, localIndex)
        super.visitVarInsn(ALOAD, localIndex)
        // Class
        super.visitLdcInsn(Type.getType("L$className;"));
        // This ref
        if (isStaticMethod) {
            super.visitInsn(ACONST_NULL)
        } else {
            super.loadThis()
        }
        // Method name
        super.visitLdcInsn(methodName)
        super.visitMethodInsn(
            INVOKESTATIC, METHOD_TRACE_OWNER, METHOD_ON_EXCEPTION, METHOD_ON_EXCEPTION_DESC,
            false
        )
        super.visitVarInsn(ALOAD, localIndex)
        super.visitInsn(ATHROW)

        // (4) visitTryCatchBlock
        super.visitTryCatchBlock(startLabel, endLabel, handlerLabel, "java/lang/Exception")
        super.visitMaxs(maxStack, maxLocals)
    }

    private fun getLocalIndex(): Int {
        val t: Type = Type.getType(methodDesc)
        val argumentTypes: Array<Type> = t.getArgumentTypes()
        val isStaticMethod = methodAccess and ACC_STATIC != 0
        var localIndex = if (isStaticMethod) 0 else 1
        for (argType in argumentTypes) {
            localIndex += argType.getSize()
        }
        return localIndex
    }
}