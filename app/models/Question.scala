package models

import anorm.{SQL, SqlQuery}
import play.api.Play.current
import play.api.db.DB

case class Question(id: Long, title: String, body: String)

object Question {
  val allQuery = SQL("SELECT * FROM questions");

  def all = DB.withConnection {
    implicit connection => {
      allQuery().map {
        row =>
          Question(row[Long]("id"),
                   row[String]("title"),
                   row[String]("body"))
      }.toList
    }
  }

  val findQuery = SQL("SELECT * FROM questions WHERE id = {id}")

  def find(id: Long) = DB.withConnection {
    implicit connection => {
      findQuery.on("id" -> id).map {
        row =>
          Question(row[Long]("id"),
                   row[String]("title"),
                   row[String]("body"))
      }.singleOpt
    }
  }
}
