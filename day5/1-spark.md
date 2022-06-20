---
marp: true
theme: gaia
paginate: true
color: brown;
backgroundColor: white
header: Scala course
footer: Scala & Spark
---
![bg](background.jpg)

# Spark overview

---
![bg](background.jpg)

## Spark and Databricks

- Databricks provide open source data engineering tools
- Cloud Platform Service provided by AWS, Azure
- Lakehouse: Data warehouse compatible with acid transactioons

---
![bg](background.jpg)

## Brief history

Before Spark, there was Hadoop:

- HDFS: Distributed **Storage system**)
- Map / Reduce: parallel **processing** of small tasks and then aggregate the results
-  Each machine was not required to be highly performant
-  The processing is Fault tolerant
-  Support for unstructured data
-  Scalable

---
![bg](background.jpg)

## Hadoop architecture

- Native Hadoop **scheduler**: YARN (Yet another resource negociator)
  - Job Scheduling: divide tasks into smaller one, internal monitoring
  - Resource Manager
- Map: break records, computing, sharding to get Key, value pairs -> sent to Reducers
- Reducer: sort, shuffle, reduce, output

HDFS: Name node (server ~> metadata) vs Data node (slave ~> data)

---
![bg](background.jpg)

## Hadoop ecosystem

- Hive, Pig
- HBase
*VS / in combination with*
- Beam
- Cassandra
- Airflow 
- Kafka & Kafka streams

Many spark connectors !

---
![bg](background.jpg)

## What is Spark ?

- Spark is a computation **engine**
- Relies on the JVM
- Spark relies on a distributed file system (HDFS, DBFS, ...)
- Driver: build DAG (Direct acyclic graph), Web UI
- Master (cluster management) vs Workers (slaves: map / reduces)
- Cluster manager (Yarn, Mesos, Kubernetes) vs native cluster

---
![bg](background.jpg)

## Spark architecture

- Can be run in several modes
  - Client mode local (one JVM)
  - Client mode Standalone (multiple worker. Not necessary local)
  - Cluster mode (the driver is inside a worker node)
    - Example case where we submit from our computer that is far from the workers

Remark: In client mode: we can use local file system.

---
![bg](background.jpg)

## Spark software

Software:
- Spark core
- Spark SQl
- Spark Streaming
- Spark ML (iteration)
- GraphX (based on RDD). See GraphFrame too based on Dataframe


---
![bg](background.jpg)

## RDD (Resilient Distributed Dataset)

- Scala collection that is:
  - Partitionned 
  - Split over several workers for parallel processing
- Java code is genrated on the fly (implies error at Runtime !)

The RDDs are low data level of representation for Spark.

We prefer Dataframe / Dataset

---
![bg](background.jpg)

## Dataframe / Dataset

- A Dataframe is a specific Dataset: ```Dataset[Row]```
- But a Dataframe is not like a Dataset !
  - The Dataframe API is not the same as the Dataset API
  - A dataset relies on case classes
- Catalyst optimization available (not done with RDD)

---
![bg](background.jpg)

## Libraries version management

There are constraints:
- Databricks constraints
  - Example: https://docs.databricks.com/release-notes/runtime/9.1.html
- Constaints between Spark and Scala
  - Scala 2.12 with Spark 3.0, Delta 
  - Scala 3 can be run over Spark 3 (but not available for Databricks)

---
![bg](background.jpg)

## Libraries version management

Be careful with:
- Provided libraries:
  - log4j (1.2.x in databricks)
  - spark, delta
- Java version (1.7 vs 11 or later according to the cluster version)

- Maybe there is need to install jar on the cluster (sparkMeasure project)
  - Not so obvious to activate for Databricks security reason

---
![bg](background.jpg)

## Environment management

- When running a Spark version in local mode:
  - Pay attention to '/' character (Linux convention) for Windows user 
    - Use Java FileSystems aand Path resolve method for building paths
  - Use Input format (UTF8)
  - Prefix file system
    - 'dbfs/' required for Java File system 
    - 'dbfs:' is optional for Spark

