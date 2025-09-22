/**
 *@BelongsProject: PieceTable
 *@BelongsPackage: com.timvx.piecetable.event
 *@Author: TIMAVICIIX
 *@CreateTime: 2025-09-17  19:59
 *@Description: TODO
 *@Version: 1.0
 */

package com.ebmlibs.piecetable.event

import com.ebmlibs.piecetable.PieceTable

sealed class EditEvent<T> {
    abstract fun undo(table: PieceTable<T>)
    abstract fun redo(table: PieceTable<T>)
}

class InsertEvent<T>(private val pos: Int, private val input: List<T>) : EditEvent<T>() {
    override fun undo(table: PieceTable<T>) {
        table.delete(pos, input.size, record = false)
    }

    override fun redo(table: PieceTable<T>) = table.insert(pos, input, record = false)
}

class DeleteEvent<T>(private val pos: Int, private val input: List<T>) : EditEvent<T>() {
    override fun undo(table: PieceTable<T>) = table.insert(pos, input, record = false)
    override fun redo(table: PieceTable<T>) {
        table.delete(pos, input.size, record = false)
    }
}

class ModifyEvent<T>(
    private val pos: Int,
    private val oldValue: T,
    private val newValue: T
) : EditEvent<T>() {
    override fun undo(table: PieceTable<T>) {
        table.delete(pos, 1, record = false)
        table.insert(pos, listOf(oldValue), record = false)
    }

    override fun redo(table: PieceTable<T>) {
        table.delete(pos, 1, record = false)
        table.insert(pos, listOf(newValue), record = false)
    }
}