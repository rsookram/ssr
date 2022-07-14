package io.github.rsookram.page

import io.github.rsookram.ssr.entity.Crop
import java.io.File

data class CroppedPage(val page: Page, val crop: Crop)

data class Page(val file: File, val offset: Int)
