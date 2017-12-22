package de.ioswarm.toolkit.mail

import javax.mail.{Folder => MailFolder}

/**
  * Created by apape on 20.11.17.
  */
private[mail] class Folder(mailFolder: MailFolder) {

  def printMails(): Unit = {
    if (!mailFolder.isOpen) open()
    mailFolder.getMessages.foreach(m => println(s"[${m.getMessageNumber}] ${m.getSubject} from ${m.getFrom.head}"))
  }

  def open(mode: Int = MailFolder.READ_ONLY): Folder = {
    mailFolder.open(mode)
    this
  }

  def close(expunge: Boolean = true): Unit = mailFolder.close(expunge)

  def messages(): Seq[Mail] = {
    if (!mailFolder.isOpen) open()
    mailFolder.getMessages.map(m => new MessageMailImpl(m))
  }

}