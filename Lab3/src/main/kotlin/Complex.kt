import kotlin.math.sqrt

const val EPS = 0.0000001
class Complex(val real: Double, val imaginary: Double) {
    val abs: Double
        get() = sqrt(real*real + imaginary * imaginary)
    constructor(real: Int, imaginary: Int) : this(real.toDouble(), imaginary.toDouble())
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Complex) return false
        return this.real == other.real && this.imaginary == other.imaginary
    }
    /*
    override fun hashCode(): Int {
        return Objects.hash(this.real, this.imaginary)
    }*/
    operator fun plus(other: Complex): Complex {
        return Complex(this.real + other.real, this.imaginary + other.imaginary)
    }
    operator fun minus(other: Complex): Complex {
    return Complex(this.real - other.real, this.imaginary - other.imaginary)
    }
    operator fun times(other: Complex): Complex{
        return Complex(this.real * other.real - this.imaginary * other.imaginary, this.real * other.imaginary + this.imaginary * other.real)
    }

}