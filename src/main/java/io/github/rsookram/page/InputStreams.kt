package io.github.rsookram.page

import java.io.InputStream

fun InputStream.fullSkip(n: Int) {
    var toSkip = n.toLong()
    while (toSkip > 0) {
        toSkip -= skip(toSkip)
    }
}
