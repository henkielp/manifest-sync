# Manifest Sync - User Manual

**Version:** 0.7.0
**Last Updated:** April 19, 2026

## What This App Does

Manifest Sync helps you verify physical deliveries against printed manifests. Scan the manifest with OCR, then match barcodes on boxes to quickly locate each item on your list. You photograph the printed delivery manifest, and the app reads the UPC numbers using your phone's camera. Then you scan barcodes on the physical boxes, and the app tells you which item you're holding and where it appears on the manifest.

The app works completely offline. No Wi-Fi, no cellular data, no internet connection needed. Everything runs on the phone itself. This makes it reliable inside walk-in coolers and backroom areas with no signal.

All photos taken during use are temporary. They exist in the phone's memory only long enough for the app to read the text or barcode, then they are discarded. Nothing is saved to the phone's storage or sent anywhere.

## Before You Start

**Print the manifest single-sided.** Double-sided printing makes it harder to look up items while scanning. If the manifest is more than one page, keep the pages separate.

**Number the manifest.** Write position numbers in the margin every 5 rows (1, 5, 10, 15, 20...). This helps you quickly find items when the app gives you a position number.

**Have a pen ready.** You will mark items on the paper manifest and mark boxes on the cart as you work.

## Getting Started

Open the app. You will see the home screen showing "Offline Manifest & Barcode Sync" with a "Capture Manifest" button and a "Help" button. Tap Help before starting to review the built-in guide, including privacy information for managers.

## Step 1: Capture the Manifest

Tap **Capture Manifest**. The camera opens with a white rectangle (the crop guide) overlaid on the screen.

**Position the crop guide:**

- Point the camera at the manifest page
- Align the crop guide so it covers the **UPC number column** and the **item description column**
- You do not need to include the Cases column
- Drag corners or edges to resize the rectangle
- Tap inside the rectangle and drag to move it without resizing

**Helpful controls on the capture screen:**

- **Light OFF / Light ON** (top right): toggles the phone's flashlight for better lighting
- **Cancel** (bottom left): returns to the previous screen without losing any data
- **Camera button** (bottom center): takes the photo

Tap the camera button when the columns are aligned inside the crop guide.

## Step 2: Verify the Crop

After taking the photo, you see the cropped image. Check that all rows are visible and readable.

- Tap **Retry** to go back and retake the photo
- Tap **Process OCR** to have the app read the text

## Step 3: Review Page Results

The app shows what it found on this page. Each item shows a position number, description, and UPC.

**What to check:**

- Does the item count look right for this section of the manifest?
- Do the UPC numbers match what you see on the paper?
- An orange warning banner appears if the app detects a possible misread

**Note about item descriptions:** The descriptions may not be perfect. Words from two different items sometimes get combined. This is a known limitation of how the phone reads printed tables. The UPC numbers are what matter for barcode matching, and those are reliable.

- Tap **Accept** to add these items to your list
- Tap **Retry** to discard and recapture this page

## Step 4: Capture Additional Pages

After accepting a page, you see the Manifest Loaded screen with all items so far.

A green banner at the bottom of the capture screen shows the last item from the previous page (position number, UPC, and description). Use this to know where to start the next page.

**Tip:** Overlap slightly with the previous page. Include the last 1 or 2 items from the previous page in your next capture. The app automatically removes duplicate UPCs.

Tap **Next Page** and repeat Steps 1 through 3 for each page of the manifest.

## Step 5: Verify the Total Count

On the Manifest Loaded screen, you see the total number of items parsed (for example, "68 items parsed").

Below the header, there is an **Expected items** field. Type the total number of items from the bottom of your printed manifest. If the numbers don't match, an orange warning appears telling you how many items may be missing.

## Step 6: Edit Quantities

All items default to 1 case. If any item has more than 1 case listed on the manifest, tap that item on the Manifest Loaded screen. A dialog appears where you can type the correct number of cases and tap Save.

## Step 7: Start Scanning Barcodes

Tap **Start Scanning**. The camera opens in barcode scanning mode (zoomed in slightly for easier reading).

A **Light OFF / Light ON** button is in the top right corner, same as the capture screen, for barcodes in dark spots between stacked boxes. Once turned on, the light stays on through the entire scanning cycle (scan, confirm, reject, next scan) until you turn it off or tap View Report.

Point the camera at a barcode on a box and tap the **scan button** (magnifying glass icon in the center). The app searches for a barcode for about 2 seconds.

If a barcode is found, you see the **Barcode Detected** screen showing:

- The barcode number
- The matched item name and position number (for example, "Item #47: FG CHEESE DANISH")
- **Confirm** and **Reject** buttons

If no barcode is found, a message says "No barcode detected. Try again." Hold the phone steady and try again.

**Tips for reliable scanning:**

- Hold the phone steady when you tap scan
- The phone reads both the standard barcode on the side of the box and the long shipping barcode on the label
- If you keep getting "No barcode detected," try moving slightly closer or adjusting the angle

## Two Ways to Use the App

### Barcode Lookup Mode (Recommended)

The paper manifest is your official record. The app is a lookup tool.

**Workflow:**

1. Pick up a box from the cart
2. Mark a slash (/) on the top right corner of the box
3. Scan the barcode with the app
4. Note the position number the app gives you (for example, #47)
5. Find that position on your numbered paper manifest
6. Circle the item on the paper manifest
7. Complete the slash into an X on the box
8. Move to the next box

When finished, tap **View Report** and check the **Multiples** tab. Any item listed there was scanned more than once, meaning multiple cases arrived. Note the quantity next to those items on your paper manifest.

Write the date, your name, and delivery status on the paper manifest.

**Advantages:**

- Paper record survives if the app crashes or the phone dies
- Manager can review your work without needing the phone
- You visually verify each item against the manifest
- Easy to annotate with notes about damage, extras, or shortages

### Digital Tracking Mode

The app tracks everything. Tap Confirm after each scan and the app keeps count.

**Workflow:**

1. Scan each box's barcode
2. Tap Confirm to record the scan
3. After scanning all boxes, tap **View Report**
4. The Missing tab shows items you haven't scanned
5. The Multiples tab shows items scanned more than once (multiple cases)
6. The Extra tab shows barcodes that weren't on the manifest

**Advantages:**

- Faster per item (no pen, no paper flipping)
- Automatic discrepancy report
- Running count shows progress

**Disadvantage:**

- If the app crashes, you lose your progress
- No permanent paper record

## Troubleshooting

**"No barcode detected" happens often:**
Try holding the phone closer to the barcode and keeping it very still for the full 2 seconds after tapping scan.

**OCR missed some items:**
Check the expected items count warning. If items are missing, tap Next Page and recapture the section that was missed. Overlapping with previous pages is safe because duplicates are automatically removed.

**Item descriptions look wrong:**
This is normal. The phone reads text column by column and sometimes combines words from different items. The UPC numbers are correct and that's what the barcode matching uses.

**App matched the wrong item:**
Some products have UPC numbers that are identical except for the last digit (for example, banana nut and chocolate chip muffins). If the OCR missed the last digit, the app may match the wrong one. Always verify the position number against your paper manifest. If it doesn't look right, tap Reject and look it up manually.

**App crashes or freezes:**
Reopen the app and start over. In Barcode Lookup mode, your paper manifest still has all your progress. This is one reason Barcode Lookup mode is recommended.

**"Could not parse any items" error:**
The camera couldn't read any UPC numbers from the image. Make sure the UPC column is fully inside the crop guide, the page is well-lit (use the light toggle), and the image is not blurry.

**Expected count doesn't match:**
Some UPCs may have been partially cut off at the edge of the crop guide. Recapture the pages where items are missing, overlapping with neighboring pages to catch anything at the edges.

## Technical Notes

- The app uses Google's machine learning libraries (ML Kit) for reading text and scanning barcodes, licensed under the Google APIs Terms of Service (free, no fees, no attribution required)
- All processing happens on the phone. No images or data are sent anywhere
- No images are saved to the phone's storage
- The app is designed and directed by Paul Henkiel, with code generated using AI assistance
