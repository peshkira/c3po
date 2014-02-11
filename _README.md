# C3PO: Clever, Crafty Content Profiling of Objects

Analyze your content with C3PO.

### What does C3PO do?

C3PO – or ‘Clever, Crafty, Content Profiling of Objects’ is a software tool, which uses metadata extracted from files of a digital collection as input to generate a profile of the content set. The tool transforms the data for faster and scalable analysis and stores it, then post-processing solves issues like conflict resolution and provides a machine-readable overview, and a web application enables the user to filter and explore any part of the data further.

### What are the benefits for the end user?

C3PO brings you the following benefits:

* In-depth knowledge of your files
* Scalability – analyse large amounts of data in no time
* Visualization of metadata characteristics
* Easy integration with [PLATO](http://ifs.tuwien.ac.at/dp/plato/) for preservation planning
* Easy integration with [SCOUT](http://openplanets.github.io/scout/) for monitoring preservation risks

### Who is the intended audience?

C3PO is for:

* Content holders
* Preservation experts
* Institutions that would like to learn more about content they have

## Features and roadmap

### Version 0.4

* Xml-based content profile generation
* Visualization, filtering throught the Web Application
* Supports [FITS](http://projects.iq.harvard.edu/fits) and [Apache TIKA](https://tika.apache.org/) meta data
* Conflict resolution using [Drools Rule Framework](https://www.jboss.org/drools/)
* Representative sample set generation
* Support for REST API (Scout)

### Roadmap

* Introduction of a controlled vocabulary for properties
* Implement HBASE backend
* Implement Data Connector API

## How to install and use

### Requirements

To install you need:

* Java 1.6
* MongoDB 2.0.5 or higher (64 Bit)
* FITS (optional)

To run Web application, you also need:

* Some Application Server (Tomcat/Jetty/JBoss 7), or
* Play Framework

### Download

At the moment there are no precompiled versions available.

### Install and Usage instructions

Please refer to [Usage Guide](https://github.com/peshkira/c3po/wiki/Usage-Guide).

### Troubleshooting

If you encounter any problems, please let us know by submitting an issue [here](https://github.com/peshkira/c3po/issues?state=open).

## More information

### Publications

* [To fits or not to fits](http://www.openplanetsfoundation.org/blogs/2012-07-27-fits-or-not-fits)
* [C3PO: a content profiling tool for preservation analysis](http://www.openplanetsfoundation.org/blogs/2012-11-19-c3po-content-profiling-tool-preservation-analysis)
* [C3PO is ready for you!](http://www.openplanetsfoundation.org/blogs/2013-05-20-c3po-ready-you)

### Licence

C3PO is released under [Apache version 2.0 license](LICENSE.txt).

### Acknowledgements

Part of this work was supported by the European Union in the 7th Framework Program, IST, through the SCAPE project, Contract 270137.

### Support

This tool is supported by the [Open Planets Foundation](http://www.openplanetsfoundation.org). 

## Develop

[![Build Status](https://travis-ci.org/openplanets/scape.png)](https://travis-ci.org/openplanets/scape)

Please read this [Dev Guide](https://github.com/peshkira/c3po/wiki/Development-Guide). You can find the [JavaDocs](http://peshkira.github.io/c3po/apidocs/index.html) here.

### Contribute

1. [Fork the GitHub project](https://help.github.com/articles/fork-a-repo)
2. Change the code and push into the forked project
3. [Submit a pull request](https://help.github.com/articles/using-pull-requests)

To increase the changes of you code being accepted and merged into the official source here's a checklist of things to go over before submitting a contribution. For example:

* Has unit tests (that covers at least 80% of the code)
* Has documentation (at least 80% of public API)
* Agrees to contributor license agreement, certifying that any contributed code is original work and that the copyright is turned over to the project
