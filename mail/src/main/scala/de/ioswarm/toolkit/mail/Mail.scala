package de.ioswarm.toolkit.mail

import java.util.Date

import javax.mail.Message.RecipientType
import javax.mail.internet.InternetAddress
import javax.mail.{Address, Flags, Message}

/**
  * Created by apape on 20.11.17.
  */
object Mail {

  def apply(from: InternetAddress
            , replyTo: Option[InternetAddress] = None
            , to: Seq[InternetAddress] = Seq.empty[InternetAddress]
            , cc: Seq[InternetAddress] = Seq.empty[InternetAddress]
            , bcc: Seq[InternetAddress] = Seq.empty[InternetAddress]
            , subject: String = ""
            , body: String = ""
            , html: Option[String] = None
            , attachments: Seq[Attachment] = Seq.empty[Attachment]
           ): Mail = new MailImpl(
    from
    , replyTo
    , to
    , cc
    , bcc
    , subject
    , body
    , html
    , attachments
  )

}

trait Mail {

  def from: InternetAddress
  def replyTo: Option[InternetAddress]
  def to: Seq[InternetAddress]
  def cc: Seq[InternetAddress]
  def bcc: Seq[InternetAddress]

  def subject: String

  def body: String
  def htmlBody: Option[String]

  def attachments: Seq[Attachment]

  def isHTMLMail: Boolean = htmlBody.isDefined

  def send(implicit smtp: SMTP): Unit = smtp.send(this)


  def from(a: InternetAddress): Mail

  def replyTo(rpl: InternetAddress): Mail

  def addTo(a: InternetAddress): Mail
  def :+(a: InternetAddress): Mail = addTo(a)

  def addCc(a: InternetAddress): Mail

  def addBcc(a: InternetAddress): Mail

  def subject(s: String): Mail

  def body(s: String): Mail

  def html(html: String): Mail

  def attach(a: Attachment*): Mail

  def delete(): Unit

  def reply(all: Boolean = false): Mail

}

private[mail] case class MailImpl(
                                 from: InternetAddress
                                 , replyTo: Option[InternetAddress]
                                 , to: Seq[InternetAddress]
                                 , cc: Seq[InternetAddress]
                                 , bcc: Seq[InternetAddress]
                                 , subject: String
                                 , body: String
                                 , htmlBody: Option[String]
                                 , attachments: Seq[Attachment]
                                 ) extends Mail {

  def from(a: InternetAddress): Mail = copy(from = a)

  def replyTo(rpl: InternetAddress): Mail = copy(replyTo = Some(rpl))

  def addTo(a: InternetAddress): Mail = copy(to = this.to :+ a)

  def addCc(a: InternetAddress): Mail = copy(cc = this.cc :+ a)

  def addBcc(a: InternetAddress): Mail = copy(bcc = this.bcc :+ a)

  def subject(s: String): Mail = copy(subject = s)

  def body(s: String): Mail = copy(body = s)

  def html(html: String): Mail = copy(htmlBody = Some(html))

  def attach(a: Attachment*): Mail = copy(attachments = this.attachments ++ a)

  override def delete(): Unit = throw new UnsupportedOperationException

  override def reply(all: Boolean = false): Mail = throw new UnsupportedOperationException

}

private[mail] class MessageMailImpl(message: Message) extends Mail {



  def recipients(recipientType: RecipientType): Array[Address] = {
    val ret = message.getRecipients(recipientType)
    if (ret == null) Array.empty[Address] else ret
  }

  def from: InternetAddress = message.getFrom.head.asInstanceOf[InternetAddress]

  def replyTo: Option[InternetAddress] = {
    val ret = message.getReplyTo
    if (ret == null) None else ret.headOption.map(_.asInstanceOf[InternetAddress])
  }


  def to: Seq[InternetAddress] = recipients(RecipientType.TO).map(_.asInstanceOf[InternetAddress])
  def cc: Seq[InternetAddress] = recipients(RecipientType.CC).map(_.asInstanceOf[InternetAddress])
  def bcc: Seq[InternetAddress] = recipients(RecipientType.BCC).map(_.asInstanceOf[InternetAddress])

  def subject: String = message.getSubject

  def body: String = message.contentPart("text/plain").headOption.map(_.getContent.toString).getOrElse(htmlBody.getOrElse(""))

  def htmlBody: Option[String] = message.contentPart("text/html").headOption.map(_.getContent.toString)

  def sendDate: Date = message.getSentDate
  def receiveDate: Date = message.getReceivedDate

  def contentType: String = message.getContentType

  def attachments: Seq[Attachment] = message.attachments

  def from(a: InternetAddress): Mail = {
    message.setFrom(a)
    this
  }

  def replyTo(rpl: InternetAddress): Mail = {
    message.setReplyTo(Array(rpl))
    this
  }

  def addTo(a: InternetAddress): Mail = {
    message.addRecipient(RecipientType.TO, a)
    this
  }

  def addCc(a: InternetAddress): Mail = {
    message.addRecipient(RecipientType.CC, a)
    this
  }

  def addBcc(a: InternetAddress): Mail = {
    message.setRecipient(RecipientType.BCC, a)
    this
  }

  def subject(s: String): Mail = {
    message.setSubject(s)
    this
  }

  def body(s: String): Mail = {
    message.setText(s)
    this
  }

  def html(html: String): Mail = this

  def attach(a: Attachment*): Mail = this

  override def delete(): Unit = message.setFlag(Flags.Flag.DELETED, true)

  override def reply(all: Boolean = false): Mail = new MessageMailImpl(message.reply(all))

}
