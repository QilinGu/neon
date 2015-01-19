package models

case class Question(id: Long, title: String, body: String)

object Question {
  var questions = Set(
    Question(1, "What is the meaning of life?", "I've been thinking about it all day and I can't figure out."),
    Question(2, "What should I eat today?", "I'm hungry."))

  def all = questions.toList
}
