package controllers

import scala.concurrent.duration._
import scala.concurrent.Await

import play.api.mvc.{Action, Controller}
import play.api.Play.current
import play.api.libs.ws.WS
import play.api.libs.json._

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
        case _: JsUndefined => InternalServerError("Sorry, something went wrong.")
        case token: JsString => {

          val userResponseFuture = WS.url(userDetailsUrl)
          .withHeaders("Authorization" -> ("token " + token.as[String])).get

          val userResponse = Await.result(userResponseFuture, 60 seconds)
          val userJson = Json.parse(userResponse.body)

          (userJson \ "id") match {
            case _: JsUndefined => InternalServerError("Sorry, something went wrong.")
            case id: JsNumber => {
              Redirect(routes.Questions.index).withSession(
                request.session + ("user.id" -> id.toString))
            }
          }
        }
      }
    }
  }
}
