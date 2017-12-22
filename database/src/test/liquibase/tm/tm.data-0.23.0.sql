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