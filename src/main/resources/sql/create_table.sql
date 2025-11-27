-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
    ) comment '用户' collate = utf8mb4_unicode_ci;
-- 更新用户表结构，添加跑腿业务相关字段
ALTER TABLE user ADD COLUMN contactInfo VARCHAR(255) COMMENT '联系方式';
ALTER TABLE user ADD COLUMN creditScore DOUBLE DEFAULT 100.0 COMMENT '信誉评分';
ALTER TABLE user ADD COLUMN orderCount INT DEFAULT 0 COMMENT '接单数量';

-- 添加索引以提高查询性能
CREATE INDEX idx_user_role ON user(userRole);
CREATE INDEX idx_credit_score ON user(creditScore);