#!/usr/bin/env node

import { mkdir, readFile, rm, unlink, writeFile } from "node:fs/promises";
import path from "node:path";
import process from "node:process";
import { deflateSync } from "node:zlib";

const TRACK_PREFIX = "[QA Demo] ";
const PLAYLIST_PREFIX = "[QA Demo] ";
const MANIFEST_MARKER = "ATSTUDIO_QA_DEMO_V1";
const ADMIN_EMAIL = "qa.admin@atstudio.local";
const PLAYLIST_EMAIL = "qa.business@atstudio.local";

const TAG_GROUPS = {
  MOOD: [
    "잔잔한",
    "신나는",
    "따뜻한",
    "몽환적인",
    "감성적인",
    "밝은",
    "긴장감있는",
    "차분한",
    "희망찬",
  ],
  GENRE: [
    "로파이",
    "팝",
    "일렉트로닉",
    "힙합",
    "어쿠스틱",
    "재즈",
    "시네마틱",
    "펑크",
    "앰비언트",
  ],
  INSTRUMENT: [
    "피아노",
    "어쿠스틱기타",
    "신스",
    "드럼",
    "베이스",
    "스트링",
    "벨",
    "브라스",
    "퍼커션",
  ],
  USAGE: [
    "유튜브용",
    "쇼츠용",
    "릴스용",
    "브이로그용",
    "카페영상용",
    "여행영상용",
    "기업광고용",
    "인트로용",
    "아웃트로용",
  ],
};

const TRACK_NAMES = [
  "새벽의 파도",
  "햇살 산책",
  "도시의 네온",
  "느린 오후",
  "별빛 우체국",
  "주말의 시작",
  "구름 위 드라이브",
  "봄날의 약속",
  "조용한 카페",
  "여행의 첫 장",
  "푸른 골목",
  "반짝이는 순간",
  "밤하늘 지도",
  "오렌지 선셋",
  "작은 용기",
  "달빛 리듬",
  "비 오는 창가",
  "우리의 엔딩 크레딧",
  "초록빛 아침",
  "서울의 밤",
  "가벼운 발걸음",
  "필름 속 여름",
  "집으로 가는 길",
  "설레는 카운트다운",
  "바람의 메모",
  "한강 피크닉",
  "미드나잇 토크",
  "따뜻한 안부",
  "새로운 페이지",
  "빛나는 하루",
  "겨울의 온도",
  "작업실의 오후",
  "모험을 시작해",
  "잔잔한 호흡",
  "꿈꾸는 플랫폼",
  "다시 만난 계절",
];

const PLAYLIST_SPECS = [
  ["오늘의 쇼츠", 0, 10],
  ["새벽 집중", 4, 8],
  ["브이로그 산책", 8, 12],
  ["카페와 작업", 12, 9],
  ["여행 하이라이트", 16, 11],
  ["밝은 브랜드 영상", 20, 8],
  ["감성 릴스", 24, 10],
  ["인트로 셀렉션", 28, 7],
  ["하루의 엔딩", 32, 8],
];

const COLORS = [
  [24, 190, 146],
  [47, 128, 237],
  [241, 154, 56],
  [218, 84, 112],
  [112, 91, 201],
  [34, 163, 187],
  [204, 171, 55],
  [76, 176, 96],
  [191, 104, 59],
  [70, 105, 153],
  [196, 74, 176],
  [58, 145, 126],
];

function parseArgs(argv) {
  const options = {
    mode: "seed",
    apiBase: "http://127.0.0.1:8080",
    credentials:
      "C:/Users/jm991/AppData/Local/ATStudio/acceptance-preview-64db91c/backend-environment-credentials.json",
    workDir: path.resolve("output/demo-seed"),
    dryRun: false,
  };

  for (let index = 0; index < argv.length; index += 1) {
    const value = argv[index];
    if (value === "--dry-run") {
      options.dryRun = true;
      continue;
    }
    const next = argv[index + 1];
    if (!next) throw new Error(`Missing value for ${value}`);
    if (value === "--mode") options.mode = next.toLowerCase();
    else if (value === "--api-base") options.apiBase = next.replace(/\/$/, "");
    else if (value === "--credentials")
      options.credentials = path.resolve(next);
    else if (value === "--work-dir") options.workDir = path.resolve(next);
    else throw new Error(`Unknown argument: ${value}`);
    index += 1;
  }

  if (!["seed", "verify", "cleanup"].includes(options.mode)) {
    throw new Error(`Unsupported mode: ${options.mode}`);
  }
  return options;
}

function log(message) {
  process.stdout.write(`${message}\n`);
}

async function apiRequest(
  apiBase,
  route,
  { method = "GET", token, body, form } = {},
) {
  const headers = { Accept: "application/json" };
  if (token) headers.Authorization = `Bearer ${token}`;
  if (body !== undefined)
    headers["Content-Type"] = "application/json; charset=utf-8";

  const response = await fetch(`${apiBase}${route}`, {
    method,
    headers,
    body: form ?? (body === undefined ? undefined : JSON.stringify(body)),
  });
  const text = await response.text();
  if (!response.ok) {
    const compact = text.replace(/\s+/g, " ").slice(0, 500);
    throw new Error(
      `${method} ${route} failed with HTTP ${response.status}: ${compact}`,
    );
  }
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

async function login(apiBase, email, password) {
  const response = await apiRequest(apiBase, "/api/auth/login", {
    method: "POST",
    body: { email, password },
  });
  const token = response?.data?.accessToken;
  if (!token)
    throw new Error(`Login did not return an access token for ${email}`);
  return token;
}

function emptyManifest() {
  return {
    version: 1,
    marker: MANIFEST_MARKER,
    updatedAt: new Date().toISOString(),
    tags: [],
    tracks: [],
    playlists: [],
  };
}

async function loadManifest(manifestPath) {
  try {
    const parsed = JSON.parse(await readFile(manifestPath, "utf8"));
    if (parsed.marker !== MANIFEST_MARKER)
      throw new Error("Manifest marker mismatch");
    return parsed;
  } catch (error) {
    if (error.code === "ENOENT") return emptyManifest();
    throw error;
  }
}

async function saveManifest(manifestPath, manifest) {
  manifest.updatedAt = new Date().toISOString();
  await mkdir(path.dirname(manifestPath), { recursive: true });
  const temporary = `${manifestPath}.tmp`;
  await writeFile(temporary, `${JSON.stringify(manifest, null, 2)}\n`, "utf8");
  await rm(manifestPath, { force: true });
  await writeFile(manifestPath, await readFile(temporary));
  await rm(temporary, { force: true });
}

function replaceManifestEntry(entries, entry) {
  const index = entries.findIndex((current) =>
    ["id", "name", "title"].some(
      (key) =>
        entry[key] !== undefined &&
        current[key] !== undefined &&
        current[key] === entry[key],
    ),
  );
  if (index >= 0) entries[index] = { ...entries[index], ...entry };
  else entries.push(entry);
}

function writeAscii(buffer, offset, value) {
  buffer.write(value, offset, value.length, "ascii");
}

function generateWav(durationSeconds, frequency, variant) {
  const sampleRate = 8000;
  const channels = 1;
  const bitsPerSample = 16;
  const sampleCount = durationSeconds * sampleRate;
  const dataSize = sampleCount * 2;
  const wav = Buffer.allocUnsafe(44 + dataSize);

  writeAscii(wav, 0, "RIFF");
  wav.writeUInt32LE(36 + dataSize, 4);
  writeAscii(wav, 8, "WAVE");
  writeAscii(wav, 12, "fmt ");
  wav.writeUInt32LE(16, 16);
  wav.writeUInt16LE(1, 20);
  wav.writeUInt16LE(channels, 22);
  wav.writeUInt32LE(sampleRate, 24);
  wav.writeUInt32LE((sampleRate * channels * bitsPerSample) / 8, 28);
  wav.writeUInt16LE((channels * bitsPerSample) / 8, 32);
  wav.writeUInt16LE(bitsPerSample, 34);
  writeAscii(wav, 36, "data");
  wav.writeUInt32LE(dataSize, 40);

  for (let sampleIndex = 0; sampleIndex < sampleCount; sampleIndex += 1) {
    const time = sampleIndex / sampleRate;
    const remaining = durationSeconds - time;
    const fade = Math.min(1, time / 0.6, remaining / 0.8);
    const pulse =
      0.78 + 0.18 * Math.sin(2 * Math.PI * (0.08 + variant * 0.002) * time);
    const fundamental = Math.sin(2 * Math.PI * frequency * time);
    const harmonic =
      0.28 * Math.sin(2 * Math.PI * frequency * 2 * time + variant * 0.2);
    const accent = 0.1 * Math.sin(2 * Math.PI * frequency * 0.5 * time);
    const value = Math.max(
      -1,
      Math.min(1, (fundamental + harmonic + accent) * 0.42 * pulse * fade),
    );
    wav.writeInt16LE(Math.round(value * 32767), 44 + sampleIndex * 2);
  }
  return wav;
}

let crcTable;
function crc32(buffer) {
  if (!crcTable) {
    crcTable = Array.from({ length: 256 }, (_, index) => {
      let value = index;
      for (let bit = 0; bit < 8; bit += 1)
        value = value & 1 ? 0xedb88320 ^ (value >>> 1) : value >>> 1;
      return value >>> 0;
    });
  }
  let crc = 0xffffffff;
  for (const byte of buffer) crc = crcTable[(crc ^ byte) & 0xff] ^ (crc >>> 8);
  return (crc ^ 0xffffffff) >>> 0;
}

function pngChunk(type, data) {
  const typeBuffer = Buffer.from(type, "ascii");
  const chunk = Buffer.allocUnsafe(12 + data.length);
  chunk.writeUInt32BE(data.length, 0);
  typeBuffer.copy(chunk, 4);
  data.copy(chunk, 8);
  chunk.writeUInt32BE(
    crc32(Buffer.concat([typeBuffer, data])),
    8 + data.length,
  );
  return chunk;
}

function generateCoverPng(variant) {
  const width = 192;
  const height = 192;
  const [red, green, blue] = COLORS[variant % COLORS.length];
  const raw = Buffer.allocUnsafe((width * 4 + 1) * height);
  for (let y = 0; y < height; y += 1) {
    const row = y * (width * 4 + 1);
    raw[row] = 0;
    for (let x = 0; x < width; x += 1) {
      const offset = row + 1 + x * 4;
      const stripe = (x + y + variant * 11) % 44 < 13 ? 24 : 0;
      const glow = Math.round(20 * (1 - Math.hypot(x - 96, y - 96) / 136));
      raw[offset] = Math.min(255, red + stripe + Math.max(0, glow));
      raw[offset + 1] = Math.min(
        255,
        green + Math.round(stripe * 0.7) + Math.max(0, glow),
      );
      raw[offset + 2] = Math.min(
        255,
        blue + Math.round(stripe * 0.45) + Math.max(0, glow),
      );
      raw[offset + 3] = 255;
    }
  }

  const header = Buffer.alloc(13);
  header.writeUInt32BE(width, 0);
  header.writeUInt32BE(height, 4);
  header[8] = 8;
  header[9] = 6;
  return Buffer.concat([
    Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
    pngChunk("IHDR", header),
    pngChunk("IDAT", deflateSync(raw, { level: 9 })),
    pngChunk("IEND", Buffer.alloc(0)),
  ]);
}

function buildTrackSpecs(tagIdsByType) {
  const keys = [
    "C",
    "C#",
    "D",
    "D#",
    "E",
    "F",
    "F#",
    "G",
    "G#",
    "A",
    "A#",
    "B",
  ];
  return TRACK_NAMES.map((name, index) => ({
    title: `${TRACK_PREFIX}${name}`,
    bpm: 68 + ((index * 7) % 80),
    tonality: keys[index % keys.length],
    duration: 36 + ((index * 7) % 40),
    frequency: 110 + (index % 12) * 18,
    description: `${name} 분위기의 데모 음원입니다. 쇼츠, 브이로그, 브랜드 영상의 배경음악 시연에 사용합니다.`,
    tagIds: [
      tagIdsByType.MOOD[index % 9],
      tagIdsByType.GENRE[(index * 2) % 9],
      tagIdsByType.INSTRUMENT[(index * 4) % 9],
      tagIdsByType.USAGE[(index * 5) % 9],
    ],
  }));
}

async function ensureTags(options, adminToken, manifest, manifestPath) {
  const existingResponse = await apiRequest(options.apiBase, "/api/tags", {
    token: adminToken,
  });
  const byName = new Map(
    (existingResponse.dataList ?? []).map((tag) => [tag.name, tag]),
  );
  const idsByType = {};

  for (const [type, names] of Object.entries(TAG_GROUPS)) {
    idsByType[type] = [];
    for (const name of names) {
      let tag = byName.get(name);
      let createdBySeed = false;
      if (!tag) {
        const created = await apiRequest(options.apiBase, "/api/tags", {
          method: "POST",
          token: adminToken,
          body: { name, type },
        });
        tag = created.data;
        createdBySeed = true;
        log(`tag created: ${name}`);
      }
      idsByType[type].push(tag.id);
      replaceManifestEntry(manifest.tags, {
        id: tag.id,
        name,
        type,
        createdBySeed:
          createdBySeed ||
          manifest.tags.some(
            (item) => item.id === tag.id && item.createdBySeed,
          ),
      });
      await saveManifest(manifestPath, manifest);
    }
  }
  return idsByType;
}

async function listAdminDemoTracks(options, adminToken) {
  const response = await apiRequest(
    options.apiBase,
    `/api/tracks/admin?keyword=${encodeURIComponent(TRACK_PREFIX.trim())}&page=1&size=100`,
    { token: adminToken },
  );
  return response.dataList ?? [];
}

async function ensureTracks(
  options,
  adminToken,
  tagIdsByType,
  manifest,
  manifestPath,
) {
  const audioDir = path.join(options.workDir, "audio");
  const coverDir = path.join(options.workDir, "covers");
  await mkdir(audioDir, { recursive: true });
  await mkdir(coverDir, { recursive: true });

  const existing = await listAdminDemoTracks(options, adminToken);
  const byTitle = new Map(existing.map((track) => [track.title, track]));
  const trackSpecs = buildTrackSpecs(tagIdsByType);
  const ensured = [];

  for (let index = 0; index < trackSpecs.length; index += 1) {
    const spec = trackSpecs[index];
    const wav = generateWav(spec.duration, spec.frequency, index);
    const cover = generateCoverPng(index);
    const fileStem = String(index + 1).padStart(2, "0");
    const wavPath = path.join(audioDir, `qa-demo-${fileStem}.wav`);
    const coverPath = path.join(coverDir, `qa-demo-${fileStem}.png`);
    await writeFile(wavPath, wav);
    await writeFile(coverPath, cover);

    let track = byTitle.get(spec.title);
    let createdBySeed = false;
    if (!track) {
      const form = new FormData();
      form.append("title", spec.title);
      form.append("bpm", String(spec.bpm));
      form.append("tonality", spec.tonality);
      form.append("description", spec.description);
      spec.tagIds.forEach((tagId) => form.append("tagIds", String(tagId)));
      form.append(
        "audioFile",
        new Blob([wav], { type: "audio/wav" }),
        `qa-demo-${fileStem}.wav`,
      );
      form.append(
        "thumbnail",
        new Blob([cover], { type: "image/png" }),
        `qa-demo-${fileStem}.png`,
      );
      const created = await apiRequest(options.apiBase, "/api/tracks", {
        method: "POST",
        token: adminToken,
        form,
      });
      track = created.data;
      createdBySeed = true;
      log(`track created ${index + 1}/${trackSpecs.length}: ${spec.title}`);
    }

    const updateForm = new FormData();
    updateForm.append("title", spec.title);
    updateForm.append("bpm", String(spec.bpm));
    updateForm.append("tonality", spec.tonality);
    updateForm.append("description", spec.description);
    updateForm.append("isActive", "true");
    spec.tagIds.forEach((tagId) => updateForm.append("tagIds", String(tagId)));
    if (track.isActive === false && !createdBySeed) {
      updateForm.append(
        "audioFile",
        new Blob([wav], { type: "audio/wav" }),
        `qa-demo-${fileStem}.wav`,
      );
      updateForm.append(
        "thumbnail",
        new Blob([cover], { type: "image/png" }),
        `qa-demo-${fileStem}.png`,
      );
    }
    const updated = await apiRequest(
      options.apiBase,
      `/api/tracks/${track.id}`,
      {
        method: "PUT",
        token: adminToken,
        form: updateForm,
      },
    );
    track = updated.data;
    ensured.push(track);
    replaceManifestEntry(manifest.tracks, {
      id: track.id,
      title: track.title,
      createdBySeed:
        createdBySeed ||
        manifest.tracks.some(
          (item) => item.id === track.id && item.createdBySeed,
        ),
      audioFile: track.audioFile ?? null,
      thumbnail: track.thumbnail ?? null,
      generatedWav: path.relative(options.workDir, wavPath),
      generatedCover: path.relative(options.workDir, coverPath),
    });
    await saveManifest(manifestPath, manifest);
  }
  return ensured;
}

function playlistTrackIds(tracks, start, count) {
  return Array.from(
    { length: count },
    (_, index) => tracks[(start + index) % tracks.length].id,
  );
}

async function ensurePlaylists(
  options,
  playlistToken,
  tracks,
  manifest,
  manifestPath,
) {
  const response = await apiRequest(options.apiBase, "/api/playlists", {
    token: playlistToken,
  });
  const existing = response.dataList ?? [];
  const byTitle = new Map(
    existing.map((playlist) => [playlist.title, playlist]),
  );
  let activeCount = existing.length;

  for (let index = 0; index < PLAYLIST_SPECS.length; index += 1) {
    const [name, start, count] = PLAYLIST_SPECS[index];
    const title = `${PLAYLIST_PREFIX}${name}`;
    let playlist = byTitle.get(title);
    let createdBySeed = false;
    if (!playlist) {
      if (activeCount >= 10)
        throw new Error(
          "Playlist limit reached before all QA Demo playlists were created",
        );
      const form = new FormData();
      form.append("title", title);
      form.append(
        "description",
        `${name} 장면을 위한 AT.M 데모 큐레이션입니다.`,
      );
      const cover = generateCoverPng(index + 4);
      form.append(
        "thumbnail",
        new Blob([cover], { type: "image/png" }),
        `qa-demo-playlist-${index + 1}.png`,
      );
      const created = await apiRequest(options.apiBase, "/api/playlists", {
        method: "POST",
        token: playlistToken,
        form,
      });
      playlist = created.data;
      createdBySeed = true;
      activeCount += 1;
      log(`playlist created ${index + 1}/${PLAYLIST_SPECS.length}: ${title}`);
    }

    const trackIds = playlistTrackIds(tracks, start, count);
    await apiRequest(
      options.apiBase,
      `/api/playlists/${playlist.id}/tracks/batch`,
      {
        method: "POST",
        token: playlistToken,
        body: { trackIds },
      },
    );
    replaceManifestEntry(manifest.playlists, {
      id: playlist.id,
      title,
      createdBySeed:
        createdBySeed ||
        manifest.playlists.some(
          (item) => item.id === playlist.id && item.createdBySeed,
        ),
      expectedTrackCount: count,
    });
    await saveManifest(manifestPath, manifest);
  }
}

async function verify(options, adminToken, playlistToken) {
  const [tagResponse, publicTracks, adminTracks, playlistResponse] =
    await Promise.all([
      apiRequest(options.apiBase, "/api/tags"),
      apiRequest(
        options.apiBase,
        `/api/tracks?keyword=${encodeURIComponent(TRACK_PREFIX.trim())}&page=1&size=100`,
      ),
      apiRequest(
        options.apiBase,
        `/api/tracks/admin?keyword=${encodeURIComponent(TRACK_PREFIX.trim())}&page=1&size=100`,
        { token: adminToken },
      ),
      apiRequest(options.apiBase, "/api/playlists", { token: playlistToken }),
    ]);

  const expectedTagNames = new Set(Object.values(TAG_GROUPS).flat());
  const tags = (tagResponse.dataList ?? []).filter((tag) =>
    expectedTagNames.has(tag.name),
  );
  const tracks = (publicTracks.dataList ?? []).filter((track) =>
    track.title.startsWith(TRACK_PREFIX),
  );
  const adminDemoTracks = (adminTracks.dataList ?? []).filter((track) =>
    track.title.startsWith(TRACK_PREFIX),
  );
  const playlists = (playlistResponse.dataList ?? []).filter((playlist) =>
    playlist.title.startsWith(PLAYLIST_PREFIX),
  );

  const streamFailures = [];
  for (const track of tracks) {
    if (!(track.duration > 0) || !track.waveformData) {
      streamFailures.push({ id: track.id, reason: "metadata" });
      continue;
    }
    const response = await fetch(
      `${options.apiBase}/api/tracks/${track.id}/stream`,
      {
        headers: { Range: "bytes=0-1023" },
      },
    );
    if (![200, 206].includes(response.status))
      streamFailures.push({ id: track.id, reason: `HTTP_${response.status}` });
    await response.body?.cancel();
  }

  const playlistDetails = [];
  for (const playlist of playlists) {
    const detail = await apiRequest(
      options.apiBase,
      `/api/playlists/${playlist.id}`,
      { token: playlistToken },
    );
    playlistDetails.push({
      id: playlist.id,
      title: playlist.title,
      trackCount: detail.data?.tracks?.length ?? 0,
    });
  }

  const titleSearch = await apiRequest(
    options.apiBase,
    `/api/tracks?keyword=${encodeURIComponent("새벽")}&page=1&size=20`,
  );
  const usageSearch = await apiRequest(
    options.apiBase,
    `/api/tracks?keyword=${encodeURIComponent("유튜브용")}&page=1&size=20`,
  );
  const result = {
    expected: { tags: 36, tracks: 36, playlists: 9 },
    actual: {
      tags: tags.length,
      activeTracks: tracks.length,
      allDemoTracks: adminDemoTracks.length,
      playlists: playlists.length,
    },
    playlists: playlistDetails,
    searches: {
      titleMatches: (titleSearch.dataList ?? []).filter((track) =>
        track.title.startsWith(TRACK_PREFIX),
      ).length,
      usageMatches: (usageSearch.dataList ?? []).filter((track) =>
        track.title.startsWith(TRACK_PREFIX),
      ).length,
    },
    streamFailures,
  };
  result.pass =
    result.actual.tags === 36 &&
    result.actual.activeTracks === 36 &&
    result.actual.playlists === 9 &&
    playlistDetails.every(
      (playlist) => playlist.trackCount >= 5 && playlist.trackCount <= 12,
    ) &&
    result.searches.titleMatches > 0 &&
    result.searches.usageMatches > 0 &&
    streamFailures.length === 0;
  log(JSON.stringify(result, null, 2));
  if (!result.pass) throw new Error("Demo seed verification failed");
  return result;
}

async function safeRemoveStorageFile(storageRoot, storageKey) {
  if (!storageRoot || !storageKey) return;
  const root = path.resolve(storageRoot);
  const target = path.resolve(root, storageKey);
  if (!target.startsWith(`${root}${path.sep}`))
    throw new Error(`Unsafe storage key rejected: ${storageKey}`);
  await unlink(target).catch((error) => {
    if (error.code !== "ENOENT") throw error;
  });
}

async function cleanup(
  options,
  credentials,
  adminToken,
  playlistToken,
  manifest,
  manifestPath,
) {
  if (manifest.marker !== MANIFEST_MARKER)
    throw new Error("Cleanup requires a valid QA Demo manifest");
  const targets = {
    playlists: manifest.playlists.filter((item) => item.createdBySeed),
    tracks: manifest.tracks.filter((item) => item.createdBySeed),
    tags: manifest.tags.filter((item) => item.createdBySeed),
  };
  if (options.dryRun) {
    log(
      JSON.stringify(
        {
          mode: "cleanup-dry-run",
          targets: Object.fromEntries(
            Object.entries(targets).map(([key, value]) => [key, value.length]),
          ),
        },
        null,
        2,
      ),
    );
    return;
  }

  for (const playlist of targets.playlists) {
    await apiRequest(options.apiBase, `/api/playlists/${playlist.id}`, {
      method: "DELETE",
      token: playlistToken,
    });
  }
  for (const track of targets.tracks) {
    await apiRequest(options.apiBase, `/api/tracks/${track.id}`, {
      method: "DELETE",
      token: adminToken,
    });
    await safeRemoveStorageFile(
      credentials.APP_STORAGE_PUBLIC_PATH,
      track.audioFile,
    );
    await safeRemoveStorageFile(
      credentials.APP_STORAGE_PUBLIC_PATH,
      track.thumbnail,
    );
  }
  for (const tag of targets.tags) {
    await apiRequest(options.apiBase, `/api/tags/${tag.id}`, {
      method: "DELETE",
      token: adminToken,
    });
  }
  await rm(path.join(options.workDir, "audio"), {
    recursive: true,
    force: true,
  });
  await rm(path.join(options.workDir, "covers"), {
    recursive: true,
    force: true,
  });
  manifest.cleanedAt = new Date().toISOString();
  await saveManifest(manifestPath, manifest);
  log(
    JSON.stringify(
      {
        mode: "cleanup",
        removed: Object.fromEntries(
          Object.entries(targets).map(([key, value]) => [key, value.length]),
        ),
      },
      null,
      2,
    ),
  );
}

async function main() {
  const options = parseArgs(process.argv.slice(2));
  const manifestPath = path.join(options.workDir, "manifest.json");
  if (options.dryRun && options.mode === "seed") {
    log(
      JSON.stringify(
        {
          mode: "seed-dry-run",
          planned: { tags: 36, tracks: 36, playlists: 9 },
          apiBase: options.apiBase,
          workDir: options.workDir,
        },
        null,
        2,
      ),
    );
    return;
  }

  const credentials = JSON.parse(await readFile(options.credentials, "utf8"));
  const password = credentials.APP_BOOTSTRAP_TEST_USERS_DEFAULT_PASSWORD;
  if (!password)
    throw new Error(
      "Bootstrap test-user password is unavailable in the runtime credential file",
    );
  const [adminToken, playlistToken] = await Promise.all([
    login(options.apiBase, ADMIN_EMAIL, password),
    login(options.apiBase, PLAYLIST_EMAIL, password),
  ]);
  const manifest = await loadManifest(manifestPath);

  if (options.mode === "cleanup") {
    await cleanup(
      options,
      credentials,
      adminToken,
      playlistToken,
      manifest,
      manifestPath,
    );
    return;
  }
  if (options.mode === "verify") {
    await verify(options, adminToken, playlistToken);
    return;
  }

  const tagIdsByType = await ensureTags(
    options,
    adminToken,
    manifest,
    manifestPath,
  );
  const tracks = await ensureTracks(
    options,
    adminToken,
    tagIdsByType,
    manifest,
    manifestPath,
  );
  await ensurePlaylists(options, playlistToken, tracks, manifest, manifestPath);
  await verify(options, adminToken, playlistToken);
}

main().catch((error) => {
  process.stderr.write(`demo seed failed: ${error.message}\n`);
  process.exitCode = 1;
});
