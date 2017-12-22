package de.ioswarm.toolkit.poi

/**
  * Created by apape on 24.11.17.
  */
trait RowReader[T] {

  def read(row: Row): T

}
trait RowWriter[T] {

  def write(row: Row, t: T): Unit

}
trait RowFormat[T] extends RowReader[T] with RowWriter[T] {

}
