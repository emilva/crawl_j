# VersionEye Maven Crawler

This project contains different crawlers which are mostly running as microservices. Most of the goals here are running as RabbitMQ workers on the VersionEye infrastructure! 

Install the project like this: 

```
mvn clean install 
```

To get a list of all possible goals type in:

```
mvn crawl:help
```

To run the process which can crawl a specific Artefact via it's Maven HTML representation run this command: 

```
mvn crawl:html_worker
```

The process to crawl a specific Artefact via the Aether project can be started like this:

```
mvn crawl:maven_index_worker
```

Easy! Right? 

For commercial support contact `support [at] versioneye.com`. 
