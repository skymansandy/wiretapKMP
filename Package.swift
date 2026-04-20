// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://repo1.maven.org/maven2/dev/skymansandy/wiretap-urlsession-kmmbridge/1.0.0-RC8/wiretap-urlsession-kmmbridge-1.0.0-RC8.zip"
let remoteKotlinChecksum = "07c9d0e7c86e7e2716f38885a8515e676619a09f2dab26244f22af8ab6b284d1"
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