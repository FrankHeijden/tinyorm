package me.geso.tinyorm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class BeanSelectStatement<T> extends
		AbstractSelectStatement<T, BeanSelectStatement<T>> {

	private final TableMeta tableMeta;
	private final TinyORM orm;
	private final Class<T> klass;
	private final Connection connection;

	BeanSelectStatement(Connection connection,
			Class<T> klass, TableMeta tableMeta, TinyORM orm) {
		super(connection, tableMeta.getName());
		this.tableMeta = tableMeta;
		this.orm = orm;
		this.klass = klass;
		this.connection = connection;
	}

	public Optional<T> execute() {
		Query query = this.buildQuery();
		try {
			String sql = query.getSQL();
			Object[] params= query.getValues();
			try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
				TinyORMUtils.fillPreparedStatementParams(preparedStatement, params);
				try (ResultSet rs = preparedStatement.executeQuery()) {
					if (rs.next()) {
						T row = this.orm.mapRowFromResultSet(klass, rs, tableMeta);
						rs.close();
						return Optional.of(row);
					} else {
						return Optional.empty();
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
