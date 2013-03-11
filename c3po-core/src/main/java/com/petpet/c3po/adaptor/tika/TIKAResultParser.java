package com.petpet.c3po.adaptor.tika;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A TIKA Result parser. Thanks to Per for his contribution
 * 
 * @author Per Møldrup-Dalum
 * 
 */
public class TIKAResultParser {

  /* join a String array with a delimiter */
  public static String join(String r[], String d) {
    if (r.length == 0)
      return "";
    StringBuilder sb = new StringBuilder();
    int i;
    for (i = 0; i < r.length - 1; i++)
      sb.append(r[i] + d);
    return sb.toString() + r[i];
  }

  public static String[] tail(String s[]) {
    return Arrays.copyOfRange(s, 1, s.length);
  }

  public static Map<String, String> KeyValueMap(InputStream input) throws IOException {
    InputStreamReader streamReader = new InputStreamReader(input, "UTF-8");
    BufferedReader bufferedReader = new BufferedReader(streamReader);

    Map<String, String> map = new LinkedHashMap<String, String>();

    String line = bufferedReader.readLine();
    while (line != null) {
      // Regex to scan for 1 or more whitespace characters
      String[] tokens = line.split("\\s+");
      if (tokens.length >= 2) {
        // remove ':' from head if it is the last character
        String head = tokens[0];
        if (head.length() > 0 && head.charAt(head.length() - 1) == ':') {
          head = head.substring(0, head.length() - 1);
        }

        map.put(head, join(tail(tokens), " "));
      }
      
      line = bufferedReader.readLine();
    }

    return map;
  }
}
