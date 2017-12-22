/*
 first, add some attachments to two existing attachmentlists, so that we can check later that they weren't deleted accidentaly during the fix
 for bug 2899.
*/

insert into ATTACHMENT_CONTENT(STREAM_CONTENT) values(NULL);
insert into ATTACHMENT(NAME, TYPE, SIZE, ADDED_ON, CONTENT_ID, ATTACHMENT_LIST_ID)
values ('superblob1', 'bin', 33, '2013-11-20', 
		(select max(ATTACHMENT_CONTENT_ID) from ATTACHMENT_CONTENT),
		(select max(ATTACHMENT_LIST_ID)-2 from ATTACHMENT_LIST)
);



insert into ATTACHMENT_CONTENT(STREAM_CONTENT) values(NULL);
insert into ATTACHMENT(NAME, TYPE, SIZE, ADDED_ON, CONTENT_ID, ATTACHMENT_LIST_ID)
values ('superblob2', 'bin', 32, '2013-11-20', 
		(select max(ATTACHMENT_CONTENT_ID) from ATTACHMENT_CONTENT),
		(select max(ATTACHMENT_LIST_ID)-1 from ATTACHMENT_LIST)
);



insert into ATTACHMENT_CONTENT(STREAM_CONTENT) values(NULL);
insert into ATTACHMENT(NAME, TYPE, SIZE, ADDED_ON, CONTENT_ID, ATTACHMENT_LIST_ID)
values ('superblob3', 'bin', 43, '2013-11-20', 
		(select max(ATTACHMENT_CONTENT_ID) from ATTACHMENT_CONTENT),
		(select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST)
);


/*
	Now let's add some undead attachment lists and resources. We'll create three resources and simple resources (left after ill-deleted 
	requirement folders) and 6 attachments lists (left after ill-deleted folder of any type (according to bug 2899)
*/

insert into ATTACHMENT_LIST values(DEFAULT);
insert into ATTACHMENT_CONTENT(STREAM_CONTENT) values(NULL);
insert into ATTACHMENT(NAME, TYPE, SIZE, ADDED_ON, CONTENT_ID, ATTACHMENT_LIST_ID)
values ('booh', 'bin', 4, '2013-11-20', 
		(select max(ATTACHMENT_CONTENT_ID) from ATTACHMENT_CONTENT),
		(select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST)
);

insert into ATTACHMENT_LIST values(DEFAULT);
insert into ATTACHMENT_CONTENT(STREAM_CONTENT) values(NULL);
insert into ATTACHMENT(NAME, TYPE, SIZE, ADDED_ON, CONTENT_ID, ATTACHMENT_LIST_ID)
values ('booh', 'bin', 4, '2013-11-20', 
		(select max(ATTACHMENT_CONTENT_ID) from ATTACHMENT_CONTENT),
		(select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST)
);

insert into ATTACHMENT_LIST values(DEFAULT);
insert into ATTACHMENT_CONTENT(STREAM_CONTENT) values(NULL);
insert into ATTACHMENT(NAME, TYPE, SIZE, ADDED_ON, CONTENT_ID, ATTACHMENT_LIST_ID)
values ('booh', 'bin', 4, '2013-11-20', 
		(select max(ATTACHMENT_CONTENT_ID) from ATTACHMENT_CONTENT),
		(select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST)
);

insert into ATTACHMENT_LIST values(DEFAULT);
insert into ATTACHMENT_CONTENT(STREAM_CONTENT) values(NULL);
insert into ATTACHMENT(NAME, TYPE, SIZE, ADDED_ON, CONTENT_ID, ATTACHMENT_LIST_ID)
values ('booh', 'bin', 4, '2013-11-20', 
		(select max(ATTACHMENT_CONTENT_ID) from ATTACHMENT_CONTENT),
		(select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST)
);

insert into ATTACHMENT_LIST values(DEFAULT);
insert into ATTACHMENT_CONTENT(STREAM_CONTENT) values(NULL);
insert into ATTACHMENT(NAME, TYPE, SIZE, ADDED_ON, CONTENT_ID, ATTACHMENT_LIST_ID)
values ('booh', 'bin', 4, '2013-11-20', 
		(select max(ATTACHMENT_CONTENT_ID) from ATTACHMENT_CONTENT),
		(select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST)
);

/*this last attachment list is left empty on purpose*/
insert into ATTACHMENT_LIST values(DEFAULT);

/* now insert the three resources */
insert into RESOURCE(NAME, CREATED_BY, CREATED_ON, ATTACHMENT_LIST_ID) values 
	('ghost 1', 'admin', '2013-11-20', (select (max(ATTACHMENT_LIST_ID)-2) from ATTACHMENT_LIST)),
	('ghost 2', 'admin', '2013-11-20', (select (max(ATTACHMENT_LIST_ID)-1) from ATTACHMENT_LIST)),
	('ghost 3', 'admin', '2013-11-20', (select max(ATTACHMENT_LIST_ID) from ATTACHMENT_LIST));

insert into SIMPLE_RESOURCE(RES_ID) values
	((select (max(res.RES_ID)-2) from RESOURCE res)),
	((select (max(res.RES_ID)-1) from RESOURCE res)),
	((select max(res.RES_ID) from RESOURCE res));
	
/*
	Done ! We have now properly screwed up our database !
*/