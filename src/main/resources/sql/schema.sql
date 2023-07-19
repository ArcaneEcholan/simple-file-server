create table if not exists sfs.permission
(
    id     int auto_increment
    primary key,
    name   varchar(255) null,
    `desc` varchar(255) null
    );

create table if not exists sfs.role
(
    id     int auto_increment
    primary key,
    name   varchar(255) null,
    `desc` varchar(255) null
    );

create table if not exists sfs.role_permission
(
    id            int auto_increment
    primary key,
    role_id       int null,
    permission_id int null
);

create table if not exists sfs.system_config
(
    id    int auto_increment
    primary key,
    `key` varchar(255) null,
    value varchar(255) null
    );

create table if not exists sfs.user
(
    id       int auto_increment
    primary key,
    username varchar(255) null,
    password varchar(255) null,
    ctime    varchar(255) null,
    mtime    varchar(255) null
    );

create table if not exists sfs.user_acc_dir
(
    id      int auto_increment
    primary key,
    user_id int  null,
    acc_dir text null
);

create table if not exists sfs.user_role
(
    id      int auto_increment
    primary key,
    user_id int null,
    role_id int null
);

