object MonixMain {
  def main(args: Array[String]): Unit = {
    println("---------")
    println("Example 1")
    println("---------")
    AdvancedExample1.run(args)
    println()
    println("---------")
    println("Example 2")
    println("---------")
    println("Pattern 1")
    AdvancedExample2.run(args)
    println()
    println("Pattern 2")
    AdvancedExample2.run2(args)
    println()
    println("Pattern 2 with Graph synchronization")
    AdvancedExample2.runGraphPattern2(args)
  }
}
