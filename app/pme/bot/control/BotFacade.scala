package pme.bot.control

import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods._
import info.mukel.telegrambot4s.models.{InlineKeyboardMarkup, _}
import pme.bot.callback
import pme.bot.entity.BotContext.settings
import pme.bot.entity.{BadArgumentException, CallbackTag}

import scala.concurrent.Future

case class BotFacade() extends TelegramBot
  with Polling
  with Commands
  with Callbacks {

  def token: String = settings.token

  def sendMessage(msg: Message, text: String): Future[Message] =
    request(SendMessage(msg.source, text, parseMode = Some(ParseMode.HTML)))

  def sendMessage(msg: Message, text: String, replyMarkup: Option[ReplyMarkup] = None): Future[Message] =
    request(SendMessage(msg.source, text, parseMode = Some(ParseMode.HTML), replyMarkup = replyMarkup))

  def sendDocument(msg: Message, inputFile: InputFile): Future[Message] =
    request(SendDocument(msg.source, inputFile))

  def sendPhoto(msg: Message, inputFile: InputFile): Future[Message] =
    request(SendPhoto(msg.source, inputFile))

  def sendEditMessage(msg: Message, markup: Option[InlineKeyboardMarkup]): Future[Either[Boolean, Message]] =
    request(
      EditMessageReplyMarkup(
        Some(ChatId(msg.source)), // msg.chat.id
        Some(msg.messageId),
        replyMarkup = markup))

  def createDefaultButtons(labels: String*): Some[InlineKeyboardMarkup] =
    Some(InlineKeyboardMarkup(
      labels.map(label => Seq(
        InlineKeyboardButton(label, callbackData = Some(callback + label))))))

  // creates buttons for a Seq of CallbackTags
  // by default 2 buttons per row
  protected def incidentTagSelector(tags: Seq[CallbackTag], columns: Int = 2): InlineKeyboardMarkup =
    InlineKeyboardMarkup(
      tags.grouped(columns).map { row =>
        row.map(t => InlineKeyboardButton.callbackData(t.label, tag(t.name)))
      }.toSeq
    )

  // prefix a callback name with the standard prefix
  protected def tag(name: String): String = callback + name

  def fileName(fileId: String, path: String): String =
    fileId.substring(10) + path.substring(path.lastIndexOf("/") + 1)

  // if successful it returns a pair (fileId, fileUrl)
  def getFilePath(msg: Message, maxSize: Option[Int] = None): Future[(String, String)] = {
    val optFileId: Option[String] =
      msg.document.map(_.fileId)
        .orElse(msg.video.map(_.fileId))
        .orElse(extractPhoto(msg, maxSize))

    Future.successful(optFileId) flatMap {
      case Some(fileId: String) =>
        request(GetFile(fileId)).map { (file: File) =>
          file.filePath match {
            case Some(path) =>
              (file.fileId, fileUrl(path))
            case _ =>
              throw  BadArgumentException(s"I could not retrieve the File from the fileId: $fileId")
          }
        }
      case _ =>
        throw  BadArgumentException("Sorry but you have to add a file to the chat.")
    }
  }

  private def extractPhoto(msg: Message, maxSize: Option[Int]): Option[String] = {
    maxSize match {
      case None => msg.photo.map(_.last.fileId)
      case Some(size) => msg.photo.map(ps =>
        ps.tail.foldLeft[String](ps.head.fileId)((acc, ps: PhotoSize) =>
          if (ps.fileSize.isDefined && ps.fileSize.get <= size) ps.fileId else acc))
    }

  }

  private def fileUrl(filePath: String) =
    s"https://api.telegram.org/file/bot$token/$filePath"

}
