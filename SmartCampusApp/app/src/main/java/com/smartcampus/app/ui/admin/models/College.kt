package com.smartcampus.app.ui.admin.models

data class College(
    var id: Int,
    var name: String,
    var location: String
) {
    override fun toString(): String {
        return "College(ID: $id, Name: '$name', Location: '$location')"
    }
}
