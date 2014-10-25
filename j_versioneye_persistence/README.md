j_versioneye_persistence
========================

This is the java implementation of versioneyes persistence layer. This project is used in VersionEyes crawling framework.

After check out run this: 

```
mvn compile
```

to see if you can compile it. You should have installed Maven3 and at least Java 6 JDK on you machine. 

This project contains some domain models and the DAOs to store the domain models to the Database. The domain models you will find in the package `versioneye/domain`. And in `versioneye/persistence` you will the interfaces for the DAOs. Currently the DAO interfaces are implemented for MongoDB. You will find the implementation in `versioneye/persistence/mongodb`. 

## Tests 

For testing we are using TestNG. All tests can be executed via Maven. 

``` 
mvn test
```

## Install 

If you do some changes to this project than document all your changes in changelog.md before counting up the version number. To install this package into your local maven repository use this command: 

```
mvn clean install
```

## IDEs 

It's up to you which IDE you wanna use to edit this project. You can generate IDE metafiles with maven, but don't push it back to ther git server! 

This here will generate metafiles for IntelliJ Idea. The most advanced IDE for Java projects. 

```
mvn idea:idea
```
If you like to deal with plugin conflicts you should use eclipse ;-) 

```
mvn eclipse:eclipse
```
Good luck. 
  
