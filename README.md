# Manifest Sync
[Download the latest APK](https://github.com/henkielp/manifest-sync/releases/latest)

A 100% offline Android app that verifies physical deliveries against printed manifests. Photograph a manifest with your phone's camera, and the app reads the UPC numbers using on-device OCR. Then scan barcodes on boxes to instantly locate each item on your list.

## How It Works

**Phase 1: Capture the manifest.** Photograph the printed manifest page by page. The app uses OCR to extract UPC numbers and item descriptions. A resizable, movable crop guide lets you isolate the relevant columns. A page preview screen lets you verify the results before accepting them.

**Phase 2: Scan barcodes.** Point the camera at a box's barcode and tap Scan. The app matches the barcode against the manifest and tells you the item name and position number (for example, "Item #14: Globe Theatre Artisan Loaf"). Both standard UPC-A barcodes and GS1-128 shipping labels are supported.

**Phase 3: Verify.** Use the position number to find the item on your paper manifest and check it off. Or let the app track scan counts and generate a discrepancy report showing missing items, multiples, and extras.

## Key Features

- **Fully offline.** All machine learning models are bundled in the APK. No internet connection required, ever. Works in walk-in coolers, warehouses, and backrooms with no signal.
- **No data stored.** All photos are temporary. They exist in memory only long enough to read the text or barcode, then they are discarded. Nothing is saved to the phone or sent anywhere.
- **Multi-page capture.** Scan manifests of any length across multiple pages. Overlapping pages are handled automatically with duplicate UPC detection.
- **GS1-128 support.** The app extracts the GTIN from GS1-128 shipping barcodes, so both the standard barcode and the shipping label on a box will match.
- **Alignment warnings.** The app detects when OCR may have missed a UPC and warns you before you start scanning.
- **Built-in help.** Privacy information, a quick start guide, and the full manual are accessible from the home screen.

## Demo: The Bard's Bakery

The repo includes a synthetic test dataset so you can try the app without real inventory.

**The Manifest:** A 20-item delivery manifest for "The Bard's Bakery" featuring items like *Prince Hamlet's Poisoned Plum Tart*, *Yorick's Skull Scone*, and *Lady Macbeth's Spotted Shortbread*. The UPC numbers are derived from physics constants (Euler's number, Pi, the golden ratio, Planck's constant, etc.). Print this page and photograph it with the app.

**The Barcodes:** An HTML file that displays scannable barcodes for all 20 items in randomized order. [Open the barcode page](https://henkielp.github.io/manifest-sync/demo/barcodes.html) in a browser, or print it for the most realistic testing experience (scanning from paper matches real box conditions).

Test files are in the `demo/` folder.

## Two Usage Modes

### Barcode Lookup Mode (Recommended)

The paper manifest is your official record. The app is a fast lookup tool. Scan a barcode, get the position number, find it on paper, mark it off. Simple, reliable, and your progress survives even if the app crashes.

### Digital Tracking Mode

The app tracks everything. Scan and confirm each box. When finished, tap View Report to see three tabs: Missing (items not scanned), Multiples (items scanned more than once), and Extras (barcodes not on the manifest).

## Building the App

The app is built using GitHub Actions and sideloaded as a debug APK. No Android Studio or local development environment required.

**Requirements:**

- A GitHub account
- An Android phone (API level 24 or higher)

**Steps:**

1. Fork or clone this repository
2. Push to the `main` branch (or trigger the workflow manually)
3. Go to the Actions tab and wait for the build to complete
4. Download the `app-debug.apk` artifact
5. Transfer the APK to your phone and install it (you may need to enable "Install from unknown sources" in your phone's settings)

## Technical Details

- **Language:** Kotlin
- **UI:** Android Views with ViewBinding
- **Camera:** CameraX (Preview, ImageCapture, ImageAnalysis)
- **OCR:** Google ML Kit Text Recognition v3 (bundled, 16.0.1)
- **Barcode scanning:** Google ML Kit Barcode Scanning (bundled, 17.3.0)
- **Barcode formats:** UPC-A, UPC-E, EAN-13, EAN-8, CODE-128 (GS1-128)
- **Architecture:** Single Activity, MVVM with StateFlow
- **Build:** GitHub Actions CI/CD, Gradle 8.4, JDK 17
- **License for ML Kit:** Google APIs Terms of Service (free, no fees, no attribution required)

## How It Was Made

This app was designed and directed by Paul Henkiel. All code was generated using Claude (Anthropic's AI assistant). The app was assembled entirely through the GitHub web interface with no local IDE.

## Known Limitations

- **Item descriptions may merge incorrectly.** OCR reads printed tables column by column, not row by row. Descriptions from adjacent items sometimes get combined. This is cosmetic: the UPC numbers are reliable and drive all barcode matching.
- **Products with nearly identical UPCs.** If two items differ only in the last digit of their UPC, and OCR drops that digit, the app may match the wrong one. Always verify the position number against your paper manifest.
