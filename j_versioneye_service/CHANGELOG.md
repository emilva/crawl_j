# Changelog

## 2.1.2 / 2014.03.08

 * BugFix: BugFix for LicenseService. Preventing creating licenses with empty names.

## 2.1.1 / 2013.12.16

 * Update: Using the newest version of the versioneye persistence jar.

## 2.1.0 / 2013.08.19

 * Add: Adding LicenseService

## 2.0.0 / 2013.08.12

 * Update: Changed DependencyService.updateKnownStatus. In case that a clojure dep can not be found look up for Java package.
 * Remove: Removed ProductService.versionExistAlready. Use instead of that the equivalent from ProductDao. This can break your build!

## 1.0.6 / 2013.08.12

 * Update: Changed log out put.

## 1.0.5 / 2013.08.11

 * Update: Remove unused dependencies to maven indexer and aehter

## 1.0.4 / 2013.08.10

 * Update: Update ArchiveService.createArchive.
 * Update: Update to newest version of versioneye-persistence

## 1.0.3 / 2013.08.05

 * Update: Updated the ProductService. Checking if repository is null
 * Update: Updated to the newest versioneye-persistence

## 1.0.2 / 2013.07.29

 * Update: Updated to newest version of versioneye-persistence

## 1.0.1 / 2013.07.28

 * Update: Updated to newest version of versioneye-persistence
