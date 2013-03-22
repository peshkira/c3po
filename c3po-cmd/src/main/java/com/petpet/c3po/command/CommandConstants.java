package com.petpet.c3po.command;

public final class CommandConstants {

  public static final String C3PO_CMD_VERSION = "0.2";

  public static final String GATHER_OPTION = "gather";
  public static final String GATHER_INPUT_TYPE_OPTION = "inputtype";
  public static final String PROFILE_OPTION = "profile";
  public static final String PROFILE_INCLUDE_ELEMENT_IDENTIFIERS = "includeelements";
  public static final String EXPORT_OPTION = "export";
  public static final String COLLECTION_OPTION = "collection";
  public static final String RECURSIVE_OPTION = "recursive";
  public static final String ANONYMIZE_OPTION = "anonymize";
  public static final String HELP_OPTION = "help";
  public static final String VERSION_OPTION = "version";

  public static final String GATHER_DIR_ARGUMENT = "fits dir";
  public static final String GATHER_INPUT_TYPE_ARGUMENT = "input type";
  public static final String PROFILE_FILEPATH_ARGUMENT = "output file directory";
  public static final String EXPORT_OUTPUT_PATH = "output file";
  public static final String COLLECTION_ID_ARGUMENT = "collection name";

  public static final String GATHER_DESCRIPTION = "Gathers and parses all fits files in the passed directory.";
  public static final String PROFILE_DESCRIPTION = "Generates a xml representation of the profile. Optionally you can supply an output directory, which will be created if it does not exist.";
  public static final String PROFILE_INCLUDE_ELEMENTS_DESCRIPTION = "If set the profile will include a special section with all element identifiers part of the collection";
  public static final String EXPORT_DESCRIPTION = "The file path where the file will be writtent to";
  public static final String COLLECTION_DESCRIPTION = "The collection identifier, e.g 'My Pictures'";
  public static final String RECURSIVE_DESCRIPTION = "Does the parsing operation recursively. To be used in combination with -g.";
  public static final String HELP_DESCRIPTION = "Prints this message";
  public static final String ANONYMIZE_DESCRIPTION = "Anonymizes the database by removing the filename and filepath of each element in the collection";
  public static final String VERSION_DESCRIPTION = "Prints out version information";

  public static final String EXPORT_MIME = "mime type";

  public static final String EXPORT_MIME_DESCRIPTION = "All elements in the matrix will have the corresponding mimetype";

  public static final String GATHER_INPUT_TYPE_DESCRIPTION = "Use one of 'FITS' or 'TIKA', to select the type of the input files.";

  
  
  private CommandConstants() {
  }
}
