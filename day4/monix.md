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

We will see what is Monix.

But before, let us review some definitions about Threads.

- A thread is a unit of execution inside a process: 'lightweight process'
- A process consists of several threads that share memory. 
- Processes are independent.

---
![bg](background.jpg)



## JVM Thread

- A JVM thread is mapped to a native OS thread
- Green thread (first JVM thread implementation)
  - Thread managed by a VM or runtime library (here: JVM !)
  - Initially done in the first versions of Java
  - Map M threads to N native OS threads (M > N)
  - See fiber model which looks similar

---
![bg](background.jpg)

## User vs Daemon threads

- Daemon thread: low priority thread running in background
  - Example: The GC thread is a deamon thread
- User thread: high priority thread running in foreground and created by the application
  - Example: The main thread that runs the entry point of the application is started by the JVM.
- The JVM exits when the last user thread terminates


---
![bg](background.jpg)

## Execution context

Scala-lang.org::
> An execution context can execute program logic asynchronously typically but not necessarily on a thread pool

- Global execution context: default Execution context associated with a (deamon) thread pool
- A thread pool:
  - More effective than creating and deleting a thread when needed
  - Schedule its own threads

---
![bg](background.jpg)

![bg](background.jpg)

## Monix

- Reactive library 
- Asynchronous task composition
- Purely functionnal library

see https://monix.io/


---

## Scheduler

It look like an execution context:
  - A scheduler is required when running a task (Thread Pool)

Moreover:
  - A scheduler is also a replacement for Java’s ScheduledExecutorService

But it is only required at run time level and not at declaration level.

---
![bg](background.jpg)

## Fiber
- Lightweight suspendable green thread
- Descriptive control flow

The architecture behind the scene is:
- A scheduler per worker thread
- A queue of requests
- Unqueuing request induces a fiber creation
  
https://typelevel.org/blog/2021/02/21/fibers-fast-mkay.html

---
![bg](background.jpg)

## Control flow

A control flow:
- Contains the logic of execution
- Is a set of instructions
- Deals with a flow of data

Examples: 
- Talend pipelines
- Data pipelines built with Spark framework
- Akka Streams library

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

- 'Asynchronous Boundary' is a context switch between threads
- 'Task shift': thread pool switch
  - Without argument: default thread pool
  - With a Scheduler argument: shift to the tread pool of the Scheduler


---
![bg](background.jpg)

## Examples

- Sync
- Async
- Observer
- Advanced example: Graph of Tasks execution


