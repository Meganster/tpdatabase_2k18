package forumdb.DAO;


import forumdb.Model.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@Repository
public class ThreadDAO {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void createThread(@NotNull Thread thread) throws DataAccessException {
        List<String> existFieldsNames = new ArrayList<>();
        List<Object> existFieldsTypes = new ArrayList<>();

        final Class checkedThread = Thread.class;
        for (Field field : checkedThread.getDeclaredFields()) {
            field.setAccessible(true);

            try {
                if (field.get(thread) != null) {
                    existFieldsNames.add(field.getName());
                    existFieldsTypes.add(field.getType().cast(field.get(thread)));
                }
            } catch (IllegalAccessException error) {
                System.out.println(error);
            }
        }

        final StringBuilder sqlNameRows = new StringBuilder();
        final StringBuilder sqlParameters = new StringBuilder();

        for (String nameRow : existFieldsNames) {
            sqlNameRows.append(nameRow).append(", ");
        }

        for (Object valueRow : existFieldsTypes) {
            sqlParameters.append(" '").append(valueRow.toString()).append("', ");
        }

        sqlNameRows.delete(sqlNameRows.length() - 2, sqlNameRows.length());
        sqlParameters.delete(sqlParameters.length() - 2, sqlParameters.length());

        final StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO Thread (").append(sqlNameRows).append(") VALUES (").append(sqlParameters).append(");");
        jdbcTemplate.update(sql.toString());
    }

    public Thread getThread(@NotNull String nickname, @NotNull String slugForum, @NotNull String title) {
        return jdbcTemplate.queryForObject("SELECT * FROM Thread WHERE author = ?::CITEXT " +
                        "AND forum = ?::CITEXT AND title = ?;",
                new Object[]{nickname, slugForum, title}, new ThreadMapper());
    }

    public List<Thread> getThreads(@NotNull String slugForum,
                                   @NotNull Integer limit, @NotNull String since, @NotNull Boolean desc) {
        final StringBuilder sql = new StringBuilder("SELECT * FROM Thread WHERE forum = '" + slugForum + "'::citext");
        if (!since.isEmpty()) {
            if (desc == true) {
                sql.append(" AND created <= '").append(since).append("'::timestamptz");
            } else {
                sql.append(" AND created >= '").append(since).append("'::timestamptz");
            }
        }

        sql.append(" ORDER BY created");
        if (desc) {
            sql.append(" DESC");
        }

        if (limit > 0) {
            sql.append(" LIMIT ").append(limit);
        }
        return jdbcTemplate.query(sql.toString(), new ThreadMapper());
    }

    public Thread getThreadByID(@NotNull Integer id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM Thread WHERE id = ?;",
                    new Object[]{id}, new ThreadMapper());
        } catch (DataAccessException e) {
            return null;
        }
    }

    public Thread getThreadBySlug(@NotNull String slug) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM Thread WHERE slug = ?::citext;",
                    new Object[]{slug}, new ThreadMapper());
        } catch (DataAccessException e) {
            return null;
        }
    }

    public Integer getVote(@NotNull Integer userID, @NotNull Integer threadID) {
        try {
            return jdbcTemplate.queryForObject("SELECT vote FROM UserVoteForThreads WHERE user_id = ? AND thread_id = ?;",
                    new Object[]{userID, threadID}, Integer.class);
        } catch (DataAccessException e) {
            return null;
        }
    }

    public void vote(@NotNull Integer threadID, @NotNull Integer userID,
                     @NotNull Integer key, @NotNull Integer voteStatus) throws DataAccessException {
        jdbcTemplate.update("UPDATE Thread SET votes = votes + ? WHERE id = ?;", key, threadID);

        if (voteStatus == 0) {
            jdbcTemplate.update("INSERT INTO UserVoteForThreads (user_id, thread_id, vote) VALUES (?, ?, ?);",
                    userID, threadID, key);
        } else {
            jdbcTemplate.update("UPDATE UserVoteForThreads SET vote = ? WHERE thread_id = ? AND user_id = ?;",
                    key, threadID, userID);
        }
    }

    public void update(@NotNull Integer threadID, @NotNull Thread changedThread) {
        final StringBuilder sql = new StringBuilder("UPDATE Thread");

        final String title = changedThread.getTitle();
        Boolean addedTitle = false;
        Boolean addedMessage = false;

        if (title != null && !title.isEmpty()) {
            sql.append(" SET title='").append(title).append("'");
            addedTitle = true;
        }

        final String message = changedThread.getMessage();
        if (message != null && !message.isEmpty()) {
            if (addedTitle) {
                sql.append(',');
            } else {
                sql.append(" SET");
            }

            sql.append(" message='").append(message).append("'");
            addedMessage = true;
        }

        if (addedMessage || addedTitle) {
            sql.append(" WHERE id=").append(threadID);
            jdbcTemplate.update(sql.toString());
        }
    }


    public static class ThreadMapper implements RowMapper<Thread> {

        @Override
        public Thread mapRow(ResultSet resultSet, int i) throws SQLException {
            final Thread thread = new Thread();
            thread.setTitle(resultSet.getString("title"));
            thread.setSlug(resultSet.getString("slug"));
            thread.setAuthor(resultSet.getString("author"));
            thread.setForum(resultSet.getString("forum"));
            thread.setMessage(resultSet.getString("message"));
            thread.setCreated(resultSet.getTimestamp("created"));
            thread.setId(resultSet.getInt("id"));
            thread.setVotes(resultSet.getInt("votes"));

            return thread;
        }
    }
}
