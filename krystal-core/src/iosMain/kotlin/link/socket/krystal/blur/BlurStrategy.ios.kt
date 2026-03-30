package link.socket.krystal.blur

// iOS passes through to native Liquid Glass — these stubs exist only to satisfy
// the expect/actual contract. They are never called at runtime on Apple platforms.

actual fun createNativeBlurStrategy(): BlurStrategy = SoftwareBlurStrategy()
actual fun createAcceleratedBlurStrategy(): BlurStrategy = SoftwareBlurStrategy()
