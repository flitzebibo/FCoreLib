package me.FurH.Core.configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class ConfigUpdater {
    
    private Pattern space_regex = Pattern.compile(" ([ ])");
    private Pattern skip_chars = Pattern.compile("[^\\x00-\\x7E]");
    private Pattern invalid_start = Pattern.compile("^[^ a-zA-Z0-9\\w]");

    /**
     * Update and fix the lines of the given yaml file
     * 
     * @param main the current file
     * @param is the original file
     */
    public void updateLines(File main, InputStream is) {

        BufferedWriter writer = null;
        OutputStreamWriter osw = null;
        FileOutputStream fos = null;
        
        try {

            List<String[]> source = getFileLines(is);
            List<String[]> client = getFileLines(main);

            fos = new FileOutputStream(main);
            osw = new OutputStreamWriter(fos, "UTF-8");

            writer = new BufferedWriter (osw);
            
            HashSet<String> wroten = new HashSet<String>();
            String lastComment = "";
            
            String l = System.getProperty("line.separator");
            for (int j1 = 0; j1 < source.size(); j1++) {
                String[] line = source.get(j1);
                String node = line[0];

                if (node.startsWith("#{S}")) {
                    lastComment = node.substring(4);
                    continue;
                }

                if (node.isEmpty()) {
                    writer.write(l);
                    continue;
                }

                if (isList(node)) {
                    writer.write(node + l);
                    continue;
                }

                if (isComment(node)) {
                    writer.write(node + l);
                    continue;
                }

                boolean isList = false;
                if (isList(node) || node.endsWith(";")) {
                    isList = true;
                }

                String[] sections = node.split("\\.");
                String spaces = "";
                int index = 0;

                while (sections.length > index) {
                    boolean lastIndex = sections.length <= index + 1;
                    
                    String section = sections[index].replaceAll(";", "");
                    
                    if (isList || !lastIndex) {

                        if (wroten.add((spaces + section))) {
                            writer.write(spaces + section + ":" + l);
                        }
                        
                        spaces += "  ";
                    } else if (line.length > 1) {
                        
                        if (!lastComment.isEmpty()) {
                            writer.write(lastComment + l);
                            lastComment = "";
                        }
                        
                        String value = line[1];
                        
                        data: for (String[] data : client) {
                            if (data.length > 1 && data[0].equalsIgnoreCase(node)) {
                                value = data[1];
                                break data;
                            }
                        }

                        writer.write(spaces + section + ": " + trateString(value) + l);
                    }

                    index++;
                }

            }

            writer.write(l+"# End of the file -->");

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
                if (osw != null) {
                    osw.flush();
                    osw.close();
                }
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException ex) { }
        }
    }
    
    private List<String[]> getFileLines(File file) {
        FileInputStream fis = null;

        try {
            
            if (!file.exists()) {
                System.out.println("File does not exists!");
                return null;
            }
            
            return getFileLines(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) { }
        }
        return null;
    }
    
    private List<String[]> getFileLines(InputStream is) {

        Scanner scanner = null;

        try {
            scanner = new Scanner(is, "UTF-8");

            List<String> lines = new ArrayList<String>();

            while (scanner.hasNext()) {
                lines.add(parseInitial(scanner.nextLine()));
            }
            
            List<String[]> newLines = new ArrayList<String[]>();

            int last = 0;
            int lastListIndex = -1;
            String lastMultiple = "";

            for (int j1 = 0; j1 < lines.size(); j1++) {
                String line = lines.get(j1).replaceAll("\t", "  ");

                if (!isList(line)) {
                    lastListIndex = -1;
                }

                if (!lastMultiple.isEmpty()) {
                    line = lastMultiple += removeInitial(replaceMultiLine(line));
                    lastMultiple = "";
                }

                if (line.isEmpty()) {
                    newLines.add(new String[] { line });
                    continue;
                }

                if (line.trim().isEmpty()) {
                    newLines.add(new String[] { line.trim() });
                    continue;
                }
                
                int index = getSectionNumber(line);

                boolean nextIsSection = false;
                if (j1 + 1 < lines.size() -1) {
                    nextIsSection = getSectionNumber(lines.get(j1 + 1)) == 0;
                }
                
                boolean afterIsSection = false;
                if (j1 - 1 > -1) {
                    afterIsSection = getSectionNumber(lines.get(j1 - 1)) == 0;
                }
                
                if (isComment(line) && !line.startsWith("#") && (nextIsSection || afterIsSection)) {
                    line = "#{S}" + line;
                }
                
                if (isComment(line)) {
                    newLines.add(new String[] { line });
                    continue;
                }

                if (isList(line)) {

                    int nextList = lastListIndex;
                    if (nextList != -1) {

                        int p1 = line.indexOf('-');

                        if (p1 != -1) {
                            line = line.substring(p1);

                            String spaces = "  ";
                            while (nextList > 0) {
                                spaces += "  ";
                                nextList--;
                            }
                            
                            line = spaces += line;
                        }
                    }

                    newLines.add(new String[] { line });
                    continue;
                }
                
                List<String> sections = new ArrayList<String>();

                if (index == 0) {
                    last = j1;
                }
                
                if (!isList(line)) { lastListIndex = index; }
                
                try {
                    sections.add(parseSpaces(getSectionSelf(line)));
                } catch (Exception ex) {
                    newLines.add(new String[] { line });
                    continue;
                }

                int subLine = j1;
                int pass = -1;

                HashSet<String> sectionAdded = new HashSet<String>();
                
                if (index > 0) {
                    while (subLine >= last) {
                        subLine--;
                        
                        if (subLine <= -1) {
                            break;
                        }
                        
                        String old = lines.get(subLine);
                        int newIndex = getSectionNumber(old);
                                                
                        if (isList(old)) {
                            continue;
                        }

                        if (isSection(old) && newIndex < index && newIndex != pass && newIndex != index) {

                            if (sectionAdded.add(old + newIndex)) {
                                sections.add(trateSection(old));
                            }

                            pass = newIndex;
                        }
                    }
                }

                Collections.reverse(sections);
                String build = "";

                for (String string : sections) {
                    build += string;
                }

                if (build.endsWith(".")) {
                    build = build.substring(0, build.length() - 1);
                }

                String content = "";
                if (!isSection(line)) {
                    int cut = line.indexOf(':', index) + 2;
                    content = line.substring(cut);
                }
                
                if (isMultiLine(content)) {
                    lastMultiple += replaceMultiLine(line);
                    continue;
                }

                boolean nextIsList = false;
                if (j1 + 1 < lines.size() -1) {
                    nextIsList = isList(lines.get(j1 + 1));
                }

                if (!isList(line)) { lastListIndex = index; }

                if (nextIsList) {
                    newLines.add(new String[] { build + ";" });
                    continue;
                }

                if (!isList(line)) { lastListIndex = index; }

                if (content.isEmpty()) {
                    continue;
                }

                if (isNumberOnly(content)) {
                    newLines.add(new String[] { build, content.trim() });
                } else {
                    content = content.trim();
                    
                    if (content.contains(" ") && 
                            !content.startsWith("'") && !content.endsWith("'") && 
                            !content.startsWith("\"") && !content.endsWith("\"")) {
                        if (isInvalidStart(content)) {
                            content = "\"" + content + "\"";
                        }
                    }

                    newLines.add(new String[] { build, content });
                }

                if (!isList(line)) { lastListIndex = index; }
            }
            
            return newLines;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (scanner != null) {
                    scanner.close();
                }
            } catch (IOException ex) { }
        }
    }

    private String replaceMultiLine(String line) {
        return line.replaceAll("'", "");
    }
    
    private boolean isMultiLine(String line) {
        return line.replaceAll(" ", "").startsWith("'") && !line.replaceAll(" ", "").endsWith("'");
    }
    
    private String removeInitial(String line) {
        
        while (line.charAt(0) == ' ') {
            line = line.substring(1);
        }
        
        return line;
    }
    
    private String trateSection(String line) {
        return parseSpaces(line.replaceAll(":", "."));
    }
    
    private String trateString(String line) {

        String newLine = line;
        Matcher matcher = skip_chars.matcher(line);
        while (matcher.find()) {
            newLine = line.replace(matcher.group(), toHex(matcher.group()));
        }

        return newLine;
    }
    
    private String toHex(String line) {
        String hex = Integer.toHexString((int) line.charAt(0));

        String prefix;
        if (hex.length() == 1) {
            prefix = "\\u000";
        } else if (hex.length() == 2) {
            prefix = "\\u00";
        } else if (hex.length() == 3) {
            prefix = "\\u0";
        } else {
            prefix = "\\u";
        }

        return prefix + hex;
    }
    
    private String parseSpaces(String line) {

        if (line.endsWith(" ")) {
            line = line.replaceAll(" ", "");
        } else {
            line = space_regex.matcher(line).replaceAll("");
        }
        
        return line;
    }
    
    private boolean isNumberOnly(String content) {
        return content.replaceAll("[^0-9-.]", "").equals(content);
    }
    
    private String getSectionSelf(String line) throws StringIndexOutOfBoundsException {
        return line.substring(0, line.indexOf(':'));
    }
    
    private boolean isSection(String line) {
        return !isList(line) && line.trim().endsWith(":");
    }

    private int getSectionNumber(String line) {
        int section = 0;

        String spaces = "  ";
        while (line.startsWith(spaces)) {
            spaces += "  ";
            section++;
        }

        return section;
    }
    
    private boolean isList(String line) {
        return line.replaceAll(" ", "").startsWith("-");
    }

    private boolean isComment(String line) {
        return line.replaceAll(" ", "").startsWith("#");
    }

    private String parseInitial(String line) {
        return line = line.replaceAll("0xFFFD", "");
    }
    
    private boolean isInvalidStart(String content) {
        return invalid_start.matcher(content).matches();
    }
}