package de.ioswarm.toolkit

import java.io.{BufferedInputStream, BufferedOutputStream, File, FileOutputStream}
import java.nio.channels.Channels

import javax.mail.internet.InternetAddress
import javax.mail.{BodyPart, Multipart, Part}
import org.apache.commons.mail.{Email, HtmlEmail, MultiPartEmail, SimpleEmail}

import scala.io.Source

/**
  * Created by apape on 20.11.17.
  */
package object mail {

  def folder(path: String)(implicit store: Store): Folder = store.folder(path)

  def inbox(implicit store: Store): Folder = folder("INBOX")(store)

  implicit def stringToInternetAddress(s: String): InternetAddress = new InternetAddress(s)

  implicit def stringToAttachment(s: String): Attachment = Attachment(new File(s))

  implicit class ExtendString(s: String) {

    def folder(implicit store: Store): Folder = mail.folder(s)(store)

    def to(a: InternetAddress): Mail = Mail(s) :+ a
    def to(a: String): Mail = Mail(s) :+ a

  }

  implicit class ExtendPart(part: Part) {

    def isAttachment: Boolean = Part.ATTACHMENT == part.getDisposition || part.getContentType.startsWith("application/")
    def isInline: Boolean = Part.INLINE == part.getDisposition

    def isMultipart: Boolean = part.getContentType.toLowerCase.startsWith("multipart/")

    def multipartContent: Multipart = part.getContent.asInstanceOf[Multipart]

    def bodyParts: Seq[BodyPart] = if (!isMultipart) Seq.empty[BodyPart] else {
      val mp = multipartContent
      for (i <- 0 until mp.getCount) yield mp.getBodyPart(i)
    }

    def attachmentParts: Seq[Part] = bodyParts.filter(_.isAttachment) ++: bodyParts.filter(!_.isAttachment).flatMap(p => p.attachmentParts)
    def attachments: Seq[Attachment] = attachmentParts.map(a => new PartAttachmentImpl(a))

    def isContentPart(contentType: String): Boolean = part.getContentType.toLowerCase.startsWith(contentType.toLowerCase())

    def contentPart(contentType: String): Seq[Part] = if (isContentPart(contentType)) Seq(part) else Seq.empty[Part] ++:
      bodyParts.filter(_.isContentPart(contentType)) ++:
      bodyParts.filter(_.isMultipart).flatMap(_.contentPart(contentType))

    def saveToFile(file: File): Unit = {
      new FileOutputStream(file).getChannel.transferFrom(Channels.newChannel(part.getInputStream), 0, Long.MaxValue)
    }
  }

  implicit class ExtendInternetAddress(adr: InternetAddress) {

    def email: String = adr.getAddress
    def name: String = adr.getPersonal

    def to(a: InternetAddress): Mail = Mail(from = adr) :+ a

  }

  implicit class ExtendMail(mail: Mail) {

    def toEmail: Email = {
      val email = mail match {
        case m: Mail if m.isHTMLMail =>
          val ret = new HtmlEmail()
          ret.setTextMsg(mail.body)
          ret.setHtmlMsg(mail.htmlBody.getOrElse(""))
          mail.attachments.foreach(a => ret.attach(a.file))
          ret
        case m: Mail if m.attachments.nonEmpty =>
          val ret = new MultiPartEmail()
          mail.attachments.foreach(a => ret.attach(a.file))
          ret.setSubject(mail.subject)
          ret.setMsg(mail.body)
          ret
        case _ =>
          val ret = new SimpleEmail()
          ret.setSubject(mail.subject)
          ret.setMsg(mail.body)
          ret
      }

      email.setFrom(mail.from.email, mail.from.name)
      mail.replyTo.map(rpl => email.addReplyTo(rpl.email, rpl.name))
      mail.to foreach (adr => email.addTo(adr.email, adr.name))
      mail.cc foreach (adr => email.addCc(adr.email, adr.name))
      mail.bcc foreach (adr => email.addBcc(adr.email, adr.name))
      email
    }

  }

}
