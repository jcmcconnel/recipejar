package recipejar.recipe

import recipejar.StringProcessor

/**
 * Port of original Ingredient.
 * Holds structured data for ingredients list.
 * parse and toXHTMLString preserve exact span format for HTML compatibility.
 */
data class Ingredient(
    var quantity: String = "",
    var unit: String = "",
    var name: String = ""
) {
    companion object {
        /**
         * Port of Java parse: extracts from <li> inner using span tokens, in sequence.
         */
        fun parse(s: String): Ingredient {
            var data0 = ""
            var data1 = ""
            var data2 = ""
            var remaining = s
            try {
                val qtyToken = "<span class=\"qty\">"
                val quantity = remaining.indexOf(qtyToken)
                if (quantity != -1) {
                    val endQuantity = remaining.indexOf("</span>", quantity)
                    if (endQuantity != -1) {
                        data0 = StringProcessor.removeCarriageReturns(remaining.substring(quantity + qtyToken.length, endQuantity)).trim()
                        remaining = StringProcessor.removeCarriageReturns(remaining.substring(endQuantity + 7))
                    }
                }
            } catch (_: IndexOutOfBoundsException) {
                // keep empty as original (KMP-safe; JVM used StringIndexOutOfBoundsException)
            }
            try {
                val unitToken = "<span class=\"unit\">"
                val unitIdx = remaining.indexOf(unitToken)
                if (unitIdx != -1) {
                    val endUnit = remaining.indexOf("</span>", unitIdx)
                    if (endUnit != -1) {
                        data1 = StringProcessor.removeCarriageReturns(remaining.substring(unitIdx + unitToken.length, endUnit)).trim()
                        remaining = StringProcessor.removeCarriageReturns(remaining.substring(endUnit + 7))
                    }
                }
            } catch (_: IndexOutOfBoundsException) {
            }
            try {
                val nameToken = "<span class=\"name\">"
                val nameIdx = remaining.indexOf(nameToken)
                if (nameIdx != -1) {
                    val endName = remaining.indexOf("</span>", nameIdx)
                    if (endName != -1) {
                        data2 = StringProcessor.removeCarriageReturns(remaining.substring(nameIdx + nameToken.length, endName)).trim()
                    }
                }
            } catch (_: IndexOutOfBoundsException) {
            }
            return Ingredient(data0, data1, data2)
        }
    }

    /**
     * Exact port of toXHTMLString for browser compat.
     */
    fun toXHTMLString(): String {
        val s = StringBuilder()
        s.append("         <li>")
        s.append("<span class=\"qty\">").append(quantity).append("</span> ")
        if (unit.isNotEmpty()) {
            s.append("<span class=\"unit\">").append(unit).append("</span> ")
        } else {
            s.append("<span class=\"unit\"></span> ")
        }
        s.append("<span class=\"name\">").append(name).append("</span>")
        s.append("</li>\n")
        return s.toString()
    }

    override fun toString(): String {
        val s = StringBuilder()
        s.append(quantity).append(" ")
        if (unit.isNotEmpty()) s.append(unit).append(" ")
        s.append(name).append(" ")
        return s.toString()
    }
}
