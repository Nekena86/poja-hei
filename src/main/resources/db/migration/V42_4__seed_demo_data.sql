-- Demonstration dataset: enough data to exercise every rule in the brief.
-- Every insert is idempotent, so re-running against a database that already holds
-- some of these rows is harmless.
--
-- Passwords: admin123 (admin), teacher123 (teachers), student123 (students).

-- ---------------------------------------------------------------- people
insert into app_user (id, email, password, first_name, last_name, role, promotion_year)
values ('a0000000-0000-0000-0000-000000000001', 'admin@hei.school',
        '$2a$10$doXH6fNHIfXmA/5.cexR1eNDHvVpak0HkLfnjq3q/ZH/ptwajkqti',
        'Admin', 'HEI', 'ADMIN', null)
on conflict do nothing;

insert into app_user (id, email, password, first_name, last_name, role, promotion_year)
values ('b0000000-0000-0000-0000-000000000001', 'rakoto@hei.school',
        '$2a$10$r/U50IBevlDZSTPo4vT4UebXL3Q699mrFqRzQyDo314rtRwgC7yJ.',
        'Jean', 'Rakoto', 'TEACHER', null),
       ('b0000000-0000-0000-0000-000000000002', 'rasoa@hei.school',
        '$2a$10$r/U50IBevlDZSTPo4vT4UebXL3Q699mrFqRzQyDo314rtRwgC7yJ.',
        'Hanta', 'Rasoa', 'TEACHER', null)
on conflict do nothing;

-- Promotion 2022 has finished its three years; promotion 2024 is still in year one.
insert into app_user (id, email, password, first_name, last_name, role, promotion_year)
values ('f0000000-0000-0000-0000-000000000001', 'jean.rakotobe@hei.school',
        '$2a$10$7MRplRPd0V8gtCjAqc7Dqu5kQ9Cv/p61sbcAUc2AtjGeeu7WKu4cW', 'Jean', 'Rakotobe', 'STUDENT', 2022),
       ('f0000000-0000-0000-0000-000000000002', 'marie.andria@hei.school',
        '$2a$10$7MRplRPd0V8gtCjAqc7Dqu5kQ9Cv/p61sbcAUc2AtjGeeu7WKu4cW', 'Marie', 'Andria', 'STUDENT', 2022),
       ('f0000000-0000-0000-0000-000000000003', 'paul.randria@hei.school',
        '$2a$10$7MRplRPd0V8gtCjAqc7Dqu5kQ9Cv/p61sbcAUc2AtjGeeu7WKu4cW', 'Paul', 'Randria', 'STUDENT', 2022),
       ('f0000000-0000-0000-0000-000000000004', 'sofia.ravelo@hei.school',
        '$2a$10$7MRplRPd0V8gtCjAqc7Dqu5kQ9Cv/p61sbcAUc2AtjGeeu7WKu4cW', 'Sofia', 'Ravelo', 'STUDENT', 2022),
       ('f0000000-0000-0000-0000-000000000005', 'eric.rasoa@hei.school',
        '$2a$10$7MRplRPd0V8gtCjAqc7Dqu5kQ9Cv/p61sbcAUc2AtjGeeu7WKu4cW', 'Eric', 'Rasoa', 'STUDENT', 2022),
       ('f0000000-0000-0000-0000-000000000006', 'lala.naivo@hei.school',
        '$2a$10$7MRplRPd0V8gtCjAqc7Dqu5kQ9Cv/p61sbcAUc2AtjGeeu7WKu4cW', 'Lala', 'Naivo', 'STUDENT', 2022),
       ('f0000000-0000-0000-0000-000000000011', 'anne.fara@hei.school',
        '$2a$10$7MRplRPd0V8gtCjAqc7Dqu5kQ9Cv/p61sbcAUc2AtjGeeu7WKu4cW', 'Anne', 'Fara', 'STUDENT', 2024),
       ('f0000000-0000-0000-0000-000000000012', 'luc.tovo@hei.school',
        '$2a$10$7MRplRPd0V8gtCjAqc7Dqu5kQ9Cv/p61sbcAUc2AtjGeeu7WKu4cW', 'Luc', 'Tovo', 'STUDENT', 2024)
on conflict do nothing;

-- ---------------------------------------------------------------- groups and courses
insert into student_group (id, ref)
values ('c0000000-0000-0000-0000-000000000001', 'G-2022-A'),
       ('c0000000-0000-0000-0000-000000000002', 'G-2022-B'),
       ('c0000000-0000-0000-0000-000000000003', 'G-2024-A')
on conflict do nothing;

insert into course (id, ref, title, credits)
values ('d0000000-0000-0000-0000-000000000001', 'PROG1', 'Algorithmique et programmation', 6),
       ('d0000000-0000-0000-0000-000000000002', 'BDD1', 'Bases de donnees', 4),
       ('d0000000-0000-0000-0000-000000000003', 'WEB1', 'Developpement web', 5)
on conflict do nothing;

-- PROG1 is taught by two different teachers, to two different groups.
-- BDD1 is given to G-2022-A only, which is the "not always all the groups" case.
insert into course_teaching (id, course_id, teacher_id, group_id)
values ('e0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001',
        'b0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001'),
       ('e0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001',
        'b0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002'),
       ('e0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000002',
        'b0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000001'),
       ('e0000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000003',
        'b0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000003')
on conflict do nothing;

-- ---------------------------------------------------------------- exams, spread over the three years
insert into exam (id, ref, course_teaching_id, date_exam, coefficient, academic_year)
values ('10000000-0000-0000-0000-000000000001', 'PROG1-2022-Y1', 'e0000000-0000-0000-0000-000000000001',
        '2023-01-20T08:00:00Z', 2.00, 1),
       ('10000000-0000-0000-0000-000000000002', 'BDD1-2022-Y2', 'e0000000-0000-0000-0000-000000000003',
        '2024-01-22T08:00:00Z', 3.00, 2),
       ('10000000-0000-0000-0000-000000000003', 'PROG1-2022-Y3', 'e0000000-0000-0000-0000-000000000001',
        '2025-01-24T08:00:00Z', 2.00, 3),
       ('10000000-0000-0000-0000-000000000004', 'PROG1-2022-B-Y1', 'e0000000-0000-0000-0000-000000000002',
        '2023-01-20T08:00:00Z', 2.00, 1),
       ('10000000-0000-0000-0000-000000000005', 'WEB1-2024-Y1', 'e0000000-0000-0000-0000-000000000004',
        '2025-01-20T08:00:00Z', 1.00, 1)
on conflict do nothing;

-- ---------------------------------------------------------------- grades
-- Averages are deliberately spread around the pass mark: Paul Randria (7.86) and
-- Lala Naivo (6.00) do not graduate, Sofia Ravelo lands exactly on 10.
insert into grade (id, student_id, exam_id, value, last_modified_at, last_modified_by)
values ('20000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000001', 16.00, '2023-01-25T10:00:00Z', 'rakoto@hei.school'),
       ('20000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000002', 15.00, '2024-01-27T10:00:00Z', 'rasoa@hei.school'),
       ('20000000-0000-0000-0000-000000000003', 'f0000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000003', 14.00, '2025-01-29T10:00:00Z', 'rakoto@hei.school'),

       ('20000000-0000-0000-0000-000000000004', 'f0000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000001', 12.00, '2023-01-25T10:00:00Z', 'rakoto@hei.school'),
       ('20000000-0000-0000-0000-000000000005', 'f0000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000002', 11.00, '2024-01-27T10:00:00Z', 'rasoa@hei.school'),
       ('20000000-0000-0000-0000-000000000006', 'f0000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000003', 13.00, '2025-01-29T10:00:00Z', 'rakoto@hei.school'),

       ('20000000-0000-0000-0000-000000000007', 'f0000000-0000-0000-0000-000000000003',
        '10000000-0000-0000-0000-000000000001', 8.00, '2023-02-10T10:00:00Z', 'rakoto@hei.school'),
       ('20000000-0000-0000-0000-000000000008', 'f0000000-0000-0000-0000-000000000003',
        '10000000-0000-0000-0000-000000000002', 7.00, '2024-01-27T10:00:00Z', 'rasoa@hei.school'),
       ('20000000-0000-0000-0000-000000000009', 'f0000000-0000-0000-0000-000000000003',
        '10000000-0000-0000-0000-000000000003', 9.00, '2025-01-29T10:00:00Z', 'rakoto@hei.school'),

       ('20000000-0000-0000-0000-000000000010', 'f0000000-0000-0000-0000-000000000004',
        '10000000-0000-0000-0000-000000000001', 10.00, '2023-01-25T10:00:00Z', 'rakoto@hei.school'),
       ('20000000-0000-0000-0000-000000000011', 'f0000000-0000-0000-0000-000000000004',
        '10000000-0000-0000-0000-000000000002', 10.00, '2024-01-27T10:00:00Z', 'rasoa@hei.school'),
       ('20000000-0000-0000-0000-000000000012', 'f0000000-0000-0000-0000-000000000004',
        '10000000-0000-0000-0000-000000000003', 10.00, '2025-01-29T10:00:00Z', 'rakoto@hei.school'),

       ('20000000-0000-0000-0000-000000000013', 'f0000000-0000-0000-0000-000000000005',
        '10000000-0000-0000-0000-000000000004', 13.00, '2023-01-25T10:00:00Z', 'rasoa@hei.school'),
       ('20000000-0000-0000-0000-000000000014', 'f0000000-0000-0000-0000-000000000006',
        '10000000-0000-0000-0000-000000000004', 6.00, '2023-01-25T10:00:00Z', 'rasoa@hei.school'),

       ('20000000-0000-0000-0000-000000000015', 'f0000000-0000-0000-0000-000000000011',
        '10000000-0000-0000-0000-000000000005', 14.00, '2025-01-25T10:00:00Z', 'rakoto@hei.school'),
       ('20000000-0000-0000-0000-000000000016', 'f0000000-0000-0000-0000-000000000012',
        '10000000-0000-0000-0000-000000000005', 9.00, '2025-01-25T10:00:00Z', 'rakoto@hei.school')
on conflict do nothing;

-- Paul Randria's first grade was corrected after a complaint: the history says why.
insert into grade_history (id, grade_id, previous_value, new_value, reason, changed_by, changed_at)
values ('30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000007',
        6.00, 8.00, 'Reclamation acceptee : une question n''avait pas ete corrigee',
        'rakoto@hei.school', '2023-02-10T10:00:00Z')
on conflict do nothing;

-- ---------------------------------------------------------------- group assignments
-- Paul Randria moved from G-2022-A to G-2022-B in the middle of his second year.
insert into student_group_history (id, student_id, group_id, start_date, end_date)
values ('40000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000001',
        'c0000000-0000-0000-0000-000000000001', '2022-09-01', null),
       ('40000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000002',
        'c0000000-0000-0000-0000-000000000001', '2022-09-01', null),
       ('40000000-0000-0000-0000-000000000003', 'f0000000-0000-0000-0000-000000000003',
        'c0000000-0000-0000-0000-000000000001', '2022-09-01', '2024-01-15'),
       ('40000000-0000-0000-0000-000000000004', 'f0000000-0000-0000-0000-000000000003',
        'c0000000-0000-0000-0000-000000000002', '2024-01-15', null),
       ('40000000-0000-0000-0000-000000000005', 'f0000000-0000-0000-0000-000000000004',
        'c0000000-0000-0000-0000-000000000001', '2022-09-01', null),
       ('40000000-0000-0000-0000-000000000006', 'f0000000-0000-0000-0000-000000000005',
        'c0000000-0000-0000-0000-000000000002', '2022-09-01', null),
       ('40000000-0000-0000-0000-000000000007', 'f0000000-0000-0000-0000-000000000006',
        'c0000000-0000-0000-0000-000000000002', '2022-09-01', null),
       ('40000000-0000-0000-0000-000000000008', 'f0000000-0000-0000-0000-000000000011',
        'c0000000-0000-0000-0000-000000000003', '2024-09-01', null),
       ('40000000-0000-0000-0000-000000000009', 'f0000000-0000-0000-0000-000000000012',
        'c0000000-0000-0000-0000-000000000003', '2024-09-01', null)
on conflict do nothing;
