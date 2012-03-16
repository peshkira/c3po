package com.petpet.c3po.command;

public final class CommandConstants {

  public static final String GATHER_OPTION = "gather";
  public static final String PROFILE_OPTION = "profile";
  public static final String COLLECTION_OPTION = "collection";
  public static final String RECURSIVE_OPTION = "recursive";
  public static final String HELP_OPTION = "help";

  public static final String GATHER_DIR_ARGUMENT = "fits dir";
  public static final String PROFILE_FILENAME_ARGUMENT = "output file directory";
  public static final String COLLECTION_ID_ARGUMENT = "collection name";

  public static final String GATHER_DESCRIPTION = "Gathers and parses all fits files in the passed directory.";
  public static final String PROFILE_DESCRIPTION = "Generates a xml representation of the profile. Optionally you can supply an output directory, which will be created if it does not exist.";
  public static final String COLLECTION_DESCRIPTION = "The collection identifier, e.g 'My Pictures'";
  public static final String RECURSIVE_DESCRIPTION = "Does the parsing operation recursively. To be used in combination with -g.";
  public static final String HELP_DESCRIPTION = "Prints this message";

  private CommandConstants() {
  }
}
