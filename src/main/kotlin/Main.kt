package com.ebmlibs

import com.ebmlibs.piecetable.PieceTable
import com.ebmlibs.piecetable.test.User

// TEST ENTRY
fun main() {
objectTest()
}

fun objectTest() {
    println("=== Unit Test for PieceTable (Output Verification) ===")

    val ut = PieceTable(
        listOf(
            User("Lila"),
            User("Comba"),
            User("Lily")
        )
    )
    val ul = mutableListOf(
        User("Lila"),
        User("Comba"),
        User("Lily")
    )

    fun showResult(name: String) {
        println(
            """ 
                
            ${name}：
            Piece Table:${ut.collect()}
            List:${ul}
        """.trimIndent()
        )
        if (ul == ut.collect()){
            println("✅ Everything is Fine!")
        }
    }

    val object1 = User("Jack")
    ut.insert(1, object1)
    ul.add(1,object1)
    showResult("Insert")

    ut.delete(1, 1)
    ul.removeAt(1)
    showResult("Delete")

    ut.undo()
    ul.add(1,User("Jack"))
    showResult("Undo")

    ut.modify(1){ obj->
        obj.copy(name = "Card")
    }
    ul[1] = ul[1].copy(name = "Card")
    showResult("Modify")
}

fun strTest() {
    println("=== Unit Test & Fuzz Test for PieceTable (Output Verification) ===")

    val pt = PieceTable("Hello World".toList())
    val sb = StringBuilder("Hello World") // 用于验证结果

    fun verify(step: String) {
        val ptText = pt.collect()
        val sbText = sb.toString()
        println("$step:")
        println("  PieceTable: '$ptText'")
        println("  StringBuilder: '$sbText'")
        if (ptText.toString() != sbText) println("  ⚠️ Mismatch detected!")
        println()
    }

    // ---- 基础测试 ----
    pt.insert(5, ", Kotlin".toList())
    sb.insert(5, ", Kotlin")
    verify("Insert ', Kotlin' at pos 5")

    pt.delete(0, 6)
    sb.delete(0, 6)
    verify("Delete 6 chars from pos 0")

    // Undo / Redo 测试
    pt.undo()
    sb.insert(0, "Hello,")
    verify("Undo last operation")

    pt.undo()
    sb.delete(5, 13) // 对应插入 ", Kotlin"
    verify("Undo 2")

    pt.redo()
    sb.insert(5, ", Kotlin")
    verify("Redo")

    pt.redo()
    sb.delete(0, 6)
    verify("Redo 2")

    // ---- 边界测试 ----
    pt.insert(0, "Start-".toList())
    sb.insert(0, "Start-")
    verify("Insert 'Start-' at beginning")

    pt.insert(pt.collect().size, "-End".toList())
    sb.append("-End")
    verify("Insert '-End' at end")

    pt.delete(0, 1000) // 删除过长
    sb.clear()
    verify("Delete oversized length")

    // ---- 随机 fuzz 测试 ----
    println("=== Random fuzz testing ===")
    val random = java.util.Random()
    val operations = 100

    val ptFuzz = PieceTable<Char>()

    val sbFuzz = StringBuilder()
    val undoStack = mutableListOf<String>()
    val redoStack = mutableListOf<String>()

    repeat(operations) { step ->
        val op = random.nextInt(3)
        when (op) {
            0 -> { // 插入随机字符串
                val pos = if (sbFuzz.isEmpty()) 0 else random.nextInt(sbFuzz.length + 1)
                val text = (1..random.nextInt(1, 10)).map { ('a'..'z').random() }.joinToString("")

                // 保存撤销状态
                undoStack.add(sbFuzz.toString())
                redoStack.clear()

                sbFuzz.insert(pos, text)
                ptFuzz.insert(pos, text.toList())
                println("Step $step: Insert '$text' at pos $pos")
            }

            1 -> { // 删除随机长度
                if (sbFuzz.isEmpty()) return@repeat
                val pos = random.nextInt(sbFuzz.length)
                val len = random.nextInt(1, sbFuzz.length - pos + 1)

                // 保存撤销状态
                undoStack.add(sbFuzz.toString())
                redoStack.clear()

                val deleted = sbFuzz.substring(pos, pos + len)
                sbFuzz.delete(pos, pos + len)
                ptFuzz.delete(pos, len)
                println("Step $step: Delete $len chars at pos $pos (deleted='$deleted')")
            }

            2 -> { // Undo / Redo
                if (random.nextBoolean()) { // Undo
                    if (undoStack.isNotEmpty()) {
                        redoStack.add(sbFuzz.toString())
                        sbFuzz.setLength(0)
                        sbFuzz.append(undoStack.removeAt(undoStack.size - 1))
                    }
                    ptFuzz.undo()
                    println("Step $step: Undo")
                } else { // Redo
                    if (redoStack.isNotEmpty()) {
                        undoStack.add(sbFuzz.toString())
                        sbFuzz.setLength(0)
                        sbFuzz.append(redoStack.removeAt(redoStack.size - 1))
                    }
                    ptFuzz.redo()
                    println("Step $step: Redo")
                }
            }
        }

        // 输出验证
        val ptText = ptFuzz.collect().joinToString().replace(", ", "")
        val sbText = sbFuzz.toString()
        println("  PieceTable: '$ptText'")
        println("  StringBuilder: '$sbText'")
        if (ptText != sbText) println("  ⚠️ Mismatch detected!")
        println()
    }


    println("=== Testing finished ===")
}

