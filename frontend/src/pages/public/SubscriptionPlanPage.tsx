/** Screen 16-1: Subscription plan comparison */
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  fetchSubscriptionPlans,
  type SubscriptionPlan,
} from '@/api/subscriptions';
import styles from './SubscriptionPlanPage.module.css';

/* ── Static plan presentation data ── */

interface PlanDisplay {
  key: string;
  name: string;
  popular: boolean;
  features: Array<{ text: string; included: boolean }>;
  btnLabel: string;
  btnVariant: 'fill' | 'ghost';
}

const PLAN_DISPLAYS: PlanDisplay[] = [
  {
    key: 'STANDARD',
    name: 'Starter',
    popular: false,
    features: [
      { text: '개인 채널 라이선스', included: true },
      { text: '모든 장르 접근', included: true },
      { text: '상업 채널 라이선스', included: false },
      { text: '기업 인증', included: false },
      { text: '우선 지원', included: false },
    ],
    btnLabel: '시작하기',
    btnVariant: 'ghost',
  },
  {
    key: 'PRO',
    name: 'Pro',
    popular: true,
    features: [
      { text: '개인 + 상업 채널 라이선스', included: true },
      { text: '모든 장르 접근', included: true },
      { text: '기업 인증 가능', included: true },
      { text: '무제한 다운로드', included: false },
      { text: '우선 지원', included: false },
    ],
    btnLabel: '지금 시작하기',
    btnVariant: 'fill',
  },
  {
    key: 'PREMIUM',
    name: 'Business',
    popular: false,
    features: [
      { text: '개인 + 상업 채널 라이선스', included: true },
      { text: '모든 장르 접근', included: true },
      { text: '기업 인증 가능', included: true },
      { text: '무제한 다운로드', included: true },
      { text: '우선 지원 (72시간 내)', included: true },
    ],
    btnLabel: '시작하기',
    btnVariant: 'ghost',
  },
];

const COMPARE_ROWS: Array<{
  label: string;
  values: [string, string, string];
}> = [
  { label: '월 다운로드 한도', values: ['30곡', '100곡', '무제한'] },
  { label: '재생목록', values: ['1개', '3개', '3개'] },
  {
    label: '개인 채널 라이선스',
    values: ['\u2713', '\u2713', '\u2713'],
  },
  {
    label: '상업 채널 라이선스',
    values: ['\u2717', '\u2713', '\u2713'],
  },
  { label: '기업 인증', values: ['\u2717', '\u2713', '\u2713'] },
  { label: '우선 지원', values: ['\u2717', '\u2717', '\u2713'] },
  {
    label: '구독 취소',
    values: ['언제든 가능', '언제든 가능', '언제든 가능'],
  },
];

interface FaqItem {
  question: string;
  answer: string;
}

const FAQ_ITEMS: FaqItem[] = [
  {
    question: '구독 취소 후에도 다운로드한 음원을 사용할 수 있나요?',
    answer:
      '구독 취소 후 결제 기간이 끝나면 신규 다운로드는 불가하지만, 구독 기간 중 다운로드한 음원의 라이선스는 계속 유효합니다. 이미 제작/업로드된 콘텐츠에는 영향이 없습니다.',
  },
  {
    question: '플랜을 중간에 업그레이드하거나 다운그레이드할 수 있나요?',
    answer:
      '업그레이드는 즉시 적용되며 남은 기간에 대한 차액이 결제됩니다. 다운그레이드는 현재 결제 기간이 끝난 후 다음 결제일부터 적용됩니다.',
  },
  {
    question: '상업 채널 라이선스란 무엇인가요?',
    answer:
      '상업 채널 라이선스는 기업 또는 브랜드 채널에서 광고, 프로모션 등 상업적 목적의 콘텐츠에 음원을 사용할 수 있는 라이선스입니다. Pro 이상 구독에 포함됩니다.',
  },
  {
    question: '기업 인증은 어떻게 진행되나요?',
    answer:
      '기업 인증은 Pro 이상 구독자가 사업자등록증을 제출하면 관리자 심사를 거쳐 승인됩니다. 승인 후 상업 채널 라이선스가 활성화됩니다.',
  },
  {
    question: '월 다운로드 횟수는 언제 초기화되나요?',
    answer:
      '월 다운로드 횟수는 구독 결제일 기준으로 매월 자동 초기화됩니다.',
  },
];

function formatPrice(n: number): string {
  return `\u20A9${n.toLocaleString()}`;
}

function formatMonthlyFromYearly(yearly: number): string {
  const monthly = Math.floor(yearly / 12);
  return `\u20A9${monthly.toLocaleString()}`;
}

export default function SubscriptionPlanPage() {
  const navigate = useNavigate();
  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isYearly, setIsYearly] = useState(true);
  const [openFaq, setOpenFaq] = useState<number>(0);

  useEffect(() => {
    fetchSubscriptionPlans()
      .then(setPlans)
      .catch(() => setError('Failed to load plans'))
      .finally(() => setLoading(false));
  }, []);

  const handleSubscribe = () => {
    navigate('/login');
  };

  const toggleFaq = (idx: number) => {
    setOpenFaq(openFaq === idx ? -1 : idx);
  };

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>Loading...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{error}</div>
      </div>
    );
  }

  /* Map API plans to display order */
  const displayPlans = PLAN_DISPLAYS.map((dp) => {
    const apiPlan = plans.find((p) => p.name === dp.key);
    return { display: dp, api: apiPlan };
  });

  return (
    <div className={styles.page}>
      {/* Hero */}
      <div className={styles.hero}>
        <h1 className={styles.heroTitle}>쇼츠 크리에이터를 위한 플랜</h1>
        <p className={styles.heroDesc}>
          고품질 라이선스 음악을 구독 하나로 무제한 사용하세요.
          <br />
          언제든 취소 가능, 위약금 없음.
        </p>
      </div>

      {/* Billing Toggle */}
      <div className={styles.billingToggle}>
        <span
          className={`${styles.toggleLabel} ${!isYearly ? styles.toggleOn : ''}`}
        >
          월간 결제
        </span>
        <button
          className={styles.toggleTrack}
          onClick={() => setIsYearly(!isYearly)}
          aria-label="Toggle billing cycle"
        >
          <span
            className={styles.toggleThumb}
            style={{ [isYearly ? 'right' : 'left']: 3 }}
          />
        </button>
        <span
          className={`${styles.toggleLabel} ${isYearly ? styles.toggleOn : ''}`}
        >
          연간 결제
        </span>
        {isYearly && (
          <span className={styles.badgeSave}>2개월 무료</span>
        )}
      </div>

      {/* Plan Cards */}
      <div className={styles.plans}>
        {displayPlans.map(({ display, api }) => {
          const cardClass = display.popular
            ? `${styles.planCard} ${styles.popular}`
            : styles.planCard;

          const price = api
            ? isYearly
              ? formatMonthlyFromYearly(api.priceYearly)
              : formatPrice(api.priceMonthly)
            : '-';

          const yearlyTotal = api ? formatPrice(api.priceYearly) : '-';

          return (
            <div key={display.key} className={cardClass}>
              {display.popular && (
                <div className={styles.popularBadge}>가장 인기 있어요</div>
              )}
              <div
                className={styles.planName}
                style={
                  display.popular ? { color: 'var(--accent)' } : undefined
                }
              >
                {display.name}
              </div>
              <div className={styles.planPrice}>
                <span className={styles.priceNum}>{price}</span>
                <span className={styles.pricePeriod}>/ 월</span>
              </div>
              {isYearly && api && (
                <div className={styles.priceAnnual}>
                  연간 결제 시{' '}
                  <span>
                    {yearlyTotal} ({formatMonthlyFromYearly(api.priceYearly)}
                    /월)
                  </span>
                </div>
              )}
              <hr
                className={styles.divider}
                style={
                  display.popular
                    ? { borderColor: 'var(--accent-border)' }
                    : undefined
                }
              />
              <ul className={styles.features}>
                {api && (
                  <li>
                    <span className={styles.check}>{'\u2713'}</span>
                    일 {api.downloadPerDay}곡 다운로드
                  </li>
                )}
                {display.features.map((f) => (
                  <li
                    key={f.text}
                    className={f.included ? '' : styles.dim}
                  >
                    <span
                      className={f.included ? styles.check : styles.cross}
                    >
                      {f.included ? '\u2713' : '\u2717'}
                    </span>
                    {f.text}
                  </li>
                ))}
              </ul>
              <button
                className={`${styles.btnPlan} ${display.btnVariant === 'fill' ? styles.btnFill : styles.btnGhost}`}
                onClick={handleSubscribe}
              >
                {display.btnLabel}
              </button>
            </div>
          );
        })}
      </div>

      {/* Compare Table */}
      <section className={styles.compareSection}>
        <h2 className={styles.compareTitle}>플랜 상세 비교</h2>
        <table className={styles.compareTable}>
          <thead>
            <tr>
              <th style={{ width: '36%' }}>기능</th>
              <th>Starter</th>
              <th className={styles.hl}>Pro</th>
              <th>Business</th>
            </tr>
          </thead>
          <tbody>
            {COMPARE_ROWS.map((row) => (
              <tr key={row.label}>
                <td>{row.label}</td>
                <td>{row.values[0]}</td>
                <td className={styles.hl}>{row.values[1]}</td>
                <td>{row.values[2]}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      {/* FAQ */}
      <section className={styles.faqSection}>
        <h2 className={styles.faqTitle}>자주 묻는 질문</h2>
        {FAQ_ITEMS.map((item, idx) => (
          <div
            key={item.question}
            className={`${styles.faqItem} ${openFaq === idx ? styles.faqOpen : ''}`}
          >
            <button
              className={styles.faqQ}
              onClick={() => toggleFaq(idx)}
            >
              {item.question}
              <span className={styles.faqIcon}>+</span>
            </button>
            {openFaq === idx && (
              <div className={styles.faqA}>{item.answer}</div>
            )}
          </div>
        ))}
      </section>
    </div>
  );
}
