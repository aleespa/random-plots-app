package com.aleespa.randomsquare.tools

import org.junit.Assert.assertEquals
import org.junit.Test

class LatexTest {

    @Test
    fun testGenerateNewtonLatex() {
        // Test case: p(z) = z^3 - 1
        // coeffs are in order: [0] -> z^0, [1] -> z^1, ...
        val coeffs1 = doubleArrayOf(-1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        assertEquals("p(z) = z^{3} - 1", generateNewtonLatex(coeffs1))

        // Test case: p(z) = z^2 + 2z + 1
        val coeffs2 = doubleArrayOf(1.0, 2.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        assertEquals("p(z) = z^{2} + 2z + 1", generateNewtonLatex(coeffs2))

        // Test case: p(z) = -z^3 + 5
        val coeffs3 = doubleArrayOf(5.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        assertEquals("p(z) = -z^{3} + 5", generateNewtonLatex(coeffs3))

        // Test case: All zeros
        val coeffs4 = DoubleArray(9) { 0.0 }
        assertEquals("p(z) = 0", generateNewtonLatex(coeffs4))
        
        // Test case: only constant
        val coeffs5 = doubleArrayOf(10.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        assertEquals("p(z) = 10", generateNewtonLatex(coeffs5))
    }
}
