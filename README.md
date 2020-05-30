# Deusbase

## Overview

**De**vtrit**usbase** is distributed key-value storage with easy CRUD HTTP API and B-Tree index under the hood. Database can store multiple values by one key. Database consists from nodes.
There are 3 types of the node:
  - Master - can do all write/read operations and lead replication journal if slave nodes exist. This node can run as standalone database and clients can use direct access to it without deploying all cluster. 
  - Slave - allows only read operations. The Destination of a slave node is to process read operations while his master node processes write operations.
  - Router - a proxy node that can redirects request to another master or slave node depend on hash function within it, type of request and cluster configuration.

The master use eventual consistency model to replicate data to its slaves.
In common case database is a cluster that include some shards. Every shard must include one master node and none or many slave nodes. To distribute data across shard one or more routers are used.  
<p align="center">
  <img src="https://i.imgur.com/SYD9fuz.png">
</p>

 ## How to start
 
#### Required environment

  - Linux
  - Java 8
  - Maven 3
  - Python 3
  
#### Building

  Download repository `git clone https://github.com/devtritus/deusbase.git`.  
  Invoke `mvn install` within *deusbase* folder. After building the project folder *deusbase_env* will appear near by deusbase folder.
  
#### Configuring

  *deusbase_env* folder contains config file with the name *cluster_config.json*. The config allows to set up nodes in the cluster and their addresses.
  After set up cluster you need to generate files of that cluster. To do that, execute command:
  ```
  python3 cluster_files_generator.py
  ```
  to generate local cluster files.
  If you want to deploy the database on a real cluster, add `--zip` key to the end of the command. It archive every node files to specific archive.
  *output* directory will be generated within *deusbase_env*. Structure of folders in *output* folder the same as specify in *cluster_config.json*.
  
#### Test it

  Every node folder contains *run.sh* script. To run a node, just execute `./run.sh` within the folder. To start cluster, execute run script for every node and for router.
  Besides them *output* directory contains terminal. By default terminal connects to router and can execute database commands on the cluster. To get a description of commands see [help.txt](https://github.com/devtritus/deusbase/blob/master/terminal/src/main/resources/help.txt) or type command `help` in the terminal. Now you can use all database functions through the terminal.
  
#### Add data

  For testing goals you can download specially prepared dataset from [imdb-names](https://sites.google.com/view/imdb-names/). Extract .tsv file to *deusbase_env* directory, after go to *terminal* folder and execute command
  ```
  ./run.sh --mode=dataset --dataset_file=../../names.imdb.tsv --row_count=500000
  ```
  This command load 500000 names. You can remove `--row_count` argument to load all dataset. After data is loaded you can run the terminal and test ability of the database.

## HTTP API
#### Request format
```
POST http://{address}:{port}/{endpoint} HTTP/1.1
Content-Type: application/json

{ args: [ "Jean Marais", ... ] }
```
#### Response format
```
HTTP/1.1 200 OK
Content-Type: application/json

{ 
  code: 0
  data: {
    "Jean Marais": [ "Le Masque de fer", "Fantomas" ],
    ...
  } 
}
```
Supported HTTP codes: ok(200), bad_request(400), internal_server_error(500).  
Supported database codes: ok(0), not_found(1), server_error(10).

If response contains server_error code then error message can be found in `response.data['error']`.

#### Requests

|Command|Endpoint|Request body|Description|
|---|---|---|---|
|READ|/read|`{ args: ["Jean Marais"] }`|Find values by exact match of key.|
|SEARCH|/search|`{ args: ["Jean Ma"] }`|Find key-values pair by first symbols. Case-sensitive.|
|CREATE|/create|`{ args: ["Jean Marais", "Fantomas"] }`|Create key and value in the database.|
|DELETE|/delete|`{ args: ["Jean Marais", "0"] }`|Delete value by key. Index is used if there are many values by one key. Index can be get from sequence of values that return by key intended to delete during READ operation.|
|UPDATE|/update|`{ args: ["Jean Marais", "0", Le Masque de fer] }`|Update value by key. Index are needed for update a concrete value if many values are available by the key. The index can be get from a sequence of the values that are returned by the key intended to delete during READ operation.|
|EXECUTE_REQUESTS|/execute_requests|`[{ command: "create", args: ["Jean Marais", "Fantomas"] },...]`|Execute a list of requests. The way to creating/updating many entries by batch|

## Options

The options in the table below can be added to the end of the run script command as program arguments. Some options already exist within *@run.sh* script. To see details, open run.sh in the text editor. The examples of program arguments: `--generate_folders`, `--url=locahost:4005`,`-node=slave`


|Name|Support|Default|Description|
|:---:|:---:|:---:|---|
|host|node|"localhost"|Address of node.|
|port|node|4001|Port of node.|
|mode|node|"master"|Select a role of node. Available roles: master, slave, router.|
|root_path|node|"./"|Path to root folder of the node.|
|max_threads|node|20|Count of Jetty threads.|
|accept_queue_size|node|100|Size of Jetty queue.|
|shard|node|optional|Subfolder for shard if cluster starts locally.|
|node|node|optional|Subfolder for node if cluster starts locally.|
|scheme|node|"default"| Name of scheme folder that contains node files.|
|generate_folders|node|no value|Generate folders if they aren't exist.|
|tree_m|master, slave|100|Count of children in b-tree node.|
|tree_cache_limit|master, slave|5000|Capacity of b-tree cache. Count of cached nodes.|
|journal|master|"journal.bin"|Path to replication journal.|
|journal_batch_size|master|512 * 1024 bytes|Size of single replication batch.|
|journal_min_size_to_truncate|master|8 * 1024 * 1024 bytes|Limit after that journal will be truncated.|
|flush_context|master|"flush_context.json"|Path to a temp storage that store request while it doesn't put to journal.|
|master_address|slave|required|Address of master node.|
|router_config|router|"router_config.json"|Path to router config.|
|url|terminal|"http://localhost:4001"|Address of node to connect.|

## Performance tests

## Problems

- The node index doesn't have a procedure to clean unused blocks on the disk so the size of the index file will always grow even if data was deleted.
- The nodes in the cluster can't migrate their data to another node. At the moment there is no way to change the cluster configuration after any data was added to.
- Database doesn't support any security protocols 

## Used dependencies

- Jetty Server 9.4.25
- Apache HTTPClient 4.5.10
- Jackson 2.10.1
- Logback 1.2.3

## Conclusion
As the result we got a distributed database with a quite fast data access. Master-slave replication works through the batch loading and allow minimize time of nodes syncronization. It is enough to resolve many problems of the data storage. During development I loaded to database a dataset with 22 millions entries and got a good reading speed. The database is single threaded inside but it don't degrade the speed of the execution the requests. Some problems don't allow to use this database in the production, but it's enough to understand features of key-value storage. I tried to use a minimum number of dependencies so I get a good experience of the low level coding. Wide experience was got in the bytes transfering operations, evolution of architecture, MVP approach, I/O operations and encapsulation of algorithms from physical representation of storing.
