#!/usr/bin/env node
// game_catalog.js
const { program } = require('commander');
const fs = require('fs');
const chalk = require('chalk');

const DATA_FILE = 'games.json';

class Game {
    constructor(name, genre, hours, price, rating) {
        this.name = name;
        this.genre = genre;
        this.hours = hours;
        this.price = price;
        this.rating = rating;
    }
}

class GameCatalog {
    constructor() {
        this.games = [];
        this.load();
    }

    load() {
        try {
            if (fs.existsSync(DATA_FILE)) {
                const data = JSON.parse(fs.readFileSync(DATA_FILE, 'utf8'));
                this.games = data.map(g => new Game(g.name, g.genre, g.hours, g.price, g.rating));
            }
        } catch (e) {
            this.games = [];
        }
    }

    save() {
        fs.writeFileSync(DATA_FILE, JSON.stringify(this.games, null, 2));
    }

    addGame(name, genre, hours, price, rating) {
        if (this.games.some(g => g.name.toLowerCase() === name.toLowerCase())) {
            console.log(chalk.red(`Игра '${name}' уже существует.`));
            return false;
        }
        this.games.push(new Game(name, genre, hours, price, rating));
        this.save();
        console.log(chalk.green(`Игра '${name}' добавлена.`));
        return true;
    }

    removeGame(name) {
        const idx = this.games.findIndex(g => g.name.toLowerCase() === name.toLowerCase());
        if (idx === -1) {
            console.log(chalk.red(`Игра '${name}' не найдена.`));
            return false;
        }
        this.games.splice(idx, 1);
        this.save();
        console.log(chalk.yellow(`Игра '${name}' удалена.`));
        return true;
    }

    listGames(filterGenre, sortBy = 'name') {
        let games = this.games;
        if (filterGenre) {
            games = games.filter(g => g.genre.toLowerCase() === filterGenre.toLowerCase());
            if (games.length === 0) {
                console.log(chalk.yellow(`Игры с жанром '${filterGenre}' не найдены.`));
                return;
            }
        }
        const sortMap = {
            name: (a, b) => a.name.localeCompare(b.name),
            hours: (a, b) => b.hours - a.hours,
            price: (a, b) => b.price - a.price,
            rating: (a, b) => b.rating - a.rating
        };
        games.sort(sortMap[sortBy] || sortMap.name);

        if (games.length === 0) {
            console.log(chalk.yellow('Каталог пуст.'));
            return;
        }
        console.log(chalk.cyan('📋 Каталог игр:'));
        for (const g of games) {
            console.log(`  ${chalk.green(g.name)} | ${chalk.yellow(g.genre)} | ${chalk.blue(g.hours)} ч | ${chalk.magenta(g.price.toFixed(2))} руб | ${chalk.red(`★ ${g.rating}`)}`);
        }
    }

    searchGames(query) {
        const results = this.games.filter(g => g.name.toLowerCase().includes(query.toLowerCase()) || g.genre.toLowerCase().includes(query.toLowerCase()));
        if (results.length === 0) {
            console.log(chalk.yellow(`По запросу '${query}' ничего не найдено.`));
            return;
        }
        console.log(chalk.cyan(`🔍 Найдено ${results.length} игр:`));
        for (const g of results) {
            console.log(`  ${chalk.green(g.name)} | ${chalk.yellow(g.genre)} | ${chalk.blue(g.hours)} ч | ${chalk.magenta(g.price.toFixed(2))} руб | ${chalk.red(`★ ${g.rating}`)}`);
        }
    }

    exportJson(filename) {
        fs.writeFileSync(filename, JSON.stringify(this.games, null, 2));
        console.log(chalk.green(`Экспортировано в ${filename} (JSON)`));
    }

    exportCsv(filename) {
        const header = 'name,genre,hours,price,rating\n';
        const rows = this.games.map(g => `${g.name},${g.genre},${g.hours},${g.price},${g.rating}`).join('\n');
        fs.writeFileSync(filename, header + rows);
        console.log(chalk.green(`Экспортировано в ${filename} (CSV)`));
    }
}

program
    .option('--add <name>', 'Добавить игру')
    .option('--genre <genre>', 'Жанр')
    .option('--hours <hours>', 'Часы игры', parseFloat)
    .option('--price <price>', 'Цена', parseFloat)
    .option('--rating <rating>', 'Рейтинг (1-10)', parseFloat)
    .option('--remove <name>', 'Удалить игру')
    .option('--list', 'Показать все игры')
    .option('--filter <genre>', 'Фильтр по жанру')
    .option('--sort <field>', 'Сортировка: name, hours, price, rating', 'name')
    .option('--search <query>', 'Поиск')
    .option('--export-json <file>', 'Экспорт в JSON')
    .option('--export-csv <file>', 'Экспорт в CSV')
    .parse(process.argv);

const opts = program.opts();
const catalog = new GameCatalog();

if (opts.add) {
    if (!opts.genre || opts.hours === undefined || opts.price === undefined || opts.rating === undefined) {
        console.error(chalk.red('Для добавления игры требуются --genre, --hours, --price, --rating'));
        process.exit(1);
    }
    if (opts.rating < 1 || opts.rating > 10) {
        console.error(chalk.red('Рейтинг должен быть от 1 до 10'));
        process.exit(1);
    }
    catalog.addGame(opts.add, opts.genre, opts.hours, opts.price, opts.rating);
} else if (opts.remove) {
    catalog.removeGame(opts.remove);
} else if (opts.list) {
    catalog.listGames(opts.filter, opts.sort);
} else if (opts.search) {
    catalog.searchGames(opts.search);
} else if (opts.exportJson) {
    catalog.exportJson(opts.exportJson);
} else if (opts.exportCsv) {
    catalog.exportCsv(opts.exportCsv);
} else {
    program.help();
}
