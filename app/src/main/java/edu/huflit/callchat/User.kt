package edu.huflit.callchat


data class User(
    var name: String? = null,
    var email: String? = null,
    var uid: String? = null,
    var profileImage: String? = null
) {
    // Default constructor
    constructor() : this("", "", "", "")
}
