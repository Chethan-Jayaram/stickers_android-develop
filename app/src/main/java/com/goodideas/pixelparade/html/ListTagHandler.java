package com.goodideas.pixelparade.html;

import android.content.res.Resources;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;
import android.util.TypedValue;

import org.xml.sax.XMLReader;

import java.util.Stack;

public class ListTagHandler implements Html.TagHandler {
    private static final String OL_TAG = "ol";
    private static final String UL_TAG = "ul";
    private static final String LI_TAG = "li";

    private static final int INDENT_DP = 8;
    /**
     * List indentation in pixels. Nested lists use multiple of this.
     */
    private static final int INDENT_PX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, INDENT_DP, Resources.getSystem().getDisplayMetrics());
    private static final int LIST_ITEM_INDENT_PX = INDENT_PX * 2;
    private static final BulletSpan BULLET_SPAN = new BulletSpan(INDENT_PX);

    /**
     * Keeps track of lists (ol, ul). On bottom of Stack is the outermost list
     * and on top of Stack is the most nested list
     */
    private final Stack<ListTag> lists = new Stack<ListTag>();

    /**
     * @see android.text.Html.TagHandler#handleTag(boolean, String, Editable, XMLReader)
     */
    @Override
    public void handleTag(final boolean opening, final String tag, final Editable output, final XMLReader xmlReader) {
        if (UL_TAG.equalsIgnoreCase(tag)) {
            if (opening) {   // handle <ul>
                lists.push(new Ul());
            } else {   // handle </ul>
                lists.pop();
            }
        } else if (OL_TAG.equalsIgnoreCase(tag)) {
            if (opening) {   // handle <ol>
                lists.push(new Ol()); // use default start index of 1
            } else {   // handle </ol>
                lists.pop();
            }
        } else if (LI_TAG.equalsIgnoreCase(tag)) {
            if (opening) {   // handle <li>
                lists.peek().openItem(output);
            } else {   // handle </li>
                lists.peek().closeItem(output, lists.size());
            }
        }
    }

    /**
     * Abstract super class for {@link Ul} and {@link Ol}.
     */
    private abstract static class ListTag {
        /**
         * Opens a new list item.
         *
         * @param text
         */
        public void openItem(final Editable text) {
            if (text.length() > 0 && text.charAt(text.length() - 1) != '\n') {
                text.append("\n");
            }
            final int len = text.length();
            text.setSpan(this, len, len, Spanned.SPAN_MARK_MARK);
        }

        /**
         * Closes a list item.
         *
         * @param text
         * @param indentation
         */
        public final void closeItem(final Editable text, final int indentation) {
            if (text.length() > 0 && text.charAt(text.length() - 1) != '\n') {
                text.append("\n");
            }
            final Object[] replaces = getReplaces(text, indentation);
            final int len = text.length();
            final ListTag listTag = getLast(text);
            final int where = text.getSpanStart(listTag);
            text.removeSpan(listTag);
            if (where != len) {
                for (Object replace : replaces) {
                    text.setSpan(replace, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        protected abstract Object[] getReplaces(final Editable text, final int indentation);

        /**
         * Note: This knows that the last returned object from getSpans() will be the most recently added.
         *
         * @see Html
         */
        private ListTag getLast(final Spanned text) {
            final ListTag[] listTags = text.getSpans(0, text.length(), ListTag.class);
            if (listTags.length == 0) {
                return null;
            }
            return listTags[listTags.length - 1];
        }
    }

    /**
     * Class representing the unordered list ({@code <ul>}) HTML tag.
     */
    private static class Ul extends ListTag {

        @Override
        protected Object[] getReplaces(final Editable text, final int indentation) {
            // Nested BulletSpans increases distance between BULLET_SPAN and text, so we must prevent it.
            int bulletMargin = INDENT_PX;
            if (indentation > 1) {
                bulletMargin = INDENT_PX - BULLET_SPAN.getLeadingMargin(true);
                if (indentation > 2) {
                    // This get's more complicated when we add a LeadingMarginSpan into the same line:
                    // we have also counter it's effect to BulletSpan
                    bulletMargin -= (indentation - 2) * LIST_ITEM_INDENT_PX;
                }
            }
            return new Object[]{
                    new LeadingMarginSpan.Standard(LIST_ITEM_INDENT_PX * (indentation - 1)),
                    new BulletSpan(bulletMargin)
            };
        }
    }

    /**
     * Class representing the ordered list ({@code <ol>}) HTML tag.
     */
    private static class Ol extends ListTag {
        private int nextIdx;

        /**
         * Creates a new {@code <ul>} with start index of 1.
         */
        public Ol() {
            this(1); // default start index
        }

        /**
         * Creates a new {@code <ul>} with given start index.
         *
         * @param startIdx
         */
        public Ol(final int startIdx) {
            this.nextIdx = startIdx;
        }

        @Override
        public void openItem(final Editable text) {
            super.openItem(text);
            text.append(Integer.toString(nextIdx++)).append(". ");
        }

        @Override
        protected Object[] getReplaces(final Editable text, final int indentation) {
            int numberMargin = LIST_ITEM_INDENT_PX * (indentation - 1);
            if (indentation > 2) {
                // Same as in ordered lists: counter the effect of nested Spans
                numberMargin -= (indentation - 2) * LIST_ITEM_INDENT_PX;
            }
            return new Object[]{new LeadingMarginSpan.Standard(numberMargin)};
        }
    }

}
