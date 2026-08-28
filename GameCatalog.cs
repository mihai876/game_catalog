// GameCatalog.cs
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace GameCatalog
{
    class Program
    {
        static void Main(string[] args)
        {
            var opts = ParseArgs(args);
            var catalog = new Catalog();
            if (opts.Add != null)
            {
                if (opts.Genre == null || opts.Hours == null || opts.Price == null || opts.Rating == null)
                {
                    Console.Error.WriteLine("❌ Для добавления игры требуются --genre, --hours, --price, --rating");
                    return;
                }
                if (opts.Rating < 1 || opts.Rating > 10)
                {
                    Console.Error.WriteLine("❌ Рейтинг должен быть от 1 до 10");
                    return;
                }
                catalog.AddGame(opts.Add, opts.Genre, opts.Hours.Value, opts.Price.Value, opts.Rating.Value);
            }
            else if (opts.Remove != null)
            {
                catalog.RemoveGame(opts.Remove);
            }
            else if (opts.List)
            {
                catalog.ListGames(opts.Filter, opts.Sort);
            }
            else if (opts.Search != null)
            {
                catalog.SearchGames(opts.Search);
            }
            else if (opts.ExportJson != null)
            {
                catalog.ExportJson(opts.ExportJson);
            }
            else if (opts.ExportCsv != null)
            {
                catalog.ExportCsv(opts.ExportCsv);
            }
            else
            {
                Console.WriteLine("Используйте --help для справки.");
            }
        }

        static Options ParseArgs(string[] args)
        {
            var opts = new Options();
            for (int i = 0; i < args.Length; i++)
            {
                switch (args[i])
                {
                    case "--add": opts.Add = args[++i]; break;
                    case "--genre": opts.Genre = args[++i]; break;
                    case "--hours": opts.Hours = double.Parse(args[++i]); break;
                    case "--price": opts.Price = double.Parse(args[++i]); break;
                    case "--rating": opts.Rating = double.Parse(args[++i]); break;
                    case "--remove": opts.Remove = args[++i]; break;
                    case "--list": opts.List = true; break;
                    case "--filter": opts.Filter = args[++i]; break;
                    case "--sort": opts.Sort = args[++i]; break;
                    case "--search": opts.Search = args[++i]; break;
                    case "--export-json": opts.ExportJson = args[++i]; break;
                    case "--export-csv": opts.ExportCsv = args[++i]; break;
                }
            }
            return opts;
        }

        class Options
        {
            public string Add { get; set; }
            public string Genre { get; set; }
            public double? Hours { get; set; }
            public double? Price { get; set; }
            public double? Rating { get; set; }
            public string Remove { get; set; }
            public bool List { get; set; }
            public string Filter { get; set; }
            public string Sort { get; set; } = "name";
            public string Search { get; set; }
            public string ExportJson { get; set; }
            public string ExportCsv { get; set; }
        }

        class Game
        {
            public string Name { get; set; }
            public string Genre { get; set; }
            public double Hours { get; set; }
            public double Price { get; set; }
            public double Rating { get; set; }
        }

        class Catalog
        {
            private const string DataFile = "games.json";
            private List<Game> games = new List<Game>();

            public Catalog() => Load();

            private void Load()
            {
                try
                {
                    if (File.Exists(DataFile))
                    {
                        string json = File.ReadAllText(DataFile);
                        games = JsonSerializer.Deserialize<List<Game>>(json) ?? new List<Game>();
                    }
                }
                catch { games = new List<Game>(); }
            }

            private void Save()
            {
                string json = JsonSerializer.Serialize(games, new JsonSerializerOptions { WriteIndented = true });
                File.WriteAllText(DataFile, json);
            }

            public void AddGame(string name, string genre, double hours, double price, double rating)
            {
                if (games.Any(g => g.Name.Equals(name, StringComparison.OrdinalIgnoreCase)))
                {
                    Console.ForegroundColor = ConsoleColor.Red;
                    Console.WriteLine($"Игра '{name}' уже существует.");
                    Console.ResetColor();
                    return;
                }
                games.Add(new Game { Name = name, Genre = genre, Hours = hours, Price = price, Rating = rating });
                Save();
                Console.ForegroundColor = ConsoleColor.Green;
                Console.WriteLine($"Игра '{name}' добавлена.");
                Console.ResetColor();
            }

            public void RemoveGame(string name)
            {
                int idx = games.FindIndex(g => g.Name.Equals(name, StringComparison.OrdinalIgnoreCase));
                if (idx == -1)
                {
                    Console.ForegroundColor = ConsoleColor.Red;
                    Console.WriteLine($"Игра '{name}' не найдена.");
                    Console.ResetColor();
                    return;
                }
                games.RemoveAt(idx);
                Save();
                Console.ForegroundColor = ConsoleColor.Yellow;
                Console.WriteLine($"Игра '{name}' удалена.");
                Console.ResetColor();
            }

            public void ListGames(string filter, string sort)
            {
                var list = games.AsEnumerable();
                if (filter != null)
                {
                    list = list.Where(g => g.Genre.Equals(filter, StringComparison.OrdinalIgnoreCase));
                    if (!list.Any())
                    {
                        Console.ForegroundColor = ConsoleColor.Yellow;
                        Console.WriteLine($"Игры с жанром '{filter}' не найдены.");
                        Console.ResetColor();
                        return;
                    }
                }
                list = sort switch
                {
                    "hours" => list.OrderByDescending(g => g.Hours),
                    "price" => list.OrderByDescending(g => g.Price),
                    "rating" => list.OrderByDescending(g => g.Rating),
                    _ => list.OrderBy(g => g.Name, StringComparer.OrdinalIgnoreCase)
                };
                if (!list.Any())
                {
                    Console.ForegroundColor = ConsoleColor.Yellow;
                    Console.WriteLine("Каталог пуст.");
                    Console.ResetColor();
                    return;
                }
                Console.ForegroundColor = ConsoleColor.Cyan;
                Console.WriteLine("📋 Каталог игр:");
                Console.ResetColor();
                foreach (var g in list)
                {
                    Console.ForegroundColor = ConsoleColor.Green;
                    Console.Write($"  {g.Name}");
                    Console.ForegroundColor = ConsoleColor.Yellow;
                    Console.Write($" | {g.Genre}");
                    Console.ForegroundColor = ConsoleColor.Blue;
                    Console.Write($" | {g.Hours} ч");
                    Console.ForegroundColor = ConsoleColor.Magenta;
                    Console.Write($" | {g.Price:F2} руб");
                    Console.ForegroundColor = ConsoleColor.Red;
                    Console.WriteLine($" | ★ {g.Rating}");
                    Console.ResetColor();
                }
            }

            public void SearchGames(string query)
            {
                var results = games.Where(g => g.Name.Contains(query, StringComparison.OrdinalIgnoreCase) ||
                                               g.Genre.Contains(query, StringComparison.OrdinalIgnoreCase)).ToList();
                if (!results.Any())
                {
                    Console.ForegroundColor = ConsoleColor.Yellow;
                    Console.WriteLine($"По запросу '{query}' ничего не найдено.");
                    Console.ResetColor();
                    return;
                }
                Console.ForegroundColor = ConsoleColor.Cyan;
                Console.WriteLine($"🔍 Найдено {results.Count} игр:");
                Console.ResetColor();
                foreach (var g in results)
                {
                    Console.ForegroundColor = ConsoleColor.Green;
                    Console.Write($"  {g.Name}");
                    Console.ForegroundColor = ConsoleColor.Yellow;
                    Console.Write($" | {g.Genre}");
                    Console.ForegroundColor = ConsoleColor.Blue;
                    Console.Write($" | {g.Hours} ч");
                    Console.ForegroundColor = ConsoleColor.Magenta;
                    Console.Write($" | {g.Price:F2} руб");
                    Console.ForegroundColor = ConsoleColor.Red;
                    Console.WriteLine($" | ★ {g.Rating}");
                    Console.ResetColor();
                }
            }

            public void ExportJson(string filename)
            {
                string json = JsonSerializer.Serialize(games, new JsonSerializerOptions { WriteIndented = true });
                File.WriteAllText(filename, json);
                Console.ForegroundColor = ConsoleColor.Green;
                Console.WriteLine($"Экспортировано в {filename} (JSON)");
                Console.ResetColor();
            }

            public void ExportCsv(string filename)
            {
                using var sw = new StreamWriter(filename);
                sw.WriteLine("name,genre,hours,price,rating");
                foreach (var g in games)
                    sw.WriteLine($"{g.Name},{g.Genre},{g.Hours},{g.Price},{g.Rating}");
                Console.ForegroundColor = ConsoleColor.Green;
                Console.WriteLine($"Экспортировано в {filename} (CSV)");
                Console.ResetColor();
            }
        }
    }
}
