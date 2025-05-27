# intershop
#### Веб-Магазин
#  Intershop — Реактивный интернет-магазин на Spring Boot
##  Описание
### Простой интернет-магазин с возможностями:

    Просмотр товаров с пагинацией и сортировкой
    Добавление/удаление товаров в корзину
    Оформление заказов
    Админка для добавления товаров
## Технологии
    Spring Boot 3.4.5 (Java 21)
    Spring Data R2DBC + Thymeleaf
    H2 Database (по умолчанию) / PostgreSQL
    Spring Boot DevTools
# Как запустить
## Клонируйте репозиторий:

    git clone https://github.com/DyrShLucK/intershop
    Запустите bootRun
        Или
    запустите через docker (docker-compose up --build)
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
