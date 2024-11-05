object BuildConfig {

    internal val demeterVersion: String
        get() = System.getProperty("demeterVersion")

    internal val demeterGroup: String
        get() = System.getProperty("demeterGroup")

    internal const val compileSdk = 34
    internal const val minSdk = 26

    internal const val javaVersion = 17
}