package de.ioswarm.toolkit.poi

import java.io._

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy
import org.apache.poi.ss.usermodel.{Cell => POICell, Row => POIRow, Sheet => POISheet, Workbook => POIWorkbook}
import org.apache.poi.xssf.usermodel.XSSFWorkbook

/**
  * Created by apape on 22.11.17.
  */
object Workbook {

  def apply(file: File): Workbook =
    if (file.getName.toLowerCase().endsWith("xlsx") || file.getName.toLowerCase().endsWith("xlsm"))
      XSSF(file)
    else
      HSSF(file)

  def apply(s: String): Workbook = apply(new File(s))

  def XSSF(in: InputStream): Workbook = new Workbook(new XSSFWorkbook(in), None)
  def XSSF(file: File): Workbook = new Workbook(new XSSFWorkbook(new FileInputStream(file)), Some(file))

  def HSSF(in: InputStream): Workbook = new Workbook(new HSSFWorkbook(in), None)
  def HSSF(file: File): Workbook = new Workbook(new HSSFWorkbook(new FileInputStream(file)), Some(file))

  def XLSX(in: InputStream): Workbook = XSSF(in)
  def XLSX(file: File): Workbook = XSSF(file)

  def XLS(in: InputStream): Workbook = HSSF(in)
  def XLS(file: File): Workbook = HSSF(file)

  def open(file: File): Workbook = apply(file)
  def open(s: String): Workbook = apply(s)

}
class Workbook(workbook: POIWorkbook, wfile: Option[File]) {

  import scala.collection.JavaConverters._

  def apply(index: Int): Sheet = sheet(index)
  def apply(name: String): Sheet = sheet(name)

  def sheets: Iterator[Sheet] = workbook.sheetIterator().asScala.map(ps => new Sheet(this, ps))

  def sheet(index: Int): Sheet = {
    assert(index >= 0)
    if (index < workbook.getNumberOfSheets) new Sheet(this, workbook.getSheetAt(index))
    else new Sheet(this, workbook.createSheet(s"Sheet-${index+1}"))
  }
  def sheet(name: String): Sheet = sheets.find(s => s.name == name).getOrElse(new Sheet(this, workbook.createSheet(name)))
  def sheet(): Sheet = new Sheet(this, workbook.getSheetAt(workbook.getActiveSheetIndex))


  def save(file: File): Unit = workbook.write(new FileOutputStream(file))
  def save(out: OutputStream): Unit = workbook.write(out)
  def save(s: String): Unit = save(new File(s))
  def save(): Unit = wfile match {
    case Some(f) => save(f)
    case None => new IllegalArgumentException
  }

  def close(): Unit = workbook.close()

}

class Sheet(val workbook: Workbook, val sheet: POISheet) {

  import scala.collection.JavaConverters._

  def apply(index: Int): Row = row(index)
  def apply(rowidx: Int, cellidx: Int): Cell = row(rowidx)(cellidx)

  def name: String = sheet.getSheetName

  def rows: Iterator[Row] = sheet.rowIterator().asScala.map(new Row(this, _))

  def rows[T](implicit reader: RowReader[T]): Iterator[T] = sheet.rowIterator().asScala.map(r => reader.read(new Row(this, r)))

  def row(index: Int): Row =
    if (index >= min && index <= max)
      new Row(this, sheet.getRow(index))
    else
      createRow(index)

  def createRow(index: Int): Row = new Row(this, sheet.createRow(index))

  def min: Int = sheet.getFirstRowNum
  def max: Int = sheet.getLastRowNum

  def size: Int = sheet.getPhysicalNumberOfRows

  def as[T](index: Int)(implicit reader: RowReader[T]): T = row(index).as(reader)
  def get[T](index: Int)(implicit reader: RowReader[T]): T = as[T](index)(reader)

  def set[T](index: Int, t: T)(implicit writer: RowWriter[T]): Unit = row(index).set(t)(writer)


  def as[T](rowidx: Int, colidx: Int)(implicit reader: CellReader[T]): T = row(rowidx).cell(colidx).as(reader)
  def get[T](rowidx: Int, colidx: Int)(implicit reader: CellReader[T]): T = as(rowidx, colidx)(reader)

  def set[T](rowidx: Int, colidx: Int, t: T)(implicit writer: CellWriter[T]): Unit = row(rowidx).cell(colidx).set(t)(writer)

}

class Row(val sheet: Sheet, val row: POIRow) {

  import scala.collection.JavaConverters._

  def apply(index: Int): Cell = cell(index)

  def apply[T](index: Int, t: T)(implicit writer: CellWriter[T]): Unit = set(index, t)(writer)

  def index: Int = row.getRowNum

  def min: Int = row.getFirstCellNum
  def max: Int = row.getLastCellNum

  def cells: Iterator[Cell] = row.cellIterator().asScala.map(new Cell(this, _))

  def cell(index: Int): Cell = new Cell(this, row.getCell(index, MissingCellPolicy.CREATE_NULL_AS_BLANK))

  def as[T](implicit reader: RowReader[T]): T = reader.read(this)
  def get[T](implicit reader: RowReader[T]): T = as(reader)

  def set[T](t: T)(implicit writer: RowWriter[T]): Unit = writer.write(this, t)

  def as[T](index: Int)(implicit reader: CellReader[T]): T = cell(index).as(reader)
  def get[T](index: Int)(implicit reader: CellReader[T]): T = as(index)(reader)

  def set[T](index: Int, t: T)(implicit writer: CellWriter[T]): Unit = cell(index)(t)(writer)

  def workbook: Workbook = sheet.workbook

}

class Cell(val row: Row, val cell: POICell) {

  def apply[T](t: T)(implicit writer: CellWriter[T]): Cell = {
    set(t)(writer)
    this
  }

  def apply[T](implicit reader: CellReader[T]): T = as(reader)

  def index: Int = cell.getColumnIndex

  def as[T](implicit reader: CellReader[T]): T = reader.read(this)
  def get[T](implicit reader: CellReader[T]): T = as(reader)

  def set[T](t: T)(implicit writer: CellWriter[T]): Unit = writer.write(this, t)

  def sheet: Sheet = row.sheet
  def workbook: Workbook = sheet.workbook

}
