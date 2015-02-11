package controllers

import controllers.actions.{AuthenticatedAction, AuthenticatedRequest}
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
    Question.find(id)
      .map(question => Ok(views.html.questions.show(question)))
      .getOrElse(NotFound(views.html.errors.notFound()))
  }

  def newForm = AuthenticatedAction { implicit request =>
    Ok(views.html.questions.newForm(questionForm))
  }

  def create = AuthenticatedAction { implicit request =>
    questionForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.questions.newForm(formWithErrors)),
      params => saveAndRedirect(params, request))
  }

  def saveAndRedirect(params: QuestionParams,
    request: AuthenticatedRequest[AnyContent]) =
    Question.create(params.title, params.body, request.userId)
      .map(question => Redirect(routes.Questions.show(question.id)))
      .getOrElse(InternalServerError(views.html.errors.serverError()))

  implicit def user(implicit request: RequestHeader): Option[User] =
    request.session.get("user.id").flatMap(id => User.find(id.toLong))

  val questionMapping = mapping(
    "title" -> nonEmptyText,
    "body" -> text
  )(QuestionParams.apply)(QuestionParams.unapply)

  val questionForm = Form(questionMapping)
}
