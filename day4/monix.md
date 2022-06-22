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

# Monix overview

---
![bg](background.jpg)

## Thread

- A thread is a native OS thread
- Green thread 
  - Thread managed by a VM or runtime library (here: JVM !)
  - Initially done in the first versions of Java
  - Map M threads to N native threads

---
![bg](background.jpg)

## Fiber
- Lightweight suspendable green thread
- Descriptive control flow

The architecture behind the scene is:
- A scheduler per worker thread
- A queue of request
- Unqueuing request induces a fiber creation
  
https://typelevel.org/blog/2021/02/21/fibers-fast-mkay.html

---
![bg](background.jpg)

## Control flow

A control flow:
- Contains the logic of execution
- Is a set of instruction
- Dealing with a flow of data

Examples: 
- Talend pipelines
- Data pipelines built with Spark framework
- Akka Streams library

---
![bg](background.jpg)

## Monix

- Reactive library 
- Asynchronous task composition
- Purely functionnal library

see https://monix.io/


---
![bg](background.jpg)

Monix gather several sub-projects:
- Monix-ececution / scala.concurrent
- Monix-catnap / cats-effects: CircuitBreaker, MVar
- Monix-eval: Task and Coeval
- Monix-reactive: Observable (ReactiveX protocol compliant)
  - compatible with Reactive Streams specification
  - asynchronous stream processing with non-blocking back pressure
- Monix-tail: Iterant (pulled bases streaming data type)

---
![bg](background.jpg)

## Vocabulary

- Asynchronous Boundary is a context switch between threads

