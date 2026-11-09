DELETE FROM outbox_notifications;
DELETE FROM accounts;
INSERT INTO accounts (login, name, birthdate, balance)
VALUES ('ivanov', 'Иванов Иван', '2001-01-01', 100),
       ('petrov', 'Петров Петр', '1995-05-15', 100),
       ('sidorov', 'Сидоров Сидор', '1988-09-30', 100);
