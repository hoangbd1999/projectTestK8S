package com.elcom.adminconsolebackend.repository.management;

import com.elcom.adminconsolebackend.dto.*;
import com.elcom.adminconsolebackend.util.SearchUtils;
import com.elcom.adminconsolebackend.util.StreamUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Repository
public class IpAccessRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    @AllArgsConstructor
    public final class QueryInfo {

        private List<Object> parameters;

        private String queryString;
    }

    private static final Map<FilterOperator, String> MAP_PREDICATE_BY_OPERATOR = Map.ofEntries(
            Map.entry(FilterOperator.EQUALS, ":table_alias.:attribute IN (:params)"),
            Map.entry(FilterOperator.NOT_EQUALS, ":table_alias.:attribute NOT IN (:params)"),
            Map.entry(FilterOperator.EMPTY, ":table_alias.:attribute IS NULL"),
            Map.entry(FilterOperator.NOT_EMPTY, ":table_alias.:attribute IS NOT NULL"),
            Map.entry(FilterOperator.IN_RANGE, "? <= :table_alias.:attribute AND :table_alias.:attribute <= ?"),
            Map.entry(FilterOperator.OUT_OF_RANGE, ":table_alias.:attribute < ? OR :table_alias.:attribute > ?"),
            Map.entry(FilterOperator.GREATER_THAN, ":table_alias.:attribute > ?"),
            Map.entry(FilterOperator.LESS_THAN, ":table_alias.:attribute < ?"),
            Map.entry(FilterOperator.STRING_CONTAINS, "upper(:table_alias.:attribute) LIKE ?"),
            Map.entry(FilterOperator.STRING_EQUALS, "upper(:table_alias.:attribute) IN (:params)"),
            Map.entry(FilterOperator.STRING_NOT_EQUALS, "upper(:table_alias.:attribute) NOT IN (:params)"),
            Map.entry(FilterOperator.MULTI_SELECTION_NOT_EQUALS, "upper(:table_alias.:attribute) NOT IN (:params)"),
            Map.entry(FilterOperator.MULTI_SELECTION_CONTAINS, "upper(:table_alias.:attribute) IN (:params)"),
            Map.entry(FilterOperator.MULTI_SELECTION_EQUALS, "upper(:table_alias.:attribute) IN (:params)")
    );

    private static final Map<FilterOperator, String> MAP_PREDICATE_TIME_BY_OPERATOR = Map.ofEntries(
            Map.entry(FilterOperator.EQUALS, "date_trunc('second', :table_alias.:attribute) = :params"),
            Map.entry(FilterOperator.NOT_EQUALS, "date_trunc('second', :table_alias.:attribute) NOT IN (:params)"),
            Map.entry(FilterOperator.EMPTY, ":table_alias.:attribute IS NULL"),
            Map.entry(FilterOperator.NOT_EMPTY, ":table_alias.:attribute IS NOT NULL"),
            Map.entry(FilterOperator.IN_RANGE, "? <= date_trunc('second', :table_alias.:attribute) AND date_trunc('second', :table_alias.:attribute) <= ?"),
            Map.entry(FilterOperator.OUT_OF_RANGE, "date_trunc('second', :table_alias.:attribute) < ? OR date_trunc('second', :table_alias.:attribute) > ?"),
            Map.entry(FilterOperator.GREATER_THAN, "date_trunc('second', :table_alias.:attribute) > ?"),
            Map.entry(FilterOperator.LESS_THAN, "date_trunc('second', :table_alias.:attribute) < ?"),
            Map.entry(FilterOperator.STRING_CONTAINS, "upper(:table_alias.:attribute) LIKE ?"),
            Map.entry(FilterOperator.STRING_EQUALS, "upper(:table_alias.:attribute) IN (:params)"),
            Map.entry(FilterOperator.STRING_NOT_EQUALS, "upper(:table_alias.:attribute) NOT IN (:params)"),
            Map.entry(FilterOperator.MULTI_SELECTION_NOT_EQUALS, "upper(:table_alias.:attribute) NOT IN (:params)"),
            Map.entry(FilterOperator.MULTI_SELECTION_CONTAINS, "upper(:table_alias.:attribute) IN (:params)"),
            Map.entry(FilterOperator.MULTI_SELECTION_EQUALS, "upper(:table_alias.:attribute) IN (:params)")
    );

    private static final Map<FilterOperator, String> MAP_PREDICATE_JSONB_BY_OPERATOR = Map.ofEntries(
            Map.entry(FilterOperator.EMPTY, " :table_alias.:attribute IS NULL "),
            Map.entry(FilterOperator.NOT_EMPTY, " :table_alias.:attribute IS NOT NULL "),
            Map.entry(FilterOperator.MULTI_SELECTION_CONTAINS, " EXISTS (SELECT 1 FROM jsonb_array_elements(:table_alias) AS ele WHERE upper(ele ->> :attribute) IN (:params)) "),
            Map.entry(FilterOperator.MULTI_SELECTION_EQUALS, " EXISTS (SELECT 1 FROM jsonb_array_elements(:table_alias) AS ele" +
                    " WHERE jsonb_array_length(:table_alias) = ? AND upper(ele ->> :attribute) IN (:params)) "),
            Map.entry(FilterOperator.MULTI_SELECTION_NOT_EQUALS, " NOT EXISTS (SELECT 1 FROM jsonb_array_elements(:table_alias) AS ele " +
                    " WHERE jsonb_array_length(:table_alias) = ? AND upper(ele ->> :attribute) IN (:params)) ")
    );

    public QueryInfo calculateQueryInfo(List<FilterCondition> conditions) {

        List<Object> parameters = new ArrayList<>();
        List<FilterCondition> ipAddressCondition = StreamUtils.filterThenToList(
                conditions, cond -> "ip_address".equals(cond.getAttrCode())
        );
        List<FilterCondition> descriptionCondition = StreamUtils.filterThenToList(
                conditions, cond -> "description".equals(cond.getAttrCode())
        );

        List<FilterCondition> accountsLimitsConditions = StreamUtils.filterThenToList(
                conditions, cond -> "accounts".equals(cond.getAttrCode())
        );

        List<FilterCondition> createdByCondition = StreamUtils.filterThenToList(
                conditions, cond -> "created_by".equals(cond.getAttrCode())
        );

        List<FilterCondition> statusCondition = StreamUtils.filterThenToList(
                conditions, cond -> "status".equals(cond.getAttrCode())
        );

        List<FilterCondition> createdAtCondition = StreamUtils.filterThenToList(
                conditions, cond -> "created_at".equals(cond.getAttrCode())
        );

        List<FilterCondition> searchCondition = StreamUtils.filterThenToList(
                conditions, cond -> "search".equals(cond.getAttrCode())
        );

        String search = CollectionUtils.isEmpty(searchCondition) ? "" :
                com.elcom.adminconsolebackend.util.StringUtils.getValueJson(searchCondition.get(0).getData());

        QueryInfo queryInfoIpAddressCondition = calculateIpAddressQueryInfo(ipAddressCondition);
        QueryInfo queryInfoDescriptionCondition = calculateDescrptionQueryInfo(descriptionCondition);
        QueryInfo queryInfoAccountsCondition = calculateAccountsQueryInfo(accountsLimitsConditions);
        QueryInfo queryInfoCreatedByCondition = calculateCreatedByQueryInfo(createdByCondition);
        QueryInfo queryInfoStatusCondition = calculateStatusQueryInfo(statusCondition);
        QueryInfo queryInfoCreatedAtCondition = calculateCreatedAtQueryInfo(createdAtCondition);
        QueryInfo queryInfoSearchCondition = calculateSearchQueryInfo(search);

        parameters.addAll(queryInfoIpAddressCondition.getParameters());
        parameters.addAll(queryInfoDescriptionCondition.getParameters());
        parameters.addAll(queryInfoAccountsCondition.getParameters());
        parameters.addAll(queryInfoCreatedByCondition.getParameters());
        parameters.addAll(queryInfoStatusCondition.getParameters());
        parameters.addAll(queryInfoCreatedAtCondition.getParameters());
        parameters.addAll(queryInfoSearchCondition.getParameters());

        String[] predicates = Stream.of(
                        queryInfoIpAddressCondition.getQueryString(),
                        queryInfoDescriptionCondition.getQueryString(),
                        queryInfoAccountsCondition.getQueryString(),
                        queryInfoCreatedByCondition.getQueryString(),
                        queryInfoStatusCondition.getQueryString(),
                        queryInfoCreatedAtCondition.getQueryString(),
                        queryInfoSearchCondition.getQueryString()
                )
                .filter(StringUtils::hasText).toArray(String[]::new);
        return new QueryInfo(parameters, String.join(" AND ", predicates));
    }

    private QueryInfo calculateIpAddressQueryInfo(List<FilterCondition> conditions) {
        if (CollectionUtils.isEmpty(conditions)) {
            return new QueryInfo(Collections.emptyList(), "");
        }
        FilterCondition condition = conditions.get(0);
        FilterOperator operator = condition.getOperator();
        ValueWrapper.ListValueWrapper<String> typedData = ValueWrapper.listInstanceOf(String.class, condition.getData());
        List<Object> parameters = new ArrayList<>();
        String predicate = MAP_PREDICATE_BY_OPERATOR.get(operator);
        if (predicate == null) {
            return new QueryInfo(Collections.emptyList(), "");
        }
        if (operator == FilterOperator.STRING_CONTAINS) {
            typedData.getValue().forEach(item -> {
                String pattern = SearchUtils.createSearchPatternFrom(item);
                parameters.add(pattern.toUpperCase());
            });
        } else {
            List<String> valueInUpperCase = StreamUtils.toList(typedData.getValue(), s -> ((String) s).toUpperCase());
            parameters.addAll(valueInUpperCase);
        }
        String[] paramMakers = new String[typedData.getValue().size()];
        Arrays.fill(paramMakers, "?");
        String paramsStr = String.join(",", paramMakers);
        predicate = predicate.replaceAll(":table_alias", "ip");
        predicate = predicate.replaceAll(":attribute", "ip_address");
        predicate = predicate.replaceAll(":params", paramsStr);
        String queryStr = "(" + predicate + ")";
        return new QueryInfo(parameters, queryStr);
    }

    private QueryInfo calculateAccountsQueryInfo(List<FilterCondition> conditions) {
        if (CollectionUtils.isEmpty(conditions)) {
            return new QueryInfo(Collections.emptyList(), "");
        }
        FilterCondition condition = conditions.get(0);
        FilterOperator operator = condition.getOperator();
        ValueWrapper.ListValueWrapper<String> typedData = ValueWrapper.listInstanceOf(String.class, condition.getData());
        List<Object> parameters = new ArrayList<>();
        String predicate = MAP_PREDICATE_JSONB_BY_OPERATOR.get(operator);
        if (predicate == null) {
            return new QueryInfo(Collections.emptyList(), "");
        }

        if (operator == FilterOperator.MULTI_SELECTION_EQUALS
                || operator == FilterOperator.MULTI_SELECTION_NOT_EQUALS) {
            parameters.add(typedData.getValue().size());
        }

        List<String> valueInUpperCase = StreamUtils.toList(typedData.getValue(), s -> ((String) s).toUpperCase());
        parameters.addAll(valueInUpperCase);

        String[] paramMakers = new String[typedData.getValue().size()];
        Arrays.fill(paramMakers, "?");
        String paramsStr = String.join(",", paramMakers);
        if (operator == FilterOperator.EMPTY || operator == FilterOperator.NOT_EMPTY) {
            predicate = predicate.replaceAll(":table_alias", "ip");
            predicate = predicate.replaceAll(":attribute", "accounts");
            predicate = predicate.replaceAll(":params", paramsStr);
        } else {
            predicate = predicate.replaceAll(":table_alias", "ip.accounts");
            predicate = predicate.replaceAll(":attribute", "'name'");
            predicate = predicate.replaceAll(":params", paramsStr);
        }
        String queryStr = "(" + predicate + ")";
        return new QueryInfo(parameters, queryStr);
    }

    private QueryInfo calculateDescrptionQueryInfo(List<FilterCondition> conditions) {
        if (CollectionUtils.isEmpty(conditions)) {
            return new QueryInfo(Collections.emptyList(), "");
        }
        FilterCondition condition = conditions.get(0);
        FilterOperator operator = condition.getOperator();
        ValueWrapper.ListValueWrapper<String> typedData = ValueWrapper.listInstanceOf(String.class, condition.getData());
        List<Object> parameters = new ArrayList<>();
        String predicate = MAP_PREDICATE_BY_OPERATOR.get(operator);
        if (predicate == null) {
            return new QueryInfo(Collections.emptyList(), "");
        }
        if (operator == FilterOperator.STRING_CONTAINS) {
            typedData.getValue().forEach(item -> {
                String pattern = SearchUtils.createSearchPatternFrom(item);
                parameters.add(pattern.toUpperCase());
            });
        } else {
            List<String> valueInUpperCase = StreamUtils.toList(typedData.getValue(), s -> ((String) s).toUpperCase());
            parameters.addAll(valueInUpperCase);
        }
        String[] paramMakers = new String[typedData.getValue().size()];
        Arrays.fill(paramMakers, "?");
        String paramsStr = String.join(",", paramMakers);
        predicate = predicate.replaceAll(":table_alias", "ip");
        predicate = predicate.replaceAll(":attribute", "description");
        predicate = predicate.replaceAll(":params", paramsStr);
        String queryStr = "(" + predicate + ")";
        return new QueryInfo(parameters, queryStr);
    }

    private QueryInfo calculateCreatedByQueryInfo(List<FilterCondition> conditions) {
        if (CollectionUtils.isEmpty(conditions)) {
            return new QueryInfo(Collections.emptyList(), "");
        }
        FilterCondition condition = conditions.get(0);
        FilterOperator operator = condition.getOperator();
        ValueWrapper.ListValueWrapper<String> typedData = ValueWrapper.listInstanceOf(String.class, condition.getData());
        List<Object> parameters = new ArrayList<>();
        String predicate = MAP_PREDICATE_BY_OPERATOR.get(operator);
        if (predicate == null) {
            return new QueryInfo(Collections.emptyList(), "");
        }
        if (operator == FilterOperator.STRING_CONTAINS) {
            typedData.getValue().forEach(item -> {
                String pattern = SearchUtils.createSearchPatternFrom(item);
                parameters.add(pattern.toUpperCase());
            });
        } else {
            List<String> valueInUpperCase = StreamUtils.toList(typedData.getValue(), s -> ((String) s).toUpperCase());
            parameters.addAll(valueInUpperCase);
        }
        String[] paramMakers = new String[typedData.getValue().size()];
        Arrays.fill(paramMakers, "?");
        String paramsStr = String.join(",", paramMakers);
        predicate = predicate.replaceAll(":table_alias", "ip");
        predicate = predicate.replaceAll(":attribute", "created_by");
        predicate = predicate.replaceAll(":params", paramsStr);
        String queryStr = "(" + predicate + ")";
        return new QueryInfo(parameters, queryStr);
    }

    private QueryInfo calculateStatusQueryInfo(List<FilterCondition> conditions) {
        if (CollectionUtils.isEmpty(conditions)) {
            return new QueryInfo(Collections.emptyList(), "");
        }
        FilterCondition condition = conditions.get(0);
        FilterOperator operator = condition.getOperator();
        ValueWrapper.ListValueWrapper<String> typedData = ValueWrapper.listInstanceOf(String.class, condition.getData());
        List<Object> parameters = new ArrayList<>(typedData.getValue());
        String predicate = MAP_PREDICATE_BY_OPERATOR.get(operator);
        if (predicate == null) {
            return new QueryInfo(Collections.emptyList(), "");
        }
        String[] paramMakers = new String[typedData.getValue().size()];
        Arrays.fill(paramMakers, "?");
        String paramsStr = String.join(",", paramMakers);
        predicate = predicate.replaceAll(":table_alias", "ip");
        predicate = predicate.replaceAll(":attribute", "status");
        predicate = predicate.replaceAll(":params", paramsStr);
        String queryStr = "(" + predicate + ")";
        return new QueryInfo(parameters, queryStr);
    }

    private QueryInfo calculateCreatedAtQueryInfo(List<FilterCondition> eventTimeCond) {
        if (CollectionUtils.isEmpty(eventTimeCond)) {
            return new QueryInfo(Collections.emptyList(), "");
        }
        List<String> predicates = new LinkedList<>();
        List<Object> parameters = new LinkedList<>();

        eventTimeCond.forEach(cond -> {
            FilterOperator operator = cond.getOperator();
            String predicate = MAP_PREDICATE_TIME_BY_OPERATOR.get(operator);
            if (predicate == null) return;
            ValueWrapper.ListValueWrapper<String> typedData = ValueWrapper.listInstanceOf(String.class, cond.getData());

            if (operator == FilterOperator.IN_RANGE || operator == FilterOperator.OUT_OF_RANGE) {
                typedData.getValue().forEach(item -> parameters.add(Timestamp.valueOf(item)));
            } else if (operator == FilterOperator.EQUALS || operator == FilterOperator.NOT_EQUALS || operator == FilterOperator.GREATER_THAN
                    || operator == FilterOperator.LESS_THAN) {
                typedData.getValue().forEach(item -> parameters.add(Timestamp.valueOf(item)));
            }

            String[] paramMakers = new String[typedData.getValue().size()];
            Arrays.fill(paramMakers, "?");
            String paramsStr = String.join(",", paramMakers);
            predicate = predicate.replaceAll(":table_alias", "ip");
            predicate = predicate.replaceAll(":attribute", "created_at");
            predicate = predicate.replaceAll(":params", paramsStr);
            predicates.add(predicate);
        });
        String queryStr = "(" + String.join(" AND ", predicates) + ")";
        String query = CollectionUtils.isEmpty(predicates) ? "" : queryStr;
        return new QueryInfo(parameters, query);
    }


    private QueryInfo calculateSearchQueryInfo(String search) {

        if (!StringUtils.hasText(search)) {
            return new QueryInfo(Collections.emptyList(), "");
        }
        String query = "(upper(ip.ip_address) LIKE ?)";
        String pattern = SearchUtils.createSearchPatternFrom(search);
        List<Object> parameters = List.of(pattern.toUpperCase());
        return new QueryInfo(parameters, query);
    }

    public List<IpAccessDTO> filterElementsOnSpecificPage(QueryInfo queryInfo, Pageable pageable) {

        String queryStr = "SELECT ip.* from admin_console.ip_access ip ";
        if (!com.elcom.adminconsolebackend.util.StringUtils.isNullOrEmpty(queryInfo.getQueryString())) {
            queryStr += " where " + queryInfo.getQueryString();
        }

        Sort sort = pageable.getSort();
        Optional<Sort.Order> optionalOrder = sort.stream().findFirst();

        if (optionalOrder.isPresent()) {
            Sort.Order order = optionalOrder.get();
            queryStr = queryStr + " ORDER BY ip." + order.getProperty() + " " + order.getDirection() + " NULLS LAST";
        }
        Query compositeQuery = entityManager.createNativeQuery(queryStr, Tuple.class);
        bindParameters(compositeQuery, queryInfo.getParameters());
        if (pageable.isPaged()) {
            compositeQuery.setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize());
        }

        List<Tuple> result = compositeQuery.getResultList();
        return StreamUtils.toList(result, row -> {
            IpAccessDTO dto = IpAccessDTO.builder()
                    .id(row.get("id", Integer.class).longValue())
                    .ipAddress(row.get("ip_address", String.class))
                    .description(row.get("description", String.class))
                    .createdBy(row.get("created_by", String.class))
                    .build();
            LocalDateTime createdAt = row.get("created_at", Timestamp.class).toLocalDateTime();
            Integer status = row.get("status", Short.class) == null ? 1 : row.get("status", Short.class).intValue();
            String accounts = row.get("accounts", String.class);
            if (accounts != null) {
                try {
                    List<AccountLimitDTO> accountsLimit = objectMapper.readValue(accounts, new TypeReference<List<AccountLimitDTO>>() {
                    });
                    dto.setAccounts(accountsLimit);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            dto.setStatus(status);
            dto.setCreatedAt(createdAt);
            return dto;
        });
    }

    public Long count(QueryInfo queryInfo) {

        String queryStr = "SELECT count(*) from admin_console.ip_access ip ";
        if (!com.elcom.adminconsolebackend.util.StringUtils.isNullOrEmpty(queryInfo.getQueryString())) {
            queryStr += " where " + queryInfo.getQueryString();
        }

        Query compositeQuery = entityManager.createNativeQuery(queryStr);
        bindParameters(compositeQuery, queryInfo.getParameters());

        return ((Number) compositeQuery.getSingleResult()).longValue();
    }

    private void bindParameters(Query compositeQuery, List<Object> parameters) {
        for (int i = 1; i <= parameters.size(); i++) {
            compositeQuery.setParameter(i, parameters.get(i - 1));
        }
    }

}
