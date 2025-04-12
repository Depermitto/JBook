import org.apache.commons.cli.CommandLine;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Actions {
    public static void err(String msg) {
        System.err.printf("\u001b[91merror:\u001b[0m " + msg + '\n');
    }

    public static void warn(String msg) {
        System.err.printf("\u001b[33mwarning:\u001b[0m " + msg + '\n');
    }

    public static int pipeline(CommandLine cmd) {
        var args = cmd.getArgList();
        assert !args.isEmpty();

        var file = new File(args.get(0));
        var filename = file.getName();
        if (!file.exists()) {
            err(filename + " does not exist");
            System.exit(1);
        }
        try (PDDocument doc = Loader.loadPDF(file)) {
            if (cmd.hasOption('m')) {
                var pdfMergerUtility = new PDFMergerUtility();
                for (int n = 1; n < args.size(); n++) {
                    var fileN = new File(args.get(n));
                    var filenameN = fileN.getName();
                    if (!fileN.exists()) {
                        err(filenameN + " does not exist, skipping...");
                        continue;
                    }
                    try (PDDocument docN = Loader.loadPDF(file)) {
                        pdfMergerUtility.appendDocument(doc, docN);
                    } catch (IOException ioe) {
                        err(filenameN + " is not a PDF file, skipping...");
                    }
                }
            }

            var pages = doc.getPages();
            if ((doc.getNumberOfPages() & 1) == 1) doc.addPage(new PDPage(pages.get(0).getMediaBox()));

            List<Integer> indices = Collections.emptyList();
            if (cmd.hasOption('b')) indices = simplex(doc.getNumberOfPages());
            else if (cmd.hasOption('B')) indices = duplex(doc.getNumberOfPages());

            var booklet = new PDDocument();
            for (int i : indices) {
                booklet.addPage(pages.get(i - 1));
            }

            var ext = filename.lastIndexOf('.');
            var path = Files.createTempFile(ext == -1 ? filename : filename.substring(0, ext) + '.', ".tmp.pdf");
            booklet.save(path.toFile());
            booklet.close();

            if (cmd.hasOption('q')) {
                System.out.println("file available at " + path.toUri());
                return 0;
            }
            if (Desktop.isDesktopSupported()) {
                var desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(path.toUri());
                    return 0;
                }
            }
            try {
                var process = new ProcessBuilder("xdg-open", path.toAbsolutePath().toString()).inheritIO().start();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    warn("cannot open booklet, file available at " + path.toUri());
                }
            } catch (IOException | InterruptedException ignore) {
                warn("opening booklet interrupted, file available at " + path.toUri());
            }
        } catch (IOException ignore) {
            err(filename + " is not a PDF file");
            return 1;
        }
        return 0;
    }

    public static List<Integer> duplex(int n) {
        var pages = new ArrayList<Integer>(n);
        for (int i = 0; i < n / 2; i++) {
            if ((i & 1) == 0) {
                pages.add(n - i);
                pages.add(i + 1);
            } else {
                pages.add(i + 1);
                pages.add(n - i);
            }
        }
        if ((n & 1) == 1) {
            pages.add((n + 1) / 2);
        }
        return pages;
    }

    @Test
    void testDuplex() {
        assert duplex(4).equals(List.of(4, 1, 2, 3));
        assert duplex(8).equals(List.of(8, 1, 2, 7, 6, 3, 4, 5));
        assert duplex(15).equals(List.of(15, 1, 2, 14, 13, 3, 4, 12, 11, 5, 6, 10, 9, 7, 8));
        assert duplex(17).equals(List.of(17, 1, 2, 16, 15, 3, 4, 14, 13, 5, 6, 12, 11, 7, 8, 10, 9));
    }

    public static List<Integer> simplex(int n) {
        var pages = duplex(n);
        for (int i = 1; i < n; i += 4) {
            if (i + 1 < n) {
                Collections.swap(pages, i, i + 1);
            }
        }
        return pages;
    }

    @Test
    void testSimplex() {
        assert simplex(4).equals(List.of(4, 2, 1, 3));
        assert simplex(8).equals(List.of(8, 2, 1, 7, 6, 4, 3, 5));
        assert simplex(15).equals(List.of(15, 2, 1, 14, 13, 4, 3, 12, 11, 6, 5, 10, 9, 8, 7));
        assert simplex(17).equals(List.of(17, 2, 1, 16, 15, 4, 3, 14, 13, 6, 5, 12, 11, 8, 7, 10, 9));
    }
}