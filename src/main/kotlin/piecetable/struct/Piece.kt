/**
 *@BelongsProject: PieceTable
 *@BelongsPackage: com.timvx.piecetable
 *@Author: TIMAVICIIX
 *@CreateTime: 2025-09-17  19:58
 *@Description: TODO
 *@Version: 1.0
 */

package com.timvx.piecetable.struct

data class Piece<T>(
    val buffer: BufferKind,
    var start: Int,
    var length: Int
) {
    val end: Int get() = start + length
}