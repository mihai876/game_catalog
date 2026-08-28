#!/usr/bin/env python3
# game_catalog.py
import argparse
import json
import csv
import sys
import os
from datetime import datetime
from colorama import init, Fore, Style

init(autoreset=True)

DATA_FILE = "games.json"

class Game:
    def __init__(self, name, genre, hours, price, rating):
        self.name = name
        self.genre = genre
        self.hours = hours
        self.price = price
        self.rating = rating

    def to_dict(self):
        return {"name": self.name, "genre": self.genre, "hours": self.hours,
                "price": self.price, "rating": self.rating}

    @classmethod
    def from_dict(cls, data):
        return cls(data["name"], data["genre"], data["hours"], data["price"], data["rating"])

class GameCatalog:
    def __init__(self):
        self.games = []
        self.load()

    def load(self):
        if not os.path.exists(DATA_FILE):
            self.games = []
            return
        try:
            with open(DATA_FILE, 'r', encoding='utf-8') as f:
                data = json.load(f)
                self.games = [Game.from_dict(item) for item in data]
        except (json.JSONDecodeError, FileNotFoundError):
            self.games = []

    def save(self):
        with open(DATA_FILE, 'w', encoding='utf-8') as f:
            json.dump([g.to_dict() for g in self.games], f, ensure_ascii=False, indent=2)

    def add_game(self, name, genre, hours, price, rating):
        if any(g.name.lower() == name.lower() for g in self.games):
            print(Fore.RED + f"Игра '{name}' уже существует.")
            return False
        game = Game(name, genre, hours, price, rating)
        self.games.append(game)
        self.save()
        print(Fore.GREEN + f"Игра '{name}' добавлена.")
        return True

    def remove_game(self, name):
        for i, g in enumerate(self.games):
            if g.name.lower() == name.lower():
                del self.games[i]
                self.save()
                print(Fore.YELLOW + f"Игра '{name}' удалена.")
                return True
        print(Fore.RED + f"Игра '{name}' не найдена.")
        return False

    def list_games(self, filter_genre=None, sort_by="name"):
        games = self.games
        if filter_genre:
            games = [g for g in games if g.genre.lower() == filter_genre.lower()]
            if not games:
                print(Fore.YELLOW + f"Игры с жанром '{filter_genre}' не найдены.")
                return
        if sort_by == "name":
            games.sort(key=lambda g: g.name.lower())
        elif sort_by == "hours":
            games.sort(key=lambda g: g.hours, reverse=True)
        elif sort_by == "price":
            games.sort(key=lambda g: g.price, reverse=True)
        elif sort_by == "rating":
            games.sort(key=lambda g: g.rating, reverse=True)
        else:
            games.sort(key=lambda g: g.name.lower())

        if not games:
            print(Fore.YELLOW + "Каталог пуст.")
            return
        print(Fore.CYAN + "📋 Каталог игр:")
        for g in games:
            print(f"  {Fore.GREEN}{g.name}{Style.RESET_ALL} | {Fore.YELLOW}{g.genre}{Style.RESET_ALL} | "
                  f"{Fore.BLUE}{g.hours} ч{Style.RESET_ALL} | {Fore.MAGENTA}{g.price:.2f} руб{Style.RESET_ALL} | "
                  f"{Fore.RED}★ {g.rating}{Style.RESET_ALL}")

    def search_games(self, query):
        results = [g for g in self.games if query.lower() in g.name.lower() or query.lower() in g.genre.lower()]
        if not results:
            print(Fore.YELLOW + f"По запросу '{query}' ничего не найдено.")
            return
        print(Fore.CYAN + f"🔍 Найдено {len(results)} игр:")
        for g in results:
            print(f"  {Fore.GREEN}{g.name}{Style.RESET_ALL} | {Fore.YELLOW}{g.genre}{Style.RESET_ALL} | "
                  f"{Fore.BLUE}{g.hours} ч{Style.RESET_ALL} | {Fore.MAGENTA}{g.price:.2f} руб{Style.RESET_ALL} | "
                  f"{Fore.RED}★ {g.rating}{Style.RESET_ALL}")

    def export_json(self, filename):
        with open(filename, 'w', encoding='utf-8') as f:
            json.dump([g.to_dict() for g in self.games], f, ensure_ascii=False, indent=2)
        print(Fore.GREEN + f"Экспортировано в {filename} (JSON)")

    def export_csv(self, filename):
        with open(filename, 'w', newline='', encoding='utf-8') as f:
            writer = csv.DictWriter(f, fieldnames=["name", "genre", "hours", "price", "rating"])
            writer.writeheader()
            for g in self.games:
                writer.writerow(g.to_dict())
        print(Fore.GREEN + f"Экспортировано в {filename} (CSV)")

def main():
    parser = argparse.ArgumentParser(description="Каталог игр (Steam)")
    parser.add_argument("--add", help="Добавить игру")
    parser.add_argument("--genre", help="Жанр")
    parser.add_argument("--hours", type=float, help="Часы игры")
    parser.add_argument("--price", type=float, help="Цена")
    parser.add_argument("--rating", type=float, help="Рейтинг (1-10)")
    parser.add_argument("--remove", help="Удалить игру")
    parser.add_argument("--list", action="store_true", help="Показать все игры")
    parser.add_argument("--filter", help="Фильтр по жанру")
    parser.add_argument("--sort", choices=["name", "hours", "price", "rating"], default="name", help="Сортировка")
    parser.add_argument("--search", help="Поиск по названию или жанру")
    parser.add_argument("--export-json", help="Экспорт в JSON")
    parser.add_argument("--export-csv", help="Экспорт в CSV")
    args = parser.parse_args()

    catalog = GameCatalog()

    if args.add:
        if not args.genre or args.hours is None or args.price is None or args.rating is None:
            print(Fore.RED + "Для добавления игры требуются --genre, --hours, --price, --rating")
            sys.exit(1)
        if args.rating < 1 or args.rating > 10:
            print(Fore.RED + "Рейтинг должен быть от 1 до 10")
            sys.exit(1)
        catalog.add_game(args.add, args.genre, args.hours, args.price, args.rating)

    elif args.remove:
        catalog.remove_game(args.remove)

    elif args.list:
        catalog.list_games(args.filter, args.sort)

    elif args.search:
        catalog.search_games(args.search)

    elif args.export_json:
        catalog.export_json(args.export_json)

    elif args.export_csv:
        catalog.export_csv(args.export_csv)

    else:
        parser.print_help()

if __name__ == "__main__":
    main()
