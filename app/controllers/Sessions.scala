package controllers

import models.User
import play.api.Play.current
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.ws.WS
import play.api.mvc._
import scala.concurrent.Await
import scala.concurrent.duration._

object Sessions extends Controller {
  def start = Action {
    Redirect(oauthRedirectUrl)
  }

  def close = Action { implicit request =>
    Redirect(routes.Questions.index)
      .withSession(request.session - "user.id" - "user.login")
  }

  def authenticate(code: String) = Action { implicit request =>
    getUserProfile(code) match {
      case Some(profile) => saveUserAndRedirect(request, profile)
      case None => InternalServerError("Sorry, something went wrong.")
    }
  }

  val oauthClientId = readConfig("oauth.github.client_id")
  val oauthClientSecret = readConfig("oauth.github.client_secret")

  def readConfig(key: String): String =
    current.configuration.getString(key) match {
      case Some(value) => value
      case None => ""
    }

  val oauthRedirectUrl =
    s"https://github.com/login/oauth/authorize?client_id=$oauthClientId"
  val oauthAccessTokenUrl = "https://github.com/login/oauth/access_token"
  val userDetailsUrl = "https://api.github.com/user"

  case class UserProfile(githubId: Long, username: String)

  def getUserProfile(code: String): Option[UserProfile] = {
    val tokenResponseFuture = WS.url(oauthAccessTokenUrl)
      .withHeaders("Accept" -> "application/json")
      .withQueryString(
        "client_id" -> oauthClientId,
        "client_secret" -> oauthClientSecret,
        "code" -> code
      ).post("")

    val tokenResponse = Await.result(tokenResponseFuture, 60 seconds)
    val tokenJson = Json.parse(tokenResponse.body)

    (tokenJson \ "access_token") match {
      case token: JsString => {
        val userResponseFuture = WS.url(userDetailsUrl)
          .withHeaders("Authorization" -> ("token " + token.as[String])).get

        val userResponse = Await.result(userResponseFuture, 60 seconds)
        Some(Json.parse(userResponse.body).as[UserProfile])
      }
      case _ => None
    }
  }

  def saveUserAndRedirect(request: Request[AnyContent], userProfile: UserProfile) = {
    val user = User.findOrCreateByGithubProfile(userProfile.githubId, userProfile.username)

    user match {
      case Some(user) => Redirect(routes.Questions.index).withSession(request.session + ("user.id" -> user.id.toString))
      case None => InternalServerError("Something went wrong.")
    }
  }

  implicit val userReads: Reads[UserProfile] = (
    (JsPath \ "id").read[Long] and
    (JsPath \ "login").read[String]
  )(UserProfile.apply _)
}
