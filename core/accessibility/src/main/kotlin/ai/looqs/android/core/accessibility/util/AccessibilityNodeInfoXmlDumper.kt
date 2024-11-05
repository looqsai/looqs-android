package ai.looqs.android.core.accessibility.util

import android.util.Log
import android.util.Xml
import android.view.Display
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import ai.looqs.android.core.util.getShortBoundsInScreen
import ai.looqs.android.core.util.screenHeight
import ai.looqs.android.core.util.screenWidth
import org.xmlpull.v1.XmlSerializer
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

object AccessibilityNodeInfoXmlDumper {
    private const val TAG = "AccessibilityNodeInfoXmlDumper"
    private val baos = ByteArrayOutputStream(262144)
    private const val DEFAULT_VIEW_CLASS_NAME = "View"
    private const val NON_XML_CHAR_REPLACEMENT = "?"

    @Synchronized
    @Throws(IOException::class)
    fun getXml(treeRoot: TreeNode<AccessibilityNodeInfo>, display: Display): String {
        dumpWindowHierarchy(treeRoot.value, display, baos, treeRoot, null)
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
            Log.e(TAG, "Trying to XML dumpNodeRec a null node, aborting node")
            return
        }

        val nodeName = toXmlNodeName(node.getClassNameSafe())
        serializer.startTag("", nodeName)

        // Only include attributes if they have meaningful values
        val text = safeCharSeqToString(node.text)
        if (text.isNotEmpty()) {
            serializer.attribute("", "text", text)
        }

        val id = safeCharSeqToString(node.viewIdResourceName)
        if (id.isNotEmpty()) {
            serializer.attribute("", "id", id)
        }

        val contentDesc = safeCharSeqToString(node.contentDescription)
        if (contentDesc.isNotEmpty()) {
            serializer.attribute("", "content-desc", contentDesc)
        }

        if (node.isClickable) {
            serializer.attribute("", "clickable", "true")
        }

        if (!node.isEnabled) { // Include only if false, since true is default
            serializer.attribute("", "enabled", "false")
        }

        // Always include bounds
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
        serializer.endTag("", nodeName)
    }

    private fun safeCharSeqToString(source: CharSequence?): String {
        return source?.let {
            XML10_PATTERN.matcher(it).replaceAll(NON_XML_CHAR_REPLACEMENT)
        } ?: ""
    }

    private fun toXmlNodeName(className: String?): String {
        if (className.isNullOrBlank()) return DEFAULT_VIEW_CLASS_NAME

        // Remove package prefixes
        var fixedName = className.substringAfterLast('.')

        // Sanitize the name
        fixedName = toNodeName(fixedName)
        if (fixedName.isBlank()) fixedName = DEFAULT_VIEW_CLASS_NAME
        return fixedName
    }

    private fun toNodeName(str: String): String {
        val nodeName = XML10_START_TAG_PATTERN.matcher(str).replaceAll("")
        return XML10_TAG_PATTERN.matcher(nodeName).replaceAll("_")
    }

    /** Returns the class name if it exists, or "View" as a fallback **/
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
