import cats.syntax.all.*
import cats.effect.*
import com.monovore.decline.*
import com.monovore.decline.effect.*

object Main
    extends CommandIOApp(
      name = "chess-gen",
      header = "Generate positions for a given variant",
      version = "0.0.1"
    ):

  override def main: Opts[IO[ExitCode]] = CLI.parse
    .map(IO.println(_).as(ExitCode.Success))
