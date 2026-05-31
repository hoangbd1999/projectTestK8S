package com.elcom.adminconsolebackend.repository.management;

import com.elcom.adminconsolebackend.dto.FilterCondition;
import com.elcom.adminconsolebackend.dto.FilterOperator;
import com.elcom.adminconsolebackend.dto.ValueWrapper;
import com.elcom.adminconsolebackend.entity.management.WarningSettingEntity;
import com.elcom.adminconsolebackend.util.SearchUtils;
import com.elcom.adminconsolebackend.util.StreamUtils;
import com.elcom.adminconsolebackend.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Stream;

@Repository
public class WarningSettingFilterRepositoryImpl implements WarningSettingFilterRepository {
    @PersistenceContext
    private EntityManager entityManager;

    private static final Map<FilterOperator, String> MAP_PREDICATE_BY_OPERATOR = Map.ofEntries(
            Map.entry(FilterOperator.EQUALS, ":table_alias.:attribute IN (:params)"),
            Map.entry(FilterOperator.NOT_EQUALS, ":table_alias.:attribute NOT IN (:params)"),
            Map.entry(FilterOperator.EMPTY, ":table_alias.:attribute IS NULL"),
            Map.entry(FilterOperator.NOT_EMPTY, ":table_alias.:attribute IS NOT NULL"),
            Map.entry(FilterOperator.IN_RANGE, "? <= :table_alias.:attribute AND :table_alias.:attribute <= ?"),
            Map.entry(FilterOperator.OUT_OF_RANGE, ":table_alias.:attribute < ? OR :table_alias.:attribute > ?"),
            Map.entry(FilterOperator.GREATER_THAN, ":table_alias.:attribute > ?"),
            Map.entry(FilterOperator.LESS_THAN, ":table_alias.:attribute < ?"),
            Map.entry(FilterOperator.STRING_CONTAINS, "(:table_alias.:attribute) ILIKE ?"),
            Map.entry(FilterOperator.STRING_EQUALS, "(:table_alias.:attribute) IN (:params)"),
            Map.entry(FilterOperator.STRING_NOT_EQUALS, "(:table_alias.:attribute) NOT IN (:params)"),
            Map.entry(FilterOperator.MULTI_SELECTION_EQUALS, "jsonb_build_array(:params) @> :table_alias.:attribute AND :table_alias.:attribute @> jsonb_build_array(:params)"),
            Map.entry(FilterOperator.MULTI_SELECTION_NOT_EQUALS, "NOT jsonb_exists_any(:table_alias.:attribute, ARRAY[:params])"),
         //   Map.entry(FilterOperator.MULTI_SELECTION_CONTAINS, ":table_alias.:attribute @> jsonb_build_array(:params)")
            Map.entry(FilterOperator.MULTI_SELECTION_CONTAINS, "EXISTS (SELECT 1 FROM jsonb_array_elements_text(:table_alias.:attribute) AS elem(val) WHERE val IN (:params))")
    );

    private static final Map<String, Class<?>> MAP_TYPE_BY_ATTR = Map.ofEntries(
            Map.entry("warning_channel", String.class),
            Map.entry("warning_objects", String.class),
            Map.entry("resource_types", String.class),
            Map.entry("email_send", String.class),
            Map.entry("warning_levels", String.class),
            Map.entry("status", String.class)
    );

    @Getter
    @AllArgsConstructor
    private final class QueryInfo {

        private List<Object> parameters;

        private String queryString;
    }

    @Override
    public Page<WarningSettingEntity> filterWarningSetting(List<FilterCondition> conditions, Pageable pageable) {
        QueryInfo queryInfo = calculateQueryInfo(conditions);
        List<WarningSettingEntity> elementsOnPage = filterElementsOnSpecificPage(queryInfo, pageable);
        long totalElements = count(queryInfo);
        return new PageImpl<>(elementsOnPage, pageable, totalElements);
    }

    private long count(QueryInfo queryInfo) {
        String queryStr = "SELECT COUNT(*) from admin_console.warning_setting w ";
        if (!StringUtils.isNullOrEmpty(queryInfo.getQueryString())) {
            queryStr += " where " + queryInfo.getQueryString();
        }
        Query compositeQuery = entityManager.createNativeQuery(queryStr);
        bindParameters(compositeQuery, queryInfo.getParameters());
        return ((Number) compositeQuery.getSingleResult()).longValue();
    }

    private QueryInfo calculateQueryInfo(List<FilterCondition> conditions) {
        List<Object> parameters = new LinkedList<>();

        List<FilterCondition> searchConds = StreamUtils.filterThenToList(
                conditions, cond -> "search".equals(cond.getAttrCode())
        );
        String search = CollectionUtils.isEmpty(searchConds) ? "" :
                StringUtils.getValueJson(searchConds.get(0).getData());


        conditions.removeAll(searchConds);

        QueryInfo searchQueryInfo = calculateQueryInfoOnSearch(search);
        QueryInfo basicAttrQueryInfo = calculateQueryInfoOnBasicAttrs(conditions);

        parameters.addAll(searchQueryInfo.getParameters());
        parameters.addAll(basicAttrQueryInfo.getParameters());

        String[] predicates = Stream.of(
                searchQueryInfo.getQueryString(),
                basicAttrQueryInfo.getQueryString()
        ).filter(org.springframework.util.StringUtils::hasText).toArray(String[]::new);

        return new QueryInfo(parameters, String.join(" AND ", predicates));
    }

    private List<WarningSettingEntity> filterElementsOnSpecificPage(QueryInfo queryInfo, Pageable pageable) {
        String queryStr = "SELECT w.* from admin_console.warning_setting w  ";
        if (!StringUtils.isNullOrEmpty(queryInfo.getQueryString())) {
            queryStr += " where " + queryInfo.getQueryString();
        }

        Sort sort = pageable.getSort();
        if (sort == null || sort.isUnsorted()) {
            sort = Sort.by(Sort.Order.desc("modified_at"));
        }
        Optional<Sort.Order> optionalOrder = sort.stream().findFirst();

        if (optionalOrder.isPresent()) {
            Sort.Order order = optionalOrder.get();
            queryStr = queryStr + " ORDER BY w." + order.getProperty() + " " + order.getDirection() + " NULLS LAST";
        }
        Query compositeQuery = entityManager.createNativeQuery(queryStr, WarningSettingEntity.class);
        bindParameters(compositeQuery, queryInfo.getParameters());
        if (pageable.isPaged()) {
            compositeQuery.setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize());
        }

        return compositeQuery.getResultList();
    }

    private void bindParameters(Query compositeQuery, List<Object> parameters) {
        for (int i = 1; i <= parameters.size(); i++) {
            compositeQuery.setParameter(i, parameters.get(i - 1));
        }
    }

    private QueryInfo calculateQueryInfoOnBasicAttrs(List<FilterCondition> conditions) {
        List<String> predicates = new LinkedList<>();
        List<Object> parameters = new LinkedList<>();

        conditions.forEach(cond -> {
            FilterOperator operator = cond.getOperator();
            String predicate = MAP_PREDICATE_BY_OPERATOR.get(operator);
            Class<?> dataType = MAP_TYPE_BY_ATTR.get(cond.getAttrCode());
            ValueWrapper.ListValueWrapper<?> typedData = ValueWrapper.listInstanceOf(dataType, cond.getData());

            if (operator == FilterOperator.MULTI_SELECTION_EQUALS) {
                parameters.addAll(typedData.getValue());
                parameters.addAll(typedData.getValue());
            } else if (operator == FilterOperator.STRING_CONTAINS) {
                List<String> patterns = StreamUtils.toList(typedData.getValue(), s -> SearchUtils.createSearchPatternFrom((String) s));
                parameters.addAll(patterns);

                String[] likePredicates = new String[patterns.size()];
                Arrays.fill(likePredicates, predicate);
                predicate = "( " + String.join(" OR ", likePredicates) + " )";
            } else if (operator == FilterOperator.STRING_EQUALS ||
                    operator == FilterOperator.STRING_NOT_EQUALS) {
                List<String> valueInUpperCase = StreamUtils.toList(typedData.getValue(), s -> ((String) s).toUpperCase());
                parameters.addAll(valueInUpperCase);
            } else {
                parameters.addAll(typedData.getValue());
            }

            String[] paramMakers = new String[typedData.getValue().size()];
            Arrays.fill(paramMakers, "?");
            String paramsStr = String.join(",", paramMakers);
            predicate = predicate.replaceAll(":table_alias", "w");
            predicate = predicate.replaceAll(":params", paramsStr);
            predicate = "( " + predicate.replaceAll(":attribute", cond.getAttrCode()) + " )";
            predicates.add(predicate);
        });

        return new QueryInfo(parameters, String.join("AND", predicates));
    }

    private QueryInfo calculateQueryInfoOnSearch(String search) {
        if (!org.springframework.util.StringUtils.hasText(search)) return new QueryInfo(Collections.emptyList(), "");

        String queryStr = " upper(warning_channel) ILIKE ? or (warning_objects @> jsonb_build_array('" + search + "')) ";

        String pattern = SearchUtils.createSearchPatternFrom(search);
        List<Object> parameters = org.springframework.util.StringUtils.hasText(pattern) ?
                List.of(pattern) : List.of("");

        return new QueryInfo(parameters, "(" + queryStr + ")");
    }

}
