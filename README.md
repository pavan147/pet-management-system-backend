# pet-management-system-backend

Auth call : 

postman request POST 'localhost:8080/api/auth/login' \
--header 'Content-Type: application/json' \
--body '{
"usernameOrEmail": "admin@gmail.com",
"password": "test"

}'