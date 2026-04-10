package com.aleespa.randomsquare

import org.junit.Test
import com.chaquo.python.Python

class ClasspathTest {
    @Test
    fun testPythonClassExists() {
        println("Python class: " + Python::class.java.name)
    }
}
