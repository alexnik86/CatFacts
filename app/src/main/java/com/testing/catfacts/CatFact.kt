package com.testing.catfacts

data class CatFact(val text: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CatFact) return false

        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        return text.hashCode()
    }
}