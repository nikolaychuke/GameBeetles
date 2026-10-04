package com.example.beetles

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "ValCurs", strict = false)
class CurrencyResponse @JvmOverloads constructor(
    @field:ElementList(inline = true, required = false, entry = "Valute")
    var valutes: List<Valute>? = null
)

@Root(name = "Valute", strict = false)
class Valute @JvmOverloads constructor(
    @field:Element(name = "CharCode", required = false)
    var charCode: String? = null,

    @field:Element(name = "Value", required = false)
    var value: String? = null
)