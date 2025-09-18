# GenericPieceTable (Kotlin)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

---

## 简介 (Introduction)

**PieceTable** 是一个基于 Treap 实现的通用文本编辑数据结构，支持

* 高效插入与删除（平均时间复杂度 `O(log n)`）
* 撤销与重做操作，基于高级函数的修改功能
* 泛型化：不仅支持 `String`，还可以适配其它字符序列（例如 `Char`、`Byte`、自定义 Token）

它常用于文本编辑器、代码编辑器、富文本处理等场景。

**PieceTable** is a generic text editing data structure implemented with a Treap, supporting:

* Efficient insertion and deletion (`O(log n)` average time complexity)
* Undo & redo & Modification function based on advanced functions
* Generic support: works with `String` as well as other element sequences (`Char`, `Byte`, custom tokens, etc.)

Typical use cases include text editors, code editors, and rich-text processing.

---

## 特性 (Features)

* 📚 **基于 Treap 的平衡树结构**，在插入和删除时保持稳定性能

* ✏️ **PieceTable 算法**，最小化拷贝，提高效率

* ↩️ **Undo / Redo 支持**

* 🧩 **泛型化适配接口**，灵活扩展支持自定义数据

* 📚 **Treap-based balanced tree**, ensuring stable performance

* ✏️ **PieceTable algorithm**, minimizing copy overhead

* ↩️ **Undo / Redo support**

* 🧩 **Generic adapter interface**, extensible to custom data

---

## 使用示例 (Usage Example)

```kotlin
fun main() {
    val table = PieceTable("Hello World")

    table.insert(5, ", Kotlin")
    println(table.collect()) // 输出: Hello, Kotlin World

    table.delete(5, 8)
    println(table.collect()) // 输出: Hello World

    table.undo()
    println(table.collect()) // 输出: Hello, Kotlin World

    table.redo()
    println(table.collect()) // 输出: Hello World
}
```

---

## 测试 (Testing)

本项目内置 **Fuzz Test**（模糊测试），通过与 `StringBuilder` 的行为对比来验证 `PieceTable` 的正确性。
This project includes a **fuzz test** that validates correctness by comparing against `StringBuilder`.


## 许可证 (License)

本项目采用 [MIT License](./LICENSE)。
This project is licensed under the [MIT License](./LICENSE).

---
