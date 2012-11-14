package com.petpet.c3po.adaptor.rules;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlInfoProcessingRule implements PreProcessingRule {

  private static final Logger LOG = LoggerFactory.getLogger(HtmlInfoProcessingRule.class);

  private Set<String> tags;

  public HtmlInfoProcessingRule() {
    this.tags = new HashSet<String>();
    this.readTags();
  }

  private void readTags() {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(HtmlInfoProcessingRule.class.getClassLoader()
          .getResourceAsStream("adaptors/htmltags")));
      String line = reader.readLine();
      while (line != null) {

        this.tags.add(line);

        line = reader.readLine();
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Override
  public int getPriority() {
    return 1;
  }

  @Override
  public boolean shouldSkip(String property, String value, String status, String tool, String version) {

    if (tool != null && tool.equalsIgnoreCase("HtmlInfo")) {

      if (property.endsWith("rences")) {
        int tagIndex = property.indexOf("Tag");

        if (tagIndex == -1) {
          return true;
        }

        String tag = property.substring(0, tagIndex);

        if (!this.tags.contains(tag)) {
          LOG.debug("Property {} seems to be faulty, skip", property);
          return true;
        }
      }
    }

    return false;
  }

}
