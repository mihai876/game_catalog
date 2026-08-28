// GameCatalog.kt
import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

data class Game(val name: String, val genre: String, val hours: Double, val price: Double, val rating: Double)

class GameCatalog {
    @Parameter(names = ["--add"])
    private var addName: String? = null

    @Parameter(names = ["--genre"])
    private var genre: String? = null

    @Parameter(names = ["--hours"])
    private var hours: Double? = null

    @Parameter(names = ["--price"])
    private var price: Double? = null

    @Parameter(names = ["--rating"])
    private var rating: Double? = null

    @Parameter(names = ["--remove"])
    private var removeName: String? = null

    @Parameter(names = ["--list"])
    private var list: Boolean = false

    @Parameter(names = ["--filter"])
    private var filterGenre: String? = null

    @Parameter(names = ["--sort"])
    private var sortBy: String = "name"

    @Parameter(names = ["--search"])
    private var searchQuery: String? = null

    @Parameter(names = ["--export-json"])
    private var exportJson: String? = null

    @Parameter(names = ["--export-csv"])
    private var exportCsv: String? = null

    private val dataFile = "games.json"
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val type = object : TypeToken<MutableList<Game>>() {}.type
    private val games = mutableListOf<Game>()

    private fun load() {
        val f = File(dataFile)
        if (!f.exists()) return
        try {
            val json = f.readText()
            val list = gson.fromJson<MutableList<Game>>(json, type)
            games.addAll(list)
        } catch (e: Exception) { /* ignore */ }
    }

    private fun save() {
        val json = gson.toJson(games)
        File(dataFile).writeText(json)
    }

    private fun addGame(name: String, genre: String, hours: Double, price: Double, rating: Double) {
        if (games.any { it.name.equals(name, ignoreCase = true) }) {
            println("\u001B[31mИгра '$name' уже существует.\u001B[0m")
            return
        }
        games.add(Game(name, genre, hours, price, rating))
        save()
        println("\u001B[32mИгра '$name' добавлена.\u001B[0m")
    }

    private fun removeGame(name: String) {
        val idx = games.indexOfFirst { it.name.equals(name, ignoreCase = true) }
        if (idx == -1) {
            println("\u001B[31mИгра '$name' не найдена.\u001B[0m")
            return
        }
        games.removeAt(idx)
        save()
        println("\u001B[33mИгра '$name' удалена.\u001B[0m")
    }

    private fun listGames(filter: String?, sort: String) {
        var list = games
        if (filter != null) {
            list = games.filter { it.genre.equals(filter, ignoreCase = true) }.toMutableList()
            if (list.isEmpty()) {
                println("\u001B[33mИгры с жанром '$filter' не найдены.\u001B[0m")
                return
            }
        }
        list.sortWith(Comparator { a, b ->
            when (sort) {
                "hours" -> b.hours.compareTo(a.hours)
                "price" -> b.price.compareTo(a.price)
                "rating" -> b.rating.compareTo(a.rating)
                else -> a.name.compareTo(b.name, ignoreCase = true)
            }
        })
        if (list.isEmpty()) {
            println("\u001B[33mКаталог пуст.\u001B[0m")
            return
        }
        println("\u001B[36m📋 Каталог игр:\u001B[0m")
        for (g in list) {
            println("  \u001B[32m${g.name}\u001B[0m | \u001B[33m${g.genre}\u001B[0m | \u001B[34m${g.hours} ч\u001B[0m | \u001B[35m${"%.2f".format(g.price)} руб\u001B[0m | \u001B[31m★ ${g.rating}\u001B[0m")
        }
    }

    private fun searchGames(query: String) {
        val results = games.filter {
            it.name.contains(query, ignoreCase = true) || it.genre.contains(query, ignoreCase = true)
        }
        if (results.isEmpty()) {
            println("\u001B[33mПо запросу '$query' ничего не найдено.\u001B[0m")
            return
        }
        println("\u001B[36m🔍 Найдено ${results.size} игр:\u001B[0m")
        for (g in results) {
            println("  \u001B[32m${g.name}\u001B[0m | \u001B[33m${g.genre}\u001B[0m | \u001B[34m${g.hours} ч\u001B[0m | \u001B[35m${"%.2f".format(g.price)} руб\u001B[0m | \u001B[31m★ ${g.rating}\u001B[0m")
        }
    }

    private fun exportJson(filename: String) {
        val json = gson.toJson(games)
        File(filename).writeText(json)
        println("\u001B[32mЭкспортировано в $filename (JSON)\u001B[0m")
    }

    private fun exportCsv(filename: String) {
        File(filename).printWriter().use { pw ->
            pw.println("name,genre,hours,price,rating")
            for (g in games) {
                pw.println("${g.name},${g.genre},${g.hours},${g.price},${g.rating}")
            }
        }
        println("\u001B[32mЭкспортировано в $filename (CSV)\u001B[0m")
    }

    fun run() {
        load()
        when {
            addName != null -> {
                if (genre == null || hours == null || price == null || rating == null) {
                    System.err.println("\u001B[31mДля добавления игры требуются --genre, --hours, --price, --rating\u001B[0m")
                    System.exit(1)
                }
                if (rating!! < 1 || rating!! > 10) {
                    System.err.println("\u001B[31mРейтинг должен быть от 1 до 10\u001B[0m")
                    System.exit(1)
                }
                addGame(addName!!, genre!!, hours!!, price!!, rating!!)
            }
            removeName != null -> removeGame(removeName!!)
            list -> listGames(filterGenre, sortBy)
            searchQuery != null -> searchGames(searchQuery!!)
            exportJson != null -> exportJson(exportJson!!)
            exportCsv != null -> exportCsv(exportCsv!!)
            else -> println("Используйте --help для справки.")
        }
    }
}

fun main(args: Array<String>) {
    val catalog = GameCatalog()
    JCommander.newBuilder().addObject(catalog).build().parse(*args)
    catalog.run()
}
