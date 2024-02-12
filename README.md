# Deusbase

## Overview

**Deusbase** is a distributed key-value storage featuring an easy-to-use CRUD HTTP API and a B-Tree index. The database can store multiple values for a single key and is comprised of nodes. There are three types of nodes:
  - **Master**: Capable of performing all write/read operations and leading the replication journal if slave nodes exist. This node can operate as a standalone database, allowing clients direct access without deploying the entire cluster.
  - **Slave**: Supports read operations only. The purpose of a slave node is to process read operations while its master node handles write operations.
  - **Router**: Acts as a proxy node that redirects requests to either a master or slave node based on the hash function, request type, and cluster configuration.

The master node utilizes an eventual consistency model to replicate data to its slaves.
Typically, the database functions as a cluster that includes several shards. Each shard must have one master node and may have none or many slave nodes. To distribute data across shards, one or more routers are used.  
<p align="center">
  <img src="https://i.imgur.com/SYD9fuz.png">
</p>

 ## How to Start
 
#### Required Environment

  - Linux
  - Java 8
  - Maven 3
  - Python 3
  
#### Building

  Download the repository using `git clone https://github.com/devtritus/deusbase.git`.  
  Run `mvn install` within the *deusbase* folder. After building, the project folder *deusbase_env* will appear adjacent to the deusbase folder.
  
#### Configuring

  The *deusbase_env* folder contains a config file named *cluster_config.json*. This config file allows you to set up nodes in the cluster and their addresses.
  After setting up the cluster, you need to generate files for that cluster. Execute the command:
  ```
  python3 cluster_files_generator.py
  ```
  to generate local cluster files.
  If you wish to deploy the database on a real cluster, add the `--zip` option at the end of the command to archive each node's files into specific archives.
  An *output* directory will be generated within *deusbase_env*. The structure of folders in the *output* folder is the same as specified in *cluster_config.json*.
  
#### Testing It

  Each node folder contains a *run.sh* script. To run a node, execute `./run.sh` within the folder. To start the cluster, execute the run script for every node and for the router.
  In addition, the *output* directory contains a terminal. By default, the terminal connects to the router and can execute database commands on the cluster. For a description of commands, see [help.txt](https://github.com/devtritus/deusbase/blob/master/terminal/src/main/resources/help.txt) or type the command `help` in the terminal. Now you can use all database functions through the terminal.
  
#### Adding Data

  For testing purposes, you can download a specially prepared dataset from [imdb-names](https://sites.google.com/view/imdb-names/). Extract the .tsv file to the *deusbase_env* directory, then go to the *terminal* folder and execute the command
  ```
  ./run.sh --mode=dataset --dataset_file=../../names.imdb.tsv --row_count=500000
  ```
  This command loads 500,000 names. You can omit the `--row_count` argument to load the entire dataset. After the data is loaded, you can run the terminal with `./run.sh` and execute, for example, the command `read John Belushi` inside the terminal.

## HTTP API
#### Request Format
```
POST http://{address}:{port}/{endpoint} HTTP/1.1
Content-Type: application/json

{ "args": [ "Jean Marais", ... ] }
```
#### Response Format
```
HTTP/1.1 200 OK
Content-Type: application/json

{ 
  "code": 0,
  "data": {
    "Jean Marais": [ "Le Masque de fer", "Fantômas" ],
    ...
  } 
}
```
Supported HTTP codes: ok (200), bad_request (400), internal_server_error (500).  
Supported database codes: ok (0), not_found (1), server_error (10).

If the response contains the server_error code, then the error message can be found in `response.data['error']`.

#### Requests

|Command|Endpoint|Request Body|Description|
|---|---|---|---|
|READ|/read|`{ "args": ["Jean Marais"] }`|Finds values by exact match of the key

.|
|SEARCH|/search|`{ "args": ["Jean Ma"] }`|Finds key-value pairs by first symbols. Case-sensitive.|
|CREATE|/create|`{ "args": ["Jean Marais", "Fantomas"] }`|Creates a key and value in the database.|
|DELETE|/delete|`{ "args": ["Jean Marais", "0"] }`|Deletes a value by key. An index is used if there are multiple values for one key. The index can be obtained from the sequence of values returned by the key intended for deletion during the READ operation.|
|UPDATE|/update|`{ "args": ["Jean Marais", "0", "Le Masque de fer"] }`|Updates a value by key. An index is needed to update a specific value if multiple values exist for the key. The index can be obtained from a sequence of values returned by the key intended for deletion during the READ operation.|
|EXECUTE_REQUESTS|/execute_requests|`[{ "command": "create", "args": ["Jean Marais", "Fantomas"] },...]`|Executes a list of requests. A way to create/update many entries in batch|

## Options

The options in the table below can be added to the end of the run script command as program arguments. Some options already exist within the *@run.sh* script. To see details, open *run.sh* in a text editor. Examples of program arguments include `--generate_folders`, `--url=localhost:4005`, `-node=slave`.


|Name|Support|Default|Description|
|:---:|:---:|:---:|---|
|host|node|"localhost"|Address of the node.|
|port|node|4001|Port of the node.|
|mode|node|"master"|Selects the role of the node. Available roles: master, slave, router.|
|root_path|node|"./"|Path to the root folder of the node.|
|max_threads|node|20|Number of Jetty threads.|
|accept_queue_size|node|100|Size of the Jetty queue.|
|shard|node|optional|Subfolder for the shard if the cluster starts locally.|
|node|node|optional|Subfolder for the node if the cluster starts locally.|
|scheme|node|"default"|Name of the scheme folder that contains node files.|
|generate_folders|node|no value|Generates folders if they don't exist.|
|tree_m|master, slave|100|Number of children in a B-tree node.|
|tree_cache_limit|master, slave|5000|Capacity of the B-tree cache. Number of cached nodes.|
|journal|master|"journal.bin"|Path to the replication journal.|
|journal_batch_size|master|512 * 1024 bytes|Size of a single replication batch.|
|journal_min_size_to_truncate|master|8 * 1024 * 1024 bytes|Limit after which the journal will be truncated.|
|flush_context|master|"flush_context.json"|Path to a temp storage that stores requests while they are not yet put into the journal.|
|master_address|slave|required|Address of the master node.|
|router_config|router|"router_config.json"|Path to the router config.|
|url|terminal|"http://localhost:4001"|Address of the node to connect.|

## Performance Tests
#### Hardware

The test was performed on two laptops inside a LAN through Wi-Fi.
||**Shard 1**|**Shard 2**|
|---|---|---|
|**CPU**|Intel Core i7-8550U CPU @ 1.80GHz|Intel Core i5-4200M CPU @ 2.50GHz|
|**Disk**|*SSD* Samsung SM961|*HDD* WDC WD10JPVX-08J|

#### Cluster Config

```
{
  "router": { "host": "192.168.1.51", "port": "4001" },
  "shard_list": [
    {
      "master": { "host": "192.168.1.50", "port": "4002" }
    },
    {
      "master": { "host": "192.168.1.51", "port": "4002" }
    }
  ]
}
```

#### Test for a Single User (Thread)

![create_chart](https://i.imgur.com/61qk92u.png)
1 user * 100,000 requests, throughput 352 req/sec, average request time 2ms

![read_chart](https://i.imgur.com/LIv1NkV.png)
1 user * 100,000 requests, throughput 330 req/sec, average request time 3ms

![search

_chart](https://i.imgur.com/uu3FmPW.png)
1 user * 10,000 requests, throughput 62 req/sec, average request time 16ms

#### Test for 10 Users (Threads)

![create_chart](https://i.imgur.com/iG4oUrB.png)
10 users * 10,000 requests, throughput 1180 req/sec, average request time 8ms

![read_chart](https://i.imgur.com/num9oiG.png)
10 users * 10,000 requests, throughput 1398 req/sec, average request time 6ms

![search_chart](https://i.imgur.com/tkvT2So.png)
10 users * 10,000 requests, throughput 270 req/sec, average request time 35ms

#### Test for 10 Users (Threads) for a Single Redis Node

![create_chart](https://i.imgur.com/MBn2kSf.png)
10 users * 10,000 requests, throughput 2074 req/sec, average request time 4ms

![read_chart](https://i.imgur.com/9Llg3sF.png)
10 users * 10,000 requests, throughput 2867 req/sec, average request time 3ms

*Tested by JMeter*

#### Results

The cluster operates much faster with many clients because the router can handle clients in parallel, allowing it to load idle resources.  
Redis is approximately twice as fast as Deusbase for these tests.

## Problems

- The node index lacks a procedure to clean unused blocks on the disk, so the index file's size will always grow, even if data is deleted.
- Nodes in the cluster cannot migrate their data to another node. Currently, there is no way to change the cluster configuration after data has been added.
- The database does not support any security protocols.
- The database only supports string keys and values, but it is possible to make values a byte sequence. To do this, you need to extend the database API.
- If a master node is lost, its shard will be available only in read-only mode until the master comes online again.

## Used Dependencies

- Jetty Server 9.4.25
- Apache HTTPClient 4.5.10
- Jackson 2.10.1
- Logback 1.2.3

## Conclusion
As a result, we have developed a distributed database with quite fast data access. Master-slave replication works through batch loading and minimizes the time required for node synchronization. It is sufficient to solve many data storage problems. During development, I loaded a dataset with 22 million entries into the database and achieved a good reading speed. The database is single-threaded internally, but this does not degrade the performance of request execution. Some issues prevent the use of this database in production, but it is sufficient to understand the features of key-value storage. I tried to use a minimum number of dependencies, gaining valuable experience in low-level coding. Extensive experience was gained in byte transfer operations, architecture evolution, MVP approach, I/O operations, and the encapsulation of algorithms from the physical representation of storage.

## References
- [Database Course (2012) by Ilya Teterin](https://www.lektorium.tv/course/22894)  
- [Database Course. Slides](http://ya-pulser.github.io/static/dbcourse.2012/index.html)  
- [B-tree Wiki](https://en.wikipedia.org/wiki/B-tree)
