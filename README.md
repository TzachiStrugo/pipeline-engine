# Pipeline Engine

## Table of Contents
- [Overview](#Overview)
- [Architecture](#Architecture)
   - [File Observer Service](#File observer service)
   - [File Data Processing Service](#File Data Processing Servic)
   - [File EventProcessing Service](#File Event Processing Servic)
- [Persistence](#Persistence)
- [Running](#Running)

## Overview
Pipeline Engine is a Java-based program built on the Spring Boot ecosystem that allows you to stream files 
from directory data sources to Cassandra as a sink data source. The engine supports two types of processing: 
data processing for large files that should be processed in distributed services using Spark for better scalability 
and performance, and event processing for small files.

## Architecture

Generated with ChatGpt :-)

                                        +---------------------+
                                        |     Directory       |
                                        |     Data Source     |
                                        +----------^----------+
                                                   |
                                                   |
                                                   v
                    +--------------------------------------------------------------+
                    |                     File Observer Service                    |
                    |  +------------------------+      +-------------------------+ |
                    |  |    Producer Worker     |      |      Observer Worker    | |
                    |  +------------------------+      +-------------------------+ |
                    |                                                              |
                    +--------------------------------------------------------------+
                                                   |
                                                   |                      
                                                   v                       
                                  +----------------------------------+    
                                  |              Kafka               |    
                                  |          Message Broker          |    
                                  +----------------------------------+    
                                       |                       |
                                       |                       |
                                       v                       v
                              +------------------+    +------------------+
                              |      event       |    |      Data        |
                              |    Processing    |    |    Processing    |
                              +------------------+    +------------------+
                                       |                       |
                                       |                       |
                                       v                       v
                                  +----------------------------------+    
                                  |              Kafka               |    
                                  |          Message Broker          |    
                                  +----------------------------------+    
                                                   |
                                                   |                      
                                                   v    
                                  +----------------------------------+    
                                  |        Data Persistence          |    
                                  |           Cassandra              |    
                                  +----------------------------------+

### File Observer Service 
The file observer component consists of workers that utilize the Java WatchService to register relevant directories as data sources.
They create jobs via an internal queue for each worker, which reads the job containing the file name and sends
it to the relevant topic based on the configuration. It's important to note that at this stage, 
only the file name is passed and not the file itself. Each processing unit reads the file and performs the
necessary processing.

### File Data Processing Service 

### File Event Processing Service


### Data Persists Service 



## Persistence
2. cassandra
2. scheme migration as part of docker file location n
3. schema erd


## Requirements
1. Java 8 or above
2. Docker Engine



<b>Improvements</b>
- Storage type can be local but to support better scalability and fault tolerance implementation should change to one of the follows:
  - distribute file system (HDFS)
  - cloud object storage
- Large content file
  - Consider to send pointer to file as event header (and not file content)
  - use Kafka compress mechanizm
  - compression / columnar storage apache parquet 
- Producer 
  - consider to bu

