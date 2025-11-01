# 🎵 Music Library Directory

This directory stores your music files for the Streamletz application.

## 📂 How to Add Music

### Method 1: Manual Copy (Development)

1. **Copy your music files** to this directory:
   ```bash
   cp /path/to/your/music/*.mp3 backend/music/
   ```

2. **Restart the backend** to scan and index new files:
   ```bash
   # If using Docker
   docker-compose restart backend
   
   # If running manually
   # Stop the backend (Ctrl+C) and restart with:
   mvn spring-boot:run
   ```

3. **Files are automatically indexed** - The backend scans this directory on startup and extracts metadata from ID3 tags.

4. **Access your music** - Navigate to the Dashboard and your tracks will appear!

### Method 2: Docker Volume (Production)

Mount an external directory in `docker-compose.yml`:

```yaml
services:
  backend:
    volumes:
      - ./backend/music:/app/music
      - /your/external/music/library:/app/music  # Add this
```

## 🎧 Supported Formats

The application supports the following audio formats:

| Format | Extension | Notes |
|--------|-----------|-------|
| MP3    | `.mp3`    | Most common, good compression |
| FLAC   | `.flac`   | Lossless, high quality |
| M4A    | `.m4a`    | AAC audio, Apple format |
| WAV    | `.wav`    | Uncompressed, large files |
| OGG    | `.ogg`    | Open source, good quality |

## 📋 Metadata Extraction

The system automatically extracts the following information from audio files:

### From ID3 Tags (if available):
- **Title** - Song name
- **Artist** - Performer/band name
- **Album** - Album name
- **Cover Art** - Album artwork image
- **Duration** - Track length

### From File Properties:
- **File Size** - Automatically calculated
- **Format** - Detected from extension
- **File Path** - Stored for streaming

### Fallback Behavior:
- If **no ID3 tags** are found → Uses filename as title
- If **no artist** tag → Sets as "Unknown Artist"
- If **no album** → Left empty
- If **no cover art** → Shows default music icon

## 📁 Directory Structure Example

```
backend/music/
├── The Beatles - Hey Jude.mp3
├── Queen - Bohemian Rhapsody.flac
├── Daft Punk - Get Lucky.m4a
├── album_folder/
│   ├── 01 - Track One.mp3
│   ├── 02 - Track Two.mp3
│   └── cover.jpg  (automatically detected)
└── mixtape/
    └── favorite_song.ogg
```

## ⚙️ Configuration

The music directory path is configured in `application.properties`:

```properties
music.storage.path=./music
```

To use a different location, update this property or set the environment variable:

```bash
MUSIC_STORAGE_PATH=/path/to/your/music
```

## 🔍 How Scanning Works

1. **On Startup**: Backend scans the `music/` directory recursively
2. **Metadata Extraction**: Reads ID3 tags using Apache Tika
3. **Database Storage**: Saves track information to PostgreSQL
4. **File Reference**: Stores relative path for streaming
5. **Cover Art**: Extracts and serves embedded album art

## 🚫 What NOT to Put Here

- ❌ Non-audio files (documents, images, etc.)
- ❌ Copyrighted content without permission
- ❌ Corrupted or DRM-protected files
- ❌ System or hidden files

## 💡 Tips

### Organize Your Music
```bash
# Good structure
music/
├── Artist Name/
│   └── Album Name/
│       ├── 01 - Track.mp3
│       └── 02 - Track.mp3

# Also works
music/
├── song1.mp3
├── song2.mp3
└── song3.flac
```

### Check File Permissions
```bash
# Ensure backend can read files
chmod -R 755 backend/music/
```

### File Naming Best Practices
- Use clear, descriptive names
- Include artist and title for better fallback
- Avoid special characters that might cause issues

### Performance
- **Large libraries**: First scan may take time
- **File size**: Consider using compressed formats (MP3/M4A/OGG)
- **Cover art**: Keep images under 1MB for faster loading

## 🐛 Troubleshooting

### Files Not Appearing?

1. **Check file format** - Is it a supported audio format?
2. **Check permissions** - Can the backend read the files?
3. **Restart backend** - New files require a restart to be scanned
4. **Check logs** - Look for scanning errors in backend logs:
   ```bash
   docker-compose logs backend
   ```

### Metadata Not Showing?

- **No ID3 tags**: Use a tool like [MP3Tag](https://www.mp3tag.de/) or [MusicBrainz Picard](https://picard.musicbrainz.org/) to add metadata
- **Corrupted tags**: Re-tag the file with proper metadata

### Cover Art Missing?

- Embed cover art in the audio file using a tag editor
- Place `cover.jpg`, `folder.jpg`, or `album.jpg` in the same directory

## 🎵 Example: Adding Music

```bash
# 1. Navigate to music directory
cd backend/music/

# 2. Copy your files
cp ~/Music/favorite-song.mp3 .

# 3. Restart backend
cd ../..
docker-compose restart backend

# 4. Check logs to confirm scanning
docker-compose logs -f backend

# 5. Open frontend and enjoy!
open http://localhost:5173
```

---

**Your sound. Your stream. Your rules.** 🎵