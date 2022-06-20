package com.example

// the alternative is def build:: Dsl
// but then we cannot anymore call show() !
trait Dsl {
  def build: this.type
}
class Show(v: String) extends Dsl {
  override def build: this.type = this
  def show(): this.type = {
    println(s"showing value $v")
    this
  }
}

class ShowSpecific(v: String) extends Show(v)
