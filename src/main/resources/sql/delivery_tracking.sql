-- 配送跟踪表
create table if not exists delivery_tracking
(
    id          bigint auto_increment comment 'id' primary key,
    taskId      bigint                                  not null comment '任务id',
    runnerId    bigint                                  not null comment '跑腿人员id',
    status      varchar(64)                             not null comment '配送状态：picked_up-已取件/delivering-配送中/arrived-已到达/completed-已完成',
    description varchar(512)                            null comment '进度描述',
    longitude   double                                  null comment '经度',
    latitude    double                                  null comment '纬度',
    address     varchar(512)                            null comment '当前地址',
    createTime  datetime     default CURRENT_TIMESTAMP  not null comment '创建时间',
    updateTime  datetime     default CURRENT_TIMESTAMP  not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint      default 0                  not null comment '是否删除',
    index idx_taskId (taskId),
    index idx_runnerId (runnerId),
    index idx_status (status),
    index idx_createTime (createTime)
) comment '配送跟踪' collate = utf8mb4_unicode_ci;
