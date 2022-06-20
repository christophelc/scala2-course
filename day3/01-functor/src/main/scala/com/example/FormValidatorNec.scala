package com.example

import cats.data.ValidatedNec
import cats.implicits._

case class RegistrationData(username: String, firstname: String, age: Int)

sealed trait FormValidatorNec {

  type ValidationResult[A] = ValidatedNec[DomainValidation, A]

  private def validateUserName(userName: String): ValidationResult[String] =
    if (userName.matches("^[a-zA-Z0-9]+$"))
      userName.validNec
    else
      UsernameHasSpecialCharacters.invalidNec

  private def validateFirstName(firstName: String): ValidationResult[String] =
    if (firstName.matches("^[a-zA-Z]+$"))
      firstName.validNec
    else
      FirstNameHasSpecialCharacters.invalidNec

  private def validateAge(age: Int): ValidationResult[Int] =
    if (age >= 18 && age <= 75)
      age.validNec
    else
      AgeIsInvalid.invalidNec

  def validateForm(username: String, firstName: String, age: Int): ValidationResult[RegistrationData] =
    (validateUserName(username), validateFirstName(firstName), validateAge(age)).mapN(RegistrationData)
}
object FormValidatorNec extends FormValidatorNec
