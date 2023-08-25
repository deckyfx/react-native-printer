package deckyfx.reactnative.printer.escposprinter

class EscPosCharsetEncoding(val name: String, escPosCharsetId: Int) {
  val command: ByteArray

  /**
   * Create new instance of EscPosCharsetEncoding.
   *
   * @param charsetName Name of charset encoding (Ex: windows-1252)
   * @param escPosCharsetId Id of charset encoding for your printer (Ex: 16)
   */
  init {
    command = byteArrayOf(0x1B, 0x74, escPosCharsetId.toByte())
  }
}
