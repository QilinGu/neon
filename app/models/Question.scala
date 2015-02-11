package models

import anorm.{SQL, SqlQuery, Row}
import play.api.Play.current
import play.api.db.DB

case class Question(id: Long, title: String, body: String)

object Question {
  def all =
    DB.withConnection { implicit connection =>
      SQL("SELECT * FROM questions")().map(mapQuestion _).toList
    }

  def find(id: Long) =
    DB.withConnection { implicit connection =>
      SQL("SELECT * FROM questions WHERE id = {id}")
        .on("id" -> id).map(mapQuestion _).singleOpt
    }

  def create(title: String, body: String, userId: Long): Option[Question] =
    DB.withConnection { implicit connection =>
      insertStatement(title, body, userId)
        .map(row => Question(row[Long]("id"), title, body))
        .singleOpt
    }

  def insertStatement(title: String, body: String, userId: Long) =
    SQL("INSERT INTO questions (title, body, user_id, created_at) " +
      "VALUES ({title}, {body}, {user_id}, NOW()) RETURNING id")
      .on("title" -> title, "body" -> body, "user_id" -> userId)

  private def mapQuestion(row: Row): Question =
    Question(row[Long]("id"), row[String]("title"), row[String]("body"))
}
