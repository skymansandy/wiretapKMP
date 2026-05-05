// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://repo1.maven.org/maven2/dev/skymansandy/wiretap-urlsession-kmmbridge/1.0.0-RC11/wiretap-urlsession-kmmbridge-1.0.0-RC11.zip"
let remoteKotlinChecksum = "e1c88a2b7944cd66b4fa9bb5b68e1ea0615da22e77e3591c4b8069dcee27764e"
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