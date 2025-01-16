class Student(val name: String, val age: Int) {
    val courses: MutableList<CourseRecord> = mutableListOf()

    fun addCourse(course: CourseRecord) {
        courses.add(course)
    }

    fun weightedAverage(): Double {
        if (courses.isEmpty()) return 0.0
        val totalWeightedGrades = courses.sumOf { it.grade * it.credits }
        val totalCredits = courses.sumOf { it.credits }
        return if (totalCredits == 0) 0.0 else totalWeightedGrades / totalCredits
    }

    fun weightedAverage(year:Int): Double {
        val yearCourses = courses.filter { it.yearCompleted == year }
        if (yearCourses.isEmpty()) return 0.0
        val totalWeightedGrades = yearCourses.sumOf { it.grade * it.credits }
        val totalCredits = yearCourses.sumOf { it.credits }
        return totalWeightedGrades / totalCredits
    }

    fun minMaxGrades2(): Pair<Double, Double> {
        var min: Double = 99.0
        var max: Double = 0.0
        for (i in courses.indices) {
            if (courses[i].grade < min)
                min = courses[i].grade
            else if (courses[i].grade > max)
                max = courses[i].grade
        }
        return Pair(min, max)
    }
    fun minMaxGrades(): Pair<Double, Double> {
        val grades = courses.map { it.grade }
        val min = grades.minOrNull() ?: 0.0
        val max = grades.maxOrNull() ?: 0.0
        return Pair(min, max)
    }
}