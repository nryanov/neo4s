package com.nryanov.neo4s

sealed trait Nullability

case object NonNull extends Nullability

case object Nullable extends Nullability
