package com.sean8.jeminai.core.util

open class TreeNode<T : Any>(var value: T) {
    var parent: TreeNode<T>? = null
    var children: MutableList<TreeNode<T>> = mutableListOf()

    fun addChild(node: TreeNode<T>) {
        children.add(node)
        node.parent = this
    }

    override fun toString(): String {
        var s = "$value"
        if (children.isNotEmpty()) {
            s += " {" + children.map { it.toString() } + " }"
        }
        return s
    }

    /***
     * Returns the nodes in the fetchTree including itself
     */
    fun countNodes(): Int {
        var childCount = 1
        children.forEach { childCount += it.countNodes() }
        return childCount
    }

    override fun equals(other: Any?): Boolean {
        return (other === this)
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + (parent?.hashCode() ?: 0)
        result = 31 * result + children.hashCode()
        return result
    }
}