// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://central.sonatype.com/repository/maven-snapshots//dev/skymansandy/wiretap-urlsession-kmmbridge/1.0.0-SNAPSHOT/wiretap-urlsession-kmmbridge-1.0.0-SNAPSHOT.zip"
let remoteKotlinChecksum = "37853d759a0b07a5016c9019620d498d0d6239ec6a99ee699d9a173c78a0c2b8"
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