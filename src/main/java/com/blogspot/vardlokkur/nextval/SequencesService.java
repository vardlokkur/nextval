package com.blogspot.vardlokkur.nextval;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_SCROLL_SENSITIVE;

/**
 * @author Warlock
 */
@Service
class SequencesService implements Sequences {

    private static final String SQL_QUERY =
        "SELECT SEQ_NAME, SEQ_VALUE FROM SEQUENCE WHERE SEQ_NAME = ? FOR UPDATE";

    private final DataSource dataSource;

    SequencesService(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int nextValue(final String sequenceName) throws SQLException {
        final long threadId = Thread.currentThread()
                                    .getId();

        try (final Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (final PreparedStatement statement =
                     connection.prepareStatement(
                         SQL_QUERY, TYPE_SCROLL_SENSITIVE, CONCUR_UPDATABLE)) {
                statement.setString(1, sequenceName);
                try (final ResultSet resultSet = statement.executeQuery()) {
                    System.out.println(
                        String.format("[%d] - select for update", threadId));
                    int nextValue = 1;
                    if (resultSet.next()) {
                        nextValue = 1 + resultSet.getInt(2);
                        resultSet.updateInt(2, nextValue);
                        resultSet.updateRow();
                    } else {
                        resultSet.moveToInsertRow();
                        resultSet.updateString(1, sequenceName);
                        resultSet.updateInt(2, nextValue);
                        resultSet.insertRow();
                    }
                    System.out.println(
                        String.format("[%d] - next val: %d", threadId, nextValue));
                    return nextValue;
                }
            } finally {
                System.out.println(String.format("[%d] - commit", threadId));
                connection.commit();
            }
        }
    }

}
