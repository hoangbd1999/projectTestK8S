package com.elcom.adminconsolebackend.service.impl.metrics;

import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

public class ProcessIOUsageEstimator {

    private final SystemInfo si = new SystemInfo();
    private final HardwareAbstractionLayer hal = si.getHardware();
    private final OperatingSystem os = si.getOperatingSystem();
    private final int pid = (int) ProcessHandle.current().pid();

    // Lưu giá trị lần trước
    private long prevDiskProcessRead = 0;
    private long prevDiskProcessWrite = 0;
    private long prevDiskTotalRead = 0;
    private long prevDiskTotalWrite = 0;

    private long prevNetProcessSent = 0;
    private long prevNetProcessRecv = 0;
//    private long prevNetTotalSpeed = 1; // avoid div-by-zero

    private long lastUpdateTimestamp = System.currentTimeMillis();

    public double getProcessDiskUsagePercent() {
        long now = System.currentTimeMillis();
//        long interval = now - lastUpdateTimestamp;

        OSProcess process = os.getProcess(pid);
        if (process == null) {
            return 0.0;
        }

        long procRead = process.getBytesRead();
        long procWrite = process.getBytesWritten();

        long totalDiskRead = 0;
        long totalDiskWrite = 0;

        for (HWDiskStore disk : hal.getDiskStores()) {
            disk.updateAttributes();
            totalDiskRead += disk.getReadBytes();
            totalDiskWrite += disk.getWriteBytes();
        }

        long procDelta = (procRead - prevDiskProcessRead) + (procWrite - prevDiskProcessWrite);
        long totalDelta = (totalDiskRead - prevDiskTotalRead) + (totalDiskWrite - prevDiskTotalWrite);

        // Update previous values
        prevDiskProcessRead = procRead;
        prevDiskProcessWrite = procWrite;
        prevDiskTotalRead = totalDiskRead;
        prevDiskTotalWrite = totalDiskWrite;
        lastUpdateTimestamp = now;

        if (totalDelta == 0) {
            return 0.0;
        }

        return (double) procDelta / totalDelta * 100;
    }

    public double getProcessNetworkUsagePercent() {
        long now = System.currentTimeMillis();
//        long interval = now - lastUpdateTimestamp;

        OSProcess process = os.getProcess(pid);
        if (process == null) {
            return 0.0;
        }

        long procSent = process.getBytesWritten();
        long procRecv = process.getBytesRead();

        long totalInterfaceSpeedBits = 0;
        for (NetworkIF net : hal.getNetworkIFs()) {
            net.updateAttributes();
            totalInterfaceSpeedBits += net.getSpeed(); // bits per second
        }

        long procDeltaBytes = (procSent - prevNetProcessSent) + (procRecv - prevNetProcessRecv);
        double procDeltaBits = procDeltaBytes * 8.0;
        double seconds = (System.currentTimeMillis() - lastUpdateTimestamp) / 1000.0;

        // bits per second used
        double processBps = procDeltaBits / seconds;

        prevNetProcessSent = procSent;
        prevNetProcessRecv = procRecv;
//        prevNetTotalSpeed = totalInterfaceSpeedBits;
        lastUpdateTimestamp = now;

        if (totalInterfaceSpeedBits == 0) {
            return 0.0;
        }

        return processBps / totalInterfaceSpeedBits * 100;
    }
}
