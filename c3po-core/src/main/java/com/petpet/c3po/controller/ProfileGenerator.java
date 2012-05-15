package com.petpet.c3po.controller;


public class ProfileGenerator {

//  private static final Logger LOG = LoggerFactory.getLogger(ProfileGenerator.class);
//
//  private DigitalCollection coll;
//
//  private PreparedQueries queries;
//
//  private List<Property> expanded;
//
//  public ProfileGenerator(String coll, List<String> expanded, PreparedQueries queries) {
//    this.queries = queries;
//    this.init(coll, expanded);
//  }
//
//  private void init(String coll, List<String> expanded) {
//    try {
//      this.coll = this.queries.getCollectionByName(coll);
//      this.expanded = Helper.getPropertiesByNames(expanded.toArray(new String[expanded.size()]));
//    } catch (NoResultException e) {
//      LOG.warn("No collection found for name: {}", coll);
//    }
//  }
//
//  public void write(String xml) {
//    try {
//      Document doc = DocumentHelper.parseText(xml);
//      this.write(doc);
//
//    } catch (DocumentException e) {
//      e.printStackTrace();
//    }
//  }
//
//  public void write(Document doc) {
//    this.write(doc, "profiles/output.xml");
//  }
//
//  public void write(Document doc, String path) {
//    try {
//      OutputFormat format = OutputFormat.createPrettyPrint();
//      File file = new File(path);
//      LOG.info("Will create profile in {}", file.getAbsolutePath());
//      if (file.getParentFile() != null && !file.getParentFile().exists()) {
//        file.getParentFile().mkdirs();
//
//      }
//
//      file.createNewFile();
//
//      XMLWriter writer = new XMLWriter(new FileWriter(path), format);
//      writer.write(doc);
//      writer.close();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }
//
//  public Document generateProfile() {
//    if (this.coll != null) {
//      return this.generateProfile(coll);
//    } else {
//      LOG.error("No collection was provided, aborting... Please configure the tool before execution");
//    }
//
//    return null;
//  }
//
//  private Element createRootElement(Document doc, DigitalCollection coll) {
//    Element collpro = doc.addElement("collection-profile").addAttribute("date", new Date().toString());
//    return collpro;
//  }
//
//  private Element createCollectionElement(Element profile, List<Property> properties) {
//    int props = properties.size();
//
//    Element collection = profile.addElement("collection").addAttribute("name", coll.getName())
//        .addAttribute("elements", coll.getElements().size() + "").addAttribute("properties", "" + props);
//
//    return collection;
//  }
//
//  private Element createPropertiesElement(Element collection) {
//    return collection.addElement("properties");
//  }
//
//  private void generatePropertyElements(Element properties, List<Property> props) {
//    for (Property p : props) {
//      LOG.debug("adding property {} to profile", p.getName());
//      List<Object[]> distr = this.queries.getSpecificPropertyValuesDistribution(p.getName(), coll);
//
//      if (!distr.isEmpty()) {
//        String mode = (String) distr.get(0)[1];
//        long count = this.queries.getElementsWithPropertyCount(p.getName(), coll);
//        Element property = properties.addElement("property").addAttribute("id", p.getName())
//            .addAttribute("name", p.getHumanReadableName()).addAttribute("type", p.getType().name())
//            .addAttribute("count", count + "").addAttribute("mode", mode);
//
//        this.processPropertyElement(property, p);
//
//        if (this.expanded.contains(p)) {
//          this.generateExpandedPropertyElement(property, distr);
//
//        } else {
//          this.generateCollapsedPropertyElement(property);
//        }
//
//      } else {
//        LOG.warn("Values for property '{}' have conflicts, excluding property...", p.getName());
//      }
//    }
//  }
//
//  private void processPropertyElement(Element property, Property p) {
//    PropertyType type = p.getType();
//    switch (type) {
//      case NUMERIC:
//        double avg = queries.getAverageOfNumericProperty(p.getName(), this.coll);
//        long min = queries.getMinOfNumericProperty(p.getName(), this.coll);
//        long max = queries.getMaxOfNumericProperty(p.getName(), this.coll);
//        long sum = queries.getSumOfNumericProperty(p.getName(), this.coll);
//        property.addAttribute("avg", "" + avg);
//        property.addAttribute("min", "" + min);
//        property.addAttribute("max", "" + max);
//        property.addAttribute("sum", "" + sum);
//
//        break;
//
//      default:
//        break;
//    }
//
//  }
//
//  private void generateExpandedPropertyElement(Element property, List<Object[]> distribution) {
//    property.addAttribute("expanded", "true");
//
//    for (Object[] d : distribution) {
//      property.addElement("item").addAttribute("value", (String) d[1]).addAttribute("count", ((Long) d[2]).toString());
//    }
//  }
//
//  private void generateCollapsedPropertyElement(Element property) {
//    property.addAttribute("expanded", "false");
//  }
//
//  private Document generateProfile(DigitalCollection coll) {
//    LOG.info("generating profile for collection '{}'", coll.getName());
//    List<Property> allProps = this.queries.getAllPropertiesInCollection(coll);
//    Collections.sort(allProps, new PropertyComparator());
//
//    Document document = DocumentHelper.createDocument();
//    Element profile = this.createRootElement(document, coll);
//    Element collection = this.createCollectionElement(profile, allProps);
//    Element properties = this.createPropertiesElement(collection);
//
//    this.generatePropertyElements(properties, allProps);
//
//    return document;
//  }
}
