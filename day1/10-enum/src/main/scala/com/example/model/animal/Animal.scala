package com.example.model.animal

sealed trait Animal
case class Dog(name: String) extends Animal
case class Cat(name: String) extends Animal
