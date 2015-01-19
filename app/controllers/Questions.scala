package controllers

import play.api.mvc.{Action, Controller}
import models.Question

object Questions extends Controller {
  def index = Action {
    Ok(views.html.questions.index(Question.all))
  }
}
