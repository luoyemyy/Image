package com.github.luoyemyy.image.picker.gallery

import com.github.luoyemyy.aclin.mvp.DataItem

class Image(var path: String, var select: Boolean = false) : DataItem()

class Bucket(var id: String, var name: String, var select: Boolean = false, var images: MutableList<Image> = mutableListOf()) : DataItem()