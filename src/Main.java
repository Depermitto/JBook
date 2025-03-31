import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("\u001b[91mMissing required parameter: 'PDF file'\u001B[0m\n" +
					"Usage: pbook <PDF file>\n" +
					"Version: 1.0");
			System.exit(1);
		}

		Logger.getLogger("org.apache.pdfbox").setLevel(Level.OFF);

		var file = new File(args[0]);
		if (!file.exists()) {
			System.err.printf("\u001b[91mError:\u001b[0m '%s' does not exist\n", file.getName());
			System.exit(1);
		}

		try (var doc = Loader.loadPDF(file)) {
			var booklet = new PDDocument();
			var bookletIndices = duplex(doc.getNumberOfPages());
			var pages = doc.getPages();
			for (int i : bookletIndices) {
				booklet.addPage(pages.get(i - 1));
			}
			booklet.save("booklet.pdf");
			booklet.close();
		} catch (IOException ioe) {
			System.err.printf("\u001b[91mError:\u001b[0m '%s' is not a PDF file\n", file.getName());
			System.exit(1);
		}
	}

	public static List<Integer> duplex(int n) {
		var pages = new ArrayList<Integer>(n);
		for (int p = 0; p < n / 2; p++) {
			if ((p & 1) == 1) {
				pages.add(p + 1);
				pages.add(n - p);
			} else {
				pages.add(n - p);
				pages.add(p + 1);
			}
		}
		if ((n & 1) == 1) {
			pages.add((n + 1) / 2);
		}
		return pages;
	}
}
