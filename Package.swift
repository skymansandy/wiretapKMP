// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://repo1.maven.org/maven2/dev/skymansandy/wiretap-urlsession-kmmbridge/1.0.0-RC10/wiretap-urlsession-kmmbridge-1.0.0-RC10.zip"
let remoteKotlinChecksum = "1e5916d6c724f5c4fc9b17c921545009c325f87ad41bd847ebd496273452195e"
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