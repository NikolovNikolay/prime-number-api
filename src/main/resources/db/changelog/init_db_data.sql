
-- Generate Users
create table if not exists "user"
(
	id bigserial not null primary key,
	name varchar(50) not null,
	key varchar(32)
);

create unique index user_id_uindex
	on "user" (id);

create index user_key_index
    on "user" (key);

insert into "user"(name, key)
values ('test user', '5db0665f64de4ff9a03c42cd9145b0ba');

-- Generate Permissions
create table permission
(
    name varchar(50) not null
        constraint permission_pk
            primary key
);

alter table permission
    owner to admin;

create unique index permission_name_uindex
    on permission (name);

insert into "permission"(name)
values ('ACCESS_PRIME_API');

-- Map Users to Permissions
create table user_permission
(
	user_id bigint not null
		constraint user_permission_pk
            primary key
        constraint user_permission_user_id_fk
            references "user",
	permission varchar(50) not null
		constraint user_permission_permission_name_fk
			references permission
);

create unique index user_permission_user_id_permission_uindex
	on user_permission (user_id, permission);

insert into user_permission(user_id, permission)
values (1, 'ACCESS_PRIME_API')

