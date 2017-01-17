package scraper.fastparser

import fastparse.all._
import fastparse.core.Logger

import scraper._

class IdentifierSuite extends LoggingFunSuite {
  import WhitespaceApi._

  private implicit val parsingLogger = Logger(logInfo(_))

  private def fullyParse[T](parser: P[T], input: String) = {
    (Start ~~ parser ~~ End parse input).get.value
  }

  private def testSuccessfulParse[T](input: String, expected: T): Unit = {
    test(s"[o] identifier: $input") {
      assertResult(expected) {
        fullyParse(IdentifierParser.identifier, input)
      }
    }
  }

  private def testFailedParse[T](input: String): Unit = {
    test(s"[x] identifier: $input") {
      val cause = intercept[ParseError] {
        fullyParse(IdentifierParser.identifier, input)
      }

      logInfo(s"Expected parsing failure: ${cause.getMessage}")
    }
  }

  val successfulCases: Seq[(String, Name)] = Seq(
    // Regular identifiers
    "data" -> i"data",
    "数据" -> i"数据",

    // Delimited identifiers
    "\"data\"" -> "data",
    "\"数据\"" -> "数据",
    "\"double\"\"quote\"" -> "double\"quote",

    // Unicode delimited identifiers
    "U&\"data\"" -> "data",
    "U&\"\\6570\\636e\"" -> "数据",
    "U&\"\\0064\\0061\\0074\\0061\"" -> "data",
    "U&\"!!\"" -> "!!",
    "U&\"\\\\\"" -> "\\",

    // Unicode delimited identifiers with unicode escape specifier
    "U&\"d!0061t!+000061\" UESCAPE '!'" -> "data",
    "U&\"!!\" UESCAPE '!'" -> "!",
    "U&\"\\\\\" UESCAPE '!'" -> "\\\\"
  )

  successfulCases foreach {
    case (input, expected) =>
      testSuccessfulParse(input, expected)
  }

  val failedCases = Seq(
    "_data",
    "\"double\\\"quote\"",
    "U&\"!\" UESCAPE '!'",
    "U&\"\\\""
  )

  failedCases foreach testFailedParse
}
