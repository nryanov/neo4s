package neo4s.utils

import scala.collection.JavaConverters._

private[neo4s] object CollectionCompat {
  implicit def javaListToScalaList[A](javaList: java.util.List[A]): List[A] = javaList.asScala.toList

  implicit def javaIterableToScalaList[A](javaList: java.lang.Iterable[A]): List[A] = javaList.asScala.toList

  implicit def javaMapToScalaMap[A](javaMap: java.util.Map[String, A]): Map[String, A] = javaMap.asScala.toMap

  implicit def scalaMapToJavaMap[A](scalaMap: Map[String, A]): java.util.Map[String, A] = scalaMap.asJava

  implicit def scalaListToJavaList[A](scalaList: List[A]): java.util.List[A] = scalaList.asJava

  def mapValues[A, B](map: Map[String, A], f: A => B): Map[String, B] = map.mapValues(f).toMap

  def forceToJava[A](list: List[A]): java.util.List[A] = list.asJava
}
