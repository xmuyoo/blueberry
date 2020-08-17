## Blueberry Collect
## Introduction

Blueberry Collect is a plug-in sub-system for crawling data from kinds of resources.

It provides a plug-in framework for each data resource.

## Supported Types of Data Resources

Totally, it supports the types of below:

- Directly Requests: Data from responses of directly requests to urls.

### Directly Requests

It has contains the data now:
- Stock Code
    - Stock code list of Shanghai Exchange and Shenzheng Exchange in China
- Stock Realtime Price
    - Data from Shanghai Exchange and Shenzheng Exchange in China
- Financial Reports 
    - Financial Reports data of companies of Shanghai Exchange and Shenzheng Exchange in China
    
## Concepts

### Collect Task

A collect task defines a runnable task of collecting data by key fields: 

- `source_url`: the data source url
- `collector_driver`: an implemented collector driver used for collecting data

In general, a `(source_url, collector_driver)` pair is unique among all the tasks, 
because when we want to collect some data, we usually have to implement a specific collector for it.

### Data Schema

A data schema defines a set of data fields, which are declared and registered by a collector.

A data schema contains a `namespace` and some `fields`:

- `namespace`: we can consider it as a data set name, fields under it should be stored isolated
- `field`: a data unit with `name`, `type` and `descrption`, which can be an indicator value or a dimension

### Collector

An runnable implementation for collecting and storing data. 

For a new kind of data, it generally needs to implement a new collector.
