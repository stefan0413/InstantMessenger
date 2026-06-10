package org.instantmessenger.backend.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock NamedParameterJdbcTemplate jdbc;
    @InjectMocks UserRepository userRepository;

    @SuppressWarnings("unchecked")
    private void stubQuery() {
        when(jdbc.query(anyString(), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());
    }

    @SuppressWarnings("unchecked")
    private SqlParameterSource captureParams(String query, long excludeUserId, int limit) {
        stubQuery();
        userRepository.search(query, excludeUserId, limit);
        var captor = ArgumentCaptor.forClass(SqlParameterSource.class);
        verify(jdbc).query(anyString(), captor.capture(), any(RowMapper.class));
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private String captureSql(String query, long excludeUserId, int limit) {
        stubQuery();
        userRepository.search(query, excludeUserId, limit);
        var captor = ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(captor.capture(), any(SqlParameterSource.class), any(RowMapper.class));
        return captor.getValue();
    }

    @Test
    void search_capsLimitAt50_whenGivenHigherValue() {
        var params = captureParams("test", 1L, 100);
        assertThat(params.getValue("limit")).isEqualTo(50);
    }

    @Test
    void search_enforcesMinLimitOf1_whenGivenZero() {
        var params = captureParams("test", 1L, 0);
        assertThat(params.getValue("limit")).isEqualTo(1);
    }

    @Test
    void search_passesLimitUnchanged_whenWithinBounds() {
        var params = captureParams("test", 1L, 25);
        assertThat(params.getValue("limit")).isEqualTo(25);
    }

    @Test
    void search_withNullQuery_usesNonFilteringSql() {
        String sql = captureSql(null, 1L, 10);
        assertThat(sql).doesNotContain("LIKE");
    }

    @Test
    void search_withBlankQuery_usesNonFilteringSql() {
        String sql = captureSql("   ", 1L, 10);
        assertThat(sql).doesNotContain("LIKE");
    }

    @Test
    void search_withNonBlankQuery_usesLikeFilteringSql() {
        String sql = captureSql("test", 1L, 10);
        assertThat(sql).contains("LIKE");
    }

    @Test
    void findByChannelIds_withEmptyList_returnsEmptyMapWithoutQueryingDatabase() {
        var result = userRepository.findByChannelIds(List.of());

        assertThat(result).isEmpty();
        verifyNoInteractions(jdbc);
    }
}
