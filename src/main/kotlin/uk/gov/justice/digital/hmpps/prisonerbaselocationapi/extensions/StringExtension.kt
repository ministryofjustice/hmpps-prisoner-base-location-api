package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.extensions

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.text.replace
import kotlin.text.toRegex

fun String.removeWhitespaceAndNewlines(): String = this.replace("(\"[^\"]*\")|\\s".toRegex(), "\$1")

fun String.decodeUrlCharacters(): String = URLDecoder.decode(this, StandardCharsets.UTF_8)
