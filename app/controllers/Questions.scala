package controllers

import play.api.mvc.{Action, Controller}
import models.Question

object Questions extends Controller {
  def index = Action {
    Ok(views.html.questions.index(Question.all))
  }

  def show(id: Long) = Action {
    Question.find(id).map { question =>
      Ok(views.html.questions.show(question))
    }.getOrElse(NotFound)
  }
}
