# Getting Started

# Market

Market is a server side java application as a stock market trading backend

### Setup database

    You need mysql database server up and running on port 3306 with a market schema and a db user with the credentials specified in application.properties

## Install dependencies

Use maven to install dependencies

```maven
mvn install
```

## Start development server

```maven
mvn spring-boot:run
```

```
With every application startup, RandomDataLoader.java will remove/recreate all the stocks on db
```

## License

[MIT]
