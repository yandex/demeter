object BuildConfig {

    val demeterVersion: String
        get() = System.getProperty("demeterVersion")

    val demeterGroup: String
        get() = System.getProperty("demeterGroup")

    internal const val compileSdkMajor = 36
    internal const val compileSdkMinor = 1
    internal const val minSdk = 22

    internal const val javaVersion = 17
}
