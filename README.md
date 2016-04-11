# C3PO: Clever, Crafty Content Profiling of Objects

Analyze your content with C3PO.

## How to install and use

### Requirements

To install you need:

* Java 1.7
* MongoDB 2.0.5 or higher (64 Bit)
* FITS (optional)

To run the Web application, you also need:

* Some Application Server (Tomcat/Jetty/JBoss 7), or
* Play Framework 2.3.0 or higher

### Download

At the moment there are no precompiled versions available.

### Install and Usage instructions

!UPDATE! Now C3PO is based on Play Framework 2.3.0. Installation instructions of the web-app are available at [Play Framework Documentation](https://www.playframework.com/documentation/2.3.x/Production).
Once Play Framework and MongoDB is installed, navigate to *c3po/c3po-webapi/* and execute
```
activator start
```
to run the app.

For the usage instructions, please refer to [Usage Guide](https://github.com/peshkira/c3po/wiki/Usage-Guide). Beware: the installation manual is outdated.

### Troubleshooting

If you encounter any problems, please let us know by submitting an issue [here](https://github.com/datascience/c3po/issues?state=open).

### Licence

C3PO is released under [Apache version 2.0 license](LICENSE.txt).

### Acknowledgements

Part of this work was supported by the Vienna Science and Technology Fund (WWTF) through project ICT12-046 (BenchmarkDP) and by the 7th Framework Program, IST, through the SCAPE project, Contract 270137.

### Support

This tool is supported by the [Open Planets Foundation](http://www.openplanetsfoundation.org). 

## Features and roadmap

### Version 0.4

* Visualization, filtering through the Web Application
* Representative sample set generation

### Roadmap

* Conflict resolution
* A controlled vocabulary for properties
* Templating mechanism 

