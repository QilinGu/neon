package models

import anorm.{SQL, SqlQuery}
import play.api.Play.current
import play.api.db.DB

case class Question(id: Long, title: String, body: String)

object Question {
  def all = DB.withConnection { implicit connection =>
    findAllQuery().map { row => mapQuestion(row) }.toList
  }

  def find(id: Long) = DB.withConnection { implicit connection =>
    findOneQuery.on("id" -> id).map { row => mapQuestion(row) }.singleOpt
  }

  val findAllQuery = SQL("SELECT * FROM questions");
  val findOneQuery = SQL("SELECT * FROM questions WHERE id = {id}")
  val createQuestionStatement =
    SQL("INSERT INTO questions (title, body, user_id, created_at) " +
      "VALUES ({title}, {body}, {user_id}, NOW()) RETURNING id")

  def create(title: String, body: String, userId: Long): Option[Question] = {
    DB.withConnection { implicit connection =>
      createQuestionStatement.on(
        "title" -> title,
        "body" -> body,
        "user_id" -> userId
      ).map { row => Question(row[Long]("id"), title, body) }.singleOpt()
    }
  }

  private def mapQuestion(row: anorm.Row): Question =
    Question(row[Long]("id"), row[String]("title"), row[String]("body"))
}
