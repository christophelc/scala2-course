package com.example

object StringUtils {
  implicit class StringImprovements(val s: String) {
    def bracket = s"[$s]"
  }
}
