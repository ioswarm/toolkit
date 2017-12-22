package de.ioswarm.toolkit.poi

import org.apache.poi.ss.usermodel.CellType

/**
  * Created by apape on 23.11.17.
  */
object Implicits extends BasicCellFormats {

  /* CELLS */
  implicit def optionCellFormat[T](implicit format: CellFormat[T]) = new CellFormat[Option[T]] {
    override def read(c: Cell): Option[T] =
      try {
        Some(format.read(c))
      } catch {
        case e: Exception => None
      }

    override def write(c: Cell, t: Option[T]): Unit = t match {
      case Some(x) => format.write(c, x)
      case None => c.cell.setCellType(CellType.BLANK)
    }
  }


  /* ROWS */
  implicit def optionRowFormat[T](implicit format: RowFormat[T]) = new RowFormat[Option[T]] {

    override def read(row: Row): Option[T] =
      try {
        Some(format.read(row))
      } catch {
        case e: Exception => None
      }

    override def write(row: Row, t: Option[T]): Unit = t match {
      case Some(x) => format.write(row, x)
      case None => row.cells.foreach(_.cell.setCellType(CellType.BLANK))
    }
  }

}
