package mono.shape.extra.manager.model

/**
 * A class for defining an anchor end-char.
 *
 * @param id is the key for retrieving predefined [AnchorChar] when serialization.
 *
 * @param displayName is the text visible on the UI tool for selection.
 */
class AnchorChar internal constructor(
    val id: String,
    val displayName: String,
    val left: Char,
    val right: Char,
    val top: Char,
    val bottom: Char
) {

    internal constructor(id: String, displayName: String, all: Char) :
        this(id, displayName, all, all, all, all)

    internal constructor(id: String, displayName: String, horizontal: Char, vertical: Char) :
        this(id, displayName, horizontal, horizontal, vertical, vertical)
}
