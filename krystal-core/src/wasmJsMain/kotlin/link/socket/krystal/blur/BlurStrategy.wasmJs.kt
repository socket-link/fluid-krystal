package link.socket.krystal.blur

// Wasm/JS blur strategies — currently fall back to software blur.
// Full browser canvas/WebGL integration is out of scope for this extraction.

actual fun createNativeBlurStrategy(): BlurStrategy = SoftwareBlurStrategy()
actual fun createAcceleratedBlurStrategy(): BlurStrategy = SoftwareBlurStrategy()
