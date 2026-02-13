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

-- 跑腿任务表
create table if not exists task
(
    id             bigint auto_increment comment 'id' primary key,
    publisherId    bigint                                  not null comment '发布者id',
    runnerId       bigint                                  null comment '接单者id',
    taskType       varchar(64)                             not null comment '任务类型：file_delivery-文件传递/goods_purchase-物品采购/food_delivery-餐饮配送/other-其他',
    title          varchar(256)                            not null comment '任务标题',
    description    text                                    null comment '任务描述',
    startLocation  varchar(512)                            not null comment '起始地点',
    endLocation    varchar(512)                            not null comment '目的地',
    expectedTime   datetime                                not null comment '期望完成时间',
    reward         decimal(10, 2)                          not null comment '报酬金额',
    status         varchar(64)  default 'pending'          not null comment '任务状态：pending-待接单/accepted-已接单/in_progress-进行中/completed-已完成/cancelled-已取消',
    contactInfo    varchar(255)                            not null comment '联系方式',
    remark         varchar(1024)                           null comment '备注',
    createTime     datetime     default CURRENT_TIMESTAMP  not null comment '创建时间',
    updateTime     datetime     default CURRENT_TIMESTAMP  not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint      default 0                  not null comment '是否删除',
    index idx_publisherId (publisherId),
    index idx_runnerId (runnerId),
    index idx_taskType (taskType),
    index idx_status (status),
    index idx_createTime (createTime)
) comment '跑腿任务' collate = utf8mb4_unicode_ci;

-- 任务评价表
create table if not exists task_review
(
    id           bigint auto_increment comment 'id' primary key,
    taskId       bigint                                  not null comment '任务id',
    reviewerId   bigint                                  not null comment '评价者id（发布者）',
    runnerId     bigint                                  not null comment '被评价者id（跑腿人员）',
    score        int                                     not null comment '评分（1-5分）',
    content      varchar(512)                            null comment '评价内容',
    tags         varchar(256)                            null comment '评价标签（如：速度快、态度好、准时送达等，逗号分隔）',
    createTime   datetime     default CURRENT_TIMESTAMP  not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP  not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                  not null comment '是否删除',
    index idx_taskId (taskId),
    index idx_reviewerId (reviewerId),
    index idx_runnerId (runnerId),
    index idx_score (score)
) comment '任务评价' collate = utf8mb4_unicode_ci;

-- 评价回复表
create table if not exists review_reply
(
    id          bigint auto_increment comment 'id' primary key,
    reviewId    bigint                              not null comment '评价id',
    replierId   bigint                              not null comment '回复者id（跑腿人员）',
    content     varchar(512)                        not null comment '回复内容',
    createTime  datetime default CURRENT_TIMESTAMP  not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP  not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                  not null comment '是否删除',
    index idx_reviewId (reviewId),
    index idx_replierId (replierId)
) comment '评价回复' collate = utf8mb4_unicode_ci;

-- 用户反馈表
create table if not exists feedback
(
    id          bigint auto_increment comment 'id' primary key,
    userId      bigint                              not null comment '提交用户id',
    type        varchar(64)                         not null comment '反馈类型：suggestion-建议/complaint-投诉/bug-Bug反馈/other-其他',
    title       varchar(256)                        not null comment '反馈标题',
    content     text                                not null comment '反馈内容',
    contactInfo varchar(255)                        null comment '联系方式（可选）',
    taskId      bigint                              null comment '关联任务id（可选）',
    status      varchar(64) default 'pending'       not null comment '处理状态：pending-待处理/processing-处理中/resolved-已解决/rejected-已驳回',
    adminId     bigint                              null comment '处理人id（管理员）',
    adminReply  text                                null comment '管理员回复内容',
    replyTime   datetime                            null comment '回复时间',
    createTime  datetime default CURRENT_TIMESTAMP  not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP  not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                  not null comment '是否删除',
    index idx_userId (userId),
    index idx_type (type),
    index idx_status (status),
    index idx_createTime (createTime)
) comment '用户反馈' collate = utf8mb4_unicode_ci;
