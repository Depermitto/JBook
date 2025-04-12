import org.apache.commons.cli.*;

import java.io.IOException;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        final Properties properties = new Properties();
        try {
            properties.load(Actions.class.getClassLoader().getResourceAsStream(".properties"));
        } catch (IOException e) {
            throw new RuntimeException("Unreachable");
        }

        var options = new Options()
                .addOption(Option.builder("b")
                        .option("b")
                        .desc("rearrange pages to create a booklet view")
                        .build())
                .addOption(Option.builder("B")
                        .option("B")
                        .desc("same as '-b' but for duplex printers")
                        .build())
                .addOption(Option.builder("merge")
                        .option("m")
                        .desc("merge all input files into a single PDF document before rearranging pages")
                        .build())
                .addOption(Option.builder()
                        .option("h")
                        .desc("print this message")
                        .build())
                .addOption(Option.builder()
                        .option("H")
                        .desc("instructions on how to use print a JBook booklet")
                        .build())
                .addOption(Option.builder("quiet")
                        .option("q")
                        .desc("do not open booklet in the web-browser")
                        .build())
                .addOption(Option.builder("version")
                        .option("v")
                        .desc("print program version")
                        .build());

        Runnable help = () ->
                new HelpFormatter().printHelp(properties.getProperty("name") + " <files...>", options);

        try {
            CommandLine cmd = new DefaultParser().parse(options, args);
            if (cmd.hasOption('h')) {
                help.run();
                return;
            }

            if (cmd.hasOption('H')) {
                System.out.println("""
                        JBook generates a booklet-style view for any input PDF document. \
                        By default, processed files are saved in your system's temporary directory. \
                        Unless the '-q' option is passed, the booklet will automatically open in your default PDF viewer.
                        
                        how to print:
                        0. run JBook on a PDF (pass '-b' if uncertain)
                        1. open the print dialog
                        2. select '2 pages per sheet'
                        3. print odd pages
                        4. flip
                        5. print even pages
                        """);
                return;
            }

            if (cmd.hasOption('v')) {
                System.out.println(properties.getProperty("version"));
                return;
            }

            if (cmd.getArgs().length == 0) {
                help.run();
                System.exit(1);
            }

            if (cmd.getArgs().length > 1 && !cmd.hasOption('m')) {
                Actions.err("multiple input files unsupported without passing the '-m' option");
                System.exit(2);
            }

            if (!cmd.hasOption('b') && !cmd.hasOption('B')) {
                Actions.err("you must specify either the '-b' or '-B' option, pass '-b' if uncertain");
                System.exit(3);
            }

            if (cmd.hasOption('b') && cmd.hasOption('B')) {
                Actions.err("cannot use both '-b' and '-B' options together");
                System.exit(4);
            }

            Logger.getLogger("org.apache.pdfbox").setLevel(Level.OFF);
            System.exit(Actions.pipeline(cmd));
        } catch (ParseException ignore) {
            help.run();
            System.exit(5);
        }
    }
}