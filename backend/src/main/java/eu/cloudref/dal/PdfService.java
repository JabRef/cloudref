package eu.cloudref.dal;

import eu.cloudref.Configuration;
import org.jbibtex.BibTeXEntry;

import java.io.*;

/**
 * Service to save and get PDF files of a reference.
 */
public class PdfService {

    /**
     * Get the directory of the PDF file of a reference.
     *
     * @param bibtexkey the BibTeX-key of the reference.
     * @return String - directory of the PDF file of a reference.
     */
    public static String getPdfFileDirectory(String bibtexkey) {
        return Configuration.getCloudRefDirectory() + "pdfDirectory/" + bibtexkey + "/";
    }

    /**
     * Get the absolute PDF file path including the file name and ending of a reference.
     *
     * @param bibtexkey the BibTeX-key of the reference.
     * @return String - the absolute PDF file path.
     */
    private static String getPdfFilePath(String bibtexkey) {
        return getPdfFileDirectory(bibtexkey) + "main.pdf";
    }

    /**
     * Check if a reference has a PDF file.
     *
     * @param bibtexkey the BibTeX-key of a reference.
     * @return true if a PDF file exists for reference, false otherwise.
     */
    public static boolean hasPdfFile(String bibtexkey) {
        File f = new File(getPdfFilePath(bibtexkey));
        if (f.exists() && !f.isDirectory()) {
            return true;
        }
        return false;
    }

    /**
     * Save a PDF file for a reference.
     *
     * @param bibtexkey the BibTeX-key of a reference.
     * @param uploadedInputStream the PDF file.
     * @return true if successfully saved, false otherwise.
     */
    public static boolean savePdfFile(String bibtexkey, InputStream uploadedInputStream) {

        if (bibtexkey != null && !bibtexkey.isEmpty() && uploadedInputStream != null) {

            // check if reference with bibtexkey exists
            BibTeXEntry reference = ReferencesService.getReference(bibtexkey, true);
            if (reference == null) {
                return false;
            }

            // save file
            File file = new File(getPdfFilePath(bibtexkey));
            try {
                writeFileToDisc(uploadedInputStream, file);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Get the PDF file of a reference.
     *
     * @param bibtexkey the BibTeX-key of the reference.
     * @return File - the PDF file of the reference.
     */
    public static File getPdfFile(String bibtexkey) {
        if (!hasPdfFile(bibtexkey)) {
            return null;
        }
        return new File(getPdfFilePath(bibtexkey));
    }

    /**
     * Write a file to disk.
     *
     * @param uploadedInputStream the file content.
     * @param file the file location.
     * @throws IOException
     */
    public static void writeFileToDisc(InputStream uploadedInputStream, File file) throws IOException {
        // create directories of they are missing
        file.getParentFile().mkdirs();

        // write file to disc
        OutputStream oos = null;
        try {
            oos = new FileOutputStream(file);
            byte[] buf = new byte[8192];
            int c = 0;

            while ((c = uploadedInputStream.read(buf, 0, buf.length)) > 0) {
                oos.write(buf, 0, c);
                oos.flush();
            }
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (uploadedInputStream != null) {
                    uploadedInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
