package de.ioswarm.toolkit.mail

import org.apache.commons.mail.DefaultAuthenticator

/**
  * Created by apape on 21.11.17.
  */
/*
  TODO:
    - as streaming sink and flow
 */
object SMTP {

  def apply(hostname: String, port: Int = 25, starttls: Boolean = false, ssl: Boolean = false, username: Option[String] = None, password: Option[String] = None): SMTP = new SMTPImpl(
    hostname
    , port
    , starttls
    , ssl
    , username
    , password
  )

}
trait SMTP {

  def hostname: String
  def port: Int
  def starttls: Boolean
  def ssl: Boolean
  def username: Option[String]
  def password: Option[String]

  def withTLS(): SMTP
  def withSSL(): SMTP
  def insecure(): SMTP

  def send(mail: Mail): Unit = {
    val email = mail.toEmail
    email.setHostName(hostname)
    email.setSmtpPort(port)
    email.setStartTLSEnabled(starttls)
    email.setSSLOnConnect(ssl)
    if (username.isDefined) email.setAuthenticator(new DefaultAuthenticator(username.getOrElse(""), password.getOrElse("")))
    email.send()
  }

}
private[mail] case class SMTPImpl(hostname: String, port: Int, starttls: Boolean, ssl: Boolean, username: Option[String], password: Option[String]) extends SMTP {

  def withTLS(): SMTP = copy(starttls = true, ssl = false)
  def withSSL(): SMTP = copy(ssl = true, starttls = false)
  def insecure(): SMTP = copy(starttls = false, ssl = false)

}
