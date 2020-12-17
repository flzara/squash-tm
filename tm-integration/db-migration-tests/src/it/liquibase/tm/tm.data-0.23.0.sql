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

insert into REQUIREMENT_LIBRARY_NODE (NAME, CREATED_BY, CREATED_ON)
values
	('requirement 11', 'sp25 user 1', '2000-01-01'),
	('requirement 21', 'sp25 user 2', '2000-01-02'),
	('requirement 22', 'sp25 user 2', '2000-01-03'),
	('requirement 31', 'sp25 user 3', '2000-01-04'),
	('requirement 12', 'sp25 user 1', '2000-01-05'),
	('requirement 23', 'sp25 user 2', '2000-01-06'),
	('requirement 32', 'sp25 user 3', '2000-01-07'),
	('requirement 13', 'sp25 user 1', '2000-01-08');

/*
insert into REQUIREMENT (RLN_ID, ATTACHMENT_LIST_ID)
select rln.rln_id, al.attachment_list_id
from REQUIREMENT_LIBRARY_NODE rln, ATTACHMENT_LIST al
where al.attachment_list_id = (select max(al2.attachment_list_id) from ATTACHMENT_LIST al2)

 and rln.created_on like 'sp25 user%'

 -- this last line does not work with postgresql. since it select no line i remove the whole block to stay consistent.
*/
