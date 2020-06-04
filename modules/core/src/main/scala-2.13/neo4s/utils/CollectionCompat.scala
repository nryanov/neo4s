package neo4s.utils

import org.neo4j.driver.Value

import scala.jdk.CollectionConverters._

private[neo4s] object CollectionCompat {
  implicit def javaListToScalaList[A](javaList: java.util.List[A]): List[A] = javaList.asScala.toList

  implicit def javaMapToScalaMap[A](javaMap: java.util.Map[String, A]): Map[String, A] = javaMap.asScala.toMap

  implicit def scalaMapToJavaMap(scalaMap: Map[String, Value]): java.util.Map[String, Value] = scalaMap.asJava

  def mapValues[A, B](map: Map[String, A], f: A => B): Map[String, B] = map.view.mapValues(f).toMap
}
