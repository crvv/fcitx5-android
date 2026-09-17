/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android

import org.fcitx.fcitx5.android.input.keyboard.AlphabetKey
import org.fcitx.fcitx5.android.input.keyboard.BackspaceKey
import org.fcitx.fcitx5.android.input.keyboard.KeyDef
import org.fcitx.fcitx5.android.input.keyboard.NumberKeyboard
import org.fcitx.fcitx5.android.input.keyboard.TextKeyboard
import org.fcitx.fcitx5.android.input.popup.PopupPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardLayoutTest {

    @Test
    fun numberKeyboardColumnsStayAligned() {
        val rows = NumberKeyboard.Layout.take(3)
        val firstRowWidths = rows.first().map { it.appearance.percentWidth }
        rows.drop(1).forEach { row ->
            assertEquals(firstRowWidths, row.map { it.appearance.percentWidth })
        }
        val textBackspace = TextKeyboard.Layout.flatten().filterIsInstance<BackspaceKey>().single()
        assertEquals(0.2f, textBackspace.appearance.percentWidth, 0.0001f)
    }

    @Test
    fun textKeyboardHasOneVisibleKeyPerLetterAndDigit() {
        val keys = TextKeyboard.Layout.flatten().filterIsInstance<AlphabetKey>()
        val characters = ('A'..'Z').map { it.toString() } + ('0'..'9').map { it.toString() }
        assertEquals(characters.sorted(), keys.map { it.character }.sorted())
        keys.forEach { key ->
            val appearance = key.appearance as KeyDef.Appearance.AltText
            assertEquals(key.character, appearance.displayText)
            assertEquals(key.punctuation, appearance.altText)
        }
        assertEquals(
            0.9f,
            TextKeyboard.Layout[2].sumOf { it.appearance.percentWidth.toDouble() }.toFloat(),
            0.0001f
        )
    }

    @Test
    fun longPressDefaultsMatchSwipeSymbolsInBothCases() {
        TextKeyboard.Layout.flatten().filterIsInstance<AlphabetKey>().forEach { key ->
            val popup = key.popup!!.filterIsInstance<KeyDef.Popup.Keyboard.Preset>().single()
            listOf(popup.label.lowercase(), popup.label.uppercase()).distinct().forEach { label ->
                assertEquals("Long press on $label", key.punctuation, PopupPreset.getValue(label).first())
            }
        }
    }

    @Test
    fun longPressKeepsAccentsAndFractions() {
        mapOf(
            "a" to listOf("A", "ä", "á", "æ"),
            "A" to listOf("a", "Ä", "Á", "Æ"),
            "e" to listOf("E", "é", "è", "ê", "ë"),
            "E" to listOf("e", "É", "È", "Ê", "Ë"),
            "u" to listOf("U", "ü", "ǔ"),
            "U" to listOf("u", "Ü", "Ǔ"),
            "1" to listOf("¹", "½", "⅓"),
            "2" to listOf("²", "⅔")
        ).forEach { (label, alternatives) ->
            assertTrue("Alternatives on $label", PopupPreset.getValue(label).toList().containsAll(alternatives))
        }
    }
}
