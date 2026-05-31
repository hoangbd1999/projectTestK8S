package com.elcom.adminconsolebackend.repository.management;

import com.elcom.adminconsolebackend.dto.*;
import com.elcom.adminconsolebackend.util.SearchUtils;
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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Stream;

@Repository
public class MonitorDataSourceFilterRepositoryImpl implements MonitorDataSourceFilterRepository {
    @PersistenceContext
    private EntityManager entityManager;

    private static final Map<FilterOperator, String> MAP_PREDICATE_BY_OPERATOR = Map.ofEntries(
            Map.entry(FilterOperator.EQUALS, ":attribute IN (:params)"),
            Map.entry(FilterOperator.NOT_EQUALS, ":attribute NOT IN (:params)"),
            Map.entry(FilterOperator.EMPTY, ":attribute IS NULL"),
            Map.entry(FilterOperator.NOT_EMPTY, ":attribute IS NOT NULL"),
            Map.entry(FilterOperator.IN_RANGE, "? <= :attribute AND :attribute <= ?"),
            Map.entry(FilterOperator.OUT_OF_RANGE, ":attribute < ? OR :attribute > ?"),
            Map.entry(FilterOperator.GREATER_THAN, ":attribute > ?"),
            Map.entry(FilterOperator.LESS_THAN, ":attribute < ?"),
            Map.entry(FilterOperator.STRING_CONTAINS, "(:attribute) ILIKE ?"),
            Map.entry(FilterOperator.STRING_EQUALS, "(:attribute) IN (:params)"),
            Map.entry(FilterOperator.STRING_NOT_EQUALS, "(:attribute) NOT IN (:params)")
    );

    private static final Map<FilterOperator, String> MAP_PREDICATE_BY_OPERATOR_TIME = Map.ofEntries(
            Map.entry(FilterOperator.EQUALS, ":attribute IN (CAST(:params as timestamp))"),
            Map.entry(FilterOperator.NOT_EQUALS, ":attribute NOT IN (CAST(:params as timestamp)) OR :attribute IS NULL "),
            Map.entry(FilterOperator.EMPTY, ":attribute IS NULL"),
            Map.entry(FilterOperator.NOT_EMPTY, ":attribute IS NOT NULL"),
            Map.entry(FilterOperator.IN_RANGE, "CAST(? as timestamp) <= :attribute AND :attribute <= CAST(? as timestamp)"),
            Map.entry(FilterOperator.OUT_OF_RANGE, ":attribute < CAST(? as timestamp) OR :attribute > CAST(? as timestamp)"),
            Map.entry(FilterOperator.GREATER_THAN, ":attribute > CAST(? as timestamp)"),
            Map.entry(FilterOperator.LESS_THAN, ":attribute < CAST(? as timestamp)")
    );


    private static final Map<String, Class<?>> MAP_TYPE_BY_ATTR = Map.ofEntries(
            Map.entry("id", String.class),
            Map.entry("data_source", String.class)
    );

    @Getter
    @AllArgsConstructor
    private final class QueryInfo {

        private List<Object> parameters;

        private String queryString;
    }

    @Override
    public Page<MonitorDataSourceGeneralDTO> filterMonitorDataSource(List<FilterCondition> conditions, Pageable pageable) {
        QueryInfo queryInfo = calculateQueryInfo(conditions);
        QueryInfo queryInfoFilterOther = calculateQueryInfoFilterOther(conditions);
        List<MonitorDataSourceGeneralDTO> elementsOnPage = filterElementsOnSpecificPage(queryInfo, queryInfoFilterOther, pageable);
        long totalElements = count(queryInfo, queryInfoFilterOther);
        return new PageImpl<>(elementsOnPage, pageable, totalElements);
    }

    private long count(QueryInfo queryInfo, QueryInfo queryInfoFilterOther) {
        String queryStr = "SELECT COUNT(*) as count from (SELECT date_trunc('day', time_ingest)::date AS ingest_date " +
                " FROM admin_console.monitor_data_source ";
        if (!StringUtils.isNullOrEmpty(queryInfo.getQueryString())) {
            queryStr += " where " + queryInfo.getQueryString();
        }

        queryStr += " GROUP BY ingest_date ";

        if (!StringUtils.isNullOrEmpty(queryInfoFilterOther.getQueryString())) {
            queryStr += " Having " + queryInfoFilterOther.getQueryString();
        }

        queryStr += " ) AS grouped ";

        Query compositeQuery = entityManager.createNativeQuery(queryStr);
        bindParameters(compositeQuery, queryInfo.getParameters());
        return ((Number) compositeQuery.getSingleResult()).longValue();
    }

    private QueryInfo calculateQueryInfoFilterOther(List<FilterCondition> conditions) {
        List<Object> parameters = new LinkedList<>();

        List<FilterCondition> numberRecordConds = StreamUtils.filterThenToList(
                conditions, cond -> "number_record".equals(cond.getAttrCode())
        );
        List<FilterCondition> sizeConds = StreamUtils.filterThenToList(
                conditions, cond -> "size".equals(cond.getAttrCode())
        );

        QueryInfo numberRecordQueryInfo = calculateQueryInfoOnNumberRecord(numberRecordConds);
        QueryInfo sizeQueryInfo = calculateQueryInfoOnSize(sizeConds);

        String[] predicates = Stream.of(
                numberRecordQueryInfo.getQueryString(),
                sizeQueryInfo.getQueryString()
        ).filter(org.springframework.util.StringUtils::hasText).toArray(String[]::new);

        return new QueryInfo(parameters, String.join(" AND ", predicates));
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

    private QueryInfo calculateQueryInfoOnNumberRecord(List<FilterCondition> numberRecordConds) {
        if (CollectionUtils.isEmpty(numberRecordConds)) return new QueryInfo(Collections.emptyList(), "");

        List<String> parameters = new ArrayList<>();
        numberRecordConds.forEach(cond -> {
            ValueWrapper.ListValueWrapper<String> typedData = ValueWrapper.listInstanceOf(String.class, cond.getData());
            parameters.addAll(typedData.getValue());
        });

      //  String paramsStr = SearchUtils.createSearchConcatObjCode(parameters);

        boolean hasEqualCond = numberRecordConds.stream().anyMatch(cond -> FilterOperator.EQUALS == cond.getOperator());
        String stringEqualQueryStr = !hasEqualCond ? "" :
                " SUM(number_record) = " + parameters.get(0) + " ";

        boolean hasNotEqualsCond = numberRecordConds.stream().anyMatch(cond -> FilterOperator.NOT_EQUALS == cond.getOperator());
        String stringNotEqualsQueryStr =  !hasNotEqualsCond ? "" :
                " SUM(number_record) != " + parameters.get(0) + " ";

        boolean hasEmptyCond = numberRecordConds.stream().anyMatch(cond -> FilterOperator.EMPTY == cond.getOperator());
        String emptyCondQueryStr = !hasEmptyCond ? "" :
                " SUM(number_record) is null ";

        boolean hasNotEmptyCond = numberRecordConds.stream().anyMatch(cond -> FilterOperator.NOT_EMPTY == cond.getOperator());
        String notEmptyCondQueryStr = !hasNotEmptyCond ? "" :
                " SUM(number_record) is not null ";

        boolean hasInRangeCond = numberRecordConds.stream().anyMatch(cond -> FilterOperator.IN_RANGE == cond.getOperator());
        String stringInRangeQueryStr = !hasInRangeCond ? "" :
                " SUM(number_record) >= " + parameters.get(0) + " AND SUM(number_record) <= " + parameters.get(1) + " ";

        boolean hasOutOfRangeCond = numberRecordConds.stream().anyMatch(cond -> FilterOperator.OUT_OF_RANGE == cond.getOperator());
        String stringOutOfRangeQueryStr = !hasOutOfRangeCond ? "" :
                " SUM(number_record) <= " + parameters.get(0) + " AND SUM(number_record) >= " + parameters.get(1) + " ";

        boolean hasGreaterCond = numberRecordConds.stream().anyMatch(cond -> FilterOperator.GREATER_THAN == cond.getOperator());
        String stringGreaterQueryStr = !hasGreaterCond ? "" :
                " SUM(number_record) > " + parameters.get(0) + " ";

        boolean hasLessCond = numberRecordConds.stream().anyMatch(cond -> FilterOperator.LESS_THAN == cond.getOperator());
        String stringLessQueryStr = !hasLessCond ? "" :
                " SUM(number_record) < " + parameters.get(0) + " ";

        String[] queryStrs = Stream.of(stringEqualQueryStr, stringNotEqualsQueryStr, emptyCondQueryStr,
                notEmptyCondQueryStr, stringInRangeQueryStr, stringOutOfRangeQueryStr, stringGreaterQueryStr, stringLessQueryStr)
                .filter(org.springframework.util.StringUtils::hasText)
                .toArray(String[]::new);
        return new QueryInfo(null, String.join(" AND ", queryStrs));
    }

    private QueryInfo calculateQueryInfoOnSize(List<FilterCondition> sizeConds) {
        if (CollectionUtils.isEmpty(sizeConds)) return new QueryInfo(Collections.emptyList(), "");

        List<String> parameters = new ArrayList<>();
        sizeConds.forEach(cond -> {
            ValueWrapper.ListValueWrapper<String> typedData = ValueWrapper.listInstanceOf(String.class, cond.getData());
            parameters.addAll(typedData.getValue());
        });

        //  String paramsStr = SearchUtils.createSearchConcatObjCode(parameters);

        boolean hasEqualCond = sizeConds.stream().anyMatch(cond -> FilterOperator.EQUALS == cond.getOperator());
        String stringEqualQueryStr = !hasEqualCond ? "" :
                " SUM(size::BIGINT) = " + parameters.get(0) + " ";

        boolean hasNotEqualsCond = sizeConds.stream().anyMatch(cond -> FilterOperator.NOT_EQUALS == cond.getOperator());
        String stringNotEqualsQueryStr =  !hasNotEqualsCond ? "" :
                " SUM(size::BIGINT) != " + parameters.get(0) + " ";

        boolean hasEmptyCond = sizeConds.stream().anyMatch(cond -> FilterOperator.EMPTY == cond.getOperator());
        String emptyCondQueryStr = !hasEmptyCond ? "" :
                " SUM(size::BIGINT) is null ";

        boolean hasNotEmptyCond = sizeConds.stream().anyMatch(cond -> FilterOperator.NOT_EMPTY == cond.getOperator());
        String notEmptyCondQueryStr = !hasNotEmptyCond ? "" :
                " SUM(size::BIGINT) is not null ";

        boolean hasInRangeCond = sizeConds.stream().anyMatch(cond -> FilterOperator.IN_RANGE == cond.getOperator());
        String stringInRangeQueryStr = !hasInRangeCond ? "" :
                " SUM(size::BIGINT) >= " + parameters.get(0) + " AND SUM(size::BIGINT) <= " + parameters.get(1) + " ";

        boolean hasOutOfRangeCond = sizeConds.stream().anyMatch(cond -> FilterOperator.OUT_OF_RANGE == cond.getOperator());
        String stringOutOfRangeQueryStr = !hasOutOfRangeCond ? "" :
                " SUM(size::BIGINT) <= " + parameters.get(0) + " AND SUM(size::BIGINT) >= " + parameters.get(1) + " ";

        boolean hasGreaterCond = sizeConds.stream().anyMatch(cond -> FilterOperator.GREATER_THAN == cond.getOperator());
        String stringGreaterQueryStr = !hasGreaterCond ? "" :
                " SUM(size::BIGINT) > " + parameters.get(0) + " ";

        boolean hasLessCond = sizeConds.stream().anyMatch(cond -> FilterOperator.LESS_THAN == cond.getOperator());
        String stringLessQueryStr = !hasLessCond ? "" :
                " SUM(size::BIGINT) < " + parameters.get(0) + " ";

        String[] queryStrs = Stream.of(stringEqualQueryStr, stringNotEqualsQueryStr, emptyCondQueryStr,
                notEmptyCondQueryStr, stringInRangeQueryStr, stringOutOfRangeQueryStr, stringGreaterQueryStr, stringLessQueryStr)
                .filter(org.springframework.util.StringUtils::hasText)
                .toArray(String[]::new);
        return new QueryInfo(null, String.join(" AND ", queryStrs));
    }

    private List<MonitorDataSourceGeneralDTO> filterElementsOnSpecificPage(QueryInfo queryInfo, QueryInfo queryInfoFilterOther, Pageable pageable) {
        String queryStr = "SELECT gen_random_uuid()::text AS id, data_source, " +
                " MAX(status) AS status, " +
                " MAX(time_ingest) AS latest_time_ingest, " +
                " date_trunc('day', time_ingest)::date AS ingest_date, " +
             //   " SUM(number_record) AS total_number_record, " +
                " SUM(size::BIGINT) AS total_size, " +
                " COUNT(*) AS row_count " +
                " FROM admin_console.monitor_data_source ";
        if (!StringUtils.isNullOrEmpty(queryInfo.getQueryString())) {
            queryStr += " where " + queryInfo.getQueryString();
        }

        queryStr += " GROUP BY data_source, ingest_date ";

        if (!StringUtils.isNullOrEmpty(queryInfoFilterOther.getQueryString())) {
            queryStr += " Having " + queryInfoFilterOther.getQueryString();
        }
        Sort sort = pageable.getSort();
        if (sort == null || sort.isUnsorted()) {
            sort = Sort.by(Sort.Order.desc("ingest_date"));
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
            MonitorDataSourceGeneralDTO monitorDataSourceGeneralDTO = new MonitorDataSourceGeneralDTO();
            monitorDataSourceGeneralDTO.setId(row.get("id", String.class));
            monitorDataSourceGeneralDTO.setDataSource(row.get("data_source", String.class));
            monitorDataSourceGeneralDTO.setStatus(row.get("status", Short.class));
            Timestamp latestTimeIngest = row.get("latest_time_ingest", Timestamp.class);
            monitorDataSourceGeneralDTO.setLatestTimeIngest(latestTimeIngest != null ? latestTimeIngest.toLocalDateTime() : null);
            Date ingestDate = row.get("ingest_date", Date.class);
            monitorDataSourceGeneralDTO.setIngestDate(ingestDate);
        //    monitorDataSourceGeneralDTO.setNumberRecord(row.get("total_number_record", Long.class));
            monitorDataSourceGeneralDTO.setSize(row.get("total_size", BigDecimal.class));
            monitorDataSourceGeneralDTO.setRowCount(row.get("row_count", Long.class));

            return monitorDataSourceGeneralDTO;
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
            if (!cond.getAttrCode().equals("number_record") && !cond.getAttrCode().equals("size")) {
                FilterOperator operator = cond.getOperator();
                String attrCode = cond.getAttrCode();
                String predicate = null;
                if (attrCode.equalsIgnoreCase("time_ingest")) {
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
                predicate = predicate.replaceAll(":params", paramsStr);
                predicate = "( " + predicate.replaceAll(":attribute", cond.getAttrCode()) + " )";
                predicates.add(predicate);
            }
        });

        return new QueryInfo(parameters, String.join("AND", predicates));
    }

    private QueryInfo calculateQueryInfoOnSearch(String search) {
        if (!org.springframework.util.StringUtils.hasText(search)) return new QueryInfo(Collections.emptyList(), "");

        String queryStr = " upper(data_source) ILIKE ? ";

        String pattern = SearchUtils.createSearchPatternFrom(search);
        List<Object> parameters = org.springframework.util.StringUtils.hasText(pattern) ?
                List.of(pattern) : List.of("");

        return new QueryInfo(parameters, "(" + queryStr + ")");
    }

}
