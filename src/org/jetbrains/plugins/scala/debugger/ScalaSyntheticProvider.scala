package org.jetbrains.plugins.scala.debugger

import com.intellij.debugger.engine.SyntheticTypeComponentProvider
import com.sun.jdi._
import org.jetbrains.plugins.scala.debugger.evaluation.util.DebuggerUtil
import org.jetbrains.plugins.scala.decompiler.DecompilerUtil

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Nikolay.Tropin
 * 2014-12-03
 */
class ScalaSyntheticProvider extends SyntheticTypeComponentProvider {
  override def isSynthetic(typeComponent: TypeComponent): Boolean = ScalaSyntheticProvider.isSynthetic(typeComponent)
}

object ScalaSyntheticProvider {
  def isSynthetic(typeComponent: TypeComponent): Boolean = {
    val isScala = DebuggerUtil.isScala(typeComponent.declaringType(), default = false)
    if (!isScala) return false

    typeComponent match {
      case m: Method if m.isConstructor && isAnonFun(m.declaringType()) => true
      case m: Method if m.name() == "apply" && hasSpecializationMethod(m.declaringType()) => true
      case m: Method if isDefaultArg(m) => true
      case m: Method if isTraitForwarder(m) => true
      case m: Method if m.name().endsWith("$adapted") => true
      case m: Method if ScalaPositionManager.isIndyLambda(m) => false
      case f: Field if f.name().startsWith("bitmap$") => true
      case _ =>
        val machine: VirtualMachine = typeComponent.virtualMachine
        machine != null && machine.canGetSyntheticAttribute && typeComponent.isSynthetic
    }
  }

  private def isAnonFun(refType: ReferenceType): Boolean = {
    short(refType.name).contains("$anonfun")
  }

  private def hasSpecializationMethod(refType: ReferenceType): Boolean = {
    refType.methods().asScala.exists(isSpecialization)
  }

  private def isSpecialization(method: Method): Boolean = {
    method.name.contains("$mc") && method.name.endsWith("$sp")
  }

  private def short(name: String) = {
    name.substring(name.lastIndexOf('.') + 1)
  }

  private def isDefaultArg(m: Method): Boolean = {
    m.name.contains("$default$")
  }

  private def isTraitForwarder(m: Method): Boolean = {
    Try(onlyInvokesStatic(m) && hasTraitWithImplementation(m)).getOrElse(false)
  }

  private def onlyInvokesStatic(m: Method): Boolean = {
    val bytecodes = m.bytecodes()
    var i = 0
    while (i < bytecodes.length) {
      val instr = bytecodes(i)
      if (BytecodeUtil.twoBytesLoadCodes.contains(instr)) i += 2
      else if (BytecodeUtil.oneByteLoadCodes.contains(instr)) i += 1
      else if (instr == DecompilerUtil.Opcodes.invokeStatic) {
        val nextIdx = i + 3
        val nextInstr = bytecodes(nextIdx)
        return nextIdx == (bytecodes.length - 1) && BytecodeUtil.returnCodes.contains(nextInstr)
      }
      else return false
    }
    false
  }

  private def hasTraitWithImplementation(m: Method): Boolean = {
    m.declaringType() match {
      case ct: ClassType =>
        val interfaces = ct.allInterfaces().asScala
        val vm = ct.virtualMachine()
        val traitImpls = interfaces.flatMap(i => vm.classesByName(i.name() + "$class").asScala)
        traitImpls.exists(ti => !ti.methodsByName(m.name()).isEmpty)
      case _ => false
    }
  }
}
