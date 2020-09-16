package com.interswitchng.smartpos.emv.pax.utilities

import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition
import java.util.*
import kotlin.math.min


class StringAlignUtils(maxChars: Int, align: Alignment?) : Format() {

    enum class Alignment {
        LEFT, CENTER, RIGHT
    }

    /** Current justification for formatting  */
    private var currentAlignment: Alignment? = null

    /** Current max length of a line  */
    private val maxChars: Int
    override fun format(input: Any, where: StringBuffer, ignore: FieldPosition): StringBuffer {
        val s = input.toString()
        val strings = splitInputString(s)
        val listItr = strings.listIterator()
        while (listItr.hasNext()) {
            val wanted = listItr.next()
            when (currentAlignment) {
                Alignment.RIGHT -> {
                    pad(where, maxChars - wanted.length)
                    where.append(wanted)
                }
                Alignment.CENTER -> {
                    val toAdd = maxChars - wanted.length
                    pad(where, toAdd / 2)
                    where.append(wanted)
                    pad(where, toAdd - toAdd / 2)
                }
                Alignment.LEFT -> {
                    where.append(wanted)
                    pad(where, maxChars - wanted.length)
                }
            }
            where.append("\n")
        }
        return where
    }

    private fun pad(to: StringBuffer, howMany: Int) {
        for (i in 0 until howMany) to.append(' ')
    }

    fun format(s: String): String {
        return format(s, StringBuffer(), FieldPosition(0)).toString()
    }

    /** ParseObject is required, but not useful here.  */
    override fun parseObject(source: String, pos: ParsePosition): Any {
        return source
    }

    private fun splitInputString(str: String?): List<String> {
        val list: MutableList<String> = ArrayList()
        if (str == null) return list
        var i = 0
        while (i < str.length) {
            val endIndex = min(i + maxChars, str.length)
            list.add(str.substring(i, endIndex))
            i += maxChars
        }
        return list
    }

    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        when (align) {
            Alignment.LEFT, Alignment.CENTER, Alignment.RIGHT -> currentAlignment = align
            else -> throw IllegalArgumentException("invalid justification arg.")
        }
        require(maxChars >= 0) { "maxChars must be positive." }
        this.maxChars = maxChars
    }
}