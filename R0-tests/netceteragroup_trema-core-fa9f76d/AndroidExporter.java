package com.netcetera.trema.core.exporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.netcetera.trema.common.TremaCoreUtil;
import com.netcetera.trema.core.Status;
import com.netcetera.trema.core.api.IExportFilter;
import com.netcetera.trema.core.api.IExporter;
import com.netcetera.trema.core.api.IKeyValuePair;
import com.netcetera.trema.core.api.ITextNode;
import com.netcetera.trema.core.api.IValueNode;
import org.inlinetest.Here;
import static org.inlinetest.Here.group;

/**
 * Exports an <code>IDatabase</code> to a Android "strings.xml" file.
 */
public class AndroidExporter implements IExporter {

    public static final Logger LOG = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private final File outputFile;

    private final OutputStreamFactory outputStreamFactory;

    private final IExportFilter[] iExportFilters;

    private final HashMap<String, String> placeholderMap;

    /**
     * Constructor.
     *
     * @param outputFile the file used to write the output into
     * @param outputStreamFactory factory used to create the outputstream to the file
     */
    public AndroidExporter(File outputFile, OutputStreamFactory outputStreamFactory) {
        this.outputFile = outputFile;
        this.outputStreamFactory = outputStreamFactory;
        iExportFilters = new IExportFilter[1];
        iExportFilters[0] = new AndroidExportFilter();
        placeholderMap = new HashMap<>();
        placeholderMap.put("%d", "%%%d\\$d");
        placeholderMap.put("%i", "%%%d\\$d");
        placeholderMap.put("%o", "%%%d\\$o");
        placeholderMap.put("%u", "%%%d\\$d");
        placeholderMap.put("%x", "%%%d\\$x");
        placeholderMap.put("%X", "%%%d\\$X");
        placeholderMap.put("%f", "%%%d\\$f");
        placeholderMap.put("%e", "%%%d\\$e");
        placeholderMap.put("%E", "%%%d\\$E");
        placeholderMap.put("%g", "%%%d\\$g");
        placeholderMap.put("%G", "%%%d\\$G");
        placeholderMap.put("%c", "%%%d\\$c");
        placeholderMap.put("%s", "%%%d\\$s");
        placeholderMap.put("%@", "%%%d\\$s");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void export(ITextNode[] nodes, String masterlanguage, String language, Status[] states) throws ExportException {
        LOG.info("Exporting Android XML file...");
        try (OutputStream outputStream = outputStreamFactory.createOutputStream(outputFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"))) {
            synchronized (this) {
                // write header
                bw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                bw.write("\n");
                bw.write("<!-- Generated file - do not edit -->");
                bw.write("\n");
                bw.write("<resources>");
                bw.write("\n");
                for (ITextNode node : nodes) {
                    IValueNode valueNode = node.getValueNode(language);
                    // get value from value node
                    if (valueNode != null) {
                        if (states == null || TremaCoreUtil.containsStatus(valueNode.getStatus(), states)) {
                            IKeyValuePair keyValuePair = new KeyValuePair(node.getKey(), valueNode.getValue());
                            for (IExportFilter filter : iExportFilters) {
                                filter.filter(keyValuePair);
                            }
                            // validate key after it has been filtered
                            String key = keyValuePair.getKey();
                            if (!isValidKeyName(key)) {
                                throw new IllegalArgumentException("Invalid string key name " + (key != null ? "'" + key + "'" : "null"));
                            }
                            // map the placeholders and write the result
                            String value = keyValuePair.getValue();
                            if (value != null) {
                                String formattedText = resolveIOSPlaceholders(value);
                                String rowText = String.format("  <string name=\"%s\">%s</string>", key, formattedText);
                                bw.write(rowText);
                                bw.write("\n");
                            }
                        }
                    }
                }
                // write footer
                bw.write("</resources>");
            }
            bw.flush();
        } catch (IOException e) {
            throw new ExportException("Could not store properties:" + e.getMessage());
        }
        LOG.info("Exporting of Android XML file finished.");
    }

    /**
     * Returns a string with resolved iOS placeholders.
     * @param original the original text
     * @return The text with resolved iOS placeholders (if there were some).
     */
    protected String resolveIOSPlaceholders(String original) {
        int index = 1;
        String resolved = original;
        final Pattern pattern = Pattern.compile("%[a-zA-Z@]");
        final Matcher matcher = pattern.matcher(original);
        while (matcher.find()) {
            String placeholderIOS = matcher.group();
            new Here("Unit", 137).given(matcher, "18.xml").checkEq(placeholderIOS, "%e");
            new Here("Unit", 137).given(matcher, "23.xml").checkEq(placeholderIOS, "%s");
            new Here("Unit", 137).given(matcher, "11.xml").checkEq(placeholderIOS, "%i");
            new Here("Unit", 137).given(matcher, "24.xml").checkEq(placeholderIOS, "%@");
            new Here("Unit", 137).given(matcher, "20.xml").checkEq(placeholderIOS, "%g");
            new Here("Unit", 137).given(matcher, "13.xml").checkEq(placeholderIOS, "%u");
            new Here("Unit", 137).given(matcher, "21.xml").checkEq(placeholderIOS, "%G");
            new Here("Unit", 137).given(matcher, "15.xml").checkEq(placeholderIOS, "%X");
            new Here("Unit", 137).given(matcher, "19.xml").checkEq(placeholderIOS, "%E");
            new Here("Unit", 137).given(matcher, "17.xml").checkEq(placeholderIOS, "%p");
            new Here("Unit", 137).given(matcher, "16.xml").checkEq(placeholderIOS, "%f");
            new Here("Unit", 137).given(matcher, "14.xml").checkEq(placeholderIOS, "%x");
            new Here("Unit", 137).given(matcher, "10.xml").checkEq(placeholderIOS, "%d");
            new Here("Unit", 137).given(matcher, "12.xml").checkEq(placeholderIOS, "%o");
            new Here("Unit", 137).given(matcher, "22.xml").checkEq(placeholderIOS, "%c");
            String placeholderAndroid = placeholderMap.get(placeholderIOS);
            if (placeholderAndroid != null) {
                placeholderAndroid = String.format(placeholderAndroid, index++);
                new Here("Unit", 140).given(placeholderAndroid, "%%%d\\$d").given(index, 4).checkEq(placeholderAndroid, "%4\\$d");
                new Here("Unit", 140).given(placeholderAndroid, "%%%d\\$G").given(index, 11).checkEq(placeholderAndroid, "%11\\$G");
                new Here("Unit", 140).given(placeholderAndroid, "%%%d\\$E").given(index, 9).checkEq(placeholderAndroid, "%9\\$E");
                new Here("Unit", 140).given(placeholderAndroid, "%%%d\\$d").given(index, 1).checkEq(placeholderAndroid, "%1\\$d");
                new Here("Unit", 140).given(placeholderAndroid, "%%%d\\$c").given(index, 12).checkEq(placeholderAndroid, "%12\\$c");
                new Here("Unit", 140).given(placeholderAndroid, "%%%d\\$o").given(index, 3).checkEq(placeholderAndroid, "%3\\$o");
                new Here("Unit", 140).given(placeholderAndroid, "%%%d\\$e").given(index, 8).checkEq(placeholderAndroid, "%8\\$e");
                new Here("Unit", 140).given(placeholderAndroid, "%%%d\\$d").given(index, 2).checkEq(placeholderAndroid, "%2\\$d");
                new Here("Unit", 140).given(placeholderAndroid, "%%%d\\$f").given(index, 7).checkEq(placeholderAndroid, "%7\\$f");
                new Here("Unit", 140).given(placeholderAndroid, "%%%d\\$X").given(index, 6).checkEq(placeholderAndroid, "%6\\$X");
                new Here("Unit", 140).given(placeholderAndroid, "%%%d\\$s").given(index, 14).checkEq(placeholderAndroid, "%14\\$s");
                new Here("Unit", 140).given(placeholderAndroid, "%%%d\\$g").given(index, 10).checkEq(placeholderAndroid, "%10\\$g");
                new Here("Unit", 140).given(placeholderAndroid, "%%%d\\$s").given(index, 13).checkEq(placeholderAndroid, "%13\\$s");
                new Here("Unit", 140).given(placeholderAndroid, "%%%d\\$x").given(index, 5).checkEq(placeholderAndroid, "%5\\$x");
                resolved = resolved.replaceFirst(placeholderIOS, placeholderAndroid);
            }
        }
        return resolved;
    }

    /**
     * Checks if the key name is valid. Valid key names in android are same as valid Java variables
     * [a-zA-Z][a-zA-Z0-9_]*.
     *
     * @param key to check
     * @return true if the key is valid, false if not
     */
    protected boolean isValidKeyName(String key) {
        // make sure that the key is not null
        if (key == null) {
            return false;
        }
        // check normal variable name
        return key.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }
}
