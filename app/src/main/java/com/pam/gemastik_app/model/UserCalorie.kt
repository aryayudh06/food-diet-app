package com.pam.gemastik_app.model

class UserCalorie {
    // Getter dan setter untuk tb
    var tb: String? = null

    // Getter dan setter untuk bb
    var bb: String? = null
    var calorie: String? = null

    // Default constructor diperlukan untuk Firebase
    constructor()

    // Constructor dengan parameter
    constructor(tb: String?, bb: String?, calorie: String?) {
        this.tb = tb
        this.bb = bb
        this.calorie = calorie
    }

}
