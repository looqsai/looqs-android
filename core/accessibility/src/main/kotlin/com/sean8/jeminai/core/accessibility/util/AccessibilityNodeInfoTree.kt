package com.sean8.jeminai.core.accessibility.util

import android.view.accessibility.AccessibilityNodeInfo

/***
 * Runs on all the tree from this node, and populates the descendants.
 * The fetch depends eventually on the cache of com.jehmihny.access.accessibility
 * @return true if was successful, false if failed and fetched only a partial tree
 */
fun TreeNode<AccessibilityNodeInfo>.populateChildren(
    deep: Boolean = true,
    includeInvisible: Boolean = false,
): Boolean {
    for (childIndex in 0 until value.childCount) {
        val expectedChild = value.getChild(childIndex)

        // Value can become null from com.jehmihny.access.accessibility process if it is not cached anymore
        if (expectedChild == null || !expectedChild.refresh()) return false

        val child = AccessibilityNodeInfo.obtain(expectedChild)
        if (includeInvisible || child.isVisibleToUser) {
            child.index = childIndex

            val childNode = TreeNode(child).apply { if (deep) populateChildren() }
            this.addChild(childNode)
        }
    }

    return true
}