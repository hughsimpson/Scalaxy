package scalaxy; package components

// This will hopefully not exist anymore when 2.10.0.final is out!
object HacksAndWorkarounds
{
  val onlyTryPatternsWithSameClass = false
  
  // TODO turn to false once macro type is fixed !
  val workAroundMissingTypeApply = true
  val workAroundNullPatternTypes = true

  val fixTypedExpressionsType = true
  val healSymbols = true

  val useStringBasedTypeEqualityInBindings = true
  val useStringBasedPatternMatching = false
}
