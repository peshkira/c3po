/*******************************************************************************
 * Copyright 2013 Petar Petrov <me@petarpetrov.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.petpet.c3po.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TestConfigGenerator {
  
  private static final int BUFFER_SIZE = 1024;
  
  public void backupRealUserConfigFile() {
    File conf = new File(Configurator.USER_PROPERTIES);
    if (conf.exists() && conf.isFile()) {
      conf.renameTo(new File(Configurator.USER_PROPERTIES + ".bak"));
    }
  }

  public void copyTestConfigFile() {
    InputStream inStream = null;
    OutputStream outStream = null;

    try {

      final File source = new File("src/test/resources/nondefaultconfig");
      final File target = new File(Configurator.USER_PROPERTIES);

      inStream = new FileInputStream(source);
      outStream = new FileOutputStream(target);

      final byte[] buffer = new byte[BUFFER_SIZE];

      int length;
      // copy the file content in bytes
      while ((length = inStream.read(buffer)) > 0) {
        outStream.write(buffer, 0, length);
      }
      inStream.close();
      outStream.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
  
  public void restoreUserConfigFile() {
    File old = new File(Configurator.USER_PROPERTIES);
    old.delete();
    
    File bak = new File(Configurator.USER_PROPERTIES + ".bak");
    bak.renameTo(new File(Configurator.USER_PROPERTIES));
    
  }
}
