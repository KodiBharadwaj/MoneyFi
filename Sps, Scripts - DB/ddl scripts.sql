alter table contact_us_table
drop column user_id

alter table contact_us_table
add is_verified bit

alter table feedback_table
drop column user_id