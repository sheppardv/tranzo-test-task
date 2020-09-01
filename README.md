# Tranzo Test Task
A sample FSM microservice using [http4s](http://http4s.org/), [doobie](http://tpolecat.github.io/doobie/),
and [circe](https://github.com/circe/circe).

For simplicity, app is using H2 in-memory databases with PostgreSQL syntax.
There is [dummy data](src/main/resources/db/migration/V1__create_todo.sql) generated for Entities and Transition Matrices.

Transition Matrix is cached using scalacache + caffeine. 

PLEASE NOTE
- did not have time to implement better unit / it test coverage
- my git broke because of OSX updates, did not follow git flow

## End points

Method | Url                     | Description
------ | -----------             | -----------
GET    | /entities               | Returns all Entities.
POST   | /entities               | Creates Entity
POST   | /states/:entity_id      | Creates Transition from last entity state to the new one
GET    | /transitions            | Returns Transition History for Entities
GET    | /transition-matrices    | Returns Transition Matrices 
POST   | /transition-matrices    | Creates Transition Matrix  

Create a Entity:
```curl -X POST --header "Content-Type: application/json" --data '{"name": "Entity 1"}' http://localhost:8088/entities```

Get all entities:
```curl http://localhost:8088/entities```

Make transition for entity to new state:
```curl -X POST --header "Content-Type: application/json" --data '{"sate": "pending"}' http://localhost:8088/states/1```

Get all Transition Matrices:
```curl http://localhost:8088/transition-matrices```

Create Transition Matrix:
```curl -X POST --header "Content-Type: application/json" --data '{"from_state": "pending", "possible_next_states": ["closed", "finished"]}' http://localhost:8088/transition-matrices```

## http4s
[http4s](http://http4s.org/) is used as the HTTP layer. http4s provides streaming and functional HTTP for Scala.
This example project uses [cats-effect](https://github.com/typelevel/cats-effect), but is possible to use
http4s with another effect monad.

By using an effect monad, side effects are postponed until the last moment.

http4s uses [fs2](https://github.com/functional-streams-for-scala/fs2) for streaming. This allows to return
streams in the HTTP layer so the response doesn't need to be generated in memory before sending it to the client.

In the example project this is done for the `GET /todos` endpoint.

## doobie
[doobie](http://tpolecat.github.io/doobie/) is used to connect to the database. This is a pure functional JDBC layer for Scala.
This example project uses [cats-effect](https://github.com/typelevel/cats-effect) in combination with doobie,
but doobie can use another effect monad.

Because both http4s and doobie use an effect monad, the combination is still pure and functional.

## circe
[circe](https://github.com/circe/circe) is the recommended JSON library to use with http4s. It provides
automatic derivation of JSON Encoders and Decoders.

## Configuration
[pureconfig](https://github.com/pureconfig/pureconfig) is used to read the configuration file `application.conf`.
This library allows reading a configuration into well typed objects.

## Database
[h2](http://www.h2database.com/) is used as a database. This is an in memory database, so when stopping the application, the state of the
microservice is lost.

Using [Flyway](https://flywaydb.org/) the database migrations are performed when starting the server.

## Tests
Project is partially covered with unit and integration tests.

## Running
You can run the microservice with `sbt run`. By default it listens to port number 8088, you can change
this in the [application.conf](src/main/resources/application.conf).

## Packaging
`sbt assembly`

## Docker
```
$ docker build .
$ docker run -p 127.0.0.1:8088:8088 <container id>
```

## Possible improvements
- better logs
- tracing
- better test coverage
