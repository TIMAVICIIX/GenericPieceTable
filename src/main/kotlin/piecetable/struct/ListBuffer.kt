/**
 *@BelongsProject: GenericPieceTable
 *@BelongsPackage: com.timvx.piecetable.struct
 *@Author: TIMAVICIIX
 *@CreateTime: 2025-09-17  20:50
 *@Description: TODO
 *@Version: 1.0
 */

package com.timvx.piecetable.struct

class ListBuffer<T>(private val list: List<T>) {
    val size: Int get() = list.size
    fun get(index: Int) = list[index]
    fun subSequence(start: Int, end: Int) = list.subList(start, end)
}
