package examples

import cats.~>
import cats.data.EitherK
import cats.free.Free
import cats.instances.option._
import freep.macros.gen

object metaFree {
  type TestOp[A] = EitherK[TestAOp, TestBOp, A]
  @gen
  trait TestA[F[_]] {
    def plus(a: Int, b: Int): Free[TestAOp, Int]
    def minus(a: Int, b: Int): Free[TestAOp, Int]
    def zero(): Free[TestAOp, Int]
  }
  @gen
  trait TestB[F[_]] {
    def multi(a: Int, b: Int): Free[TestBOp, Int]
    def divide(a: Int, b: Int): Free[TestBOp, Int]
  }
}

object Main extends App {
  import metaFree._, TestAOp._, TestBOp._
  def program(implicit A: TestAOps[TestOp], B: TestBOps[TestOp]): Free[TestOp, Int] =
    for {
      w <- A.plus(10, 20)
      x <- A.minus(10, 9)
      y <- B.multi(3, 3)
      z <- B.divide(9, 3)
    } yield w * x * y * z
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
