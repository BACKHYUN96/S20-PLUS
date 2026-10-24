package app.purebit.theme

/** Ordered, bounded building blocks. No executable expressions or playback settings. */
enum class PlayerBlock { ROUTE, ARTWORK, METADATA, SEEK, TIME, LOOP, TRANSPORT, VOLUME, MESSAGE, PICKER }

data class PlayerLayoutSpec(
    val blocks: List<PlayerBlock>,
    val artworkHeight: Int = 0,
    val horizontalPadding: Int = 20,
    val titleSize: Int = 22,
    val playSize: Int = 78
) {
    fun validated(): PlayerLayoutSpec {
        require(blocks.size == PlayerBlock.entries.size && blocks.toSet() == PlayerBlock.entries.toSet()) { "각 재생 화면 블록을 한 번씩 지정하세요" }
        require(artworkHeight == 0 || artworkHeight in 160..480) { "앨범 영역 높이는 160~480 dp입니다" }
        require(horizontalPadding in 8..40 && titleSize in 16..32 && playSize in 64..112) { "레이아웃 크기 범위 오류" }
        return this
    }
    companion object {
        fun defaults(renderer: ThemeRenderer) = if(renderer != ThemeRenderer.ORIGINAL)
            PlayerLayoutSpec(listOf(PlayerBlock.ROUTE,PlayerBlock.ARTWORK,PlayerBlock.METADATA,PlayerBlock.SEEK,PlayerBlock.TIME,PlayerBlock.TRANSPORT,PlayerBlock.VOLUME,PlayerBlock.LOOP,PlayerBlock.MESSAGE,PlayerBlock.PICKER),horizontalPadding=if(renderer==ThemeRenderer.ABYSS)16 else 24,titleSize=23,playSize=if(renderer==ThemeRenderer.ABYSS)88 else 90)
        else PlayerLayoutSpec(PlayerBlock.entries.toList())
    }
}
