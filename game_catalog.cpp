// game_catalog.cpp
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <algorithm>
#include <sstream>
#include <cctype>
#include <json/json.h> // using jsoncpp

using namespace std;

struct Game {
    string name, genre;
    double hours, price, rating;
};

class Catalog {
private:
    vector<Game> games;

    void load() {
        ifstream ifs("games.json");
        if (!ifs) return;
        Json::Value root;
        ifs >> root;
        for (const auto& item : root) {
            Game g;
            g.name = item["name"].asString();
            g.genre = item["genre"].asString();
            g.hours = item["hours"].asDouble();
            g.price = item["price"].asDouble();
            g.rating = item["rating"].asDouble();
            games.push_back(g);
        }
    }

    void save() {
        Json::Value root(Json::arrayValue);
        for (const auto& g : games) {
            Json::Value item;
            item["name"] = g.name;
            item["genre"] = g.genre;
            item["hours"] = g.hours;
            item["price"] = g.price;
            item["rating"] = g.rating;
            root.append(item);
        }
        ofstream ofs("games.json");
        ofs << root.toStyledString();
    }

    string toLower(const string& s) {
        string res = s;
        transform(res.begin(), res.end(), res.begin(), ::tolower);
        return res;
    }

public:
    Catalog() { load(); }

    void addGame(const string& name, const string& genre, double hours, double price, double rating) {
        for (const auto& g : games) {
            if (toLower(g.name) == toLower(name)) {
                cout << "\033[31mИгра '" << name << "' уже существует.\033[0m" << endl;
                return;
            }
        }
        Game g{name, genre, hours, price, rating};
        games.push_back(g);
        save();
        cout << "\033[32mИгра '" << name << "' добавлена.\033[0m" << endl;
    }

    void removeGame(const string& name) {
        for (auto it = games.begin(); it != games.end(); ++it) {
            if (toLower(it->name) == toLower(name)) {
                games.erase(it);
                save();
                cout << "\033[33mИгра '" << name << "' удалена.\033[0m" << endl;
                return;
            }
        }
        cout << "\033[31mИгра '" << name << "' не найдена.\033[0m" << endl;
    }

    void listGames(const string& filter, const string& sortBy) {
        vector<Game> list = games;
        if (!filter.empty()) {
            list.erase(remove_if(list.begin(), list.end(), [&](const Game& g) {
                return toLower(g.genre) != toLower(filter);
            }), list.end());
            if (list.empty()) {
                cout << "\033[33mИгры с жанром '" << filter << "' не найдены.\033[0m" << endl;
                return;
            }
        }
        if (sortBy == "hours") {
            sort(list.begin(), list.end(), [](const Game& a, const Game& b) { return a.hours > b.hours; });
        } else if (sortBy == "price") {
            sort(list.begin(), list.end(), [](const Game& a, const Game& b) { return a.price > b.price; });
        } else if (sortBy == "rating") {
            sort(list.begin(), list.end(), [](const Game& a, const Game& b) { return a.rating > b.rating; });
        } else {
            sort(list.begin(), list.end(), [](const Game& a, const Game& b) { return toLower(a.name) < toLower(b.name); });
        }
        if (list.empty()) {
            cout << "\033[33mКаталог пуст.\033[0m" << endl;
            return;
        }
        cout << "\033[36m📋 Каталог игр:\033[0m" << endl;
        for (const auto& g : list) {
            cout << "  \033[32m" << g.name << "\033[0m | \033[33m" << g.genre << "\033[0m | \033[34m" << g.hours << " ч\033[0m | \033[35m" << g.price << " руб\033[0m | \033[31m★ " << g.rating << "\033[0m" << endl;
        }
    }

    void searchGames(const string& query) {
        vector<Game> results;
        for (const auto& g : games) {
            if (toLower(g.name).find(toLower(query)) != string::npos ||
                toLower(g.genre).find(toLower(query)) != string::npos) {
                results.push_back(g);
            }
        }
        if (results.empty()) {
            cout << "\033[33mПо запросу '" << query << "' ничего не найдено.\033[0m" << endl;
            return;
        }
        cout << "\033[36m🔍 Найдено " << results.size() << " игр:\033[0m" << endl;
        for (const auto& g : results) {
            cout << "  \033[32m" << g.name << "\033[0m | \033[33m" << g.genre << "\033[0m | \033[34m" << g.hours << " ч\033[0m | \033[35m" << g.price << " руб\033[0m | \033[31m★ " << g.rating << "\033[0m" << endl;
        }
    }

    void exportJSON(const string& filename) {
        Json::Value root(Json::arrayValue);
        for (const auto& g : games) {
            Json::Value item;
            item["name"] = g.name;
            item["genre"] = g.genre;
            item["hours"] = g.hours;
            item["price"] = g.price;
            item["rating"] = g.rating;
            root.append(item);
        }
        ofstream ofs(filename);
        ofs << root.toStyledString();
        cout << "\033[32mЭкспортировано в " << filename << " (JSON)\033[0m" << endl;
    }

    void exportCSV(const string& filename) {
        ofstream ofs(filename);
        ofs << "name,genre,hours,price,rating\n";
        for (const auto& g : games) {
            ofs << g.name << "," << g.genre << "," << g.hours << "," << g.price << "," << g.rating << "\n";
        }
        cout << "\033[32mЭкспортировано в " << filename << " (CSV)\033[0m" << endl;
    }
};

int main(int argc, char* argv[]) {
    string add, genre, remove, filter, sortBy = "name", search, expJson, expCsv;
    double hours = 0, price = 0, rating = 0;
    bool list = false;

    for (int i = 1; i < argc; ++i) {
        string arg = argv[i];
        if (arg == "--add" && i+1 < argc) add = argv[++i];
        else if (arg == "--genre" && i+1 < argc) genre = argv[++i];
        else if (arg == "--hours" && i+1 < argc) hours = stod(argv[++i]);
        else if (arg == "--price" && i+1 < argc) price = stod(argv[++i]);
        else if (arg == "--rating" && i+1 < argc) rating = stod(argv[++i]);
        else if (arg == "--remove" && i+1 < argc) remove = argv[++i];
        else if (arg == "--list") list = true;
        else if (arg == "--filter" && i+1 < argc) filter = argv[++i];
        else if (arg == "--sort" && i+1 < argc) sortBy = argv[++i];
        else if (arg == "--search" && i+1 < argc) search = argv[++i];
        else if (arg == "--export-json" && i+1 < argc) expJson = argv[++i];
        else if (arg == "--export-csv" && i+1 < argc) expCsv = argv[++i];
    }

    Catalog catalog;
    if (!add.empty()) {
        if (genre.empty() || hours == 0 || price == 0 || rating == 0) {
            cerr << "\033[31mДля добавления игры требуются --genre, --hours, --price, --rating\033[0m" << endl;
            return 1;
        }
        if (rating < 1 || rating > 10) {
            cerr << "\033[31mРейтинг должен быть от 1 до 10\033[0m" << endl;
            return 1;
        }
        catalog.addGame(add, genre, hours, price, rating);
    } else if (!remove.empty()) {
        catalog.removeGame(remove);
    } else if (list) {
        catalog.listGames(filter, sortBy);
    } else if (!search.empty()) {
        catalog.searchGames(search);
    } else if (!expJson.empty()) {
        catalog.exportJSON(expJson);
    } else if (!expCsv.empty()) {
        catalog.exportCSV(expCsv);
    } else {
        cout << "Используйте --help для справки." << endl;
    }
    return 0;
}
