--
--     This file is part of the Squashtest platform.
--     Copyright (C) Henix, henix.fr
--
--     See the NOTICE file distributed with this work for additional
--     information regarding copyright ownership.
--
--     This is free software: you can redistribute it and/or modify
--     it under the terms of the GNU Lesser General Public License as published by
--     the Free Software Foundation, either version 3 of the License, or
--     (at your option) any later version.
--
--     this software is distributed in the hope that it will be useful,
--     but WITHOUT ANY WARRANTY; without even the implied warranty of
--     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--     GNU Lesser General Public License for more details.
--
--     You should have received a copy of the GNU Lesser General Public License
--     along with this software.  If not, see <http://www.gnu.org/licenses/>.
--


-- Increasing column size of email column in CORE_USER table test related datas.


-- create users.

insert into CORE_PARTY values(DEFAULT);
insert into CORE_USER(PARTY_ID, LOGIN, FIRST_NAME, LAST_NAME, EMAIL, ACTIVE, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON)
values ((select max(PARTY_ID) from CORE_PARTY), 'Bob', 'Bob', 'Bobovitch', 'bob@bob.com', true, 'admin', '2013-10-21', NULL, NULL);
