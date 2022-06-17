package cn.ifafu.ifafu.service.parser

import cn.ifafu.ifafu.bean.dto.Term
import cn.ifafu.ifafu.bean.dto.TermOptions
import org.jsoup.nodes.Document

abstract class BaseOptionParser {
    protected fun parseOption(document: Document): TermOptions {
        val yearOptionNodes = document
            .selectFirst("select[name=xnd]")
            ?.children() ?: emptyList()

        val selectedYear = yearOptionNodes
            .firstOrNull { it.hasAttr("selected") }
            ?.`val`() ?: ""

        val years = yearOptionNodes
            .map { it.`val`() }
            .sortedByDescending { it }
            .toMutableList()

        val termOptionNodes = document
            .selectFirst("select[name=xqd]")
            ?.children() ?: emptyList()

        val selectedTerm = termOptionNodes
            .firstOrNull { it.hasAttr("selected") }
            ?.`val`() ?: ""

        val terms = termOptionNodes
            .map { it.`val`() }
            .filter { it != "3" }
            .sorted()
            .toMutableList()

        val list = ArrayList<Term>()
        for (year in years) {
            for (term in terms) {
                list.add(Term(year, term))
            }
        }
        val select = Term(selectedYear, selectedTerm)

        return TermOptions(select, list)
    }
}