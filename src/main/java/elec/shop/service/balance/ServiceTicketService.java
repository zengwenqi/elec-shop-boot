package elec.shop.service.balance;

import elec.shop.pojo.balance.ServiceTicket;
import elec.shop.pojo.balance.dto.ServiceTicketCreateDTO;
import elec.shop.pojo.balance.dto.ServiceTicketQueryDTO;
import elec.shop.pojo.balance.vo.ServiceTicketVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;

import java.io.OutputStream;
import java.io.IOException;

/**
* @author Lenovo
* @description 针对表【service_ticket(工单表)】的数据库操作Service
* @createDate 2025-06-23 16:45:35
*/
public interface ServiceTicketService extends IService<ServiceTicket> {

    /**
     * 创建工单
     * @param dto 工单创建参数
     * @param userId 用户ID
     * @return 工单ID
     */
    Long createTicket(ServiceTicketCreateDTO dto, Long userId);

    /**
     * 分页查询工单列表
     * @param dto 查询参数
     * @return 分页结果
     */
    Page<ServiceTicketVO> queryTickets(ServiceTicketQueryDTO dto);

    /**
     * 获取工单详情
     * @param ticketId 工单ID
     * @param userId 用户ID
     * @return 工单详情
     */
    ServiceTicketVO getTicketDetail(Long ticketId, Long userId);

    /**
     * 关闭工单
     * @param ticketId 工单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean closeTicket(Long ticketId, Long userId);

    /**
     * 导出工单数据
     * @param dto 查询条件
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    void exportTickets(ServiceTicketQueryDTO dto, HttpServletResponse response) throws IOException;
}
