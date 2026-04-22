package com.hulkhiretech.payments.repository.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.hulkhiretech.payments.repository.interfaces.ValidationRulesRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ValidationRulesRepositoryImpl implements ValidationRulesRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String SELECT_ACTIVE_RULES_SQL = """
            SELECT validatorName
            FROM validations.validation_rules
            WHERE isActive = true
            ORDER BY priority ASC
            """;

    @Override
    public List<String> loadActiveValidatorNamesOrderedByPriority() {
        log.debug("Loading active validator names ordered by priority");
        
        List<String> names = namedParameterJdbcTemplate.query(
        		SELECT_ACTIVE_RULES_SQL, 
        		new MapSqlParameterSource(),
                (rs, rowNum) -> rs.getString("validatorName")
                );
        return names == null ? new ArrayList<>() : names;
    }
}