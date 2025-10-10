# 财务日志系统使用说明

## 概述

本系统提供了一套完整的低耦合财务日志记录方案，采用事件驱动架构，支持异步批量入库，有效降低数据库压力。

## 核心组件

### 1. MoneyLogEvent (财务日志事件)
- 位置: `elec.shop.event.MoneyLogEvent`
- 功能: 定义财务日志事件的数据结构
- 特点: 提供静态工厂方法创建各种类型的财务事件

### 2. AsyncMoneyLogService (异步财务日志服务)
- 位置: `elec.shop.service.finance.AsyncMoneyLogService`
- 功能: 实现批量入库和缓存机制
- 特点: 
  - 队列缓存，批量处理
  - 自动故障转移到同步处理
  - 统计监控功能

### 3. MoneyLogEventListener (事件监听器)
- 位置: `elec.shop.listener.MoneyLogEventListener`
- 功能: 监听财务事件，触发异步入库
- 特点: 支持事务后处理和立即处理两种模式

### 4. MoneyLogHelper (工具类)
- 位置: `elec.shop.utils.MoneyLogHelper`
- 功能: 提供简单的静态方法调用接口
- 特点: 低耦合，易于使用

### 5. AsyncConfig (异步配置)
- 位置: `elec.shop.config.AsyncConfig`
- 功能: 配置专用线程池
- 特点: 针对财务日志优化的线程池配置

## 使用方法

### 基本使用（默认CNY货币）

```java
// 记录充值日志
MoneyLogHelper.logRecharge(
    userId,           // 用户ID
    username,         // 用户名
    userType,         // 用户类型
    amount,           // 充值金额
    balanceBefore,    // 操作前余额
    balanceAfter,     // 操作后余额
    orderNo           // 订单号
);

// 记录采购日志
MoneyLogHelper.logPurchase(userId, username, userType, amount, 
                          balanceBefore, balanceAfter, orderNo);

// 记录退款日志
MoneyLogHelper.logRefund(userId, username, userType, amount, 
                        balanceBefore, balanceAfter, orderNo);

// 记录佣金日志
MoneyLogHelper.logCommission(userId, username, userType, amount, 
                           balanceBefore, balanceAfter, orderNo);

// 记录提现日志
MoneyLogHelper.logWithdraw(userId, username, userType, amount, 
                          balanceBefore, balanceAfter, orderNo);

// 记录转账日志
MoneyLogHelper.logTransfer(userId, username, userType, amount, 
                          balanceBefore, balanceAfter, orderNo);
```

### 自定义货币类型使用

```java
// 记录美元充值日志
MoneyLogHelper.logRecharge(
    userId, username, userType, amount, 
    balanceBefore, balanceAfter, 
    "USD",            // 自定义货币类型
    orderNo
);

// 记录欧元采购日志
MoneyLogHelper.logPurchase(userId, username, userType, amount, 
                          balanceBefore, balanceAfter, "EUR", orderNo);

// 记录比特币转账日志
MoneyLogHelper.logTransfer(userId, username, userType, amount, 
                          balanceBefore, balanceAfter, "BTC", orderNo);
```

### 完全自定义操作类型和货币

```java
// 记录积分兑换日志
MoneyLogHelper.logCustom(
    userId, username, userType,
    "POINTS_EXCHANGE",    // 自定义操作类型
    amount, balanceBefore, balanceAfter,
    "CNY",               // 货币类型
    orderNo,
    "积分兑换现金"         // 操作描述
);

// 记录红包收入日志
MoneyLogHelper.logCustom(
    userId, username, userType,
    "RED_PACKET",        // 自定义操作类型
    amount, balanceBefore, balanceAfter,
    "CNY", null, "新年红包"
);

// 记录美元投资收益
MoneyLogHelper.logCustom(
    userId, username, userType,
    "INVESTMENT_RETURN", // 自定义操作类型
    amount, balanceBefore, balanceAfter,
    "USD",              // 美元货币
    "INV2025001", "美元投资收益"
);

// 记录比特币交易
MoneyLogHelper.logCustom(
    userId, username, userType,
    "CRYPTO_TRADE",     // 自定义操作类型
    new BigDecimal("0.001"), balanceBefore, balanceAfter,
    "BTC",              // 比特币货币
    "BTC2025001", "比特币交易"
);

// 记录违约金扣除
MoneyLogHelper.logCustom(
    userId, username, userType,
    "PENALTY_FEE",      // 自定义操作类型
    new BigDecimal("-200.00"), balanceBefore, balanceAfter,
    "CNY", null, "违约金扣除"
);
```

### 带操作员信息的使用

```java
// 记录充值日志（带操作员信息）
MoneyLogHelper.logRecharge(
    userId, username, userType, amount, 
    balanceBefore, balanceAfter, orderNo,
    operatorId,       // 操作员ID
    operatorName      // 操作员姓名
);

// 记录自定义货币充值（带操作员信息）
MoneyLogHelper.logRecharge(
    userId, username, userType, amount, 
    balanceBefore, balanceAfter, 
    "USD",           // 自定义货币
    orderNo, operatorId, operatorName
);
```

### 直接使用事件对象

```java
// 创建自定义事件
MoneyLogEvent event = MoneyLogEvent.createCustomEvent(
    userId, username, userType,
    "CUSTOM_OPERATION",  // 自定义操作类型
    amount, balanceBefore, balanceAfter,
    "EUR",              // 自定义货币
    orderNo, description, operatorName, ipAddress
);

// 记录事件
MoneyLogHelper.logCustom(event);
```

### 自定义事件

```java
MoneyLogEvent event = MoneyLogEvent.builder()
    .userId(userId)
    .username(username)
    .operationType("CUSTOM")
    .amount(amount)
    // ... 其他字段
    .build();

MoneyLogHelper.logCustom(event);
```

## 在业务代码中集成

### 示例1: 充值服务

```java
@Service
public class RechargeService {
    
    public void recharge(Long userId, BigDecimal amount) {
        // 1. 业务逻辑处理
        User user = userService.getById(userId);
        BigDecimal balanceBefore = user.getBalance();
        
        // 2. 更新余额
        user.setBalance(balanceBefore.add(amount));
        userService.updateById(user);
        
        // 3. 记录财务日志（异步，低耦合）
        MoneyLogHelper.logRecharge(
            userId, 
            user.getUsername(), 
            user.getUserType(),
            amount, 
            balanceBefore, 
            user.getBalance(), 
            generateOrderNo()
        );
    }
}
```

### 示例2: 采购服务

```java
@Service
public class PurchaseService {
    
    @Transactional
    public void purchase(Long userId, BigDecimal amount, String orderNo) {
        // 1. 检查余额
        User user = userService.getById(userId);
        if (user.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("余额不足");
        }
        
        // 2. 扣减余额
        BigDecimal balanceBefore = user.getBalance();
        user.setBalance(balanceBefore.subtract(amount));
        userService.updateById(user);
        
        // 3. 创建采购订单
        createPurchaseOrder(userId, amount, orderNo);
        
        // 4. 记录财务日志（事务提交后异步处理）
        MoneyLogHelper.logPurchase(
            userId, 
            user.getUsername(), 
            user.getUserType(),
            amount, 
            balanceBefore, 
            user.getBalance(), 
            orderNo
        );
    }
}
```

## 配置说明

### 线程池配置

```java
// 财务日志专用线程池
@Bean("moneyLogExecutor")
public Executor moneyLogExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);        // 核心线程数
    executor.setMaxPoolSize(8);         // 最大线程数
    executor.setQueueCapacity(1000);    // 队列容量
    executor.setKeepAliveSeconds(60);   // 空闲时间
    // ...
    return executor;
}
```

### 批量处理配置

```java
// 在 AsyncMoneyLogService 中
private static final int BATCH_SIZE = 50;           // 批量大小
private static final long BATCH_INTERVAL = 5000;   // 处理间隔(ms)
```

## 监控和统计

### 获取统计信息

```java
// 通过测试接口获取
GET /test/money-log/statistics

// 或直接调用服务
String stats = asyncMoneyLogService.getStatistics();
int queueSize = asyncMoneyLogService.getQueueSize();
```

### 强制刷新队列

```java
// 通过测试接口
POST /test/money-log/flush

// 或直接调用服务
asyncMoneyLogService.flushAll();
```

## 测试接口

系统提供了丰富的测试接口来验证财务日志功能：

### 基础测试接口

```bash
# 测试充值（默认CNY）
POST /test/money-log/recharge
{
    "userId": 1,
    "username": "testuser",
    "userType": 4,
    "amount": 100.00,
    "balanceBefore": 1000.00,
    "balanceAfter": 1100.00,
    "orderNo": "R2025001"
}

# 测试充值（自定义货币）
POST /test/money-log/recharge/custom-currency
{
    "userId": 1,
    "username": "testuser", 
    "userType": 4,
    "amount": 100.00,
    "balanceBefore": 500.00,
    "balanceAfter": 600.00,
    "currency": "USD",
    "orderNo": "R2025002"
}
```

### 自定义操作类型测试

```bash
# 测试完全自定义操作类型和货币
POST /test/money-log/custom
{
    "userId": 1,
    "username": "testuser",
    "userType": 4,
    "operationType": "POINTS_EXCHANGE",
    "amount": 50.00,
    "balanceBefore": 1000.00,
    "balanceAfter": 1050.00,
    "currency": "CNY",
    "orderNo": "PE2025001",
    "description": "积分兑换现金"
}

# 测试多种自定义操作类型示例
POST /test/money-log/custom-examples?userId=1&username=testuser&userType=4
# 返回结果包含：积分兑换、红包收入、美元交易、欧元投资收益、比特币交易、违约金扣除等示例

# 测试批量自定义日志
POST /test/money-log/batch-custom?userId=1&username=testuser&userType=4&count=10
# 生成10笔不同操作类型和货币的测试数据
```

### 传统操作类型测试（向后兼容）

```bash
# 测试传统操作类型
POST /test/money-log/traditional?userId=1&username=testuser&userType=4
# 返回结果包含：传统充值、传统采购、传统退款、传统佣金、传统提现、传统转账
```

### 监控和管理接口

```bash
# 获取服务统计信息
GET /test/money-log/stats

# 强制刷新队列
POST /test/money-log/flush
```

### 自定义操作类型示例

系统支持以下自定义操作类型示例：

| 操作类型 | 说明 | 货币类型 | 示例场景 |
|---------|------|---------|----------|
| POINTS_EXCHANGE | 积分兑换 | CNY | 用户积分兑换现金 |
| RED_PACKET | 红包收入 | CNY | 节日红包、活动奖励 |
| USD_TRADE | 美元交易 | USD | 跨境贸易收入 |
| INVESTMENT_RETURN | 投资收益 | EUR/USD | 理财产品收益 |
| CRYPTO_TRADE | 数字货币交易 | BTC/ETH | 加密货币交易 |
| PENALTY_FEE | 违约金 | CNY | 违约金扣除 |
| BATCH_TEST_N | 批量测试 | CNY/USD/EUR | 系统测试用途 |

### 货币类型支持

系统支持任意自定义货币类型，常见示例：

- **法定货币**: CNY（人民币）、USD（美元）、EUR（欧元）、JPY（日元）、GBP（英镑）
- **数字货币**: BTC（比特币）、ETH（以太坊）、USDT（泰达币）
- **积分货币**: POINTS（积分）、CREDITS（信用点）
- **游戏货币**: GOLD（金币）、DIAMOND（钻石）

## 优势特点

1. **低耦合**: 业务代码只需调用静态方法，无需依赖具体实现
2. **高性能**: 异步批量处理，降低数据库压力
3. **高可靠**: 故障自动转移到同步处理，确保日志不丢失
4. **易监控**: 提供统计信息和队列状态监控
5. **易扩展**: 基于事件驱动，易于添加新的处理逻辑
6. **事务安全**: 支持事务提交后处理，确保数据一致性

## 注意事项

1. **操作类型灵活性**
   - 系统同时支持传统枚举操作类型（RECHARGE、PURCHASE等）和完全自定义操作类型
   - 自定义操作类型可以是任意字符串，建议使用有意义的英文标识符
   - 在导出和显示时，系统会优先尝试从枚举中获取中文描述，如果没有找到则直接显示原值

2. **货币类型支持**
   - 系统支持任意自定义货币类型，不限于传统法定货币
   - 可以支持数字货币、积分、游戏币等各种虚拟货币
   - 建议使用标准的货币代码（如ISO 4217标准）以保持一致性

3. **向后兼容性**
   - 所有原有的API接口保持不变，确保现有代码无需修改
   - 新增的自定义功能通过重载方法提供，不影响原有功能
   - 数据库结构无需变更，操作类型和货币类型都存储为字符串

4. **性能考虑**
   - 异步处理机制确保财务日志记录不影响主业务性能
   - 批量处理和队列机制提高大量日志的处理效率
   - 合理配置线程池参数以适应业务量

5. **数据一致性**
   - 使用事务监听器确保业务事务成功后才记录日志
   - 提供重试机制处理临时性失败
   - 建议在关键业务节点进行余额校验

6. **监控和运维**
   - 提供丰富的统计接口监控系统运行状态
   - 支持强制刷新队列处理异常情况
   - 详细的日志记录便于问题排查

7. **扩展建议**
   - 可以根据业务需要扩展更多自定义字段
   - 建议建立操作类型的业务规范和命名约定
   - 考虑实现操作类型的多语言支持