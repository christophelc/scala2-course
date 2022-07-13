package model

import monix.eval.Task
import monix.reactive.Observable

trait  Runtime[T] {
  def executeSync(env: Env = Env.Empty): Env
  def observable(env: Env = Env.Empty): Observable[Env] = Observable.evalOnce(executeSync(env))
  def executeAsync(env: Env = Env.Empty): Task[Env] = Task {
    executeSync(env)
  }
}