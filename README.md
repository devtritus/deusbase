# Deusbase

## Overview

Deusbase is distributed key-value storage with easy CRUD HTTP API and B-Tree index under the hood. Database can store multiple values by one key. Database consists from nodes.
There are 3 types of the node:
  - Master - can do all write/read operations and lead replication journal if slave nodes exist. This node can run as standalone database and clients can use direct access to it without deploying all cluster. 
  - Slave - allows only read operations. The Destination of a slave node is to process read operations while his master node processes write operations.
  - Router - a proxy node that can redirects request to another master or slave node depend on hash function within it, type of request and cluster configuration.

The master use eventual consistency model to replicate data to its slaves.
In common case database is a cluster that include some shards. Every shard must include one master node and none or many slave nodes. To distribute data across shard one or more routers are used. 

 ## How to start
 
#### Required environment

  - Linux
  - Java 8
  - Maven 3
  - Python 3
  
#### Building

  1. Download repository `git clone https://github.com/devtritus/deusbase.git`.
  2. Invoke `mvn install` within *deusbase* folder. After building the project folder *deusbase_env* will appear near by deusbase folder.
  
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
  ./run.sh --mode=dataset --dataset_file=../../names.imdb.tsv --row_count=100000 --url=http://localhost:4005
  ```
  After data is loaded you can test ability of the database.

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
|---------------|---|---|---|
|READ|/read|`{ args: ["Jean Marais"] }`|Find values by exact match of key.|
|SEARCH|/search|`{ args: ["Jean Ma"] }`|Find key-values pair by first symbols. Case-sensitive.|
|CREATE|/create|`{ args: ["Jean Marais", "Fantomas"] }`|Create key and value in the database.|
|DELETE|/delete|`{ args: ["Jean Marais", "0"] }`|Delete value by key. Index is used if there are many values by one key. Index can be get from sequence of values that return by key intended to delete during READ operation.|
|UPDATE|/update|`{ args: ["Jean Marais", "0", Le Masque de fer] }`|Update value by key. Index are needed for update a concrete value if many values are available by the key. The index can be get from a sequence of the values that are returned by the key intended to delete during READ operation.|

## Node settings

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
