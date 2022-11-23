package tasklist

class Task() {
    var descriptions: MutableList<String> = mutableListOf<String>()
    var priority: String = ""
    var date: String = ""
    var time: String = ""
    var overdue: String = ""
}