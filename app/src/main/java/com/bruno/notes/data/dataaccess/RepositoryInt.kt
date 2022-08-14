package com.bruno.notes.data.dataaccess

interface RepositoryInt<T> {
    fun getAll(): List<T>

    fun getById(id: Int): T

    fun getByTitle(title: String): List<T>

    fun containsInTitleOrBody(toSearch: String): List<T>

    fun save(input: T): Int

    fun update(id: Int, toUpdate: T): T

    fun delete(id: Int): Boolean
}