// Unit tests for the pure computations in src/main/resources/fit-to-page.js.
//
// The script is imported for its side effect only: in Node (where `window` is
// undefined) it publishes its pure API on globalThis. No DOM or browser mock is
// needed, and the 'beforeprint' listener is not exercised here.
import { test } from 'node:test';
import assert from 'node:assert/strict';

import '../../main/resources/fit-to-page.js';

const {
  A4,
  PAGE_MARGIN_MM,
  HTML_MARGIN_PX,
  A4_PRINTABLE_WIDTH_PX,
  A4_PRINTABLE_HEIGHT_PX,
  mmToPx,
  printableSizePx,
  fontSizeProperty,
  contentHeightPx,
  findLargestFittingFontSize,
} = globalThis.fitToPage;

test('converts millimetres to CSS pixels', () => {
  assert.equal(mmToPx(25.4), 96);
});

test('computes the printable width and height of an A4 page', () => {
  const marginsPx = 2 * (mmToPx(PAGE_MARGIN_MM) + HTML_MARGIN_PX);
  const expectedWidth = mmToPx(A4.widthMm) - marginsPx;
  const expectedHeight = mmToPx(A4.heightMm) - marginsPx;

  assert.equal(printableSizePx(A4.widthMm), expectedWidth);
  assert.equal(printableSizePx(A4.heightMm), expectedHeight);
  assert.equal(A4_PRINTABLE_WIDTH_PX, expectedWidth);
  assert.equal(A4_PRINTABLE_HEIGHT_PX, expectedHeight);
});

test('derives the CSS custom property name for a medium', () => {
  assert.equal(fontSizeProperty('screen'), '--screen-font-size');
  assert.equal(fontSizeProperty('print'), '--print-font-size');
});

test('computes the content height from the measured table and body positions', () => {
  assert.equal(
    contentHeightPx({marginTop: 20, tableBottom: 1000, bodyTop: 5, marginBottom: 30}),
    1045,
  );
});

test('finds the largest font size that fits', () => {
  // A song whose height is twice its font size fits up to the middle of the
  // available height.
  assert.equal(findLargestFittingFontSize(8, 1000, (size) => 2 * size <= 1000), 500);
});

test('accepts a font size whose content height equals the available height exactly', () => {
  assert.equal(findLargestFittingFontSize(8, 48, (size) => size <= 40), 40);
});

test('falls back to the minimum font size when even the smallest size does not fit', () => {
  assert.equal(findLargestFittingFontSize(8, 48, () => false), 8);
});

test('uses the maximum font size when even the largest size fits', () => {
  assert.equal(findLargestFittingFontSize(8, 48, () => true), 48);
});

test('respects the font size bounds', () => {
  assert.equal(findLargestFittingFontSize(12, 18, () => true), 18);
  assert.equal(findLargestFittingFontSize(12, 18, () => false), 12);
  assert.equal(findLargestFittingFontSize(12, 18, (size) => size <= 15), 15);
});

test('only probes integer font sizes within the bounds', () => {
  const probed = [];

  findLargestFittingFontSize(12, 18, (size) => {
    probed.push(size);
    return size <= 15;
  });

  assert.ok(probed.length > 0);
  for (const size of probed) {
    assert.ok(Number.isInteger(size), `expected an integer font size, got ${size}`);
    assert.ok(size >= 12 && size <= 18, `font size ${size} is outside the bounds`);
  }
});

test('fits a song whose content height grows linearly with the font size', () => {
  // Content height: table height (2 * font size) plus the body margins.
  const expected = Math.floor((A4_PRINTABLE_HEIGHT_PX - 40) / 2);
  const fits = (fontSize) => contentHeightPx({
    marginTop: 20,
    marginBottom: 20,
    tableBottom: 2 * fontSize,
    bodyTop: 0,
  }) <= A4_PRINTABLE_HEIGHT_PX;

  assert.equal(findLargestFittingFontSize(8, 1000, fits), expected);
});

test('accounts for the body margins when measuring the content height', () => {
  // The table alone is exactly the printable height minus the margins, so it
  // only fits because of them; the same table height without margins would not.
  const tableBottom = A4_PRINTABLE_HEIGHT_PX - 40;
  const fits = (fontSize) => contentHeightPx({
    marginTop: 20,
    marginBottom: 20,
    tableBottom,
    bodyTop: 0,
  }) <= A4_PRINTABLE_HEIGHT_PX;

  assert.equal(findLargestFittingFontSize(8, 1000, fits), 1000);
});

test('fits on the exact boundary of the printable height', () => {
  // The content exactly fills the printable page for font sizes up to 40, and
  // is one pixel too tall above that. Only the "equal" case must be accepted.
  const fits = (fontSize) => contentHeightPx({
    marginTop: 0,
    marginBottom: 0,
    tableBottom: fontSize <= 40 ? A4_PRINTABLE_HEIGHT_PX : A4_PRINTABLE_HEIGHT_PX + 1,
    bodyTop: 0,
  }) <= A4_PRINTABLE_HEIGHT_PX;

  assert.equal(findLargestFittingFontSize(8, 48, fits), 40);
});
