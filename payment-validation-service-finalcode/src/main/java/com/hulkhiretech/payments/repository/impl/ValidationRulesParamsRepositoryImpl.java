package com.hulkhiretech.payments.repository.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.hulkhiretech.payments.repository.interfaces.ValidationRulesParamsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ValidationRulesParamsRepositoryImpl implements ValidationRulesParamsRepository {

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private static final String SELECT_PARAMS_SQL = """
			SELECT validatorName, paramName, paramValue
			FROM validations.validation_rules_params
			""";

	@Override
	public Map<String, Map<String, String>> loadAllValidatorParams() {
		log.debug("Loading validator rule parameters");

		List<Map<String, String>> rows = namedParameterJdbcTemplate.query(SELECT_PARAMS_SQL,
				new MapSqlParameterSource(), new RowMapper<Map<String, String>>() {
			@Override
			public Map<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
				Map<String, String> m = new HashMap<>();
				m.put("validatorName", rs.getString("validatorName"));
				m.put("paramName", rs.getString("paramName"));
				m.put("paramValue", rs.getString("paramValue"));
				return m;
			}
		});

		Map<String, Map<String, String>> result = new HashMap<>();

		for (Map<String, String> r : rows) {
			String validatorName = r.get("validatorName");
			String paramName = r.get("paramName");
			String paramValue = r.get("paramValue");

			result.computeIfAbsent(
					validatorName, k -> new HashMap<>()).put(paramName, paramValue);
		}

		return result;
	}
}
