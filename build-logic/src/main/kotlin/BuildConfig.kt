object BuildConfig {

    val demeterVersion: String
        get() = System.getProperty("demeterVersion")

    val demeterGroup: String
        get() = System.getProperty("demeterGroup")

    internal const val compileSdk = 36
    internal const val minSdk = 26

    internal const val javaVersion = 21
}
