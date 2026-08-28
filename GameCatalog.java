// GameCatalog.java
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class GameCatalog {
    private static final String DATA_FILE = "games.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<Game>>(){}.getType();

    @Parameter(names = "--add")
    private String addName;
    @Parameter(names = "--genre")
    private String genre;
    @Parameter(names = "--hours")
    private Double hours;
    @Parameter(names = "--price")
    private Double price;
    @Parameter(names = "--rating")
    private Double rating;
    @Parameter(names = "--remove")
    private String removeName;
    @Parameter(names = "--list")
    private boolean list;
    @Parameter(names = "--filter")
    private String filterGenre;
    @Parameter(names = "--sort")
    private String sortBy = "name";
    @Parameter(names = "--search")
    private String searchQuery;
    @Parameter(names = "--export-json")
    private String exportJson;
    @Parameter(names = "--export-csv")
    private String exportCsv;

    static class Game {
        String name, genre;
        double hours, price, rating;
        Game(String name, String genre, double hours, double price, double rating) {
            this.name = name;
            this.genre = genre;
            this.hours = hours;
            this.price = price;
            this.rating = rating;
        }
    }

    private List<Game> games = new ArrayList<>();

    private void load() {
        try {
            String json = new String(Files.readAllBytes(Paths.get(DATA_FILE)));
            games = GSON.fromJson(json, LIST_TYPE);
        } catch (Exception e) {
            games = new ArrayList<>();
        }
    }

    private void save() {
        try {
            Files.write(Paths.get(DATA_FILE), GSON.toJson(games).getBytes());
        } catch (IOException e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    private void addGame(String name, String genre, double hours, double price, double rating) {
        for (Game g : games) {
            if (g.name.equalsIgnoreCase(name)) {
                System.out.println("\u001B[31mИгра '" + name + "' уже существует.\u001B[0m");
                return;
            }
        }
        games.add(new Game(name, genre, hours, price, rating));
        save();
        System.out.println("\u001B[32mИгра '" + name + "' добавлена.\u001B[0m");
    }

    private void removeGame(String name) {
        Iterator<Game> it = games.iterator();
        while (it.hasNext()) {
            Game g = it.next();
            if (g.name.equalsIgnoreCase(name)) {
                it.remove();
                save();
                System.out.println("\u001B[33mИгра '" + name + "' удалена.\u001B[0m");
                return;
            }
        }
        System.out.println("\u001B[31mИгра '" + name + "' не найдена.\u001B[0m");
    }

    private void listGames(String filter, String sort) {
        List<Game> list = new ArrayList<>(games);
        if (filter != null) {
            list.removeIf(g -> !g.genre.equalsIgnoreCase(filter));
            if (list.isEmpty()) {
                System.out.println("\u001B[33mИгры с жанром '" + filter + "' не найдены.\u001B[0m");
                return;
            }
        }
        Comparator<Game> comp;
        switch (sort) {
            case "hours": comp = (a,b) -> Double.compare(b.hours, a.hours); break;
            case "price": comp = (a,b) -> Double.compare(b.price, a.price); break;
            case "rating": comp = (a,b) -> Double.compare(b.rating, a.rating); break;
            default: comp = (a,b) -> a.name.compareToIgnoreCase(b.name);
        }
        list.sort(comp);
        if (list.isEmpty()) {
            System.out.println("\u001B[33mКаталог пуст.\u001B[0m");
            return;
        }
        System.out.println("\u001B[36m📋 Каталог игр:\u001B[0m");
        for (Game g : list) {
            System.out.printf("  \u001B[32m%s\u001B[0m | \u001B[33m%s\u001B[0m | \u001B[34m%.1f ч\u001B[0m | \u001B[35m%.2f руб\u001B[0m | \u001B[31m★ %.1f\u001B[0m%n",
                    g.name, g.genre, g.hours, g.price, g.rating);
        }
    }

    private void searchGames(String query) {
        List<Game> results = new ArrayList<>();
        for (Game g : games) {
            if (g.name.toLowerCase().contains(query.toLowerCase()) || g.genre.toLowerCase().contains(query.toLowerCase())) {
                results.add(g);
            }
        }
        if (results.isEmpty()) {
            System.out.println("\u001B[33mПо запросу '" + query + "' ничего не найдено.\u001B[0m");
            return;
        }
        System.out.println("\u001B[36m🔍 Найдено " + results.size() + " игр:\u001B[0m");
        for (Game g : results) {
            System.out.printf("  \u001B[32m%s\u001B[0m | \u001B[33m%s\u001B[0m | \u001B[34m%.1f ч\u001B[0m | \u001B[35m%.2f руб\u001B[0m | \u001B[31m★ %.1f\u001B[0m%n",
                    g.name, g.genre, g.hours, g.price, g.rating);
        }
    }

    private void exportJson(String filename) throws IOException {
        Files.write(Paths.get(filename), GSON.toJson(games).getBytes());
        System.out.println("\u001B[32mЭкспортировано в " + filename + " (JSON)\u001B[0m");
    }

    private void exportCsv(String filename) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("name,genre,hours,price,rating");
            for (Game g : games) {
                pw.printf("%s,%s,%.1f,%.2f,%.1f%n", g.name, g.genre, g.hours, g.price, g.rating);
            }
        }
        System.out.println("\u001B[32mЭкспортировано в " + filename + " (CSV)\u001B[0m");
    }

    public void run() throws Exception {
        load();

        if (addName != null) {
            if (genre == null || hours == null || price == null || rating == null) {
                System.err.println("\u001B[31mДля добавления игры требуются --genre, --hours, --price, --rating\u001B[0m");
                System.exit(1);
            }
            if (rating < 1 || rating > 10) {
                System.err.println("\u001B[31mРейтинг должен быть от 1 до 10\u001B[0m");
                System.exit(1);
            }
            addGame(addName, genre, hours, price, rating);
        } else if (removeName != null) {
            removeGame(removeName);
        } else if (list) {
            listGames(filterGenre, sortBy);
        } else if (searchQuery != null) {
            searchGames(searchQuery);
        } else if (exportJson != null) {
            exportJson(exportJson);
        } else if (exportCsv != null) {
            exportCsv(exportCsv);
        } else {
            System.out.println("Используйте --help для справки.");
        }
    }

    public static void main(String[] args) throws Exception {
        GameCatalog catalog = new GameCatalog();
        JCommander.newBuilder().addObject(catalog).build().parse(args);
        catalog.run();
    }
}
