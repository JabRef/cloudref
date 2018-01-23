package eu.cloudref.dal;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.cloudref.Configuration;
import eu.cloudref.db.User;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXFormatter;
import org.jbibtex.BibTeXParser;
import org.jbibtex.BibTeXString;
import org.jbibtex.CrossReferenceValue;
import org.jbibtex.DigitStringValue;
import org.jbibtex.Key;
import org.jbibtex.ParseException;
import org.jbibtex.StringValue;
import org.jbibtex.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read and write .bib files from/to the file system.
 */
public class BibService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BibService.class);

    private static String getBibDirectory() {
        return GitService.getRepositoryDirectory();
    }

    public static boolean bibtexkeyExists(String key) {
        File f = new File(getBibDirectory() + key + ".bib");
        if (f.exists()) {
            return true;
        } else {
            return false;
        }
    }

    private static List<BibTeXEntry> loadBibs(String bibtexkey, boolean modified) {
        List<BibTeXEntry> references = new ArrayList<>();
        List<File> files = new ArrayList<>();

        File directory = new File(getBibDirectory());
        directory.mkdirs();
        if (bibtexkey == null) {
            // get all bib files of directory
            files.addAll(Arrays.asList(directory.listFiles()));
        } else {
            // get bib file for bibtexkey
            files.add(new File(getBibDirectory() + bibtexkey + ".bib"));
        }

        // get references
        if (!files.isEmpty()) {
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        BibTeXDatabase database = parseBibTeX(file);

                        Collection<BibTeXEntry> entries = database.getEntries().values();
                        for (BibTeXEntry entry : entries) {
                            if (modified) {
                                // set pdf field to true if pdf exists for key
                                String key = entry.getKey().getValue();
                                Value v = new DigitStringValue(String.valueOf(PdfService.hasPdfFile(key)));
                                entry.addField(new Key("Pdf"), v);

                                // transform crossref to crossrefString
                                Value crossref = entry.getField(BibTeXEntry.KEY_CROSSREF);
                                if (crossref != null) {
                                    entry.addField(new Key("crossrefString"), new StringValue(crossref.toUserString(), StringValue.Style.BRACED));
                                    entry.removeField(BibTeXEntry.KEY_CROSSREF);
                                }
                            }
                            references.add(entry);
                        }
                    } catch (IOException e) {
                        LOGGER.error("Could not load file", e);
                    }
                }
            }
        }
        return references;
    }

    /**
     * Load all saved BibTeX entries.
     *
     * @return List<BibTeXEntry> list with all saved BibTeX entries.
     */
    public static List<BibTeXEntry> loadBibs() {
        return loadBibs(null, true);
    }

    /**
     * Get the reference of the given bibtexkey.
     *
     * @param bibtexkey the key of the reference.
     * @param modified  indicates if the result should have additional information, e.g. has reference a PDF file.
     * @return BibTeXEntry
     */
    public static BibTeXEntry loadBib(String bibtexkey, boolean modified) {
        List<BibTeXEntry> entries = loadBibs(bibtexkey, modified);
        if (entries.isEmpty()) {
            // no entry with that bibtexkey
            return null;
        } else {
            return entries.get(0);
        }
    }

    /**
     * Save given BibTeX entry on disk.
     *
     * @param newReference BibTeX entry which should be saved.
     * @return true if successfully saved, otherwise false.
     */
    public static BibTeXEntry saveBib(BibTeXEntry newReference) {

        if (newReference == null) {
            return null;
        }
        // remove all empty fields of reference
        BibTeXEntry newReferenceWithoutEmptyFields = removeEmptyFields(newReference);

        // remove pdf field
        newReferenceWithoutEmptyFields.removeField(new Key("Pdf"));
        Value crossrefString = newReferenceWithoutEmptyFields.getField(new Key("crossrefString"));

        // transform crossrefString to crossref
        if (crossrefString != null) {
            CrossReferenceValue crossref = new CrossReferenceValue(crossrefString, null);
            newReferenceWithoutEmptyFields.removeField(new Key("crossrefString"));
            newReferenceWithoutEmptyFields.addField(BibTeXEntry.KEY_CROSSREF, crossref);
        }

        // create or update reference
        BibTeXDatabase database = new BibTeXDatabase();
        database.addObject(newReferenceWithoutEmptyFields);
        File file = new File(getBibDirectory() + newReferenceWithoutEmptyFields.getKey().getValue() + ".bib");
        try {
            formatBibTeX(database, file);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return newReferenceWithoutEmptyFields;
    }


    /**
     * Remove all empty fields of a reference.
     *
     * @param entry the reference.
     * @return the reference without empty fields.
     */
    private static BibTeXEntry removeEmptyFields(BibTeXEntry entry) {
        List<Key> removeKeys = new ArrayList<>();
        if (entry != null) {
            if (entry.getFields() != null && !entry.getFields().isEmpty()) {
                if (entry.getFields().entrySet() != null) {
                    for (Map.Entry<Key, Value> e : entry.getFields().entrySet()) {
                        if (e.getValue() == null || e.getValue().toUserString() == null ||
                                e.getValue().toUserString().equals("")) {
                            removeKeys.add(e.getKey());
                        }
                    }
                }
            }
            for (Key s : removeKeys) {
                entry.removeField(s);
            }
        }
        return entry;
    }

    /**
     * Save a input stream which contains multiple BibTeX entries.
     *
     * @param uploadedInputStream InputStream which contains BibTeX entries.
     * @return true if successfully saved, otherwise false.
     */
    public static boolean saveReferences(InputStream uploadedInputStream, User user) {
        String directory = Configuration.getCloudRefDirectory();
        new File(directory).mkdirs();

        try {
            // create temp file to save input stream
            File temp = File.createTempFile(directory + "temp-bib-", ".bib");

            try {
                // add space at end of each line to support fields over multiple lines correctly
                InputStream is = convertStream(uploadedInputStream);
                // write temp file to disc
                PdfService.writeFileToDisc(is, temp);

                try {
                    // read database from temp BibTeX file
                    BibTeXDatabase database = parseBibTeX(temp);

                    // all BibTeX entries after converted, e.g. reference type only with lowercase characters
                    List<BibTeXEntry> convertedEntries = new ArrayList<>();
                    // entries which bibtexkey already exists at backend
                    Map<String, String> modifiedBibTeXKeys = new HashMap<>();

                    // modify BibTeX entries if necessary
                    Collection<BibTeXEntry> entries = database.getEntries().values();
                    for (BibTeXEntry entry : entries) {
                        String newKey = null;
                        // convert key
                        String tempKey;
                        // convert entry, e.g. reference type only with lowercase characters
                        Key convertedKey = convertKey(entry.getKey().getValue());
                        if (!convertedKey.equals(entry.getKey())) {
                            newKey = convertedKey.getValue();
                            tempKey = newKey;
                        } else {
                            tempKey = entry.getKey().getValue();
                        }

                        // check if key exists already, if yes create a unique bibtexkey
                        if (bibtexkeyExists(tempKey)) {
                            if (newKey == null) {
                                newKey = bibtexkeyExists(entry.getKey().getValue()) + "-";
                            } else {
                                newKey += "-";
                            }
                            int number = 1;
                            while (bibtexkeyExists(newKey + String.valueOf(number))) {
                                number++;
                            }
                            newKey += String.valueOf(number);
                            modifiedBibTeXKeys.put(convertedKey.getValue(), newKey);
                        }
                        convertedEntries.add(convertEntry(entry, newKey));
                    }

                    List<String> insertedKeys = new ArrayList<>();
                    // if modified some key (because it exists already) check if crossref of other entries must be changed
                    if (!modifiedBibTeXKeys.isEmpty()) {
                        for (BibTeXEntry e : convertedEntries) {
                            // check if entry has crossref entry
                            Value crossrefValue = e.getField(BibTeXEntry.KEY_CROSSREF);
                            if (crossrefValue != null) {
                                // change crossref entry to new bibtexkey
                                String newCrossrefValue = modifiedBibTeXKeys.get(crossrefValue.toUserString());
                                if (newCrossrefValue != null) {
                                    CrossReferenceValue crossRef = new CrossReferenceValue(new StringValue(newCrossrefValue, StringValue.Style.BRACED), null);
                                    e.removeField(BibTeXEntry.KEY_CROSSREF);
                                    e.addField(BibTeXEntry.KEY_CROSSREF, crossRef);
                                }
                            }
                            saveBib(e);
                            insertedKeys.add(e.getKey().getValue());
                        }
                    } else {
                        // write each entry to separate file at disk
                        for (BibTeXEntry e : convertedEntries) {
                            saveBib(e);
                            insertedKeys.add(e.getKey().getValue());
                        }
                    }
                    // commit inserted files
                    GitService.commitImportedBibFile(insertedKeys, user);
                    return true;
                } catch (IOException e) {
                    LOGGER.error("could not save reference", e);
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                // delete temp file
                if (!temp.delete()) {
                    temp.deleteOnExit();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static InputStream convertStream(InputStream inputStream) throws IOException {
        String newLine = System.getProperty("line.separator");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder result = new StringBuilder();
        String line;
        boolean flag = false;
        while ((line = reader.readLine()) != null) {
            // add space to end of every line to support field values over multiple lines correctly
            result.append(flag ? newLine : "").append(line + " ");
            flag = true;
        }

        // convert String to InputStream
        return new ByteArrayInputStream(result.toString().getBytes());
    }

    private static Key convertKey(String unmodifiedKey) {
        Key key;

        // convert bibtexkey that it is valid after given pattern
        // necessary to save bib file at disc
        Pattern KEY_PATTERN = Pattern.compile("[A-Za-z0-9-]*");
        if (!unmodifiedKey.matches(KEY_PATTERN.pattern())) {
            String newKey = "";
            Matcher m = KEY_PATTERN.matcher(unmodifiedKey);
            while (m.find()) {
                if (m.group(0).equals("")) {
                    newKey += "-";
                } else {
                    newKey += m.group(0);
                }
            }
            key = new Key(newKey.substring(0, newKey.length() - 1));
        } else {
            key = new Key(unmodifiedKey);
        }
        return key;
    }

    private static BibTeXEntry convertEntry(BibTeXEntry entry, String newKey) {
        Key key;
        if (newKey == null) {
            key = entry.getKey();
        } else {
            key = new Key(newKey);
        }

        // convert type to lowercase characters
        Key type = new Key(entry.getType().toString().toLowerCase());

        BibTeXEntry result = new BibTeXEntry(type, key);

        // convert keys of fields to lowercase characters
        for (Map.Entry<Key, Value> field : entry.getFields().entrySet()) {
            // convert crossref key if it exists
            if (field.getKey().equals(BibTeXEntry.KEY_CROSSREF)) {
                Key k = convertKey(field.getValue().toUserString());
                CrossReferenceValue crossref2 = new CrossReferenceValue(new StringValue(k.getValue(), StringValue.Style.BRACED), null);
                result.addField(BibTeXEntry.KEY_CROSSREF, crossref2);
            } else {
                result.addField(new Key(field.getKey().toString().toLowerCase()), field.getValue());
            }
        }

        return result;
    }

    private static BibTeXDatabase parseBibTeX(File file) throws IOException {
        file.getParentFile().mkdirs();

        try (Reader reader = new FileReader(file)) {
            BibTeXParser parser = null;
            try {
                parser = new BibTeXParser() {

                    @Override
                    public void checkStringResolution(Key key, BibTeXString string) {
                        if (string == null) {
                            LOGGER.error("Unresolved string: \"" + key.getValue() + "\"");
                        }
                    }

                    @Override
                    public void checkCrossReferenceResolution(Key key, BibTeXEntry entry) {
    //                    if (entry == null) {
    //                        System.err.println("Unresolved cross-reference: \"" + key.getValue() + "\"");
    //                    }
                    }
                };
            } catch (ParseException e) {
                LOGGER.error("Could not parse reference", e);
            }

            try {
                return parser.parse(reader);
            } catch (ParseException e) {
                LOGGER.error("Could not parse reference", e);
                String content = String.join("\n", Files.readAllLines(file.toPath()));
                LOGGER.debug("File content", content);
                throw new IOException("Could not parse reference", e);
            }
        }
    }

    private static void formatBibTeX(BibTeXDatabase database, File file) throws IOException {
        file.getParentFile().mkdirs();
        Writer writer = (file != null ? new FileWriter(file) : new OutputStreamWriter(System.out));

        try {
            BibTeXFormatter formatter = new BibTeXFormatter();

            formatter.format(database, writer);
        } finally {
            writer.close();
        }
    }
}
