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
//                .addOption(Option.builder("output")
//                        .option("m")
//                        .hasArgs()
//                        .argName("file")
//                        .desc("merge all input documents into <file>")
//                        .build())
                .addOption(Option.builder("b")
                        .option("b")
                        .desc("rearrange pages to create a booklet view")
                        .build())
                .addOption(Option.builder("B")
                        .option("B")
                        .desc("same as 'b' but for duplex printers")
                        .build())
                .addOption(Option.builder("quiet")
                        .option("q")
                        .desc("do not open booklet in the web-browser")
                        .build())
                .addOption(Option.builder()
                        .option("h")
                        .desc("print this message")
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

            if (cmd.hasOption('v')) {
                System.out.println(properties.getProperty("version"));
                return;
            }

            if (cmd.getArgs().length == 0) {
                help.run();
                System.exit(1);
            }

            if (!cmd.hasOption('b') && !cmd.hasOption('B')) {
                Actions.err("you must specify either the '-b' or '-B' option");
                System.exit(2);
            }

            if (cmd.hasOption('b') && cmd.hasOption('B')) {
                Actions.err("cannot use both '-b' and '-B' options together");
                System.exit(3);
            }

            Logger.getLogger("org.apache.pdfbox").setLevel(Level.OFF);
            System.exit(Actions.pipeline(cmd));
        } catch (ParseException ignore) {
            help.run();
            System.exit(4);
        }
//        var pdfMergerUtility = new PDFMergerUtility();
//        for (int i = 1; i < cmd.getArgList().size(); i++) {
//            var file = new File(cmd.getArgList().get(i));
//            filename = file.getName();
//            if (!file.exists()) {
//                System.err.printf("\u001b[91mError:\u001b[0m '%s' does not exist, skipping...\n", filename);
//                continue;
//            }
//
//            try (var doc = Loader.loadPDF(file)) {
//                pdfMergerUtility.appendDocument(merge, doc);
//            } catch (IOException ioe) {
//                System.err.printf("\u001b[91merror:\u001b[0m '%s' is not a PDF file, skipping...\n", filename);
//            }
//        }
    }
}