import onceler._
object Test extends App {
  def newString = new String("new String")
  assert(newString ne newString)
  assert(once(newString) ne once(newString))
  assert({ def o() = once(newString); o() eq o() })
}