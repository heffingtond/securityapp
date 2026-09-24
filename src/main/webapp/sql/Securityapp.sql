-- All sql
CREATE database APPLICATION_SECURITY;

use APPLICATION_SECURITY;

drop table ORGANIZATION;

create table ORGANIZATION
(
	organization_id 						integer primary key not null auto_increment,
	organization_name				varchar(128) not null,
	organization_address			varchar(128) not null,
	organization_city					varchar(128) not null,
	organization_state					varchar(128) not null,
	organization_zip					varchar(5) not null,
	organization_zip_ext				varchar(4),
	primary_url							varchar(128) not null,
	organization_description		varchar(1024)
) ENGINE=InnoDB;

insert into ORGANIZATION values( 1, 'Watchtower', '1603 Seminole Dr', 'Johnson City', 'TN', '37604', '', 'http://localhost/securityapp/', 'SaaS provider for comprehensive application security layer.'  );

drop table ORGANIZATION_CONTACT;

create table ORGANIZATION_CONTACT
(
	organization_contact_id	 		integer primary key not null auto_increment,
	organization_id						integer,
	foreign key ( organization_id ) references  ORGANIZATION( organization_id ),
	first_name								varchar(128) not null,
	last_name								varchar(128) not null,
	email									varchar(128),
	title										varchar(128),
	mobile_phone						varchar(12),
	office_phone							varchar(12),
	office_phone_ext					varchar(10)
) ENGINE=InnoDB;

drop table AUTHENTICATION_PROFILE;

select * from ORGANIZATION;
select * from AUTHENTICATION_PROFILE;

create table AUTHENTICATION_PROFILE
(
	authentication_profile_id 		integer primary key not null auto_increment,
	organization_id						integer,
	foreign key ( organization_id ) references  ORGANIZATION( organization_id ),
	user_id									varchar(50) not null,
	password								varchar(50) not null,
	salt										varchar(128),
	first_name								varchar(128) not null,
	last_name								varchar(128) not null,
	mobile_phone						varchar(12),
	office_phone							varchar(12),
	office_phone_ext					varchar(10),
	home_phone							varchar(12),
	failed_login_attempts				integer,
	verification_code_method		varchar(50) -- email or text mobile phone.
) ENGINE=InnoDB;

CREATE INDEX user_id_index ON AUTHENTICATION_PROFILE ( user_id );

select * from AUTHENTICATION_PROFILE;
delete from AUTHENTICATION_PROFILE;

insert into authentication_profile values( 1, 1, 'heffington', 'Taylor_314', 'salt', 'david', 'heffington', '5129342542', '5125551212', null,null, 0, 'text'  );

drop table APPLICATION;

select * from APPLICATION;



insert into APPLICATION values( 1, 17, 'SPTR', 'State Passthrough Reporting', 'https://fiscalcpa/sptr', 'Facilitate annual state reporting of grant state passthrough activity.', 'Financial Reporting Section of Fiscal Management');

insert into APPLICATION values( 1, 1, 'SECURITY_APP', 'Watchtower Security', 'http://localhost/securityapp/', 'Watchtower is a software as a service security platform that may be leveraged by any application for authentication and authorizations.', 'Watchtower Security Corp.');

-- Natural PK1: [domain_name + application_name]
-- Natural PK2: [application_cd]
create table APPLICATION
(
	application_id 						integer primary key not null auto_increment,
	organization_id						integer,
	foreign key ( organization_id ) references  ORGANIZATION( organization_id ),
	application_cd						varchar(50) not null,  -- unique code for the application.  Must be unique for the database. 
	application_name					varchar(128) not null,
	domain_name						varchar(128) not null,
	application_description			varchar(1028),
	owner									varchar(1024)
) ENGINE=InnoDB;

drop table ROLE;
select * from role;
insert into ROLE values( 1, 1, 'SPTR_AGENCY_ANALYST', 'Agency-level access for State Passthrough Reporting data entry.' );
insert into ROLE values( 2, 1, 'SPTR_CERTIFY', 'This user can certify his agency.' );
insert into ROLE values( 3, 1, 'SPTR_REPORT_MASTER', 'This user can run all reports.' );
insert into ROLE values( 4, 1, 'SPTR_NO_DELETE', 'This user cannot delete entries.' );
insert into ROLE values( 1, 1, 'SECURITY_MASTER', 'This user has full autonomy for security maintenance.' );

-- Natural PK1: [role_name]
create table ROLE
(
	role_id 						integer primary key not null auto_increment,
	application_id			integer,
	foreign key ( application_id ) references  APPLICATION( application_id ),
	role_name					varchar(128) not null,
	role_description		varchar(5000) not null
) ENGINE=InnoDB;

drop table DEPARTMENT;
select * from organization;
select * from DEPARTMENT;
select * from APPLICATION;
insert into DEPARTMENT values( 1, 1, 'Watchtower Root administration', 'WATCHTOWER_ROOT', 'Top-level department for the Watchtower application.' );

create table DEPARTMENT
(
	department_id			integer primary key not null auto_increment,
	organization_id			integer,
	foreign key ( organization_id ) references  ORGANIZATION( organization_id ),
	department_name		varchar(128) not null,
	department_code		varchar(20) not null,
	department_description	varchar(5000)
) ENGINE=InnoDB;

drop table APPLICATION_USER_DEPARTMENT_ROLE;


use APPLICATION_SECURITY;
	select * from APPLICATION_USER_DEPARTMENT_ROLE
select * from ORGANIZATION
select * from AUTHENTICATION_PROFILE
select * from DEPARTMENT
select * from APPLICATION;
select * from ROLE;

insert into APPLICATION_USER_DEPARTMENT_ROLE values( 1, 2, 1, 1,1 );
insert into APPLICATION_USER_DEPARTMENT_ROLE values( 2, 7, 1, 1,2 );
insert into APPLICATION_USER_DEPARTMENT_ROLE values( 3, 7, 1,2,3 );
insert into APPLICATION_USER_DEPARTMENT_ROLE values( 4, 7, 1,2,4 );
insert into APPLICATION_USER_DEPARTMENT_ROLE values( 5, 8, 1,2,5 );
insert into APPLICATION_USER_DEPARTMENT_ROLE values( 6, 8, 1,2,1 );
insert into APPLICATION_USER_DEPARTMENT_ROLE values( 7, 9, 2,3,5 );
delete from APPLICATION_USER_DEPARTMENT_ROLE where application_user_department_role_id = 6

create table APPLICATION_USER_DEPARTMENT_ROLE
(
	application_user_department_role_id 			integer primary key not null auto_increment,
	authentication_profile_id								integer,
	foreign key ( authentication_profile_id ) 		references  AUTHENTICATION_PROFILE( authentication_profile_id ),
	application_id											integer,
	foreign key ( application_id ) 						references  APPLICATION( application_id ),
	department_id											integer,
	foreign key ( department_id ) 					references  DEPARTMENT( department_id ),
	role_id														integer,
	foreign key ( role_id ) 								references  ROLE( role_id )
) ENGINE=InnoDB;

drop table VERIFICATION_CODE;

insert into VERIFICATION_CODE values( 1, 2, 1, '123456',DEFAULT );
select * from verification_code;
-- [Natural PK: authentication_profile_id + application id.  Duplicates are not allowed.]
create table VERIFICATION_CODE
(
	verification_code_id 		integer primary key not null auto_increment,
	authentication_profile_id				integer,
	foreign key ( authentication_profile_id ) references  AUTHENTICATION_PROFILE( authentication_profile_id ),
	application_id				integer,
	foreign key ( application_id ) references  APPLICATION( application_id ),
	verification_cd				varchar(6) not null,
	created_ts						varchar(1000)
) ENGINE=InnoDB;

select * from application;



SELECT  d.department_code, r.role_name 
FROM ROLE r, APPLICATION a, APPLICATION_USER_DEPARTMENT_ROLE audr, DEPARTMENT d, AUTHENTICATION_PROFILE ap, ORGANIZATION o
where r.application_id = a.application_id
and   a.application_id = audr.application_id

and   audr.authentication_profile_id = ap.authentication_profile_id
and   o.organization_id = ap.organization_id
and   d.organization_id = ap.organization_id
and   a.organization_id = ap.organization_id
and   a.application_cd = 'SPTR'
and   ap.user_id = 'GWASH123'
order by d.department_code

SELECT 
   d.department_code, r.role_name 
FROM DEPARTMENT d, ROLE r
RIGHT JOIN APPLICATION a      ON a.application_id = r.application_id 
RIGHT JOIN APPLICATION_USER_DEPARTMENT_ROLE audr      ON a.application_id = audr.application_id
RIGHT JOIN AUTHENTICATION_PROFILE ap      ON ap.authentication_profile_id = audr.authentication_profile_id
RIGHT JOIN ORGANIZATION o      ON o.organization_id = ap.organization_id
where   a.application_cd = 'SPTR'
and   ap.user_id = 'GWASH123'
and d.department_id = audr.department_id
and d.organization_id = o.organization_id
and r.application_id = a.application_id
and audr.role_id = r.role_id



-- Role request payload:
-- User ID
-- Application Code

-- Role assignments payload:
-- Department
	--	All roles for department (role name)
