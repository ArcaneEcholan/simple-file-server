create table if not exists permission
(
    id     INT(10) auto_increment
    primary key,
    name   VARCHAR(255) null,
    `desc` VARCHAR(255) null
    );

create table if not exists role
(
    id     INT(10) auto_increment
    primary key,
    name   VARCHAR(255) null,
    `desc` VARCHAR(255) null
    );

create table if not exists role_permission
(
    id            INT(10) auto_increment
    primary key,
    role_id       INT(10) null,
    permission_id INT(10) null
    );

create table if not exists system_config
(
    id    INT(10) auto_increment
    primary key,
    `key` VARCHAR(255) null,
    value VARCHAR(255) null
    );

create table if not exists user
(
    id       INT(10) auto_increment
    primary key,
    username VARCHAR(255) null,
    password VARCHAR(255) null,
    ctime    VARCHAR(255) null,
    mtime    VARCHAR(255) null
    );

create table if not exists user_role
(
    id      INT(10) auto_increment
    primary key,
    user_id INT(10) null,
    role_id INT(10) null
    );

