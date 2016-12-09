package com.github.acrisci.commander

import java.io.File
import com.github.acrisci.commander.errors.{InvalidCommandException, ProgramParseException}
import org.scalatest.{FlatSpec, Matchers}

class TestProgram extends FlatSpec with Matchers{
  def testProgram: Program = {
    new Program(exitOnError=false)
      .version("1.0.0")
      .option("-p, --peppers", "Add peppers")
      .option("-o, --onions", "Add onions")
      .option("-a, --anchovies", "Add anchovies")
      .option("-b, --bbq-sauce <type>", "Add bbq sauce")
      .option("-c, --cheese [type]", "Add cheese", default="pepper jack")
      .option("-l, --olives [type]", "Add olives")
      .option("-L, --lettuce [type]", "Add lettuce", default="iceberg")
      .option("-P, --pickles [type]", "Add pickles")
      .option("-t, --tomatoes <type>", "Add tomatoes")
      .option("-n, --num [num]", "Number of pizzas", default=1, fn=_.toInt)
  }

  "Program" should "throw parse errors for invalid options" in {
    withClue("coercion to int should throw an exception when invalid number is given") {
      intercept[NumberFormatException] {
        // default program behavior should exit instead of throw the exception.
        // it throws the exception because exitOnError is false
        testProgram.parse(Array("-n", "invalid"))
      }
    }

    withClue("accessing a program option value that does not exist should error") {
      intercept[RuntimeException] {
        val program = testProgram.parse(Array())
        program.notAnOptionValue
      }
    }

    withClue("giving argument with missing required param should error") {
      intercept[ProgramParseException] {
        testProgram.parse(Array("-t"))
      }
    }

    withClue("an unknown option should throw an error") {
      intercept[ProgramParseException] {
        testProgram.parse(Array("--uknown-option"))
      }
    }

    withClue("a missing required option should throw an error") {
      intercept[ProgramParseException] {
        new Program(exitOnError=false)
          .option("-r, --required-option", "A required option", required=true)
          .parse(Array())
      }
    }

    withClue("a program without a version given the version option should throw an error") {
      intercept[ProgramParseException] {
        new Program(exitOnError=false).parse(Array("--version"))
      }
    }
  }

  "Program" should "throw command errors for invalid commands" in {
    withClue("A command with a given class that has no main method should throw an error") {
      intercept[InvalidCommandException] {
        new Program().command(classOf[CommandThatHasNoMain]).parse(Array())
      }
    }

    withClue("A command given a class with an invalid main method should throw an error") {
      intercept[InvalidCommandException] {
        new Program().command(classOf[CommandWithInvalidMain]).parse(Array())
      }
    }
  }

  "Program" should "parse arguments correctly" in {
    val fakeArgs = Array("-po", "unknown1", "--bbq-sauce=sweet", "--cheese", "cheddar", "-l", "black", "unknown2", "unknown3", "-n", "10")
    val program = testProgram.parse(fakeArgs)

    assertResult("1.0.0", "version should be set") { program.version }

    assertResult(true, "peppers was given in a combined short opt") { program.peppers }

    assertResult("sweet", "bbq sauce was given as a long opt with equals sign") { program.bbqSauce }

    assertResult("cheddar", "cheese was given as a long opt") { program.cheese }

    assertResult(true, "onions was given in a combined short opt") { program.onions }

    assertResult(false, "anchovies has no param and was not present") { program.anchovies }

    assertResult("black", "olives was given a param as a short opt") { program.olives }

    assertResult("iceberg", "lettuce was not given, but has a default") { program.lettuce }

    assertResult(null, "pickles was not given and has no default") { program.pickles }

    assertResult("java.lang.Integer", "num was coerced to an int") { program.num.getClass.getName }
    assertResult(10, "num should be parsed as an int") { program.num }

    assertResult(15, "TypeTags should work for easy casting") { program.num[Int] + 5 }

    assertResult(List("unknown1", "unknown2", "unknown3"),
      "args should contain the unknown args") { program.args }
  }

  "Program" should "properly create the help string" in {
    var program = new Program()
      .version("1.0.0")
      .description("A test program")
      .epilogue("This is the epilogue")
      .option("-p, --peppers", "Add peppers")

    val helpString = """
      |Usage: TestProgram [options]
      |
      |  A test program
      |
      |  Options:
      |
      |    -h, --help     output usage information
      |    -V, --version  output the version number
      |    -p, --peppers  Add peppers
      |
      |  This is the epilogue""".stripMargin

    assertResult(helpString.trim, "program should have a useful help string") { program.helpInformation.trim }

    val usage = "java -jar my-program.jar [options]"

    program = new Program()
      .version("1.0.0")
      .usage(usage)
      .parse(Array())

    assert(program.helpInformation.trim.startsWith(s"Usage: $usage"),
      "the program should have the overridden usage string")

    program = new Program().parse(Array())

    assert(!program.helpInformation.contains("--version"),
      "A program with no version should not have a --version option present in the help string")
  }

  "Program" should "properly execute commands when given" in {
    val helpFile: File = {
      val file = File.createTempFile("commander-scala-test/command-one-flag", "")
      file.delete()
      file
    }

    def reset() = try { helpFile.delete() } catch { case _: Exception => }

    def programWithCommands = try {
      new Program(exitOnError=false, exitOnCommand=false)
        .version("1.0.0")
        .command(classOf[CommandThatDoesNothing], "do-nothing", "it does nothing at all")
        .description("A program with commands.")
    } catch {
      case e: Exception =>
        sys.error(e.getMessage)
    }
    reset()

    val helpString1 = """
      |  Usage: TestProgram [options] [command]
      |
      |  A program with commands.
      |
      |  Commands:
      |
      |    command-that-writes-a-file [path]  it creates a file when it runs
      |    do-nothing                         it does nothing at all
      |    help [cmd]                         Display help for [cmd]
      |
      |  Options:
      |
      |    -h, --help     output usage information
      |    -V, --version  output the version number
      |""".stripMargin

    try {
      val program = programWithCommands
        .command(classOf[CommandThatWritesAFile], "[path]", "it creates a file when it runs")
        .parse(Array("command-that-writes-a-file", helpFile.getPath))

      cleanUp(helpString1) shouldBe cleanUp(program.helpInformation)
    } catch { case e: Exception =>
      System.err.println(e.getMessage)
      System.exit(-1)
    }
    assert(helpFile.exists, "the command should run when the hyphen-case name of the class is given") // fails
    reset()

    val helpString2 = """
      |  Usage: TestProgram [options] [command]
      |
      |  A program with commands.
      |
      |  Commands:
      |
      |    write [path]  it has the name overridden
      |    do-nothing    it does nothing at all
      |    help [cmd]    Display help for [cmd]
      |
      |  Options:
      |
      |    -h, --help     output usage information
      |    -V, --version  output the version number
      |""".stripMargin

    val program2 = programWithCommands
      .command(classOf[CommandThatWritesAFile], "write [path]", "it has the name overridden")
      .parse(Array("write", helpFile.getPath))

    assert(helpFile.exists, "the command should run when the overridden name is given")
    reset()

    assertResult(helpString2.trim, "the program should format help string info correctly for commands") { program2.helpInformation.trim }
    reset()
  }

  "Commands" should "have implicit help" in {
    def file: File = new CommandThatWritesOnHelp().file
    def reset() = try { file.delete() } catch { case _: Exception => }
    reset()

    new Program(exitOnCommand=false)
      .command(classOf[CommandThatWritesOnHelp], "command-with-help")
      .parse(Array("help", "command-with-help"))

    assert(file.exists, "File does not exist")
    reset()
  }

  def cleanUp(string: String): String = string.trim.replaceAll("\\s+", " ")
}

class CommandThatDoesNothing {
  def main(args: Array[String]): Unit = {}
}

class CommandThatHasNoMain

class CommandThatWritesAFile {
  def main(args: Array[String]): Unit = {
    val file =  new File(args(0))
    file.setLastModified(new java.util.Date().getTime)
    ()
  }
}

class CommandThatWritesOnHelp {
  def file(): File = File.createTempFile("implicit-help-flag", "")

  def main(args: Array[String]): Unit = {
    file()
  }
}

class CommandWithInvalidMain {
  def main(invalidArg: String): Unit = {
    // main should have arguments of type Array[String]
  }
}
