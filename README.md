# API de franquicias

### Ejecución del proyecto en ambiente local

Para la ejecución del proyecto es necesario tener `Docker` instalado, además de `Docker compose`

Usamos el siguiente comando para iniciar

```
docker-compose up --build -d
```

El servidor estará disponible en localhost:8080 

### Endpoints
Los endpoints de esta API se pueden probar dentro del cliente Postman. La collection puede ser importada
y se encuentra en el mismo repositorio y se llama `collection.json`. Sin embargo, si se quiere saber que endpoints
tiene la API, pueden encontrarlos en el package `router`.

### Test unitarios
Los test unitarios se pueden ejecutar con la instrucción `mvnw test` dentro la carpeta raíz del proyecto