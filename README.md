## Каталог игр (Steam)

Многоязычное консольное приложение для управления персональным каталогом игр с возможностью импорта данных из Steam.  
Позволяет добавлять, удалять, искать, фильтровать и сортировать игры, а также экспортировать каталог в различные форматы.

## Особенности
- Добавление игр с указанием названия, жанра, времени игры (часы), цены и рейтинга (1–10).
- Удаление игры по названию.
- Список всех игр с фильтрацией по жанру.
- Сортировка по названию, времени игры, цене или рейтингу.
- Поиск игр по подстроке в названии или жанре.
- Экспорт каталога в JSON и CSV.
- Цветной вывод в терминале (где поддерживается).
- Хранение данных в локальном JSON-файле.
- Поддержка аргументов командной строки для быстрых операций.

## Установка и запуск
Для каждого языка требуются соответствующие инструменты и зависимости.

### Запуск на разных языках

1. **Python**  
   Установка: `pip install colorama` (опционально).  
   Запуск: `python game_catalog.py --add "Half-Life" --genre "FPS" --hours 100 --price 19.99 --rating 9`

2. **JavaScript (Node.js)**  
   Установка: `npm install commander chalk`  
   Запуск: `node game_catalog.js --add "Half-Life" --genre "FPS" --hours 100 --price 19.99 --rating 9`

3. **Go**  
   Запуск: `go run game_catalog.go --add "Half-Life" --genre "FPS" --hours 100 --price 19.99 --rating 9`

4. **Rust**  
   Сборка: `cargo build --release`  
   Запуск: `cargo run -- --add "Half-Life" --genre "FPS" --hours 100 --price 19.99 --rating 9`

5. **Java**  
   Сборка: `javac -cp gson.jar GameCatalog.java`  
   Запуск: `java -cp .;gson.jar GameCatalog --add "Half-Life" --genre "FPS" --hours 100 --price 19.99 --rating 9`

6. **C# (.NET Core)**  
   Установка: `dotnet add package Newtonsoft.Json`  
   Запуск: `dotnet run -- --add "Half-Life" --genre "FPS" --hours 100 --price 19.99 --rating 9`

7. **C++ (Linux)**  
   Сборка: `g++ -std=c++11 -o game_catalog game_catalog.cpp -ljsoncpp`  
   Запуск: `./game_catalog --add "Half-Life" --genre "FPS" --hours 100 --price 19.99 --rating 9`

8. **Kotlin (JVM)**  
   Сборка: `kotlinc -cp gson.jar GameCatalog.kt`  
   Запуск: `kotlin -cp .;gson.jar GameCatalogKt --add "Half-Life" --genre "FPS" --hours 100 --price 19.99 --rating 9`

## Использование

Общие аргументы командной строки:

- `--add <название>` – добавить игру (требует `--genre`, `--hours`, `--price`, `--rating`).
- `--remove <название>` – удалить игру по названию.
- `--list` – показать все игры.
- `--filter <жанр>` – фильтровать по жанру.
- `--sort <поле>` – сортировка: `name`, `hours`, `price`, `rating` (по умолчанию `name`).
- `--search <текст>` – поиск по названию или жанру.
- `--export-json <файл>` – экспорт в JSON.
- `--export-csv <файл>` – экспорт в CSV.
- `--help` – справка.

## Структура репозитория
/
├── README.md
├── game_catalog.py
├── game_catalog.js
├── game_catalog.go
├── game_catalog.rs
├── GameCatalog.java
├── GameCatalog.cs
├── game_catalog.cpp
└── GameCatalog.kt
