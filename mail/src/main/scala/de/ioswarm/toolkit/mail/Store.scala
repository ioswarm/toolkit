package de.ioswarm.toolkit.mail

import javax.mail.{Session => MailSession, Store => MailStore}

/**
  * Created by apape on 20.11.17.
  */
trait Store {

  def session: MailSession
  def store: MailStore

  def close(): Unit = store.close()

  def folder(path: String): Folder = new Folder(store.getFolder(path))

}
