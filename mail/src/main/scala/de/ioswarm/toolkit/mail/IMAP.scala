package de.ioswarm.toolkit.mail

import javax.mail.{Session => MailSession, Store => MailStore}

import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by apape on 20.11.17.
  */
/*
  TODO:
   - remove
   - moveTo
   - reply
   - as streaming source and ?flow?
 */
object IMAP {

  private[mail] def config: Config = ConfigFactory.load().getConfig("mail.imap")

  def apply(cfg: Config = config, protocol: String = "imap", hostname: Option[String] = None, port: Option[Int] = None, username: Option[String] = None, password: Option[String] = None): IMAP = new IMAP(
    protocol
    , hostname.getOrElse(config.getString("hostname"))
    , port.getOrElse(cfg.getInt("port"))
    , username.getOrElse(cfg.getString("username"))
    , password.getOrElse(cfg.getString("password"))
  )

  def apply(hostname: String): IMAP = apply(hostname = Some(hostname))
  def apply(hostname: String, port: Int): IMAP = apply(hostname = Some(hostname), port = Some(port))
  def apply(hostname: String, username: String, password: String): IMAP = apply(hostname = Some(hostname), username = Some(username), password = Some(password))
  def apply(hostname: String, port: Int, username: String, password: String): IMAP = apply(hostname = Some(hostname), port = Some(port), username = Some(username), password = Some(password))
  def apply(username: String, password: String): IMAP = apply(username = Some(username), password = Some(password))

}
object IMAPS {

  def apply(hostname: String): IMAP = IMAP(protocol = "imaps", hostname = Some(hostname))
  def apply(hostname: String, port: Int): IMAP = IMAP(protocol = "imaps", hostname = Some(hostname), port = Some(port))
  def apply(hostname: String, username: String, password: String): IMAP = IMAP(protocol = "imaps", hostname = Some(hostname), username = Some(username), password = Some(password))
  def apply(hostname: String, port: Int, username: String, password: String): IMAP = IMAP(protocol = "imaps", hostname = Some(hostname), port = Some(port), username = Some(username), password = Some(password))
  def apply(username: String, password: String): IMAP = IMAP(protocol = "imaps", username = Some(username), password = Some(password))

}
private[mail] class IMAP(protocol: String, hostname: String, port: Int, username: String, password: String) extends Store {

  val session: MailSession = {
    val props = System.getProperties
    props.setProperty("mail.store.protocol", protocol)
    MailSession.getDefaultInstance(props)
  }

  val store: MailStore = session.getStore(protocol)

  store.connect(hostname, port, username, password)

}
