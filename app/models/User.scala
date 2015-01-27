package models

import anorm.{ SQL, SqlQuery }
import play.api.Play.current
import play.api.db.DB

case class User(id: Long, githubId: Long, username: String)

object User {
  val findQuery = SQL("SELECT * FROM users WHERE id = {id}")

  def find(id: Long) = DB.withConnection { implicit connection =>
    {
      findQuery.on("id" -> id).map {
        row =>
          User(
            row[Long]("id"),
            row[Long]("github_id"),
            row[String]("username")
          )
      }.singleOpt
    }
  }

  val findByGithubIdQuery = SQL("SELECT * FROM users WHERE github_id = {github_id}")

  def findByGithubId(githubId: Long) = DB.withConnection { implicit connection =>
    {
      findByGithubIdQuery.on("github_id" -> githubId).map {
        row =>
          User(
            row[Long]("id"),
            row[Long]("github_id"),
            row[String]("username")
          )
      }.singleOpt()
    }
  }

  val createUserStatement =
    SQL("INSERT INTO users (github_id, username, created_at) VALUES ({github_id}, {username}, NOW()) RETURNING id")

  def create(githubId: Long, username: String): Option[User] = {
    DB.withConnection { implicit connection =>
      createUserStatement.on("github_id" -> githubId, "username" -> username).map {
        row => User(row[Long]("id"), githubId, username)
      }.singleOpt()
                     }
  }

  def findOrCreateByGithubProfile(githubId: Long, username: String): Option[User] = {
    DB.withConnection { implicit connection =>
      findByGithubId(githubId) match {
        case user: Some[User] => user
        case None => create(githubId, username)
      }
    }
  }
}
