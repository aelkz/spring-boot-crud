## Spring Boot CRUD Application

### Endpoints

| Método | URI | Descrição |
| ------ | --- | ---------- |
| GET    |/v2/api-docs     | swagger json |
| GET    |/swagger-ui.html | swagger html |
| GET    |/actuator/info   | info / heartbeat - provided by boot |
| GET    |/actuator/health | application health - provided by boot |
| GET    |/v1/user/{id}    | get user by id |
| GET    |/v1/users        | get N users with an offset|
| PUT    |/v1/user         | add/update user|

### Author

@aelkz<br>
raphael.alex@gmail.com