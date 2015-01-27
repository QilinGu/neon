package controllers

import play.api.mvc.{Action, Controller, RequestHeader}
import models.{Question, User}

object Questions extends Controller {
  def index = Action {
    implicit request =>
      Ok(views.html.questions.index(Question.all))
  }

  def show(id: Long) = Action {
    implicit request => {
      Question.find(id) match {
        case Some(question) => Ok(views.html.questions.show(question))
        case None => NotFound
      }
    }
  }

  implicit def user(implicit request: RequestHeader): Option[User] = {
    request.session.get("user.id") match {
      case Some(id) => User.find(id.toLong)
      case None => None
    }
  }
}
