--<Таблица пользователи>
create table users
(
    user_id bigserial primary key,
    username varchar(255) not null unique,
    user_password varchar(255) not null,
    user_first_name varchar(255),
    user_second_name varchar(255),
    user_father_name varchar(255),
    session boolean NOT NULL DEFAULT false
);
create index users_username_index on users(username);
--</Таблица пользователи>

--<Таблица роли>
create table roles
(
    role_id serial primary key,
    name varchar(50) not null
);
--</Таблица роли>

--<Таблица роли пользователей>
create table user_roles
(
    user_id int not null,
    role_id int not null,

    primary key (user_id, role_id),

    constraint fk_user
        foreign key (user_id)
            references users(user_id)
            on delete cascade
            on update cascade,

    constraint fk_role
        foreign key (role_id)
            references roles(role_id)
            on delete cascade
            on update cascade
);
--</Таблица роли пользователей>

--<Таблица телеграм>
create table telegram
(
    user_id int not null unique,
    chat_id varchar(255) not null unique,

    constraint fk_user
        foreign key(user_id)
            references users(user_id)
            on delete cascade
);
create index telegram_chat_id on telegram(chat_id);
--</Таблица телеграм>

--<Таблица Папки> -- folder_access = {1 - закрытый, 2 - приватный , 3 - открытый}
create table folders
(
    folder_id bigserial primary key,
    user_id int,
    folder_path varchar(255) not null,
    folder_name varchar(255),
    folder_access int not null default 1,

    constraint fk_user
        foreign key(user_id)
            references  users(user_id)
            on delete cascade,

    check (folder_access >= 1 and folder_access <= 3)
);

create index folders_folder_path_index on folders(folder_path);
create index folders_folder_access_index on folders(folder_access);
--</Таблица Папки>

--<Таблица Наследование папок>
create table inheritor_folders
(
    parrent_folder_id int not null,
    child_folder_id int not null,

    constraint fk_parrent_folder
        foreign key(parrent_folder_id)
            references folders(folder_id)
            on delete cascade,

    constraint fk_child_folder
        foreign key(child_folder_id)
            references folders(folder_id)
            on delete cascade
);

create index inheritor_folders_parrent_folder_index on inheritor_folders(parrent_folder_id);
create index inheritor_folders_child_folder_index on inheritor_folders(child_folder_id);
--</Таблица Наследование папок>

--<Таблица Приватный доступ>
create table private_access
(
    folder_id int not null,
    user_id int not null,

    constraint fk_folder
        foreign key(folder_id)
            references folders(folder_id)
            on delete cascade,

    constraint fk_user
        foreign key(user_id)
            references  users(user_id)
            on delete cascade
);

create index private_access_user_id_index on private_access(user_id);
create index private_access_folder_id_index on private_access(folder_id);
--</Таблица Приватный доступ>

-- <Таблица Сохраненные папки>
create table favorite_folders
(
    user_id int not null,
    folder_id int not null,

    constraint fk_user
        foreign key(user_id)
            references users(user_id)
            on delete cascade,

    constraint fk_folder
        foreign key(folder_id)
            references folders(folder_id)
            on delete cascade
);
-- </Таблица Сохраненные папки>

--<Таблица Избранные файлы>
create table favorite_files
(
    user_id int not null,
    folder_id int not null,
    file_path varchar(255) not null,
    constraint fk_user
        foreign key(user_id)
            references users(user_id)
            on delete cascade,
    constraint fk_folder
        foreign key(folder_id)
            references folders(folder_id)
            on delete cascade
);

create index favorite_files_user_id_index on favorite_files(user_id);
--</Таблица Избранные файлы>

--</Таблица Рефреш тоекн>
create table refresh_tokens (
                                user_id int not null unique,
                                user_token varchar(255) primary key,
                                expire_date varchar(100) not null,
                                constraint fk_user foreign key(user_id)
                                    references users(user_id)
                                    on delete cascade
                                    on update cascade);
--</Таблица Рефреш тоекн>

--тестовые данные
insert into users (user_id, username, user_password, user_first_name, user_second_name, user_father_name)
values
    (
        1,
        'admin',
        '$2a$04$Fx/SX9.BAvtPlMyIIqqFx.hLY2Xp8nnhpzvEEVINvVpwIPbA3v/.i',
        'Иванов',
        'Иван',
        'Иванович'
    ),
    (
        2,
        'user',
        '$2a$04$Fx/SX9.BAvtPlMyIIqqFx.hLY2Xp8nnhpzvEEVINvVpwIPbA3v/.i',
        'Сидоров',
        'Семен',
        'Семенович'
    );

insert into roles (role_id, name)
values
    (
        1,
        'ROLE_ADMIN'
    ),
    (
        2,
        'ROLE_USER'
    );

insert into user_roles (role_id, user_id)
values
    (
        1,
        1
    ),
    (
        2,
        2
    );
