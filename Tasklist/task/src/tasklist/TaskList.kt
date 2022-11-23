package tasklist

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import java.lang.RuntimeException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

const val redColor = "\u001B[101m \u001B[0m"
const val yellowColor = "\u001B[103m \u001B[0m"
const val greenColor = "\u001B[102m \u001B[0m"
const val blueColor = "\u001B[104m \u001B[0m"

class TaskList {
    var tasks = mutableListOf<Task>()
    private val listPriorities = listOf("C", "H", "N", "L")
    val priorityColors = mapOf("C" to redColor, "H" to yellowColor, "N" to greenColor, "L" to blueColor)
    val dueColors = mapOf("O" to redColor, "T" to yellowColor, "I" to greenColor)

    private val formatterDate = DateTimeFormatter.ofPattern("yyyy-M-d", Locale.US)
    private val formatterTime = DateTimeFormatter.ofPattern("H:m")

    fun addTask() {
        val task = Task()
        task.priority = parseTaskPriority()
        task.date = parseTaskDate()
        task.overdue = parseTaskOverdue(task)
        task.time = parseTaskTime()
        task.descriptions = parseTaskDescription()
        tasks.add(task)
    }

    fun isEmpty(): Boolean {
        return tasks.isEmpty()
    }

    fun printTasks() {
        println(
            """
            +----+------------+-------+---+---+--------------------------------------------+
            | N  |    Date    | Time  | P | D |                   Task                     |
            +----+------------+-------+---+---+--------------------------------------------+
        """.trimIndent()
        )
        for (i in 0 until tasks.size) {
            val index = i + 1
            val task = tasks[i]
            val header = "${task.date} | ${task.time} | ${priorityColors[task.priority]} | ${dueColors[task.overdue]} |"
            if (index < 10) {
                print("| $index  | $header")
            } else {
                print("| $index | $header")
            }

            val chunkedDescriptions = task.descriptions
                .flatMap { it.chunked(44) }

            if (chunkedDescriptions[0].length < 44) {
                val numberOfBlanks = 44 - chunkedDescriptions[0].length
                val addString = " ".repeat(numberOfBlanks)
                println("${chunkedDescriptions[0] + addString}|")
            } else {
                println("${chunkedDescriptions[0]}|")
            }

            for (j in 1 until chunkedDescriptions.size) {
                if (chunkedDescriptions[j].length < 44) {
                    val numberOfBlanks = 44 - chunkedDescriptions[j].length
                    val addString = " ".repeat(numberOfBlanks)
                    println("|    |            |       |   |   |${chunkedDescriptions[j] + addString}|")
                } else {
                    println("|    |            |       |   |   |${chunkedDescriptions[j]}|")
                }
            }
            println("+----+------------+-------+---+---+--------------------------------------------+")
        }
    }

    fun deleteTask() {
        while (true) {
            println("Input the task number (1-${tasks.size}):")
            try {
                val userDelete = readln().toInt()
                if (userDelete > tasks.size || userDelete <= 0) {
                    println("Invalid task number")
                    continue
                } else {
                    tasks.removeAt(userDelete - 1)
                    println("The task is deleted")
                    break
                }
            } catch (e: RuntimeException) {
                println("Invalid task number")
            }
        }
    }

    fun editTask() {
        while (true) {
            println("Input the task number (1-${this.tasks.size}):")
            try {
                val userEditTask = readln().toInt()
                if (userEditTask > this.tasks.size || userEditTask <= 0) {
                    println("Invalid task number")
                    continue
                } else {
                    while (true) {
                        println("Input a field to edit (priority, date, time, task):")
                        val index = userEditTask - 1
                        when (readln()) {
                            "time" -> {
                                this.tasks[index].time = parseTaskTime()
                            }

                            "task" -> {
                                this.tasks[index].descriptions = parseTaskDescription()
                            }

                            "priority" -> {
                                this.tasks[index].priority = parseTaskPriority()
                            }

                            "date" -> {
                                this.tasks[index].date = parseTaskDate()
                            }

                            else -> {
                                println("Invalid field")
                                continue
                            }
                        }
                        return println("The task is changed")
                    }
                }
            } catch (e: RuntimeException) {
                println("Invalid task number")
            }
        }
    }

    private fun parseTaskDescription(): MutableList<String> {
        val descriptions = mutableListOf<String>()
        println("Input a new task (enter a blank line to end):")
        while (true) {
            val userAdd = readln().trim()
            if (userAdd.isNotEmpty()) {
                descriptions.add(userAdd)
            } else {
                break
            }
        }
        if (descriptions.isEmpty()) {
            println("The task is blank")
        } else {
            return descriptions
        }
        return descriptions
    }

    private fun parseTaskTime(): String {
        while (true) {
            println("Input the time (hh:mm):")
            val userTime = readln()
            if (!isValidTime(userTime)) {
                continue
            } else {
                return getFormattedTime(userTime)
            }
        }
    }

    private fun isValidTime(userTime: String): Boolean {
        try {
            parseTime(userTime)
        } catch (e: RuntimeException) {
            println("The input time is invalid")
            return false
        }
        return true
    }

    private fun getFormattedTime(userTime: String): String {
        return LocalTime.parse(userTime, formatterTime).toString()
    }

    private fun parseTime(userInput: String): LocalTime {
        val list = userInput.split(":")
        return LocalTime.of(list[0].toInt(), list[1].toInt())
    }

    private fun parseTaskOverdue(task: Task): String {
        val taskDate = parseDate(task.date)
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
        val numberOfDays = currentDate.daysUntil(taskDate)
        return if (numberOfDays < 0) {
            "O"
        } else if (numberOfDays > 0) {
            "I"
        } else {
            "T"
        }
    }

    private fun parseTaskDate(): String {
        while (true) {
            println("Input the date (yyyy-mm-dd):")
            val userDate = readln()
            if (!isValidDate(userDate)) {
                continue
            } else {
                return getFormattedDate(userDate)
            }
        }
    }

    private fun isValidDate(userDate: String): Boolean {
        try {
            parseDate(userDate)
        } catch (e: RuntimeException) {
            println("The input date is invalid")
            return false
        }
        return true
    }

    private fun parseDate(userInput: String): LocalDate {
        val list = userInput.split("-")
        return LocalDate(list[0].toInt(), list[1].toInt(), list[2].toInt())
    }

    private fun getFormattedDate(userDate: String): String {
        return java.time.LocalDate.parse(userDate, formatterDate).toString()
    }

    private fun parseTaskPriority(): String {
        while (true) {
            println("Input the task priority (C, H, N, L):")
            val userPriority = readln().uppercase()
            if (!isValidPriority(userPriority)) {
                continue
            } else {
                return userPriority
            }
        }
    }

    private fun isValidPriority(userPriority: String): Boolean {
        return userPriority in listPriorities
    }
}