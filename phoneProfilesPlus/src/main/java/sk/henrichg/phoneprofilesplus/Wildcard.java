package sk.henrichg.phoneprofilesplus;

// Copyright (c) 2003-2009, Jodd Team (jodd.org). All Rights Reserved.


/*
 * Checks whether a string matches a given wildcard pattern.
 * Possible patterns allow to match single characters (for example '?') or any count of
 * characters (for example '*'). Wildcard characters can be escaped (by an '\').
 * <p>
 * This method uses recursive matching, as in linux or windows. regexp works the same.
 * This method is very fast, comparing to similar implementations.
 */
class Wildcard {

    /*
     * Checks whether a string matches a given wildcard pattern.
     *
     * @param string  input string
     * @param pattern pattern to match
     * @param one wildcard for one character
     * @param more wildcard for more characters
     * @return      <code>true</code> if string matches the pattern, otherwise <code>false</code>
     */
    @SuppressWarnings("SameParameterValue")
    static boolean match(String string, String pattern, char one, char more, boolean caseInsensitive) {
        return match(string, pattern, one, more, 0, 0, caseInsensitive);
    }

    /*
     * Checks if two strings are equals or if they {@link ##match(String, String, char, char)}.
     * Useful for cases when matching a lot of equal strings and speed is important.
     */
    /*public static boolean equalsOrMatch(String string, String pattern, char one, char more, boolean caseInsensitive) {
        if (string.equals(pattern)) {
            return true;
        }
        return match(string, pattern, one, more, 0, 0, caseInsensitive);
    }*/


    /*
     * Internal matching recursive function.
     */
    private static boolean match(String string, String pattern, char one, char more, int stringStartNdx, int patternStartNdx, boolean caseInsensitive) {
        int pNdx = patternStartNdx;
        int sNdx = stringStartNdx;
        int pLen = pattern.length();
        if (pLen == 1) {
            if (pattern.charAt(0) == more) {     // speed-up
                return true;
            }
        }
        int sLen = string.length();
        boolean nextIsNotWildcard = false;

        while (true) {

            // check if end of string and/or pattern occurred
            if (sNdx >= sLen) {   // end of string still may have pending <more> in pattern
                while ((pNdx < pLen) && (pattern.charAt(pNdx) == more)) {
                    pNdx++;
                }
                return pNdx >= pLen;
            }
            if (pNdx >= pLen) {         // end of pattern, but not end of the string
                return false;
            }
            char p = pattern.charAt(pNdx);    // pattern char

            // perform logic
            if (!nextIsNotWildcard) {

                if (p == '\\') {
                    pNdx++;
                    nextIsNotWildcard = true;
                    continue;
                }
                if (p == one) {
                    sNdx++;
                    pNdx++;
                    continue;
                }
                if (p == more) {
                    char pNext = 0;           // next pattern char
                    if (pNdx + 1 < pLen) {
                        pNext = pattern.charAt(pNdx + 1);
                    }
                    if (pNext == more) {         // double <more> have the same effect as one <more>
                        pNdx++;
                        continue;
                    }
                    int i;
                    pNdx++;

                    // find recursively if there is any substring from the end of the
                    // line that matches the rest of the pattern !!!
                    for (i = string.length(); i >= sNdx; i--) {
                        if (match(string, pattern, one, more, i, pNdx, caseInsensitive)) {
                            return true;
                        }
                    }
                    return false;
                }
            } else {
                nextIsNotWildcard = false;
            }

            // check if pattern char and string char are equals
            if (caseInsensitive) {
                String sP = "" + p;
                String sCh = "" + string.charAt(sNdx);
                if (sP.compareToIgnoreCase(sCh) != 0)
                    return false;
            } else if (p != string.charAt(sNdx)) {
                return false;
            }

            // everything matches for now, continue
            sNdx++; pNdx++;
        }
    }


    // ---------------------------------------------------------------- utilities

    /*
     * Matches string to at least one pattern.
     * Returns index of matched pattern, or <code>-1</code> otherwise.
     * @see ##match(String, String, char, char)
     */
    /*public static int matchOne(String src, String[] patterns, char one, char more, boolean caseInsensitive) {
        for (int i = 0; i < patterns.length; i++) {
            if (match(src, patterns[i], one, more, caseInsensitive)) {
                return i;
            }
        }
        return -1;
    }*/

}
