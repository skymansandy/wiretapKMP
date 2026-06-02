// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://repo1.maven.org/maven2/dev/skymansandy/wiretap-urlsession-kmmbridge/1.0.0-RC13/wiretap-urlsession-kmmbridge-1.0.0-RC13.zip"
let remoteKotlinChecksum = "f852a0e85d6e000603fb2dbae8fd4c815687a7b15a7cea26d8fa931fd5ef748c"
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