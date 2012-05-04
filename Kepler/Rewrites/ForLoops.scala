package scalaxy; package rewrites

import macros._
import matchers._
//import scala.reflect.mirror._

object ForLoops extends Compilet {
  /*override val matchActions = Seq(
    "foreach" -> simpleForeachUntil(0, 0, {})
  )*/
  
  def simpleForeachUntil[U : TypeTag](start: Int, end: Int, body: U) = replace(
    for (i <- start until end) 
      body,
    {
      var ii = start
      while (ii < end) {
        val i = ii
        body
        ii = ii + 1  
      }
    }
  )
  
  def simpleForeachTo[U : TypeTag](start: Int, end: Int, body: U) = replace(
    for (i <- start to end) 
      body,
    {
      var ii = start
      while (ii <= end) {
        val i = ii
        body
        ii = ii + 1  
      }
    }
  )
  
  def rgForeachUntilBy[U : TypeTag](start: Int, end: Int, step: Int, body: U) = 
    when(
      for (i <- start until end by step) 
        body
    )(
      step
    ) {
      case PositiveIntConstant(_) :: Nil =>
        replacement {
          var ii = start
          while (ii < end) {
            val i = ii
            body
            ii = ii + step 
          }
        }
      case NegativeIntConstant(_) :: Nil =>
        replacement {
          var ii = start
          while (ii > end) {
            val i = ii
            body
            ii = ii + step 
          }
        }
      case _ =>
        warning("Cannot optimize : step is not constant")
    }
    
  /*
  def simpleForeachUntil[U](start: Int, end: Int, body: Int => U) = replace(
    for (i <- start until end) body(i),
    {
      var ii = start
      while (ii < end) {
        val i = ii
        body(i)
        ii = ii + 1  
      }
    }
  )
  def simpleForeachTo[U](start: Int, end: Int, body: Int => U) = replace(
    for (i <- start to end) body(i),
    {
      var ii = start
      while (ii <= end) {
        val i = ii
        body(i)
        ii = ii + 1  
      }
    }
  )
  */
}
