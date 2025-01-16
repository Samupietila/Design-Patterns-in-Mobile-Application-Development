class Major(val name: String) {
    val students: MutableList<Student> = mutableListOf()

    fun addStudent(student: Student) {
        students.add(student)
    }

    fun stats(): Triple<Double, Double, Double> {
        val averages = students.map { it.weightedAverage() }
        val min = averages.minOrNull() ?: 0.0
        val max = averages.maxOrNull() ?: 0.0
        val avgOfAvg = averages.sum() / averages.size
        return Triple(min, max, avgOfAvg)
    }

    fun stats(courseName: String): Triple<Double, Double, Double> {
        val averages = students.map { student ->
            val grades = student.courses.filter { it.name == courseName }.map { it.grade }
            if (grades.isNotEmpty()) grades.sum() / grades.size else 0.0
        }
        val min = averages.minOrNull() ?: 0.0
        val max = averages.maxOrNull() ?: 0.0
        val avgOfAvg = averages.sum() / averages.size
        return Triple(min, max, avgOfAvg)
    }
}