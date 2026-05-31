package com.elcom.adminconsolebackend.service.impl.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
//import oshi.hardware.NetworkIF;
//import oshi.hardware.HWDiskStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
//import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
//import oshi.hardware.CentralProcessor;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

@Component
public class SystemMetricsService {
    
    private final MeterRegistry registry;

    private final SystemInfo systemInfo = new SystemInfo();
    private final HardwareAbstractionLayer hal = systemInfo.getHardware();
    private final OperatingSystem os = systemInfo.getOperatingSystem();
//    private final CentralProcessor processor = hal.getProcessor();
    private final GlobalMemory memory = hal.getMemory();

//    private final long[] prevSystemCpuTicks = processor.getSystemCpuLoadTicks();
    private final int currentPid = (int) ProcessHandle.current().pid();
    private OSProcess previousProcess = os.getProcess(currentPid);
    
    ProcessIOUsageEstimator estimator = new ProcessIOUsageEstimator();

    // ===================== SYSTEM-WIDE =====================
//    private final AtomicReference<Double> systemCpuPercent = new AtomicReference<>(0.0);
//    private final AtomicReference<Double> systemRamPercent = new AtomicReference<>(0.0);
//    private final AtomicLong systemMemoryUsed = new AtomicLong(0);
//    private final AtomicLong systemMemoryTotal = new AtomicLong(1);

//    private final AtomicLong diskReadBytes = new AtomicLong(0);
//    private final AtomicLong diskWriteBytes = new AtomicLong(0);
//    private final AtomicLong netRecvBytes = new AtomicLong(0);
//    private final AtomicLong netSentBytes = new AtomicLong(0);

    // ===================== PROCESS (JVM) =====================
    private final AtomicReference<Double> processCpuPercent = new AtomicReference<>(0.0);
    private final AtomicReference<Double> processRamPercent = new AtomicReference<>(0.0);
    private final AtomicReference<Double> processDiskPercent = new AtomicReference<>(0.0);
    private final AtomicReference<Double> processNetworkPercent = new AtomicReference<>(0.0);
//    private final AtomicLong processMemoryUsed = new AtomicLong(0);

    public SystemMetricsService(MeterRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void registerGauges() {
        
        // System RAM
//        Gauge.builder("system_memory_usage_percent", systemRamPercent, AtomicReference::get)
//                .description("RAM usage percent of the whole system")
//                .baseUnit("percent")
//                .register(registry);
//
//        Gauge.builder("system_memory_used_bytes", systemMemoryUsed, AtomicLong::get)
//                .description("Used memory in bytes (system)")
//                .baseUnit("bytes")
//                .register(registry);
//
//        Gauge.builder("system_memory_total_bytes", systemMemoryTotal, AtomicLong::get)
//                .description("Total memory in bytes (system)")
//                .baseUnit("bytes")
//                .register(registry);

        // System CPU
//        Gauge.builder("system_cpu_usage_percent", systemCpuPercent, AtomicReference::get)
//                .description("CPU usage percent of the system")
//                .baseUnit("percent")
//                .register(registry);

        // Disk
//        Gauge.builder("system_disk_read_bytes", diskReadBytes, AtomicLong::get)
//                .description("Total bytes read from disk")
//                .baseUnit("bytes")
//                .register(registry);
//
//        Gauge.builder("system_disk_write_bytes", diskWriteBytes, AtomicLong::get)
//                .description("Total bytes written to disk")
//                .baseUnit("bytes")
//                .register(registry);
//
//        // Network
//        Gauge.builder("system_network_received_bytes", netRecvBytes, AtomicLong::get)
//                .description("Total bytes received over network")
//                .baseUnit("bytes")
//                .register(registry);
//
//        Gauge.builder("system_network_sent_bytes", netSentBytes, AtomicLong::get)
//                .description("Total bytes sent over network")
//                .baseUnit("bytes")
//                .register(registry);

//        Gauge.builder("process_memory_used_bytes", processMemoryUsed, AtomicLong::get)
//                .description("Resident Set Size (RSS) of this Java process")
//                .baseUnit("bytes")
//                .register(registry);

        // Process CPU
        Gauge.builder("process_cpu_usage_percent", processCpuPercent, AtomicReference::get)
                .description("CPU usage percent of this Java process")
                .baseUnit("percent")
                .register(registry);
        
        // Process RAM
        Gauge.builder("process_memory_usage_percent", processRamPercent, AtomicReference::get)
                .description("RAM usage percent of this Java process")
                .baseUnit("percent")
                .register(registry);
        
        Gauge.builder("process_disk_usage_percent", processDiskPercent, AtomicReference::get)
                .description("Disk IO usage percent of this Java process")
                .baseUnit("percent")
                .register(registry);

        Gauge.builder("process_network_usage_percent", processNetworkPercent, AtomicReference::get)
                .description("Network usage percent of this Java process")
                .baseUnit("percent")
                .register(registry);
    }

    @Scheduled(fixedRate = 20000L)
    public void updateMetrics() {
        
//        updateSystemCpu();
//        updateSystemMemory();

//        updateDiskIO();
//        updateNetworkIO();

        updateProcessMetrics();
    }

//    private void updateSystemCpu() {
//        long[] currentTicks = processor.getSystemCpuLoadTicks();
////        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevSystemCpuTicks) * 100.0;
//        System.arraycopy(currentTicks, 0, prevSystemCpuTicks, 0, prevSystemCpuTicks.length);
////        systemCpuPercent.set(cpuLoad);
//    }

//    private void updateSystemMemory() {
//        long total = memory.getTotal();
//        long available = memory.getAvailable();
//        long used = total - available;
////        systemMemoryTotal.set(total);
////        systemMemoryUsed.set(used);
////        systemRamPercent.set((double) used / total * 100);
//    }

//    private void updateDiskIO() {
//        long totalRead = 0;
//        long totalWrite = 0;
//        for (HWDiskStore disk : hal.getDiskStores()) {
//            disk.updateAttributes();
//            totalRead += disk.getReadBytes();
//            totalWrite += disk.getWriteBytes();
//        }
//        diskReadBytes.set(totalRead);
//        diskWriteBytes.set(totalWrite);
//    }
//
//    private void updateNetworkIO() {
//        long recv = 0;
//        long sent = 0;
//        for (NetworkIF net : hal.getNetworkIFs()) {
//            net.updateAttributes();
//            recv += net.getBytesRecv();
//            sent += net.getBytesSent();
//        }
//        netRecvBytes.set(recv);
//        netSentBytes.set(sent);
//    }

    private void updateProcessMetrics() {
        
        OSProcess currentProcess = os.getProcess(currentPid);
        if (currentProcess != null && previousProcess != null) {
            double cpuPercent = currentProcess.getProcessCpuLoadBetweenTicks(previousProcess) * 100.0;
            long rss = currentProcess.getResidentSetSize();
            double ramPercent = (double) rss / memory.getTotal() * 100;

            processCpuPercent.set(cpuPercent);
//            processMemoryUsed.set(rss);
            processRamPercent.set(ramPercent);

            previousProcess = currentProcess;
        }
        
        double diskPercent = estimator.getProcessDiskUsagePercent();
        double netPercent = estimator.getProcessNetworkUsagePercent();
        processDiskPercent.set(diskPercent);
        processNetworkPercent.set(netPercent);
    }
}
