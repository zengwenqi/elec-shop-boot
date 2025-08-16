CREATE TABLE `sys_jwt_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text NOT NULL COMMENT '配置值',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='JWT配置表';

-- 插入初始配置数据
INSERT INTO `sys_jwt_config` (`config_key`, `config_value`, `description`) VALUES
('jwt.access-token-private-key', '', 'Access Token私钥'),
('jwt.access-token-public-key', '', 'Access Token公钥'),
('jwt.refresh-token-private-key', '', 'Refresh Token私钥'),
('jwt.refresh-token-public-key', '', 'Refresh Token公钥'),
('jwt.access-token-expiration', '900000', 'Access Token过期时间（毫秒）'),
('jwt.refresh-token-expiration', '604800000', 'Refresh Token过期时间（毫秒）'),
('jwt.issuer', 'elec-shop', '令牌发行者'),
('jwt.login-attempt-limit', '5', '登录尝试次数限制'),
('jwt.login-lock-duration', '300', '登录锁定时间（秒）'); 