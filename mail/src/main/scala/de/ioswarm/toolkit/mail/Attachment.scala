package de.ioswarm.toolkit.mail

import java.io.{File, FileOutputStream}
import java.nio.file.Files
import java.text.Normalizer
import javax.mail.Part
import javax.mail.internet.MimeUtility

/**
  * Created by apape on 21.11.17.
  */
object Attachment {

  def apply(file: File): Attachment = new FileAttachmentImpl(file)

}

trait Attachment {

  def filename: String
  def size: Int

  def file: File

  def saveTo(dir: File): File
  def saveToFile(file: File): File

}
private[mail] class PartAttachmentImpl(part: Part) extends Attachment {

  def filename: String = {
    val decode = MimeUtility.decodeText(part.getFileName)
    Normalizer.normalize(decode, Normalizer.Form.NFC)
  }

  def file: File = {
    saveTo(new File(System.getProperty("java.io.tmpdir", "/tmp")))
  }

  def size: Int = part.getSize

  def saveTo(dir: File): File = saveToFile(new File(filename, dir.getAbsolutePath))

  def saveToFile(file: File): File = {
    part.saveToFile(file)
    file
  }

}

private[mail] class FileAttachmentImpl(val file: File) extends Attachment {

  def filename: String = file.getName
  def size: Int = file.length().toInt

  def saveTo(dir: File): File = {
    val f = new File(file.getName, dir.getAbsolutePath)
    saveToFile(f)
  }

  def saveToFile(f: File): File = {
    Files.copy(file.toPath, new FileOutputStream(f))
    f
  }

}