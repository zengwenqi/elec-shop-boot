package elec.shop.utils;

import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.hardware.HWDiskStore;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于 OSHI 的服务器性能监控工具类
 */
@Component
public class ServerMonitorUtil {

    private static final SystemInfo SYSTEM_INFO = new SystemInfo();
    private static final HardwareAbstractionLayer HARDWARE = SYSTEM_INFO.getHardware();
    private static final OperatingSystem OS = SYSTEM_INFO.getOperatingSystem();

    /**
     * 获取 CPU 相关信息
     *
     * @return 包含 CPU 核心数、使用率等信息的 Map
     */
    public Map<String, Object> getCpuInfo() {
        Map<String, Object> cpuMap = new HashMap<>();
        CentralProcessor processor = HARDWARE.getProcessor();

        // CPU 核心数（逻辑核心）
        int logicalProcessorCount = processor.getLogicalProcessorCount();
        // CPU 核心数（物理核心）
        int physicalProcessorCount = processor.getPhysicalProcessorCount();
        cpuMap.put("logicalCoreCount", logicalProcessorCount);
        cpuMap.put("physicalCoreCount", physicalProcessorCount);

        // CPU 使用率（需等待一段时间计算）
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000); // 等待 1 秒，确保获取准确的使用率
        double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        cpuMap.put("usage", round(cpuUsage)); // 保留两位小数

        // CPU 型号
        cpuMap.put("model", processor.getProcessorIdentifier().getName());

        return cpuMap;
    }

    /**
     * 获取内存相关信息
     *
     * @return 包含总内存、可用内存、使用率等信息的 Map
     */
    public Map<String, Object> getMemoryInfo() {
        Map<String, Object> memoryMap = new HashMap<>();
        GlobalMemory memory = HARDWARE.getMemory();

        // 总内存（单位：GB）
        double totalMem = bytesToGB(memory.getTotal());
        // 可用内存（单位：GB）
        double availableMem = bytesToGB(memory.getAvailable());
        // 已使用内存
        double usedMem = totalMem - availableMem;
        // 内存使用率
        double memUsage = (usedMem / totalMem) * 100;

        memoryMap.put("totalGB", round(totalMem));
        memoryMap.put("availableGB", round(availableMem));
        memoryMap.put("usedGB", round(usedMem));
        memoryMap.put("usage", round(memUsage));

        return memoryMap;
    }

    /**
     * 获取磁盘相关信息（取第一个磁盘）
     *
     * @return 包含磁盘总容量、已使用容量、使用率等信息的 Map
     */
    public Map<String, Object> getDiskInfo() {
        Map<String, Object> diskMap = new HashMap<>();
        FileSystem fileSystem = OS.getFileSystem();
        List<OSFileStore> fileStores = fileSystem.getFileStores();

        if (fileStores.isEmpty()) {
            return diskMap;
        }

        // 取第一个文件系统（或遍历所有）
        OSFileStore store = fileStores.get(0);

        long totalSpace = store.getTotalSpace();
        long freeSpace = store.getUsableSpace();
        long usedSpace = totalSpace - freeSpace;

        diskMap.put("name", store.getName());
        diskMap.put("mount", store.getMount());
        diskMap.put("totalGB", round(bytesToGB(totalSpace)));
        diskMap.put("usedGB", round(bytesToGB(usedSpace)));
        diskMap.put("freeGB", round(bytesToGB(freeSpace)));
        diskMap.put("usage", round((double) usedSpace / totalSpace * 100));

        return diskMap;
    }

    /**
     * 获取网络相关信息（取第一个网络接口）
     *
     * @return 包含网络上传/下载速度、IP 等信息的 Map
     */
    public Map<String, Object> getNetworkInfo() {
        Map<String, Object> networkMap = new HashMap<>();
        List<NetworkIF> networkIFs = HARDWARE.getNetworkIFs();
        if (networkIFs.isEmpty()) {
            return networkMap;
        }

        // 取第一个非回环接口（过滤本地回环地址）
        NetworkIF network = null;
        for (NetworkIF iface : networkIFs) {
            if (!iface.getDisplayName().contains("Loopback")&&!iface.getDisplayName().contains("lo")) {
                network = iface;
                break;
            }
        }
        if (network == null) {
            network = networkIFs.get(0); // 若没有非回环接口，取第一个
        }

        // 网络接口名称
        networkMap.put("name", network.getName());
        // MAC 地址
        networkMap.put("mac", network.getMacaddr());
        // IP 地址（取第一个 IPv4 地址）
        String[] ipAddresses = network.getIPv4addr();
        if (ipAddresses.length > 0) {
            networkMap.put("ip", ipAddresses[0]);
        }

        // 计算网络速度（上传/下载，单位：MB/s）
        long prevBytesRecv = network.getBytesRecv(); // 已接收字节数（下载）
        long prevBytesSent = network.getBytesSent(); // 已发送字节数（上传）
        Util.sleep(1000); // 等待 1 秒
        network.updateAttributes(); // 更新网络信息
        long bytesRecv = network.getBytesRecv() - prevBytesRecv; // 1 秒内下载字节数
        long bytesSent = network.getBytesSent() - prevBytesSent; // 1 秒内上传字节数

        networkMap.put("downloadSpeedMB", round(bytesToMB(bytesRecv))); // 下载速度
        networkMap.put("uploadSpeedMB", round(bytesToMB(bytesSent)));   // 上传速度

        return networkMap;
    }

    /**
     * 获取服务器完整信息（整合 CPU、内存、磁盘、网络）
     *
     * @return 包含所有性能指标的 Map
     */
    public Map<String, Object> getServerFullInfo() {
        Map<String, Object> fullInfo = new HashMap<>();
        fullInfo.put("cpu", getCpuInfo());
        fullInfo.put("memory", getMemoryInfo());
        fullInfo.put("disk", getDiskInfo());
        fullInfo.put("network", getNetworkInfo());
        fullInfo.put("os", OS.getFamily() + " " + OS.getVersionInfo().getVersion()); // 操作系统信息
        return fullInfo;
    }

    // ------------------------------ 工具方法 ------------------------------

    /**
     * 字节转换为 GB（1GB = 1024^3 字节）
     */
    private static double bytesToGB(long bytes) {
        return bytes / 1024.0 / 1024.0 / 1024.0;
    }

    /**
     * 字节转换为 MB（1MB = 1024^2 字节）
     */
    private static double bytesToMB(long bytes) {
        return bytes / 1024.0 / 1024.0;
    }

    /**
     * 保留两位小数
     */
    private static double round(double value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // ------------------------------ 测试方法 ------------------------------
//    public static void main(String[] args) {
//        // 测试获取 CPU 信息
//        System.out.println("CPU 信息: " + getCpuInfo());
//        // 测试获取内存信息
//        System.out.println("内存信息: " + getMemoryInfo());
//        // 测试获取磁盘信息
//        System.out.println("磁盘信息: " + getDiskInfo());
//        // 测试获取网络信息
//        System.out.println("网络信息: " + getNetworkInfo());
//        // 测试获取完整信息
//        System.out.println("服务器完整信息: " + getServerFullInfo());
//    }
}
