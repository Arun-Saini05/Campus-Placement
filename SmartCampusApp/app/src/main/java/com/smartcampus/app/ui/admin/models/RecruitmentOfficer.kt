package com.smartcampus.app.ui.admin.models

data class RecruitmentOfficer(
    var id: Int,
    var name: String,
    var email: String,
    var company: String
) {
    override fun toString(): String {
        return "Recruitment Officer(ID: $id, Name: '$name', Email: '$email', Company: '$company')"
    }
}
