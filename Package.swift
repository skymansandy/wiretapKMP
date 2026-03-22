// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "file:/Users/runner/work/wiretapKMP/wiretapKMP/wiretap-urlsession/build/publishing/mavenCentral//WiretapKMP/wiretap-urlsession-kmmbridge/unspecified/wiretap-urlsession-kmmbridge-unspecified.zip"
let remoteKotlinChecksum = "4e4935ba4e34f67a1fe2758803bcae91abe882aea02c8f779aa1ed6615a048cd"
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