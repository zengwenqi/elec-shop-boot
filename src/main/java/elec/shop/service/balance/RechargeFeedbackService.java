package elec.shop.service.balance;

import com.baomidou.mybatisplus.core.metadata.IPage;
import elec.shop.pojo.balance.RechargeFeedback;
import com.baomidou.mybatisplus.extension.service.IService;
import elec.shop.pojo.balance.vo.RechargeFeedbackVO;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【recharge_feedback(充值反馈表)】的数据库操作Service
* @createDate 2025-06-29 15:31:03
*/
public interface RechargeFeedbackService extends IService<RechargeFeedback> {

    /**
     * 更新充值反馈状态
     * @param id
     * @param status
     * @param handlerId
     * @param handler
     * @return
     */
    boolean updateStatus(Long id, String status, Long handlerId, String handler);

    /**
     * 根据用户ID查询充值反馈列表
     * @param userId
     * @return
     */
    List<RechargeFeedbackVO> listByUserId(Long userId);

    /**
     * 根据状态查询充值反馈列表
     * @param status
     * @return
     */
    List<RechargeFeedbackVO> listByStatus(String status);

    /**
     * 分页查询充值反馈列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    IPage<RechargeFeedbackVO> selectPage(Integer pageNum, Integer pageSize, String status, String rechargeNo);

    /**
     * 获取导出数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param status 状态
     * @return 导出数据列表
     */
    List<RechargeFeedbackVO> getExportData(String startTime, String endTime, String status);
}
