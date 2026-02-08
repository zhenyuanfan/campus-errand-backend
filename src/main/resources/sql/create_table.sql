-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/publisher/runner',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    contactInfo  varchar(255)                           null comment '联系方式',
    creditScore  double       default 100.0             null comment '信誉评分',
    orderCount   int          default 0                 null comment '接单数量',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName),
    INDEX idx_user_role (userRole),
    INDEX idx_credit_score (creditScore)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 操作记录表
create table if not exists operation_log
(
    id          bigint auto_increment comment 'id' primary key,
    userId      bigint                             not null comment '用户 id',
    action      varchar(256)                       not null comment '操作行为',
    detail      text                               null comment '详细信息',
    createTime  datetime default CURRENT_TIMESTAMP  not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP  not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '操作记录' collate = utf8mb4_unicode_ci;

-- 通知消息表
create table if not exists message
(
    id          bigint auto_increment comment 'id' primary key,
    userId      bigint                             not null comment '接收用户 id',
    title       varchar(256)                       not null comment '标题',
    content     text                               not null comment '内容',
    type        varchar(64)                        not null comment '消息类型',
    isRead      tinyint  default 0                 not null comment '是否已读（0-未读，1-已读）',
    createTime  datetime default CURRENT_TIMESTAMP  not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP  not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '通知消息' collate = utf8mb4_unicode_ci;
