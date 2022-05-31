---
marp: true
theme: gaia
paginate: true
color: brown;
backgroundColor: white
header: Scala course
footer: Scala & Spark
---
![bg](background.jpg)

# Scala overview


---
![bg](background.jpg)

## Different way of thinking 


Before learning scala, let us:

- see different way of thinking in programming languages

---
![bg](background.jpg)


## a) Ast: Abstract syntax tree

1) Represent the following tree on one line:

````
                        A
                      /   \
                     B     C
                    / \   /  \
                   D   E  F   G
`````

2) Explain what is postfix, infix, prefix notation

3) What is a homoiconic langage ?

--- 
![bg](background.jpg)

4) What if we change the values of each node to:
````
                        -
                      /   \
                     -     +
                    / \   /  \
                   2   3  4   5
`````

What could we do with these data ?

--- 
![bg](background.jpg)

## b) Logic programming language

What is prolog ? 

What does mean:

```child(martha, charlotte).``` 

and 

```?- descend(martha,laura).``` ?

---
![bg](background.jpg)

````

child(martha,charlotte).
child(charlotte,caroline).
child(caroline,laura).
child(laura,rose).

descend(X,Y) :- child(X,Y).

descend(X,Y) :- child(X,Z),
                child(Z,Y).

?- descend(martha,laura).
?- descend(martha,X)
````

See also a full example in prolog: solving a Cludeo problem.

---
![bg](background.jpg)

## Application: Type inference in Scala

````
val a = 10
val b = 10 / 3.0
def f(x: Double) = Some(x)
f(3.0)
f(3)

object Transform {
 def some(x: Int) = Some(x)
 def some(x: Double) = Some(x)
}
Transform.some(3)
Transformo.some(3.0)
````
---
![bg](background.jpg)

````
def f(a: Int)(implicit b:Int) = a + b
implicit val w = 3
f(4)
````

Here we showed simple inference mechanism. 

But this mechanism is very powerful and used with type class in Scala.

=> to go further: https://docs.scala-lang.org/tour/type-inference.html


---
![bg](background.jpg)

## What is unification in prolog ?

See what is unification: http://lpn.swi-prolog.org/lpnpage.php?pagetype=html&pageid=lpn-htmlse5

Very summarized principle:
````
?- martha = X => X = martha
?- child(martha) = X => X = charlotte
?- child(X) = charlotte => X = martha
X = Y
````

A special case of unification is **pattern matching**

---
![bg](background.jpg)

## Pattern matching in scala

````
val maybeAnimal: Option[String] = Some("cat")
maybeAnimal match {
     case Some("dog") => println("dog found")
     case Some(animal) => println(s"Unknown animal: $animal")
     case _ => println("No animal found")
}
````

Here the variable ```animal``` is instanciated with the value ```cat```. The variable ```maybeAnimal``` has been deconstructed.

---
![bg](background.jpg)

## Mixin of logic and functional 

- Mercury: a mix of logical and functional langage
https://mercurylang.org/

- Shen langagea: https://shenlanguage.org/

Very very accurate types !! https://bluishcoder.co.nz/2019/10/03/defining-types-in-shen.html

---
![bg](background.jpg)

## Type refinement

We want early error detection: 
- at Runtime is too late
- at Compilation level => but how to do ?  

In theory we could use a refine type to avoid for example a Division by Zero error.

Other example in Scala 3: 
```
val a :1  = 1 // 1 is a type here, a subtype of Int
```

---
![bg](background.jpg)

## Scala path dependent type  

```
class A(n: Int) {
    class B() {} 
}
```
 => class B type depends of class A(n): A(10).B type is different from A(11).B type

Example:  Omicron problem (https://medium.com/virtuslab/path-dependent-types-9f2d7927c1fa)

Since Scala 3: type coherency improvements.

---
![bg](background.jpg)

## c) Some classification in Programming 

This is not exhaustive. This is here to introduce some useful patterns.

- Imperative (well known)
- Logical
- Functional
  - Haskell
  - ML familiy language
  - Front ent: elm, purescript

---
![bg](background.jpg)

- Programmation by constraint (libraries can extend prolog, java)

See: https://developers.google.com/optimization/cp/queens

- Programmation by contract

=> Check that some conditions are fullfilled when building an object, calling a function, or just before returning a value.

In scala: ```require``` keyword

---
![bg](background.jpg)

## d) Some history

- Alan Turing => turing machine => procedural langage

- Alonzo Church (lambda calculus) => ML langages

 Formalism: Sequent calculus

Question: In which version did lamba calculus apppeared in java ?

See the Shen book discussing of that.

---
![bg](background.jpg)

## e) Concurrency

C++ => mutex (?)

Erlang => Actors

Rust => ownership to avoid multi write access to a resource

Java => Future, Promise

---
![bg](background.jpg)

## Kotlin coroutines

Kotlin => simplify concurrency with coroutines (see Go routines too !)
**suspended keyword**

Example: https://kotlinlang.org/docs/async-programming.html#threading

```
public abstract suspend fun dosth()
```

=> means this function can be blocking. And we can cancel it too.

---
![bg](background.jpg)

## Concurrency in functional languages

Concurrency vs // ?

Functional frameworks:
- Immutability 
- Referential transparency

---
![bg](background.jpg)

## Immutability 

Work with data structure that cannot be altered.

```
val a = List(1, 2, 3)
val b = List(4, 5, 6)
val c = a ++ b
println(c)
println(a) // unchanged
println(b) // unchanged
```

Behind the scene, only a small part of the data will be copied to build  the variable ```c```.


---
![bg](background.jpg)

## Referential transparency

```
import scala.concurrent.Future
implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
val printFuture = Future { println("Foo") }
for {
  x <- printFuture
  y <- printFuture
} yield ()
```

Here printFuture is only displayed one time (when we declare the variable).

---
![bg](background.jpg)

What is the link with concurrency ?

- We can decompose a complex task onto several smaller one
- Each small task:
  - will be independent of the other one since:there is no side effect
  - can be run in parallel

We just need to aggregate the results at the end.

---
![bg](background.jpg)

## Actor model

- The actor model (Spark relies on this architecture). 
  - Inspired from the erlang actors. 
  - The principle: entites spread over the network that can pass message between each other. 
  
Principle: do one thing and only one => one actor to manage one resource

We can have stateful and stateless actors.

---
![bg](background.jpg)

## Monix library

It is a functional library.

We can build a complex workflow of computation and do very powerful operations in a very expressive way.

See a comparison between Monix and Actor model: https://monix.io/docs/current/reactive/observable-comparisons.html

![bg](background.jpg)

---
![bg](background.jpg)

## Concurrency in the web architecture

What is an event loop ? 

- Example: Node.js
- Javascript (server side) multi-threaded or not ?
- Multiple event loop 

vs

Play framework (there are many other)

---
![bg](background.jpg)

## Functional front end language

Javascript can be transpiled. Examples:
- Scalajs
- Elm 
  - Haskell like
  - No Runtime Exceptions (see the web site)
- Kotlin

Develop front-end with a functional language instead of using javascript ! 

---
![bg](background.jpg)

## Polymorphism

Generally speaking: a function with several form (implies its arity)

- Universal
    - parametric (generic)
    - inclusion (exemple: sub type polymorphism)
- ad hoc
    - overloading
    - coercion (casting)

Remark: Typeclass belongs to ad hoc polymorphism.

---
![bg](background.jpg)

Example of parametric polymorphism:

````
trait Addition[T] {
    def +(a: T, b: T): T
}
class AdditionInt extends Addition[Int] {
    override def +(a: Int, b: Int): Int = a + b
}
class AdditionDouble extends Addition[Double] {
    override def +(a: Double, b: Double): Double = a + b
}
val contextInt = new AdditionInt()
contextInt.+(3,4)
contextInt +(3,4) // infix notation

val contextDouble = new AdditionDouble()
contextDouble +(3,4)
````

---
![bg](background.jpg)

## Other polymorphism

Duck type: like sub typing but without inheritancea. Use it only for test purpose (this mechanism uses runtime reflection):
```
import scala.language.reflectiveCalls
class Dog { def speak() { println("woof") } }
class Klingon { def speak() { println("Qapla!") } }

object DuckTyping extends App {
    def callSpeak[A <: { def speak(): Unit }](obj: A) { obj.speak() }
    callSpeak(new Dog)
    callSpeak(new Klingon)
}
```

---
![bg](background.jpg)

## Method Dispatch: Single Dispatch


```
trait Animal { def sound: String }
class Cat extends Animal { override def sound: String = "miaou" }
class Dog extends Animal { override def sound: String = "woawoa" }

val a: Animal = new Cat()
println(a.sound)
```

The receiver class select the right method to call. This is resolved at Runtime.

---
![bg](background.jpg)

## Method Dispatch: Double Dispatch

```
trait Person { def sound(a: Animal): String = a.sound }
class Man extends Person { override def sound(a: Animal): String = s"I'm a man and my animal sound is: ${a.sound}"}
class Woman extends Person { override def sound(a: Animal): String = s"I'm a woman and my animal sound is: ${a.sound}"}
val person: Person = new Man()
person.sound(a)
```

Everything is ok for the moment:
- The receiver class ```person``` resolve sound as ```Man.sound```
- Then it is like the previous slide: we resolve ```sound``` for ```Animal```.

---
![bg](background.jpg)

```
trait Person { 
    def sound(a: Dog): String 
    def sound(a: Cat): String
}
class Man extends Person { 
    override def sound(a: Dog): String = s"I'm a man and my animal is a dog"
    override def sound(a: Cat): String = s"I'm a man and my animal is a cat"
}
class Woman extends Person { 
    override def sound(a: Dog): String = s"I'm a woman and my animal is a dog"
    override def sound(a: Cat): String = s"I'm a woman and my animal is a cat"
}
val person: Person = new Man()

person.sound(a)
error: overloaded method value sound with alternatives:
  (a: Cat)String <and>
  (a: Dog)String
 cannot be applied to (Animal)
       person.sound(a)

```

---
![bg](background.jpg)

Workarounds:

- Visitor pattern (verbose and not very elegant)
- Pattern matching (use ```sealed trait```)
- Switch to Clojure that supports multiple dispatch ! 


---
![bg](background.jpg)

## Types

A type represents a set of value. It can be:
- predefined by the language
- user defined

```
scala> def f = 10
f: Int
```

What do you think ?
```def f = throw new RuntimeException("error")```

---
![bg](background.jpg)

Singleton type:
```
object A
scala> A: A.type
scala> A.getClass
res6: Class[_ <: A.type] = class A$
```

Function signature (one input and one output parameters):
```
def f(a: Int) = a*2
f _
Int => Int
```

---
![bg](background.jpg)

Function signature (two input parameters and one output parameter):
```
def f(a: Int, b: Int) = a*b
f _
(Int, Int) => Int
```

Currying
```
def f(a: Int)(b: Int) = a*b
f _
 Int => (Int => Int)
```

---
![bg](background.jpg)

# Algebra Data types (ADT)

Do you remind what is the neutral type element ?
Addition of types:
```
trait A
trait B
trait C extends A with B
trait D extends B with A
val a = new C {}
```

```
scala> val a: Either[String, Int] = Left("error")
a: Either[String,Int] = Left(error)
```

---
![bg](background.jpg)

In scala 3:  ```val a: A | B = new A {}```

Product of types:
```
case class Person(firstname: String, lastname: String)
val person = Person("John", "doe")
val a = (1, 2)
```

To go further, see: https://medium.com/@shannonbarnes_85491/algebraic-data-types-in-scala-701f3227fe91#:~:text=The%20product%20type%20in%20Scala,with%20logical%20conjunctions%20(ANDs).
`

---
![bg](background.jpg)

## Category theory

This theroy is an abstraction level over many mathematics field. 

As regards programmation:
- type cosmogony
- relevant types

Think about the construction of numbers in mathematics.

---
![bg](background.jpg)

The idea was to do the same in informatics: from some primitive types, how to create new types ?

Mathematicians identifies some releveant structures:
- Structure over which operators are stable: Monoid
  - Example: let us think about a clock. By adding or substracting time, you still get a time that can be displayed by a clock !
- Constructor types known as **functors**. 
  - Example: from a type ```Int```, I can create the new type ```List[Int]```
- Better: Monad for composition chaining
- Alternative: Applicative functor for some parallel processing 

---
![bg](background.jpg)

## Higher kinded types

Reminder: A class defines a type but a type is not necessarly a class.

Example: Higher kinded types
```
trait SuperType[F[_]] { ... }
```
see: https://www.baeldung.com/scala/higher-kinded-types

To go further, see Bartosz Milewski's courses.

---
![bg](background.jpg)

## Methodology

- Pre-requiste: Specificaitons  (example: Whatsapp = a UI with one field and that's it !)

- POC (if necessary): check feasibility
- Write detailed specification before any coding phase
- Write some signature of functions only and try TDD approach
- Have always a working code. Step by step: have a strategy for publishing incremental versions
- Take time to read the code once finished. Code review if possible
