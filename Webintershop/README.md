# intershop
#### Веб-Магазин
#  Intershop — Реактивный интернет-магазин на Spring Boot
##  Описание
### Простой интернет-магазин с возможностями:

    Просмотр товаров с пагинацией и сортировкой
    С сервисом оплаты который имеет баланс 10000
    С Кэшированием продукта и списка продуктов на Redis
    Добавление/удаление товаров в корзину
    Оформление заказов
    Админка для добавления товаров
    Авторизация на сайте двумя спосабами
    login/password преддобавленых пользователей (user123/user123)(manager/manager)
        и с помощью keycloak(User/User)
    
## Технологии
    Spring Boot 3.4.5 (Java 21)
    Spring Data R2DBC + Thymeleaf
    H2 Database (по умолчанию) / PostgreSQL
    Spring Boot DevTools
    Spring Security
# Как запустить
## Клонируйте репозиторий:

    git clone https://github.com/DyrShLucK/intershop
    (Желательно сделать ./gradlew bootJar   для Intersshop и PayService)
    Запустите bootRun
        Или
    запустите через docker (docker-compose up --build)
        запустите приложение только после запуска keycloak
## Откройте в браузере:
    http://localhost:8080/intershop
##  Настройка БД (опционально)
    По умолчанию используется H2 (в памяти).
Для PostgreSQL:

    в application.properties поставтье spring.profiles.active=prod и создайте таблицу intershop в Postrgres

#  Особенности
    Админка : /admin/add-product (добавление товаров)
    Корзина : /intershop/cart (изменение количества товаров)
    Заказы : /intershop/orders (список оформленных заказов)
