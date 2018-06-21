
-- Increasing column size of email column in CORE_USER table test related datas.


-- create users.

insert into CORE_PARTY values(DEFAULT);
insert into CORE_USER(PARTY_ID, LOGIN, FIRST_NAME, LAST_NAME, EMAIL, ACTIVE, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON)
values ((select max(PARTY_ID) from CORE_PARTY), 'Bob', 'Bob', 'Bobovitch', 'bob@bob.com', true, 'admin', '2013-10-21', NULL, NULL);
