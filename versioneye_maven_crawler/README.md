# VersionEye Maven Crawler

This is one of VersionEyes Crawlers implemented as maven plugin. This crawler is crawling Maven Repositories. 
Install it like this: 

```
mvn clean install 
```

To get a list of all possible goals type in:

```
mvn crawl:help
```

To crawl the maven central repository run this:

```
mvn crawl:central
```

To crawl only the popular projects in the database run this:

```
mvn crawl:popular
```

And to crawl only one specific project, you specified in your pom in the configuration section, run this:

```
mvn crawl:single
```

Easy! Right? 
