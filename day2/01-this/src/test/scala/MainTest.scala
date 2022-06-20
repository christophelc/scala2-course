import com.example.VectorInt
import org.scalatest.{ FlatSpec, MustMatchers }

class MainTest extends FlatSpec with MustMatchers {

  "Vector(1, 2) at 1" must "equal 2" in {
    val v1 = VectorInt(Seq(1, 2))
    assert(v1.cell(1) == 2)
  }
  "Vector(1, 2) + Vector(1, 3" must "equal Vector(2,5)" in {
    val v1        = VectorInt(Seq(1, 2))
    val v2        = VectorInt(Seq(1, 3))
    val vExpected = VectorInt(Seq(2, 5))
    assert(v1 + v2 == vExpected)
  }
}
