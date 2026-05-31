package com.elcom.adminconsolebackend.repository.management;

import com.elcom.adminconsolebackend.dto.FilterCondition;
import com.elcom.adminconsolebackend.dto.FilterOperator;
import com.elcom.adminconsolebackend.dto.ValueWrapper;
import com.elcom.adminconsolebackend.dto.WarningMessageGeneralDTO;
import com.elcom.adminconsolebackend.util.SearchUtils;
import com.elcom.adminconsolebackend.util.SecurityUtils;
import com.elcom.adminconsolebackend.util.StreamUtils;
import com.elcom.adminconsolebackend.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Stream;

@Repository
public class WarningMessageFilterRepositoryImpl implements WarningMessageFilterRepository {
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
            Map.entry(FilterOperator.STRING_NOT_EQUALS, "(:table_alias.:attribute) NOT IN (:params)")
    );

    private static final Map<FilterOperator, String> MAP_PREDICATE_BY_OPERATOR_TIME = Map.ofEntries(
            Map.entry(FilterOperator.EQUALS, ":table_alias.:attribute IN (CAST(:params as timestamp))"),
            Map.entry(FilterOperator.NOT_EQUALS, ":table_alias.:attribute NOT IN (CAST(:params as timestamp)) OR :table_alias.:attribute IS NULL "),
            Map.entry(FilterOperator.EMPTY, ":table_alias.:attribute IS NULL"),
            Map.entry(FilterOperator.NOT_EMPTY, ":table_alias.:attribute IS NOT NULL"),
            Map.entry(FilterOperator.IN_RANGE, "CAST(? as timestamp) <= :table_alias.:attribute AND :table_alias.:attribute <= CAST(? as timestamp)"),
            Map.entry(FilterOperator.OUT_OF_RANGE, ":table_alias.:attribute < CAST(? as timestamp) OR :table_alias.:attribute > CAST(? as timestamp)"),
            Map.entry(FilterOperator.GREATER_THAN, ":table_alias.:attribute > CAST(? as timestamp)"),
            Map.entry(FilterOperator.LESS_THAN, ":table_alias.:attribute < CAST(? as timestamp)")
    );

    private static final Map<String, Class<?>> MAP_TYPE_BY_ATTR = Map.ofEntries(
            Map.entry("application", String.class),
            Map.entry("warning_level", String.class),
            Map.entry("description", String.class),
            Map.entry("warning_time", String.class)
    );

    @Getter
    @AllArgsConstructor
    private final class QueryInfo {

        private List<Object> parameters;

        private String queryString;
    }

    @Override
    public Page<WarningMessageGeneralDTO> filterWarning(List<FilterCondition> conditions, Pageable pageable, String userId) {
        QueryInfo queryInfo = calculateQueryInfo(conditions);
        List<WarningMessageGeneralDTO> elementsOnPage = filterElementsOnSpecificPage(queryInfo, pageable, userId);
        long totalElements = count(queryInfo);
        return new PageImpl<>(elementsOnPage, pageable, totalElements);
    }

    private long count(QueryInfo queryInfo) {
        String queryStr = "SELECT COUNT(*) from admin_console.warning_message_test h ";
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
        List<FilterCondition> readStatusConds = StreamUtils.filterThenToList(
                conditions, cond -> "status".equals(cond.getAttrCode())
        );
        String search = CollectionUtils.isEmpty(searchConds) ? "" :
                StringUtils.getValueJson(searchConds.get(0).getData());


        conditions.removeAll(searchConds);
        conditions.removeAll(readStatusConds);

        QueryInfo searchQueryInfo = calculateQueryInfoOnSearch(search);
        QueryInfo basicAttrQueryInfo = calculateQueryInfoOnBasicAttrs(conditions);
        QueryInfo readStatusQueryInfo = calculateQueryInfoOnReadStatus(readStatusConds, SecurityUtils.getCurrentUserId());

        parameters.addAll(searchQueryInfo.getParameters());
        parameters.addAll(basicAttrQueryInfo.getParameters());

        String[] predicates = Stream.of(
                searchQueryInfo.getQueryString(),
                basicAttrQueryInfo.getQueryString(),
                readStatusQueryInfo.getQueryString()
        ).filter(org.springframework.util.StringUtils::hasText).toArray(String[]::new);

        return new QueryInfo(parameters, String.join(" AND ", predicates));
    }

    private List<WarningMessageGeneralDTO> filterElementsOnSpecificPage(QueryInfo queryInfo, Pageable pageable, String userId) {
        String queryStr = "SELECT h.id, h.application, h.warning_level,h.description,h.warning_time ,h.instance ,h.resource_type, CASE " +
                " WHEN u.warning_id IS NOT NULL THEN 1 ELSE 0 " +
                " END AS status FROM " +
                " admin_console.warning_message_test h " +
                " LEFT JOIN " +
                " admin_console.warning_message_read u " +
                " ON h.id = u.warning_id " +
                " AND u.user_id = '"+ userId +"' ";
        if (!StringUtils.isNullOrEmpty(queryInfo.getQueryString())) {
            queryStr += " where " + queryInfo.getQueryString();
        }

        Sort sort = pageable.getSort();
        if (sort == null || sort.isUnsorted()) {
            sort = Sort.by(Sort.Order.desc("warning_time"));
        }
        Optional<Sort.Order> optionalOrder = sort.stream().findFirst();

        if (optionalOrder.isPresent()) {
            Sort.Order order = optionalOrder.get();
            queryStr = queryStr + " ORDER BY " + order.getProperty() + " " + order.getDirection() + " NULLS LAST";
        }
        Query compositeQuery = entityManager.createNativeQuery(queryStr, Tuple.class);
        bindParameters(compositeQuery, queryInfo.getParameters());
        if (pageable.isPaged()) {
            compositeQuery.setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize());
        }
        List<Tuple> result = compositeQuery.getResultList();

        return StreamUtils.toList(result, row -> {
            WarningMessageGeneralDTO warningMessageGeneralDTO = new WarningMessageGeneralDTO();
            warningMessageGeneralDTO.setId(row.get("id", String.class));
            warningMessageGeneralDTO.setApplication(row.get("application", String.class));
            warningMessageGeneralDTO.setWarningLevel(row.get("warning_level", Short.class));
            warningMessageGeneralDTO.setDescription(row.get("description", String.class));
            warningMessageGeneralDTO.setResourceType(row.get("resource_type", String.class));
            Timestamp warningTime = row.get("warning_time", Timestamp.class);
            warningMessageGeneralDTO.setWarningTime(warningTime != null ? warningTime.toLocalDateTime() : null);
            warningMessageGeneralDTO.setInstance(row.get("instance", String.class));
            warningMessageGeneralDTO.setStatus(row.get("status", Integer.class));


            return warningMessageGeneralDTO;
        });
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
            String attrCode = cond.getAttrCode();
            String predicate = null;
            if (attrCode.equalsIgnoreCase("warning_time")) {
                predicate = MAP_PREDICATE_BY_OPERATOR_TIME.get(operator);
            } else {
                predicate = MAP_PREDICATE_BY_OPERATOR.get(operator);
            }

            Class<?> dataType = MAP_TYPE_BY_ATTR.get(cond.getAttrCode());
            ValueWrapper.ListValueWrapper<?> typedData = ValueWrapper.listInstanceOf(dataType, cond.getData());

            if (operator == FilterOperator.STRING_CONTAINS) {
                List<String> patterns = StreamUtils.toList(typedData.getValue(), s -> SearchUtils.createSearchPatternFrom((String) s));
                parameters.addAll(patterns);

                String[] likePredicates = new String[patterns.size()];
                Arrays.fill(likePredicates, predicate);
                predicate = "( " + String.join(" OR ", likePredicates) + " )";
            } else if (operator == FilterOperator.STRING_EQUALS || operator == FilterOperator.STRING_NOT_EQUALS) {
                List<String> valueInUpperCase = StreamUtils.toList(typedData.getValue(), s -> ((String) s));
                parameters.addAll(valueInUpperCase);
            } else {
                parameters.addAll(typedData.getValue());
            }

            String[] paramMakers = new String[typedData.getValue().size()];
            Arrays.fill(paramMakers, "?");
            String paramsStr = String.join(",", paramMakers);
            predicate = predicate.replaceAll(":table_alias", "h");
            predicate = predicate.replaceAll(":params", paramsStr);
            predicate = "( " + predicate.replaceAll(":attribute", cond.getAttrCode()) + " )";
            predicates.add(predicate);
        });

        return new QueryInfo(parameters, String.join("AND", predicates));
    }

    private QueryInfo calculateQueryInfoOnSearch(String search) {
        if (!org.springframework.util.StringUtils.hasText(search)) return new QueryInfo(Collections.emptyList(), "");

        String queryStr = " upper(application) ILIKE ? or upper(description) ILIKE ? ";

        String pattern = SearchUtils.createSearchPatternFrom(search);
        List<Object> parameters = org.springframework.util.StringUtils.hasText(pattern) ?
                List.of(pattern,pattern) : List.of("");

        return new QueryInfo(parameters, "(" + queryStr + ")");
    }

    private QueryInfo calculateQueryInfoOnReadStatus(List<FilterCondition> readStatusConds, String userId) {
        if (CollectionUtils.isEmpty(readStatusConds)) return new QueryInfo(Collections.emptyList(), "");
        FilterCondition cond = readStatusConds.get(0);
        FilterOperator operatorNew = cond.getOperator();

        ValueWrapper.ListValueWrapper<String> typedData = ValueWrapper.listInstanceOf(String.class, cond.getData());
//        String status = typedData.getValue().get(0);
//        String operator = "READ".equals(status) ? " IN " : " NOT IN ";
        String currentUser = "";
        String operator = "";
        List<String> sizeData = typedData.getValue();
        if (!sizeData.isEmpty() && sizeData.size() < 2) {
            String status = typedData.getValue().get(0);
            if (operatorNew == FilterOperator.EQUALS) {
                operator = "READ".equals(status) ? " IN " : " NOT IN ";
                currentUser = userId;
            } else if (operatorNew == FilterOperator.NOT_EQUALS) {
                operator = !"READ".equals(status) ? " IN " : " NOT IN ";
                currentUser = userId;
            }
        } else {
            if (operatorNew == FilterOperator.EQUALS) {
                operator = " NOT IN ";
                currentUser = "";
            } else if (operatorNew == FilterOperator.NOT_EQUALS) {
                operator = " IN ";
                currentUser = "";
            }
        }
        if (operatorNew == FilterOperator.NOT_EMPTY) {
            operator = " NOT IN ";
            currentUser = "";
        } else if (operatorNew == FilterOperator.EMPTY) {
            operator = " IN ";
            currentUser = "";
        }

        String queryStr = "id " + operator + "( " +
                "SELECT warning_id FROM admin_console.warning_message_read r " +
                "WHERE r.user_id = '" + currentUser + "')";

        return new QueryInfo(List.of(), queryStr);
    }
}
