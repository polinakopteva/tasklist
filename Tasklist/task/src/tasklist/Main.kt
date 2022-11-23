package tasklist

import java.io.File
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

val fileName = "tasklist.json"
val listActions = listOf("add", "print", "edit", "delete", "end")
val listActionNotEmptyTasks = listOf("print", "delete", "edit")

fun main() {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val type = Types.newParameterizedType(MutableList::class.java, Task::class.java)
    val taskAdapter = moshi.adapter<MutableList<Task>>(type)

    val file = File(fileName)

    val tasks = TaskList()

    if (file.exists()) {
        val tasklist = File(fileName).readText()
        tasks.tasks = taskAdapter.fromJson(tasklist)!!
    }

    loop@ while (true) {
        println("Input an action (add, print, edit, delete, end):")
        val userInput = readln().trim()
        if (userInput !in listActions) {
            println("The input action is invalid")
            continue@loop
        } else if (userInput.isEmpty()) {
            continue@loop
        } else if (userInput == "end") {
            println("Tasklist exiting!")
            break@loop
        } else if ((userInput in listActionNotEmptyTasks) && tasks.isEmpty()) {
            println("No tasks have been input")
            continue@loop
        } else if (userInput == "add") {
            tasks.addTask()
            continue@loop
        } else if (userInput == "print") {
            tasks.printTasks()
            continue@loop
        } else if (userInput == "delete") {
            tasks.printTasks()
            tasks.deleteTask()
            continue@loop
        } else if (userInput == "edit") {
            tasks.printTasks()
            tasks.editTask()
            continue@loop
        }
    }
    val jsonFile = File(fileName)

    jsonFile.writeText(taskAdapter.toJson(tasks.tasks))
}



