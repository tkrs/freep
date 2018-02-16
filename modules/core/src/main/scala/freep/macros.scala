package freep

import scala.annotation.StaticAnnotation
import scala.meta._

import scala.collection.immutable.Seq

object macros {
  class gen extends StaticAnnotation {
    inline def apply(defn: Any): Any = meta {
      def lower(s: String): String = s.splitAt(1) match {
        case (a, b) => a.toLowerCase + b
      }
      defn match {
        case x @ q"..$_ trait $tname[..$tparams] extends $template" =>
          val op = tname.value + "Op"
          val ops = tname.value + "Ops"
          val opName = Type.Name(op)
          val opsName = Type.Name(ops)
          val opClasses = template.stats.getOrElse(Seq.empty).map {
            case q"..$mods def $ename[..$tparams](...$paramss): Free[..$ttparams]" =>
              val t = ttparams(1)
              val c = ename.value.head.toString.toUpperCase + ename.value.tail
              q"final case class ${Type.Name(c)}(...$paramss) extends ${Ctor.Ref.Name(op)}[$t]"
          }
          // println(opClasses)
          val opsMethods = template.stats.getOrElse(Seq.empty).zip(opClasses).map {
            case (q"..$_ def $ename[..$tparams](...$paramss): Free[..$rtparams]", q"..$_ case class $tname(...$_) extends $_[$_]") =>
              val xs = paramss.map(_.map(p => Term.Name(p.name.value)))
              q"""def $ename[..$tparams](...$paramss): Free[F, ${rtparams(1)}] =
                    Free.inject[$opName, F](${Ctor.Ref.Name(tname.value)}.apply(...$xs))
              """
          }
          // println(opsMethods)
          val opTName = Term.Name(op)
          val term = q"""
            import cats.InjectK
            import cats.free.Free
            object $opTName {
              ..$opClasses
            }
            object ${Term.Name(tname.value)} {
              private final class $opsName[F[_]](implicit inj: InjectK[$opName, F]) extends ${Ctor.Ref.Name(tname.value)}[F] {
                import $opTName._
                ..$opsMethods
              }

              implicit def ${Term.Name(lower(tname.value))}[F[_]](implicit inj: InjectK[$opName, F]): $tname[F] =
                new ${Ctor.Ref.Name(opsName.value)}()
            }
           """

          println(term)
          Term.Block(x +: term.stats)
      }
    }
  }
}
