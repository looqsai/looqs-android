package com.sean8.jeminai.core.accessibility.record

import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.sean8.jeminai.core.accessibility.util.TreeNode
import com.sean8.jeminai.core.accessibility.util.getShortBoundsInScreen
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONArray
import org.json.JSONObject

fun TreeNode<AccessibilityNodeInfo>.toJson(): String {
    try {
        val root = JSONObject(value.toJson())

        val childArray = JSONArray()
        root.put(getChildKeyPropertyName(), childArray)

        // Go over children and add to Json
        for (i in 0 until children.size) {
            childArray.put(JSONObject(children[i].toJson()))
        }

        return root.toString()
    } catch (e: Exception) {
        Log.e("AccessibilityNodeInfoJson", "Error in parsing json", e)
        return "{}"
    }
}

fun AccessibilityNodeInfo.toJson(): String {
    return propertiesAdapter.toJson(nodeToData(this))
}

fun AccessibilityNodeInfo.nodeDigestPrintable(): String {
    val displayName =
        this.viewIdResourceName?.split(":")?.last()
            ?: this.contentDescription
            ?: this.getClassNameSafe().split(".").last()


    return " window ${
        windowId.toString().padEnd(4)
    }| ${getShortBoundsInScreen().padEnd(22)}| ${displayName.padEnd(35)}"
}

data class Attributes(
    val bounds: String?,
    val checkable: Boolean?,
    val checked: Boolean?,
    @Json(name = "class")
    val classX: String?,
    val clickable: Boolean?,
    @Json(name = "content-desc")
    val contentDesc: String?,
    val enabled: Boolean?,
    val focusable: Boolean?,
    val focused: Boolean?,
    val index: Int?,
    @Json(name = "long-clickable")
    val longClickable: Boolean?,
    @Json(name = "package")
    val packageX: String?,
    val password: Boolean?,
    @Json(name = "resource-id")
    val resourceId: String?,
    val scrollable: Boolean?,
    val selected: Boolean?,
    val text: String?,
    @Json(name = "visible-to-user")
    val visibleToUser: Boolean?,
    @Json(name = "target-node")
    var isTargetNode: Boolean = false,
    val drawingOrder: Int?,
)

fun nodeToData(node: AccessibilityNodeInfo): Attributes {
    return Attributes(
        safeCharSeqToString(node.getShortBoundsInScreen()),
        node.isCheckable,
        node.isChecked,
        safeCharSeqToString(node.className),
        node.isClickable,
        safeCharSeqToString(node.contentDescription),
        node.isEnabled,
        node.isFocusable,
        node.isFocused,
        node.index,
        node.isLongClickable,
        safeCharSeqToString(node.packageName),
        node.isPassword,
        safeCharSeqToString(node.viewIdResourceName),
        node.isScrollable,
        node.isSelected,
        safeCharSeqToString(node.text),
        node.isVisibleToUser,
        node.isTargetNode,
        node.drawingOrder,
    )
}

fun Attributes.toNode(): AccessibilityNodeInfo =
    AccessibilityNodeInfo.obtain().apply {
        val attr = this@toNode

        this.isCheckable = attr.checkable ?: false
        this.isChecked = attr.checked ?: false
        this.className = attr.classX
        this.isClickable = attr.clickable ?: false
        this.contentDescription = attr.contentDesc
        this.isEnabled = attr.enabled ?: false
        this.isFocusable = attr.focusable ?: false
        this.isFocused = attr.focused ?: false
        this.index = attr.index
        this.isLongClickable = attr.longClickable ?: false
        this.packageName = attr.packageX
        this.isPassword = attr.password ?: false
        this.viewIdResourceName = attr.resourceId
        this.isScrollable = attr.scrollable ?: false
        this.isSelected = attr.selected ?: false
        this.text = attr.text
        this.isVisibleToUser = attr.visibleToUser ?: false
        this.isTargetNode = attr.isTargetNode
        this.drawingOrder = attr.drawingOrder ?: -999

        val rect = unFlattenRectFromString(attr.bounds!!)
        this.setBoundsInScreen(rect)
    }

private fun safeCharSeqToString(cs: CharSequence?) = cs?.toString() ?: ""

val propertiesAdapter: JsonAdapter<Attributes> = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
    .adapter(Attributes::class.java)

fun getChildKeyPropertyName(): String {
    return "children"
}

fun unFlattenRectFromString(str: String): Rect? {
    // [0,0][1000,1000]
    val stripped = str
        .replace("[", "")
        .replace(",", " ")
        .replace("]", " ")

    return stripped.split(" ").run {
        Rect(
            Integer.parseInt(this[0]),
            Integer.parseInt(this[1]),
            Integer.parseInt(this[2]),
            Integer.parseInt(this[3]),
        )
    }
}