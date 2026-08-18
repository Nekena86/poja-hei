-- Domain schema for the HEI graduates application.
-- Flyway now owns this schema; Hibernate only validates the entities against it
-- (spring.jpa.hibernate.ddl-auto=validate).
--
-- Written with IF NOT EXISTS so it also applies cleanly to the existing database,
-- whose tables were created by an earlier ddl-auto=update run.

create table if not exists app_user (
    id             uuid         not null,
    email          varchar(255) not null unique,
    password       varchar(255) not null,
    first_name     varchar(255) not null,
    last_name      varchar(255) not null,
    role           varchar(255) not null check (role in ('STUDENT', 'TEACHER', 'ADMIN')),
    promotion_year integer,
    primary key (id)
);

create table if not exists course (
    id      uuid         not null,
    ref     varchar(255) not null unique,
    title   varchar(255) not null,
    credits integer      not null,
    primary key (id)
);

create table if not exists student_group (
    id  uuid         not null,
    ref varchar(255) not null unique,
    primary key (id)
);

-- One row per (course, teacher, group): a course can be taught by several teachers,
-- and given to several groups, but not necessarily to all of them.
create table if not exists course_teaching (
    id         uuid not null,
    course_id  uuid not null references course,
    teacher_id uuid not null references app_user,
    group_id   uuid not null references student_group,
    primary key (id),
    unique (course_id, teacher_id, group_id)
);

create table if not exists exam (
    id                 uuid                     not null,
    ref                varchar(255)             not null unique,
    course_teaching_id uuid                     not null references course_teaching,
    date_exam          timestamp(6) with time zone not null,
    coefficient        numeric(38, 2)           not null,
    academic_year      integer                  not null,
    primary key (id)
);

create table if not exists grade (
    id                uuid                        not null,
    student_id        uuid                        not null references app_user,
    exam_id           uuid                        not null references exam,
    value             numeric(5, 2)               not null,
    last_modified_at  timestamp(6) with time zone not null,
    last_modified_by  varchar(255)                not null,
    primary key (id),
    unique (student_id, exam_id)
);

-- Every change to a grade is appended here, with the reason it was changed.
create table if not exists grade_history (
    id             uuid                        not null,
    grade_id       uuid                        not null references grade,
    previous_value numeric(38, 2),
    new_value      numeric(38, 2)              not null,
    reason         varchar(255)                not null,
    changed_by     varchar(255)                not null,
    changed_at     timestamp(6) with time zone not null,
    primary key (id)
);

-- A student can move to another group at any time: the current row is closed with an
-- end_date and a new one opens, so the whole path stays readable.
create table if not exists student_group_history (
    id         uuid not null,
    student_id uuid not null references app_user,
    group_id   uuid not null references student_group,
    start_date date not null,
    end_date   date,
    primary key (id)
);

-- Added after the first version of the schema: needed to report a promotion's results
-- year by year, which an exam date alone cannot tell us.
alter table exam add column if not exists academic_year integer;
update exam set academic_year = 1 where academic_year is null;
alter table exam alter column academic_year set not null;
