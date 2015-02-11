package models

import anorm.{ SQL, SqlQuery, Row }
import play.api.Play.current
import play.api.db.DB

case class User(id: Long, githubId: Long, username: String)

object User {
  def find(id: Long) =
    DB.withConnection { implicit connection =>
      SQL("SELECT * FROM users WHERE id = {id}")
        .on("id" -> id).map(mapUser _).singleOpt
    }

  def findByGithubId(githubId: Long) =
    DB.withConnection { implicit connection =>
      SQL("SELECT * FROM users WHERE github_id = {github_id}")
        .on("github_id" -> githubId).map(mapUser _).singleOpt
    }

  def findOrCreateByGithubProfile(githubId: Long, username: String): Option[User] =
    DB.withConnection { implicit connection =>
      findByGithubId(githubId).orElse(create(githubId, username))
    }

  def create(githubId: Long, username: String): Option[User] =
    DB.withConnection { implicit connection =>
      insertStatement(githubId, username)
        .map(row => User(row[Long]("id"), githubId, username))
        .singleOpt
    }

  private def insertStatement(githubId: Long, username: String) =
    SQL("INSERT INTO users (github_id, username, created_at) VALUES " +
      "({github_id}, {username}, NOW()) RETURNING id")
      .on("github_id" -> githubId, "username" -> username)

  private def mapUser(row: Row): User =
    User(row[Long]("id"), row[Long]("github_id"), row[String]("username"))
}
