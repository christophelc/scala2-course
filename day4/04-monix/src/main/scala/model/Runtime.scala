package model

import monix.eval.Task

trait  Runtime[T] {
  def executeSync(env: Env = Env.Empty): Env
  def executeAsync(env: Env = Env.Empty): Task[Env] = Task {
    executeSync(env)
  }
}