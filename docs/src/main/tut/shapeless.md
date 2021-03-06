---
layout: tutorial
title: "Generic module"
section: tutorial
sort_order: 21
---
While kantan.csv goes out of its way to provide [default instances](default_instances.html) for as many types as it can,
some are made problematic by my strict rule against runtime reflection. Fortunately, [shapeless](http://shapeless.io)
provides _compile time_ reflection, which makes it possible for the `generic` module to automatically derive instances
for more common types and patterns.

The `generic` module can be used by adding the following dependency to your `build.sbt`:

```scala
libraryDependencies += "com.nrinaudo" %% "kantan.csv-generic" % "0.1.16"
```

If you're using Scala 2.10.x, you should also add the macro paradise plugin to your build:

```scala
libraryDependencies += compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
```

Let's first declare the imports we'll need in the rest of this tutorial:

```tut:silent
import kantan.csv.ops._     // Provides CSV specific syntax.
import kantan.csv.generic._ // Provides automatic instance derivation.
```

The rest of this post will be a simple list of supported types.

## `CellEncoder`s and `CellDecoder`s

### Case classes of arity 1

All case classes of arity 1 have [`CellDecoder`] and [`CellEncoder`] instances, provided the type of their single field
also does.

Let's declare a (fairly useless) case class (we'll be making a more useful one in the next section):

```tut:silent
case class Wrapper[A](a: A)
```

We can directly encode from and decode to instances of `Wrapper`:

```tut
val decoded = "1, 2, 3\n4, 5, 6".unsafeReadCsv[List, List[Wrapper[Int]]](',', false)

decoded.asCsv(',')
```

### Sum types

We can also get free [`CellDecoder`] and [`CellEncoder`] instances for sum types where all alternatives have a
[`CellDecoder`] and [`CellEncoder`]. For example:

```tut:silent
sealed abstract class Or[+A, +B]
case class Left[A](value: A) extends Or[A, Nothing]
case class Right[B](value: B) extends Or[Nothing, B]
```

`Left` is a unary case class and will have a [`CellDecoder`] if its type parameter has one, and the same goes for
`Right`. This allows us to write:

```tut
val decoded = "1,true\nfalse,2".unsafeReadCsv[List, List[Int Or Boolean]](',', false)

decoded.asCsv(',')
```

## Rows

### Case classes

All case classes have [`RowEncoder`] and [`RowDecoder`] instances, provided all their fields also do.

Take, for example, a custom [`Tuple2`] implementation (using an actual [`Tuple2`] might not be very convincing, as
it's supported by kantan.csv without needing the `generic` module):

```tut:silent
case class CustomTuple2[A, B](a: A, b: B)
```

We can encode from and decode to that type for free:

```tut
val decoded = "1,\n2,false".unsafeReadCsv[List, CustomTuple2[Int, Option[Boolean]]](',', false)

decoded.asCsv(',')
```

It is *very* important to realise that while this is a pretty nice feature, it's also a very limited one. The only
time where you can get your case class codecs derived automatically is when the case class' fields and the CSV columns
are in exactly the same order. Any other scenario and you need to use old fashioned
[encoders](arbitrary_types_as_rows.html) and [decoders](rows_as_arbitrary_types.html).

### Sum types

As with cells, sum types have [`RowEncoder`] and [`RowDecoder`] instances provided their all their alternatives also do.

In the following example:

* `(Int, Boolean)` has both, since it's a [`Tuple2`] of primitive types.
* `CustomTuple2[String, Option[Boolean]]` has both, since it's a case class where all fields also do.

```tut
"1,true\nfoobar,".unsafeReadCsv[List, (Int, Boolean) Or CustomTuple2[String, Option[Boolean]]](',', false)
```

[`RowDecoder`]:{{ site.baseurl }}/api/kantan/csv/RowDecoder$.html
[`RowEncoder`]:{{ site.baseurl }}/api/kantan/csv/package$$RowEncoder.html
[`CellCodec`]:{{ site.baseurl }}/api/kantan/csv/package$$CellCodec.html
[`CellDecoder`]:{{ site.baseurl }}/api/kantan/csv/CellDecoder$.html
[`CellEncoder`]:{{ site.baseurl }}/api/kantan/csv/package$$CellEncoder.html
[`Tuple2`]:http://www.scala-lang.org/api/current/scala/Tuple2.html
