package controllers.actions

import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import scala.concurrent.Future

class AuthenticatedRequest[A](val userId: Long, request: Request[A])
    extends WrappedRequest[A](request)

object AuthenticatedAction extends ActionBuilder[AuthenticatedRequest] {
  def invokeBlock[A](request: Request[A],
    action: (AuthenticatedRequest[A]) => Future[Result]) =
    getUserId(request) match {
      case Some(id) => action(new AuthenticatedRequest[A](id, request))
      case None => Future.successful(Results.Forbidden(views.html.errors.forbidden()))
    }

  def getUserId[A](request: Request[A]): Option[Long] =
    request.session.get("user.id").map(_.toLong)
}
