# WI-20260302-ATS-011 Summary

## Status: COMPLETED (pending test verification)

## Changes Made

### M-5 Fix: TrackService track_tags cascade cleanup
- **File**: `src/main/java/com/atstudio/atstudio/service/TrackService.java` (line 167)
- **Change**: Added `trackTagRepository.deleteAllByTrack(track)` before `track.deactivate()` in `deleteTrack()` method
- **Why**: Soft-deleting a Track without removing track_tags records left orphan junction table data

### M-10 Fix: PlaylistService playlist_tracks cascade cleanup
- **File**: `src/main/java/com/atstudio/atstudio/service/PlaylistService.java` (line 196)
- **Change**: Added `playlistTrackRepository.deleteAllByIdPlaylistId(playlistId)` before `playlist.deactivate()` in `deletePlaylist()` method
- **Why**: Soft-deleting a Playlist without removing playlist_tracks records left orphan junction table data

### Tests Added
- **TrackServiceTest**: `deleteTrack_deletesTrackTagsBeforeDeactivate()` -- verifies InOrder that track_tags deletion happens before deactivation
- **PlaylistServiceTest**: `deletePlaylist_deletesPlaylistTracksBeforeDeactivate()` -- verifies InOrder that playlist_tracks deletion happens before deactivation
- **PlaylistServiceTest**: Updated existing `deletePlaylist_success()` to also verify `deleteAllByIdPlaylistId()` is called

## Risk Assessment
- **Risk**: LOW -- both repositories already had the required delete methods; changes are additive (one line each)
- **Side effects**: None -- junction table cleanup before soft-delete is the correct pattern already used in `updateTrack()` (line 154)

## Test Verification
- **Command**: `gradlew.bat clean test --tests "*TrackServiceTest" --tests "*PlaylistServiceTest"`
- **Blocker**: Pre-existing compilation error in `CompanyCertificationService.java:75` (unrelated to this WI, likely from concurrent WI) blocks build. A `clean` build should resolve if the file on disk is correct.
- **Manual verification required**: Run the test command after the compilation blocker is resolved.
