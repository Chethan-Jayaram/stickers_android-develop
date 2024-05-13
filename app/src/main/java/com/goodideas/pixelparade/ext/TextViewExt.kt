package com.goodideas.pixelparade.ext

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.text.util.Linkify
import android.view.View
import android.widget.TextView
import com.goodideas.pixelparade.html.ListTagHandler

fun TextView.setHtml(html: String, onClick: (url: String) -> Unit) {
    val sequence = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(
            html,
            Html.FROM_HTML_MODE_COMPACT,
            null,
            ListTagHandler()
        )
    } else {
        Html.fromHtml(
            html,
            null,
            ListTagHandler()
        )
    }
    val strBuilder = SpannableStringBuilder(sequence)
    Linkify.addLinks(strBuilder, Linkify.ALL)
    val urls = strBuilder.getSpans(0, sequence.length, URLSpan::class.java)
    for (span in urls) {
        makeLinkClickable(context, strBuilder, span, onClick)
    }
    text = strBuilder
    movementMethod = LinkMovementMethod.getInstance()
}

private fun makeLinkClickable(
    context: Context,
    strBuilder: SpannableStringBuilder,
    span: URLSpan,
    onClick: (url: String) -> Unit
) {
    val start = strBuilder.getSpanStart(span)
    val end = strBuilder.getSpanEnd(span)
    val flags = strBuilder.getSpanFlags(span)
    val clickable = object : ClickableSpan() {
        override fun onClick(view: View) {
            context.browse(span.url, false)
            onClick.invoke(span.url)
        }
    }
    strBuilder.setSpan(clickable, start, end, flags)
    strBuilder.removeSpan(span)
}
