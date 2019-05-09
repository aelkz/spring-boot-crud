## Spring Boot CRUD Application

### Endpoints

| Método | URI | Descrição |
| ------ | --- | ---------- |
| GET    |/v2/api-docs     | swagger json |
| GET    |/swagger-ui.html | swagger html |
| GET    |/actuator/info   | info / heartbeat - provided by spring boot actuator |
| GET    |/actuator/health | application health - provided by spring boot actuator |
| GET    |/v1/user/{id}    | get user by id |
| GET    |/v1/users        | get N users with an offset|
| PUT    |/v1/user         | add/update user|

### Author

@aelkz<br>
raphael.alex@gmail.com<br>
This project is based on https://github.com/pavelfomin/spring-boot-rest-example