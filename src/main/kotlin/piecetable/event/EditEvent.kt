/**
 *@BelongsProject: PieceTable
 *@BelongsPackage: com.timvx.piecetable.event
 *@Author: TIMAVICIIX
 *@CreateTime: 2025-09-17  19:59
 *@Description: TODO
 *@Version: 1.0
 */

package com.timvx.piecetable.event

import com.timvx.piecetable.PieceTable

sealed class EditEvent<T> {
    abstract fun undo(table: PieceTable<T>)
    abstract fun redo(table: PieceTable<T>)
}

class InsertEvent<T>(val pos: Int, val input: List<T>) : EditEvent<T>() {
    override fun undo(table: PieceTable<T>) {
        table.delete(pos, input.size, record = false)
    }

    override fun redo(table: PieceTable<T>) = table.insert(pos, input, record = false)
}

class DeleteEvent<T>(val pos: Int, val input: List<T>) : EditEvent<T>() {
    override fun undo(table: PieceTable<T>) = table.insert(pos, input, record = false)
    override fun redo(table: PieceTable<T>) {
        table.delete(pos, input.size, record = false)
    }
}