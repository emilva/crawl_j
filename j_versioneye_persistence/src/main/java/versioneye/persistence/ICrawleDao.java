package versioneye.persistence;

import org.bson.types.ObjectId;
import versioneye.domain.Crawle;

import java.sql.Timestamp;

public interface ICrawleDao {

    void create(final Crawle crawle);

    void updateDates(ObjectId id, Timestamp updated, Timestamp duration);

    Crawle getById(ObjectId id);

}
