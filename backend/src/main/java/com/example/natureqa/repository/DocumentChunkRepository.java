package com.example.natureqa.repository;

import com.example.natureqa.exception.EmbeddingDimensionMismatchException;
import com.example.natureqa.exception.VectorStorageException;
import com.pgvector.PGvector;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DocumentChunkRepository {

    public static final int EMBEDDING_DIMENSIONS = 1536;
    public static final int DEFAULT_NEAREST_LIMIT = 3;
    public static final int MAX_NEAREST_LIMIT = 20;

    private final JdbcTemplate jdbcTemplate;

    public DocumentChunkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void insertChunks(List<String> contents, List<float[]> embeddings) {
        if (contents.isEmpty()) {
            return;
        }
        if (contents.size() != embeddings.size()) {
            throw new IllegalArgumentException("contents and embeddings must have the same size.");
        }
        for (float[] vec : embeddings) {
            if (vec.length != EMBEDDING_DIMENSIONS) {
                throw new EmbeddingDimensionMismatchException(
                        "Expected embedding dimension " + EMBEDDING_DIMENSIONS + " for pgvector storage, got "
                                + vec.length + ".");
            }
        }

        try {
            jdbcTemplate.batchUpdate(
                    "INSERT INTO document_chunks (content, embedding) VALUES (?, ?)",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setString(1, contents.get(i));
                            ps.setObject(2, new PGvector(embeddings.get(i)));
                        }

                        @Override
                        public int getBatchSize() {
                            return contents.size();
                        }
                    });
        } catch (DataAccessException ex) {
            throw new VectorStorageException("Could not save document chunks to the database.", ex);
        }
    }

    public List<ChunkNearestRow> findNearest(float[] queryEmbedding, int limit) {
        if (queryEmbedding.length != EMBEDDING_DIMENSIONS) {
            throw new EmbeddingDimensionMismatchException(
                    "Expected embedding dimension " + EMBEDDING_DIMENSIONS + " for similarity search, got "
                            + queryEmbedding.length + ".");
        }
        int safeLimit = Math.min(MAX_NEAREST_LIMIT, Math.max(1, limit));
        String sql =
                """
                SELECT id, content, embedding <-> ? AS distance
                FROM document_chunks
                ORDER BY distance
                LIMIT ?
                """;
        try {
            return jdbcTemplate.query(
                    sql,
                    (ResultSet rs, int rowNum) ->
                            new ChunkNearestRow(rs.getLong("id"), rs.getString("content"), rs.getDouble("distance")),
                    new PGvector(queryEmbedding),
                    safeLimit);
        } catch (DataAccessException ex) {
            throw new VectorStorageException("Could not search document chunks.", ex);
        }
    }

    public record ChunkNearestRow(long id, String content, double distance) {}
}
