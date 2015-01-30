package controllers.actions

import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import scala.concurrent.Future

object AuthenticatedAction extends ActionBuilder[Request] {
  def invokeBlock[A](request: Request[A], action: (Request[A]) => Future[Result]) =
    if (isAuthenticated(request)) action(request)
    else Future.successful(Results.Forbidden(views.html.errors.forbidden()))

  def isAuthenticated[A](request: Request[A]): Boolean =
    !request.session.get("user.id").isEmpty
}
