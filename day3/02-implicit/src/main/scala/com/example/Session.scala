package com.example

case class Context(id: String, factor: Int)

case class Number(a: Int) {
  def mul(b: Number)(implicit ctx: Context): Number =
    Number(a * b.a * ctx.factor)

}
