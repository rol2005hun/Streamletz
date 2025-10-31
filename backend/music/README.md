# Music Library

## Hogyan adj hozzá zenéket?

1. **Helyezz MP3/FLAC/M4A/WAV/OGG fájlokat** ebbe a mappába
2. **Indítsd újra a backend-et**: `docker-compose restart backend`
3. A backend automatikusan beolvassa a fájlok metaadatait (ID3 tag-ek)
4. A zenék megjelennek a Dashboard-on

## Metadata

A rendszer a következő információkat olvassa ki:
- **Cím** (Title) - ID3 tag vagy fájlnév
- **Előadó** (Artist) - ID3 tag vagy "Unknown Artist"
- **Album** - ID3 tag (opcionális)
- **Időtartam** - Audio header-ből
- **Fájlméret** - Automatikusan
- **Formátum** - Fájl kiterjesztésből

## Támogatott formátumok
- MP3 (.mp3)
- FLAC (.flac)
- M4A (.m4a)
- WAV (.wav)
- OGG (.ogg)

## Példa

Egyszerűen másold be a zenéket:
```
backend/music/
├── song1.mp3
├── song2.mp3
└── my-favorite-track.flac
```

Majd:
```bash
docker-compose restart backend
```

## Tipp

Ha nincs ID3 tag a fájlban, a fájlnevet használja cím gyanánt!