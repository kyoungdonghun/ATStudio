/** Screen 16-1: Subscription plan comparison */
import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  fetchSubscriptionPlans,
  type SubscriptionPlan,
} from '@/api/subscriptions';
import { fetchMySubscription, type MySubscription } from '@/api/userSubscriptions';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
import { formatPrice } from '@/utils/format';
import type { UserType } from '@/types';
import styles from './SubscriptionPlanPage.module.css';

/* ── Display name mapping ── */

function getDisplayName(name: string): string {
  switch (name) {
    case 'STANDARD': return '스탠더드';
    case 'DELUXE': return '\uB514\uB7ED\uC2A4';
    case 'PREMIUM': return '\uD504\uB9AC\uBBF8\uC5C4';
    default: return name;
  }
}

/* ── Static features per tier ── */

interface TierFeatures {
  features: Array<{ text: string; included: boolean }>;
  btnLabel: string;
  btnVariant: 'fill' | 'ghost';
}

function getTierFeatures(name: string, plan: SubscriptionPlan): TierFeatures {
  const base: Array<{ text: string; included: boolean }> = [
    {
      text: plan.downloadPerDay === -1
        ? '\uBB34\uC81C\uD55C \uB2E4\uC6B4\uB85C\uB4DC'
        : `\uC77C ${plan.downloadPerDay}\uACE1 \uB2E4\uC6B4\uB85C\uB4DC`,
      included: true,
    },
    { text: `\uC7AC\uC0DD\uBAA9\uB85D ${plan.maxPlaylists}\uAC1C`, included: true },
    {
      text: `\uD654\uC774\uD2B8\uB9AC\uC2A4\uD2B8 \uCC44\uB110 ${plan.maxWhitelistChannels}\uAC1C`,
      included: true,
    },
    { text: '\uBAA8\uB4E0 \uC7A5\uB974 \uC811\uADFC', included: true },
  ];

  switch (name) {
    case 'STANDARD':
      return {
        features: [
          ...base,
          { text: '\uC6B0\uC120 \uC9C0\uC6D0', included: false },
        ],
        btnLabel: '\uC2DC\uC791\uD558\uAE30',
        btnVariant: 'ghost',
      };
    case 'DELUXE':
      return {
        features: [
          ...base,
          { text: '\uC6B0\uC120 \uC9C0\uC6D0', included: false },
        ],
        btnLabel: '\uC9C0\uAE08 \uC2DC\uC791\uD558\uAE30',
        btnVariant: 'fill',
      };
    case 'PREMIUM':
      return {
        features: [
          ...base,
          { text: '\uC6B0\uC120 \uC9C0\uC6D0 (72\uC2DC\uAC04 \uB0B4)', included: true },
        ],
        btnLabel: '\uC2DC\uC791\uD558\uAE30',
        btnVariant: 'ghost',
      };
    default:
      return { features: base, btnLabel: '\uC2DC\uC791\uD558\uAE30', btnVariant: 'ghost' };
  }
}

/* ── FAQ ── */

interface FaqItem {
  question: string;
  answer: string;
}

const FAQ_ITEMS: FaqItem[] = [
  {
    question: '\uAD6C\uB3C5 \uCDE8\uC18C \uD6C4\uC5D0\uB3C4 \uB2E4\uC6B4\uB85C\uB4DC\uD55C \uC74C\uC6D0\uC744 \uC0AC\uC6A9\uD560 \uC218 \uC788\uB098\uC694?',
    answer:
      '\uAD6C\uB3C5 \uCDE8\uC18C \uD6C4 \uACB0\uC81C \uAE30\uAC04\uC774 \uB05D\uB098\uBA74 \uC2E0\uADDC \uB2E4\uC6B4\uB85C\uB4DC\uB294 \uBD88\uAC00\uD558\uC9C0\uB9CC, \uAD6C\uB3C5 \uAE30\uAC04 \uC911 \uB2E4\uC6B4\uB85C\uB4DC\uD55C \uC74C\uC6D0\uC758 \uB77C\uC774\uC120\uC2A4\uB294 \uACC4\uC18D \uC720\uD6A8\uD569\uB2C8\uB2E4. \uC774\uBBF8 \uC81C\uC791/\uC5C5\uB85C\uB4DC\uB41C \uCF58\uD150\uCE20\uC5D0\uB294 \uC601\uD5A5\uC774 \uC5C6\uC2B5\uB2C8\uB2E4.',
  },
  {
    question: '\uD50C\uB79C\uC744 \uC911\uAC04\uC5D0 \uC5C5\uADF8\uB808\uC774\uB4DC\uD558\uAC70\uB098 \uB2E4\uC6B4\uADF8\uB808\uC774\uB4DC\uD560 \uC218 \uC788\uB098\uC694?',
    answer:
      '\uC5C5\uADF8\uB808\uC774\uB4DC\uB294 \uC989\uC2DC \uC801\uC6A9\uB418\uBA70 \uB0A8\uC740 \uAE30\uAC04\uC5D0 \uB300\uD55C \uCC28\uC561\uC774 \uACB0\uC81C\uB429\uB2C8\uB2E4. \uB2E4\uC6B4\uADF8\uB808\uC774\uB4DC\uB294 \uD604\uC7AC \uACB0\uC81C \uAE30\uAC04\uC774 \uB05D\uB09C \uD6C4 \uB2E4\uC74C \uACB0\uC81C\uC77C\uBD80\uD130 \uC801\uC6A9\uB429\uB2C8\uB2E4.',
  },
  {
    question: '\uC0C1\uC5C5 \uCC44\uB110 \uB77C\uC774\uC120\uC2A4\uB780 \uBB34\uC5C7\uC778\uAC00\uC694?',
    answer:
      '\uC0C1\uC5C5 \uCC44\uB110 \uB77C\uC774\uC120\uC2A4\uB294 \uAE30\uC5C5 \uB610\uB294 \uBE0C\uB79C\uB4DC \uCC44\uB110\uC5D0\uC11C \uAD11\uACE0, \uD504\uB85C\uBAA8\uC158 \uB4F1 \uC0C1\uC5C5\uC801 \uBAA9\uC801\uC758 \uCF58\uD150\uCE20\uC5D0 \uC74C\uC6D0\uC744 \uC0AC\uC6A9\uD560 \uC218 \uC788\uB294 \uB77C\uC774\uC120\uC2A4\uC785\uB2C8\uB2E4. Pro \uC774\uC0C1 \uAD6C\uB3C5\uC5D0 \uD3EC\uD568\uB429\uB2C8\uB2E4.',
  },
  {
    question: '\uAE30\uC5C5 \uC778\uC99D\uC740 \uC5B4\uB5BB\uAC8C \uC9C4\uD589\uB418\uB098\uC694?',
    answer:
      '\uAE30\uC5C5 \uC778\uC99D\uC740 \uAE30\uC5C5 \uD68C\uC6D0\uC774 \uC0AC\uC5C5\uC790\uB4F1\uB85D\uC99D\uC744 \uC81C\uCD9C\uD558\uBA74 \uAD00\uB9AC\uC790 \uC2EC\uC0AC\uB97C \uAC70\uCCD0 \uC2B9\uC778\uB429\uB2C8\uB2E4. \uC2B9\uC778 \uD6C4 \uC0C1\uC5C5 \uCC44\uB110 \uB77C\uC774\uC120\uC2A4\uAC00 \uD65C\uC131\uD654\uB429\uB2C8\uB2E4.',
  },
  {
    question: '\uC77C \uB2E4\uC6B4\uB85C\uB4DC \uD69F\uC218\uB294 \uC5B8\uC81C \uCD08\uAE30\uD654\uB418\uB098\uC694?',
    answer:
      '\uC77C \uB2E4\uC6B4\uB85C\uB4DC \uD69F\uC218\uB294 \uB9E4\uC77C \uC790\uC815\uC5D0 \uC790\uB3D9 \uCD08\uAE30\uD654\uB429\uB2C8\uB2E4.',
  },
];

function formatMonthlyFromYearly(yearly: number): string {
  return formatPrice(Math.floor(yearly / 12));
}

export default function SubscriptionPlanPage() {
  const navigate = useNavigate();
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const loggedInUserType = useAuthStore((s) => s.user?.userType);

  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);
  const [mySub, setMySub] = useState<MySubscription | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isYearly, setIsYearly] = useState(true);
  const [openFaq, setOpenFaq] = useState<number>(0);
  const [viewType, setViewType] = useState<UserType>(loggedInUserType ?? 'INDIVIDUAL');

  const loadPlans = useCallback(async (userType: UserType) => {
    try {
      setLoading(true);
      setError(null);
      const fetched = await fetchSubscriptionPlans(userType);
      setPlans(fetched);
      if (isAuthenticated) {
        try {
          const sub = await fetchMySubscription();
          setMySub(sub);
        } catch {
          /* no active subscription */
        }
      }
    } catch {
      setError('\uD50C\uB79C \uC815\uBCF4\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4.');
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    loadPlans(viewType);
  }, [viewType, loadPlans]);

  const toastShow = useToastStore((s) => s.show);

  const handleSubscribe = (planName: string) => {
    if (!isAuthenticated) {
      toastShow('warning', '로그인 후 구독을 진행할 수 있습니다.');
      navigate('/login');
      return;
    }
    if (loggedInUserType && loggedInUserType !== viewType) {
      const label = loggedInUserType === 'INDIVIDUAL' ? '개인' : '기업';
      toastShow('warning', `현재 ${label} 회원입니다. ${label} 회원용 플랜을 선택해주세요.`);
      return;
    }
    const cycle = isYearly ? 'YEARLY' : 'MONTHLY';
    if (mySub && mySub.status === 'ACTIVE') {
      navigate(`/subscriptions/manage?plan=${planName}&cycle=${cycle}`);
    } else {
      navigate(`/subscriptions/payment?plan=${planName}&cycle=${cycle}`);
    }
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

  /* Sort plans by priceMonthly for consistent ordering */
  const sortedPlans = [...plans]
    .filter((p) => p.isActive)
    .sort((a, b) => a.priceMonthly - b.priceMonthly);

  /* Build dynamic compare rows */
  const compareRows: Array<{ label: string; values: string[] }> = [
    {
      label: '\uC77C \uB2E4\uC6B4\uB85C\uB4DC \uD55C\uB3C4',
      values: sortedPlans.map((p) =>
        p.downloadPerDay === -1 ? '\uBB34\uC81C\uD55C' : `${p.downloadPerDay}\uACE1`,
      ),
    },
    {
      label: '\uC7AC\uC0DD\uBAA9\uB85D',
      values: sortedPlans.map((p) => `${p.maxPlaylists}\uAC1C`),
    },
    {
      label: '\uD654\uC774\uD2B8\uB9AC\uC2A4\uD2B8 \uCC44\uB110',
      values: sortedPlans.map((p) => `${p.maxWhitelistChannels}\uAC1C`),
    },
    {
      label: '\uBAA8\uB4E0 \uC7A5\uB974 \uC811\uADFC',
      values: sortedPlans.map(() => '\u2713'),
    },
    {
      label: '\uC6B0\uC120 \uC9C0\uC6D0',
      values: sortedPlans.map((p) =>
        p.name === 'PREMIUM' ? '\u2713' : '\u2717',
      ),
    },
    {
      label: '\uAD6C\uB3C5 \uCDE8\uC18C',
      values: sortedPlans.map(() => '\uC5B8\uC81C\uB4E0 \uAC00\uB2A5'),
    },
  ];

  return (
    <div className={styles.page}>
      {/* Hero */}
      <div className={styles.hero}>
        <h1 className={styles.heroTitle}>{'\uC1FC\uCE20 \uD06C\uB9AC\uC5D0\uC774\uD130\uB97C \uC704\uD55C \uD50C\uB79C'}</h1>
        <p className={styles.heroDesc}>
          {'\uACE0\uD488\uC9C8 \uB77C\uC774\uC120\uC2A4 \uC74C\uC545\uC744 \uAD6C\uB3C5 \uD558\uB098\uB85C \uBB34\uC81C\uD55C \uC0AC\uC6A9\uD558\uC138\uC694.'}
          <br />
          {'\uC5B8\uC81C\uB4E0 \uCDE8\uC18C \uAC00\uB2A5, \uC704\uC57D\uAE08 \uC5C6\uC74C.'}
        </p>
      </div>

      {/* User Type Tabs */}
      <div className={styles.userTypeTabs}>
        <button
          className={`${styles.userTypeTab} ${viewType === 'INDIVIDUAL' ? styles.userTypeTabActive : ''}`}
          onClick={() => setViewType('INDIVIDUAL')}
        >
          {'\uAC1C\uC778'}
        </button>
        <button
          className={`${styles.userTypeTab} ${viewType === 'BUSINESS' ? styles.userTypeTabActive : ''}`}
          onClick={() => setViewType('BUSINESS')}
        >
          {'\uAE30\uC5C5'}
        </button>
      </div>

      {/* Billing Toggle */}
      <div className={styles.billingToggle}>
        <span
          className={`${styles.toggleLabel} ${!isYearly ? styles.toggleOn : ''}`}
        >
          {'\uC6D4\uAC04 \uACB0\uC81C'}
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
          {'\uC5F0\uAC04 \uACB0\uC81C'}
        </span>
        {isYearly && (
          <span className={styles.badgeSave}>{'2\uAC1C\uC6D4 \uBB34\uB8CC'}</span>
        )}
      </div>

      {/* Current subscription banner */}
      {mySub && mySub.status === 'ACTIVE' && (
        <div className={styles.currentSubBanner}>
          {'\uD604\uC7AC '}
          <strong>{getDisplayName(mySub.subscription.name)}</strong>
          {' \uD50C\uB79C\uC744 \uAD6C\uB3C5 \uC911\uC785\uB2C8\uB2E4.'}
          <button
            className={styles.manageLink}
            onClick={() => navigate('/subscriptions/manage')}
          >
            {'\uAD6C\uB3C5 \uAD00\uB9AC \u2192'}
          </button>
        </div>
      )}

      {/* Plan Cards */}
      <div className={styles.plans}>
        {sortedPlans.map((plan) => {
          const isMiddle = plan.name === 'DELUXE';
          const tier = getTierFeatures(plan.name, plan);
          const isCurrentPlan =
            mySub?.status === 'ACTIVE' &&
            mySub.subscription.name.toUpperCase() === plan.name.toUpperCase();

          const cardClass = [
            styles.planCard,
            isMiddle ? styles.popular : '',
            isCurrentPlan ? styles.currentPlan : '',
          ]
            .filter(Boolean)
            .join(' ');

          const price = isYearly
            ? formatMonthlyFromYearly(plan.priceYearly)
            : formatPrice(plan.priceMonthly);

          const yearlyTotal = formatPrice(plan.priceYearly);

          return (
            <div key={plan.id} className={cardClass}>
              {isCurrentPlan && (
                <div className={styles.currentBadge}>{'\uD604\uC7AC \uAD6C\uB3C5 \uC911'}</div>
              )}
              {!isCurrentPlan && isMiddle && (
                <div className={styles.popularBadge}>{'\uAC00\uC7A5 \uC778\uAE30 \uC788\uC5B4\uC694'}</div>
              )}
              <div
                className={styles.planName}
                style={
                  isMiddle ? { color: 'var(--accent)' } : undefined
                }
              >
                {getDisplayName(plan.name)}
              </div>
              <div className={styles.planPrice}>
                <span className={styles.priceNum}>{price}</span>
                <span className={styles.pricePeriod}>{'/ \uC6D4'}</span>
              </div>
              {isYearly && (
                <div className={styles.priceAnnual}>
                  {'\uC5F0\uAC04 \uACB0\uC81C \uC2DC '}
                  <span>
                    {yearlyTotal} ({formatMonthlyFromYearly(plan.priceYearly)}
                    {'/\uC6D4'})
                  </span>
                </div>
              )}
              <hr
                className={styles.divider}
                style={
                  isMiddle
                    ? { borderColor: 'var(--accent-border)' }
                    : undefined
                }
              />
              <ul className={styles.features}>
                {tier.features.map((f) => (
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
                className={`${styles.btnPlan} ${isCurrentPlan ? styles.btnCurrent : tier.btnVariant === 'fill' ? styles.btnFill : styles.btnGhost}`}
                onClick={() => handleSubscribe(plan.name)}
                disabled={isCurrentPlan}
              >
                {isCurrentPlan ? '\uD604\uC7AC \uD50C\uB79C' : tier.btnLabel}
              </button>
            </div>
          );
        })}
      </div>

      {/* Compare Table */}
      <section className={styles.compareSection}>
        <h2 className={styles.compareTitle}>{'\uD50C\uB79C \uC0C1\uC138 \uBE44\uAD50'}</h2>
        <div className={styles.compareWrap}>
        <table className={styles.compareTable}>
          <thead>
            <tr>
              <th style={{ width: '36%' }}>{'\uAE30\uB2A5'}</th>
              {sortedPlans.map((p, i) => (
                <th key={p.id} className={i === 1 ? styles.hl : undefined}>
                  {getDisplayName(p.name)}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {compareRows.map((row) => (
              <tr key={row.label}>
                <td>{row.label}</td>
                {row.values.map((val, i) => (
                  <td key={i} className={i === 1 ? styles.hl : undefined}>
                    {val}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
        </div>
      </section>

      {/* FAQ */}
      <section className={styles.faqSection}>
        <h2 className={styles.faqTitle}>{'\uC790\uC8FC \uBB3B\uB294 \uC9C8\uBB38'}</h2>
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
