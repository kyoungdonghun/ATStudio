const TOSS_SDK_URL = 'https://js.tosspayments.com/v2/standard';

type TossAmount = {
  value: number;
  currency: string;
};

type TossWidgets = {
  setAmount: (amount: TossAmount) => Promise<void>;
  renderPaymentMethods: (options: { selector: string; variantKey?: string }) => Promise<void>;
  renderAgreement: (options: { selector: string; variantKey?: string }) => Promise<void>;
  requestPayment: (options: {
    orderId: string;
    orderName: string;
    successUrl: string;
    failUrl: string;
  }) => Promise<void>;
};

type TossPayment = {
  requestBillingAuth: (options: {
    method: string;
    successUrl: string;
    failUrl: string;
  }) => Promise<void>;
};

type TossPaymentsInstance = {
  widgets: (options: { customerKey: string }) => TossWidgets;
  payment: (options: { customerKey: string }) => TossPayment;
};

type TossPaymentsFactory = (clientKey: string) => TossPaymentsInstance;

declare global {
  interface Window {
    TossPayments?: TossPaymentsFactory;
  }
}

let sdkPromise: Promise<TossPaymentsFactory> | null = null;

export type { TossPayment, TossWidgets };

export function loadTossPaymentsSdk(): Promise<TossPaymentsFactory> {
  if (window.TossPayments) {
    return Promise.resolve(window.TossPayments);
  }

  if (sdkPromise) {
    return sdkPromise;
  }

  const loadPromise = new Promise<TossPaymentsFactory>((resolve, reject) => {
    const existingScript = document.querySelector<HTMLScriptElement>(
      `script[src="${TOSS_SDK_URL}"]`,
    );

    const resolveIfReady = () => {
      if (window.TossPayments) {
        resolve(window.TossPayments);
        return;
      }
      reject(new Error('Toss Payments SDK를 불러오지 못했습니다.'));
    };

    if (existingScript) {
      existingScript.addEventListener('load', resolveIfReady, { once: true });
      existingScript.addEventListener('error', () => reject(new Error('Toss SDK load failed')), {
        once: true,
      });
      return;
    }

    const script = document.createElement('script');
    script.src = TOSS_SDK_URL;
    script.async = true;
    script.addEventListener('load', resolveIfReady, { once: true });
    script.addEventListener('error', () => reject(new Error('Toss SDK load failed')), {
      once: true,
    });
    document.head.appendChild(script);
  });

  sdkPromise = loadPromise.catch((error: unknown) => {
    sdkPromise = null;
    const failedScript = document.querySelector<HTMLScriptElement>(`script[src="${TOSS_SDK_URL}"]`);
    failedScript?.remove();
    throw error;
  });

  return sdkPromise;
}
