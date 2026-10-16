# Вариант 8. Маршрутизация курьерской доставки

REST API на Spring Boot: контроллер (запрос и маппинг) → сервис (правила и расчёт массы) → репозиторий (БД).

## Почему ответ был кривой

`GET /api/shipments` отдавал сущность Hibernate наружу. У отправителя стоит ленивая связь, и Jackson пытался сериализовать прокси. В ответе либо простыня `ByteBuddyInterceptor` / `hibernateLazyInitializer`, либо 500 со стектрейсом.

Плюс контроллеры и сервисы были только у отправлений. `/api/users`, `/api/routes`, `/api/stops`, `/api/shipment-assignments` отвечали 404. Удаление пользователя или отправления с назначениями падало по внешнему ключу.

## Запуск

Нужна Java 21.

```bash
./mvnw spring-boot:run
```

Если при старте Hibernate ругается на схему или `created_at`, удали папку `data` и запусти ещё раз. Старый файл H2 собран без новых колонок.

## Коды ответов

| Операция | Метод | Код |
| --- | --- | --- |
| список / один | GET | 200 |
| создание | POST | 201 и JSON с id |
| обновление | PATCH | 200 и JSON |
| удаление | DELETE | 204, связанные записи тоже удаляются |

`createdAt` ставит сервер. В запросе его передавать не нужно.

## Пример

```http
POST /api/users
{"name":"Иван","email":"ivan@example.com","role":"SENDER"}

POST /api/shipments
{"senderId":1,"address":"ул. Ленина, 1","weight":2.5,"status":"CREATED"}

POST /api/routes
{"routeDate":"2026-10-16","maxWeight":10,"status":"PLANNED"}

POST /api/stops
{"routeId":1,"address":"ул. Ленина, 1","sequenceNumber":1}

POST /api/shipment-assignments
{"shipmentId":1,"stopId":1,"status":"PENDING"}
```

`GET /api/routes/1` вернёт `currentWeight` и `remainingWeight`. Если сумма масс больше `maxWeight`, назначение отвечает 400.
