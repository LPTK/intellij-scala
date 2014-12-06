package org.jetbrains.plugins.scala.codeInsight.generation

/**
 * Tests for toString method generation.
 * @author Rado Buransky (buransky.com)
 */
class GenerateToStringTest extends ScalaGenerateTestBase {
  override val handler = new ScalaGenerateToStringHandler

  def testFindAllFields(): Unit = {
    val text = s"""class A (i: Int, val j: Int) {
                 |  val x = 0$CARET_MARKER
                 |  var y = 0
                 |  private val z = 0
                 |}"""
    val result = """class A (i: Int, val j: Int) {
                   |  val x = 0
                   |  var y = 0
                   |  private val z = 0
                   |
                   |  override def toString = s"A($x, $y, $z, $j)"
                   |}"""

    testInvoke(text, result, checkCaret = false)
  }

  def testEmptyClass(): Unit = {
    val text = s"""class A() {
                 |  $CARET_MARKER
                 |}"""
    val result = """class A() {
                   |
                   |  override def toString = s"A()"
                   |}"""

    testInvoke(text, result, checkCaret = false)
  }

  def testObject(): Unit = {
    val text = s"""object Obj {
                 |  val a = 1
                 |  val b = 2
                 |  $CARET_MARKER
                 |}"""
    val result = """object Obj {
                 |  val a = 1
                 |  val b = 2
                 |
                 |  override def toString = s"Obj($a, $b)"
                 |}"""

    testInvoke(text, result, checkCaret = false)
  }

  def testTrait(): Unit = {
    val text = s"""trait T {
                 |  val a = 1
                 |  val b = 2
                 |  $CARET_MARKER
                 |}"""
    val result = """trait T {
                 |  val a = 1
                 |  val b = 2
                 |
                 |  override def toString = s"T($a, $b)"
                 |}"""

    testInvoke(text, result, checkCaret = false)
  }
}
