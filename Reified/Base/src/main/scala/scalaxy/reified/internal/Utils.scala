package scalaxy.reified.internal

import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._
import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox

import scalaxy.reified.ReifiedValue

object Utils {

  private[reified] def newExpr[A](tree: Tree): Expr[A] = {
    Expr[A](
      currentMirror,
      CurrentMirrorTreeCreator(tree))
  }

  def typeCheck[A](expr: Expr[A], pt: Type = WildcardType): Expr[A] = {
    newExpr[A](typeCheckTree(expr.tree, pt))
  }

  val optimisingOptions =
    System.getProperty(
      "scalaxy.reified.optimisingOptions",
      "-optimise -Yclosure-elim -Yinline") // -Xprint:lambdalift"
  lazy val optimisingToolbox = {
    currentMirror.mkToolBox(options = optimisingOptions)
  }

  private[reified] def getModulePath(u: scala.reflect.api.Universe)(moduleSym: u.Symbol): u.Tree = {
    import u._
    def rec(relements: List[String]): Tree = relements match {
      case name :: Nil =>
        Ident(name: TermName)
      case ("`package`") :: rest =>
        //rec(rest)
        Select(rec(rest), "package": TermName)
      case name :: rest =>
        Select(rec(rest), name: TermName)
    }
    rec(moduleSym.fullName.split("\\.").reverse.toList)
  }

  def resolveModulePaths(u: scala.reflect.api.Universe)(root: u.Tree): u.Tree = {
    import u._
    new Transformer {
      override def transform(tree: Tree) = tree match {
        case Ident() if tree.symbol != null && tree.symbol != NoSymbol =>
          val sym = tree.symbol
          val owner = tree.symbol.owner
          if (sym.isModule) {
            getModulePath(u)(sym.asModule)
          } else if (owner.isModule || owner.isPackage) {
            Select(getModulePath(u)(owner), sym.name)
          } else {
            tree
          }
        case _ =>
          super.transform(tree)
      }
    }.transform(root)
  }

  def typeCheckTree(tree: Tree, pt: Type = WildcardType): Tree = {
    try {
      optimisingToolbox.typeCheck(tree, pt)
    } catch {
      case ex: Throwable =>
        throw new RuntimeException(s"Failed to typeCheck($tree, $pt): $ex", ex)
    }
  }

  def safeReset(tree: Tree, toolbox: ToolBox[universe.type]): Tree = {
    val resolved = resolveModulePaths(universe)(tree)
    toolbox.resetAllAttrs(resolved)
  }
}
