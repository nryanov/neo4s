import neo4s.core.StringInterpolation
import neo4s.core.syntax.ExecutableIOSyntax
import neo4s.utils.MetaInstances

package object neo4s extends Aliases {
  object implicits extends MetaInstances with StringInterpolation with ExecutableIOSyntax
}
