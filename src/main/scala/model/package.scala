package object model {
  trait AppError

  case object NotFoundError                      extends AppError
  case class InvalidTransitionError(msg: String) extends AppError
}
