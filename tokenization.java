import org.junit.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class tokenization
{
    public static void main(String[] args) throws IOException
    {
        List<String> result = tokenize("src/tokenization-input-part-A.txt");
        FileWriter writer = new FileWriter("src/tokenized.txt");
        for (String str : result)
            writer.write(str + System.lineSeparator());
        writer.close();

        List<String> result2 = tokenize("src/tokenization-input-part-B.txt");
        FileWriter writer2 = new FileWriter("src/terms.txt");

        Map<String, Integer> map =
                new TreeMap<String, Integer>();
        for (String str : result2)
        {
            if (!map.containsKey(str))
                map.put(str, 1);
            else
                map.put(str, map.get(str) + 1);
        }

        while (map.size() > 0)
        {
            Map.Entry<String, Integer> newMap = map.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get();
            writer2.write(newMap.getKey() + " : " + newMap.getValue() + System.lineSeparator());
            map.remove(newMap.getKey());
        }
        writer.close();
        writer2.close();
    }

    public static List<String> tokenize(String fileName) throws IOException
    {
        //1. open stopwords, read into some set/dictionary
        File file1 = new File("src/stopwords.txt");
        Set<String> stopwords = new HashSet<String>();
        Scanner input = new Scanner(file1);
        while (input.hasNext())
        {
            String word = input.next();
            stopwords.add(word);
        }

        //2. open input file and read it into a single string
        Path filePath = Path.of(fileName);
        File file2 = new File(fileName);
        String fileStr = Files.readString(filePath);

        //3. convert string to lowercase
        fileStr = fileStr.toLowerCase();

        //4. Check abbreviations, can use a matcher with regular expression and
        // substitute the periods in the matched elements with empty string
        Pattern abbreviation = Pattern.compile("\\b(?:[a-zA-Z]\\.){2,}");
        Matcher abbrevStr = abbreviation.matcher(fileStr);
        StringBuilder sb = new StringBuilder(fileStr);
        while (abbrevStr.find())
        {
            //find this string in the original string
            String foundStr = abbrevStr.group();
            int found_str_length = foundStr.length();
            int indexFound2 = sb.indexOf(foundStr);
            String newStr = "";

            //if there are periods in here, remove them
            for (int i = indexFound2; i < found_str_length + indexFound2; i++)
            {
                if (sb.charAt(i) == '.')
                {
                    sb.deleteCharAt(i);
                }
            }
        }
        String fileStr2 = sb.toString();

        //5. Get rid of punctuation
        String[] newTokens = fileStr2.split("\\p{Punct}");

        //6 Stopword removal
        //7. stemming
        // if tok in stopword set continue, if not stem it and put in output

        List<String> processed = new ArrayList<String>();
        List<String> newString = new ArrayList<String>();
        FileWriter tempWriter = new FileWriter("src/total_words.txt");
        FileWriter tempWriter2 = new FileWriter("src/vocab.txt");
        for (String temp : newTokens)
        {
            String[] newStr2 = temp.split("\\s");
            for (String word : newStr2)
            {
                if (!word.matches("^\\s*$"))
                    newString.add(word);
            }
        }
        String[] test = newString.toArray(new String[0]);
        for (String tok: test)
        {
            if (stopwords.contains(tok))
                continue;
            else
            {
                String newStr = stem(tok);
                processed.add(newStr);
            }
        }
        return processed;
    }

    //ONLY APPLY 1 RULE PER STEP
    public static String stem(String tok)
    {
        //replace sses by ss
        //longest applicable suffix executed

        boolean do_nothing = false;
        StringBuilder sb = new StringBuilder(tok);
        if (sb.length() >= 2 && sb.charAt(sb.length() - 1) == 's' && sb.charAt(sb.length() - 2) == 's')
            do_nothing = true;
        else if (sb.length() >= 2 && sb.charAt(sb.length() - 1) == 's' && sb.charAt(sb.length() - 2) == 'u')
            do_nothing = true;

        if (sb.indexOf("sses") != -1)
        {
            sb.delete(sb.indexOf("sses") + 2, sb.indexOf("sses") + 4);
        }
        else if (sb.indexOf("ied") != -1)
        {
            if (sb.indexOf("ied") - 2 < 0)
                sb.deleteCharAt(sb.indexOf("ied") + 2);
            else
                sb.delete(sb.indexOf("ied") + 1, sb.indexOf("ied") + 3);
        }
        else if (sb.indexOf("ies") != -1)
        {
            if (sb.indexOf("ies") - 2 < 0)
                sb.deleteCharAt(sb.indexOf("ies") + 2);
            else
                sb.delete(sb.indexOf("ies") + 1, sb.indexOf("ies") + 3);
        }
        else if (sb.length() >= 2 && sb.charAt(sb.length() - 1) == 's' && !isVowel(sb.charAt(sb.length() - 2)) && do_nothing == false)
        {
            sb.deleteCharAt(sb.length() - 1);
        }
        //replace ied or ies by i if preceded by more than one letter, otherwise by ie
        else if (sb.indexOf("eedly") != -1)
        {
            if (counts(sb.toString()))
                sb.delete(sb.indexOf("eedly") + 2, sb.indexOf("eedly") + 5);
        }
        else if (sb.indexOf("ingly") != -1)
        {
            if (ed_ing(sb.toString()))
                sb.delete(sb.indexOf("ingly"), sb.indexOf("ingly") + 5);

            if (check_at(sb.toString()))
                sb.append("e");
            else if (check_l_s_z(sb.toString()))
                sb.deleteCharAt(sb.length() - 1);
            else if (sb.length() <= 3 && !Character.isDigit(sb.charAt(sb.length() - 1)))
                sb.append("e");
        }
        else if (sb.indexOf("edly") != -1)
        {
            if (ed_ing(sb.toString()))
                sb.delete(sb.indexOf("edly"), sb.indexOf("edly") + 4);

            if (check_at(sb.toString()))
                sb.append("e");
            else if (check_l_s_z(sb.toString()))
                sb.deleteCharAt(sb.length() - 1);
            else if (sb.length() <= 3 && !Character.isDigit(sb.charAt(sb.length() - 1)))
                sb.append("e");
        }
        else if (sb.indexOf("ing") != -1)
        {
            if (sb.indexOf("ing") != -1)
            {
                if (ed_ing(sb.toString()))
                    sb.delete(sb.indexOf("ing"), sb.indexOf("ing") + 3);
            }

            if (check_at(sb.toString()))
                sb.append("e");
            else if (check_l_s_z(sb.toString()))
                sb.deleteCharAt(sb.length() - 1);
            else if (sb.length() <= 3 && !Character.isDigit(sb.charAt(sb.length() - 1)))
                sb.append("e");
        }
        else if (sb.indexOf("eed") != -1)
        {
            if (counts(sb.toString()))
                sb.deleteCharAt(sb.indexOf("eed") + 2);
        }
        else if (sb.indexOf("ed") != -1)
        {
            if (ed_ing(sb.toString()))
                sb.delete(sb.indexOf("ed"), sb.indexOf("ed") + 2);

            if (check_at(sb.toString()))
                sb.append("e");
            else if (check_l_s_z(sb.toString()))
                sb.deleteCharAt(sb.length() - 1);
            else if (sb.length() <= 3 && !Character.isDigit(sb.charAt(sb.length() - 1)))
                sb.append("e");
        }

        return sb.toString();
    }

    public static boolean isVowel(char c)
    {
        return "AEIOUaeiou".indexOf(c) != -1;
    }

    public static boolean counts(String sb)
    {
        StringBuilder sb2 = new StringBuilder(sb);
        if (sb2.indexOf("eedly") != -1)
        {
            int deleteIndex = sb2.indexOf("eedly");
            sb2.delete(deleteIndex, sb2.length());
        }
        else if (sb2.indexOf("eed") != -1)
        {
            int deleteIndex = sb2.indexOf("eed");
            sb2.delete(deleteIndex, sb2.length());
        }

        //count occurrences of vowel then nonvowel
        int count = 0;
        char[] arr = sb2.toString().toCharArray();
        for (int i = 0; i < arr.length - 1; i++)
        {
            if (isVowel(arr[i]) && !isVowel(arr[i + 1]))
                count++;
        }
        if (count > 1 || count == 0)
            return false;

        return true;
    }

    public static boolean ed_ing(String sb)
    {
        StringBuilder sb2 = new StringBuilder(sb);
        if (sb2.indexOf("edly") != -1)
        {
            int deleteIndex = sb2.indexOf("edly");
            sb2.delete(deleteIndex, sb2.length());
        }
        else if (sb2.indexOf("ed") != -1)
        {
            int deleteIndex = sb2.indexOf("ed");
            sb2.delete(deleteIndex, sb2.length());
        }
        else if (sb2.indexOf("ingly") != -1)
        {
            int deleteIndex = sb2.indexOf("ingly");
            sb2.delete(deleteIndex, sb2.length());
        }
        else if (sb2.indexOf("ing") != -1)
        {
            int deleteIndex = sb2.indexOf("ing");
            sb2.delete(deleteIndex, sb2.length());
        }
        char[] temp = sb2.toString().toCharArray();
        for (int i = 0; i < temp.length; i++)
        {
            if (isVowel(temp[i]))
                return true;
        }
        return false;
    }

    public static boolean check_at(String str)
    {
        StringBuilder sb = new StringBuilder(str);
        return sb.length() > 1 && (sb.charAt(sb.length() - 1) == 't' && sb.charAt(sb.length() - 2) == 'a') || (sb.length() > 1 &&
                sb.charAt(sb.length() - 1) == 'l' && sb.charAt(sb.length() - 2) == 'b')
                || (sb.length() > 1 && sb.charAt(sb.length() - 1) == 'z' && sb.charAt(sb.length() - 2) == 'i');
    }

    public static boolean check_l_s_z(String str)
    {
        StringBuilder sb = new StringBuilder(str);
        return sb.length() > 1 && sb.charAt(sb.length() - 2) == sb.charAt(sb.length() - 1) && sb.charAt(sb.length() - 1) != 'l' &&
                sb.charAt(sb.length() - 1)
                        != 's' && sb.charAt(sb.length() - 1) != 'z';
    }

    @Test
    public void testAbbrev()
    {
        String abbrev = "U.S.A.";

        Pattern abbreviation = Pattern.compile("\\b(?:[a-zA-Z]\\.){2,}");
        Matcher abbrevStr = abbreviation.matcher(abbrev);
        StringBuilder sb = new StringBuilder(abbrev);
        while (abbrevStr.find())
        {
            //find this string in the original string
            String newStr = "";

            //if there are periods in here, remove them
            for (int i = 0; i < sb.length(); i++)
            {
                if (sb.charAt(i) == '.')
                {
                    sb.deleteCharAt(i);
                }
            }
            assert(sb.toString().equals("USA"));
        }
    }
}

