// game_catalog.rs
use clap::{App, Arg};
use serde::{Deserialize, Serialize};
use serde_json;
use std::fs;
use std::io::Write;
use colored::*;

const DATA_FILE: &str = "games.json";

#[derive(Serialize, Deserialize, Clone)]
struct Game {
    name: String,
    genre: String,
    hours: f64,
    price: f64,
    rating: f64,
}

struct Catalog {
    games: Vec<Game>,
}

impl Catalog {
    fn new() -> Self {
        let mut c = Catalog { games: Vec::new() };
        c.load();
        c
    }

    fn load(&mut self) {
        if let Ok(data) = fs::read_to_string(DATA_FILE) {
            if let Ok(games) = serde_json::from_str(&data) {
                self.games = games;
                return;
            }
        }
        self.games = Vec::new();
    }

    fn save(&self) {
        let json = serde_json::to_string_pretty(&self.games).unwrap();
        fs::write(DATA_FILE, json).unwrap();
    }

    fn add_game(&mut self, name: &str, genre: &str, hours: f64, price: f64, rating: f64) -> bool {
        if self.games.iter().any(|g| g.name.to_lowercase() == name.to_lowercase()) {
            println!("{}", format!("Игра '{}' уже существует.", name).red());
            return false;
        }
        self.games.push(Game { name: name.to_string(), genre: genre.to_string(), hours, price, rating });
        self.save();
        println!("{}", format!("Игра '{}' добавлена.", name).green());
        true
    }

    fn remove_game(&mut self, name: &str) -> bool {
        let idx = self.games.iter().position(|g| g.name.to_lowercase() == name.to_lowercase());
        if let Some(i) = idx {
            self.games.remove(i);
            self.save();
            println!("{}", format!("Игра '{}' удалена.", name).yellow());
            return true;
        }
        println!("{}", format!("Игра '{}' не найдена.", name).red());
        false
    }

    fn list_games(&self, filter_genre: Option<&str>, sort_by: &str) {
        let mut games = self.games.clone();
        if let Some(genre) = filter_genre {
            games.retain(|g| g.genre.to_lowercase() == genre.to_lowercase());
            if games.is_empty() {
                println!("{}", format!("Игры с жанром '{}' не найдены.", genre).yellow());
                return;
            }
        }
        match sort_by {
            "hours" => games.sort_by(|a, b| b.hours.partial_cmp(&a.hours).unwrap()),
            "price" => games.sort_by(|a, b| b.price.partial_cmp(&a.price).unwrap()),
            "rating" => games.sort_by(|a, b| b.rating.partial_cmp(&a.rating).unwrap()),
            _ => games.sort_by(|a, b| a.name.to_lowercase().cmp(&b.name.to_lowercase())),
        }
        if games.is_empty() {
            println!("{}", "Каталог пуст.".yellow());
            return;
        }
        println!("{}", "📋 Каталог игр:".cyan());
        for g in games {
            println!("  {} | {} | {} ч | {:.2} руб | ★ {:.1}",
                g.name.green(),
                g.genre.yellow(),
                g.hours.blue(),
                g.price.magenta(),
                g.rating.red()
            );
        }
    }

    fn search_games(&self, query: &str) {
        let results: Vec<&Game> = self.games.iter()
            .filter(|g| g.name.to_lowercase().contains(&query.to_lowercase()) ||
                         g.genre.to_lowercase().contains(&query.to_lowercase()))
            .collect();
        if results.is_empty() {
            println!("{}", format!("По запросу '{}' ничего не найдено.", query).yellow());
            return;
        }
        println!("{}", format!("🔍 Найдено {} игр:", results.len()).cyan());
        for g in results {
            println!("  {} | {} | {} ч | {:.2} руб | ★ {:.1}",
                g.name.green(),
                g.genre.yellow(),
                g.hours.blue(),
                g.price.magenta(),
                g.rating.red()
            );
        }
    }

    fn export_json(&self, filename: &str) {
        let json = serde_json::to_string_pretty(&self.games).unwrap();
        fs::write(filename, json).unwrap();
        println!("{}", format!("Экспортировано в {} (JSON)", filename).green());
    }

    fn export_csv(&self, filename: &str) {
        let mut wtr = csv::Writer::from_path(filename).unwrap();
        wtr.write_record(&["name", "genre", "hours", "price", "rating"]).unwrap();
        for g in &self.games {
            wtr.write_record(&[&g.name, &g.genre, &g.hours.to_string(), &g.price.to_string(), &g.rating.to_string()]).unwrap();
        }
        wtr.flush().unwrap();
        println!("{}", format!("Экспортировано в {} (CSV)", filename).green());
    }
}

fn main() {
    let matches = App::new("Game Catalog")
        .arg(Arg::with_name("add").long("add").takes_value(true).help("Добавить игру"))
        .arg(Arg::with_name("genre").long("genre").takes_value(true).help("Жанр"))
        .arg(Arg::with_name("hours").long("hours").takes_value(true).help("Часы игры"))
        .arg(Arg::with_name("price").long("price").takes_value(true).help("Цена"))
        .arg(Arg::with_name("rating").long("rating").takes_value(true).help("Рейтинг (1-10)"))
        .arg(Arg::with_name("remove").long("remove").takes_value(true).help("Удалить игру"))
        .arg(Arg::with_name("list").long("list").help("Показать все игры"))
        .arg(Arg::with_name("filter").long("filter").takes_value(true).help("Фильтр по жанру"))
        .arg(Arg::with_name("sort").long("sort").takes_value(true).default_value("name").help("Сортировка: name, hours, price, rating"))
        .arg(Arg::with_name("search").long("search").takes_value(true).help("Поиск"))
        .arg(Arg::with_name("export-json").long("export-json").takes_value(true).help("Экспорт в JSON"))
        .arg(Arg::with_name("export-csv").long("export-csv").takes_value(true).help("Экспорт в CSV"))
        .get_matches();

    let mut catalog = Catalog::new();

    if let Some(name) = matches.value_of("add") {
        let genre = matches.value_of("genre").expect("--genre required");
        let hours: f64 = matches.value_of("hours").expect("--hours required").parse().unwrap();
        let price: f64 = matches.value_of("price").expect("--price required").parse().unwrap();
        let rating: f64 = matches.value_of("rating").expect("--rating required").parse().unwrap();
        if rating < 1.0 || rating > 10.0 {
            println!("{}", "Рейтинг должен быть от 1 до 10".red());
            std::process::exit(1);
        }
        catalog.add_game(name, genre, hours, price, rating);
    } else if let Some(name) = matches.value_of("remove") {
        catalog.remove_game(name);
    } else if matches.is_present("list") {
        catalog.list_games(matches.value_of("filter"), matches.value_of("sort").unwrap());
    } else if let Some(query) = matches.value_of("search") {
        catalog.search_games(query);
    } else if let Some(file) = matches.value_of("export-json") {
        catalog.export_json(file);
    } else if let Some(file) = matches.value_of("export-csv") {
        catalog.export_csv(file);
    } else {
        println!("Используйте --help для справки.");
    }
}
