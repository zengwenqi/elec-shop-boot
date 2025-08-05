package elec.shop.service.sys;

import com.baomidou.mybatisplus.core.metadata.IPage;
import elec.shop.pojo.sys.dto.SystemLogQueryDTO;
import elec.shop.pojo.sys.vo.SystemLogVO;

/**
 * 系统日志服务接口
 */
public interface SystemLogService {
    
    /**
     * 分页查询系统日志
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<SystemLogVO> getSystemLogs(SystemLogQueryDTO queryDTO);
    
    /**
     * 根据ID获取日志详情
     * @param logId 日志ID
     * @return 日志详情
     */
    SystemLogVO getLogDetail(Long logId);
    
    /**
     * 导出系统日志
     * @param queryDTO 查询条件
     * @param response HTTP响应对象
     */
    void exportSystemLogs(SystemLogQueryDTO queryDTO, jakarta.servlet.http.HttpServletResponse response);
    
    /**
     * 清空系统日志
     * @param logType 日志类型，为空则清空所有
     * @return 清空结果
     */
    Boolean clearSystemLogs(String logType);
    
    /**
     * 获取日志统计信息
     * @return 统计信息
     */
    Object getLogStatistics();
}