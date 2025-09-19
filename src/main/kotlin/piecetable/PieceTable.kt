/**
 *@BelongsProject: PieceTable
 *@BelongsPackage: com.timvx.piecetable
 *@Author: TIMAVICIIX
 *@CreateTime: 2025-09-17  19:59
 *@Description: TODO
 *@Version: 1.0
 */

package com.timvx.piecetable

import com.timvx.piecetable.event.DeleteEvent
import com.timvx.piecetable.event.EditEvent
import com.timvx.piecetable.event.InsertEvent
import com.timvx.piecetable.struct.BufferKind
import com.timvx.piecetable.struct.ListBuffer
import com.timvx.piecetable.struct.Piece
import kotlin.random.Random

/**
 * PieceTable 使用隐式 Treap 实现 order-statistic（按字符索引 split/merge）
 * 包含 undo/redo，且在每次 edit 后做相邻片段合并（避免 piece 泡沫）。
 */
class PieceTable<T>(
    original: List<T> = emptyList(),
    private val maxHistory: Int = 100 // 限制 undo/redo 栈的最大长度
) {
    private val originalBuffer: ListBuffer<T> = ListBuffer(original)
    private val addBuffer: MutableList<T> = mutableListOf()

    private var root: TreapNode<T>? = null

    // undo / redo
    private val undoStack = ArrayDeque<EditEvent<T>>()
    private val redoStack = ArrayDeque<EditEvent<T>>()

    init {
        if (original.isNotEmpty()) {
            root = TreapNode(Piece(BufferKind.ORIGINAL, 0, original.size))
            root?.recalc()
        }
    }

    /** 封装 push 方法，带栈长度限制 */
    private fun pushUndo(ev: EditEvent<T>) {
        undoStack.addLast(ev)
        if (undoStack.size > maxHistory) {
            undoStack.removeFirst()
        }
    }

    private fun pushRedo(ev: EditEvent<T>) {
        redoStack.addLast(ev)
        if (redoStack.size > maxHistory) {
            redoStack.removeFirst()
        }
    }

    // Treap 节点
    private class TreapNode<T>(
        val piece: Piece<T>,
        val priority: Int = Random.nextInt(),
        var left: TreapNode<T>? = null,
        var right: TreapNode<T>? = null
    ) {
        var subtreeChars: Int = piece.length
        val chars: Int get() = piece.length
        fun recalc() {
            subtreeChars = piece.length + (left?.subtreeChars ?: 0) + (right?.subtreeChars ?: 0)
        }
    }

    fun modify(index: Int, record: Boolean = true, transform: (T) -> T) {
        val total = root?.subtreeChars ?: 0
        if (index < 0 || index >= total) return

        val oldValue = collect()[index]
        val newValue = transform(oldValue)

        if (oldValue == newValue) return // 无变化，不记录

        delete(index, 1, record = false)
        insert(index, listOf(newValue), record = false)

        if (record) {
            undoStack.addLast(com.timvx.piecetable.event.ModifyEvent(index, oldValue, newValue))
            redoStack.clear()
        }
    }


    fun insert(posInput: Int, obj: T, record: Boolean = true) {
        insert(posInput, listOf(obj), record)
    }

    // insert / delete / undo / redo ... 同 String 版本，泛型化即可
    fun insert(posInput: Int, objList: List<T>, record: Boolean = true) {
        if (objList.isEmpty()) return
        var pos = posInput.coerceAtLeast(0)
        val total = root?.subtreeChars ?: 0
        if (pos > total) pos = total

        val addStart = addBuffer.size
        addBuffer.addAll(objList)

        val newPiece = Piece<T>(BufferKind.ADD, addStart, objList.size)
        val newNode = TreapNode(newPiece)

        val (left, right) = split(root, pos)
        root = merge(merge(left, newNode), right)
        mergeAdjacentFull()

        if (record) {
            undoStack.addLast(InsertEvent(pos, objList))
            redoStack.clear()
        }
    }

    fun delete(posInput: Int, lengthInput: Int, record: Boolean = true): List<T> {
        if (lengthInput <= 0) return emptyList()
        val pos = posInput.coerceAtLeast(0)
        val total = root?.subtreeChars ?: 0
        if (pos >= total) return emptyList()
        var length = lengthInput
        if (pos + length > total) length = total - pos

        val (left, midRight) = split(root, pos)
        val (mid, right) = split(midRight, length)

        val deleted = mutableListOf<T>()
        inorderCollect(mid) { piece ->
            val buf = if (piece.buffer == BufferKind.ORIGINAL) originalBuffer else ListBuffer(addBuffer)
            deleted.addAll(buf.subSequence(piece.start, piece.end))
        }

        root = merge(left, right)
        mergeAdjacentFull()

        if (record) {
            undoStack.addLast(DeleteEvent(pos, deleted))
            redoStack.clear()
        }
        return deleted
    }

    fun collect(): List<T> = buildList {
        inorderCollect(root) { piece ->
            val buf = if (piece.buffer == BufferKind.ORIGINAL) originalBuffer else ListBuffer(addBuffer)
            addAll(buf.subSequence(piece.start, piece.end))
        }
    }

    private fun inorderCollect(n: TreapNode<T>?, visit: (Piece<T>) -> Unit) {
        if (n == null) return
        inorderCollect(n.left, visit)
        visit(n.piece)
        inorderCollect(n.right, visit)
    }


    /***************** Undo / Redo *****************/
    fun undo() {
        val ev = undoStack.removeLastOrNull() ?: return
        ev.undo(this)
        pushRedo(ev)
    }

    fun redo() {
        val ev = redoStack.removeLastOrNull() ?: return
        ev.redo(this)
        pushUndo(ev)
    }

    /***************** Treap merge / split / rebuild *****************/
    /***************** 基本 Treap 操作 *****************/

    // merge 两棵 treap（所有节点顺序保持），返回新的 root
    private fun merge(a: TreapNode<T>?, b: TreapNode<T>?): TreapNode<T>? {
        if (a == null) return b
        if (b == null) return a
        return if (a.priority > b.priority) {
            a.right = merge(a.right, b)
            a.recalc()
            a
        } else {
            b.left = merge(a, b.left)
            b.recalc()
            b
        }
    }

    // split 按字符数量 pos，把 t 分成 (left, right)，left 含前 pos 个字符
    private fun split(t: TreapNode<T>?, pos: Int): Pair<TreapNode<T>?, TreapNode<T>?> {
        if (t == null) return null to null
        val leftSize = t.left?.subtreeChars ?: 0
        return when {
            pos < leftSize -> {
                val (l, r) = split(t.left, pos)
                t.left = r
                t.recalc()
                l to t
            }

            pos > leftSize + t.chars -> {
                val (l, r) = split(t.right, pos - leftSize - t.chars)
                t.right = l
                t.recalc()
                t to r
            }

            else -> {
                when (val offsetInNode = pos - leftSize) {
                    0 -> {
                        // 全部在右边
                        return t.left to TreapNode(t.piece.copy(), t.priority, null, t.right).also { it.recalc() }
                    }

                    t.chars -> {
                        // 全部在左边
                        return TreapNode(t.piece.copy(), t.priority, t.left, null).also { it.recalc() } to t.right
                    }

                    else -> {
                        // 拆分当前节点 piece
                        val leftPiece = Piece<T>(t.piece.buffer, t.piece.start, offsetInNode)
                        val rightPiece =
                            Piece<T>(t.piece.buffer, t.piece.start + offsetInNode, t.piece.length - offsetInNode)

                        val leftNode = TreapNode(leftPiece, Random.nextInt())
                        val rightNode = TreapNode(rightPiece, Random.nextInt())

                        leftNode.left = t.left
                        rightNode.right = t.right

                        leftNode.recalc()
                        rightNode.recalc()

                        return leftNode to rightNode
                    }
                }
            }
        }
    }

    private fun mergeAdjacentFull() {
        val list = ArrayList<Piece<T>>()
        inorderCollect(root) { piece ->
            if (list.isEmpty()) list.add(piece.copy())
            else {
                val last = list.last()
                if (last.buffer == piece.buffer && last.end == piece.start) {
                    // 源 buffer 在索引上连续 -> 合并
                    last.length += piece.length
                } else {
                    list.add(piece.copy())
                }
            }
        }
        // rebuild treap from list
        root = buildTreapFromPieces(list, 0, list.size)
    }

    // build balanced-ish treap from pieces [l, r)
    private fun buildTreapFromPieces(list: List<Piece<T>>, l: Int, r: Int): TreapNode<T>? {
        if (l >= r) return null
        val mid = (l + r) / 2
        val node = TreapNode(list[mid], Random.nextInt())
        node.left = buildTreapFromPieces(list, l, mid)
        node.right = buildTreapFromPieces(list, mid + 1, r)
        node.recalc()
        return node
    }
}
