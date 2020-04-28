import shapeless.Generic.Aux
import shapeless._
import shapeless.labelled.FieldType

object T {
  def main(args: Array[String]): Unit = {
    final case class Foo(a: Int, b: String, c: Long)

    val generic: Aux[Foo, Int :: String :: Long :: HNil] = Generic[Foo]
    val labelledGeneric: LabelledGeneric.Aux[Foo, FieldType[Int with labelled.KeyTag[Symbol with tag.Tagged[{
        type a
      }
    ], Int], Int] :: FieldType[String with labelled.KeyTag[Symbol with tag.Tagged[{
        type b
      }
    ], String], String] :: FieldType[Long with labelled.KeyTag[Symbol with tag.Tagged[{
        type c
      }
    ], Long], Long] :: HNil] = LabelledGeneric[Foo]
  }
}
