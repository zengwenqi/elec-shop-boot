package elec.shop.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import elec.shop.pojo.sys.SysUser;
import elec.shop.utils.AllContextUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    // 从当前上下文中获取用户信息（这里需要根据实际情况实现）
    private SysUser getCurrentUser() {
        SysUser loginSysUser = AllContextUtils.getLoginSysUser();
        return loginSysUser; // 默认返回系统用户，实际应替换为真实逻辑
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdBy", Long.class, getCurrentUser().getUserId());
        this.strictInsertFill(metaObject, "createdAt", Date.class, new Date());
        this.strictInsertFill(metaObject, "updatedBy", Long.class, getCurrentUser().getUserId());
        this.strictInsertFill(metaObject, "updatedAt", Date.class, new Date());
        this.strictInsertFill(metaObject, "isDeleted", Integer.class, 0); // 默认未删除
        this.strictInsertFill(metaObject, "version", Integer.class, 1); // 初始版本为1
        this.strictInsertFill(metaObject, "tenantId", Integer.class, 0); // 租户ID为0
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedBy", Long.class, getCurrentUser().getUserId());
        this.strictUpdateFill(metaObject, "updatedAt", Date.class, new Date());

        // 检测是否为逻辑删除操作，若是则填充deleteTime
        Object deletedValue = getFieldValByName("isDeleted", metaObject);
        TableInfo tableInfo = TableInfoHelper.getTableInfo(metaObject.getOriginalObject().getClass());

        if (tableInfo != null && tableInfo.getLogicDeleteFieldInfo() != null) {
            Object logicNotDeleteValue = tableInfo.getLogicDeleteFieldInfo().getLogicNotDeleteValue();
            Object logicDeleteValue = tableInfo.getLogicDeleteFieldInfo().getLogicDeleteValue();

            if (deletedValue != null &&
                    !deletedValue.equals(logicNotDeleteValue) &&
                    deletedValue.equals(logicDeleteValue)) {
                // 当前是逻辑删除操作，填充deleteTime
                this.strictUpdateFill(metaObject, "deletedAt", Date.class, new Date());
            }
        }
    }


}
