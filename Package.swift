// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://repo1.maven.org/maven2/dev/skymansandy/wiretap-urlsession-kmmbridge/1.0.1/wiretap-urlsession-kmmbridge-1.0.1.zip"
let remoteKotlinChecksum = "d16333705d8a1b448079ada578f3fcaf4605b6a664bc83c727d1f082c6e7bcc2"
let packageName = "WiretapURLSession"
// END KMMBRIDGE BLOCK

let package = Package(
    name: packageName,
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(
            name: packageName,
            targets: [packageName]
        ),
    ],
    targets: [
        .binaryTarget(
            name: packageName,
            url: remoteKotlinUrl,
            checksum: remoteKotlinChecksum
        )
        ,
    ]
)