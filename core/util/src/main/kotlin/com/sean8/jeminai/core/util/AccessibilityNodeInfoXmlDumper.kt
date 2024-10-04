package com.sean8.jeminai.core.util

import android.util.Log
import android.util.Xml
import android.view.Display
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import org.xmlpull.v1.XmlSerializer
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

/**
 * This way we produce the same XML as Appium.
 * It was originally copied from Android source code, then changed according to Appium implementation mostly
 * to create a safe encoding and such:
 * https://github.com/appium/appium-uiautomator2-server/blob/master/app/src/main/java/io/appium/uiautomator2/core/AccessibilityNodeInfoDumper.java
 */
object AccessibilityNodeInfoXmlDumper {
    private const val TAG = "AccessibilityNodeInfoXmlDumper"
    private val baos = ByteArrayOutputStream(262144)
    private const val DEFAULT_VIEW_CLASS_NAME = "android.view.View"
    private const val NON_XML_CHAR_REPLACEMENT = "?"

    private val NAF_EXCLUDED_CLASSES = arrayOf(
        android.widget.GridView::class.java.name,
        android.widget.GridLayout::class.java.name,
        android.widget.ListView::class.java.name,
        android.widget.TableLayout::class.java.name,
    )

    @Synchronized
    @Throws(IOException::class)
    fun getXml(treeRoot: TreeNode<AccessibilityNodeInfo>, display: Display): String {
        dumpWindowHierarchy(treeRoot.value, display, baos, treeRoot, null)
        val result = baos.toString(StandardCharsets.UTF_8.name())
        baos.reset()
        return result
    }

    @Synchronized
    @Throws(IOException::class)
    fun getXml(
        activeWindowRoot: AccessibilityNodeInfo,
        display: Display,
        target: AccessibilityNodeInfo?,
    ): String {
        dumpWindowHierarchy(activeWindowRoot, display, baos, null, target)
        val result = baos.toString(StandardCharsets.UTF_8.name())
        baos.reset()
        return result
    }

    @Throws(IOException::class)
    private fun dumpWindowHierarchy(
        activeWindowRoot: AccessibilityNodeInfo,
        display: Display,
        out: OutputStream,
        treeRoot: TreeNode<AccessibilityNodeInfo>?,
        target: AccessibilityNodeInfo?,
    ) {
        val serializer: XmlSerializer = Xml.newSerializer()
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
        serializer.setOutput(out, StandardCharsets.UTF_8.name())

        serializer.startDocument(StandardCharsets.UTF_8.name(), true)
        serializer.startTag("", "hierarchy")
        serializer.attribute("", "rotation", "0")

        serializer.attribute("", "width", display.screenWidth.toString())
        serializer.attribute("", "height", display.screenHeight.toString())

        dumpNodeRec(activeWindowRoot, serializer, 0, treeRoot, target)

        serializer.endTag("", "hierarchy")
        serializer.endDocument()
    }

    @Throws(IOException::class)
    private fun dumpNodeRec(
        node: AccessibilityNodeInfo?,
        serializer: XmlSerializer,
        index: Int,
        treeRoot: TreeNode<AccessibilityNodeInfo>?,
        target: AccessibilityNodeInfo?,
    ) {
        if (node == null) {
            Log.e(" ", "Trying to XML dumpNodeRec a null node, aborting node")
            return
        }

        val startingTag = toXmlNodeName(node.getClassNameSafe())
        serializer.startTag("", startingTag)

        if (!nafExcludedClass(node) && !nafCheck(node, treeRoot)) {
            serializer.attribute("", "NAF", "true")
        }

        serializer.attribute("", "index", index.toString())
        serializer.attribute("", "text", safeCharSeqToString(node.text))
        serializer.attribute("", "resource-id", safeCharSeqToString(node.viewIdResourceName))
        serializer.attribute("", "class", node.getClassNameSafe())
        serializer.attribute("", "package", safeCharSeqToString(node.packageName))
        serializer.attribute("", "content-desc", safeCharSeqToString(node.contentDescription))
        serializer.attribute("", "checkable", node.isCheckable.toString())
        serializer.attribute("", "checked", node.isChecked.toString())
        serializer.attribute("", "clickable", node.isClickable.toString())
        serializer.attribute("", "enabled", node.isEnabled.toString())
        serializer.attribute("", "focusable", node.isFocusable.toString())
        serializer.attribute("", "focused", node.isFocused.toString())
        serializer.attribute("", "scrollable", node.isScrollable.toString())
        serializer.attribute("", "long-clickable", node.isLongClickable.toString())
        serializer.attribute("", "password", node.isPassword.toString())
        serializer.attribute("", "selected", node.isSelected.toString())
        serializer.attribute("", "bounds", node.getShortBoundsInScreen())

        val count = treeRoot?.children?.size ?: node.childCount
        for (i in 0 until count) {
            val treeRootNext = treeRoot?.children?.get(i)
            val child = treeRootNext?.value ?: node.getChild(i)
            if (child != null) {
                if (child.isVisibleToUser) {
                    dumpNodeRec(child, serializer, i, treeRootNext, target)
                } else {
                    Log.v(TAG, "Skipping invisible child $child")
                }
            } else {
                Log.v(TAG, "Null child $node")
            }
        }
        serializer.endTag("", startingTag)
    }

    private fun nafExcludedClass(node: AccessibilityNodeInfo): Boolean {
        val className = safeCharSeqToString(node.className)
        return NAF_EXCLUDED_CLASSES.any { className.endsWith(it) }
    }

    private fun nafCheck(
        node: AccessibilityNodeInfo,
        treeRoot: TreeNode<AccessibilityNodeInfo>?,
    ): Boolean {
        val isNaf = node.isClickable && node.isEnabled &&
            safeCharSeqToString(node.contentDescription).isEmpty() &&
            safeCharSeqToString(node.text).isEmpty()

        if (!isNaf) return true

        return childNafCheck(node, treeRoot)
    }

    private fun childNafCheck(
        node: AccessibilityNodeInfo,
        treeRoot: TreeNode<AccessibilityNodeInfo>?,
    ): Boolean {
        val childCount = treeRoot?.children?.size ?: node.childCount
        for (x in 0 until childCount) {
            val treeRootNext = treeRoot?.children?.get(x)
            val childNode = treeRootNext?.value ?: node.getChild(x)

            if (!safeCharSeqToString(childNode?.contentDescription).isEmpty() ||
                !safeCharSeqToString(childNode?.text).isEmpty()
            ) return true

            if (childNafCheck(childNode, treeRootNext)) return true
        }
        return false
    }

    private fun safeCharSeqToString(source: CharSequence?): String {
        return source?.let {
            XML10_PATTERN.matcher(it).replaceAll(NON_XML_CHAR_REPLACEMENT)
        } ?: ""
    }

    private fun toXmlNodeName(className: String?): String {
        if (className.isNullOrBlank()) return DEFAULT_VIEW_CLASS_NAME

        var fixedName = className.replace("[$@#&]".toRegex(), ".")
            .replace("\\.+".toRegex(), ".")
            .replace("(^\\.|\\.$)".toRegex(), "")

        fixedName = toNodeName(fixedName)
        if (fixedName.isBlank()) fixedName = DEFAULT_VIEW_CLASS_NAME
        if (fixedName != className) {
            Log.v(TAG, "Rewrote class name '$className' to XML node name '$fixedName'")
        }
        return fixedName
    }

    private fun toNodeName(str: String): String {
        val nodeName = XML10_START_TAG_PATTERN.matcher(str).replaceAll("")
        return XML10_TAG_PATTERN.matcher(nodeName).replaceAll("_")
    }

    /** Returns the class name if exist, or View as a fallback class **/
    private fun AccessibilityNodeInfo.getClassNameSafe(): String =
        if (className.isNullOrEmpty()) View::class.java.name else className.toString()


    private val XML10_PATTERN = Pattern.compile(
        "[^" + "\u0009\r\n" +
            "\u0020-\uD7FF" + "\uE000-\uFFFD" + "\ud800\udc00-\udbff\udfff" + "]",
    )

    private val XML10_START_TAG_PATTERN = Pattern.compile(
        "^[^_" +
            "A-Z" + "a-z" + "\u00C0-\u00D6" + "\u00F8-\u02FF" +
            "\u200C-\u200D" + "\u2070-\u218F" + "\u2C00-\u2FEF" +
            "\u3001-\uD7FF" + "\uF900-\uFDCF" + "\uFDF0-\uFFFD" +
            "\ud800\udc00-\udbff\udfff" + "]+",
    )

    private val XML10_TAG_PATTERN = Pattern.compile(
        "[^\\-._" +
            "0-9" + "\u00B7" + "\u0300-\u036F" + "\u203F-\u2040" +
            "A-Z" + "a-z" + "\u00C0-\u00D6" + "\u00F8-\u02FF" +
            "\u200C-\u200D" + "\u2070-\u218F" + "\u2C00-\u2FEF" +
            "\u3001-\uD7FF" + "\uF900-\uFDCF" + "\uFDF0-\uFFFD" +
            "\ud800\udc00-\udbff\udfff" + "]",
    )
}

