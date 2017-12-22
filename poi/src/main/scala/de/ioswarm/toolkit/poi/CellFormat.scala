package de.ioswarm.toolkit.poi

import java.sql.{Time, Timestamp, Date => SQLDate}
import java.util.Date

import org.apache.poi.ss.usermodel.CellType

/**
  * Created by apape on 23.11.17.
  */
trait CellReader[T] {

  def read(c: Cell): T

}

trait CellWriter[T] {

  def write(c: Cell, t: T): Unit

}

trait CellFormat[T] extends CellReader[T] with CellWriter[T] {

}

trait BasicCellFormats {

  implicit def intFormat = new CellFormat[Int] {

    override def read(c: Cell): Int = c.cell.getCellTypeEnum match {
      case CellType.NUMERIC => c.cell.getNumericCellValue.toInt
      case CellType.FORMULA if c.cell.getCachedFormulaResultTypeEnum == CellType.NUMERIC =>
        c.cell.getNumericCellValue.toInt
      case CellType.BLANK => 0
      case _ => throw new IllegalArgumentException
    }
    override def write(c: Cell, t: Int): Unit = c.cell.setCellValue(t.toDouble)

  }

  implicit def longFormat = new CellFormat[Long] {

    override def read(c: Cell): Long = c.cell.getCellTypeEnum match {
      case CellType.NUMERIC => c.cell.getNumericCellValue.toInt
      case CellType.FORMULA if c.cell.getCachedFormulaResultTypeEnum == CellType.NUMERIC =>
        c.cell.getNumericCellValue.toLong
      case CellType.BLANK => 0l
      case _ => throw new IllegalArgumentException
//      case _ => null
    }
    override def write(c: Cell, t: Long): Unit = c.cell.setCellValue(t.toDouble)

  }

  implicit def doubleFormat = new CellFormat[Double] {

    override def read(c: Cell): Double = c.cell.getCellTypeEnum match {
      case CellType.NUMERIC => c.cell.getNumericCellValue
      case CellType.FORMULA if c.cell.getCachedFormulaResultTypeEnum == CellType.NUMERIC =>
        c.cell.getNumericCellValue
      case CellType.BLANK => 0.0d
      case _ => throw new IllegalArgumentException
//      case _ => null
    }
    override def write(c: Cell, t: Double): Unit = c.cell.setCellValue(t)
  }

  implicit def booleanFormat = new CellFormat[Boolean] {

    override def read(c: Cell): Boolean = c.cell.getCellTypeEnum match {
      case CellType.BOOLEAN => c.cell.getBooleanCellValue
      case CellType.FORMULA if c.cell.getCachedFormulaResultTypeEnum == CellType.BOOLEAN =>
        c.cell.getBooleanCellValue
      case _ => false
//      case _ => false
    }
    override def write(c: Cell, t: Boolean): Unit = c.cell.setCellValue(t)

  }

  implicit def stringFormat = new CellFormat[String] {

    override def read(c: Cell): String = c.cell.getCellTypeEnum match {
      case CellType.STRING => c.cell.getStringCellValue
      case CellType.BLANK => ""
      case CellType.BOOLEAN => c.cell.getBooleanCellValue.toString
      case CellType.NUMERIC => c.cell.getNumericCellValue.toString
      case CellType.ERROR => s"ERROR#${c.cell.getErrorCellValue}"
      case CellType.FORMULA if c.cell.getCachedFormulaResultTypeEnum == CellType.STRING =>
        c.cell.getStringCellValue
      case _ => throw new IllegalArgumentException
//      case _ => null
    }
    override def write(c: Cell, t: String): Unit = c.cell.setCellValue(t)

  }

  implicit def dateFormat = new CellFormat[Date] {

    override def read(c: Cell): Date = c.cell.getCellTypeEnum match {
      case CellType.NUMERIC => c.cell.getDateCellValue
      case _ => throw new IllegalArgumentException
    }

    override def write(c: Cell, t: Date): Unit = {
      val style = c.cell.getRow.getSheet.getWorkbook.createCellStyle()
      val helper = c.cell.getRow.getSheet.getWorkbook.getCreationHelper
      style.setDataFormat(helper.createDataFormat().getFormat("dd.MM.yyyy")) // TODO LOCALE?
      c.cell.setCellValue(t)
      c.cell.setCellStyle(style)
    }

  }

  implicit def sqlDateFormat = new CellFormat[SQLDate] {

    override def read(c: Cell): SQLDate = c.cell.getCellTypeEnum match {
      case CellType.NUMERIC => new SQLDate(c.cell.getDateCellValue.getTime)
      case _ => throw new IllegalArgumentException
    }

    override def write(c: Cell, t: SQLDate): Unit = {
      val style = c.cell.getRow.getSheet.getWorkbook.createCellStyle()
      val helper = c.cell.getRow.getSheet.getWorkbook.getCreationHelper
      style.setDataFormat(helper.createDataFormat().getFormat("dd.MM.yyyy")) // TODO LOCALE?
      c.cell.setCellValue(new Date(t.getTime))
      c.cell.setCellStyle(style)
    }

  }

  implicit def timestampFormat = new CellFormat[Timestamp] {

    override def read(c: Cell): Timestamp = c.cell.getCellTypeEnum match {
      case CellType.NUMERIC => new Timestamp(c.cell.getDateCellValue.getTime)
      case _ => throw new IllegalArgumentException
    }

    override def write(c: Cell, t: Timestamp): Unit = {
      val style = c.cell.getRow.getSheet.getWorkbook.createCellStyle()
      val helper = c.cell.getRow.getSheet.getWorkbook.getCreationHelper
      style.setDataFormat(helper.createDataFormat().getFormat("dd.MM.yyyy HH:mm:ss")) // TODO LOCALE?
      c.cell.setCellValue(new Timestamp(t.getTime))
      c.cell.setCellStyle(style)
    }

  }

  implicit def timeFormat = new CellFormat[Time] {

    override def read(c: Cell): Time = c.cell.getCellTypeEnum match {
      case CellType.NUMERIC => new Time(c.cell.getDateCellValue.getTime)
      case _ => throw new IllegalArgumentException
    }

    override def write(c: Cell, t: Time): Unit = {
      val style = c.cell.getRow.getSheet.getWorkbook.createCellStyle()
      val helper = c.cell.getRow.getSheet.getWorkbook.getCreationHelper
      style.setDataFormat(helper.createDataFormat().getFormat("HH:mm:ss")) // TODO LOCALE?
      c.cell.setCellValue(new Date(t.getTime))
      c.cell.setCellStyle(style)
    }

  }

}