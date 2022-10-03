package com.example

case class Person(firstname: String, lastname: String)

// typeclass
trait Serializer[T] {
  def stringify(t: T): String
}

// API for type class
object Show {
  // function version
  def show[T](t: T)(implicit serializer: Serializer[T]) = serializer.stringify(t)

  // class extension version
  implicit class ShowClass[T: Serializer](t: T) {
    def show: String = Show.show(t)
  }
}

object SerializerInstance {
  implicit val serializerString: Serializer[String] = new Serializer[String] {
    override def stringify(t: String): String = s" ==> $t"
  }
  implicit val serializerPerson: Serializer[Person] = new Serializer[Person] {
    override def stringify(t: Person): String = s" ==> ${t.firstname} ${t.lastname}"
  }
}
