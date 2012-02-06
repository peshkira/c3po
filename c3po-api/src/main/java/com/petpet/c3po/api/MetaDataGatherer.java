package com.petpet.c3po.api;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface MetaDataGatherer {

  void setConfig(Map<String, String> config);

  long getCount();

  List<InputStream> getNext(int count);

  List<InputStream> getAll();
}
