create table "t_department"(
  "id" int not null primary key auto_increment,
  "name" varchar(128) not null,
  "location" varchar(128) not null,
  "mixedCase" varchar(128),
  "uid" int,
  "dept_id" int,
  "deleted" bit default '0',
  "created_at" timestamp default current_timestamp,
  "updated_at" timestamp default current_timestamp
);

create table "t_employee"(
  "id" int not null primary key auto_increment,
  "name" varchar(128) not null,
  "job" varchar(128) not null,
  "manager_id" int null,
  "hire_date" date not null,
  "salary" bigint not null,
  "department_id" int not null,
  "uid" int,
  "dept_id" int,
  "deleted" bit default '0',
  "created_at" timestamp default current_timestamp,
  "updated_at" timestamp default current_timestamp
);

create schema "company";
create table "company"."t_customer" (
  "id" int not null primary key auto_increment,
  "name" varchar(128) not null,
  "email" varchar(128) not null,
  "phone_number" varchar(128) not null,
  "uid" int,
  "dept_id" int,
  "deleted" bit default '0',
  "created_at" timestamp default current_timestamp,
  "updated_at" timestamp default current_timestamp
);

insert into "t_department"("name", "location") values ('tech', 'Guangzhou');
insert into "t_department"("name", "location") values ('finance', 'Beijing');

insert into "t_employee"("name", "job", "manager_id", "hire_date", "salary", "department_id")
    values ('vince', 'engineer', null, '2018-01-01', 100, 1);
insert into "t_employee"("name", "job", "manager_id", "hire_date", "salary", "department_id")
    values ('marry', 'trainee', 1, '2019-01-01', 50, 1);

insert into "t_employee"("name", "job", "manager_id", "hire_date", "salary", "department_id")
    values ('tom', 'director', null, '2018-01-01', 200, 2);
insert into "t_employee"("name", "job", "manager_id", "hire_date", "salary", "department_id")
    values ('penny', 'assistant', 3, '2019-01-01', 100, 2);



create table "t_employee0"(
  "id" int not null primary key auto_increment,
  "name" varchar(128) not null,
  "job" varchar(128) not null,
  "manager_id" int null,
  "hire_date" date not null,
  "salary" bigint not null,
  "department_id" int not null
);

insert into "t_employee0"("name", "job", "manager_id", "hire_date", "salary", "department_id")
    values ('vince', 'engineer', null, '2018-01-01', 100, 1);
insert into "t_employee0"("name", "job", "manager_id", "hire_date", "salary", "department_id")
    values ('marry', 'trainee', 1, '2019-01-01', 50, 1);

insert into "t_employee0"("name", "job", "manager_id", "hire_date", "salary", "department_id")
    values ('tom', 'director', null, '2018-01-01', 200, 2);
insert into "t_employee0"("name", "job", "manager_id", "hire_date", "salary", "department_id")
    values ('penny', 'assistant', 3, '2019-01-01', 100, 2);
