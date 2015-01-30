package models

import anorm.{ SQL, SqlQuery }
import play.api.Play.current
import play.api.db.DB
import controllers.QuestionParams

case class Question(id: Long, title: String, body: String)

object Question {
  val allQuery = SQL("SELECT * FROM questions");

  def all = DB.withConnection { implicit connection =>
    allQuery().map {
      row =>
      Question(
        row[Long]("id"),
        row[String]("title"),
        row[String]("body")
      )
    }.toList
  }

  val findQuery = SQL("SELECT * FROM questions WHERE id = {id}")

  def find(id: Long) = DB.withConnection {
    implicit connection =>
      {
        findQuery.on("id" -> id).map {
          row =>
            Question(
              row[Long]("id"),
              row[String]("title"),
              row[String]("body")
            )
        }.singleOpt
      }
  }

  val createQuestionStatement =
    SQL("INSERT INTO questions (title, body, created_at) VALUES ({title}, {body}, NOW()) RETURNING id")

  def create(params: QuestionParams): Option[Question] = {
    DB.withConnection { implicit connection =>
      createQuestionStatement.on(
        "title" -> params.title,
        "body" -> params.body
      ).map {
          row => Question(row[Long]("id"), params.title, params.body)
        }.singleOpt()
    }
  }
}
