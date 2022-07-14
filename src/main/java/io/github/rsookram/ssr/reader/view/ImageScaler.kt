package io.github.rsookram.ssr.reader.view

typealias ScaleImage = (Int, Int, Int, Int) -> Pair<Int, Int>

object ImageScaler {

    val maxWidth: ScaleImage = { imageWidth, imageHeight, maxWidth, _ ->
        Pair(
            maxWidth,
            (imageHeight * (maxWidth.toDouble() / imageWidth)).toInt()
        )
    }

    val maxHeight: ScaleImage = { imageWidth, imageHeight, _, maxHeight ->
        Pair(
            (imageWidth * (maxHeight.toDouble() / imageHeight)).toInt(),
            maxHeight
        )
    }

    val fitCenter: ScaleImage = { imageWidth, imageHeight, maxWidth, maxHeight ->
        if ((imageWidth.toDouble() / maxWidth) > (imageHeight.toDouble() / maxHeight)) {
            maxWidth(imageWidth, imageHeight, maxWidth, maxHeight)
        } else {
            maxHeight(imageWidth, imageHeight, maxWidth, maxHeight)
        }
    }
}
