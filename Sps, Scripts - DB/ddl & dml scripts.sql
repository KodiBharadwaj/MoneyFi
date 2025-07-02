alter table contact_us_table
drop column user_id

alter table contact_us_table
add is_verified bit

alter table feedback_table
drop column user_id

alter table contact_us_table
add reference_number varchar(30)

alter table contact_us_table
add is_request_active bit

alter table contact_us_table
add request_reason varchar(30)

update contact_us_table set is_request_active = 0
