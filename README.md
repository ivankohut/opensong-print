# opensong-to-print

A small command-line tool that converts a folder of [OpenSong](https://www.opensong.org/) XML song files
into printable HTML pages, one per song, plus an index listing the whole hymnbook.

The generated pages are laid out for printing: lyrics in slides, section names (verse, chorus, …)
labelled on the side with the original OpenSong section codes, optional space left above the first
line of each section for writing in chords, and a font size that can be adjusted in the browser
before printing. The page headings and labels are in Slovak ("Spevník", "Refrén", "Sloha", …).

## Requirements

- Java 17 or newer

## Build

```bash
./gradlew shadowJar
```

This produces a self-contained executable jar at `build/libs/opensong-to-print.jar`.

## Usage

```bash
./print-folder.sh "<hymnbook name>" <folder with OpenSong songs>
```

or directly:

```bash
java -jar build/libs/opensong-to-print.jar "<hymnbook name>" <folder with OpenSong songs>
```

For example:

```bash
java -jar build/libs/opensong-to-print.jar "Oranžový spevníček" ./my-songs
```

The tool scans the given folder for files in the OpenSong format (files whose content starts with
`<?xml version="1.0" encoding="UTF-8"?>` followed by a `<song>` root element), and writes the result
into an `html` subfolder next to them:

```
my-songs/
├── song1          # OpenSong XML files (no extension expected)
├── song2
└── html/
    ├── index.html # hymnbook index: song number – title, sorted by number
    ├── song1.html
    └── song2.html
```

Open `html/index.html` in a browser, adjust the font size or the chord space if needed, and print.

## Song features

- Sections are split into slides: `||` starts a new slide, `|` starts a new line within a slide.
- Section type codes are recognised: `V` (verse), `C` (chorus), `P` (pre-chorus), `B` (bridge),
  `T` (tag), optionally followed by a number (e.g. `[V2]`).
- Lines starting with `.` are ignored.
- Chords (written above the lyrics in OpenSong files) are not rendered, but space is reserved
  above the first verse line so chords can be added by hand. This can be toggled on the printed page.
- The hymn number (`hymn_number`) and title (`title`) are shown in the page header, and the songs
  are sorted by number in the index.

## Tests

Unit tests:

```bash
./gradlew test
```

End-to-end test (builds the jar, converts the sample songs in `src/test`, and compares the output
with the expected HTML files):

```bash
cd src/test
./e2e-test.sh
```

## Project layout

- `src/main/kotlin/Main.kt` — the whole application: OpenSong XML parsing, section/slide model,
  and HTML generation.
- `src/test/kotlin/` — unit tests.
- `src/test/*.xml`, `src/test/expected-*.html`, `src/test/e2e-test.sh` — e2e test fixtures.
- `print-folder.sh` — convenience wrapper around `java -jar`.
