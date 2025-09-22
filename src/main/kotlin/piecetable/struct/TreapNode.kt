/**
 *@BelongsProject: PieceTable
 *@BelongsPackage: com.timvx.piecetable.struct
 *@Author: TIMAVICIIX
 *@CreateTime: 2025-09-17  19:59
 *@Description: TODO
 *@Version: 1.0
 */

package com.ebmlibs.piecetable.struct

import kotlin.random.Random

/**
 * 隐式 Treap 节点（每个节点对应一个 Piece）
 */
class TreapNode<T>(
    var piece: Piece<T>,
    val priority: Int = Random.nextInt(),
    var left: TreapNode<T>? = null,
    var right: TreapNode<T>? = null
) {
    var chars: Int = piece.length
    var subtreeChars: Int = chars

    fun recalc() {
        chars = piece.length
        subtreeChars = chars + (left?.subtreeChars ?: 0) + (right?.subtreeChars ?: 0)
    }
}