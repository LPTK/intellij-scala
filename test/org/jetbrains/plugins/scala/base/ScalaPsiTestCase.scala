package org.jetbrains.plugins.scala.base

import _root_.com.intellij.testFramework.PsiTestCase
import _root_.org.jetbrains.plugins.scala.lang.psi.impl.toplevel.synthetic.SyntheticClasses
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.roots.{ModifiableRootModel, ContentEntry, OrderRootType, ModuleRootManager}
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.{VfsUtil, LocalFileSystem, VirtualFile}
import java.io.{File, IOException}
import lang.superMember.SuperMethodTestUtil
import util.TestUtils
import com.intellij.openapi.roots.libraries.LibraryTable
import com.intellij.openapi.projectRoots.JavaSdk
import _root_.org.scalatest.junit.JUnit3Suite

/**
 * User: Alexander Podkhalyuzin
 * Date: 24.02.2009
 */

abstract class ScalaPsiTestCase extends PsiTestCase with JUnit3Suite {
  private val JDK_HOME = TestUtils.getMockJdk

  protected val rootPath = TestUtils.getTestDataPath
  protected var testPath = ""

  protected var realOutput: String = ""

  override protected def setUp: Unit = {
    super.setUp
    myProject.getComponent(classOf[SyntheticClasses]).registerClasses
    ScalaLoader.loadScala

    val rootModel = ModuleRootManager.getInstance(getModule).getModifiableModel

    val testDataRoot = LocalFileSystem.getInstance.findFileByPath(rootPath)
    val testName = getTestName(true) + ".scala"
    assert(testDataRoot != null)

    val contentEntry = rootModel.addContentEntry(testDataRoot);
    rootModel.setSdk(JavaSdk.getInstance.createJdk("java sdk", JDK_HOME, false))
    contentEntry.addSourceFolder(testDataRoot, false)

    // Add Scala Library
    val libraryTable = rootModel.getModuleLibraryTable
    val scalaLib = libraryTable.createLibrary("scala_lib")
    val libModel = scalaLib.getModifiableModel
    val libRoot = new File(TestUtils.getMockScalaLib)
    assert(libRoot.exists)

    val srcRoot = new File(TestUtils.getMockScalaSrc)
    assert(srcRoot.exists)

    libModel.addRoot(VfsUtil.getUrlForLibraryRoot(libRoot), OrderRootType.CLASSES)
    libModel.addRoot(VfsUtil.getUrlForLibraryRoot(srcRoot), OrderRootType.SOURCES)

    ApplicationManager.getApplication.runWriteAction(new Runnable {
      def run {
        libModel.commit
        rootModel.commit
      }
    })
  }

  protected def playTest {
    val filePath = rootPath + testPath + ".scala"
    val vFile = LocalFileSystem.getInstance.findFileByPath(filePath.replace(File.separatorChar, '/'))
    assert(vFile != null, "file " + filePath + " not found")
    val output = getTestOutput(vFile)

    /*val resourcePath = rootPath + testPath + ".testoptions"
    val resourceFile = LocalFileSystem.getInstance.findFileByPath(filePath.replace(File.separatorChar, '/'))
    confiureOption(StringUtil.convertLineSeparators(VfsUtil.loadText(resourceFile), "\n"))*/
    assert(output === realOutput)
  }

  //protected def confiureOption(text: String): Unit
  protected def getTestOutput(file: VirtualFile): String
}
