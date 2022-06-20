package com.example

import com.example.model.animal.Country
import org.scalatest.{ FlatSpec, MustMatchers }

class MainTest extends FlatSpec with MustMatchers {

  "Countryies" must "work" in {
    val countries = Country.values
    assert(countries.size == 2)
    assert(countries.equals(Seq(Country.Germany, Country.India)))
  }
}
