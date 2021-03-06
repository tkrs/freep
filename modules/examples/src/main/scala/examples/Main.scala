package examples

import cats.~>
import cats.data.EitherK
import cats.free.Free
import cats.instances.option._
import freep.macros.genFree

object metaFree {

  @genFree
  trait TestA[F[_]] {
    def plus(a: Int, b: Int): Free[F, Int]
    def minus(a: Int, b: Int): Free[F, Int]
    def zero(): Free[F, Int]
  }

  @genFree
  trait TestB[F[_]] {
    def multi(a: Int, b: Int): Free[F, Int]
    def divide(a: Int, b: Int): Free[F, Int]
  }
}

object Main extends App {
  import metaFree._

  type TestOp[A] = EitherK[TestAOp, TestBOp, A]

  def program(implicit A: TestA[TestOp], B: TestB[TestOp]): Free[TestOp, Int] =
    for {
      w <- A.plus(10, 20)
      x <- A.minus(10, 9)
      y <- B.multi(3, 3)
      z <- B.divide(9, 3)
    } yield w * x * y * z

  import TestAOp._, TestBOp._

  val interpreterA: TestAOp ~> Option = λ[TestAOp ~> Option] {
    case Plus(a, b)  => Some(a + b)
    case Minus(a, b) => Some(a - b)
    case Zero()      => Some(0)
  }
  val interpreterB: TestBOp ~> Option = λ[TestBOp ~> Option] {
    case Multi(a, b)  => Some(a * b)
    case Divide(a, b) => Some(a / b)
  }
  val interpreter: TestOp ~> Option = interpreterA or interpreterB
  println(program.foldMap(interpreter))
}
