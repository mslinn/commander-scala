package com.github.acrisci.commander

private[commander] class Opt(var flags: String, var description: String, var default: Any = null, var required: Boolean = false, var fn: String => Any = identity) {
  var paramRequired: Boolean = flags.contains("<")
  var paramOptional: Boolean = flags.contains("[")
  private val flagsList = splitFlags(flags)
  private var short = ""
  private var long = ""
  var givenParam = false
  var present = false

  // FIXME the single backslash is probably an error
  if (flagsList.length > 1 && !flagsList(1).matches(raw"^[\[<].*")) {
    short = flagsList(0)
    long = flagsList(1)
  } else {
    long = flagsList(0)
  }

  var value: Any = _

  if (takesParam || default != null) {
    value = default
  } else {
    value = false
  }

  private def splitFlags(flags: String): Array[String] = "[ ,|]+".r.split(flags)

  def name(): String = long.replaceAll("--", "").replaceAll("no-", "")

  def is(arg: String): Boolean = arg == short || arg == long

  def takesParam(): Boolean = paramRequired || paramOptional
}
