// game_catalog.go
package main

import (
	"encoding/csv"
	"encoding/json"
	"flag"
	"fmt"
	"os"
	"sort"
	"strconv"
	"strings"
)

const dataFile = "games.json"

type Game struct {
	Name   string  `json:"name"`
	Genre  string  `json:"genre"`
	Hours  float64 `json:"hours"`
	Price  float64 `json:"price"`
	Rating float64 `json:"rating"`
}

type Catalog struct {
	Games []Game `json:"games"`
}

func (c *Catalog) load() {
	data, err := os.ReadFile(dataFile)
	if err != nil {
		c.Games = []Game{}
		return
	}
	if err := json.Unmarshal(data, c); err != nil {
		c.Games = []Game{}
	}
}

func (c *Catalog) save() {
	data, _ := json.MarshalIndent(c, "", "  ")
	os.WriteFile(dataFile, data, 0644)
}

func (c *Catalog) addGame(name, genre string, hours, price, rating float64) bool {
	for _, g := range c.Games {
		if strings.EqualFold(g.Name, name) {
			fmt.Printf("\033[31mИгра '%s' уже существует.\033[0m\n", name)
			return false
		}
	}
	c.Games = append(c.Games, Game{name, genre, hours, price, rating})
	c.save()
	fmt.Printf("\033[32mИгра '%s' добавлена.\033[0m\n", name)
	return true
}

func (c *Catalog) removeGame(name string) bool {
	for i, g := range c.Games {
		if strings.EqualFold(g.Name, name) {
			c.Games = append(c.Games[:i], c.Games[i+1:]...)
			c.save()
			fmt.Printf("\033[33mИгра '%s' удалена.\033[0m\n", name)
			return true
		}
	}
	fmt.Printf("\033[31mИгра '%s' не найдена.\033[0m\n", name)
	return false
}

func (c *Catalog) listGames(filterGenre, sortBy string) {
	games := c.Games
	if filterGenre != "" {
		filtered := []Game{}
		for _, g := range games {
			if strings.EqualFold(g.Genre, filterGenre) {
				filtered = append(filtered, g)
			}
		}
		if len(filtered) == 0 {
			fmt.Printf("\033[33mИгры с жанром '%s' не найдены.\033[0m\n", filterGenre)
			return
		}
		games = filtered
	}
	switch sortBy {
	case "hours":
		sort.Slice(games, func(i, j int) bool { return games[i].Hours > games[j].Hours })
	case "price":
		sort.Slice(games, func(i, j int) bool { return games[i].Price > games[j].Price })
	case "rating":
		sort.Slice(games, func(i, j int) bool { return games[i].Rating > games[j].Rating })
	default:
		sort.Slice(games, func(i, j int) bool { return games[i].Name < games[j].Name })
	}
	if len(games) == 0 {
		fmt.Println("\033[33mКаталог пуст.\033[0m")
		return
	}
	fmt.Println("\033[36m📋 Каталог игр:\033[0m")
	for _, g := range games {
		fmt.Printf("  \033[32m%s\033[0m | \033[33m%s\033[0m | \033[34m%.1f ч\033[0m | \033[35m%.2f руб\033[0m | \033[31m★ %.1f\033[0m\n",
			g.Name, g.Genre, g.Hours, g.Price, g.Rating)
	}
}

func (c *Catalog) searchGames(query string) {
	results := []Game{}
	for _, g := range c.Games {
		if strings.Contains(strings.ToLower(g.Name), strings.ToLower(query)) ||
			strings.Contains(strings.ToLower(g.Genre), strings.ToLower(query)) {
			results = append(results, g)
		}
	}
	if len(results) == 0 {
		fmt.Printf("\033[33mПо запросу '%s' ничего не найдено.\033[0m\n", query)
		return
	}
	fmt.Printf("\033[36m🔍 Найдено %d игр:\033[0m\n", len(results))
	for _, g := range results {
		fmt.Printf("  \033[32m%s\033[0m | \033[33m%s\033[0m | \033[34m%.1f ч\033[0m | \033[35m%.2f руб\033[0m | \033[31m★ %.1f\033[0m\n",
			g.Name, g.Genre, g.Hours, g.Price, g.Rating)
	}
}

func (c *Catalog) exportJSON(filename string) {
	data, _ := json.MarshalIndent(c, "", "  ")
	os.WriteFile(filename, data, 0644)
	fmt.Printf("\033[32mЭкспортировано в %s (JSON)\033[0m\n", filename)
}

func (c *Catalog) exportCSV(filename string) {
	f, _ := os.Create(filename)
	defer f.Close()
	w := csv.NewWriter(f)
	defer w.Flush()
	w.Write([]string{"name", "genre", "hours", "price", "rating"})
	for _, g := range c.Games {
		w.Write([]string{g.Name, g.Genre, strconv.FormatFloat(g.Hours, 'f', 1, 64),
			strconv.FormatFloat(g.Price, 'f', 2, 64), strconv.FormatFloat(g.Rating, 'f', 1, 64)})
	}
	fmt.Printf("\033[32mЭкспортировано в %s (CSV)\033[0m\n", filename)
}

func main() {
	var (
		add     string
		genre   string
		hours   float64
		price   float64
		rating  float64
		remove  string
		list    bool
		filter  string
		sortBy  string
		search  string
		expJson string
		expCsv  string
	)
	flag.StringVar(&add, "add", "", "Добавить игру")
	flag.StringVar(&genre, "genre", "", "Жанр")
	flag.Float64Var(&hours, "hours", 0, "Часы игры")
	flag.Float64Var(&price, "price", 0, "Цена")
	flag.Float64Var(&rating, "rating", 0, "Рейтинг (1-10)")
	flag.StringVar(&remove, "remove", "", "Удалить игру")
	flag.BoolVar(&list, "list", false, "Показать все игры")
	flag.StringVar(&filter, "filter", "", "Фильтр по жанру")
	flag.StringVar(&sortBy, "sort", "name", "Сортировка: name, hours, price, rating")
	flag.StringVar(&search, "search", "", "Поиск")
	flag.StringVar(&expJson, "export-json", "", "Экспорт в JSON")
	flag.StringVar(&expCsv, "export-csv", "", "Экспорт в CSV")
	flag.Parse()

	catalog := &Catalog{}
	catalog.load()

	if add != "" {
		if genre == "" || hours == 0 || price == 0 || rating == 0 {
			fmt.Println("\033[31mДля добавления игры требуются --genre, --hours, --price, --rating\033[0m")
			os.Exit(1)
		}
		if rating < 1 || rating > 10 {
			fmt.Println("\033[31mРейтинг должен быть от 1 до 10\033[0m")
			os.Exit(1)
		}
		catalog.addGame(add, genre, hours, price, rating)
	} else if remove != "" {
		catalog.removeGame(remove)
	} else if list {
		catalog.listGames(filter, sortBy)
	} else if search != "" {
		catalog.searchGames(search)
	} else if expJson != "" {
		catalog.exportJSON(expJson)
	} else if expCsv != "" {
		catalog.exportCSV(expCsv)
	} else {
		fmt.Println("Используйте --help для справки.")
	}
}
