package controllers

import controllers.actions.AuthenticatedAction
import models.{Question, User}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

case class QuestionParams(title: String, body: String)

object Questions extends Controller {
  def index = Action { implicit request =>
    Ok(views.html.questions.index(Question.all))
  }

  def show(id: Long) = Action { implicit request =>
    Question.find(id) match {
      case Some(question) => Ok(views.html.questions.show(question))
      case None => NotFound(views.html.errors.notFound())
    }
  }

  def newForm = AuthenticatedAction { implicit request =>
    Ok(views.html.questions.newForm(questionForm))
  }

  def create = AuthenticatedAction { implicit request =>
    questionForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.questions.newForm(formWithErrors)),
      params => saveAndRedirect(params))
  }

  def saveAndRedirect(params: QuestionParams) =
    Question.create(params) match {
      case Some(question) => Redirect(routes.Questions.show(question.id))
      case None => InternalServerError(views.html.errors.serverError())
    }

  implicit def user(implicit request: RequestHeader): Option[User] = {
    request.session.get("user.id") match {
      case Some(id) => User.find(id.toLong)
      case None => None
    }
  }

  val questionMapping = mapping(
    "title" -> nonEmptyText,
    "body" -> text
  )(QuestionParams.apply)(QuestionParams.unapply)

  val questionForm = Form(questionMapping)
}
