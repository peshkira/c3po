[![Build Status](https://travis-ci.org/peshkira/c3po.png)](https://travis-ci.org/peshkira/c3po)

C3PO
===================================================

Clever, Crafty, Content Profiling of Objects (c3po) is a software tool prototype, which uses FITS generated data of a digital collection as input
and generates a profile of the content set in an automatic fashion. It is designed in a way so that different meta data formats originating from
other tools can be easily integrated. The tool follows the proposed three part profiling process and provides facilities for data export and further
analysis of the content, such as helpful visualisations of the meta data characteristics, partitioning of the collection into homogeneous sets
based on any known characteristic. For each chosen partition of the content, a special machine-readable profile can be generated that contains aggregations
and distributions for many of the properties. The profile optionally contains the set of chosen representative samples as well as their identifiers
within a content repository and a list of all objects that fall into the particular partition. A machine-readable content profile conforming to such
a specific format plays an important role for integration with a planning component, content repositories and monitoring systems and thus for the
automation of the entire cycle of planning and operations.

In order to support the decision making c3po makes use of different algorithms that choose a small set of sample records (up to 100)
based on the size of objects, the distribution of specific characteristics, or other common features.
Currently there are three algorithms: one based on size statistics, one based on systematic sampling and one
that searches for a subset with similar distribution of preselected properties.

Releases
------------------------
Please refer to [this](https://github.com/peshkira/c3po/wiki/Downloads)

Setup
------------------------
Please refer to [this](https://github.com/peshkira/c3po/wiki/Setup-Guide) guide.

Development
------------------------
Please refer to [this](https://github.com/peshkira/c3po/wiki/Development-Guide) guide.

Screenshot
------------------------
![Collection Overview](https://dl.dropbox.com/u/8290338/blog/c3po_overview.png "Collection Overview")

More Information
------------------------
You can find more information in the following links:
- [Website](http://ifs.tuwien.ac.at/imp/c3po)
- [Blog Post](http://www.openplanetsfoundation.org/blogs/2012-11-19-c3po-content-profiling-tool-preservation-analysis)
- [Screencast](https://vimeo.com/53069664)
