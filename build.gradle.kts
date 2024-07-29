plugins {
    id("com.android.application") version "8.5.1" apply false
    id("com.android.library") version "8.5.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
}

task<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
