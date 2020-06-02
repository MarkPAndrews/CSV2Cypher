# **CSV2Cypher**
This application is used to convert csv files into cypher scripts for ingestion into Neo4j.  It also generates POJO object classes and a Swagger json file. mapping.json controls what files are used and how they are mapped.

###Example run configuration:

```
Program Arguments: C:\git\CSV2Cypher\src\main\resources\mapping.json
Working Path: C:\git\CSV2Cypher\import
```
 
 Use `run_loader.sh` to execute the generate cypher files