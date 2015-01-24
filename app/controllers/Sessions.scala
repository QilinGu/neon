package controllers

import scala.concurrent.duration._
import scala.concurrent.Await

import play.api.mvc.{Action, Controller}
import play.api.Play.current
import play.api.libs.ws.WS
import play.api.libs.json._
import play.api.libs.functional.syntax._

import models.User

object Sessions extends Controller {
  val oauthClientId =
    current.configuration.getString("oauth.github.client_id") match {
      case Some(clientId) => clientId
      case None => ""
    }

  val oauthClientSecret =
    current.configuration.getString("oauth.github.client_secret") match {
      case Some(secret) => secret
      case None => ""
    }

  val oauthRedirectUrl =
    "https://github.com/login/oauth/authorize?client_id=" + oauthClientId
  val oauthAccessTokenUrl = "https://github.com/login/oauth/access_token"
  val userDetailsUrl = "https://api.github.com/user"

  def start = Action {
    Redirect(oauthRedirectUrl)
  }

  def close = Action {
    implicit request =>
      Redirect(routes.Questions.index).withSession(request.session - "user.id" - "user.login")
  }

  def authenticate(code: String) = Action {
    request => {
      val tokenResponseFuture = WS.url(oauthAccessTokenUrl)
      .withHeaders("Accept" -> "application/json")
      .withQueryString(
        "client_id" -> oauthClientId,
        "client_secret" -> oauthClientSecret,
        "code" -> code).post("")

      val tokenResponse = Await.result(tokenResponseFuture, 60 seconds)
      val tokenJson = Json.parse(tokenResponse.body)

      (tokenJson \ "access_token") match {
        case token: JsString => {
          val userResponseFuture = WS.url(userDetailsUrl)
          .withHeaders("Authorization" -> ("token " + token.as[String])).get

          val userResponse = Await.result(userResponseFuture, 60 seconds)
          val user = Json.parse(userResponse.body).as[User]

          Redirect(routes.Questions.index).withSession(
            request.session
              + ("user.id" -> user.id.toString)
              + ("user.login" -> user.login))
        }
        case _ => InternalServerError("Sorry, something went wrong.")
      }
    }
  }

  implicit val userReads: Reads[User] = (
    (JsPath \ "id").read[Long] and
    (JsPath \ "login").read[String]
  )(User.apply _)
}
