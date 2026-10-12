// This is a sample to-do list, a more advanced one will be used on the actual application

import java.io.File

// Declared in ranking order, so HIGH sorts above LOW
enum class Priority(val label: String) {
    HIGH("High"),
    LOW("Low")
}

data class Task(val text: String, val priority: Priority)

private val DEFAULT_FILE = File(System.getProperty("user.home"), ".trailmate_todo")

// Holds the tasks and saves them to a file. There is no UI code in here, so a UI can call this directly
class ToDoList(private val file: File = DEFAULT_FILE) {
    private val tasks = mutableListOf<Task>()

    init {
        if (file.exists()) {
            for (line in file.readLines()) {
                val parts = line.split("\t", limit = 2)
                val priority = Priority.entries.find { it.name == parts[0] }
                if (parts.size == 2 && priority != null) tasks.add(Task(parts[1], priority))
            }
            tasks.sortBy { it.priority }
        }
    }

    // High priority tasks first, then low priority, each in the order they were added
    fun all(): List<Task> = tasks.toList()

    // Add: fails if the task is blank
    fun add(text: String, priority: Priority): Boolean {
        val cleaned = clean(text)
        if (cleaned.isEmpty()) return false
        tasks.add(Task(cleaned, priority))
        save()
        return true
    }

    // Edit: changes the text and priority of the task at this position in all()
    fun edit(index: Int, text: String, priority: Priority): Boolean {
        val cleaned = clean(text)
        if (index !in tasks.indices || cleaned.isEmpty()) return false
        tasks[index] = Task(cleaned, priority)
        save()
        return true
    }

    // Remove: deletes the task at this position in all()
    fun remove(index: Int): Boolean {
        if (index !in tasks.indices) return false
        tasks.removeAt(index)
        save()
        return true
    }

    // Each task is saved on its own line, so it can't contain a line break
    private fun clean(text: String): String = text.replace(Regex("[\\r\\n]+"), " ").trim()

    private fun save() {
        tasks.sortBy { it.priority }
        file.writeText(tasks.joinToString("\n") { "${it.priority.name}\t${it.text}" })
    }
}
