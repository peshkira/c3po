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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.FileAppender;

/**
 * A log file appender.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class C3POFileAppender extends FileAppender {
  private static final String LOG_PATH = "logs/";

  private static final String LOG_NAME = "c3po_";

  private static final String LOG_EXT = ".log";

  public C3POFileAppender() {
    final SimpleDateFormat formatter = new SimpleDateFormat( "yyyyMMdd" );
    this.setFile( C3POFileAppender.LOG_PATH + LOG_NAME + formatter.format( new Date() ) + LOG_EXT );
  }
}
