/*
 * Copyright 2017 Nicolas Rinaudo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kantan.csv.laws

import kantan.csv.engine.ReaderEngine
import kantan.csv.ops._

trait RfcReaderLaws {
  implicit def engine: ReaderEngine

  private def equals(csv: String, expected: List[List[Cell]]): Boolean =
    csv.unsafeReadCsv[List, List[String]](',', false) == expected.map(_.map(_.value))

  private def cellsToCsv(csv: List[List[Cell]], colSep: String = ",", rowSep: String = "\r\n"): String =
    valsToCsv(csv.map(_.map(_.encoded)), colSep, rowSep)

  private def valsToCsv(csv: List[List[String]], colSep: String = ",", rowSep: String = "\r\n"): String =
    csv.map(_.mkString(colSep)).mkString(rowSep)




  // - RFC 4180: 2.1 ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def crlfRowSeparator(csv: List[List[Cell]]): Boolean = equals(cellsToCsv(csv), csv)
  def lfRowSeparator(csv: List[List[Cell]]): Boolean   = equals(cellsToCsv(csv, rowSep = "\n"), csv)


  // - RFC 4180: 2.2 ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def crlfEnding(csv: List[List[Cell]]): Boolean  = equals(cellsToCsv(csv) + "\r\n", csv)
  def lfEnding(csv: List[List[Cell]]): Boolean    = equals(cellsToCsv(csv) + "\n", csv)
  def emptyEnding(csv: List[List[Cell]]): Boolean = equals(cellsToCsv(csv), csv)


  // - RFC 4180: 2.4 ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def leadingWhitespace(csv: List[List[Cell]]): Boolean = {
    val spaced = csv.map(_.map(_.map(s ⇒ " \t" + s)))
    equals(cellsToCsv(spaced), spaced)
  }

  def trailingWhitespace(csv: List[List[Cell]]): Boolean = {
    val spaced = csv.map(_.map(_.map(s ⇒ s + " \t")))
    equals(cellsToCsv(spaced), spaced)
  }

  def trailingComma(csv: List[List[Cell]]): Boolean =
    equals(cellsToCsv(csv, rowSep = ",\r\n"), csv.map(_ :+ Cell.Empty))


  // - RFC 4180: 2.5 ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def unnecessaryDoubleQuotes(csv: List[List[Cell.NonEscaped]]): Boolean =
    equals(valsToCsv(csv.map(_.map(v ⇒ "\"" + v.value + "\""))), csv)

  def unescapedDoubleQuotes(csv: List[List[Cell.NonEscaped]]): Boolean = {
    // Note that we trim here to make sure we don't have end up with whitespace followed by a double-quote: that'd be
    // a valid start of escaped cell.
    val corrupt = csv.map(_.map(_.map(_.trim.flatMap(_ + "\""))))
    equals(valsToCsv(corrupt.map(_.map(_.value))), corrupt)
  }


  // - RFC 4180: 2.6 ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def escapedCells(csv: List[List[Cell.Escaped]]): Boolean = equals(cellsToCsv(csv), csv)
}
