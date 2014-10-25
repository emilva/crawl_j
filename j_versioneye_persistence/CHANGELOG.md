# Changelog

## 2.0.3-SNAPSHOT - DEV

## 2.0.2 / 2013.12.16

 * Update: Updated ProductRecourceDao.updateCrawledForGithub. Optimize the error logging.

## 2.0.1 / 2013.08.24

 * Update: Updated ProductResourceDao.updateCrawledForGithub, writing now the language to DB and removing the github prefix in prod_key.

## 2.0.0 / 2013.08.19

 * Remove: Removed ProductDao.updateLicense! Breaking change!

## 1.7.0 / 2013.08.11

 * New: Added Model, DAO and Test for MavenRepository.

## 1.6.0 / 2013.08.10

 * New: VersionArchive.doesArchiveExistArleady(String language, String prod_key, String version, String name, String url)

## 1.5.0 / 2013.08.10

 * New: VersionArchive.removeArchive(String language, String prod_key, String version, String name)

## 1.4.1 / 2013.08.09

 * BugFix: Product.followers was not set by loading new products from DB. Added BugFix and Tests!

## 1.4.0 / 2013.08.09

 * New: Added ProductDao.getUniqueFollowedJavaIds(). Returns all IDs from Java Products which have followers.

## 1.3.0 / 2013.08.05

 * New: Added LicenseDao.existAlready.

## 1.2.0 / 2013.07.29

 * New: Added License Model & LicenseDao & LicenseDaoTest.

## 1.1.0 / 2013.07.28

 * New: Added new method productDao.getByKey(String language, String groupId, String artifactId)
 * Extend: dependencyDao.existAlready(String language, String prodKey, String prod_version, String depProdKey, String version)
 * New: Added new method dependencyDao.deleteDependencies(String language, String prodKey, String prod_version)

## 1.0.1 / 2013.07.27

 * BugFix: Product.updateFromDbObject(DBObject object) is now setting the language on the model

## 1.0.0 / 2013.07.27

 * Renamed: NotificationDao.createForFollower to NotificationDao.createNotification
 * Refactored: NotificationDao.getBy(String userId, String productId, String versionId) to NotificationDao.getBy(ObjectId userId, ObjectId productId, String versionId).
 * Refactored some tests.
 * Added ProductDao.dropAllProducts()

