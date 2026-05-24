/** Admin read-only payment operations view */
import { useCallback, useEffect, useState } from 'react';
import {
  fetchAdminBillingAgreements,
  fetchAdminPaymentOrders,
  fetchAdminPaymentReconciliationIncidents,
  fetchAdminSubscriptionPayments,
  updateAdminPaymentReconciliationIncidentStatus,
  type AdminBillingAgreement,
  type AdminPaymentOrder,
  type AdminPaymentReconciliationIncident,
  type AdminPaymentReconciliationIncidentStatus,
  type AdminSubscriptionPayment,
} from '@/api/admin';
import Pagination from '@/components/ui/Pagination';
import { useToastStore } from '@/store/toastStore';
import type { PageInfo } from '@/types';
import { formatDate, formatDateTime, formatPrice } from '@/utils/format';
import styles from './PaymentReadOnlyPage.module.css';

type TabKey = 'orders' | 'agreements' | 'payments' | 'incidents';

const INCIDENT_STATUSES: AdminPaymentReconciliationIncidentStatus[] = [
  'OPEN',
  'ACKNOWLEDGED',
  'RESOLVED',
  'IGNORED',
];

interface IncidentEdit {
  status: AdminPaymentReconciliationIncidentStatus;
  note: string;
}

export default function PaymentReadOnlyPage() {
  const [tab, setTab] = useState<TabKey>('orders');
  const [page, setPage] = useState(1);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [orders, setOrders] = useState<AdminPaymentOrder[]>([]);
  const [agreements, setAgreements] = useState<AdminBillingAgreement[]>([]);
  const [payments, setPayments] = useState<AdminSubscriptionPayment[]>([]);
  const [incidents, setIncidents] = useState<AdminPaymentReconciliationIncident[]>([]);
  const [incidentStatusFilter, setIncidentStatusFilter] = useState<
    AdminPaymentReconciliationIncidentStatus | ''
  >('OPEN');
  const [incidentEdits, setIncidentEdits] = useState<Record<number, IncidentEdit>>({});
  const [updatingIncidentId, setUpdatingIncidentId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const showToast = useToastStore((s) => s.show);

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
      } else if (tab === 'payments') {
        const result = await fetchAdminSubscriptionPayments(page, 20);
        setPayments(result.dataList);
        setPageInfo(result.pageInfo);
      } else {
        const result = await fetchAdminPaymentReconciliationIncidents(
          page,
          20,
          incidentStatusFilter || undefined,
        );
        setIncidents(result.dataList);
        setIncidentEdits(buildIncidentEdits(result.dataList));
        setPageInfo(result.pageInfo);
      }
    } catch {
      setError('결제 정보를 불러오지 못했습니다.');
      setPageInfo(null);
    } finally {
      setLoading(false);
    }
  }, [incidentStatusFilter, page, tab]);

  useEffect(() => {
    void loadData();
  }, [loadData]);

  function changeTab(next: TabKey) {
    setTab(next);
    setPage(1);
  }

  function changeIncidentFilter(next: AdminPaymentReconciliationIncidentStatus | '') {
    setIncidentStatusFilter(next);
    setPage(1);
  }

  function changeIncidentEdit(incidentId: number, patch: Partial<IncidentEdit>) {
    setIncidentEdits((prev) => ({
      ...prev,
      [incidentId]: {
        status: prev[incidentId]?.status ?? 'OPEN',
        note: prev[incidentId]?.note ?? '',
        ...patch,
      },
    }));
  }

  async function saveIncident(incident: AdminPaymentReconciliationIncident) {
    const edit = incidentEdits[incident.id] ?? {
      status: incident.status,
      note: incident.resolutionNote ?? '',
    };
    setUpdatingIncidentId(incident.id);
    setError(null);
    try {
      await updateAdminPaymentReconciliationIncidentStatus(incident.id, {
        status: edit.status,
        note: edit.note.trim() || undefined,
      });
      showToast('success', 'Incident 상태가 저장되었습니다.');
      await loadData();
    } catch {
      showToast('error', 'Incident 상태를 저장하지 못했습니다.');
    } finally {
      setUpdatingIncidentId(null);
    }
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>{'결제 운영'}</h1>

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
        <button
          className={tab === 'incidents' ? styles.tabActive : styles.tab}
          onClick={() => changeTab('incidents')}
        >
          {'대사 Incident'}
        </button>
      </div>

      {tab === 'incidents' && (
        <div className={styles.filterBar}>
          <span className={styles.filterLabel}>{'상태'}</span>
          <select
            className={styles.filterSelect}
            value={incidentStatusFilter}
            onChange={(e) =>
              changeIncidentFilter(e.target.value as AdminPaymentReconciliationIncidentStatus | '')
            }
          >
            <option value="">{'전체'}</option>
            {INCIDENT_STATUSES.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
        </div>
      )}

      {loading && <div className={styles.loading}>{'불러오는 중...'}</div>}
      {error && <div className={styles.error}>{error}</div>}
      {!loading && !error && tab === 'orders' && <OrderTable orders={orders} />}
      {!loading && !error && tab === 'agreements' && <AgreementTable agreements={agreements} />}
      {!loading && !error && tab === 'payments' && <PaymentTable payments={payments} />}
      {!loading && !error && tab === 'incidents' && (
        <IncidentTable
          incidents={incidents}
          edits={incidentEdits}
          updatingIncidentId={updatingIncidentId}
          onEditChange={changeIncidentEdit}
          onSave={saveIncident}
        />
      )}

      {pageInfo && pageInfo.total > pageInfo.size && (
        <Pagination pageInfo={pageInfo} currentPage={page} onPageChange={setPage} />
      )}
    </div>
  );
}

function buildIncidentEdits(
  incidents: AdminPaymentReconciliationIncident[],
): Record<number, IncidentEdit> {
  return incidents.reduce<Record<number, IncidentEdit>>((acc, incident) => {
    acc[incident.id] = {
      status: incident.status,
      note: incident.resolutionNote ?? '',
    };
    return acc;
  }, {});
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

function IncidentTable({
  incidents,
  edits,
  updatingIncidentId,
  onEditChange,
  onSave,
}: {
  incidents: AdminPaymentReconciliationIncident[];
  edits: Record<number, IncidentEdit>;
  updatingIncidentId: number | null;
  onEditChange: (incidentId: number, patch: Partial<IncidentEdit>) => void;
  onSave: (incident: AdminPaymentReconciliationIncident) => void;
}) {
  return (
    <div className={styles.tableWrap}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>{'심각도'}</th>
            <th>{'유형'}</th>
            <th>{'상태'}</th>
            <th>{'주문번호'}</th>
            <th>{'사용자'}</th>
            <th>{'PG/목적'}</th>
            <th>{'로컬'}</th>
            <th>{'Provider'}</th>
            <th>{'금액'}</th>
            <th>{'발생'}</th>
            <th>{'탐지'}</th>
            <th>{'처리'}</th>
          </tr>
        </thead>
        <tbody>
          {incidents.length === 0 && <EmptyRow colSpan={12} />}
          {incidents.map((incident) => {
            const edit = edits[incident.id] ?? {
              status: incident.status,
              note: incident.resolutionNote ?? '',
            };
            const saving = updatingIncidentId === incident.id;
            return (
              <tr key={incident.id}>
                <td>
                  <span className={severityClass(incident.severity)}>
                    {incident.severity}
                  </span>
                </td>
                <td>
                  <div className={styles.issueType}>{incident.issueType}</div>
                  <div className={styles.subtle}>ID {incident.id}</div>
                </td>
                <td>
                  <span className={statusClass(incident.status)}>{incident.status}</span>
                </td>
                <td>{incident.orderId ?? '-'}</td>
                <td>{incident.userNickname ?? incident.userId ?? '-'}</td>
                <td>
                  <div>{incident.provider ?? '-'}</div>
                  <div className={styles.subtle}>{incident.purpose ?? '-'}</div>
                </td>
                <td>{incident.localStatus ?? '-'}</td>
                <td>
                  <div>{incident.providerStatus ?? '-'}</div>
                  <div className={styles.subtle}>{incident.providerTransactionId ?? '-'}</div>
                </td>
                <td>
                  <div>{formatNullablePrice(incident.localAmount)}</div>
                  <div className={styles.subtle}>{formatNullablePrice(incident.providerAmount)}</div>
                </td>
                <td>{incident.occurrenceCount}</td>
                <td>
                  <div>{formatDateTime(incident.lastDetectedAt)}</div>
                  <div className={styles.subtle}>{formatDateTime(incident.notifiedAt)}</div>
                </td>
                <td>
                  <div className={styles.incidentActions}>
                    <select
                      className={styles.statusSelect}
                      value={edit.status}
                      onChange={(e) =>
                        onEditChange(incident.id, {
                          status: e.target.value as AdminPaymentReconciliationIncidentStatus,
                        })
                      }
                    >
                      {INCIDENT_STATUSES.map((status) => (
                        <option key={status} value={status}>
                          {status}
                        </option>
                      ))}
                    </select>
                    <textarea
                      className={styles.noteInput}
                      value={edit.note}
                      maxLength={500}
                      onChange={(e) => onEditChange(incident.id, { note: e.target.value })}
                      placeholder="처리 메모"
                    />
                    <button
                      className={styles.saveBtn}
                      type="button"
                      disabled={saving}
                      onClick={() => onSave(incident)}
                    >
                      {saving ? '저장 중' : '저장'}
                    </button>
                  </div>
                </td>
              </tr>
            );
          })}
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

function severityClass(severity: string): string {
  const stateClass = styles[`severity${severity}`] ?? '';
  return `${styles.statusBadge} ${stateClass}`;
}

function formatNullablePrice(value: number | null): string {
  return value == null ? '-' : formatPrice(value);
}
