package by.slesh.itechart.fullcontact.dao;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import by.slesh.itechart.fullcontact.domain.Entity;

import com.mysql.jdbc.StringUtils;

public abstract class EntityDao<T extends Entity> extends AbstractDao implements Getable<T>, Countable, Deletable {
    private final static Logger LOGGER = LoggerFactory.getLogger(EntityDao.class);

    private String deleteQuery;
    private String countQuery;
    private String getQuery;
    private String getIdQuery;
    private String getAllQuery;
    private String getLimitQuery;
    private DaoReader<T> reader;

    public EntityDao() {
    }

    public EntityDao(boolean isUseCurrentConnection, boolean isCloseConnectionAfterWork) {
	super(isUseCurrentConnection, isCloseConnectionAfterWork);
    }

    public EntityDao(final String deleteQuery, final String countQuery, final String getQuery, final String getIdQuery,
	    final String getLimitQuery, final String getAllQuery, DaoReader<T> reader, boolean isUseCurrentConnection,
	    boolean isCloseConnectionAfterWork) {
	this(isUseCurrentConnection, isCloseConnectionAfterWork);
	this.deleteQuery = deleteQuery;
	this.countQuery = countQuery;
	this.getQuery = getQuery;
	this.getIdQuery = getIdQuery;
	this.getLimitQuery = getLimitQuery;
	this.getAllQuery = getAllQuery;
	this.reader = reader;
    }

    public void setDeleteQuery(String deleteQuery) {
	this.deleteQuery = deleteQuery;
    }

    public void setCountQuery(String countQuery) {
	this.countQuery = countQuery;
    }

    public void setGetQuery(String getQuery) {
	this.getQuery = getQuery;
    }

    public void setGetIdQuery(final String getIdQuery) {
	this.getIdQuery = getIdQuery;
    }

    public void setGetLimitQuery(String getLimitQuery) {
	this.getLimitQuery = getLimitQuery;
    }

    public void setGetAllQuery(final String getAllQuery) {
	this.getAllQuery = getAllQuery;
    }

    @SuppressWarnings("unchecked")
    public void setReader(DaoReader<Entity> reader) {
	this.reader = (DaoReader<T>) reader;
    }

    @Override
    public void delete(long id) throws ClassNotFoundException, IOException, SQLException {
	LOGGER.info("BEGIN");
	LOGGER.info("id: {}", id);

	try {
	    connect();
	    preparedStatement = getPrepareStatement(deleteQuery);
	    preparedStatement.setLong(1, id);
	    preparedStatement.executeUpdate();

	    LOGGER.info("query: {}", preparedStatement);
	} finally {
	    closeResources();
	}

	LOGGER.info("END");
    }

    @Override
    public long count() throws ClassNotFoundException, IOException, SQLException {
	LOGGER.info("BEGIN");

	long quantity = 0;
	try {
	    connect();
	    statement = getStatement();
	    resultSet = statement.executeQuery(countQuery);
	    while (resultSet.next()) {
		quantity = resultSet.getLong(1);
	    }

	    LOGGER.info("quantity: " + quantity + " contacts");
	    LOGGER.info("query: " + statement);
	} finally {
	    closeResources();
	}

	LOGGER.info("END");

	return quantity;
    }

    @Override
    public long getId(String value) throws ClassNotFoundException, IOException, SQLException {
	LOGGER.info("BEGIN");
	LOGGER.info("value: {}", value);

	long id = 0;
	if (StringUtils.isEmptyOrWhitespaceOnly(getIdQuery)) {
	    LOGGER.info("RETURN: query empty or null");
	    return 0;
	}
	try {
	    connect();
	    preparedStatement = getPrepareStatement(getIdQuery);
	    preparedStatement.setString(1, value);
	    ResultSet resultSet = preparedStatement.executeQuery();
	    while (resultSet.next()) {
		id = resultSet.getLong(1);
	    }

	    LOGGER.info("query: {}", preparedStatement);
	    LOGGER.info("id for {} = {}", value, id);
	} finally {
	    closeResources();
	}

	LOGGER.info("END");

	return id;
    }

    @Override
    public T get(long id) throws ClassNotFoundException, IOException, SQLException {
	LOGGER.info("BEGIN");
	LOGGER.info("id: {}", id);

	if (StringUtils.isEmptyOrWhitespaceOnly(getQuery) || reader == null) {
	    LOGGER.info("RETURN: query empty or null");
	    return null;
	}

	T item = null;
	try {
	    connect();
	    preparedStatement = getPrepareStatement(getQuery);
	    preparedStatement.setLong(1, id);
	    
	    LOGGER.info("query: {}", preparedStatement);

	    ResultSet resultSet = preparedStatement.executeQuery();
	    item = reader.read(resultSet);
	    
	    LOGGER.info("entity with id {}: {}", id, item);
	} finally {
	    closeResources();
	}

	return item;
    }

    @Override
    public List<T> getLimit(long start, long size) throws ClassNotFoundException, IOException, SQLException {
	LOGGER.info("BEGIN");

	if (StringUtils.isEmptyOrWhitespaceOnly(getLimitQuery) || reader == null) {
	    LOGGER.info("RETURN: query empty or null");
	    return null;
	}
	List<T> list = null;
	try {
	    connect();
	    preparedStatement = getPrepareStatement(getLimitQuery);
	    preparedStatement.setLong(1, start);
	    preparedStatement.setLong(2, size);
	    list = getHelper(preparedStatement);
	} finally {
	    closeResources();
	}

	LOGGER.info("END");

	return list;
    }

    @Override
    public List<T> getAll() throws ClassNotFoundException, IOException, SQLException {
	LOGGER.info("BEGIN");

	if (StringUtils.isEmptyOrWhitespaceOnly(getAllQuery) || reader == null) {
	    LOGGER.info("RETURN: query empty or null");
	    return null;
	}
	List<T> list = null;
	try {
	    connect();
	    preparedStatement = getPrepareStatement(getAllQuery);
	    list = getHelper(preparedStatement);
	} finally {
	    closeResources();
	}

	LOGGER.info("END");

	return list;
    }
    
    private final List<T> getHelper(PreparedStatement statement) throws ClassNotFoundException, IOException, SQLException{
	List<T> list = new ArrayList<T>();
	T item = null;
	try {
	    connect();
	    resultSet = statement.executeQuery();
	    while ((item = reader.read(resultSet)) != null) {
		list.add(item);
	    }

	    LOGGER.info("query: {}", statement);
	} finally {
	    closeResources();
	}

	LOGGER.info("fetch {} entities", list.size());

	return list;
    }
}
