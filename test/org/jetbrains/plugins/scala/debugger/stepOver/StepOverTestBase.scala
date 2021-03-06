package org.jetbrains.plugins.scala.debugger.stepOver

import org.jetbrains.plugins.scala.debugger.{ScalaDebuggerTestCase, ScalaPositionManager}
import org.jetbrains.plugins.scala.extensions._
import org.junit.Assert

import scala.io.Source

/**
 * @author Nikolay.Tropin
 */
abstract class StepOverTestBase extends ScalaDebuggerTestCase {
  def doStepOver(): Unit = {
    val stepOverCommand = getDebugProcess.createStepOverCommand(suspendContext, false)
    getDebugProcess.getManagerThread.invokeAndWait(stepOverCommand)
  }

  def testStepThrough(fileText: String, expectedLineNumbers: Seq[Int], startBreakpoint: (Int, Integer) = (2, -1)): Unit = {
    val mainClassName = "Sample"
    val fileName = s"$mainClassName.scala"
    val lines = Source.fromString(fileText.stripMargin.trim).getLines().toSeq
    Assert.assertTrue(s"File should start with definition of object $mainClassName" , lines.head.startsWith(s"object $mainClassName"))
    Assert.assertTrue("Method main should be defined on a second line", lines(1).trim.startsWith("def main") && lines(2).trim.nonEmpty)

    def checkLine(expectedLineNumber: Int): Unit = {
      val actualLineNumber = currentLineNumber
      if (actualLineNumber != expectedLineNumber) {
        val message = {
          val actualLine = lines(actualLineNumber)
          val expectedLine = lines(expectedLineNumber)
          s"""Wrong line number.
              |Expected $expectedLineNumber: $expectedLine
              |Actual $actualLineNumber: $actualLine""".stripMargin
        }
        Assert.fail(message)
      }
    }

    addFileToProject(fileName, fileText.stripMargin.trim)
    addBreakpoint(fileName, startBreakpoint._1, startBreakpoint._2)
    val expectedNumbers = expectedLineNumbers.toIterator
    runDebugger(mainClassName) {
      while (!processTerminatedNoBreakpoints()) {
        if (expectedNumbers.hasNext) checkLine(expectedNumbers.next())
        else {
          val lineNumber = currentLineNumber
          Assert.fail(s"No expected lines left, stopped at line $lineNumber: ${lines(lineNumber)}")
        }
        doStepOver()
      }
    }
  }

  private def currentLineNumber: Int = {
    managed[Integer] {
      val location = suspendContext.getFrameProxy.location
      inReadAction {
        new ScalaPositionManager(getDebugProcess).getSourcePosition(location).getLine
      }
    }
  }
}
