package com.example

trait Animal {
  def speed: Int = 0

  def showSpeed: Int = this.speed

  def me: this.type = this
  def meAgain       = this
}

trait AnimalDisplay {
  self: Animal =>

  def show(): Unit = println(s"${this.showSpeed} m/s")
}

//case class Dog(name: String) extends AnimalDisplay // do not compile
case class Dog(name: String) extends AnimalDisplay with Animal {
  override def speed: Int = 3

}
