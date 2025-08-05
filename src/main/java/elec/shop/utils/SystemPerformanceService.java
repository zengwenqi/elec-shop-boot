package elec.shop.utils;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;

public class SystemPerformanceService {

    public static String getSystemPerformance() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

        // CPU 使用率（采样 1 秒）
        CentralProcessor processor = hal.getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

        // 内存使用率
        GlobalMemory memory = hal.getMemory();
        long totalMem = memory.getTotal();
        long usedMem = totalMem - memory.getAvailable();
        double memUsage = (double) usedMem / totalMem * 100;

        // 磁盘使用率
        FileSystem fileSystem = si.getOperatingSystem().getFileSystem();
        long totalDisk = 0;
        long usedDisk = 0;
        for (OSFileStore store : fileSystem.getFileStores()) {
            totalDisk += store.getTotalSpace();
            usedDisk += store.getTotalSpace() - store.getUsableSpace();
        }
        double diskUsage = (double) usedDisk / totalDisk * 100;

        // 综合评分（加权平均，可自定义）
        double score = 100 - (cpuLoad * 0.4 + memUsage * 0.3 + diskUsage * 0.3);
        score = Math.max(0, Math.min(100, score));

        return String.format("%.1f%%", score);
    }

    public static void main(String[] args) {
        System.out.println("系统性能评分: " + getSystemPerformance());
    }
}
