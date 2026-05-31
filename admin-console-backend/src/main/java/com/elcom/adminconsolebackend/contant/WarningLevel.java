package com.elcom.adminconsolebackend.contant;

public enum WarningLevel {
    ///////////////////// SERVER ///////////////////////////////
// server down
    serverDown,
// cpu server
    LowCPUUsage,
    WarningCPUUsage,
    HighCPUUsage,

// ram server
    LowMemoryUsage,
    WarningMemoryUsage,
    HighMemoryUsage,

// disk server
    LowDiskUsage,
    WarningDiskUsage,
    HighDiskUsage,

// temperature server
    LowTemperature,
    WarningTemperature,
    HighTemperature,

    ///////////////////// SERVICE ////////////////////////////////
// cpu service
    LowCPUServiceUsage,
    WarningCPUServiceUsage,
    HighCPUServiceUsage,

// heap memory service
    LowHeapMemoryServiceUsage,
    WarningHeapMemoryServiceUsage,
    HighHeapMemoryServiceUsage,
    LowMemoryServiceUsage,
    WarningMemoryServiceUsage,
    HighMemoryServiceUsage,

// non-heap memory service
    LowNonHeapMemoryServiceUsage,
    WarningNonHeapMemoryServiceUsage,
    HighNonHeapMemoryServiceUsage,

// duration
    LowDurationServiceUsage,
    WarningDurationServiceUsage,
    HighDurationServiceUsage,

    ///////////////////// DATABASE ////////////////////////////////
    // --------- ClickHouse --------//
// memory
    LowMemoryClickHouseUsage,
    WarningMemoryClickHouseUsage,
    CriticalMemoryClickHouseUsage,

// connection TCP
    LowConnectionTCPClickHouseUsage,
    WarningConnectionTCPClickHouseUsage,
    CriticalConnectionTCPClickHouseUsage,

// connection HTTP
    LowConnectionHTTPClickHouseUsage,
    WarningConnectionHTTPClickHouseUsage,
    CriticalConnectionHTTPClickHouseUsage,

// query
    LowQueryClickHouseUsage,
    WarningQueryClickHouseUsage,
    CriticalQueryClickHouseUsage,

    // --------- Postgre --------//
// cpu
    LowCpuPostgresUsage,
    WarningCpuPostgresUsage,
    CriticalCpuPostgresUsage,

// memory
    LowMemoryPostgresUsage,
    WarningMemoryPostgresUsage,
    CriticalMemoryPostgresUsage,

    // --------- Redis --------//

// memory
    LowMemoryRedisUsage,
    WarningMemoryRedisUsage,
    CriticalMemoryRedisUsage

}
