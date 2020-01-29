package onceler

object once {
  def apply[T](factory: => T): T = ???
}
