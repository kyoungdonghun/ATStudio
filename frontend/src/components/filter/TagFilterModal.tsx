import { useState, useEffect, useRef } from 'react';
import Modal from '@/components/ui/Modal';
import FilterChip from '@/components/ui/FilterChip';
import type { TagItem } from '@/types';
import styles from './TagFilterModal.module.css';

interface TagFilterModalProps {
  open: boolean;
  onClose: () => void;
  genreTags: TagItem[];
  moodTags: TagItem[];
  instrumentTags: TagItem[];
  activeGenres: string[];
  activeMoods: string[];
  activeInstruments: string[];
  activeBpmLabel: string;
  bpmPresets: readonly { label: string }[];
  onApply: (genres: string[], moods: string[], instruments: string[], bpm: string) => void;
}

export default function TagFilterModal({
  open,
  onClose,
  genreTags,
  moodTags,
  instrumentTags,
  activeGenres,
  activeMoods,
  activeInstruments,
  activeBpmLabel,
  bpmPresets,
  onApply,
}: TagFilterModalProps) {
  const [selectedGenres, setSelectedGenres] = useState<string[]>(activeGenres);
  const [selectedMoods, setSelectedMoods] = useState<string[]>(activeMoods);
  const [selectedInstruments, setSelectedInstruments] = useState<string[]>(activeInstruments);
  const [selectedBpm, setSelectedBpm] = useState(activeBpmLabel);
  const [query, setQuery] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);

  /* Sync with parent state when modal opens */
  useEffect(() => {
    if (open) {
      setSelectedGenres(activeGenres);
      setSelectedMoods(activeMoods);
      setSelectedInstruments(activeInstruments);
      setSelectedBpm(activeBpmLabel);
      setQuery('');
      setTimeout(() => inputRef.current?.focus(), 100);
    }
  }, [open, activeGenres, activeMoods, activeInstruments, activeBpmLabel]);

  const q = query.trim().toLowerCase();

  const filteredGenres = q ? genreTags.filter((t) => t.name.toLowerCase().includes(q)) : genreTags;
  const filteredMoods = q ? moodTags.filter((t) => t.name.toLowerCase().includes(q)) : moodTags;
  const filteredInstruments = q
    ? instrumentTags.filter((t) => t.name.toLowerCase().includes(q))
    : instrumentTags;
  const filteredBpm = q ? bpmPresets.filter((p) => p.label.toLowerCase().includes(q)) : bpmPresets;

  function toggleGenre(name: string) {
    setSelectedGenres((prev) =>
      prev.includes(name) ? prev.filter((g) => g !== name) : [...prev, name],
    );
  }

  function toggleMood(name: string) {
    setSelectedMoods((prev) =>
      prev.includes(name) ? prev.filter((m) => m !== name) : [...prev, name],
    );
  }

  function toggleInstrument(name: string) {
    setSelectedInstruments((prev) =>
      prev.includes(name) ? prev.filter((i) => i !== name) : [...prev, name],
    );
  }

  function handleApply() {
    onApply(selectedGenres, selectedMoods, selectedInstruments, selectedBpm);
    onClose();
  }

  function handleClear() {
    setSelectedGenres([]);
    setSelectedMoods([]);
    setSelectedInstruments([]);
    setSelectedBpm('');
  }

  const totalSelected =
    selectedGenres.length +
    selectedMoods.length +
    selectedInstruments.length +
    (selectedBpm ? 1 : 0);
  const noResults =
    filteredGenres.length === 0 &&
    filteredMoods.length === 0 &&
    filteredInstruments.length === 0 &&
    filteredBpm.length === 0;

  return (
    <Modal open={open} onClose={onClose} title="필터 검색">
      <div className={styles.body}>
        {/* Search input */}
        <div className={styles.searchWrap}>
          <input
            ref={inputRef}
            className={styles.searchInput}
            type="text"
            placeholder="태그 이름으로 검색..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
          {query && (
            <button className={styles.searchClear} onClick={() => setQuery('')}>
              {'\u2715'}
            </button>
          )}
        </div>

        {noResults ? (
          <div className={styles.noResults}>{`"${query}"에 해당하는 태그가 없습니다.`}</div>
        ) : (
          <>
            {/* Genre */}
            {filteredGenres.length > 0 && (
              <div className={styles.section}>
                <span className={styles.sectionLabel}>{'장르'}</span>
                <div className={styles.chips}>
                  {filteredGenres.map((tag) => (
                    <FilterChip
                      key={tag.id}
                      label={tag.name}
                      active={selectedGenres.includes(tag.name)}
                      onClick={() => toggleGenre(tag.name)}
                    />
                  ))}
                </div>
              </div>
            )}

            {/* Instrument */}
            {filteredInstruments.length > 0 && (
              <div className={styles.section}>
                <span className={styles.sectionLabel}>{'악기'}</span>
                <div className={styles.chips}>
                  {filteredInstruments.map((tag) => (
                    <FilterChip
                      key={tag.id}
                      label={tag.name}
                      active={selectedInstruments.includes(tag.name)}
                      onClick={() => toggleInstrument(tag.name)}
                    />
                  ))}
                </div>
              </div>
            )}

            {/* Mood */}
            {filteredMoods.length > 0 && (
              <div className={styles.section}>
                <span className={styles.sectionLabel}>{'분위기'}</span>
                <div className={styles.chips}>
                  {filteredMoods.map((tag) => (
                    <FilterChip
                      key={tag.id}
                      label={tag.name}
                      active={selectedMoods.includes(tag.name)}
                      onClick={() => toggleMood(tag.name)}
                    />
                  ))}
                </div>
              </div>
            )}

            {/* BPM */}
            {filteredBpm.length > 0 && (
              <div className={styles.section}>
                <span className={styles.sectionLabel}>BPM</span>
                <div className={styles.chips}>
                  {filteredBpm.map((preset) => (
                    <FilterChip
                      key={preset.label}
                      label={preset.label}
                      active={selectedBpm === preset.label}
                      onClick={() =>
                        setSelectedBpm((prev) => (prev === preset.label ? '' : preset.label))
                      }
                    />
                  ))}
                </div>
              </div>
            )}
          </>
        )}

        {/* Footer */}
        <div className={styles.footer}>
          {totalSelected > 0 && (
            <span className={styles.selectedCount}>{`${totalSelected}개 선택됨`}</span>
          )}
          <button className={styles.clearBtn} onClick={handleClear}>
            {'초기화'}
          </button>
          <button className={styles.applyBtn} onClick={handleApply}>
            {'적용'}
          </button>
        </div>
      </div>
    </Modal>
  );
}
