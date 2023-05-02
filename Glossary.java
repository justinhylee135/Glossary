import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * Online glossary for textbook.
 *
 * @author Justin Lee
 */
public final class Glossary {
    /**
     * No argument constructor--private to prevent instantiation.
     */
    private Glossary() {

    }

    /**
     * Compare {@code String}s in lexicographic order.
     */
    private static class StringLT implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }

    /**
     * Sort words and corresponding definition into map.
     *
     * @param fileInput
     *            file input to get the words and definitions from.
     *
     * @return a map of the word and corresponding definition.
     */
    public static Map<String, String> sortWordAndDef(SimpleReader fileInput) {
        //Map of word and definitions
        Map<String, String> wordAndDef = new Map1L<>();

        //Repeat until input file is empty
        while (!fileInput.atEOS()) {
            //Input word to be key for map
            String key = fileInput.nextLine();

            //Input definition to be value for Map
            String value = fileInput.nextLine();

            //Temporary store for next line
            String temp = fileInput.nextLine();

            //If temp contains something that is not an empty line
            while (!fileInput.atEOS() && !temp.equals("")) {
                //Add the extended definition into value
                value += " " + temp;

                //update temp
                temp = fileInput.nextLine();
            }

            //Add key and value into Map
            wordAndDef.add(key, value);
        }

        //Return input sorted into map
        return wordAndDef;
    }

    /**
     * Converts map of words and definition into a queue of just the words.
     *
     * @param wordsAndDef
     *            Map of key(words) and values(definitions).
     * @return Queue of words from wordsAndDef.
     */
    public static Queue<String> convertMapToQueue(
            Map<String, String> wordsAndDef) {
        Queue<String> words = new Queue1L<>();

        //Goes through each pair of wordsAndDef map
        for (Map.Pair<String, String> p : wordsAndDef) {
            //Adds to queue key of p
            words.enqueue(p.key());
        }

        //Return queue of all keys from map
        return words;
    }

    /**
     * Outputs HTML for index.
     *
     * @param indexOut
     *            Output to index stream.
     * @param words
     *            Words sorted in queue.
     * @param wordsAndDef
     *            Words and corresponding definition in map.
     * @param outputFileName
     *            Name of the file out writer to be used to create file outs for
     *            words.
     */
    public static void outputIndex(SimpleWriter indexOut, Queue<String> words,
            Map<String, String> wordsAndDef, String outputFileName) {
        //Opening tags
        indexOut.println("<html>");
        indexOut.println("<head><title> Glossary </title></head>");

        //Start body
        indexOut.println("<body>");

        //Output Header and a horizontal line
        indexOut.println("<h1>Glossary</h1>");
        indexOut.println("<hr>");

        //Output Index header
        indexOut.println("<h2>Index</h2>");

        //Output Unordered List
        indexOut.println("<ul>");

        //Output each word from words Queue
        while (words.length() > 0) {
            indexOut.print("<li>");

            //key for the map will be stored
            String key = words.dequeue();

            //Output stream for word page
            String wordFileName = outputFileName + "/" + key + ".html";

            //Generate word html page
            generateDefinitionPage(key, wordsAndDef, wordFileName);

            //Link word page to word in index
            indexOut.print("<a href = \"" + key + ".html\">" + key + "</a>");

            indexOut.println("</li>");
        }

        //Closing tags
        indexOut.println("</ul></body>");
        indexOut.println("</html>");
    }

    /**
     * Generates html page for word definition.
     *
     * @param word
     *            The definition page will be provided for this word.
     * @param wordsAndDef
     *            The definition will be pulled from this map.
     * @param wordFileName
     *            Name of the file output stream for the word.
     */
    public static void generateDefinitionPage(String word,
            Map<String, String> wordsAndDef, String wordFileName) {
        //Open output stream for word file
        SimpleWriter wordOut = new SimpleWriter1L(wordFileName);

        //Opening tags
        wordOut.println("<html>");
        wordOut.println("<head><title>" + word + "</title></head>");

        //Start body
        wordOut.println("<body>");

        //Output Header and definition
        wordOut.println(
                "<h1 style=\"color:Red \"><b><i>" + word + "</i></b></h1>");
        //&esmp; outputs four spaces (source: www.geeksforgeeks.org)
        wordOut.print("<p>&emsp;&emsp;");

        /**
         * Goes through each word of the definition and either outputs a link to
         * another definition page or outputs the word.
         */

        //Position of char index in word definition
        int position = 0;

        //Go through each character in the word definition
        while (position < wordsAndDef.value(word).length()) {
            //Store the word in a String
            String temp = nextWordOrSeparator(wordsAndDef.value(word),
                    position);

            //Check whether the string is a word in the map.
            if (wordsAndDef.hasKey(temp)) {
                //Output a link to definition page
                wordOut.print(
                        "<a href = \"" + temp + ".html\">" + temp + "</a>");
            } else {
                //Output the definition
                wordOut.print(temp);
            }

            //Update position to after the word ends
            position += temp.length();
        }

        //Close definition and output a horizontal line
        wordOut.println("</p>");
        wordOut.println("<hr>");

        //Output return to link
        wordOut.println("<p>Return to " + "<a href = \"" + "index.html" + "\">"
                + "index</a>.</p>");

        //Closing tags
        wordOut.println("</body>");
        wordOut.println("</html>");

        //Close output stream for word
        wordOut.close();
    }

    /**
     * Returns next word from text.
     *
     * @param text
     *            The text in which a word will be pulled form.
     * @param position
     *            The start of the next word index.
     * @return The next word in text
     */
    public static String nextWordOrSeparator(String text, int position) {
        assert text != null : "Violation of: text is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        //Separators to end word
        final String separatorStr = ", . ! ? ~ ";
        Set<Character> separatorSet = new Set1L<>();
        generateElements(separatorStr, separatorSet);

        //End position for substring to return
        int endPos = position + 1;

        //Assume first character is not a separator
        boolean isSeparator = false;

        //If first letter is a separator
        if (separatorSet.contains(text.charAt(position))) {
            isSeparator = true;
        }

        //Avoid going out of bounds by comparing endPos to length of text
        //Compare whether char is a separator to the corresponding isSet boolean
        while (endPos < text.length()
                && separatorSet.contains(text.charAt(endPos)) == isSeparator) {
            endPos++; //increment
        }

        //Return the next word
        return text.substring(position, endPos);
    }

    /**
     * Generates the set of characters in the given {@code String} into the
     * given {@code Set}.
     *
     * @param str
     *            the given {@code String}
     * @param charSet
     *            the {@code Set} to be replaced
     * @replaces charSet
     * @ensures charSet = entries(str)
     */
    public static void generateElements(String str, Set<Character> charSet) {
        assert str != null : "Violation of: str is not null";
        assert charSet != null : "Violation of: charSet is not null";

        //Clear set
        charSet.clear();

        //Add each character of str into charSet
        for (int i = 0; i < str.length(); i++) {
            //Check that char isn't already in charSet to avoid duplicates
            if (!charSet.contains(str.charAt(i))) {
                charSet.add(str.charAt(i));
            }
        }
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        //Text Output
        SimpleWriter out = new SimpleWriter1L();
        //Text Input
        SimpleReader in = new SimpleReader1L();

        //Get file name
        out.println("Enter name of input file: ");
        String fileName = in.nextLine();

        //Create file input stream
        SimpleReader fileInput = new SimpleReader1L(fileName);

        //Sort input into map
        Map<String, String> wordAndDef = sortWordAndDef(fileInput);

        //Convert map into queue
        Queue<String> words = convertMapToQueue(wordAndDef);

        //Sort queue into alphabetic order
        Comparator<String> cs = new StringLT();
        words.sort(cs);

        //Establish output stream
        out.println("Enter name of output file: ");
        String outputFileName = in.nextLine();

        //Output index page
        String indexOutputFileName = outputFileName + "/index.html";
        SimpleWriter indexOut = new SimpleWriter1L(indexOutputFileName);
        outputIndex(indexOut, words, wordAndDef, outputFileName);

        //Close input and output streams
        fileInput.close();
        indexOut.close();
        out.close();
        in.close();
    }

}
