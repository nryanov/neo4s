package neo4s.core.syntax

import neo4s.core.ExecutableOp.ExecutableIO

trait ExecutableIOSyntax {
  implicit def toExecutableIOOps[A](executableIO: ExecutableIO[A]): ExecutableIOOps[A] = new ExecutableIOOps[A](executableIO)
}
