package com.bruno.notes.helpers

class TakePictureAndDetailsCommunication {
    var savedPicture = false

    companion object {
        private var INSTANCE: TakePictureAndDetailsCommunication? = null

        fun getInstance(): TakePictureAndDetailsCommunication {
            return INSTANCE ?: synchronized(this) {
                val instance = TakePictureAndDetailsCommunication()
                INSTANCE = instance
                return instance
            }
        }
    }
}