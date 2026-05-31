package com.elcom.adminconsolebackend.repository.clickhouse;

import com.elcom.adminconsolebackend.dto.CountDecodeLevelDTO;
import com.elcom.adminconsolebackend.dto.CountRecordByDateDTO;
import com.elcom.adminconsolebackend.dto.CountStationResponseDTO;
import com.elcom.adminconsolebackend.util.StringUtils;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;


import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Admin
 */
@Repository
public class MediaReportCustomizeRepository extends BaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            MediaReportCustomizeRepository.class);

    @Autowired
    public MediaReportCustomizeRepository(
            @Qualifier("clickhouseSourceEntityManagerFactory") EntityManagerFactory factory,DataSource dataSource) {
        super(factory, dataSource);
    }

    public Long countNumberRecordInDay(String startTime, String endTime, String year, String monthStart, String dayStart, String monthEnd) {
        Session session = openSession();
        try {
            String sql = "";
            if(StringUtils.isNullOrEmpty(monthEnd)) {
                sql = " SELECT COUNT() as count FROM file('beidou/not-decoded-raws/year="+ year +"/month="+ monthStart +"/date="+ dayStart +"/hour=*/*.parquet', 'Parquet') " +
                        " WHERE timestamp BETWEEN :startTime AND :endTime ";
            } else {
                sql = " SELECT COUNT()  FROM  " +
                        " ( SELECT * FROM file('beidou/not-decoded-raws/year="+ year +"/month="+ monthStart +"/date=*/hour=*/*.parquet', 'Parquet') " +
                        "  UNION ALL " +
                        "  SELECT * FROM file('beidou/not-decoded-raws/year="+ year +"/month="+ monthEnd +"/date=*/hour=*/*.parquet', 'Parquet') " +
                        " ) as count  " +
                        " WHERE timestamp BETWEEN :startTime AND :endTime ";
            }

            NativeQuery query = session.createNativeQuery(sql);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);

            return ((BigDecimal) query.getSingleResult()).longValue();
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        } finally {
            closeSession(session);
        }
        return null;
    }

    public CountDecodeLevelDTO countDecodeLevel(String startTime, String endTime, String year, String monthStart, String monthEnd) {
        Session session = openSession();
        try {
//            String sql = " SELECT count(*) AS total, " +
//                    " countIf(service = 1) AS undefined, " +
//                    " countIf(service = 6) AS message, " +
//                    " countIf(service NOT IN (1, 6)) AS position " +
//                    " FROM file('beidou/not-decoded-raws/year=*/month=*/date=*/hour=*/*.parquet', 'Parquet') " +
//                    " WHERE timestamp BETWEEN :startTime AND :endTime ";

            String sql = "";
            if(monthStart.equalsIgnoreCase(monthEnd)) {
                 sql = " SELECT count(*) AS total, " +
                        " countIf(service = 1) AS undefined, " +
                        " countIf(service = 6) AS message, " +
                        " countIf(service NOT IN (1, 6)) AS position " +
                        " FROM file('beidou/not-decoded-raws/year=" + year + "/month="+ monthStart +"/date=*/hour=*/*.parquet', 'Parquet') " +
                        " WHERE timestamp BETWEEN :startTime AND :endTime ";
            } else {
                sql = " SELECT count(*) AS total, " +
                        " countIf(service = 1) AS undefined, " +
                        " countIf(service = 6) AS message, " +
                        " countIf(service NOT IN (1, 6)) AS position " +
                        " FROM ( " +
                        " SELECT * FROM file('beidou/not-decoded-raws/year=" + year + "/month=" + monthStart + "/date=*/hour=*/*.parquet', 'Parquet') " +
                        " UNION ALL " +
                        " SELECT * FROM file('beidou/not-decoded-raws/year=" + year + "/month=" + monthEnd + "/date=*/hour=*/*.parquet', 'Parquet') " +
                        " ) AS combined " +
                        " WHERE timestamp BETWEEN :startTime AND :endTime ";
            }


                NativeQuery<Object[]> query = session.createNativeQuery(sql);
                query.setParameter("startTime", startTime);
                query.setParameter("endTime", endTime);

                Object[] row = (Object[]) query.getSingleResult();

                CountDecodeLevelDTO dto = new CountDecodeLevelDTO();
                dto.setTotal((row[0] != null) ? ((Number) row[0]).longValue() : 0L);
                dto.setUndefined((row[1] != null) ? ((Number) row[1]).longValue() : 0L);
                dto.setMessage((row[2] != null) ? ((Number) row[2]).longValue() : 0L);
                dto.setPosition((row[3] != null) ? ((Number) row[3]).longValue() : 0L);
                return dto;

        } catch (Exception ex) {
            LOGGER.error("Error when countDecodeLevel", ex);
        } finally {
            closeSession(session);
        }
        return null;
    }

    public List<CountStationResponseDTO> countStation(String startTime, String endTime, String year, String monthStart, String monthEnd) {
        Session session = openSession();
        try {
//            String sql = " SELECT station, " +
//                    " count(*) AS countRecord, " +
//                    " round(count(*) * 100.0 / sum(count(*)) OVER (), 2) AS percent " +
//                    " FROM file('beidou/not-decoded-raws/year=*/month=*/date=*/hour=*/*.parquet', 'Parquet') " +
//                    " WHERE timestamp BETWEEN :startTime AND :endTime " +
//                    " GROUP BY station " +
//                    " ORDER BY percent DESC ";
            String sql = "";
            if(monthStart.equalsIgnoreCase(monthEnd)) {
                sql = " SELECT station, " +
                        " count(*) AS countRecord, " +
                        " round(count(*) * 100.0 / sum(count(*)) OVER (), 2) AS percent " +
                        " FROM file('beidou/not-decoded-raws/year=" + year + "/month=" + monthStart + "/date=*/hour=*/*.parquet', 'Parquet') " +
                        " WHERE timestamp BETWEEN :startTime AND :endTime " +
                        " GROUP BY station " +
                        " ORDER BY percent DESC ";
            } else {
                sql = " SELECT station, " +
                        " count(*) AS countRecord, " +
                        " round(count(*) * 100.0 / sum(count(*)) OVER (), 2) AS percent " +
                        " FROM ( " +
                        "    SELECT * FROM file('beidou/not-decoded-raws/year=" + year + "/month=" + monthStart + "/date=*/hour=*/*.parquet', 'Parquet') " +
                        "    UNION ALL " +
                        "    SELECT * FROM file('beidou/not-decoded-raws/year=" + year + "/month=" + monthEnd + "/date=*/hour=*/*.parquet', 'Parquet') " +
                        " ) AS combined " +
                        " WHERE timestamp BETWEEN :startTime AND :endTime " +
                        " GROUP BY station " +
                        " ORDER BY percent DESC ";
            }

            NativeQuery<Object[]> query = session.createNativeQuery(sql);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);

            List<Object[]> resultList = query.getResultList();
            List<CountStationResponseDTO> dtoList = new ArrayList<>();

            for (Object[] row : resultList) {
                CountStationResponseDTO dto = new CountStationResponseDTO();
                dto.setStation((row[0] != null) ? row[0].toString() : "");
                dto.setCountRecord((row[1] != null) ? ((Number) row[1]).longValue() : 0L);
                dto.setPercent((row[2] != null) ? ((Number) row[2]).doubleValue() : 0.0);
                dtoList.add(dto);
            }

            return dtoList;
        } catch (Exception ex) {
            LOGGER.error("Error when countDecodeLevel", ex);
        } finally {
            closeSession(session);
        }
        return null;
    }

    public List<CountRecordByDateDTO> countRecordByDate(String startTime, String endTime, String year, String monthStart, String monthEnd) {
        Session session = openSession();
        try {
//            String sql = " SELECT " +
//                    " toDate(timestamp / 1000000) AS day, " +
//                    " count(*) AS total, " +
//                    " countIf(service = 1) AS undefined, " +
//                    " countIf(service = 6) AS message, " +
//                    " countIf(service NOT IN (1, 6)) AS position " +
//                    " FROM file('beidou/not-decoded-raws/year=2025/month=*/date=*/hour=*/*.parquet', 'Parquet') " +
//                    " WHERE timestamp BETWEEN :startTime AND :endTime " +
//                    " GROUP BY day " +
//                    " ORDER BY day ";
            String sql = "";
            if(monthStart.equalsIgnoreCase(monthEnd)) {
//                sql = " SELECT " +
//                        " toDate(timestamp / 1000000) AS day, " +
//                        " count(*) AS total, " +
//                        " countIf(service = 1) AS undefined, " +
//                        " countIf(service = 6) AS message, " +
//                        " countIf(service NOT IN (1, 6)) AS position " +
//                        " FROM file('beidou/not-decoded-raws/year=" + year + "/month=" + monthStart + "/date=*/hour=*/*.parquet', 'Parquet') " +
//                        " WHERE timestamp BETWEEN :startTime AND :endTime " +
//                        " GROUP BY day " +
//                        " ORDER BY day ";
                sql = " WITH " +
                        "    toDate(toUInt64(:startTime) / 1000000) AS start_date, " +
                        "    toDate(toUInt64(:endTime) / 1000000) AS end_date " +
                        " SELECT " +
                        "    d AS day, " +
                        "    coalesce(stats.total, 0) AS total, " +
                        "    coalesce(stats.undefined, 0) AS undefined, " +
                        "    coalesce(stats.message, 0) AS message, " +
                        "    coalesce(stats.position, 0) AS position " +
                        " FROM ( " +
                        "    SELECT " +
                        "        toDate(timestamp / 1000000) AS day, " +
                        "        count() AS total, " +
                        "        countIf(service = 1) AS undefined, " +
                        "        countIf(service = 6) AS message, " +
                        "        countIf(service NOT IN (1, 6)) AS position " +
                        "    FROM file('beidou/not-decoded-raws/year=" + year + "/month=" + monthStart + "/date=*/hour=*/*.parquet', 'Parquet') " +
                        "    WHERE timestamp BETWEEN :startTime AND :endTime " +
                        "    GROUP BY day " +
                        " ) AS stats " +
                        " RIGHT JOIN " +
                        " ( " +
                        "    SELECT arrayJoin( " +
                        "        arrayMap(x -> toDate(start_date + x), " +
                        "                 range(dateDiff('day', start_date, end_date) + 1)) " +
                        "    ) AS d " +
                        " ) AS days " +
                        " ON stats.day = days.d " +
                        " ORDER BY day";
            } else {
//                 sql = " SELECT " +
//                        " toDate(timestamp / 1000000) AS day, " +
//                        " count(*) AS total, " +
//                        " countIf(service = 1) AS undefined, " +
//                        " countIf(service = 6) AS message, " +
//                        " countIf(service NOT IN (1, 6)) AS position " +
//                        " FROM ( " +
//                        "    SELECT * FROM file('beidou/not-decoded-raws/year=" + year + "/month=" + monthStart + "/date=*/hour=*/*.parquet', 'Parquet') " +
//                        "    UNION ALL " +
//                        "    SELECT * FROM file('beidou/not-decoded-raws/year=" + year + "/month=" + monthEnd + "/date=*/hour=*/*.parquet', 'Parquet') " +
//                        " ) AS combined " +
//                        " WHERE timestamp BETWEEN :startTime AND :endTime " +
//                        " GROUP BY day " +
//                        " ORDER BY day ";
                sql = " WITH " +
                        "    toDate(toUInt64(:startTime) / 1000000) AS start_date, " +
                        "    toDate(toUInt64(:endTime) / 1000000) AS end_date " +
                        " SELECT " +
                        "    d AS day, " +
                        "    coalesce(stats.total, 0) AS total, " +
                        "    coalesce(stats.undefined, 0) AS undefined, " +
                        "    coalesce(stats.message, 0) AS message, " +
                        "    coalesce(stats.position, 0) AS position " +
                        " FROM ( " +
                        "    SELECT " +
                        "        toDate(timestamp / 1000000) AS day, " +
                        "        count() AS total, " +
                        "        countIf(service = 1) AS undefined, " +
                        "        countIf(service = 6) AS message, " +
                        "        countIf(service NOT IN (1, 6)) AS position " +
                        " FROM ( " +
                        "    SELECT * FROM file('beidou/not-decoded-raws/year=" + year + "/month=" + monthStart + "/date=*/hour=*/*.parquet', 'Parquet') " +
                        "    UNION ALL " +
                        "    SELECT * FROM file('beidou/not-decoded-raws/year=" + year + "/month=" + monthEnd + "/date=*/hour=*/*.parquet', 'Parquet') " +
                        " ) AS combined " +
                        "    WHERE timestamp BETWEEN :startTime AND :endTime " +
                        "    GROUP BY day " +
                        " ) AS stats " +
                        " RIGHT JOIN " +
                        " ( " +
                        "    SELECT arrayJoin( " +
                        "        arrayMap(x -> toDate(start_date + x), " +
                        "                 range(dateDiff('day', start_date, end_date) + 1)) " +
                        "    ) AS d " +
                        " ) AS days " +
                        " ON stats.day = days.d " +
                        " ORDER BY day";
            }

            NativeQuery<Object[]> query = session.createNativeQuery(sql);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);

            List<Object[]> resultList = query.getResultList();
            List<CountRecordByDateDTO> dtoList = new ArrayList<>();

            for (Object[] row : resultList) {
                CountRecordByDateDTO dto = new CountRecordByDateDTO();
                dto.setDay((row[0] != null) ? (Date) row[0] : null);
                dto.setTotal((row[1] != null) ? ((Number) row[1]).longValue() : 0L);
                dto.setUndefined((row[2] != null) ? ((Number) row[2]).longValue() : 0L);
                dto.setMessage((row[3] != null) ? ((Number) row[3]).longValue() : 0L);
                dto.setPosition((row[4] != null) ? ((Number) row[4]).longValue() : 0L);
                dtoList.add(dto);
            }

            return dtoList;
        } catch (Exception ex) {
            LOGGER.error("Error when countDecodeLevel", ex);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return null;
    }

}
