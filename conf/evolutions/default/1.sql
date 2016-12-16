# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table category (
  id                            bigint not null,
  name                          varchar(255),
  constraint pk_category primary key (id)
);
create sequence category_seq;

create table product (
  id                            bigint not null,
  name                          varchar(255),
  category_id                   bigint,
  description                   varchar(255),
  stock                         integer,
  price                         double,
  constraint pk_product primary key (id)
);
create sequence product_seq;

create table user (
  email                         varchar(255) not null,
  role                          varchar(255),
  name                          varchar(255),
  password                      varchar(255),
  constraint pk_user primary key (email)
);

alter table product add constraint fk_product_category_id foreign key (category_id) references category (id) on delete restrict on update restrict;
create index ix_product_category_id on product (category_id);


# --- !Downs

alter table product drop constraint if exists fk_product_category_id;
drop index if exists ix_product_category_id;

drop table if exists category;
drop sequence if exists category_seq;

drop table if exists product;
drop sequence if exists product_seq;

drop table if exists user;

