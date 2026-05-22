/** Admin read-only payment operations view */
import { useCallback, useEffect, useState } from 'react';
import {
  fetchAdminBillingAgreements,
  fetchAdminPaymentOrders,
  fetchAdminSubscriptionPayments,
  type AdminBillingAgreement,
  type AdminPaymentOrder,
  type AdminSubscriptionPayment,
} from '@/api/admin';
import Pagination from '@/components/ui/Pagination';
import type { PageInfo } from '@/types';
import { formatDate, formatDateTime, formatPrice } from '@/utils/format';
import styles from './PaymentReadOnlyPage.module.css';

type TabKey = 'orders' | 'agreements' | 'payments';

export default function PaymentReadOnlyPage() {
  const [tab, setTab] = useState<TabKey>('orders');
  const [page, setPage] = useState(1);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [orders, setOrders] = useState<AdminPaymentOrder[]>([]);
  const [agreements, setAgreements] = useState<AdminBillingAgreement[]>([]);
  const [payments, setPayments] = useState<AdminSubscriptionPayment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      if (tab === 'orders') {
        const result = await fetchAdminPaymentOrders(page, 20);
        setOrders(result.dataList);
        setPageInfo(result.pageInfo);
      } else if (tab === 'agreements') {
        const result = await fetchAdminBillingAgreements(page, 20);
        setAgreements(result.dataList);
        setPageInfo(result.pageInfo);
      } else {
        const result = await fetchAdminSubscriptionPayments(page, 20);
        setPayments(result.dataList);
        setPageInfo(result.pageInfo);
      }
    } catch {
      setError('결제 정보를 불러오지 못했습니다.');
      setPageInfo(null);
    } finally {
      setLoading(false);
    }
  }, [page, tab]);

  useEffect(() => {
    void loadData();
  }, [loadData]);

  function changeTab(next: TabKey) {
    setTab(next);
    setPage(1);
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>{'결제 조회'}</h1>

      <div className={styles.tabs}>
        <button
          className={tab === 'orders' ? styles.tabActive : styles.tab}
          onClick={() => changeTab('orders')}
        >
          {'주문'}
        </button>
        <button
          className={tab === 'agreements' ? styles.tabActive : styles.tab}
          onClick={() => changeTab('agreements')}
        >
          {'자동결제'}
        </button>
        <button
          className={tab === 'payments' ? styles.tabActive : styles.tab}
          onClick={() => changeTab('payments')}
        >
          {'결제내역'}
        </button>
      </div>

      {loading && <div className={styles.loading}>{'불러오는 중...'}</div>}
      {error && <div className={styles.error}>{error}</div>}
      {!loading && !error && tab === 'orders' && <OrderTable orders={orders} />}
      {!loading && !error && tab === 'agreements' && <AgreementTable agreements={agreements} />}
      {!loading && !error && tab === 'payments' && <PaymentTable payments={payments} />}

      {pageInfo && pageInfo.total > pageInfo.size && (
        <Pagination pageInfo={pageInfo} currentPage={page} onPageChange={setPage} />
      )}
    </div>
  );
}

function OrderTable({ orders }: { orders: AdminPaymentOrder[] }) {
  return (
    <div className={styles.tableWrap}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>{'주문번호'}</th>
            <th>{'사용자'}</th>
            <th>{'목적'}</th>
            <th>{'PG'}</th>
            <th>{'상태'}</th>
            <th>{'플랜'}</th>
            <th>{'금액'}</th>
            <th>{'실패'}</th>
            <th>{'생성일'}</th>
          </tr>
        </thead>
        <tbody>
          {orders.length === 0 && <EmptyRow colSpan={9} />}
          {orders.map((order) => (
            <tr key={order.id}>
              <td>{order.orderId}</td>
              <td>{order.userNickname}</td>
              <td>{order.purpose}</td>
              <td>{order.provider}</td>
              <td>
                <span className={statusClass(order.status)}>{order.status}</span>
              </td>
              <td>{order.subscriptionName}</td>
              <td>{formatPrice(order.amount)}</td>
              <td>{order.failureCode ?? '-'}</td>
              <td>{formatDateTime(order.createdAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function AgreementTable({ agreements }: { agreements: AdminBillingAgreement[] }) {
  return (
    <div className={styles.tableWrap}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>{'사용자'}</th>
            <th>{'PG'}</th>
            <th>{'상태'}</th>
            <th>{'수단'}</th>
            <th>{'다음 결제일'}</th>
            <th>{'실패 횟수'}</th>
            <th>{'해지일'}</th>
          </tr>
        </thead>
        <tbody>
          {agreements.length === 0 && <EmptyRow colSpan={8} />}
          {agreements.map((agreement) => (
            <tr key={agreement.id}>
              <td>{agreement.id}</td>
              <td>{agreement.userNickname}</td>
              <td>{agreement.provider}</td>
              <td>
                <span className={statusClass(agreement.status)}>{agreement.status}</span>
              </td>
              <td>
                {agreement.maskedMethod
                  ? `${agreement.payMethod ?? 'CARD'} ${agreement.maskedMethod}`
                  : agreement.payMethod ?? '-'}
              </td>
              <td>{formatDate(agreement.nextBillingAt)}</td>
              <td>{agreement.failureCount}</td>
              <td>{formatDateTime(agreement.cancelledAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function PaymentTable({ payments }: { payments: AdminSubscriptionPayment[] }) {
  return (
    <div className={styles.tableWrap}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>{'사용자'}</th>
            <th>{'주문번호'}</th>
            <th>{'플랜'}</th>
            <th>{'주기'}</th>
            <th>{'PG'}</th>
            <th>{'상태'}</th>
            <th>{'금액'}</th>
            <th>{'생성일'}</th>
          </tr>
        </thead>
        <tbody>
          {payments.length === 0 && <EmptyRow colSpan={9} />}
          {payments.map((payment) => (
            <tr key={payment.id}>
              <td>{payment.id}</td>
              <td>{payment.userNickname}</td>
              <td>{payment.orderId ?? '-'}</td>
              <td>{payment.subscriptionName}</td>
              <td>{payment.billingCycle}</td>
              <td>{payment.provider ?? '-'}</td>
              <td>
                <span className={statusClass(payment.paymentStatus)}>{payment.paymentStatus}</span>
              </td>
              <td>{formatPrice(payment.amount)}</td>
              <td>{formatDateTime(payment.createdAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function EmptyRow({ colSpan }: { colSpan: number }) {
  return (
    <tr>
      <td colSpan={colSpan} className={styles.empty}>
        {'조회된 결제 정보가 없습니다.'}
      </td>
    </tr>
  );
}

function statusClass(status: string): string {
  const stateClass = styles[`status${status}`] ?? '';
  return `${styles.statusBadge} ${stateClass}`;
}
