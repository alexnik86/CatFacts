package com.testing.catfacts

data class AllCatFacts(val all:List<CatFact> = emptyList()) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AllCatFacts) return false

        if (all != other.all) return false

        return true
    }

    override fun hashCode(): Int {
        return all.hashCode()
    }
}