package com.mac.expensee.core.ui.theme

import androidx.compose.ui.unit.dp

/**
 * The app's spacing scale -- every `Modifier.padding(...)`, `Arrangement.spacedBy(...)`, and
 * `PaddingValues(...)`/`contentPadding` across every screen references one of these instead of a
 * raw dp literal, so the rhythm between elements stays consistent and any future adjustment is a
 * one-line change here rather than a grep-and-replace across the app.
 *
 * Deliberately scoped to *spacing* only -- component sizing (icon/dot diameters, chart bar
 * heights, corner radii) isn't spacing and doesn't belong here; those stay as local `.size(...)`/
 * `.height(...)`/`RoundedCornerShape(...)` values on the composable that owns them, same as
 * before this was centralized.
 *
 * Roughly a 4dp grid. [tiny] and [tight] are the two exceptions that already existed in the app
 * before this was centralized -- kept at their original values (not rounded onto the grid) so
 * centralizing spacing doesn't change any screen's actual layout.
 */
object Spacing {
    /** Only used to inset the small `CircularProgressIndicator` inside the auth screens' submit button. */
    val tiny = 2.dp
    val extraSmall = 4.dp
    /** A couple of dashboard rows use this slightly tighter vertical rhythm instead of [small]. */
    val tight = 6.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val extraLarge = 24.dp
    val xxl = 32.dp
}
