package mono.html.toolbar.view.shapetool

internal fun classes(vararg cls: Class?): String =
    cls.filterNotNull().joinToString(" ") { it.value }

enum class Class(val value: String) {
    ADD_BOTTOM_SPACE("add-bottom-space"),
    ADD_RIGHT_SPACE("add-right-space"),
    CENTER_EVEN_SPACE("center-even-space"),
    CENTER_VERTICAL("center-vertical"),
    COLUMN("tcolumn"),
    DISABLED("disabled"),
    GRID("grid"),
    HALF("half"),
    HIDE("hide"),
    ICON_BUTTON("icon-button"),
    INLINE_TITLE("inline-title"),
    INPUT_CHECK_BOX("tool-input-checkbox"),
    INPUT_TEXT("tool-input-text"),
    MEDIUM("medium"),
    MONOFONT("monofont"),
    ROW("trow"),
    QUARTER("quarter"),
    SELECTED("selected"),
    SECTION("tsection"),
    SECTION_TITLE("tsection-title"),
    SHORT("short"),
    SMALL("small"),
    SMALL_SPACE("small-space"),
    TOOL("tool"),
    TOOL_TITLE("tool-title"),
    VISIBLE("visible");

    infix fun x(isAccepted: Boolean): Class? = this.takeIf { isAccepted }
}
