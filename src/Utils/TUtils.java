package Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

public class TUtils {

    // Inserts an object into a sorted list using a given comparator and returns the final index
    public static <T> int indexedBasedBinaryInsert(ArrayList<T> list, T obj, Comparator<T> comparator) {
        if (list.isEmpty()) {
            list.add(obj);
            return 0;
        }
        int segmentStartIndex = 0;
        int segmentEndIndex = list.size()-1;
        while (segmentStartIndex != segmentEndIndex) { // Narrow it down to one target item
            int midpoint = (segmentStartIndex + segmentEndIndex) / 2;
            if (comparator.compare(obj, list.get(midpoint)) < 0) {
                segmentEndIndex = midpoint;
            } else {
                segmentStartIndex = midpoint + 1;
            }
        }

        int insertIndex;
        if (comparator.compare(obj, list.get(segmentStartIndex)) < 0) insertIndex = segmentStartIndex;
        else insertIndex = segmentStartIndex + 1;
        list.add(insertIndex, obj);
        return insertIndex;
    }
    public interface Comparator<T> {
        int compare(T obj1, T obj2); // returns -1 of obj1 < obj2, 0 if obj1 == obj2, and 1 if obj1 > obj2
    }

    public static String divideStringIntoLines(String text, FontMetrics fm, int maxLineWidth) {
        String[] explicitLines = text.split("\n");
        String finalLines = "";
        for (String explicitLine : explicitLines) {
            String[] wordsInLine = explicitLine.split(" ");
            String lineConstructor = "";
            int currentLineWidth = 0;
            for (int i = 0; i < wordsInLine.length; i++) {
                String toAppend = (lineConstructor.isEmpty()) ? wordsInLine[i] : " " + wordsInLine[i]; // First word in line shouldn't get a space before it
                int wordWidth = fm.stringWidth(toAppend);
                if (currentLineWidth + wordWidth <= maxLineWidth) {
                    lineConstructor += toAppend;
                    currentLineWidth += wordWidth;
                }
                else {
                    finalLines += lineConstructor + "\n";
                    lineConstructor = wordsInLine[i];
                    currentLineWidth = fm.stringWidth(lineConstructor);
                }
            }
        }
        return finalLines;
    }

    public static HashSet<String> permutations(String base) {
        return permutations(new StringBuilder(base), "");
    }

    public static HashSet<String> permutations (StringBuilder base, String constructor) {
        HashSet<String> ret = new HashSet<>();
        if (base.length() < 1) {
            ret.add(constructor);
            return ret;
        }

        for (int i = 0; i < base.length(); i++) {
            StringBuilder copy = new StringBuilder(base);
            char item = copy.charAt(i);
            copy.deleteCharAt(i);
            String new_constructor = constructor;
            new_constructor += item;
            for (String perm : permutations(copy, new_constructor)) {
                ret.add(perm);
            }
        }
        return ret;
    }
}
