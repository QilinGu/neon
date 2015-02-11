package controllers

import models.User
import play.api.Play.current
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.ws.{WS, WSResponse}
import play.api.mvc._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object Sessions extends Controller {
  def start = Action {
    Redirect(OAuthRedirectUrl)
  }

  def close = Action { request =>
    Redirect(routes.Questions.index).withSession(request.session - "user.id")
  }

  def authenticate(code: String) = Action { request =>
    getProfile(code)
      .flatMap(profile => saveUserAndRedirect(request, profile))
      .getOrElse(InternalServerError(views.html.errors.serverError()))
  }

  private def getProfile(code: String): Option[Profile] =
    requestAccessToken(code).flatMap(token => requestProfile(token))

  private def requestAccessToken(code: String): Option[String] =
    request[AccessToken](accessTokenRequest(code), tokenParser).map(_.token)

  private def requestProfile(authToken: String): Option[Profile] =
    request[Profile](profileRequest(authToken), profileParser)

  private def request[A](request: Future[WSResponse], formatter: Reads[A]): Option[A] = {
    val response = Await.result(request, 10 seconds)
    try Some(Json.parse(response.body).as[A](formatter))
    catch { case e: Exception => None }
  }

  def saveUserAndRedirect[A](request: Request[A], profile: Profile): Option[Result] =
    User.findOrCreateByGithubProfile(profile.githubId, profile.username)
      .map(user => Redirect(routes.Questions.index)
        .withSession(request.session + ("user.id" -> user.id.toString)))

  case class AccessToken(token: String)
  case class Profile(githubId: Long, username: String)

  val tokenParser: Reads[AccessToken] =
    (JsPath \ "access_token").read[String].map(AccessToken(_))

  val profileParser: Reads[Profile] = (
    (JsPath \ "id").read[Long] and
    (JsPath \ "login").read[String]
  )(Profile.apply _)

  private def profileRequest(authToken: String) =
    WS.url(UserDetailsUrl).withHeaders("Authorization" -> s"token $authToken").get()

  private def accessTokenRequest(code: String) =
    WS.url(OAuthAccessTokenUrl).withHeaders("Accept" -> "application/json")
      .post(Map("client_id" -> Seq(OAuthClientId),
        "client_secret" -> Seq(OAuthClientSecret),
        "code" -> Seq(code)))

  val OAuthClientId = readConfig("oauth.github.client_id")
  val OAuthClientSecret = readConfig("oauth.github.client_secret")
  val OAuthRedirectUrl =
    s"https://github.com/login/oauth/authorize?client_id=$OAuthClientId"
  val OAuthAccessTokenUrl = "https://github.com/login/oauth/access_token"
  val UserDetailsUrl = "https://api.github.com/user"

  private def readConfig(key: String): String =
    current.configuration.getString(key).getOrElse("")
}
